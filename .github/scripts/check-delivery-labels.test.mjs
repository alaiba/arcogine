import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { execFileSync, spawnSync } from 'node:child_process';
import { check, validateFile } from './check-delivery-labels.mjs';

function fixture(t, files) {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'delivery-labels-'));
  t.after(() => fs.rmSync(root, { recursive: true, force: true }));
  execFileSync('git', ['init', '-q'], { cwd: root });
  for (const [name, content] of Object.entries(files)) {
    const target = path.join(root, name);
    fs.mkdirSync(path.dirname(target), { recursive: true });
    fs.writeFileSync(target, content);
  }
  execFileSync('git', ['add', '-A'], { cwd: root });
  return root;
}

test('accepts canonical planning coordinates and ordinary durable identifiers', (t) => {
  const root = fixture(t, { 'docs/planning/example.md': 'PLAN-ENG-4 and REV-123.\n', 'docs/architecture/overview.md': 'See D3 and O1.\n' });
  assert.deepEqual(check(root), []);
});

test('accepts simple and hierarchical canonical planning coordinates', () => {
  for (const text of ['PLAN-ENG-4\n', 'PLAN-ENG-4-B and PLAN-GOV-1-3\n']) assert.deepEqual(validateFile('docs/planning/example.md', text), []);
});

test('rejects malformed labels and legacy planning labels', () => {
  assert.match(validateFile('docs/planning/example.md', 'PLAN-eng-4\n')[0], /malformed delivery-coordinate/);
  assert.match(validateFile('docs/planning/example.md', 'Gate 4\n')[0], /legacy delivery-coordinate form/);
  assert.match(validateFile('docs/planning/example.md', 'REV-abc\n')[0], /malformed delivery-coordinate/);
  assert.match(validateFile('docs/planning/example.md', 'PLAN-GOV-1.3\n')[0], /malformed delivery-coordinate/);
  assert.match(validateFile('docs/planning/example.md', 'PLAN-ENG-4_B\n')[0], /malformed delivery-coordinate/);
  assert.match(validateFile('docs/planning/example.md', 'G1.3\n')[0], /legacy delivery-coordinate form/);
  for (const legacy of ['D1', 'C3', 'O2', 'W1', 'DH-E']) assert.match(validateFile('docs/planning/example.md', `${legacy}\n`)[0], /legacy delivery-coordinate form/);
});

test('rejects reserved labels in durable content and filenames', () => {
  assert.match(validateFile('docs/architecture/overview.md', 'PLAN-ENG-4\n')[0], /durable artifact contains a PLAN/);
  assert.match(validateFile('docs/architecture/PLAN-ENG-4-overview.md', 'Semantic\n')[0], /file path embeds/);
  assert.match(validateFile('docs/planning/gate-4-runtime.md', 'Semantic\n')[0], /planning filename/);
  assert.match(validateFile('docs/architecture/overview.md', 'PLAN-eng-4\n')[0], /durable artifact contains a PLAN/);
  assert.match(validateFile('docs/architecture/overview.md', 'REV-123\n')[0], /durable artifact contains a REV/);
  assert.match(validateFile('docs/architecture/overview.md', 'REV-12.foo\n')[0], /malformed delivery-coordinate/);
  assert.match(validateFile('product/Foo.java', "/** PLAN-ENG-4\'s contract. */\n")[0], /durable artifact contains a PLAN/);
  for (const rel of ['infra/dev/tool.mjs', '.github/workflows/ci.yml', 'product/Foo.java']) assert.match(validateFile(rel, 'PLAN-ENG-4\n')[0], /durable artifact contains a PLAN/);
});

test('reads only tracked files and ignores untracked/generated output', (t) => {
  const root = fixture(t, { 'docs/architecture/overview.md': 'Semantic\n' });
  const generated = path.join(root, 'build', 'generated.md');
  fs.mkdirSync(path.dirname(generated));
  fs.writeFileSync(generated, 'PLAN-ENG-4\n');
  assert.deepEqual(check(root), []);
});

test('returns actionable diagnostics with file and line for tracked leaks', (t) => {
  const root = fixture(t, { 'docs/architecture/overview.md': 'ok\nPLAN-ENG-4\n' });
  assert.match(check(root)[0], /overview\.md:2: durable artifact contains/);
});

test('allows ordinary compact identifiers and policy metavariables', () => {
  assert.deepEqual(validateFile('AGENTS.md', 'PLAN-<TRACK>-<LOCAL-ID> and REV-<NNN>.\n'), []);
  assert.deepEqual(validateFile('docs/architecture/overview.md', 'D3, O1, and C1 are ordinary identifiers.\n'), []);
  assert.deepEqual(validateFile('docs/planning/example.md', 'See PLAN-ENG-4.\n'), []);
  assert.deepEqual(validateFile('docs/planning/runtime-observation.md', 'Semantic content.\n'), []);
});

test('exempts its own literal policy fixtures', () => {
  assert.deepEqual(validateFile('.github/scripts/check-delivery-labels.mjs', 'PLAN-ENG-4 and REV-123\n'), []);
  assert.deepEqual(validateFile('.github/scripts/check-delivery-labels.test.mjs', 'PLAN-ENG-4\n'), []);
});
