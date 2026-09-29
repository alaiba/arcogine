#!/usr/bin/env node
import fs from 'node:fs';
import path from 'node:path';
import { execFileSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

export const PRODUCT_PREFIX = 'product/';
export const TRANSIENT_AUTHORITY = /(?:docs[\\/](?:planning|research)[\\/]|workspace[\\/]research[\\/])/g;

function readUtf8(file) {
  try {
    return new TextDecoder('utf-8', { fatal: true }).decode(fs.readFileSync(file));
  } catch {
    return null;
  }
}

export function trackedFiles(root) {
  return execFileSync('git', ['ls-files'], { cwd: root, encoding: 'utf8' })
    .split(/\r?\n/)
    .filter(Boolean)
    .map((rel) => rel.replaceAll('\\', '/'));
}

export function validateFile(rel, text) {
  if (!rel.startsWith(PRODUCT_PREFIX)) return [];

  const errors = [];
  for (const [index, line] of text.split(/\r?\n/).entries()) {
    for (const match of line.matchAll(TRANSIENT_AUTHORITY)) {
      errors.push(
        `${rel}:${index + 1}: product source/test references transient planning/research material (${match[0]}) -- state the durable capability/contract/invariant directly or reference its durable authority instead`,
      );
    }
  }
  return errors;
}

export function check(root) {
  const errors = [];
  for (const rel of trackedFiles(root)) {
    const text = readUtf8(path.join(root, ...rel.split('/')));
    if (text === null) continue;
    errors.push(...validateFile(rel, text));
  }
  return errors;
}

function main() {
  const root = path.resolve(
    process.env.SOURCE_AUTHORITY_LINKS_ROOT ?? path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..'),
  );
  let errors;
  try {
    errors = check(root);
  } catch (error) {
    console.error(`Source-authority-link check failed: ${error.message}`);
    return 1;
  }
  if (errors.length) {
    console.error('Source-authority-link check failed:');
    for (const error of errors) console.error(`- ${error}`);
    console.error(
      'Product source and tests are durable: planning/research may cite implementation as evidence, but product code must not depend on docs/planning/, docs/research/, or workspace/research/ material.',
    );
    return 1;
  }
  console.log('Source-authority-link check passed');
  return 0;
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) process.exitCode = main();
