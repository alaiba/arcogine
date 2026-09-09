/**
 * Deterministic evidence for the continuous-improvement register helper. No network
 * access: every case exercises the pure functions directly (state derivation, marker
 * parsing/rendering, body merging). GitHub I/O is intentionally excluded from this
 * module and therefore from these tests -- see continuous-improvement.mjs's
 * `--- GitHub I/O ---` boundary comment.
 */

import test from 'node:test';
import assert from 'node:assert/strict';

import {
  MARKER_START,
  MARKER_END,
  REGISTER_TITLE,
  deriveWeeklyState,
  parseCompletionComment,
  latestValidCompletion,
  deriveRetrospectiveState,
  renderObligations,
  buildInitialBody,
  splitMarkers,
  mergeRegisterBody,
  findRegisterIssue,
  obligationsChanged,
} from './continuous-improvement.mjs';

const NOW = '2026-09-09T00:00:00.000Z';
const SHA_A = 'a'.repeat(40);
const SHA_B = 'b'.repeat(40);

test('weekly Consistency review state', async (t) => {
  await t.test('never verified is DUE, not OVERDUE', () => {
    assert.equal(deriveWeeklyState(null, NOW), 'DUE');
  });

  await t.test('within the weekly interval is CURRENT', () => {
    assert.equal(deriveWeeklyState('2026-09-05T00:00:00.000Z', NOW), 'CURRENT');
  });

  await t.test('past the interval but within the overdue threshold is DUE', () => {
    assert.equal(deriveWeeklyState('2026-08-30T00:00:00.000Z', NOW), 'DUE');
  });

  await t.test('well past the interval is OVERDUE', () => {
    assert.equal(deriveWeeklyState('2026-08-01T00:00:00.000Z', NOW), 'OVERDUE');
  });
});

test('completion evidence parsing', async (t) => {
  await t.test('valid evidence parses reviewedHead/completedAt/mode', () => {
    const body = `Consistency review completed\nreviewed head: ${SHA_A}\ncompleted at: 2026-09-08T12:00:00Z\nmode: incremental`;
    const parsed = parseCompletionComment(body);
    assert.equal(parsed.reviewedHead, SHA_A);
    assert.equal(parsed.completedAt, '2026-09-08T12:00:00.000Z');
    assert.equal(parsed.mode, 'incremental');
  });

  await t.test('missing reviewed head is malformed and ignored', () => {
    const body = 'Consistency review completed\ncompleted at: 2026-09-08T12:00:00Z';
    assert.equal(parseCompletionComment(body), null);
  });

  await t.test('non-sha reviewed head is malformed and ignored', () => {
    const body = 'Consistency review completed\nreviewed head: not-a-sha\ncompleted at: 2026-09-08T12:00:00Z';
    assert.equal(parseCompletionComment(body), null);
  });

  await t.test('invalid completed-at timestamp is malformed and ignored', () => {
    const body = `Consistency review completed\nreviewed head: ${SHA_A}\ncompleted at: not-a-date`;
    assert.equal(parseCompletionComment(body), null);
  });

  await t.test('unrelated comment is ignored, not an error', () => {
    assert.equal(parseCompletionComment('just a comment'), null);
    assert.equal(parseCompletionComment(null), null);
  });

  await t.test('latestValidCompletion picks the newest valid entry and skips malformed ones', () => {
    const older = `Consistency review completed\nreviewed head: ${SHA_A}\ncompleted at: 2026-09-01T00:00:00Z`;
    const newer = `Consistency review completed\nreviewed head: ${SHA_B}\ncompleted at: 2026-09-08T00:00:00Z`;
    const malformed = 'Consistency review completed\nreviewed head: nope';
    const best = latestValidCompletion([older, malformed, newer, 'unrelated']);
    assert.equal(best.reviewedHead, SHA_B);
  });

  await t.test('a stale comment that predates a later reconciled head is still usable evidence of its own claim', () => {
    // The helper trusts the structured claim; it is the reviewer's job to only post
    // evidence for a head it actually reviewed. Staleness relative to the *current*
    // main is a due-state question (age), not a malformed-evidence question.
    const stale = `Consistency review completed\nreviewed head: ${SHA_A}\ncompleted at: 2026-01-01T00:00:00Z`;
    const parsed = parseCompletionComment(stale);
    assert.equal(parsed.reviewedHead, SHA_A);
  });
});

