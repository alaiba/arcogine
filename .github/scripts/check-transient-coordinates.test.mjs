import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { execFileSync } from 'node:child_process';
import { check, validateFile } from './check-transient-coordinates.mjs';

const SHA = '0123456789abcdef0123456789abcdef01234567';
function repo(t, files) {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'transient-coordinates-'));
  t.after(() => fs.rmSync(root, { recursive: true, force: true }));
  execFileSync('git', ['init', '-q'], { cwd: root });
  for (const [name, content] of Object.entries(files)) {
    const file = path.join(root, name);
    fs.mkdirSync(path.dirname(file), { recursive: true });
    fs.writeFileSync(file, content);
  }
  execFileSync('git', ['add', '-A'], { cwd: root });
  return root;
}

test('rejects exact SHA and concrete workspace paths in durable files', (t) => {
  const root = repo(t, { 'docs/reference/example.md': `Evidence: ${SHA} + workspace/research/report.md\n` });
  assert.match(check(root)[0], /durable artifact contains a transient workspace coordinate/);
});
test('rejects workspace coordinates in non-Markdown durable text', () => {
  assert.match(validateFile('.github/workflows/example.yml', `artifact: ${SHA} workspace/review/result.json`)[0], /durable artifact contains a transient workspace coordinate/);
});
test('allows historical SHAs, generic policy and workspace mentions without a pair', () => {
  assert.deepEqual(validateFile('docs/development/example.md', `SHA ${SHA}; evidence in workspace/research/.`), []);
});
test('allows active planning and transient workspace coordinates', () => {
  assert.deepEqual(validateFile('docs/planning/example.md', `${SHA} workspace/research/report.md`), []);
  assert.deepEqual(validateFile('workspace/research/report.md', `${SHA} workspace/research/source.md`), []);
});
test('requires a complete hexadecimal SHA and a concrete workspace path', () => {
  assert.deepEqual(validateFile('docs/reference/example.md', `${SHA.slice(1)} workspace/research/report.md`), []);
  assert.deepEqual(validateFile('docs/reference/example.md', `${SHA} workspace/`), []);
});
test('exempts its own literal fixture file', () => {
  assert.deepEqual(validateFile('.github/scripts/check-transient-coordinates.test.mjs', `${SHA} workspace/research/report.md`), []);
});
