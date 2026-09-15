#!/usr/bin/env node
/**
 * Pure planning and verification helpers for the connector-only PR freshness fallback.
 *
 * This module deliberately does not call GitHub or perform a ref mutation. A connector
 * (or another repository-scoped adapter) supplies Git tree/commit data and applies the
 * returned commit/ref specifications. The accepted tree shape is intentionally narrow:
 * complete, non-truncated recursive Git tree snapshots with regular-file leaf entries
 * only, exact blob SHA and mode preservation. Directory entries from a complete
 * recursive Git tree are ignored; symlinks, submodules, renames/copies, and path-shape
 * ambiguity fail closed. Same-path regular-text modify/modify overlaps may be supplied
 * as clean git-merge-file resolutions over the exact A/B/H blobs only when the exact
 * PR-head Git attributes/config prove the built-in text merge driver is applicable.
 */

const SHA_PATTERN = /^[0-9a-f]{40}$/i;
const REGULAR_FILE_MODES = new Set(['100644', '100755']);
const TEXT_ATTRIBUTE_VALUES = new Set(['set', 'auto', 'unspecified']);

function requireSha(value, label) {
  if (!SHA_PATTERN.test(String(value ?? ''))) {
    throw new Error(`${label} must be a full Git SHA`);
  }
  return String(value).toLowerCase();
}

function requirePath(path, label) {
  if (typeof path !== 'string' || path.length === 0) {
    throw new Error(`${label} must be a non-empty repository-relative path`);
  }
  if (
    path.startsWith('/') ||
    path.endsWith('/') ||
    path.includes('\\') ||
    path.includes('//') ||
    path.split('/').some((part) => part === '' || part === '.' || part === '..')
  ) {
    throw new Error(`${label} is not a normalized repository-relative path: ${path}`);
  }
  if (path.normalize('NFC') !== path) {
    throw new Error(`${label} is not Unicode-normalized: ${path}`);
  }
  return path;
}

function cloneEntry(entry) {
  return entry ? { path: entry.path, mode: entry.mode, type: entry.type, sha: entry.sha } : null;
}

function entryEqual(left, right) {
  if (left === null || left === undefined || right === null || right === undefined) {
    return (left === null || left === undefined) && (right === null || right === undefined);
  }
  return left.path === right.path && left.mode === right.mode && left.type === right.type && left.sha === right.sha;
}

function caseFold(path) {
  return path.toLowerCase();
}

function assertNoCaseFoldCollisions(paths, label) {
  const seen = new Map();
  for (const path of paths) {
    const folded = caseFold(path);
    const previous = seen.get(folded);
    if (previous && previous !== path) {
      throw new Error(`${label} contains case-folded path ambiguity: ${previous} and ${path}`);
    }
    seen.set(folded, path);
  }
}

function isSameOrDescendant(path, ancestor, caseSensitive = true) {
  const left = caseSensitive ? path : caseFold(path);
  const right = caseSensitive ? ancestor : caseFold(ancestor);
  return left === right || left.startsWith(`${right}/`);
}

function assertNoAncestorCollisions(paths, label) {
  const sorted = [...paths].sort();
  for (let index = 0; index < sorted.length - 1; index += 1) {
    if (isSameOrDescendant(sorted[index + 1], sorted[index])) {
      throw new Error(`${label} contains a file/directory ancestor collision: ${sorted[index]} and ${sorted[index + 1]}`);
    }
  }
}

function normalizeTree(entries, label) {
  if (!Array.isArray(entries)) throw new Error(`${label} must be an array of Git tree entries`);

  const byPath = new Map();
  for (const raw of entries) {
    if (!raw || typeof raw !== 'object') throw new Error(`${label} contains an invalid tree entry`);
    const path = requirePath(raw.path, `${label} entry path`);

    // Recursive Git tree responses include directory entries as well as leaves. Directory
    // SHAs are derived from their children, so only leaves participate in the replay plan.
    if (raw.type === 'tree') {
      if (raw.mode !== '040000') throw new Error(`${label} has an invalid directory mode at ${path}`);
      continue;
    }

    if (raw.type !== 'blob' || !REGULAR_FILE_MODES.has(String(raw.mode))) {
      throw new Error(
        `${label} contains unsupported entry at ${path}; only regular blob modes 100644/100755 are mechanically replayable`,
      );
    }
    const entry = {
      path,
      mode: String(raw.mode),
      type: 'blob',
      sha: requireSha(raw.sha, `${label} entry ${path} SHA`),
    };
    const previous = byPath.get(path);
    if (previous && !entryEqual(previous, entry)) {
      throw new Error(`${label} contains duplicate path entries with different states: ${path}`);
    }
    byPath.set(path, entry);
  }

  assertNoCaseFoldCollisions([...byPath.keys()], label);
  return byPath;
}

