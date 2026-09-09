#!/usr/bin/env node
/**
 * continuous-improvement.mjs -- bootstrap/update Arcogine's continuous-improvement
 * register issue and derive recurring-obligation state.
 *
 * This script is the executable half of docs/development/continuous-improvement.md.
 * It never runs the Consistency agent, a delivery-process retrospective, or any other
 * judgment-bearing improvement work: it only makes recurring obligations and their due
 * state visible, and preserves the agent/human-managed intervention section untouched.
 *
 * Pure logic (state derivation, marker parsing/rendering, body merging) is exported
 * and covered by continuous-improvement.test.mjs without any network access. GitHub
 * I/O lives only in the functions below `--- GitHub I/O ---` and in main().
 *
 *   node .github/scripts/continuous-improvement.mjs           # bootstrap/update the register
 *   node .github/scripts/continuous-improvement.mjs --dry-run # compute state, print, no writes
 *
 * Auth: GH_TOKEN or GITHUB_TOKEN must be set (the workflow provides GITHUB_TOKEN).
 */

import { parseArgs } from 'node:util';
import { pathToFileURL } from 'node:url';

export const DEFAULT_REPO = 'alaiba/arcogine';
export const REGISTER_TITLE = 'Continuous improvement register';
export const MARKER_START = '<!-- continuous-improvement:obligations:start -->';
export const MARKER_END = '<!-- continuous-improvement:obligations:end -->';
export const WEEKLY_INTERVAL_DAYS = 7;
export const WEEKLY_OVERDUE_DAYS = 14;
export const RETROSPECTIVE_GUARD_THRESHOLD = 25;

// ------------------------------- pure logic -------------------------------

/** Days between two ISO-8601 timestamps (b - a), fractional. */
export function daysBetween(aISO, bISO) {
  return (new Date(bISO).getTime() - new Date(aISO).getTime()) / (1000 * 60 * 60 * 24);
}

/**
 * Derive weekly Consistency-review obligation state from the last valid completion
 * evidence. `lastVerifiedISO` is null when no valid completion evidence exists yet
 * (register just bootstrapped) -- treated as DUE, not OVERDUE, so a brand-new
 * register does not immediately read as overdue.
 */
export function deriveWeeklyState(lastVerifiedISO, nowISO) {
  if (!lastVerifiedISO) return 'DUE';
  const age = daysBetween(lastVerifiedISO, nowISO);
  if (age <= WEEKLY_INTERVAL_DAYS) return 'CURRENT';
  if (age <= WEEKLY_OVERDUE_DAYS) return 'DUE';
  return 'OVERDUE';
}

const SHA_RE = /^[0-9a-f]{40}$/;

/**
 * Parse one completion-evidence comment. Returns null when the comment does not
 * match the required structured format (malformed/unrelated comments are ignored,
 * never treated as completion proof).
 */
export function parseCompletionComment(body) {
  if (typeof body !== 'string') return null;
  if (!/^\s*Consistency review completed\s*$/m.test(body)) return null;

  const head = /reviewed head:\s*(\S+)/i.exec(body);
  const at = /completed at:\s*(\S+)/i.exec(body);
  const mode = /mode:\s*(\S+)/i.exec(body);

  if (!head || !SHA_RE.test(head[1])) return null;
  if (!at) return null;
  const parsed = new Date(at[1]);
  if (Number.isNaN(parsed.getTime())) return null;

  return {
    reviewedHead: head[1],
    completedAt: parsed.toISOString(),
    mode: mode ? mode[1] : 'unspecified',
  };
}

/**
 * Pick the most recent valid completion-evidence comment out of a list of raw
 * comment bodies. Malformed comments are ignored rather than rejected as errors --
 * a stray or corrupted comment must never crash the run, only fail to count as
 * evidence.
 */
export function latestValidCompletion(commentBodies) {
  let best = null;
  for (const body of commentBodies) {
    const parsed = parseCompletionComment(body);
    if (!parsed) continue;
    if (!best || parsed.completedAt > best.completedAt) best = parsed;
  }
  return best;
}

/**
 * Derive the delivery-process-retrospective trigger state. Raw merged-PR count is a
 * mechanical guard only: reaching it produces CHECK_TRIGGER (a human/agent must judge
 * whether the *substantive* threshold in docs/development/continuous-improvement.md
 * actually fired), never an automatic DUE for the retrospective itself. Explicit
 * repository-recorded escape evidence (see data file) can also surface CHECK_TRIGGER,
 * but this function never classifies PR history itself.
 */
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

