package com.arcogine.governance;

import com.arcogine.types.ModelFingerprint;

/** Domain adapter that validates canonical artifact bytes and recomputes their semantic fingerprint. */
public interface SemanticArtifactVerifier {

    boolean supports(ModelFingerprint fingerprint);

    ModelFingerprint fingerprint(byte[] canonicalBytes);

    /**
     * An opaque token naming the exact definition build this verifier checks artifacts against.
     *
     * <p>A content fingerprint does not identify the exact development revision of its canonical
     * definition. A proving store records this token when it is created and refuses to reopen under
     * any other, so material written under one definition build is never interpreted under another.
     * The token is build context, never semantic identity: it does not participate in fingerprints,
     * revisions or comparison.
     */
    String definitionBinding();
}
