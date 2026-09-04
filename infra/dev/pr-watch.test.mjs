/**
 * Deterministic evidence for pr-watch lifecycle resolution.
 *
 *   node --test infra/dev/
 *
 * No network access and no dependencies: every case builds a synthetic pull-request payload
 * and asserts the resolved lifecycle state. Each block names the defect it pins.
 */

import test from 'node:test';
import assert from 'node:assert/strict';

import { dispositionOf, summarize, resolveLifecycle, stateLines, diff } from './pr-watch.mjs';

const HEAD = 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa';
const OLD = 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb';

function pr({ reviews = [], checks = [{ name: 'gate', conclusion: 'SUCCESS' }], threads = [], ...rest } = {}) {
  return {
    number: 1,
    title: 'test',
    isDraft: false,
    headRefOid: HEAD,
    baseRefName: 'main',
    baseRefOid: 'cccccccccccccccccccccccccccccccccccccccc',
    state: 'OPEN',
    mergeable: 'MERGEABLE',
    mergeStateStatus: 'CLEAN',
    reviews: { totalCount: reviews.length, nodes: reviews },
    reviewThreads: { totalCount: threads.length, nodes: threads },
    commits: { nodes: [{ commit: { statusCheckRollup: { contexts: { nodes: checks } } } }] },
    ...rest,
  };
}

const review = (opts) => ({
  author: { login: opts.author ?? 'reviewer' },
  state: opts.state ?? 'COMMENTED',
  submittedAt: opts.at,
  commit: { oid: opts.commit ?? HEAD },
  body: opts.body ?? '',
});

const stateOf = (payload, comparison) => resolveLifecycle(summarize(payload, comparison)).state;
const READY_BODY = 'Looks good.\n\nDisposition: **READY TO MERGE**.';

test('disposition parsing', async (t) => {
  await t.test('reads an explicit final disposition', () => {
    assert.equal(dispositionOf(READY_BODY), 'READY TO MERGE');
    assert.equal(dispositionOf('Disposition: CHANGES REQUIRED'), 'CHANGES REQUIRED');
    assert.equal(dispositionOf('Disposition: **NON-BLOCKING FOLLOW-UPS ONLY**'), 'NON-BLOCKING FOLLOW-UPS ONLY');
  });

  // Regression: a review whose prose quoted "Disposition: READY TO MERGE" as an example was
  // read as merge-ready even though its closing disposition was CHANGES REQUIRED.
  await t.test('ignores a disposition quoted mid-sentence and takes the final one', () => {
    const body = [
      'Reviewer B can submit a later comment with `Disposition: READY TO MERGE`; the earlier',
      'blocker is then discarded.',
      '',
      'Disposition: **CHANGES REQUIRED**.',
    ].join('\n');
    assert.equal(dispositionOf(body), 'CHANGES REQUIRED');
  });

  await t.test('takes the last disposition when several are line-anchored', () => {
    assert.equal(dispositionOf('Disposition: READY TO MERGE\n\nDisposition: CHANGES REQUIRED'), 'CHANGES REQUIRED');
  });

  await t.test('never guesses a verdict from prose', () => {
    assert.equal(dispositionOf('I think this is READY TO MERGE once CI passes.'), null);
    assert.equal(dispositionOf('no verdict here'), null);
    assert.equal(dispositionOf(''), null);
    assert.equal(dispositionOf(null), null);
  });

  // REV-005: an inline or code-quoted marker is discussion, and must not manufacture the
  // positive review authority needed to reach READY TO MERGE.
  await t.test('an inline or code-quoted marker is not a verdict when nothing is anchored', () => {
    assert.equal(dispositionOf('A reviewer may write `Disposition: READY TO MERGE` in prose.'), null);
    assert.equal(dispositionOf('For example, Disposition: READY TO MERGE would end the review.'), null);
  });

  await t.test('a list-marked disposition still counts, a blockquoted one does not', () => {
    assert.equal(dispositionOf('- Disposition: READY TO MERGE'), 'READY TO MERGE');
    // Quoted text is another review's verdict, not this one's.
    assert.equal(dispositionOf('> Disposition: **READY TO MERGE**'), null);
  });

  // REV-005: the marker must actually END the review. A verdict followed by substantive
  // blocker prose is not the reviewer's final disposition.
  await t.test('a disposition followed by substantive prose is not final', () => {
    const body = [
      'Disposition: READY TO MERGE',
      '',
      'Actually, on reflection the resolver still has a false-positive path and',
      'this needs another pass before it can merge.',
    ].join('\n');
    assert.equal(dispositionOf(body), null);
  });

  await t.test('trailing blank lines do not defeat a final disposition', () => {
    assert.equal(dispositionOf('Looks good.\n\nDisposition: **READY TO MERGE**.\n\n   \n'), 'READY TO MERGE');
  });

  await t.test('an earlier disposition does not win when the review ends on another', () => {
    const body = 'Disposition: READY TO MERGE\n\nsecond pass follows\n\nDisposition: **CHANGES REQUIRED**.';
    assert.equal(dispositionOf(body), 'CHANGES REQUIRED');
  });
});

