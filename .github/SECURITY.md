# Security Policy

## Scope

Arcogine is a simulation engine intended for local development and experimentation. The MVP does not include production-grade authentication, authorization, or data encryption.

This document has two parts: the current security posture and limitations (below), which describe today's software honestly, and the [mature-product security principles](#mature-product-security-principles) implied by the [Product Charter](../docs/product/charter.md), which are conceptual expectations for a future that can execute real operations — not a description of anything implemented today. Adding TLS, CORS restrictions, or network hardening to the current software does not, by itself, make it suitable for operating a production business; that requires the concerns listed in that section, none of which are implemented here.

The proposed [Operational Execution and Digital Twin Architecture](../docs/architecture/operational-execution-digital-twin.md) and [readiness plan](../docs/planning/operational-execution-digital-twin-readiness.md) now own the concrete planning boundary for execution-context separation, verified operational identity/trust, consequential authorization, command/deployment safety, observation authenticity/provenance, and fail-safe recovery. Those documents are proposals, not claims about current product security.

## Reporting a Vulnerability

If you discover a security issue, please report it by opening a GitHub issue with the label `security`. For sensitive issues, contact the maintainers directly.

## Supported Versions

| Version | Supported |
|---------|-----------|
| 0.x (MVP) | Yes |

## Known Limitations

- The REST API does not require authentication.
- CORS is configured permissively for development.
- Scenario files and simulation state are not encrypted.

These limitations are acceptable for a local-only, single-user experimentation tool. They are **not** sufficient for production-consequential operational execution.

## Security Posture

Arcogine is local-first by default. Before exposing the service, you should apply network deployment controls.

### Key defaults

- Native CLI/API default bind: `127.0.0.1:3000`
- Container images keep bind behavior explicit for container networking
- No built-in production authentication/authorization
- No built-in application TLS termination
- No runtime encryption for scenario state
- No production-grade operational peer/source/target trust model
- No production actuation safety boundary

## Hardening for Network Deployment

If you expose the current simulation service beyond localhost, apply at least:

1. **Bind address** — Use `--addr 127.0.0.1:3000` for native/local runs. For containerized networked runs, configure host binding intentionally and avoid broad accidental exposure.

2. **CORS** — Set `CORS_ALLOWED_ORIGIN=http://your-ui-host:port` to restrict cross-origin access. When unset, CORS is permissive (`*`).

3. **TLS** — Arcogine does not terminate TLS. Place it behind a reverse proxy (nginx, Caddy, or a cloud load balancer) with TLS termination.

4. **Dependency auditing** — Before deployment, run the Java dependency scan (`cd product && ./gradlew cyclonedxBom && trivy sbom ... product/build/reports/cyclonedx/bom.json`) and the frontend audit (`cd product/interfaces/web && npm audit --audit-level=high`). CI runs npm audit as part of the frontend job and scans built container images via Trivy in the docker job. Run `./arcogine check --full` locally for the complete security suite including the Java dependency scan.

5. **Log verbosity** — Set `LOGGING_LEVEL_ROOT=WARN` in production-like environments to reduce log noise.

These controls harden network exposure of the current simulation service. They do **not** satisfy the additional trust, authorization, command-integrity, observation-authenticity, or fail-safe requirements for real-world actuation.

### Security scan ownership

Security execution follows the quality-gate contract:

- Scan commands invoke each scanner's native tool directly (`trivy sbom`, `trivy image`,
  `npm audit`, `gitleaks detect`) — locally via `./arcogine check --full`, in CI via the
  jobs in `.github/workflows/ci.yml` — so all checks are discoverable from the same
  command surface documented in `docs/development/testing.md`.
- CI remains responsible for installing scanner binaries/tools and enforcing policy
  controls (`--exit-code`, report handling, fail-fast behavior) around those commands.

For the full security verification test list, see `docs/development/testing.md`.

## Mature-product security principles

The [Product Charter](../docs/product/charter.md) describes a mature Arcogine that can eventually operate real production systems, not just simulate them. Real execution introduces concerns that today's local, single-user simulation tool does not need to address, and that are not implemented here:

- **Identity** — knowing who or what (human, agent, service, external system) is taking or reporting an action.
- **Verified trust/authenticity** — distinguishing a claimed identity from a sufficiently verified peer/source/target identity for the consequence of the operation, and protecting the integrity/authenticity of consequential command and observation paths.
- **Authority** — what that verified identity is permitted to do, and to what.
- **Permissions / least privilege** — enforceable boundaries around capability, target, and execution context, not just UI-level hiding.
- **Credential and trust lifecycle** — explicit expiry, rotation/revocation, secret handling, and loss-of-trust behavior appropriate to the selected deployment model.
- **Environment separation** — simulation, staging, and production must never be ambiguously distinguishable to a user or agent, per the Charter's "reality is explicit" principle.
- **Auditability** — a durable, trustworthy record of what happened and why, extending the current event-log/provenance direction to real-world consequence.
- **Safe failure** — real actions can fail partway; the system must have a defined, safe response rather than undefined behavior.
- **Unverifiable-input handling** — an unverifiable source/target/actor or integrity-failed message must not silently retain authoritative or permissive status.
- **Approvals where appropriate** — some actions should require confirmation or sign-off proportionate to their consequence, per the Charter's "safety scales with consequence" principle.
- **Operational consequence** — real actions have effects outside Arcogine (money moves, machines run, orders ship) that cannot be undone by resetting simulation state.
- **Independent observation provenance** — production observations retain their source/time/trust provenance independently of whichever Arcogine model revision later interprets them.

The concrete mechanism may eventually use OAuth/OIDC, certificates, protocol security profiles, a secrets manager, hardware roots of trust, or other technologies. This policy intentionally does not choose those mechanisms. The requirement is architectural: before Arcogine treats a path as production-consequential, the identity/trust basis, integrity/authenticity expectations, authority, least privilege, credential lifecycle, failure behavior, and threat assumptions must be explicit and testable.

None of these mature controls are implemented by the current MVP merely because they are now planned. The operational readiness track is responsible for turning the relevant principles into acceptance criteria before a live adapter can be considered production-ready.
