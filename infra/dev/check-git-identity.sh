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

if ! account_info="$(gh api user --jq '[.login, (.id | tostring), (.email // "")] | @tsv' 2>/dev/null)"; then
  account_info=""
fi
IFS=$'\t' read -r github_login github_id github_profile_email <<< "$account_info"
if [[ -z "${github_login:-}" || -z "${github_id:-}" ]]; then
  echo 'ERROR: Could not identify the authenticated GitHub account; commit blocked.' >&2
  echo '       Authenticate gh and retry.' >&2
  exit 1
fi

# The verified email list requires gh's user scope. A missing scope does not
# invalidate public or privacy-preserving noreply addresses; it only means a
# private secondary address cannot be confirmed by this check.
verified_emails=""
verified_email_list_available=false
if verified_emails="$(gh api user/emails --jq '.[] | select(.verified == true) | .email' 2>/dev/null)"; then
  verified_email_list_available=true
fi

email_belongs_to_account() {
  local candidate="$1"
  local verified_email
  local normalized_candidate="${candidate,,}"

  [[ -n "$normalized_candidate" ]] || return 1
  if [[ -n "$github_profile_email" && "$normalized_candidate" == "${github_profile_email,,}" ]]; then
    return 0
  fi

  while IFS= read -r verified_email; do
    if [[ -n "$verified_email" && "$normalized_candidate" == "${verified_email,,}" ]]; then
      return 0
    fi
  done <<< "$verified_emails"

  if [[ "$normalized_candidate" == "${github_id}+${github_login,,}@users.noreply.github.com" ||
    "$normalized_candidate" == "${github_login,,}@users.noreply.github.com" ]]; then
    return 0
  fi
  return 1
}

author_ident="$(git var GIT_AUTHOR_IDENT 2>/dev/null || true)"
committer_ident="$(git var GIT_COMMITTER_IDENT 2>/dev/null || true)"
if [[ "$author_ident" =~ \<([^\>]*)\> ]]; then
  author_email="${BASH_REMATCH[1]}"
else
  author_email=""
fi
if [[ "$committer_ident" =~ \<([^\>]*)\> ]]; then
  committer_email="${BASH_REMATCH[1]}"
else
  committer_email=""
fi

for identity in "configured Git email:$configured_email" "effective author email:$author_email" "effective committer email:$committer_email"; do
  identity_name="${identity%%:*}"
  identity_email="${identity#*:}"
  if ! email_belongs_to_account "$identity_email"; then
    printf 'ERROR: %s is not confirmed as an email for the authenticated GitHub account; commit blocked.\n' "$identity_name" >&2
    printf '       Email: %s\n' "$identity_email" >&2
    printf '       Account: @%s\n' "$github_login" >&2
    if [[ "$verified_email_list_available" == true ]]; then
      echo '       Use a verified account email or a GitHub noreply address for this account.' >&2
    else
      echo '       GitHub did not expose the verified email list to gh.' >&2
      echo '       For a private or secondary email, grant the user scope with: gh auth refresh -h github.com -s user' >&2
      echo '       Or use this account’s GitHub noreply address.' >&2
    fi
    exit 1
  fi
done

echo "Git commit emails match the authenticated GitHub account: @$github_login"
