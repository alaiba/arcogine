package com.arcogine.challenge.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.challenge.EvaluationPolicyIdentity;
import com.arcogine.challenge.evaluation.ReferenceChallengeEvaluationPolicy;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class EvaluationPolicyResolverTest {

    private static final EvaluationPolicyIdentity SUPPORTED = new EvaluationPolicyIdentity(
            ReferenceChallengeEvaluationPolicy.POLICY_ID, ReferenceChallengeEvaluationPolicy.POLICY_VERSION);

    @Test
    void supportsTheReferencePolicyIdentity() {
        assertTrue(EvaluationPolicyResolver.isSupported(SUPPORTED));
        assertEquals(Optional.empty(), EvaluationPolicyResolver.resolve(SUPPORTED));
    }

    @Test
    void rejectsAnUnknownPolicyId() {
        EvaluationPolicyIdentity unknown = new EvaluationPolicyIdentity("policy.unknown", "1");

        assertFalse(EvaluationPolicyResolver.isSupported(unknown));
        Optional<ChallengeContentIssue> issue = EvaluationPolicyResolver.resolve(unknown, "evaluationPolicy");
        assertTrue(issue.isPresent());
        assertEquals("evaluationPolicy.unsupported", issue.get().code());
        assertEquals("evaluationPolicy", issue.get().path());
    }

    @Test
    void rejectsAKnownPolicyIdWithAnUnknownVersion() {
        EvaluationPolicyIdentity wrongVersion =
                new EvaluationPolicyIdentity(ReferenceChallengeEvaluationPolicy.POLICY_ID, "99");

        assertFalse(EvaluationPolicyResolver.isSupported(wrongVersion));
        assertTrue(EvaluationPolicyResolver.resolve(wrongVersion).isPresent());
    }
}
