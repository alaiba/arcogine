#!/usr/bin/env bash
# Hermetic regression coverage for claude-cloud.sh. The provisioning script is
# run as a subprocess from an unrelated directory with a temporary repository,
# redirected log directory, and fake inventory commands. No package manager,
# container runtime, or host Java/Node installation is used.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(git -C "$SCRIPT_DIR" rev-parse --show-toplevel)"
SCRIPT_SOURCE="$SCRIPT_DIR/claude-cloud.sh"
PACKAGE_NODE_RANGE="$(sed -n 's/.*"node"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' "$REPO_ROOT/product/interfaces/web/package.json")"
BASH_BIN="$(command -v bash)"
TEMP_ROOT="$(mktemp -d)"
trap 'rm -rf "$TEMP_ROOT"' EXIT

failures=0
checks=0
case_number=0
CASE_ROOT=""
TEST_REPO=""
INVOKE_DIR=""
FAKE_BIN=""
CORE_BIN=""
LOG_DIR=""
COMMAND_LOG=""
LAST_STATUS=0
LAST_OUTPUT=""

make_core_path() {
  local tool real
  for tool in pwd mkdir date tee uname nproc grep cut sed free df awk; do
    real="$(type -P "$tool" || true)"
    if [[ -n "$real" ]]; then
      printf '#!/bin/bash\nexec "%s" "$@"\n' "$real" > "$CORE_BIN/$tool"
      chmod +x "$CORE_BIN/$tool"
      continue
    fi
    case "$tool" in
      nproc)
        printf '%s\n' '#!/bin/bash' 'echo 1' > "$CORE_BIN/$tool"
        ;;
      free)
        printf '%s\n' '#!/bin/bash' 'echo "              total        used        free      shared  buff/cache   available"' 'echo "Mem:           1G          0G          1G          0G          0G          1G"' > "$CORE_BIN/$tool"
        ;;
      df)
        printf '%s\n' '#!/bin/bash' 'echo "Filesystem      Size  Used Avail Use%% Mounted on"' 'echo "/dev/fake       1G    0    1G   0%% /"' > "$CORE_BIN/$tool"
        ;;
      *)
        echo "missing required test primitive: $tool" >&2
        return 1
        ;;
    esac
    chmod +x "$CORE_BIN/$tool"
  done
}

make_fake_tool() {
  local tool="$1"
  cp "$CASE_ROOT/fake-tool" "$FAKE_BIN/$tool"
}

make_fakes() {
  local tools="$1" tool
  cat > "$CASE_ROOT/fake-tool" <<'EOF'
#!/bin/bash
set -euo pipefail

tool="${0##*/}"
log="${ARCOGINE_CLOUD_COMMAND_LOG:?}"
printf '%s cwd=%s' "$tool" "$PWD" >> "$log"
for arg in "$@"; do printf ' %s' "$arg" >> "$log"; done
printf '\n' >> "$log"

if [[ "$tool" == "${ARCOGINE_TEST_FAIL_VERSION_TOOL:-}" && "${1:-}" == "--version" ]]; then
  echo "$tool: fake version probe failed" >&2
  exit 23
fi

case "$tool" in
  java)
    if [[ "${1:-}" == "-version" ]]; then
      if [[ "${ARCOGINE_TEST_JAVA_NOISE:-0}" == 1 ]]; then
        echo 'Picked up JAVA_TOOL_OPTIONS: -Dfake=true' >&2
      fi
      if [[ "${ARCOGINE_TEST_JAVA_STYLE:-modern}" == legacy ]]; then
        echo "java version \"1.${ARCOGINE_TEST_JAVA_VERSION:-8}.0_381\"" >&2
      else
        echo "openjdk version \"${ARCOGINE_TEST_JAVA_VERSION:-21.0.6}\"" >&2
      fi
    fi
    ;;
  javac)
    [[ "${1:-}" == "-version" ]] && echo "javac ${ARCOGINE_TEST_JAVA_VERSION:-21.0.6}"
    ;;
  node)
    if [[ "${1:-}" == "--version" ]]; then
      echo "v${ARCOGINE_TEST_NODE_VERSION:-22.22.2}"
    elif [[ "${1:-}" == "-p" ]]; then
      sed -n 's/.*"node"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' \
        "$PWD/product/interfaces/web/package.json"
    fi
    ;;
  *)
    echo "$tool: fake version"
    ;;
