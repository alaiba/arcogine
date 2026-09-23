#!/usr/bin/env bash
set -euo pipefail

script="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/discover-changed-files.sh"
classifier="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/classify-changes.sh"
tmp_dir="$(mktemp -d)"
trap 'rm -rf "$tmp_dir"' EXIT
repo="$tmp_dir/repo"
mkdir -p "$repo"
cd "$repo"

git init -q
git config user.name "Classifier test"
git config user.email "classifier-test@example.invalid"
mkdir -p docs product/domains/factory .github/workflows
printf 'base\n' > docs/guide.md
printf 'classify\n' > .github/workflows/ci.yml
printf 'source\n' > product/domains/factory/Factory.java
git add .
git commit -qm "base"
base_sha="$(git rev-parse HEAD)"

git checkout -qb candidate
printf 'candidate\n' >> docs/guide.md
git commit -qam "docs change"

# The PR comparison uses only the locally available immutable base commit and
# returns the three-dot changed path. The origin points to a local path that
# is not a repository, so an accidental fetch would fail immediately without
# making this test depend on any live network service.
git remote add origin "$tmp_dir/not-a-repository"
actual="$(GIT_TERMINAL_PROMPT=0 bash "$script" pull_request "$base_sha")"
expected="docs/guide.md"
if [[ "$actual" != "$expected" ]]; then
  echo "FAIL: local PR-base discovery"
  echo "  expected: $expected"
  echo "  actual:   $actual"
  exit 1
fi
echo "PASS: local PR-base discovery uses three-dot diff without fetching"

# An unavailable event base returns the tracked-file set and a distinct status
# so the workflow can add an Actions warning while still over-selecting tests.
set +e
actual="$(bash "$script" pull_request 0000000000000000000000000000000000000000)"
status=$?
set -e
if [[ "$status" -ne 2 ]] || [[ "$actual" != $'.github/workflows/ci.yml\ndocs/guide.md\nproduct/domains/factory/Factory.java' ]]; then
  echo "FAIL: unavailable PR-base fallback"
  echo "  expected status: 2"
  echo "  actual status:   $status"
  echo "  actual files:    $actual"
  exit 1
fi
classified="$(printf '%s\n' "$actual" | bash "$classifier" | tr '\n' ',')"
if [[ "$classified" != "backend=true,docs_only=false," ]]; then
  echo "FAIL: unavailable PR-base fallback did not select backend validation"
  echo "  actual classification: $classified"
  exit 1
fi
echo "PASS: unavailable PR-base falls back to all tracked files and selects backend"

# Push comparisons retain the existing before/after semantics. Full-sweep
# events continue to return every tracked path.
before_sha="$base_sha"
after_sha="$(git rev-parse HEAD)"
actual="$(bash "$script" push "$base_sha" "$before_sha" "$after_sha")"
if [[ "$actual" != "docs/guide.md" ]]; then
  echo "FAIL: push range discovery"
  echo "  expected: docs/guide.md"
  echo "  actual:   $actual"
  exit 1
fi
actual="$(bash "$script" schedule)"
if [[ "$actual" != $'.github/workflows/ci.yml\ndocs/guide.md\nproduct/domains/factory/Factory.java' ]]; then
  echo "FAIL: full-sweep discovery"
  echo "  actual: $actual"
  exit 1
fi
echo "PASS: push range and full-sweep discovery remain unchanged"
