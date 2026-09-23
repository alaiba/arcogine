#!/usr/bin/env bash
set -euo pipefail

# Workspace initialization only. Toolchain installation lives in the
# Dockerfile; do not add tool-install fallbacks here.

echo "==> Configuring git..."
gh auth setup-git 2>/dev/null || true
REMOTE_URL=$(git remote get-url origin 2>/dev/null || true)
if [[ "$REMOTE_URL" == git@github.com:* ]]; then
  HTTPS_URL=$(echo "$REMOTE_URL" | sed 's|git@github.com:|https://github.com/|')
  git remote set-url origin "$HTTPS_URL"
  echo "    Switched remote to HTTPS for credential forwarding"
fi

echo "==> Checking Git commit identity..."
# Configure with explicit ARCOGINE_GIT_USER_* values when supplied; otherwise
# inspect the existing identity and warn without blocking container creation.
source infra/dev/git-identity.sh
configure_arcogine_git_identity

echo "==> Installing repository dependencies (./arcogine setup)..."
./arcogine setup

echo "==> Dev container ready. Canonical commands:"
echo "    ./arcogine setup   — (re-)install dependencies"
echo "    ./arcogine test    — run Java unit tests"
echo "    ./arcogine check   — run full quality gates"