function requireCompleteTreeSnapshot(snapshot, label) {
  if (!snapshot || typeof snapshot !== 'object') {
    throw new Error(`${label} snapshot is required`);
  }
  if (snapshot.truncated !== false) {
    throw new Error(`${label} snapshot must explicitly prove complete tree data with truncated=false`);
  }
  return {
    sha: requireSha(snapshot.sha, `${label} SHA`),
    tree: normalizeTree(snapshot.tree, `${label} tree`),
  };
}

function sortedEntries(byPath) {
  return [...byPath.values()]
    .sort((left, right) => (left.path < right.path ? -1 : left.path > right.path ? 1 : 0))
    .map(cloneEntry);
}

function changedEntries(from, to) {
  const paths = new Set([...from.keys(), ...to.keys()]);
  return [...paths]
    .sort()
    .map((path) => ({ path, before: cloneEntry(from.get(path) ?? null), after: cloneEntry(to.get(path) ?? null) }))
    .filter((change) => !entryEqual(change.before, change.after));
}

function assertNoRenameShape(changes, label) {
  const additions = changes.filter((change) => !change.before && change.after);
  const deletions = changes.filter((change) => change.before && !change.after);
  if (additions.length > 0 && deletions.length > 0) {
    throw new Error(`${label} contains additions and deletions; rename/copy interpretation is not mechanically provable`);
  }
}

function assertNoCrossPathOverlap(headChanges, baseChanges) {
  for (const headChange of headChanges) {
    for (const baseChange of baseChanges) {
      if (headChange.path === baseChange.path) continue;
      if (isSameOrDescendant(headChange.path, baseChange.path) || isSameOrDescendant(baseChange.path, headChange.path)) {
        throw new Error(
          `PR and base changes overlap at or below ${headChange.path}/${baseChange.path}; semantic merge resolution is required`,
        );
      }
      if (
        isSameOrDescendant(headChange.path, baseChange.path, false) ||
        isSameOrDescendant(baseChange.path, headChange.path, false)
      ) {
        throw new Error(
          `PR and base changes have a case-folded path collision at ${headChange.path}/${baseChange.path}`,
        );
      }
    }
  }
}

function requireAttributeValue(value, label) {
  if (typeof value !== 'string' || value.length === 0) {
    throw new Error(`${label} must be a git-check-attr/config value`);
  }
  return value;
}

function normalizeTextMergeAttributeProof(raw, path) {
  if (!raw || typeof raw !== 'object') {
    throw new Error(`text merge resolution for ${path} must include Git attribute eligibility proof`);
  }

  const sourceSha = requireSha(raw.sourceSha, `text merge ${path} attribute source SHA`);
  const text = requireAttributeValue(raw.text, `text merge ${path} text attribute`);
  const merge = requireAttributeValue(raw.merge, `text merge ${path} merge attribute`);
  const mergeDefault = requireAttributeValue(raw.mergeDefault, `text merge ${path} merge.default value`);

  if (!TEXT_ATTRIBUTE_VALUES.has(text)) {
    throw new Error(`text merge resolution for ${path} is not eligible for text merge: text attribute is ${text}`);
  }

  // A named low-level merge driver remains configurable through merge.<name>.driver,
  // including a driver literally named "text". Therefore merge=text and
  // merge.default=text do not prove Git will use its built-in text algorithm. The
  // fallback accepts only the two forms that select the built-in algorithm without a
  // configurable driver name: boolean `merge` set, or `merge` unspecified with no
  // merge.default configured.
  if (merge === 'unspecified') {
    if (mergeDefault !== 'unspecified') {
      throw new Error(
        `text merge resolution for ${path} is not eligible for the built-in text merge: merge.default is ${mergeDefault}`,
      );
    }
  } else if (merge !== 'set') {
    throw new Error(
      `text merge resolution for ${path} is not eligible for the built-in text merge: merge attribute is ${merge}`,
    );
  }

  return { sourceSha, text, merge, mergeDefault };
}

