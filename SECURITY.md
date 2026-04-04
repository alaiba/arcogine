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

If you deploy Arcogine outside a trusted local environment:

1. Use `--addr 127.0.0.1:3000` (the default) and place behind a reverse proxy with TLS termination.
2. Set the `CORS_ALLOWED_ORIGIN` environment variable to your UI's origin (e.g., `http://yourdomain.com`).
3. Run `cargo audit` and `npm audit` before each deployment.
4. Set `RUST_LOG=warn` in production to reduce log verbosity.
5. Consider adding authentication before exposing the API to untrusted networks.
