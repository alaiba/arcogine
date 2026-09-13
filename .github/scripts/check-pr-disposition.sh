#!/usr/bin/env bash
# PR disposition merge-gate evaluator.
#
# Enforces Arcogine's review-authorization invariant for the current PR head:
#
#   - a PR with independently verified trusted Dependabot provenance is
#     authorized without requiring a positive reviewer disposition;
#   - every other PR requires the latest applicable canonical reviewer
#     disposition for the current head to be READY TO MERGE;
#   - an explicit current-head CHANGES REQUIRED disposition blocks either path.
#
# Reviewer disposition vocabulary (exactly two values):
#   READY TO MERGE    - reviewer authorizes merge of this exact head
#   CHANGES REQUIRED  - reviewer blocks merge; remediation required
#
# This evaluator does not reason about CI, mergeability, unresolved threads,
# native GitHub review state (APPROVED/CHANGES_REQUESTED/COMMENTED/DISMISSED),
# or approval/dismissal lifetime. Those richer lifecycle concerns belong to
# infra/dev/pr-watch.mjs. CI and other branch-protection requirements are
# enforced independently by GitHub.
#
# PR_TRUSTED_DEPENDABOT is supplied only by the trusted base-side workflow
# after check-dependabot-provenance.sh verifies both GitHub PR identity and the
# current PR commit set. Candidate PR content cannot set this value.
#
# Input (environment variables):
#   PR_HEAD_SHA           - current pull_request.head.sha
#   PR_TRUSTED_DEPENDABOT - "true" only after trusted provenance verification
#   REVIEW_BODIES_B64     - newline-separated list of base64-encoded review
#                           bodies, one per authoritative review, in
#                           chronological order (oldest first) as returned by
#                           GitHub.
#
# Output:
#   Exit 0 when the current head is authorized by trusted Dependabot
#   provenance or by a current-head READY TO MERGE disposition, unless the
#   latest current-head disposition is CHANGES REQUIRED.
#   Exit 1 otherwise, with a diagnostic message on stderr.

set -euo pipefail

if [ -z "${PR_HEAD_SHA:-}" ]; then
  echo "error: PR_HEAD_SHA not set" >&2
  exit 1
fi

# Extract the canonical disposition block from a review body, if present.
extract_canonical_disposition() {
  local body="$1"

  if [[ "$body" =~ (^|$'\n')Reviewed[[:blank:]]+head:[[:blank:]]*([a-f0-9]+)[[:blank:]]*$'\n'Disposition:[[:blank:]]*\*\*([A-Z][A-Z _-]*)\*\*[[:space:]]*$ ]]; then
    echo "${BASH_REMATCH[2]} ${BASH_REMATCH[3]}"
  fi
}

is_valid_disposition() {
  case "$1" in
    "READY TO MERGE"|"CHANGES REQUIRED")
      return 0
      ;;
    *)
      return 1
      ;;
  esac
}

latest_disp=""

# Trusted Dependabot PRs may legitimately have no reviews at all. Parse review
# bodies when present so an explicit current-head CHANGES REQUIRED can revoke
# the default provenance authorization.
if [ -n "${REVIEW_BODIES_B64:-}" ]; then
  while IFS= read -r b64_line; do
    [ -z "$b64_line" ] && continue

    body=$(printf '%s' "$b64_line" | base64 -d 2>/dev/null) || continue

    canonical=$(extract_canonical_disposition "$body") || true
    [ -z "$canonical" ] && continue

    head=$(echo "$canonical" | cut -d' ' -f1)
    disp=$(echo "$canonical" | cut -d' ' -f2-)

    [ "$head" != "$PR_HEAD_SHA" ] && continue

    if ! is_valid_disposition "$disp"; then
      echo "PR disposition gate failed: unsupported disposition '$disp' for current head $PR_HEAD_SHA." >&2
      exit 1
    fi

    latest_disp="$disp"
  done <<<"$REVIEW_BODIES_B64"
fi

if [ "$latest_disp" = "CHANGES REQUIRED" ]; then
  echo "PR disposition gate failed: latest current-head disposition is CHANGES REQUIRED." >&2
  exit 1
fi

if [ "${PR_TRUSTED_DEPENDABOT:-false}" = "true" ]; then
  echo "PR disposition gate passed: current head $PR_HEAD_SHA has trusted Dependabot provenance and no current-head CHANGES REQUIRED disposition."
  exit 0
fi

if [ -z "$latest_disp" ]; then
  echo "PR disposition gate failed: no canonical reviewer disposition exists for current head $PR_HEAD_SHA." >&2
  exit 1
fi

if [ "$latest_disp" != "READY TO MERGE" ]; then
  echo "PR disposition gate failed: latest current-head disposition is $latest_disp; READY TO MERGE is required." >&2
  exit 1
fi

echo "PR disposition gate passed: current head $PR_HEAD_SHA has canonical READY TO MERGE disposition."
exit 0
