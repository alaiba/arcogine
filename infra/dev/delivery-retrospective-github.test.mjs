import test from 'node:test';
import assert from 'node:assert/strict';

import {
  acquireEvidence,
  collectSearchPages,
  serializeReviewConnection,
} from './delivery-retrospective-github.mjs';

const baseline = { number: 10, merged: true, mergedAt: '2026-09-04T02:09:23Z', baseRefName: 'main' };
const through = { number: 12, merged: true, mergedAt: '2026-09-06T09:29:55Z', baseRefName: 'main' };
const revision = (number) => ['REV', String(number).padStart(3, '0')].join('-');

test('search pagination is complete and stable across pages', async () => {
  const pages = new Map([
    [null, {
      issueCount: 3,
      nodes: [{ number: 10 }, { number: 11 }],
      pageInfo: { hasNextPage: true, endCursor: 'next' },
    }],
    ['next', {
      issueCount: 3,
      nodes: [{ number: 12 }],
      pageInfo: { hasNextPage: false, endCursor: null },
    }],
  ]);
  const records = await collectSearchPages(async (cursor) => pages.get(cursor));
  assert.deepEqual(records.map((record) => record.number), [10, 11, 12]);
});

test('search pagination refuses a changing result count', async () => {
  let page = 0;
  await assert.rejects(() => collectSearchPages(async () => {
    page += 1;
    return {
      issueCount: page === 1 ? 2 : 3,
      nodes: [{ number: page }],
      pageInfo: { hasNextPage: page === 1, endCursor: page === 1 ? 'next' : null },
    };
  }), /result count changed during pagination/);
});

test('search pagination fails closed above GitHub search 1000-result cap', async () => {
  await assert.rejects(() => collectSearchPages(async () => ({
    issueCount: 1001,
    nodes: [],
    pageInfo: { hasNextPage: false, endCursor: null },
  })), /cannot prove completeness above 1000/);
});

test('search pagination fails closed on missing cursors, repeated cursors, and missing records', async () => {
  await assert.rejects(() => collectSearchPages(async () => ({
    issueCount: 1,
    nodes: [{ number: 1 }],
    pageInfo: { hasNextPage: true, endCursor: null },
  })), /returned no cursor/);

  let page = 0;
  await assert.rejects(() => collectSearchPages(async () => {
    page += 1;
    return {
      issueCount: 2,
      nodes: [{ number: page }],
      pageInfo: { hasNextPage: true, endCursor: 'same' },
    };
  }), /repeated a pagination cursor/);

  await assert.rejects(() => collectSearchPages(async () => ({
    issueCount: 2,
    nodes: [{ number: 1 }],
    pageInfo: { hasNextPage: false, endCursor: null },
  })), /reported 2 results but fetched 1/);
});

test('review connection above its supported page size fails closed rather than truncating', () => {
  assert.throws(() => serializeReviewConnection({
    number: 11,
    reviews: { totalCount: 101, nodes: Array.from({ length: 100 }, () => ({})), pageInfo: { hasNextPage: true } },
  }), /more than 100 submitted reviews/);
});

test('review connection with inconsistent count fails closed', () => {
  assert.throws(() => serializeReviewConnection({
    number: 11,
    reviews: { totalCount: 2, nodes: [{}], pageInfo: { hasNextPage: false } },
  }), /reported 2 reviews but fetched 1/);
});

test('review serializer preserves a missing author association as unproven', () => {
  const result = serializeReviewConnection({
    number: 11,
    reviews: {
      totalCount: 1,
      pageInfo: { hasNextPage: false, endCursor: null },
      nodes: [{
        id: 'PRR_missingAssociation',
        body: null,
        submittedAt: '2026-09-06T12:00:00Z',
        commit: null,
      }],
    },
  });
  assert.equal(result.items[0].authorAssociation, null);
});

