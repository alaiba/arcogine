#!/usr/bin/env bash
# Builds the changed-path list consumed by classify-changes.sh.
# Usage: discover-changed-files.sh EVENT_NAME [PR_BASE_SHA] [PUSH_BEFORE] [PUSH_AFTER]
set -euo pipefail

event_name="${1:-}"
pr_base_sha="${2:-}"
push_before="${3:-}"
push_after="${4:-}"

case "$event_name" in
  pull_request)
    if [[ ! "$pr_base_sha" =~ ^([[:xdigit:]]{40}|[[:xdigit:]]{64})$ ]] || \
      ! git cat-file -e "${pr_base_sha}^{commit}" 2>/dev/null; then
      # Keep the complete tracked-file list as the conservative fallback. The
      # caller emits the GitHub warning when this distinct status is returned.
      git ls-files
      exit 2
    fi
    git diff --name-only "${pr_base_sha}...HEAD"
    ;;
  push)
    git diff --name-only "$push_before" "$push_after" 2>/dev/null || git ls-files
    ;;
  *)
    # schedule / workflow_dispatch: full security sweep.
    git ls-files
    ;;
esac
