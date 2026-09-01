package com.arcogine.governance;

import com.arcogine.types.ModelFingerprint;

/** Domain adapter that validates canonical artifact bytes and recomputes durable semantic identity. */
public interface SemanticArtifactVerifier {

    boolean supports(ModelFingerprint fingerprint);

    ModelFingerprint fingerprint(byte[] canonicalBytes);
}
