#!/usr/bin/env node
/**
 * delivery-retrospective.mjs -- mechanically establish one retrospective delivery window.
 *
 * The helper owns facts that should not be hand-counted:
 * - the exact main-target PRs merged in (baseline merge time, through-PR merge time];
 * - the number of canonical CHANGES REQUIRED review submissions;
 * - the per-PR 0 / 1 / 2 / 3+ blocking-review checkpoint distribution.
 *
 * Interpretation, semantic finding classification, and improvement decisions remain human/reviewer work.
 */

import { execFileSync } from 'node:child_process';
import { readFileSync } from 'node:fs';
import { parseArgs } from 'node:util';
import { fileURLToPath, pathToFileURL } from 'node:url';

import { dispositionOf } from './pr-lifecycle.mjs';

const DEFAULT_REPO = 'alaiba/arcogine';
const STATE_PATH = fileURLToPath(
  new URL('../../.github/continuous-improvement/retrospective.json', import.meta.url),
);

function usage() {
  return `delivery-retrospective -- establish an exact retrospective PR/review window

USAGE
  node infra/dev/delivery-retrospective.mjs --through-pr <number> [options]

OPTIONS
  --repo <owner/name>   Repository (default: ${DEFAULT_REPO})
  --baseline-pr <n>     Override baseline PR from versioned retrospective state
  --through-pr <n>      Last merged PR included in the window (required)
  --json                Emit machine-readable JSON
  --help                Show this help

AUTH
  Uses GH_TOKEN or GITHUB_TOKEN. Otherwise runs \`gh auth token\` once.

The command fails closed on incomplete GitHub search results, >1000 search matches,
or a PR with >100 submitted reviews rather than publishing partial counts.
`;
}

function resolveToken() {
  const fromEnv = process.env.GH_TOKEN || process.env.GITHUB_TOKEN;
  if (fromEnv) return fromEnv.trim();
  try {
    return execFileSync('gh', ['auth', 'token'], {
      encoding: 'utf8',
      stdio: ['ignore', 'pipe', 'ignore'],
    }).trim();
  } catch {
    throw new Error(
      'no GitHub token: set GH_TOKEN or GITHUB_TOKEN, or install and authenticate the gh CLI',
    );
  }
}

function readBaselinePr() {
  let parsed;
  try {
    parsed = JSON.parse(readFileSync(STATE_PATH, 'utf8'));
  } catch (error) {
    throw new Error(`cannot read retrospective state at ${STATE_PATH}: ${error.message}`);
  }
  const baselinePr = parsed?.baselinePr;
  if (!Number.isInteger(baselinePr) || baselinePr <= 0) {
    throw new Error('retrospective state must contain a positive integer baselinePr');
  }
  return baselinePr;
}

async function githubGraphql(token, query, variables) {
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
  if (!response.ok) {
    throw new Error(`GitHub GraphQL HTTP ${response.status} ${response.statusText}`);
  }
  const payload = await response.json();
  if (payload.errors?.length) {
    throw new Error(`GitHub GraphQL: ${payload.errors.map((e) => e.message).join('; ')}`);
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
      ... on PullRequest {
        number
        merged
        mergedAt
        baseRefName
        reviews(first:100) {
          totalCount
          nodes { body }
        }
      }
    }
  }
}`;

function parseRepo(repo) {
  const [owner, name, extra] = String(repo).split('/');
  if (!owner || !name || extra) throw new Error(`--repo must be owner/name, got "${repo}"`);
  return { owner, name };
}

function assertMergedEndpoint(pr, label) {
  if (!pr) throw new Error(`${label} pull request was not found`);
  if (!pr.merged || !pr.mergedAt) throw new Error(`${label} PR #${pr.number} is not merged`);
  if (pr.baseRefName !== 'main') {
    throw new Error(`${label} PR #${pr.number} targets ${pr.baseRefName}, not main`);
  }
}

function utcDay(iso) {
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) throw new Error(`invalid timestamp: ${iso}`);
  return date.toISOString().slice(0, 10);
}

function buildExactWindow(records, baselineMergedAt, throughMergedAt) {
  const start = new Date(baselineMergedAt).getTime();
  const end = new Date(throughMergedAt).getTime();
  if (!Number.isFinite(start) || !Number.isFinite(end) || end <= start) {
    throw new Error('through PR must merge after the baseline PR');
  }

  const byNumber = new Map();
  for (const record of records) {
    if (!record?.merged || !record.mergedAt || record.baseRefName !== 'main') continue;
    const merged = new Date(record.mergedAt).getTime();
    if (merged <= start || merged > end) continue;
    if (byNumber.has(record.number)) {
      throw new Error(`duplicate PR #${record.number} in retrospective search results`);
    }
    byNumber.set(record.number, record);
  }

  return [...byNumber.values()].sort((a, b) => {
    const time = String(a.mergedAt).localeCompare(String(b.mergedAt));
    return time || a.number - b.number;
  });
}

