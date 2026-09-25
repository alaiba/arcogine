#!/usr/bin/env node
/**
 * Pure retrospective analyzer for Retrospective Evidence v1.
 * This module reads a supplied evidence file but has no GitHub, Git, token, or
 * mutable repository-state dependency. Identical evidence produces identical
 * analytical output.
 */

import { readFileSync } from 'node:fs';
import { parseArgs } from 'node:util';
import { pathToFileURL } from 'node:url';
import {
  FINDING_CATEGORIES,
  FINDING_CONFIDENCES,
  FINDING_SEVERITIES,
  FINDING_STATUSES,
  validateEvidence,
} from './delivery-retrospective-evidence.mjs';

const TRUSTED_REVIEW_ASSOCIATIONS = new Set(['OWNER', 'MEMBER', 'COLLABORATOR']);
const DISPOSITION_ALTERNATION = ['READY TO MERGE', 'CHANGES REQUIRED']
  .map((disposition) => disposition.replace(/ /g, '\\s+'))
  .join('|');
const FINDING_HEADER = /^REV-(\d+) - (\S(?:.*\S)?)$/;
const FINDING_LIKE_LINE = /^\s*REV-/;
const FIELD_NAMES = ['Severity', 'Category', 'Confidence', 'Head', 'Subject', 'Status'];

function usage() {
  return `delivery-retrospective -- analyze a Retrospective Evidence v1 bundle

USAGE
  node infra/dev/delivery-retrospective.mjs --input <evidence.json> [--json]

OPTIONS
  --input <path>   Source-neutral evidence JSON produced by an acquisition adapter
  --json           Emit machine-readable analytical output (default)
  --help           Show this help

This analyzer performs no GitHub, Git, token, or network access.
`;
}

/** Extract a historical review's explicit closing disposition, or null. */
export function closingDisposition(body) {
  if (!body) return null;
  const lines = String(body).split(/\r?\n/);
  let lastMeaningful = null;
  for (let index = lines.length - 1; index >= 0; index -= 1) {
    if (lines[index].trim() !== '') {
      lastMeaningful = lines[index];
      break;
    }
  }
  if (lastMeaningful === null) return null;
  const match = lastMeaningful.match(
    new RegExp(`^[ \\t*_+-]*Disposition:\\s*[*_]*\\s*(${DISPOSITION_ALTERNATION})\\s*[*_]*\\s*[.]?\\s*$`, 'i'),
  );
  if (!match) return null;
  return match[1].replace(/\s+/g, ' ').toUpperCase();
}

/** Construct the exact (baseline merge, through merge] main-target window. */
export function buildExactWindow(records, baselineMergedAt, throughMergedAt) {
  const start = Date.parse(baselineMergedAt);
  const end = Date.parse(throughMergedAt);
  if (!Number.isFinite(start) || !Number.isFinite(end) || end <= start) {
    throw new Error('through PR must merge after the baseline PR');
  }

  const byNumber = new Map();
  for (const record of records) {
    if (!record?.merged || !record.mergedAt || record.baseRefName !== 'main') continue;
    const merged = Date.parse(record.mergedAt);
    if (!Number.isFinite(merged)) throw new Error(`invalid merge timestamp for PR #${record.number}`);
    if (merged <= start || merged > end) continue;
    if (byNumber.has(record.number)) {
      throw new Error(`duplicate PR #${record.number} in retrospective search results`);
    }
    byNumber.set(record.number, record);
  }

  return [...byNumber.values()].sort((left, right) => {
    const time = Date.parse(left.mergedAt) - Date.parse(right.mergedAt);
    return time || left.number - right.number;
  });
}

export function changesRequiredCount(record) {
  const reviews = record?.reviews;
  if (!reviews || reviews.complete !== true || !Array.isArray(reviews.items)) {
    throw new Error(`PR #${record?.number ?? '?'} has no complete review payload`);
  }
  if (reviews.reportedCount !== reviews.items.length) {
    throw new Error(
      `PR #${record.number} has ${reviews.reportedCount} reviews but only ${reviews.items.length} were fetched`,
    );
  }
  return reviews.items.filter(
    (review) =>
      TRUSTED_REVIEW_ASSOCIATIONS.has(review.authorAssociation) &&
      closingDisposition(review.body) === 'CHANGES REQUIRED',
  ).length;
}

