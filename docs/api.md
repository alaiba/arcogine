# Arcogine — API Reference

The Arcogine API is an HTTP + SSE interface served by the `arcogine serve` command (default `127.0.0.1:3000`). The UI communicates exclusively through this API; you can also use it directly with curl or any HTTP client.

All JSON request bodies are limited to 1 MiB. Error responses use the shape `{ "error": "description" }`.

## Health

### `GET /api/health`

Liveness check.

```bash
curl http://localhost:3000/api/health
```

```json
{ "status": "ok" }
```

## Scenario management

### `POST /api/scenario`

Load a TOML scenario into the simulation. This resets any running simulation.

```bash
curl -X POST http://localhost:3000/api/scenario \
  -H 'Content-Type: application/json' \
  -d '{ "toml": "[simulation]\nrng_seed = 42\nmax_ticks = 100\n..." }'
```

**Request body:** `{ "toml": string }` — the full scenario TOML as a string.

**Success (200):** `{ "success": true, "message": "Scenario loaded" }`

**Errors:** `400` if TOML is malformed or the scenario fails validation. `500` if the simulation thread is unreachable.

## Simulation control

All control endpoints return a full `SimSnapshot` on success (see [Snapshot format](#snapshot-format) below).

### `POST /api/sim/run`

Start or resume the simulation. Advances continuously until paused, completed, or `max_ticks` is reached.

```bash
curl -X POST http://localhost:3000/api/sim/run
```

**Errors:** `409` if no scenario is loaded or the simulation is already completed (reset first).

### `POST /api/sim/pause`

Pause a running simulation.

```bash
curl -X POST http://localhost:3000/api/sim/pause
```

### `POST /api/sim/step`

Advance by exactly one event.

```bash
curl -X POST http://localhost:3000/api/sim/step
```

**Errors:** `409` if no scenario is loaded or the simulation is completed.

### `POST /api/sim/reset`

Reset the simulation to tick 0 using the currently loaded scenario.

```bash
curl -X POST http://localhost:3000/api/sim/reset
```

**Errors:** `409` if no scenario is loaded.

## Intervention commands

### `POST /api/price`

Change the product price. Injects a price-change event at the current simulation time.

```bash
curl -X POST http://localhost:3000/api/price \
  -H 'Content-Type: application/json' \
  -d '{ "price": 8.0 }'
```

**Request body:** `{ "price": number }` — must be between 0 and 1,000,000.

**Errors:** `400` if price is out of range. `409` if no scenario is loaded.

### `POST /api/machines`

Toggle a machine online or offline.

```bash
curl -X POST http://localhost:3000/api/machines \
  -H 'Content-Type: application/json' \
  -d '{ "machine_id": 1, "online": false }'
```

**Request body:** `{ "machine_id": number, "online": boolean }`

**Errors:** `409` if no scenario is loaded.

### `POST /api/agent`

Enable or disable the automated Sales Agent.

```bash
curl -X POST http://localhost:3000/api/agent \
  -H 'Content-Type: application/json' \
  -d '{ "enabled": true }'
```

**Request body:** `{ "enabled": boolean }`

## Queries

### `GET /api/snapshot`

Full simulation state snapshot.

```bash
curl http://localhost:3000/api/snapshot
```

Returns a `SimSnapshot` (see [Snapshot format](#snapshot-format)).

### `GET /api/kpis`

Current KPI values.

```bash
curl http://localhost:3000/api/kpis
```

```json
[
  { "name": "throughput", "value": 2.4, "unit": "jobs/tick" },
  { "name": "utilization", "value": 0.85, "unit": "ratio" },
  { "name": "lead_time", "value": 12.3, "unit": "ticks" },
  { "name": "backlog", "value": 7.0, "unit": "jobs" }
]
```

### `GET /api/factory/topology`

Machine and routing information for the loaded scenario.

```bash
curl http://localhost:3000/api/factory/topology
```

```json
{
  "machines": [
    { "id": 1, "name": "Mill", "online": true, "busy": false }
  ],
  "edges": [
    { "from_machine": 1, "to_machine": 2, "product_id": 1 }
  ]
}
```

### `GET /api/jobs`

All jobs in the current simulation.

```bash
curl http://localhost:3000/api/jobs
```

Returns a JSON array of job objects with `job_id`, `product_id`, `status`, `current_step`, `total_steps`, `created_at`, `completed_at`, and `revenue` fields.

### `GET /api/export/events`

Full event log from the simulation.

```bash
curl http://localhost:3000/api/export/events
```

Returns `{ "events": [...] }` — the append-only event log. The log is capped at a fixed capacity; check the response for truncation.

## Server-Sent Events (SSE)

### `GET /api/events/stream`

Real-time event stream. Each SSE message has an `event:` line matching the event type (e.g., `OrderCreation`, `TaskStart`, `TaskEnd`, `PriceChange`, `AgentDecision`) and a `data:` line with the JSON event payload.

```bash
curl -N http://localhost:3000/api/events/stream
```

The server enforces a maximum of 64 concurrent SSE connections. Excess connections receive `503`.

## Snapshot format

The `SimSnapshot` returned by control and query endpoints:

```json
{
  "run_state": "Running",
  "current_time": 142,
  "events_processed": 387,
  "kpis": [ { "name": "throughput", "value": 2.4, "unit": "jobs/tick" } ],
  "topology": { "machines": [...], "edges": [...] },
  "jobs": [...],
  "total_revenue": 1250.0,
  "completed_sales": 48,
  "backlog": 7,
  "current_price": 5.0,
  "agent_enabled": false,
  "scenario_loaded": true,
  "last_error": null
}
```

`run_state` is one of: `"Idle"`, `"Running"`, `"Paused"`, `"Completed"`.

## CORS

CORS is permissive by default (allows all origins). Set the `CORS_ALLOWED_ORIGIN` environment variable to restrict it:

```bash
CORS_ALLOWED_ORIGIN=http://localhost:5173 cargo run --bin arcogine -- serve
```
