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
import { pathToFileURL } from 'node:url';

const DEFAULT_REPO = 'alaiba/arcogine';
const DEFAULT_INTERVAL_SECONDS = 60;
const FAILURE_ALERT_THRESHOLD = 3;

/**
 * The one status branch protection needs to require. `.github/workflows/ci.yml` defines
 * `gate` as the always-running aggregate job for exactly this purpose. Naming it is what
 * lets the resolver tell "required validation passed" apart from "some unrelated context
 * happens to be green" -- any green context would otherwise stand in for the real gate.
 */
const DEFAULT_REQUIRED_CHECK = 'gate';

/**
 * Disposition vocabulary owned by .github/agents/pr-reviewer.agent.md. Exactly two
 * values: READY TO MERGE authorizes merge of the exact reviewed head; CHANGES REQUIRED
 * blocks it. CI is not a reviewer disposition and review authorization is independent of
 * it -- a current-head review may already be READY TO MERGE while CI is still pending.
 * required validation is enforced independently via requiredCheck below: AWAITING below
 * covers both "no current-head disposition yet" and "disposition is READY but the
 * independent CI condition is not yet green", without needing a second review solely
 * because CI transitions from pending to green.
 */
const DISPOSITIONS = ['READY TO MERGE', 'CHANGES REQUIRED'];

const DISPOSITION_ALTERNATION = DISPOSITIONS.map((d) => d.replace(/ /g, '\\s+')).join('|');

const QUERY = `
query($owner:String!, $name:String!, $number:Int!) {
  repository(owner:$owner, name:$name) {
    pullRequest(number:$number) {
      number title isDraft state
      headRefOid mergeable mergeStateStatus
      baseRefName baseRefOid
      reviews(last:100) {
        totalCount
        nodes { author { login } state submittedAt body commit { oid } }
      }
      reviewThreads(last:100) { totalCount nodes { isResolved } }
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

  Exactly one pull request per invocation. Run it once per PR to follow several.

OPTIONS
  --repo <owner/name>   Repository (default: ${DEFAULT_REPO})
  --watch               Poll continuously, emitting one line per change.
                        Without it, the state is resolved once and printed.
  --interval <seconds>  Poll interval for --watch (default: ${DEFAULT_INTERVAL_SECONDS}, minimum: 10)
  --json                Emit a single JSON object instead of text (single resolution only)
  --required-check <n>  Status that proves required validation ran (default: ${DEFAULT_REQUIRED_CHECK})
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

/**
 * How far the head is ahead of / behind the base branch.
 *
 * Needed because a review is only evidence about the base-to-head transition that existed
 * when it was written. GraphQL's mergeStateStatus does not reliably report BEHIND (it
 * depends on branch-protection settings), so ask the compare endpoint directly.
 */
async function fetchComparison({ repo, token }, pr) {
  const response = await fetch(
    `https://api.github.com/repos/${repo}/compare/${encodeURIComponent(pr.baseRefName)}...${pr.headRefOid}`,
    {
      headers: {
        authorization: `bearer ${token}`,
        accept: 'application/vnd.github+json',
        'user-agent': 'arcogine-pr-watch',
        connection: 'close',
      },
    },
  );
  if (!response.ok) {
    throw new Error(`GitHub compare HTTP ${response.status} ${response.statusText}`);
  }
  const body = await response.json();
  return { aheadBy: body.ahead_by ?? 0, behindBy: body.behind_by ?? 0 };
}

/**
 * Extract the reviewer contract's explicit FINAL disposition, or null when absent.
 *
 * Two rules matter, and both exist because of an observed false positive: a review whose
 * prose contained the literal text "Disposition: READY TO MERGE" as an example was read as
 * merge-ready even though its actual closing disposition was CHANGES REQUIRED.
 *
 *  - Prefer a disposition that starts its own line. A disposition quoted mid-sentence is
 *    discussion, not a verdict.
 *  - Take the LAST match, never the first. The contract calls for an explicit *final*
 *    disposition, so earlier occurrences are superseded by construction.
 *
 * The contract calls for an explicit *final* disposition, so this recognises one only when
 * it genuinely ends the review: the marker must start its own line AND that line must be
 * the last non-blank line of the body.
 *
 * Everything weaker has been tried and produced a false READY:
 *  - taking the first match read a disposition quoted in prose as the verdict;
 *  - an unanchored fallback let an inline `Disposition: ...` code span do the same;
 *  - taking the last anchored match anywhere still accepted a marker followed by
 *    substantive blocker prose, where the reviewer plainly did not end on that verdict.
 *
 * Blockquoted lines are excluded outright -- quoted text is someone else's verdict, not
 * this review's. A body with no contract-shaped final disposition resolves to null, which
 * the lifecycle treats conservatively as AWAITING.
 */
