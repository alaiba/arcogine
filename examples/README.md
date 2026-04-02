# Example Scenarios

This directory contains TOML scenario files for Arcogine simulations.

## Planned scenarios (Phase 3)

- `basic_scenario.toml` — Balanced factory with moderate demand. Baseline for comparison.
- `overload_scenario.toml` — Low price drives high demand, creating backlog and bottlenecks.
- `capacity_expansion_scenario.toml` — Additional machines added to relieve bottleneck.

## Scenario format

Scenarios are defined in TOML using ISA-95-aligned section names. See `crates/sim-types/src/scenario.rs` for the configuration schema and `crates/sim-core/src/scenario.rs` for the loader.
