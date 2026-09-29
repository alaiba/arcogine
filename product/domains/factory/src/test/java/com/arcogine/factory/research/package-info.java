/**
 * A test-only substrate for deterministic research experiments over the supported Factory/Engine
 * runtime contract.
 *
 * <p>It gives experiments one shared shape instead of each defining its own harness. It is research
 * and test infrastructure, not an Arcogine subsystem: it lives only in test sources, adds no
 * production API or dependency, and is not an analytics framework, a plugin system, or an
 * evidence format.
 *
 * <p>The vocabulary keeps inputs, evidence and derivation apart:
 *
 * <pre>{@code
 * ExperimentFixture        inputs and ground truth: authored model, ordered script, window intent,
 *      |                   expected claims -- no evidence, no repository revision
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
 *   <li>Evidence is the published model plus supported observations and supported events. Nothing
 *       here reads scheduler queues, handler or store state, or the internal events that advancement
 *       and command results expose, and a source-level test holds the package to that.
 *   <li>A derivation is research-local. Its existence here does not make it an Engine fact,
 *       game-owned analytics, or a public API, and a test helper cannot decide otherwise. It may
 *       read only the inputs its {@code ResearchDefinition} declares, can only read a
 *       supported-event range that has no gap, and may refuse.
 *   <li>A fixture carries no Engine-definition identifier because the Engine interpretation has
 *       none, and no repository revision because the revision that produced a run belongs to the
 *       research custody that ran it ({@code docs/development/researching.md}). The Engine
 *       interpretation a fixture runs against is the one of the revision it is executed on.
 * </ul>
 *
 * <p>To add a fixture, build a model (the {@code ThreeStepRoutingFamily} projects onto the current
 * Factory model without inventing facts), write its script and expected claims, and add it to a
 * corpus such as {@code StarterCorpus}. The corpus-wide tests then hold it to deterministic replay,
 * its declared window, and its expected claims without any per-fixture harness.
 */
package com.arcogine.factory.research;
