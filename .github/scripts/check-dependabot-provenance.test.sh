#!/usr/bin/env bash
# Tests for check-dependabot-provenance.sh.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROVENANCE_SCRIPT="$SCRIPT_DIR/check-dependabot-provenance.sh"

pass=0
fail=0
count=0

ci_run_json() {
  local head="$1"
  local actor_login="$2"
  local actor_type="$3"
  local actor_id="$4"
  local event="${5:-pull_request}"
  local name="${6:-CI}"
  local pr_number="${7:-303}"
  jq -nc \
    --arg head "$head" \
    --arg login "$actor_login" \
    --arg type "$actor_type" \
    --argjson id "$actor_id" \
    --arg event "$event" \
    --arg name "$name" \
    --argjson pr "$pr_number" \
    '{name:$name,event:$event,head_sha:$head,actor:{login:$login,type:$type,id:$id},pull_requests:[{number:$pr}]}'
}

commit_json() {
  local sha="$1"
  local login="$2"
  local type="$3"
  local id="$4"
  local verified="$5"
  jq -nc \
    --arg sha "$sha" \
    --arg login "$login" \
    --arg type "$type" \
    --argjson id "$id" \
    --argjson verified "$verified" \
    '{sha:$sha,author:{login:$login,type:$type,id:$id},commit:{verification:{verified:$verified}}}'
}

commit_set_json() {
  printf '%s\n' "$@" | jq -s '.'
}

run_case() {
  local name="$1"
  local expected="$2"
  local author_login="$3"
  local author_type="$4"
  local author_id="$5"
  local run_json="$6"
  local commits_json="$7"

  count=$((count + 1))
  export PR_NUMBER=303
  export PR_HEAD_SHA="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
  export PR_AUTHOR_LOGIN="$author_login"
  export PR_AUTHOR_TYPE="$author_type"
  export PR_AUTHOR_ID="$author_id"
  export PR_HEAD_CI_RUN_B64=""
  export PR_COMMITS_B64=""

  if [ -n "$run_json" ]; then
    PR_HEAD_CI_RUN_B64=$(printf '%s' "$run_json" | base64 -w0)
  fi
  if [ -n "$commits_json" ]; then
    PR_COMMITS_B64=$(printf '%s' "$commits_json" | base64 -w0)
  fi

  local code=0
  local output
  output=$(bash "$PROVENANCE_SCRIPT" 2>&1) || code=$?
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
BOT_ID=49699333
USER_ID=17069361

bot_run=$(ci_run_json "$HEAD" 'dependabot[bot]' 'Bot' "$BOT_ID")
human_run=$(ci_run_json "$HEAD" 'alaiba' 'User' "$USER_ID")
wrong_bot_id_run=$(ci_run_json "$HEAD" 'dependabot[bot]' 'Bot' 999)
stale_run=$(ci_run_json "$OLD" 'dependabot[bot]' 'Bot' "$BOT_ID")
manual_run=$(ci_run_json "$HEAD" 'dependabot[bot]' 'Bot' "$BOT_ID" 'workflow_dispatch')
wrong_name_run=$(ci_run_json "$HEAD" 'dependabot[bot]' 'Bot' "$BOT_ID" 'pull_request' 'Other Workflow')
wrong_pr_run=$(ci_run_json "$HEAD" 'dependabot[bot]' 'Bot' "$BOT_ID" 'pull_request' 'CI' 999)

bot_head=$(commit_json "$HEAD" 'dependabot[bot]' 'Bot' "$BOT_ID" true)
bot_old=$(commit_json "$OLD" 'dependabot[bot]' 'Bot' "$BOT_ID" true)
human_old=$(commit_json "$OLD" 'alaiba' 'User' "$USER_ID" true)
unsigned_head=$(commit_json "$HEAD" 'dependabot[bot]' 'Bot' "$BOT_ID" false)
wrong_bot_head=$(commit_json "$HEAD" 'dependabot[bot]' 'Bot' 999 true)

valid_commits=$(commit_set_json "$bot_head")
valid_two_commits=$(commit_set_json "$bot_old" "$bot_head")
human_contaminated=$(commit_set_json "$human_old" "$bot_head")
unsigned_commits=$(commit_set_json "$unsigned_head")
wrong_bot_commits=$(commit_set_json "$wrong_bot_head")
stale_commits=$(commit_set_json "$bot_old")
empty_commits='[]'

run_case \
  "Dependabot opener + exact-head Dependabot CI actor + verified bot commit -> PASS" \
  0 'dependabot[bot]' 'Bot' "$BOT_ID" "$bot_run" "$valid_commits"

run_case \
  "multiple verified Dependabot commits ending at the current head -> PASS" \
  0 'dependabot[bot]' 'Bot' "$BOT_ID" "$bot_run" "$valid_two_commits"

run_case \
  "human opener cannot borrow Dependabot provenance -> FAIL" \
  1 'alaiba' 'User' "$USER_ID" "$bot_run" "$valid_commits"

run_case \
  "lookalike opener with wrong account id -> FAIL" \
  1 'dependabot[bot]' 'Bot' 999 "$bot_run" "$valid_commits"

run_case \
  "human CI actor revokes Dependabot bypass -> FAIL" \
  1 'dependabot[bot]' 'Bot' "$BOT_ID" "$human_run" "$valid_commits"

run_case \
  "lookalike CI actor with wrong account id -> FAIL" \
  1 'dependabot[bot]' 'Bot' "$BOT_ID" "$wrong_bot_id_run" "$valid_commits"

run_case \
  "CI run for stale head cannot authorize current head -> FAIL" \
  1 'dependabot[bot]' 'Bot' "$BOT_ID" "$stale_run" "$valid_commits"

run_case \
  "manual workflow run is not pull-request provenance -> FAIL" \
  1 'dependabot[bot]' 'Bot' "$BOT_ID" "$manual_run" "$valid_commits"

run_case \
  "different workflow cannot stand in for CI -> FAIL" \
  1 'dependabot[bot]' 'Bot' "$BOT_ID" "$wrong_name_run" "$valid_commits"

run_case \
  "CI run associated with another PR -> FAIL" \
  1 'dependabot[bot]' 'Bot' "$BOT_ID" "$wrong_pr_run" "$valid_commits"

run_case \
  "missing exact-head CI run -> FAIL closed" \
  1 'dependabot[bot]' 'Bot' "$BOT_ID" '' "$valid_commits"

run_case \
  "maintainer-authored commit anywhere in PR commit set -> FAIL" \
  1 'dependabot[bot]' 'Bot' "$BOT_ID" "$bot_run" "$human_contaminated"

run_case \
  "unsigned Dependabot commit -> FAIL" \
  1 'dependabot[bot]' 'Bot' "$BOT_ID" "$bot_run" "$unsigned_commits"

run_case \
  "lookalike commit author with wrong Dependabot account id -> FAIL" \
  1 'dependabot[bot]' 'Bot' "$BOT_ID" "$bot_run" "$wrong_bot_commits"

run_case \
  "empty PR commit set -> FAIL" \
  1 'dependabot[bot]' 'Bot' "$BOT_ID" "$bot_run" "$empty_commits"

run_case \
  "PR commit set not ending at exact current head -> FAIL" \
  1 'dependabot[bot]' 'Bot' "$BOT_ID" "$bot_run" "$stale_commits"

echo ""
echo "Dependabot provenance test results: $pass/$count passed"
if [ "$fail" -ne 0 ]; then
  echo "Failed: $fail"
  exit 1
fi
