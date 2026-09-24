#!/usr/bin/env bash
# Explicit entry point for the always-required repository-tooling suite.
set -euo pipefail
repo_root="$(git rev-parse --show-toplevel)"
cd "$repo_root"

run() {
  local description="$1"
  shift
  echo "==> $description"
  "$@"
}

run "Changed-file classifier" bash .github/scripts/classify-changes.test.sh
run "Changed-file discovery" bash .github/scripts/discover-changed-files.test.sh
run "Arcogine preflight" bash .github/scripts/arcogine-preflight.test.sh
run "Arcogine CLI" bash .github/scripts/arcogine-cli.test.sh
run "Claude Cloud provisioning" bash infra/dev/claude-cloud.test.sh
run "Git identity setup" bash infra/dev/git-identity.test.sh
run "Node repository-tooling tests" node --test \
  .github/scripts/check-markdown-links.test.mjs \
  .github/scripts/check-delivery-labels.test.mjs \
  .github/scripts/check-transient-workspace.test.mjs \
  .github/scripts/check-transient-coordinates.test.mjs \
  .github/scripts/check-pr-disposition.test.mjs \
  .github/scripts/check-dependabot-provenance.test.mjs \
  .github/scripts/check-continuous-improvement-reminder.test.mjs \
  .github/scripts/check-ci-gate.test.mjs \
  infra/dev/delivery-retrospective.test.mjs \
  infra/dev/delivery-retrospective-github.test.mjs \
  infra/dev/repo-snapshot.test.mjs \
  infra/dev/github-attribution-hygiene.test.mjs
run "Markdown-link check" node .github/scripts/check-markdown-links.mjs .
run "Delivery-label check" node .github/scripts/check-delivery-labels.mjs
run "Transient-coordinate check" node .github/scripts/check-transient-coordinates.mjs
run "Transient-workspace check" node .github/scripts/check-transient-workspace.mjs
run "Continuous-improvement reminder contract" node .github/scripts/check-continuous-improvement-reminder.mjs
run "GitHub Actions workflow syntax" bash .github/scripts/check-actions-workflows.sh
