/**
 * Source-neutral contract between retrospective acquisition and analysis.
 * Keep transport-specific facts in producer metadata; this module owns only
 * the normalized evidence shape and its completeness invariants.
 */

export const EVIDENCE_SCHEMA_VERSION = 1;

export const FINDING_CATEGORIES = Object.freeze([
  'CORRECTNESS',
  'ARCHITECTURE',
  'DETERMINISM',
  'OWNERSHIP_BOUNDARY',
  'COMPATIBILITY',
  'IDENTITY_PROVENANCE',
  'PLANNING_STATUS',
  'DOCUMENTATION_ACCURACY',
  'TEST_EVIDENCE',
  'SCOPE',
  'TOOLCHAIN_CI',
  'SECURITY_AUTHORITY',
  'PR_RECONCILIATION',
]);

export const FINDING_SEVERITIES = Object.freeze(['P0', 'P1', 'P2', 'P3', 'Nit']);
export const FINDING_CONFIDENCES = Object.freeze(['HIGH', 'MEDIUM', 'LOW']);
export const FINDING_STATUSES = Object.freeze(['OPEN', 'RESOLVED', 'OBSOLETE']);
export const REVIEW_AUTHOR_ASSOCIATIONS = Object.freeze([
  'COLLABORATOR',
  'CONTRIBUTOR',
  'FIRST_TIMER',
  'FIRST_TIME_CONTRIBUTOR',
  'MANNEQUIN',
  'MEMBER',
  'NONE',
  'OWNER',
]);

function fail(message) {
  throw new Error(`invalid retrospective evidence: ${message}`);
}

function isObject(value) {
  return value !== null && typeof value === 'object' && !Array.isArray(value);
}

function positiveInteger(value) {
  return Number.isInteger(value) && value > 0;
}

function nonnegativeInteger(value) {
  return Number.isInteger(value) && value >= 0;
}

function nonemptyString(value) {
  return typeof value === 'string' && value.trim().length > 0;
}

function timestamp(value) {
  return typeof value === 'string' && value.trim() !== '' && Number.isFinite(Date.parse(value));
}

function validateEndpoint(endpoint, label, requestedNumber) {
  if (!isObject(endpoint)) fail(`${label} endpoint facts are missing`);
  if (!positiveInteger(endpoint.number) || endpoint.number !== requestedNumber) {
    fail(`${label} endpoint number does not match the requested PR`);
  }
  if (endpoint.merged !== true || !timestamp(endpoint.mergedAt)) {
    fail(`${label} endpoint must be a merged PR with a valid merge timestamp`);
  }
  if (endpoint.baseRefName !== 'main') {
    fail(`${label} endpoint PR #${endpoint.number} does not target main`);
  }
}

function validateReview(review, prNumber, index, reviewIds) {
  const label = `PR #${prNumber} review ${index + 1}`;
  if (!isObject(review)) fail(`${label} is missing`);
  if (review.id !== null && !nonemptyString(review.id)) fail(`${label} id must be a string or null`);
  if (review.id !== null) {
    if (reviewIds.has(review.id)) fail(`${label} duplicates review identity ${review.id}`);
    reviewIds.add(review.id);
  }
  if (typeof review.body !== 'string' && review.body !== null) {
    fail(`${label} body must be a string or null`);
  }
  if (!nonemptyString(review.authorAssociation)) fail(`${label} author association is missing`);
  if (!REVIEW_AUTHOR_ASSOCIATIONS.includes(review.authorAssociation)) {
    fail(`${label} author association ${review.authorAssociation} is unsupported`);
  }
  if (!timestamp(review.submittedAt)) fail(`${label} submission timestamp is missing or invalid`);
  if (review.reviewedHead !== null && !nonemptyString(review.reviewedHead)) {
    fail(`${label} reviewed head must be a string or null`);
  }
}

