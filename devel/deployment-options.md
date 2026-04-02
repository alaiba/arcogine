# Arcogine Deployment Analysis

## Project Shape

Arcogine is currently a two-part application:

- A Rust backend (`sim-api` exposed via `sim-cli`) that serves HTTP endpoints and an SSE stream on port `3000`
- A React + Vite frontend in `ui/` that builds to static assets and is served by Nginx on port `5173`

There is no database, queue, cache, or object storage dependency in the current repository. Simulation state, snapshots, and event logs are held in memory inside the API process.

## Deployment-Relevant Observations

### Backend

- The API is stateful at the process level:
  - simulation state lives in `AppState`
  - the simulation runs on a dedicated OS thread
  - snapshots and event logs are stored in memory
- This means a multi-replica deployment is not safe by default:
  - each replica would hold different simulation state
  - client requests and SSE connections must hit the same process
- If the process restarts, loaded scenarios and in-memory event history are lost

### Frontend

- The frontend calls the API with a relative base path: `/api`
- In development, Vite proxies `/api` to `http://localhost:3000`
- In the UI container, Nginx proxies `/api/` to the Docker service name `api:3000`
- The current frontend is therefore easiest to deploy behind the same origin as the API, or behind a reverse proxy that exposes both under one hostname

### Containers

- The root `Dockerfile` produces a single Rust API image
- `ui/Dockerfile` builds static assets and serves them with Nginx
- `compose.yaml` already defines a local two-service deployment
- The API runtime image now includes `curl`, which matches the `compose.yaml` healthcheck contract

## Practical Production Paths

### 1. Single VM, reverse proxy, Docker Compose

Best current fit.

- Run both containers on one VM
- Put Caddy or Nginx in front for TLS and one public hostname
- Keep API and UI on the same machine and same origin
- Lowest operational complexity

Good targets:

- Hetzner Cloud
- DigitalOcean Droplets
- Linode/Akamai
- AWS Lightsail

### 2. Single-container-host PaaS for API plus static hosting for UI

Possible, but only if routing is handled carefully.

- Host the Rust API on Render, Fly.io, Railway, Northflank, or a container app platform
- Host the UI on Cloudflare Pages, Netlify, Vercel, or an object-store + CDN setup
- You would need either:
  - a reverse proxy in front of both services under one domain, or
  - frontend changes to support an absolute API base URL and CORS/SSE validation in production

This is viable, but less aligned with the current same-origin assumptions.

### 3. Managed container platform with both services

Reasonable if you want cloud primitives without full Kubernetes.

- Deploy the API container and UI container to:
  - AWS ECS/Fargate
  - Azure Container Apps
  - Google Cloud Run plus static hosting or containerized UI
- Use an ingress/load balancer to present one hostname
- Still keep the API at one replica unless state is externalized

This is cleaner for teams already operating in a cloud environment, but cost and setup overhead rise quickly relative to the app’s current complexity.

### 4. Kubernetes

Not justified for the current repository unless required by an existing platform standard.

- The app has no distributed-state design yet
- Horizontal scaling is blocked by in-memory simulation state
- Operational overhead is high compared with the current system shape

## Current Constraints That Affect Costing

- No persistence layer today:
  - restarts lose runtime state
  - if production needs saved scenarios, audit history, or multi-user sessions, storage costs must be added
- No auth layer is visible in the current API:
  - production exposure may require auth, rate limiting, and TLS termination
- Open CORS policy:
  - acceptable for local/dev, but should be tightened for production
- SSE is part of the product behavior:
  - hosting must support long-lived HTTP connections cleanly

## Recommended Baseline

For the codebase as it exists now, the most realistic first production deployment is:

- one small VM
- Docker Compose for API + UI
- Caddy or Nginx for TLS and routing
- one public domain
- basic uptime monitoring, log shipping, and backups only if persistent data is later introduced

This keeps cost and complexity low while matching the application’s current single-instance architecture.

## Research Prompt

Use the prompt below with a model that can inspect the repository and browse current vendor pricing.

```text
Analyze this repository and identify realistic paths to deploy it to production.

Context:
- The project is Arcogine, a Rust backend plus React/Vite frontend.
- Backend: Axum/Tokio HTTP API with SSE, built from the workspace root.
- Frontend: static build served by Nginx from `ui/`.
- Current local deployment uses Docker Compose with two services: `api` and `ui`.
- The backend currently keeps simulation state in memory inside the API process.
- The frontend currently expects same-origin `/api` routing rather than a separately configured API base URL.
- There is no database, queue, or object storage dependency in the current repo unless you discover one in the code.

Your tasks:
1. Inspect the repository structure and determine the application topology, runtime dependencies, and deployment constraints.
2. Identify all realistic production deployment options for the code as it exists today. Include at least:
   - single VM with Docker Compose
   - managed container hosting
   - split frontend/static hosting plus backend hosting
   - cloud-native container platforms
   - explicitly state whether Kubernetes is justified or not
3. For each option, estimate the monthly cost for:
   - hobby / low-traffic usage
   - small team internal tool usage
   - moderate production usage
4. Include related expenditures, not just base hosting. Price and describe:
   - compute
   - bandwidth / egress
   - managed TLS / certificates if applicable
   - domain registration
   - container registry if needed
   - logging / monitoring
   - backups / persistence if needed
   - CI/CD runner costs if material
5. Call out architecture constraints that affect deployment economics, especially:
   - in-memory simulation state
   - SSE requirements
   - same-origin frontend/API assumption
   - lack of persistence
   - inability to horizontally scale safely without redesign
6. Recommend the best near-term production approach and the best future-scalable approach, and explain why.
7. Provide concrete next steps to move from the current repo to the recommended production setup.

Research requirements:
- Browse the web for current pricing and product limits because costs change frequently.
- Use exact dates in the report, for example “Pricing checked on April 2, 2026”.
- Cite every provider/pricing source with direct links.
- Prefer official vendor pricing pages and official product documentation.
- Distinguish clearly between facts observed in the repository and your own inference.
- If pricing is usage-based or region-dependent, show the assumptions explicitly.

Output format:
- Start with a short repository deployment summary.
- Then provide a comparison table with columns:
  - Option
  - Fit for current architecture
  - Monthly cost range
  - Operational complexity
  - Key risks / blockers
  - Notes
- Then provide a detailed section for each option.
- Then provide a final recommendation with a phased plan:
  - Phase 1: deploy current architecture with minimal changes
  - Phase 2: changes needed for better reliability
  - Phase 3: changes needed for horizontal scalability

Be strict about matching recommendations to the code that actually exists, not an idealized future architecture.
```
