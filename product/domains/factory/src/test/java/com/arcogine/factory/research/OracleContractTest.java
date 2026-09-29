package com.arcogine.factory.research;

import static com.arcogine.factory.research.OutcomeAssertions.assertDerived;
import static com.arcogine.factory.research.OutcomeAssertions.assertUnderdetermined;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.research.ExperimentFixture.WindowIntent;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

/**
 * The contract a research-local derivation lives under: it reads only what its definition declares,
 * can never mistake a gapped event range for a complete one, may refuse, and depends on nothing but
 * supported evidence.
 */
class OracleContractTest {

    private static ResearchDefinition definition(String name, EvidenceInput... inputs) {
        return new ResearchDefinition(name, "test-only statement", Set.of(inputs));
    }

    private static Oracle<String> oracle(
            ResearchDefinition definition, Function<DeclaredEvidence, OracleOutcome<String>> body) {
        return new Oracle<>() {
            @Override
            public ResearchDefinition definition() {
                return definition;
            }

            @Override
            public OracleOutcome<String> evaluate(DeclaredEvidence evidence) {
                return body.apply(evidence);
            }
        };
    }

    @Test
    void aDerivationCannotReadEvidenceItsDefinitionDidNotDeclare() {
        ExperimentEvidence evidence = ExperimentRunner.run(StarterCorpus.capacityConstrainedBaseline());
        ResearchDefinition observationsOnly = definition("observations-only", EvidenceInput.OBSERVATIONS);

        IllegalStateException events = assertThrows(
                IllegalStateException.class,
                () -> oracle(observationsOnly, declared -> {
                            declared.completeEvents(0, 1);
                            return declared.derived("unreachable");
                        })
                        .evaluateOn(evidence));
        assertTrue(events.getMessage().contains("SUPPORTED_EVENTS"), events.getMessage());
        assertTrue(events.getMessage().contains("observations-only"), events.getMessage());

        assertThrows(
                IllegalStateException.class,
                () -> oracle(observationsOnly, declared -> {
                            declared.publishedModel();
                            return declared.derived("unreachable");
                        })
                        .evaluateOn(evidence));
        assertThrows(
                IllegalStateException.class,
                () -> oracle(definition("model-only", EvidenceInput.PUBLISHED_MODEL), declared -> {
                            declared.observation("mid-run");
                            return declared.derived("unreachable");
                        })
                        .evaluateOn(evidence));
        assertThrows(
                IllegalStateException.class,
                () -> oracle(observationsOnly, declared -> {
                            declared.missingEvents(0, 1);
                            return declared.derived("unreachable");
                        })
                        .evaluateOn(evidence));
    }

    @Test
    void aRangeWithAGapCanNeverBeReadAsIfItWereComplete() {
        ExperimentFixture joinedLate = new ExperimentFixture(
                "partial/baseline-joined-late",
                StarterCorpus.BASELINE_FAMILY.model(),
                List.of(
                        ExperimentStep.submit(
                                ThreeStepRoutingFamily.PRODUCT,
                                StarterCorpus.BASELINE_QUANTITY,
                                ThreeStepRoutingFamily.UNIT_PRICE),
                        ExperimentStep.discardEvents("joined-late"),
                        ExperimentStep.advanceToQuiescence(1_000),
                        ExperimentStep.captureEvents("after-join")),
                WindowIntent.PARTIAL,
                List.of());
        ExperimentEvidence partial = ExperimentRunner.run(joinedLate);
        long last = partial.window().runFinalSequence();
        ResearchDefinition events = definition("events", EvidenceInput.SUPPORTED_EVENTS);

        // Events 1..5 were drained but not retained. A range that reaches them is unavailable, whole,
        // even though most of it is retained; there is no way to read the retained part as if it were all.
        OracleOutcome<String> spanning = oracle(events, declared -> declared
                        .completeEvents(0, last)
                        .map(range -> declared.<String>derived("read " + range.size()))
                        .orElseGet(() -> declared.underdetermined(
                                "missing " + declared.missingEvents(0, last))))
                .evaluateOn(partial);
        assertTrue(assertUnderdetermined(spanning).reasons().getFirst().contains("from=1, through=5"));

        // A range that lies wholly inside what was retained is readable, and support records exactly it.
        OracleOutcome<String> suffix = oracle(events, declared -> declared
                        .completeEvents(5, last)
                        .map(range -> declared.<String>derived("read " + range.size()))
                        .orElseGet(() -> declared.underdetermined("unexpected gap")))
                .evaluateOn(partial);
        OracleOutcome.Derived<String> derived = assertDerived(suffix);
        assertEquals("read " + (last - 5), derived.value());
        assertEquals(Optional.of(new SequenceRange(6, last)), derived.support().events());
        assertEquals(List.of(), derived.support().observations());
    }