function normalizeTextMergeResolutions(resolutions) {
  if (!Array.isArray(resolutions)) throw new Error('textMergeResolutions must be an array');
  const byPath = new Map();
  for (const raw of resolutions) {
    if (!raw || typeof raw !== 'object') throw new Error('text merge resolution must be an object');
    const path = requirePath(raw.path, 'text merge resolution path');
    if (raw.method !== 'git-merge-file') {
      throw new Error(`text merge resolution for ${path} must use method git-merge-file`);
    }
    if (raw.clean !== true) {
      throw new Error(`text merge resolution for ${path} must prove a clean conflict-free merge`);
    }
    if (byPath.has(path)) throw new Error(`duplicate text merge resolution for ${path}`);
    byPath.set(path, {
      path,
      method: 'git-merge-file',
      clean: true,
      mergeBaseBlobSha: requireSha(raw.mergeBaseBlobSha, `text merge ${path} merge-base blob SHA`),
      baseBlobSha: requireSha(raw.baseBlobSha, `text merge ${path} live-base blob SHA`),
      headBlobSha: requireSha(raw.headBlobSha, `text merge ${path} PR-head blob SHA`),
      resultBlobSha: requireSha(raw.resultBlobSha, `text merge ${path} result blob SHA`),
      attributeProof: normalizeTextMergeAttributeProof(raw.attributeProof, path),
    });
  }
  return byPath;
}

function requireTextModifyModifyResolution({ path, mergeBaseTree, baseTree, headTree, headSha, resolution }) {
  if (!resolution) {
    throw new Error(
      `PR and base both modify ${path}; a clean three-way text merge resolution is required before the merge tree is mechanically provable`,
    );
  }

  const ancestor = mergeBaseTree.get(path) ?? null;
  const baseAfter = baseTree.get(path) ?? null;
  const headAfter = headTree.get(path) ?? null;
  if (!ancestor || !baseAfter || !headAfter) {
    throw new Error(
      `overlap at ${path} is not a regular modify/modify text case; add/delete and delete/modify resolution remain unsupported`,
    );
  }
  if (ancestor.mode !== baseAfter.mode || ancestor.mode !== headAfter.mode) {
    throw new Error(`overlap at ${path} changes file mode; content merge cannot choose mode semantics`);
  }
  if (
    resolution.mergeBaseBlobSha !== ancestor.sha ||
    resolution.baseBlobSha !== baseAfter.sha ||
    resolution.headBlobSha !== headAfter.sha
  ) {
    throw new Error(`text merge resolution for ${path} does not match the exact A/B/H blob inputs`);
  }
  if (resolution.attributeProof.sourceSha !== headSha) {
    throw new Error(`text merge attribute proof for ${path} is not bound to the exact inspected PR head`);
  }

  return {
    path,
    mode: ancestor.mode,
    type: 'blob',
    sha: resolution.resultBlobSha,
  };
}

function sameEntries(left, right) {
  if (left.length !== right.length) return false;
  return left.every((entry, index) => entryEqual(entry, right[index]));
}

function sameChanges(left, right) {
  if (left.length !== right.length) return false;
  return left.every(
    (change, index) =>
      change.path === right[index].path &&
      entryEqual(change.before, right[index].before) &&
      entryEqual(change.after, right[index].after),
  );
}

/**
 * Construct the exact tree for a narrow, mechanically provable merge.
 *
 * The caller resolves A (the merge base) and supplies recursive Git tree entries for A,
 * B (the live base), and H (the inspected PR head). Disjoint supported changes replay
 * exactly. A same-path regular-file modify/modify overlap is accepted only when the
 * caller supplies both a clean git-merge-file result over the exact A/B/H blob bytes and
 * Git attribute/config evidence from the exact H tree proving the built-in text merge
 * driver is applicable. The result is a plan only; it does not create a Git object or
 * mutate a branch.
 */
