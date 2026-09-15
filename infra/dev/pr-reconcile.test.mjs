import test from 'node:test';
import assert from 'node:assert/strict';

import { exactHeadLeasePushArgs, reconcilePr } from './pr-reconcile.mjs';

const OLD = 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa';
const BASE = 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb';
const NEW = 'cccccccccccccccccccccccccccccccccccccccc';
const MERGE_BASE = '9999999999999999999999999999999999999999';
const MOVED_BASE = 'dddddddddddddddddddddddddddddddddddddddd';
const MOVED_HEAD = 'eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee';
const REPO = 'alaiba/arcogine';
const BRANCH = 'feature/test';
const TOKEN = 'test-token';
const HUMAN_IDENTITY = { name: 'Vasile Alaiba', email: 'vasile@alaiba.ro' };
const BASE_TRACKING_REF = 'refs/remotes/pr-reconcile/base';
const HEAD_TRACKING_REF = 'refs/remotes/pr-reconcile/head';

function pr({ head = OLD, state = 'open', base = 'main', headRef = BRANCH, headRepo = REPO } = {}) {
  return {
    state,
    head: {
      ref: headRef,
      sha: head,
      repo: { full_name: headRepo },
    },
    base: { ref: base, sha: 'historical' },
  };
}

function harness({
  behind = 1,
  ahead = 1,
  preFiles = ['file.txt'],
  liveBases = [BASE],
  fetchedBase = BASE,
  fetchedHead = OLD,
  initialPrState = 'open',
  beforePublishHead = OLD,
  afterPrState = 'open',
  headRef = BRANCH,
  foreignHeadRepo = REPO,
  rebaseFailure = null,
  pushFailure = null,
  changedPaths = 'file.txt',
} = {}) {
  const calls = [];
  let prReads = 0;
  let baseReads = 0;
  let rebased = false;
  let pushed = false;
  let cleaned = false;

  function run(file, args, options = {}) {
    calls.push({ file, args: [...args], options });

    if (file === 'gh' && args[0] === 'api') {
      const endpoint = args[1];
      if (endpoint === `repos/${REPO}/pulls/277`) {
        prReads += 1;
        const head = pushed ? NEW : prReads === 1 ? OLD : beforePublishHead;
        const state = pushed ? afterPrState : prReads === 1 ? initialPrState : 'open';
        return JSON.stringify(pr({ head, state, headRef, headRepo: foreignHeadRepo }));
      }

      if (endpoint === `repos/${REPO}/branches/main`) {
        const index = Math.min(baseReads, liveBases.length - 1);
        baseReads += 1;
        return JSON.stringify({ commit: { sha: liveBases[index] } });
      }

      if (endpoint?.startsWith(`repos/${REPO}/compare/`)) {
        return JSON.stringify({ behind_by: behind, ahead_by: ahead, files: preFiles });
      }
      throw new Error(`unexpected GitHub command: ${args.join(' ')}`);
    }

    if (file !== 'git') throw new Error(`unexpected command: ${file} ${args.join(' ')}`);

    if (args[0] === 'init' || args[0] === 'config' || args[0] === 'remote' || args[0] === 'fetch' || args[0] === 'checkout') {
      return '';
    }
    if (args[0] === 'rev-parse' && args[1] === BASE_TRACKING_REF) return fetchedBase;
    if (args[0] === 'rev-parse' && args[1] === HEAD_TRACKING_REF) return fetchedHead;
    if (args[0] === 'rev-parse' && args[1] === 'HEAD') return rebased ? NEW : OLD;
    if (args[0] === 'merge-base' && args[1] === HEAD_TRACKING_REF) return MERGE_BASE;
    if (args[0] === 'merge-base' && args[1] === BASE_TRACKING_REF) return fetchedBase;
    if (args[0] === 'diff') return changedPaths;
    if (args[0] === 'rebase' && args[1] === '--abort') return '';
    if (args[0] === 'rebase') {
      if (rebaseFailure) throw new Error(rebaseFailure);
      rebased = true;
      return '';
    }
    if (args[0] === 'push') {
      if (pushFailure) throw new Error(pushFailure);
      pushed = true;
      return '';
    }

    throw new Error(`unexpected Git command: ${args.join(' ')}`);
  }

  return {
    run,
    calls,
    workspaceFactory: () => 'C:\\temporary\\pr-reconcile',
    cleanup: () => {
      cleaned = true;
    },
    get baseReads() {
      return baseReads;
    },
    get pushed() {
      return pushed;
    },
    get cleaned() {
      return cleaned;
    },
  };
}

const noWait = async () => {};

function callsFor(h, file, firstArg) {
  return h.calls.filter((call) => call.file === file && call.args[0] === firstArg);
}

