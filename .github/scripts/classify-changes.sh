#!/usr/bin/env bash
# Pure change-classification function used by the `classify` job in
# .github/workflows/ci.yml. Reads changed file paths on stdin and writes
# backend/docs_only=true|false lines to stdout.
set -euo pipefail

files="$(cat)"

# Current Gradle modules under product/, each recognized explicitly as a backend surface.
backend_modules='types|governance|storage|simulation|domains|consumer|research-experiments|architecture-conformance-test'

ci_or_shared=false
if printf '%s\n' "$files" | grep -qE '^(\.github/workflows/|arcogine$|product/build\.gradle|product/settings\.gradle|product/gradle/|product/gradle\.properties)'; then
  ci_or_shared=true
fi

docs_only=false
if [ -n "$files" ] && ! printf '%s\n' "$files" | grep -qvE '^(docs/|README\.md$|.*\.md$)'; then
  docs_only=true
fi

unknown=false
if [ -n "$files" ] && [ "$docs_only" = false ]; then
  if printf '%s\n' "$files" | grep -qvE "^(docs/|README\\.md\$|.*\\.md\$|\\.github/workflows/|arcogine\$|product/build\\.gradle|product/settings\\.gradle|product/gradle/|product/gradle\\.properties|product/($backend_modules)/)"; then
    unknown=true
  fi
fi

backend=false
if [ "$ci_or_shared" = true ] || [ "$unknown" = true ] || printf '%s\n' "$files" | grep -qE "^product/($backend_modules)/"; then
  backend=true
fi

echo "backend=$backend"
echo "docs_only=$docs_only"
