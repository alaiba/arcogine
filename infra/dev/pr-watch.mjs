#!/usr/bin/env node
/**
 * pr-watch.mjs -- resolve or watch an Arcogine pull request's lifecycle state.
 *
 * Portable agent tooling for the PR lifecycle contract in AGENTS.md. Dependency-free:
 * uses only Node builtins, so it runs anywhere Node runs with no install step and no
 * shell-specific setup.
 *
 *   node infra/dev/pr-watch.mjs 252              # resolve once, print state, exit
 *   node infra/dev/pr-watch.mjs 252 --watch      # poll and emit one line per change
 *   node infra/dev/pr-watch.mjs 252 --json       # machine-readable single resolution
 *
 * Auth: GH_TOKEN or GITHUB_TOKEN if set; otherwise `gh auth token` is invoked ONCE at
 * startup to obtain a token. After that only fetch() is used, so `gh` is never required
 * per poll and never needs to be on PATH if a token is supplied by environment.
 *
 * Three deliberate design rules, each earned from a real failure:
 *
 *  1. Silence must mean "no activity", never "broken". Consecutive failures emit an
 *     explicit POLL FAILED line rather than going quiet, because a quiet broken watcher
 *     is indistinguishable from a quiet PR.
 *  2. Both additions and removals are reported. A diff that prints only additions turns
 *     a check flipping back to green, or a review being withdrawn, into a blank event.
 *  3. Never key review detection on GraphQL `latestReviews` or `reviewDecision`.
 *     `latestReviews` omits reviews authored by the PR author, and `reviewDecision` is
 *     only set by APPROVED/CHANGES_REQUESTED -- never by COMMENTED. A watcher built on
 *     either field silently misses this repository's normal review traffic. Use
 *     `reviews` and read the disposition out of the body instead.
 */

import { execFileSync } from 'node:child_process';
import { parseArgs } from 'node:util';

const DEFAULT_REPO = 'alaiba/arcogine';
const DEFAULT_INTERVAL_SECONDS = 60;
const FAILURE_ALERT_THRESHOLD = 3;

/** Disposition vocabulary owned by .github/agents/pr-reviewer.agent.md. */
const DISPOSITIONS = [
  'READY TO MERGE',
  'READY AFTER CI',
  'CHANGES REQUIRED',
  'NON-BLOCKING FOLLOW-UPS ONLY',
];

const QUERY = `
query($owner:String!, $name:String!, $number:Int!) {
  repository(owner:$owner, name:$name) {
    pullRequest(number:$number) {
      number title isDraft state
      headRefOid mergeable mergeStateStatus
      reviews(last:20) {
        nodes { author { login } state submittedAt body commit { oid } }
      }
      reviewThreads(last:100) { nodes { isResolved } }
      commits(last:1) {
        nodes {
          commit {
            statusCheckRollup {
              contexts(last:100) {
                nodes {
                  __typename
                  ... on CheckRun { name conclusion status }
                  ... on StatusContext { context state }
                }
              }
            }
          }
        }
      }
    }
  }
}`;

function usage() {
  return `pr-watch -- resolve or watch an Arcogine pull request's lifecycle state

USAGE
  node infra/dev/pr-watch.mjs <pr-number> [options]

OPTIONS
  --repo <owner/name>   Repository (default: ${DEFAULT_REPO})
  --watch               Poll continuously, emitting one line per change.
                        Without it, the state is resolved once and printed.
  --interval <seconds>  Poll interval for --watch (default: ${DEFAULT_INTERVAL_SECONDS}, minimum: 10)
  --json                Emit a single JSON object instead of text (single resolution only)
  --exit-code           Exit 0 READY TO MERGE, 2 CHANGES REQUIRED, 3 AWAITING
                        (without it, exit 0 on any successful resolution)
  --help                Show this help

AUTH
  Uses GH_TOKEN or GITHUB_TOKEN when set. Otherwise runs \`gh auth token\` once at
  startup. Supply a token by environment to avoid needing the gh CLI at all.

EXIT CODES
  0  resolved successfully (or state-mapped with --exit-code)
  1  operational failure (auth, network, API, bad arguments)
  2  CHANGES REQUIRED   (only with --exit-code)
  3  AWAITING           (only with --exit-code)

EXAMPLES
  node infra/dev/pr-watch.mjs 252
  node infra/dev/pr-watch.mjs 252 --json
  node infra/dev/pr-watch.mjs 252 --watch --interval 120
`;
}