export function summarizeWindow(window) {
  const rounds = window.map((record) => changesRequiredCount(record));
  const distribution = { zero: 0, one: 0, two: 0, threePlus: 0 };
  for (const count of rounds) {
    if (count === 0) distribution.zero += 1;
    else if (count === 1) distribution.one += 1;
    else if (count === 2) distribution.two += 1;
    else distribution.threePlus += 1;
  }
  return {
    mergedPrCount: window.length,
    trustedChangesRequiredSubmissions: rounds.reduce((sum, count) => sum + count, 0),
    reviewRoundDistribution: distribution,
  };
}

function parseReviewFindings(body, prNumber, review, diagnostics) {
  if (!body) return [];
  const sourceLines = String(body).split(/\r?\n/);
  const lines = [];
  let fence = null;
  let inBlockQuote = false;
  for (const line of sourceLines) {
    if (fence) {
      const close = line.match(/^\s*(`{3,}|~{3,})\s*$/);
      if (close && close[1][0] === fence.character && close[1].length >= fence.length) fence = null;
      lines.push('');
      continue;
    }
    const open = line.match(/^\s*(`{3,}|~{3,})/);
    if (open) {
      fence = { character: open[1][0], length: open[1].length };
      lines.push('');
      continue;
    }
    if (/^\s*>/.test(line)) {
      inBlockQuote = true;
      lines.push('');
      continue;
    }
    if (inBlockQuote) {
      if (line.trim() === '') inBlockQuote = false;
      lines.push('');
      continue;
    }
    lines.push(line);
  }
  const starts = [];
  lines.forEach((line, index) => {
    if (FINDING_LIKE_LINE.test(line)) starts.push(index);
  });
  const parsed = [];
  for (let startIndex = 0; startIndex < starts.length; startIndex += 1) {
    const firstLine = starts[startIndex];
    const endLine = starts[startIndex + 1] ?? lines.length;
    const block = lines.slice(firstLine, endLine);
    const header = block[0].match(FINDING_HEADER);
    const revisionId = header ? `REV-${header[1]}` : block[0].trim().match(/^REV-[^\s]+/)?.[0] ?? null;
    const fields = new Map();
    const duplicates = new Set();
    for (const line of block.slice(1)) {
      const match = line.match(/^(Severity|Category|Confidence|Head|Subject|Status):\s*(.*?)\s*$/);
      if (!match) continue;
      if (fields.has(match[1])) duplicates.add(match[1]);
      fields.set(match[1], match[2]);
    }

    const missing = FIELD_NAMES.filter((name) => !fields.has(name) || fields.get(name) === '');
    const reasons = [];
    if (!header) reasons.push('noncanonical finding heading');
    if (missing.length) reasons.push(`missing fields: ${missing.join(', ')}`);
    if (duplicates.size) reasons.push(`duplicate fields: ${[...duplicates].sort().join(', ')}`);
    if (reasons.length) {
      diagnostics.malformedFindingBlocks.push({
        prNumber,
        reviewId: review.id,
        submittedAt: review.submittedAt,
        revisionId,
        reasons,
      });
      continue;
    }

    const finding = {
      prNumber,
      revisionId: `REV-${header[1]}`,
      title: header[2],
      severity: fields.get('Severity'),
      category: fields.get('Category'),
      confidence: fields.get('Confidence'),
      head: fields.get('Head'),
      subject: fields.get('Subject'),
      status: fields.get('Status'),
      reviewId: review.id,
      submittedAt: review.submittedAt,
      reviewOrder: review.__analysisOrder,
    };
    diagnostics.structuredFindingsParsed += 1;
    if (!FINDING_CATEGORIES.includes(finding.category)) diagnostics.unknownCategories.add(finding.category);
    if (!FINDING_SEVERITIES.includes(finding.severity)) diagnostics.unsupportedValues.severity.add(finding.severity);
    if (!FINDING_CONFIDENCES.includes(finding.confidence)) diagnostics.unsupportedValues.confidence.add(finding.confidence);
    if (!FINDING_STATUSES.includes(finding.status)) diagnostics.unsupportedValues.status.add(finding.status);
    if (review.reviewedHead && finding.head.toLowerCase() !== review.reviewedHead.toLowerCase()) {
      diagnostics.headMismatches.push({
        prNumber,
        revisionId: finding.revisionId,
        reviewId: review.id,
        findingHead: finding.head,
        sourceReviewedHead: review.reviewedHead,
      });
    }
    parsed.push(finding);
  }
  return parsed;
}

function stableIdentityFields(finding) {
  return ['title', 'severity', 'category', 'confidence', 'subject'];
}

function findingAggregateKey(...values) {
  return JSON.stringify(values);
}

function revisionNumber(revisionId) {
  return Number(/^REV-(\d+)$/.exec(revisionId)?.[1] ?? Number.POSITIVE_INFINITY);
}

function compareRevisionIds(left, right) {
  return revisionNumber(left) - revisionNumber(right) || left.localeCompare(right);
}

function sortedCountObject(counts) {
  return Object.fromEntries([...counts.entries()].sort(([left], [right]) => left.localeCompare(right)));
}

function sortedPairRows(counts, keyNames, countName) {
  return [...counts.entries()]
    .map(([key, count]) => ({ ...Object.fromEntries(JSON.parse(key).map((value, index) => [keyNames[index], value])), [countName]: count }))
    .sort((left, right) => {
      for (const key of keyNames) {
        const compared = String(left[key]).localeCompare(String(right[key]));
        if (compared !== 0) return compared;
      }
      return 0;
    });
}

function analyzeFindings(window) {
  const diagnostics = {
    trustedReviewCount: 0,
    trustedBlockingReviewCount: 0,
    structuredFindingsParsed: 0,
    blockingReviewsWithoutParseableFinding: [],
    findingsOmittedFromRereview: [],
    malformedFindingBlocks: [],
    duplicateFindingIdentities: [],
    conflictingFindingIdentities: [],
    headMismatches: [],
    unknownCategories: new Set(),
    unsupportedValues: {
      severity: new Set(),
      confidence: new Set(),
      status: new Set(),
    },
  };

  const occurrences = [];
  for (const pr of window) {
    const trustedReviews = pr.reviews.items
      .map((review, index) => ({ ...review, __analysisOrder: index }))
      .filter((review) => TRUSTED_REVIEW_ASSOCIATIONS.has(review.authorAssociation))
      .sort((left, right) =>
        Date.parse(left.submittedAt) - Date.parse(right.submittedAt) ||
        String(left.id ?? '').localeCompare(String(right.id ?? '')) ||
        String(left.body ?? '').localeCompare(String(right.body ?? '')),
      );
    const previouslySeenIds = new Set();
    for (let start = 0; start < trustedReviews.length;) {
      const submittedAt = Date.parse(trustedReviews[start].submittedAt);
      let end = start + 1;
      while (end < trustedReviews.length && Date.parse(trustedReviews[end].submittedAt) === submittedAt) end += 1;
      const timestampGroup = trustedReviews.slice(start, end);
      const groupFindings = [];
      let hasClosingDisposition = false;
      for (const review of timestampGroup) {
        diagnostics.trustedReviewCount += 1;
        const disposition = closingDisposition(review.body);
        if (disposition === 'CHANGES REQUIRED') diagnostics.trustedBlockingReviewCount += 1;
        if (disposition) hasClosingDisposition = true;
        const findings = parseReviewFindings(review.body, pr.number, review, diagnostics);
        if (disposition === 'CHANGES REQUIRED' && findings.length === 0) {
          diagnostics.blockingReviewsWithoutParseableFinding.push({
            prNumber: pr.number,
            reviewId: review.id,
            submittedAt: review.submittedAt,
          });
        }
        groupFindings.push(...findings);
      }
      const presentIds = new Set(groupFindings.map((finding) => finding.revisionId));
      if (hasClosingDisposition && previouslySeenIds.size) {
        const omitted = [...previouslySeenIds].filter((revisionId) => !presentIds.has(revisionId)).sort(compareRevisionIds);
        if (omitted.length) {
          diagnostics.findingsOmittedFromRereview.push({
            prNumber: pr.number,
            submittedAt: timestampGroup[0].submittedAt,
            reviewIds: timestampGroup.map((review) => review.id),
            revisionIds: omitted,
          });
        }
      }
      for (const revisionId of presentIds) previouslySeenIds.add(revisionId);
      occurrences.push(...groupFindings);
      start = end;
    }
  }

  const byIdentity = new Map();
  const inSameReview = new Map();
  for (const occurrence of occurrences) {
    const key = findingAggregateKey(occurrence.prNumber, occurrence.revisionId);
    const reviewKey = findingAggregateKey(key, occurrence.reviewId ?? `index:${occurrence.reviewOrder}`);
    const priorSameReview = inSameReview.get(reviewKey);
    if (priorSameReview) {
      diagnostics.duplicateFindingIdentities.push({ prNumber: occurrence.prNumber, revisionId: occurrence.revisionId, reviewId: occurrence.reviewId });
      continue;
    }
    inSameReview.set(reviewKey, occurrence);

    const history = byIdentity.get(key) ?? [];
    history.push(occurrence);
    byIdentity.set(key, history);
  }

  const distinctFindings = [...byIdentity.values()].map((history) => {
    const ordered = [...history].sort((left, right) => {
      const time = Date.parse(left.submittedAt) - Date.parse(right.submittedAt);
      if (time) return time;
      const identity = String(left.reviewId ?? '').localeCompare(String(right.reviewId ?? ''));
      if (identity) return identity;
      const head = left.head.localeCompare(right.head);
      if (head) return head;
      const status = left.status.localeCompare(right.status);
      if (status) return status;
      return left.reviewOrder - right.reviewOrder;
    });
    const first = ordered[0];
    const last = ordered[ordered.length - 1];
    const identityChanges = stableIdentityFields(first).filter((field) =>
      new Set(ordered.map((item) => item[field])).size > 1,
    );
    if (identityChanges.length) {
      diagnostics.conflictingFindingIdentities.push({
        prNumber: first.prNumber,
        revisionId: first.revisionId,
        firstReviewId: first.reviewId,
        conflictingReviewIds: ordered.slice(1).map((item) => item.reviewId),
        changedFields: identityChanges,
      });
    }
    if (ordered.length > 1) {
      const sameTimeGroups = new Map();
      for (const item of ordered) {
        const groupTime = Date.parse(item.submittedAt);
        const group = sameTimeGroups.get(groupTime) ?? [];
        group.push(item);
        sameTimeGroups.set(groupTime, group);
      }
      for (const [, group] of sameTimeGroups) {
        if (
          new Set(group.map((item) => item.status)).size > 1 ||
          new Set(group.map((item) => item.head)).size > 1
        ) {
          diagnostics.conflictingFindingIdentities.push({
            prNumber: first.prNumber,
            revisionId: first.revisionId,
            submittedAt: group[0].submittedAt,
            changedFields: ['head or status at identical submission time'],
          });
        }
      }
    }
    return {
      prNumber: first.prNumber,
      revisionId: first.revisionId,
      title: first.title,
      severity: first.severity,
      category: first.category,
      confidence: first.confidence,
      subject: first.subject,
      firstReviewedHead: first.head,
      firstReview: { id: first.reviewId, submittedAt: first.submittedAt },
      currentReviewedHead: last.head,
      status: last.status,
      lastReview: { id: last.reviewId, submittedAt: last.submittedAt },
    };
  }).sort((left, right) => left.prNumber - right.prNumber || compareRevisionIds(left.revisionId, right.revisionId));

  const bySeverity = new Map();
  const byCategory = new Map();
  const bySeverityCategory = new Map();
  const byConfidence = new Map();
  const categoryPrs = new Map();
  const severityCategoryPrs = new Map();
  for (const finding of distinctFindings) {
    bySeverity.set(finding.severity, (bySeverity.get(finding.severity) ?? 0) + 1);
    byCategory.set(finding.category, (byCategory.get(finding.category) ?? 0) + 1);
    bySeverityCategory.set(
      findingAggregateKey(finding.severity, finding.category),
      (bySeverityCategory.get(findingAggregateKey(finding.severity, finding.category)) ?? 0) + 1,
    );
    byConfidence.set(finding.confidence, (byConfidence.get(finding.confidence) ?? 0) + 1);
    const categoryKey = findingAggregateKey(finding.category);
    const categorySet = categoryPrs.get(categoryKey) ?? new Set();
    categorySet.add(finding.prNumber);
    categoryPrs.set(categoryKey, categorySet);
    const pairKey = findingAggregateKey(finding.severity, finding.category);
    const pairSet = severityCategoryPrs.get(pairKey) ?? new Set();
    pairSet.add(finding.prNumber);
    severityCategoryPrs.set(pairKey, pairSet);
  }

  const categoryIncidence = new Map([...categoryPrs].map(([category, prs]) => [category, prs.size]));
  const pairIncidence = new Map([...severityCategoryPrs].map(([pair, prs]) => [pair, prs.size]));
  const incompleteReasons = [];
  if (diagnostics.blockingReviewsWithoutParseableFinding.length) incompleteReasons.push('blocking reviews without a parseable canonical finding');
  if (diagnostics.findingsOmittedFromRereview.length) incompleteReasons.push('prior finding identities omitted from re-review');
  if (diagnostics.malformedFindingBlocks.length) incompleteReasons.push('malformed finding blocks');
  if (diagnostics.duplicateFindingIdentities.length) incompleteReasons.push('duplicate PR + REV identity within a review');
  if (diagnostics.conflictingFindingIdentities.length) incompleteReasons.push('conflicting reuse of a PR + REV identity');
  if (diagnostics.headMismatches.length) incompleteReasons.push('finding head disagrees with source review head');
  if (diagnostics.unknownCategories.size) incompleteReasons.push('unknown category values');
  for (const [kind, values] of Object.entries(diagnostics.unsupportedValues)) {
    if (values.size) incompleteReasons.push(`unsupported ${kind} values`);
  }

  const diagnosticOutput = {
    trustedReviewCount: diagnostics.trustedReviewCount,
    trustedBlockingReviewCount: diagnostics.trustedBlockingReviewCount,
    structuredFindingsParsed: diagnostics.structuredFindingsParsed,
    blockingReviewsWithoutParseableFinding: diagnostics.blockingReviewsWithoutParseableFinding,
    findingsOmittedFromRereview: diagnostics.findingsOmittedFromRereview,
    malformedFindingBlocks: diagnostics.malformedFindingBlocks,
    duplicateFindingIdentities: diagnostics.duplicateFindingIdentities,
    conflictingFindingIdentities: diagnostics.conflictingFindingIdentities,
    headMismatches: diagnostics.headMismatches,
    unknownCategories: [...diagnostics.unknownCategories].sort((a, b) => a.localeCompare(b)),
    unsupportedValues: Object.fromEntries(
      Object.entries(diagnostics.unsupportedValues).map(([kind, values]) => [kind, [...values].sort((a, b) => a.localeCompare(b))]),
    ),
    incompleteReasons,
  };
  const complete = incompleteReasons.length === 0;

  return {
    complete,
    completeness: {
      severityDistribution: complete,
      categoryDistribution: complete,
      severityCategoryDistribution: complete,
      confidenceDistribution: complete,
      prIncidence: complete,
    },
    totalDistinctFindings: distinctFindings.length,
    prsWithFindings: new Set(distinctFindings.map((finding) => finding.prNumber)).size,
    findingsBySeverity: sortedCountObject(bySeverity),
    findingsByCategory: sortedCountObject(byCategory),
    findingsBySeverityCategory: sortedPairRows(bySeverityCategory, ['severity', 'category'], 'findingCount'),
    findingsByConfidence: sortedCountObject(byConfidence),
    prIncidenceByCategory: sortedPairRows(categoryIncidence, ['category'], 'prCount'),
    prIncidenceBySeverityCategory: sortedPairRows(pairIncidence, ['severity', 'category'], 'prCount'),
    distinctFindings,
    diagnostics: diagnosticOutput,
  };
}

/** Analyze one complete source-neutral evidence bundle. */
export function analyzeEvidence(evidence) {
  validateEvidence(evidence);
  const { baseline, through } = evidence.endpoints;
  const window = buildExactWindow(evidence.candidates.items, baseline.mergedAt, through.mergedAt);
  if (!window.some((pr) => pr.number === evidence.requested.throughPr)) {
    throw new Error(`through PR #${evidence.requested.throughPr} is missing from the exact retrospective window`);
  }
  const findingAnalytics = analyzeFindings(window);
  const summary = summarizeWindow(window);
  return {
    schemaVersion: 1,
    repository: evidence.repository,
    baseline: { number: baseline.number, mergedAt: baseline.mergedAt, baseRefName: baseline.baseRefName },
    through: { number: through.number, mergedAt: through.mergedAt, baseRefName: through.baseRefName },
    exactWindow: {
      afterMergedAtExclusive: baseline.mergedAt,
      throughMergedAtInclusive: through.mergedAt,
    },
    ...summary,
    pullRequests: window.map((pr) => ({ number: pr.number, mergedAt: pr.mergedAt })),
    findingAnalytics,
  };
}

export function render(summary) {
  const distribution = summary.reviewRoundDistribution;
  const findings = summary.findingAnalytics;
  return [
    `Retrospective window: PR #${summary.baseline.number} (exclusive) -> PR #${summary.through.number} (inclusive)`,
    `Merged PRs: ${summary.mergedPrCount}`,
    `Trusted CHANGES REQUIRED submissions: ${summary.trustedChangesRequiredSubmissions}`,
    `Review checkpoints: ${distribution.zero} zero / ${distribution.one} one / ${distribution.two} two / ${distribution.threePlus} three-plus`,
    `PRs: ${summary.pullRequests.map((pr) => `#${pr.number}`).join(', ')}`,
    `Distinct structured findings: ${findings.totalDistinctFindings} across ${findings.prsWithFindings} PRs`,
    `Finding analytics complete: ${findings.complete ? 'yes' : 'no'}${findings.diagnostics.incompleteReasons.length ? ` (${findings.diagnostics.incompleteReasons.join('; ')})` : ''}`,
  ].join('\n');
}

function main() {
  let parsed;
  try {
    parsed = parseArgs({
      allowPositionals: false,
      options: {
        input: { type: 'string' },
        json: { type: 'boolean', default: true },
        help: { type: 'boolean', default: false },
      },
    });
  } catch (error) {
    process.stderr.write(`${error.message}\n\n${usage()}`);
    return 1;
  }
  if (parsed.values.help) {
    process.stdout.write(usage());
    return 0;
  }
  if (!parsed.values.input) {
    process.stderr.write(`--input is required\n\n${usage()}`);
    return 1;
  }
  try {
    const evidence = JSON.parse(readFileSync(parsed.values.input, 'utf8'));
    const summary = analyzeEvidence(evidence);
    process.stdout.write(parsed.values.json ? `${JSON.stringify(summary, null, 2)}\n` : `${render(summary)}\n`);
    return 0;
  } catch (error) {
    process.stderr.write(`delivery-retrospective: ${error.message}\n`);
    return 1;
  }
}

const invokedDirectly = process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href;
if (invokedDirectly) process.exitCode = main();
