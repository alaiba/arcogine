# Security Policy

## Scope

Arcogine is a simulation engine intended for local development and experimentation. The MVP does not include production-grade authentication, authorization, or data encryption.

This document has two parts: the current security posture and limitations (below), which describe today's software honestly, and the [mature-product security principles](#mature-product-security-principles) implied by the [Product Charter](PRODUCT_CHARTER.md), which are conceptual expectations for a future that can execute real operations — not a description of anything implemented today. Adding TLS, CORS restrictions, or network hardening to the current software does not, by itself, make it suitable for operating a production business; that requires the concerns listed in that section, none of which are designed or implemented here.

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

These limitations are acceptable for a local-only, single-user experimentation tool. Production deployments should add appropriate security controls.

## Security Posture

Arcogine is local-first by default. Before exposing the service, you should apply network deployment controls.

### Key defaults

- Native CLI/API default bind: `127.0.0.1:3000`
- Container images keep bind behavior explicit for container networking
- No built-in production authentication/authorization
- No built-in application TLS termination
- No runtime encryption for scenario state

## Hardening for Network Deployment

If you expose Arcogine beyond localhost, apply at least:

1. **Bind address** — Use `--addr 127.0.0.1:3000` for native/local runs. For containerized networked runs, configure host binding intentionally and avoid broad accidental exposure.

2. **CORS** — Set `CORS_ALLOWED_ORIGIN=http://your-ui-host:port` to restrict cross-origin access. When unset, CORS is permissive (`*`).

3. **TLS** — Arcogine does not terminate TLS. Place it behind a reverse proxy (nginx, Caddy, or a cloud load balancer) with TLS termination.

4. **Dependency auditing** — Run `make java-audit` and `make frontend-audit` before deployment. CI runs npm audit as part of `make ci-frontend` and scans container images via `make trivy-scan-api` / `make trivy-scan-ui`. Run `make quality-full` locally for the complete security suite including the Java dependency scan.

5. **Log verbosity** — Set `LOGGING_LEVEL_ROOT=WARN` in production-like environments to reduce log noise.

### Security scan ownership

Security execution follows the quality-gate contract:

- Scan command bodies are wrapped in Make targets (`rust-audit`, `frontend-audit`,
  `trivy-scan-api`, `trivy-scan-ui`, `gitleaks`) so all checks are discoverable from
  the same command surface.
- CI remains responsible for installing scanner binaries/tools and enforcing policy
  controls (`--exit-code`, report handling, fail-fast behavior) around those targets.

For the full security verification test list, see `docs/TESTING.md`.

## Mature-product security principles

The [Product Charter](PRODUCT_CHARTER.md) describes a mature Arcogine that can eventually operate real production systems, not just simulate them. Real execution introduces concerns that today's local, single-user simulation tool does not need to address, and that are not designed or scoped here — this section names them conceptually so they aren't forgotten, not so they get built prematurely:

- **Identity** — knowing who or what (human, agent, external system) is taking an action.
- **Authority** — what that identity is permitted to do, and to what.
- **Permissions** — enforceable boundaries around capability, not just UI-level hiding.
- **Environment separation** — simulation, staging, and production must never be ambiguously distinguishable to a user or agent, per the Charter's "reality is explicit" principle.
- **Auditability** — a durable, trustworthy record of what happened and why, extending the current event-log/provenance direction to real-world consequence.
- **Safe failure** — real actions can fail partway; the system must have a defined, safe response rather than undefined behavior.
- **Approvals where appropriate** — some actions should require confirmation or sign-off proportionate to their consequence, per the Charter's "safety scales with consequence" principle.
- **Operational consequence** — real actions have effects outside Arcogine (money moves, machines run, orders ship) that cannot be undone by resetting simulation state.

None of these are designed, scheduled, or committed to by this document. They are the conceptual boundary this security policy will need to grow into if and when Arcogine begins to execute real operations.
