#!/bin/sh
# Final clean experiment run: restore the scratch tree to the execution SHA, install the experiment
# sources, run only the Campaign 1 classes and write ledgers to $CAMPAIGN1_RESULTS_DIR.
set -u
cd /repo && git checkout -q -- product
sh /packet/tools/install-experiments.sh > "$CAMPAIGN1_RESULTS_DIR.sources.txt"
cd /repo/product
for module in types domains/factory storage; do
  rm -rf "$module/build/test-results"
done
sh ./gradlew --console=plain --continue \
  :types:test --tests 'com.arcogine.types.Campaign1*' \
  :factory:test --tests 'com.arcogine.factory.model.Campaign1*' \
  :storage:test --tests 'com.arcogine.governance.change.Campaign1*' 2>&1 | grep -E 'BUILD|FAILED'
sh /packet/tools/summarize-results.sh
