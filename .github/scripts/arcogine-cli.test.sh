#!/usr/bin/env bash
# Hermetic black-box regression coverage for the public ./arcogine interface.
# The real wrapper is copied into a temporary git repository and every native
# tool it dispatches to is replaced with a small logging stub. This protects
# orchestration behavior without running Gradle, npm, Docker, or the product.
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

for arg in "$@"; do
  if [[ "$arg" == ':cli:stageDist' && "${ARCOGINE_TEST_OMIT_API:-0}" != 1 ]]; then
    mkdir -p "$PWD/../dist/api"
    : > "$PWD/../dist/api/arcogine.jar"
  fi
done
EOF

  cat > "$fake_bin/npm" <<'EOF'
#!/bin/bash
set -euo pipefail
log="${ARCOGINE_TEST_LOG:?}"
printf 'npm cwd=%s args=' "$PWD" >> "$log"
for arg in "$@"; do printf '|%s' "$arg" >> "$log"; done
printf '\n' >> "$log"

if [[ "${ARCOGINE_TEST_NPM_FAIL_ON:-}" == "$*" ]]; then
  exit 19
fi

if [[ "$*" == 'run build' && "${ARCOGINE_TEST_OMIT_WEB:-0}" != 1 ]]; then
  mkdir -p "${ARCOGINE_DIST_WEB:?}"
  : > "${ARCOGINE_DIST_WEB}/index.html"
fi
EOF

  cat > "$fake_bin/npx" <<'EOF'
#!/bin/bash
set -euo pipefail
log="${ARCOGINE_TEST_LOG:?}"
printf 'npx cwd=%s args=' "$PWD" >> "$log"
for arg in "$@"; do printf '|%s' "$arg" >> "$log"; done
printf '\n' >> "$log"
EOF

  cat > "$fake_bin/node" <<'EOF'
#!/bin/bash
set -euo pipefail
log="${ARCOGINE_TEST_LOG:?}"
printf 'node cwd=%s args=' "$PWD" >> "$log"
for arg in "$@"; do printf '|%s' "$arg" >> "$log"; done
printf '\n' >> "$log"
if [[ "${1:-}" == '-p' ]]; then
  printf '%s\n' "${ARCOGINE_TEST_PACKAGE_RANGE:-^22.22.2 || ^24.15.0 || ^26.0.0}"
fi
EOF

  cat > "$fake_bin/docker" <<'EOF'
#!/bin/bash
set -euo pipefail
log="${ARCOGINE_TEST_LOG:?}"
printf 'docker cwd=%s args=' "$PWD" >> "$log"
for arg in "$@"; do printf '|%s' "$arg" >> "$log"; done
printf '\n' >> "$log"

if [[ "${1:-}" == compose && "${2:-}" == version ]]; then
  [[ "${ARCOGINE_TEST_COMPOSE_OK:-1}" == 1 ]]
  exit
fi
last_arg="${!#}"
if [[ "$last_arg" == 3000 ]]; then
  printf '0.0.0.0:3000\n'
elif [[ "$last_arg" == 5173 ]]; then
  printf '0.0.0.0:5173\n'
