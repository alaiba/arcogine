# Arcogine — Architectural Overview

This document sits under the [Product Charter](/docs/product/charter.md), which defines Arcogine's enduring product direction and principles. This document describes the design philosophy and architectural principles that guide Arcogine's implementation *today*, and distinguishes principles expected to persist regardless of implementation from constraints specific to the current MVP. This document and the focused architecture/specification documents it links are the current architectural authority; Git and pull-request history hold the historical rationale for how they reached their present form, and selected non-normative [decision-rationale records](/docs/history/README.md#decision-rationale) may preserve why a significant choice was made without ever defining current architecture.

## Routed topics

Each section below names the topic that owns it and when to read it. A routed topic is mandatory whenever its trigger applies; only the state ownership table is reference material, consulted for the entities a change touches. Before touching cross-module boundaries, read at least the domain authority, events/state/observations, module and determinism topics.

## Enduring architectural principles

Owned by [Enduring architectural principles](overview/principles-and-domain-authority.md). Read it when reasoning about which domain owns a fact or a semantic responsibility.

### Domain authority

Owned by [Domain authority](overview/principles-and-domain-authority.md#domain-authority). Read it when a change places, moves or relies on authority for a fact, a definition or a derived analysis.

### Determinism, equivalence, identity, and durability

Owned by [Determinism, equivalence, identity, and durability](overview/determinism-and-durability.md). Read it when a change makes or relies on a determinism, durability, equivalence, canonicalization, fingerprint, identity or label claim; equivalence, identity and labels continue in [equivalence and identity](overview/equivalence-and-identity.md).

#### Durability and related properties

Owned by [Durability and related properties](overview/determinism-and-durability.md#durability-and-related-properties). Read it when a change claims that something is durable, stable or retained.

### Semantic evolution and support

Owned by [Semantic evolution and support](overview/semantic-evolution.md). Read it when a change alters the meaning, support or promotion of a semantic contract, or relies on the development reset policy.

## Current implementation constraints (MVP)

Owned by [Current implementation constraints (MVP)](overview/current-constraints.md). Read it when judging what exists today, what the Product Charter implies for architecture, or the simulation-first boundary.

## Core Architecture Philosophy: Events, State, Observations

Owned by [Core Architecture Philosophy: Events, State, Observations](overview/events-state-observations.md). Read it when a change adds or alters events, mutable state, observations, projections or decisions, or touches commercial, operational and financial facts.

#### State ownership table

Owned by [State ownership table](overview/state-ownership-table.md). Read it when looking up the authoritative owner of a specific piece of state (reference).

## Operational execution — not implemented

Owned by [Operational execution — not implemented](overview/operational-execution.md). Read it when a change approaches production-control, external actuation or digital-twin concerns.

## Attribution and decision boundaries

Owned by [Attribution and decision boundaries](overview/attribution-and-provenance.md). Read it when a change introduces or relies on actors, decision sources, subjects or provenance; requests, replay and deliberately absent abstractions continue in [requests and replay](overview/attribution-requests-and-replay.md).

## Discrete-Event Simulation (DES)

Owned by [Discrete-Event Simulation (DES)](overview/simulation-modules-and-types.md). Read it when adding a domain or module, changing the dependency graph or event dispatch, or adding shared types.

### Dependency graph

Owned by [Dependency graph](overview/simulation-modules-and-types.md#dependency-graph). Read it when a change adds or alters a module dependency.

## Determinism Contract

Owned by [Determinism Contract](overview/determinism-contract.md). Read it when a change can affect replay, ordering, randomness, time or result equality.

## Factory model and runtime provenance (current state)

Owned by [Factory model and runtime provenance (current state)](overview/factory-provenance-adapters-and-stack.md). Read it when a change touches Factory model publication or runtime provenance, an outward adapter, or the technology stack.

## Outward Adapters

Owned by [Outward Adapters](overview/factory-provenance-adapters-and-stack.md#outward-adapters). Read it when adding or changing an outward consumer or adapter.
