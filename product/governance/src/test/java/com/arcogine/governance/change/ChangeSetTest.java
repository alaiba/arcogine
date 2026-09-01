package com.arcogine.governance.change;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.ModelFingerprint;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Pure value-contract tests for {@link ChangeSet}, independent of any domain or persistence. */
class ChangeSetTest {

    private static final ModelFingerprint BASE_FP = fingerprint("aa");
    private static final ModelFingerprint CANDIDATE_FP = fingerprint("bb");

    @Test
    void semanticChangesAreStoredInDeterministicOrderRegardlessOfInputOrder() {
        ChangedEntityRef machineA = new ChangedEntityRef("factory.resource", "1", "A");
        ChangedEntityRef machineB = new ChangedEntityRef("factory.resource", "2", "B");
        SemanticChange addedB =
                new SemanticChange(SemanticChangeKind.ENTITY_ADDED, machineB, "added");
        SemanticChange modifiedA =
                new SemanticChange(SemanticChangeKind.ENTITY_MODIFIED, machineA, "capacity changed");

        ChangeSet forward =
                new ChangeSet(
                        revisionId(1),
                        BASE_FP,
                        CANDIDATE_FP,
                        revisionId(2),
                        List.of(addedB, modifiedA),
                        null,
                        ChangeProvenance.of("test", "reorder check"));
        ChangeSet reversed =
                new ChangeSet(
                        revisionId(1),
                        BASE_FP,
                        CANDIDATE_FP,
                        revisionId(2),
                        List.of(modifiedA, addedB),
                        null,
                        ChangeProvenance.of("test", "reorder check"));

        assertEquals(forward.semanticChanges(), reversed.semanticChanges());
        assertEquals(List.of(modifiedA, addedB), forward.semanticChanges());
    }

    @Test
    void impactScopeIsDerivedFromChangedEntitiesAndDeduplicated() {
        ChangedEntityRef machineA = new ChangedEntityRef("factory.resource", "1", "A");
        SemanticChange first =
                new SemanticChange(SemanticChangeKind.ENTITY_MODIFIED, machineA, "name changed");
        SemanticChange second =
                new SemanticChange(SemanticChangeKind.ENTITY_MODIFIED, machineA, "capacity changed");

        ChangeSet changeSet =
                new ChangeSet(
                        revisionId(1),
                        BASE_FP,
                        CANDIDATE_FP,
                        revisionId(2),
                        List.of(first, second),
                        null,
                        ChangeProvenance.of("test", "dedup check"));

        assertEquals(List.of(machineA), changeSet.impactScope().affectedEntities());
    }

    @Test
    void noSemanticChangesIsAValidNoOpTransition() {
        ChangeSet changeSet =
                new ChangeSet(
                        revisionId(1),
                        BASE_FP,
                        BASE_FP,
                        revisionId(2),
                        List.of(),
                        null,
                        ChangeProvenance.of("test", "rollback"));

        assertTrue(changeSet.isSemanticNoOp());
        assertTrue(changeSet.impactScope().isEmpty());
        assertFalse(changeSet.baseRevisionId().equals(changeSet.resultingRevisionId()));
    }

    @Test
    void resultingRevisionIdIsAbsentForAnUnpersistedCandidate() {
        ChangeSet changeSet =
                new ChangeSet(
                        revisionId(1),
                        BASE_FP,
                        CANDIDATE_FP,
                        null,
                        List.of(),
                        null,
                        ChangeProvenance.of("test", "candidate not yet accepted"));

        assertTrue(changeSet.resultingRevisionIdOptional().isEmpty());
    }

    @Test
    void externalChangeReferenceIsRetainedAsAssociationNotIdentity() {
        ExternalChangeReference jira = new ExternalChangeReference("jira", "ARC-123");
        ChangeProvenance provenance = ChangeProvenance.of("operator", "capacity upgrade", jira);

        ChangeSet changeSet =
                new ChangeSet(
                        revisionId(1),
                        BASE_FP,
                        CANDIDATE_FP,
                        revisionId(2),
                        List.of(),
                        null,
                        provenance);

        assertEquals(jira, changeSet.provenance().externalReferenceOptional().orElseThrow());
        // The reference does not participate in ChangeSet/entity identity at all -- it is only
        // reachable through provenance, never compared for equality of the changed entities.
    }

    @Test
    void minimumContractHasNoAuthorizationDeploymentOrEvidenceFields() {
        for (var recordComponent : ChangeSet.class.getRecordComponents()) {
            String name = recordComponent.getName();
            assertFalse(name.toLowerCase().contains("approv"), name);
            assertFalse(name.toLowerCase().contains("deploy"), name);
            assertFalse(name.toLowerCase().contains("evidence"), name);
            assertFalse(name.toLowerCase().contains("conform"), name);
        }
    }

    @Test
    void rejectsNullRequiredFields() {
        assertThrows(
                NullPointerException.class,
                () ->
                        new ChangeSet(
                                null, BASE_FP, CANDIDATE_FP, null, List.of(), null,
                                ChangeProvenance.of("t", "r")));
        assertThrows(
                NullPointerException.class,
                () ->
                        new ChangeSet(
                                revisionId(1), null, CANDIDATE_FP, null, List.of(), null,
                                ChangeProvenance.of("t", "r")));
    }

    private static ModelFingerprint fingerprint(String suffix) {
        return new ModelFingerprint("test-model", "v1", "sha256", "0".repeat(62) + suffix);
    }

    private static ControlledRevisionId revisionId(int suffix) {
        return ControlledRevisionId.parse(
                "00000000-0000-4000-8000-" + String.format("%012d", suffix));
    }
}
