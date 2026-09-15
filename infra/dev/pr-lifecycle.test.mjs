/**
 * Deterministic evidence for pr-lifecycle resolution.
 *
 *   node --test infra/dev/pr-lifecycle.test.mjs
 *
 * No network access and no dependencies: every case builds a synthetic pull-request payload
 * and asserts the resolved lifecycle state. Each block names the defect it pins.
 */

import test from 'node:test';
import assert from 'node:assert/strict';

import { dispositionOf, summarize, resolveLifecycle } from './pr-lifecycle.mjs';

const HEAD = 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa';
const OLD = 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb';
const AUTHORIZED_CHECKS = [
  { name: 'gate', conclusion: 'SUCCESS' },
  { name: 'disposition', conclusion: 'SUCCESS' },
];

function pr({ reviews = [], checks = AUTHORIZED_CHECKS, threads = [], ...rest } = {}) {
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
  });

  await t.test('removed/unsupported disposition vocabulary is not recognized', () => {
    assert.equal(dispositionOf('Disposition: READY AFTER CI'), null);
    assert.equal(dispositionOf('Disposition: **NON-BLOCKING FOLLOW-UPS ONLY**'), null);
    assert.equal(dispositionOf('Disposition: APPROVED'), null);
  });

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

  await t.test('an inline or code-quoted marker is not a verdict when nothing is anchored', () => {
    assert.equal(dispositionOf('A reviewer may write `Disposition: READY TO MERGE` in prose.'), null);
    assert.equal(dispositionOf('For example, Disposition: READY TO MERGE would end the review.'), null);
  });

  await t.test('a list-marked disposition still counts, a blockquoted one does not', () => {
    assert.equal(dispositionOf('- Disposition: READY TO MERGE'), 'READY TO MERGE');
    assert.equal(dispositionOf('> Disposition: **READY TO MERGE**'), null);
  });

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

  await t.test('same-line trailing prose or negation is not a verdict', () => {
    assert.equal(dispositionOf('Disposition: READY TO MERGE? Actually no.'), null);
    assert.equal(dispositionOf('Disposition: READY TO MERGE once the resolver is fixed'), null);
    assert.equal(dispositionOf('Disposition: CHANGES REQUIRED, but only cosmetically -- see below'), null);
  });

  await t.test('intentional surrounding markdown and a closing stop are still accepted', () => {
    assert.equal(dispositionOf('Disposition: **READY TO MERGE**.'), 'READY TO MERGE');
    assert.equal(dispositionOf('Disposition: READY TO MERGE'), 'READY TO MERGE');
    assert.equal(dispositionOf('**Disposition: CHANGES REQUIRED**'), 'CHANGES REQUIRED');
  });
});

test('review-authorization check identity', async (t) => {
  const comparison = { aheadBy: 1, behindBy: 0 };

  await t.test('trusted disposition success can authorize a PR without a review', () => {
    assert.equal(stateOf(pr({ reviews: [] }), comparison), 'READY TO MERGE');
  });

  await t.test('an absent disposition check leaves authorization unresolved', () => {
    const checks = [{ name: 'gate', conclusion: 'SUCCESS' }];
    assert.equal(stateOf(pr({ reviews: [], checks }), comparison), 'AWAITING');
  });

  await t.test('a failed disposition check is AWAITING when no explicit blocker exists', () => {
    const checks = [
      { name: 'gate', conclusion: 'SUCCESS' },
      { name: 'disposition', conclusion: 'FAILURE' },
    ];
    assert.equal(stateOf(pr({ reviews: [], checks }), comparison), 'AWAITING');
  });

});

