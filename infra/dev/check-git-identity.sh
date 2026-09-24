#!/usr/bin/env bash
set -euo pipefail

configured_email="$(git config --get user.email 2>/dev/null || true)"
if [[ -z "$configured_email" ]]; then
  echo 'ERROR: Git has no user.email configured; commit blocked.' >&2
  exit 1
fi

if ! command -v gh >/dev/null 2>&1; then
  echo 'ERROR: GitHub CLI (gh) is required to verify the commit email; commit blocked.' >&2
  exit 1
fi

# The verified primary email is available when gh has the user scope. Fall back
# to the public profile email when that scope is unavailable.
if ! github_email="$(gh api user/emails --jq '[.[] | select(.primary and .verified) | .email][0] // empty' 2>/dev/null)"; then
  github_email=""
fi
if [[ -z "$github_email" ]]; then
  if ! github_email="$(gh api user --jq '.email // empty' 2>/dev/null)"; then
    github_email=""
  fi
fi

if [[ -z "$github_email" ]]; then
  echo 'ERROR: Could not read a verified primary or public email from the authenticated GitHub account; commit blocked.' >&2
  echo '       Authenticate gh, or publish the account email in GitHub settings.' >&2
  exit 1
fi

if [[ "$configured_email" != "$github_email" ]]; then
  printf 'ERROR: Git commit email does not match the authenticated GitHub account; commit blocked.\n' >&2
  printf '       Git:    %s\n' "$configured_email" >&2
  printf '       GitHub: %s\n' "$github_email" >&2
  printf '       Update this repository with: git config --local user.email "%s"\n' "$github_email" >&2
  exit 1
fi

echo "Git commit email matches the authenticated GitHub account: $github_email"
