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

mkdir -p "$TEMP_ROOT/bin"
cat >"$TEMP_ROOT/bin/gh" <<'EOF'
#!/usr/bin/env bash
if [[ "$1 $2" == api\ user/emails ]]; then
  if [[ "${GH_TEST_MODE:-}" == primary ]]; then echo owner@example.com; fi
elif [[ "$1 $2" == api\ user ]]; then
  echo 'owner@example.com'
else
  exit 2
fi
EOF
chmod +x "$TEMP_ROOT/bin/gh"
git config --local user.email owner@example.com
GH_TEST_MODE=primary PATH="$TEMP_ROOT/bin:$PATH" bash "$SCRIPT_DIR/check-git-identity.sh" >identity-primary.log
grep -qF 'Git commit email matches the authenticated GitHub account: owner@example.com' identity-primary.log

PATH="$TEMP_ROOT/bin:$PATH" bash "$SCRIPT_DIR/check-git-identity.sh" >identity-match.log
grep -qF 'Git commit email matches the authenticated GitHub account: owner@example.com' identity-match.log

git config --local user.email other@example.com
if PATH="$TEMP_ROOT/bin:$PATH" bash "$SCRIPT_DIR/check-git-identity.sh" >identity-mismatch.log 2>&1; then
  echo 'identity check unexpectedly accepted an email mismatch' >&2
  exit 1
fi
grep -qF 'Git commit email does not match the authenticated GitHub account' identity-mismatch.log

popd >/dev/null
echo 'Git identity checks passed.'
