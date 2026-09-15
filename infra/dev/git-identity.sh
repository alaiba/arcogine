#!/usr/bin/env bash
# Configure and validate the human Git identity used for Arcogine commits.
# This helper is intentionally warning-only: a bad or missing identity must not
# make the development container unusable.

configure_arcogine_git_identity() {
  local requested_name="${ARCOGINE_GIT_USER_NAME:-}"
  local requested_email="${ARCOGINE_GIT_USER_EMAIL:-}"
  local configured_name configured_email owner_name owner_email identity

  if [[ -n "$requested_name" || -n "$requested_email" ]]; then
    if [[ -z "$requested_name" || -z "$requested_email" ]]; then
      echo "WARNING: set both ARCOGINE_GIT_USER_NAME and ARCOGINE_GIT_USER_EMAIL to configure the commit identity; leaving Git configuration unchanged." >&2
    elif git config --local user.name "$requested_name" &&
      git config --local user.email "$requested_email" &&
      git config --local arcogine.owner.name "$requested_name" &&
      git config --local arcogine.owner.email "$requested_email"; then
      echo "    Configured Git commit identity from ARCOGINE_GIT_USER_NAME/ARCOGINE_GIT_USER_EMAIL."
    else
      echo "WARNING: could not configure the requested local Git identity; continuing with the existing configuration." >&2
    fi
  fi

  configured_name="$(git config --get user.name 2>/dev/null || true)"
  configured_email="$(git config --get user.email 2>/dev/null || true)"
  owner_name="$(git config --get arcogine.owner.name 2>/dev/null || true)"
  owner_email="$(git config --get arcogine.owner.email 2>/dev/null || true)"
  identity="${configured_name} <${configured_email}>"

  if [[ -z "$configured_name" || -z "$configured_email" ]]; then
    echo "WARNING: Git author/committer identity is incomplete; commits may be attributed incorrectly." >&2
  elif [[ "${configured_name,,}" =~ (^|[^[:alpha:]])(claude|codex|anthropic|openai|bot)([^[:alpha:]]|$) ]] ||
    [[ "${configured_email,,}" == 'noreply@anthropic.com' ]]; then
    echo "WARNING: Git identity appears agent- or bot-owned: $identity" >&2
    echo "         Set ARCOGINE_GIT_USER_NAME and ARCOGINE_GIT_USER_EMAIL to the human repository owner's identity before committing." >&2
  else
    if [[ -z "$owner_name" && -z "$owner_email" ]]; then
      if git config --local arcogine.owner.name "$configured_name" &&
        git config --local arcogine.owner.email "$configured_email"; then
        echo "    Persisted the validated Arcogine owner identity for later tooling."
      fi
    elif [[ "$owner_name" != "$configured_name" || "$owner_email" != "$configured_email" ]]; then
      echo "WARNING: durable Arcogine owner identity does not match Git commit identity: $identity" >&2
    fi
    echo "    Git commit identity: $identity"
  fi
}

if [[ "${BASH_SOURCE[0]}" == "$0" ]]; then
  configure_arcogine_git_identity
fi
