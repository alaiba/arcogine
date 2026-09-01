package com.arcogine.challenge.attempt;

import static com.arcogine.challenge.attempt.ChallengeAttemptFixtures.economics;
import static com.arcogine.challenge.attempt.ChallengeAttemptFixtures.evaluate;
import static com.arcogine.challenge.attempt.ChallengeAttemptFixtures.placed;
import static com.arcogine.challenge.attempt.ChallengeAttemptFixtures.snapshot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.challenge.ChallengeFixtures;
import com.arcogine.challenge.admissibility.CandidateDraftSnapshot;
import com.arcogine.challenge.admissibility.PlacedEquipment;
import com.arcogine.challenge.economics.DraftEconomics;
import com.arcogine.challenge.evaluation.ChallengeEvaluationResult;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChallengeAttemptTest {

    @Test
    void completedAttemptRetainsExactChallengeAndPolicyVersion() {
        ChallengeEvaluationResult result = evaluate(true, 350L, 10_000L);
        ChallengeAttempt attempt = ChallengeAttempt.record(snapshot(), economics(10_000L), result);

        assertEquals(ChallengeFixtures.referenceChallenge().identity(), attempt.challengeIdentity());
        assertEquals(ChallengeFixtures.referenceChallenge().evaluationPolicy(),
                attempt.evaluationPolicy());
        assertEquals(result, attempt.evaluationResult());
    }

    @Test
    void recordingGeneratesDistinctIdentitiesForEquivalentInputs() {
        ChallengeEvaluationResult result = evaluate(true, 350L, 10_000L);
        ChallengeAttempt first = ChallengeAttempt.record(snapshot(), economics(10_000L), result);
        ChallengeAttempt second = ChallengeAttempt.record(snapshot(), economics(10_000L), result);

        assertNotEquals(first.id(), second.id());
        assertEquals(first.evaluationResult(), second.evaluationResult());
    }

    @Test
    void completedAttemptDefensivelyRetainsCandidateSnapshot() {
        List<PlacedEquipment> source = new ArrayList<>(List.of(placed("cutter-1")));
        CandidateDraftSnapshot snapshot = new CandidateDraftSnapshot(source);
        ChallengeAttempt attempt = ChallengeAttempt.record(snapshot, economics(5_000L),
                evaluate(true, 350L, 5_000L));

        source.clear();

        assertEquals(1, attempt.candidateSnapshot().placedEquipment().size());
        assertThrows(UnsupportedOperationException.class,
                () -> attempt.candidateSnapshot().placedEquipment().clear());
    }

    @Test
    void historicalEconomicsDoNotChangeWhenCurrentCatalogueChanges() {
        DraftEconomics originalEconomics = economics(10_000L);
        ChallengeAttempt attempt = ChallengeAttempt.record(snapshot(), originalEconomics,
                evaluate(true, 350L, 10_000L));

        DraftEconomics changedCatalogueEconomics = new DraftEconomics(40_000L, 25_000L, 15_000L);

        assertEquals(originalEconomics, attempt.economics());
        assertNotEquals(changedCatalogueEconomics, attempt.economics());
    }

    @Test
    void attemptRetainsEvaluationResultWithoutReevaluation() {
        ChallengeEvaluationResult result = evaluate(true, 350L, 10_000L);
        ChallengeAttempt attempt = ChallengeAttempt.record(snapshot(), economics(10_000L), result);

        assertTrue(attempt.evaluationResult().successful());
        assertEquals(result.score(), attempt.evaluationResult().score());
    }

    @Test
    void optionalRuntimeProvenanceIsNotFabricated() {
        ChallengeEvaluationResult result = evaluate(true, 350L, 10_000L);
        ChallengeAttempt attempt = ChallengeAttempt.record(snapshot(), economics(10_000L), result);

        assertEquals("model.factory-basics:v1",
                attempt.evaluationResult().provenance().publishedModelReference());
        assertEquals("run.factory-basics:historical-1",
                attempt.evaluationResult().provenance().runReference());
    }

    @Test
    void rejectsNullConstruction() {
        ChallengeAttemptId id = ChallengeAttemptId.generate();
        CandidateDraftSnapshot snapshot = snapshot();
        DraftEconomics economics = economics(10_000L);
        ChallengeEvaluationResult result = evaluate(true, 350L, 10_000L);

        assertThrows(NullPointerException.class,
                () -> new ChallengeAttempt(null, snapshot, economics, result));
        assertThrows(NullPointerException.class,
                () -> new ChallengeAttempt(id, null, economics, result));
        assertThrows(NullPointerException.class,
                () -> new ChallengeAttempt(id, snapshot, null, result));
        assertThrows(NullPointerException.class,
                () -> new ChallengeAttempt(id, snapshot, economics, null));
        assertThrows(NullPointerException.class, () -> new ChallengeAttemptId(null));
        assertThrows(IllegalArgumentException.class, () -> new ChallengeAttemptId(" "));
    }
}