/** Render the workflow-owned obligations block (without the marker comments). */
export function renderObligations({ weekly, retrospective }) {
  const lines = [];
  lines.push('## Recurring obligations');
  lines.push('');
  lines.push('Derived by `.github/workflows/continuous-improvement.yml` via `.github/scripts/continuous-improvement.mjs`.');
  lines.push('See [docs/development/continuous-improvement.md](../blob/main/docs/development/continuous-improvement.md) for what these states mean and who acts on them.');
  lines.push('');
  lines.push('### Weekly Consistency review');
  lines.push('');
  lines.push(`- last verified: ${weekly.lastVerifiedAt ?? 'never'}`);
  lines.push(`- reviewed head: ${weekly.reviewedHead ?? 'n/a'}`);
  lines.push(`- next due / interval: every ${WEEKLY_INTERVAL_DAYS} days`);
  lines.push(`- state: **${weekly.state}**`);
  lines.push('');
  lines.push('### Delivery-process retrospective');
  lines.push('');
  lines.push(`- last baseline: PR #${retrospective.baselinePr} (${retrospective.baselineDate})`);
  lines.push(`- raw merged PRs since baseline: ${retrospective.rawMergedSinceBaseline}`);
  lines.push(`- escape evidence recorded: ${retrospective.escapeEvidenceCount} post-merge escape(s), P1 lifecycle escape: ${retrospective.p1LifecycleEscape}`);
  lines.push(`- state: **${retrospective.state}**`);
  lines.push('');
  lines.push(`_Last updated: ${weekly.nowISO}_`);
  return lines.join('\n');
}

const DEFAULT_INTERVENTIONS_SECTION = `## Active improvement interventions

Agent/human-managed. The workflow above never adds, closes, or dispositions rows here -- see
[docs/development/continuous-improvement.md](../blob/main/docs/development/continuous-improvement.md).

| Improvement | Source/evidence | Verification condition | State |
| --- | --- | --- | --- |
| Executable PR lifecycle enforcement (\`infra/dev/pr-watch.mjs\`, \`pr-reconcile.mjs\`, \`check-pr-disposition.sh\`, \`pr-disposition.yml\`) | [2026-09-05 retrospective](../blob/main/docs/development/delivery-process-retrospective-2026-09-05.md), highest-leverage improvement #1 | Reconciliation-finding share (39/79 baseline) measurably lower at next retrospective | IMPLEMENTED -- awaiting retrospective verification |
| Required validation for repository workflow tooling wired into the always-running CI classify job | [2026-09-05 retrospective](../blob/main/docs/development/delivery-process-retrospective-2026-09-05.md), highest-leverage improvement #2 | Lifecycle/tooling-finding share (6/79 baseline) does not recur from an untested helper at next retrospective | IMPLEMENTED -- awaiting retrospective verification |
| Earlier closure of high-risk semantic-neighbor and acceptance-evidence state in planning/implementation for authority/status-sensitive work | [2026-09-05 retrospective](../blob/main/docs/development/delivery-process-retrospective-2026-09-05.md), highest-leverage improvement #3 | Semantic-propagation + baseline-reconciliation finding share (39/79 baseline) trends down at next retrospective | AWAITING VERIFICATION |
| Trial: small closure-set handoff on the next architecture/status-sensitive slices | [2026-09-05 retrospective](../blob/main/docs/development/delivery-process-retrospective-2026-09-05.md), proposed experiment | Recurrence of propagation/base findings on trialed slices; retain only if it decreases | TRIAL -- awaiting verification |
| Trial: final PR closeout summary after substantial remediation | [2026-09-05 retrospective](../blob/main/docs/development/delivery-process-retrospective-2026-09-05.md), proposed experiment | PR-description drift (6/79 baseline) decreases without becoming another approval ritual | TRIAL -- awaiting verification |
`;