function assertNoSemanticResolution(h) {
  assert.equal(
    h.calls.some(
      (call) =>
        call.file === 'git' &&
        (call.args[0] === 'merge' ||
          call.args[0] === 'merge-file' ||
          (call.args[0] === 'rebase' && call.args[1] === '--continue') ||
          call.args[0] === 'add'),
    ),
    false,
    'reviewer normalization must not make semantic conflict-resolution choices',
  );
  assert.equal(
    h.calls.some((call) => call.file === 'gh' && call.args[1]?.includes('/update-branch')),
    false,
    'reviewer normalization must not use a GitHub merge endpoint',
  );
}

test('a PR that already contains observed main is not rebased', async () => {
  const h = harness({ behind: 0 });
  const result = await reconcilePr({
    number: 277,
    repo: REPO,
    run: h.run,
    log: () => {},
    sleep: noWait,
    token: TOKEN,
    humanIdentity: HUMAN_IDENTITY,
    workspaceFactory: h.workspaceFactory,
    cleanup: h.cleanup,
  });

  assert.deepEqual(result, { changed: false, oldHead: OLD, newHead: OLD, baseRef: 'main', baseSha: BASE });
  assert.equal(callsFor(h, 'git', 'rebase').length, 0);
  assert.equal(callsFor(h, 'git', 'push').length, 0);
  assert.equal(h.cleaned, false);
});

test('a stale PR is rebased once onto observed main and published with an exact-head lease', async () => {
  const h = harness();
  const result = await reconcilePr({
    number: 277,
    repo: REPO,
    run: h.run,
    log: () => {},
    sleep: noWait,
    token: TOKEN,
    humanIdentity: HUMAN_IDENTITY,
    workspaceFactory: h.workspaceFactory,
    cleanup: h.cleanup,
  });

  assert.equal(result.changed, true);
  assert.equal(result.oldHead, OLD);
  assert.equal(result.newHead, NEW);
  assert.equal(result.baseSha, BASE);
  assert.equal(callsFor(h, 'git', 'rebase').length, 1);
  assert.deepEqual(callsFor(h, 'git', 'push')[0].args, exactHeadLeasePushArgs(BRANCH, OLD, NEW));
  assert.match(callsFor(h, 'git', 'push')[0].args[1], /^--force-with-lease=/);
  assert.equal(callsFor(h, 'git', 'push')[0].args.includes('--force'), false);
  assert.equal(callsFor(h, 'git', 'push')[0].args.includes(TOKEN), false);
  assert.equal(callsFor(h, 'git', 'push')[0].options.env.GIT_TERMINAL_PROMPT, '0');
  assert.equal(
    callsFor(h, 'git', 'push')[0].options.env.GIT_CONFIG_VALUE_0,
    `AUTHORIZATION: basic ${Buffer.from(`x-access-token:${TOKEN}`).toString('base64')}`,
  );
  assert.equal(h.cleaned, true);
  assertNoSemanticResolution(h);
});

test('a rebase conflict aborts local work without mutating the remote branch', async () => {
  const h = harness({ rebaseFailure: 'content conflict' });

  await assert.rejects(
    reconcilePr({
      number: 277,
      repo: REPO,
      run: h.run,
      log: () => {},
      sleep: noWait,
      token: TOKEN,
      humanIdentity: HUMAN_IDENTITY,
      workspaceFactory: h.workspaceFactory,
      cleanup: h.cleanup,
    }),
    /conflict-free rebase failed.*no remote branch was changed.*implementation\/author reconciliation.*content conflict/,
  );

  assert.equal(callsFor(h, 'git', 'push').length, 0);
  assert.equal(
    h.calls.some((call) => call.file === 'git' && call.args[0] === 'rebase' && call.args[1] === '--abort'),
    true,
  );
  assert.equal(h.pushed, false);
  assert.equal(h.cleaned, true);
  assertNoSemanticResolution(h);
});

test('a remote PR head change after rebase begins refuses publication and requests lifecycle re-resolution', async () => {
  const h = harness({ beforePublishHead: MOVED_HEAD });

  await assert.rejects(
    reconcilePr({
      number: 277,
      repo: REPO,
      run: h.run,
      log: () => {},
      sleep: noWait,
      token: TOKEN,
      humanIdentity: HUMAN_IDENTITY,
      workspaceFactory: h.workspaceFactory,
      cleanup: h.cleanup,
    }),
    /PR head moved during rebase.*no remote branch was changed.*re-resolve lifecycle state/,
  );

  assert.equal(callsFor(h, 'git', 'push').length, 0);
  assert.equal(h.pushed, false);
  assert.equal(h.cleaned, true);
  assertNoSemanticResolution(h);
});

