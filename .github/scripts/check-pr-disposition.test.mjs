import test from 'node:test';
import assert from 'node:assert/strict';
import { evaluate } from './check-pr-disposition.mjs';

const HEAD = 'abc123def456';
const OLD = 'fed654cba321';
const body = (head, value, backticks = false) => `Reviewed head: ${backticks ? `\`${head}\`` : head}\nDisposition: **${value}**`;
const run = (bodies = [], trusted = 'false', head = HEAD) => evaluate({ head, trusted, bodiesBase64: bodies.map((item) => Buffer.from(item).toString('base64')).join('\n') });

test('requires a current-head READY disposition for ordinary pull requests', () => {
  assert.equal(run().ok, false);
  assert.equal(run([body(HEAD, 'READY TO MERGE')]).ok, true);
  assert.equal(run([body(OLD, 'READY TO MERGE')]).ok, false);
});
test('latest current-head disposition wins and old-head findings do not apply', () => {
  assert.equal(run([body(HEAD, 'CHANGES REQUIRED'), body(HEAD, 'READY TO MERGE')]).ok, true);
  assert.equal(run([body(HEAD, 'READY TO MERGE'), body(HEAD, 'CHANGES REQUIRED')]).ok, false);
  assert.equal(run([body(OLD, 'CHANGES REQUIRED'), body(HEAD, 'READY TO MERGE')]).ok, true);
});
test('rejects unsupported, quoted, trailing, malformed, and noncanonical dispositions', () => {
  for (const text of ['Disposition: **READY TO MERGE**', 'I think READY TO MERGE.', `${body(HEAD, 'READY TO MERGE')}\nMore text`, `Example ${body(HEAD, 'READY TO MERGE')}`, body(HEAD, 'APPROVED')]) assert.equal(run([text]).ok, false, text);
  assert.equal(run([body(HEAD, 'READY AFTER CI')]).ok, false);
  assert.equal(run([body(HEAD, 'NON-BLOCKING FOLLOW-UPS ONLY')]).ok, false);
});
test('accepts whitespace and cosmetic head backticks without relaxing exact head matching', () => {
  assert.equal(run([`Review\nReviewed head:   ${HEAD}\nDisposition:   **READY TO MERGE**   `]).ok, true);
  assert.equal(run([body(HEAD, 'READY TO MERGE', true)]).ok, true);
  assert.equal(run([body(OLD, 'READY TO MERGE', true)]).ok, false);
});
test('trusted provenance bypasses positive review but current-head blockers revoke it', () => {
  assert.equal(run([], 'true').ok, true);
  assert.equal(run([body(HEAD, 'CHANGES REQUIRED')], 'true').ok, false);
  assert.equal(run([body(HEAD, 'CHANGES REQUIRED', true)], 'true').ok, false);
  assert.equal(run([body(OLD, 'CHANGES REQUIRED')], 'true').ok, true);
  assert.equal(run([], 'false').ok, false);
});
test('canonical blocks require exact line placement and adjacency', () => {
  for (const text of [
    `Example:\n    ${body(HEAD, 'READY TO MERGE').replaceAll('\n', '\n    ')}`,
    `Reviewed head: ${HEAD}\n\nDisposition: **READY TO MERGE**`,
    `Reviewed head:t${HEAD}\nDisposition: **READY TO MERGE**`,
    `Reviewed head: ${HEAD}\nDisposition:t**READY TO MERGE**`,
  ]) assert.equal(run([text]).ok, false, text);
});
test('later unrelated reviews do not erase the last applicable current-head decision', () => {
  assert.equal(run([body(HEAD, 'READY TO MERGE'), 'unrelated later comment with no disposition']).ok, true);
  assert.equal(run(['first unrelated comment', body(OLD, 'CHANGES REQUIRED'), body(HEAD, 'READY TO MERGE'), 'later unrelated comment']).ok, true);
});
test('requires the current head input', () => assert.equal(evaluate({ head: '', trusted: 'false' }).message, 'error: PR_HEAD_SHA not set'));
