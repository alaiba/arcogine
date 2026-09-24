#!/usr/bin/env bash
set -euo pipefail

repo_root="$(git rev-parse --show-toplevel)"
if [[ ! -x "$repo_root/.githooks/pre-commit" ]]; then
  echo 'ERROR: .githooks/pre-commit is missing or not executable.' >&2
  exit 1
fi

git config --local core.hooksPath .githooks
echo 'Configured Git hooks path: .githooks'
