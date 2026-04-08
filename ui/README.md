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

Run from the **repository root** (where the `Makefile` lives), not from `ui/`:

```bash
make frontend-lint        # ESLint
make frontend-typecheck   # tsc --noEmit
make frontend-build       # production build
make frontend-test        # unit tests (Vitest)
make frontend-coverage    # unit tests with coverage
```

### E2E smoke tests

```bash
make playwright           # runs npx playwright test in ui/
```

## Source map

- `src/api/` — REST client and SSE client.
- `src/stores/` — simulation and baseline state stores.
- `src/components/` — layout, dashboard, experiment, onboarding, and shared components.
- `src/App.tsx` — main shell and composition used at runtime.
