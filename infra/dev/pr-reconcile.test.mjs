import test from 'node:test';
import assert from 'node:assert/strict';

import { reconcilePr } from './pr-reconcile.mjs';

const OLD = 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa';
const BASE = 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb';
const NEW = 'cccccccccccccccccccccccccccccccccccccccc';
const MOVED_BASE = 'dddddddddddddddddddddddddddddddddddddddd';
const MOVED_HEAD = 'eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee';
const CONCURRENT_HEAD = 'ffffffffffffffffffffffffffffffffffffffff';
const REPO = 'alaiba/arcogine';
const BRANCH = 'feature/test';

function pr({ head = OLD, state = 'open', base = 'main', baseSha = 'historical', headRepo = REPO } = {}) {
  return {
    state,
    head: {
      ref: BRANCH,
      sha: head,
      repo: {
        full_name: headRepo,
        html_url: `https://github.com/${headRepo}`,
        ssh_url: `git@github.com:${headRepo}.git`,
        clone_url: `https://github.com/${headRepo}.git`,
      },
    },
    base: { ref: base, sha: baseSha },
  };
}

function harness({
  behind = 1,
  ahead = 1,
  preFiles = ['file.txt'],
  postFiles = ['file.txt'],
  liveBases = [BASE, BASE, BASE],
  setupHead = OLD,
  afterHead = NEW,
  afterPrState = 'open',
  afterOldHeadBehind = 0,
  afterBehind = 0,
  afterAhead = 1,
  afterFiles = postFiles,
  updateFailure = null,
  foreignHeadRepo = REPO,
  baseMetadata = 'historical',
  initialPrState = 'open',
} = {}) {
  const calls = [];
  let prReads = 0;
  let baseReads = 0;
  let updateCalls = 0;
  let updateApplied = false;

  function run(file, args, options = {}) {
    calls.push({ file, args: [...args], options });
    const endpoint = args[0] === 'api' ? args[1] : null;

    if (file !== 'gh' || args[0] !== 'api') {
      if (options.allowFailure) return null;
      throw new Error(`unexpected command: ${file} ${args.join(' ')}`);
    }

    if (endpoint === `repos/${REPO}/pulls/277`) {
      prReads += 1;
      const head = prReads === 1 ? OLD : setupHead;
      const state = updateApplied && prReads > 2 ? afterPrState : prReads === 1 ? initialPrState : 'open';
      return JSON.stringify(
        pr({
          head: updateApplied && prReads > 2 ? afterHead : head,
          state,
          baseSha: baseMetadata,
          headRepo: foreignHeadRepo,
        }),
      );
    }

    if (endpoint === `repos/${REPO}/branches/main`) {
      const index = Math.min(baseReads, liveBases.length - 1);
      baseReads += 1;
      return JSON.stringify({ commit: { sha: liveBases[index] } });
    }

    if (endpoint?.startsWith(`repos/${REPO}/compare/`)) {
      const comparison = endpoint.split('/compare/')[1];
      if (comparison === `${OLD}...${NEW}`) {
        return JSON.stringify({ behind_by: afterOldHeadBehind, ahead_by: 1, files: ['file.txt'] });
      }
      if (comparison.endsWith(`...${OLD}`)) {
        return JSON.stringify({ behind_by: behind, ahead_by: ahead, files: preFiles });
      }
      return JSON.stringify({ behind_by: afterBehind, ahead_by: afterAhead, files: afterFiles });
    }

    if (endpoint === `repos/${REPO}/pulls/277/update-branch`) {
      updateCalls += 1;
      if (updateFailure) throw new Error(updateFailure);
      updateApplied = true;
      return '{}';
    }

    throw new Error(`unexpected command: ${file} ${args.join(' ')}`);
  }

  return { run, calls, get updateCalls() { return updateCalls; } };
}

function updateCalls(calls) {
  return calls.filter(
    (call) => call.file === 'gh' && call.args[0] === 'api' && call.args[1].endsWith('/update-branch'),
  );
}

function assertNoRewriteCommands(calls) {
  assert.equal(
    calls.some(
      (call) =>
        call.file === 'git' ||
        call.args.includes('rebase') ||
        call.args.some((arg) => arg.includes('--force')),
    ),
    false,
    'normal reconciliation must not use local rebase or force-push machinery',
  );
}

const noWait = async () => {};

test('stale ordinary PR requests one expected-head-bound merge-style Update branch', async () => {
  const h = harness();
  const result = await reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {}, sleep: noWait });

  assert.equal(result.changed, true);
  assert.equal(result.oldHead, OLD);
  assert.equal(result.newHead, NEW);
  assert.equal(h.updateCalls, 1);
  assert.deepEqual(updateCalls(h.calls)[0].args, [
    'api',
    `repos/${REPO}/pulls/277/update-branch`,
    '--method',
    'PUT',
    '-H',
    'Accept: application/vnd.github+json',
    '-f',
    `expected_head_sha=${OLD}`,
  ]);
  assertNoRewriteCommands(h.calls);
});

