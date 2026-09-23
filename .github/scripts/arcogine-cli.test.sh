#!/usr/bin/env bash
# Hermetic black-box regression coverage for the public ./arcogine interface.
# The real wrapper is copied into a temporary git repository and every native
# tool it dispatches to is replaced with a small logging stub. This protects
# orchestration behavior without running Gradle or the product.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(git -C "$SCRIPT_DIR" rev-parse --show-toplevel)"
SCRIPT_SOURCE="$REPO_ROOT/arcogine"
ORIGINAL_PATH="$PATH"
TEMP_ROOT="$(mktemp -d)"
trap 'rm -rf "$TEMP_ROOT"' EXIT

failures=0
cases=0
case_number=0
CASE_ROOT=""
TEST_REPO=""
EXPECTED_REPO_ROOT=""
FAKE_BIN=""
TEST_LOG=""
LAST_STATUS=0
LAST_OUTPUT=""

make_fakes() {
  local fake_bin="$1"

  cat > "$fake_bin/gradlew" <<'EOF'
#!/bin/bash
set -euo pipefail
log="${ARCOGINE_TEST_LOG:?}"
printf 'gradle cwd=%s' "$PWD" >> "$log"
for arg in "$@"; do printf ' arg=%s' "$arg" >> "$log"; done
printf '\n' >> "$log"

if [[ -n "${ARCOGINE_TEST_GRADLE_FAIL_ON:-}" ]]; then
  for arg in "$@"; do
    if [[ "$arg" == "$ARCOGINE_TEST_GRADLE_FAIL_ON" ]]; then
      exit 17
    fi
  done
fi
EOF

  cat > "$fake_bin/node" <<'EOF'
#!/bin/bash
set -euo pipefail
log="${ARCOGINE_TEST_LOG:?}"
printf 'node cwd=%s args=' "$PWD" >> "$log"
for arg in "$@"; do printf '|%s' "$arg" >> "$log"; done
printf '\n' >> "$log"
EOF

  for tool in trivy gitleaks; do
    cat > "$fake_bin/$tool" <<'EOF'
#!/bin/bash
set -euo pipefail
log="${ARCOGINE_TEST_LOG:?}"
printf '%s cwd=%s args=' "$(basename "$0")" "$PWD" >> "$log"
for arg in "$@"; do printf '|%s' "$arg" >> "$log"; done
printf '\n' >> "$log"
EOF
    chmod +x "$fake_bin/$tool"
  done

  chmod +x "$fake_bin"/*
}

start_case() {
  case_number=$((case_number + 1))
  CASE_ROOT="$TEMP_ROOT/case-$case_number"
  TEST_REPO="$CASE_ROOT/repo with spaces"
  FAKE_BIN="$CASE_ROOT/fake-bin"
  TEST_LOG="$CASE_ROOT/invocations.log"

  mkdir -p "$TEST_REPO/product" "$FAKE_BIN"
  git init -q "$TEST_REPO"
  EXPECTED_REPO_ROOT="$(git -C "$TEST_REPO" rev-parse --show-toplevel)"
  cp "$SCRIPT_SOURCE" "$TEST_REPO/arcogine"
  chmod +x "$TEST_REPO/arcogine"
  : > "$TEST_LOG"
  make_fakes "$FAKE_BIN"
  cp "$FAKE_BIN/gradlew" "$TEST_REPO/product/gradlew"
  chmod +x "$TEST_REPO/product/gradlew"

  unset ARCOGINE_TEST_GRADLE_FAIL_ON
  export ARCOGINE_TEST_LOG="$TEST_LOG"
}

run_script() {
  set +e
  LAST_OUTPUT="$(env PATH="$FAKE_BIN:$ORIGINAL_PATH" "$TEST_REPO/arcogine" "$@" 2>&1)"
  LAST_STATUS=$?
  set -e
}

record_result() {
  local name="$1" expected_status="$2" expected_text="${3:-}"
  cases=$((cases + 1))
  if [[ "$LAST_STATUS" -ne "$expected_status" ]]; then
    echo "FAIL: $name (exit $LAST_STATUS, expected $expected_status)"
    printf '  output: %s\n' "$LAST_OUTPUT"
    failures=$((failures + 1))
    return
  fi
  if [[ -n "$expected_text" ]] && ! grep -qF -- "$expected_text" <<< "$LAST_OUTPUT"; then
    echo "FAIL: $name (output did not contain '$expected_text')"
    printf '  output: %s\n' "$LAST_OUTPUT"
    failures=$((failures + 1))
    return
  fi
  echo "PASS: $name"
}

record_nonzero() {
  local name="$1"
  cases=$((cases + 1))
  if [[ "$LAST_STATUS" -ne 0 ]]; then
    echo "PASS: $name"
  else
    echo "FAIL: $name (expected a non-zero exit status)"
    printf '  output: %s\n' "$LAST_OUTPUT"
    failures=$((failures + 1))
  fi
}

assert_log_contains() {
  local name="$1" text="$2"
  cases=$((cases + 1))
  if grep -qF -- "$text" "$TEST_LOG"; then
    echo "PASS: $name"
  else
    echo "FAIL: $name (invocation log did not contain '$text')"
    printf '  log:\n%s\n' "$(cat "$TEST_LOG")"
    failures=$((failures + 1))
  fi
}

# Help and fixed-arity parsing.
start_case help-empty; run_script; record_result 'no arguments print usage' 0 'Usage: ./arcogine'
start_case help-short; run_script -h; record_result '-h prints usage' 0 'Usage: ./arcogine'
start_case help-long; run_script --help; record_result '--help prints usage' 0 'Usage: ./arcogine'
start_case unknown; run_script definitely-not-a-command; record_result 'unknown command fails with diagnostic' 1 'Unknown command: definitely-not-a-command'

invalid_cases=(
  'setup|extra'
  'test|extra'
  'check|--full extra'
  'check|--bogus'
  'snapshot|extra'
  '-h|extra'
)
for invalid_case in "${invalid_cases[@]}"; do
  IFS='|' read -r command arg_string <<< "$invalid_case"
  read -r -a args <<< "$arg_string"
  start_case "invalid-$command-${case_number}"
  if [[ -n "$arg_string" ]]; then
    run_script "$command" "${args[@]}"
  else
    run_script "$command"
  fi
  record_result "rejects invalid arguments for '$command $arg_string'" 1 'Usage:'
done

# Dispatch to the expected native tools.
start_case setup; run_script setup; record_result 'setup dispatch succeeds' 0
assert_log_contains 'setup uses the repository Gradle wrapper' "gradle cwd=$TEST_REPO/product arg=--no-daemon arg=help"

start_case test; run_script test; record_result 'test dispatch succeeds' 0
assert_log_contains 'test runs Java through the repository wrapper' "gradle cwd=$TEST_REPO/product arg=--no-daemon arg=test"

start_case check-fast; run_script check; record_result 'check dispatch succeeds' 0
assert_log_contains 'check runs the complete Java quality gate through the wrapper' "gradle cwd=$TEST_REPO/product arg=--no-daemon arg=compileJava arg=compileTestJava arg=checkstyleMain arg=checkstyleTest arg=test arg=jacocoTestReport arg=jacocoTestCoverageVerification"

start_case check-full; run_script check --full; record_result 'check --full dispatch succeeds' 0
assert_log_contains 'full check runs the Java quality gate' "arg=jacocoTestCoverageVerification"
assert_log_contains 'full check reaches the dependency audit' 'trivy cwd='
assert_log_contains 'full check reaches the secret scan' 'gitleaks cwd='

start_case snapshot; run_script snapshot; record_result 'snapshot dispatch succeeds' 0
assert_log_contains 'snapshot reaches the repository snapshot tool' "node cwd=$TEST_REPO args=|$EXPECTED_REPO_ROOT/infra/dev/repo-snapshot.mjs"

start_case fail-fast-test
export ARCOGINE_TEST_GRADLE_FAIL_ON=test
run_script test
record_nonzero 'Java test failure propagates'

if [[ "$failures" -gt 0 ]]; then
  echo "$failures assertion(s) failed across $cases checks."
  exit 1
fi
echo "All arcogine CLI contract checks passed ($cases checks)."
