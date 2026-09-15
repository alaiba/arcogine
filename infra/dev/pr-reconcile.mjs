#!/usr/bin/env node
/**
 * pr-reconcile.mjs -- safely rebase an open Arcogine PR onto its observed base.
 *
 * The reviewer captures the PR head H and live base B, rebases the PR commits in a
 * temporary local repository, and publishes the result only with an exact-head
 * --force-with-lease. Git performs conflict detection; a conflict stops the attempt
 * without any remote mutation and belongs to the implementation/author.
 *
 * Usage:
 *   node infra/dev/pr-reconcile.mjs <pr-number>
 *
 * Preconditions:
 *   - PR is open;
 *   - PR head branch lives in the canonical repository;
 *   - the authenticated GitHub user is the repository owner and matches the configured human Git identity;
 *   - an authenticated gh CLI and Git with push access are available.
 *
 * The user's checkout is never used or changed. The observed base is the target for
 * this one attempt. The helper does not rebase again merely because main advances while
 * the local rebase or publication is in progress.
 */

import { execFileSync } from 'node:child_process';
import { mkdtempSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { parseArgs } from 'node:util';
import { pathToFileURL } from 'node:url';

const DEFAULT_REPO = 'alaiba/arcogine';
const DEFAULT_PUBLISH_ATTEMPTS = 10;
const DEFAULT_PUBLISH_DELAY_MS = 1000;
const BASE_TRACKING_REF = 'refs/remotes/pr-reconcile/base';
const HEAD_TRACKING_REF = 'refs/remotes/pr-reconcile/head';

function usage() {
  return `pr-reconcile -- safely rebase an open PR onto its observed base

USAGE
  node infra/dev/pr-reconcile.mjs <pr-number> [--repo owner/name]

OPTIONS
  --repo <owner/name>  Repository (default: ${DEFAULT_REPO})
  --help               Show this help

REQUIRES
  An authenticated gh CLI as the human repository owner, with matching user.name and
  user.email, plus Git with push access. The user's local checkout is not changed; rebase
  work is performed in a temporary repository.

SAFETY
  The helper captures the PR head and live base, performs one automatic Git rebase, then
  verifies the remote PR head still equals the captured SHA before publishing. Publication
  uses --force-with-lease bound to that exact old head. It never uses an unguarded force
  push, resolves conflicts, or retries merely because main moved after the base snapshot.
`;
}

function commandRunner(file, args, { allowFailure = false, cwd, env } = {}) {
  try {
    return execFileSync(file, args, {
      encoding: 'utf8',
      stdio: ['ignore', 'pipe', 'pipe'],
      ...(cwd ? { cwd } : {}),
      ...(env ? { env } : {}),
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
  if (pr.head.ref === 'main') {
    throw new Error(`pull request ${repo}#${number} head is main; refusing to rewrite the base branch`);
  }
  if (pr.head.ref === pr.base.ref) {
    throw new Error(`pull request ${repo}#${number} head and base branches are identical; refusing branch replacement`);
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

function requireStablePublishHead(initialPr, currentPr, repo, number, expectedHead) {
  requireOpenSameRepoPr(currentPr, repo, number);
  if (currentPr.base.ref !== initialPr.base.ref) {
    throw new Error(
      `PR base ref moved during rebase: expected ${initialPr.base.ref}, fetched ${currentPr.base.ref}; ` +
        'discard the rebased result and re-resolve lifecycle state',
    );
  }
  if (currentPr.head.ref !== initialPr.head.ref) {
    throw new Error(
      `PR head ref moved during rebase: expected ${initialPr.head.ref}, fetched ${currentPr.head.ref}; ` +
        'no remote branch was changed; re-resolve lifecycle state',
    );
  }
  if (currentPr.head.sha !== expectedHead) {
    throw new Error(
      `PR head moved during rebase: expected ${expectedHead}, fetched ${currentPr.head.sha}; ` +
        'no remote branch was changed; re-resolve lifecycle state',
    );
  }
}

function resolveToken(run) {
  const fromEnv = process.env.GH_TOKEN || process.env.GITHUB_TOKEN;
  if (fromEnv?.trim()) return fromEnv.trim();
  try {
    const token = run('gh', ['auth', 'token']);
    if (token?.trim()) return token.trim();
  } catch {
    // Fall through to the actionable error below.
  }
  throw new Error(
    'no GitHub token: set GH_TOKEN or GITHUB_TOKEN, or install and authenticate the gh CLI',
  );
}

function requireHumanIdentity(identity, ownerIdentity) {
  const name = identity?.name?.trim();
  const email = identity?.email?.trim();
  if (!name || !email) throw new Error('human Git identity (user.name and user.email) is required for rebase');
  if (/\b(bot|dependabot|github actions|codex|claude|openai)\b/i.test(`${name} ${email}`)) {
    throw new Error('configured Git identity appears agent- or bot-owned; refusing rebase');
  }
  if (ownerIdentity && (name !== ownerIdentity.name || email.toLowerCase() !== ownerIdentity.email.toLowerCase())) {
    throw new Error(
      'configured Git identity does not match the authenticated human repository owner; refusing rebase',
    );
  }
  return { name, email };
}

function resolveHumanIdentity(run, repo, configuredIdentity) {
  const owner = repo.split('/', 1)[0];
  const authenticatedUser = parseJson(run('gh', ['api', 'user']), 'GitHub authenticated-user API');
  if (authenticatedUser.login?.toLowerCase() !== owner.toLowerCase()) {
    throw new Error(
      `authenticated GitHub user ${authenticatedUser.login ?? '(unknown)'} is not repository owner ${owner}; ` +
        'refusing rebase',
    );
  }
  const ownerIdentity = {
    name: authenticatedUser.name?.trim(),
    email: authenticatedUser.email?.trim(),
  };
  if (!ownerIdentity.name || !ownerIdentity.email) {
    throw new Error(
      'authenticated human repository owner profile must expose name and email for rebase identity validation',
    );
  }

  if (configuredIdentity) return requireHumanIdentity(configuredIdentity, ownerIdentity);

  let name;
  let email;
  try {
    name = run('git', ['config', '--get', 'user.name']);
    email = run('git', ['config', '--get', 'user.email']);
  } catch {
    throw new Error('human Git identity (user.name and user.email) is required for rebase');
  }
  return requireHumanIdentity({ name, email }, ownerIdentity);
}

function gitAuthEnvironment(token) {
  if (!token?.trim()) throw new Error('a GitHub token is required for the guarded branch publication');
  const authorization = Buffer.from(`x-access-token:${token.trim()}`).toString('base64');
  return {
    ...process.env,
    GIT_CONFIG_COUNT: '1',
    GIT_CONFIG_KEY_0: 'http.extraHeader',
    GIT_CONFIG_VALUE_0: `AUTHORIZATION: basic ${authorization}`,
    GIT_TERMINAL_PROMPT: '0',
  };
}

function createWorkspace() {
  return mkdtempSync(join(tmpdir(), 'arcogine-pr-reconcile-'));
}

function cleanupWorkspace(workspace) {
  rmSync(workspace, { recursive: true, force: true });
}

function git(run, workspace, args, options = {}) {
  return run('git', args, { cwd: workspace, ...options });
}

function remoteUrl(repo) {
  return `https://github.com/${repo}.git`;
}

function requireSameSha(actual, expected, label) {
  if (String(actual).toLowerCase() !== String(expected).toLowerCase()) {
    throw new Error(
      `${label} moved while preparing the rebase: expected ${expected}, fetched ${actual}; ` +
        'no remote branch was changed; re-resolve lifecycle state',
    );
  }
}

function prepareRepository({ run, workspace, repo, observedBase, observedHead, identity, env }) {
  git(run, workspace, ['init', '--quiet'], { env });
  git(run, workspace, ['config', 'user.name', identity.name], { env });
  git(run, workspace, ['config', 'user.email', identity.email], { env });
  git(run, workspace, ['remote', 'add', 'origin', remoteUrl(repo)], { env });
  git(
    run,
    workspace,
    [
      'fetch',
      '--no-tags',
      'origin',
      observedBase,
      observedHead,
    ],
    { env },
  );
  git(run, workspace, ['update-ref', BASE_TRACKING_REF, observedBase], { env });
  git(run, workspace, ['update-ref', HEAD_TRACKING_REF, observedHead], { env });

  const fetchedBase = git(run, workspace, ['rev-parse', BASE_TRACKING_REF], { env });
  const fetchedHead = git(run, workspace, ['rev-parse', HEAD_TRACKING_REF], { env });
  requireSameSha(fetchedBase, observedBase, 'observed base');
  requireSameSha(fetchedHead, observedHead, 'observed PR head');
}

function rebaseOnce({ run, workspace, observedBase, observedHead, env, number }) {
  const mergeBase = git(run, workspace, ['merge-base', HEAD_TRACKING_REF, BASE_TRACKING_REF], { env });
  if (!mergeBase) throw new Error(`could not resolve a merge base for PR #${number}`);

  git(run, workspace, ['checkout', '--detach', HEAD_TRACKING_REF], { env });
  try {
    git(
      run,
      workspace,
      ['rebase', '--onto', BASE_TRACKING_REF, mergeBase, HEAD_TRACKING_REF],
      { env },
    );
  } catch (error) {
    git(run, workspace, ['rebase', '--abort'], { env, allowFailure: true });
    throw new Error(
      `conflict-free rebase failed for PR #${number}; no remote branch was changed; ` +
        `rebase conflicts require implementation/author reconciliation: ${error.message}`,
    );
  }

  const newHead = git(run, workspace, ['rev-parse', 'HEAD'], { env });
  if (!newHead) throw new Error(`rebase for PR #${number} produced no commit`);
  if (newHead.toLowerCase() === observedHead.toLowerCase()) {
    throw new Error(
      `rebase for PR #${number} did not create a new head; refusing to publish an unverified result`,
    );
  }

  const rebasedBase = git(run, workspace, ['merge-base', BASE_TRACKING_REF, 'HEAD'], { env });
  requireSameSha(rebasedBase, observedBase, 'rebased base');
  const changedPaths = git(run, workspace, ['diff', '--name-only', BASE_TRACKING_REF, 'HEAD'], { env });
  if (!changedPaths) {
    throw new Error(`rebase for PR #${number} would collapse the PR to an empty diff; refusing publication`);
  }

  return { oldHead: observedHead, newHead };
}

function exactHeadLeasePushArgs(headRef, expectedHead, newHead) {
  return [
    'push',
    `--force-with-lease=refs/heads/${headRef}:${expectedHead}`,
    'origin',
    `${newHead}:refs/heads/${headRef}`,
  ];
}

function publishRebasedHead({ run, workspace, headRef, oldHead, newHead, env }) {
  try {
    git(run, workspace, exactHeadLeasePushArgs(headRef, oldHead, newHead), { env });
  } catch (error) {
    throw new Error(
      `exact-head guarded rebase publication failed or was not confirmed; do not retry blindly; ` +
        `re-resolve lifecycle state: ${error.message}`,
    );
  }
}

async function waitForPublishedPr({ run, repo, number, oldHead, newHead, baseRef, attempts, delayMs, sleep }) {
  for (let attempt = 0; attempt < attempts; attempt += 1) {
    const pr = fetchPr(run, repo, number);
    requireOpenSameRepoPr(pr, repo, number);
    if (pr.base.ref !== baseRef) {
      throw new Error(`PR base ref changed from ${baseRef} after rebase publication; re-resolve lifecycle state`);
    }
    if (pr.head.sha === newHead) return pr;
    if (pr.head.sha !== oldHead) {
      throw new Error(
        `PR head changed after rebase publication: expected ${newHead}, fetched ${pr.head.sha}; ` +
          're-resolve lifecycle state',
      );
    }
    if (attempt + 1 < attempts) await sleep(delayMs);
  }
  throw new Error(`rebased PR #${number} was not visible at its new head ${newHead}`);
}

async function reconcilePr({
  number,
  repo = DEFAULT_REPO,
  run = commandRunner,
  log = console.log,
  sleep = (delayMs) => new Promise((resolve) => setTimeout(resolve, delayMs)),
  attempts = DEFAULT_PUBLISH_ATTEMPTS,
  delayMs = DEFAULT_PUBLISH_DELAY_MS,
  token,
  humanIdentity,
  workspaceFactory = createWorkspace,
  cleanup = cleanupWorkspace,
}) {
  if (!Number.isInteger(number) || number <= 0) throw new Error(`invalid PR number: ${number}`);
  if (!/^[^/]+\/[^/]+$/.test(repo)) throw new Error(`--repo must be owner/name, got "${repo}"`);
  if (!Number.isInteger(attempts) || attempts <= 0) throw new Error(`invalid publish attempts: ${attempts}`);
  if (!Number.isInteger(delayMs) || delayMs < 0) throw new Error(`invalid publish delay: ${delayMs}`);

  const pr = fetchPr(run, repo, number);
  requireOpenSameRepoPr(pr, repo, number);
  const oldHead = pr.head.sha;
  const baseRef = pr.base.ref;
  const observedBase = fetchBranchHead(run, repo, baseRef);
  const before = fetchComparison(run, repo, observedBase, oldHead);
  const behindBy = comparisonDistance(before, 'behind_by');

  if (behindBy === 0) {
    requireNonEmptyChange(
      before,
      `PR #${number} has no diff against ${baseRef}; refusing to treat an empty PR as current`,
    );
    log(`PR #${number} already contains observed ${baseRef}@${observedBase}; nothing to rebase.`);
    return { changed: false, oldHead, newHead: oldHead, baseRef, baseSha: observedBase };
  }

  requireNonEmptyChange(
    before,
    `PR #${number} has no diff against ${baseRef}; refusing rebase that could collapse the PR`,
  );

  const gitToken = token ?? resolveToken(run);
  const identity = resolveHumanIdentity(run, repo, humanIdentity);
  const env = gitAuthEnvironment(gitToken);
  const workspace = workspaceFactory();
  try {
    prepareRepository({
      run,
      workspace,
      repo,
      observedBase,
      observedHead: oldHead,
      identity,
      env,
    });

    const rebased = rebaseOnce({
      run,
      workspace,
      observedBase,
      observedHead: oldHead,
      env,
      number,
    });

    // This read is the non-negotiable precondition check. The lease below is the atomic
    // guard for a race after this read; main is deliberately not re-read or chased here.
    const beforePublish = fetchPr(run, repo, number);
    requireStablePublishHead(pr, beforePublish, repo, number, oldHead);

    publishRebasedHead({
      run,
      workspace,
      headRef: pr.head.ref,
      oldHead,
      newHead: rebased.newHead,
      env,
    });

    const after = await waitForPublishedPr({
      run,
      repo,
      number,
      oldHead,
      newHead: rebased.newHead,
      baseRef,
      attempts,
      delayMs,
      sleep,
    });

    log(
      `Rebased PR #${number}: ${oldHead} -> ${after.head.sha} onto observed ${baseRef}@${observedBase}. ` +
        'Re-resolve the normal current-head lifecycle; later base drift is not chased by this attempt.',
    );
    return {
      changed: true,
      oldHead,
      newHead: after.head.sha,
      baseRef,
      baseSha: observedBase,
    };
  } finally {
    cleanup(workspace);
  }
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

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  process.exitCode = await main();
}

export {
  DEFAULT_REPO,
  reconcilePr,
  requireOpenSameRepoPr,
  exactHeadLeasePushArgs,
  parseCli,
  usage,
};
