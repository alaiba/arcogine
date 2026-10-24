package com.arcogine.governance.conformance;

import com.arcogine.governance.evidence.EvidenceUse;
import com.arcogine.governance.evidence.EvidenceUseRole;
import com.arcogine.types.ControlledRevisionId;

/**
 * Binds a non-comparator use's named target revision to the evaluated subject revision.
 *
 * <p>A use that names a controlled revision targets exactly that revision; equal fingerprints never
 * widen it to another revision or to a subject without one. A use that names no revision is
 * content-level and remains valid for any subject with its fingerprint. Comparator uses keep their
 * own target.
 */
final class EvidenceUseRevisionBinding {

    private EvidenceUseRevisionBinding() {}

    static void requireSubjectRevision(EvidenceUse use, ControlledRevisionId subjectRevision) {
        if (use.role() == EvidenceUseRole.COMPARATOR || use.targetControlledRevision() == null) {
            return;
        }
        if (subjectRevision == null) {
            throw new IllegalArgumentException(
                    "non-comparator evidence use names a controlled revision but the evaluated subject has none");
        }
        if (!use.targetControlledRevision().equals(subjectRevision)) {
            throw new IllegalArgumentException(
                    "non-comparator evidence use names a different controlled revision than the evaluated subject");
        }
    }
}