function dispositionOf(body) {
  if (!body) return null;
  const lines = String(body).split(/\r?\n/);
  let lastMeaningful = null;
  for (let i = lines.length - 1; i >= 0; i -= 1) {
    if (lines[i].trim() !== '') {
      lastMeaningful = lines[i];
      break;
    }
  }
  if (lastMeaningful === null) return null;
  // The whole line must BE the verdict, not merely begin with one. A prefix-only match
  // accepts "Disposition: READY TO MERGE? Actually no." as READY. Only intentional
  // surrounding markdown and a closing full stop may follow the vocabulary.
  // No '>' in the prefix class either: a quoted disposition is not this review's verdict.
  const match = lastMeaningful.match(
    new RegExp(`^[ \\t*_+-]*Disposition:\\s*[*_]*\\s*(${DISPOSITION_ALTERNATION})\\s*[*_]*\\s*[.]?\\s*$`, 'i'),
  );
  if (!match) return null;
  return match[1].replace(/\s+/g, ' ').toUpperCase();
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

function summarize(pr, comparison = { aheadBy: 0, behindBy: 0 }, requiredCheck = DEFAULT_REQUIRED_CHECK) {
  const checks = normalizeChecks(pr);
  const required = checks.find((c) => c.name === requiredCheck) ?? null;
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

  // Per author, not globally: a later review by someone else must not erase a standing
  // blocker. Each author's own latest review supersedes only their own earlier ones.
  const latestByAuthor = new Map();
  for (const review of reviews) latestByAuthor.set(review.author, review);

  // Two different kinds of "changes required", with different lifetimes:
  //
  //  - A formal GitHub CHANGES_REQUESTED review stands until its author submits a newer
  //    review or it is dismissed. Pushing does not clear it, so it blocks on any head.
  //  - A CHANGES REQUIRED *disposition* in a COMMENTED review attests to the commit it
  //    reviewed. Once the head moves past it, remediation has happened and AGENTS.md puts
  //    the PR back in AWAITING for re-evaluation -- so it becomes a stale blocker, not a
  //    standing one. Treating it as standing would leave a PR permanently CHANGES REQUIRED
  //    no matter how much remediation landed.
  // A formal CHANGES_REQUESTED is cleared only by that same author later APPROVING or by
  // the review being DISMISSED -- never by them merely commenting again. Collapsing to the
  // author's chronologically latest review would let a follow-up COMMENT silently discard a
  // block GitHub still enforces.
  const formalBlockers = [];
  for (const [author, _latest] of latestByAuthor) {
    const mine = reviews.filter((r) => r.author === author);
    const lastRequested = mine.findLast((r) => r.state === 'CHANGES_REQUESTED');
    if (!lastRequested) continue;
    const clearedAfter = mine.some(
      (r) =>
        String(r.submittedAt) > String(lastRequested.submittedAt) &&
        (r.state === 'APPROVED' || r.state === 'DISMISSED'),
    );
    if (!clearedAfter) formalBlockers.push(lastRequested);
  }

  // A CHANGES REQUIRED *disposition* in a COMMENTED review attests to the commit it
  // reviewed, so once the head moves it becomes stale rather than standing.
  const latest = [...latestByAuthor.values()];
  const dispositionBlockers = latest.filter(
    (r) => r.state !== 'CHANGES_REQUESTED' && r.disposition === 'CHANGES REQUIRED' && r.commit === pr.headRefOid,
  );
  const blockers = [...formalBlockers, ...dispositionBlockers];
  const staleBlockers = latest.filter(
    (r) =>
      r.state !== 'CHANGES_REQUESTED' &&
      r.disposition === 'CHANGES REQUIRED' &&
      r.commit !== pr.headRefOid &&
      !formalBlockers.includes(r),
  );

  const reviewsTruncated = (pr.reviews?.totalCount ?? reviews.length) > reviews.length;
  const threadNodes = pr.reviewThreads?.nodes ?? [];
  const threadsTruncated = (pr.reviewThreads?.totalCount ?? threadNodes.length) > threadNodes.length;

  return {
    number: pr.number,
    title: pr.title,
    isDraft: pr.isDraft,
    prState: pr.state ?? 'OPEN',
    requiredCheckName: requiredCheck,
    requiredCheck: required,
    threadsTruncated,
    head: pr.headRefOid,
    baseRef: pr.baseRefName,
    baseOid: pr.baseRefOid,
    behindBy: comparison.behindBy,
    aheadBy: comparison.aheadBy,
    mergeable: pr.mergeable,
    mergeStateStatus: pr.mergeStateStatus,
    checks,
    checksFailing: checks.filter((c) => !GREEN.has(c.verdict) && !PENDING.has(c.verdict)),
    checksPending: checks.filter((c) => PENDING.has(c.verdict)),
    openThreads,
    reviews,
    reviewsTruncated,
    blockers,
    staleBlockers,
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
  // A merged or closed PR has no open-PR lifecycle left to resolve. Report the terminal
  // fact instead of continuing to answer a question that no longer applies.
  if (s.prState && s.prState !== 'OPEN') {
    return { state: s.prState, terminal: true, reasons: [`pull request is ${s.prState.toLowerCase()}`] };
  }

  if (s.isDraft) return { state: 'AWAITING', reasons: ['pull request is a draft'] };

  const blocking = [];
  if (s.mergeable === 'CONFLICTING') blocking.push('merge conflict with the base branch');
  if (s.checksFailing.length > 0) {
    blocking.push(`failing checks: ${s.checksFailing.map((c) => `${c.name} (${c.verdict})`).join(', ')}`);
  }
  // Every author's standing blocker counts, not just the newest review overall.
  for (const b of s.blockers) {
    const why = b.state === 'CHANGES_REQUESTED' ? 'CHANGES_REQUESTED' : 'disposition CHANGES REQUIRED';
    blocking.push(`${b.author} has a standing ${why} review (${b.submittedAt} on ${b.commit.slice(0, 7)})`);
  }
  if (blocking.length > 0) return { state: 'CHANGES REQUIRED', reasons: blocking };

  const waiting = [];
  // A review only ever attests to the base-to-head transition that existed when it was
  // written. If the base has advanced, no existing review covers the current transition.
  if (s.behindBy > 0) {
    waiting.push(
      `head is ${s.behindBy} commit(s) behind ${s.baseRef}; no review covers the current base-to-head transition`,
    );
  }
  if (s.reviewsTruncated) {
    waiting.push('review history exceeds the fetched window, so review state cannot be fully resolved');
  }

  for (const b of s.staleBlockers ?? []) {
    waiting.push(
      `${b.author} required changes on ${b.commit.slice(0, 7)}; the head has since moved -- awaiting re-review`,
    );
  }

  const head = s.newestReviewOnHead;
  if (!head) {
    waiting.push(
      s.newestReview
        ? `newest review is on ${s.newestReview.commit.slice(0, 7)}, not the current head -- awaiting re-review`
        : 'no review has been submitted',
    );
  } else if (!head.disposition) {
    waiting.push('review on current head states no explicit disposition');
  }

  // Absence of evidence is not green, and an unrelated green context is not the required
  // one. Only the named required check proves validation ran; other contexts may fail (and
  // block above) but can never substitute for it.
  if (!s.requiredCheck) {
    waiting.push(
      `required check "${s.requiredCheckName}" is not present on the head commit; ` +
        `required validation is not proven to have run (${s.checks.length} other context(s) present)`,
    );
  } else if (s.requiredCheck.verdict !== 'SUCCESS') {
    // SKIPPED and NEUTRAL are green enough for an auxiliary context, but not for the one
    // status that is supposed to prove validation ran: a skipped gate ran nothing. Only
    // SUCCESS is evidence here, whatever the broader green class allows elsewhere.
    waiting.push(
      `required check "${s.requiredCheckName}" is ${s.requiredCheck.verdict}; ` +
        'only SUCCESS proves required validation',
    );
  }
  if (s.checksPending.length > 0) {
    waiting.push(`checks still pending: ${s.checksPending.map((c) => c.name).join(', ')}`);
  }
  if (s.threadsTruncated) {
    waiting.push('review threads exceed the fetched window, so unresolved-thread state cannot be fully resolved');
  }
  if (s.openThreads > 0) waiting.push(`${s.openThreads} unresolved review thread(s)`);
  if (s.mergeable !== 'MERGEABLE') waiting.push(`GitHub reports mergeable=${s.mergeable}`);

  if (waiting.length > 0) return { state: 'AWAITING', reasons: waiting };

  return {
    state: 'READY TO MERGE',
    reasons: [
      `review disposition on current head is ${head.disposition}`,
      `required check "${s.requiredCheckName}" is ${s.requiredCheck.verdict}`,
      `head is level with ${s.baseRef}`,
    ],
  };
}

/** Stable, sorted, line-oriented projection used for change detection in --watch. */
function stateLines(s) {
  const lines = [
    `head: ${s.head.slice(0, 7)}`,
    // Every field the resolver reads must appear here, or a change that moves the lifecycle
    // can produce no signal at all: a draft being marked ready, or the PR being merged or
    // closed, would otherwise be invisible to --watch.
    `pr-state: ${s.prState}${s.isDraft ? ' (draft)' : ''}`,
    `required-check: ${s.requiredCheck ? s.requiredCheck.verdict : 'ABSENT'}`,
    // Base identity and distance are part of the watched state: a base advance can
    // invalidate an existing review without the head changing at all.
    `base: ${s.baseRef}@${String(s.baseOid).slice(0, 7)} (behind ${s.behindBy}, ahead ${s.aheadBy})`,
    `merge: ${s.mergeStateStatus} (${s.mergeable})`,
    // Truncation flags are lifecycle inputs, so they belong in the projection too: crossing
    // a connection boundary can flip the resolver to AWAITING while every returned item
    // still looks resolved, which would otherwise change the answer with no watch signal.
    `open-threads: ${s.openThreads}${s.threadsTruncated ? ' (truncated)' : ''}`,
    `reviews-truncated: ${s.reviewsTruncated ? 'yes' : 'no'}`,
    `checks-present: ${s.checks.length}`,
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
    `base        ${s.baseRef}@${String(s.baseOid).slice(0, 7)}  (behind ${s.behindBy}, ahead ${s.aheadBy})`,
    `merge       ${s.mergeStateStatus} (${s.mergeable})`,
    `checks      ${s.checks.length - s.checksFailing.length - s.checksPending.length} green, ` +
      `${s.checksFailing.length} failing, ${s.checksPending.length} pending` +
      `  [required "${s.requiredCheckName}": ${s.requiredCheck ? s.requiredCheck.verdict : 'ABSENT'}]`,
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
      const summary = summarize(pr, await fetchComparison(options, pr), options.requiredCheck);
      const lines = stateLines(summary);

      if (consecutiveFailures > 0 && alerted) {
        console.log(`POLL RECOVERED after ${consecutiveFailures} consecutive failure(s)`);
      }
      consecutiveFailures = 0;
      alerted = false;

      const lifecycle = resolveLifecycle(summary);

      if (previous === null) {
        console.log(`baseline ${summary.head.slice(0, 7)} -- ${lifecycle.state}`);
      } else {
        const changes = diff(previous, lines);
        if (changes.length > 0) {
          console.log(`CHANGED -- ${lifecycle.state}`);
          for (const change of changes) console.log(change);
        }
      }
      previous = lines;

      // Stop on a terminal state rather than polling a merged or closed PR forever while
      // pretending to resolve an open-PR lifecycle.
      if (lifecycle.terminal) {
        console.log(`TERMINAL -- ${lifecycle.state}; stopping watch`);
        return;
      }
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
        'required-check': { type: 'string', default: DEFAULT_REQUIRED_CHECK },
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

  // Exactly one PR. Silently resolving the first and discarding the rest is worse than
  // refusing, because the output looks like it honoured every argument.
  if (positionals.length > 1) {
    process.stderr.write(
      `expected exactly one pr-number, got ${positionals.length}: ${positionals.join(' ')}\n` +
        'pr-watch handles one pull request per invocation; run it once per PR.\n',
    );
    return 1;
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

  const options = { repo: values.repo, number, token, interval, requiredCheck: values['required-check'] };

  if (values.watch) {
    await watch(options);
    return 0; // unreachable; watch loops until the process is stopped
  }

  let summary;
  try {
    const pr = await fetchPullRequest(options);
    summary = summarize(pr, await fetchComparison(options, pr), options.requiredCheck);
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

// Pure resolution logic is exported so pr-watch.test.mjs can exercise the lifecycle states
// deterministically, without network access.
export { dispositionOf, summarize, resolveLifecycle, stateLines, diff };

const invokedDirectly =
  process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href;

if (invokedDirectly) {
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
}
