import test from 'node:test';
import assert from 'node:assert/strict';

import {
  acquireEvidence,
  collectIssuePages,
  collectSearchPages,
  parseHandoffReference,
  resolveHandoff,
  serializeIssue,
  serializeReviewConnection,
} from './delivery-retrospective-github.mjs';

const baseline = { number: 10, merged: true, mergedAt: '2026-09-04T02:09:23Z', baseRefName: 'main' };
const through = { number: 12, merged: true, mergedAt: '2026-09-06T09:29:55Z', baseRefName: 'main' };
const revision = (number) => ['REV', String(number).padStart(3, '0')].join('-');
const handoffCommit = 'c'.repeat(40);
const handoffPath = ['workspace', 'research', 'example-question', 'implementation-handoff.md'].join('/');
const handoffLine = `Implementation handoff: ${handoffPath} at ${handoffCommit}, committed 2026-09-04T12:00:00+03:00`;

function issueNode(number, createdAt, closedAt = null, closer = null) {
  return { number, createdAt, closedAt, timelineItems: { nodes: closer ? [{ closer }] : [] } };
}

function closingPr(number, repository = 'alaiba/arcogine') {
  return { __typename: 'PullRequest', number, repository: { nameWithOwner: repository } };
}

function issueListing(nodes, overrides = {}) {
  return { totalCount: nodes.length, pageInfo: { hasNextPage: false, endCursor: null }, nodes, ...overrides };
}

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

test('a PR body names its implementation handoff on one standalone line', () => {
  assert.deepEqual(parseHandoffReference(`## Summary\n\nSlice 1 of the question.\n\n${handoffLine}\n`, 11), {
    path: handoffPath,
    commit: handoffCommit,
    committedAt: '2026-09-04T12:00:00+03:00',
  });
  const decorated = `Implementation handoff: \`${handoffPath}\` at \`${handoffCommit.toUpperCase()}\`, committed 2026-09-04T09:00:00Z.`;
  assert.equal(parseHandoffReference(decorated, 11).commit, handoffCommit);
  assert.equal(parseHandoffReference('Closes #5.', 11), null);
  assert.equal(parseHandoffReference(null, 11), null);
});

test('handoff lines in fenced or quoted examples are not handoff references', () => {
  const body = ['```text', 'Implementation handoff: <path> at <commit>, committed <date>', '```', '', '> Implementation handoff: quoted'].join('\n');
  assert.equal(parseHandoffReference(body, 11), null);
});

