 # syntax=docker/dockerfile:1.7
FROM rust:1.94-slim AS builder

WORKDIR /app
COPY Cargo.toml Cargo.lock rust-toolchain.toml ./
COPY crates/ crates/

RUN --mount=type=cache,target=/usr/local/cargo/registry \
    --mount=type=cache,target=/usr/local/cargo/git \
    cargo build --release --bin arcogine

FROM debian:trixie-slim

RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get install -y --no-install-recommends ca-certificates curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/target/release/arcogine /usr/local/bin/arcogine
COPY examples/ /app/examples/

WORKDIR /app
EXPOSE 3000

ENTRYPOINT ["arcogine"]
CMD ["serve", "--addr", "0.0.0.0:3000"]
