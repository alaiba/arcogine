#!/usr/bin/env node
/**
 * pr-reconcile.mjs -- safely rebase an open Arcogine PR branch onto its current base.
 *
 * The remote PR branch is updated exactly once, from the old PR head directly to the
 * completed rebased head. The helper never points the PR branch at the base commit as an
 * intermediate state, because GitHub can automatically close an open PR when head == base.
 *
 * Usage:
 *   node infra/dev/pr-reconcile.mjs <pr-number>
 *
 * Preconditions:
 *   - clean working tree;
 *   - current branch is the PR head branch;
 *   - local HEAD exactly matches the live remote PR head;
 *   - PR is open and its head branch lives in the canonical repository;
 *   - git and an authenticated gh CLI are available.
 *
 * Reconciliation is a rebase, not a merge, to preserve Arcogine's linear-history policy.
 * Conflicts abort the operation before any remote ref changes.
 */

import { execFileSync } from 'node:child_process';
import { parseArgs } from 'node:util';
import { pathToFileURL } from 'node:url';

const DEFAULT_REPO = 'alaiba/arcogine';

function usage() {
  return `pr-reconcile -- safely rebase an open PR branch onto its current base

USAGE
  node infra/dev/pr-reconcile.mjs <pr-number> [--repo owner/name]

OPTIONS
  --repo <owner/name>  Repository (default: ${DEFAULT_REPO})
  --help               Show this help

REQUIRES
  git and an authenticated gh CLI. Run from a clean checkout of the PR head branch.

SAFETY
  The PR branch is never moved to the base commit as an intermediate step. The helper
  constructs the complete rebased head locally, verifies that the PR diff is still
  non-empty and the base is an ancestor, then performs one force-with-lease push directly
  from the old remote head to the final rebased head.
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

function remoteIdentity(remoteUrl) {
  const value = String(remoteUrl ?? '').trim().replace(/\/+$/, '').replace(/\.git$/, '');
  const scp = value.match(/^[^@]+@([^:]+):(.+)$/);
  if (scp) return `${scp[1].toLowerCase()}/${scp[2]}`.replace(/\/+/g, '/');
  try {
    const url = new URL(value);
    return `${url.hostname.toLowerCase()}${url.pathname.replace(/\/+/g, '/')}`;
  } catch {
    return null;
  }
}

function requireCanonicalOrigin(run, pr, repo) {
  const fetchUrls = run('git', ['remote', 'get-url', '--all', 'origin']).split(/\r?\n/).filter(Boolean);
  const pushUrls = run('git', ['remote', 'get-url', '--push', '--all', 'origin'])
    .split(/\r?\n/).filter(Boolean);
  const candidates = [pr.head.repo?.html_url, pr.head.repo?.ssh_url, pr.head.repo?.clone_url]
    .map(remoteIdentity)
    .filter(Boolean);
  const allUrls = [...fetchUrls, ...pushUrls];
  if (!allUrls.length || allUrls.some((url) => !candidates.includes(remoteIdentity(url)))) {
    throw new Error(`origin fetch/push remote ${allUrls.join(', ') || '(missing)'} is not the PR head repository ${repo}; refusing remote mutation`);
  }
}

function fetchPr(run, repo, number) {
  return parseJson(run('gh', ['api', apiPath(repo, `pulls/${number}`)]), 'GitHub PR API');
}

function fetchComparison(run, repo, baseRef, headSha) {
  const encodedBase = encodeURIComponent(baseRef);
  return parseJson(
    run('gh', ['api', apiPath(repo, `compare/${encodedBase}...${headSha}`)]),
    'GitHub compare API',
  );
}

function fetchRemoteRef(run, remote, ref) {
  const output = run('git', ['ls-remote', remote, ref]);
  const match = output.match(/^([0-9a-f]{40})\s+(.+)$/m);
  if (!match || match[2] !== ref) {
    throw new Error(`could not resolve ${remote} ${ref}`);
  }
  return match[1];
}

function ensureNonEmptyDiff(run, baseRemote, message) {
  const files = run('git', ['diff', '--name-only', `${baseRemote}...HEAD`]);
  if (!files.trim()) throw new Error(message);
  return files.split(/\r?\n/).filter(Boolean);
}

async function reconcilePr({ number, repo = DEFAULT_REPO, run = commandRunner, log = console.log }) {
  if (!Number.isInteger(number) || number <= 0) throw new Error(`invalid PR number: ${number}`);
  if (!/^[^/]+\/[^/]+$/.test(repo)) throw new Error(`--repo must be owner/name, got "${repo}"`);

  if (run('git', ['status', '--porcelain'])) {
    throw new Error('working tree is not clean; commit/stash changes before reconciling');
  }

  const branch = run('git', ['branch', '--show-current']);
  if (!branch) throw new Error('detached HEAD is not supported; check out the PR branch first');

  const pr = fetchPr(run, repo, number);
  requireOpenSameRepoPr(pr, repo, number);
  requireCanonicalOrigin(run, pr, repo);

  const headRef = pr.head.ref;
  const baseRef = pr.base.ref;
  const oldHead = pr.head.sha;

  if (branch !== headRef) {
    throw new Error(`current branch is ${branch}, but PR #${number} head branch is ${headRef}`);
  }

  const localHead = run('git', ['rev-parse', 'HEAD']);
  if (localHead !== oldHead) {
    throw new Error(
      `local HEAD ${localHead} does not match live PR head ${oldHead}; fetch/reset deliberately before reconciling`,
    );
  }

  const baseRemote = `refs/remotes/origin/${baseRef}`;
  const headRemote = `refs/remotes/origin/${headRef}`;
  run('git', [
    'fetch',
    'origin',
    `+refs/heads/${baseRef}:${baseRemote}`,
    `+refs/heads/${headRef}:${headRemote}`,
  ]);

  const fetchedHead = run('git', ['rev-parse', headRemote]);
  if (fetchedHead !== oldHead) {
    throw new Error(`PR head moved during reconciliation setup: expected ${oldHead}, fetched ${fetchedHead}`);
  }
  const fetchedBase = run('git', ['rev-parse', baseRemote]);
  const liveBase = fetchRemoteRef(run, 'origin', `refs/heads/${baseRef}`);
  if (fetchedBase !== liveBase) {
    throw new Error(`PR base moved during reconciliation setup: expected ${fetchedBase}, fetched ${liveBase}`);
  }

  const before = fetchComparison(run, repo, baseRef, oldHead);
  if ((before.behind_by ?? 0) === 0) {
    log(`PR #${number} is already level with ${baseRef}; nothing to do.`);
    return { changed: false, oldHead, newHead: oldHead, baseRef };
  }

  ensureNonEmptyDiff(
    run,
    baseRemote,
    `PR #${number} has no diff against ${baseRef}; refusing reconciliation that could collapse the PR`,
  );

  try {
    run('git', ['rebase', baseRemote]);
  } catch (error) {
    run('git', ['rebase', '--abort'], { allowFailure: true });
    throw new Error(`rebase onto ${baseRef} failed; remote PR branch was not changed: ${error.message}`);
  }

  const newHead = run('git', ['rev-parse', 'HEAD']);
  const baseHead = run('git', ['rev-parse', baseRemote]);
  if (newHead === baseHead) {
    throw new Error(
      `rebased head equals ${baseRef}; refusing to update the remote PR branch because GitHub may close the PR`,
    );
  }

  ensureNonEmptyDiff(
    run,
    baseRemote,
    `rebased PR #${number} has no diff against ${baseRef}; refusing to update the remote branch`,
  );

  const ancestor = run('git', ['merge-base', '--is-ancestor', baseRemote, 'HEAD'], { allowFailure: true });
  if (ancestor === null) {
    throw new Error(`rebased head does not contain current ${baseRef}; refusing to push`);
  }

  // This is the only remote branch mutation in the whole operation. force-with-lease binds
  // it to the head we inspected, so a concurrent push cannot be overwritten silently.
  run('git', [
    'push',
    'origin',
    `HEAD:refs/heads/${headRef}`,
    `--force-with-lease=refs/heads/${headRef}:${oldHead}`,
  ]);

  const afterPr = fetchPr(run, repo, number);
  requireOpenSameRepoPr(afterPr, repo, number);
  requireCanonicalOrigin(run, afterPr, repo);
  if (afterPr.head.sha !== newHead) {
    throw new Error(`post-push PR head mismatch: expected ${newHead}, GitHub reports ${afterPr.head.sha}`);
  }

  const after = fetchComparison(run, repo, baseRef, newHead);
  if ((after.behind_by ?? 0) !== 0) {
    throw new Error(`post-push verification failed: PR is still behind ${baseRef} by ${after.behind_by}`);
  }
  if ((after.ahead_by ?? 0) <= 0) {
    throw new Error('post-push verification failed: PR no longer has commits ahead of its base');
  }

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

export { DEFAULT_REPO, reconcilePr, requireOpenSameRepoPr, requireCanonicalOrigin, parseCli, usage };