test('review serializer retains identities and metadata without disposition or finding interpretation', () => {
  const body = `${revision(1)} - A structured finding\n\nSeverity: P2\nDisposition: **CHANGES REQUIRED**.`;
  const result = serializeReviewConnection({
    number: 11,
    reviews: {
      totalCount: 1,
      pageInfo: { hasNextPage: false, endCursor: null },
      nodes: [{
        id: 'PRR_kwDOExample',
        body,
        authorAssociation: 'MEMBER',
        submittedAt: '2026-09-06T12:00:00Z',
        commit: { oid: 'abcdef0123456789' },
      }],
    },
  });
  assert.deepEqual(result, {
    complete: true,
    reportedCount: 1,
    items: [{
      id: 'PRR_kwDOExample',
      body,
      authorAssociation: 'MEMBER',
      submittedAt: '2026-09-06T12:00:00Z',
      reviewedHead: 'abcdef0123456789',
    }],
  });
});

test('acquisition emits a validated source-neutral evidence bundle with complete review data', async () => {
  const reviewBody = [
    `${revision(1)} - A stale process statement`,
    '',
    'Severity: P2',
    'Category: DOCUMENTATION_ACCURACY',
    'Confidence: HIGH',
    'Head: abcdef0123456789',
    'Subject: process guide',
    'Status: OPEN',
    '',
    'Disposition: **CHANGES REQUIRED**.',
  ].join('\n');
  const searchItems = [
    baseline,
    { number: 11, merged: true, mergedAt: '2026-09-05T00:00:00Z', baseRefName: 'main' },
    through,
    { number: 13, merged: true, mergedAt: '2026-09-06T10:00:00Z', baseRefName: 'main' },
  ];
  const calls = [];
  const graphql = async (_token, query, variables) => {
    calls.push({ query, variables });
    if (query.includes('baseline: pullRequest')) return { repository: { baseline, through } };
    if (query.includes('search(type:ISSUE')) {
      return { search: { issueCount: searchItems.length, nodes: searchItems, pageInfo: { hasNextPage: false, endCursor: null } } };
    }
    if (query.includes('reviews(first:100)')) {
      const sourcePr = searchItems.find((item) => item.number === variables.number);
      const outOfWindow = ![11, 12].includes(sourcePr.number);
      return {
        repository: {
          pullRequest: {
            ...sourcePr,
            reviews: {
              totalCount: outOfWindow ? 101 : sourcePr.number === 11 ? 1 : 0,
              pageInfo: { hasNextPage: outOfWindow, endCursor: outOfWindow ? 'more' : null },
              nodes: outOfWindow ? Array.from({ length: 100 }, () => ({})) : sourcePr.number === 11 ? [{
                id: 'PRR_11',
                body: reviewBody,
                authorAssociation: 'OWNER',
                submittedAt: '2026-09-05T12:00:00Z',
                commit: { oid: 'abcdef0123456789' },
              }] : [],
            },
          },
        },
      };
    }
    throw new Error(`unexpected query: ${query}`);
  };

  const result = await acquireEvidence({
    repo: 'alaiba/arcogine',
    baselinePr: 10,
    throughPr: 12,
    token: 'fixture-token',
    graphql,
    now: () => '2026-09-24T15:00:00Z',
  });
  assert.equal(result.schemaVersion, 1);
  assert.equal(result.source.adapter, 'github-graphql');
  assert.equal(result.endpoints.baseline.mergedAt, baseline.mergedAt);
  assert.equal(result.endpoints.through.mergedAt, through.mergedAt);
  assert.equal(result.candidates.complete, true);
  assert.equal(result.candidates.reportedCount, 2);
  assert.equal(result.source.candidateSearch.reportedCount, 4);
  assert.equal(result.candidates.items[0].reviews.items[0].body, reviewBody);
  assert.equal(result.candidates.items[0].reviews.items[0].id, 'PRR_11');
  assert.equal(result.candidates.items[0].reviews.items[0].reviewedHead, 'abcdef0123456789');
  assert.equal(result.candidates.items[1].reviews.reportedCount, 0);
  assert.equal('findingAnalytics' in result, false);
  assert.equal('disposition' in result, false);
  assert.equal(calls.length, 4);
  assert.deepEqual(
    calls.filter((call) => call.query.includes('reviews(first:100)')).map((call) => call.variables.number),
    [11, 12],
  );
  assert.match(calls[1].variables.query, /merged:2026-09-04\.\.2026-09-06/);
});
