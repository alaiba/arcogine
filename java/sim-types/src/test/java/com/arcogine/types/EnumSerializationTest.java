package com.arcogine.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/** Ported from crates/sim-types/src/lib.rs machine_state/job_status serde roundtrip tests. */
class EnumSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void machineStateRoundtrip() throws Exception {
        for (MachineState state : MachineState.values()) {
            String json = mapper.writeValueAsString(state);
            assertEquals(state, mapper.readValue(json, MachineState.class));
        }
    }

    @Test
    void jobStatusRoundtrip() throws Exception {
        for (JobStatus status : JobStatus.values()) {
            String json = mapper.writeValueAsString(status);
            assertEquals(status, mapper.readValue(json, JobStatus.class));
        }
    }
}
