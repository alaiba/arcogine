# Arcogine UI

The `product/interfaces/web/` package is the web-based experiment console for interacting with the Arcogine simulation. It is a simulation/experiment-oriented interface — one current mode of engaging with Arcogine (see [`docs/product/charter.md`](../../../docs/product/charter.md#5-modes-of-engagement-not-personas)), not "the Arcogine UX" in the mature-product sense.

## What this UI does

- Loads built-in scenarios and scenario files from `docs/examples/`.
- Sends simulation commands to the Java/Spring Boot API using relative `/api` routes.
- Streams events through SSE and displays KPIs, queues, jobs, and topology.
- Provides controls for price, machine availability, agent toggles, and baseline comparison.

## API communication model

- **Native development**: Vite proxies `/api` to `http://localhost:3000`.
- **Container development**: Nginx proxies `/api` to the `api` service.
- There is no active `VITE_API_URL` API client override in the shipped UI flow.

## Local development

```bash
cd product/interfaces/web
npm ci
npm run dev
```

### Helpful backend command

In another terminal, run the API:

```bash
./arcogine run api
```

## Validation commands

Run from `product/interfaces/web/`:

```bash
npm run lint              # ESLint
npx tsc --noEmit          # typecheck
npm run build             # production build
npm test                  # unit tests (Vitest)
npm run test:coverage     # unit tests with coverage
```

Lint, typecheck, tests, and build also run as part of `./arcogine check` from the repository root.

### E2E smoke tests

```bash
npx playwright test       # from product/interfaces/web/
```

Also runs as part of `./arcogine check --full` from the repository root.

## Source map

- `src/api/` — REST client and SSE client.
- `src/stores/` — simulation and baseline state stores.
- `src/components/` — layout, dashboard, experiment, onboarding, and shared components.
- `src/App.tsx` — main shell and composition used at runtime.
