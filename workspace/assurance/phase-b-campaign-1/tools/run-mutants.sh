#!/bin/sh
# Campaign 1 mutation runner. Inside the build container (/repo = git-archive scratch with its
# baseline in the index, /packet = this packet). Usage: run-mutants.sh OUT_DIR [ID ...]
# The pseudo-ID BASELINE runs every selection unmutated. Results: OUT_DIR/<ID>.result, one line
# per JUnit suite (class tests skipped failures errors) plus a STATUS line.
set -uf
out="$1"; shift
mkdir -p "$out"
cd /repo/product

selection_args() {
  case "$1" in
    types) echo ":types:test" ;;
    factory) echo ":factory:test --tests com.arcogine.factory.model.* --tests com.arcogine.factory.change.* --tests com.arcogine.factory.process.EligibleMachineOrderTest --tests com.arcogine.factory.process.ConfiguredResourceMetadataTest" ;;
    types+factory) echo ":types:test $(selection_args factory)" ;;
    seam) echo ":storage:test :governance:test --tests com.arcogine.governance.change.*" ;;
    all) echo "$(selection_args types+factory) $(selection_args seam)" ;;
  esac
}

ids="$*"
[ -n "$ids" ] || ids="BASELINE $(java /packet/tools/Mutator.java /packet/mutation/mutants.txt list | cut -d' ' -f1)"

for id in $ids; do
  (cd /repo && git checkout -q -- product/types/src/main product/governance/src/main product/storage/src/main product/domains/factory/src/main)
  rm -rf types/build/test-results domains/factory/build/test-results storage/build/test-results governance/build/test-results
  if [ "$id" = BASELINE ]; then
    sel=all
  else
    sel=$(java /packet/tools/Mutator.java /packet/mutation/mutants.txt list | awk -v i="$id" '$1==i {print $2}')
    if ! java /packet/tools/Mutator.java /packet/mutation/mutants.txt apply "$id" /repo > "$out/$id.apply" 2>&1; then
      echo "STATUS $id INCONCLUSIVE apply-failed" > "$out/$id.result"
      continue
    fi
  fi
  start=$(date +%s)
  # shellcheck disable=SC2046
  sh ./gradlew --console=plain --continue $(selection_args "$sel") > "$out/$id.log" 2>&1
  code=$?
  elapsed=$(( $(date +%s) - start ))
  sh /packet/tools/summarize-results.sh | grep -v '^TOTAL' > "$out/$id.result"
  if grep -qE 'compile(Test)?Java FAILED' "$out/$id.log"; then
    status="INCONCLUSIVE compile-failure"
  elif grep -qE 'There were failing tests|Test.*FAILED' "$out/$id.log" || awk '$5+$6>0 {f=1} END {exit !f}' "$out/$id.result"; then
    status="FAILING-TESTS"
  elif [ "$code" -ne 0 ]; then
    status="INCONCLUSIVE build-exit-$code"
  else
    status="ALL-PASS"
  fi
  echo "STATUS $id $status selection=$sel exit=$code seconds=$elapsed" >> "$out/$id.result"
  tail -1 "$out/$id.result"
done
(cd /repo && git checkout -q -- product/types/src/main product/governance/src/main product/storage/src/main product/domains/factory/src/main)
