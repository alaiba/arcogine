import test from 'node:test';
import assert from 'node:assert/strict';

import { reconcilePr } from './pr-reconcile.mjs';

const OLD = 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa';
const BASE = 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb';
const NEW = 'cccccccccccccccccccccccccccccccccccccccc';
const REPO = 'alaiba/arcogine';
const BRANCH = 'feature/test';

function pr(head = OLD, state = 'open') {
  return {
    state,
    head: { ref: BRANCH, sha: head, repo: { full_name: REPO, html_url: `https://github.com/${REPO}`, ssh_url: `git@github.com:${REPO}.git`, clone_url: `https://github.com/${REPO}.git` } },
    base: { ref: 'main', sha: BASE },
  };
}

function harness({
  behind = 1,
  ahead = 1,
  preDiff = 'file.txt',
  postDiff = 'file.txt',
  fetchedHead = OLD,
  rebaseFails = false,
  branch = BRANCH,
  workingTree = '',
  afterPrState = 'open',
} = {}) {
  const calls = [];
  let rebased = false;
  let prReads = 0;
  let compareReads = 0;

  function run(file, args, options = {}) {
    calls.push({ file, args: [...args], options });
    const key = `${file} ${args.join(' ')}`;

    if (key === 'git status --porcelain') return workingTree;
    if (key === 'git branch --show-current') return branch;
    if (key === 'git remote get-url --push --all origin') return `https://github.com/${REPO}.git/`;
    if (key === 'git rev-parse HEAD') return rebased ? NEW : OLD;
    if (
      key ===
      `git fetch origin +refs/heads/main:refs/remotes/origin/main +refs/heads/${BRANCH}:refs/remotes/origin/${BRANCH}`
    ) {
      return '';
    }
    if (key === `git rev-parse refs/remotes/origin/${BRANCH}`) return fetchedHead;
    if (key === 'git rev-parse refs/remotes/origin/main') return BASE;
    if (key === 'git diff --name-only refs/remotes/origin/main...HEAD') {
      return rebased ? postDiff : preDiff;
    }
    if (key === 'git rebase refs/remotes/origin/main') {
      if (rebaseFails) throw new Error('synthetic conflict');
      rebased = true;
      return '';
    }
    if (key === 'git rebase --abort') return '';
    if (key === 'git merge-base --is-ancestor refs/remotes/origin/main HEAD') return '';
    if (file === 'git' && args[0] === 'push') return '';

    if (file === 'gh' && args[0] === 'api' && args[1] === `repos/${REPO}/pulls/277`) {
      prReads += 1;
      return JSON.stringify(pr(prReads === 1 ? OLD : NEW, prReads === 1 ? 'open' : afterPrState));
    }
    if (file === 'gh' && args[0] === 'api' && args[1].startsWith(`repos/${REPO}/compare/`)) {
      compareReads += 1;
      return JSON.stringify(
        compareReads === 1
          ? { behind_by: behind, ahead_by: ahead }
          : { behind_by: 0, ahead_by: 1 },
      );
    }

    if (options.allowFailure) return null;
    throw new Error(`unexpected command: ${key}`);
  }

  return { run, calls };
}

function pushes(calls) {
  return calls.filter((call) => call.file === 'git' && call.args[0] === 'push');
}

test('successful reconciliation constructs the new head before one leased remote update', async () => {
  const h = harness();
  const result = await reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {} });

  assert.equal(result.changed, true);
  assert.equal(result.oldHead, OLD);
  assert.equal(result.newHead, NEW);

  const pushCalls = pushes(h.calls);
  assert.equal(pushCalls.length, 1, 'the remote PR branch must be mutated exactly once');
  assert.deepEqual(pushCalls[0].args, [
    'push',
    'origin',
    `HEAD:refs/heads/${BRANCH}`,
    `--force-with-lease=refs/heads/${BRANCH}:${OLD}`,
  ]);

  const rebaseIndex = h.calls.findIndex(
    (call) => call.file === 'git' && call.args.join(' ') === 'rebase refs/remotes/origin/main',
  );
  const pushIndex = h.calls.findIndex((call) => call.file === 'git' && call.args[0] === 'push');
  assert.ok(rebaseIndex >= 0 && pushIndex > rebaseIndex, 'push must happen only after rebase completes');

  assert.equal(
    h.calls.some(
      (call) =>
        call.file === 'git' &&
        (call.args[0] === 'update-ref' ||
          (call.args[0] === 'push' && call.args.some((arg) => arg.includes('main:refs/heads'))))),
    false,
    'the helper must never point the PR branch at the base as an intermediate state',
  );
});

