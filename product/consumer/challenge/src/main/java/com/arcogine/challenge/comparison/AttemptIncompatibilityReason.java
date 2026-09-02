package com.arcogine.challenge.comparison;

/** A stable, machine-readable reason two attempts cannot be meaningfully compared. */
public record AttemptIncompatibilityReason(String code, String message) {

    public AttemptIncompatibilityReason {
        if (code == null) {
            throw new NullPointerException("code");
        }
        if (message == null) {
            throw new NullPointerException("message");
        }
    }
}
