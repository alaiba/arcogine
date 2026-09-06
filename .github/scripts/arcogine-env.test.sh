#!/usr/bin/env bash
# Regression coverage for ./arcogine's ensure_env behavior: the root .env
# runtime override file must be seeded from the relocated
# infra/docker/.env.example template, created only when missing, and never
# clobbered when the developer already has one.
#
# Deliberately hermetic: each check runs in its own throwaway git worktree
# copy of just the files ensure_env's callers need (the arcogine script and
# infra/docker/.env.example), with a fake `docker` stub on PATH so no real
# Docker Compose is invoked and the developer's own root .env is never
# touched.
set -euo pipefail

dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(git -C "$dir" rev-parse --show-toplevel)"
script_src="$repo_root/arcogine"
template_src="$repo_root/infra/docker/.env.example"

failures=0

# Fake `docker` stub: reports Compose present and no-ops every subcommand,
# so `./arcogine down` reaches and exercises ensure_env without touching a
# real container runtime.
fake_docker() {
  local sandbox="$1"
  cat > "$sandbox/docker" <<'EOF'
#!/usr/bin/env bash
if [ "$1" = "compose" ] && [ "$2" = "version" ]; then
  echo "Docker Compose version v0.0.0-fake"
  exit 0
fi
echo "docker: fake ($*)"
exit 0
EOF
  chmod +x "$sandbox/docker"
}

# Build a standalone repo-shaped workspace containing only what ensure_env's
# callers need: the arcogine script at its root (so its own
# `git rev-parse --show-toplevel` resolves to this workspace, not the real
# repo) and infra/docker/.env.example at the relocated path. A workspace
# without the template is used to prove ensure_env does not fall back to
# any stale path.
build_workspace() {
  local include_template="$1"
  local workspace
  workspace="$(mktemp -d)"
  git init -q "$workspace"
  cp "$script_src" "$workspace/arcogine"
  chmod +x "$workspace/arcogine"
  mkdir -p "$workspace/infra/docker"
  if [ "$include_template" = "1" ]; then
    cp "$template_src" "$workspace/infra/docker/.env.example"
  fi
  echo "$workspace"
}

build_path_sandbox() {
  local sandbox tool real
  sandbox="$(mktemp -d)"
  for tool in dirname cd pwd bash git rm mkdir cp sed grep tr head awk cat diff mktemp chmod; do
    real="$(command -v "$tool" 2>/dev/null || true)"
    [ -n "$real" ] && ln -sf "$real" "$sandbox/$tool"
  done
  fake_docker "$sandbox"
  echo "$sandbox"
}

run_ensure_env() {
  local workspace="$1" path_sandbox="$2"
  (cd "$workspace" && PATH="$path_sandbox" ./arcogine down >/tmp/arcogine-env-test.out 2>&1)
}

fail() {
  echo "FAIL: $1"
  shift
  [ $# -gt 0 ] && printf '  %s\n' "$@"
  failures=$((failures + 1))
}

# --- Case 1: missing root .env is created from the relocated template ---
workspace="$(build_workspace 1)"
sandbox="$(build_path_sandbox)"
if run_ensure_env "$workspace" "$sandbox"; then
  if [ -f "$workspace/.env" ] && diff -q "$workspace/.env" "$template_src" >/dev/null 2>&1; then
    echo "PASS: missing root .env is created and matches infra/docker/.env.example"
  else
    fail "missing root .env is created and matches infra/docker/.env.example" \
      "created: $( [ -f "$workspace/.env" ] && echo yes || echo no )"
  fi
else
  fail "missing root .env is created and matches infra/docker/.env.example" \
    "./arcogine down failed: $(cat /tmp/arcogine-env-test.out)"
fi
rm -rf "$workspace" "$sandbox"

# --- Case 2: existing root .env is never overwritten ---
workspace="$(build_workspace 1)"
sandbox="$(build_path_sandbox)"
sentinel="CUSTOM_SENTINEL_VALUE=do-not-touch-me"
printf '%s\n' "$sentinel" > "$workspace/.env"
before_sum="$(cksum < "$workspace/.env")"
if run_ensure_env "$workspace" "$sandbox"; then
  after_sum="$(cksum < "$workspace/.env")"
  if [ "$before_sum" = "$after_sum" ] && grep -qF "$sentinel" "$workspace/.env"; then
    echo "PASS: existing root .env is never overwritten"
  else
    fail "existing root .env is never overwritten" \
      "before: $before_sum" "after: $after_sum" "contents: $(cat "$workspace/.env")"
  fi
else
  fail "existing root .env is never overwritten" \
    "./arcogine down failed: $(cat /tmp/arcogine-env-test.out)"
fi
rm -rf "$workspace" "$sandbox"

# --- Case 3: the relocated template is the source of truth, not a stale path ---
# No infra/docker/.env.example in this workspace, plus a decoy at the old
# root .env.example location with different content. If ensure_env ever
# regressed to reading a stale root path, it would silently succeed and seed
# .env from the decoy; instead it must fail because its real, relocated
# source is missing.
workspace="$(build_workspace 0)"
printf 'DECOY=should-never-be-used\n' > "$workspace/.env.example"
sandbox="$(build_path_sandbox)"
if run_ensure_env "$workspace" "$sandbox"; then
  fail "relocated template is the source of truth (no stale-path fallback)" \
    "./arcogine down unexpectedly succeeded without infra/docker/.env.example" \
    "resulting .env: $( [ -f "$workspace/.env" ] && cat "$workspace/.env" || echo '(none)' )"
else
  if [ -f "$workspace/.env" ] && grep -qF "DECOY" "$workspace/.env"; then
    fail "relocated template is the source of truth (no stale-path fallback)" \
      "root .env was seeded from the decoy root .env.example"
  else
    echo "PASS: relocated template is the source of truth (no stale-path fallback)"
  fi
fi
rm -rf "$workspace" "$sandbox"

rm -f /tmp/arcogine-env-test.out

if [ "$failures" -gt 0 ]; then
  echo "$failures test(s) failed"
  exit 1
fi
echo "All arcogine env tests passed."
