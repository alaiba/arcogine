#!/usr/bin/env bash
# Trusted Dependabot provenance verifier for the PR disposition workflow.
#
# A Dependabot-authored PR is eligible for review-authorization bypass only
# while its *current* PR commit set remains entirely Dependabot-created. GitHub
# allows maintainers to push extra commits onto Dependabot branches, so PR
# author identity alone is deliberately insufficient.
#
# Inputs (all supplied by the trusted base-side workflow after GitHub API reads):
#   PR_HEAD_SHA          - current pull_request.head.sha
#   PR_AUTHOR_LOGIN      - current pull_request.user.login
#   PR_AUTHOR_TYPE       - current pull_request.user.type
#   PR_COMMITS_JSON_B64  - base64-encoded JSON array returned by
#                          GET /repos/{owner}/{repo}/pulls/{number}/commits
#
# Exit 0 only when:
#   - the PR author is GitHub's exact Dependabot bot account;
#   - at least one PR commit exists;
#   - the final listed PR commit is the exact current PR head; and
#   - every PR commit is associated by GitHub with dependabot[bot], has account
#     type Bot, and carries a GitHub-verified commit signature.
#
# Any maintainer-authored reconciliation or compatibility commit intentionally
# revokes this bypass and returns the PR to the ordinary review path. To keep a
# stale Dependabot PR eligible, refresh it through Dependabot's own rebase/
# recreate mechanism so the resulting current commit set remains bot-created.

set -euo pipefail

if [ -z "${PR_HEAD_SHA:-}" ]; then
  echo "Dependabot provenance failed: PR_HEAD_SHA is not set." >&2
  exit 1
fi

if [ "${PR_AUTHOR_LOGIN:-}" != "dependabot[bot]" ] || [ "${PR_AUTHOR_TYPE:-}" != "Bot" ]; then
  echo "Dependabot provenance failed: PR author is not the trusted dependabot[bot] Bot account." >&2
  exit 1
fi

if [ -z "${PR_COMMITS_JSON_B64:-}" ]; then
  echo "Dependabot provenance failed: PR commit list is absent." >&2
  exit 1
fi

commits_json=$(printf '%s' "$PR_COMMITS_JSON_B64" | base64 -d 2>/dev/null) || {
  echo "Dependabot provenance failed: PR commit list is not valid base64." >&2
  exit 1
}

if ! jq -e --arg head "$PR_HEAD_SHA" '
  type == "array"
  and length > 0
  and .[-1].sha == $head
  and all(.[ ];
    .author.login == "dependabot[bot]"
    and .author.type == "Bot"
    and .commit.verification.verified == true
  )
' >/dev/null <<<"$commits_json"; then
  echo "Dependabot provenance failed: current PR commits are not exclusively verified Dependabot commits bound to head $PR_HEAD_SHA." >&2
  exit 1
fi

echo "Dependabot provenance passed: current head $PR_HEAD_SHA contains only verified Dependabot-created PR commits."
