# Arcogine UI

The `ui/` package is the web-based experiment console for interacting with the Arcogine simulation.

## What this UI does

- Loads built-in scenarios and scenario files from `examples/`.
- Sends simulation commands to the Rust API using relative `/api` routes.
- Streams events through SSE and displays KPIs, queues, jobs, and topology.
- Provides controls for price, machine availability, agent toggles, and baseline comparison.

## API communication model

- **Native development**: Vite proxies `/api` to `http://localhost:3000`.
- **Container development**: Nginx proxies `/api` to the `api` service.
- There is no active `VITE_API_URL` API client override in the shipped UI flow.

## Local development

```bash
cd ui
npm ci
npm run dev
```

### Helpful backend command

In another terminal, run the API:

```bash
cargo run --bin arcogine -- serve --addr 127.0.0.1:3000
```

## Validation commands

```bash
cd ui
npm ci
npx tsc --noEmit
npm run build
```

### E2E smoke tests

```bash
cd ui
npx playwright test
```

## Source map

- `src/api/` — REST client and SSE client.
- `src/stores/` — simulation and baseline state stores.
- `src/components/` — layout, dashboard, experiment, onboarding, and shared components.
- `src/App.tsx` — main shell and composition used at runtime.
