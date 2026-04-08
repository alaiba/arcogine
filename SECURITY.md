# Security Policy

## Scope

Arcogine is a simulation engine intended for local development and experimentation. The MVP does not include production-grade authentication, authorization, or data encryption.

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

4. **Dependency auditing** — Run `make rust-audit` and `make frontend-audit` before deployment. CI runs npm audit as part of `make ci-frontend` and scans container images via `make trivy-scan-api` / `make trivy-scan-ui`. Run `make quality-full` locally for the complete security suite including Rust audit.

5. **Log verbosity** — Set `RUST_LOG=warn` in production-like environments to reduce log noise.

### Security scan ownership

Security execution follows the quality-gate contract:

- Scan command bodies are wrapped in Make targets (`rust-audit`, `frontend-audit`,
  `trivy-scan-api`, `trivy-scan-ui`, `gitleaks`) so all checks are discoverable from
  the same command surface.
- CI remains responsible for installing scanner binaries/tools and enforcing policy
  controls (`--exit-code`, report handling, fail-fast behavior) around those targets.

For the full security verification test list, see `docs/TESTING.md`.
