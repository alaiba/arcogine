#!/usr/bin/env bash
# Tests for check-dependabot-provenance.sh.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROVENANCE_SCRIPT="$SCRIPT_DIR/check-dependabot-provenance.sh"

pass=0
fail=0
count=0

commit_json() {
  local sha="$1"
  local login="$2"
  local type="$3"
  local verified="$4"
  jq -nc \
    --arg sha "$sha" \
    --arg login "$login" \
    --arg type "$type" \
    --argjson verified "$verified" \
    '{sha:$sha, author:{login:$login,type:$type}, commit:{verification:{verified:$verified}}}'
}

run_case() {
  local name="$1"
  local expected="$2"
  local head="$3"
  local author_login="$4"
  local author_type="$5"
  local commits_json="$6"

  count=$((count + 1))
  export PR_HEAD_SHA="$head"
  export PR_AUTHOR_LOGIN="$author_login"
  export PR_AUTHOR_TYPE="$author_type"
  export PR_COMMITS_JSON_B64
  PR_COMMITS_JSON_B64=$(printf '%s' "$commits_json" | base64 -w0)

  local code=0
  local output
  output=$("$PROVENANCE_SCRIPT" 2>&1) || code=$?
  if [ "$code" -eq "$expected" ]; then
    echo "✓ Provenance $count: $name"
    pass=$((pass + 1))
  else
    echo "✗ Provenance $count: $name"
    echo "  expected: $expected; got: $code"
    echo "  output: $output"
    fail=$((fail + 1))
  fi
}

HEAD="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
OLD="bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"

bot_head=$(commit_json "$HEAD" 'dependabot[bot]' 'Bot' true)
bot_old=$(commit_json "$OLD" 'dependabot[bot]' 'Bot' true)
user_head=$(commit_json "$HEAD" 'alaiba' 'User' true)
unsigned_bot=$(commit_json "$HEAD" 'dependabot[bot]' 'Bot' false)
other_bot=$(commit_json "$HEAD" 'other[bot]' 'Bot' true)

run_case \
  "single verified Dependabot commit -> PASS" \
  0 "$HEAD" 'dependabot[bot]' 'Bot' \
  "[$bot_head]"

run_case \
  "multiple verified Dependabot commits ending at current head -> PASS" \
  0 "$HEAD" 'dependabot[bot]' 'Bot' \
  "[$bot_old,$bot_head]"

run_case \
  "maintainer-authored extra commit revokes bypass -> FAIL" \
  1 "$HEAD" 'dependabot[bot]' 'Bot' \
  "[$bot_old,$user_head]"

run_case \
  "unsigned Dependabot commit -> FAIL" \
  1 "$HEAD" 'dependabot[bot]' 'Bot' \
  "[$unsigned_bot]"

run_case \
  "unrelated bot commit -> FAIL" \
  1 "$HEAD" 'dependabot[bot]' 'Bot' \
  "[$other_bot]"

run_case \
  "Dependabot commits under non-Dependabot PR author -> FAIL" \
  1 "$HEAD" 'alaiba' 'User' \
  "[$bot_head]"

run_case \
  "commit list not bound to current head -> FAIL" \
  1 "$HEAD" 'dependabot[bot]' 'Bot' \
  "[$bot_old]"

run_case \
  "empty commit list -> FAIL" \
  1 "$HEAD" 'dependabot[bot]' 'Bot' \
  '[]'

echo ""
echo "Dependabot provenance test results: $pass/$count passed"
if [ "$fail" -ne 0 ]; then
  echo "Failed: $fail"
  exit 1
fi
