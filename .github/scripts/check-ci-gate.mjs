#!/usr/bin/env node
const needs = JSON.parse(process.env.NEEDS_JSON ?? '{}');
const backend = process.env.BACKEND === 'true';
const fullSweep = ['schedule', 'workflow_dispatch'].includes(process.env.EVENT_NAME);
const expected = { classify: true, java: backend || fullSweep, 'java-audit': backend || fullSweep, 'security-secrets': true };
const needKeys = Object.keys(needs).sort();
const expectedKeys = Object.keys(expected).sort();
console.log(`needs: ${JSON.stringify(needKeys)}`);
console.log(`expected: ${JSON.stringify(expectedKeys)}`);
if (JSON.stringify(needKeys) !== JSON.stringify(expectedKeys)) {
  console.error('Failing gate: needs: and expected_to_run have drifted apart.');
  console.error('  in needs: but missing from expected_to_run:', needKeys.filter((key) => !(key in expected)));
  console.error('  in expected_to_run but missing from needs:', expectedKeys.filter((key) => !(key in needs)));
  process.exit(1);
}
const failed = [];
const unexpectedSkips = [];
for (const [job, info] of Object.entries(needs)) {
  console.log(`${job}: ${info?.result} (expected to run: ${expected[job]})`);
  if (['failure', 'cancelled'].includes(info?.result)) failed.push([job, info.result]);
  else if (info?.result === 'skipped' && expected[job]) unexpectedSkips.push(job);
}
if (failed.length) { console.error('Failing gate: required job(s) failed or were cancelled:', failed); process.exit(1); }
if (unexpectedSkips.length) { console.error('Failing gate: job(s) skipped despite their surface being selected:', unexpectedSkips); process.exit(1); }
console.log('All required jobs succeeded or were intentionally skipped.');
