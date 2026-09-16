#!/usr/bin/env node
import { readFile } from 'node:fs/promises';
import { parseArgs } from 'node:util';
import { pathToFileURL } from 'node:url';

export const DEFAULT_REPO = 'alaiba/arcogine';
export const REGISTER_ISSUE_NUMBER = 295;
export const REGISTER_TITLE = 'Continuous improvement register';
export const MARKER_START = '<!-- continuous-improvement:obligations:start -->';
export const MARKER_END = '<!-- continuous-improvement:obligations:end -->';
export const WEEKLY_INTERVAL_DAYS = 7;
export const WEEKLY_OVERDUE_DAYS = 14;
export const RETROSPECTIVE_GUARD_THRESHOLD = 25;
export const TRUSTED_COMPLETION_ASSOCIATIONS = new Set(['OWNER', 'MEMBER', 'COLLABORATOR']);

const SHA_RE = /^[0-9a-f]{40}$/;

export function daysBetween(aISO, bISO) {
  return (new Date(bISO).getTime() - new Date(aISO).getTime()) / (1000 * 60 * 60 * 24);
}

export function deriveWeeklyState(lastVerifiedISO, nowISO) {
  if (!lastVerifiedISO) return 'DUE';
  const age = daysBetween(lastVerifiedISO, nowISO);
  if (age <= WEEKLY_INTERVAL_DAYS) return 'CURRENT';
  if (age <= WEEKLY_OVERDUE_DAYS) return 'DUE';
  return 'OVERDUE';
}

