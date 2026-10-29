#!/bin/sh
# Supplementary T-TIME run: SimTime mutants against the retained Engine overflow classes, to record
# which value-level faults the Engine-outcome tests also observe. Usage: run-time-engine-mutants.sh OUT_DIR
set -u
out="$1"
mkdir -p "$out"
cd /repo/product
set -f
selection=":factory:test --tests com.arcogine.factory.process.TaskEndTimeOverflowTest --tests com.arcogine.factory.process.SequenceExhaustionTest :simulation:test"
for id in BASELINE T-CTOR-ADMITS-MINUS-ONE T-CANADD-NEG-OFF-BY-ONE T-CANADD-POS-OFF-BY-ONE T-PLUS-UNCHECKED T-MINUS-UNCLAMPED; do
  (cd /repo && git checkout -q -- product/types/src/main)
  rm -rf domains/factory/build/test-results simulation/build/test-results
  if [ "$id" != BASELINE ]; then
    java /packet/tools/Mutator.java /packet/mutation/mutants.txt apply "$id" /repo > "$out/$id.apply" 2>&1 || { echo "STATUS $id INCONCLUSIVE apply-failed" > "$out/$id.result"; continue; }
  fi
  # shellcheck disable=SC2086
  sh ./gradlew --console=plain --continue $selection > "$out/$id.log" 2>&1
  code=$?
  set +f
  for dir in domains/factory/build/test-results/test simulation/build/test-results/test; do
    for xml in "$dir"/*.xml; do
      [ -f "$xml" ] && sed -n 's/.*<testsuite name="\([^"]*\)" tests="\([0-9]*\)" skipped="\([0-9]*\)" failures="\([0-9]*\)" errors="\([0-9]*\)".*/engine \1 \2 \3 \4 \5/p' "$xml"
    done
  done | sort > "$out/$id.result"
  if grep -qE 'compile(Test)?Java FAILED' "$out/$id.log"; then status="INCONCLUSIVE compile-failure"
  elif awk '$5+$6>0 {f=1} END {exit !f}' "$out/$id.result"; then status="FAILING-TESTS"
  elif [ "$code" -ne 0 ]; then status="INCONCLUSIVE build-exit-$code"
  else status="ALL-PASS"; fi
  echo "STATUS $id $status selection=time-engine exit=$code" >> "$out/$id.result"
  tail -1 "$out/$id.result"
done
(cd /repo && git checkout -q -- product/types/src/main)
