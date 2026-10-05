package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arcogine.types.MachineId;
import com.arcogine.types.ModelFingerprint;
import com.arcogine.types.RunId;
import com.arcogine.types.SimTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RuntimeRecordValidationTest {

    private static final RunId RUN = new RunId(UUID.fromString("00000000-0000-4000-8000-000000000001"));
    private static final ModelFingerprint MODEL =
            new ModelFingerprint("factory-model", "sha256", "0".repeat(64));
    private static final SimTime TIME = SimTime.of(1);

    @Test
    void observationMetadataRejectsMissingContextAndNegativeCursor() {
        assertThrows(NullPointerException.class,
                () -> new RuntimeObservationMetadata(null, MODEL, TIME, RuntimeRunState.ACTIVE, 0));
        assertThrows(NullPointerException.class,
                () -> new RuntimeObservationMetadata(RUN, null, TIME, RuntimeRunState.ACTIVE, 0));
        assertThrows(NullPointerException.class,
                () -> new RuntimeObservationMetadata(RUN, MODEL, null, RuntimeRunState.ACTIVE, 0));
        assertThrows(NullPointerException.class,
                () -> new RuntimeObservationMetadata(RUN, MODEL, TIME, null, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new RuntimeObservationMetadata(RUN, MODEL, TIME, RuntimeRunState.ACTIVE, -1));
        assertEquals(0, new RuntimeObservationMetadata(
                RUN, MODEL, TIME, RuntimeRunState.QUIESCENT, 0).latestEventSequence());
    }

    @Test
    void eventEnvelopeRequiresPositiveSequenceAndCopiesAffectedRefs() {
        List<AffectedEntityRef> refs = new ArrayList<>(
                List.of(new AffectedEntityRef.MachineRef(new MachineId(1))));
        RuntimeEventPayload payload = new RuntimeEventPayload.MachineAvailabilityChanged(new MachineId(1), true);
        assertThrows(IllegalArgumentException.class,
                () -> envelope(0, refs, payload));
        assertThrows(IllegalArgumentException.class,
                () -> envelope(-1, refs, payload));
        RuntimeEventEnvelope accepted = envelope(1, refs, payload);
        refs.clear();
        assertEquals(1, accepted.affectedEntityRefs().size());
    }

    private static RuntimeEventEnvelope envelope(
            long sequence, List<AffectedEntityRef> refs, RuntimeEventPayload payload) {
        return new RuntimeEventEnvelope(
                RUN, sequence, TIME, RuntimeEventType.MACHINE_AVAILABILITY_CHANGED,
                MODEL, Optional.empty(), refs, payload);
    }
}
