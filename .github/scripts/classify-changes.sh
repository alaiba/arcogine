#!/usr/bin/env bash
# Pure change-classification function used by the `classify` job in
# .github/workflows/ci.yml. Reads changed file paths on stdin and writes
# backend/docker/docs_only=true|false lines to stdout.
set -euo pipefail

files="$(cat)"

ci_or_shared=false
if printf '%s\n' "$files" | grep -qE '^(\.github/workflows/|arcogine$|product/build\.gradle|product/settings\.gradle|product/gradle/|product/gradle\.properties)'; then
  ci_or_shared=true
elif printf '%s\n' "$files" | grep -E '^infra/docker/' | grep -qvE '^infra/docker/\.env\.example$'; then
  ci_or_shared=true
fi

docs_only=false
if [ -n "$files" ] && ! printf '%s\n' "$files" | grep -qvE '^(docs/|README\.md$|.*\.md$)'; then
  docs_only=true
fi

unknown=false
if [ -n "$files" ] && [ "$docs_only" = false ]; then
  if printf '%s\n' "$files" | grep -qvE '^(docs/|README\.md$|.*\.md$|\.github/workflows/|arcogine$|product/build\.gradle|product/settings\.gradle|product/gradle/|product/gradle\.properties|infra/docker/|product/(types|governance|simulation|domains|agents|consumer|interfaces/api|interfaces/cli)/)'; then
    unknown=true
  fi
fi

backend=false
if [ "$ci_or_shared" = true ] || [ "$unknown" = true ] || printf '%s\n' "$files" | grep -qE '^product/(types|governance|simulation|domains|agents|consumer|interfaces/api|interfaces/cli)/'; then
  backend=true
fi

docker=false
if [ "$ci_or_shared" = true ] || [ "$unknown" = true ] || printf '%s\n' "$files" | grep -qE '^infra/docker/'; then
  docker=true
fi

echo "backend=$backend"
echo "docker=$docker"
echo "docs_only=$docs_only"
