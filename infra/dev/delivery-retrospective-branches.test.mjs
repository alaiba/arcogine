import test from 'node:test';
import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { mkdtempSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';
import {
  buildInventory, collect, ghGet, readBaselinePr, resolveWindow, windowRelation,
} from './delivery-retrospective-branches.mjs';
import { analyzeEvidence } from './delivery-retrospective.mjs';
import { EVIDENCE_SCHEMA_VERSION } from './delivery-retrospective-evidence.mjs';

const repo = 'alaiba/arcogine';
const SINCE = '2026-09-21T10:00:00Z';
const IN_WINDOW = '2026-10-01T12:00:00Z';
const UNTIL = '2026-10-14T12:00:00Z';
const SCANNED = '2026-10-16T09:00:00Z';
const script = fileURLToPath(new URL('./delivery-retrospective-branches.mjs', import.meta.url));

const candidate = (number, mergedAt) => ({
  number, merged: true, mergedAt, baseRefName: 'main',
  reviews: { complete: true, reportedCount: 0, items: [] },
  handoff: null,
});
const evidence = (overrides = {}) => ({
  schemaVersion: EVIDENCE_SCHEMA_VERSION,
  repository: repo,
  requested: { baselinePr: 368, throughPr: 500 },
  endpoints: {
    baseline: { number: 368, merged: true, mergedAt: SINCE, baseRefName: 'main' },
    through: { number: 500, merged: true, mergedAt: UNTIL, baseRefName: 'main' },
  },
  candidates: { complete: true, reportedCount: 2, items: [candidate(480, IN_WINDOW), candidate(500, UNTIL)] },
  issues: { complete: true, reportedCount: 0, items: [] },
  source: { adapter: 'github-graphql', acquiredAt: '2026-10-16T08:59:00Z' },
  ...overrides,
});
const branch = (name, tip = 'a'.repeat(40), protectedFlag = false) => ({
  name, commit: { sha: tip }, protected: protectedFlag,
});
const pr = (number, ref, options = {}) => {
  const open = options.open === true;
  const closedAt = open ? null : options.closedAt ?? IN_WINDOW;
  return {
    number,
    title: options.title ?? 'Change ' + number,
    state: open ? 'open' : 'closed',
    created_at: options.createdAt ?? '2026-09-30T09:00:00Z',
    closed_at: closedAt,
    merged_at: options.merged === true && !open ? closedAt : null,
    base: { ref: options.base ?? 'main' },
    head: {
      ref,
      sha: options.headSha ?? 'f'.repeat(40),
      repo: options.fork ? { full_name: 'other/fork' } : options.deletedRepo ? null : { full_name: repo },
      label: options.label ?? (options.fork ? 'other' : 'alaiba') + ':' + ref,
    },
  };
};
// The merged PRs the evidence proves, as the all-state listing reports them.
const windowPulls = () => [
  pr(368, 'retro/baseline', { createdAt: '2026-09-20T09:00:00Z', closedAt: SINCE, merged: true }),
  pr(480, 'feature/in-window', { closedAt: IN_WINDOW, merged: true }),
  pr(500, 'docs/through', { createdAt: '2026-10-13T09:00:00Z', closedAt: UNTIL, merged: true }),
];
const spanning = () => pr(401, 'research/spanning', {
  createdAt: '2026-09-01T00:00:00Z', closedAt: '2026-10-20T00:00:00Z', merged: true,
});
const inventory = (overrides = {}) => buildInventory({
  repo, defaultBranch: 'main', evidence: evidence(), baselinePr: 368, scannedAt: SCANNED,
  branches: [branch('main', 'c'.repeat(40), true)], pulls: windowPulls(), ...overrides,
});
const named = (data, name) => data.branches.find((entry) => entry.name === name);

test('the evidence bundle merge timestamps are the branch window, shared with the PR/review analysis', () => {
  const data = inventory();
  const analysis = analyzeEvidence(evidence());
  assert.deepEqual(data.window.baseline, { number: 368, mergedAt: SINCE });
  assert.deepEqual(data.window.through, { number: 500, mergedAt: UNTIL });
  assert.equal(data.window.afterMergedAtExclusive, analysis.exactWindow.afterMergedAtExclusive);
  assert.equal(data.window.throughMergedAtInclusive, analysis.exactWindow.throughMergedAtInclusive);
  assert.equal(data.scannedAt, SCANNED);
  assert.equal('checkpoint' in data, false);
  assert.equal(named(data, 'retro/baseline'), undefined);
  const through = named(data, 'docs/through').linkedPrs[0];
  assert.equal(through.windowRelation, 'overlaps');
  assert.equal(through.mergedInWindow, true);
  assert.equal(through.inMainPrSample, true);
});

test('only the merged-main PR sample counts as delivery; other in-window merges keep their target', () => {
  const data = inventory({
    pulls: [
      ...windowPulls(),
      pr(470, 'research/topic', {
        createdAt: '2026-09-01T00:00:00Z', closedAt: IN_WINDOW, merged: true, base: 'workspace/integration',
      }),
    ],
  });
  const facts = (name) => {
    const { baseRef, mergedInWindow, inMainPrSample } = named(data, name).linkedPrs[0];
    return { baseRef, mergedInWindow, inMainPrSample };
  };
  assert.deepEqual(facts('research/topic'), {
    baseRef: 'workspace/integration', mergedInWindow: true, inMainPrSample: false,
  });
  assert.deepEqual(facts('feature/in-window'), { baseRef: 'main', mergedInWindow: true, inMainPrSample: true });
});

test('PR lifecycle overlap respects the exclusive lower and inclusive upper bounds', () => {
  const data = inventory({
    pulls: [
      ...windowPulls(),
      spanning(),
      pr(402, 'research/open-since-before', { createdAt: '2026-09-10T00:00:00Z', open: true }),
      pr(403, 'old/ends-at-baseline', { createdAt: '2026-09-15T00:00:00Z', closedAt: SINCE, merged: false }),
      pr(404, 'edge/created-at-through', { createdAt: UNTIL, open: true }),
      pr(405, 'later/created-after-through', { createdAt: '2026-10-14T12:00:01Z', closedAt: '2026-10-15T00:00:00Z' }),
    ],
  });
  assert.deepEqual(data.branches.map((entry) => entry.name), [
    'docs/through', 'edge/created-at-through', 'feature/in-window', 'main',
    'research/open-since-before', 'research/spanning',
  ]);
  assert.equal(named(data, 'research/spanning').live, false);
  assert.equal(named(data, 'research/spanning').prOverlapsWindow, true);
});

test('every live branch stays in the inventory without manufactured window relevance', () => {
  const data = inventory({
    branches: [
      branch('main', 'c'.repeat(40), true), branch('feature/in-window'), branch('research/no-pr'),
      branch('docs/old'), branch('feature/next'), branch('experimental/untouched'),
    ],
    pulls: [
      ...windowPulls(),
      pr(300, 'docs/old', { createdAt: '2026-08-01T00:00:00Z', closedAt: '2026-08-02T00:00:00Z' }),
      pr(510, 'feature/next', { createdAt: '2026-10-15T00:00:00Z', open: true }),
    ],
  });
  assert.deepEqual(data.branches.map((entry) => entry.name), [
    'docs/old', 'docs/through', 'experimental/untouched', 'feature/in-window', 'feature/next',
    'main', 'research/no-pr',
  ]);
  assert.equal(named(data, 'main').default, true);
  assert.equal(named(data, 'main').protected, true);
  assert.equal(named(data, 'feature/in-window').prOverlapsWindow, true);
  for (const name of ['docs/old', 'experimental/untouched', 'feature/next', 'main', 'research/no-pr']) {
    assert.equal(named(data, name).live, true, name);
    assert.equal(named(data, name).prOverlapsWindow, false, name);
  }
  assert.equal(named(data, 'docs/old').linkedPrs[0].windowRelation, 'before');
  assert.deepEqual(
    named(data, 'feature/next').linkedPrs.map(({ windowRelation: relation, state }) => [relation, state]),
    [['after', 'open']],
  );
});

test('deleted heads surface, forks are excluded, unknown origins are disclosed, reused names keep provenance', () => {
  const unknown = pr(488, 'private-case', { deletedRepo: true, label: 'unknown:private-case' });
  const noHead = pr(489, null);
  const unknownReused = pr(250, 'docs/reused', {
    createdAt: '2026-07-01T00:00:00Z', closedAt: '2026-07-02T00:00:00Z', deletedRepo: true, label: 'unknown:docs/reused',
  });
  const data = inventory({
    branches: [branch('main'), branch('docs/reused', 'd'.repeat(40))],
    pulls: [
      ...windowPulls(),
      pr(471, 'research/deleted', { deletedRepo: true }),
      pr(473, 'fork/research', { fork: true }),
      pr(200, 'fork/old', { fork: true, createdAt: '2026-06-01T00:00:00Z', closedAt: '2026-06-02T00:00:00Z' }),
      unknown, noHead, unknownReused,
      pr(290, 'docs/reused', { createdAt: '2026-08-01T00:00:00Z', closedAt: '2026-08-02T00:00:00Z', headSha: 'e'.repeat(40) }),
      pr(483, 'docs/reused'),
    ],
  });
  const deleted = named(data, 'research/deleted');
  assert.equal(deleted.live, false);
  assert.equal(deleted.tip, null);
  assert.deepEqual(deleted.source, ['historical-pr-head']);
  assert.equal(deleted.linkedPrs[0].sourceRepoVerified, false);
  assert.deepEqual(data.excludedForkPrs, [473]);
  assert.equal(named(data, 'fork/research'), undefined);
  assert.deepEqual(data.unresolvedHeadPrs, [250, 488, 489]);
  assert.equal(named(data, 'private-case'), undefined);
  const reused = named(data, 'docs/reused');
  assert.equal(reused.tip, 'd'.repeat(40));
  assert.deepEqual(reused.source, ['live-ref', 'historical-pr-head']);
  assert.deepEqual(
    reused.linkedPrs.map(({ number, recordedHead, windowRelation: relation }) => [number, recordedHead, relation]),
    [[290, 'e'.repeat(40), 'before'], [483, 'f'.repeat(40), 'overlaps']],
  );
});

test('invalid, mismatched, or incomplete evidence and repository facts fail closed', () => {
  const through = evidence().endpoints.through;
  const failures = [
    [{ evidence: undefined }, /root must be an object/],
    [{ evidence: evidence({ schemaVersion: EVIDENCE_SCHEMA_VERSION + 1 }) }, /unsupported schema version/],
    [{ evidence: evidence({ issues: { complete: false, reportedCount: 0, items: [] } }) }, /issue collection is incomplete/],
    [{ evidence: evidence({ repository: 'other/repo' }) }, /does not match alaiba\/arcogine/],
    [{ baselinePr: 367 }, /expected retrospective baseline PR #367/],
    [{ baselinePr: undefined }, /positive integer/],
    [{ evidence: evidence({ endpoints: { ...evidence().endpoints, through: { ...through, mergedAt: null } } }) }, /through endpoint must be a merged PR/],
    [{ evidence: evidence({ endpoints: { ...evidence().endpoints, through: { ...through, mergedAt: '2026-09-01T00:00:00Z' } } }) }, /must merge after/],
    [{ evidence: evidence({ candidates: { ...evidence().candidates, complete: false } }) }, /incomplete/],
    [{ pulls: windowPulls().filter((entry) => entry.number !== 480) }, /incomplete or disagrees .* PR #480/],
    [{ pulls: windowPulls().map((entry) => entry.number === 500 ? { ...entry, merged_at: SCANNED, closed_at: SCANNED } : entry) }, /PR #500/],
    [{ pulls: windowPulls().map((entry) => entry.number === 480 ? { ...entry, base: { ref: 'develop' } } : entry) }, /disagrees .* PR #480/],
    [{ pulls: [...windowPulls(), pr(490, 'feature/unsampled', { merged: true })] }, /evidence omits PR #490/],
    [{ pulls: [...windowPulls(), { ...pr(607, 'x'), base: null }] }, /PR #607 target branch is missing/],
    [{ pulls: [...windowPulls(), { ...pr(600, 'x'), state: 'open' }] }, /state and closure disagree/],
    [{ pulls: [...windowPulls(), { ...pr(601, 'x'), state: 'merged' }] }, /unknown state/],
    [{ pulls: [...windowPulls(), pr(602, 'x', { createdAt: '2026-10-02T00:00:00Z' })] }, /closes before it was created/],
    [{ pulls: [...windowPulls(), { ...pr(603, 'x'), merged_at: 'not-a-date' }] }, /PR #603 merge timestamp/],
    [{ pulls: [...windowPulls(), { ...pr(604, 'x'), merged_at: undefined }] }, /PR #604 merge timestamp/],
    [{ pulls: [...windowPulls(), { ...pr(605, 'x', { open: true }), merged_at: IN_WINDOW }] }, /merged but not closed/],
    [{ pulls: [...windowPulls(), pr(606, 'x'), pr(606, 'x')] }, /Repeated pull request/],
    [{ branches: [branch('main'), branch('same'), branch('same')] }, /repeated live/],
    [{ branches: [branch('feature/only')] }, /lacks the default branch main/],
    [{ scannedAt: '2026-10-14T11:59:59Z' }, /precedes the through PR merge/],
  ];
  for (const [overrides, pattern] of failures) {
    assert.throws(() => inventory(overrides), pattern, String(pattern));
  }
});

test('lifecycles spanning the window keep earlier and later facts temporally labeled', () => {
  const window = resolveWindow(evidence(), { repo, baselinePr: 368 });
  const relation = (start, end) => windowRelation({ start, end }, window);
  assert.equal(relation('2026-09-01T00:00:00Z', '2026-10-20T00:00:00Z'), 'overlaps');
  assert.equal(relation('2026-09-01T00:00:00Z', null), 'overlaps');
  assert.equal(relation('2026-09-01T00:00:00Z', SINCE), 'before');
  assert.equal(relation(SINCE, SINCE), 'before');
  assert.equal(relation(UNTIL, UNTIL), 'overlaps');
  assert.equal(relation('2026-10-14T12:00:01Z', null), 'after');
  assert.throws(() => relation(UNTIL, SINCE), /ends before it starts/);

  const data = inventory({ pulls: [...windowPulls(), spanning()] });
  const linked = named(data, 'research/spanning').linkedPrs[0];
  assert.equal(linked.windowRelation, 'overlaps');
  assert.equal(linked.mergedInWindow, false);
  assert.equal(linked.inMainPrSample, false);
  assert.equal(linked.mergedAt, '2026-10-20T00:00:00Z');
});

test('collector only reads, validates evidence before any API call, and paginates branches and all-state PRs', () => {
  const calls = [];
  const get = (path, paginated) => {
    calls.push({ path, paginated });
    if (path === 'repos/alaiba/arcogine') return { default_branch: 'main' };
    if (path.includes('/branches?')) return [branch('main'), branch('experimental/no-pr')];
    if (path.includes('/pulls?')) return windowPulls();
    throw Error('unexpected fetch ' + path);
  };
  const result = collect({ repo, evidence: evidence(), baselinePr: 368, get, now: () => new Date(SCANNED) });
  assert.deepEqual(result.branches.map((entry) => entry.name), ['docs/through', 'experimental/no-pr', 'feature/in-window', 'main']);
  assert.equal(result.scannedAt, new Date(SCANNED).toISOString());
  assert.equal(calls.filter((call) => call.paginated === true).length, 2);
  assert.ok(calls.some((call) => call.path.includes('state=all')));
  assert.equal(calls.some((call) => call.path.includes('/issues')), false);

  calls.length = 0;
  assert.throws(() => collect({ repo, evidence: evidence(), baselinePr: 367, get }), /baseline/);
  assert.equal(calls.length, 0);
});

test('GitHub CLI access is GET-only; pagination accepts all pages and rejects malformed slurp', () => {
  const fake = (command, args) => {
    assert.equal(command, 'gh');
    assert.deepEqual(args.slice(0, 3), ['api', '-X', 'GET']);
    assert.ok(args.includes('--paginate'));
    assert.ok(args.includes('--slurp'));
    return JSON.stringify([[{ name: 'a' }], [{ name: 'b' }]]);
  };
  assert.equal(ghGet('repos/a/b/branches?per_page=100', true, fake).length, 2);
  assert.throws(() => ghGet('x', true, () => JSON.stringify({ incomplete: true })), /pagination/);
});

test('the expected baseline comes from versioned retrospective state', () => {
  assert.ok(Number.isSafeInteger(readBaselinePr()) && readBaselinePr() > 0);
  const dir = mkdtempSync(join(tmpdir(), 'retrospective-branches-'));
  writeFileSync(join(dir, 'valid.json'), JSON.stringify({ schemaVersion: 2, baselinePr: 368 }));
  writeFileSync(join(dir, 'invalid.json'), JSON.stringify({ schemaVersion: 2, baselinePr: '368' }));
  assert.equal(readBaselinePr(join(dir, 'valid.json')), 368);
  assert.throws(() => readBaselinePr(join(dir, 'invalid.json')), /no positive baselinePr/);
  assert.throws(() => readBaselinePr(join(dir, 'absent.json')), /Cannot read retrospective state/);
});

test('CLI rejects the former checkpoint interface and fails closed before any API call', () => {
  const run = (...args) => spawnSync(process.execPath, [script, ...args], { encoding: 'utf8' });
  const legacy = run('--checkpoint', '460');
  assert.equal(legacy.status, 1);
  assert.match(legacy.stderr, /Unknown option '--checkpoint'/);
  const missing = run();
  assert.equal(missing.status, 1);
  assert.match(missing.stderr, /Provide --evidence/);
  const dir = mkdtempSync(join(tmpdir(), 'retrospective-branches-cli-'));
  const absent = run('--evidence', join(dir, 'absent.json'), '--baseline-pr', '368');
  assert.equal(absent.status, 1);
  assert.match(absent.stderr, /Cannot read evidence bundle/);
  writeFileSync(join(dir, 'other.json'), JSON.stringify(evidence({ repository: 'other/repo' })));
  const mismatched = run('--evidence', join(dir, 'other.json'), '--baseline-pr', '368');
  assert.equal(mismatched.status, 1);
  assert.match(mismatched.stderr, /does not match alaiba\/arcogine/);
});
