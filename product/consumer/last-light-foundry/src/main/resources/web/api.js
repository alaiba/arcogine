// Thin fetch wrapper for the Java server. Every failure becomes an `ApiError` with a stable `kind`:
//   'network'  the server could not be reached (disconnected)
//   'refused'  the server answered with a structured error (invalid / rejected request)
// The client never computes simulation outcomes; it only asks and renders.

export class ApiError extends Error {
  constructor(kind, status, code, message, body) {
    super(message);
    this.kind = kind;
    this.status = status;
    this.code = code;
    this.body = body;
  }
}

async function call(method, path, body) {
  let response;
  try {
    response = await fetch(path, {
      method,
      headers: body === undefined ? {} : { 'Content-Type': 'application/json' },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch (cause) {
    throw new ApiError('network', 0, 'network', 'The Last Light Foundry server cannot be reached.', null);
  }
  let payload = null;
  try {
    payload = await response.json();
  } catch (cause) {
    payload = null;
  }
  if (!response.ok) {
    const error = payload?.error ?? {};
    throw new ApiError('refused', response.status, error.code ?? 'http.error',
      error.message ?? `The server answered ${response.status}.`, payload);
  }
  return payload;
}

export const api = {
  missions: () => call('GET', '/api/missions'),
  mission: (id) => call('GET', `/api/missions/${id}`),
  check: (id, draft) => call('POST', `/api/missions/${id}/check`, draft),
  start: (id, draft) => call('POST', `/api/missions/${id}/runs`, draft),
  run: (runId) => call('GET', `/api/runs/${runId}`),
  step: (runId, count) => call('POST', `/api/runs/${runId}/step`, { count }),
  advance: (runId, maxEvents) => call('POST', `/api/runs/${runId}/advance`, { maxEvents }),
  reset: (runId) => call('POST', `/api/runs/${runId}/reset`, {}),
  events: (runId, after, limit = 200) => call('GET', `/api/runs/${runId}/events?after=${after}&limit=${limit}`),
};
