import { deepStrictEqual, match, ok, strictEqual, throws } from 'node:assert';
import { join } from 'node:path';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import {
  AUTHORITY_TEXT,
  REPOMIX_VERSION,
  assertHeaderPresent,
  buildHeader,
  buildTrackedFilesManifest,
  isCanonicalRemoteUrl,
  parseRemoteMainCommit,
  snapshotPath,
  validateCanonicalProvenance,
  validateRepositoryState,
} from './repo-snapshot.mjs';

const commit = '0123456789abcdef0123456789abcdef01234567';
const otherCommit = '89abcdef0123456789abcdef0123456789abcdef';
const generatedAt = '2026-09-16T00:00:00.000Z';

test('provenance header identifies reusable revision-bound project-source baseline', () => {
  const header = buildHeader({ commit, generatedAt });
  const normalizedHeader = header.replace(/\s+/g, ' ');
  match(header, new RegExp(`^Repository: alaiba/arcogine$`, 'm'));
  match(header, new RegExp(`^Branch: main$`, 'm'));
  match(header, new RegExp(`^Commit: ${commit}$`, 'm'));
  match(header, new RegExp(`^Generated: ${generatedAt}$`, 'm'));
  match(header, new RegExp(`^Generator: Repomix ${REPOMIX_VERSION}$`, 'm'));
  ok(normalizedHeader.includes('Purpose: project-source repository-content baseline'));
  ok(normalizedHeader.includes('use one live GitHub compare from S to that ref'));
  ok(normalizedHeader.includes('exact resolved target commit SHA (T)'));
  ok(normalizedHeader.includes('reuse it as T and compare S directly to T'));
  ok(normalizedHeader.includes('does not expose exact T, do not use its changed-path set for delta-mode live reads'));
  ok(normalizedHeader.includes('fetched at immutable ref=T rather than the mutable branch ref'));
  ok(normalizedHeader.includes('additions, modifications, deletions, renames, and copies'));
  ok(normalizedHeader.includes('Formal Consistency review follows this same protocol'));
  ok(normalizedHeader.includes('establish one exact target T and a complete target corpus'));
  ok(normalizedHeader.includes('refresh the project Repomix rather than attesting from a partial or ambiguous corpus'));
  ok(header.includes(AUTHORITY_TEXT));
  assertHeaderPresent(`${header}\n\n<tracked_files count="0">\n</tracked_files>`, header);
});

test('generation timestamp is unambiguous UTC ISO', () => {
  match(buildHeader({ commit, generatedAt }), /^Generated: \d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/m);
});

test('snapshot output is deterministic and directed under logs', () => {
  strictEqual(snapshotPath({ root: 'C:/repo', commit }), join('C:/repo', 'logs', 'arcogine-main-0123456.xml'));
});

test('feature branches, detached HEAD, and dirty trees are refused', () => {
  throws(() => validateRepositoryState({ branch: 'feature/snapshot', status: '' }), /current branch is feature\/snapshot/);
  throws(() => validateRepositoryState({ branch: '', status: '' }), /detached HEAD/);
  throws(() => validateRepositoryState({ branch: 'main', status: ' M arcogine' }), /working tree has changes/);
  strictEqual(validateRepositoryState({ branch: 'main', status: '' }), undefined);
});

test('canonical remote urls are recognized narrowly', () => {
  ok(isCanonicalRemoteUrl('git@github.com:alaiba/arcogine.git'));
  ok(isCanonicalRemoteUrl('ssh://git@github.com/alaiba/arcogine.git'));
  ok(isCanonicalRemoteUrl('https://github.com/alaiba/arcogine'));
  ok(!isCanonicalRemoteUrl('https://github.com/someone-else/arcogine.git'));
  ok(!isCanonicalRemoteUrl('https://example.com/alaiba/arcogine.git'));
  ok(!isCanonicalRemoteUrl(''));
});

test('remote main parser requires one exact main ref', () => {
  strictEqual(parseRemoteMainCommit(`${commit}\trefs/heads/main\n`), commit);
  throws(() => parseRemoteMainCommit(''), /could not resolve canonical/);
  throws(() => parseRemoteMainCommit(`${commit}\trefs/heads/not-main\n`), /could not resolve canonical/);
});

test('canonical provenance requires exact equality with remote main', () => {
  strictEqual(
    validateCanonicalProvenance({
      remoteUrl: 'https://github.com/alaiba/arcogine.git',
      commit,
      remoteMainCommit: commit,
    }),
    undefined,
  );

  throws(
    () => validateCanonicalProvenance({
      remoteUrl: 'https://github.com/alaiba/arcogine.git',
      commit,
      remoteMainCommit: otherCommit,
    }),
    /equal current canonical main/,
  );

  throws(
    () => validateCanonicalProvenance({
      remoteUrl: 'https://github.com/alaiba/arcogine.git',
      commit: otherCommit,
      remoteMainCommit: commit,
    }),
    /equal current canonical main/,
  );

  throws(
    () => validateCanonicalProvenance({
      remoteUrl: 'https://github.com/someone-else/arcogine.git',
      commit,
      remoteMainCommit: commit,
    }),
    /canonical alaiba\/arcogine repository/,
  );
});

test('tracked-file manifest is complete for its input and XML-safe', () => {
  const manifest = buildTrackedFilesManifest(['AGENTS.md', 'docs/a&b.md', 'assets/<diagram>.png']);
  match(manifest, /^<tracked_files count="3">/);
  match(manifest, /<path>AGENTS\.md<\/path>/);
  match(manifest, /<path>docs\/a&amp;b\.md<\/path>/);
  match(manifest, /<path>assets\/&lt;diagram&gt;\.png<\/path>/);
});

test('Repomix config preserves evidence and makes exclusions explicit', async () => {
  const config = JSON.parse(await readFile(new URL('./repomix.config.json', import.meta.url), 'utf8'));
  strictEqual(config.output.style, 'xml');
  strictEqual(config.output.compress, false);
  strictEqual(config.output.removeComments, false);
  strictEqual(config.output.fileSummary, false);
  strictEqual(config.output.directoryStructure, true);
  strictEqual(config.output.files, true);
  strictEqual(config.ignore.useGitignore, true);
  strictEqual(config.ignore.useDotIgnore, false);
  strictEqual(config.ignore.useDefaultPatterns, false);
  strictEqual(config.security.enableSecurityCheck, true);

  const ignored = config.ignore.customPatterns;
  deepStrictEqual(ignored, [
    '.git/**',
    '**/*.pem',
    '**/*.key',
    '**/.env',
    '**/.env.local',
    '**/.env.*.local',
  ]);
  for (const binaryPattern of ['**/*.jar', '**/*.pdf', '**/*.png', '**/*.zip']) {
    ok(!ignored.includes(binaryPattern));
  }
});
