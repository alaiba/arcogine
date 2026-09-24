import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { execFileSync } from 'node:child_process';
import { trackedWorkspaceFiles } from './check-transient-workspace.mjs';

function repo(t, files) {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'transient-workspace-'));
  t.after(() => fs.rmSync(root, { recursive: true, force: true }));
  execFileSync('git', ['init', '-q'], { cwd: root });
  for (const name of files) {
    const file = path.join(root, name);
    fs.mkdirSync(path.dirname(file), { recursive: true });
    fs.writeFileSync(file, 'fixture\n');
  }
  execFileSync('git', ['add', '-A'], { cwd: root });
  return root;
}

test('passes when no workspace files are tracked', (t) => assert.deepEqual(trackedWorkspaceFiles(repo(t, ['docs/policy.md'])), []));
test('rejects tracked files throughout reserved workspace root', (t) => {
  const root = repo(t, ['workspace/implementation/prompt.md', 'workspace/research/checkpoint.md']);
  assert.deepEqual(trackedWorkspaceFiles(root), ['workspace/implementation/prompt.md', 'workspace/research/checkpoint.md']);
});
test('allows similarly named paths outside the root', (t) => assert.deepEqual(trackedWorkspaceFiles(repo(t, ['workspace-not-reserved/a.md', 'docs/workspace/a.md'])), []));
