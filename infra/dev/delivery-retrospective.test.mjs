import test from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { spawnSync } from 'node:child_process';

import {
  analyzeEvidence,
  buildExactWindow,
  changesRequiredCount,
  closingDisposition,
  render,
  summarizeWindow,
} from './delivery-retrospective.mjs';
import { validateEvidence } from './delivery-retrospective-evidence.mjs';

const headA = 'a'.repeat(40);
const headB = 'b'.repeat(40);
const handoffCommit = 'c'.repeat(40);
const handoffPath = ['workspace', 'research', 'example-question', 'implementation-handoff.md'].join('/');
const revision = (number) => ['REV', String(number).padStart(3, '0')].join('-');
const bareRevision = (number) => ['REV', String(number)].join('-');

function review(body, options = {}) {
  return {
    id: options.id ?? 'R_1',
    body,
    authorAssociation: options.authorAssociation ?? 'OWNER',
    submittedAt: options.submittedAt ?? '2026-09-06T12:00:00Z',
    reviewedHead: options.reviewedHead ?? headA,
  };
}

function candidate(number, mergedAt, reviews = [], options = {}) {
  return {
    number,
    merged: true,
    mergedAt,
    baseRefName: options.baseRefName ?? 'main',
    reviews: {
      complete: options.reviewsComplete ?? true,
      reportedCount: options.reportedCount ?? reviews.length,
      items: reviews,
    },
    handoff: options.handoff ?? null,
    closingIssues: {
      complete: true,
      reportedCount: (options.closingIssues ?? []).length,
      items: options.closingIssues ?? [],
    },
  };
}

function handoff(committedAt) {
  return { path: handoffPath, commit: handoffCommit, committedAt };
}

function issue(number, createdAt, closedAt = null) {
  return { number, createdAt, closedAt };
}

function issueCollection(items) {
  return { complete: true, reportedCount: items.length, items };
}

function evidence(items, overrides = {}) {
  return {
    schemaVersion: 2,
    repository: 'alaiba/arcogine',
    requested: { baselinePr: 1, throughPr: 4 },
    endpoints: {
      baseline: { number: 1, merged: true, mergedAt: '2026-09-04T02:09:23Z', baseRefName: 'main' },
      through: { number: 4, merged: true, mergedAt: '2026-09-08T00:00:00Z', baseRefName: 'main' },
    },
    candidates: { complete: true, reportedCount: items.length, items },
    issues: issueCollection([]),
    source: { adapter: 'github-graphql', acquiredAt: '2026-09-24T15:00:00Z' },
    ...overrides,
  };
}

function finding(id, fields = {}) {
  const severity = fields.severity === null ? [] : [`Severity: ${fields.severity ?? 'P2'}`];
  return [
    `${id} - ${fields.title ?? 'The current report describes a stale contract'}`,
    '',
    ...severity,
    `Category: ${fields.category ?? 'DOCUMENTATION_ACCURACY'}`,
    `Confidence: ${fields.confidence ?? 'HIGH'}`,
    `Head: ${fields.head ?? headA}`,
    `Subject: ${fields.subject ?? 'reporting contract'}`,
    '',
    'Problem:',
    'The maintained description does not match the executable contract.',
    '',
    'Evidence:',
    'A current test exercises the conflicting behavior.',
    '',
    'Why it matters:',
    'Readers can rely on an incorrect current-state statement.',
    '',
    'Required invariant/outcome:',
    'The maintained statement agrees with the executable contract.',
    `Status: ${fields.status ?? 'OPEN'}`,
  ].join('\n');
}

function blockingReview(body, options = {}) {
  return review(`${body}\n\nDisposition: **CHANGES REQUIRED**.`, options);
}

function baseEvidence(reviews = [], overrides = {}) {
  return evidence([
    candidate(1, '2026-09-04T02:09:23Z'),
    candidate(2, '2026-09-06T00:00:00Z', reviews),
    candidate(4, '2026-09-08T00:00:00Z'),
  ], overrides);
}

