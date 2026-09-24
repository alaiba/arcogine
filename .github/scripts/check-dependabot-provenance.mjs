#!/usr/bin/env node
import path from 'node:path';
import { fileURLToPath } from 'node:url';

export const DEPENDABOT = Object.freeze({ login: 'dependabot[bot]', type: 'Bot', id: 49699333 });
export function verify({ number, head, authorLogin, authorType, authorId, runBase64 }) {
  if (!number || !head) return { ok: false, message: 'Dependabot provenance failed: PR_NUMBER and PR_HEAD_SHA are required.' };
  if (authorLogin !== DEPENDABOT.login || authorType !== DEPENDABOT.type || String(authorId) !== String(DEPENDABOT.id)) return { ok: false, message: "Dependabot provenance failed: PR opener is not GitHub's trusted Dependabot account." };
  if (!runBase64) return { ok: false, message: 'Dependabot provenance failed: exact-head CI run is absent.' };
  let run;
  if (!/^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$/.test(runBase64)) return { ok: false, message: 'Dependabot provenance failed: CI run is not valid base64.' };
  try { run = JSON.parse(Buffer.from(runBase64, 'base64').toString('utf8')); } catch { return { ok: false, message: 'Dependabot provenance failed: CI run is not valid base64.' }; }
  const actor = run?.actor;
  const associated = Array.isArray(run?.pull_requests) && run.pull_requests.some((pr) => pr?.number === Number(number));
  if (run?.name !== 'CI' || run?.event !== 'pull_request' || run?.head_sha !== head || actor?.login !== DEPENDABOT.login || actor?.type !== DEPENDABOT.type || actor?.id !== DEPENDABOT.id || !associated) return { ok: false, message: 'Dependabot provenance failed: exact-head CI was not initiated by the trusted Dependabot account for this PR.' };
  return { ok: true, message: `Dependabot provenance passed: PR #${number} head ${head} was opened and current-head CI was initiated by GitHub Dependabot.` };
}
function main() {
  const result = verify({ number: process.env.PR_NUMBER, head: process.env.PR_HEAD_SHA, authorLogin: process.env.PR_AUTHOR_LOGIN, authorType: process.env.PR_AUTHOR_TYPE, authorId: process.env.PR_AUTHOR_ID, runBase64: process.env.PR_HEAD_CI_RUN_B64 });
  (result.ok ? console.log : console.error)(result.message);
  return result.ok ? 0 : 1;
}
if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) process.exitCode = main();