    @Test
    void completeEventsReturnsTheRetainedEventsInSequenceOrder() {
        ExperimentEvidence evidence = ExperimentRunner.run(StarterCorpus.capacityConstrainedBaseline());
        ResearchDefinition events = definition("events", EvidenceInput.SUPPORTED_EVENTS);
        OracleOutcome<List<Long>> outcome = new Oracle<List<Long>>() {
            @Override
            public ResearchDefinition definition() {
                return events;
            }

            @Override
            public OracleOutcome<List<Long>> evaluate(DeclaredEvidence declared) {
                List<RuntimeEventEnvelope> range = declared.completeEvents(2, 6).orElseThrow();
                return declared.derived(range.stream().map(RuntimeEventEnvelope::sequence).toList());
            }
        }.evaluateOn(evidence);

        assertEquals(List.of(3L, 4L, 5L, 6L), assertDerived(outcome).value());
        assertEquals(Optional.of(new SequenceRange(3, 6)), assertDerived(outcome).support().events());
    }

    @Test
    void anUnknownObservationLabelIsAnErrorNotAnEmptyResult() {
        ExperimentEvidence evidence = ExperimentRunner.run(StarterCorpus.capacityConstrainedBaseline());

        IllegalArgumentException failure = assertThrows(
                IllegalArgumentException.class, () -> new WaitingWorkByStepOracle("no-such-label").evaluateOn(evidence));

        assertTrue(failure.getMessage().contains("no-such-label"), failure.getMessage());
        assertTrue(failure.getMessage().contains("mid-run"), "the message lists the labels that were captured");
    }

    @Test
    void refusalIsAFirstClassOutcomeAndAClaimCanExpectIt() {
        ExperimentEvidence evidence = ExperimentRunner.run(StarterCorpus.longStepAndParallelCapacity());
        Oracle<List<ProcessingOccupancyOracle.ResourceOccupancy>> atSubmission =
                new ProcessingOccupancyOracle("after-submission");

        // The oracle refuses, so a claim that expects the refusal holds and one that expects a value does not.
        assertTrue(ExpectedClaim.refuses("refused", atSubmission).check(evidence).holds());
        assertFalse(ExpectedClaim.derives("guessed", atSubmission, List.of()).check(evidence).holds());

        // And in the other direction: a derived value is not a refusal, and a wrong value does not hold.
        Oracle<List<ProcessingOccupancyOracle.ResourceOccupancy>> atMidRun = new ProcessingOccupancyOracle("mid-run");
        assertFalse(ExpectedClaim.refuses("wrongly-expected-refusal", atMidRun).check(evidence).holds());
        assertFalse(ExpectedClaim.derives("wrong-value", atMidRun, List.of()).check(evidence).holds());
    }

    @Test
    void aDerivationDependsOnlyOnDeclaredEvidenceNotOnRunIdentityOrTheRuntime() {
        // Reconstruction: the outcome computed from one run's evidence is reproduced exactly from a
        // second run's evidence after normalizing only run identity. Nothing else can differ, because the
        // derivation never sees the runtime -- only the evidence bundle -- so it cannot depend on it.
        for (ExperimentFixture fixture : StarterCorpus.all()) {
            ExperimentEvidence first = ExperimentRunner.run(fixture);
            ExperimentEvidence second = ExperimentRunner.run(fixture).withNormalizedRunIdentity();
            for (ExpectedClaim<?> claim : fixture.expectedClaims()) {
                assertEquals(
                        claim.oracle().evaluateOn(first),
                        claim.oracle().evaluateOn(second),
                        fixture.id() + " / " + claim.name());
            }
        }
    }

    @Test
    void aFixtureListsTheDistinctDefinitionsItsClaimsDependOn() {
        assertEquals(
                List.of(WaitingWorkByStepOracle.DEFINITION, ProcessingOccupancyOracle.DEFINITION),
                StarterCorpus.capacityConstrainedBaseline().researchDefinitions());
        assertEquals(
                List.of(ProcessingOccupancyOracle.DEFINITION),
                StarterCorpus.longStepAndParallelCapacity().researchDefinitions());
        assertEquals(
                Set.of(EvidenceInput.OBSERVATIONS, EvidenceInput.SUPPORTED_EVENTS),
                ProcessingOccupancyOracle.DEFINITION.inputs());
    }

    @Test
    void definitionsAndOutcomesRejectAmbiguousShapes() {
        assertThrows(IllegalArgumentException.class, () -> new ResearchDefinition(" ", "s", Set.of(EvidenceInput.OBSERVATIONS)));
        assertThrows(IllegalArgumentException.class, () -> new ResearchDefinition("n", " ", Set.of(EvidenceInput.OBSERVATIONS)));
        assertThrows(IllegalArgumentException.class, () -> new ResearchDefinition("n", "s", Set.of()));
        ResearchDefinition definition = definition("d", EvidenceInput.OBSERVATIONS);
        assertThrows(
                IllegalArgumentException.class, () -> new OracleOutcome.Underdetermined<String>(definition, List.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> ExpectedClaim.refuses(" ", oracle(definition, declared -> declared.derived("unused"))));
    }
}
