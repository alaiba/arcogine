#!/usr/bin/env bash
# Table-driven test for ./arcogine's require_commands preflights. Run
# directly (bash .github/scripts/arcogine-preflight.test.sh). Exercises the
# missing-tool branches that ordinary CI (fully provisioned runners) never
# hits, so a regression in the fail-fast diagnostics added for constrained
# environments fails loudly here instead of only surfacing as a late,
# unrelated error partway through an expensive `--full` run.
#
# Deliberately hermetic: every optional tool the script preflights (docker,
# trivy, gitleaks, curl) is provided as a fake stub rather than relying on
# whatever happens to be installed on the machine running this test, so
# results don't depend on the host's own toolchain.
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

# Fake `docker` stub: present, but `docker compose version` fails unless
# compose_ok=1, simulating a host with the Docker CLI but no Compose plugin.
fake_docker() {
  local sandbox="$1" compose_ok="$2"
  cat > "$sandbox/docker" <<EOF
#!/usr/bin/env bash
if [ "\$1" = "compose" ] && [ "\$2" = "version" ]; then
  if [ "$compose_ok" = "1" ]; then
    echo "Docker Compose version v0.0.0-fake"
    exit 0
  fi
  echo "docker: 'compose' is not a docker command." >&2
  exit 1
fi
echo "Docker version 0.0.0-fake, build fake"
EOF
  chmod +x "$sandbox/docker"
}

# Build a PATH with bash/coreutils/git (needed for the script itself to run
# at all) plus fake stubs for whichever of docker/trivy/gitleaks/curl are
# listed as "present" in $present_tools, and a docker stub honoring
# $compose_ok. Any tool not listed is absent, so the script's own
# `command -v` sees it as genuinely missing.
build_sandbox() {
  local present_tools="$1" compose_ok="${2:-1}"
  local sandbox tool real
  sandbox="$(mktemp -d)"
  for tool in dirname cd pwd bash git rm mkdir cp sed grep tr head awk cat; do
    real="$(command -v "$tool" 2>/dev/null || true)"
    [ -n "$real" ] && ln -sf "$real" "$sandbox/$tool"
  done
  local t
  for t in $present_tools; do
    if [ "$t" = "docker" ]; then
      fake_docker "$sandbox" "$compose_ok"
    else
      fake_present "$sandbox" "$t"
    fi
  done
  echo "$sandbox"
}

# check NAME ARGS PRESENT_TOOLS COMPOSE_OK EXPECTED_EXIT EXPECTED_STDERR_SUBSTRING
check() {
  local name="$1" args="$2" present_tools="$3" compose_ok="$4" expected_exit="$5" expected_grep="$6"
  local sandbox actual_exit stderr_out

  sandbox="$(build_sandbox "$present_tools" "$compose_ok")"
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

all_tools="docker trivy gitleaks curl"

# `./arcogine image` requires only docker.
check "image: missing docker fails fast" \
  "image" "" 1 1 "docker"

# `./arcogine check --full` requires docker, trivy, gitleaks, and curl (the
# HTTP probe mechanism its own Docker smoke test uses) before doing any
# expensive source/E2E/build/image work.
check "check --full: missing docker fails fast" \
  "check --full" "trivy gitleaks curl" 1 1 "docker"

check "check --full: missing trivy fails fast" \
  "check --full" "docker gitleaks curl" 1 1 "trivy"

check "check --full: missing gitleaks fails fast" \
  "check --full" "docker trivy curl" 1 1 "gitleaks"

check "check --full: missing curl fails fast" \
  "check --full" "docker trivy gitleaks" 1 1 "curl"

# Plain `./arcogine check` (no --full) must not require any container/security
# tooling at all; it should get past the preflight (and fail later, on the
# first missing project tool it actually needs, not on docker/trivy/etc).
check "check (no --full): does not require docker/trivy/gitleaks/curl" \
  "check" "" 1 1 ""

# Docker present but Compose unavailable: exercised separately by
# `cmd_check --full`, `cmd_up`, and `cmd_down`, each with its own diagnostic.
check "check --full: docker without compose fails fast" \
  "check --full" "$all_tools" 0 1 "Docker Compose"

check "up: docker without compose fails fast" \
  "up" "docker" 0 1 "Docker Compose"

check "down: docker without compose fails fast" \
  "down" "docker" 0 1 "Docker Compose"

if [ "$failures" -gt 0 ]; then
  echo "$failures test(s) failed"
  exit 1
fi
echo "All arcogine preflight tests passed."