test('required check identity (REV-003)', async (t) => {
  const reviews = [review({ at: '2026-01-01T00:00:00Z', body: READY_BODY })];
  const comparison = { aheadBy: 1, behindBy: 0 };

  await t.test('an unrelated green context cannot substitute for the required gate', () => {
    const checks = [{ name: 'Secret scan', conclusion: 'SUCCESS' }];
    assert.equal(stateOf(pr({ reviews, checks }), comparison), 'AWAITING');
  });

  await t.test('a pending gate is not green even when everything else passed', () => {
    const checks = [
      { name: 'Secret scan', conclusion: 'SUCCESS' },
      { name: 'gate', conclusion: null, status: 'QUEUED' },
    ];
    assert.equal(stateOf(pr({ reviews, checks }), comparison), 'AWAITING');
  });

  await t.test('a failing gate blocks', () => {
    assert.equal(stateOf(pr({ reviews, checks: [{ name: 'gate', conclusion: 'FAILURE' }] }), comparison), 'CHANGES REQUIRED');
  });

  await t.test('a green gate alongside other contexts is validation evidence', () => {
    const checks = [
      { name: 'gate', conclusion: 'SUCCESS' },
      { name: 'Secret scan', conclusion: 'SUCCESS' },
      { name: 'Java checks', conclusion: 'SKIPPED' },
    ];
    assert.equal(stateOf(pr({ reviews, checks }), comparison), 'READY TO MERGE');
  });

  await t.test('the required check name is configurable', () => {
    const payload = pr({ reviews, checks: [{ name: 'ci/custom', conclusion: 'SUCCESS' }] });
    assert.equal(resolveLifecycle(summarize(payload, comparison, 'ci/custom')).state, 'READY TO MERGE');
    assert.equal(resolveLifecycle(summarize(payload, comparison, 'gate')).state, 'AWAITING');
  });
});

test('terminal pull-request states (REV-006)', async (t) => {
  const reviews = [review({ at: '2026-01-01T00:00:00Z', body: READY_BODY })];
  const comparison = { aheadBy: 1, behindBy: 0 };

  await t.test('a merged PR reports a terminal state, not an open-PR lifecycle', () => {
    const resolved = resolveLifecycle(summarize(pr({ reviews, state: 'MERGED' }), comparison));
    assert.equal(resolved.state, 'MERGED');
    assert.equal(resolved.terminal, true);
  });

  await t.test('a closed PR reports a terminal state', () => {
    const resolved = resolveLifecycle(summarize(pr({ reviews, state: 'CLOSED' }), comparison));
    assert.equal(resolved.state, 'CLOSED');
    assert.equal(resolved.terminal, true);
  });

  await t.test('a draft-to-ready transition produces a watch signal', () => {
    const draft = stateLines(summarize(pr({ reviews, isDraft: true }), comparison));
    const ready = stateLines(summarize(pr({ reviews, isDraft: false }), comparison));
    assert.ok(diff(draft, ready).length > 0, 'readiness change must be observable');
  });

  await t.test('a merge produces a watch signal', () => {
    const open = stateLines(summarize(pr({ reviews }), comparison));
    const merged = stateLines(summarize(pr({ reviews, state: 'MERGED' }), comparison));
    assert.ok(diff(open, merged).length > 0, 'merge must be observable');
  });

  await t.test('required-check presence is part of the watched projection', () => {
    const withGate = stateLines(summarize(pr({ reviews }), comparison));
    const without = stateLines(summarize(pr({ reviews, checks: [] }), comparison));
    assert.ok(diff(withGate, without).length > 0, 'required-check absence must be observable');
  });
});

