package com.arcogine.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ModelFingerprintTest {

    @Test
    void rendersCanonicalExternalForm() {
        ModelFingerprint fingerprint = new ModelFingerprint("factory-model", "sha256", "a".repeat(64));

        assertEquals("factory-model:sha256:" + "a".repeat(64), fingerprint.toString());
    }

    @Test
    void rejectsBlankComponents() {
        assertThrows(NullPointerException.class, () -> new ModelFingerprint(null, "sha256", "a".repeat(64)));
        assertThrows(IllegalArgumentException.class, () -> new ModelFingerprint("factory-model", " ", "a".repeat(64)));
        assertThrows(IllegalArgumentException.class, () -> new ModelFingerprint("factory-model", "sha256", " "));
    }

    @Test
    void rejectsMalformedSha256Digest() {
        assertThrows(IllegalArgumentException.class, () -> new ModelFingerprint("factory-model", "sha256", "A".repeat(64)));
        assertThrows(IllegalArgumentException.class, () -> new ModelFingerprint("factory-model", "sha256", "a".repeat(63)));
    }
}
