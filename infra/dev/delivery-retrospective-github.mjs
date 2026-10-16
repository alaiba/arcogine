#!/usr/bin/env node
/** GitHub GraphQL acquisition adapter for Retrospective Evidence v2. */

import { execFileSync } from 'node:child_process';
import { mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { parseArgs } from 'node:util';
import { EVIDENCE_SCHEMA_VERSION, validateEvidence } from './delivery-retrospective-evidence.mjs';

const DEFAULT_REPO = 'alaiba/arcogine';
const STATE_PATH = fileURLToPath(
  new URL('../../.github/continuous-improvement/retrospective.json', import.meta.url),
);
const SEARCH_LIMIT = 1000;
const REVIEW_PAGE_SIZE = 100;
const HANDOFF_LABEL = /^\s*Implementation handoff:/;
const HANDOFF_LINE =
  /^\s*Implementation handoff:\s+`?([^\s`]+)`?\s+at\s+`?([0-9a-fA-F]{40})`?,\s+committed\s+`?(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d+)?(?:Z|[+-]\d{2}:\d{2}))`?\.?\s*$/;

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

The adapter fails closed above GitHub's ${SEARCH_LIMIT}-result search bound, when a
candidate PR has more than ${REVIEW_PAGE_SIZE} submitted reviews, when the issue listing
changes size during pagination, and when a PR's "Implementation handoff:" line is malformed,
repeated, or names a commit, path, or commit date that GitHub does not confirm.
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

const PULL_REQUEST_QUERY = `
query($owner:String!, $name:String!, $number:Int!) {
  repository(owner:$owner, name:$name) {
    pullRequest(number:$number) {
      number merged mergedAt baseRefName body
      reviews(first:100) {
        totalCount
        pageInfo { hasNextPage endCursor }
        nodes { id body authorAssociation submittedAt commit { oid } }
      }
    }
  }
}`;

const HANDOFF_QUERY = `
query($owner:String!, $name:String!, $oid:GitObjectID!, $expression:String!) {
  repository(owner:$owner, name:$name) {
    commit: object(oid:$oid) { ... on Commit { oid committedDate } }
    file: object(expression:$expression) { ... on Blob { oid } }
  }
}`;

// The whole issue listing in creation order, without a state filter: an issue that closes or reopens
// during acquisition still appears exactly once, which separate open and closed searches cannot prove.
const ISSUE_LISTING_QUERY = `
query($owner:String!, $name:String!, $cursor:String) {
  repository(owner:$owner, name:$name) {
    issues(first:100, after:$cursor, orderBy:{field:CREATED_AT, direction:ASC}) {
      totalCount
      pageInfo { hasNextPage endCursor }
      nodes {
        number createdAt closedAt
        timelineItems(itemTypes:[CLOSED_EVENT], last:1) {
          nodes { ... on ClosedEvent { closer { __typename ... on PullRequest { number repository { nameWithOwner } } } } }
        }
      }
    }
  }
}`;

async function collectPages(fetchPage, { source, subject, countField, limit = Number.POSITIVE_INFINITY }) {
  const nodes = [];
  const seenCursors = new Set();
  let cursor = null;
  let count = null;
  do {
    const page = await fetchPage(cursor);
    if (
      !page ||
      !Number.isInteger(page[countField]) ||
      !page.pageInfo ||
      typeof page.pageInfo.hasNextPage !== 'boolean' ||
      !Array.isArray(page.nodes)
    ) {
      throw new Error(`retrospective ${source} returned malformed pagination metadata`);
    }
    if (count === null) {
      count = page[countField];
      if (count > limit) {
        throw new Error(
          `retrospective ${source} matched ${count} ${subject} results; GitHub search cannot prove completeness above ${limit}`,
        );
      }
    } else if (page[countField] !== count) {
      throw new Error(`retrospective ${source} result count changed during pagination (${count} -> ${page[countField]})`);
    }
    if (page.nodes.some((node) => !node || !Number.isInteger(node.number))) {
      throw new Error(`retrospective ${source} returned a missing or malformed ${subject} node`);
    }
    nodes.push(...page.nodes);
    if (page.pageInfo.hasNextPage && !page.pageInfo.endCursor) {
      throw new Error(`retrospective ${source} says another page exists but returned no cursor`);
    }
    const nextCursor = page.pageInfo.hasNextPage ? page.pageInfo.endCursor : null;
    if (nextCursor && seenCursors.has(nextCursor)) throw new Error(`retrospective ${source} repeated a pagination cursor`);
    if (nextCursor) seenCursors.add(nextCursor);
    cursor = nextCursor;
  } while (cursor);

  if (nodes.length !== count) {
    throw new Error(`retrospective ${source} reported ${count} results but fetched ${nodes.length}; refusing partial data`);
  }
  const numbers = new Set();
  for (const node of nodes) {
    if (numbers.has(node.number)) throw new Error(`duplicate ${subject} #${node.number} in retrospective ${source} pages`);
    numbers.add(node.number);
  }
  return nodes;
}

export function collectSearchPages(fetchPage) {
  return collectPages(fetchPage, { source: 'search', subject: 'PR', countField: 'issueCount', limit: SEARCH_LIMIT });
}

/** Page through the repository's complete issue listing; it has no search result cap. */
export function collectIssuePages(fetchPage) {
  return collectPages(fetchPage, { source: 'issue listing', subject: 'issue', countField: 'totalCount' });
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
      authorAssociation: review.authorAssociation ?? null,
      submittedAt: review.submittedAt,
      reviewedHead: review.commit?.oid ?? null,
    })),
  };
}

