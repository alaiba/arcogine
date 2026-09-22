#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo="$(cd "$script_dir/../.." && pwd)"
workflow="$repo/.github/workflows/continuous-improvement-reminder.yml"
failures=0

require_literal() {
  local description="$1"
  local literal="$2"
  if grep -Fq -- "$literal" "$workflow"; then
    echo "PASS: $description"
  else
    echo "FAIL: $description"
    failures=$((failures + 1))
  fi
}

reject_literal() {
  local description="$1"
  local literal="$2"
  if grep -Fq -- "$literal" "$workflow"; then
    echo "FAIL: $description"
    failures=$((failures + 1))
  else
    echo "PASS: $description"
  fi
}

require_literal "scheduled trigger is present" "  schedule:"
reject_literal "branch-selectable workflow_dispatch is absent" "workflow_dispatch"
require_literal "fixed reminder concurrency group is present" "  group: continuous-improvement-reminder"
require_literal "running reminder is never canceled" "  cancel-in-progress: false"
require_literal "issue permission stays narrow" "  issues: write"
require_literal "checkpoint identity stays exact" "TITLE: Continuous improvement checkpoint"
require_literal "workflow still creates the reminder issue" "gh issue create"

if [ "$failures" -gt 0 ]; then
  echo "$failures continuous-improvement reminder contract check(s) failed."
  exit 1
fi

echo "Continuous-improvement reminder contract checks passed."
