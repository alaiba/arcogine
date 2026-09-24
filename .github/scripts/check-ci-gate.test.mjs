import test from 'node:test';
import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const script = path.resolve(path.dirname(fileURLToPath(import.meta.url)), 'check-ci-gate.mjs');
function run(needs, backend = 'false', event = 'pull_request') {
  return spawnSync(process.execPath, [script], { encoding: 'utf8', env: { ...process.env, NEEDS_JSON: JSON.stringify(needs), BACKEND: backend, EVENT_NAME: event } });
}
const passing = { classify: { result: 'success' }, java: { result: 'skipped' }, 'java-audit': { result: 'skipped' }, 'security-secrets': { result: 'success' } };

test('allows successful jobs and intentional skips', () => assert.equal(run(passing).status, 0));
test('fails failed/cancelled jobs and selected jobs that skip', () => {
  assert.equal(run({ ...passing, classify: { result: 'failure' } }).status, 1);
  assert.equal(run({ ...passing, 'security-secrets': { result: 'cancelled' } }).status, 1);
  assert.equal(run({ ...passing, java: { result: 'skipped' } }, 'true').status, 1);
});
test('full sweeps expect conditional jobs to run', () => assert.equal(run(passing, 'false', 'schedule').status, 1));
test('fails closed when needs and expected job sets differ', () => assert.equal(run({ ...passing, extra: { result: 'success' } }).status, 1));
