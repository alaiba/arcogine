package com.arcogine.governance.requirement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.governance.change.ChangedEntityRef;
import com.arcogine.governance.change.ChangeProvenance;
import com.arcogine.governance.change.ChangeSet;
import com.arcogine.governance.change.SemanticChange;
import com.arcogine.governance.change.SemanticChangeKind;
import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.ModelFingerprint;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Proves invariant 3: scope is explicit, deterministic, and integrates with the real the change-set/impact-scope capability seam. */
class RequirementScopeTest {

    @Test
    void scopeSelectionIsDeterministic() {
        ChangedEntityRef machineB = new ChangedEntityRef("factory.resource", "2", "B");
        ChangedEntityRef machineA = new ChangedEntityRef("factory.resource", "1", "A");

        RequirementScope forward = RequirementScope.of(List.of(machineB, machineA));
        RequirementScope reversed = RequirementScope.of(List.of(machineA, machineB));

        assertEquals(forward, reversed);
        assertEquals(List.of(machineA, machineB), forward.entities());
    }

    @Test
    void scopeUsesStableChangedEntityIdentity() {
        ChangedEntityRef labeledA = new ChangedEntityRef("factory.resource", "1", "Press A");
        ChangedEntityRef relabeledA = new ChangedEntityRef("factory.resource", "1", "Renamed Press");

        RequirementScope scope = RequirementScope.of(labeledA, relabeledA);

        // Relabeling does not create a second scope entry -- stable identity dedupes, label does not.
        assertEquals(1, scope.entities().size());
    }

    @Test
    void requirementScopeCanMatchExistingG2ImpactScope() {
        ChangedEntityRef pressA = new ChangedEntityRef("factory.resource", "1", "Press A");
        SemanticChange change = new SemanticChange(SemanticChangeKind.ENTITY_MODIFIED, pressA, "capacity changed");
        ChangeSet changeSet = changeSetWith(change);

        RequirementScope scope = RequirementScope.of(pressA);

        assertTrue(scope.intersects(changeSet.impactScope()));
    }

    @Test
    void unrelatedImpactDoesNotSelectRequirement() {
        ChangedEntityRef pressA = new ChangedEntityRef("factory.resource", "1", "Press A");
        ChangedEntityRef unrelatedProduct = new ChangedEntityRef("factory.product", "9", "Widget");
        SemanticChange change =
                new SemanticChange(SemanticChangeKind.ENTITY_MODIFIED, unrelatedProduct, "name changed");
        ChangeSet changeSet = changeSetWith(change);

        RequirementScope scope = RequirementScope.of(pressA);

        assertFalse(scope.intersects(changeSet.impactScope()));
    }

    @Test
    void emptyScopeIsEmptyAndNeverIntersects() {
        RequirementScope fromEmptyCollection = RequirementScope.of(List.of());
        ChangedEntityRef pressA = new ChangedEntityRef("factory.resource", "1", "Press A");
        SemanticChange change = new SemanticChange(SemanticChangeKind.ENTITY_MODIFIED, pressA, "capacity changed");
        ChangeSet changeSet = changeSetWith(change);

        assertEquals(RequirementScope.empty(), fromEmptyCollection);
        assertTrue(fromEmptyCollection.isEmpty());
        assertFalse(RequirementScope.of(pressA).isEmpty());
        assertFalse(fromEmptyCollection.intersects(changeSet.impactScope()));
    }

    @Test
    void equalityAndPresentationAreConsistent() {
        ChangedEntityRef pressA = new ChangedEntityRef("factory.resource", "1", "Press A");
        ChangedEntityRef pressB = new ChangedEntityRef("factory.resource", "2", "Press B");
        RequirementScope scope = RequirementScope.of(pressA);
        RequirementScope sameScope = RequirementScope.of(pressA);
        RequirementScope differentScope = RequirementScope.of(pressB);

        assertEquals(scope, sameScope);
        assertEquals(scope.hashCode(), sameScope.hashCode());
        assertNotEquals(scope, differentScope);
        assertNotEquals(scope, "not a scope");
        assertTrue(scope.toString().contains("RequirementScope"));
    }

    private static ChangeSet changeSetWith(SemanticChange change) {
        return new ChangeSet(
                revisionId(1),
                fingerprint("aa"),
                fingerprint("bb"),
                revisionId(2),
                List.of(change),
                null,
                ChangeProvenance.of("test", "scope matching"));
    }

    private static ModelFingerprint fingerprint(String suffix) {
        return new ModelFingerprint("test-model", "v1", "sha256", "0".repeat(62) + suffix);
    }

    private static ControlledRevisionId revisionId(int suffix) {
        return ControlledRevisionId.parse("00000000-0000-4000-8000-" + String.format("%012d", suffix));
    }
}
