package com.arcogine.consumer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelArtifact;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.governance.ControlledRevision;
import com.arcogine.governance.ControlledRevisionAuthority;
import com.arcogine.governance.RevisionProvenance;
import com.arcogine.governance.RevisionRecorder;
import com.arcogine.governance.SemanticArtifact;
import com.arcogine.storage.ArcogineStorage;
import com.arcogine.storage.BuiltInStorage;
import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** An external package consumes only Storage and Governance contracts. */
class StoragePublicContractTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void realFactoryRevisionIsAcceptedAndResolvedThroughPublicContracts() {
        MachineId machine = new MachineId(1);
        FactoryModelVersion version = FactoryModelPublisher.publish(new FactoryModel(
                List.of(new ConfiguredResource(machine, "Mill", 1, 125.5, 2)),
                List.of(new OperationDefinition(100, "Routing", List.of(
                        new OperationStepDefinition(1, "Machine", Set.of(machine), 5)))),
                List.of(new ProductDefinition(new ProductId(10), "Widget", 100))));
        ArcogineStorage storage = BuiltInStorage.open(
                temporaryDirectory.resolve("history"), FactoryModelArtifact.verifier());
        ControlledRevisionAuthority revisions = storage.controlledRevisions();
        ControlledRevision candidate = new ControlledRevision(
                ControlledRevisionId.generate(),
                version.fingerprint(),
                List.of(),
                new RevisionProvenance(Instant.EPOCH, new RevisionRecorder("test", "operator")));

        ControlledRevision accepted = revisions.accept(candidate, new SemanticArtifact(
                version.fingerprint(), FactoryModelArtifact.encode(version)));
        assertEquals(accepted, revisions.resolve(accepted.id()).revision());
        assertArrayEquals(FactoryModelArtifact.encode(version),
                revisions.resolve(accepted.id()).artifact().canonicalBytes());
    }
}
