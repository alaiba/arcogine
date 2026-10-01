/**
 * A substrate for deterministic research experiments over the supported Factory/Engine runtime
 * contract.
 *
 * <p>It gives experiments one shared shape instead of each defining its own harness. It is
 * non-shipped research infrastructure, not an Arcogine subsystem: it adds no product capability or
 * public API, no production or consumer source set depends on it, and it is not an analytics
 * framework, a plugin system, or an evidence format.
 *
 * <p>The vocabulary keeps inputs, evidence and derivation apart:
 *
 * <pre>{@code
 * ExperimentFixture        inputs and ground truth: the published model, ordered script, window
 *      |                   intent, expected claims -- no evidence, no repository revision
 *      v  ExperimentRunner  a fresh FactoryRuntime, driven only through supported control
 * ExperimentEvidence       raw supported evidence: published model facts, observations, retained
 *      |                   supported events, and the EvidenceWindow saying what was collected
 *      v  Oracle            a research-local derivation, given only DeclaredEvidence
 * OracleOutcome            a derived value with the exact evidence that supports it, or a refusal
 * }</pre>
 *
 * <p>Points every user of this package should hold on to:
 *
 * <ul>
 *   <li>Runtime event delivery drains. An experiment's events are only as complete as the drains it
 *       kept, so where evidence is collected is part of the experiment, and {@code EvidenceWindow}
 *       records it. A fixture declares whether it means to capture the complete run, and the runner
 *       fails if the evidence does not match.
 *   <li>Evidence is the published model plus supported observations and supported events, kept in
 *       experiment-local values; it is not a Governance evidence reference. Nothing here reads
 *       scheduler queues, handler or store state, or the internal events that advancement and
 *       command results expose: the module compiles against only the Factory and shared-type
 *       modules, and its boundary test holds the whole package to the supported contract.
 *   <li>A derivation is research-local. Its existence here does not make it an Engine fact,
 *       game-owned analytics, a Factory concept, or a public API, and a test helper cannot decide
 *       otherwise. It may read only the inputs its {@code ResearchDefinition} declares, can only
 *       read a supported-event range that has no gap, and may refuse. A measurement it reports is
 *       not an interpretation of that measurement: which resource or step limits a design is a
 *       question an investigation answers in its own artifacts.
 *   <li>A fixture carries no Engine-definition identifier because the Engine interpretation has
 *       none, and no repository revision because the revision that produced a run belongs to the
 *       research custody that ran it ({@code docs/development/researching.md}). The Engine
 *       interpretation a fixture runs against is the one of the revision it is executed on.
 * </ul>
 *
 * <p>To add a fixture, author a model ({@code LinearRoutingFamily} projects resources, their
 * concurrency, the steps each may serve and their order onto the current Factory model without
 * inventing facts, and {@code ThreeStepRoutingFamily} is its one-resource-per-stage case), write its
 * script and expected claims, and add it to a corpus. The module's own corpus tests hold every
 * fixture of its corpora to deterministic replay, its declared window, and its expected claims
 * without any per-fixture harness; {@code ExperimentRunner.runAndReplay} gives any other experiment
 * the same replay check.
 */
package com.arcogine.research.experiment;
