/**
 * Pure deterministic evidence for the continuous-improvement helper.
 * Network I/O is intentionally excluded from this module.
 */

import test from 'node:test';
import assert from 'node:assert/strict';

import {
  MARKER_START,
  MARKER_END,
  REGISTER_ISSUE_NUMBER,
  deriveWeeklyState,
  parseCompletionComment,
  latestValidCompletion,
  isAccountedCompletion,
  isPersistedConsistencyFinding,
  deriveRetrospectiveState,
  renderObligations,
  splitMarkers,
  mergeRegisterBody,
  obligationsChanged,
} from './continuous-improvement.mjs';

const NOW = '2026-09-16T08:00:00.000Z';
const SHA_A = 'a'.repeat(40);
const SHA_B = 'b'.repeat(40);
const CURRENT_FINDING = {
  number: 341,
  title: 'CONS: example finding',
  body: 'Severity: P2\nCategory: PUBLIC_DOC_DRIFT',
  state: 'open',
};
const NUMERIC_FINDING = {
  number: 215,
  title: 'CONS-215: historical numeric finding',
  body: 'Severity: P2\nCategory: ARCHITECTURE_STALENESS\nStatus: RESOLVED',
  state: 'open',
};
const LEGACY_FINDING = {
  number: 204,
  title: 'CONS-001: calibration finding',
  body: 'Severity: P2\nCategory: ARCHITECTURE_STALENESS\nStatus: RESOLVED',
  state: 'open',
};
const FINDINGS = new Map([
  [341, CURRENT_FINDING],
  [215, NUMERIC_FINDING],
  [204, LEGACY_FINDING],
]);

const completion = ({ head = SHA_A, scope = 'FULL', findings = 'none' } = {}) =>
  `Consistency review completed\nhead: ${head}\nscope: ${scope}\nfindings: ${findings}`;
const trusted = (body, createdAt) => ({ body, authorAssociation: 'OWNER', createdAt });

test('fixed register identity', () => {
  assert.equal(REGISTER_ISSUE_NUMBER, 295);
});

test('weekly state', () => {
  assert.equal(deriveWeeklyState(null, NOW), 'DUE');
  assert.equal(deriveWeeklyState('2026-09-12T00:00:00Z', NOW), 'CURRENT');
  assert.equal(deriveWeeklyState('2026-09-06T00:00:00Z', NOW), 'DUE');
  assert.equal(deriveWeeklyState('2026-08-20T00:00:00Z', NOW), 'OVERDUE');
});

test('completion schema is small and strict', () => {
  const clean = parseCompletionComment(completion());
  assert.equal(clean.scope, 'FULL');
  assert.equal(clean.result, 'CLEAN');
  assert.deepEqual(clean.findingIssueNumbers, []);

  const findings = parseCompletionComment(completion({ scope: 'incremental', findings: '#341, #215' }));
  assert.equal(findings.scope, 'INCREMENTAL');
  assert.equal(findings.result, 'FINDINGS');
  assert.deepEqual(findings.findingIssueNumbers, [341, 215]);

  assert.equal(parseCompletionComment(`Consistency review completed\nhead: ${SHA_A}\nfindings: none`), null);
  assert.equal(parseCompletionComment(completion({ head: 'not-a-sha' })), null);
  assert.equal(parseCompletionComment(completion({ scope: 'PR_FORWARD' })), null);
  assert.equal(parseCompletionComment(completion({ findings: '#341, #341' })), null);
  assert.equal(parseCompletionComment(completion() + '\nresult: CLEAN'), null);
});

test('current and historical Consistency issue titles validate', () => {
  assert.equal(isPersistedConsistencyFinding(CURRENT_FINDING, 341), true);
  assert.equal(isPersistedConsistencyFinding(NUMERIC_FINDING, 215), true);
  assert.equal(isPersistedConsistencyFinding(LEGACY_FINDING, 204), true);
  assert.equal(isPersistedConsistencyFinding({ ...CURRENT_FINDING, title: 'ordinary issue' }, 341), false);
  assert.equal(isPersistedConsistencyFinding({ ...CURRENT_FINDING, body: 'Severity: P2' }, 341), false);
});