esac
EOF
  chmod +x "$CASE_ROOT/fake-tool"
  for tool in $tools; do
    make_fake_tool "$tool"
  done
}

start_case() {
  local package_range="${1:-$PACKAGE_NODE_RANGE}"
  local tools="${2:-bash git curl java javac node npm npx docker trivy gitleaks}"
  case_number=$((case_number + 1))
  CASE_ROOT="$TEMP_ROOT/case-$case_number"
  TEST_REPO="$CASE_ROOT/repository with spaces"
  INVOKE_DIR="$CASE_ROOT/invocation directory"
  FAKE_BIN="$CASE_ROOT/fake-bin"
  CORE_BIN="$CASE_ROOT/core-bin"
  LOG_DIR="$CASE_ROOT/log directory"
  COMMAND_LOG="$CASE_ROOT/commands.log"

  mkdir -p "$TEST_REPO/product/interfaces/web" "$INVOKE_DIR" "$FAKE_BIN" "$CORE_BIN" "$LOG_DIR"
  printf '%s\n' "{\"engines\":{\"node\":\"$package_range\"}}" \
    > "$TEST_REPO/product/interfaces/web/package.json"
  : > "$COMMAND_LOG"
  make_core_path
  make_fakes "$tools"

  export ARCOGINE_CLOUD_COMMAND_LOG="$COMMAND_LOG"
  unset ARCOGINE_TEST_FAIL_VERSION_TOOL ARCOGINE_TEST_JAVA_NOISE \
    ARCOGINE_TEST_JAVA_STYLE ARCOGINE_TEST_JAVA_VERSION ARCOGINE_TEST_NODE_VERSION
}

run_cloud() {
  local output_file="$CASE_ROOT/stdout.log"
  set +e
  (cd "$INVOKE_DIR" && \
    ARCOGINE_REPO_DIR="$TEST_REPO" \
    ARCOGINE_LOG_DIR="$LOG_DIR" \
    PATH="$FAKE_BIN:$CORE_BIN" \
    "$BASH_BIN" "$SCRIPT_SOURCE" > "$output_file" 2>&1)
  LAST_STATUS=$?
  LAST_OUTPUT="$(< "$output_file")"
  set -e
}

