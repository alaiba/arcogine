package com.arcogine.challenge.content;

import com.arcogine.challenge.EvaluationPolicyIdentity;
import com.arcogine.challenge.evaluation.ReferenceChallengeEvaluationPolicy;
import java.util.Optional;
import java.util.Set;

/**
 * A small immutable static registry of evaluation policy (id, version) pairs this build knows how
 * to evaluate.
 *
 * <p>This exists so untrusted content that names an unknown or unsupported {@link
 * EvaluationPolicyIdentity} fails with a deterministic, structured {@link ChallengeContentIssue}
 * rather than reaching {@code ReferenceChallengeEvaluationPolicy.evaluate} and throwing an
 * uncaught {@code IllegalArgumentException} later, at evaluation time instead of load time.
 *
 * <p>It does not evaluate anything itself; it only answers "is this policy identity supported by
 * this build". Adding a new evaluation policy version means adding both the new policy
 * implementation and a new entry here -- an unregistered policy stays unsupported even if a class
 * implementing it exists.
 */
public final class EvaluationPolicyResolver {

    private static final Set<EvaluationPolicyIdentity> SUPPORTED = Set.of(new EvaluationPolicyIdentity(
            ReferenceChallengeEvaluationPolicy.POLICY_ID, ReferenceChallengeEvaluationPolicy.POLICY_VERSION));

    private EvaluationPolicyResolver() {}

    /** Returns {@code true} when {@code identity} names a policy this build can evaluate. */
    public static boolean isSupported(EvaluationPolicyIdentity identity) {
        if (identity == null) {
            throw new NullPointerException("identity");
        }
        return SUPPORTED.contains(identity);
    }

    /**
     * Resolves {@code identity}, returning an empty {@link Optional} when it is supported, or a
     * structured {@link ChallengeContentIssue} (code {@code "evaluationPolicy.unsupported"}) at
     * {@code path} when it is not.
     */
    public static Optional<ChallengeContentIssue> resolve(EvaluationPolicyIdentity identity, String path) {
        if (isSupported(identity)) {
            return Optional.empty();
        }
        return Optional.of(new ChallengeContentIssue(
                "evaluationPolicy.unsupported",
                path,
                "unsupported evaluation policy: " + identity.id() + " v" + identity.version()));
    }

    /** Convenience overload resolving at the conventional {@code "evaluationPolicy"} path. */
    public static Optional<ChallengeContentIssue> resolve(EvaluationPolicyIdentity identity) {
        return resolve(identity, "evaluationPolicy");
    }
}