test('finding accounting requires real open Consistency issues', () => {
  const parsed = parseCompletionComment(completion({ findings: '#341' }));
  assert.equal(isAccountedCompletion(parsed, FINDINGS), true);
  assert.equal(isAccountedCompletion(parseCompletionComment(completion()), FINDINGS), true);
  assert.equal(isAccountedCompletion(parsed, new Map([[341, { ...CURRENT_FINDING, state: 'closed' }]])), false);
  assert.equal(isAccountedCompletion(parsed, new Map()), false);
});

test('latest completion uses GitHub comment time and requires a FULL baseline', () => {
  const isolatedIncremental = trusted(completion({ scope: 'INCREMENTAL', head: SHA_B }), '2026-09-15T00:00:00Z');
  assert.equal(latestValidCompletion([isolatedIncremental], NOW, FINDINGS), null);

  const full = trusted(completion({ head: SHA_A }), '2026-09-10T00:00:00Z');
  const incremental = trusted(completion({ scope: 'INCREMENTAL', head: SHA_B }), '2026-09-15T00:00:00Z');
  const latest = latestValidCompletion([full, incremental], NOW, FINDINGS);
  assert.equal(latest.reviewedHead, SHA_B);
  assert.equal(latest.completedAt, '2026-09-15T00:00:00.000Z');

  const forged = { body: completion({ head: SHA_B }), authorAssociation: 'NONE', createdAt: '2026-09-15T00:00:00Z' };
  assert.equal(latestValidCompletion([forged], NOW, FINDINGS), null);

  const future = trusted(completion({ head: SHA_B }), '2026-12-01T00:00:00Z');
  assert.equal(latestValidCompletion([future], NOW, FINDINGS), null);
});

test('finding completion validates cited issues before becoming a baseline', () => {
  const full = trusted(completion({ findings: '#341' }), '2026-09-15T00:00:00Z');
  assert.equal(latestValidCompletion([full], NOW, FINDINGS).result, 'FINDINGS');
  assert.equal(latestValidCompletion([full], NOW, new Map()), null);
});

test('retrospective guard remains mechanical', () => {
  assert.equal(deriveRetrospectiveState({ rawMergedSinceBaseline: 24 }), 'CURRENT');
  assert.equal(deriveRetrospectiveState({ rawMergedSinceBaseline: 25 }), 'CHECK_TRIGGER');
  assert.equal(deriveRetrospectiveState({ rawMergedSinceBaseline: 1, escapeEvidenceCount: 2 }), 'CHECK_TRIGGER');
  assert.equal(deriveRetrospectiveState({ rawMergedSinceBaseline: 1, p1LifecycleEscape: true }), 'CHECK_TRIGGER');
});

test('register marker update preserves human-owned content', () => {
  const body = `# Register\n\n${MARKER_START}\n\nold\n\n${MARKER_END}\n\n## Human\nkeep me\n`;
  const merged = mergeRegisterBody(body, 'new');
  assert.match(merged, /new/);
  assert.match(merged, /## Human\nkeep me/);
  assert.equal(splitMarkers(merged).current, 'new');
});

test('last-updated timestamp alone is not a semantic change', () => {
  const base = 'state\n_Last updated: 2026-09-15T00:00:00Z_';
  const body = `${MARKER_START}\n\n${base}\n\n${MARKER_END}`;
  assert.equal(obligationsChanged(body, 'state\n_Last updated: 2026-09-16T00:00:00Z_'), false);
  assert.equal(obligationsChanged(body, 'different\n_Last updated: 2026-09-16T00:00:00Z_'), true);
});

test('rendered weekly result is derived from findings', () => {
  const text = renderObligations({
    weekly: {
      nowISO: NOW,
      lastVerifiedAt: '2026-09-15T00:00:00Z',
      reviewedHead: SHA_A,
      result: 'FINDINGS',
      findingIssueNumbers: [341],
      state: 'CURRENT',
    },
    retrospective: {
      baselinePr: 260,
      baselineDate: '2026-09-05',
      rawMergedSinceBaseline: 30,
      escapeEvidenceCount: 0,
      p1LifecycleEscape: false,
      state: 'CHECK_TRIGGER',
    },
  });
  assert.match(text, /accounted result: FINDINGS/);
  assert.match(text, /finding issues: #341/);
  assert.match(text, /state: \*\*CHECK_TRIGGER\*\*/);
});