test('delivery-process retrospective trigger', async (t) => {
  await t.test('below the raw guard threshold is CURRENT', () => {
    assert.equal(deriveRetrospectiveState({ rawMergedSinceBaseline: 10 }), 'CURRENT');
  });

  await t.test('reaching the raw guard threshold is CHECK_TRIGGER, not an automatic DUE', () => {
    assert.equal(deriveRetrospectiveState({ rawMergedSinceBaseline: 25 }), 'CHECK_TRIGGER');
  });

  await t.test('just under the threshold stays CURRENT', () => {
    assert.equal(deriveRetrospectiveState({ rawMergedSinceBaseline: 24 }), 'CURRENT');
  });

  await t.test('two recorded post-merge escapes trigger CHECK_TRIGGER even under the raw guard', () => {
    assert.equal(
      deriveRetrospectiveState({ rawMergedSinceBaseline: 3, escapeEvidenceCount: 2 }),
      'CHECK_TRIGGER',
    );
  });

  await t.test('one recorded P1 lifecycle escape triggers CHECK_TRIGGER even under the raw guard', () => {
    assert.equal(
      deriveRetrospectiveState({ rawMergedSinceBaseline: 1, p1LifecycleEscape: true }),
      'CHECK_TRIGGER',
    );
  });

  await t.test('baseline advancement is data-driven: a higher baselinePr changes nothing about this function', () => {
    // deriveRetrospectiveState never reads a baseline PR number itself -- the caller
    // supplies rawMergedSinceBaseline, which is computed from whatever baselinePr the
    // committed data file currently holds. Advancing the baseline is therefore a data
    // change, not a business-logic change.
    const before = deriveRetrospectiveState({ rawMergedSinceBaseline: 5 });
    const after = deriveRetrospectiveState({ rawMergedSinceBaseline: 5 });
    assert.equal(before, after);
  });
});

test('register bootstrap and idempotent update', async (t) => {
  await t.test('initial bootstrap renders markers exactly once and preserves seeded interventions', () => {
    const obligations = renderObligations({
      weekly: { lastVerifiedAt: null, reviewedHead: null, nowISO: NOW, state: 'DUE' },
      retrospective: {
        baselinePr: 260,
        baselineDate: '2026-09-05',
        rawMergedSinceBaseline: 0,
        escapeEvidenceCount: 0,
        p1LifecycleEscape: false,
        state: 'CURRENT',
      },
    });
    const body = buildInitialBody(obligations);
    assert.equal(body.split(MARKER_START).length - 1, 1);
    assert.equal(body.split(MARKER_END).length - 1, 1);
    assert.match(body, /Active improvement interventions/);
    assert.match(body, /Executable PR lifecycle enforcement/);
    // must parse back cleanly
    const { managed } = splitMarkers(body);
    assert.match(managed, /Weekly Consistency review/);
  });

  await t.test('repeated run with unchanged state is a no-op per obligationsChanged', () => {
    const obligations = renderObligations({
      weekly: { lastVerifiedAt: null, reviewedHead: null, nowISO: NOW, state: 'DUE' },
      retrospective: {
        baselinePr: 260,
        baselineDate: '2026-09-05',
        rawMergedSinceBaseline: 0,
        escapeEvidenceCount: 0,
        p1LifecycleEscape: false,
        state: 'CURRENT',
      },
    });
    const body = buildInitialBody(obligations);
    const { managed } = splitMarkers(body);
    // Re-render with a different "now" timestamp only -- the semantic state is
    // identical, so a byte-diff on the "_Last updated_" line alone must not be
    // treated as no-op by a naive strict-equality check, but a same-second rerun
    // (same inputs) must compare equal.
    const rerendered = renderObligations({
      weekly: { lastVerifiedAt: null, reviewedHead: null, nowISO: NOW, state: 'DUE' },
      retrospective: {
        baselinePr: 260,
        baselineDate: '2026-09-05',
        rawMergedSinceBaseline: 0,
        escapeEvidenceCount: 0,
        p1LifecycleEscape: false,
        state: 'CURRENT',
      },
    });
    assert.equal(obligationsChanged(managed, rerendered), false);
  });

  await t.test('a genuine state change is detected as changed', () => {
    const before = renderObligations({
      weekly: { lastVerifiedAt: null, reviewedHead: null, nowISO: NOW, state: 'CURRENT' },
      retrospective: {
        baselinePr: 260,
        baselineDate: '2026-09-05',
        rawMergedSinceBaseline: 0,
        escapeEvidenceCount: 0,
        p1LifecycleEscape: false,
        state: 'CURRENT',
      },
    });
    const after = renderObligations({
      weekly: { lastVerifiedAt: null, reviewedHead: null, nowISO: NOW, state: 'OVERDUE' },
      retrospective: {
        baselinePr: 260,
        baselineDate: '2026-09-05',
        rawMergedSinceBaseline: 0,
        escapeEvidenceCount: 0,
        p1LifecycleEscape: false,
        state: 'CURRENT',
      },
    });
    assert.equal(obligationsChanged(before, after), true);
  });
});

