#!/usr/bin/env node
import fs from 'node:fs';
import path from 'node:path';
import { execFileSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

export const PLANNING_DIR = 'docs/planning/';
export const SELF_FILES = new Set(['.github/scripts/check-delivery-labels.mjs', '.github/scripts/check-delivery-labels.test.mjs']);
const PLAN = /\bPLAN-[A-Za-z0-9]+(?:[-._][A-Za-z0-9]+)*/g;
const REV = /\bREV-[A-Za-z0-9]+(?:[-._][A-Za-z0-9]+)*/g;
const CANONICAL_PLAN = /^PLAN-[A-Z]+(?:-[A-Za-z0-9]+)+$/;
const CANONICAL_REV = /^REV-\d+$/;
const LEGACY = [/\bGate\s?\d+\b/g, /\bG\d+(?:\.\d+)?(?:-[A-Za-z0-9]+)?\b/g, /\bD\d+\b/g, /\bC\d+\b/g, /\bO\d+\b/g, /\bW1\b/g, /\bDH-[A-Z]\b/g];
const LEGACY_FILENAME = /(?:^|[-_])(?:gate-?\d+|g\d+(?:[.-]\d+)?|d\d+|c\d+|o\d+|w1|dh-[a-z])(?:[-_]|$)/i;

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
  const inPlanning = rel.startsWith(PLANNING_DIR);
  if (matches(rel, PLAN).length || matches(rel, REV).length) errors.push(`${rel}: file path embeds a reserved PLAN-*/REV-<N> delivery-coordinate token; filenames must remain semantic, never coordinate-derived`);
  else if (inPlanning && LEGACY_FILENAME.test(path.posix.basename(rel))) errors.push(`${rel}: planning filename still embeds a legacy delivery-coordinate form; planning filenames should be semantic (the label belongs in the content, not the path)`);
  for (const [index, line] of text.split(/\r?\n/).entries()) {
    if (!inPlanning) {
      for (const match of matches(line, PLAN)) errors.push(`${rel}:${index + 1}: durable artifact contains a PLAN-* delivery coordinate (\`${match[0]}\`) -- name the capability/contract/invariant instead`);
      for (const match of matches(line, REV)) errors.push(CANONICAL_REV.test(match[0])
        ? `${rel}:${index + 1}: durable artifact contains a REV-<N> delivery coordinate (\`${match[0]}\`) -- name the capability/contract/invariant instead`
        : `${rel}:${index + 1}: malformed delivery-coordinate token \`${match[0]}\` -- review/finding identifiers must conform exactly to REV-<digits>`);
      continue;
    }
    let masked = line;
    for (const match of matches(line, PLAN)) {
      const token = match[0];
      if (CANONICAL_PLAN.test(token)) masked = masked.slice(0, match.index) + ' '.repeat(token.length) + masked.slice(match.index + token.length);
      else errors.push(`${rel}:${index + 1}: malformed delivery-coordinate token \`${token}\` -- planning coordinates must conform exactly to PLAN-<TRACK>-<LOCAL-ID> (uppercase-ASCII track, hyphen-separated segments)`);
    }
    for (const match of matches(line, REV)) {
      const token = match[0];
      if (CANONICAL_REV.test(token)) masked = masked.slice(0, match.index) + ' '.repeat(token.length) + masked.slice(match.index + token.length);
      else errors.push(`${rel}:${index + 1}: malformed delivery-coordinate token \`${token}\` -- review/finding identifiers must conform exactly to REV-<digits>`);
    }
    for (const pattern of LEGACY) for (const match of matches(masked, pattern)) errors.push(`${rel}:${index + 1}: legacy delivery-coordinate form \`${match[0]}\` -- use the canonical PLAN-<TRACK>-<LOCAL-ID> namespace instead`);
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
    console.error('Planning coordinates use PLAN-<TRACK>-<LOCAL-ID>; PR-local review/finding identifiers use REV-<N> (variable-width decimal, e.g. REV-1, REV-9, REV-10). Both are allowed in docs/planning/ and active delivery history (issues, PRs, reviews, branches, commits, handoffs); durable artifacts (documentation, code/Javadoc comments, workflow definitions, test/class/file names) must name the underlying capability, contract, identity, invariant, or behavior instead. Legacy ambiguous forms (Gate 4, G1.3, D1, C1, O1, W1, DH-E, ...) must not be reintroduced into planning material.');
    return 1;
  }
  console.log('Delivery-label check passed');
  return 0;
}
if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) process.exitCode = main();
