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
#   PR_HEAD_SHA        - current pull_request.head.sha
#   REVIEW_BODIES_B64  - newline-separated list of base64-encoded review
#                        bodies, one per authoritative review, in
#                        chronological order (oldest first) as returned by
#                        GitHub. Base64 encoding (rather than a JSON array)
#                        is deliberate: it needs no JSON re-parsing and no
#                        ad-hoc object-boundary splitting downstream, so a
#                        review body's own content (quotes, braces, embedded
#                        JSON-looking text) can never be misread as a
#                        structural delimiter.
#
# Output:
#   Exit 0 if the latest applicable current-head disposition is READY TO MERGE.
#   Exit 1 otherwise, with a diagnostic message on stderr.

set -euo pipefail

if [ -z "${PR_HEAD_SHA:-}" ]; then
  echo "error: PR_HEAD_SHA not set" >&2
  exit 1
fi

if [ -z "${REVIEW_BODIES_B64:-}" ]; then
  echo "error: REVIEW_BODIES_B64 not set" >&2
  exit 1
fi

# Extract the canonical disposition block from a review body, if present.
# Canonical format (strict: exactly these two lines, adjacent, at the end of
# the body, with "Reviewed head:" anchored to the start of its own line --
# no content between the lines, no blank line between the lines, no
# indentation before "Reviewed" (which would read as a Markdown code block),
# and no content after the disposition line):
#   Reviewed head: <SHA>
#   Disposition: **VALUE**
# Prints "head_sha disposition" if found, else nothing.
#
# [:space:] deliberately does NOT appear in the line-start/adjacency portions
# of this pattern: in Bash's regex engine it matches newline as well as
# horizontal whitespace, so using it there would let a blank line between the
# two canonical lines, or Markdown-code-block indentation before "Reviewed",
# both match as if they were the strict adjacent block. [[:blank:]] (the
# POSIX class for space and tab, and nothing else -- notably NOT newline) is
# used for intentionally-tolerated in-line whitespace instead. A bracket
# expression like [\ \t] does NOT mean "space or tab": inside [...], a
# backslash is an ordinary literal character in POSIX bracket expressions, so
# that construct actually matches a literal backslash, a literal space, or a
# literal letter "t" -- meaning the token "Reviewedthead" would wrongly parse
# as "Reviewed<tab>head". [[:blank:]] has no such trap. The line break
# between the two canonical lines is a single literal newline with nothing
# else permitted around it.
extract_canonical_disposition() {
  local body="$1"

  # Group 1 is the (start-of-string | newline) anchor; group 2 is the head
  # SHA; group 3 is the disposition value. "Reviewed" must immediately follow
  # that anchor with no leading whitespace of any kind, so a body like
  # "Example Reviewed head: <sha>" (same-line prefix) or "    Reviewed head:
  # <sha>" (code-block indentation) does not match. The exactly-one-newline
  # between the SHA and "Disposition:" rejects a blank line between them.
  if [[ "$body" =~ (^|$'\n')Reviewed[[:blank:]]+head:[[:blank:]]*([a-f0-9]+)[[:blank:]]*$'\n'Disposition:[[:blank:]]*\*\*([A-Z][A-Z _-]*)\*\*[[:space:]]*$ ]]; then
    echo "${BASH_REMATCH[2]} ${BASH_REMATCH[3]}"
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

# Latest applicable disposition for the current head. Reviews are processed in
# the order given (chronological); the last one matching the current head wins.
# Older-head canonical blocks are ignored entirely — this gate enforces
# current-head binding only, not historical reviewer authority.
latest_disp=""

while IFS= read -r b64_line; do
  [ -z "$b64_line" ] && continue

  body=$(printf '%s' "$b64_line" | base64 -d 2>/dev/null) || continue

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
done <<<"$REVIEW_BODIES_B64"

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
