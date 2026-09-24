import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { check, localCandidate, markdownTargets } from './check-markdown-links.mjs';

test('parses inline and reference links while ignoring code', () => {
  assert.deepEqual(markdownTargets('See [guide](guide.md).\n[g]: <docs/a.md>\n`[x](no.md)`\n```\n[x](no.md)\n```'), [[1, 'guide.md'], [2, 'docs/a.md']]);
});

test('resolves relative, root-style, external, anchor, and route targets', (t) => {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'markdown-links-'));
  t.after(() => fs.rmSync(root, { recursive: true, force: true }));
  fs.mkdirSync(path.join(root, 'docs'));
  const source = path.join(root, 'docs', 'README.md');
  fs.writeFileSync(source, '# Docs\n');
  fs.writeFileSync(path.join(root, 'docs', 'guide.md'), '# Guide\n');
  assert.equal(localCandidate(root, source, 'guide.md'), path.join(root, 'docs', 'guide.md'));
  assert.equal(localCandidate(root, source, '/docs/guide.md'), path.join(root, 'docs', 'guide.md'));
  assert.equal(localCandidate(root, source, '/api/health'), null);
  assert.equal(localCandidate(root, source, 'https://example.com/'), null);
  assert.equal(localCandidate(root, source, '#section'), null);
});

test('checker reports broken links and ignores external/root web routes', (t) => {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'markdown-links-'));
  t.after(() => fs.rmSync(root, { recursive: true, force: true }));
  fs.mkdirSync(path.join(root, 'docs'));
  fs.writeFileSync(path.join(root, 'docs', 'README.md'), '[bad](missing.md)\n[route](/api/health)\n[external](https://example.com)\n');
  assert.deepEqual(check(root), ["docs/README.md:1: broken local link 'missing.md' -> docs/missing.md"]);
});

test('preserves parent-relative segments, URL decoding, and query/fragment handling', (t) => {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'markdown-links-'));
  t.after(() => fs.rmSync(root, { recursive: true, force: true }));
  fs.mkdirSync(path.join(root, 'docs', 'nested'), { recursive: true });
  fs.writeFileSync(path.join(root, 'docs', 'guide with space.md'), '# Guide\n');
  const source = path.join(root, 'docs', 'nested', 'README.md');
  fs.writeFileSync(source, '[parent](../guide%20with%20space.md?view=1#section)\n');
  assert.deepEqual(check(root), []);
});
