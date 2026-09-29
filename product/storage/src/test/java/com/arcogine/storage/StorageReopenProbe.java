package com.arcogine.storage;

import com.arcogine.factory.model.FactoryModelArtifact;
import com.arcogine.governance.ControlledRevisionAuthority;
import com.arcogine.types.ControlledRevisionId;
import java.nio.file.Path;

/** Child JVM entry point for the process-boundary persistence test. */
public final class StorageReopenProbe {

    private StorageReopenProbe() {}

    public static void main(String[] args) {
        ControlledRevisionAuthority authority = BuiltInStorage
                .open(Path.of(args[0]), FactoryModelArtifact.verifier())
                .controlledRevisions();
        ControlledRevisionId id = ControlledRevisionId.parse(args[1]);
        if (authority.revisions().size() != 1 || !authority.resolve(id).revision().id().equals(id)) {
            throw new IllegalStateException("independent process failed to resolve accepted revision");
        }
        System.out.println("REOPEN_OK");
    }
}