function resolveToken() {
  const fromEnv = process.env.GH_TOKEN || process.env.GITHUB_TOKEN;
  if (fromEnv) return fromEnv.trim();
  try {
    return execFileSync('gh', ['auth', 'token'], {
      encoding: 'utf8',
      stdio: ['ignore', 'pipe', 'ignore'],
    }).trim();
  } catch {
    throw new Error(
      'no GitHub token: set GH_TOKEN or GITHUB_TOKEN, or install and authenticate the gh CLI',
    );
  }
}

async function fetchPullRequest({ repo, number, token }) {
  const [owner, name] = repo.split('/');
  if (!owner || !name) throw new Error(`--repo must be owner/name, got "${repo}"`);

  const response = await fetch('https://api.github.com/graphql', {
    method: 'POST',
    headers: {
      authorization: `bearer ${token}`,
      'content-type': 'application/json',
      'user-agent': 'arcogine-pr-watch',
      // Do not pool the socket. A pooled keep-alive connection outlives the response and
      // delays process exit (and, on Windows, trips a libuv assertion if the process is
      // torn down while it is still closing).
      connection: 'close',
    },
    body: JSON.stringify({ query: QUERY, variables: { owner, name, number } }),
  });

  if (!response.ok) {
    throw new Error(`GitHub API HTTP ${response.status} ${response.statusText}`);
  }
  const payload = await response.json();
  if (payload.errors?.length) {
    throw new Error(`GitHub API: ${payload.errors.map((e) => e.message).join('; ')}`);
  }
  const pr = payload.data?.repository?.pullRequest;
  if (!pr) throw new Error(`pull request ${repo}#${number} not found`);
  return pr;
}

/** Extract the reviewer contract's explicit final disposition, or null when absent. */
function dispositionOf(body) {
  if (!body) return null;
  const match = body.match(
    /Disposition:\s*\**\s*(READY TO MERGE|READY AFTER CI|CHANGES REQUIRED|NON-BLOCKING FOLLOW-UPS ONLY)/i,
  );
  if (match) return match[1].toUpperCase();
  // Fall back to a lone disposition token only if exactly one appears anywhere.
  const present = DISPOSITIONS.filter((d) => body.toUpperCase().includes(d));
  return present.length === 1 ? present[0] : null;
}

function normalizeChecks(pr) {
  const contexts = pr.commits?.nodes?.[0]?.commit?.statusCheckRollup?.contexts?.nodes ?? [];
  return contexts.map((c) => {
    const name = c.name ?? c.context ?? '(unnamed)';
    const raw = c.conclusion ?? c.state ?? (c.status === 'COMPLETED' ? 'SUCCESS' : c.status);
    const verdict = String(raw ?? 'PENDING').toUpperCase();
    return { name, verdict };
  });
}

const GREEN = new Set(['SUCCESS', 'SKIPPED', 'NEUTRAL']);
const PENDING = new Set(['PENDING', 'QUEUED', 'IN_PROGRESS', 'WAITING', 'EXPECTED', 'REQUESTED']);

function summarize(pr) {
  const checks = normalizeChecks(pr);
  const reviews = (pr.reviews?.nodes ?? []).map((r) => ({
    author: r.author?.login ?? '(unknown)',
    state: r.state,
    submittedAt: r.submittedAt,
    commit: r.commit?.oid ?? '',
    disposition: dispositionOf(r.body),
  }));
  reviews.sort((a, b) => String(a.submittedAt).localeCompare(String(b.submittedAt)));

  const onHead = reviews.filter((r) => r.commit === pr.headRefOid);
  const openThreads = (pr.reviewThreads?.nodes ?? []).filter((t) => !t.isResolved).length;

  return {
    number: pr.number,
    title: pr.title,
    isDraft: pr.isDraft,
    head: pr.headRefOid,
    mergeable: pr.mergeable,
    mergeStateStatus: pr.mergeStateStatus,
    checks,
    checksFailing: checks.filter((c) => !GREEN.has(c.verdict) && !PENDING.has(c.verdict)),
    checksPending: checks.filter((c) => PENDING.has(c.verdict)),
    openThreads,
    reviews,
    newestReview: reviews.at(-1) ?? null,
    newestReviewOnHead: onHead.at(-1) ?? null,
  };
}

