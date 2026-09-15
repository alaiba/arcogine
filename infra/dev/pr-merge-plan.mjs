#!/usr/bin/env node
/**
 * Pure planning and verification helpers for the connector-only PR freshness fallback.
 *
 * This module deliberately does not call GitHub or perform a ref mutation. A connector
 * (or another repository-scoped adapter) supplies Git tree/commit data and applies the
 * returned commit/ref specifications. The narrow accepted tree shape is intentional:
 * regular-file leaf entries only, with exact blob SHA and mode preservation. Directory
 * entries from a recursive Git tree are ignored; symlinks, submodules, renames/copies,
 * and path-shape ambiguity fail closed.
 */

const SHA_PATTERN = /^[0-9a-f]{40}$/i;
const REGULAR_FILE_MODES = new Set(['100644', '100755']);

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

function assertDisjointChanges(headChanges, baseChanges) {
  for (const headChange of headChanges) {
    for (const baseChange of baseChanges) {
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
 * B (the live base), and H (the inspected PR head). The result is a plan only; it does
 * not create a Git object or mutate a branch.
 */
function createMechanicalMergePlan({ mergeBase, base, head }) {
  if (!mergeBase || !base || !head) throw new Error('merge base, live base, and PR head are required');
  const mergeBaseSha = requireSha(mergeBase.sha, 'merge base SHA');
  const baseSha = requireSha(base.sha, 'live base SHA');
  const headSha = requireSha(head.sha, 'PR head SHA');

  const mergeBaseTree = normalizeTree(mergeBase.tree, 'merge base tree');
  const baseTree = normalizeTree(base.tree, 'live base tree');
  const headTree = normalizeTree(head.tree, 'PR head tree');
  const baseChanges = changedEntries(mergeBaseTree, baseTree);
  const headChanges = changedEntries(mergeBaseTree, headTree);

  if (headChanges.length === 0) throw new Error('PR side has no intended change against the merge base');
  assertNoAncestorCollisions(headChanges.map((change) => change.path), 'PR changes');
  assertNoAncestorCollisions(baseChanges.map((change) => change.path), 'base changes');
  assertNoRenameShape(headChanges, 'PR changes');
  assertNoRenameShape(baseChanges, 'base changes');
  assertDisjointChanges(headChanges, baseChanges);

  const finalTree = new Map(baseTree);
  for (const change of headChanges) {
    if (change.after) finalTree.set(change.path, cloneEntry(change.after));
    else finalTree.delete(change.path);
  }
  assertNoCaseFoldCollisions([...finalTree.keys()], 'merged tree');
  assertNoAncestorCollisions([...finalTree.keys()], 'merged tree');

  const intendedDiff = changedEntries(baseTree, finalTree);
  if (intendedDiff.length === 0) throw new Error('merged tree has no non-empty PR diff against the live base');
  for (const change of headChanges) {
    if (!entryEqual(finalTree.get(change.path) ?? null, change.after)) {
      throw new Error(`merged tree did not preserve the intended PR state at ${change.path}`);
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
