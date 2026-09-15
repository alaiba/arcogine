#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEMP_ROOT="$(mktemp -d)"
trap 'rm -rf "$TEMP_ROOT"' EXIT

source "$SCRIPT_DIR/git-identity.sh"

git -C "$TEMP_ROOT" init -q
pushd "$TEMP_ROOT" >/dev/null

ARCOGINE_GIT_USER_NAME='Arcogine Owner' \
ARCOGINE_GIT_USER_EMAIL='owner@example.com' \
  bash -c 'source "$1"; configure_arcogine_git_identity' bash "$SCRIPT_DIR/git-identity.sh" >configured.log 2>&1

[[ "$(git config --local user.name)" == 'Arcogine Owner' ]]
[[ "$(git config --local user.email)" == 'owner@example.com' ]]
grep -qF 'Git commit identity: Arcogine Owner <owner@example.com>' configured.log

git config --local user.name 'Configured Owner'
git config --local user.email configured@example.com
ARCOGINE_GIT_USER_NAME= ARCOGINE_GIT_USER_EMAIL= \
  bash -c 'source "$1"; configure_arcogine_git_identity' bash "$SCRIPT_DIR/git-identity.sh" >existing.log 2>&1
grep -qF 'Git commit identity: Configured Owner <configured@example.com>' existing.log

git config --local user.name Claude
git config --local user.email noreply@anthropic.com
if ARCOGINE_GIT_USER_NAME= ARCOGINE_GIT_USER_EMAIL= bash -c 'source "$1"; configure_arcogine_git_identity' bash "$SCRIPT_DIR/git-identity.sh" >bot.log 2>&1; then
  :
else
  echo 'identity helper unexpectedly blocked setup' >&2
  exit 1
fi
grep -qF 'WARNING: Git identity appears agent- or bot-owned' bot.log

popd >/dev/null
echo 'Git identity checks passed.'