/** Build the full initial register body on first bootstrap. */
export function buildInitialBody(obligationsText) {
  return [
    `# ${REGISTER_TITLE}`,
    '',
    'This issue is the active continuous-improvement register for Arcogine: current recurring-obligation',
    'state plus still-unverified improvement interventions. See',
    '[docs/development/continuous-improvement.md](../blob/main/docs/development/continuous-improvement.md) for the',
    'operating model this issue implements. Detailed evidence lives in repository history, PRs, reviews, issues,',
    'and dated retrospectives -- this issue is operational state, not a process database.',
    '',
    MARKER_START,
    '',
    obligationsText,
    '',
    MARKER_END,
    '',
    DEFAULT_INTERVENTIONS_SECTION,
  ].join('\n');
}

/**
 * Split a register body into (before, managed, after) around the marker pair.
 * Throws when the markers are missing, duplicated, or out of order -- malformed
 * marker state must fail loudly rather than silently overwriting the wrong region
 * or the whole issue.
 */
export function splitMarkers(body) {
  const startIdx = [...body.matchAll(new RegExp(escapeRegex(MARKER_START), 'g'))].map((m) => m.index);
  const endIdx = [...body.matchAll(new RegExp(escapeRegex(MARKER_END), 'g'))].map((m) => m.index);

  if (startIdx.length !== 1 || endIdx.length !== 1) {
    throw new Error(
      `malformed continuous-improvement register markers: found ${startIdx.length} start marker(s) and ` +
        `${endIdx.length} end marker(s), expected exactly one of each`,
    );
  }
  if (endIdx[0] <= startIdx[0]) {
    throw new Error('malformed continuous-improvement register markers: end marker precedes start marker');
  }

  const before = body.slice(0, startIdx[0]);
  const managed = body.slice(startIdx[0] + MARKER_START.length, endIdx[0]);
  const after = body.slice(endIdx[0] + MARKER_END.length);
  return { before, managed, after };
}