function validateCandidate(candidate, index, prNumbers) {
  const label = `candidate PR ${index + 1}`;
  if (!isObject(candidate)) fail(`${label} is missing`);
  if (!positiveInteger(candidate.number)) fail(`${label} number must be a positive integer`);
  if (prNumbers.has(candidate.number)) fail(`duplicate candidate PR #${candidate.number}`);
  prNumbers.add(candidate.number);
  if (candidate.merged !== true || !timestamp(candidate.mergedAt)) {
    fail(`candidate PR #${candidate.number} must be merged with a valid merge timestamp`);
  }
  if (!nonemptyString(candidate.baseRefName)) {
    fail(`candidate PR #${candidate.number} target branch is missing`);
  }
  const reviews = candidate.reviews;
  if (!isObject(reviews) || reviews.complete !== true) {
    fail(`candidate PR #${candidate.number} review collection is incomplete`);
  }
  if (!nonnegativeInteger(reviews.reportedCount) || !Array.isArray(reviews.items)) {
    fail(`candidate PR #${candidate.number} review count metadata is malformed`);
  }
  if (reviews.reportedCount !== reviews.items.length) {
    fail(
      `candidate PR #${candidate.number} review count mismatch: reported ${reviews.reportedCount}, received ${reviews.items.length}`,
    );
  }
  const reviewIds = new Set();
  reviews.items.forEach((review, reviewIndex) => {
    validateReview(review, candidate.number, reviewIndex, reviewIds);
  });
}

/** Validate and return a Retrospective Evidence v1 value. */
export function validateEvidence(evidence) {
  if (!isObject(evidence)) fail('root must be an object');
  if (evidence.schemaVersion !== EVIDENCE_SCHEMA_VERSION) {
    fail(`unsupported schema version ${String(evidence.schemaVersion)}`);
  }
  if (!nonemptyString(evidence.repository) || !/^[^/]+\/[^/]+$/.test(evidence.repository)) {
    fail('repository must be owner/name');
  }
  const requested = evidence.requested;
  if (!isObject(requested) || !positiveInteger(requested.baselinePr) || !positiveInteger(requested.throughPr)) {
    fail('requested baselinePr and throughPr must be positive integers');
  }
  if (requested.baselinePr === requested.throughPr) fail('baselinePr and throughPr must differ');

  const endpoints = evidence.endpoints;
  if (!isObject(endpoints)) fail('endpoint facts are missing');
  validateEndpoint(endpoints.baseline, 'baseline', requested.baselinePr);
  validateEndpoint(endpoints.through, 'through', requested.throughPr);
  if (Date.parse(endpoints.through.mergedAt) <= Date.parse(endpoints.baseline.mergedAt)) {
    fail('through endpoint must merge after the baseline endpoint');
  }

  const source = evidence.source;
  if (!isObject(source) || !nonemptyString(source.adapter)) fail('source adapter provenance is missing');

  const candidates = evidence.candidates;
  if (!isObject(candidates) || candidates.complete !== true) {
    fail('candidate PR collection is incomplete');
  }
  if (!nonnegativeInteger(candidates.reportedCount) || !Array.isArray(candidates.items)) {
    fail('candidate PR count metadata is malformed');
  }
  if (candidates.reportedCount !== candidates.items.length) {
    fail(`candidate PR count mismatch: reported ${candidates.reportedCount}, received ${candidates.items.length}`);
  }

  const prNumbers = new Set();
  candidates.items.forEach((candidate, index) => validateCandidate(candidate, index, prNumbers));
  const through = candidates.items.find((candidate) => candidate.number === requested.throughPr);
  if (!through) fail(`through PR #${requested.throughPr} is missing from candidate evidence`);
  if (through.mergedAt !== endpoints.through.mergedAt || through.baseRefName !== endpoints.through.baseRefName) {
    fail(`through PR #${requested.throughPr} candidate facts disagree with its endpoint facts`);
  }
  return evidence;
}
