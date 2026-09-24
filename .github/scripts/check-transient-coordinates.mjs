#!/usr/bin/env node
import fs from 'node:fs';
import path from 'node:path';
import { execFileSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

export const SELF_FILES = new Set(['.github/scripts/check-transient-coordinates.mjs', '.github/scripts/check-transient-coordinates.test.mjs']);
export const ALLOWED_PREFIXES = ['docs/planning/', 'workspace/'];
export const COORDINATE = /(?<![0-9A-Fa-f])[0-9A-Fa-f]{40}(?![0-9A-Fa-f])[`'"|+,:;(){}\[\]<>\s–—-]*workspace\/(?:[A-Za-z0-9._-]+\/)*[A-Za-z0-9._-]+(?![A-Za-z0-9._/-])/g;
export function trackedFiles(root) {
  return execFileSync('git', ['ls-files'], { cwd: root, encoding: 'utf8' }).split(/\r?\n/).filter(Boolean).map((rel) => rel.replaceAll('\\', '/'));
}
function readUtf8(file) {
  try { return new TextDecoder('utf-8', { fatal: true }).decode(fs.readFileSync(file)); }
  catch { return null; }
}
export function validateFile(rel, text) {
  if (SELF_FILES.has(rel) || ALLOWED_PREFIXES.some((prefix) => rel.startsWith(prefix))) return [];
  const errors = [];
  for (const [index, line] of text.split(/\r?\n/).entries()) for (const match of line.matchAll(COORDINATE)) {
    errors.push(`${rel}:${index + 1}: durable artifact contains a transient workspace coordinate (\`${match[0]}\`); transfer retained meaning to durable state or cite durable delivery-history provenance`);
  }
  return errors;
}
export function check(root) {
  const errors = [];
  for (const rel of trackedFiles(root)) {
    const text = readUtf8(path.join(root, ...rel.split('/')));
    if (text !== null) errors.push(...validateFile(rel, text));
  }
  return errors;
}
function main() {
  const root = path.resolve(process.env.TRANSIENT_COORDINATES_ROOT ?? path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..'));
  let errors;
  try { errors = check(root); } catch (error) { console.error(`Transient-coordinate check failed: ${error.message}`); return 1; }
  if (errors.length) {
    console.error('Transient-coordinate check failed:');
    for (const error of errors) console.error(`- ${error}`);
    console.error('Durable tracked files must not depend on exact SHA + workspace artifact coordinates. Historical SHAs and semantic cases without recognizable coordinate syntax remain allowed and subject to human review.');
    return 1;
  }
  console.log('Transient-coordinate check passed');
  return 0;
}
if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) process.exitCode = main();
