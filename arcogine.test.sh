#!/usr/bin/env bash
# Table-driven test for ./arcogine's require_commands preflights. Run
# directly (bash arcogine.test.sh). Exercises the missing-tool branches that
# ordinary CI (fully provisioned runners) never hits, so a regression in the
# fail-fast diagnostics added for constrained environments fails loudly here
# instead of only surfacing as a late, unrelated error partway through an
# expensive `--full` run.
set -euo pipefail

dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
script="$dir/arcogine"

failures=0

# Build a minimal PATH containing only the given real tools (as symlinks),
# so the script's own `command -v` checks see exactly the listed tools as
# present and everything else as missing.
sandbox_path() {
  local sandbox
  sandbox="$(mktemp -d)"
  local tool real
  for tool in "$@"; do
    real="$(command -v "$tool" 2>/dev/null || true)"
    [ -n "$real" ] && ln -s "$real" "$sandbox/$tool"
  done
  echo "$sandbox"
}

# check NAME ARGS STDIN_PATH_TOOLS EXPECTED_EXIT EXPECTED_STDERR_SUBSTRING
check() {
  local name="$1" args="$2" tools="$3" expected_exit="$4" expected_grep="$5"
  local sandbox path_dir actual_exit stderr_out

  path_dir="$(sandbox_path $tools)"
  set +e
  stderr_out="$(PATH="$path_dir" "$script" $args 2>&1 1>/dev/null)"
  actual_exit=$?
  set -e
  rm -rf "$path_dir"

  if [ "$actual_exit" -ne "$expected_exit" ]; then
    echo "FAIL: $name (exit $actual_exit, expected $expected_exit)"
    echo "  stderr: $stderr_out"
    failures=$((failures + 1))
    return
  fi
  if [ -n "$expected_grep" ] && ! printf '%s' "$stderr_out" | grep -qF "$expected_grep"; then
    echo "FAIL: $name (stderr did not mention '$expected_grep')"
    echo "  stderr: $stderr_out"
    failures=$((failures + 1))
    return
  fi
  echo "PASS: $name"
}

# `./arcogine image` requires only docker.
check "image: missing docker fails fast" \
  "image" "bash coreutils git" 1 "docker"

# `./arcogine check --full` requires docker, trivy, gitleaks, and curl (the
# HTTP probe mechanism its own Docker smoke test uses) before doing any
# expensive source/E2E/build/image work.
check "check --full: missing docker fails fast" \
  "check --full" "bash coreutils git trivy gitleaks curl" 1 "docker"

check "check --full: missing trivy fails fast" \
  "check --full" "bash coreutils git docker gitleaks curl" 1 "trivy"

check "check --full: missing gitleaks fails fast" \
  "check --full" "bash coreutils git docker trivy curl" 1 "gitleaks"

check "check --full: missing curl fails fast" \
  "check --full" "bash coreutils git docker trivy gitleaks" 1 "curl"

# Plain `./arcogine check` (no --full) must not require any container/security
# tooling at all; it should get past the preflight (and fail later, on the
# first missing project tool it actually needs, not on docker/trivy/etc).
check "check (no --full): does not require docker/trivy/gitleaks/curl" \
  "check" "bash coreutils git" 1 ""

if [ "$failures" -gt 0 ]; then
  echo "$failures test(s) failed"
  exit 1
fi
echo "All arcogine preflight tests passed."
