#!/usr/bin/env node
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const INLINE_LINK = /!?\[[^\]]*\]\(([^)]+)\)/g;
const REFERENCE_LINK = /^\s*\[[^\]]+\]:\s*(\S+)/;
const INLINE_CODE = /`[^`]*`/g;
const FENCE = /^\s*(```+|~~~+)/;
const EXTERNAL_SCHEMES = new Set(['http', 'https', 'mailto', 'tel', 'data', 'javascript']);

export function destination(raw) {
  const value = raw.trim();
  if (value.startsWith('<') && value.includes('>')) return value.slice(1, value.indexOf('>'));
  return value.split(/\s+/, 1)[0];
}

export function markdownTargets(text) {
  const result = [];
  let inFence = false;
  let fenceChar = '';
  for (const [index, line] of text.split(/\r?\n/).entries()) {
    const fence = line.match(FENCE);
    if (fence) {
      const char = fence[1][0];
      if (!inFence) { inFence = true; fenceChar = char; }
      else if (char === fenceChar) { inFence = false; fenceChar = ''; }
      continue;
    }
    if (inFence) continue;
    const cleaned = line.replace(INLINE_CODE, '');
    for (const match of cleaned.matchAll(INLINE_LINK)) result.push([index + 1, destination(match[1])]);
    const reference = cleaned.match(REFERENCE_LINK);
    if (reference) result.push([index + 1, destination(reference[1])]);
  }
  return result;
}

export function localCandidate(repo, source, rawTarget) {
  if (!rawTarget || rawTarget.startsWith('#')) return null;
  const schemeMatch = rawTarget.match(/^([A-Za-z][A-Za-z0-9+.-]*):(.*)$/s);
  const scheme = schemeMatch?.[1].toLowerCase() ?? '';
  if (EXTERNAL_SCHEMES.has(scheme)) return null;
  if ((!scheme && rawTarget.startsWith('//')) || (scheme && schemeMatch[2].startsWith('//') && !schemeMatch[2].startsWith('///'))) return null;
  let pathText = schemeMatch ? schemeMatch[2] : rawTarget;
  const suffix = pathText.search(/[?#]/);
  if (suffix >= 0) pathText = pathText.slice(0, suffix);
  try { pathText = decodeURIComponent(pathText); } catch { /* retain malformed escapes, as urlsplit does */ }
  if (!pathText) return null;
  let candidate;
  if (pathText.startsWith('/')) {
    const relative = pathText.replace(/^\/+/, '');
    if (!relative) return null;
    const first = relative.split(/[\\/]/)[0];
    if (!fs.existsSync(path.join(repo, first)) && path.extname(relative).toLowerCase() !== '.md') return null;
    candidate = path.join(repo, relative);
  } else candidate = path.resolve(path.dirname(source), pathText.replace(/^\/+/, ''));
  const resolved = path.resolve(candidate);
  const rel = path.relative(repo, resolved);
  if (rel === '..' || rel.startsWith(`..${path.sep}`) || path.isAbsolute(rel)) return resolved;
  return resolved;
}

function walkMarkdown(root) {
  const files = [];
  const walk = (dir) => {
    for (const entry of fs.readdirSync(dir, { withFileTypes: true }).sort((a, b) => a.name < b.name ? -1 : a.name > b.name ? 1 : 0)) {
      if (entry.name === '.git') continue;
      const full = path.join(dir, entry.name);
      if (entry.isDirectory()) walk(full);
      else if (entry.isFile() && entry.name.endsWith('.md')) files.push(full);
    }
  };
  walk(root);
  return files.sort((a, b) => {
    const left = path.relative(root, a).split(path.sep).join('/');
    const right = path.relative(root, b).split(path.sep).join('/');
    return left < right ? -1 : left > right ? 1 : 0;
  });
}

function quotedTarget(value) {
  const quote = value.includes("'") && !value.includes('"') ? '"' : "'";
  return `${quote}${value.replaceAll('\\', '\\\\').replaceAll(quote, `\\${quote}`).replaceAll('\n', '\\n').replaceAll('\r', '\\r').replaceAll('\t', '\\t')}${quote}`;
}

function readUtf8(file) {
  try { return new TextDecoder('utf-8', { fatal: true }).decode(fs.readFileSync(file)); }
  catch { return null; }
}

export function check(repo) {
  const failures = [];
  for (const source of walkMarkdown(repo)) {
    const text = readUtf8(source);
    if (text === null) continue;
    for (const [line, target] of markdownTargets(text)) {
      const candidate = localCandidate(repo, source, target);
      if (candidate !== null && !fs.existsSync(candidate)) {
        const display = path.relative(repo, candidate) || candidate;
        failures.push(`${path.relative(repo, source).split(path.sep).join('/')}:${line}: broken local link ${quotedTarget(target)} -> ${display.split(path.sep).join('/')}`);
      }
    }
  }
  return failures;
}

function main() {
  const repo = path.resolve(process.argv[2] ?? '.');
  const failures = check(repo);
  if (failures.length) {
    console.error('Broken repository-local Markdown links found:');
    for (const failure of failures) console.error(`  ${failure}`);
    return 1;
  }
  console.log('Repository-local Markdown links are valid.');
  return 0;
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) process.exitCode = main();
