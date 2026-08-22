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

echo "==> Copying .env.example -> .env (if not present)..."
[ -f .env ] || cp .env.example .env

echo "==> Ensuring node_modules volume is writable..."
# Non-recursive: the mount point itself is what a fresh named volume
# creates root-owned; everything under it is written by vscode-run
# tooling from here on, so there's nothing to chown recursively.
# (The Gradle cache volume doesn't need this: the Dockerfile pre-creates
# its mount point owned by vscode, which Docker copies into the volume
# on first mount.)
sudo chown vscode:vscode ui/node_modules 2>/dev/null || true

echo "==> Installing UI dependencies..."
cd ui && npm ci && cd ..

echo "==> Installing Playwright Chromium browser for E2E tests..."
cd ui && npx playwright install chromium && cd ..

echo "==> Dev container ready. Canonical commands:"
echo "    ./arcogine setup   — (re-)install dependencies"
echo "    ./arcogine test    — run Java + frontend unit tests"
echo "    ./arcogine check   — run full quality gates"
echo "    ./arcogine run api — start the API on :3000"
echo "    ./arcogine run ui  — start the UI on :5173"
