package com.arcogine.challenge.evaluation;

import com.arcogine.challenge.ChallengeDefinition;
import com.arcogine.challenge.EvaluationPolicyIdentity;
import java.util.ArrayList;
import java.util.List;

/**
 * The first, versioned policy for the reference vertical slice.
 *
 * <p>Policy {@value #POLICY_ID} v{@value #POLICY_VERSION} succeeds only when the supplied
 * authoritative facts say the fixed contract completed on or before the challenge deadline and
 * the supplied game-owned construction cost is within budget. A successful score is {@code 1 +
 * deadlineMarginTicks + unusedBudgetCredits}; an unsuccessful result scores zero. The one-point
 * completion bonus makes exact-deadline, exact-budget completion distinguishable from failure.
 * Changing any of these observable rules requires a new policy version.
 */
public final class ReferenceChallengeEvaluationPolicy {

    public static final String POLICY_ID = "policy.contract-completion";
    public static final String POLICY_VERSION = "1";
    private static final EvaluationPolicyIdentity IDENTITY =
            new EvaluationPolicyIdentity(POLICY_ID, POLICY_VERSION);

    private ReferenceChallengeEvaluationPolicy() {}

    /** Evaluates explicit facts without accessing Arcogine runtime or factory-model types. */
    public static ChallengeEvaluationResult evaluate(ChallengeEvaluationInput input) {
        if (input == null) {
            throw new NullPointerException("input");
        }
        ChallengeDefinition challenge = input.challenge();
        validateChallenge(challenge);
        if (!IDENTITY.equals(challenge.evaluationPolicy())) {
            throw new IllegalArgumentException("challenge evaluation policy is not supported: "
                    + challenge.evaluationPolicy());
        }

        long unusedBudget = Math.subtractExact(
                challenge.startingBudget(), input.committedConstructionCostCredits());
        Long deadlineMargin = input.outcomeFacts().contractCompleted()
                ? Math.subtractExact(challenge.deadline(), input.outcomeFacts().completionTick())
                : null;
        List<ChallengeEvaluationIssue> issues = new ArrayList<>();
        addContractIssue(input.outcomeFacts(), issues);
        addDeadlineIssue(deadlineMargin, issues);
        addBudgetIssue(unusedBudget, issues);

        boolean successful = issues.isEmpty();
        long score = successful ? Math.addExact(1L, Math.addExact(deadlineMargin, unusedBudget)) : 0L;
        return new ChallengeEvaluationResult(challenge.identity(), challenge.evaluationPolicy(),
                input.provenance(), successful, issues, deadlineMargin, unusedBudget, score);
    }

    private static void validateChallenge(ChallengeDefinition challenge) {
        if (challenge.startingBudget() < 0) {
            throw new IllegalArgumentException("challenge startingBudget must be non-negative");
        }
        if (challenge.deadline() <= 0) {
            throw new IllegalArgumentException("challenge deadline must be positive");
        }
    }

    private static void addContractIssue(AuthoritativeOutcomeFacts outcome,
            List<ChallengeEvaluationIssue> issues) {
        if (!outcome.contractCompleted()) {
            issues.add(issue("challenge.contract.incomplete", "outcomeFacts.contractCompleted",
                    "authoritative outcome does not report fixed contract completion"));
        }
    }

    private static void addDeadlineIssue(Long deadlineMargin, List<ChallengeEvaluationIssue> issues) {
        if (deadlineMargin != null && deadlineMargin < 0) {
            issues.add(issue("challenge.deadline.missed", "outcomeFacts.completionTick",
                    "authoritative completion tick exceeds challenge deadline"));
        }
    }

    private static void addBudgetIssue(long unusedBudget, List<ChallengeEvaluationIssue> issues) {
        if (unusedBudget < 0) {
            issues.add(issue("challenge.budget.exceeded", "committedConstructionCostCredits",
                    "committed construction cost exceeds challenge starting budget"));
        }
    }

    private static ChallengeEvaluationIssue issue(String code, String path, String message) {
        return new ChallengeEvaluationIssue(code, path, message);
    }
}
