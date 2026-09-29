import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { execFileSync } from 'node:child_process';
import { check, validateFile } from './check-source-authority-links.mjs';

function fixture(t, files) {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'source-authority-links-'));
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

test('rejects planning and research references from product source and tests', () => {
  for (const [rel, text] of [
    ['product/factory/src/main/java/Foo.java', '/** See docs/planning/factory.md. */\n'],
    ['product/factory/src/test/java/FooTest.java', '/** See ../../../../docs/research/investigations/foo.md. */\n'],
    ['product/factory/build.gradle.kts', '// docs\\planning\\factory.md\n'],
    ['product/factory/src/test/java/FooTest.java', '// workspace/research/investigations/foo.md\n'],
  ]) {
    assert.match(validateFile(rel, text)[0], /references transient planning\/research material/);
  }
});

test('allows durable semantic authorities from product source', () => {
  for (const text of [
    '/** See docs/architecture/engine-semantics.md. */\n',
    '/** See docs/reference/runtime.md. */\n',
    '/** Current behavior is deterministic. */\n',
  ]) {
    assert.deepEqual(validateFile('product/factory/src/main/java/Foo.java', text), []);
  }
});

test('does not apply the product-source rule to process documentation or repository tooling', () => {
  assert.deepEqual(validateFile('docs/development/reviewing.md', 'docs/planning/example.md\n'), []);
  assert.deepEqual(validateFile('.github/scripts/tool.mjs', 'docs/research/example.md\n'), []);
});

test('reads only tracked product files', (t) => {
  const root = fixture(t, { 'product/factory/src/main/java/Foo.java': '/** durable */\n' });
  const generated = path.join(root, 'product', 'factory', 'build', 'generated.java');
  fs.mkdirSync(path.dirname(generated), { recursive: true });
  fs.writeFileSync(generated, '/** docs/planning/example.md */\n');
  assert.deepEqual(check(root), []);
});

test('returns actionable file and line diagnostics', (t) => {
  const root = fixture(t, {
    'product/factory/src/test/java/FooTest.java': 'ok\n/** docs/research/example.md */\n',
  });
  assert.match(check(root)[0], /FooTest\.java:2: product source\/test references/);
});
