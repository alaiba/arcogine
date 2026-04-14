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

echo "==> Ensuring quality-gate binaries are present..."
if ! command -v trivy >/dev/null 2>&1; then
  echo "    - trivy missing, installing v0.69.3"
  curl -fsSL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- -b /tmp/trivy
  sudo mv /tmp/trivy/trivy /usr/local/bin/trivy
fi

if ! command -v gitleaks >/dev/null 2>&1; then
  echo "    - gitleaks missing, installing latest release"
  VERSION=$(curl -fsSL https://api.github.com/repos/gitleaks/gitleaks/releases/latest | sed -n 's/.*\"tag_name\": \"\\([^\"]*\\)\".*/\\1/p' | head -n1)
  ARCH="$(uname -m)"
  case "$ARCH" in
    x86_64) GITLEAKS_ARCH="linux_x64" ;;
    aarch64|arm64) GITLEAKS_ARCH="linux_arm64" ;;
    *) echo "Unsupported architecture for gitleaks: $ARCH" ; exit 1 ;;
  esac
  ASSET="gitleaks_${VERSION#v}_linux_x64.tar.gz"
  TMPDIR=$(mktemp -d)
  curl -fsSL "https://github.com/gitleaks/gitleaks/releases/download/$VERSION/${ASSET/linux_x64/$GITLEAKS_ARCH}" -o "$TMPDIR/$ASSET"
  tar -xzf "$TMPDIR/$ASSET" -C "$TMPDIR"
  sudo cp "$TMPDIR/gitleaks" /usr/local/bin/gitleaks
  sudo chmod +x /usr/local/bin/gitleaks
  rm -rf "$TMPDIR"
fi

trivy --version >/dev/null 2>&1 && echo "    - trivy: $(trivy --version | head -n1)" || true
gitleaks version >/dev/null 2>&1 && echo "    - gitleaks: $(gitleaks version)" || true

echo "==> Dev container ready. Quick commands:"
echo "    cargo test              — run all Rust tests"
echo "    cargo watch -x test     — watch mode"
echo "    cargo run --bin arcogine -- serve  — start API on :3000"
echo "    cd ui && npm run dev    — start Vite on :5173"
