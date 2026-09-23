#!/usr/bin/env bash
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
    echo "  expected: $expected"
    echo "  actual:   $actual"
    failures=$((failures + 1))
  fi
}

check "empty diff" "" "backend=false,docs_only=false,"
check "docs-only" "docs/foo.md
README.md" "backend=false,docs_only=true,"
check "backend-only" "product/domains/factory/src/main/java/com/arcogine/factory/Foo.java" "backend=true,docs_only=false,"
check "governance backend" "product/governance/src/main/java/com/arcogine/governance/Foo.java" "backend=true,docs_only=false,"
check "docs mixed with backend" "docs/foo.md
product/domains/factory/src/main/java/com/arcogine/factory/Foo.java" "backend=true,docs_only=false,"
check "CI workflow change forces executable surfaces" ".github/workflows/ci.yml" "backend=true,docs_only=false,"
check "unknown non-doc path fails safe" "product/gradlew" "backend=true,docs_only=false,"
check "unknown root file fails safe" ".trivyignore" "backend=true,docs_only=false,"
check "retired module path fails safe" "product/agents/src/main/java/com/arcogine/agents/Legacy.java" "backend=true,docs_only=false,"

if [ "$failures" -gt 0 ]; then
  echo "$failures classification test(s) failed."
  exit 1
fi

echo "All classification tests passed."

python3 "$dir/check-markdown-links.test.py"
python3 "$dir/check-markdown-links.py" "$repo"
python3 "$dir/check-delivery-labels.test.py"
python3 "$dir/check-delivery-labels.py"

echo "Validating GitHub attribution hygiene helper..."
node --test "$repo/infra/dev/github-attribution-hygiene.test.mjs"

echo "Validating Git identity setup helper..."
bash "$repo/infra/dev/git-identity.test.sh"
