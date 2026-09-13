#!/usr/bin/env bash
# Trusted Dependabot provenance verifier for the PR disposition workflow.
#
# A Dependabot-authored PR is eligible for positive-review bypass only when
# trusted GitHub API state proves all of the following for the exact current
# head:
#   - the PR opener is GitHub's exact Dependabot bot account;
#   - the CI pull-request workflow run for that exact head was initiated by
#     that same Dependabot account; and
#   - every commit carried by the PR is GitHub-associated with Dependabot and
#     signature-verified, with the final PR commit equal to the current head.
#
# The CI actor check matters because maintainers may push commits to Dependabot
# branches. The commit-set check is defense in depth: a later bot-triggered
# rebase/recreate must not make an earlier maintainer-authored commit disappear
# from provenance analysis merely because the newest workflow actor is the bot.
#
# Inputs supplied by the trusted base-side workflow after GitHub API reads:
#   PR_NUMBER             - current pull request number
#   PR_HEAD_SHA           - current pull_request.head.sha
#   PR_AUTHOR_LOGIN       - current pull_request.user.login
#   PR_AUTHOR_TYPE        - current pull_request.user.type
#   PR_AUTHOR_ID          - current pull_request.user.id
#   PR_HEAD_CI_RUN_B64    - base64-encoded GitHub Actions workflow-run object
#                           for CI on the exact current PR head
#
# Optional test/fixture input:
#   PR_COMMITS_B64        - base64-encoded JSON array from the PR commits API.
#                           Production normally omits this; the script fetches
#                           the authoritative commit list itself with GH_TOKEN.
#
# Production API context:
#   GH_TOKEN              - token used by gh api
#   GITHUB_REPOSITORY     - owner/name of the current repository
#
# Mutable PR text, branch names, labels, commit messages, and raw git author
# strings are not provenance signals. The verifier relies on GitHub account
# association, workflow actor identity, exact-head binding, and signature state.

set -euo pipefail

DEPENDABOT_LOGIN='dependabot[bot]'
DEPENDABOT_TYPE='Bot'
DEPENDABOT_ID='49699333'

if [ -z "${PR_NUMBER:-}" ] || [ -z "${PR_HEAD_SHA:-}" ]; then
  echo "Dependabot provenance failed: PR_NUMBER and PR_HEAD_SHA are required." >&2
  exit 1
fi

if [ "${PR_AUTHOR_LOGIN:-}" != "$DEPENDABOT_LOGIN" ] \
  || [ "${PR_AUTHOR_TYPE:-}" != "$DEPENDABOT_TYPE" ] \
  || [ "${PR_AUTHOR_ID:-}" != "$DEPENDABOT_ID" ]; then
  echo "Dependabot provenance failed: PR opener is not GitHub's trusted Dependabot account." >&2
  exit 1
fi

if [ -z "${PR_HEAD_CI_RUN_B64:-}" ]; then
  echo "Dependabot provenance failed: exact-head CI run is absent." >&2
  exit 1
fi

run_json=$(printf '%s' "$PR_HEAD_CI_RUN_B64" | base64 -d 2>/dev/null) || {
  echo "Dependabot provenance failed: CI run is not valid base64." >&2
  exit 1
}

if ! jq -e \
  --arg head "$PR_HEAD_SHA" \
  --argjson pr "$PR_NUMBER" \
  --arg login "$DEPENDABOT_LOGIN" \
  --arg type "$DEPENDABOT_TYPE" \
  --argjson id "$DEPENDABOT_ID" '
    .name == "CI"
    and .event == "pull_request"
    and .head_sha == $head
    and .actor.login == $login
    and .actor.type == $type
    and .actor.id == $id
    and any(.pull_requests[]?; .number == $pr)
  ' >/dev/null <<<"$run_json"; then
  echo "Dependabot provenance failed: exact-head CI was not initiated by the trusted Dependabot account for this PR." >&2
  exit 1
fi

if [ -n "${PR_COMMITS_B64:-}" ]; then
  commits_json=$(printf '%s' "$PR_COMMITS_B64" | base64 -d 2>/dev/null) || {
    echo "Dependabot provenance failed: PR commit fixture is not valid base64." >&2
    exit 1
  }
else
  if [ -z "${GH_TOKEN:-}" ] || [ -z "${GITHUB_REPOSITORY:-}" ]; then
    echo "Dependabot provenance failed: GH_TOKEN and GITHUB_REPOSITORY are required to fetch PR commits." >&2
    exit 1
  fi

  commits_json=$(gh api \
    "repos/$GITHUB_REPOSITORY/pulls/$PR_NUMBER/commits?per_page=100" \
    --paginate \
    --slurp \
    | jq -c 'add') || {
      echo "Dependabot provenance failed: could not fetch authoritative PR commit list." >&2
      exit 1
    }
fi

if ! jq -e \
  --arg head "$PR_HEAD_SHA" \
  --arg login "$DEPENDABOT_LOGIN" \
  --arg type "$DEPENDABOT_TYPE" \
  --argjson id "$DEPENDABOT_ID" '
    length > 0
    and .[-1].sha == $head
    and all(.[];
      .author.login == $login
      and .author.type == $type
      and .author.id == $id
      and .commit.verification.verified == true
    )
  ' >/dev/null <<<"$commits_json"; then
  echo "Dependabot provenance failed: current PR commit set is not exclusively signature-verified GitHub Dependabot commits ending at the exact head." >&2
  exit 1
fi

echo "Dependabot provenance passed: PR #$PR_NUMBER head $PR_HEAD_SHA has exact Dependabot opener, exact-head Dependabot CI actor, and an exclusively signature-verified Dependabot PR commit set."