async function collectSearchPages(fetchPage) {
  const nodes = [];
  let cursor = null;
  let issueCount = null;

  do {
    const search = await fetchPage(cursor);
    if (!search || !Number.isInteger(search.issueCount) || !search.pageInfo) {
      throw new Error('retrospective search returned malformed pagination metadata');
    }
    if (issueCount === null) {
      issueCount = search.issueCount;
      if (issueCount > 1000) {
        throw new Error(
          `retrospective search matched ${issueCount} PRs; GitHub search cannot prove completeness above 1000`,
        );
      }
    } else if (search.issueCount !== issueCount) {
      throw new Error(
        `retrospective search result count changed during pagination (${issueCount} -> ${search.issueCount})`,
      );
    }

    nodes.push(...(search.nodes ?? []).filter(Boolean));
    if (search.pageInfo.hasNextPage && !search.pageInfo.endCursor) {
      throw new Error('retrospective search says another page exists but returned no cursor');
    }
    cursor = search.pageInfo.hasNextPage ? search.pageInfo.endCursor : null;
  } while (cursor);

  if (nodes.length !== issueCount) {
    throw new Error(
      `retrospective search reported ${issueCount} results but fetched ${nodes.length}; refusing partial data`,
    );
  }
  return nodes;
}

function changesRequiredCount(record) {
  const reviews = record?.reviews;
  if (!reviews) throw new Error(`PR #${record?.number ?? '?'} has no review payload`);
  if (reviews.totalCount > reviews.nodes.length) {
    throw new Error(
      `PR #${record.number} has ${reviews.totalCount} reviews but only ${reviews.nodes.length} were fetched`,
    );
  }
  return reviews.nodes.filter((review) => dispositionOf(review.body) === 'CHANGES REQUIRED').length;
}

function summarizeWindow(window) {
  const rounds = window.map((record) => changesRequiredCount(record));
  const distribution = { zero: 0, one: 0, two: 0, threePlus: 0 };
  for (const count of rounds) {
    if (count === 0) distribution.zero += 1;
    else if (count === 1) distribution.one += 1;
    else if (count === 2) distribution.two += 1;
    else distribution.threePlus += 1;
  }
  return {
    mergedPrCount: window.length,
    changeRequiredSubmissions: rounds.reduce((sum, count) => sum + count, 0),
    reviewRoundDistribution: distribution,
  };
}

async function fetchWindow({ repo, baselinePr, throughPr, token }) {
  const { owner, name } = parseRepo(repo);
  const endpointData = await githubGraphql(token, ENDPOINT_QUERY, {
    owner,
    name,
    baseline: baselinePr,
    through: throughPr,
  });
  const baseline = endpointData.repository?.baseline;
  const through = endpointData.repository?.through;
  assertMergedEndpoint(baseline, 'baseline');
  assertMergedEndpoint(through, 'through');

  const startDay = utcDay(baseline.mergedAt);
  const endDay = utcDay(through.mergedAt);
  const searchText =
    `repo:${repo} is:pr is:merged base:main merged:${startDay}..${endDay}`;

  const nodes = await collectSearchPages(async (cursor) => {
    const data = await githubGraphql(token, SEARCH_QUERY, { query: searchText, cursor });
    return data.search;
  });

  const window = buildExactWindow(nodes, baseline.mergedAt, through.mergedAt);
  if (!window.some((pr) => pr.number === throughPr)) {
    throw new Error(`through PR #${throughPr} is missing from the exact retrospective window`);
  }

  return {
    repository: repo,
    baselinePr,
    baselineMergedAt: baseline.mergedAt,
    throughPr,
    throughMergedAt: through.mergedAt,
    pullRequests: window.map((pr) => ({ number: pr.number, mergedAt: pr.mergedAt })),
    ...summarizeWindow(window),
  };
}

function render(summary) {
  const d = summary.reviewRoundDistribution;
  return [
    `Retrospective window: PR #${summary.baselinePr} (exclusive) -> PR #${summary.throughPr} (inclusive)`,
    `Merged PRs: ${summary.mergedPrCount}`,
    `CHANGES REQUIRED submissions: ${summary.changeRequiredSubmissions}`,
    `Review checkpoints: ${d.zero} zero / ${d.one} one / ${d.two} two / ${d.threePlus} three-plus`,
    `PRs: ${summary.pullRequests.map((pr) => `#${pr.number}`).join(', ')}`,
  ].join('\n');
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
        json: { type: 'boolean', default: false },
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

  const throughPr = Number(parsed.values['through-pr']);
  if (!Number.isInteger(throughPr) || throughPr <= 0) {
    process.stderr.write('--through-pr must be a positive integer\n');
    return 1;
  }

  const baselinePr = parsed.values['baseline-pr'] === undefined
    ? readBaselinePr()
    : Number(parsed.values['baseline-pr']);
  if (!Number.isInteger(baselinePr) || baselinePr <= 0) {
    process.stderr.write('--baseline-pr must be a positive integer\n');
    return 1;
  }

  try {
    const summary = await fetchWindow({
      repo: parsed.values.repo,
      baselinePr,
      throughPr,
      token: resolveToken(),
    });
    process.stdout.write(
      parsed.values.json ? `${JSON.stringify(summary, null, 2)}\n` : `${render(summary)}\n`,
    );
    return 0;
  } catch (error) {
    process.stderr.write(`delivery-retrospective: ${error.message}\n`);
    return 1;
  }
}

export { buildExactWindow, changesRequiredCount, collectSearchPages, summarizeWindow };

const invokedDirectly =
  process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href;

if (invokedDirectly) {
  main().then(
    (code) => { process.exitCode = code; },
    (error) => {
      process.stderr.write(`delivery-retrospective: ${error?.stack ?? error}\n`);
      process.exitCode = 1;
    },
  );
}
