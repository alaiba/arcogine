#!/usr/bin/env node
/**
 * pr-reconcile.mjs -- safely bring an open Arcogine PR current with its base.
 *
 * The normal path asks GitHub to merge the current base branch into the PR branch.
 * GitHub owns conflict detection and the branch update, while expected_head_sha makes
 * the request conditional on the exact head that this helper inspected.
 *
 * Usage:
 *   node infra/dev/pr-reconcile.mjs <pr-number>
 *
 * Preconditions:
 *   - PR is open;
 *   - PR head branch lives in the canonical repository;
 *   - an authenticated gh CLI with pull-request write access is available.
 *
 * A local checkout is not mutated or required. Reconciliation is history-preserving:
 * the old PR head remains an ancestor of the resulting head. Any conflict, head/base
 * movement during setup, or failed post-update invariant leaves semantic resolution to
 * the implementation/author rather than attempting it here.
 */

import { execFileSync } from 'node:child_process';
import { parseArgs } from 'node:util';
import { pathToFileURL } from 'node:url';

const DEFAULT_REPO = 'alaiba/arcogine';
const DEFAULT_UPDATE_ATTEMPTS = 10;
const DEFAULT_UPDATE_DELAY_MS = 1000;

function usage() {
  return `pr-reconcile -- safely bring an open PR current with its base

USAGE
  node infra/dev/pr-reconcile.mjs <pr-number> [--repo owner/name]

OPTIONS
  --repo <owner/name>  Repository (default: ${DEFAULT_REPO})
  --help               Show this help

REQUIRES
  An authenticated gh CLI with pull-request write access. The local checkout, if any,
  is not changed.

SAFETY
  The helper asks GitHub to perform its merge-style Update branch operation, bound to the
  exact inspected PR head with expected_head_sha. It never rebases or force-pushes for
  ordinary freshness, and it verifies that the old head and live base are ancestors of
  the resulting non-empty PR head before returning success.
`;
}

function commandRunner(file, args, { allowFailure = false } = {}) {
  try {
    return execFileSync(file, args, {
      encoding: 'utf8',
      stdio: ['ignore', 'pipe', 'pipe'],
    }).trim();
  } catch (error) {
    if (allowFailure) return null;
    const stderr = error?.stderr?.toString?.().trim();
    const detail = stderr ? `: ${stderr}` : '';
    throw new Error(`${file} ${args.join(' ')} failed${detail}`);
  }
}

function parseJson(text, label) {
  try {
    return JSON.parse(text);
  } catch {
    throw new Error(`${label} returned invalid JSON`);
  }
}

function apiPath(repo, suffix) {
  return `repos/${repo}/${suffix}`;
}

function refPath(ref) {
  return encodeURIComponent(ref).replaceAll('%2F', '/');
}

function requireOpenSameRepoPr(pr, repo, number) {
  if (String(pr.state).toLowerCase() !== 'open') {
    throw new Error(`pull request ${repo}#${number} is not open`);
  }
  const headRepo = pr.head?.repo?.full_name;
  if (headRepo !== repo) {
    throw new Error(
      `pull request ${repo}#${number} head lives in ${headRepo ?? '(unknown)'}; ` +
        'cross-repository PR reconciliation is not supported by this helper',
    );
  }
  if (!pr.head?.ref || !pr.head?.sha || !pr.base?.ref) {
    throw new Error(`pull request ${repo}#${number} is missing head/base metadata`);
  }
}

function fetchPr(run, repo, number) {
  return parseJson(run('gh', ['api', apiPath(repo, `pulls/${number}`)]), 'GitHub PR API');
}

function fetchBranchHead(run, repo, ref) {
  const branch = parseJson(
    run('gh', ['api', apiPath(repo, `branches/${refPath(ref)}`)]),
    'GitHub branch API',
  );
  const sha = branch.commit?.sha;
  if (!sha) throw new Error(`GitHub branch API returned no commit SHA for ${repo}:${ref}`);
  return sha;
}

function fetchComparison(run, repo, baseSha, headSha) {
  return parseJson(
    run('gh', [
      'api',
      apiPath(repo, `compare/${refPath(baseSha)}...${refPath(headSha)}`),
    ]),
    'GitHub compare API',
  );
}

function comparisonDistance(comparison, field) {
  const value = comparison[field];
  if (!Number.isInteger(value) || value < 0) {
    throw new Error(`GitHub compare API returned invalid ${field}`);
  }
  return value;
}

function requireNonEmptyChange(comparison, message) {
  const aheadBy = comparisonDistance(comparison, 'ahead_by');
  if (aheadBy <= 0 || (Array.isArray(comparison.files) && comparison.files.length === 0)) {
    throw new Error(message);
  }
}

function requireStableSetup(initialPr, initialBase, currentPr, currentBase, repo, number) {
  requireOpenSameRepoPr(currentPr, repo, number);
  if (currentPr.base.ref !== initialPr.base.ref) {
    throw new Error(
      `PR base ref moved during reconciliation setup: expected ${initialPr.base.ref}, fetched ${currentPr.base.ref}`,
    );
  }
  if (currentPr.head.ref !== initialPr.head.ref || currentPr.head.sha !== initialPr.head.sha) {
    throw new Error(
      `PR head moved during reconciliation setup: expected ${initialPr.head.sha}, fetched ${currentPr.head.sha}`,
    );
  }
  if (currentBase !== initialBase) {
    throw new Error(
      `PR base moved during reconciliation setup: expected ${initialBase}, fetched ${currentBase}`,
    );
  }
}

