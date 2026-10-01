package com.arcogine.research.experiment;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The deterministic input side of one research experiment: everything needed to reproduce a run,
 * and the ground truth its derivations are expected to reach, kept apart from any evidence a run
 * produces.
 *
 * <p>A fixture preserves semantic inputs only. It carries no repository revision: the exact source
 * baseline that produced a run belongs to the research custody that ran it ({@code
 * docs/development/researching.md}), not to a durable Java fixture. It carries no Engine-definition
 * identifier either, because the Engine interpretation has none ({@code
 * docs/architecture/engine-semantics.md} section 1); a fixture always runs against the
 * interpretation of the revision it is executed on.
 *
 * <p>The authored model is published once, where the fixture is built, so a malformed fixture fails
 * there; every run of the fixture is instantiated from that one published version. Two fixtures are
 * equal exactly when their inputs are: a published version is equal to another publication of an
 * equal authored model.
 *
 * @param id stable name, meaningful within the fixture corpus
 * @param publishedModel the published version of the authored model; it fixes the fingerprint every
 *     run is instantiated from
 * @param script the explicit, ordered workload/command and evidence-collection script
 * @param windowIntent whether the script is meant to retain every supported event of the run
 * @param expectedClaims oracle ground truth, stated before the run and never mixed into evidence
 */
public record ExperimentFixture(
        String id,
        FactoryModelVersion publishedModel,
        List<ExperimentStep> script,
        WindowIntent windowIntent,
        List<ExpectedClaim<?>> expectedClaims) {

    /**
     * What the fixture intends its captured supported-event window to be. The runner checks that
     * the evidence it produced matches, so a partial trace can never pass as a complete history and
     * a deliberately partial fixture cannot quietly turn complete.
     */
    public enum WindowIntent {
        /** Every supported event of the run, from the first through the closing observation. */
        COMPLETE_RUN,
        /** A deliberately incomplete capture; window-dependent claims must be underdetermined. */
        PARTIAL
    }

    public ExperimentFixture {
        if (Objects.requireNonNull(id, "id").isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        Objects.requireNonNull(publishedModel, "publishedModel");
        Objects.requireNonNull(windowIntent, "windowIntent");
        script = List.copyOf(Objects.requireNonNull(script, "script"));
        expectedClaims = List.copyOf(Objects.requireNonNull(expectedClaims, "expectedClaims"));
        if (script.isEmpty()) {
            throw new IllegalArgumentException("a fixture needs at least one script step");
        }
        requireDistinctLabels(script);
    }

    /**
     * Publishes {@code authoredModel} through the existing Factory publication boundary, which
     * validates it, and keeps that published version for every run of the fixture.
     */
    public ExperimentFixture(
            String id,
            FactoryModel authoredModel,
            List<ExperimentStep> script,
            WindowIntent windowIntent,
            List<ExpectedClaim<?>> expectedClaims) {
        this(id,
                FactoryModelPublisher.publish(Objects.requireNonNull(authoredModel, "authoredModel")),
                script,
                windowIntent,
                expectedClaims);
    }

    /** The authored model facts every run of this fixture is instantiated from. */
    public FactoryModel authoredModel() {
        return publishedModel.model();
    }

    /** The distinct research-local definitions this fixture's expected claims depend on. */
    public List<ResearchDefinition> researchDefinitions() {
        return expectedClaims.stream().map(claim -> claim.oracle().definition()).distinct().toList();
    }

    private static void requireDistinctLabels(List<ExperimentStep> script) {
        Set<String> labels = new HashSet<>();
        for (ExperimentStep step : script) {
            if (step instanceof ExperimentStep.Capture capture) {
                if (ExperimentEvidence.CLOSING_LABEL.equals(capture.label())) {
                    throw new IllegalArgumentException(
                            "label '" + capture.label() + "' is reserved for the runner's closing observation");
                }
                if (!labels.add(capture.label())) {
                    throw new IllegalArgumentException("duplicate collection label '" + capture.label() + "'");
                }
            }
        }
    }
}
