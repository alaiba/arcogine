package com.arcogine.game.lastlightfoundry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.challenge.ChallengeDefinition;
import com.arcogine.challenge.ChallengeWorkload;
import com.arcogine.game.lastlightfoundry.content.Mission;
import com.arcogine.game.lastlightfoundry.draft.Draft;
import com.arcogine.game.lastlightfoundry.engine.DesignChecker;
import com.arcogine.game.lastlightfoundry.engine.RunDriver;
import java.util.List;
import org.junit.jupiter.api.Test;

class RunDriverTest {

    private final Mission wake = Fixtures.wake();

    private RunDriver wakeRun(long retention) {
        Draft draft = Fixtures.draft(Fixtures.CUTTER, Fixtures.ASSEMBLER, Fixtures.INSPECTOR);
        return RunDriver.start(wake, draft, DesignChecker.check(wake, draft), retention);
    }

    @Test
    void commandsOnATerminalRunAreNoOps() {
        RunDriver driver = wakeRun(1_000);
        RunDriver.RunView done = Fixtures.finish(driver);
        List<String> transcript = driver.transcript();
        assertEquals(done.processedEvents(), driver.step(5).processedEvents());
        assertEquals(done.processedEvents(), driver.advance(5, 1_000).processedEvents());
        assertEquals(transcript, driver.transcript(), "a finished run accepts no further engine commands");
    }

    @Test
    void resetCreatesAFreshRunAndLeavesTheOldRunUntouched() {
        RunDriver first = wakeRun(1_000);
        first.step(3);
        long firstSequence = first.view().observation().metadata().latestEventSequence();
        RunDriver second = first.reset();
        assertNotEquals(first.view().observation().metadata().runId(), second.view().observation().metadata().runId());
        assertEquals(first.version().fingerprint(), second.version().fingerprint());
        assertEquals(firstSequence, first.view().observation().metadata().latestEventSequence());
        assertTrue(second.view().observation().metadata().latestEventSequence() < firstSequence);
    }

    @Test
    void eventCursorsAreValidatedAndOldCursorsAreRefusedAsGaps() {
        RunDriver driver = wakeRun(4);
        Fixtures.finish(driver);
        long latest = driver.view().observation().metadata().latestEventSequence();
        assertTrue(latest > 4);
        RunDriver.GapException gap = assertThrows(RunDriver.GapException.class, () -> driver.eventsAfter(0, 10));
        assertEquals(driver.view().oldestRetainedSequence(), gap.oldestRetained());
        assertEquals(4, driver.eventsAfter(latest - 4, 10).events().size());
        assertThrows(RunDriver.CursorException.class, () -> driver.eventsAfter(latest + 1, 10));
        assertThrows(RunDriver.CursorException.class, () -> driver.eventsAfter(-1, 10));
    }

    @Test
    void eventPagesReportWhetherMoreEventsRemain() {
        RunDriver driver = wakeRun(1_000);
        Fixtures.finish(driver);
        RunDriver.EventPage page = driver.eventsAfter(0, 3);
        assertEquals(3, page.events().size());
        assertTrue(page.more());
        assertEquals(1, page.events().get(0).sequence());
    }

    @Test
    void aRejectedWorkloadIsReportedHonestlyWithoutMutation() {
        // A mission whose workload quantity is invalid for the engine: the engine rejects the
        // command before creating any order, and the run says so instead of pretending to start.
        ChallengeDefinition base = wake.challenge();
        ChallengeDefinition broken = new ChallengeDefinition(
                base.identity(), base.floor(), base.startingBudget(), new ChallengeWorkload("product.x", 0),
                base.availableEquipment(), base.deadline(), base.evaluationPolicy(), base.catalogueIdentity(),
                base.catalogueSemanticFingerprint());
        Mission mission = new Mission(wake.id(), wake.title(), wake.tagline(), wake.briefing(), wake.hints(),
                broken, wake.catalogue(), wake.productName(), wake.unitPrice(), wake.steps(), wake.equipment(),
                wake.starterDraft(), wake.contentFingerprint());
        Draft draft = Fixtures.draft(Fixtures.CUTTER, Fixtures.ASSEMBLER, Fixtures.INSPECTOR);
        RunDriver driver = RunDriver.start(mission, draft, DesignChecker.check(mission, draft), 100);
        RunDriver.RunView view = driver.view();
        assertEquals(RunDriver.Status.REJECTED, view.status());
        assertTrue(view.detail().contains("OutOfRange"));
        assertEquals(0, view.observation().orders().size());
        assertEquals(0, view.observation().metadata().latestEventSequence());
        assertEquals(RunDriver.Status.REJECTED, driver.step(1).status());
    }
}
