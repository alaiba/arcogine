#!/usr/bin/env bash
# Tests for check-pr-disposition.sh evaluator.
#
# The evaluator answers exactly one question: does the latest applicable
# review body for the current PR head end in a canonical READY TO MERGE
# disposition block? These tests exercise that invariant plus the parser's
# false-positive guards. They deliberately do not exercise CI, mergeability,
# reviewer identity, or approval/dismissal semantics — those belong to
# infra/dev/pr-watch.mjs, not this gate.
#
# Input format matches the workflow's actual output: REVIEW_BODIES_B64 is a
# newline-separated list of base64-encoded review bodies, in chronological
# order. Building it with base64 here (rather than hand-writing a JSON array)
# is deliberate: it exercises the exact same encode/decode path production
# uses, so a regression in that path (as opposed to a synthetic JSON fixture
# that never touches it) fails these tests too.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DISPOSITION_SCRIPT="$SCRIPT_DIR/check-pr-disposition.sh"

test_count=0
pass_count=0
fail_count=0

# Encode one or more review bodies (each a full multi-line string, passed as
# a separate argument) into the newline-separated base64 list the evaluator
# expects.
encode_bodies() {
  local out=""
  local body
  for body in "$@"; do
    local b64
    b64=$(printf '%s' "$body" | base64 -w0)
    if [ -z "$out" ]; then
      out="$b64"
    else
      out="$out"$'\n'"$b64"
    fi
  done
  printf '%s' "$out"
}

test_case() {
  local name="$1"
  local expected_exit="$2"
  local head_sha="$3"
  shift 3
  local bodies_b64
  bodies_b64=$(encode_bodies "$@")

  test_count=$((test_count + 1))

  export PR_HEAD_SHA="$head_sha"
  export REVIEW_BODIES_B64="$bodies_b64"

  local exit_code=0
  local output
  output=$("$DISPOSITION_SCRIPT" 2>&1) || exit_code=$?

  if [ "$exit_code" -eq "$expected_exit" ]; then
    echo "✓ Test $test_count: $name"
    pass_count=$((pass_count + 1))
  else
    echo "✗ Test $test_count: $name"
    echo "  Expected exit code: $expected_exit"
    echo "  Got exit code: $exit_code"
    echo "  Output: $output"
    fail_count=$((fail_count + 1))
  fi
}

CURRENT="abc123def456"
OLD="fed654cba321"

# 1. No reviews -> FAIL
test_count=$((test_count + 1))
export PR_HEAD_SHA="$CURRENT"
export REVIEW_BODIES_B64=""
exit_code=0
output=$("$DISPOSITION_SCRIPT" 2>&1) || exit_code=$?
if [ "$exit_code" -eq 1 ]; then
  echo "✓ Test $test_count: no reviews -> FAIL"
  pass_count=$((pass_count + 1))
else
  echo "✗ Test $test_count: no reviews -> FAIL (got $exit_code: $output)"
  fail_count=$((fail_count + 1))
fi

# 2. Current-head READY TO MERGE -> PASS
test_case \
  "current-head READY TO MERGE -> PASS" \
  0 \
  "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **READY TO MERGE**"

# 3. Current-head CHANGES REQUIRED -> FAIL
test_case \
  "current-head CHANGES REQUIRED -> FAIL" \
  1 \
  "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **CHANGES REQUIRED**"

# 4. Stale-head READY TO MERGE -> FAIL
test_case \
  "stale-head READY TO MERGE -> FAIL" \
  1 \
  "$CURRENT" \
  "Reviewed head: $OLD
Disposition: **READY TO MERGE**"

# 5. Current-head READY followed by current-head CHANGES REQUIRED -> FAIL
test_case \
  "current-head READY then CHANGES REQUIRED -> FAIL" \
  1 \
  "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **READY TO MERGE**" \
  "Reviewed head: $CURRENT
Disposition: **CHANGES REQUIRED**"

# 6. Current-head CHANGES REQUIRED followed by current-head READY -> PASS
test_case \
  "current-head CHANGES REQUIRED then READY -> PASS" \
  0 \
  "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **CHANGES REQUIRED**" \
  "Reviewed head: $CURRENT
Disposition: **READY TO MERGE**"

# 7. Old-head blocker followed by current-head READY -> PASS
test_case \
  "old-head blocker + current-head READY -> PASS" \
  0 \
  "$CURRENT" \
  "Reviewed head: $OLD
Disposition: **CHANGES REQUIRED**" \
  "Reviewed head: $CURRENT
Disposition: **READY TO MERGE**"

# 8. Removed disposition READY AFTER CI -> FAIL/unrecognized
test_case \
  "removed disposition READY AFTER CI -> FAIL" \
  1 \
  "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **READY AFTER CI**"

# 9. Removed disposition NON-BLOCKING FOLLOW-UPS ONLY -> FAIL/unrecognized
test_case \
  "removed disposition NON-BLOCKING FOLLOW-UPS ONLY -> FAIL" \
  1 \
  "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **NON-BLOCKING FOLLOW-UPS ONLY**"

