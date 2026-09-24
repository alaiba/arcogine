#!/usr/bin/env node
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const CANONICAL = /(?:^|\n)Reviewed[ \t]+head:[ \t]*`?([a-f0-9]+)`?[ \t]*\nDisposition:[ \t]*\*\*([A-Z][A-Z _-]*)\*\*[\s]*$/;
const VALID = new Set(['READY TO MERGE', 'CHANGES REQUIRED']);

export function evaluate({ head, trusted = false, bodiesBase64 = '' }) {
  if (!head) return { ok: false, message: 'error: PR_HEAD_SHA not set' };
  let latest = '';
  for (const encoded of bodiesBase64.split(/\r?\n/).filter(Boolean)) {
    let body;
    if (!/^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$/.test(encoded)) continue;
    try { body = Buffer.from(encoded, 'base64').toString('utf8'); } catch { continue; }
    const match = body.match(CANONICAL);
    if (!match || match[1] !== head) continue;
    if (!VALID.has(match[2])) return { ok: false, message: `PR disposition gate failed: unsupported disposition '${match[2]}' for current head ${head}.` };
    latest = match[2];
  }
  if (latest === 'CHANGES REQUIRED') return { ok: false, message: 'PR disposition gate failed: latest current-head disposition is CHANGES REQUIRED.' };
  if (trusted === 'true') return { ok: true, message: `PR disposition gate passed: current head ${head} has trusted Dependabot provenance and no current-head CHANGES REQUIRED disposition.` };
  if (!latest) return { ok: false, message: `PR disposition gate failed: no canonical reviewer disposition exists for current head ${head}.` };
  if (latest !== 'READY TO MERGE') return { ok: false, message: `PR disposition gate failed: latest current-head disposition is ${latest}; READY TO MERGE is required.` };
  return { ok: true, message: `PR disposition gate passed: current head ${head} has canonical READY TO MERGE disposition.` };
}

function main() {
  const result = evaluate({ head: process.env.PR_HEAD_SHA, trusted: process.env.PR_TRUSTED_DEPENDABOT ?? 'false', bodiesBase64: process.env.REVIEW_BODIES_B64 ?? '' });
  (result.ok ? console.log : console.error)(result.message);
  return result.ok ? 0 : 1;
}
if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) process.exitCode = main();
