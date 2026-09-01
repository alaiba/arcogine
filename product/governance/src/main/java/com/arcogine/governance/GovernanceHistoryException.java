package com.arcogine.governance;

import java.io.Serial;

/** Typed failure raised by authoritative controlled-revision history operations. */
public final class GovernanceHistoryException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Stable failure categories exposed by the Governance history boundary. */
    public enum Code {
        DUPLICATE_REVISION_ID,
        MISSING_PARENT,
        MISSING_REVISION,
        MISSING_ARTIFACT,
        FINGERPRINT_MISMATCH,
        UNSUPPORTED_ARTIFACT_POLICY,
        STORAGE_INTEGRITY
    }

    private final Code code;

    public GovernanceHistoryException(Code code, String message) {
        super(message);
        this.code = code;
    }

    public GovernanceHistoryException(Code code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public Code code() {
        return code;
    }
}
