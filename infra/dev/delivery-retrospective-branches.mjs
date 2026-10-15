#!/usr/bin/env node
/**
 * Retrospective branch inventory. This is evidence discovery, not a cleanup
 * decision or an addition to the source-neutral PR/review metrics dataset.
 */
import { execFileSync } from 'node:child_process';
import { mkdirSync, writeFileSync } from 'node:fs';
import { dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';

export const CHECKPOINT_TITLE = 'Continuous improvement checkpoint';
const DEFAULT_REPO = 'alaiba/arcogine';

function parseDate(value, label) {
  const time = Date.parse(value);
  if (typeof value !== 'string' || !Number.isFinite(time)) {
    throw new Error('Invalid ' + label + ' timestamp');
  }
  return time;
}

export function validateCheckpoint(issue, number) {
  if (issue?.title !== CHECKPOINT_TITLE || issue?.pull_request || issue?.number !== number) {
    throw new Error('Issue #' + number + ' is not the exact Continuous improvement checkpoint');
  }
  parseDate(issue.created_at, 'checkpoint');
  return issue.created_at;
}

export function buildInventory({ repo, defaultBranch, checkpoint, checkpointNumber, branches, pulls, scannedAt }) {
  if (!/^[A-Za-z0-9_.-]+\/[A-Za-z0-9_.-]+$/.test(repo)) throw new Error('Invalid repository identity');
  if (!defaultBranch || !Array.isArray(branches) || !Array.isArray(pulls)) {
    throw new Error('Incomplete repository branch/PR evidence');
  }
  const since = parseDate(validateCheckpoint(checkpoint, checkpointNumber), 'checkpoint');
  const until = parseDate(scannedAt, 'inventory');
  if (until < since) throw new Error('Checkpoint occurs after inventory snapshot');
  const owner = repo.split('/')[0], byName = new Map(), seenPrs = new Set();
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
      linkedPrs: [],
      source: ['live-ref'],
    });
  }

  const excludedForkPrs = [], unresolvedHeadPrs = [];
  for (const pr of pulls) {
    if (!Number.isInteger(pr?.number) || seenPrs.has(pr.number)) {
      throw new Error('Malformed or repeated pull request evidence');
    }
    seenPrs.add(pr.number);
    const created = parseDate(pr.created_at, 'PR creation');
    if (created > until) continue;
    const closed = pr.closed_at === null ? null : parseDate(pr.closed_at, 'PR closure');
    if (closed !== null && closed < since) continue;
    const ref = pr.head?.ref;
    if (!ref) {
      unresolvedHeadPrs.push(pr.number);
      continue;
    }
    const sourceRepo = pr.head?.repo?.full_name;
    const ownerLabel = owner + ':' + ref;
    if (sourceRepo && sourceRepo !== repo) {
      excludedForkPrs.push(pr.number);
      continue;
    }
    if (!sourceRepo && pr.head?.label !== ownerLabel) {
      unresolvedHeadPrs.push(pr.number);
      continue;
    }
    let entry = byName.get(ref);
    if (!entry) {
      entry = {
        name: ref,
        live: false,
        tip: null,
        protected: null,
        default: ref === defaultBranch,
        linkedPrs: [],
        source: ['historical-pr-head'],
      };
      byName.set(ref, entry);
    } else if (!entry.source.includes('historical-pr-head')) {
      entry.source.push('historical-pr-head');
    }
    entry.linkedPrs.push({
      number: pr.number,
      title: pr.title ?? '',
      state: pr.state,
      mergedAt: pr.merged_at ?? null,
      createdAt: pr.created_at,
      closedAt: pr.closed_at,
      recordedHead: pr.head.sha ?? null,
      sourceRepoVerified: sourceRepo === repo,
      url: pr.html_url ?? 'https://github.com/' + repo + '/pull/' + pr.number,
    });
  }
  const entries = [...byName.values()].sort((a, b) => a.name.localeCompare(b.name));
  return {
    schemaVersion: 1,
    repo,
    defaultBranch,
    checkpoint: { number: checkpointNumber, openedAt: checkpoint.created_at },
    scannedAt,
    scope: 'all surviving repository refs plus discoverable PR heads whose PR lifetime overlaps [checkpoint opening, inventory time]',
    branches: entries,
    excludedForkPrs,
    unresolvedHeadPrs,
    limitations: [
      'A deleted branch with no PR cannot reliably be reconstructed from current refs and PR metadata.',
      'A PR lifetime overlapping the interval does not prove precisely when its source branch existed.',
      'A live branch name can have been reused; verify exact commits and independent custody before interpreting its history.',
      'Branch retention and DELETE CANDIDATE are assessed by the retrospective agent, not this inventory.',
      'This live inventory is separate from the immutable baseline-to-through merged-PR analytics window.',
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

export function collect({ repo = DEFAULT_REPO, checkpointNumber, get = ghGet, now = () => new Date() }) {
  if (!Number.isSafeInteger(checkpointNumber) || checkpointNumber < 1) {
    throw new Error('--checkpoint must identify a positive issue number');
  }
  if (!/^[A-Za-z0-9_.-]+\/[A-Za-z0-9_.-]+$/.test(repo)) throw new Error('Invalid --repo');
  const checkpoint = get('repos/' + repo + '/issues/' + checkpointNumber);
  const metadata = get('repos/' + repo);
  const scannedAt = now().toISOString();
  const branches = get('repos/' + repo + '/branches?per_page=100', true);
  const pulls = get('repos/' + repo + '/pulls?state=all&per_page=100&sort=created&direction=desc', true);
  return buildInventory({
    repo, defaultBranch: metadata.default_branch, checkpoint,
    checkpointNumber, branches, pulls, scannedAt,
  });
}

function main() {
  const { values } = parseArgs({
    options: {
      repo: { type: 'string', default: DEFAULT_REPO },
      checkpoint: { type: 'string' },
      output: { type: 'string' },
      help: { type: 'boolean', default: false },
    },
  });
  if (values.help) {
    console.log('Usage: node infra/dev/delivery-retrospective-branches.mjs --checkpoint <issue> [--repo <owner/repo>] [--output logs/retrospective-branches.json]');
    return;
  }
  if (!/^[1-9][0-9]*$/.test(values.checkpoint ?? '')) {
    throw new Error('Provide --checkpoint <positive issue number>');
  }
  const result = collect({ repo: values.repo, checkpointNumber: Number(values.checkpoint) });
  const json = JSON.stringify(result, null, 2) + '\n';
  if (values.output) {
    mkdirSync(dirname(values.output), { recursive: true });
    writeFileSync(values.output, json, 'utf8');
    console.log('Inventory: ' + result.branches.length + ' branch identities; saved to ' + values.output);
  } else {
    process.stdout.write(json);
  }
}
if (process.argv[1] === fileURLToPath(import.meta.url)) {
  try { main(); }
  catch (error) { console.error('Branch inventory incomplete: ' + error.message); process.exitCode = 1; }
}
