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
    mergeable: 'MERGEABLE',
    mergeStateStatus: 'CLEAN',
    reviews: { totalCount: reviews.length, nodes: reviews },
    reviewThreads: { nodes: threads },
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

  await t.test('a reviewer own later review supersedes their earlier blocker', () => {
    const reviews = [
      review({ author: 'alice', at: '2026-01-01T00:00:00Z', state: 'CHANGES_REQUESTED' }),
      review({ author: 'alice', at: '2026-01-02T00:00:00Z', body: READY_BODY }),
    ];
    assert.equal(stateOf(pr({ reviews }), { aheadBy: 1, behindBy: 0 }), 'READY TO MERGE');
  });

  await t.test('a CHANGES REQUIRED disposition blocks as firmly as CHANGES_REQUESTED', () => {
    const reviews = [review({ at: '2026-01-01T00:00:00Z', body: 'Disposition: **CHANGES REQUIRED**.' })];
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
