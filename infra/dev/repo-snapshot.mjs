#!/usr/bin/env node
/**
 * Generate Arcogine's canonical whole-repository Repomix snapshot.
 *
 * This utility intentionally owns only Arcogine's preconditions, provenance,
 * output location, and Repomix invocation. Repomix remains an ephemeral npx
 * dependency rather than a product or web-application dependency.
 */

import { execFileSync, spawnSync } from 'node:child_process';
import { existsSync, mkdirSync, readFileSync, rmSync, statSync, writeFileSync } from 'node:fs';
import { dirname, join, relative, resolve } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

export const REPOMIX_VERSION = '1.18.0';
export const REPOSITORY = 'alaiba/arcogine';
export const REQUIRED_BRANCH = 'main';
export const CANONICAL_REMOTE_PATTERN = /(?:^|[/:])alaiba\/arcogine(?:\.git)?$/i;
export const CANONICAL_MAIN_REF = 'refs/remotes/origin/main';

const scriptDirectory = dirname(fileURLToPath(import.meta.url));
export const REPOSITORY_ROOT = resolve(scriptDirectory, '..', '..');
export const CONFIG_PATH = join(scriptDirectory, 'repomix.config.json');
export const LOG_DIRECTORY = join(REPOSITORY_ROOT, 'logs');

export const AUTHORITY_TEXT = `This artifact is a point-in-time cache of repository contents.

It is suitable for repository-content retrieval and for claims about the
repository at the commit recorded above.

It is not live authority for:
- later changes to main;
- open pull requests or PR heads;
- submitted reviews or unresolved review threads;
- CI/check status;
- mergeability;
- issues;
- other mutable GitHub state.

Query live repository/GitHub evidence when freshness matters.`;

export function buildHeader({ commit, generatedAt, branch = REQUIRED_BRANCH }) {
  return `Repository: ${REPOSITORY}
Branch: ${branch}
Commit: ${commit}
Generated: ${generatedAt}
Generator: Repomix ${REPOMIX_VERSION}

${AUTHORITY_TEXT}`;
}

export function snapshotPath({ root = REPOSITORY_ROOT, commit }) {
  return join(root, 'logs', `arcogine-main-${commit.slice(0, 7)}.xml`);
}

export function validateRepositoryState({ branch, status }) {
  if (branch !== REQUIRED_BRANCH) {
    throw new Error(
      `snapshot requires a clean ${REQUIRED_BRANCH} checkout, but the current branch is ${branch || 'detached HEAD'}. ` +
        `Check out ${REQUIRED_BRANCH} and run './arcogine snapshot' again.`,
    );
  }
  if (status.trim() !== '') {
    throw new Error(
      `snapshot requires a clean ${REQUIRED_BRANCH} checkout, but the working tree has changes. ` +
        `Commit or move those changes elsewhere, then run './arcogine snapshot' again.`,
    );
  }
}

export function isCanonicalRemoteUrl(url) {
  return CANONICAL_REMOTE_PATTERN.test((url || '').trim().replace(/\/$/, ''));
}

export function validateCanonicalProvenance({ remoteUrl, commit, isAncestorOfMain }) {
  if (!isCanonicalRemoteUrl(remoteUrl)) {
    throw new Error(
      `snapshot requires the 'origin' remote to point at the canonical ${REPOSITORY} repository, but it resolved to ` +
        `${remoteUrl || '(no origin remote)'}. Canonical provenance cannot be established from a fork or unrelated remote.`,
    );
  }
  if (!isAncestorOfMain) {
    throw new Error(
      `snapshot requires HEAD (${commit}) to be reachable from the canonical ${REPOSITORY} history at ${CANONICAL_MAIN_REF}, ` +
        `but it is not. Push or fetch so the local ${REQUIRED_BRANCH} branch reflects canonical history, then run './arcogine snapshot' again.`,
    );
  }
}

function git(args, { allowFailure = false } = {}) {
  try {
    return execFileSync('git', args, {
      cwd: REPOSITORY_ROOT,
      encoding: 'utf8',
      stdio: ['ignore', 'pipe', 'pipe'],
    }).trimEnd();
  } catch (error) {
    if (allowFailure) return '';
    const detail = error?.stderr?.toString().trim();
    throw new Error(`could not inspect Git repository${detail ? `: ${detail}` : ''}`);
  }
}

