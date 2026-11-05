import { test } from 'node:test';
import assert from 'node:assert/strict';
import { execFileSync, spawnSync } from 'node:child_process';
import { chmodSync, cpSync, mkdirSync, mkdtempSync, rmSync, symlinkSync, writeFileSync } from 'node:fs';
import path from 'node:path';
import { tmpdir } from 'node:os';
import { fileURLToPath } from 'node:url';

const script = fileURLToPath(new URL('./scan-secrets.mjs', import.meta.url));
const config = fileURLToPath(new URL('../security/gitleaks.toml', import.meta.url));
// A synthetic value assembled at runtime must never appear in logs.
const secret = 'ghp_' + 'A1b2C3d4E5f6G7h8I9j0K1l2M3n4O5p6Q7r8';

test('real scanner enforces candidate history and tracked merge tree boundaries', () => {
  const root = mkdtempSync(path.join(tmpdir(), 'scan-scope-test-'));
  const git = (...args) => execFileSync('git', args, { cwd: root, encoding: 'utf8' }).trim();
  const commit = message => { git('add', '-A'); git('commit', '-qm', message); return git('rev-parse', 'HEAD'); };
  const scan = (args = [], env = {}) => spawnSync(process.execPath, [script, ...args], { cwd: root, encoding: 'utf8', env: { ...process.env, ...env } });
  const verify = (result, success) => {
    assert.equal(result.status === 0, success, `${result.stdout}\n${result.stderr}`);
    assert.ok(!(result.stdout + result.stderr).includes(secret), 'output exposed a secret');
  };
  try {
    git('init', '-q'); git('config', 'user.name', 'Test'); git('config', 'user.email', 'test@example.invalid');
    mkdirSync(path.join(root, '.github/security'), { recursive: true });
    cpSync(config, path.join(root, '.github/security/gitleaks.toml'));
    writeFileSync(path.join(root, 'clean.txt'), 'clean\n');
    const base = commit('base');
    git('checkout', '-qb', 'unrelated');
    writeFileSync(path.join(root, 'secret.txt'), `token = "${secret}"\n`);
    const unrelated = commit('unrelated secret');
    git('tag', 'unrelated-tag');
    git('checkout', '-q', '--detach', base);
    verify(scan(), true);
    writeFileSync(path.join(root, 'secret.txt'), `token = "${secret}"\n`);
    commit('secret added');
    rmSync(path.join(root, 'secret.txt'));
    commit('secret removed');
    verify(scan(), false);
    git('checkout', '-q', '--detach', base);
    symlinkSync('clean.txt', path.join(root, 'type-change.txt'));
    commit('add harmless symlink');
    rmSync(path.join(root, 'type-change.txt'));
    writeFileSync(path.join(root, 'type-change.txt'), `token = "${secret}"\n`);
    commit('replace symlink with secret file');
    assert.match(git('diff-tree', '--no-commit-id', '--name-status', '-r', 'HEAD'), /^T\s+type-change\.txt$/m);
    rmSync(path.join(root, 'type-change.txt'));
    commit('remove type-change secret');
    verify(scan(), false);
    git('checkout', '-q', '--detach', base);
    mkdirSync(path.join(root, 'workspace'), { recursive: true });
    writeFileSync(path.join(root, 'workspace/evidence.txt'), `token = "${secret}"\n`);
    commit('evidence secret');
    verify(scan(), false);
    git('checkout', '-q', '--detach', base);
    writeFileSync(path.join(root, 'head.txt'), 'head\n');
    const head = commit('head');
    git('checkout', '-q', '--detach', base);
    writeFileSync(path.join(root, 'base.txt'), 'base\n');
    const newBase = commit('new base');
    const cleanTree = git('write-tree');
    const cleanMerge = git('commit-tree', cleanTree, '-p', newBase, '-p', head, '-m', 'clean merge');
    git('checkout', '-q', '--detach', cleanMerge);
    verify(scan(['--ci'], { GITHUB_EVENT_NAME: 'pull_request', GITHUB_SHA: cleanMerge, PR_HEAD_SHA: head, PR_BASE_SHA: newBase }), true);
    git('checkout', '-q', '--detach', newBase);
    writeFileSync(path.join(root, 'merge-only.txt'), `token = "${secret}"\n`);
    git('add', 'merge-only.txt');
    const tree = git('write-tree');
    const merge = git('commit-tree', tree, '-p', newBase, '-p', head, '-m', 'merge');
    git('checkout', '-q', '--detach', merge);
    const env = { GITHUB_EVENT_NAME: 'pull_request', GITHUB_SHA: merge, PR_HEAD_SHA: head, PR_BASE_SHA: newBase };
    verify(scan(['--ci'], env), false);
    verify(scan(['--ci'], { ...env, PR_BASE_SHA: base }), false);
    verify(scan(['--ci'], { ...env, GITHUB_SHA: unrelated }), false);
    verify(scan(['--ci'], { ...env, PR_HEAD_SHA: '0'.repeat(40) }), false);
    verify(scan(['--ci'], { ...env, GITHUB_EVENT_NAME: 'unexpected' }), false);
    for (const event of ['push', 'schedule', 'workflow_dispatch']) {
      verify(scan(['--ci'], { GITHUB_EVENT_NAME: event, GITHUB_SHA: merge }), false);
    }
    rmSync(path.join(root, 'merge-only.txt'));
    commit('remove merge-only secret');
    verify(scan(), false);
    git('checkout', '-q', '--detach', head);
    for (const event of ['push', 'schedule', 'workflow_dispatch']) {
      verify(scan(['--ci'], { GITHUB_EVENT_NAME: event, GITHUB_SHA: head }), true);
    }
    const shallow = path.join(root, '.git/shallow');
    writeFileSync(shallow, `${head}\n`);
    verify(scan(), false);
    rmSync(shallow);
    const fakeBin = path.join(root, 'fake-bin');
    mkdirSync(fakeBin);
    const fakeScanner = path.join(fakeBin, 'gitleaks');
    writeFileSync(fakeScanner, '#!/bin/sh\nexit 42\n');
    chmodSync(fakeScanner, 0o755);
    verify(scan([], { PATH: `${fakeBin}${path.delimiter}${process.env.PATH}` }), false);
    verify(scan(['--ci'], { GITHUB_EVENT_NAME: 'pull_request', GITHUB_SHA: head, PR_HEAD_SHA: head, PR_BASE_SHA: newBase }), false);
    verify(spawnSync(process.execPath, [script], { cwd: root, encoding: 'utf8', env: { ...process.env, PATH: '' } }), false);
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});