function escapeRegex(s) {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

/**
 * Replace only the workflow-owned region of an existing register body, preserving
 * everything else -- including the agent/human-managed intervention section --
 * byte for byte.
 */
export function mergeRegisterBody(oldBody, obligationsText) {
  const { before, after } = splitMarkers(oldBody);
  return `${before}${MARKER_START}\n\n${obligationsText}\n\n${MARKER_END}${after}`;
}

/**
 * Find the register issue by exact title among candidate issues (open + closed).
 * Never creates a duplicate silently: more than one exact-title match is ledger
 * corruption and must fail loudly rather than picking one arbitrarily.
 */
export function findRegisterIssue(issues) {
  const matches = issues.filter((i) => i.title === REGISTER_TITLE);
  if (matches.length > 1) {
    throw new Error(
      `ambiguous continuous-improvement register state: found ${matches.length} issues titled ` +
        `"${REGISTER_TITLE}" (#${matches.map((i) => i.number).join(', #')}); refusing to guess which is canonical`,
    );
  }
  return matches[0] ?? null;
}

/**
 * Decide whether an update actually changes the rendered obligations text, so the
 * caller can skip posting a body update / comment when nothing changed -- avoiding
 * weekly spam for unchanged state.
 */
export function obligationsChanged(oldManagedText, newManagedText) {
  return oldManagedText.trim() !== newManagedText.trim();
}

// ------------------------------- GitHub I/O -------------------------------

function authToken() {
  const token = process.env.GH_TOKEN || process.env.GITHUB_TOKEN;
  if (!token) throw new Error('no GitHub token: set GH_TOKEN or GITHUB_TOKEN');
  return token;
}

async function ghRepo(path, { method = 'GET', body, repo = DEFAULT_REPO } = {}) {
  return ghFetch(`https://api.github.com/repos/${repo}${path}`, { method, body });
}

async function ghSearch(query) {
  return ghFetch(`https://api.github.com/search/issues?q=${encodeURIComponent(query)}&per_page=100`, {});
}

async function ghFetch(url, { method = 'GET', body } = {}) {
  const response = await fetch(url, {
    method,
    headers: {
      Authorization: `Bearer ${authToken()}`,
      Accept: 'application/vnd.github+json',
      'Content-Type': 'application/json',
      'User-Agent': 'arcogine-continuous-improvement',
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!response.ok) {
    const text = await response.text().catch(() => '');
    throw new Error(`GitHub API ${method} ${url} failed: ${response.status} ${response.statusText} ${text}`);
  }
  return response.json();
}

async function listAllIssuesByTitleSearch(repo) {
  // search_issues covers open + closed in one call; we still verify exact title
  // equality afterward since the search query only narrows candidates.
  const result = await ghSearch(`repo:${repo} in:title "${REGISTER_TITLE}" type:issue`);
  return (result.items || []).map((i) => ({ number: i.number, title: i.title, state: i.state, body: i.body }));
}

async function listIssueComments(repo, number) {
  const comments = await ghRepo(`/issues/${number}/comments?per_page=100`, { repo });
  return comments.map((c) => c.body);
}

async function createRegisterIssue(repo, body) {
  return ghRepo('/issues', { repo, method: 'POST', body: { title: REGISTER_TITLE, body } });
}

async function updateIssueBody(repo, number, body) {
  return ghRepo(`/issues/${number}`, { repo, method: 'PATCH', body: { body } });
}

/**
 * Raw merged-PR count with number > baselinePr. Walks closed PRs newest-first via
 * the REST list endpoint (not search, whose numeric range qualifiers are unreliable
 * for issue/PR numbers) and stops once PR numbers drop to/below the baseline.
 */
async function countMergedPRsSince(repo, baselinePr) {
  let count = 0;
  let page = 1;
  for (;;) {
    const batch = await ghRepo(`/pulls?state=closed&sort=created&direction=desc&per_page=100&page=${page}`, { repo });
    if (batch.length === 0) break;
    let sawBelowBaseline = false;
    for (const pr of batch) {
      if (pr.number <= baselinePr) {
        sawBelowBaseline = true;
        continue;
      }
      if (pr.merged_at) count += 1;
    }
    if (sawBelowBaseline || batch.length < 100) break;
    page += 1;
  }
  return count;
}

function loadRetrospectiveBaseline() {
  return import('./continuous-improvement-data.json', { with: { type: 'json' } }).then((m) => m.default);
}

// ------------------------------- entrypoint -------------------------------

async function main() {
  const { values } = parseArgs({
    options: {
      'dry-run': { type: 'boolean', default: false },
      repo: { type: 'string', default: DEFAULT_REPO },
    },
  });

  const repo = values.repo;
  const now = new Date().toISOString();

  const issues = await listAllIssuesByTitleSearch(repo);
  let register = findRegisterIssue(issues);

  const baseline = await loadRetrospectiveBaseline();
  const rawMergedSinceBaseline = await countMergedPRsSince(repo, baseline.baselinePr);

  let lastVerifiedAt = null;
  let reviewedHead = null;
  if (register) {
    const comments = await listIssueComments(repo, register.number);
    const evidence = latestValidCompletion(comments);
    if (evidence) {
      lastVerifiedAt = evidence.completedAt;
      reviewedHead = evidence.reviewedHead;
    }
  }

  const weekly = {
    lastVerifiedAt,
    reviewedHead,
    nowISO: now,
    state: deriveWeeklyState(lastVerifiedAt, now),
  };
  const retrospective = {
    ...baseline,
    rawMergedSinceBaseline,
    state: deriveRetrospectiveState({
      rawMergedSinceBaseline,
      escapeEvidenceCount: baseline.escapeEvidenceCount,
      p1LifecycleEscape: baseline.p1LifecycleEscape,
    }),
  };

  const obligationsText = renderObligations({ weekly, retrospective });

  console.log(`Weekly Consistency review: ${weekly.state} (last verified: ${weekly.lastVerifiedAt ?? 'never'})`);
  console.log(`Delivery-process retrospective: ${retrospective.state} (raw merged since baseline: ${rawMergedSinceBaseline})`);

  if (values['dry-run']) {
    console.log('--dry-run: no writes performed');
    console.log(obligationsText);
    return;
  }

  if (!register) {
    const body = buildInitialBody(obligationsText);
    const created = await createRegisterIssue(repo, body);
    console.log(`Bootstrapped continuous improvement register: #${created.number}`);
    return;
  }

  const { managed: oldManaged } = splitMarkers(register.body ?? '');
  if (!obligationsChanged(oldManaged, obligationsText)) {
    console.log(`No change to recurring-obligation state on #${register.number}; skipping update.`);
    return;
  }

  const newBody = mergeRegisterBody(register.body ?? '', obligationsText);
  await updateIssueBody(repo, register.number, newBody);
  console.log(`Updated continuous improvement register #${register.number}.`);
}

const isMain =
  process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href;

if (isMain) {
  main().catch((err) => {
    console.error(err.stack || err.message || String(err));
    process.exitCode = 1;
  });
}
