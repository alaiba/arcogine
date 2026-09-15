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
const C = 'cccccccccccccccccccccccccccccccccccccccc';
const H = 'dddddddddddddddddddddddddddddddddddddddd';
const H2 = '1111111111111111111111111111111111111111';
const M = 'eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee';
const RESEARCH = 'ffffffffffffffffffffffffffffffffffffffff';

function blob(path, sha, mode = '100644') {
  return { path, sha, mode, type: 'blob' };
}

function snapshot(sha, tree, truncated = false) {
  return { sha, tree, truncated };
}

function disjointPlan() {
  return createMechanicalMergePlan({
    mergeBase: snapshot(A, [blob('base.txt', A), blob('pr.txt', A)]),
    base: snapshot(B, [blob('base.txt', B), blob('pr.txt', A)]),
    head: snapshot(H, [blob('base.txt', A), blob('pr.txt', H, '100755')]),
  });
}

function defaultTextAttributeProof(overrides = {}) {
  return {
    sourceSha: H,
    text: 'auto',
    merge: 'unspecified',
    mergeDefault: 'unspecified',
    ...overrides,
  };
}

function cleanTextMergeResolution(overrides = {}) {
  return {
    path: 'shared.md',
    method: 'git-merge-file',
    clean: true,
    mergeBaseBlobSha: A,
    baseBlobSha: B,
    headBlobSha: H,
    resultBlobSha: C,
    attributeProof: defaultTextAttributeProof(),
    ...overrides,
  };
}

test('disjoint mechanical changes produce an exact merged tree and preserve the changed mode', () => {
  const plan = disjointPlan();

  assert.equal(plan.kind, 'mechanical-merge');
  assert.deepEqual(plan.finalTree, [blob('base.txt', B), blob('pr.txt', H, '100755')]);
  assert.equal(plan.intendedDiff.length, 1);
  assert.equal(plan.intendedDiff[0].path, 'pr.txt');
  assert.equal(plan.intendedDiff[0].after.mode, '100755');
});

test('same-path regular-text modify/modify accepts an exact clean three-way merge result', () => {
  const plan = createMechanicalMergePlan({
    mergeBase: snapshot(A, [blob('shared.md', A)]),
    base: snapshot(B, [blob('shared.md', B)]),
    head: snapshot(H, [blob('shared.md', H)]),
    textMergeResolutions: [cleanTextMergeResolution()],
  });

  assert.deepEqual(plan.finalTree, [blob('shared.md', C)]);
  assert.equal(plan.intendedDiff.length, 1);
  assert.deepEqual(plan.textMergeResolutions, [cleanTextMergeResolution()]);
});

test('same-path modify/modify fails closed without a clean text merge resolution', () => {
  assert.throws(
    () =>
      createMechanicalMergePlan({
        mergeBase: snapshot(A, [blob('shared.md', A)]),
        base: snapshot(B, [blob('shared.md', B)]),
        head: snapshot(H, [blob('shared.md', H)]),
      }),
    /three-way text merge resolution/,
  );

  assert.throws(
    () =>
      createMechanicalMergePlan({
        mergeBase: snapshot(A, [blob('shared.md', A)]),
        base: snapshot(B, [blob('shared.md', B)]),
        head: snapshot(H, [blob('shared.md', H)]),
        textMergeResolutions: [cleanTextMergeResolution({ clean: false })],
      }),
    /clean conflict-free/,
  );
});

test('text merge resolution is bound to the exact A/B/H blobs, head attributes, and unchanged mode', () => {
  assert.throws(
    () =>
      createMechanicalMergePlan({
        mergeBase: snapshot(A, [blob('shared.md', A)]),
        base: snapshot(B, [blob('shared.md', B)]),
        head: snapshot(H, [blob('shared.md', H)]),
        textMergeResolutions: [cleanTextMergeResolution({ headBlobSha: H2 })],
      }),
    /exact A\/B\/H blob inputs/,
  );

  assert.throws(
    () =>
      createMechanicalMergePlan({
        mergeBase: snapshot(A, [blob('shared.md', A)]),
        base: snapshot(B, [blob('shared.md', B)]),
        head: snapshot(H, [blob('shared.md', H)]),
        textMergeResolutions: [
          cleanTextMergeResolution({ attributeProof: defaultTextAttributeProof({ sourceSha: H2 }) }),
        ],
      }),
    /exact inspected PR head/,
  );

  assert.throws(
    () =>
      createMechanicalMergePlan({
        mergeBase: snapshot(A, [blob('shared.md', A)]),
        base: snapshot(B, [blob('shared.md', B, '100755')]),
        head: snapshot(H, [blob('shared.md', H)]),
        textMergeResolutions: [cleanTextMergeResolution()],
      }),
    /mode/,
  );
});

