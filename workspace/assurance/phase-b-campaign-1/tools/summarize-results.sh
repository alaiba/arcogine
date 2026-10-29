#!/bin/sh
# Print one line per JUnit XML suite (class tests skipped failures errors) for the campaign modules,
# then per-module totals. Run inside the build container from /repo/product.
set -u
for module in types domains/factory storage governance; do
  dir="$module/build/test-results/test"
  [ -d "$dir" ] || continue
  for xml in "$dir"/*.xml; do
    sed -n 's/.*<testsuite name="\([^"]*\)" tests="\([0-9]*\)" skipped="\([0-9]*\)" failures="\([0-9]*\)" errors="\([0-9]*\)".*/\1 \2 \3 \4 \5/p' "$xml"
  done | sort | awk -v m="$module" '{print m, $0; t+=$2; s+=$3; f+=$4; e+=$5} END {print "TOTAL", m, "suites=" NR, "tests=" t, "skipped=" s, "failures=" f, "errors=" e}'
done