test('required check identity', async (t) => {
  const reviews = [review({ at: '2026-01-01T00:00:00Z', body: READY_BODY })];
  const comparison = { aheadBy: 1, behindBy: 0 };

  await t.test('an unrelated green context cannot substitute for the required gate', () => {
    const checks = [
      { name: 'Secret scan', conclusion: 'SUCCESS' },
      { name: 'disposition', conclusion: 'SUCCESS' },
    ];
    assert.equal(stateOf(pr({ reviews, checks }), comparison), 'AWAITING');
  });

  await t.test('a pending gate is not green even when everything else passed', () => {
    const checks = [
      { name: 'Secret scan', conclusion: 'SUCCESS' },
      { name: 'gate', conclusion: null, status: 'QUEUED' },
      { name: 'disposition', conclusion: 'SUCCESS' },
    ];
    assert.equal(stateOf(pr({ reviews, checks }), comparison), 'AWAITING');
  });

  await t.test('a failing gate blocks', () => {
    const checks = [
      { name: 'gate', conclusion: 'FAILURE' },
      { name: 'disposition', conclusion: 'SUCCESS' },
    ];
    assert.equal(stateOf(pr({ reviews, checks }), comparison), 'CHANGES REQUIRED');
  });

  await t.test('a skipped required gate is not validation evidence', () => {
    const checks = [
      { name: 'gate', conclusion: 'SKIPPED' },
      { name: 'disposition', conclusion: 'SUCCESS' },
    ];
    assert.equal(stateOf(pr({ reviews, checks }), comparison), 'AWAITING');
  });

  await t.test('a neutral required gate is not validation evidence', () => {
    const checks = [
      { name: 'gate', conclusion: 'NEUTRAL' },
      { name: 'disposition', conclusion: 'SUCCESS' },
    ];
    assert.equal(stateOf(pr({ reviews, checks }), comparison), 'AWAITING');
  });

  await t.test('a skipped gate is not rescued by other green contexts', () => {
    const checks = [
      { name: 'gate', conclusion: 'SKIPPED' },
      { name: 'Secret scan', conclusion: 'SUCCESS' },
      { name: 'Java checks', conclusion: 'SUCCESS' },
      { name: 'disposition', conclusion: 'SUCCESS' },
    ];
    assert.equal(stateOf(pr({ reviews, checks }), comparison), 'AWAITING');
  });

  await t.test('a green gate alongside other contexts is validation evidence', () => {
    const checks = [
      { name: 'gate', conclusion: 'SUCCESS' },
      { name: 'Secret scan', conclusion: 'SUCCESS' },
      { name: 'Java checks', conclusion: 'SKIPPED' },
      { name: 'disposition', conclusion: 'SUCCESS' },
    ];
    assert.equal(stateOf(pr({ reviews, checks }), comparison), 'READY TO MERGE');
  });

  await t.test('the required check name is configurable', () => {
    const payload = pr({ reviews, checks: [
      { name: 'ci/custom', conclusion: 'SUCCESS' },
      { name: 'disposition', conclusion: 'SUCCESS' },
    ] });
    assert.equal(resolveLifecycle(summarize(payload, comparison, 'ci/custom')).state, 'READY TO MERGE');
    assert.equal(resolveLifecycle(summarize(payload, comparison, 'gate')).state, 'AWAITING');
  });
});

test('terminal pull-request states', async (t) => {
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

});

test('review-thread truncation', async (t) => {
  const reviews = [review({ at: '2026-01-01T00:00:00Z', body: READY_BODY })];
  const comparison = { aheadBy: 1, behindBy: 0 };
  const truncated = pr({ reviews });
  truncated.reviewThreads = { totalCount: 250, nodes: [{ isResolved: true }] };

  await t.test('truncated threads hold single-shot resolution at AWAITING', () => {
    assert.equal(stateOf(truncated, comparison), 'AWAITING');
  });

});

test('base reconciliation normalization', async (t) => {
  const approved = [review({ at: '2026-01-01T00:00:00Z', body: READY_BODY })];

  await t.test('ready when level with the base', () => {
    assert.equal(stateOf(pr({ reviews: approved }), { aheadBy: 1, behindBy: 0 }), 'READY TO MERGE');
  });

  await t.test('a base advance requires reconciliation before substantive review', () => {
    const resolved = resolveLifecycle(
      summarize(pr({ reviews: approved }), { aheadBy: 1, behindBy: 1 }),
    );
    assert.equal(resolved.state, 'CHANGES REQUIRED');
    assert.match(resolved.reasons.join('\n'), /reconcile with the current base before substantive review/);
  });

});

