package com.arcogine.research.experiment;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeObservation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds deliberately altered copies of an evidence bundle, for tests that need to show a check
 * notices a difference or an oracle refuses evidence that does not add up. Nothing here runs a
 * runtime, so an altered bundle is never mistaken for something a run produced.
 */
final class TamperedEvidence {

    private TamperedEvidence() {}

    static ExperimentEvidence withEvents(ExperimentEvidence evidence, List<RuntimeEventEnvelope> events) {
        return new ExperimentEvidence(
                evidence.fixtureId(),
                evidence.publishedModel(),
                evidence.modelFingerprint(),
                evidence.script(),
                evidence.commands(),
                evidence.observations(),
                events,
                evidence.window());
    }

    static ExperimentEvidence withObservation(
            ExperimentEvidence evidence, String label, RuntimeObservation replacement) {
        Map<String, RuntimeObservation> observations = new LinkedHashMap<>(evidence.observations());
        observations.put(label, replacement);
        return new ExperimentEvidence(
                evidence.fixtureId(),
                evidence.publishedModel(),
                evidence.modelFingerprint(),
                evidence.script(),
                evidence.commands(),
                observations,
                evidence.retainedEvents(),
                evidence.window());
    }

    static ExperimentEvidence withPublishedModel(ExperimentEvidence evidence, FactoryModel model) {
        return new ExperimentEvidence(
                evidence.fixtureId(),
                model,
                evidence.modelFingerprint(),
                evidence.script(),
                evidence.commands(),
                evidence.observations(),
                evidence.retainedEvents(),
                evidence.window());
    }
}