test('guarded publication fails closed when the exact old head lease is rejected', async () => {
  const h = harness({ pushFailure: 'stale info' });

  await assert.rejects(
    reconcilePr({
      number: 277,
      repo: REPO,
      run: h.run,
      log: () => {},
      sleep: noWait,
      token: TOKEN,
      humanIdentity: HUMAN_IDENTITY,
      workspaceFactory: h.workspaceFactory,
      cleanup: h.cleanup,
    }),
    /exact-head guarded rebase publication failed.*do not retry blindly.*re-resolve lifecycle state.*stale info/,
  );

  assert.deepEqual(callsFor(h, 'git', 'push')[0].args, exactHeadLeasePushArgs(BRANCH, OLD, NEW));
  assert.equal(h.pushed, false);
  assertNoSemanticResolution(h);
});

test('an agent-owned Git identity is rejected before rebase work begins', async () => {
  const h = harness();

  await assert.rejects(
    reconcilePr({
      number: 277,
      repo: REPO,
      run: h.run,
      log: () => {},
      sleep: noWait,
      token: TOKEN,
      humanIdentity: { name: 'Codex', email: 'codex@example.com' },
      workspaceFactory: h.workspaceFactory,
      cleanup: h.cleanup,
    }),
    /agent- or bot-owned/,
  );

  assert.equal(callsFor(h, 'git', 'rebase').length, 0);
  assert.equal(callsFor(h, 'git', 'push').length, 0);
});

test('main movement after B is observed does not trigger an immediate retry or second rebase', async () => {
  const h = harness({ liveBases: [BASE, MOVED_BASE] });
  const result = await reconcilePr({
    number: 277,
    repo: REPO,
    run: h.run,
    log: () => {},
    sleep: noWait,
    token: TOKEN,
    humanIdentity: HUMAN_IDENTITY,
    workspaceFactory: h.workspaceFactory,
    cleanup: h.cleanup,
  });

  assert.equal(result.changed, true);
  assert.equal(h.baseReads, 1, 'the attempt must not chase a base that moved after B was captured');
  assert.equal(callsFor(h, 'git', 'rebase').length, 1);
  assert.equal(callsFor(h, 'git', 'push').length, 1);
});

test('successful rebase returns a new head for current-head lifecycle handling', async () => {
  const h = harness();
  const result = await reconcilePr({
    number: 277,
    repo: REPO,
    run: h.run,
    log: () => {},
    sleep: noWait,
    token: TOKEN,
    humanIdentity: HUMAN_IDENTITY,
    workspaceFactory: h.workspaceFactory,
    cleanup: h.cleanup,
  });

  assert.notEqual(result.newHead, result.oldHead);
  assert.equal(result.newHead, NEW);
  assertNoSemanticResolution(h);
});

test('an empty or collapsed stale PR is refused before any rebase or publication', async () => {
  for (const options of [{ preFiles: [] }, { ahead: 0 }, { changedPaths: '' }]) {
    const h = harness(options);
    await assert.rejects(
      reconcilePr({
        number: 277,
        repo: REPO,
        run: h.run,
        log: () => {},
        sleep: noWait,
        token: TOKEN,
        humanIdentity: HUMAN_IDENTITY,
        workspaceFactory: h.workspaceFactory,
        cleanup: h.cleanup,
      }),
      /no diff|collapse the PR|empty diff/,
    );
    assert.equal(callsFor(h, 'git', 'rebase').length, options.changedPaths === '' ? 1 : 0);
    assert.equal(callsFor(h, 'git', 'push').length, 0);
  }
});

test('cross-repository and closed PRs are rejected before any remote mutation', async (t) => {
  await t.test('cross-repository head', async () => {
    const h = harness({ foreignHeadRepo: 'someone/fork' });
    await assert.rejects(
      reconcilePr({ number: 277, repo: REPO, run: h.run, token: TOKEN, workspaceFactory: h.workspaceFactory, cleanup: h.cleanup }),
      /cross-repository PR reconciliation is not supported/,
    );
    assert.equal(callsFor(h, 'git', 'push').length, 0);
  });

  await t.test('closed PR', async () => {
    const h = harness({ initialPrState: 'closed' });
    await assert.rejects(
      reconcilePr({ number: 277, repo: REPO, run: h.run, token: TOKEN, workspaceFactory: h.workspaceFactory, cleanup: h.cleanup }),
      /pull request .* is not open/,
    );
    assert.equal(callsFor(h, 'git', 'push').length, 0);
  });

  await t.test('identical head and base branches', async () => {
    const h = harness({ headRef: 'main' });
    await assert.rejects(
      reconcilePr({ number: 277, repo: REPO, run: h.run, token: TOKEN, workspaceFactory: h.workspaceFactory, cleanup: h.cleanup }),
      /head is main|head and base branches are identical/,
    );
    assert.equal(callsFor(h, 'git', 'push').length, 0);
  });
});
