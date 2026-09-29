package com.arcogine.storage;

import com.arcogine.governance.ControlledRevisionAuthority;

/** Arcogine-owned storage capabilities exposed through their semantic contracts. */
public interface ArcogineStorage {

    /** The authoritative acceptance and exact historical-resolution port owned by Governance. */
    ControlledRevisionAuthority controlledRevisions();
}
