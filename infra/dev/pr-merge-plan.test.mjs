import test from 'node:test';
import assert from 'node:assert/strict';

import {
  createMechanicalMergePlan,
  createMergeCommitSpec,
  createFastForwardUpdate,
  verifyFastForwardResult,
} from './pr-merge-plan.mjs';

const A = 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa';
const B = 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb';
const B2 = '2222222222222222222222222222222222222222';
const H = 'dddddddddddddddddddddddddddddddddddddddd';
const H2 = '1111111111111111111111111111111111111111';
const M = 'eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee';
const RESEARCH = 'ffffffffffffffffffffffffffffffffffffffff';

function blob(path, sha, mode = '100644') {
  return { path, sha, mode, type: 'blob' };
}

function snapshot(sha, tree) {
  return { sha, tree };
}

function disjointPlan() {
  return createMechanicalMergePlan({
    mergeBase: snapshot(A, [blob('base.txt', A), blob('pr.txt', A)]),
    base: snapshot(B, [blob('base.txt', B), blob('pr.txt', A)]),
    head: snapshot(H, [blob('base.txt', A), blob('pr.txt', H, '100755')]),
  });
}

test('disjoint mechanical changes produce an exact merged tree and preserve the changed mode', () => {
  const plan = disjointPlan();

  assert.equal(plan.kind, 'mechanical-merge');
  assert.deepEqual(plan.finalTree, [blob('base.txt', B), blob('pr.txt', H, '100755')]);
  assert.equal(plan.intendedDiff.length, 1);
  assert.equal(plan.intendedDiff[0].path, 'pr.txt');
  assert.equal(plan.intendedDiff[0].after.mode, '100755');
});

test('overlapping and ancestor-descendant changes refuse semantic resolution', () => {
  assert.throws(
    () =>
      createMechanicalMergePlan({
        mergeBase: snapshot(A, [blob('shared.txt', A)]),
        base: snapshot(B, [blob('shared.txt', B)]),
        head: snapshot(H, [blob('shared.txt', H)]),
      }),
    /overlap/,
  );

  assert.throws(
    () =>
      createMechanicalMergePlan({
        mergeBase: snapshot(A, [blob('dir/file.txt', A)]),
        base: snapshot(B, [blob('dir/file.txt', A)]),
        head: snapshot(H, [blob('dir', H)]),
      }),
    /ancestor|overlap/,
  );

  assert.throws(
    () =>
      createMechanicalMergePlan({
        mergeBase: snapshot(A, [blob('Readme.md', A), blob('other.txt', A)]),
        base: snapshot(B, [blob('Readme.md', A), blob('README.md', B), blob('other.txt', B)]),
        head: snapshot(H, [blob('Readme.md', H), blob('other.txt', A)]),
      }),
    /case-folded/,
  );
});

test('rename-shaped and special Git entries are rejected', () => {
  assert.throws(
    () =>
      createMechanicalMergePlan({
        mergeBase: snapshot(A, [blob('old.txt', A)]),
        base: snapshot(B, [blob('old.txt', A)]),
        head: snapshot(H, [blob('new.txt', H)]),
      }),
    /rename\/copy/,
  );

  for (const entry of [
    { path: 'link', sha: H, mode: '120000', type: 'blob' },
    { path: 'submodule', sha: H, mode: '160000', type: 'commit' },
  ]) {
    assert.throws(
      () =>
        createMechanicalMergePlan({
          mergeBase: snapshot(A, []),
          base: snapshot(B, []),
          head: snapshot(H, [entry]),
        }),
      /unsupported/,
    );
  }
});

test('a deletion without an addition is replayed exactly when no other side changes that path', () => {
  const plan = createMechanicalMergePlan({
    mergeBase: snapshot(A, [blob('delete.txt', A), blob('keep.txt', A)]),
    base: snapshot(B, [blob('delete.txt', A), blob('keep.txt', B)]),
    head: snapshot(H, [blob('keep.txt', A)]),
  });

  assert.deepEqual(plan.finalTree, [blob('keep.txt', B)]);
  assert.deepEqual(plan.intendedDiff, [{ path: 'delete.txt', before: blob('delete.txt', A), after: null }]);
});

test('merge commit spec uses H as first parent and B as second with a repository-safe identity', () => {
  const plan = disjointPlan();
  const spec = createMergeCommitSpec(plan, {
    baseRef: 'main',
    headRef: 'feature/test',
    author: { name: 'Vasile Alaiba', email: 'vasile@alaiba.ro' },
    committer: { name: 'Vasile Alaiba', email: 'vasile@alaiba.ro' },
    humanIdentityVerified: true,
  });

  assert.deepEqual(spec.parents, [H, B]);
  assert.equal(spec.message, 'Merge main into feature/test');
  assert.equal(spec.tree[1].sha, H);
  assert.throws(
    () => createMergeCommitSpec(plan, { headRef: 'feature/test', author: { name: 'Codex bot', email: 'bot@example.com' }, committer: { name: 'Codex bot', email: 'bot@example.com' }, humanIdentityVerified: true }),
    /agent- or bot-owned/,
  );
});

test('fast-forward mutation is explicitly non-forced and rejects a moved head', () => {
  assert.deepEqual(
    createFastForwardUpdate({ expectedHead: H, currentHead: H, target: M, currentHeadIsAncestor: true }),
    { expectedOldHead: H, newHead: M, force: false },
  );
  assert.throws(
    () => createFastForwardUpdate({ expectedHead: H, currentHead: H2, target: M, currentHeadIsAncestor: false }),
    /moved|non-fast-forward/,
  );
  assert.throws(
    () => createFastForwardUpdate({ expectedHead: H, currentHead: H, target: M, currentHeadIsAncestor: true, force: true }),
    /force=false/,
  );
});

function successfulObservation(plan) {
  return {
    prState: 'OPEN',
    headSha: M,
    parents: [H, B],
    tree: plan.finalTree,
    oldHeadAncestor: true,
    baseAncestor: true,
    currentBaseSha: B,
    currentBaseAncestor: true,
    behindBy: 0,
    aheadBy: 1,
    evidenceAncestors: { [RESEARCH]: true },
  };
}

test('post-update verification proves ancestry, freshness, non-empty intended diff, and research evidence custody', () => {
  const plan = disjointPlan();
  assert.deepEqual(
    verifyFastForwardResult({ plan, mergeCommitSha: M, observed: successfulObservation(plan), requiredEvidence: [{ sha: RESEARCH }] }),
    { ok: true, retry: false, reasons: [] },
  );
});

test('base advance after the update is a retryable freshness failure, never a rollback signal', () => {
  const plan = disjointPlan();
  const observed = successfulObservation(plan);
  observed.currentBaseSha = B2;
  observed.currentBaseAncestor = false;

  const result = verifyFastForwardResult({ plan, mergeCommitSha: M, observed });
  assert.equal(result.ok, false);
  assert.equal(result.retry, true);
  assert.match(result.reasons.join('\n'), /advanced/);
});

test('unexpected tree content or lost evidence fails closed', () => {
  const plan = disjointPlan();
  const observed = successfulObservation(plan);
  observed.tree = [blob('base.txt', B), blob('pr.txt', 'cccccccccccccccccccccccccccccccccccccccc', '100755')];
  observed.evidenceAncestors = {};
  const result = verifyFastForwardResult({ plan, mergeCommitSha: M, observed, requiredEvidence: [{ sha: RESEARCH }] });

  assert.equal(result.ok, false);
  assert.equal(result.retry, false);
  assert.match(result.reasons.join('\n'), /tree|evidence/);
});