assert_result() {
  local name="$1" expected_status="$2" expected_text="${3:-}"
  checks=$((checks + 1))
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

assert_output_contains() {
  local name="$1" text="$2"
  checks=$((checks + 1))
  if grep -qF -- "$text" <<< "$LAST_OUTPUT"; then
    echo "PASS: $name"
  else
    echo "FAIL: $name (output did not contain '$text')"
    printf '  output: %s\n' "$LAST_OUTPUT"
    failures=$((failures + 1))
  fi
}

assert_output_not_contains() {
  local name="$1" text="$2"
  checks=$((checks + 1))
  if ! grep -qF -- "$text" <<< "$LAST_OUTPUT"; then
    echo "PASS: $name"
  else
    echo "FAIL: $name (output unexpectedly contained '$text')"
    printf '  output: %s\n' "$LAST_OUTPUT"
    failures=$((failures + 1))
  fi
}

assert_command_log_not_contains() {
  local name="$1" text="$2"
  checks=$((checks + 1))
  if ! grep -qF -- "$text" "$COMMAND_LOG"; then
    echo "PASS: $name"
  else
    echo "FAIL: $name (command log unexpectedly contained '$text')"
    printf '  command log:\n%s\n' "$(cat "$COMMAND_LOG")"
    failures=$((failures + 1))
  fi
}

# Repository resolution and invocation-directory diagnostics.
start_case
missing_repo="$CASE_ROOT/missing repository"
TEST_REPO="$missing_repo"
run_cloud
assert_result 'missing expected repository fails clearly' 1 "FATAL: expected Arcogine repository at '$missing_repo'"
assert_output_contains 'missing-repository output preserves the invocation diagnostic' "Invoked from:          $INVOKE_DIR"

start_case
run_cloud
assert_result 'overrideable repository path succeeds' 0 'Arcogine Claude Cloud provisioning complete.'
assert_output_contains 'provisioning changes to the overridden repository' "Changing to repository directory: $TEST_REPO"
if grep -qF -- "node cwd=$TEST_REPO" "$COMMAND_LOG"; then
  echo "PASS: repository-relative inspection occurs after changing directory"
else
  echo "FAIL: repository-relative inspection did not use the overridden repository"
  printf '  command log:\n%s\n' "$(cat "$COMMAND_LOG")"
  failures=$((failures + 1))
fi

# Java parsing, compatibility warnings, and JDK detection.
java_cases=(
  '21.0.6|modern|0|Java major version OK: 21'
  '25.0.1|modern|0|Java major version OK: 25'
  '20.0.2|modern|0|requires Java 21 or newer'
  '8|legacy|0|requires Java 21 or newer'
)
for java_case in "${java_cases[@]}"; do
  IFS='|' read -r version style expected text <<< "$java_case"
  start_case
  export ARCOGINE_TEST_JAVA_VERSION="$version" ARCOGINE_TEST_JAVA_STYLE="$style"
  run_cloud
  assert_result "Java $version ($style) keeps provisioning non-fatal" "$expected" "$text"
done

start_case
export ARCOGINE_TEST_JAVA_VERSION=21.0.6 ARCOGINE_TEST_JAVA_NOISE=1
run_cloud
assert_result 'JAVA_TOOL_OPTIONS-style noise does not break Java detection' 0 'Java major version OK: 21'

start_case '^22.22.2 || ^24.15.0 || ^26.0.0' 'bash git curl javac node npm npx docker trivy gitleaks'
run_cloud
assert_result 'missing java is a warning rather than a provisioning failure' 0 'Java is not available on PATH'

start_case '^22.22.2 || ^24.15.0 || ^26.0.0' 'bash git curl java node npm npx docker trivy gitleaks'
export ARCOGINE_TEST_JAVA_VERSION=21.0.6
run_cloud
assert_result 'missing javac warns that a JDK is required' 0 'javac is not available'

# Node support boundaries mirror the manifest's current explicit caret ranges.
node_cases=(
  '22.22.1|unsupported'
  '22.22.2|supported'
  '22.23.0|supported'
  '24.14.9|unsupported'
  '24.15.0|supported'
  '24.16.1|supported'
  '26.0.0|supported'
  '26.99.99|supported'
  '23.0.0|unsupported'
  '25.0.0|unsupported'
  '22.22|unsupported'
)
for node_case in "${node_cases[@]}"; do
  IFS='|' read -r version expectation <<< "$node_case"
  start_case
  export ARCOGINE_TEST_NODE_VERSION="$version"
  run_cloud
  if [[ "$expectation" == supported ]]; then
    assert_result "Node $version is accepted at its support boundary" 0 "Node.js version OK: $version"
  else
    assert_result "Node $version is rejected outside its support boundary" 0 "Node.js $version is outside Arcogine's supported range"
  fi
done

start_case '^22.22.2 || ^24.15.0 || ^26.0.0' 'bash git curl java javac npm npx docker trivy gitleaks'
export ARCOGINE_TEST_JAVA_VERSION=21.0.6
run_cloud
assert_result 'missing node warns without blocking provisioning' 0 'Node.js is not available on PATH'

start_case '^22.22.2 || ^24.15.0 || ^26.0.0' 'bash git curl java javac node npx docker trivy gitleaks'
export ARCOGINE_TEST_JAVA_VERSION=21.0.6 ARCOGINE_TEST_NODE_VERSION=22.22.2
run_cloud
assert_result 'supported node with missing npm warns without blocking provisioning' 0 'npm is not available on PATH'

start_case '^22.22.2 || ^24.15.0 || ^26.0.0' 'bash git curl java javac node npm npx docker trivy gitleaks'
export ARCOGINE_TEST_JAVA_VERSION=21.0.6 ARCOGINE_TEST_NODE_VERSION=22.22.2
run_cloud
assert_result 'matching Node support contract has no drift warning' 0 'Node.js version OK: 22.22.2'
assert_output_not_contains 'matching Node support contract stays aligned' 'Node support contract drift'

start_case '^22.22.2 || ^24.15.0 || ^26.0.0' 'bash git curl java javac node npm npx docker trivy gitleaks'
export ARCOGINE_TEST_JAVA_VERSION=21.0.6 ARCOGINE_TEST_NODE_VERSION=22.22.2
printf '%s\n' '{"engines":{"node":"^24.15.0"}}' > "$TEST_REPO/product/interfaces/web/package.json"
run_cloud
assert_result 'manifest support-contract drift is reported clearly' 0 'Node support contract drift'
assert_output_contains 'drift warning names the manifest range' "package.json declares '^24.15.0'"

# Optional inventory remains informational, including a failing version probe.
start_case '^22.22.2 || ^24.15.0 || ^26.0.0' 'java javac node npm npx'
export ARCOGINE_TEST_JAVA_VERSION=21.0.6 ARCOGINE_TEST_NODE_VERSION=22.22.2
run_cloud
assert_result 'missing optional tools are reported as not installed' 0 'not installed'

start_case
export ARCOGINE_TEST_JAVA_VERSION=21.0.6 ARCOGINE_TEST_NODE_VERSION=22.22.2 ARCOGINE_TEST_FAIL_VERSION_TOOL=trivy
run_cloud
assert_result 'failing optional version probe remains informational' 0 'version command failed; continuing because this inventory entry is informational'

# The provisioning invariant is behavioral: only version/manifest inspection
# commands appear in the fake command log, and the success message states that
# project dependencies were not installed.
assert_output_contains 'successful provisioning communicates no dependency installation' 'No project dependencies were installed during provisioning.'
for forbidden in 'npm ci' './gradlew' 'playwright install' 'apt ' 'apt-get '; do
  assert_command_log_not_contains "provisioning never invokes '$forbidden'" "$forbidden"
done

# Redirected logging captures the same visible diagnostics emitted by the run.
log_files=("$LOG_DIR"/*.log)
checks=$((checks + 1))
if [[ "${#log_files[@]}" -eq 1 && -f "${log_files[0]}" ]]; then
  echo "PASS: redirected log file is created"
else
  echo "FAIL: redirected log file was not created in '$LOG_DIR'"
  failures=$((failures + 1))
fi
checks=$((checks + 1))
if [[ -f "${log_files[0]}" ]] && grep -qF -- 'Arcogine Claude Cloud provisioning complete.' "${log_files[0]}"; then
  echo "PASS: visible completion diagnostic is captured in the log"
else
  echo "FAIL: visible completion diagnostic is missing from the redirected log"
  failures=$((failures + 1))
fi
checks=$((checks + 1))
if [[ -f "${log_files[0]}" ]] && grep -qF -- 'No project dependencies were installed during provisioning.' "${log_files[0]}"; then
  echo "PASS: provisioning diagnostic is captured in the log"
else
  echo "FAIL: provisioning diagnostic is missing from the redirected log"
  failures=$((failures + 1))
fi

if [[ "$failures" -gt 0 ]]; then
  echo "$failures assertion(s) failed across $checks checks."
  exit 1
fi
echo "All Claude Cloud provisioning checks passed ($checks checks)."
