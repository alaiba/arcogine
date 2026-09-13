#!/usr/bin/env bash
# Tests for check-pr-disposition.sh evaluator.
#
# The evaluator answers one review-authorization question for the current PR
# head: is the PR independently verified as trusted Dependabot automation, or
# does the latest applicable review body end in a canonical READY TO MERGE
# disposition block? An explicit current-head CHANGES REQUIRED blocks either
# path.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DISPOSITION_SCRIPT="$SCRIPT_DIR/check-pr-disposition.sh"

test_count=0
pass_count=0
fail_count=0

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

# Ordinary PRs use reviewer authorization unless a test explicitly selects the
# separately verified trusted-Dependabot path.
export PR_TRUSTED_DEPENDABOT="false"

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
test_case "current-head READY TO MERGE -> PASS" 0 "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **READY TO MERGE**"

# 3. Current-head CHANGES REQUIRED -> FAIL
test_case "current-head CHANGES REQUIRED -> FAIL" 1 "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **CHANGES REQUIRED**"

# 4. Stale-head READY TO MERGE -> FAIL
test_case "stale-head READY TO MERGE -> FAIL" 1 "$CURRENT" \
  "Reviewed head: $OLD
Disposition: **READY TO MERGE**"

# 5. Current-head READY followed by current-head CHANGES REQUIRED -> FAIL
test_case "current-head READY then CHANGES REQUIRED -> FAIL" 1 "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **READY TO MERGE**" \
  "Reviewed head: $CURRENT
Disposition: **CHANGES REQUIRED**"

# 6. Current-head CHANGES REQUIRED followed by current-head READY -> PASS
test_case "current-head CHANGES REQUIRED then READY -> PASS" 0 "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **CHANGES REQUIRED**" \
  "Reviewed head: $CURRENT
Disposition: **READY TO MERGE**"

# 7. Old-head blocker followed by current-head READY -> PASS
test_case "old-head blocker + current-head READY -> PASS" 0 "$CURRENT" \
  "Reviewed head: $OLD
Disposition: **CHANGES REQUIRED**" \
  "Reviewed head: $CURRENT
Disposition: **READY TO MERGE**"

# 8-9. Removed dispositions are unsupported.
test_case "removed disposition READY AFTER CI -> FAIL" 1 "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **READY AFTER CI**"
test_case "removed disposition NON-BLOCKING FOLLOW-UPS ONLY -> FAIL" 1 "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **NON-BLOCKING FOLLOW-UPS ONLY**"

# 10-13. Non-canonical prose/shapes cannot authorize.
test_case "prose mentioning READY TO MERGE, no canonical block -> FAIL" 1 "$CURRENT" \
  "I think this is READY TO MERGE once CI passes."
test_case "quoted canonical block inside prose -> FAIL" 1 "$CURRENT" \
  "Prior review said:
Reviewed head: $CURRENT
Disposition: **READY TO MERGE**
But I now have new concerns."
test_case "READY block followed by substantive text -> FAIL" 1 "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **READY TO MERGE**
Actually wait, one more thing to check."
test_case "malformed canonical block (no head SHA) -> FAIL" 1 "$CURRENT" \
  "Disposition: **READY TO MERGE**"

# 14. Evaluator reads bodies, not native GitHub review action type.
test_case "canonical READY block is usable regardless of review action type -> PASS" 0 "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **READY TO MERGE**"

# 15. Chronological ordering: latest applicable disposition wins.
test_case "aggregated multi-page reviews evaluated in chronological order -> PASS" 0 "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **CHANGES REQUIRED**" \
  "unrelated comment, no disposition" \
  "Reviewed head: $CURRENT
Disposition: **READY TO MERGE**"

# 16. Whitespace tolerance.
test_case "canonical block with extra whitespace -> PASS" 0 "$CURRENT" \
  "Some review text
Reviewed head:   $CURRENT
Disposition:   **READY TO MERGE**   "

# 17. Unsupported value.
test_case "unsupported disposition value -> FAIL" 1 "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **APPROVED**"

# 18. A later unrelated review does not erase a controlling disposition.
test_case "review-ingestion: controlling disposition survives a later unrelated review -> PASS" 0 "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **READY TO MERGE**" \
  "unrelated later comment with no disposition"

# 19. Reviewed head marker must be anchored to its own line.
test_case "canonical-block anchoring: Reviewed head not anchored to line start -> FAIL" 1 "$CURRENT" \
  "Example Reviewed head: $CURRENT
Disposition: **READY TO MERGE**"

# 20. Many reviews preserve the controlling current-head disposition.
test_case "many reviews, controlling disposition in the middle -> PASS" 0 "$CURRENT" \
  "first unrelated comment" \
  "Reviewed head: $OLD
Disposition: **CHANGES REQUIRED**" \
  "Reviewed head: $CURRENT
Disposition: **READY TO MERGE**" \
  "later unrelated comment" \
  "another later unrelated comment"

# 21. Indented marker is a Markdown code block, not canonical disposition.
test_case "canonical-block anchoring: indented Reviewed head -> FAIL" 1 "$CURRENT" \
  "Example:
    Reviewed head: $CURRENT
    Disposition: **READY TO MERGE**"

# 22. Blank line breaks strict canonical adjacency.
test_case "canonical-block anchoring: blank line between canonical lines -> FAIL" 1 "$CURRENT" \
  "Reviewed head: $CURRENT

Disposition: **READY TO MERGE**"

# 23-24. Literal letter t cannot substitute for whitespace.
test_case "canonical whitespace: literal t at word boundary -> FAIL" 1 "$CURRENT" \
  "Reviewedthead:${CURRENT}
Disposition: **READY TO MERGE**"
test_case "canonical whitespace: literal t around head/value -> FAIL" 1 "$CURRENT" \
  "Reviewed head:t${CURRENT}
Disposition:t**READY TO MERGE**"

# 25. Separately verified trusted Dependabot provenance needs no positive review.
export PR_TRUSTED_DEPENDABOT="true"
test_case "trusted Dependabot provenance without reviews -> PASS" 0 "$CURRENT"

# 26. A current-head negative review revokes the default Dependabot authorization.
test_case "trusted Dependabot with current-head CHANGES REQUIRED -> FAIL" 1 "$CURRENT" \
  "Reviewed head: $CURRENT
Disposition: **CHANGES REQUIRED**"

# 27. A stale negative review does not revoke current trusted provenance.
test_case "trusted Dependabot with stale-head CHANGES REQUIRED -> PASS" 0 "$CURRENT" \
  "Reviewed head: $OLD
Disposition: **CHANGES REQUIRED**"

# 28. Without trusted provenance, no-review falls back to ordinary authorization.
export PR_TRUSTED_DEPENDABOT="false"
test_case "untrusted/no-review PR -> FAIL" 1 "$CURRENT"

# Exercise the trusted provenance verifier independently; this proves that a
# maintainer-authored extra commit cannot obtain PR_TRUSTED_DEPENDABOT=true.
bash "$SCRIPT_DIR/check-dependabot-provenance.test.sh"

# GitHub can reject an Actions workflow before scheduling any job, which makes
# evaluator-only tests insufficient. Lint every workflow definition from the
# always-running disposition test suite so malformed orchestration cannot
# reach main while the required gate is green.
bash "$SCRIPT_DIR/check-actions-workflows.sh"

echo ""
echo "Test Results: $pass_count/$test_count passed"
if [ $fail_count -gt 0 ]; then
  echo "Failed: $fail_count"
  exit 1
fi
exit 0
