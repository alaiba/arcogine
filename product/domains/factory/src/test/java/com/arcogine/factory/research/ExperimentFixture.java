package com.arcogine.factory.research;

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
 * @param id stable name, meaningful within the fixture corpus
 * @param authoredModel the authored Factory model; publishing it yields the exact version, and
 *     therefore the fingerprint, the run is instantiated from
 * @param script the explicit, ordered workload/command and evidence-collection script
 * @param windowIntent whether the script is meant to retain every supported event of the run
 * @param expectedClaims oracle ground truth, stated before the run and never mixed into evidence
 */
public record ExperimentFixture(
        String id,
        FactoryModel authoredModel,
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
        Objects.requireNonNull(authoredModel, "authoredModel");
        Objects.requireNonNull(windowIntent, "windowIntent");
        script = List.copyOf(Objects.requireNonNull(script, "script"));
        expectedClaims = List.copyOf(Objects.requireNonNull(expectedClaims, "expectedClaims"));
        if (script.isEmpty()) {
            throw new IllegalArgumentException("a fixture needs at least one script step");
        }
        requireDistinctLabels(script);
        // Publication validates the authored model, so a malformed fixture fails where it is built.
        FactoryModelPublisher.publish(authoredModel);
    }

    /** Publishes the authored model through the existing Factory publication boundary. */
    public FactoryModelVersion publishedModel() {
        return FactoryModelPublisher.publish(authoredModel);
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
