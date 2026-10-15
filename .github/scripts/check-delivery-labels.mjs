#!/usr/bin/env node
import fs from 'node:fs';
import path from 'node:path';
import { execFileSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

export const SELF_FILES = new Set(['.github/scripts/check-delivery-labels.mjs', '.github/scripts/check-delivery-labels.test.mjs']);
const PLAN = /\bPLAN-[A-Za-z0-9]+(?:[-._][A-Za-z0-9]+)*/g;
const REV = /\bREV-[A-Za-z0-9]+(?:[-._][A-Za-z0-9]+)*/g;
const CANONICAL_REV = /^REV-\d+$/;

function matches(text, regex) { return [...text.matchAll(regex)]; }
function readUtf8(file) {
  try { return new TextDecoder('utf-8', { fatal: true }).decode(fs.readFileSync(file)); }
  catch { return null; }
}
export function trackedFiles(root) {
  return execFileSync('git', ['ls-files'], { cwd: root, encoding: 'utf8' }).split(/\r?\n/).filter(Boolean).map((rel) => rel.replaceAll('\\', '/'));
}

export function validateFile(rel, text) {
  if (SELF_FILES.has(rel)) return [];
  const errors = [];
  if (matches(rel, PLAN).length || matches(rel, REV).length) errors.push(`${rel}: file path embeds a reserved PLAN-*/REV-<N> delivery-coordinate token; filenames must remain semantic, never coordinate-derived`);
  for (const [index, line] of text.split(/\r?\n/).entries()) {
    for (const match of matches(line, PLAN)) errors.push(`${rel}:${index + 1}: durable artifact contains a PLAN-* delivery coordinate (\`${match[0]}\`) -- the PLAN-* namespace is retired; name the capability/contract/invariant instead`);
    for (const match of matches(line, REV)) errors.push(CANONICAL_REV.test(match[0])
      ? `${rel}:${index + 1}: durable artifact contains a REV-<N> delivery coordinate (\`${match[0]}\`) -- name the capability/contract/invariant instead`
      : `${rel}:${index + 1}: malformed delivery-coordinate token \`${match[0]}\` -- review/finding identifiers must conform exactly to REV-<digits>`);
  }
  return errors;
}

export function check(root) {
  const errors = [];
  for (const rel of trackedFiles(root)) {
    const absolute = path.join(root, ...rel.split('/'));
    const text = readUtf8(absolute);
    if (text === null) continue;
    errors.push(...validateFile(rel, text));
  }
  return errors;
}

function main() {
  const root = path.resolve(process.env.DELIVERY_LABELS_ROOT ?? path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..'));
  let errors;
  try { errors = check(root); } catch (error) { console.error(`Delivery-label check failed: ${error.message}`); return 1; }
  if (errors.length) {
    console.error('Delivery-label check failed:');
    for (const error of errors) console.error(`- ${error}`);
    console.error('The PLAN-* planning namespace is retired; track work in GitHub issues and refer to them by number in delivery history. PR-local review/finding identifiers use REV-<N> (variable-width decimal, e.g. REV-1, REV-10) in pull-request comments and reviews only. No tracked file -- documentation, code/Javadoc comments, workflow definitions, test/class/file names, or transient workspace material -- may contain either; name the underlying capability, contract, identity, invariant, or behavior instead.');
    return 1;
  }
  console.log('Delivery-label check passed');
  return 0;
}
if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) process.exitCode = main();
