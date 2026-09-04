#!/usr/bin/env bash
# PR disposition merge-gate evaluator.
#
# Enforces exactly one invariant: the latest applicable canonical reviewer
# disposition for the current PR head must be READY TO MERGE.
#
# Reviewer disposition vocabulary (exactly two values):
#   READY TO MERGE    - reviewer authorizes merge of this exact head
#   CHANGES REQUIRED  - reviewer blocks merge; remediation required
#
# This evaluator does not reason about CI, mergeability, unresolved threads,
# reviewer identity, review state (APPROVED/CHANGES_REQUESTED/COMMENTED/
# DISMISSED), or approval/dismissal lifetime. Those richer lifecycle concerns
# belong to infra/dev/pr-watch.mjs. This gate answers exactly one question:
# does the latest applicable review body for the current PR head end in a
# canonical READY TO MERGE disposition block? CI and other branch-protection
# requirements are enforced independently by GitHub.
#
# Input (environment variables):
#   PR_HEAD_SHA   - current pull_request.head.sha
#   REVIEWS_JSON  - JSON array of review objects: [{ "body": "..." }, ...]
#                   in chronological order (oldest first), as returned by GitHub.
#
# Output:
#   Exit 0 if the latest applicable current-head disposition is READY TO MERGE.
#   Exit 1 otherwise, with a diagnostic message on stderr.

set -euo pipefail

if [ -z "${PR_HEAD_SHA:-}" ]; then
  echo "error: PR_HEAD_SHA not set" >&2
  exit 1
fi

if [ -z "${REVIEWS_JSON:-}" ]; then
  echo "error: REVIEWS_JSON not set" >&2
  exit 1
fi

# Extract the canonical disposition block from a review body, if present.
# Canonical format (strict: exactly these two lines, adjacent, at the end of
# the body — no content between them, no content after):
#   Reviewed head: <SHA>
#   Disposition: **VALUE**
# Prints "head_sha disposition" if found, else nothing.
extract_canonical_disposition() {
  local body="$1"

  if [[ "$body" =~ Reviewed[[:space:]]+head:[[:space:]]*([a-f0-9]+)[[:space:]]*$'\n'[[:space:]]*Disposition:[[:space:]]*\*\*([A-Z][A-Z_[:space:]-]*)\*\*[[:space:]]*$ ]]; then
    echo "${BASH_REMATCH[1]} ${BASH_REMATCH[2]}"
  fi
}

# Only two dispositions exist. Anything else (including removed legacy values
# such as READY AFTER CI or NON-BLOCKING FOLLOW-UPS ONLY) is unsupported.
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

# Split the REVIEWS_JSON array into individual review-body strings, in order.
# No external JSON tooling required (bash + sed only), so this evaluator has
# no runtime dependency beyond bash itself and can be tested anywhere.
extract_bodies() {
  local json="$1"

  # Split "}, {" object boundaries onto their own lines, strip the outer [ ].
  local normalized
  normalized=$(echo "$json" | sed 's/}, {/\n/g; s/^\[//; s/\]$//')

  while IFS= read -r line; do
    [ -z "$line" ] && continue

    local body
    body=$(echo "$line" | sed -n 's/.*"body":[[:space:]]*"\(.*\)"[[:space:]]*}.*/\1/p')
    [ -z "$body" ] && body=$(echo "$line" | sed -n 's/.*"body":[[:space:]]*"\(.*\)"[[:space:]]*,.*/\1/p')
    [ -z "$body" ] && continue

    # Unescape JSON string escapes
    body="${body//\\n/$'\n'}"
    body="${body//\\\"/\"}"
    body="${body//\\\\/\\}"

    printf '%s\0' "$body"
  done <<<"$normalized"
}

# Latest applicable disposition for the current head. Reviews are processed in
# the order given (chronological); the last one matching the current head wins.
# Older-head canonical blocks are ignored entirely — this gate enforces
# current-head binding only, not historical reviewer authority.
latest_disp=""

while IFS= read -r -d '' body; do
  canonical=$(extract_canonical_disposition "$body") || true
  [ -z "$canonical" ] && continue

  head=$(echo "$canonical" | cut -d' ' -f1)
  disp=$(echo "$canonical" | cut -d' ' -f2-)

  # Ignore canonical blocks for any head other than the current one.
  [ "$head" != "$PR_HEAD_SHA" ] && continue

  if ! is_valid_disposition "$disp"; then
    echo "PR disposition gate failed: unsupported disposition '$disp' for current head $PR_HEAD_SHA." >&2
    exit 1
  fi

  latest_disp="$disp"
done < <(extract_bodies "$REVIEWS_JSON")

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
