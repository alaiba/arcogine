#!/bin/sh
# Verify every mutant's old text occurs exactly once at the baseline, restoring after each.
set -u
for id in $(java /packet/tools/Mutator.java /packet/mutation/mutants.txt list | cut -d' ' -f1); do
  java /packet/tools/Mutator.java /packet/mutation/mutants.txt apply "$id" /repo 2>&1 | tail -1
  (cd /repo && git checkout -q -- product/types/src/main product/governance/src/main product/storage/src/main product/domains/factory/src/main)
done
