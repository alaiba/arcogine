#!/usr/bin/env bash
# Table-driven test for classify-changes.sh. Run directly (bash
# .github/scripts/classify-changes.test.sh) or as a CI step in the
# `classify` job — either way a regression in the classifier regex fails
# loudly here instead of silently letting a real change skip its checks.
#
# The classify job is also the always-running dependency of the repository's
# single required `gate` status, so repository-wide documentation-link, delivery-label, and ADR
# history checks are invoked here as part of the same fail-closed path rather
# than through separate, non-required workflows.
set -euo pipefail

dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
script="$dir/classify-changes.sh"
repo="$(cd "$dir/../.." && pwd)"

failures=0

check() {
  local name="$1" files="$2" expected="$3" actual
  actual="$(printf '%s' "$files" | "$script" | tr '\n' ',')"
  if [ "$actual" = "$expected" ]; then
    echo "PASS: $name"
  else
    echo "FAIL: $name"
    echo "  input:    $(printf '%s' "$files" | tr '\n' ' ')"
    echo "  expected: $expected"
    echo "  actual:   $actual"
    failures=$((failures + 1))
  fi
}

check "empty diff" \
  "" \
  "backend=false,frontend=false,docker=false,docs_only=false,"

check "docs-only" \
  "docs/foo.md
README.md" \
  "backend=false,frontend=false,docker=false,docs_only=true,"

check "backend-only" \
  "product/domains/factory/src/main/java/com/arcogine/factory/Foo.java" \
  "backend=true,frontend=false,docker=false,docs_only=false,"

check "frontend-only" \
  "product/interfaces/web/src/App.tsx" \
  "backend=false,frontend=true,docker=false,docs_only=false,"

check "docker-only environment template" \
  "infra/docker/.env.example" \
  "backend=false,frontend=false,docker=true,docs_only=false,"

check "docs mixed with backend is not docs-only" \
  "docs/foo.md
product/domains/factory/src/main/java/com/arcogine/factory/Foo.java" \
  "backend=true,frontend=false,docker=false,docs_only=false,"

check "CI workflow change forces every subsystem" \
  ".github/workflows/ci.yml" \
  "backend=true,frontend=true,docker=true,docs_only=false,"

check "infra/docker change forces every subsystem (shared packaging)" \
  "infra/docker/api.Dockerfile" \
  "backend=true,frontend=true,docker=true,docs_only=false,"

check "shared frontend manifest forces every subsystem" \
  "product/interfaces/web/package-lock.json" \
  "backend=true,frontend=true,docker=true,docs_only=false,"

check "unrecognized non-doc path (product/gradlew) fails safe to every subsystem" \
  "product/gradlew" \
  "backend=true,frontend=true,docker=true,docs_only=false,"

check "unrecognized non-doc path (.trivyignore) fails safe to every subsystem" \
  ".trivyignore" \
  "backend=true,frontend=true,docker=true,docs_only=false,"

if [ "$failures" -gt 0 ]; then
  echo "$failures classification test(s) failed."
  exit 1
fi

echo "All classification tests passed."

python3 "$dir/check-markdown-links.test.py"
python3 "$dir/check-markdown-links.py" "$repo"
python3 "$dir/check-delivery-labels.test.py"
python3 "$dir/check-delivery-labels.py"
python3 "$dir/check-adr-immutability.test.py"
python3 "$dir/check-adr-rename.test.py"
python3 "$dir/check-adr-immutability.py" --ci
