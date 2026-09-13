#!/usr/bin/env bash
# Trusted Dependabot provenance verifier for the PR disposition workflow.
#
# A Dependabot-authored PR is eligible for positive-review bypass only when
# trusted GitHub API state proves that both the PR opener and the pull-request
# event that produced CI for the exact current head are GitHub's Dependabot
# bot. This matters because maintainers may push commits to Dependabot branches:
# PR authorship alone would otherwise let later human-authored heads inherit the
# bot's review exception.
#
# Inputs (all supplied by the trusted base-side workflow after GitHub API reads):
#   PR_NUMBER             - current pull request number
#   PR_HEAD_SHA           - current pull_request.head.sha
#   PR_AUTHOR_LOGIN       - current pull_request.user.login
#   PR_AUTHOR_TYPE        - current pull_request.user.type
#   PR_AUTHOR_ID          - current pull_request.user.id
#   PR_HEAD_CI_RUN_B64    - base64-encoded GitHub Actions workflow-run object
#                           for CI on the exact current PR head
#
# Exit 0 only when:
#   - the PR opener is GitHub's exact Dependabot bot account;
#   - the supplied CI run is a pull_request run named CI for the exact head;
#   - that run is associated with this PR; and
#   - the run actor is the same exact Dependabot bot account.
#
# The stable GitHub account id is checked in addition to login/type. Mutable PR
# text, branch names, labels, commit author strings, and commit messages are not
# provenance signals.

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

echo "Dependabot provenance passed: PR #$PR_NUMBER head $PR_HEAD_SHA was opened and current-head CI was initiated by GitHub Dependabot."
