#!/bin/sh
# Copy the transient Campaign 1 experiment sources into the scratch product tree (never the real
# worktree). Run inside the build container: /packet is this packet, /repo the git-archive scratch.
set -eu
cp -r /packet/experiment-sources/types/src/. /repo/product/types/src/
cp -r /packet/experiment-sources/factory/src/. /repo/product/domains/factory/src/
if [ -d /packet/experiment-sources/storage ]; then
  cp -r /packet/experiment-sources/storage/src/. /repo/product/storage/src/
fi
find /packet/experiment-sources -name '*.java' | sort | while read -r f; do sha256sum "$f"; done