test('a malformed or repeated handoff line fails closed', () => {
  assert.throws(
    () => parseHandoffReference(`Implementation handoff: ${handoffPath} at abc1234, committed 2026-09-04`, 11),
    /PR #11 has a malformed "Implementation handoff:" line/,
  );
  assert.throws(() => parseHandoffReference(`${handoffLine}\n${handoffLine}`, 11), /PR #11 names 2 implementation handoffs/);
});

test('a named handoff is confirmed against the commit and file GitHub records', async () => {
  const named = parseHandoffReference(handoffLine, 11);
  const resolve = (repository) => resolveHandoff({
    graphql: async (_token, _query, variables) => {
      assert.deepEqual(variables, {
        owner: 'alaiba', name: 'arcogine', oid: handoffCommit, expression: `${handoffCommit}:${handoffPath}`,
      });
      return { repository };
    },
    owner: 'alaiba',
    name: 'arcogine',
    prNumber: 11,
    named,
  });
  assert.deepEqual(
    await resolve({ commit: { oid: handoffCommit, committedDate: '2026-09-04T09:00:00Z' }, file: { oid: 'd'.repeat(40) } }),
    { path: handoffPath, commit: handoffCommit, committedAt: '2026-09-04T09:00:00Z' },
  );
  await assert.rejects(() => resolve({ commit: null, file: null }), /GitHub cannot resolve/);
  await assert.rejects(
    () => resolve({ commit: { oid: handoffCommit, committedDate: '2026-09-04T09:00:00Z' }, file: null }),
    /is not a file at commit/,
  );
  await assert.rejects(
    () => resolve({ commit: { oid: handoffCommit, committedDate: '2026-09-04T10:00:00Z' }, file: { oid: 'd'.repeat(40) } }),
    /GitHub records 2026-09-04T10:00:00Z/,
  );
});

test('an issue names a closing PR only when GitHub records one from this repository as its current closer', () => {
  const repo = 'alaiba/arcogine';
  const opened = '2026-09-03T00:00:00Z';
  const closed = '2026-09-05T00:00:03Z';
  assert.deepEqual(serializeIssue(issueNode(34, opened, closed, closingPr(11)), repo), {
    number: 34, createdAt: opened, closedAt: closed, closedByPullRequest: 11,
  });
  assert.equal(serializeIssue(issueNode(35, opened, closed), repo).closedByPullRequest, null);
  assert.equal(serializeIssue(issueNode(36, opened, closed, { __typename: 'Commit' }), repo).closedByPullRequest, null);
  assert.equal(
    serializeIssue(issueNode(37, opened, closed, closingPr(11, 'other/repository')), repo).closedByPullRequest,
    null,
  );
  assert.equal(serializeIssue(issueNode(38, opened, null, closingPr(11)), repo).closedByPullRequest, null);
  assert.throws(() => serializeIssue({ number: 39, createdAt: opened, closedAt: null }, repo), /malformed facts for issue #39/);
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
  const listedIssues = [
    issueNode(35, '2026-09-01T00:00:00Z', '2026-09-03T12:00:00Z', closingPr(9)),
    issueNode(30, '2026-09-01T00:00:00Z'),
    issueNode(32, '2026-09-02T00:00:00Z', '2026-09-06T10:00:00Z'),
    issueNode(33, '2026-09-02T00:00:00Z', '2026-09-06T09:00:00Z'),
    issueNode(34, '2026-09-03T00:00:00Z', '2026-09-05T00:00:03Z', closingPr(11)),
    issueNode(31, '2026-09-06T10:00:00Z'),
  ];
  const calls = [];
  const graphql = async (_token, query, variables) => {
    calls.push({ query, variables });
    if (query.includes('baseline: pullRequest')) return { repository: { baseline, through } };
    if (query.includes('search(type:ISSUE')) {
      return { search: { issueCount: searchItems.length, nodes: searchItems, pageInfo: { hasNextPage: false, endCursor: null } } };
    }
    if (query.includes('issues(first:100')) {
      return { repository: { issues: issueListing(listedIssues) } };
    }
    if (query.includes('commit: object(oid')) {
      return {
        repository: {
          commit: { oid: variables.oid, committedDate: '2026-09-04T09:00:00Z' },
          file: { oid: 'd'.repeat(40) },
        },
      };
    }
    if (query.includes('reviews(first:100)')) {
      const sourcePr = searchItems.find((item) => item.number === variables.number);
      const outOfWindow = ![11, 12].includes(sourcePr.number);
      return {
        repository: {
          pullRequest: {
            ...sourcePr,
            body: sourcePr.number === 11 ? `Implements slice 1.\n\n${handoffLine}\n\nCloses #34.` : null,
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
  assert.equal(result.schemaVersion, 2);
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
  assert.deepEqual(result.candidates.items[0].handoff, {
    path: handoffPath,
    commit: handoffCommit,
    committedAt: '2026-09-04T09:00:00Z',
  });
  assert.equal(result.candidates.items[1].handoff, null);
  assert.deepEqual(result.issues, {
    complete: true,
    reportedCount: 4,
    items: [
      { number: 30, createdAt: '2026-09-01T00:00:00Z', closedAt: null, closedByPullRequest: null },
      { number: 32, createdAt: '2026-09-02T00:00:00Z', closedAt: '2026-09-06T10:00:00Z', closedByPullRequest: null },
      { number: 33, createdAt: '2026-09-02T00:00:00Z', closedAt: '2026-09-06T09:00:00Z', closedByPullRequest: null },
      { number: 34, createdAt: '2026-09-03T00:00:00Z', closedAt: '2026-09-05T00:00:03Z', closedByPullRequest: 11 },
    ],
  });
  assert.equal(result.source.issueListing.reportedCount, 6);
  assert.equal('findingAnalytics' in result, false);
  assert.equal('disposition' in result, false);
  assert.equal(calls.length, 6);
  assert.deepEqual(
    calls.filter((call) => call.query.includes('reviews(first:100)')).map((call) => call.variables.number),
    [11, 12],
  );
  assert.match(calls[1].variables.query, /merged:2026-09-04\.\.2026-09-06/);
});

function windowGraphql(issuePage) {
  return async (_token, query, variables) => {
    if (query.includes('baseline: pullRequest')) return { repository: { baseline, through } };
    if (query.includes('search(type:ISSUE')) {
      return { search: { issueCount: 2, nodes: [baseline, through], pageInfo: { hasNextPage: false, endCursor: null } } };
    }
    if (query.includes('issues(first:100')) {
      assert.doesNotMatch(query, /states:|is:open|is:closed/);
      return { repository: { issues: issuePage(variables.cursor) } };
    }
    return {
      repository: {
        pullRequest: {
          ...through,
          body: null,
          reviews: { totalCount: 0, pageInfo: { hasNextPage: false, endCursor: null }, nodes: [] },
        },
      },
    };
  };
}

test('an issue that closes and reopens during acquisition stays in the issue cohort', async () => {
  // Issue #36 was open at the window end and closed after it; it is served closed, then reopened.
  let issueFetches = 0;
  const pages = (cursor) => {
    if (cursor === null) {
      return issueListing([issueNode(30, '2026-09-01T00:00:00Z')], { totalCount: 2, pageInfo: { hasNextPage: true, endCursor: 'next' } });
    }
    issueFetches += 1;
    return issueListing([issueFetches % 2
      ? issueNode(36, '2026-09-03T00:00:00Z', '2026-09-07T00:00:00Z')
      : issueNode(36, '2026-09-03T00:00:00Z')], { totalCount: 2 });
  };
  for (const closedAt of ['2026-09-07T00:00:00Z', null]) {
    const result = await acquireEvidence({
      repo: 'alaiba/arcogine', baselinePr: 10, throughPr: 12, token: 'fixture-token', graphql: windowGraphql(pages),
    });
    assert.deepEqual(result.issues.items.map((issue) => [issue.number, issue.closedAt]), [
      [30, null],
      [36, closedAt],
    ]);
  }
  assert.equal(issueFetches, 2);
});

test('the issue listing fails closed when its size changes during pagination', async () => {
  const pages = (cursor) => (cursor === null
    ? issueListing([issueNode(30, '2026-09-01T00:00:00Z')], { totalCount: 2, pageInfo: { hasNextPage: true, endCursor: 'next' } })
    : issueListing([issueNode(36, '2026-09-03T00:00:00Z')], { totalCount: 3 }));
  await assert.rejects(
    () => acquireEvidence({
      repo: 'alaiba/arcogine', baselinePr: 10, throughPr: 12, token: 'fixture-token', graphql: windowGraphql(pages),
    }),
    /issue listing result count changed during pagination \(2 -> 3\)/,
  );
});

test('the issue listing pages completely past the search result cap', async () => {
  const first = Array.from({ length: 1000 }, (_, index) => ({ number: index + 1 }));
  const issues = await collectIssuePages(async (cursor) => (cursor === null
    ? { totalCount: 1001, pageInfo: { hasNextPage: true, endCursor: 'next' }, nodes: first }
    : { totalCount: 1001, pageInfo: { hasNextPage: false, endCursor: null }, nodes: [{ number: 1001 }] }));
  assert.equal(issues.length, 1001);
});