test('review-thread truncation (REV-008)', async (t) => {
  const reviews = [review({ at: '2026-01-01T00:00:00Z', body: READY_BODY })];
  const comparison = { aheadBy: 1, behindBy: 0 };
  const truncated = pr({ reviews });
  truncated.reviewThreads = { totalCount: 250, nodes: [{ isResolved: true }] };

  await t.test('truncated threads hold single-shot resolution at AWAITING', () => {
    assert.equal(stateOf(truncated, comparison), 'AWAITING');
  });

  // The resolver's answer changes, so --watch must see it: every returned thread is
  // resolved in both cases, so openThreads alone would show no difference at all.
  await t.test('crossing the truncation boundary produces a watch signal', () => {
    const whole = pr({ reviews, threads: [{ isResolved: true }] });
    const before = stateLines(summarize(whole, comparison));
    const after = stateLines(summarize(truncated, comparison));
    assert.notDeepEqual(before, after);
    assert.ok(diff(before, after).length > 0, 'thread truncation must be observable');
  });
});

test('review freshness against the base (REV-001)', async (t) => {
  const approved = [review({ at: '2026-01-01T00:00:00Z', body: READY_BODY })];

  await t.test('ready when level with the base', () => {
    assert.equal(stateOf(pr({ reviews: approved }), { aheadBy: 1, behindBy: 0 }), 'READY TO MERGE');
  });

  await t.test('a base advance invalidates an otherwise clean review', () => {
    assert.equal(stateOf(pr({ reviews: approved }), { aheadBy: 1, behindBy: 1 }), 'AWAITING');
  });

  await t.test('base identity and distance are part of the watched state', () => {
    const level = stateLines(summarize(pr({ reviews: approved }), { aheadBy: 1, behindBy: 0 }));
    const behind = stateLines(summarize(pr({ reviews: approved }), { aheadBy: 1, behindBy: 1 }));
    assert.notDeepEqual(level, behind);
    assert.ok(diff(level, behind).length > 0, 'base movement must produce a detectable change');
  });
});

test('blocking review aggregation (REV-002)', async (t) => {
  await t.test('a later positive review cannot mask another reviewer standing blocker', () => {
    const reviews = [
      review({ author: 'alice', at: '2026-01-01T00:00:00Z', state: 'CHANGES_REQUESTED' }),
      review({ author: 'bob', at: '2026-01-02T00:00:00Z', body: READY_BODY }),
    ];
    assert.equal(stateOf(pr({ reviews }), { aheadBy: 1, behindBy: 0 }), 'CHANGES REQUIRED');
  });

  // GitHub clears requested changes only on an approving review by the same collaborator,
  // or on dismissal. A later COMMENT does not, so neither may this.
  await t.test('a later comment by the same author does NOT clear their CHANGES_REQUESTED', () => {
    const reviews = [
      review({ author: 'alice', at: '2026-01-01T00:00:00Z', state: 'CHANGES_REQUESTED' }),
      review({ author: 'alice', at: '2026-01-02T00:00:00Z', body: READY_BODY }),
    ];
    assert.equal(stateOf(pr({ reviews }), { aheadBy: 1, behindBy: 0 }), 'CHANGES REQUIRED');
  });

  await t.test('an approving review by the same author clears their CHANGES_REQUESTED', () => {
    const reviews = [
      review({ author: 'alice', at: '2026-01-01T00:00:00Z', state: 'CHANGES_REQUESTED' }),
      review({ author: 'alice', at: '2026-01-02T00:00:00Z', state: 'APPROVED', body: READY_BODY }),
    ];
    assert.equal(stateOf(pr({ reviews }), { aheadBy: 1, behindBy: 0 }), 'READY TO MERGE');
  });

  await t.test('a dismissed review clears the block', () => {
    const reviews = [
      review({ author: 'alice', at: '2026-01-01T00:00:00Z', state: 'CHANGES_REQUESTED' }),
      review({ author: 'alice', at: '2026-01-02T00:00:00Z', state: 'DISMISSED' }),
      review({ author: 'bob', at: '2026-01-03T00:00:00Z', body: READY_BODY }),
    ];
    assert.equal(stateOf(pr({ reviews }), { aheadBy: 1, behindBy: 0 }), 'READY TO MERGE');
  });

  await t.test('an approval before the block does not clear it', () => {
    const reviews = [
      review({ author: 'alice', at: '2026-01-01T00:00:00Z', state: 'APPROVED' }),
      review({ author: 'alice', at: '2026-01-02T00:00:00Z', state: 'CHANGES_REQUESTED' }),
    ];
    assert.equal(stateOf(pr({ reviews }), { aheadBy: 1, behindBy: 0 }), 'CHANGES REQUIRED');
  });

  await t.test('a CHANGES REQUIRED disposition blocks as firmly as CHANGES_REQUESTED', () => {
    const reviews = [review({ at: '2026-01-01T00:00:00Z', body: 'Disposition: **CHANGES REQUIRED**.' })];
    assert.equal(stateOf(pr({ reviews }), { aheadBy: 1, behindBy: 0 }), 'CHANGES REQUIRED');
  });

  // A PR must be able to leave CHANGES REQUIRED. AGENTS.md's cycle is remediate -> AWAITING
  // for re-evaluation, so a disposition attached to a superseded commit must not pin the PR
  // in CHANGES REQUIRED forever.
  await t.test('a CHANGES REQUIRED disposition on a superseded head becomes AWAITING', () => {
    const reviews = [review({ at: '2026-01-01T00:00:00Z', commit: OLD, body: 'Disposition: **CHANGES REQUIRED**.' })];
    assert.equal(stateOf(pr({ reviews }), { aheadBy: 1, behindBy: 0 }), 'AWAITING');
  });

  // ...but a formal GitHub CHANGES_REQUESTED review is not cleared by pushing.
  await t.test('a formal CHANGES_REQUESTED review still blocks after the head moves', () => {
    const reviews = [review({ at: '2026-01-01T00:00:00Z', commit: OLD, state: 'CHANGES_REQUESTED' })];
    assert.equal(stateOf(pr({ reviews }), { aheadBy: 1, behindBy: 0 }), 'CHANGES REQUIRED');
  });

  await t.test('a truncated review window cannot resolve READY', () => {
    const payload = pr({ reviews: [review({ at: '2026-01-01T00:00:00Z', body: READY_BODY })] });
    payload.reviews.totalCount = 500;
    assert.equal(stateOf(payload, { aheadBy: 1, behindBy: 0 }), 'AWAITING');
  });
});

