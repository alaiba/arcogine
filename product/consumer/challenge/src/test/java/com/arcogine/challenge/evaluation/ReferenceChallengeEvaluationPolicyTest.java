package com.arcogine.challenge.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.challenge.ChallengeDefinition;
import com.arcogine.challenge.ChallengeFixtures;
import com.arcogine.challenge.EvaluationPolicyIdentity;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReferenceChallengeEvaluationPolicyTest {

    @Test
    void completedOnTimeAndWithinBudgetIsSuccessfulAndDeterministic() {
        ChallengeEvaluationInput input = input(true, 350L, 10_000L);

        ChallengeEvaluationResult first = evaluate(input);
        ChallengeEvaluationResult second = evaluate(input);

        assertTrue(first.successful());
        assertEquals(first, second);
        assertEquals(50L, first.deadlineMarginTicks());
        assertEquals(30_000L, first.unusedBudgetCredits());
        assertEquals(30_051L, first.score());
        assertEquals(ChallengeFixtures.referenceChallenge().identity(), first.challengeIdentity());
        assertEquals(ChallengeFixtures.referenceChallenge().evaluationPolicy(), first.evaluationPolicy());
        assertEquals("model.factory-basics:v1", first.provenance().publishedModelReference());
    }

    @Test
    void consumesSyntheticAuthoritativeFactsWithoutFactoryRuntime() throws ClassNotFoundException {
        ChallengeEvaluationResult result = evaluate(input(true, 400L, 40_000L));

        assertTrue(result.successful());
        assertEquals(0L, result.deadlineMarginTicks());
        assertEquals(0L, result.unusedBudgetCredits());
        assertEquals(1L, result.score());
        assertThrows(ClassNotFoundException.class,
                () -> Class.forName("com.arcogine.factory.process.FactoryRuntime"));
    }

    @Test
    void incompleteContractCannotBecomeSuccessfulThroughGoodOtherFacts() {
        ChallengeEvaluationResult result = evaluate(input(false, null, 0L));

        assertFalse(result.successful());
        assertEquals(List.of("challenge.contract.incomplete"), codes(result));
        assertEquals(null, result.deadlineMarginTicks());
        assertEquals(40_000L, result.unusedBudgetCredits());
        assertEquals(0L, result.score());
    }

    @Test
    void deadlineAndBudgetFailuresAreExplainedInStableRuleOrder() {
        ChallengeEvaluationResult result = evaluate(input(true, 401L, 40_001L));

        assertFalse(result.successful());
        assertEquals(-1L, result.deadlineMarginTicks());
        assertEquals(-1L, result.unusedBudgetCredits());
        assertEquals(List.of("challenge.deadline.missed", "challenge.budget.exceeded"), codes(result));
    }

    @Test
    void deadlineMarginUsesSuppliedCompletionFactRatherThanProductionReconstruction() {
        assertEquals(1L, evaluate(input(true, 399L, 0L)).deadlineMarginTicks());
        assertEquals(-1L, evaluate(input(true, 401L, 0L)).deadlineMarginTicks());
    }

    @Test
    void historicalFixtureReproducesRecordedOutput() {
        ChallengeEvaluationResult result = evaluate(input(true, 375L, 12_000L));

        assertEquals(25L, result.deadlineMarginTicks());
        assertEquals(28_000L, result.unusedBudgetCredits());
        assertEquals(28_026L, result.score());
        assertTrue(result.successful());
    }

    @Test
    void resultIssuesAreImmutableAndOrdered() {
        ChallengeEvaluationResult result = evaluate(input(true, 401L, 40_001L));

        assertThrows(UnsupportedOperationException.class, () -> result.issues().clear());
        assertEquals(List.of("challenge.deadline.missed", "challenge.budget.exceeded"), codes(result));
    }

    @Test
    void inputAndOutcomeInvariantsRejectMalformedFacts() {
        assertThrows(IllegalArgumentException.class, () -> new AuthoritativeOutcomeFacts(true, null));
        assertThrows(IllegalArgumentException.class, () -> new AuthoritativeOutcomeFacts(false, 1L));
        assertThrows(IllegalArgumentException.class, () -> new AuthoritativeOutcomeFacts(true, -1L));
        assertThrows(IllegalArgumentException.class, () -> new ChallengeEvaluationInput(
                ChallengeFixtures.referenceChallenge(), provenance(), new AuthoritativeOutcomeFacts(true, 1L), -1L));
        assertThrows(IllegalArgumentException.class, () -> new EvaluationProvenance(" ", "run-1"));
    }

    @Test
    void unsupportedPolicyAndMalformedChallengeAreRejected() {
        ChallengeDefinition challenge = ChallengeFixtures.referenceChallenge();
        ChallengeDefinition unsupported = new ChallengeDefinition(challenge.identity(), challenge.floor(),
                challenge.startingBudget(), challenge.workload(), challenge.availableEquipment(),
                challenge.deadline(), new EvaluationPolicyIdentity("policy.other", "1"),
                challenge.catalogueIdentity(), challenge.catalogueSemanticFingerprint());
        ChallengeDefinition invalidDeadline = new ChallengeDefinition(challenge.identity(), challenge.floor(),
                challenge.startingBudget(), challenge.workload(), challenge.availableEquipment(), 0L,
                challenge.evaluationPolicy(), challenge.catalogueIdentity(), challenge.catalogueSemanticFingerprint());

        assertThrows(IllegalArgumentException.class, () -> evaluate(new ChallengeEvaluationInput(
                unsupported, provenance(), new AuthoritativeOutcomeFacts(true, 1L), 0L)));
        assertThrows(IllegalArgumentException.class, () -> evaluate(new ChallengeEvaluationInput(
                invalidDeadline, provenance(), new AuthoritativeOutcomeFacts(true, 1L), 0L)));
    }

    @Test
    void resultInvariantRejectsMutableInconsistentShape() {
        List<ChallengeEvaluationIssue> issues = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> new ChallengeEvaluationResult(
                ChallengeFixtures.referenceChallenge().identity(),
                ChallengeFixtures.referenceChallenge().evaluationPolicy(), provenance(), false, issues,
                null, 0L, 0L));
    }

    private static ChallengeEvaluationResult evaluate(ChallengeEvaluationInput input) {
        return ReferenceChallengeEvaluationPolicy.evaluate(input);
    }

    private static ChallengeEvaluationInput input(boolean complete, Long completionTick, long cost) {
        return new ChallengeEvaluationInput(ChallengeFixtures.referenceChallenge(), provenance(),
                new AuthoritativeOutcomeFacts(complete, completionTick), cost);
    }

    private static EvaluationProvenance provenance() {
        return new EvaluationProvenance("model.factory-basics:v1", "run.factory-basics:historical-1");
    }

    private static List<String> codes(ChallengeEvaluationResult result) {
        return result.issues().stream().map(ChallengeEvaluationIssue::code).toList();
    }
}
