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
case "${1:-} ${2:-}" in
  'api user')
    printf '%s\t42\t%s\n' "${GH_TEST_LOGIN:-owner}" "${GH_TEST_PROFILE_EMAIL:-public@example.com}"
    ;;
  'api repos/alaiba/arcogine')
    printf '%s\tUser\n' "${GH_TEST_REPO_OWNER:-owner}"
    ;;
  'api --paginate')
    [[ "${3:-}" == user/emails ]] || exit 2
    if [[ "${GH_TEST_EMAILS:-}" == available ]]; then
      printf '%s\n' "${GH_TEST_VERIFIED_EMAILS:-}"
    else
      exit 1
    fi
    ;;
  *) exit 2 ;;
esac
EOF
chmod +x "$TEMP_ROOT/bin/gh"
git config --local user.email secondary@example.com
GH_TEST_EMAILS=available GH_TEST_VERIFIED_EMAILS=secondary@example.com \
  PATH="$TEMP_ROOT/bin:$PATH" bash "$SCRIPT_DIR/check-git-identity.sh" >identity-secondary.log
grep -qF 'Git commit emails match the authenticated GitHub account: @owner' identity-secondary.log

if GH_TEST_LOGIN=other PATH="$TEMP_ROOT/bin:$PATH" \
  bash "$SCRIPT_DIR/check-git-identity.sh" >identity-wrong-owner.log 2>&1; then
  echo 'identity check unexpectedly accepted a non-owner GitHub account' >&2
  exit 1
fi
grep -qF 'is not the human owner of alaiba/arcogine' identity-wrong-owner.log

git config --local user.email public@example.com
GH_TEST_EMAILS=unavailable PATH="$TEMP_ROOT/bin:$PATH" \
  bash "$SCRIPT_DIR/check-git-identity.sh" >identity-public.log
grep -qF 'Git commit emails match the authenticated GitHub account: @owner' identity-public.log

git config --local user.email '42+owner@users.noreply.github.com'
GH_TEST_EMAILS=unavailable GH_TEST_PROFILE_EMAIL= PATH="$TEMP_ROOT/bin:$PATH" \
  bash "$SCRIPT_DIR/check-git-identity.sh" >identity-noreply.log
grep -qF 'Git commit emails match the authenticated GitHub account: @owner' identity-noreply.log

git config --local user.email private-secondary@example.com
if GH_TEST_EMAILS=unavailable GH_TEST_PROFILE_EMAIL= PATH="$TEMP_ROOT/bin:$PATH" \
  bash "$SCRIPT_DIR/check-git-identity.sh" >identity-private-unknown.log 2>&1; then
  echo 'identity check unexpectedly accepted an unverified private email' >&2
  exit 1
fi
grep -qF 'gh auth refresh -h github.com -s user' identity-private-unknown.log

git config --local user.email unrelated@example.com
if GH_TEST_EMAILS=available GH_TEST_VERIFIED_EMAILS=secondary@example.com \
  PATH="$TEMP_ROOT/bin:$PATH" bash "$SCRIPT_DIR/check-git-identity.sh" >identity-mismatch.log 2>&1; then
  echo 'identity check unexpectedly accepted an email not associated with the account' >&2
  exit 1
fi
grep -qF 'is not confirmed as an email for the authenticated GitHub account' identity-mismatch.log

git config --local user.email public@example.com
if GH_TEST_EMAILS=unavailable GIT_AUTHOR_EMAIL=author-override@example.com \
  PATH="$TEMP_ROOT/bin:$PATH" bash "$SCRIPT_DIR/check-git-identity.sh" >author-override.log 2>&1; then
  echo 'identity check unexpectedly accepted an author email override' >&2
  exit 1
fi
grep -qF 'effective author email' author-override.log

popd >/dev/null
echo 'Git identity checks passed.'