function updateBranch(run, repo, number, expectedHead) {
  try {
    run('gh', [
      'api',
      apiPath(repo, `pulls/${number}/update-branch`),
      '--method',
      'PUT',
      '-H',
      'Accept: application/vnd.github+json',
      '-f',
      `expected_head_sha=${expectedHead}`,
    ]);
  } catch (error) {
    throw new Error(
      `merge-style Update branch failed; remote PR branch was not changed: ${error.message}`,
    );
  }
}

async function waitForUpdatedPr({
  run,
  repo,
  number,
  oldHead,
  attempts,
  delayMs,
  sleep,
}) {
  for (let attempt = 0; attempt < attempts; attempt += 1) {
    const pr = fetchPr(run, repo, number);
    requireOpenSameRepoPr(pr, repo, number);
    if (pr.head.sha !== oldHead) return pr;
    if (attempt + 1 < attempts) await sleep(delayMs);
  }
  throw new Error(`merge-style Update branch did not advance PR #${number} from ${oldHead}`);
}

async function reconcilePr({
  number,
  repo = DEFAULT_REPO,
  run = commandRunner,
  log = console.log,
  sleep = (delayMs) => new Promise((resolve) => setTimeout(resolve, delayMs)),
  attempts = DEFAULT_UPDATE_ATTEMPTS,
  delayMs = DEFAULT_UPDATE_DELAY_MS,
}) {
  if (!Number.isInteger(number) || number <= 0) throw new Error(`invalid PR number: ${number}`);
  if (!/^[^/]+\/[^/]+$/.test(repo)) throw new Error(`--repo must be owner/name, got "${repo}"`);
  if (!Number.isInteger(attempts) || attempts <= 0) throw new Error(`invalid update attempts: ${attempts}`);
  if (!Number.isInteger(delayMs) || delayMs < 0) throw new Error(`invalid update delay: ${delayMs}`);

  const pr = fetchPr(run, repo, number);
  requireOpenSameRepoPr(pr, repo, number);
  const oldHead = pr.head.sha;
  const baseRef = pr.base.ref;
  const liveBase = fetchBranchHead(run, repo, baseRef);
  const before = fetchComparison(run, repo, liveBase, oldHead);
  const behindBy = comparisonDistance(before, 'behind_by');

  if (behindBy === 0) {
    requireNonEmptyChange(
      before,
      `PR #${number} has no diff against ${baseRef}; refusing to treat an empty PR as current`,
    );
    log(`PR #${number} is already level with ${baseRef}; nothing to do.`);
    return { changed: false, oldHead, newHead: oldHead, baseRef };
  }

  requireNonEmptyChange(
    before,
    `PR #${number} has no diff against ${baseRef}; refusing reconciliation that could collapse the PR`,
  );

  // Re-resolve the snapshot immediately before the platform mutation. The API's
  // expected_head_sha remains the atomic guard for a race after this check.
  const setupPr = fetchPr(run, repo, number);
  const setupBase = fetchBranchHead(run, repo, baseRef);
  requireStableSetup(pr, liveBase, setupPr, setupBase, repo, number);

  updateBranch(run, repo, number, oldHead);

  const afterPr = await waitForUpdatedPr({
    run,
    repo,
    number,
    oldHead,
    attempts,
    delayMs,
    sleep,
  });
  if (afterPr.base.ref !== baseRef) {
    throw new Error(`post-update PR base ref changed from ${baseRef} to ${afterPr.base.ref}`);
  }
  const newHead = afterPr.head.sha;
  const afterBase = fetchBranchHead(run, repo, baseRef);

  const oldHeadRelation = fetchComparison(run, repo, oldHead, newHead);
  if (comparisonDistance(oldHeadRelation, 'behind_by') !== 0) {
    throw new Error(`post-update verification failed: old PR head ${oldHead} is not an ancestor of ${newHead}`);
  }

  const after = fetchComparison(run, repo, afterBase, newHead);
  if (comparisonDistance(after, 'behind_by') !== 0) {
    throw new Error(`post-update verification failed: PR is still behind ${baseRef}`);
  }
  requireNonEmptyChange(
    after,
    `post-update verification failed: PR #${number} no longer has a non-empty diff against ${baseRef}`,
  );

  log(`Reconciled PR #${number}: ${oldHead} -> ${newHead}; ${baseRef} is now current.`);
  return { changed: true, oldHead, newHead, baseRef };
}

function parseCli(argv) {
  const { values, positionals } = parseArgs({
    args: argv,
    options: {
      repo: { type: 'string', default: DEFAULT_REPO },
      help: { type: 'boolean', default: false },
    },
    allowPositionals: true,
  });
  if (values.help) return { help: true };
  if (positionals.length !== 1 || !/^\d+$/.test(positionals[0])) {
    throw new Error('exactly one numeric PR number is required');
  }
  return { help: false, number: Number(positionals[0]), repo: values.repo };
}

async function main(argv = process.argv.slice(2)) {
  try {
    const args = parseCli(argv);
    if (args.help) {
      process.stdout.write(usage());
      return 0;
    }
    await reconcilePr(args);
    return 0;
  } catch (error) {
    console.error(`pr-reconcile: ${error.message}`);
    return 1;
  }
}

if (import.meta.url === pathToFileURL(process.argv[1]).href) {
  process.exitCode = await main();
}

export {
  DEFAULT_REPO,
  reconcilePr,
  requireOpenSameRepoPr,
  parseCli,
  usage,
};
