package com.arcogine.game.lastlightfoundry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.challenge.evaluation.ChallengeEvaluationResult;
import com.arcogine.game.lastlightfoundry.content.Mission;
import com.arcogine.game.lastlightfoundry.draft.Draft;
import com.arcogine.game.lastlightfoundry.draft.Installation;
import com.arcogine.game.lastlightfoundry.engine.DesignChecker;
import com.arcogine.game.lastlightfoundry.engine.RunDriver;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Engine-derived proving cases for The Relay: every timing below is produced by running the real
 * Challenge -> Factory validation/publication -> runtime -> reference evaluation path, and the
 * constants here are the observed results, not values the game hard-codes.
 */
class RelayMechanicsTest {

    private final Mission relay = Fixtures.relay();

    private ChallengeEvaluationResult evaluate(Draft draft, long expectedCost, long expectedTick) {
        DesignChecker.DesignReport report = DesignChecker.check(relay, draft);
        assertTrue(report.runnable(), () -> "design should be runnable: " + report);
        assertEquals(expectedCost, report.economics().economics().committedConstructionCostCredits());
        RunDriver.RunView view = Fixtures.finish(Fixtures.run(relay, draft));
        assertEquals(RunDriver.Status.COMPLETED, view.status());
        assertEquals(expectedTick, view.observation().metadata().currentTime().value());
        assertNotNull(view.evaluation());
        return view.evaluation();
    }

    @Test
    void dedicatedLineWinsNarrowlyAt850CreditsAnd56Ticks() {
        ChallengeEvaluationResult result = evaluate(Fixtures.dedicated(), 850, 56);
        assertTrue(result.successful());
        assertEquals(1L, result.deadlineMarginTicks());
        assertEquals(212L, result.unusedBudgetCredits());
        // The reference policy: 1 + deadline margin + unused budget.
        assertEquals(BigInteger.valueOf(214), result.score());
    }

    @Test
    void flexibleDesignWinsAt913CreditsAndItsTimingDependsOnTheExplicitIdentityOrder() {
        ChallengeEvaluationResult flexLast = evaluate(Fixtures.flexLast(), 913, 47);
        ChallengeEvaluationResult flexFirst = evaluate(Fixtures.flexFirst(), 913, 46);
        assertTrue(flexLast.successful());
        assertTrue(flexFirst.successful());
        assertNotEquals(flexLast.score(), flexFirst.score(),
                "reassigning the flex cell's installation number is a deliberate, visible design change");
    }

    @Test
    void twinAssemblerDesignWinsAt1010CreditsAnd45Ticks() {
        ChallengeEvaluationResult result = evaluate(Fixtures.twin(), 1010, 45);
        assertTrue(result.successful());
        assertEquals(12L, result.deadlineMarginTicks());
        assertEquals(BigInteger.valueOf(65), result.score());
    }

    @Test
    void cheapSingleLineLosesByMissingTheDeadlineAndScoresZero() {
        ChallengeEvaluationResult result = evaluate(Fixtures.cheapLine(), 600, 67);
        assertFalse(result.successful());
        assertEquals(-10L, result.deadlineMarginTicks());
        assertEquals(BigInteger.ZERO, result.score());
        assertEquals("challenge.deadline.missed", result.issues().get(0).code());
    }

    @Test
    void scoreOrderingIsTheReferencePolicyNotCompletionSpeed() {
        // The twin finishes 11 ticks sooner than the dedicated line but scores lower: it spends
        // more of the budget. Faster does not mean a larger score.
        ChallengeEvaluationResult dedicated = evaluate(Fixtures.dedicated(), 850, 56);
        ChallengeEvaluationResult twin = evaluate(Fixtures.twin(), 1010, 45);
        assertTrue(dedicated.score().compareTo(twin.score()) > 0);
    }

    @Test
    void movingEquipmentOrReorderingTheDraftDoesNotChangeTheFactoryModelOrResult() {
        Draft base = Fixtures.flexFirst();
        List<Installation> moved = new ArrayList<>();
        for (Installation installation : base.installations()) {
            moved.add(new Installation(installation.number(), installation.itemId(),
                    11 - installation.x(), 7 - installation.y()));
        }
        Collections.reverse(moved);
        Draft relocated = new Draft(moved);

        DesignChecker.DesignReport original = DesignChecker.check(relay, base);
        DesignChecker.DesignReport shuffled = DesignChecker.check(relay, relocated);
        assertEquals(original.publishedVersion().fingerprint(), shuffled.publishedVersion().fingerprint());
        RunDriver baseRun = Fixtures.run(relay, base);
        RunDriver relocatedRun = Fixtures.run(relay, relocated);
        Fixtures.finish(baseRun);
        Fixtures.finish(relocatedRun);
        assertEquals(normalized(baseRun.view(), baseRun), normalized(relocatedRun.view(), relocatedRun));
    }

