import test from 'node:test';
import assert from 'node:assert/strict';
import { buildInventory, collect, ghGet, validateCheckpoint } from './delivery-retrospective-branches.mjs';

const repo = 'alaiba/arcogine';
const checkpoint = { number: 460, title: 'Continuous improvement checkpoint', created_at: '2026-10-05T17:09:33Z' };
const asOf = '2026-10-15T10:00:00Z';
const branch = (name, tip = 'a'.repeat(40), protectedFlag = false) => ({
  name, commit: { sha: tip }, protected: protectedFlag,
});
const pr = (number, ref, options = {}) => ({
  number,
  title: options.title ?? 'Research reconciliation',
  state: options.closed === undefined ? 'closed' : options.closed ? 'closed' : 'open',
  created_at: options.createdAt ?? '2026-10-06T09:00:00Z',
  closed_at: options.closedAt === undefined ? '2026-10-10T12:00:00Z' : options.closedAt,
  merged_at: options.mergedAt === undefined ? '2026-10-10T12:00:00Z' : options.mergedAt,
  head: { ref, sha: 'f'.repeat(40), repo: options.fork ? { full_name: 'other/fork' } : options.deletedRepo ? null : { full_name: repo }, label: (options.fork ? 'other' : 'alaiba') + ':' + ref },
});

test('every live branch is inventoried even with no PR or pre-checkpoint activity', () => {
  const data = buildInventory({
    repo, defaultBranch: 'main', checkpoint, checkpointNumber: 460, scannedAt: asOf,
    branches: [branch('main', 'c'.repeat(40), true), branch('feature/no-pr'), branch('workspace/research-case')],
    pulls: [],
  });
  assert.deepEqual(data.branches.map(b => b.name), ['feature/no-pr', 'main', 'workspace/research-case']);
  assert.equal(data.branches.find(b => b.name === 'main').default, true);
  assert.equal(data.branches.find(b => b.name === 'feature/no-pr').live, true);
  assert.equal(data.checkpoint.openedAt, checkpoint.created_at);
  assert.equal(data.branches.every(b => b.linkedPrs.length === 0), true);
});

test('historical PR heads overlapping checkpoint window surface even after remote deletion', () => {
  const data = buildInventory({
    repo, defaultBranch: 'main', checkpoint, checkpointNumber: 460, scannedAt: asOf,
    branches: [branch('main')],
    pulls: [
      pr(471, 'workspace/analytics', { createdAt: '2026-10-01T00:00:00Z', deletedRepo: true }),
      pr(472, 'workspace/game', { closedAt: null, mergedAt: null, closed: false }),
      pr(430, 'old/closed', { createdAt: '2026-09-01T00:00:00Z', closedAt: '2026-10-03T00:00:00Z' }),
      pr(473, 'fork/research', { fork: true }),
    ],
  });
  assert.deepEqual(data.branches.map(b => b.name), ['main', 'workspace/analytics', 'workspace/game']);
  assert.equal(data.branches.find(b => b.name === 'workspace/analytics').live, false);
  assert.equal(data.branches.find(b => b.name === 'workspace/analytics').linkedPrs[0].sourceRepoVerified, false);
  assert.equal(data.branches.find(b => b.name === 'workspace/game').linkedPrs[0].state, 'open');
  assert.deepEqual(data.excludedForkPrs, [473]);
});

test('a retained live name with a different PR-recorded head keeps both identities', () => {
  const data = buildInventory({
    repo, defaultBranch: 'main', checkpoint, checkpointNumber: 460, scannedAt: asOf,
    branches: [branch('docs/reused', 'd'.repeat(40))],
    pulls: [pr(483, 'docs/reused')],
  });
  assert.equal(data.branches.length, 1);
  assert.equal(data.branches[0].tip, 'd'.repeat(40));
  assert.equal(data.branches[0].linkedPrs[0].recordedHead, 'f'.repeat(40));
  assert.deepEqual(data.branches[0].source, ['live-ref', 'historical-pr-head']);
});

test('malformed checkpoint, missing origin and duplicate evidence cannot silently pass', () => {
  assert.throws(() => validateCheckpoint({ ...checkpoint, title: 'Not a checkpoint' }, 460), /not the exact/);
  const data = { repo, defaultBranch: 'main', checkpoint, checkpointNumber: 460, scannedAt: asOf, branches: [], pulls: [] };
  assert.throws(() => buildInventory({ ...data, branches: [branch('same'), branch('same')] }), /repeated live/);
  assert.throws(() => buildInventory({ ...data, pulls: [pr(500, 'foo'), pr(500, 'foo')] }), /repeated pull/);
  assert.throws(() => buildInventory({ ...data, scannedAt: '2026-10-01T00:00:00Z' }), /after inventory/);
  const missing = buildInventory({ ...data, pulls: [pr(501, 'case', { deletedRepo: true, fork: true })] });
  assert.deepEqual(missing.unresolvedHeadPrs, [501]);
});

test('unknown origin is disclosed and not assumed to be same-repository research', () => {
  const p = pr(488, 'private-case', { deletedRepo: true });
  p.head.label = 'unknown:private-case';
  const data = buildInventory({
    repo, defaultBranch: 'main', checkpoint, checkpointNumber: 460, scannedAt: asOf,
    branches: [], pulls: [p],
  });
  assert.deepEqual(data.unresolvedHeadPrs, [488]);
  assert.equal(data.branches.length, 0);
});

test('collector uses paginated live branches and all-state PRs', () => {
  const paths = [];
  const get = (path, paginated) => {
    paths.push({ path, paginated });
    if (path.endsWith('/issues/460')) return checkpoint;
    if (path === 'repos/alaiba/arcogine') return { default_branch: 'main' };
    if (path.includes('/branches?')) return [branch('main'), branch('experimental/no-pr')];
    if (path.includes('/pulls?')) return [pr(492, 'workspace/research-case')];
    throw Error('unexpected fetch ' + path);
  };
  const result = collect({
    repo, checkpointNumber: 460, get, now: () => new Date(asOf),
  });
  assert.equal(result.branches.length, 3);
  assert.equal(paths.filter(x => x.paginated === true).length, 2);
  assert.ok(paths.find(x => x.path.includes('state=all')));
});

test('GitHub CLI pagination accepts all pages and rejects malformed slurp', () => {
  const fake = (_, args) => {
    assert.ok(args.includes('--paginate'));
    assert.ok(args.includes('--slurp'));
    return JSON.stringify([[{ name: 'a' }], [{ name: 'b' }]]);
  };
  assert.equal(ghGet('repos/a/b/branches?per_page=100', true, fake).length, 2);
  assert.throws(() => ghGet('x', true, () => JSON.stringify({ incomplete: true })), /pagination/);
});
