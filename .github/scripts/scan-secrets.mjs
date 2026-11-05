#!/usr/bin/env node
// Scan an immutable candidate's history and its complete tracked tree.
import { spawnSync } from 'node:child_process';
import { mkdtempSync, mkdirSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import path from 'node:path';

function run(command, args, options = {}) {
  const result = spawnSync(command, args, { maxBuffer: 256 * 1024 * 1024, ...options });
  if (result.error || result.status !== 0) throw new Error(`${command} failed (${result.status ?? result.error?.message})`);
  return result.stdout;
}

try {
  const git = (...args) => run('git', args, { encoding: 'utf8' }).trim();
  const root = git('rev-parse', '--show-toplevel');
  process.chdir(root);
  if (git('rev-parse', '--is-shallow-repository') !== 'false') throw new Error('shallow history is not supported');
  const fullSha = value => {
    if (!/^[0-9a-f]{40}$/.test(value || '')) throw new Error('a full commit SHA is required');
    if (git('rev-parse', '--verify', `${value}^{commit}`) !== value) throw new Error('commit mismatch');
    return value;
  };
  const checkedOut = fullSha(git('rev-parse', 'HEAD'));
  let history = checkedOut;
  if (process.argv[2] === '--ci') {
    const event = process.env.GITHUB_EVENT_NAME;
    const runSha = fullSha(process.env.GITHUB_SHA);
    if (checkedOut !== runSha) throw new Error('checkout does not match the event revision');
    if (event === 'pull_request') {
      const base = fullSha(process.env.PR_BASE_SHA);
      history = fullSha(process.env.PR_HEAD_SHA);
      const parents = git('show', '-s', '--format=%P', runSha).split(' ');
      if (parents.length !== 2 || parents[0] !== base || parents[1] !== history) {
        throw new Error('PR merge parents do not match event base and head');
      }
    } else if (!['push', 'schedule', 'workflow_dispatch'].includes(event)) {
      throw new Error('unsupported CI event');
    }
  } else if (process.argv.length !== 2) {
    throw new Error('usage: scan-secrets.mjs [--ci]');
  }
  // Missing objects are an error even when Git can otherwise enumerate a partial clone.
  for (const revision of new Set([history, checkedOut])) {
    const objects = git('rev-list', '--objects', '--missing=print', revision);
    if (objects.split('\n').some(line => line.startsWith('?'))) throw new Error('required Git objects are missing');
  }
  const config = path.join(root, '.github/security/gitleaks.toml');
  run('gitleaks', ['detect', '--source', root, '--config', config, '--redact=100', `--log-opts=--full-history -m ${history} --diff-filter=tuxdb`], { stdio: 'inherit' });

  const temporary = mkdtempSync(path.join(tmpdir(), 'arcogine-secret-tree-'));
  try {
    // Read blobs directly rather than using git archive (which honors export-ignore),
    // checkout filters, the working tree, or untracked/generated files.
    const entries = run('git', ['ls-tree', '-r', '-z', '--full-tree', checkedOut]).toString('utf8').split('\0').filter(Boolean);
    for (const entry of entries) {
      const match = /^(\d+) (\w+) ([0-9a-f]{40})\t([\s\S]+)$/.exec(entry);
      if (!match || match[2] !== 'blob') throw new Error('unsupported or malformed tracked tree entry');
      const target = path.resolve(temporary, match[4]);
      if (!target.startsWith(`${temporary}${path.sep}`)) throw new Error('unsafe tracked path');
      mkdirSync(path.dirname(target), { recursive: true });
      writeFileSync(target, run('git', ['cat-file', 'blob', match[3]]));
    }
    run('gitleaks', ['detect', '--no-git', '--source', temporary, '--config', config, '--redact=100'], { stdio: 'inherit' });
  } finally {
    rmSync(temporary, { recursive: true, force: true });
  }
} catch (error) {
  console.error(`Secret scan failed: ${error.message}`);
  process.exitCode = 1;
}
