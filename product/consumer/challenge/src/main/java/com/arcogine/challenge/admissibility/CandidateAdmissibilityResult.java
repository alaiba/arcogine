package com.arcogine.challenge.admissibility;

import java.util.List;

/** The immutable, deterministic outcome of candidate admissibility. */
public record CandidateAdmissibilityResult(boolean admitted, List<CandidateAdmissibilityIssue> issues) {

    public CandidateAdmissibilityResult {
        if (issues == null) {
            throw new NullPointerException("issues");
        }
        issues = List.copyOf(issues);
        if (admitted != issues.isEmpty()) {
            throw new IllegalArgumentException("admitted candidates must have no issues");
        }
    }

    public static CandidateAdmissibilityResult success() {
        return new CandidateAdmissibilityResult(true, List.of());
    }

    public static CandidateAdmissibilityResult rejected(List<CandidateAdmissibilityIssue> issues) {
        if (issues == null || issues.isEmpty()) {
            throw new IllegalArgumentException("rejected candidates must have issues");
        }
        return new CandidateAdmissibilityResult(false, issues);
    }
}