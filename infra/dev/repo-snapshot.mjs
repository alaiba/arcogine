#!/usr/bin/env node
/** Generate Arcogine's exact-current-main Repomix corpus. */

import { execFileSync, spawnSync } from 'node:child_process';
import { existsSync, mkdirSync, readFileSync, rmSync, statSync, writeFileSync } from 'node:fs';
import { dirname, join, relative, resolve } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

export const REPOMIX_VERSION = '1.18.0';
export const REPOSITORY = 'alaiba/arcogine';
export const REQUIRED_BRANCH = 'main';
export const CANONICAL_MAIN_REF = 'refs/heads/main';
export const CANONICAL_REMOTE_PATTERN =
  /^(?:https:\/\/github\.com\/|git@github\.com:|ssh:\/\/git@github\.com\/)alaiba\/arcogine(?:\.git)?\/?$/i;

const scriptDirectory = dirname(fileURLToPath(import.meta.url));
export const REPOSITORY_ROOT = resolve(scriptDirectory, '..', '..');
export const CONFIG_PATH = join(scriptDirectory, 'repomix.config.json');
export const LOG_DIRECTORY = join(REPOSITORY_ROOT, 'logs');

export const AUTHORITY_TEXT = `Purpose: ChatGPT/project-source repository-content baseline

This artifact represents canonical alaiba/arcogine main at exactly the commit recorded above (S).
For ordinary ChatGPT/project-source use, resolve the repository revision the task needs as target T.
If S equals T, use this artifact directly. If S is an ancestor of T and live GitHub can establish a
complete S..T changed-path delta, keep this artifact as the primary corpus for unchanged paths and
use live target content only for affected paths. If ancestry or a complete usable delta cannot be
established, use live repository evidence for T or refresh the snapshot. This baseline-plus-delta
rule may be used for current main or another descendant branch head.

A formal Consistency review is stricter: S must equal live main exactly before repository content is
used. If it does not, stop, update the project Repomix from current main, and retry; do not rebuild
the formal review corpus through baseline-plus-delta reconciliation.

The tracked-file manifest enumerates every git-tracked path at S. Repomix content follows for
reviewable repository text. Generated/dependency material is excluded by repository ignore rules;
secret-like files are excluded explicitly; binary contents may be omitted while their paths remain
visible in the manifest.

This artifact is not live authority for issues, pull requests, reviews, CI/check status,
mergeability, branch heads, or other mutable GitHub state.`;

export function buildHeader({ commit, generatedAt, branch = REQUIRED_BRANCH }) {
  return `Repository: ${REPOSITORY}\nBranch: ${branch}\nCommit: ${commit}\nGenerated: ${generatedAt}\nGenerator: Repomix ${REPOMIX_VERSION}\n\n${AUTHORITY_TEXT}`;
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

export function parseRemoteMainCommit(output) {
  const line = (output || '').trim();
  const match = /^([0-9a-f]{40})\s+refs\/heads\/main$/.exec(line);
  if (!match) {
    throw new Error(`could not resolve canonical ${REPOSITORY} ${CANONICAL_MAIN_REF} from origin`);
  }
  return match[1];
}

export function validateCanonicalProvenance({ remoteUrl, commit, remoteMainCommit }) {
  if (!isCanonicalRemoteUrl(remoteUrl)) {
    throw new Error(
      `snapshot requires the 'origin' remote to point at the canonical ${REPOSITORY} repository, but it resolved to ` +
        `${remoteUrl || '(no origin remote)'}.`,
    );
  }
  if (!/^[0-9a-f]{40}$/.test(remoteMainCommit || '')) {
    throw new Error(`could not establish the current canonical ${REPOSITORY} main commit`);
  }
  if (commit !== remoteMainCommit) {
    throw new Error(
      `snapshot requires HEAD (${commit}) to equal current canonical main (${remoteMainCommit}). ` +
        `Update local main and retry './arcogine snapshot'.`,
    );
  }
}

function git(args, { allowFailure = false, trim = true } = {}) {
  try {
    const output = execFileSync('git', args, {
      cwd: REPOSITORY_ROOT,
      encoding: 'utf8',
      stdio: ['ignore', 'pipe', 'pipe'],
    });
    return trim ? output.trimEnd() : output;
  } catch (error) {
    if (allowFailure) return '';
    const detail = error?.stderr?.toString().trim();
    throw new Error(`could not inspect Git repository${detail ? `: ${detail}` : ''}`);
  }
}

function resolveCanonicalMainCommit() {
  return parseRemoteMainCommit(git(['ls-remote', '--exit-code', 'origin', CANONICAL_MAIN_REF]));
}

function trackedFiles() {
  return git(['ls-files', '-z'], { trim: false }).split('\0').filter(Boolean);
}

function escapeXml(value) {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&apos;');
}

export function buildTrackedFilesManifest(paths) {
  return [
    `<tracked_files count="${paths.length}">`,
    ...paths.map((path) => `  <path>${escapeXml(path)}</path>`),
    '</tracked_files>',
  ].join('\n');
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
  const result = spawnSync(command, commandArgs, {
    cwd: REPOSITORY_ROOT,
    stdio: 'inherit',
  });

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
  const remoteMainCommit = resolveCanonicalMainCommit();
  validateCanonicalProvenance({ remoteUrl, commit, remoteMainCommit });

  const generatedAt = new Date().toISOString();
  const paths = trackedFiles();
  const manifest = buildTrackedFilesManifest(paths);
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
    const content = `${header}\n\n${manifest}\n\n${repomixContent}`;
    assertHeaderPresent(content, header);
    writeFileSync(outputPath, content, 'utf8');

    const bytes = statSync(outputPath).size;
    return { outputPath, commit, generatedAt, branch, bytes, trackedFileCount: paths.length };
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
    console.log(`Tracked files: ${result.trackedFileCount.toLocaleString('en-US')}`);
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