function createMechanicalMergePlan({ mergeBase, base, head, textMergeResolutions = [] }) {
  if (!mergeBase || !base || !head) throw new Error('merge base, live base, and PR head are required');
  const mergeBaseSnapshot = requireCompleteTreeSnapshot(mergeBase, 'merge base');
  const baseSnapshot = requireCompleteTreeSnapshot(base, 'live base');
  const headSnapshot = requireCompleteTreeSnapshot(head, 'PR head');
  const mergeBaseSha = mergeBaseSnapshot.sha;
  const baseSha = baseSnapshot.sha;
  const headSha = headSnapshot.sha;

  const mergeBaseTree = mergeBaseSnapshot.tree;
  const baseTree = baseSnapshot.tree;
  const headTree = headSnapshot.tree;
  const baseChanges = changedEntries(mergeBaseTree, baseTree);
  const headChanges = changedEntries(mergeBaseTree, headTree);
  const baseChangesByPath = new Map(baseChanges.map((change) => [change.path, change]));
  const textMergesByPath = normalizeTextMergeResolutions(textMergeResolutions);
  const usedTextMerges = new Set();

  if (headChanges.length === 0) throw new Error('PR side has no intended change against the merge base');
  assertNoAncestorCollisions(headChanges.map((change) => change.path), 'PR changes');
  assertNoAncestorCollisions(baseChanges.map((change) => change.path), 'base changes');
  assertNoRenameShape(headChanges, 'PR changes');
  assertNoRenameShape(baseChanges, 'base changes');
  assertNoCrossPathOverlap(headChanges, baseChanges);

  const finalTree = new Map(baseTree);
  for (const change of headChanges) {
    const baseChange = baseChangesByPath.get(change.path);
    if (!baseChange) {
      if (change.after) finalTree.set(change.path, cloneEntry(change.after));
      else finalTree.delete(change.path);
      continue;
    }

    if (entryEqual(change.after, baseChange.after)) {
      continue;
    }

    const resolution = textMergesByPath.get(change.path);
    const mergedEntry = requireTextModifyModifyResolution({
      path: change.path,
      mergeBaseTree,
      baseTree,
      headTree,
      headSha,
      resolution,
    });
    finalTree.set(change.path, mergedEntry);
    usedTextMerges.add(change.path);
  }

  for (const path of textMergesByPath.keys()) {
    if (!usedTextMerges.has(path)) {
      throw new Error(`text merge resolution for ${path} was not required by an exact same-path modify/modify overlap`);
    }
  }

  assertNoCaseFoldCollisions([...finalTree.keys()], 'merged tree');
  assertNoAncestorCollisions([...finalTree.keys()], 'merged tree');

  const intendedDiff = changedEntries(baseTree, finalTree);
  if (intendedDiff.length === 0) throw new Error('merged tree has no non-empty PR diff against the live base');

  for (const change of headChanges) {
    const baseChange = baseChangesByPath.get(change.path);
    if (!baseChange || entryEqual(change.after, baseChange.after)) {
      if (!entryEqual(finalTree.get(change.path) ?? null, change.after)) {
        throw new Error(`merged tree did not preserve the intended PR state at ${change.path}`);
      }
      continue;
    }
    const resolution = textMergesByPath.get(change.path);
    if ((finalTree.get(change.path)?.sha ?? null) !== resolution?.resultBlobSha) {
      throw new Error(`merged tree did not preserve the verified text-merge result at ${change.path}`);
    }
  }

  return {
    kind: 'mechanical-merge',
    mergeBaseSha,
    baseSha,
    headSha,
    baseTree: sortedEntries(baseTree),
    finalTree: sortedEntries(finalTree),
    baseChanges,
    headChanges,
    textMergeResolutions: [...usedTextMerges].sort().map((path) => ({ ...textMergesByPath.get(path) })),
    intendedDiff,
  };
}

function requireRef(value, label) {
  if (typeof value !== 'string' || value.trim() === '' || /[\r\n]/.test(value)) {
    throw new Error(`${label} must be a non-empty ref name`);
  }
  return value;
}

function requireHumanIdentity(identity, label) {
  if (!identity || typeof identity !== 'object' || !identity.name || !identity.email) {
    throw new Error(`${label} must include a human name and email`);
  }
  if (/\b(bot|dependabot|github actions|codex|claude|openai)\b/i.test(`${identity.name} ${identity.email}`)) {
    throw new Error(`${label} appears agent- or bot-owned`);
  }
  return { name: String(identity.name), email: String(identity.email) };
}

/** Create the commit object fields for the history-preserving merge. */
function createMergeCommitSpec(
  plan,
  { baseRef = 'main', headRef, author, committer, humanIdentityVerified = false } = {},
) {
  if (!plan || plan.kind !== 'mechanical-merge') throw new Error('a mechanical merge plan is required');
  if (!humanIdentityVerified) {
    throw new Error('human repository-owner identity must be verified before creating the merge commit');
  }
  const normalizedBaseRef = requireRef(baseRef, 'base ref');
  const normalizedHeadRef = requireRef(headRef, 'head ref');
  const normalizedAuthor = requireHumanIdentity(author, 'commit author');
  const normalizedCommitter = requireHumanIdentity(committer, 'commit committer');

  return {
    tree: plan.finalTree,
    parents: [plan.headSha, plan.baseSha],
    message: `Merge ${normalizedBaseRef} into ${normalizedHeadRef}`,
    author: normalizedAuthor,
    committer: normalizedCommitter,
  };
}