test('evidence v1 accepts complete collections with matching counts', () => {
  const value = baseEvidence([review('Disposition: **READY TO MERGE**.')]);
  assert.equal(validateEvidence(value), value);
});

test('evidence v1 rejects missing review author provenance', () => {
  const value = baseEvidence([review('Disposition: **READY TO MERGE**.')]);
  value.candidates.items[1].reviews.items[0].authorAssociation = null;
  assert.throws(() => validateEvidence(value), /author association is missing/);
});

test('evidence v1 rejects unsupported review author associations', () => {
  const value = baseEvidence([review('Disposition: **READY TO MERGE**.')]);
  value.candidates.items[1].reviews.items[0].authorAssociation = 'UNKNOWN';
  assert.throws(() => validateEvidence(value), /author association UNKNOWN is unsupported/);
});

test('evidence validation rejects an unsupported schema version', () => {
  assert.throws(() => validateEvidence(baseEvidence([], { schemaVersion: 1 })), /unsupported schema version 1/);
});

test('every candidate states its named handoff explicitly, null when it names none', () => {
  const value = baseEvidence();
  delete value.candidates.items[1].handoff;
  assert.throws(() => validateEvidence(value), /PR #2 handoff reference is missing/);
});

test('a named handoff carries a full commit and a commit date no later than its merge', () => {
  const shortCommit = baseEvidence();
  shortCommit.candidates.items[1].handoff = { ...handoff('2026-09-05T00:00:00Z'), commit: 'abc1234' };
  assert.throws(() => validateEvidence(shortCommit), /full lowercase commit SHA/);

  const afterMerge = baseEvidence();
  afterMerge.candidates.items[1].handoff = handoff('2026-09-06T00:00:01Z');
  assert.throws(() => validateEvidence(afterMerge), /handoff committed after the PR merged/);
});

test('closing-issue references and issue facts fail closed when incomplete or unresolved', () => {
  const incompleteReferences = baseEvidence();
  incompleteReferences.candidates.items[1].closingIssues.complete = false;
  assert.throws(() => validateEvidence(incompleteReferences), /closing-issue references are incomplete/);

  const incompleteIssues = baseEvidence([], { issues: { ...issueCollection([]), complete: false } });
  assert.throws(() => validateEvidence(incompleteIssues), /issue collection is incomplete/);

  const miscounted = baseEvidence([], { issues: { ...issueCollection([issue(9, '2026-09-01T00:00:00Z')]), reportedCount: 2 } });
  assert.throws(() => validateEvidence(miscounted), /issue count mismatch/);

  const duplicated = baseEvidence([], {
    issues: issueCollection([issue(9, '2026-09-01T00:00:00Z'), issue(9, '2026-09-01T00:00:00Z')]),
  });
  assert.throws(() => validateEvidence(duplicated), /duplicate issue #9/);

  const unresolved = baseEvidence();
  unresolved.candidates.items[1].closingIssues = { complete: true, reportedCount: 1, items: [99] };
  assert.throws(() => validateEvidence(unresolved), /closing issue #99 of candidate PR #2 has no issue facts/);

  const openedLater = baseEvidence([], { issues: issueCollection([issue(9, '2026-09-06T00:00:01Z')]) });
  openedLater.candidates.items[1].closingIssues = { complete: true, reportedCount: 1, items: [9] };
  assert.throws(() => validateEvidence(openedLater), /closing issue #9 was opened after candidate PR #2 merged/);
});

test('handoff lead time runs from the handoff commit date to the merge of window PRs only', () => {
  const value = evidence([
    candidate(1, '2026-09-04T02:09:23Z', [], { handoff: handoff('2026-09-01T00:00:00Z') }),
    candidate(2, '2026-09-06T00:00:00Z', [], { handoff: handoff('2026-09-04T12:00:00Z') }),
    candidate(3, '2026-09-07T00:00:00Z'),
    candidate(4, '2026-09-08T00:00:00Z', [], { handoff: handoff('2026-09-05T00:00:00Z') }),
  ]);
  assert.deepEqual(analyzeEvidence(value).researchToDeliveryLeadTime.handoffToMerge, {
    count: 2,
    medianDays: 2.25,
    items: [
      {
        prNumber: 2,
        handoffPath,
        handoffCommit,
        handoffCommittedAt: '2026-09-04T12:00:00Z',
        mergedAt: '2026-09-06T00:00:00Z',
        elapsedDays: 1.5,
      },
      {
        prNumber: 4,
        handoffPath,
        handoffCommit,
        handoffCommittedAt: '2026-09-05T00:00:00Z',
        mergedAt: '2026-09-08T00:00:00Z',
        elapsedDays: 3,
      },
    ],
  });
});

test('issue lead time runs from opening to the first window merge that closes the issue', () => {
  const value = evidence([
    candidate(1, '2026-09-04T02:09:23Z', [], { closingIssues: [10] }),
    candidate(2, '2026-09-06T00:00:00Z', [], { closingIssues: [12, 11] }),
    candidate(3, '2026-09-07T00:00:00Z', [], { closingIssues: [12] }),
    candidate(4, '2026-09-08T00:00:00Z'),
  ], {
    issues: issueCollection([
      issue(10, '2026-09-01T00:00:00Z', '2026-09-04T02:09:25Z'),
      issue(11, '2026-09-05T00:00:00Z', '2026-09-06T00:00:03Z'),
      issue(12, '2026-09-02T00:00:00Z', '2026-09-06T00:00:03Z'),
    ]),
  });
  const leadTime = analyzeEvidence(value).researchToDeliveryLeadTime;
  assert.deepEqual(leadTime.issueOpeningToMerge, {
    count: 2,
    medianDays: 2.5,
    items: [
      { issueNumber: 11, openedAt: '2026-09-05T00:00:00Z', closingPrNumber: 2, mergedAt: '2026-09-06T00:00:00Z', elapsedDays: 1 },
      { issueNumber: 12, openedAt: '2026-09-02T00:00:00Z', closingPrNumber: 2, mergedAt: '2026-09-06T00:00:00Z', elapsedDays: 4 },
    ],
  });
  assert.equal(leadTime.openIssueAge.count, 0);
});

test('open issue age is measured at the window end for issues then open and not closed by a window PR', () => {
  const value = evidence([
    candidate(1, '2026-09-04T02:09:23Z'),
    candidate(2, '2026-09-06T00:00:00Z', [], { closingIssues: [24] }),
    candidate(4, '2026-09-08T00:00:00Z'),
  ], {
    issues: issueCollection([
      issue(20, '2026-09-01T00:00:00Z'),
      issue(21, '2026-09-03T00:00:00Z', '2026-09-10T00:00:00Z'),
      issue(22, '2026-09-02T00:00:00Z', '2026-09-07T00:00:00Z'),
      issue(23, '2026-09-09T00:00:00Z'),
      issue(24, '2026-09-05T00:00:00Z'),
    ]),
  });
  assert.deepEqual(analyzeEvidence(value).researchToDeliveryLeadTime.openIssueAge, {
    windowEnd: '2026-09-08T00:00:00Z',
    count: 2,
    medianDays: 6,
    items: [
      { issueNumber: 20, openedAt: '2026-09-01T00:00:00Z', ageDays: 7 },
      { issueNumber: 21, openedAt: '2026-09-03T00:00:00Z', ageDays: 5 },
    ],
  });
});

test('text rendering reports each lead-time measure with its count and any median', () => {
  const value = baseEvidence();
  value.candidates.items[1].handoff = handoff('2026-09-04T00:00:00Z');
  const text = render(analyzeEvidence(value));
  assert.match(text, /^Handoff commit to merge: 1 \(median 2 days\)$/m);
  assert.match(text, /^Issue opening to closing merge: 0$/m);
  assert.match(text, /^Issues open at window end: 0$/m);
});

test('a window without handoffs or issues reports empty lead-time measures without a median', () => {
  const leadTime = analyzeEvidence(baseEvidence()).researchToDeliveryLeadTime;
  assert.deepEqual(leadTime.handoffToMerge, { count: 0, medianDays: null, items: [] });
  assert.deepEqual(leadTime.issueOpeningToMerge, { count: 0, medianDays: null, items: [] });
  assert.deepEqual(leadTime.openIssueAge, { windowEnd: '2026-09-08T00:00:00Z', count: 0, medianDays: null, items: [] });
});

test('complete candidate collection with a count mismatch is rejected', () => {
  const value = baseEvidence();
  value.candidates.reportedCount += 1;
  assert.throws(() => analyzeEvidence(value), /candidate PR count mismatch/);
});

test('complete review collection with a count mismatch is rejected', () => {
  const value = baseEvidence([review('Disposition: **READY TO MERGE**.')]);
  value.candidates.items[1].reviews.reportedCount = 3;
  assert.throws(() => analyzeEvidence(value), /review count mismatch/);
});

test('malformed or missing endpoint facts are rejected', () => {
  const value = baseEvidence();
  delete value.endpoints.through.mergedAt;
  assert.throws(() => analyzeEvidence(value), /through endpoint must be a merged PR/);
});

test('an endpoint that is not a merged main PR is rejected', () => {
  const value = baseEvidence();
  value.endpoints.baseline.baseRefName = 'release';
  assert.throws(() => analyzeEvidence(value), /does not target main/);
});

test('duplicate candidate PR identities are rejected', () => {
  const value = baseEvidence();
  value.candidates.items.splice(2, 0, structuredClone(value.candidates.items[1]));
  value.candidates.reportedCount += 1;
  assert.throws(() => analyzeEvidence(value), /duplicate candidate PR #2/);
});

test('incomplete nested review collections cannot produce a retrospective summary', () => {
  const value = baseEvidence([review('Disposition: **READY TO MERGE**.')]);
  value.candidates.items[1].reviews.complete = false;
  assert.throws(() => analyzeEvidence(value), /review collection is incomplete/);
});

test('through candidate facts must agree with the endpoint facts', () => {
  const value = baseEvidence();
  value.candidates.items[2].mergedAt = '2026-09-07T23:59:00Z';
  assert.throws(() => analyzeEvidence(value), /candidate facts disagree/);
});

test('exact window uses merge timestamps, not PR numbers, and sorts deterministically', () => {
  const records = [
    { number: 500, merged: true, mergedAt: '2026-09-04T01:00:00Z', baseRefName: 'main' },
    { number: 256, merged: true, mergedAt: '2026-09-04T23:00:19Z', baseRefName: 'main' },
    { number: 259, merged: true, mergedAt: '2026-09-06T09:29:55Z', baseRefName: 'main' },
    { number: 368, merged: true, mergedAt: '2026-09-20T22:28:50Z', baseRefName: 'main' },
    { number: 100, merged: true, mergedAt: '2026-09-21T00:00:00Z', baseRefName: 'main' },
  ];
  assert.deepEqual(
    buildExactWindow(records, '2026-09-04T02:09:23Z', '2026-09-20T22:28:50Z').map((item) => item.number),
    [256, 259, 368],
  );
});

test('exact window excludes non-main and non-merged records', () => {
  const records = [
    { number: 1, merged: true, mergedAt: '2026-09-05T00:00:00Z', baseRefName: 'release' },
    { number: 2, merged: false, mergedAt: '2026-09-06T00:00:00Z', baseRefName: 'main' },
    { number: 3, merged: true, mergedAt: '2026-09-07T00:00:00Z', baseRefName: 'main' },
  ];
  assert.deepEqual(buildExactWindow(records, '2026-09-04T00:00:00Z', '2026-09-08T00:00:00Z').map((item) => item.number), [3]);
});

test('duplicate PRs in the retrospective window fail closed', () => {
  const records = [
    { number: 7, merged: true, mergedAt: '2026-09-05T00:00:00Z', baseRefName: 'main' },
    { number: 7, merged: true, mergedAt: '2026-09-05T00:00:00Z', baseRefName: 'main' },
  ];
  assert.throws(() => buildExactWindow(records, '2026-09-04T00:00:00Z', '2026-09-06T00:00:00Z'), /duplicate PR #7/);
});

test('analyzer enforces exact time bounds and main target while retaining exact PR order', () => {
  const value = evidence([
    candidate(1, '2026-09-04T02:09:23Z'),
    candidate(3, '2026-09-06T01:00:00Z', [], { baseRefName: 'release' }),
    candidate(2, '2026-09-06T00:00:00Z'),
    candidate(4, '2026-09-08T00:00:00Z'),
    candidate(5, '2026-09-09T00:00:00Z'),
  ]);
  const summary = analyzeEvidence(value);
  assert.deepEqual(summary.pullRequests.map((item) => item.number), [2, 4]);
  assert.equal(summary.mergedPrCount, 2);
});

test('closing disposition parser ignores prose, examples, blockquotes, and non-closing verdicts', () => {
  const counted = [
    'Disposition: CHANGES REQUIRED',
    '**Disposition: CHANGES REQUIRED**',
    '- Disposition: CHANGES REQUIRED',
    'Earlier: Disposition: READY TO MERGE\n\nDisposition: CHANGES REQUIRED',
    'Finding.\n\nDisposition: **CHANGES REQUIRED**.\n\n   \n',
  ];
  const ignored = [
    'Disposition: CHANGES REQUIRED\n\nBlocking finding follows after the verdict.',
    '> Disposition: **CHANGES REQUIRED**',
    'Disposition: CHANGES REQUIRED, but only cosmetically -- see below',
    'For example, Disposition: CHANGES REQUIRED would block the PR.',
    'Disposition: CHANGES REQUIRED\n\nDisposition: READY TO MERGE',
    '',
    null,
  ];
  assert.deepEqual(counted.map((body) => closingDisposition(body)), Array(counted.length).fill('CHANGES REQUIRED'));
  assert.deepEqual(ignored.map((body) => closingDisposition(body)), [
    null, null, null, null, 'READY TO MERGE', null, null,
  ]);
});

test('only trusted author associations contribute to historical blocker counts', () => {
  const record = candidate(10, '2026-09-05T00:00:00Z', [
    blockingReview('', { id: 'r1', authorAssociation: 'NONE' }),
    blockingReview('', { id: 'r2', authorAssociation: 'CONTRIBUTOR' }),
    blockingReview('', { id: 'r3', authorAssociation: 'MEMBER' }),
    blockingReview('', { id: 'r4', authorAssociation: 'COLLABORATOR' }),
  ]);
  assert.equal(changesRequiredCount(record), 2);
});

test('review checkpoint summary remains 0 / 1 / 2 / 3+ per PR', () => {
  const window = [
    candidate(1, '2026-09-05T00:00:00Z', []),
    candidate(2, '2026-09-06T00:00:00Z', [blockingReview('', { id: 'r1' })]),
    candidate(3, '2026-09-07T00:00:00Z', [blockingReview('', { id: 'r2' }), blockingReview('', { id: 'r3' })]),
    candidate(4, '2026-09-08T00:00:00Z', [
      blockingReview('', { id: 'r4' }),
      blockingReview('', { id: 'r5' }),
      blockingReview('', { id: 'r6' }),
      review('Disposition: **READY TO MERGE**.', { id: 'r7' }),
    ]),
  ];
  assert.deepEqual(summarizeWindow(window), {
    mergedPrCount: 4,
    trustedChangesRequiredSubmissions: 6,
    reviewRoundDistribution: { zero: 1, one: 1, two: 1, threePlus: 1 },
  });
});

test('canonical severity-less finding fields are captured', () => {
  const value = baseEvidence([blockingReview(finding(revision(1), { severity: null }))]);
  const summary = analyzeEvidence(value);
  assert.equal(summary.findingAnalytics.complete, true);
  assert.deepEqual(summary.findingAnalytics.distinctFindings[0], {
    prNumber: 2,
    revisionId: revision(1),
    title: 'The current report describes a stale contract',
    severity: null,
    category: 'DOCUMENTATION_ACCURACY',
    confidence: 'HIGH',
    subject: 'reporting contract',
    firstReviewedHead: headA,
    firstReview: { id: 'R_1', submittedAt: '2026-09-06T12:00:00Z' },
    currentReviewedHead: headA,
    status: 'OPEN',
    lastReview: { id: 'R_1', submittedAt: '2026-09-06T12:00:00Z' },
  });
});

test('multiple findings in one review are parsed as separate identities', () => {
  const body = `${finding(revision(1))}\n\n${finding(revision(2), { category: 'TEST_EVIDENCE' })}\n\nDisposition: **CHANGES REQUIRED**.`;
  const summary = analyzeEvidence(baseEvidence([review(body)]));
  assert.equal(summary.findingAnalytics.diagnostics.structuredFindingsParsed, 2);
  assert.deepEqual(summary.findingAnalytics.distinctFindings.map((item) => item.revisionId), [revision(1), revision(2)]);
  assert.equal(summary.trustedChangesRequiredSubmissions, 1);
});

test('variable-width REV identifiers parse alongside historical zero-padded ones and sort numerically', () => {
  const body = [
    finding(bareRevision(1)),
    finding(revision(2), { category: 'SCOPE' }),
    finding(bareRevision(10), { category: 'TEST_EVIDENCE' }),
  ].join('\n\n') + '\n\nDisposition: **CHANGES REQUIRED**.';
  const summary = analyzeEvidence(baseEvidence([review(body)]));
  assert.equal(summary.findingAnalytics.diagnostics.structuredFindingsParsed, 3);
  assert.deepEqual(summary.findingAnalytics.diagnostics.malformedFindingBlocks, []);
  assert.deepEqual(
    summary.findingAnalytics.distinctFindings.map((item) => item.revisionId),
    [bareRevision(1), revision(2), bareRevision(10)],
  );
});

test('same PR + REV carried through re-review counts once and records first and current lifecycle facts', () => {
  const first = blockingReview(finding(revision(1)), { id: 'R_1', submittedAt: '2026-09-06T12:00:00Z', reviewedHead: headA });
  const second = review(finding(revision(1), { head: headB, status: 'RESOLVED' }), {
    id: 'R_2',
    submittedAt: '2026-09-07T12:00:00Z',
    reviewedHead: headB,
  });
  const summary = analyzeEvidence(baseEvidence([first, second]));
  assert.equal(summary.findingAnalytics.totalDistinctFindings, 1);
  assert.equal(summary.findingAnalytics.distinctFindings[0].firstReview.id, 'R_1');
  assert.equal(summary.findingAnalytics.distinctFindings[0].lastReview.id, 'R_2');
  assert.equal(summary.findingAnalytics.distinctFindings[0].status, 'RESOLVED');
  assert.equal(summary.findingAnalytics.distinctFindings[0].currentReviewedHead, headB);
});

test('historical severity does not split identity when a current re-review omits severity', () => {
  const first = blockingReview(finding(revision(1), { severity: 'P1' }), {
    id: 'R_1', submittedAt: '2026-09-06T12:00:00Z', reviewedHead: headA,
  });
  const second = review(finding(revision(1), { severity: null, head: headB, status: 'RESOLVED' }), {
    id: 'R_2', submittedAt: '2026-09-07T12:00:00Z', reviewedHead: headB,
  });
  const result = analyzeEvidence(baseEvidence([first, second])).findingAnalytics;
  assert.equal(result.complete, true);
  assert.equal(result.totalDistinctFindings, 1);
  assert.equal(result.distinctFindings[0].severity, 'P1');
  assert.equal(result.distinctFindings[0].status, 'RESOLVED');
  assert.deepEqual(result.diagnostics.conflictingFindingIdentities, []);
});

test('obsolete lifecycle status remains explicit in the distinct finding dataset', () => {
  const first = review(finding(revision(1)), { id: 'R_1', submittedAt: '2026-09-06T12:00:00Z' });
  const second = review(finding(revision(1), { head: headB, status: 'OBSOLETE' }), {
    id: 'R_2', submittedAt: '2026-09-07T12:00:00Z', reviewedHead: headB,
  });
  const result = analyzeEvidence(baseEvidence([first, second])).findingAnalytics;
  assert.equal(result.distinctFindings[0].status, 'OBSOLETE');
});

test('a re-review that omits prior finding identity is visibly incomplete', () => {
  const first = blockingReview(finding(revision(1)), { id: 'R_1', submittedAt: '2026-09-06T12:00:00Z' });
  const second = review('Disposition: **READY TO MERGE**.', { id: 'R_2', submittedAt: '2026-09-07T12:00:00Z' });
  const result = analyzeEvidence(baseEvidence([first, second])).findingAnalytics;
  assert.deepEqual(result.diagnostics.findingsOmittedFromRereview, [{
    prNumber: 2,
    submittedAt: '2026-09-07T12:00:00Z',
    reviewIds: ['R_2'],
    revisionIds: [revision(1)],
  }]);
  assert.equal(result.complete, false);
});

test('genuinely new REV identifier creates another distinct finding', () => {
  const value = baseEvidence([
    review(`${finding(revision(1))}\n\n${finding(revision(2), { category: 'TEST_EVIDENCE' })}`),
  ]);
  assert.equal(analyzeEvidence(value).findingAnalytics.totalDistinctFindings, 2);
});

test('finding distributions count both findings and per-PR incidence deterministically', () => {
  const repeatedCategory = `${finding(revision(1))}\n\n${finding(revision(2), { subject: 'another reporting issue' })}`;
  const value = evidence([
    candidate(2, '2026-09-06T00:00:00Z', [review(repeatedCategory, { id: 'R_2' })]),
    candidate(3, '2026-09-07T00:00:00Z', [review(finding(revision(1)), { id: 'R_3' })]),
    candidate(4, '2026-09-08T00:00:00Z'),
  ]);
  const result = analyzeEvidence(value).findingAnalytics;
  assert.equal(result.totalDistinctFindings, 3);
  assert.equal(result.prsWithFindings, 2);
  assert.deepEqual(result.findingsByCategory, { DOCUMENTATION_ACCURACY: 3 });
  assert.deepEqual(result.findingsByConfidence, { HIGH: 3 });
  assert.deepEqual(result.prIncidenceByCategory, [{ category: 'DOCUMENTATION_ACCURACY', prCount: 2 }]);
});

test('unknown categories are preserved literally and mark finding analytics incomplete', () => {
  const result = analyzeEvidence(baseEvidence([review(finding(revision(1), { category: 'OLD_REVIEW_TAXONOMY' }))])).findingAnalytics;
  assert.deepEqual(result.findingsByCategory, { OLD_REVIEW_TAXONOMY: 1 });
  assert.deepEqual(result.diagnostics.unknownCategories, ['OLD_REVIEW_TAXONOMY']);
  assert.equal(result.complete, false);
  assert.equal(result.completeness.categoryDistribution, false);
});

test('malformed canonical-looking finding data and blocker gaps are visible, never silently dropped', () => {
  const malformed = `${revision(1)} - Incomplete\n\nSeverity: P2\nCategory: DOCUMENTATION_ACCURACY`;
  const result = analyzeEvidence(baseEvidence([blockingReview(malformed)])).findingAnalytics;
  assert.equal(result.totalDistinctFindings, 0);
  assert.equal(result.diagnostics.malformedFindingBlocks.length, 1);
  assert.equal(result.diagnostics.blockingReviewsWithoutParseableFinding.length, 1);
  assert.equal(result.complete, false);
  assert.equal(result.completeness.categoryDistribution, false);
});

test('quoted and fenced finding examples are ignored as noncanonical reviewer data', () => {
  const example = finding(revision(1));
  const body = `> ${example.replaceAll('\n', '\n> ')}\n\n\`\`\`text\n${example}\n\`\`\``;
  const result = analyzeEvidence(baseEvidence([blockingReview(body)])).findingAnalytics;
  assert.equal(result.diagnostics.structuredFindingsParsed, 0);
  assert.equal(result.diagnostics.malformedFindingBlocks.length, 0);
  assert.equal(result.diagnostics.blockingReviewsWithoutParseableFinding.length, 1);
  assert.equal(result.complete, false);
});

test('historical severity is preserved without enforcing current review policy', () => {
  const result = analyzeEvidence(baseEvidence([review(finding(revision(1), {
    severity: 'Nit', confidence: 'MAYBE', status: 'DEFERRED',
  }))])).findingAnalytics;
  assert.equal(result.distinctFindings[0].severity, 'Nit');
  assert.deepEqual(result.diagnostics.unsupportedValues, {
    confidence: ['MAYBE'], status: ['DEFERRED'],
  });
  assert.equal(result.complete, false);
});

test('reusing one PR + REV identity for a different semantic finding is detected', () => {
  const first = review(finding(revision(1)), { id: 'R_1', submittedAt: '2026-09-06T12:00:00Z' });
  const changed = review(finding(revision(1), { subject: 'unrelated runtime defect', head: headB }), {
    id: 'R_2', submittedAt: '2026-09-07T12:00:00Z', reviewedHead: headB,
  });
  const result = analyzeEvidence(baseEvidence([first, changed])).findingAnalytics;
  assert.equal(result.totalDistinctFindings, 1);
  assert.equal(result.diagnostics.conflictingFindingIdentities.length, 1);
  assert.equal(result.complete, false);
});

test('repeating one PR + REV identity twice in a single review is diagnosed', () => {
  const body = `${finding(revision(1))}\n\n${finding(revision(1))}`;
  const result = analyzeEvidence(baseEvidence([review(body)])).findingAnalytics;
  assert.equal(result.totalDistinctFindings, 1);
  assert.equal(result.diagnostics.duplicateFindingIdentities.length, 1);
  assert.equal(result.complete, false);
});

test('finding Head must agree with source review head when the latter is available', () => {
  const result = analyzeEvidence(baseEvidence([review(finding(revision(1)), { reviewedHead: headB })])).findingAnalytics;
  assert.equal(result.diagnostics.headMismatches.length, 1);
  assert.equal(result.complete, false);
});

test('identical evidence with different producer metadata has identical analysis', () => {
  const first = baseEvidence([blockingReview(finding(revision(1)))]);
  const second = structuredClone(first);
  second.source = { adapter: 'fixture-other-producer', acquiredAt: '2026-09-25T00:00:00Z', acquisitionRun: 'fixture' };
  assert.deepEqual(analyzeEvidence(first), analyzeEvidence(second));
});

test('analyzer CLI consumes a file without GitHub credentials or acquisition tools', () => {
  const directory = mkdtempSync(join(tmpdir(), 'arcogine-retrospective-'));
  const input = join(directory, 'evidence.json');
  try {
    writeFileSync(input, JSON.stringify(baseEvidence([blockingReview(finding(revision(1)))])), 'utf8');
    const env = { ...process.env };
    delete env.GH_TOKEN;
    delete env.GITHUB_TOKEN;
    const result = spawnSync(process.execPath, ['infra/dev/delivery-retrospective.mjs', '--input', input, '--json'], {
      cwd: process.cwd(),
      encoding: 'utf8',
      env,
      timeout: 10_000,
    });
    assert.equal(result.status, 0, result.stderr);
    const output = JSON.parse(result.stdout);
    assert.equal(output.findingAnalytics.totalDistinctFindings, 1);
    assert.equal(output.researchToDeliveryLeadTime.handoffToMerge.count, 0);
  } finally {
    rmSync(directory, { recursive: true, force: true });
  }
});