test('blocking review aggregation', async (t) => {
  await t.test('a later positive review cannot mask another reviewer standing native blocker', () => {
    const reviews = [
      review({ author: 'alice', at: '2026-01-01T00:00:00Z', state: 'CHANGES_REQUESTED' }),
      review({ author: 'bob', at: '2026-01-02T00:00:00Z', body: READY_BODY }),
    ];
    assert.equal(stateOf(pr({ reviews }), { aheadBy: 1, behindBy: 0 }), 'CHANGES REQUIRED');
  });

  await t.test('a later comment by the same author does NOT clear accidental native CHANGES_REQUESTED', () => {
    const reviews = [
      review({ author: 'alice', at: '2026-01-01T00:00:00Z', state: 'CHANGES_REQUESTED' }),
      review({ author: 'alice', at: '2026-01-02T00:00:00Z', body: READY_BODY }),
    ];
    assert.equal(stateOf(pr({ reviews }), { aheadBy: 1, behindBy: 0 }), 'CHANGES REQUIRED');
  });

  await t.test('an approving review by the same author clears native CHANGES_REQUESTED', () => {
    const reviews = [
      review({ author: 'alice', at: '2026-01-01T00:00:00Z', state: 'CHANGES_REQUESTED' }),
      review({ author: 'alice', at: '2026-01-02T00:00:00Z', state: 'APPROVED', body: READY_BODY }),
    ];
    assert.equal(stateOf(pr({ reviews }), { aheadBy: 1, behindBy: 0 }), 'READY TO MERGE');
  });

  await t.test('a dismissed review clears the native block', () => {
    const reviews = [
      review({ author: 'alice', at: '2026-01-01T00:00:00Z', state: 'CHANGES_REQUESTED' }),
      review({ author: 'alice', at: '2026-01-02T00:00:00Z', state: 'DISMISSED' }),
      review({ author: 'bob', at: '2026-01-03T00:00:00Z', body: READY_BODY }),
    ];
    assert.equal(stateOf(pr({ reviews }), { aheadBy: 1, behindBy: 0 }), 'READY TO MERGE');
  });

  await t.test('an approval before the native block does not clear it', () => {
    const reviews = [
      review({ author: 'alice', at: '2026-01-01T00:00:00Z', state: 'APPROVED' }),
      review({ author: 'alice', at: '2026-01-02T00:00:00Z', state: 'CHANGES_REQUESTED' }),
    ];
    assert.equal(stateOf(pr({ reviews }), { aheadBy: 1, behindBy: 0 }), 'CHANGES REQUIRED');
  });

  await t.test('a CHANGES REQUIRED disposition blocks as firmly as native CHANGES_REQUESTED', () => {
    const reviews = [review({ at: '2026-01-01T00:00:00Z', body: 'Disposition: **CHANGES REQUIRED**.' })];
    assert.equal(stateOf(pr({ reviews }), { aheadBy: 1, behindBy: 0 }), 'CHANGES REQUIRED');
  });

  await t.test('a stale CHANGES REQUIRED disposition is history once current-head authorization succeeds', () => {
    const reviews = [review({ at: '2026-01-01T00:00:00Z', commit: OLD, body: 'Disposition: **CHANGES REQUIRED**.' })];
    assert.equal(stateOf(pr({ reviews }), { aheadBy: 1, behindBy: 0 }), 'READY TO MERGE');
  });

  await t.test('a stale CHANGES REQUIRED disposition cannot replace a missing current-head authorization', () => {
    const reviews = [review({ at: '2026-01-01T00:00:00Z', commit: OLD, body: 'Disposition: **CHANGES REQUIRED**.' })];
    const checks = [
      { name: 'gate', conclusion: 'SUCCESS' },
      { name: 'disposition', conclusion: 'FAILURE' },
    ];
    assert.equal(stateOf(pr({ reviews, checks }), { aheadBy: 1, behindBy: 0 }), 'AWAITING');
  });

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

test('validation presence', async (t) => {
  const reviews = [review({ at: '2026-01-01T00:00:00Z', body: READY_BODY })];
  const comparison = { aheadBy: 1, behindBy: 0 };

  await t.test('absent checks are not green', () => {
    assert.equal(stateOf(pr({ reviews, checks: [] }), comparison), 'AWAITING');
  });

  await t.test('pending checks are not green', () => {
    const checks = [
      { name: 'gate', conclusion: null, status: 'IN_PROGRESS' },
      { name: 'disposition', conclusion: 'SUCCESS' },
    ];
    assert.equal(stateOf(pr({ reviews, checks }), comparison), 'AWAITING');
  });

  await t.test('failing checks are CHANGES REQUIRED', () => {
    const checks = [
      { name: 'gate', conclusion: 'FAILURE' },
      { name: 'disposition', conclusion: 'SUCCESS' },
    ];
    assert.equal(stateOf(pr({ reviews, checks }), comparison), 'CHANGES REQUIRED');
  });

  await t.test('skipped and neutral auxiliary checks count as green', () => {
    const checks = [
      { name: 'gate', conclusion: 'SUCCESS' },
      { name: 'disposition', conclusion: 'SUCCESS' },
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

  await t.test('a stale READY review does not matter when current-head authorization is absent', () => {
    const stale = [review({ at: '2026-01-01T00:00:00Z', commit: OLD, body: READY_BODY })];
    const checks = [
      { name: 'gate', conclusion: 'SUCCESS' },
      { name: 'disposition', conclusion: 'FAILURE' },
    ];
    assert.equal(stateOf(pr({ reviews: stale, checks }), comparison), 'AWAITING');
  });

  await t.test('current-head CHANGES REQUIRED remains a blocker even while disposition check is stale-success', () => {
    const blocked = [review({ at: '2026-01-01T00:00:00Z', body: 'Disposition: **CHANGES REQUIRED**.' })];
    assert.equal(stateOf(pr({ reviews: blocked }), comparison), 'CHANGES REQUIRED');
  });

  await t.test('no review is acceptable only when trusted disposition authorization succeeds', () => {
    assert.equal(stateOf(pr({ reviews: [] }), comparison), 'READY TO MERGE');
    const checks = [
      { name: 'gate', conclusion: 'SUCCESS' },
      { name: 'disposition', conclusion: 'FAILURE' },
    ];
    assert.equal(stateOf(pr({ reviews: [], checks }), comparison), 'AWAITING');
  });

  await t.test('a vague review cannot compensate for failed disposition authorization', () => {
    const vague = [review({ at: '2026-01-01T00:00:00Z', body: 'nice work' })];
    const checks = [
      { name: 'gate', conclusion: 'SUCCESS' },
      { name: 'disposition', conclusion: 'FAILURE' },
    ];
    assert.equal(stateOf(pr({ reviews: vague, checks }), comparison), 'AWAITING');
  });

  await t.test('a removed disposition value never authorizes merge when the trusted check fails', () => {
    const stale = [review({ at: '2026-01-01T00:00:00Z', body: 'Disposition: **READY AFTER CI**' })];
    const checks = [
      { name: 'gate', conclusion: 'SUCCESS' },
      { name: 'disposition', conclusion: 'FAILURE' },
    ];
    assert.equal(stateOf(pr({ reviews: stale, checks }), comparison), 'AWAITING');
  });

  await t.test('authorization success with required CI pending awaits', () => {
    const checks = [
      { name: 'gate', conclusion: null, status: 'IN_PROGRESS' },
      { name: 'disposition', conclusion: 'SUCCESS' },
    ];
    assert.equal(stateOf(pr({ reviews, checks }), comparison), 'AWAITING');
  });

  await t.test('authorization success with required CI failure is CHANGES REQUIRED', () => {
    const checks = [
      { name: 'gate', conclusion: 'FAILURE' },
      { name: 'disposition', conclusion: 'SUCCESS' },
    ];
    assert.equal(stateOf(pr({ reviews, checks }), comparison), 'CHANGES REQUIRED');
  });

  await t.test('authorization success with required CI success and mergeable is READY TO MERGE', () => {
    assert.equal(stateOf(pr({ reviews }), comparison), 'READY TO MERGE');
  });
});
