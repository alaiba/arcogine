#!/usr/bin/env node
/**
 * Retrospective branch inventory over the retrospective's own window: the
 * baseline PR merge (exclusive) through the through PR merge (inclusive), read
 * from a validated Retrospective Evidence v1 bundle. This is evidence
 * discovery, not a cleanup decision or an addition to the source-neutral
 * PR/review metrics dataset.
 */
import { execFileSync } from 'node:child_process';
import { mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';
import { validateEvidence } from './delivery-retrospective-evidence.mjs';

const DEFAULT_REPO = 'alaiba/arcogine';
const REPO_PATTERN = /^[A-Za-z0-9_.-]+\/[A-Za-z0-9_.-]+$/;
const STATE_PATH = fileURLToPath(
  new URL('../../.github/continuous-improvement/retrospective.json', import.meta.url),
);
const USAGE = 'Usage: node infra/dev/delivery-retrospective-branches.mjs --evidence logs/retrospective-evidence.json'
  + ' [--baseline-pr <n>] [--repo <owner/repo>] [--output logs/retrospective-branches.json]';

function parseDate(value, label) {
  const time = Date.parse(value);
  if (typeof value !== 'string' || !Number.isFinite(time)) {
    throw new Error('Invalid ' + label + ' timestamp');
  }
  return time;
}

function positivePr(value) {
  return Number.isSafeInteger(value) && value > 0;
}

/** The versioned retrospective baseline the evidence bundle must start from. */
export function readBaselinePr(path = STATE_PATH) {
  let state;
  try {
    state = JSON.parse(readFileSync(path, 'utf8'));
  } catch (error) {
    throw new Error('Cannot read retrospective state at ' + path + ': ' + error.message);
  }
  if (!positivePr(state?.baselinePr)) throw new Error('Retrospective state has no positive baselinePr');
  return state.baselinePr;
}

/**
 * Resolve the single retrospective window from a validated Evidence v1 bundle.
 * The window never comes from a checkpoint issue or from inventory scan time.
 */
export function resolveWindow(evidence, { repo, baselinePr }) {
  validateEvidence(evidence);
  if (evidence.repository !== repo) {
    throw new Error('Evidence repository ' + evidence.repository + ' does not match ' + repo);
  }
  if (!positivePr(baselinePr)) throw new Error('Expected baseline PR must be a positive integer');
  if (evidence.requested.baselinePr !== baselinePr) {
    throw new Error('Evidence baseline PR #' + evidence.requested.baselinePr
      + ' does not match the expected retrospective baseline PR #' + baselinePr);
  }
  const { baseline, through } = evidence.endpoints;
  return {
    baseline: { number: baseline.number, mergedAt: baseline.mergedAt },
    through: { number: through.number, mergedAt: through.mergedAt },
    afterMergedAtExclusive: baseline.mergedAt,
    throughMergedAtInclusive: through.mergedAt,
    evidenceSource: { adapter: evidence.source.adapter, acquiredAt: evidence.source.acquiredAt ?? null },
  };
}

/**
 * Place a lifecycle that starts at `start` and ends at `end` (null while still
 * ongoing) relative to the window: `before` when it ended at or before the
 * exclusive lower bound, `after` when it started after the inclusive upper
 * bound, otherwise `overlaps`.
 */
export function windowRelation({ start, end }, window) {
  const startTime = parseDate(start, 'lifecycle start');
  const endTime = end === null ? null : parseDate(end, 'lifecycle end');
  if (endTime !== null && endTime < startTime) throw new Error('Lifecycle ends before it starts');
  if (endTime !== null && endTime <= Date.parse(window.afterMergedAtExclusive)) return 'before';
  if (startTime > Date.parse(window.throughMergedAtInclusive)) return 'after';
  return 'overlaps';
}

function validatePull(pr) {
  if (!positivePr(pr?.number)) throw new Error('Malformed pull request evidence');
  const label = 'PR #' + pr.number;
  if (pr.state !== 'open' && pr.state !== 'closed') throw new Error(label + ' has an unknown state');
  if ((pr.state === 'open') !== (pr.closed_at === null)) throw new Error(label + ' state and closure disagree');
  const created = parseDate(pr.created_at, label + ' creation');
  const closed = pr.closed_at === null ? null : parseDate(pr.closed_at, label + ' closure');
  if (closed !== null && closed < created) throw new Error(label + ' closes before it was created');
  const merged = pr.merged_at === null ? null : parseDate(pr.merged_at, label + ' merge');
  if (merged !== null && pr.state !== 'closed') throw new Error(label + ' is merged but not closed');
  return merged;
}

export function buildInventory({ repo, defaultBranch, evidence, baselinePr, branches, pulls, scannedAt }) {
  if (!REPO_PATTERN.test(repo)) throw new Error('Invalid repository identity');
  if (!defaultBranch || !Array.isArray(branches) || !Array.isArray(pulls)) {
    throw new Error('Incomplete repository branch/PR evidence');
  }
  const window = resolveWindow(evidence, { repo, baselinePr });
  const since = Date.parse(window.afterMergedAtExclusive);
  const until = Date.parse(window.throughMergedAtInclusive);
  if (parseDate(scannedAt, 'inventory') < until) {
    throw new Error('Inventory snapshot precedes the through PR merge');
  }

  const owner = repo.split('/')[0], byName = new Map(), mergedByPr = new Map();
  for (const branch of branches) {
    if (!branch?.name || !branch?.commit?.sha || byName.has(branch.name)) {
      throw new Error('Malformed or repeated live branch evidence');
    }
    byName.set(branch.name, {
      name: branch.name,
      live: true,
      tip: branch.commit.sha,
      protected: branch.protected === true,
      default: branch.name === defaultBranch,
      prOverlapsWindow: false,
      linkedPrs: [],
      source: ['live-ref'],
    });
  }
  if (!byName.has(defaultBranch)) {
    throw new Error('Live branch evidence lacks the default branch ' + defaultBranch);
  }

  for (const pr of pulls) {
    const merged = validatePull(pr);
    if (mergedByPr.has(pr.number)) throw new Error('Repeated pull request evidence for PR #' + pr.number);
    mergedByPr.set(pr.number, merged);
  }
  // Every merged PR the evidence proves must appear identically in the
  // all-state listing; anything else means that listing is incomplete.
  for (const expected of [evidence.endpoints.baseline, evidence.endpoints.through, ...evidence.candidates.items]) {
    if (mergedByPr.get(expected.number) !== Date.parse(expected.mergedAt)) {
      throw new Error('All-state PR metadata is incomplete or disagrees with retrospective evidence for PR #'
        + expected.number);
    }
  }

  const sameRepoPrs = [], excludedForkPrs = [], unresolved = [];
  for (const pr of pulls) {
    const relation = windowRelation({ start: pr.created_at, end: pr.closed_at }, window);
    const ref = pr.head?.ref;
    const sourceRepo = pr.head?.repo?.full_name;
    if (ref && sourceRepo && sourceRepo !== repo) {
      if (relation === 'overlaps') excludedForkPrs.push(pr.number);
    } else if (!ref || (!sourceRepo && pr.head?.label !== owner + ':' + ref)) {
      unresolved.push({ number: pr.number, ref, relation });
    } else {
      sameRepoPrs.push({ pr, ref, relation, sourceRepoVerified: sourceRepo === repo });
    }
  }

  // A PR lifecycle overlapping the window makes its head a candidate for inspection.
  for (const { ref, relation } of sameRepoPrs) {
    if (relation !== 'overlaps' || byName.has(ref)) continue;
    byName.set(ref, {
      name: ref,
      live: false,
      tip: null,
      protected: null,
      default: false,
      prOverlapsWindow: false,
      linkedPrs: [],
      source: [],
    });
  }
  for (const { pr, ref, relation, sourceRepoVerified } of sameRepoPrs) {
    const entry = byName.get(ref);
    if (!entry) continue;
    if (!entry.source.includes('historical-pr-head')) entry.source.push('historical-pr-head');
    if (relation === 'overlaps') entry.prOverlapsWindow = true;
    const merged = mergedByPr.get(pr.number);
    entry.linkedPrs.push({
      number: pr.number,
      title: pr.title ?? '',
      state: pr.state,
      createdAt: pr.created_at,
      closedAt: pr.closed_at,
      mergedAt: pr.merged_at,
      windowRelation: relation,
      mergedInWindow: merged !== null && merged > since && merged <= until,
      recordedHead: pr.head.sha ?? null,
      sourceRepoVerified,
      url: pr.html_url ?? 'https://github.com/' + repo + '/pull/' + pr.number,
    });
  }

  const byNumber = (a, b) => a - b;
  const entries = [...byName.values()].sort((a, b) => a.name.localeCompare(b.name));
  for (const entry of entries) entry.linkedPrs.sort((a, b) => a.number - b.number);
  return {
    schemaVersion: 2,
    repo,
    defaultBranch,
    window,
    scannedAt,
    scope: 'every live repository ref, plus every same-repository PR-head identity whose PR lifecycle overlaps'
      + ' the retrospective window (afterMergedAtExclusive, throughMergedAtInclusive]',
    branches: entries,
    excludedForkPrs: excludedForkPrs.sort(byNumber),
    unresolvedHeadPrs: unresolved
      .filter(({ ref, relation }) => relation === 'overlaps' || (ref && byName.has(ref)))
      .map(({ number }) => number)
      .sort(byNumber),
    limitations: [
      'A deleted branch with no PR cannot reliably be reconstructed from current refs and PR metadata.',
      'A PR lifecycle overlapping the window makes its head branch a candidate for inspection; it does not prove when the branch existed or that its commits and artifacts fall inside the window.',
      'A live branch with no window-overlapping PR may still carry in-window work; establish that from verifiable commit evidence, not from this inventory.',
      'A branch name can have been reused; verify exact commits and independent custody before interpreting its history.',
      'Research classification, window relevance of individual artifacts, and branch retention are assessed by the retrospective agent, not this inventory.',
      'scannedAt records when refs were observed; it is not an analysis cutoff.',
    ],
  };
}

export function ghGet(endpoint, paginate = false, run = execFileSync) {
  const args = ['api', '-X', 'GET'];
  if (paginate) args.push('--paginate', '--slurp');
  args.push(endpoint);
  const data = JSON.parse(run('gh', args, { encoding: 'utf8', maxBuffer: 64 * 1024 * 1024 }));
  if (!paginate) return data;
  if (!Array.isArray(data) || !data.every((page) => Array.isArray(page))) {
    throw new Error('GitHub pagination did not return complete page arrays for ' + endpoint);
  }
  return data.flat();
}

export function collect({ repo = DEFAULT_REPO, evidence, baselinePr, get = ghGet, now = () => new Date() }) {
  if (!REPO_PATTERN.test(repo)) throw new Error('Invalid --repo');
  resolveWindow(evidence, { repo, baselinePr });
  const metadata = get('repos/' + repo);
  const scannedAt = now().toISOString();
  const branches = get('repos/' + repo + '/branches?per_page=100', true);
  const pulls = get('repos/' + repo + '/pulls?state=all&per_page=100&sort=created&direction=desc', true);
  return buildInventory({
    repo, defaultBranch: metadata.default_branch, evidence, baselinePr, branches, pulls, scannedAt,
  });
}

function main() {
  const { values } = parseArgs({
    options: {
      repo: { type: 'string', default: DEFAULT_REPO },
      evidence: { type: 'string' },
      'baseline-pr': { type: 'string' },
      output: { type: 'string' },
      help: { type: 'boolean', default: false },
    },
  });
  if (values.help) {
    console.log(USAGE);
    return;
  }
  if (!values.evidence) throw new Error('Provide --evidence <Retrospective Evidence v1 bundle>');
  let baselinePr;
  if (values['baseline-pr'] === undefined) baselinePr = readBaselinePr();
  else if (/^[1-9][0-9]*$/.test(values['baseline-pr'])) baselinePr = Number(values['baseline-pr']);
  else throw new Error('--baseline-pr must be a positive PR number');
  let evidence;
  try {
    evidence = JSON.parse(readFileSync(values.evidence, 'utf8'));
  } catch (error) {
    throw new Error('Cannot read evidence bundle ' + values.evidence + ': ' + error.message);
  }
  const result = collect({ repo: values.repo, evidence, baselinePr });
  const json = JSON.stringify(result, null, 2) + '\n';
  if (values.output) {
    mkdirSync(dirname(values.output), { recursive: true });
    writeFileSync(values.output, json, 'utf8');
    const overlapping = result.branches.filter((entry) => entry.prOverlapsWindow).length;
    console.log('Inventory: ' + result.branches.length + ' branch identities (' + overlapping
      + ' with a PR overlapping PR #' + result.window.baseline.number + ' -> #' + result.window.through.number
      + '); saved to ' + values.output);
  } else {
    process.stdout.write(json);
  }
}
if (process.argv[1] === fileURLToPath(import.meta.url)) {
  try { main(); }
  catch (error) { console.error('Branch inventory incomplete: ' + error.message); process.exitCode = 1; }
}