/**
 * Map the observed facts onto the AGENTS.md PR lifecycle states.
 *
 * Deliberately conservative: a review that predates the current head cannot establish a
 * merge disposition for the head, so it resolves to AWAITING re-review rather than
 * carrying a stale verdict forward.
 */
function resolveLifecycle(s) {
  const reasons = [];

  if (s.isDraft) return { state: 'AWAITING', reasons: ['pull request is a draft'] };

  if (s.mergeable === 'CONFLICTING') reasons.push('merge conflict with the base branch');
  if (s.checksFailing.length > 0) {
    reasons.push(`failing checks: ${s.checksFailing.map((c) => `${c.name} (${c.verdict})`).join(', ')}`);
  }
  const head = s.newestReviewOnHead;
  if (head?.disposition === 'CHANGES REQUIRED') reasons.push('review disposition on current head is CHANGES REQUIRED');
  if (head?.state === 'CHANGES_REQUESTED') reasons.push('a CHANGES_REQUESTED review stands on the current head');
  if (reasons.length > 0) return { state: 'CHANGES REQUIRED', reasons };

  if (!head) {
    return {
      state: 'AWAITING',
      reasons: [
        s.newestReview
          ? `newest review is on ${s.newestReview.commit.slice(0, 7)}, not the current head -- awaiting re-review`
          : 'no review has been submitted',
      ],
    };
  }
  if (!head.disposition) {
    return { state: 'AWAITING', reasons: ['review on current head states no explicit disposition'] };
  }
  if (head.disposition === 'READY AFTER CI' && s.checksPending.length > 0) {
    return { state: 'AWAITING', reasons: ['READY AFTER CI with required checks still pending'] };
  }
  if (s.checksPending.length > 0) {
    return { state: 'AWAITING', reasons: ['required checks still pending'] };
  }
  if (s.openThreads > 0) {
    return { state: 'AWAITING', reasons: [`${s.openThreads} unresolved review thread(s)`] };
  }
  if (s.mergeable !== 'MERGEABLE') {
    return { state: 'AWAITING', reasons: [`GitHub reports mergeable=${s.mergeable}`] };
  }
  return {
    state: 'READY TO MERGE',
    reasons: [`review disposition on current head is ${head.disposition}; required checks green`],
  };
}

/** Stable, sorted, line-oriented projection used for change detection in --watch. */
function stateLines(s) {
  const lines = [
    `head: ${s.head.slice(0, 7)}`,
    `merge: ${s.mergeStateStatus} (${s.mergeable})`,
    `open-threads: ${s.openThreads}`,
  ];
  for (const r of s.reviews) {
    lines.push(
      `review ${r.submittedAt} by ${r.author} [${r.state}]` +
        `${r.disposition ? ` {${r.disposition}}` : ''} on ${r.commit.slice(0, 7)}`,
    );
  }
  for (const c of s.checks) {
    if (!GREEN.has(c.verdict)) lines.push(`check ${c.name}: ${c.verdict}`);
  }
  return lines.sort();
}

function renderOnce(s, lifecycle) {
  const out = [
    `PR #${s.number}  ${s.title}`,
    `head        ${s.head.slice(0, 7)}`,
    `merge       ${s.mergeStateStatus} (${s.mergeable})`,
    `checks      ${s.checks.length - s.checksFailing.length - s.checksPending.length} green, ` +
      `${s.checksFailing.length} failing, ${s.checksPending.length} pending`,
    `threads     ${s.openThreads} unresolved`,
  ];
  if (s.newestReview) {
    const r = s.newestReview;
    const onHead = r.commit === s.head ? 'current head' : `${r.commit.slice(0, 7)} (stale)`;
    out.push(`review      ${r.submittedAt} by ${r.author} [${r.state}]` +
      `${r.disposition ? ` {${r.disposition}}` : ' {no explicit disposition}'} on ${onHead}`);
  } else {
    out.push('review      none submitted');
  }
  out.push('', `LIFECYCLE   ${lifecycle.state}`);
  for (const reason of lifecycle.reasons) out.push(`            - ${reason}`);
  return out.join('\n');
}

