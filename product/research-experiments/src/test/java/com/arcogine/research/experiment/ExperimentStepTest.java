package com.arcogine.research.experiment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arcogine.types.SimTime;
import org.junit.jupiter.api.Test;

class ExperimentStepTest {

    @Test
    void negativeAdvancementTargetsAreRefusedAtConstruction() {
        assertThrows(IllegalArgumentException.class, () -> ExperimentStep.advanceUntil(-1));
        assertThrows(IllegalArgumentException.class, () -> ExperimentStep.advanceToQuiescence(-1));
    }

    @Test
    void nonNegativeAdvancementTargetsConstruct() {
        assertEquals(SimTime.ZERO, ExperimentStep.advanceUntil(0).target());
        assertEquals(SimTime.of(Long.MAX_VALUE), ExperimentStep.advanceToQuiescence(Long.MAX_VALUE).deadline());
    }
}
