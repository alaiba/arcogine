package com.arcogine.challenge.admissibility;

/** A stable, game-rule diagnostic explaining why a candidate draft is rejected. */
public record CandidateAdmissibilityIssue(String code, String path, String message) {

    public CandidateAdmissibilityIssue {
        if (code == null) {
            throw new NullPointerException("code");
        }
        if (path == null) {
            throw new NullPointerException("path");
        }
        if (message == null) {
            throw new NullPointerException("message");
        }
    }

    @Override
    public String toString() {
        return path + " [" + code + "]: " + message;
    }
}