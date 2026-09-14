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
  isAccountedCompletion,
  isPersistedConsistencyFinding,
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
const FINDING_123 = {
  number: 123,
  title: 'CONS-123: example finding',
  body: '## Finding\n\nSeverity: P2\nCategory: PUBLIC_DOC_DRIFT\nStatus: OPEN',
};
const FINDING_204 = {
  number: 204,
  title: 'CONS-001: legacy finding',
  body: '## Finding\n\nSeverity: P2\nCategory: ARCHITECTURE_STALENESS\nStatus: RESOLVED',
};
const FINDINGS = new Map([
  [FINDING_123.number, FINDING_123],
  [FINDING_204.number, FINDING_204],
]);

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
  const cleanEvidence = (mode, sha = SHA_A, completedAt = '2026-09-08T12:00:00Z') =>
    `Consistency review completed\nreviewed head: ${sha}\ncompleted at: ${completedAt}\nmode: ${mode}\nresult: CLEAN\nfinding issues: none`;
  const findingsEvidence = (mode, issueList = '#123', sha = SHA_A, completedAt = '2026-09-08T12:00:00Z') =>
    `Consistency review completed\nreviewed head: ${sha}\ncompleted at: ${completedAt}\nmode: ${mode}\nresult: FINDINGS\nfinding issues: ${issueList}`;
  const trusted = (body, createdAt) => ({ body, authorAssociation: 'OWNER', createdAt });

  await t.test('FULL + CLEAN + no finding issues qualifies', () => {
    const parsed = parseCompletionComment(cleanEvidence('FULL'));
    assert.equal(parsed.mode, 'FULL');
    assert.equal(parsed.result, 'CLEAN');
    assert.equal(parsed.findingIssueNumbers.length, 0);
    assert.equal(isAccountedCompletion(parsed, FINDINGS), true);
  });

  await t.test('INCREMENTAL + CLEAN + no finding issues qualifies', () => {
    const parsed = parseCompletionComment(cleanEvidence('INCREMENTAL'));
    assert.equal(parsed.mode, 'INCREMENTAL');
    assert.equal(isAccountedCompletion(parsed, FINDINGS), true);
  });

  await t.test('FINDINGS with a persisted Consistency issue qualifies', () => {
    const parsed = parseCompletionComment(findingsEvidence('FULL'));
    assert.deepEqual(parsed.findingIssueNumbers, [123]);
    assert.equal(isAccountedCompletion(parsed, FINDINGS), true);
  });

  await t.test('valid evidence parses the complete accounted schema', () => {
    const parsed = parseCompletionComment(cleanEvidence('incremental', SHA_A, '2026-09-08T12:00:00Z'));
    assert.equal(parsed.reviewedHead, SHA_A);
    assert.equal(parsed.completedAt, '2026-09-08T12:00:00.000Z');
    assert.equal(parsed.mode, 'INCREMENTAL');
    assert.equal(parsed.result, 'CLEAN');
  });

  await t.test('missing reviewed head is malformed and ignored', () => {
    const body = 'Consistency review completed\ncompleted at: 2026-09-08T12:00:00Z\nmode: FULL\nresult: CLEAN\nfinding issues: none';
    assert.equal(parseCompletionComment(body), null);
  });

  await t.test('non-sha reviewed head is malformed and ignored', () => {
    const body = 'Consistency review completed\nreviewed head: not-a-sha\ncompleted at: 2026-09-08T12:00:00Z\nmode: FULL\nresult: CLEAN\nfinding issues: none';
    assert.equal(parseCompletionComment(body), null);
  });

  await t.test('invalid completed-at timestamp is malformed and ignored', () => {
    const body = `Consistency review completed\nreviewed head: ${SHA_A}\ncompleted at: not-a-date\nmode: FULL\nresult: CLEAN\nfinding issues: none`;
    assert.equal(parseCompletionComment(body), null);
  });

  await t.test('unrelated comment is ignored, not an error', () => {
    assert.equal(parseCompletionComment('just a comment'), null);
    assert.equal(parseCompletionComment(null), null);
  });

  await t.test('latestValidCompletion picks the newest valid entry and skips malformed ones', () => {
    const older = trusted(
      cleanEvidence('FULL', SHA_A, '2026-09-01T00:00:00Z'),
      '2026-09-01T00:00:00Z',
    );
    const newer = trusted(
      cleanEvidence('INCREMENTAL', SHA_B, '2026-09-08T00:00:00Z'),
      '2026-09-08T00:00:00Z',
    );
    const malformed = trusted('Consistency review completed\nreviewed head: nope', '2026-09-08T00:00:00Z');
    const best = latestValidCompletion([older, malformed, newer, trusted('unrelated', NOW)], NOW, FINDINGS);
    assert.equal(best.reviewedHead, SHA_B);
  });

  await t.test('legacy four-line evidence is not an accounted baseline', () => {
    const stale = `Consistency review completed\nreviewed head: ${SHA_A}\ncompleted at: 2026-01-01T00:00:00Z\nmode: FULL`;
    const parsed = parseCompletionComment(stale);
    assert.equal(parsed, null);
    assert.equal(latestValidCompletion([trusted(stale, '2026-01-01T00:00:00Z')], NOW, FINDINGS), null);
    assert.equal(deriveWeeklyState(null, NOW), 'DUE');
  });

  await t.test('a later new-format qualifying completion supersedes legacy history', () => {
    const legacy = trusted(
      'Consistency review completed\nreviewed head: ' + SHA_A + '\ncompleted at: 2026-09-01T00:00:00Z\nmode: FULL',
      '2026-09-01T00:00:00Z',
    );
    const fresh = trusted(cleanEvidence('FULL', SHA_B, '2026-09-08T00:00:00Z'), '2026-09-08T00:00:00Z');
    assert.equal(latestValidCompletion([legacy, fresh], NOW, FINDINGS).reviewedHead, SHA_B);
  });

  await t.test('an unauthorized commenter cannot fabricate completion evidence by matching the syntax', () => {
    // Structured syntax alone must not confer completion authority.
    // Anyone can comment on a public issue; only a trusted author association
    // (OWNER/MEMBER/COLLABORATOR) counts.
    const forged = {
      body: cleanEvidence('FULL'),
      authorAssociation: 'NONE',
      createdAt: '2026-09-08T00:00:00Z',
    };
    assert.equal(latestValidCompletion([forged], NOW), null);
  });

  await t.test('a future-dated completion claim is rejected even from a trusted author', () => {
    // A fabricated future "completed at" would otherwise make daysBetween
    // negative and keep the weekly obligation CURRENT indefinitely.
    const future = trusted(
      cleanEvidence('FULL', SHA_A, '2026-12-31T00:00:00Z'),
      '2026-12-31T00:00:00Z',
    );
    assert.equal(latestValidCompletion([future], NOW), null);
  });

  await t.test('an authorized, non-future completion is accepted', () => {
    const valid = trusted(
      cleanEvidence('FULL'),
      '2026-09-08T00:00:00Z',
    );
    const best = latestValidCompletion([valid], NOW, FINDINGS);
    assert.equal(best.reviewedHead, SHA_A);
  });

  await t.test('FINDINGS with no issue identities cannot advance the baseline', () => {
    const invalid = trusted(
      `Consistency review completed\nreviewed head: ${SHA_A}\ncompleted at: 2026-09-08T00:00:00Z\nmode: FULL\nresult: FINDINGS\nfinding issues: none`,
      '2026-09-08T00:00:00Z',
    );
    assert.equal(parseCompletionComment(invalid.body), null);
    assert.equal(latestValidCompletion([invalid], NOW, FINDINGS), null);
  });

  await t.test('CLEAN with issue identities is internally inconsistent', () => {
    const invalid = `Consistency review completed\nreviewed head: ${SHA_A}\ncompleted at: 2026-09-08T00:00:00Z\nmode: FULL\nresult: CLEAN\nfinding issues: #123`;
    assert.equal(parseCompletionComment(invalid), null);
  });

  await t.test('duplicate and malformed issue references are rejected', () => {
    const duplicate = findingsEvidence('FULL', '#123, #123');
    const malformed = findingsEvidence('FULL', '#123, issue-124');
    assert.equal(parseCompletionComment(duplicate), null);
    assert.equal(parseCompletionComment(malformed), null);
  });

  await t.test('bogus or non-Consistency issue references do not qualify', () => {
    const bogusIssue = { number: 123, title: 'ordinary issue', body: 'not a Consistency finding' };
    const parsed = parseCompletionComment(findingsEvidence('FULL'));
    assert.equal(isAccountedCompletion(parsed, new Map([[123, bogusIssue]])), false);
    assert.equal(isPersistedConsistencyFinding(bogusIssue, 123), false);
    assert.equal(isPersistedConsistencyFinding(FINDING_204, 204), true);
  });

  await t.test('UNPERSISTED diagnostic findings are explicit but never qualify', () => {
    const parsed = parseCompletionComment(findingsEvidence('DIAGNOSTIC_ONLY', 'UNPERSISTED'));
    assert.equal(parsed.findingIssuesKind, 'UNPERSISTED');
    assert.equal(isAccountedCompletion(parsed, FINDINGS), false);
    assert.equal(latestValidCompletion([trusted(findingsEvidence('DIAGNOSTIC_ONLY', 'UNPERSISTED'), '2026-09-08T00:00:00Z')], NOW, FINDINGS), null);
  });

  await t.test('PR_FORWARD completion never qualifies or refreshes the weekly baseline', () => {
    const parsed = parseCompletionComment(cleanEvidence('PR_FORWARD'));
    assert.equal(isAccountedCompletion(parsed, FINDINGS), false);
    assert.equal(latestValidCompletion([trusted(cleanEvidence('PR_FORWARD'), '2026-09-08T00:00:00Z')], NOW, FINDINGS), null);
  });

  await t.test('unknown mode fails closed', () => {
    assert.equal(parseCompletionComment(cleanEvidence('WEEKLY')), null);
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

  await t.test('repeated run with unchanged state is a no-op per obligationsChanged, even across a clock change', () => {
    // renderObligations always embeds "_Last updated: <nowISO>_", so this
    // regression test must use two genuinely different `nowISO` values -- reusing
    // the same NOW for both renders cannot expose a comparison that fails to
    // exclude the volatile timestamp line.
    const LATER = '2026-09-09T06:00:00.000Z';
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
    const rerendered = renderObligations({
      weekly: { lastVerifiedAt: null, reviewedHead: null, nowISO: LATER, state: 'DUE' },
      retrospective: {
        baselinePr: 260,
        baselineDate: '2026-09-05',
        rawMergedSinceBaseline: 0,
        escapeEvidenceCount: 0,
        p1LifecycleEscape: false,
        state: 'CURRENT',
      },
    });
    assert.notEqual(managed.trim(), rerendered.trim(), 'sanity check: the two renders must actually differ by clock alone');
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