/**
 * Plan the one connector ref update. The server must enforce the returned force=false
 * operation. A false ancestry result is a retry signal, never permission to force-update.
 */
function createFastForwardUpdate({ expectedHead, currentHead, target, currentHeadIsAncestor, force = false }) {
  const expected = requireSha(expectedHead, 'expected PR head SHA');
  const current = requireSha(currentHead, 'current PR head SHA');
  const newHead = requireSha(target, 'merge commit SHA');
  if (force !== false) throw new Error('mechanical freshness fallback requires force=false; never escalate to force=true');
  if (current !== expected) throw new Error(`PR head moved from ${expected} to ${current}; discard and recompute`);
  if (currentHeadIsAncestor !== true) {
    throw new Error(`current PR head ${current} is not an ancestor of ${newHead}; server rejected a non-fast-forward candidate, retry`);
  }
  return { expectedOldHead: expected, newHead, force: false };
}

function verificationFailure(reasons, retry = false) {
  return { ok: false, retry, reasons };
}

/** Verify the post-update invariants without making another mutation. */
function verifyFastForwardResult({ plan, mergeCommitSha, observed, requiredEvidence = [] }) {
  if (!plan || plan.kind !== 'mechanical-merge') return verificationFailure(['mechanical merge plan is missing']);
  const reasons = [];
  const target = requireSha(mergeCommitSha, 'merge commit SHA');
  if (!observed || typeof observed !== 'object') return verificationFailure(['post-update observation is missing']);

  if (String(observed.prState ?? '').toUpperCase() !== 'OPEN') reasons.push('pull request is no longer open');
  if (String(observed.headSha ?? '').toLowerCase() !== target) reasons.push(`PR head is not the expected merge commit ${target}`);
  if (observed.oldHeadAncestor !== true) reasons.push('old PR head is not an ancestor of the resulting head');
  if (observed.baseAncestor !== true) reasons.push('incorporated base is not an ancestor of the resulting head');
  if (observed.currentBaseSha !== plan.baseSha) {
    reasons.push(`live base advanced from ${plan.baseSha} to ${observed.currentBaseSha ?? '(unknown)'}`);
  }
  if (observed.currentBaseAncestor !== true) reasons.push('current live base is not an ancestor of the resulting head');
  if (observed.behindBy !== 0) reasons.push(`PR is still ${observed.behindBy ?? '(unknown)'} commit(s) behind the live base`);
  if (!Number.isInteger(observed.aheadBy) || observed.aheadBy <= 0) reasons.push('PR has no non-empty change ahead of the live base');

  const parents = Array.isArray(observed.parents) ? observed.parents.map((parent) => String(parent).toLowerCase()) : [];
  if (parents.length !== 2 || parents[0] !== plan.headSha || parents[1] !== plan.baseSha) {
    reasons.push('merge commit parents do not match first-parent H / second-parent B');
  }

  try {
    if (observed.treeTruncated !== false) {
      throw new Error('post-update tree must explicitly prove complete tree data with truncated=false');
    }
    const observedTree = normalizeTree(observed.tree, 'post-update tree');
    const expectedTree = plan.finalTree;
    if (!sameEntries(sortedEntries(observedTree), expectedTree)) reasons.push('resulting merge tree differs from the verified mechanical tree');
    const observedDiff = changedEntries(normalizeTree(plan.baseTree, 'planned base tree'), observedTree);
    if (!sameChanges(observedDiff, plan.intendedDiff)) {
      reasons.push('net live-base diff does not preserve exactly the intended PR change');
    }
  } catch (error) {
    reasons.push(error.message);
  }

  const evidenceAncestors = observed.evidenceAncestors ?? {};
  for (const evidence of requiredEvidence) {
    const sha = requireSha(evidence.sha, 'handed-off evidence SHA');
    if (evidenceAncestors[sha] !== true) reasons.push(`handed-off evidence commit ${sha} is not reachable from the resulting head`);
  }

  const retry = observed.currentBaseSha !== plan.baseSha || observed.headSha?.toLowerCase() !== target;
  return reasons.length === 0 ? { ok: true, retry: false, reasons: [] } : verificationFailure(reasons, retry);
}

export {
  createMechanicalMergePlan,
  createMergeCommitSpec,
  createFastForwardUpdate,
  verifyFastForwardResult,
};