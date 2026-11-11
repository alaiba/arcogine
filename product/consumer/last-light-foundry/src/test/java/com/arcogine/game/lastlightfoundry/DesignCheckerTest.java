package com.arcogine.game.lastlightfoundry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.game.lastlightfoundry.content.Mission;
import com.arcogine.game.lastlightfoundry.draft.Draft;
import com.arcogine.game.lastlightfoundry.draft.Installation;
import com.arcogine.game.lastlightfoundry.engine.DesignChecker;
import com.arcogine.game.lastlightfoundry.engine.GameService;
import com.arcogine.game.lastlightfoundry.engine.RunDriver;
import java.util.List;
import org.junit.jupiter.api.Test;

class DesignCheckerTest {

    private final Mission wake = Fixtures.wake();
    private final Mission relay = Fixtures.relay();

    @Test
    void starterDraftIsAdmissibleForTheContractButNotPublishable() {
        // Challenge admissibility and Factory publication are independent verdicts.
        Draft starter = new Draft(wake.starterDraft());
        DesignChecker.DesignReport report = DesignChecker.check(wake, starter);
        assertTrue(report.admissibility().admitted());
        assertTrue(report.economics().isSuccess());
        assertEquals(220, report.economics().economics().committedConstructionCostCredits());
        assertFalse(report.publishable());
        assertFalse(report.runnable());
        assertTrue(report.publicationIssues().stream().anyMatch(issue -> issue.field().contains("eligibleResources")));
    }

    @Test
    void placingTheMissingInspectorMakesTheStarterDraftRunnableAndTheOrderShips() {
        List<Installation> installations = new java.util.ArrayList<>(wake.starterDraft());
        installations.add(new Installation(3, Fixtures.INSPECTOR, 7, 2));
        Draft draft = new Draft(installations);
        DesignChecker.DesignReport report = DesignChecker.check(wake, draft);
        assertTrue(report.runnable());
        RunDriver.RunView view = Fixtures.finish(Fixtures.run(wake, draft));
        assertEquals(RunDriver.Status.COMPLETED, view.status());
        assertTrue(view.evaluation().successful());
        assertEquals(3, view.observation().orders().get(0).completedQuantity());
    }

    @Test
    void publishableDesignCanStillBeInadmissibleForTheChallenge() {
        // Five cutters, assemblers and inspectors exceed the 400 credit budget and quantity limits
        // but every step still has an eligible machine, so the Factory model is valid.
        Draft draft = Fixtures.draft(
                Fixtures.CUTTER, Fixtures.CUTTER, Fixtures.CUTTER, Fixtures.ASSEMBLER, Fixtures.INSPECTOR);
        DesignChecker.DesignReport report = DesignChecker.check(wake, draft);
        assertTrue(report.publishable());
        assertFalse(report.admissibility().admitted());
        assertFalse(report.runnable());
        List<String> codes = report.admissibility().issues().stream().map(issue -> issue.code()).toList();
        assertTrue(codes.contains("candidate.budget.exceeded"));
        assertTrue(codes.contains("candidate.quantity-limit.exceeded"));
    }

    @Test
    void floorRulesAreEnforcedByTheChallengePolicy() {
        Draft draft = new Draft(List.of(
                new Installation(1, Fixtures.CUTTER, 0, 0),
                new Installation(2, Fixtures.ASSEMBLER, 0, 0),
                new Installation(3, Fixtures.INSPECTOR, 99, 0)));
        List<String> codes = DesignChecker.check(wake, draft).admissibility().issues().stream()
                .map(issue -> issue.code()).toList();
        assertTrue(codes.contains("candidate.placement.overlap"));
        assertTrue(codes.contains("candidate.placement.out-of-bounds"));
    }

    @Test
    void unknownEquipmentIsReportedByBothAuthoritiesWithoutThrowing() {
        Draft draft = new Draft(List.of(new Installation(1, "equipment.warp-drive", 0, 0)));
        DesignChecker.DesignReport report = DesignChecker.check(wake, draft);
        assertFalse(report.economics().isSuccess());
        assertFalse(report.admissibility().admitted());
        assertFalse(report.publishable());
        assertNull(report.publishedVersion());
        assertTrue(report.publicationIssues().get(0).message().contains("unknown equipment"));
    }

    @Test
    void duplicateInstallationNumbersAreRefused() {
        Draft draft = new Draft(List.of(
                new Installation(1, Fixtures.CUTTER, 0, 0), new Installation(1, Fixtures.ASSEMBLER, 1, 0),
                new Installation(2, Fixtures.INSPECTOR, 2, 0)));
        DesignChecker.DesignReport report = DesignChecker.check(wake, draft);
        assertFalse(report.runnable());
        assertTrue(report.admissibility().issues().stream()
                .anyMatch(issue -> issue.code().equals("candidate.occurrence.duplicate-identity")));
    }

    @Test
    void anEmptyDraftIsAdmissibleButNotPublishable() {
        DesignChecker.DesignReport report = DesignChecker.check(relay, new Draft(List.of()));
        assertTrue(report.admissibility().admitted());
        assertFalse(report.publishable());
    }

    @Test
    void aRunCannotBeStartedFromAnUnrunnableDesign() {
        DesignChecker.DesignReport report = DesignChecker.check(wake, new Draft(wake.starterDraft()));
        assertThrows(IllegalArgumentException.class,
                () -> RunDriver.start(wake, new Draft(wake.starterDraft()), report, 10));
    }

    @Test
    void theServiceRefusesToStartAnUnrunnableDesign() {
        GameService service = new GameService(com.arcogine.game.lastlightfoundry.content.MissionLibrary.loadBundled());
        assertTrue(service.start(wake, new Draft(wake.starterDraft())).isEmpty());
        assertTrue(service.find("00000000-0000-0000-0000-000000000000").isEmpty());
        assertTrue(service.reset("00000000-0000-0000-0000-000000000000").isEmpty());
    }
}
