package com.arcogine.governance.evidence;

import java.util.List;
import java.util.Optional;

/** A bounded authority for resolving immutable evidence references in headless use. */
public interface EvidenceReferenceAuthority {

    /** Records a reference, or returns the existing equal reference after verifying no rebind. */
    EvidenceReference record(EvidenceReference reference);

    Optional<EvidenceReference> find(String sourceIdentity, String revisionIdentity);

    List<EvidenceReference> references();
}