test('validation presence (REV-003)', async (t) => {
  const reviews = [review({ at: '2026-01-01T00:00:00Z', body: READY_BODY })];
  const comparison = { aheadBy: 1, behindBy: 0 };

  await t.test('absent checks are not green', () => {
    assert.equal(stateOf(pr({ reviews, checks: [] }), comparison), 'AWAITING');
  });

  await t.test('pending checks are not green', () => {
    const checks = [{ name: 'gate', conclusion: null, status: 'IN_PROGRESS' }];
    assert.equal(stateOf(pr({ reviews, checks }), comparison), 'AWAITING');
  });

  await t.test('failing checks are CHANGES REQUIRED', () => {
    const checks = [{ name: 'gate', conclusion: 'FAILURE' }];
    assert.equal(stateOf(pr({ reviews, checks }), comparison), 'CHANGES REQUIRED');
  });

  await t.test('skipped and neutral count as green', () => {
    const checks = [
      { name: 'gate', conclusion: 'SUCCESS' },
      { name: 'java', conclusion: 'SKIPPED' },
      { name: 'legacy', context: 'legacy', state: 'SUCCESS' },
    ];
    assert.equal(stateOf(pr({ reviews, checks }), comparison), 'READY TO MERGE');
  });
});

test('remaining lifecycle inputs', async (t) => {
  const reviews = [review({ at: '2026-01-01T00:00:00Z', body: READY_BODY })];
  const comparison = { aheadBy: 1, behindBy: 0 };

  await t.test('a draft is never ready', () => {
    assert.equal(stateOf(pr({ reviews, isDraft: true }), comparison), 'AWAITING');
  });

  await t.test('a conflict is CHANGES REQUIRED', () => {
    assert.equal(stateOf(pr({ reviews, mergeable: 'CONFLICTING' }), comparison), 'CHANGES REQUIRED');
  });

  await t.test('unresolved threads hold it at AWAITING', () => {
    assert.equal(stateOf(pr({ reviews, threads: [{ isResolved: false }] }), comparison), 'AWAITING');
  });

  await t.test('a review on an older head awaits re-review', () => {
    const stale = [review({ at: '2026-01-01T00:00:00Z', commit: OLD, body: READY_BODY })];
    assert.equal(stateOf(pr({ reviews: stale }), comparison), 'AWAITING');
  });

  await t.test('no review at all awaits review', () => {
    assert.equal(stateOf(pr({ reviews: [] }), comparison), 'AWAITING');
  });

  await t.test('a review with no explicit disposition awaits', () => {
    const vague = [review({ at: '2026-01-01T00:00:00Z', body: 'nice work' })];
    assert.equal(stateOf(pr({ reviews: vague }), comparison), 'AWAITING');
  });
});

test('watch diffing reports removals as well as additions', () => {
  assert.deepEqual(diff(['a', 'b'], ['b', 'c']), ['- a', '+ c']);
  assert.deepEqual(diff(['a'], []), ['- a']);
  assert.deepEqual(diff([], ['a']), ['+ a']);
  assert.deepEqual(diff(['a'], ['a']), []);
});
