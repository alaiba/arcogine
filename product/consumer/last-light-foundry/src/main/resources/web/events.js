// Pure client-side event join logic. The server is the only source of simulation facts; this module
// only decides whether a batch of supported events extends what the client already holds.
//
// Contract (see docs on GameServer): every event names its run, sequence and model fingerprint.
// Sequences are strictly +1 per run. A client joins from a snapshot's `latestEventSequence`, and a
// batch is accepted only if it continues the cursor exactly.

/**
 * @param {{runId:string, fingerprint:string, cursor:bigint}} state
 * @param {Array<{runId:string, sequence:string, modelFingerprint:string}>} batch
 * @returns {{accepted:object[], state:object, rejected:{oldRun:number, duplicate:number}, gap:boolean, wrongModel:boolean}}
 */
export function acceptEvents(state, batch) {
  const accepted = [];
  const rejected = { oldRun: 0, duplicate: 0 };
  let gap = false;
  let wrongModel = false;
  let cursor = state.cursor;
  for (const event of batch) {
    if (event.runId !== state.runId) {
      rejected.oldRun += 1;
      continue;
    }
    if (event.modelFingerprint !== state.fingerprint) {
      wrongModel = true;
      continue;
    }
    const sequence = BigInt(event.sequence);
    if (sequence <= cursor) {
      rejected.duplicate += 1;
      continue;
    }
    if (sequence !== cursor + 1n) {
      gap = true;
      break;
    }
    accepted.push(event);
    cursor = sequence;
  }
  return { accepted, state: { ...state, cursor }, rejected, gap, wrongModel };
}

/** A fresh join state taken from an atomic run snapshot. */
export function joinFromSnapshot(run) {
  return { runId: run.runId, fingerprint: run.modelFingerprint, cursor: BigInt(run.latestEventSequence) };
}

/** Human description of one supported event. */
export function describeEvent(event) {
  const p = event.payload;
  switch (event.type) {
    case 'ORDER_ACCEPTED':
      return `Order ${p.orderId} accepted: ${p.quantity} units, ${p.jobIds.length} jobs created by the engine`;
    case 'JOB_DISPATCHED':
      return `Job ${p.jobId} started step ${p.stepIndex + 1} on machine ${p.machineId}`;
    case 'JOB_WAITING':
      return `Job ${p.jobId} is waiting for machine ${p.eligibleMachines.join(' or ')}`;
    case 'JOB_STEP_COMPLETED':
      return `Job ${p.jobId} finished step ${p.stepIndex + 1} on machine ${p.machineId}${p.jobComplete ? ' (job complete)' : ''}`;
    case 'ORDER_COMPLETED':
      return `Order ${p.orderId} shipped: ${p.quantity} units`;
    case 'MACHINE_AVAILABILITY_CHANGED':
      return `Machine ${p.machineId} went ${p.online ? 'online' : 'offline'}`;
    default:
      return event.type;
  }
}
