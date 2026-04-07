#!/usr/bin/env bash
set -euo pipefail

echo "==> Configuring git..."
gh auth setup-git 2>/dev/null || true
REMOTE_URL=$(git remote get-url origin 2>/dev/null || true)
if [[ "$REMOTE_URL" == git@github.com:* ]]; then
  HTTPS_URL=$(echo "$REMOTE_URL" | sed 's|git@github.com:|https://github.com/|')
  git remote set-url origin "$HTTPS_URL"
  echo "    Switched remote to HTTPS for credential forwarding"
fi

echo "==> Building Rust workspace..."
cargo build

echo "==> Installing UI dependencies..."
cd ui && npm ci && cd ..

echo "==> Installing Playwright Chromium browser for E2E tests..."
cd ui && npx playwright install chromium && cd ..

echo "==> Copying .env.example → .env (if not present)..."
[ -f .env ] || cp .env.example .env

echo "==> Dev container ready. Quick commands:"
echo "    cargo test              — run all Rust tests"
echo "    cargo watch -x test     — watch mode"
echo "    cargo run --bin arcogine -- serve  — start API on :3000"
echo "    cd ui && npm run dev    — start Vite on :5173"
