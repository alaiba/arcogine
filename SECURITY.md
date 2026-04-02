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