test('an already-current PR is a no-op', async () => {
  const h = harness({ behind: 0 });
  const result = await reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {} });
  assert.equal(result.changed, false);
  assert.equal(pushes(h.calls).length, 0);
  assert.equal(h.calls.some((call) => call.args[0] === 'rebase'), false);
});

test('a rebase conflict aborts locally and never mutates the remote branch', async () => {
  const h = harness({ rebaseFails: true });
  await assert.rejects(
    reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {} }),
    /remote PR branch was not changed/,
  );
  assert.equal(pushes(h.calls).length, 0);
  assert.ok(h.calls.some((call) => call.file === 'git' && call.args.join(' ') === 'rebase --abort'));
});

test('an empty PR diff is refused before rebase or push', async () => {
  const h = harness({ preDiff: '' });
  await assert.rejects(
    reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {} }),
    /no diff against main/,
  );
  assert.equal(pushes(h.calls).length, 0);
  assert.equal(h.calls.some((call) => call.args[0] === 'rebase'), false);
});

test('a rebase that collapses the PR diff is refused before push', async () => {
  const h = harness({ postDiff: '' });
  await assert.rejects(
    reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {} }),
    /rebased PR #277 has no diff/,
  );
  assert.equal(pushes(h.calls).length, 0);
});

test('a concurrent remote head move is refused before rebase', async () => {
  const h = harness({ fetchedHead: 'dddddddddddddddddddddddddddddddddddddddd' });
  await assert.rejects(
    reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {} }),
    /PR head moved during reconciliation setup/,
  );
  assert.equal(pushes(h.calls).length, 0);
});

test('the helper refuses a different checked-out branch', async () => {
  const h = harness({ branch: 'other' });
  await assert.rejects(
    reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {} }),
    /current branch is other/,
  );
  assert.equal(pushes(h.calls).length, 0);
});

test('the helper refuses a dirty working tree', async () => {
  const h = harness({ workingTree: ' M file.txt' });
  await assert.rejects(
    reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {} }),
    /working tree is not clean/,
  );
  assert.equal(pushes(h.calls).length, 0);
});

test('post-push verification requires the PR to remain open', async () => {
  const h = harness({ afterPrState: 'closed' });
  await assert.rejects(
    reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {} }),
    /is not open/,
  );
  assert.equal(pushes(h.calls).length, 1, 'verification occurs after the single atomic push');
});

test('a foreign origin is rejected before any remote mutation', async () => {
  const h = harness();
  const original = h.run;
  h.run = (file, args, options = {}) => file === 'git' && args.join(' ') === 'remote get-url --push --all origin'
    ? 'git@github.com:someone/fork.git' : original(file, args, options);
  await assert.rejects(reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {} }), /not the PR head repository/);
  assert.equal(pushes(h.calls).length, 0);
  assert.equal(h.calls.some((call) => call.args[0] === 'fetch'), false);
});

test('a foreign pushurl is rejected even when the fetch origin is canonical', async () => {
  const h = harness();
  const original = h.run;
  h.run = (file, args, options = {}) => file === 'git' && args.join(' ') === 'remote get-url --push --all origin'
    ? `https://github.com/${REPO}.git\ngit@github.com:someone/fork.git` : original(file, args, options);
  await assert.rejects(reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {} }), /origin push remote/);
  assert.equal(pushes(h.calls).length, 0);
});