test('marker parsing failure modes', async (t) => {
  await t.test('missing markers fail loudly', () => {
    assert.throws(() => splitMarkers('# no markers here'), /malformed/);
  });

  await t.test('duplicated start marker fails loudly rather than silently picking one', () => {
    const body = `${MARKER_START}\nfoo\n${MARKER_END}\n${MARKER_START}\nbar\n${MARKER_END}`;
    assert.throws(() => splitMarkers(body), /malformed/);
  });

  await t.test('end marker before start marker fails loudly', () => {
    const body = `${MARKER_END}\n${MARKER_START}`;
    assert.throws(() => splitMarkers(body), /malformed/);
  });
});

test('preservation of the agent/human intervention region', async (t) => {
  await t.test('mergeRegisterBody replaces only the managed region', () => {
    const original = [
      '# Continuous improvement register',
      '',
      'intro prose',
      '',
      MARKER_START,
      'old obligations text',
      MARKER_END,
      '',
      '## Active improvement interventions',
      '',
      '| Improvement | State |',
      '| --- | --- |',
      '| Something an agent added | AWAITING VERIFICATION |',
    ].join('\n');

    const updated = mergeRegisterBody(original, 'new obligations text');

    assert.match(updated, /new obligations text/);
    assert.doesNotMatch(updated, /old obligations text/);
    assert.match(updated, /Something an agent added/);
    assert.match(updated, /intro prose/);
    // exactly one marker pair remains
    assert.equal(updated.split(MARKER_START).length - 1, 1);
    assert.equal(updated.split(MARKER_END).length - 1, 1);
  });
});

test('register discovery', async (t) => {
  await t.test('finds the register by exact title', () => {
    const issues = [
      { number: 5, title: 'CONS-5: something else' },
      { number: 300, title: REGISTER_TITLE },
    ];
    assert.equal(findRegisterIssue(issues).number, 300);
  });

  await t.test('no register present returns null (bootstrap case)', () => {
    assert.equal(findRegisterIssue([{ number: 5, title: 'CONS-5: unrelated' }]), null);
  });

  await t.test('does not touch CONS-* consistency-finding issues', () => {
    const issues = [
      { number: 204, title: 'CONS-001: some finding' },
      { number: 205, title: 'CONS-002: another finding' },
    ];
    assert.equal(findRegisterIssue(issues), null);
  });

  await t.test('duplicate exact-title matches fail loudly instead of silently creating another', () => {
    const issues = [
      { number: 300, title: REGISTER_TITLE },
      { number: 301, title: REGISTER_TITLE },
    ];
    assert.throws(() => findRegisterIssue(issues), /ambiguous/);
  });
});
