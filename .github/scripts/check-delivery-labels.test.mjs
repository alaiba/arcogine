import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { execFileSync } from 'node:child_process';
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

test('accepts ordinary durable identifiers', (t) => {
  const root = fixture(t, { 'docs/architecture/overview.md': 'See D3 and O1.\n' });
  assert.deepEqual(check(root), []);
});

test('rejects the retired PLAN-* namespace in every tracked location', () => {
  for (const rel of ['docs/planning/example.md', 'docs/research/investigations/x.md', 'workspace/research/handoff.md', 'docs/architecture/overview.md']) {
    assert.match(validateFile(rel, 'PLAN-ENG-4\n')[0], /durable artifact contains a PLAN/);
  }
});

test('rejects reserved labels in durable content and filenames', () => {
  assert.match(validateFile('docs/architecture/PLAN-ENG-4-overview.md', 'Semantic\n')[0], /file path embeds/);
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
  assert.deepEqual(validateFile('AGENTS.md', 'PLAN-<TRACK>-<LOCAL-ID> and REV-<N>.\n'), []);
  assert.deepEqual(validateFile('docs/architecture/overview.md', 'D3, O1, and C1 are ordinary identifiers.\n'), []);
  assert.deepEqual(validateFile('docs/research/investigations/x.md', 'Gate 4, G1.3, W1 and DH-E are ordinary text.\n'), []);
});

test('rejects well-formed REV-<digits> identifiers of any width in tracked files', () => {
  for (const rev of ['REV-1', 'REV-9', 'REV-10', 'REV-2047']) {
    assert.match(validateFile('docs/architecture/overview.md', `${rev}\n`)[0], /durable artifact contains a REV/);
  }
});

test('exempts its own literal policy fixtures', () => {
  assert.deepEqual(validateFile('.github/scripts/check-delivery-labels.mjs', 'PLAN-ENG-4 and REV-123\n'), []);
  assert.deepEqual(validateFile('.github/scripts/check-delivery-labels.test.mjs', 'PLAN-ENG-4\n'), []);
});
