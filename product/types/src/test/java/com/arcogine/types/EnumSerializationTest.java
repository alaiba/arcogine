package com.arcogine.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/** Ported from crates/sim-types/src/lib.rs machine_state/job_status serde roundtrip tests. */
class EnumSerializationTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();

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
