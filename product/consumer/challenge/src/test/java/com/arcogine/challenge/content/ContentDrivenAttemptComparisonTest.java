package com.arcogine.challenge.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.challenge.ChallengeDefinition;
import com.arcogine.challenge.admissibility.CandidateDraftSnapshot;
import com.arcogine.challenge.admissibility.EquipmentOccurrenceId;
import com.arcogine.challenge.admissibility.GridPlacement;
import com.arcogine.challenge.admissibility.PlacedEquipment;
import com.arcogine.challenge.attempt.ChallengeAttempt;
import com.arcogine.challenge.comparison.AttemptComparison;
import com.arcogine.challenge.comparison.AttemptComparisonWinner;
import com.arcogine.challenge.comparison.ChallengeAttemptComparator;
import com.arcogine.challenge.economics.DraftEconomics;
import com.arcogine.challenge.evaluation.AuthoritativeOutcomeFacts;
import com.arcogine.challenge.evaluation.ChallengeEvaluationInput;
import com.arcogine.challenge.evaluation.ChallengeEvaluationResult;
import com.arcogine.challenge.evaluation.EvaluationProvenance;
import com.arcogine.challenge.evaluation.ReferenceChallengeEvaluationPolicy;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Proves that C4's attempt/comparison contract composes correctly with C5 data-driven content:
 * a {@link ChallengeDefinition} loaded through {@link ChallengeContentLoader} feeds two {@link
 * ChallengeAttempt} instances with different outcomes, and {@link ChallengeAttemptComparator}
 * ranks them deterministically without depending on attempt identity.
 *
 * <p>This is not a new C4 feature: it reuses the existing attempt/comparison types exactly as
 * {@code ChallengeAttemptComparatorTest} does, but derives the challenge definition from the C5
 * content path instead of a hand-built fixture.
 */
class ContentDrivenAttemptComparisonTest {

    private static final String LOADED_CHALLENGE = """
            {
              "schemaVersion": "challenge-content:v1",
              "identity": {"id": "content-driven-comparison", "version": "1"},
              "floor": {"width": 10, "height": 8},
              "startingBudget": 40000,
              "workload": {"productReference": "widget", "requiredQuantity": 20},
              "availableEquipment": ["equipment.cutter"],
              "deadline": 400,
              "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"}
            }
            """;

    @Test
    void loadedContentFeedsDeterministicAttemptComparison() {
        ChallengeDefinition challenge = loadDefinition();

        // earlyCheaper: margin 50, unused budget 30,000, score 30,051.
        // lateCostlier: margin 10, unused budget 28,000, score 28,011 -- earlier completion and
        // lower construction cost score higher under the shared policy's own score ordering.
        ChallengeAttempt earlyCheaper = attempt(challenge, 350L, 10_000L);
        ChallengeAttempt lateCostlier = attempt(challenge, 390L, 12_000L);

        AttemptComparison first = ChallengeAttemptComparator.compare(earlyCheaper, lateCostlier).comparison();
        AttemptComparison second = ChallengeAttemptComparator.compare(earlyCheaper, lateCostlier).comparison();

        assertEquals(first, second);
        assertEquals(AttemptComparisonWinner.FIRST, first.winner());
        assertTrue(first.firstSuccessful());
        assertTrue(first.secondSuccessful());
    }

    @Test
    void attemptIdentityDoesNotInfluenceContentDrivenComparison() {
        ChallengeDefinition challenge = loadDefinition();

        ChallengeAttempt earlyCheaperA = attempt(challenge, 350L, 10_000L);
        ChallengeAttempt lateCostlierA = attempt(challenge, 390L, 12_000L);
        ChallengeAttempt earlyCheaperB = attempt(challenge, 350L, 10_000L);
        ChallengeAttempt lateCostlierB = attempt(challenge, 390L, 12_000L);

        assertNotEquals(earlyCheaperA.id(), earlyCheaperB.id());
        assertNotEquals(lateCostlierA.id(), lateCostlierB.id());
        assertEquals(ChallengeAttemptComparator.compare(earlyCheaperA, lateCostlierA).comparison(),
                ChallengeAttemptComparator.compare(earlyCheaperB, lateCostlierB).comparison());
    }

    private static ChallengeDefinition loadDefinition() {
        ChallengeContentLoadResult result = ChallengeContentLoader.load(LOADED_CHALLENGE);
        assertTrue(result.isSuccess(), () -> "expected success, got issues: " + result.issues());
        return result.definition();
    }

    private static ChallengeAttempt attempt(ChallengeDefinition challenge, long completionTick, long cost) {
        EvaluationProvenance provenance =
                new EvaluationProvenance("model.content-driven:v1", "run.content-driven:historical-1");
        ChallengeEvaluationInput input = new ChallengeEvaluationInput(
                challenge, provenance, new AuthoritativeOutcomeFacts(true, completionTick), cost);
        ChallengeEvaluationResult result = ReferenceChallengeEvaluationPolicy.evaluate(input);

        CandidateDraftSnapshot snapshot = new CandidateDraftSnapshot(List.of(new PlacedEquipment(
                new EquipmentOccurrenceId("cutter-1"), challenge.availableEquipment().get(0),
                new GridPlacement(0, 0))));
        DraftEconomics economics = new DraftEconomics(challenge.startingBudget(), cost, challenge.startingBudget() - cost);
        return ChallengeAttempt.record(snapshot, economics, result);
    }
}