fi
EOF

  chmod +x "$fake_bin"/*
}

start_case() {
  case_number=$((case_number + 1))
  CASE_ROOT="$TEMP_ROOT/case-$case_number"
  TEST_REPO="$CASE_ROOT/repo with spaces"
  FAKE_BIN="$CASE_ROOT/fake-bin"
  TEST_LOG="$CASE_ROOT/invocations.log"

  mkdir -p "$TEST_REPO/product/interfaces/web" "$TEST_REPO/infra/docker" \
    "$TEST_REPO/docs/examples" "$FAKE_BIN"
  git init -q "$TEST_REPO"
  EXPECTED_REPO_ROOT="$(git -C "$TEST_REPO" rev-parse --show-toplevel)"
  cp "$SCRIPT_SOURCE" "$TEST_REPO/arcogine"
  chmod +x "$TEST_REPO/arcogine"
  cp "$REPO_ROOT/infra/docker/.env.example" "$TEST_REPO/infra/docker/.env.example"
  printf '%s\n' '{"engines":{"node":"^22.22.2 || ^24.15.0 || ^26.0.0"}}' \
    > "$TEST_REPO/product/interfaces/web/package.json"
  : > "$TEST_LOG"
  make_fakes "$FAKE_BIN"
  cp "$FAKE_BIN/gradlew" "$TEST_REPO/product/gradlew"
  chmod +x "$TEST_REPO/product/gradlew"

  unset ARCOGINE_TEST_GRADLE_FAIL_ON ARCOGINE_TEST_NPM_FAIL_ON \
    ARCOGINE_TEST_OMIT_API ARCOGINE_TEST_OMIT_WEB
  export ARCOGINE_TEST_LOG="$TEST_LOG"
  export ARCOGINE_TEST_PACKAGE_RANGE='^22.22.2 || ^24.15.0 || ^26.0.0'
  export ARCOGINE_TEST_COMPOSE_OK=1
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

assert_output_contains() {
  local name="$1" text="$2"
  cases=$((cases + 1))
  if grep -qF -- "$text" <<< "$LAST_OUTPUT"; then
    echo "PASS: $name"
  else
    echo "FAIL: $name (output did not contain '$text')"
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

assert_log_not_contains() {
  local name="$1" text="$2"
  cases=$((cases + 1))
  if ! grep -qF -- "$text" "$TEST_LOG"; then
    echo "PASS: $name"
  else
    echo "FAIL: $name (invocation log unexpectedly contained '$text')"
    printf '  log:\n%s\n' "$(cat "$TEST_LOG")"
    failures=$((failures + 1))
  fi
}

assert_file() {
  local name="$1" path="$2"
  cases=$((cases + 1))
  if [[ -f "$path" ]]; then
    echo "PASS: $name"
  else
    echo "FAIL: $name (missing '$path')"
    failures=$((failures + 1))
  fi
}

assert_absent() {
  local name="$1" path="$2"
  cases=$((cases + 1))
  if [[ ! -e "$path" ]]; then
    echo "PASS: $name"
  else
    echo "FAIL: $name (unexpected '$path')"
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
  'build|extra'
  'snapshot|extra'
  'image|extra'
  'up|--no-rebuild extra'
  'down|extra'
  'run|'
  'run|api extra'
  'run|scenario'
  'run|scenario path extra'
  'run|unknown'
  'clean|extra'
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
assert_log_contains 'setup installs frontend dependencies through npm from web directory' "npm cwd=$TEST_REPO/product/interfaces/web args=|ci"
assert_log_contains 'setup reaches the explicit Playwright install command' 'npx cwd='

start_case test; run_script test; record_result 'test dispatch succeeds' 0
assert_log_contains 'test runs Java through the repository wrapper' "gradle cwd=$TEST_REPO/product arg=--no-daemon arg=test"
assert_log_contains 'test runs frontend tests from the web directory' "npm cwd=$TEST_REPO/product/interfaces/web args=|test"

start_case snapshot; run_script snapshot; record_result 'snapshot dispatch succeeds' 0
assert_log_contains 'snapshot reaches the repository snapshot tool' "node cwd=$TEST_REPO args=|$EXPECTED_REPO_ROOT/infra/dev/repo-snapshot.mjs"

start_case run-api; run_script run api; record_result 'run api dispatch succeeds' 0
assert_log_contains 'run api uses the backend development command' "gradle cwd=$TEST_REPO/product arg=--no-daemon arg=:cli:bootRun arg=--args=serve"

start_case run-web; run_script run web; record_result 'run web dispatch succeeds' 0
assert_log_contains 'run web uses the frontend development server from web directory' "npm cwd=$TEST_REPO/product/interfaces/web args=|run|dev"

start_case run-ui; run_script run ui; record_result 'run ui compatibility alias dispatches successfully' 0
assert_log_contains 'run ui is equivalent to run web' "npm cwd=$TEST_REPO/product/interfaces/web args=|run|dev"

# Scenario paths are normalized by the wrapper and passed as one native CLI argument.
start_case scenario-relative
relative_scenario='docs/examples/example.toml'
run_script run scenario "$relative_scenario"
record_result 'relative scenario path dispatch succeeds' 0
assert_log_contains 'relative scenario path is rooted at the repository' \
  "arg=--args=run $EXPECTED_REPO_ROOT/$relative_scenario"

start_case scenario-absolute
absolute_scenario="$CASE_ROOT/absolute scenario.toml"
: > "$absolute_scenario"
run_script run scenario "$absolute_scenario"
record_result 'absolute scenario path dispatch succeeds' 0
assert_log_contains 'absolute scenario path is passed unchanged' \
  "arg=--args=run $absolute_scenario"

# Build invariants: dist is refreshed, both native builds are required, and both
# canonical artifacts are required before success.
start_case build-success
mkdir -p "$TEST_REPO/dist"
printf 'stale\n' > "$TEST_REPO/dist/stale.txt"
run_script build
record_result 'build succeeds when both canonical artifacts are produced' 0
assert_absent 'build removes stale dist output' "$TEST_REPO/dist/stale.txt"
assert_file 'build produces the canonical API artifact' "$TEST_REPO/dist/api/arcogine.jar"
assert_file 'build produces the canonical web artifact' "$TEST_REPO/dist/web/index.html"
assert_log_contains 'build dispatches the staged Java artifact command' "gradle cwd=$TEST_REPO/product arg=--no-daemon arg=:cli:stageDist"
assert_log_contains 'build dispatches the frontend production build' "npm cwd=$TEST_REPO/product/interfaces/web args=|run|build"

start_case build-missing-api
export ARCOGINE_TEST_OMIT_API=1
run_script build
record_result 'build fails when the API artifact is absent' 1 'build did not produce'

start_case build-missing-web
export ARCOGINE_TEST_OMIT_WEB=1
run_script build
record_result 'build fails when the web artifact is absent' 1 'build did not produce'

# Image preconditions are checked before either image build is dispatched.
start_case image-missing-api
mkdir -p "$TEST_REPO/dist/web"
: > "$TEST_REPO/dist/web/index.html"
run_script image
record_result 'image fails when the API artifact is absent' 1 'arcogine.jar not found'
assert_log_not_contains 'missing API precondition prevents Docker image builds' 'args=|build'

start_case image-missing-web
mkdir -p "$TEST_REPO/dist/api"
: > "$TEST_REPO/dist/api/arcogine.jar"
run_script image
record_result 'image fails when the web artifact is absent' 1 'index.html not found'
assert_log_not_contains 'missing web precondition prevents Docker image builds' 'args=|build'

start_case image-success
mkdir -p "$TEST_REPO/dist/api" "$TEST_REPO/dist/web"
: > "$TEST_REPO/dist/api/arcogine.jar"
: > "$TEST_REPO/dist/web/index.html"
run_script image
record_result 'image dispatch succeeds with canonical artifacts' 0
assert_log_contains 'image builds the API runtime image from dist/api' "args=|build|-f|$EXPECTED_REPO_ROOT/infra/docker/api.Dockerfile|-t|arcogine-api:ci|$EXPECTED_REPO_ROOT/dist/api"
assert_log_contains 'image builds the web runtime image from dist/web' "args=|build|-f|$EXPECTED_REPO_ROOT/infra/docker/web.Dockerfile|-t|arcogine-ui:ci|$EXPECTED_REPO_ROOT/dist/web"

# Up either performs the complete prerequisite path or explicitly skips it.
start_case up-rebuild
run_script up
record_result 'up performs rebuild and startup' 0
assert_log_contains 'ordinary up performs the build stage' "arg=:cli:stageDist"
assert_log_contains 'ordinary up performs Docker image builds' 'args=|build|-f'
assert_log_contains 'ordinary up starts Compose after prerequisites' 'args=|compose|--project-directory'
assert_log_contains 'ordinary up starts the stack' '|up|-d|--wait|--wait-timeout|120'

start_case up-no-rebuild
run_script up --no-rebuild
record_result 'up --no-rebuild starts without rebuilding' 0
assert_log_not_contains 'no-rebuild skips the build stage' 'arg=:cli:stageDist'
assert_log_not_contains 'no-rebuild skips Docker image builds' 'args=|build|-f'
assert_log_contains 'no-rebuild still starts Compose' 'args=|compose|--project-directory'

start_case fail-fast-test
export ARCOGINE_TEST_GRADLE_FAIL_ON=test
run_script test
record_nonzero 'Java test failure propagates'
assert_log_not_contains 'Java test failure prevents frontend tests' 'npm cwd='

start_case fail-fast-up-build
export ARCOGINE_TEST_GRADLE_FAIL_ON=:cli:stageDist
run_script up
record_nonzero 'build failure prevents later up stages'
assert_log_not_contains 'failed build prevents image packaging' 'args=|build|-f'
assert_log_not_contains 'failed build prevents Compose startup' 'args=|up|-d'

# clean owns only the canonical generated distribution directory and accepts no arguments.
start_case clean
mkdir -p "$TEST_REPO/dist"
: > "$TEST_REPO/dist/generated.txt"
run_script clean
record_result 'clean succeeds and removes dist' 0
assert_absent 'clean removes only canonical dist output' "$TEST_REPO/dist"
run_script clean
record_result 'clean succeeds when dist is already absent' 0

if [[ "$failures" -gt 0 ]]; then
  echo "$failures assertion(s) failed across $cases checks."
  exit 1
fi
echo "All arcogine CLI contract checks passed ($cases checks)."
