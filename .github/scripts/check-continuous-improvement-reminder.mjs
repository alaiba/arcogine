#!/usr/bin/env node
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

export function validateReminderWorkflow(text) {
  const required = [
    ['scheduled trigger is present', '  schedule:'],
    ['fixed reminder concurrency group is present', '  group: continuous-improvement-reminder'],
    ['running reminder is never canceled', '  cancel-in-progress: false'],
    ['issue permission stays narrow', '  issues: write'],
    ['checkpoint identity stays exact', 'TITLE: Continuous improvement checkpoint'],
    ['workflow still creates the reminder issue', 'gh issue create'],
  ];
  const results = required.map(([name, literal]) => [name, text.includes(literal)]);
  results.push(['branch-selectable workflow_dispatch is absent', !text.includes('workflow_dispatch')]);
  return results;
}
function main() {
  const scriptDir = path.dirname(fileURLToPath(import.meta.url));
  const workflow = path.resolve(scriptDir, '../workflows/continuous-improvement-reminder.yml');
  let results;
  try { results = validateReminderWorkflow(fs.readFileSync(workflow, 'utf8')); }
  catch (error) { console.error(`Continuous-improvement reminder contract check failed: ${error.message}`); return 1; }
  for (const [name, pass] of results) console.log(`${pass ? 'PASS' : 'FAIL'}: ${name}`);
  const failures = results.filter(([, pass]) => !pass).length;
  if (failures) { console.error(`${failures} continuous-improvement reminder contract check(s) failed.`); return 1; }
  console.log('Continuous-improvement reminder contract checks passed.');
  return 0;
}
if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) process.exitCode = main();
