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

## Hardening for Network Deployment

If you intend to expose Arcogine beyond localhost (e.g., on a LAN or the internet), apply the following measures:

1. **Bind address** — The CLI defaults to `127.0.0.1:3000`. For container deployments the Dockerfile binds to `0.0.0.0`. For non-Docker use, keep the `127.0.0.1` default or use `--addr` explicitly.

2. **CORS** — Set `CORS_ALLOWED_ORIGIN=http://your-ui-host:port` to restrict cross-origin access. When unset, CORS is permissive (`*`).

3. **TLS** — Arcogine does not terminate TLS. Place it behind a reverse proxy (nginx, Caddy, or a cloud load balancer) with TLS termination.

4. **Dependency auditing** — Run `cargo audit` and `npm audit` (in the `ui/` directory) before deployment. CI runs these checks automatically on every push.

5. **Log verbosity** — Set `RUST_LOG=warn` in production to reduce log volume and avoid leaking internal details.