function unquotedLines(body) {
  const lines = [];
  let fence = null;
  for (const line of String(body ?? '').split(/\r?\n/)) {
    if (fence) {
      const close = line.match(/^\s*(`{3,}|~{3,})\s*$/);
      if (close && close[1][0] === fence[0] && close[1].length >= fence.length) fence = null;
      continue;
    }
    const open = line.match(/^\s*(`{3,}|~{3,})/);
    if (open) {
      fence = open[1];
      continue;
    }
    if (!/^\s*>/.test(line)) lines.push(line);
  }
  return lines;
}

/**
 * Read the implementation handoff a PR body names on its standalone
 * `Implementation handoff: <path> at <commit>, committed <commit date>` line, or null.
 * Fenced and quoted text is ignored; a malformed or repeated line fails closed.
 */
export function parseHandoffReference(body, prNumber) {
  const references = [];
  for (const line of unquotedLines(body)) {
    if (!HANDOFF_LABEL.test(line)) continue;
    const match = line.match(HANDOFF_LINE);
    if (!match) {
      throw new Error(
        `PR #${prNumber} has a malformed "Implementation handoff:" line; expected ` +
          '"Implementation handoff: <path> at <full commit SHA>, committed <ISO 8601 commit date>"',
      );
    }
    references.push({ path: match[1], commit: match[2].toLowerCase(), committedAt: match[3] });
  }
  if (references.length > 1) {
    throw new Error(`PR #${prNumber} names ${references.length} implementation handoffs; a pull request implements one`);
  }
  return references[0] ?? null;
}

/** Confirm a named handoff against GitHub and return its evidence facts. */
export async function resolveHandoff({ graphql, token, owner, name, prNumber, named }) {
  const data = await graphql(token, HANDOFF_QUERY, {
    owner,
    name,
    oid: named.commit,
    expression: `${named.commit}:${named.path}`,
  });
  const committedDate = data?.repository?.commit?.committedDate;
  if (typeof committedDate !== 'string' || !Number.isFinite(Date.parse(committedDate))) {
    throw new Error(`PR #${prNumber} names handoff commit ${named.commit}, which GitHub cannot resolve`);
  }
  if (!data.repository.file?.oid) {
    throw new Error(`PR #${prNumber} names handoff ${named.path}, which is not a file at commit ${named.commit}`);
  }
  if (Date.parse(committedDate) !== Date.parse(named.committedAt)) {
    throw new Error(
      `PR #${prNumber} names handoff commit date ${named.committedAt}, but GitHub records ${committedDate}`,
    );
  }
  return { path: named.path, commit: named.commit, committedAt: committedDate };
}

/**
 * Serialize an issue's opening and closing times and the pull request in `repo` that GitHub records
 * as the closer of its current closure. A pull request that only links the issue as one it may close
 * is not a closer; an open issue, or one closed some other way, has none.
 */
export function serializeIssue(node, repo) {
  const events = node?.timelineItems?.nodes;
  if (!Number.isInteger(node?.number) || !Array.isArray(events)) {
    throw new Error(`issue listing returned malformed facts for issue #${node?.number ?? '?'}`);
  }
  const closedAt = node.closedAt ?? null;
  const closer = closedAt === null ? null : events[0]?.closer;
  const closedByPullRequest =
    closer?.__typename === 'PullRequest' &&
    Number.isInteger(closer.number) &&
    String(closer.repository?.nameWithOwner).toLowerCase() === repo.toLowerCase()
      ? closer.number
      : null;
  return { number: node.number, createdAt: node.createdAt, closedAt, closedByPullRequest };
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
    const pullRequestData = await graphql(token, PULL_REQUEST_QUERY, { owner, name, number: candidate.number });
    const pullRequest = pullRequestData?.repository?.pullRequest;
    if (!pullRequest || pullRequest.number !== candidate.number) {
      throw new Error(`PR #${candidate.number} disappeared or changed identity during review retrieval`);
    }
    for (const field of ['merged', 'mergedAt', 'baseRefName']) {
      if (pullRequest[field] !== candidate[field]) {
        throw new Error(`PR #${candidate.number} facts changed during acquisition (${field})`);
      }
    }
    const named = parseHandoffReference(pullRequest.body, candidate.number);
    const handoff = named
      ? await resolveHandoff({ graphql, token, owner, name, prNumber: candidate.number, named })
      : null;
    items.push({
      number: candidate.number,
      merged: candidate.merged,
      mergedAt: candidate.mergedAt,
      baseRefName: candidate.baseRefName,
      reviews: serializeReviewConnection(pullRequest),
      handoff,
    });
  }

  // Issues open at some point in the window, each with its recorded closer, from one listing of every
  // issue regardless of state.
  const start = Date.parse(baseline.mergedAt);
  const end = Date.parse(through.mergedAt);
  const listed = await collectIssuePages(async (cursor) => {
    const data = await graphql(token, ISSUE_LISTING_QUERY, { owner, name, cursor });
    return data?.repository?.issues;
  });
  const issueItems = listed
    .map((node) => serializeIssue(node, repo))
    .filter((issue) =>
      Date.parse(issue.createdAt) <= end && (issue.closedAt === null || Date.parse(issue.closedAt) > start),
    )
    .sort((left, right) => left.number - right.number);

  const evidence = {
    schemaVersion: EVIDENCE_SCHEMA_VERSION,
    repository: repo,
    requested: { baselinePr, throughPr },
    endpoints: { baseline, through },
    candidates: { complete: true, reportedCount: items.length, items },
    issues: { complete: true, reportedCount: issueItems.length, items: issueItems },
    source: {
      adapter: 'github-graphql',
      acquiredAt: now(),
      candidateSearch: { query: searchText, reportedCount: candidates.length },
      reviewConnectionPageSize: REVIEW_PAGE_SIZE,
      issueListing: { reportedCount: listed.length },
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
