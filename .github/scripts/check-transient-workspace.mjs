#!/usr/bin/env node
import path from 'node:path';
import { execFileSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

export function trackedWorkspaceFiles(root) {
  return execFileSync('git', ['ls-files', '--', 'workspace/'], { cwd: root, encoding: 'utf8' }).split(/\r?\n/).filter(Boolean);
}
function main() {
  let files;
  try {
    const root = path.resolve(process.env.TRANSIENT_WORKSPACE_ROOT ?? execFileSync('git', ['rev-parse', '--show-toplevel'], { encoding: 'utf8' }).trim());
    files = trackedWorkspaceFiles(root);
  } catch (error) { console.error(`Transient workspace check failed: ${error.message}`); return 1; }
  if (files.length) {
    console.error('Tracked files under reserved transient workspace/ are not allowed:');
    for (const file of files) console.error(`  ${file}`);
    return 1;
  }
  console.log('No tracked files under reserved transient workspace/.');
  return 0;
}
if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) process.exitCode = main();
