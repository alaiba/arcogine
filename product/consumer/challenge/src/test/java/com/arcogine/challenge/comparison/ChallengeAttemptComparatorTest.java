package com.arcogine.challenge.comparison;

import static com.arcogine.challenge.attempt.ChallengeAttemptFixtures.economics;
import static com.arcogine.challenge.attempt.ChallengeAttemptFixtures.evaluate;
import static com.arcogine.challenge.attempt.ChallengeAttemptFixtures.provenance;
import static com.arcogine.challenge.attempt.ChallengeAttemptFixtures.snapshot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.challenge.ChallengeDefinition;
import com.arcogine.challenge.ChallengeFixtures;
import com.arcogine.challenge.ChallengeIdentity;
import com.arcogine.challenge.EvaluationPolicyIdentity;
import com.arcogine.challenge.attempt.ChallengeAttempt;
import com.arcogine.challenge.economics.DraftEconomics;
import com.arcogine.challenge.evaluation.AuthoritativeOutcomeFacts;
import com.arcogine.challenge.evaluation.ChallengeEvaluationInput;
import com.arcogine.challenge.evaluation.ChallengeEvaluationResult;
import com.arcogine.challenge.evaluation.ReferenceChallengeEvaluationPolicy;
import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChallengeAttemptComparatorTest {

    @Test
    void compatibleAttemptsProduceDeterministicComparison() {
        ChallengeAttempt worse = attempt(true, 350L, 10_000L);
        ChallengeAttempt better = attempt(true, 390L, 12_000L);

        AttemptComparisonResult first = ChallengeAttemptComparator.compare(worse, better);
        AttemptComparisonResult second = ChallengeAttemptComparator.compare(worse, better);

        assertTrue(first.compatibility().comparable());
        assertEquals(first.comparison(), second.comparison());
    }

    @Test
    void comparisonReportsScoreOrOutcomeDeltaFromExistingEvaluationFacts() {
        // first: margin 50, unused budget 30,000, score 30,051.
        // second: margin 10, unused budget 28,000, score 28,011 -- later completion and higher
        // construction cost, so first wins under the shared policy's own score ordering.
        ChallengeAttempt worse = attempt(true, 350L, 10_000L);
        ChallengeAttempt better = attempt(true, 390L, 12_000L);

        AttemptComparisonResult result = ChallengeAttemptComparator.compare(worse, better);

        AttemptComparison comparison = result.comparison();
        assertEquals(BigInteger.valueOf(-2_040L), comparison.scoreDelta());
        assertEquals(BigInteger.valueOf(-40L), comparison.deadlineMarginDeltaTicks());
        assertEquals(BigInteger.valueOf(-2_000L), comparison.unusedBudgetDeltaCredits());
        assertEquals(BigInteger.valueOf(2_000L), comparison.constructionCostDeltaCredits());
        assertEquals(AttemptComparisonWinner.FIRST, comparison.winner());
        assertTrue(comparison.firstSuccessful());
        assertTrue(comparison.secondSuccessful());
    }

    @Test
    void identicalAttemptsCompareAsTies() {
        ChallengeAttempt first = attempt(true, 350L, 10_000L);
        ChallengeAttempt second = attempt(true, 350L, 10_000L);

        AttemptComparison comparison = ChallengeAttemptComparator.compare(first, second).comparison();

        assertEquals(BigInteger.ZERO, comparison.scoreDelta());
        assertEquals(BigInteger.ZERO, comparison.deadlineMarginDeltaTicks());
        assertEquals(BigInteger.ZERO, comparison.unusedBudgetDeltaCredits());
        assertEquals(BigInteger.ZERO, comparison.constructionCostDeltaCredits());
        assertEquals(AttemptComparisonWinner.TIE, comparison.winner());
    }

    @Test
    void constructionCostDifferenceIsExactAcrossTheFullLongRange() {
        ChallengeAttempt min = extremeAttempt(0L, 0L, Long.MIN_VALUE, BigInteger.ONE);
        ChallengeAttempt max = extremeAttempt(0L, 0L, Long.MAX_VALUE, BigInteger.ONE);

        AttemptComparison up = ChallengeAttemptComparator.compare(min, max).comparison();
        AttemptComparison down = ChallengeAttemptComparator.compare(max, min).comparison();

        assertEquals(new BigInteger("18446744073709551615"), up.constructionCostDeltaCredits());
        assertEquals(new BigInteger("-18446744073709551615"), down.constructionCostDeltaCredits());
        assertEquals(AttemptComparisonWinner.TIE, up.winner());
        assertEquals(AttemptComparisonWinner.TIE, down.winner());
    }

    @Test
    void unusedBudgetDifferenceIsExactAcrossTheFullLongRange() {
        ChallengeAttempt min = extremeAttempt(0L, Long.MIN_VALUE, 0L, BigInteger.ONE);
        ChallengeAttempt max = extremeAttempt(0L, Long.MAX_VALUE, 0L, BigInteger.ONE);

        assertEquals(new BigInteger("18446744073709551615"),
                ChallengeAttemptComparator.compare(min, max).comparison().unusedBudgetDeltaCredits());
        assertEquals(new BigInteger("-18446744073709551615"),
                ChallengeAttemptComparator.compare(max, min).comparison().unusedBudgetDeltaCredits());
    }

    @Test
    void deadlineMarginDifferenceIsExactAcrossTheFullLongRange() {
        ChallengeAttempt min = extremeAttempt(Long.MIN_VALUE, 0L, 0L, BigInteger.ONE);
        ChallengeAttempt max = extremeAttempt(Long.MAX_VALUE, 0L, 0L, BigInteger.ONE);

        assertEquals(new BigInteger("18446744073709551615"),
                ChallengeAttemptComparator.compare(min, max).comparison().deadlineMarginDeltaTicks());
        assertEquals(new BigInteger("-18446744073709551615"),
                ChallengeAttemptComparator.compare(max, min).comparison().deadlineMarginDeltaTicks());
    }

    @Test
    void equalExtremeValuesDifferByZero() {
        ChallengeAttempt first = extremeAttempt(Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE,
                BigInteger.ONE);
        ChallengeAttempt second = extremeAttempt(Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE,
                BigInteger.ONE);

        AttemptComparison comparison = ChallengeAttemptComparator.compare(first, second).comparison();

        assertEquals(BigInteger.ZERO, comparison.deadlineMarginDeltaTicks());
        assertEquals(BigInteger.ZERO, comparison.unusedBudgetDeltaCredits());
        assertEquals(BigInteger.ZERO, comparison.constructionCostDeltaCredits());
    }

    @Test
    void winnerIsStillDecidedByTheScoreSignAlone() {
        ChallengeAttempt lowScore = extremeAttempt(0L, 0L, Long.MIN_VALUE, BigInteger.ONE);
        ChallengeAttempt highScore = extremeAttempt(0L, 0L, Long.MAX_VALUE, BigInteger.TWO);

        assertEquals(AttemptComparisonWinner.SECOND,
                ChallengeAttemptComparator.compare(lowScore, highScore).comparison().winner());
        assertEquals(AttemptComparisonWinner.FIRST,
                ChallengeAttemptComparator.compare(highScore, lowScore).comparison().winner());
    }

    @Test
    void deadlineMarginDifferenceIsAbsentWhenEitherMarginIsAbsent() {
        ChallengeAttempt successful = attempt(true, 350L, 10_000L);
        ChallengeAttempt unsuccessful = attempt(false, null, 10_000L);
        assertNull(unsuccessful.evaluationResult().deadlineMarginTicks());

        assertNull(ChallengeAttemptComparator.compare(successful, unsuccessful).comparison()
                .deadlineMarginDeltaTicks());
        assertNull(ChallengeAttemptComparator.compare(unsuccessful, successful).comparison()
                .deadlineMarginDeltaTicks());
        assertNull(ChallengeAttemptComparator.compare(unsuccessful, unsuccessful).comparison()
                .deadlineMarginDeltaTicks());
    }

    @Test
    void comparisonRecordRequiresUnusedBudgetAndConstructionCostDifferences() {
        assertThrows(NullPointerException.class, () -> new AttemptComparison(true, true,
                BigInteger.ZERO, null, null, BigInteger.ZERO, AttemptComparisonWinner.TIE));
        assertThrows(NullPointerException.class, () -> new AttemptComparison(true, true,
                BigInteger.ZERO, null, BigInteger.ZERO, null, AttemptComparisonWinner.TIE));
        AttemptComparison withoutMargin = new AttemptComparison(true, true, BigInteger.ZERO, null,
                BigInteger.ZERO, BigInteger.ZERO, AttemptComparisonWinner.TIE);
        assertNull(withoutMargin.deadlineMarginDeltaTicks());
    }

    @Test
    void differentChallengeVersionsAreIncomparable() {
        ChallengeAttempt referenceAttempt = attempt(true, 350L, 10_000L);
        ChallengeAttempt otherChallengeAttempt =
                attemptWithChallengeIdentity(new ChallengeIdentity("challenge.factory-basics", "2"));

        AttemptComparisonResult result =
                ChallengeAttemptComparator.compare(referenceAttempt, otherChallengeAttempt);

        assertFalse(result.compatibility().comparable());
        assertNull(result.comparison());
        assertEquals(List.of("attempt.challenge.mismatch"),
                result.compatibility().reasons().stream()
                        .map(AttemptIncompatibilityReason::code).toList());
    }

    @Test
    void differentEvaluationPolicyVersionsAreIncomparable() {
        ChallengeAttempt referenceAttempt = attempt(true, 350L, 10_000L);
        ChallengeAttempt otherPolicyAttempt = attemptWithEvaluationPolicy(
                new EvaluationPolicyIdentity("policy.contract-completion", "2"));

        AttemptComparisonResult result =
                ChallengeAttemptComparator.compare(referenceAttempt, otherPolicyAttempt);

        assertFalse(result.compatibility().comparable());
        assertNull(result.comparison());
        assertEquals(List.of("attempt.evaluationPolicy.mismatch"),
                result.compatibility().reasons().stream()
                        .map(AttemptIncompatibilityReason::code).toList());
    }

    @Test
    void bothMismatchesAreReportedInStableOrder() {
        ChallengeAttempt referenceAttempt = attempt(true, 350L, 10_000L);
        ChallengeDefinition reference = ChallengeFixtures.referenceChallenge();
        ChallengeDefinition modified = new ChallengeDefinition(
                new ChallengeIdentity("challenge.factory-basics", "2"), reference.floor(),
                reference.startingBudget(), reference.workload(), reference.availableEquipment(),
                reference.deadline(), new EvaluationPolicyIdentity("policy.contract-completion", "2"),
                reference.catalogueIdentity(), reference.catalogueSemanticFingerprint());
        ChallengeEvaluationResult reevaluated = evaluate(true, 350L, 10_000L);
        ChallengeEvaluationResult withBothMismatched = new ChallengeEvaluationResult(
                modified.identity(), modified.evaluationPolicy(), reevaluated.provenance(),
                reevaluated.successful(), reevaluated.issues(), reevaluated.deadlineMarginTicks(),
                reevaluated.unusedBudgetCredits(), reevaluated.score());
        ChallengeAttempt bothMismatchedAttempt =
                ChallengeAttempt.record(snapshot(), economics(10_000L), withBothMismatched);

        AttemptComparisonResult result =
                ChallengeAttemptComparator.compare(referenceAttempt, bothMismatchedAttempt);

        assertEquals(List.of("attempt.challenge.mismatch", "attempt.evaluationPolicy.mismatch"),
                result.compatibility().reasons().stream()
                        .map(AttemptIncompatibilityReason::code).toList());
    }

    @Test
    void attemptIdentityDoesNotInfluenceComparison() {
        ChallengeAttempt worseA = attempt(true, 350L, 10_000L);
        ChallengeAttempt betterA = attempt(true, 390L, 12_000L);
        ChallengeAttempt worseB = attempt(true, 350L, 10_000L);
        ChallengeAttempt betterB = attempt(true, 390L, 12_000L);

        assertNotEquals(worseA.id(), worseB.id());
        assertNotEquals(betterA.id(), betterB.id());
        assertEquals(ChallengeAttemptComparator.compare(worseA, betterA).comparison(),
                ChallengeAttemptComparator.compare(worseB, betterB).comparison());
    }

    @Test
    void comparisonDoesNotMutateEitherAttempt() {
        ChallengeAttempt worse = attempt(true, 350L, 10_000L);
        ChallengeAttempt better = attempt(true, 390L, 12_000L);
        ChallengeEvaluationResult worseResultBefore = worse.evaluationResult();
        ChallengeEvaluationResult betterResultBefore = better.evaluationResult();

        ChallengeAttemptComparator.compare(worse, better);

        assertEquals(worseResultBefore, worse.evaluationResult());
        assertEquals(betterResultBefore, better.evaluationResult());
    }

    @Test
    void rejectsNullAttempts() {
        ChallengeAttempt valid = attempt(true, 350L, 10_000L);
        assertThrows(NullPointerException.class, () -> ChallengeAttemptComparator.compare(null, valid));
        assertThrows(NullPointerException.class, () -> ChallengeAttemptComparator.compare(valid, null));
    }

    private static ChallengeAttempt attempt(boolean complete, Long completionTick, long cost) {
        ChallengeEvaluationResult result = evaluate(complete, completionTick, cost);
        return ChallengeAttempt.record(snapshot(), economics(cost), result);
    }

    /** A compatible successful attempt carrying arbitrary recorded values, as the record accepts. */
    private static ChallengeAttempt extremeAttempt(long marginTicks, long unusedBudget,
            long constructionCost, BigInteger score) {
        ChallengeEvaluationResult base = evaluate(true, 350L, 10_000L);
        ChallengeEvaluationResult result = new ChallengeEvaluationResult(base.challengeIdentity(),
                base.evaluationPolicy(), base.provenance(), true, base.issues(), marginTicks,
                unusedBudget, score);
        return ChallengeAttempt.record(snapshot(),
                new DraftEconomics(40_000L, constructionCost, 0L), result);
    }

    private static ChallengeAttempt attemptWithChallengeIdentity(ChallengeIdentity identity) {
        ChallengeDefinition reference = ChallengeFixtures.referenceChallenge();
        ChallengeDefinition modified = new ChallengeDefinition(identity, reference.floor(),
                reference.startingBudget(), reference.workload(), reference.availableEquipment(),
                reference.deadline(), reference.evaluationPolicy(), reference.catalogueIdentity(),
                reference.catalogueSemanticFingerprint());
        ChallengeEvaluationResult result = ReferenceChallengeEvaluationPolicy.evaluate(
                new ChallengeEvaluationInput(modified, provenance(),
                        new AuthoritativeOutcomeFacts(true, 350L), 10_000L));
        return ChallengeAttempt.record(snapshot(), economics(10_000L), result);
    }

    private static ChallengeAttempt attemptWithEvaluationPolicy(EvaluationPolicyIdentity policy) {
        ChallengeEvaluationResult reevaluated = evaluate(true, 350L, 10_000L);
        ChallengeEvaluationResult withDifferentPolicy = new ChallengeEvaluationResult(
                reevaluated.challengeIdentity(), policy, reevaluated.provenance(),
                reevaluated.successful(), reevaluated.issues(), reevaluated.deadlineMarginTicks(),
                reevaluated.unusedBudgetCredits(), reevaluated.score());
        return ChallengeAttempt.record(snapshot(), economics(10_000L), withDifferentPolicy);
    }
}
