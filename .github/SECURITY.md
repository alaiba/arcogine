# Security Policy

## Scope

Arcogine is a simulation engine intended for local development and experimentation. The MVP does not include production-grade authentication, authorization, or data encryption.

This document covers how to report a vulnerability, what the current software actually does and does not protect, and what must be true before Arcogine is exposed more widely. It describes today's software honestly; where a requirement belongs to another authority, it points there rather than restating it.

Two escalations are deliberately kept apart, because they are different boundaries with different owners:

- **Hosted or multi-user exposure** — more than one principal reaching the same instance, with no actuation. Product/interface ownership; see [Before hosted or multi-user exposure](#before-hosted-or-multi-user-exposure).
- **Production-consequential operation** — actions with effects outside Arcogine. Owned by the [Operational Execution and Digital Twin Architecture](../docs/architecture/operational-execution-digital-twin.md) and its [readiness plan](../docs/planning/operational-execution-digital-twin-readiness.md), which are proposals and requirements, not claims about current product security. See [Mature-product security principles](#mature-product-security-principles).

Adding TLS, CORS restrictions, or network hardening to the current software does not, by itself, satisfy either.

## Reporting a Vulnerability

**Report a suspected vulnerability privately, through GitHub's private vulnerability reporting**, from this repository's *Security* tab → *Report a vulnerability*. That channel is confidential between you and the maintainers.

Do **not** open a public issue for a suspected vulnerability. This repository is public, and a public issue discloses the problem to everyone before there is a fix. Public issues remain the right place for ordinary bugs and for hardening suggestions that are not themselves exploitable.

What to expect — this is a small project, and these are the responsibilities the maintainers accept, not a staffed service level:

1. **Acknowledgement** that the report was received and is being looked at.
2. **Triage** — a maintainer decides whether it is a vulnerability in Arcogine, at what exposure it applies (many issues only matter beyond the local single-user default described below), and how severe it is. A report that turns out not to be a vulnerability is closed with the reasoning.
3. **Remediation** — an accepted vulnerability is fixed, or an explicit decision is recorded that it will not be fixed and why.
4. **Verification** — the fix lands with a regression check where the defect is testable, so the same failure cannot return unnoticed.
5. **Closure** — the reporter is told the outcome, and a GitHub security advisory is published when the issue affected anyone using the software.

Dependency vulnerabilities follow a separate, already-owned path: see [Security scan ownership](#security-scan-ownership) and `.github/agents/dependency-maintainer.agent.md`.

## Supported Versions

| Version | Supported |
|---------|-----------|
| 0.x (MVP) | Yes |

## Security Posture

Arcogine is local-first by default. Understanding the current posture means separating three different things: controls that exist and are verified, limits that hardening cannot remove, and guidance for deploying anyway.

### Implemented controls

These exist in the current software today. Where executable verification exists, the maintained criteria and the tests that verify them are in [`docs/development/testing.md`](../docs/development/testing.md#security-verification-tests). Rows explicitly marked as unverified describe configuration, not observed enforcement.

| Control | Behavior |
|---|---|
| Default bind address | Native CLI/API binds `127.0.0.1:3000`, so exposure beyond localhost is an explicit choice. Container images keep bind behavior explicit for container networking. |
| Request body limit | Requests to `/api/*` over 1 MiB are rejected with `413`, including requests sent with chunked transfer encoding and no `Content-Length`. |
| SSE connection limit | Concurrent `/api/events/stream` connections are capped at 64; further connections get `503` rather than exhausting server resources. |
| Scenario input validation | Referential and range validation rejects invalid scenarios with `400` rather than partially applying them. |
| Economy value bounds | Out-of-range price/economy input is rejected rather than applied to simulation state. |
| Web image response headers | The nginx image configures `Content-Security-Policy`, `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`, and `Permissions-Policy`. Configured, but with no executable response-header check — treat these as deployment settings rather than verified controls. |
| CORS | Restricted when `CORS_ALLOWED_ORIGIN` is set; permissive (`*`) when unset. Configured, but with no executable check — treat it as a deployment setting rather than a verified control. |

### Structural limits that hardening does not remove

These are properties of the current architecture. No reverse proxy, TLS terminator, or firewall changes any of them, and they are the reason the current software is not safe to expose to untrusted or multiple principals:

- **No user or principal concept.** The REST API does not require authentication, and there is nothing to authenticate *as*. Authorization, per-user data separation, and audit attribution therefore do not exist and cannot be configured on.
- **One shared simulation.** Simulation state is a single process-wide `SimThread` singleton. Every client shares one simulation: any caller can load a scenario, run, pause, reset, or change prices under any other caller. This makes multi-user operation structurally impossible today rather than merely unauthenticated.
- **No scenario resource or cost bounds.** Beyond the request body cap, a scenario is not bounded by the compute or memory it will consume, so an accepted scenario can consume as much as it asks for.
- **No encryption of scenario files or simulation state at rest.**

These limits are acceptable for a local-only, single-user experimentation tool. They are **not** sufficient for hosted, multi-user, or production-consequential operation.

## Hardening for Network Deployment

If you expose the current simulation service beyond localhost, apply at least:

1. **Bind address** — Use `--addr 127.0.0.1:3000` for native/local runs. For containerized networked runs, configure host binding intentionally and avoid broad accidental exposure.

2. **CORS** — Set `CORS_ALLOWED_ORIGIN=http://your-ui-host:port` to restrict cross-origin access. When unset, CORS is permissive (`*`).

3. **TLS** — Arcogine does not terminate TLS. Place it behind a reverse proxy (nginx, Caddy, or a cloud load balancer) with TLS termination.

4. **Dependency auditing** — Before deployment, run the Java dependency scan (`cd product && ./gradlew cyclonedxBom && trivy sbom ... product/build/reports/cyclonedx/bom.json`) and the frontend audit (`cd product/interfaces/web && npm audit --audit-level=high`). CI runs npm audit as part of the frontend job and scans built container images via Trivy in the docker job. Run `./arcogine check --full` locally for the complete security suite including the Java dependency scan.

5. **Log verbosity** — Set `LOGGING_LEVEL_ROOT=WARN` in production-like environments to reduce log noise.

These controls harden network exposure of the current simulation service. They limit *who can reach it* and *how traffic is carried*. They do not add a user model, isolate one caller's simulation from another's, or bound what a scenario costs to run — see the structural limits above — and they do not satisfy the additional trust, authorization, command-integrity, observation-authenticity, or fail-safe requirements for real-world actuation.

Concretely: applying all five controls above makes it reasonable to expose the service to a trusted operator on a trusted network. It does **not** make it a safe multi-user service, and it does not make it a production-operational one.

## Before hosted or multi-user exposure

A hosted or multi-user Arcogine is a different product boundary, even when it performs no physical actuation. The absence of the controls below is not a current defect — it matches the current local, single-user scope — but they stop being optional the moment a second principal can reach the same instance.

Before any hosted or multi-user consumer is treated as safe, these must be explicit, recorded readiness criteria with executable verification, not prose:

- **Authentication** — who or what is making a request.
- **Authorization enforcement** — what that identity may do, enforced at the API boundary rather than hidden in the UI.
- **Per-principal isolation** — one caller's scenario, simulation state, and results separated from another's. This is the criterion the current singleton architecture fails structurally.
- **Resource and cost isolation** — bounds on what one principal can consume.
- **Audit attribution** — a durable record of who did what.

Ownership when that trigger fires: this is **product/interface ownership**, not Operational's. Operational Execution owns trust and authority semantics for *consequential* operations — actions with effects outside Arcogine — and a hosted, non-actuating deployment is not that. Generic actor and capability semantics also do not become Operational's by default; the research register's open actor-identity question may become relevant to the identity *referent* once a concrete consumer exists, but authorization enforcement and per-principal isolation belong with the interface that exposes them.

This section is a trigger, not a plan. It exists so the question is asked before exposure rather than after.

## Security scan ownership

Security execution follows the quality-gate contract:

- Scan commands invoke each scanner's native tool directly (`trivy sbom`, `trivy image`,
  `npm audit`, `gitleaks detect`) — locally via `./arcogine check --full`, in CI via the
  jobs in `.github/workflows/ci.yml` — so all checks are discoverable from the same
  command surface documented in `docs/development/testing.md`.
- CI remains responsible for installing scanner binaries/tools and enforcing policy
  controls (`--exit-code`, report handling, fail-fast behavior) around those commands.

**Responding to findings.** A finding inside an open pull request is owned by that PR's author: it blocks the PR and is fixed, or explicitly accepted, before merge. A finding from the **daily scheduled scan** has no PR and therefore no author, so it is the maintainer's to triage on the next working day — decide whether it is real, then fix it, add a reasoned `.trivyignore` entry, or record why no action is needed. A scheduled-scan failure is not resolved by a later green run: an unrelated merge can change the scanned artifact and clear the red without anyone having looked at the finding, which is exactly what happened to the 2026-09-09 failure. Close the finding deliberately, not by waiting for the signal to disappear.

**Dependabot posture.** Dependabot **alerts** are enabled, and are the surface that reports known vulnerabilities in resolved dependencies. They are deliberately kept alongside the CI scanners rather than replaced by them: the Trivy SBOM and image scans run with `--ignore-unfixed`, so a CRITICAL vulnerability with no available fix is invisible to the CI gate by design, and alerts are what still surface it. Dependabot **version updates** (weekly Gradle/npm/Actions, grouped) are configured separately in `.github/dependabot.yml`. Remediation for anything these surface is owned by the dependency maintainer (`.github/agents/dependency-maintainer.agent.md`) either way.

For the full security verification test list, see `docs/development/testing.md`.

## Mature-product security principles

The [Product Charter](../docs/product/charter.md) describes a mature Arcogine that can eventually operate real production systems, not just simulate them. Real execution — actions whose effects land outside Arcogine, where money moves, machines run, or orders ship — introduces a body of trust and safety semantics that this policy does not own and does not restate.

Those semantics are owned by the [Operational Execution and Digital Twin Architecture](../docs/architecture/operational-execution-digital-twin.md): actor, authority, trust and capability in §5; external commands as claims rather than facts in §6; observation authenticity and provenance in §8; the integration adapter boundary in §14; and safety and failure principles, including fail-safe behavior and environment separation, in §16. Durable operational identity is settled by the Operational continuity contract, and the semantic authority of external formats by the external representation policy. Read those for the requirements; they are the authority, and duplicating them here would only let the two drift apart.

Two boundaries matter for reading this policy:

- Those documents are **proposals and requirements, not descriptions of implemented behavior**. Nothing in them is implemented by the current MVP merely because it is planned. Turning the relevant principles into acceptance criteria before a live adapter can be considered production-ready is the operational readiness track's responsibility.
- They cover **consequential operation**. They are not the owner of ordinary application and interface security for a non-actuating deployment — that is the product/interface boundary described under [Before hosted or multi-user exposure](#before-hosted-or-multi-user-exposure).

This policy intentionally chooses no mechanism. Whether the eventual answer uses OAuth/OIDC, certificates, a secrets manager, or hardware roots of trust is out of scope here; the requirement is that before Arcogine treats a path as production-consequential, its identity and trust basis, integrity expectations, authority, least privilege, credential lifecycle, failure behavior, and threat assumptions are explicit and testable.
