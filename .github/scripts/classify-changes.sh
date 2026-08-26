#!/usr/bin/env bash
# Pure change-classification function used by the `classify` job in
# .github/workflows/ci.yml. Reads changed file paths on stdin (one per line,
# empty input allowed) and writes backend/frontend/docker/docs_only=true|false
# lines to stdout. Kept separate from the workflow so it has executable test
# coverage (classify-changes.test.sh) independent of live GitHub Actions
# context (git fetch, PR base ref, etc.).
set -euo pipefail

files="$(cat)"

# CI/tooling changes or shared manifests conservatively trigger every subsystem.
ci_or_shared=false
if printf '%s\n' "$files" | grep -qE '^(\.github/workflows/|arcogine$|product/build\.gradle|product/settings\.gradle|product/gradle/|product/gradle\.properties|product/interfaces/web/package(-lock)?\.json|product/interfaces/web/tsconfig|infra/docker/)'; then
  ci_or_shared=true
fi

# docs_only: every changed file is documentation, nothing else.
docs_only=false
if [ -n "$files" ] && ! printf '%s\n' "$files" | grep -qvE '^(docs/|README\.md$|.*\.md$)'; then
  docs_only=true
fi

# Any changed file that isn't documentation and doesn't match a known
# subsystem path is unrecognized. Fail safe: an unrecognized non-doc path
# (e.g. product/gradlew, .trivyignore, a new top-level dir) forces every
# subsystem on rather than silently skipping everything.
unknown=false
if [ -n "$files" ] && [ "$docs_only" = false ]; then
  if printf '%s\n' "$files" | grep -qvE '^(docs/|README\.md$|.*\.md$|\.github/workflows/|arcogine$|product/build\.gradle|product/settings\.gradle|product/gradle/|product/gradle\.properties|product/interfaces/web/package(-lock)?\.json|product/interfaces/web/tsconfig|infra/docker/|\.env\.example$|product/(types|simulation|domains|agents|interfaces/api|interfaces/cli)/|product/interfaces/web/)'; then
    unknown=true
  fi
fi

backend=false
if [ "$ci_or_shared" = true ] || [ "$unknown" = true ] || printf '%s\n' "$files" | grep -qE '^product/(types|simulation|domains|agents|interfaces/api|interfaces/cli)/'; then
  backend=true
fi

frontend=false
if [ "$ci_or_shared" = true ] || [ "$unknown" = true ] || printf '%s\n' "$files" | grep -qE '^product/interfaces/web/'; then
  frontend=true
fi

docker=false
if [ "$ci_or_shared" = true ] || [ "$unknown" = true ] || printf '%s\n' "$files" | grep -qE '^infra/docker/|^\.env\.example$'; then
  docker=true
fi

echo "backend=$backend"
echo "frontend=$frontend"
echo "docker=$docker"
echo "docs_only=$docs_only"
