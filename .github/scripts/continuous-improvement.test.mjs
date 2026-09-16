import test from 'node:test';
import assert from 'node:assert/strict';

import {
  MARKER_START,
  MARKER_END,
  REGISTER_ISSUE_NUMBER,
  deriveWeeklyState,
  parseWeeklyRecord,
  deriveRetrospectiveState,
  renderObligations,
  splitMarkers,
  mergeRegisterBody,
  obligationsChanged,
} from './continuous-improvement.mjs';

const NOW = '2026-09-16T08:00:00.000Z';
const SHA = 'a'.repeat(40);

function registerBody(weekly) {
  return [
    '# Continuous improvement register',
    '',
    MARKER_START,
    '',
    '## Recurring obligations',
    '',
    '### Weekly Consistency review',
    '',
    `- last verified: ${weekly.lastVerified}`,
    `- reviewed head: ${weekly.reviewedHead}`,
    `- accounted result: ${weekly.result}`,
    `- finding issues: ${weekly.findings}`,
    '- next due / interval: every 7 days',
    `- state: **${weekly.state ?? 'CURRENT'}**`,
    '',
    '### Delivery-process retrospective',
    '',
    '- last baseline: PR #260 (2026-09-05)',
    '- raw merged PRs since baseline: 0',
    '- escape evidence recorded: 0 post-merge escape(s), P1 lifecycle escape: false',
    '- state: **CURRENT**',
    '',
    '_Last updated: 2026-09-16T00:00:00.000Z_',
    '',
    MARKER_END,
    '',
    '## Active improvement interventions',
    'keep me',
  ].join('\n');
}

test('fixed register identity', () => {
  assert.equal(REGISTER_ISSUE_NUMBER, 295);
});

test('weekly state derives only from last verified date', () => {
  assert.equal(deriveWeeklyState(null, NOW), 'DUE');
  assert.equal(deriveWeeklyState('2026-09-12', NOW), 'CURRENT');
  assert.equal(deriveWeeklyState('2026-09-06', NOW), 'DUE');
  assert.equal(deriveWeeklyState('2026-08-20', NOW), 'OVERDUE');
});

test('unverified weekly register parses', () => {
  const parsed = parseWeeklyRecord(registerBody({
    lastVerified: 'never',
    reviewedHead: 'n/a',
    result: 'n/a',
    findings: 'n/a',
    state: 'DUE',
  }));
  assert.deepEqual(parsed, {
    lastVerified: null,
    reviewedHead: null,
    result: null,
    findingIssueNumbers: [],
  });
});

test('clean weekly register parses', () => {
  const parsed = parseWeeklyRecord(registerBody({
    lastVerified: '2026-09-16',
    reviewedHead: SHA,
    result: 'CLEAN',
    findings: 'none',
  }));
  assert.equal(parsed.lastVerified, '2026-09-16');
  assert.equal(parsed.reviewedHead, SHA);
  assert.equal(parsed.result, 'CLEAN');
  assert.deepEqual(parsed.findingIssueNumbers, []);
});

test('findings weekly register parses', () => {
  const parsed = parseWeeklyRecord(registerBody({
    lastVerified: '2026-09-16',
    reviewedHead: SHA,
    result: 'FINDINGS',
    findings: '#341, #342',
  }));
  assert.deepEqual(parsed.findingIssueNumbers, [341, 342]);
});

test('weekly record fails closed on contradictory or malformed state', () => {
  assert.throws(() => parseWeeklyRecord(registerBody({
    lastVerified: 'never',
    reviewedHead: SHA,
    result: 'n/a',
    findings: 'n/a',
  })), /contradictory/);

  assert.throws(() => parseWeeklyRecord(registerBody({
    lastVerified: '2026-09-16',
    reviewedHead: SHA,
    result: 'CLEAN',
    findings: '#341',
  })), /CLEAN/);

  assert.throws(() => parseWeeklyRecord(registerBody({
    lastVerified: '2026-09-16',
    reviewedHead: SHA,
    result: 'FINDINGS',
    findings: 'none',
  })), /FINDINGS/);

  assert.throws(() => parseWeeklyRecord(registerBody({
    lastVerified: '16-09-2026',
    reviewedHead: SHA,
    result: 'CLEAN',
    findings: 'none',
  })), /YYYY-MM-DD/);
});

test('retrospective guard remains mechanical', () => {
  assert.equal(deriveRetrospectiveState({ rawMergedSinceBaseline: 24 }), 'CURRENT');
  assert.equal(deriveRetrospectiveState({ rawMergedSinceBaseline: 25 }), 'CHECK_TRIGGER');
  assert.equal(deriveRetrospectiveState({ rawMergedSinceBaseline: 1, escapeEvidenceCount: 2 }), 'CHECK_TRIGGER');
  assert.equal(deriveRetrospectiveState({ rawMergedSinceBaseline: 1, p1LifecycleEscape: true }), 'CHECK_TRIGGER');
});

test('register marker update preserves human-owned content', () => {
  const body = registerBody({
    lastVerified: 'never',
    reviewedHead: 'n/a',
    result: 'n/a',
    findings: 'n/a',
    state: 'DUE',
  });
  const merged = mergeRegisterBody(body, 'new obligations');
  assert.match(merged, /new obligations/);
  assert.match(merged, /## Active improvement interventions\nkeep me/);
  assert.equal(splitMarkers(merged).current, 'new obligations');
});

test('last-updated timestamp alone is not a semantic change', () => {
  const body = `${MARKER_START}\n\nstate\n_Last updated: 2026-09-15T00:00:00Z_\n\n${MARKER_END}`;
  assert.equal(obligationsChanged(body, 'state\n_Last updated: 2026-09-16T00:00:00Z_'), false);
  assert.equal(obligationsChanged(body, 'different\n_Last updated: 2026-09-16T00:00:00Z_'), true);
});

test('rendering preserves reviewer-owned weekly accounting while deriving state', () => {
  const text = renderObligations({
    nowISO: NOW,
    weekly: {
      lastVerified: '2026-09-16',
      reviewedHead: SHA,
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
  assert.match(text, /last verified: 2026-09-16/);
  assert.match(text, /accounted result: FINDINGS/);
  assert.match(text, /finding issues: #341/);
  assert.match(text, /state: \*\*CHECK_TRIGGER\*\*/);
});
