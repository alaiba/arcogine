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

## Current execution surface

Arcogine currently has no application server, HTTP API, or CLI product surface to load these scenarios through interactively. They remain educational/executable documentation of the scenario format rather than a runnable starter loop today. A future outward consumer will be introduced from the supported runtime contract when a concrete product need exists.
