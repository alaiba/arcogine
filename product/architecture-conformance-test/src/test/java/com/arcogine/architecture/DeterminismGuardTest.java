package com.arcogine.architecture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.architecture.DeterminismGuardFixtures;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class DeterminismGuardTest {
    @Test
    void wallClockGuardRejectsLegacyClockAcquisition() {
        for (Class<?> fixture : new Class<?>[] {
            DeterminismGuardFixtures.LegacyDate.class,
            DeterminismGuardFixtures.LegacyCalendar.class,
            DeterminismGuardFixtures.CalendarSubclass.class
        }) {
            assertTrue(ArchitectureTest.simulation_never_reads_wall_clock_time
                    .evaluate(new ClassFileImporter().importClasses(fixture)).hasViolation(), fixture.getName());
        }
    }

    @Test
    void concurrencyGuardRejectsBackgroundAndParallelFactories() {
        for (Class<?> fixture : new Class<?>[] {
            DeterminismGuardFixtures.BackgroundTimer.class,
            DeterminismGuardFixtures.ParallelStreamFactory.class
        }) {
            assertTrue(ArchitectureTest.simulation_never_runs_concurrently
                    .evaluate(new ClassFileImporter().importClasses(fixture)).hasViolation(), fixture.getName());
        }
    }

    @Test
    void guardsAllowSequentialCollectionStreams() {
        var classes = new ClassFileImporter()
                .importClasses(DeterminismGuardFixtures.SequentialCollectionStream.class);
        assertFalse(ArchitectureTest.simulation_never_reads_wall_clock_time.evaluate(classes).hasViolation());
        assertFalse(ArchitectureTest.simulation_never_runs_concurrently.evaluate(classes).hasViolation());
    }
}