test('text merge eligibility fails closed for missing, binary, or custom Git attribute semantics', () => {
  const args = {
    mergeBase: snapshot(A, [blob('shared.md', A)]),
    base: snapshot(B, [blob('shared.md', B)]),
    head: snapshot(H, [blob('shared.md', H)]),
  };

  assert.throws(
    () => createMechanicalMergePlan({ ...args, textMergeResolutions: [cleanTextMergeResolution({ attributeProof: undefined })] }),
    /attribute eligibility proof/,
  );

  assert.throws(
    () =>
      createMechanicalMergePlan({
        ...args,
        textMergeResolutions: [
          cleanTextMergeResolution({
            attributeProof: defaultTextAttributeProof({ text: 'unset', merge: 'unset' }),
          }),
        ],
      }),
    /text attribute is unset/,
  );

  assert.throws(
    () =>
      createMechanicalMergePlan({
        ...args,
        textMergeResolutions: [
          cleanTextMergeResolution({ attributeProof: defaultTextAttributeProof({ merge: 'union' }) }),
        ],
      }),
    /merge attribute is union/,
  );

  assert.throws(
    () =>
      createMechanicalMergePlan({
        ...args,
        textMergeResolutions: [
          cleanTextMergeResolution({ attributeProof: defaultTextAttributeProof({ mergeDefault: 'custom-driver' }) }),
        ],
      }),
    /merge\.default is custom-driver/,
  );
});

test('named text merge driver routes fail closed because the driver can be overridden', () => {
  const args = {
    mergeBase: snapshot(A, [blob('shared.md', A)]),
    base: snapshot(B, [blob('shared.md', B)]),
    head: snapshot(H, [blob('shared.md', H)]),
  };

  assert.throws(
    () =>
      createMechanicalMergePlan({
        ...args,
        textMergeResolutions: [
          cleanTextMergeResolution({ attributeProof: defaultTextAttributeProof({ merge: 'text' }) }),
        ],
      }),
    /merge attribute is text/,
  );

  assert.throws(
    () =>
      createMechanicalMergePlan({
        ...args,
        textMergeResolutions: [
          cleanTextMergeResolution({ attributeProof: defaultTextAttributeProof({ mergeDefault: 'text' }) }),
        ],
      }),
    /merge\.default is text/,
  );
});

test('boolean-set merge attribute remains eligible without consulting merge.default', () => {
  const resolution = cleanTextMergeResolution({
    attributeProof: defaultTextAttributeProof({ text: 'set', merge: 'set', mergeDefault: 'custom-driver' }),
  });
  const plan = createMechanicalMergePlan({
    mergeBase: snapshot(A, [blob('shared.md', A)]),
    base: snapshot(B, [blob('shared.md', B)]),
    head: snapshot(H, [blob('shared.md', H)]),
    textMergeResolutions: [resolution],
  });

  assert.deepEqual(plan.finalTree, [blob('shared.md', C)]);
});

test('ancestor-descendant and case-folded overlaps still refuse semantic resolution', () => {
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
    treeTruncated: false,
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
  observed.tree = [blob('base.txt', B), blob('pr.txt', C, '100755')];
  observed.evidenceAncestors = {};
  const result = verifyFastForwardResult({ plan, mergeCommitSha: M, observed, requiredEvidence: [{ sha: RESEARCH }] });

  assert.equal(result.ok, false);
  assert.equal(result.retry, false);
  assert.match(result.reasons.join('\n'), /tree|evidence/);
});

test('incomplete Git tree snapshots fail closed before planning or verification', () => {
  assert.throws(
    () =>
      createMechanicalMergePlan({
        mergeBase: snapshot(A, [blob('base.txt', A)], true),
        base: snapshot(B, [blob('base.txt', B)]),
        head: snapshot(H, [blob('base.txt', A), blob('pr.txt', H)]),
      }),
    /complete|truncated/,
  );

  const plan = disjointPlan();
  const observed = successfulObservation(plan);
  delete observed.treeTruncated;
  const result = verifyFastForwardResult({ plan, mergeCommitSha: M, observed });

  assert.equal(result.ok, false);
  assert.equal(result.retry, false);
  assert.match(result.reasons.join('\n'), /complete|truncated/);
});