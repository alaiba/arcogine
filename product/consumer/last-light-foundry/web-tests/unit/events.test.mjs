import assert from 'node:assert/strict';
import { test } from 'node:test';
import { acceptEvents, describeEvent, joinFromSnapshot } from '../../src/main/resources/web/events.js';

const state = { runId: 'run-1', fingerprint: 'fp-1', cursor: 0n };
const event = (sequence, over = {}) => ({ runId: 'run-1', sequence: String(sequence), modelFingerprint: 'fp-1', ...over });

test('contiguous events extend the cursor', () => {
  const result = acceptEvents(state, [event(1), event(2), event(3)]);
  assert.equal(result.accepted.length, 3);
  assert.equal(result.state.cursor, 3n);
  assert.equal(result.gap, false);
});

test('a missing sequence is detected as a gap and nothing after it is accepted', () => {
  const result = acceptEvents(state, [event(1), event(3), event(4)]);
  assert.equal(result.accepted.length, 1);
  assert.equal(result.state.cursor, 1n);
  assert.equal(result.gap, true);
});

test('duplicates and out-of-order replays are rejected', () => {
  const joined = { ...state, cursor: 5n };
  const result = acceptEvents(joined, [event(4), event(5), event(6)]);
  assert.deepEqual(result.accepted.map((item) => item.sequence), ['6']);
  assert.equal(result.rejected.duplicate, 2);
});

test('events from an old run or a different model are never applied', () => {
  const result = acceptEvents(state, [event(1, { runId: 'old' }), event(1, { modelFingerprint: 'other' }), event(1)]);
  assert.equal(result.rejected.oldRun, 1);
  assert.equal(result.wrongModel, true);
  assert.equal(result.accepted.length, 1);
});

test('sequences beyond the JavaScript safe integer range compare exactly', () => {
  const big = { ...state, cursor: 9007199254740992n };
  const result = acceptEvents(big, [event('9007199254740993'), event('9007199254740994')]);
  assert.equal(result.accepted.length, 2);
  assert.equal(result.state.cursor, 9007199254740994n);
});

test('joining from a snapshot starts at its latest sequence', () => {
  assert.deepEqual(joinFromSnapshot({ runId: 'r', modelFingerprint: 'f', latestEventSequence: '41' }),
    { runId: 'r', fingerprint: 'f', cursor: 41n });
});

test('every supported event type has a description', () => {
  const samples = [
    ['ORDER_ACCEPTED', { orderId: '1', quantity: '3', jobIds: ['1', '2', '3'] }, /accepted/],
    ['JOB_DISPATCHED', { jobId: '1', stepIndex: 0, machineId: '2' }, /started step 1/],
    ['JOB_WAITING', { jobId: '1', eligibleMachines: ['2', '3'] }, /2 or 3/],
    ['JOB_STEP_COMPLETED', { jobId: '1', stepIndex: 1, machineId: '2', jobComplete: true }, /job complete/],
    ['ORDER_COMPLETED', { orderId: '1', quantity: '3' }, /shipped/],
    ['MACHINE_AVAILABILITY_CHANGED', { machineId: '2', online: false }, /offline/],
  ];
  for (const [type, payload, pattern] of samples) assert.match(describeEvent({ type, payload }), pattern);
  assert.equal(describeEvent({ type: 'SOMETHING_ELSE', payload: {} }), 'SOMETHING_ELSE');
});
