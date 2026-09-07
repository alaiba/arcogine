#!/usr/bin/env bash
set -euo pipefail

repo_root="$(git rev-parse --show-toplevel)"
version="1.7.12"
archive_sha256="8aca8db96f1b94770f1b0d72b6dddcb1ebb8123cb3712530b08cc387b349a3d8"
tmp_dir="$(mktemp -d)"
trap 'rm -rf "$tmp_dir"' EXIT
archive="$tmp_dir/actionlint.tar.gz"

# Always download and verify the exact validator version used by CI. Do not
# silently accept a runner- or developer-provided actionlint from PATH: parser
# behavior is part of the disposition-gate regression contract.
curl -fsSL "https://github.com/rhysd/actionlint/releases/download/v${version}/actionlint_${version}_linux_amd64.tar.gz" -o "$archive"
echo "${archive_sha256}  ${archive}" | sha256sum -c -
tar -xzf "$archive" -C "$tmp_dir" actionlint
actionlint_bin="$tmp_dir/actionlint"

cd "$repo_root"
"$actionlint_bin" -shellcheck= -pyflakes= .github/workflows/*.yml
