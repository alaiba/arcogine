# Arcogine scenarios

This directory contains shipped TOML scenarios for quick onboarding and challenge play.

All listed scenarios exist today and are intentionally designed as challenge modes.

## `basic_scenario.toml` — Balanced baseline

**Goal:** learn controls without edge-case pressure.

**What to look for:** KPI stability, smooth queue movement, and predictable order flow.

## `overload_scenario.toml` — Stress handling

**Goal:** rescue production quality when demand outpaces capacity.

**What to look for:** backlog buildup, lead-time pressure, and the impact of price/machine interventions.

## `capacity_expansion_scenario.toml` — Structural improvement

**Goal:** compare one-off upgrades to reactive controls.

**What to look for:** throughput recovery and whether expanded capacity beats repeated parameter tuning.

## Scenario format

Scenarios are defined in TOML using ISA-95-aligned section names.
See:

- `crates/sim-types/src/scenario.rs` for schema
- `crates/sim-core/src/scenario.rs` for loader details

## Starter loop for first-time players

1. Open the UI and load one of the built-in scenarios from the welcome overlay.
2. Run or Step to establish baseline dynamics.
3. Save a baseline before making interventions.
4. Change one control at a time (price, machine state, agent).
5. Compare against saved baseline metrics and iterate.