# 10. Prose mentioning READY TO MERGE (no canonical block) -> FAIL
test_case \
  "prose mentioning READY TO MERGE, no canonical block -> FAIL" \
  1 \
  "$CURRENT" \
  "I think this is READY TO MERGE once CI passes."

# 11. Quoted/example READY block (not this review's own final block) -> FAIL
test_case \
  "quoted canonical block inside prose -> FAIL" \
  1 \
  "$CURRENT" \
  "Prior review said:
Reviewed head: $CURRENT
Disposition: **READY TO MERGE**
But I now have new concerns."

# 12. READY block followed by substantive text -> FAIL
test_case \
  "READY block followed by substantive text -> FAIL" \
  1 \
  "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **READY TO MERGE**
Actually wait, one more thing to check."

# 13. Malformed canonical block (missing head SHA) -> FAIL
test_case \
  "malformed canonical block (no head SHA) -> FAIL" \
  1 \
  "$CURRENT" \
  "Disposition: **READY TO MERGE**"

# 14. Any authoritative review carrying a canonical READY block is usable,
#     regardless of formal GitHub review action type. The evaluator does not
#     filter or reason about review state (APPROVED/CHANGES_REQUESTED/
#     COMMENTED/DISMISSED) at all -- it only reads bodies. Author-association
#     filtering happens upstream in the workflow, not here.
test_case \
  "canonical READY block is usable regardless of review action type -> PASS" \
  0 \
  "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **READY TO MERGE**"

# 15. Pagination preserves true review ordering: an aggregated multi-page
#     review list must be evaluated in chronological order so the latest
#     applicable disposition (not merely the first or a random one) wins.
test_case \
  "aggregated multi-page reviews evaluated in chronological order -> PASS" \
  0 \
  "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **CHANGES REQUIRED**" \
  "unrelated comment, no disposition" \
  "Reviewed head: $CURRENT
Disposition: **READY TO MERGE**"

# 16. Whitespace tolerance in the canonical block
test_case \
  "canonical block with extra whitespace -> PASS" \
  0 \
  "$CURRENT" \
  "Some review text
Reviewed head:   $CURRENT
Disposition:   **READY TO MERGE**   "

# 17. Unsupported disposition value entirely (not a legacy removed value)
test_case \
  "unsupported disposition value -> FAIL" \
  1 \
  "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **APPROVED**"

# 18. REV-001 regression: a controlling current-head disposition followed by
#     a later, unrelated review with no disposition must still authorize
#     merge. This is the exact production-shaped scenario where naive
#     object-boundary text splitting (rather than a decode that needs no
#     re-parsing at all) previously hid the earlier disposition.
test_case \
  "REV-001: controlling disposition survives a later unrelated review -> PASS" \
  0 \
  "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **READY TO MERGE**" \
  "unrelated later comment with no disposition"

# 19. REV-009 regression: "Reviewed head:" must be anchored to the start of
#     its own line. A body where the marker is preceded by other text on the
#     same line is not a canonical block, even though the two-line shape
#     otherwise matches.
test_case \
  "REV-009: Reviewed head: not anchored to line start -> FAIL" \
  1 \
  "$CURRENT" \
  "Example Reviewed head: $CURRENT
Disposition: **READY TO MERGE**"

# 20. Many reviews (exercises multi-line REVIEW_BODIES_B64 input beyond a
#     trivial 2-3 line case, proving the newline-delimited decode scales).
test_case \
  "many reviews, controlling disposition in the middle -> PASS" \
  0 \
  "$CURRENT" \
  "first unrelated comment" \
  "Reviewed head: $OLD
Disposition: **CHANGES REQUIRED**" \
  "Reviewed head: $CURRENT
Disposition: **READY TO MERGE**" \
  "later unrelated comment" \
  "another later unrelated comment"

# 21. REV-009 (second pass): indented "Reviewed head:" reads as a Markdown
#     code block, not the live canonical block, and must not match. Bash's
#     [[:space:]] class matches newline as well as horizontal whitespace, so
#     an earlier fix that tolerated leading [[:space:]]* before "Reviewed"
#     accidentally tolerated leading indentation too.
test_case \
  "REV-009: indented Reviewed head: (code-block formatting) -> FAIL" \
  1 \
  "$CURRENT" \
  "Example:
    Reviewed head: $CURRENT
    Disposition: **READY TO MERGE**"

# 22. REV-009 (second pass): a blank line between "Reviewed head:" and
#     "Disposition:" violates the documented strict-adjacency requirement and
#     must not match, even though both lines are otherwise well-formed.
test_case \
  "REV-009: blank line between the two canonical lines -> FAIL" \
  1 \
  "$CURRENT" \
  "Reviewed head: $CURRENT

Disposition: **READY TO MERGE**"

# Summary
echo ""
echo "Test Results: $pass_count/$test_count passed"
if [ $fail_count -gt 0 ]; then
  echo "Failed: $fail_count"
  exit 1
fi
exit 0
