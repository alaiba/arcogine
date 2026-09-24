import test from 'node:test';
import assert from 'node:assert/strict';
import { verify } from './check-dependabot-provenance.mjs';

const HEAD = 'a'.repeat(40);
const actor = { login: 'dependabot[bot]', type: 'Bot', id: 49699333 };
const run = (overrides = {}) => Buffer.from(JSON.stringify({ name: 'CI', event: 'pull_request', head_sha: HEAD, actor, pull_requests: [{ number: 303 }], ...overrides })).toString('base64');
const input = (overrides = {}) => ({ number: '303', head: HEAD, authorLogin: actor.login, authorType: actor.type, authorId: actor.id, runBase64: run(), ...overrides });

test('accepts exact Dependabot opener and exact-head CI actor for this PR', () => assert.equal(verify(input()).ok, true));
test('rejects a human or lookalike PR opener', () => {
  assert.equal(verify(input({ authorLogin: 'alaiba', authorType: 'User', authorId: 17069361 })).ok, false);
  assert.equal(verify(input({ authorId: 999 })).ok, false);
});
test('rejects human, lookalike, stale, manual, or non-CI workflow runs', () => {
  for (const value of [run({ actor: { login: 'alaiba', type: 'User', id: 17069361 } }), run({ actor: { ...actor, id: 999 } }), run({ head_sha: 'b'.repeat(40) }), run({ event: 'workflow_dispatch' }), run({ name: 'Other Workflow' })]) assert.equal(verify(input({ runBase64: value })).ok, false);
});
test('rejects runs associated with another pull request or malformed data', () => {
  assert.equal(verify(input({ runBase64: run({ pull_requests: [{ number: 999 }] }) })).ok, false);
  assert.equal(verify(input({ runBase64: '***not base64 json***' })).ok, false);
  assert.equal(verify(input({ runBase64: '' })).ok, false);
});
