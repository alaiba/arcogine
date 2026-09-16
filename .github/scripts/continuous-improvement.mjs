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

const SHA_RE = /^[0-9a-f]{40}$/;
const DATE_RE = /^\d{4}-\d{2}-\d{2}$/;

export function daysBetween(aISO, bISO) {
  return (new Date(bISO).getTime() - new Date(aISO).getTime()) / (1000 * 60 * 60 * 24);
}

export function deriveWeeklyState(lastVerifiedDate, nowISO) {
  if (!lastVerifiedDate) return 'DUE';
  const age = daysBetween(`${lastVerifiedDate}T00:00:00Z`, nowISO);
  if (age <= WEEKLY_INTERVAL_DAYS) return 'CURRENT';
  if (age <= WEEKLY_OVERDUE_DAYS) return 'DUE';
  return 'OVERDUE';
}

function parseIssueList(raw) {
  if (raw === 'none') return [];
  if (raw === 'n/a') return null;
  const parts = raw.split(',').map((part) => part.trim());
  if (!parts.length || parts.some((part) => !/^#[1-9][0-9]*$/.test(part))) {
    throw new Error('malformed weekly Consistency finding issue list');
  }
  const numbers = parts.map((part) => Number(part.slice(1)));
  if (new Set(numbers).size !== numbers.length) {
    throw new Error('duplicate weekly Consistency finding issue reference');
  }
  return numbers;
}

function sectionBetween(text, startHeading, endHeading) {
  const start = text.indexOf(startHeading);
  const end = text.indexOf(endHeading);
  if (start < 0 || end < 0 || end <= start) {
    throw new Error(`missing or malformed section: ${startHeading}`);
  }
  return text.slice(start + startHeading.length, end);
}

function bulletValue(section, label) {
  const prefix = `- ${label}: `;
  const matches = section
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter((line) => line.startsWith(prefix));
  if (matches.length !== 1) throw new Error(`expected exactly one weekly Consistency field: ${label}`);
  return matches[0].slice(prefix.length).trim();
}

export function splitMarkers(body) {
  const firstStart = body.indexOf(MARKER_START);
  const firstEnd = body.indexOf(MARKER_END);
  if (firstStart < 0 || firstEnd < 0 || firstEnd <= firstStart) {
    throw new Error('register markers are missing or malformed');
  }
  if (body.indexOf(MARKER_START, firstStart + MARKER_START.length) >= 0) {
    throw new Error('register start marker is duplicated');
  }
  if (body.indexOf(MARKER_END, firstEnd + MARKER_END.length) >= 0) {
    throw new Error('register end marker is duplicated');
  }
  return {
    before: body.slice(0, firstStart),
    current: body.slice(firstStart + MARKER_START.length, firstEnd).replace(/^\n+|\n+$/g, ''),
    after: body.slice(firstEnd + MARKER_END.length),
  };
}

export function parseWeeklyRecord(body) {
  const { current } = splitMarkers(body);
  const section = sectionBetween(current, '### Weekly Consistency review', '### Delivery-process retrospective');
  const lastVerifiedRaw = bulletValue(section, 'last verified');
  const reviewedHeadRaw = bulletValue(section, 'reviewed head');
  const resultRaw = bulletValue(section, 'accounted result');
  const issuesRaw = bulletValue(section, 'finding issues');

  if (lastVerifiedRaw === 'never') {
    if (reviewedHeadRaw !== 'n/a' || resultRaw !== 'n/a' || issuesRaw !== 'n/a') {
      throw new Error('unverified weekly Consistency record has contradictory accounted fields');
    }
    return { lastVerified: null, reviewedHead: null, result: null, findingIssueNumbers: [] };
  }

  if (!DATE_RE.test(lastVerifiedRaw) || Number.isNaN(new Date(`${lastVerifiedRaw}T00:00:00Z`).getTime())) {
    throw new Error('weekly Consistency last verified must be UTC YYYY-MM-DD');
  }
  if (!SHA_RE.test(reviewedHeadRaw)) throw new Error('weekly Consistency reviewed head must be a full SHA');
  if (!['CLEAN', 'FINDINGS'].includes(resultRaw)) throw new Error('weekly Consistency accounted result must be CLEAN or FINDINGS');

  const findingIssueNumbers = parseIssueList(issuesRaw);
  if (findingIssueNumbers === null) throw new Error('verified weekly Consistency record cannot use n/a finding issues');
  if (resultRaw === 'CLEAN' && findingIssueNumbers.length !== 0) {
    throw new Error('CLEAN weekly Consistency record must have finding issues: none');
  }
  if (resultRaw === 'FINDINGS' && findingIssueNumbers.length === 0) {
    throw new Error('FINDINGS weekly Consistency record must cite at least one issue');
  }

  return {
    lastVerified: lastVerifiedRaw,
    reviewedHead: reviewedHeadRaw,
    result: resultRaw,
    findingIssueNumbers,
  };
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

export function renderObligations({ weekly, retrospective, nowISO }) {
  const findingIssues = weekly.lastVerified
    ? (weekly.findingIssueNumbers.length ? weekly.findingIssueNumbers.map((n) => `#${n}`).join(', ') : 'none')
    : 'n/a';
  return [
    '## Recurring obligations',
    '',
    'See [docs/development/continuous-improvement.md](../blob/main/docs/development/continuous-improvement.md) for semantics and ownership.',
    '',
    '### Weekly Consistency review',
    '',
    `- last verified: ${weekly.lastVerified ?? 'never'}`,
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
    `_Last updated: ${nowISO}_`,
  ].join('\n');
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
  const weeklyRecord = parseWeeklyRecord(register.body ?? '');
  const rawMergedSinceBaseline = await countMergedPullsSince(repo, token, data.baselinePr);

  const weekly = {
    ...weeklyRecord,
    state: deriveWeeklyState(weeklyRecord.lastVerified, nowISO),
  };
  const retrospective = {
    ...data,
    rawMergedSinceBaseline,
    state: deriveRetrospectiveState({ rawMergedSinceBaseline, ...data }),
  };
  const obligations = renderObligations({ weekly, retrospective, nowISO });

  if (values['dry-run']) {
    console.log(obligations);
    return;
  }
  if (!obligationsChanged(register.body ?? '', obligations)) return;

  await githubJson(`https://api.github.com/repos/${repo}/issues/${REGISTER_ISSUE_NUMBER}`, {
    token,
    method: 'PATCH',
    body: { body: mergeRegisterBody(register.body ?? '', obligations) },
  });
}

const invokedPath = process.argv[1] && pathToFileURL(process.argv[1]).href;
if (invokedPath === import.meta.url) {
  main().catch((error) => {
    console.error(error);
    process.exitCode = 1;
  });
}
