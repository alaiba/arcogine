import { deepStrictEqual, match, ok, strictEqual, throws } from 'node:assert';
import { join } from 'node:path';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import {
  AUTHORITY_TEXT,
  REPOMIX_VERSION,
  assertHeaderPresent,
  buildHeader,
  snapshotPath,
  validateRepositoryState,
} from './repo-snapshot.mjs';

const commit = '0123456789abcdef0123456789abcdef01234567';
const generatedAt = '2026-09-05T00:00:00.000Z';

test('provenance header contains the exact represented state and authority boundary', () => {
  const header = buildHeader({ commit, generatedAt });

  match(header, new RegExp(`^Repository: alaiba/arcogine$`, 'm'));
  match(header, new RegExp(`^Branch: main$`, 'm'));
  match(header, new RegExp(`^Commit: ${commit}$`, 'm'));
  match(header, new RegExp(`^Generated: ${generatedAt}$`, 'm'));
  match(header, new RegExp(`^Generator: Repomix ${REPOMIX_VERSION}$`, 'm'));
  ok(header.includes(AUTHORITY_TEXT));
  assertHeaderPresent(`${header}\n\n<file_summary />`, header);
});

test('generation timestamp is required to be an unambiguous UTC ISO timestamp', () => {
  const header = buildHeader({ commit, generatedAt });
  strictEqual(generatedAt.endsWith('Z'), true);
  match(header, /^Generated: \d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/m);
});

test('snapshot output is deterministic and directed under logs', () => {
  strictEqual(snapshotPath({ root: 'C:/repo', commit }), join('C:/repo', 'logs', 'arcogine-main-0123456.xml'));
});

test('feature branches and dirty working trees are refused', () => {
  throws(
    () => validateRepositoryState({ branch: 'feature/snapshot', status: '' }),
    /current branch is feature\/snapshot/,
  );
  throws(
    () => validateRepositoryState({ branch: 'main', status: ' M arcogine' }),
    /working tree has changes/,
  );
  throws(
    () => validateRepositoryState({ branch: '', status: '' }),
    /current branch is detached HEAD/,
  );
});

test('clean main is accepted', () => {
  strictEqual(validateRepositoryState({ branch: 'main', status: '' }), undefined);
});

test('config retains source and documentation while excluding generated/dependency material', async () => {
  const config = JSON.parse(await readFile(new URL('./repomix.config.json', import.meta.url), 'utf8'));
  deepStrictEqual(config.output.style, 'xml');
  strictEqual(config.output.compress, false);
  const ignored = config.ignore.customPatterns;
  ok(ignored.includes('logs/**'));
  ok(ignored.includes('**/build/**'));
  ok(ignored.includes('**/node_modules/**'));
  ok(ignored.includes('dist/**'));
  ok(ignored.includes('**/*.jar'));
  ok(config.ignore.useGitignore);
  ok(config.security.enableSecurityCheck);
});
