package com.arcogine.storage;

import com.arcogine.governance.SemanticArtifactVerifier;
import java.nio.file.Path;
import java.time.Clock;

/** Opens Arcogine's built-in local storage implementation. */
public final class BuiltInStorage {

    private BuiltInStorage() {}

    /**
     * Opens an owned root or creates one at an absent location. An existing foreign, incomplete, or
     * differently definition-bound root is refused without adoption or deletion. Acceptance commits
     * per operation; the returned object has no close-time commit step.
     *
     * @throws com.arcogine.governance.GovernanceHistoryException if the location is unsupported or
     *     its persistent state cannot be verified
     */
    public static ArcogineStorage open(Path root, SemanticArtifactVerifier verifier) {
        return open(root, verifier, Clock.systemUTC());
    }

    static ArcogineStorage open(Path root, SemanticArtifactVerifier verifier, Clock clock) {
        return FileArcogineStorage.open(root, verifier, clock);
    }
}
