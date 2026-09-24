#!/usr/bin/env node
/** GitHub GraphQL acquisition adapter for Retrospective Evidence v1. */

import { execFileSync } from 'node:child_process';
import { mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { parseArgs } from 'node:util';
import { validateEvidence } from './delivery-retrospective-evidence.mjs';

const DEFAULT_REPO = 'alaiba/arcogine';
const STATE_PATH = fileURLToPath(
  new URL('../../.github/continuous-improvement/retrospective.json', import.meta.url),
);
const SEARCH_LIMIT = 1000;
const REVIEW_PAGE_SIZE = 100;

function usage() {
  return `delivery-retrospective-github -- acquire source-neutral retrospective evidence

USAGE
  node infra/dev/delivery-retrospective-github.mjs --through-pr <number> [options]

OPTIONS
  --repo <owner/name>   Repository (default: ${DEFAULT_REPO})
  --baseline-pr <n>     Override baseline PR from versioned retrospective state
  --through-pr <n>      Last merged PR included in the candidate evidence (required)
  --output <path>       Write evidence JSON to a file (default: stdout)
  --help                Show this help

AUTH
  Uses GH_TOKEN or GITHUB_TOKEN. Otherwise runs \`gh auth token\` once.

The adapter fails closed above GitHub's ${SEARCH_LIMIT}-result search bound and when a
candidate PR has more than ${REVIEW_PAGE_SIZE} submitted reviews.
`;
}

function parseRepo(repo) {
  const [owner, name, extra] = String(repo).split('/');
  if (!owner || !name || extra) throw new Error(`--repo must be owner/name, got "${repo}"`);
  return { owner, name };
}

function positivePr(value, label) {
  const number = Number(value);
  if (!Number.isInteger(number) || number <= 0) throw new Error(`${label} must be a positive integer`);
  return number;
}

function readBaselinePr() {
  let parsed;
  try {
    parsed = JSON.parse(readFileSync(STATE_PATH, 'utf8'));
  } catch (error) {
    throw new Error(`cannot read retrospective state at ${STATE_PATH}: ${error.message}`);
  }
  return positivePr(parsed?.baselinePr, 'retrospective baselinePr');
}

export function resolveToken(env = process.env, run = execFileSync) {
  const fromEnv = env.GH_TOKEN || env.GITHUB_TOKEN;
  if (fromEnv) return fromEnv.trim();
  try {
    return run('gh', ['auth', 'token'], { encoding: 'utf8', stdio: ['ignore', 'pipe', 'ignore'] }).trim();
  } catch {
    throw new Error('no GitHub token: set GH_TOKEN or GITHUB_TOKEN, or install and authenticate the gh CLI');
  }
}

export async function githubGraphql(token, query, variables) {
  const response = await fetch('https://api.github.com/graphql', {
    method: 'POST',
    headers: {
      authorization: `bearer ${token}`,
      'content-type': 'application/json',
      'user-agent': 'arcogine-delivery-retrospective',
      connection: 'close',
    },
    body: JSON.stringify({ query, variables }),
  });
  if (!response.ok) throw new Error(`GitHub GraphQL HTTP ${response.status} ${response.statusText}`);
  const payload = await response.json();
  if (payload.errors?.length) {
    throw new Error(`GitHub GraphQL: ${payload.errors.map((error) => error.message).join('; ')}`);
  }
  return payload.data;
}

const ENDPOINT_QUERY = `
query($owner:String!, $name:String!, $baseline:Int!, $through:Int!) {
  repository(owner:$owner, name:$name) {
    baseline: pullRequest(number:$baseline) { number merged mergedAt baseRefName }
    through: pullRequest(number:$through) { number merged mergedAt baseRefName }
  }
}`;

const SEARCH_QUERY = `
query($query:String!, $cursor:String) {
  search(type:ISSUE, query:$query, first:100, after:$cursor) {
    issueCount
    pageInfo { hasNextPage endCursor }
    nodes {
      ... on PullRequest { number merged mergedAt baseRefName }
    }
  }
}`;

const REVIEW_QUERY = `
query($owner:String!, $name:String!, $number:Int!) {
  repository(owner:$owner, name:$name) {
    pullRequest(number:$number) {
      number merged mergedAt baseRefName
      reviews(first:100) {
        totalCount
        pageInfo { hasNextPage endCursor }
        nodes { id body authorAssociation submittedAt commit { oid } }
      }
    }
  }
}`;

export async function collectSearchPages(fetchPage) {
  const nodes = [];
  const seenCursors = new Set();
  let cursor = null;
  let issueCount = null;
  do {
    const search = await fetchPage(cursor);
    if (
      !search ||
      !Number.isInteger(search.issueCount) ||
      !search.pageInfo ||
      typeof search.pageInfo.hasNextPage !== 'boolean' ||
      !Array.isArray(search.nodes)
    ) {
      throw new Error('retrospective search returned malformed pagination metadata');
    }
    if (issueCount === null) {
      issueCount = search.issueCount;
      if (issueCount > SEARCH_LIMIT) {
        throw new Error(
          `retrospective search matched ${issueCount} PRs; GitHub search cannot prove completeness above ${SEARCH_LIMIT}`,
        );
      }
    } else if (search.issueCount !== issueCount) {
      throw new Error(`retrospective search result count changed during pagination (${issueCount} -> ${search.issueCount})`);
    }
    if (search.nodes.some((node) => !node || !Number.isInteger(node.number))) {
      throw new Error('retrospective search returned a missing or malformed pull request node');
    }
    nodes.push(...search.nodes);
    if (search.pageInfo.hasNextPage && !search.pageInfo.endCursor) {
      throw new Error('retrospective search says another page exists but returned no cursor');
    }
    const nextCursor = search.pageInfo.hasNextPage ? search.pageInfo.endCursor : null;
    if (nextCursor && seenCursors.has(nextCursor)) throw new Error('retrospective search repeated a pagination cursor');
    if (nextCursor) seenCursors.add(nextCursor);
    cursor = nextCursor;
  } while (cursor);

  if (nodes.length !== issueCount) {
    throw new Error(`retrospective search reported ${issueCount} results but fetched ${nodes.length}; refusing partial data`);
  }
  const numbers = new Set();
  for (const node of nodes) {
    if (numbers.has(node.number)) throw new Error(`duplicate PR #${node.number} in retrospective search pages`);
    numbers.add(node.number);
  }
  return nodes;
}

export function serializeReviewConnection(pullRequest) {
  if (!pullRequest) throw new Error('candidate pull request disappeared during review retrieval');
  const connection = pullRequest.reviews;
  if (!connection || !Number.isInteger(connection.totalCount) || !Array.isArray(connection.nodes) || !connection.pageInfo) {
    throw new Error(`PR #${pullRequest.number} returned malformed review connection metadata`);
  }
  if (connection.totalCount > REVIEW_PAGE_SIZE || connection.pageInfo.hasNextPage) {
    throw new Error(
      `PR #${pullRequest.number} has more than ${REVIEW_PAGE_SIZE} submitted reviews; refusing truncated review evidence`,
    );
  }
  if (connection.totalCount !== connection.nodes.length) {
    throw new Error(
      `PR #${pullRequest.number} reported ${connection.totalCount} reviews but fetched ${connection.nodes.length}; refusing partial data`,
    );
  }
  return {
    complete: true,
    reportedCount: connection.totalCount,
    items: connection.nodes.map((review) => ({
      id: review.id ?? null,
      body: review.body ?? null,
      authorAssociation: review.authorAssociation ?? 'UNKNOWN',
      submittedAt: review.submittedAt,
      reviewedHead: review.commit?.oid ?? null,
    })),
  };
}

function utcDay(value) {
  const time = Date.parse(value);
  if (!Number.isFinite(time)) throw new Error(`invalid merge timestamp: ${value}`);
  return new Date(time).toISOString().slice(0, 10);
}

function inExactWindow(candidate, baseline, through) {
  const mergedAt = Date.parse(candidate?.mergedAt);
  if (!Number.isFinite(mergedAt)) {
    throw new Error(`invalid merge timestamp for PR #${candidate?.number ?? '?'}`);
  }
  return candidate.merged === true &&
    candidate.baseRefName === 'main' &&
    mergedAt > Date.parse(baseline.mergedAt) &&
    mergedAt <= Date.parse(through.mergedAt);
}

function endpointFact(pr, label) {
  if (!pr) throw new Error(`${label} pull request was not found`);
  return {
    number: pr.number,
    merged: pr.merged,
    mergedAt: pr.mergedAt,
    baseRefName: pr.baseRefName,
  };
}

/** Acquire a complete source-neutral bundle. `graphql` and `now` are injectable for tests. */
export async function acquireEvidence({
  repo = DEFAULT_REPO,
  baselinePr,
  throughPr,
  graphql = githubGraphql,
  token,
  now = () => new Date().toISOString(),
}) {
  const { owner, name } = parseRepo(repo);
  baselinePr = positivePr(baselinePr, 'baselinePr');
  throughPr = positivePr(throughPr, 'throughPr');
  if (baselinePr === throughPr) throw new Error('baselinePr and throughPr must differ');

  const endpointData = await graphql(token, ENDPOINT_QUERY, {
    owner,
    name,
    baseline: baselinePr,
    through: throughPr,
  });
  const repository = endpointData?.repository;
  const baseline = endpointFact(repository?.baseline, 'baseline');
  const through = endpointFact(repository?.through, 'through');
  const startDay = utcDay(baseline.mergedAt);
  const endDay = utcDay(through.mergedAt);
  const searchText = `repo:${repo} is:pr is:merged base:main merged:${startDay}..${endDay}`;

  const candidates = await collectSearchPages(async (cursor) => {
    const data = await graphql(token, SEARCH_QUERY, { query: searchText, cursor });
    return data?.search;
  });

  const items = [];
  const exactCandidates = candidates.filter((candidate) => inExactWindow(candidate, baseline, through));
  for (const candidate of exactCandidates) {
    const reviewData = await graphql(token, REVIEW_QUERY, { owner, name, number: candidate.number });
    const pullRequest = reviewData?.repository?.pullRequest;
    if (!pullRequest || pullRequest.number !== candidate.number) {
      throw new Error(`PR #${candidate.number} disappeared or changed identity during review retrieval`);
    }
    for (const field of ['merged', 'mergedAt', 'baseRefName']) {
      if (pullRequest[field] !== candidate[field]) {
        throw new Error(`PR #${candidate.number} facts changed during acquisition (${field})`);
      }
    }
    items.push({
      number: candidate.number,
      merged: candidate.merged,
      mergedAt: candidate.mergedAt,
      baseRefName: candidate.baseRefName,
      reviews: serializeReviewConnection(pullRequest),
    });
  }

  const evidence = {
    schemaVersion: 1,
    repository: repo,
    requested: { baselinePr, throughPr },
    endpoints: { baseline, through },
    candidates: { complete: true, reportedCount: items.length, items },
    source: {
      adapter: 'github-graphql',
      acquiredAt: now(),
      candidateSearch: { query: searchText, reportedCount: candidates.length },
      reviewConnectionPageSize: REVIEW_PAGE_SIZE,
    },
  };
  return validateEvidence(evidence);
}

async function main() {
  let parsed;
  try {
    parsed = parseArgs({
      allowPositionals: false,
      options: {
        repo: { type: 'string', default: DEFAULT_REPO },
        'baseline-pr': { type: 'string' },
        'through-pr': { type: 'string' },
        output: { type: 'string' },
        help: { type: 'boolean', default: false },
      },
    });
  } catch (error) {
    process.stderr.write(`${error.message}\n\n${usage()}`);
    return 1;
  }
  if (parsed.values.help) {
    process.stdout.write(usage());
    return 0;
  }

  let baselinePr;
  let throughPr;
  try {
    baselinePr = parsed.values['baseline-pr'] === undefined
      ? readBaselinePr()
      : positivePr(parsed.values['baseline-pr'], '--baseline-pr');
    if (!Number.isInteger(baselinePr) || baselinePr <= 0) throw new Error('--baseline-pr must be a positive integer');
    throughPr = positivePr(parsed.values['through-pr'], '--through-pr');
  } catch (error) {
    process.stderr.write(`${error.message}\n\n${usage()}`);
    return 1;
  }

  let token;
  try {
    token = resolveToken();
  } catch (error) {
    process.stderr.write(`delivery-retrospective-github: ${error.message}\n`);
    return 1;
  }
  return acquireEvidence({ repo: parsed.values.repo, baselinePr, throughPr, token })
    .then((evidence) => {
      const output = `${JSON.stringify(evidence, null, 2)}\n`;
      if (parsed.values.output) {
        const outputPath = resolve(parsed.values.output);
        mkdirSync(dirname(outputPath), { recursive: true });
        writeFileSync(outputPath, output, 'utf8');
      } else {
        process.stdout.write(output);
      }
      return 0;
    })
    .catch((error) => {
      process.stderr.write(`delivery-retrospective-github: ${error.message}\n`);
      return 1;
    });
}

const invokedDirectly = process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href;
if (invokedDirectly) main().then((code) => { process.exitCode = code; });
