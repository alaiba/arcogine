import test from 'node:test';
import assert from 'node:assert/strict';

import {
  buildExactWindow,
  changesRequiredCount,
  collectSearchPages,
  summarizeWindow,
} from './delivery-retrospective.mjs';

const review = (body, authorAssociation = 'OWNER') => ({ body, authorAssociation });
const pr = (number, mergedAt, bodies = [], extra = {}) => ({
  number,
  merged: true,
  mergedAt,
  baseRefName: 'main',
  reviews: {
    totalCount: bodies.length,
    nodes: bodies.map((body) => review(body)),
  },
  ...extra,
});

test('exact retrospective window uses merge timestamps, not PR numbers', () => {
  const records = [
    pr(500, '2026-09-04T01:00:00Z'),
    pr(256, '2026-09-04T23:00:19Z'),
    pr(259, '2026-09-06T09:29:55Z'),
    pr(368, '2026-09-20T22:28:50Z'),
    pr(100, '2026-09-21T00:00:00Z'),
  ];
  assert.deepEqual(
    buildExactWindow(
      records,
      '2026-09-04T02:09:23Z',
      '2026-09-20T22:28:50Z',
    ).map((item) => item.number),
    [256, 259, 368],
  );
});

test('window excludes non-main and non-merged candidates', () => {
  const records = [
    pr(1, '2026-09-05T00:00:00Z', [], { baseRefName: 'release' }),
    pr(2, '2026-09-06T00:00:00Z', [], { merged: false }),
    pr(3, '2026-09-07T00:00:00Z'),
  ];
  assert.deepEqual(
    buildExactWindow(records, '2026-09-04T00:00:00Z', '2026-09-08T00:00:00Z')
      .map((item) => item.number),
    [3],
  );
});

test('duplicate PRs fail instead of silently altering counts', () => {
  const records = [
    pr(7, '2026-09-05T00:00:00Z'),
    pr(7, '2026-09-05T00:00:00Z'),
  ];
  assert.throws(
    () => buildExactWindow(records, '2026-09-04T00:00:00Z', '2026-09-06T00:00:00Z'),
    /duplicate PR #7/,
  );
});

test('trusted CHANGES REQUIRED parser ignores prose examples', () => {
  const record = pr(10, '2026-09-05T00:00:00Z', [
    'Example: `Disposition: CHANGES REQUIRED` is not a verdict.',
    'Finding here.\n\nDisposition: **CHANGES REQUIRED**.',
    'Fixed.\n\nDisposition: **READY TO MERGE**.',
  ]);
  assert.equal(changesRequiredCount(record), 1);
});

test('untrusted review authors do not affect retrospective blocker counts', () => {
  const record = pr(10, '2026-09-05T00:00:00Z');
  record.reviews = {
    totalCount: 4,
    nodes: [
      review('Disposition: **CHANGES REQUIRED**.', 'NONE'),
      review('Disposition: **CHANGES REQUIRED**.', 'CONTRIBUTOR'),
      review('Disposition: **CHANGES REQUIRED**.', 'MEMBER'),
      review('Disposition: **CHANGES REQUIRED**.', 'COLLABORATOR'),
    ],
  };
  assert.equal(changesRequiredCount(record), 2);
});

test('truncated review payload fails closed', () => {
  const record = pr(10, '2026-09-05T00:00:00Z', [
    'Disposition: CHANGES REQUIRED',
  ]);
  record.reviews.totalCount = 101;
  assert.throws(() => changesRequiredCount(record), /only 1 were fetched/);
});

test('summary computes review-round totals deterministically', () => {
  const window = [
    pr(1, '2026-09-05T00:00:00Z', []),
    pr(2, '2026-09-06T00:00:00Z', ['Disposition: CHANGES REQUIRED']),
    pr(3, '2026-09-07T00:00:00Z', [
      'Disposition: CHANGES REQUIRED',
      'Disposition: CHANGES REQUIRED',
    ]),
    pr(4, '2026-09-08T00:00:00Z', [
      'Disposition: CHANGES REQUIRED',
      'Disposition: CHANGES REQUIRED',
      'Disposition: CHANGES REQUIRED',
      'Disposition: READY TO MERGE',
    ]),
  ];
  assert.deepEqual(summarizeWindow(window), {
    mergedPrCount: 4,
    trustedChangesRequiredSubmissions: 6,
    reviewRoundDistribution: { zero: 1, one: 1, two: 1, threePlus: 1 },
  });
});

test('search pagination is complete and stable before counts are accepted', async () => {
  const pages = new Map([
    [null, {
      issueCount: 3,
      nodes: [{ number: 1 }, { number: 2 }],
      pageInfo: { hasNextPage: true, endCursor: 'next' },
    }],
    ['next', {
      issueCount: 3,
      nodes: [{ number: 3 }],
      pageInfo: { hasNextPage: false, endCursor: null },
    }],
  ]);
  const nodes = await collectSearchPages(async (cursor) => pages.get(cursor));
  assert.deepEqual(nodes.map((item) => item.number), [1, 2, 3]);
});

test('search pagination fails closed on incomplete retrieval', async () => {
  await assert.rejects(
    () => collectSearchPages(async () => ({
      issueCount: 3,
      nodes: [{ number: 1 }, { number: 2 }],
      pageInfo: { hasNextPage: false, endCursor: null },
    })),
    /reported 3 results but fetched 2/,
  );
});

test('search pagination refuses GitHub search windows above the completeness cap', async () => {
  await assert.rejects(
    () => collectSearchPages(async () => ({
      issueCount: 1001,
      nodes: [],
      pageInfo: { hasNextPage: false, endCursor: null },
    })),
    /cannot prove completeness above 1000/,
  );
});