function diff(prev, next) {
  const before = new Set(prev);
  const after = new Set(next);
  const added = next.filter((l) => !before.has(l)).map((l) => `+ ${l}`);
  const removed = prev.filter((l) => !after.has(l)).map((l) => `- ${l}`);
  return [...removed, ...added];
}

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

async function watch(options) {
  const intervalMs = options.interval * 1000;
  let previous = null;
  let consecutiveFailures = 0;
  let alerted = false;

  console.log(`watching ${options.repo}#${options.number} every ${options.interval}s`);

  for (;;) {
    try {
      const pr = await fetchPullRequest(options);
      const summary = summarize(pr);
      const lines = stateLines(summary);

      if (consecutiveFailures > 0 && alerted) {
        console.log(`POLL RECOVERED after ${consecutiveFailures} consecutive failure(s)`);
      }
      consecutiveFailures = 0;
      alerted = false;

      if (previous === null) {
        const { state } = resolveLifecycle(summary);
        console.log(`baseline ${summary.head.slice(0, 7)} -- ${state}`);
      } else {
        const changes = diff(previous, lines);
        if (changes.length > 0) {
          const { state } = resolveLifecycle(summary);
          console.log(`CHANGED -- ${state}`);
          for (const change of changes) console.log(change);
        }
      }
      previous = lines;
    } catch (error) {
      consecutiveFailures += 1;
      // Rule 1: never let a broken watcher look like a quiet PR.
      if (consecutiveFailures >= FAILURE_ALERT_THRESHOLD && !alerted) {
        console.log(`POLL FAILED (${consecutiveFailures} consecutive): ${error.message}`);
        alerted = true;
      }
    }
    await sleep(intervalMs);
  }
}

async function main() {
  let parsed;
  try {
    parsed = parseArgs({
      allowPositionals: true,
      options: {
        repo: { type: 'string', default: DEFAULT_REPO },
        watch: { type: 'boolean', default: false },
        interval: { type: 'string', default: String(DEFAULT_INTERVAL_SECONDS) },
        json: { type: 'boolean', default: false },
        'exit-code': { type: 'boolean', default: false },
        help: { type: 'boolean', default: false },
      },
    });
  } catch (error) {
    process.stderr.write(`${error.message}\n\n${usage()}`);
    return 1;
  }

  const { values, positionals } = parsed;
  if (values.help || positionals.length === 0) {
    process.stdout.write(usage());
    return values.help ? 0 : 1;
  }

  const number = Number(positionals[0]);
  if (!Number.isInteger(number) || number <= 0) {
    process.stderr.write(`pr-number must be a positive integer, got "${positionals[0]}"\n`);
    return 1;
  }
  const interval = Math.max(10, Number(values.interval) || DEFAULT_INTERVAL_SECONDS);

  let token;
  try {
    token = resolveToken();
  } catch (error) {
    process.stderr.write(`${error.message}\n`);
    return 1;
  }

  const options = { repo: values.repo, number, token, interval };

  if (values.watch) {
    await watch(options);
    return 0; // unreachable; watch loops until the process is stopped
  }

  let summary;
  try {
    summary = summarize(await fetchPullRequest(options));
  } catch (error) {
    process.stderr.write(`${error.message}\n`);
    return 1;
  }
  const lifecycle = resolveLifecycle(summary);

  if (values.json) {
    process.stdout.write(`${JSON.stringify({ ...summary, lifecycle }, null, 2)}\n`);
  } else {
    process.stdout.write(`${renderOnce(summary, lifecycle)}\n`);
  }

  if (!values['exit-code']) return 0;
  if (lifecycle.state === 'READY TO MERGE') return 0;
  if (lifecycle.state === 'CHANGES REQUIRED') return 2;
  return 3;
}

// Set exitCode and let the event loop drain rather than calling process.exit(): forcing
// exit while HTTP handles are still closing aborts the process instead of returning the
// intended status (observed on Windows as a libuv UV_HANDLE_CLOSING assertion).
main().then(
  (code) => {
    process.exitCode = code;
  },
  (error) => {
    process.stderr.write(`pr-watch: ${error?.stack ?? error}\n`);
    process.exitCode = 1;
  },
);