function parseFindingIssueList(raw) {
  if (/^none$/i.test(raw)) return [];
  const parts = raw.split(',').map((part) => part.trim());
  if (parts.length === 0 || parts.some((part) => !/^#[1-9][0-9]*$/.test(part))) return null;
  const numbers = parts.map((part) => Number(part.slice(1)));
  if (numbers.some((number) => !Number.isSafeInteger(number))) return null;
  if (new Set(numbers).size !== numbers.length) return null;
  return numbers;
}

export function parseCompletionComment(body) {
  if (typeof body !== 'string') return null;
  const lines = body.trim().split(/\r?\n/).map((line) => line.trim()).filter(Boolean);
  if (lines.length !== 4 || lines[0] !== 'Consistency review completed') return null;

  const head = /^head:\s*([0-9a-f]{40})$/i.exec(lines[1])?.[1]?.toLowerCase();
  const scope = /^scope:\s*(FULL|INCREMENTAL)$/i.exec(lines[2])?.[1]?.toUpperCase();
  const findingsRaw = /^findings:\s*(.+)$/i.exec(lines[3])?.[1]?.trim();
  if (!head || !SHA_RE.test(head) || !scope || !findingsRaw) return null;

  const findingIssueNumbers = parseFindingIssueList(findingsRaw);
  if (!findingIssueNumbers) return null;
  return {
    reviewedHead: head,
    scope,
    findingIssueNumbers,
    result: findingIssueNumbers.length ? 'FINDINGS' : 'CLEAN',
  };
}

const LEGACY_CONSISTENCY_ALIASES = new Map([
  [204, 'CONS-001'],
  [205, 'CONS-002'],
  [206, 'CONS-003'],
  [207, 'CONS-004'],
  [208, 'CONS-005'],
  [209, 'CONS-006'],
]);

export function isPersistedConsistencyFinding(issue, expectedNumber) {
  if (!issue || issue.number !== expectedNumber || issue.pull_request) return false;

  const title = issue.title ?? '';
  const currentTitle = /^CONS:\s+\S/.test(title);
  const historical = /^CONS-([0-9]+):\s+\S/.exec(title);
  if (!currentTitle && !historical) return false;
  if (historical) {
    const alias = `CONS-${historical[1]}`;
    const legacy = LEGACY_CONSISTENCY_ALIASES.get(expectedNumber);
    if (legacy ? alias !== legacy : Number(historical[1]) !== expectedNumber) return false;
  }

  const body = issue.body ?? '';
  return /^Severity:\s*(?:P0|P1|P2|P3|Nit)\s*$/m.test(body) && /^Category:\s*\S.+$/m.test(body);
}

function findingIssueMap(findingIssues) {
  if (findingIssues instanceof Map) return findingIssues;
  return new Map((findingIssues ?? []).map((issue) => [issue.number, issue]));
}

export function isAccountedCompletion(completion, findingIssues = new Map()) {
  if (!completion) return false;
  if (completion.findingIssueNumbers.length === 0) return true;
  const issues = findingIssueMap(findingIssues);
  return completion.findingIssueNumbers.every((number) => {
    const issue = issues.get(number);
    return issue?.state === 'open' && isPersistedConsistencyFinding(issue, number);
  });
}

export function latestValidCompletion(comments, nowISO, findingIssues = new Map()) {
  const now = new Date(nowISO).getTime();
  const valid = [];

  for (const comment of comments ?? []) {
    const parsed = parseCompletionComment(comment.body);
    if (!parsed) continue;
    const association = comment.authorAssociation ?? comment.author_association;
    if (!TRUSTED_COMPLETION_ASSOCIATIONS.has(association)) continue;
    const createdAtRaw = comment.createdAt ?? comment.created_at;
    const createdAt = new Date(createdAtRaw);
    if (Number.isNaN(createdAt.getTime()) || createdAt.getTime() > now) continue;
    if (!isAccountedCompletion(parsed, findingIssues)) continue;
    valid.push({ ...parsed, completedAt: createdAt.toISOString() });
  }

  valid.sort((a, b) => a.completedAt.localeCompare(b.completedAt));
  let baselineEstablished = false;
  let latest = null;
  for (const completion of valid) {
    if (completion.scope === 'FULL') {
      baselineEstablished = true;
      latest = completion;
    } else if (baselineEstablished) {
      latest = completion;
    }
  }
  return latest;
}

export function deriveRetrospectiveState({
  rawMergedSinceBaseline,
  escapeEvidenceCount = 0,
  p1LifecycleEscape = false,
  guardThreshold = RETROSPECTIVE_GUARD_THRESHOLD,
}) {
  if (rawMergedSinceBaseline >= guardThreshold) return 'CHECK_TRIGGER';
  if (escapeEvidenceCount >= 2) return 'CHECK_TRIGGER';
  if (p1LifecycleEscape) return 'CHECK_TRIGGER';
  return 'CURRENT';
}

export function renderObligations({ weekly, retrospective }) {
  const findingIssues = weekly.result
    ? (weekly.findingIssueNumbers.length ? weekly.findingIssueNumbers.map((n) => `#${n}`).join(', ') : 'none')
    : 'n/a';
  return [
    '## Recurring obligations',
    '',
    'Derived by `.github/workflows/continuous-improvement.yml` via `.github/scripts/continuous-improvement.mjs`.',
    'See [docs/development/continuous-improvement.md](../blob/main/docs/development/continuous-improvement.md) for semantics and ownership.',
    '',
    '### Weekly Consistency review',
    '',
    `- last verified: ${weekly.lastVerifiedAt ?? 'never'}`,
    `- reviewed head: ${weekly.reviewedHead ?? 'n/a'}`,
    `- accounted result: ${weekly.result ?? 'n/a'}`,
    `- finding issues: ${findingIssues}`,
    `- next due / interval: every ${WEEKLY_INTERVAL_DAYS} days`,
    `- state: **${weekly.state}**`,
    '',
    '### Delivery-process retrospective',
    '',
    `- last baseline: PR #${retrospective.baselinePr} (${retrospective.baselineDate})`,
    `- raw merged PRs since baseline: ${retrospective.rawMergedSinceBaseline}`,
    `- escape evidence recorded: ${retrospective.escapeEvidenceCount} post-merge escape(s), P1 lifecycle escape: ${retrospective.p1LifecycleEscape}`,
    `- state: **${retrospective.state}**`,
    '',
    `_Last updated: ${weekly.nowISO}_`,
  ].join('\n');
}

export function splitMarkers(body) {
  const firstStart = body.indexOf(MARKER_START);
  const firstEnd = body.indexOf(MARKER_END);
  if (firstStart < 0 || firstEnd < 0 || firstEnd <= firstStart) throw new Error('register markers are missing or malformed');
  if (body.indexOf(MARKER_START, firstStart + MARKER_START.length) >= 0) throw new Error('register start marker is duplicated');
  if (body.indexOf(MARKER_END, firstEnd + MARKER_END.length) >= 0) throw new Error('register end marker is duplicated');
  return {
    before: body.slice(0, firstStart),
    current: body.slice(firstStart + MARKER_START.length, firstEnd).replace(/^\n+|\n+$/g, ''),
    after: body.slice(firstEnd + MARKER_END.length),
  };
}

export function mergeRegisterBody(body, obligationsText) {
  const { before, after } = splitMarkers(body);
  return `${before}${MARKER_START}\n\n${obligationsText}\n\n${MARKER_END}${after}`;
}

function semanticObligations(text) {
  return text.split('\n').filter((line) => !/^_Last updated: .*_$/.test(line)).join('\n').trim();
}

export function obligationsChanged(body, obligationsText) {
  return semanticObligations(splitMarkers(body).current) !== semanticObligations(obligationsText);
}

async function githubJson(url, { token, method = 'GET', body } = {}) {
  const response = await fetch(url, {
    method,
    headers: {
      Accept: 'application/vnd.github+json',
      Authorization: `Bearer ${token}`,
      'X-GitHub-Api-Version': '2022-11-28',
      'User-Agent': 'arcogine-continuous-improvement',
      ...(body ? { 'Content-Type': 'application/json' } : {}),
    },
    ...(body ? { body: JSON.stringify(body) } : {}),
  });
  if (!response.ok) throw new Error(`${method} ${url} failed: ${response.status} ${await response.text()}`);
  if (response.status === 204) return null;
  return response.json();
}

async function fetchAllPages(url, token) {
  const out = [];
  for (let page = 1; ; page += 1) {
    const separator = url.includes('?') ? '&' : '?';
    const batch = await githubJson(`${url}${separator}per_page=100&page=${page}`, { token });
    out.push(...batch);
    if (batch.length < 100) return out;
  }
}

async function loadData() {
  const raw = await readFile(new URL('./continuous-improvement-data.json', import.meta.url), 'utf8');
  return JSON.parse(raw);
}

async function fetchRegister(repo, token) {
  const issue = await githubJson(`https://api.github.com/repos/${repo}/issues/${REGISTER_ISSUE_NUMBER}`, { token });
  if (issue.pull_request || issue.title !== REGISTER_TITLE) {
    throw new Error(`issue #${REGISTER_ISSUE_NUMBER} is not the expected ${REGISTER_TITLE}`);
  }
  return issue;
}

async function fetchFindingIssues(repo, token, comments) {
  const numbers = new Set();
  for (const comment of comments) {
    const parsed = parseCompletionComment(comment.body);
    for (const number of parsed?.findingIssueNumbers ?? []) numbers.add(number);
  }
  const issues = new Map();
  for (const number of numbers) {
    try {
      const issue = await githubJson(`https://api.github.com/repos/${repo}/issues/${number}`, { token });
      issues.set(number, issue);
    } catch (error) {
      console.warn(`Could not load finding issue #${number}: ${error.message}`);
    }
  }
  return issues;
}

async function countMergedPullsSince(repo, token, baselinePr) {
  const pulls = await fetchAllPages(`https://api.github.com/repos/${repo}/pulls?state=closed&sort=created&direction=asc`, token);
  return pulls.filter((pr) => pr.number > baselinePr && pr.merged_at).length;
}

export async function main() {
  const { values } = parseArgs({ options: { 'dry-run': { type: 'boolean', default: false } } });
  const token = process.env.GH_TOKEN ?? process.env.GITHUB_TOKEN;
  if (!token) throw new Error('GH_TOKEN or GITHUB_TOKEN is required');
  const repo = process.env.GITHUB_REPOSITORY ?? DEFAULT_REPO;
  const nowISO = new Date().toISOString();
  const data = await loadData();

  const register = await fetchRegister(repo, token);
  const comments = await fetchAllPages(`https://api.github.com/repos/${repo}/issues/${REGISTER_ISSUE_NUMBER}/comments`, token);
  const findingIssues = await fetchFindingIssues(repo, token, comments);
  const completion = latestValidCompletion(comments, nowISO, findingIssues);
  const rawMergedSinceBaseline = await countMergedPullsSince(repo, token, data.baselinePr);

  const weekly = {
    nowISO,
    lastVerifiedAt: completion?.completedAt ?? null,
    reviewedHead: completion?.reviewedHead ?? null,
    result: completion?.result ?? null,
    findingIssueNumbers: completion?.findingIssueNumbers ?? [],
    state: deriveWeeklyState(completion?.completedAt ?? null, nowISO),
  };
  const retrospective = {
    ...data,
    rawMergedSinceBaseline,
    state: deriveRetrospectiveState({ rawMergedSinceBaseline, ...data }),
  };
  const obligations = renderObligations({ weekly, retrospective });

  if (values['dry-run']) {
    console.log(obligations);
    return;
  }
  if (!obligationsChanged(register.body ?? '', obligations)) return;

  const body = mergeRegisterBody(register.body ?? '', obligations);
  await githubJson(`https://api.github.com/repos/${repo}/issues/${REGISTER_ISSUE_NUMBER}`, {
    token,
    method: 'PATCH',
    body: { body },
  });
}

const invokedPath = process.argv[1] && pathToFileURL(process.argv[1]).href;
if (invokedPath === import.meta.url) {
  main().catch((error) => {
    console.error(error);
    process.exitCode = 1;
  });
}