test('an already-current PR is a no-op', async () => {
  const h = harness({ behind: 0 });
  const result = await reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {}, sleep: noWait });

  assert.equal(result.changed, false);
  assert.equal(h.updateCalls, 0);
  assertNoRewriteCommands(h.calls);
});

test('an empty or collapsed PR is refused before synchronization', async () => {
  for (const options of [{ preFiles: [] }, { ahead: 0 }]) {
    const h = harness(options);
    await assert.rejects(
      reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {}, sleep: noWait }),
      /no diff against main/,
    );
    assert.equal(h.updateCalls, 0);
    assertNoRewriteCommands(h.calls);
  }
});

test('a merge-style update conflict fails without implementation-side resolution', async () => {
  const h = harness({ updateFailure: 'synthetic conflict' });
  await assert.rejects(
    reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {}, sleep: noWait }),
    /merge-style Update branch failed; remote PR branch was not changed: synthetic conflict/,
  );
  assert.equal(h.updateCalls, 1);
  assertNoRewriteCommands(h.calls);
});

test('a concurrent head movement is bound by expected_head_sha and is not overwritten', async () => {
  const h = harness({ updateFailure: 'expected head does not match current head' });
  await assert.rejects(
    reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {}, sleep: noWait }),
    /merge-style Update branch failed/,
  );
  assert.equal(h.updateCalls, 1);
  assert.equal(updateCalls(h.calls)[0].args.at(-1), `expected_head_sha=${OLD}`);
  assertNoRewriteCommands(h.calls);
  assert.equal(updateCalls(h.calls).length, 1, 'a failed expected-head request must not be retried blindly');
});

test('a concurrent head move during setup is refused before Update branch', async () => {
  const h = harness({ setupHead: MOVED_HEAD });
  await assert.rejects(
    reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {}, sleep: noWait }),
    /PR head moved during reconciliation setup/,
  );
  assert.equal(h.updateCalls, 0);
  assertNoRewriteCommands(h.calls);
});

test('a base ref move during setup is refused before Update branch', async () => {
  const h = harness({ liveBases: [BASE, MOVED_BASE] });
  await assert.rejects(
    reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {}, sleep: noWait }),
    /PR base moved during reconciliation setup/,
  );
  assert.equal(h.updateCalls, 0);
  assertNoRewriteCommands(h.calls);
});

test('stale PR base metadata does not block reconciliation against the live base ref', async () => {
  const h = harness({ baseMetadata: 'stale-pr-metadata' });
  const result = await reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {}, sleep: noWait });

  assert.equal(result.changed, true);
  assert.equal(h.updateCalls, 1);
  assertNoRewriteCommands(h.calls);
});

test('post-update verification requires the old head to remain an ancestor', async () => {
  const h = harness({ afterOldHeadBehind: 1 });
  await assert.rejects(
    reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {}, sleep: noWait }),
    /old PR head .* is not an ancestor/,
  );
  assert.equal(h.updateCalls, 1);
  assertNoRewriteCommands(h.calls);
});

test('post-update verification requires the live base to be incorporated and the PR to remain non-empty', async () => {
  for (const options of [{ afterBehind: 1 }, { afterAhead: 0 }, { afterFiles: [] }]) {
    const h = harness(options);
    await assert.rejects(
      reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {}, sleep: noWait }),
      /post-update verification failed|no longer has a non-empty diff/,
    );
    assert.equal(h.updateCalls, 1);
    assertNoRewriteCommands(h.calls);
  }
});

test('post-update verification requires the PR to remain open', async () => {
  const h = harness({ afterPrState: 'closed' });
  await assert.rejects(
    reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {}, sleep: noWait }),
    /is not open/,
  );
  assert.equal(h.updateCalls, 1);
  assertNoRewriteCommands(h.calls);
});

test('cross-repository PRs are rejected before any update', async () => {
  const h = harness({ foreignHeadRepo: 'someone/fork' });
  await assert.rejects(
    reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {}, sleep: noWait }),
    /cross-repository PR reconciliation is not supported/,
  );
  assert.equal(h.updateCalls, 0);
  assertNoRewriteCommands(h.calls);
});

test('closed PRs are rejected before any update', async () => {
  const h = harness({ initialPrState: 'closed' });
  await assert.rejects(
    reconcilePr({ number: 277, repo: REPO, run: h.run, log: () => {}, sleep: noWait }),
    /pull request .* is not open/,
  );
  assert.equal(h.updateCalls, 0);
  assertNoRewriteCommands(h.calls);
});
