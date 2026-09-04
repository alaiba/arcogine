#!/usr/bin/env bash
# Tests for check-pr-disposition.sh evaluator.
#
# The evaluator answers exactly one question: does the latest applicable
# review body for the current PR head end in a canonical READY TO MERGE
# disposition block? These tests exercise that invariant plus the parser's
# false-positive guards. They deliberately do not exercise CI, mergeability,
# reviewer identity, or approval/dismissal semantics — those belong to
# infra/dev/pr-watch.mjs, not this gate.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DISPOSITION_SCRIPT="$SCRIPT_DIR/check-pr-disposition.sh"

test_count=0
pass_count=0
fail_count=0

test_case() {
  local name="$1"
  local expected_exit="$2"
  local head_sha="$3"
  local reviews_json="$4"

  test_count=$((test_count + 1))

  export PR_HEAD_SHA="$head_sha"
  export REVIEWS_JSON="$reviews_json"

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
test_case \
  "no reviews -> FAIL" \
  1 \
  "$CURRENT" \
  '[]'

# 2. Current-head READY TO MERGE -> PASS
test_case \
  "current-head READY TO MERGE -> PASS" \
  0 \
  "$CURRENT" \
  '[{"body":"Reviewed head: abc123def456\nDisposition: **READY TO MERGE**"}]'

# 3. Current-head CHANGES REQUIRED -> FAIL
test_case \
  "current-head CHANGES REQUIRED -> FAIL" \
  1 \
  "$CURRENT" \
  '[{"body":"Reviewed head: abc123def456\nDisposition: **CHANGES REQUIRED**"}]'

# 4. Stale-head READY TO MERGE -> FAIL
test_case \
  "stale-head READY TO MERGE -> FAIL" \
  1 \
  "$CURRENT" \
  '[{"body":"Reviewed head: fed654cba321\nDisposition: **READY TO MERGE**"}]'

# 5. Current-head READY followed by current-head CHANGES REQUIRED -> FAIL
test_case \
  "current-head READY then CHANGES REQUIRED -> FAIL" \
  1 \
  "$CURRENT" \
  '[{"body":"Reviewed head: abc123def456\nDisposition: **READY TO MERGE**"},{"body":"Reviewed head: abc123def456\nDisposition: **CHANGES REQUIRED**"}]'

# 6. Current-head CHANGES REQUIRED followed by current-head READY -> PASS
test_case \
  "current-head CHANGES REQUIRED then READY -> PASS" \
  0 \
  "$CURRENT" \
  '[{"body":"Reviewed head: abc123def456\nDisposition: **CHANGES REQUIRED**"},{"body":"Reviewed head: abc123def456\nDisposition: **READY TO MERGE**"}]'

# 7. Old-head blocker followed by current-head READY -> PASS
test_case \
  "old-head blocker + current-head READY -> PASS" \
  0 \
  "$CURRENT" \
  '[{"body":"Reviewed head: fed654cba321\nDisposition: **CHANGES REQUIRED**"},{"body":"Reviewed head: abc123def456\nDisposition: **READY TO MERGE**"}]'

# 8. Removed disposition READY AFTER CI -> FAIL/unrecognized
test_case \
  "removed disposition READY AFTER CI -> FAIL" \
  1 \
  "$CURRENT" \
  '[{"body":"Reviewed head: abc123def456\nDisposition: **READY AFTER CI**"}]'

# 9. Removed disposition NON-BLOCKING FOLLOW-UPS ONLY -> FAIL/unrecognized
test_case \
  "removed disposition NON-BLOCKING FOLLOW-UPS ONLY -> FAIL" \
  1 \
  "$CURRENT" \
  '[{"body":"Reviewed head: abc123def456\nDisposition: **NON-BLOCKING FOLLOW-UPS ONLY**"}]'

# 10. Prose mentioning READY TO MERGE (no canonical block) -> FAIL
test_case \
  "prose mentioning READY TO MERGE, no canonical block -> FAIL" \
  1 \
  "$CURRENT" \
  '[{"body":"I think this is READY TO MERGE once CI passes."}]'

# 11. Quoted/example READY block (not this review'\''s own final block) -> FAIL
test_case \
  "quoted canonical block inside prose -> FAIL" \
  1 \
  "$CURRENT" \
  '[{"body":"Prior review said:\nReviewed head: abc123def456\nDisposition: **READY TO MERGE**\nBut I now have new concerns."}]'

# 12. READY block followed by substantive text -> FAIL
test_case \
  "READY block followed by substantive text -> FAIL" \
  1 \
  "$CURRENT" \
  '[{"body":"Reviewed head: abc123def456\nDisposition: **READY TO MERGE**\nActually wait, one more thing to check."}]'

# 13. Malformed canonical block (missing head SHA) -> FAIL
test_case \
  "malformed canonical block (no head SHA) -> FAIL" \
  1 \
  "$CURRENT" \
  '[{"body":"Disposition: **READY TO MERGE**"}]'

# 14. Any review carrying a canonical READY block is usable, regardless of formal
#     GitHub review action type. The evaluator does not filter or reason about
#     review state (APPROVED/CHANGES_REQUESTED/COMMENTED/DISMISSED) at all — it
#     only reads bodies. This is the repository's normal reviewer mechanism
#     (informal COMMENTED reviews carrying the canonical disposition).
test_case \
  "canonical READY block is usable regardless of review action type -> PASS" \
  0 \
  "$CURRENT" \
  '[{"body":"Reviewed head: abc123def456\nDisposition: **READY TO MERGE**"}]'

# 15. Pagination preserves true review ordering: an aggregated multi-page
#     REVIEWS_JSON must evaluate reviews in chronological order so the latest
#     applicable disposition (not merely the first or a random one) wins.
test_case \
  "aggregated multi-page reviews evaluated in chronological order -> PASS" \
  0 \
  "$CURRENT" \
  '[{"body":"Reviewed head: abc123def456\nDisposition: **CHANGES REQUIRED**"},{"body":"unrelated comment, no disposition"},{"body":"Reviewed head: abc123def456\nDisposition: **READY TO MERGE**"}]'

# Extra: whitespace tolerance in the canonical block
test_case \
  "canonical block with extra whitespace -> PASS" \
  0 \
  "$CURRENT" \
  '[{"body":"Some review text\nReviewed head:   abc123def456   \nDisposition:   **READY TO MERGE**   "}]'

# Extra: unsupported disposition value entirely (not a legacy removed value)
test_case \
  "unsupported disposition value -> FAIL" \
  1 \
  "$CURRENT" \
  '[{"body":"Reviewed head: abc123def456\nDisposition: **APPROVED**"}]'

# Summary
echo ""
echo "Test Results: $pass_count/$test_count passed"
if [ $fail_count -gt 0 ]; then
  echo "Failed: $fail_count"
  exit 1
fi
exit 0
