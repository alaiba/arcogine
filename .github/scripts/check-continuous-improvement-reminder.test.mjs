import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { validateReminderWorkflow } from './check-continuous-improvement-reminder.mjs';

const here = path.dirname(fileURLToPath(import.meta.url));
const workflow = fs.readFileSync(path.resolve(here, '../workflows/continuous-improvement-reminder.yml'), 'utf8');

test('current reminder workflow preserves schedule, concurrency, narrow permission, and deduplication contract', () => {
  assert.deepEqual(validateReminderWorkflow(workflow).filter(([, ok]) => !ok), []);
});
test('contract check rejects manual dispatch and weakened authority/lifecycle invariants', () => {
  const altered = workflow.replace('  issues: write', '  contents: write').replace('  cancel-in-progress: false', '  cancel-in-progress: true').replace('gh issue create', 'gh issue edit').concat('\nworkflow_dispatch:\n');
  assert.ok(validateReminderWorkflow(altered).some(([, ok]) => !ok));
});
