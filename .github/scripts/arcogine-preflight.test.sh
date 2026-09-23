#!/usr/bin/env bash
# Table-driven test for ./arcogine's require_commands preflights. Run
# directly (bash .github/scripts/arcogine-preflight.test.sh). Exercises the
# missing-tool branches that ordinary CI (fully provisioned runners) never
# hits, so a regression in the fail-fast diagnostics added for constrained
# environments fails loudly here instead of only surfacing as a late,
# unrelated error partway through an expensive `--full` run.
#
# Deliberately hermetic: every optional tool the script preflights (trivy,
# gitleaks) is provided as a fake stub rather than relying on whatever
# happens to be installed on the machine running this test, so results
# don't depend on the host's own toolchain.
set -euo pipefail

dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(git -C "$dir" rev-parse --show-toplevel)"
script="$repo_root/arcogine"

failures=0

# Fake stub for a tool that should appear simply "present": accepts any
# arguments and exits 0.
fake_present() {
  local sandbox="$1" tool="$2"
  cat > "$sandbox/$tool" <<EOF
#!/usr/bin/env bash
echo "$tool: fake"
EOF
  chmod +x "$sandbox/$tool"
}

# Build a PATH with bash/coreutils/git (needed for the script itself to run
# at all) plus fake stubs for whichever of trivy/gitleaks are listed as
# "present" in $present_tools. Any tool not listed is absent, so the
# script's own `command -v` sees it as genuinely missing.
build_sandbox() {
  local present_tools="$1"
  local sandbox tool real
  sandbox="$(mktemp -d)"
  for tool in dirname cd pwd bash git rm mkdir cp sed grep tr head awk cat; do
    real="$(command -v "$tool" 2>/dev/null || true)"
    [ -n "$real" ] && ln -sf "$real" "$sandbox/$tool"
  done
  local t
  for t in $present_tools; do
    fake_present "$sandbox" "$t"
  done
  echo "$sandbox"
}

# check NAME ARGS PRESENT_TOOLS EXPECTED_EXIT EXPECTED_STDERR_SUBSTRING
check() {
  local name="$1" args="$2" present_tools="$3" expected_exit="$4" expected_grep="$5"
  local sandbox actual_exit stderr_out

  sandbox="$(build_sandbox "$present_tools")"
  set +e
  stderr_out="$(PATH="$sandbox" "$script" $args 2>&1 1>/dev/null)"
  actual_exit=$?
  set -e
  rm -rf "$sandbox"

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

# `./arcogine check --full` requires trivy and gitleaks (the dependency audit
# and secret scan) before doing any expensive source work.
check "check --full: missing trivy fails fast" \
  "check --full" "gitleaks" 1 "trivy"

check "check --full: missing gitleaks fails fast" \
  "check --full" "trivy" 1 "gitleaks"

# Plain `./arcogine check` (no --full) must not require any security tooling
# at all; it should get past the preflight (and fail later, on the first
# missing project tool it actually needs, not on trivy/gitleaks).
check "check (no --full): does not require trivy/gitleaks" \
  "check" "" 1 ""

if [ "$failures" -gt 0 ]; then
  echo "$failures test(s) failed"
  exit 1
fi
echo "All arcogine preflight tests passed."