    @Test
    void reassigningAnInstallationNumberIsAnExplicitChangeToFactoryIdentity() {
        DesignChecker.DesignReport last = DesignChecker.check(relay, Fixtures.flexLast());
        DesignChecker.DesignReport first = DesignChecker.check(relay, Fixtures.flexFirst());
        assertNotEquals(last.publishedVersion().fingerprint(), first.publishedVersion().fingerprint());
    }

    @Test
    void replayWithIdenticalInputsYieldsIdenticalSemanticEventsAfterRunIdNormalization() {
        RunDriver first = Fixtures.run(relay, Fixtures.flexFirst());
        RunDriver second = Fixtures.run(relay, Fixtures.flexFirst());
        Fixtures.finish(first);
        Fixtures.finish(second);
        assertNotEquals(first.view().observation().metadata().runId(), second.view().observation().metadata().runId());
        assertEquals(normalized(first.view(), first), normalized(second.view(), second));
        assertEquals(first.view().observation().metadata().modelFingerprint(),
                second.view().observation().metadata().modelFingerprint());
    }

    @Test
    void singleStepAndBoundedAdvancementReachTheSameEventStream() {
        RunDriver stepped = Fixtures.run(relay, Fixtures.dedicated());
        Fixtures.finish(stepped);

        RunDriver batched = Fixtures.run(relay, Fixtures.dedicated());
        while (batched.view().status() == RunDriver.Status.ACTIVE) {
            batched.advance(7, Long.MAX_VALUE);
        }
        RunDriver byTime = Fixtures.run(relay, Fixtures.dedicated());
        long bound = 0;
        while (byTime.view().status() == RunDriver.Status.ACTIVE) {
            byTime.advance(1_000, bound);
            bound += 3;
        }
        assertEquals(normalized(stepped.view(), stepped), normalized(batched.view(), batched));
        assertEquals(normalized(stepped.view(), stepped), normalized(byTime.view(), byTime));
    }

    @Test
    void aTimeTargetIsABoundAndNeverMovesTheClock() {
        RunDriver driver = Fixtures.run(relay, Fixtures.dedicated());
        RunDriver.RunView before = driver.view();
        RunDriver.RunView after = driver.advance(1_000, 2);
        // The first step completion is at tick 3: a target of 2 processes nothing and invents no time.
        assertEquals(before.observation().metadata().currentTime(), after.observation().metadata().currentTime());
        assertEquals(0, after.processedEvents());
    }

    @Test
    void primaryWorkloadIsOneRealOrderOfTheMissionQuantityWithEngineCreatedChildJobs() {
        RunDriver driver = Fixtures.run(relay, Fixtures.dedicated());
        var observation = driver.view().observation();
        assertEquals(1, observation.orders().size());
        assertEquals(12, observation.orders().get(0).requestedQuantity());
        assertEquals(12, observation.jobs().size());
        assertEquals(driver.view().primaryOrder(), observation.orders().get(0).orderId());
        assertNull(driver.view().evaluation(), "no outcome is fabricated before the order completes");
    }

    @Test
    void supportedEventsAreContiguousAndCorrelatedToTheRunAndModel() {
        RunDriver driver = Fixtures.run(relay, Fixtures.twin());
        Fixtures.finish(driver);
        var view = driver.view();
        List<RuntimeEventEnvelope> events = driver.eventsAfter(0, 10_000).events();
        assertEquals(view.observation().metadata().latestEventSequence(), events.size());
        long expected = 1;
        for (RuntimeEventEnvelope event : events) {
            assertEquals(expected++, event.sequence());
            assertEquals(view.observation().metadata().runId(), event.runId());
            assertEquals(driver.version().fingerprint(), event.modelFingerprint());
        }
    }

    private static String normalized(RunDriver.RunView view, RunDriver driver) {
        StringBuilder out = new StringBuilder();
        out.append(view.status()).append('|').append(view.observation().metadata().currentTime());
        for (RuntimeEventEnvelope event : driver.eventsAfter(0, 10_000).events()) {
            out.append('\n').append(event.sequence()).append(' ').append(event.simulationTime())
                    .append(' ').append(event.eventType()).append(' ').append(event.payload());
        }
        return out.toString();
    }
}
