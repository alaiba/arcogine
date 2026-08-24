# Arcogine scenarios

This directory contains shipped TOML scenarios for quick onboarding — a guided learning path through the simulation's controls and dynamics, not Arcogine's product identity (see the [Product Charter](../product/charter.md)).

All listed scenarios exist today and are intentionally designed as a learning progression, each building on the last.

## `basic.toml` — Balanced baseline

**Goal:** learn controls without edge-case pressure.

**What to look for:** KPI stability, smooth queue movement, and predictable order flow.

## `overload.toml` — Stress handling

**Goal:** rescue production quality when demand outpaces capacity.

**What to look for:** backlog buildup, lead-time pressure, and the impact of price/machine interventions.

## `capacity-expansion.toml` — Structural improvement

**Goal:** compare one-off upgrades to reactive controls.

**What to look for:** throughput recovery and whether expanded capacity beats repeated parameter tuning.

## Scenario format

Scenarios are defined in TOML using ISA-95-aligned section names.
See:

- `product/types/src/main/java/com/arcogine/types/scenario/ScenarioConfig.java` for schema
- `product/simulation/src/main/java/com/arcogine/core/scenario/ScenarioLoader.java` for loader details

## Starter loop for first-time users

1. Open the UI and load one of the built-in scenarios from the welcome overlay.
2. Run or Step to establish baseline dynamics.
3. Save a baseline before making interventions.
4. Change one control at a time (price, machine state, agent).
5. Compare against saved baseline metrics and iterate.
