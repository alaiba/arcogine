#!/usr/bin/env bash
set -euo pipefail

echo "==> Building Rust workspace..."
cargo build

echo "==> Installing UI dependencies..."
cd ui && npm ci && cd ..

echo "==> Copying .env.example → .env (if not present)..."
[ -f .env ] || cp .env.example .env

echo "==> Dev container ready. Quick commands:"
echo "    cargo test              — run all Rust tests"
echo "    cargo watch -x test     — watch mode"
echo "    cargo run --bin arcogine -- serve  — start API on :3000"
echo "    cd ui && npm run dev    — start Vite on :5173"