function isAncestor(commit, ref) {
  const result = spawnSync('git', ['merge-base', '--is-ancestor', commit, ref], {
    cwd: REPOSITORY_ROOT,
    stdio: 'ignore',
  });
  return result.status === 0;
}

function invokeRepomix({ temporaryOutputPath }) {
  const executable = process.platform === 'win32' ? 'npx.cmd' : 'npx';
  const args = [
    '--yes',
    `repomix@${REPOMIX_VERSION}`,
    '--config',
    CONFIG_PATH,
    '--output',
    temporaryOutputPath,
    '--style',
    'xml',
    REPOSITORY_ROOT,
  ];
  const command = process.platform === 'win32' ? process.env.ComSpec || 'cmd.exe' : executable;
  const commandArgs =
    process.platform === 'win32'
      ? [
          '/d',
          '/s',
          '/c',
          [executable, ...args]
            .map((value, index) => (index === 0 ? value : quoteWindowsArgument(value)))
            .join(' '),
        ]
      : args;
  const result = spawnSync(
    command,
    commandArgs,
    {
      cwd: REPOSITORY_ROOT,
      stdio: 'inherit',
    },
  );

  if (result.error) {
    throw new Error(`could not start pinned Repomix ${REPOMIX_VERSION}: ${result.error.message}`);
  }
  if (result.status !== 0) {
    throw new Error(`Repomix ${REPOMIX_VERSION} failed with exit code ${result.status}`);
  }
}

function quoteWindowsArgument(value) {
  if (!/[\s"&|<>^]/.test(String(value))) return String(value);
  const escaped = String(value)
    .replace(/(\\*)"/g, '$1$1\\"')
    .replace(/(\\*)$/g, '$1$1');
  return `"${escaped}"`;
}

export function assertHeaderPresent(content, header) {
  if (!content.startsWith(`${header}\n\n`)) {
    throw new Error('generated snapshot did not contain the expected provenance header');
  }
}

export function generateSnapshot() {
  const branch = git(['symbolic-ref', '--quiet', '--short', 'HEAD'], { allowFailure: true });
  const status = git(['status', '--porcelain=v1', '--untracked-files=all']);
  validateRepositoryState({ branch, status });

  const commit = git(['rev-parse', 'HEAD']);
  if (!/^[0-9a-f]{40}$/.test(commit)) {
    throw new Error(`Git returned an invalid full commit SHA: ${commit}`);
  }

  const remoteUrl = git(['remote', 'get-url', 'origin'], { allowFailure: true });
  const isAncestorOfMain = isAncestor(commit, CANONICAL_MAIN_REF);
  validateCanonicalProvenance({ remoteUrl, commit, isAncestorOfMain });

  const generatedAt = new Date().toISOString();
  const outputPath = snapshotPath({ commit });
  const temporaryOutputPath = join(LOG_DIRECTORY, `.arcogine-main-${commit.slice(0, 7)}.repomix.xml`);

  mkdirSync(LOG_DIRECTORY, { recursive: true });
  rmSync(temporaryOutputPath, { force: true });

  try {
    invokeRepomix({ temporaryOutputPath });
    if (!existsSync(temporaryOutputPath)) {
      throw new Error(`Repomix completed without creating ${relative(REPOSITORY_ROOT, temporaryOutputPath)}`);
    }
    const repomixContent = readFileSync(temporaryOutputPath, 'utf8');
    const header = buildHeader({ branch, commit, generatedAt });
    const content = `${header}\n\n${repomixContent}`;
    assertHeaderPresent(content, header);
    writeFileSync(outputPath, content, 'utf8');

    const bytes = statSync(outputPath).size;
    return { outputPath, commit, generatedAt, branch, bytes };
  } finally {
    rmSync(temporaryOutputPath, { force: true });
  }
}

export function main() {
  try {
    const result = generateSnapshot();
    console.log(`Generated: ${relative(REPOSITORY_ROOT, result.outputPath)}`);
    console.log(`Commit: ${result.commit}`);
    console.log(`Timestamp: ${result.generatedAt} (UTC)`);
    console.log(`Repomix: ${REPOMIX_VERSION}`);
    console.log(`Size: ${result.bytes.toLocaleString('en-US')} bytes`);
  } catch (error) {
    console.error(`FATAL: ${error.message}`);
    process.exitCode = 1;
  }
}

const invokedScript = process.argv[1] ? pathToFileURL(resolve(process.argv[1])).href : null;
if (invokedScript && import.meta.url === invokedScript) {
  main();
}
