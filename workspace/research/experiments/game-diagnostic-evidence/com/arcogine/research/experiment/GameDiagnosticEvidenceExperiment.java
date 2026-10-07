package com.arcogine.research.experiment;

import static com.arcogine.research.experiment.GameDiagnosticCorpus.ASSEMBLE;
import static com.arcogine.research.experiment.GameDiagnosticCorpus.CUT;
import static com.arcogine.research.experiment.GameDiagnosticCorpus.INSPECT;
import static com.arcogine.research.experiment.GameDiagnosticCorpus.MID;
import static com.arcogine.research.experiment.GameDiagnosticCorpus.attempt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.process.ResourceObservation;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.research.experiment.GameDiagnosticCorpus.InterventionRow;
import com.arcogine.research.experiment.GameDiagnosticCorpus.Pair;
import com.arcogine.research.experiment.GameDiagnostics.Attempt;
import com.arcogine.research.experiment.GameDiagnostics.Contract;
import com.arcogine.research.experiment.GameDiagnostics.Kind;
import com.arcogine.research.experiment.GameDiagnostics.Question;
import com.arcogine.research.experiment.GameDiagnostics.Statement;
import com.arcogine.research.experiment.GameEvidenceOracles.CompletingUnit;
import com.arcogine.research.experiment.GameEvidenceOracles.CompletionChain;
import com.arcogine.research.experiment.WaitingWorkByStepOracle.Attribution;
import com.arcogine.research.experiment.WaitingWorkByStepOracle.WaitingAtStep;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * The game diagnostic-evidence experiment: first the hand-derived oracle (fixed before any candidate
 * was evaluated), then the six audits of the brief over every candidate contract, then the
 * claim-to-evidence matrix, audit matrix and blinded walkthrough packet written under the module's
 * build directory.
 */
class GameDiagnosticEvidenceExperiment {

    private static final Path OUT = Path.of("build", "game-diagnostic-evidence");

    // =============================================================== oracle (hand-derived ground truth)

    @Test
    void handDerivedCompletionsAndInterventionsHold() {
        for (InterventionRow row : GameDiagnosticCorpus.interventions()) {
            assertEquals(row.completion(), completion(attempt(row.id(), row.base())), row.id());
            assertEquals(row.plusCut(), completion(attempt(row.id() + "+cut", GameDiagnosticCorpus.plusOne(row.base(), CUT))),
                    row.id() + " + cut");
            assertEquals(row.plusAssemble(),
                    completion(attempt(row.id() + "+assemble", GameDiagnosticCorpus.plusOne(row.base(), ASSEMBLE))),
                    row.id() + " + assemble");
            assertEquals(row.plusInspect(),
                    completion(attempt(row.id() + "+inspect", GameDiagnosticCorpus.plusOne(row.base(), INSPECT))),
                    row.id() + " + inspect");
        }
        assertEquals(47, completion(attempt("G6", GameDiagnosticCorpus.G6)));
        assertEquals(67, completion(attempt("G8", GameDiagnosticCorpus.G8)));
        for (Pair pair : GameDiagnosticCorpus.pairs()) {
            assertEquals(pair.expectedFirst(), completion(pair.first()), pair.id() + " first");
            assertEquals(pair.expectedSecond(), completion(pair.second()), pair.id() + " second");
        }
    }

    @Test
    void handDerivedWaitingAtBoundariesHolds() {
        assertWaiting(attempt("G1", GameDiagnosticCorpus.G1, 33), Map.of(ASSEMBLE, 3L, INSPECT, 1L), Set.of());
        assertWaiting(attempt("G2", GameDiagnosticCorpus.G2, 17), Map.of(CUT, 6L, ASSEMBLE, 1L), Set.of());
        assertWaiting(attempt("G6", GameDiagnosticCorpus.G6, 17), Map.of(CUT, 6L, INSPECT, 1L), Set.of(INSPECT));
        assertWaiting(attempt("G8", GameDiagnosticCorpus.G8, 11), Map.of(CUT, 8L, INSPECT, 1L), Set.of());

        RuntimeObservation g6 = attempt("G6", GameDiagnosticCorpus.G6, 17).evidence().observation(MID);
        assertEquals(1, g6.pendingWork().size(), "one INSPECT unit is pending multi-eligible work");
        for (ResourceObservation resource : g6.resources()) {
            assertEquals(resource.name().equals("Cutter") ? 6 : 0, resource.queueDepth(), resource.name());
        }
    }

    @Test
    void handDerivedIdleResourcesHold() {
        assertIdleWithoutWork(attempt("G2", GameDiagnosticCorpus.G2, 17), "Inspector 2");
        assertIdleWithoutWork(attempt("G8", GameDiagnosticCorpus.G8, 11), "Assembler 2");
    }

    @Test
    void handDerivedBusyTicksDivergenceHolds() {
        Attempt g4 = attempt("G4", GameDiagnosticCorpus.G4, 9);
        ResourceObservation twinAtNine = resource(g4.evidence().observation(MID), "Twin Assembler");
        assertEquals(2, twinAtNine.activeJobIds().size(), "both slots active at tick 9");
        assertEquals(4, twinAtNine.busyTicks(), "only the first completed assembly is credited");
        ResourceObservation twinAtEnd = resource(g4.evidence().observation(ExperimentEvidence.CLOSING_LABEL), "Twin Assembler");
        assertEquals(48, twinAtEnd.busyTicks());
        assertEquals(45, g4.evidence().observation(ExperimentEvidence.CLOSING_LABEL).metadata().currentTime().value());
    }

    @Test
    void handDerivedCompletingUnitAndStepWaitsHold() {
        assertCompletingUnit(attempt("G1", GameDiagnosticCorpus.G1), 67, List.of(33L, 11L, 11L));
        assertCompletingUnit(attempt("G2", GameDiagnosticCorpus.G2), 56, List.of(33L, 11L, 0L));
        assertCompletingUnit(attempt("G3", GameDiagnosticCorpus.G3), 45, List.of(33L, 0L, 0L));
        assertCompletingUnit(attempt("G4", GameDiagnosticCorpus.G4), 45, List.of(33L, 0L, 0L));
        assertCompletingUnit(attempt("G5", GameDiagnosticCorpus.G5), 67, List.of(33L, 0L, 22L));
        assertCompletingUnit(attempt("G7", GameDiagnosticCorpus.G7), 68, List.of(33L, 22L, 0L));
        assertCompletingUnit(attempt("G8", GameDiagnosticCorpus.G8), 67, List.of(33L, 0L, 22L));

        assertStepWaits(attempt("G1", GameDiagnosticCorpus.G1), List.of(198L, 66L, 66L));
        assertStepWaits(attempt("G3", GameDiagnosticCorpus.G3), List.of(198L, 0L, 0L));
        assertStepWaits(attempt("G4", GameDiagnosticCorpus.G4), List.of(198L, 0L, 0L));
        assertStepWaits(attempt("G7", GameDiagnosticCorpus.G7), List.of(198L, 132L, 0L));
        assertStepWaits(attempt("G8", GameDiagnosticCorpus.G8), List.of(198L, 0L, 132L));
    }

    @Test
    void handDerivedCompletionChainsHold() {
        assertPacing(attempt("G1", GameDiagnosticCorpus.G1), Optional.of(INSPECT));
        assertPacing(attempt("G2", GameDiagnosticCorpus.G2), Optional.of(ASSEMBLE));
        assertPacing(attempt("G3", GameDiagnosticCorpus.G3), Optional.of(CUT));
        assertPacing(attempt("G4", GameDiagnosticCorpus.G4), Optional.of(CUT));
        assertPacing(attempt("G5", GameDiagnosticCorpus.G5), Optional.of(INSPECT));
        CompletionChain g6 = chain(attempt("G6", GameDiagnosticCorpus.G6));
        assertTrue(g6.uniquePacingStep().isEmpty(), "G6 has a tie on its chain");
        assertTrue(g6.ties().stream().anyMatch(t -> t.occurrence().dispatchTick() == 33 && t.occurrence().unit() == 11
                && t.releasedBy().unit() == 8), "G6 tie at tick 33: unit 11 ready as Shared is released by unit 8");
        CompletionChain g7 = chain(attempt("G7", GameDiagnosticCorpus.G7));
        assertTrue(g7.uniquePacingStep().isEmpty(), "G7 is co-binding");
        assertTrue(g7.ties().stream().anyMatch(t -> t.occurrence().dispatchTick() == 63 && t.occurrence().unit() == 12),
                "G7 tie at tick 63 for unit 12's inspection");
    }

    // =============================================================== audits

    private record AuditResult(String contract, String audit, List<String> findings) {

        boolean passed() {
            return findings.isEmpty();
        }
    }

    private static final List<String> AUDITS = List.of(
            "traceability", "reconstruction", "counterexample", "refusal", "controlled-mutation", "terminology");

    private static final Map<String, Map<String, AuditResult>> RESULTS = new LinkedHashMap<>();

    private static synchronized Map<String, Map<String, AuditResult>> results() {
        if (RESULTS.isEmpty()) {
            for (Contract contract : GameContracts.all()) {
                Map<String, AuditResult> byAudit = new LinkedHashMap<>();
                byAudit.put("traceability", traceability(contract));
                byAudit.put("reconstruction", reconstruction(contract));
                byAudit.put("counterexample", counterexample(contract));
                byAudit.put("refusal", refusal(contract));
                byAudit.put("controlled-mutation", controlledMutation(contract));
                byAudit.put("terminology", terminology(contract));
                RESULTS.put(contract.id(), byAudit);
            }
        }
        return RESULTS;
    }

    @Test
    void theMinimalClaimEvidenceBundlePassesEveryAudit() {
        results().get("C3b-bundle-minimal").values().forEach(result ->
                assertTrue(result.passed(), result.audit() + ": " + result.findings()));
    }

    @Test
    void theRevisedPlainBundlePassesEveryAudit() {
        results().get("C3c-plain-bundle").values().forEach(result ->
                assertTrue(result.passed(), result.audit() + ": " + result.findings()));
    }

    @Test
    void everyAuditCatchesTheNaiveControlExceptReconstruction() {
        Map<String, AuditResult> naive = results().get("N-naive-dashboard");
        for (String audit : AUDITS) {
            if (!audit.equals("reconstruction")) {
                assertFalse(naive.get(audit).passed(), audit + " must be able to fail");
            }
        }
    }

    @Test
    void theModuleBoundaryTestScopeCoversThisExperiment() {
        // ResearchPackageBoundaryTest holds every class of this package to the supported runtime contract.
        com.tngtech.archunit.core.domain.JavaClasses research =
                new com.tngtech.archunit.core.importer.ClassFileImporter().importPackages("com.arcogine.research");
        for (Class<?> type : List.of(GameEvidence.class, GameEvidenceOracles.CompletionChainTrace.class,
                GameEvidenceOracles.IdleResources.class, GameStatements.class, GameContracts.class, GameDiagnosticCorpus.class,
                GamePlainContract.class, GameWalkthroughPack.class, BakeryInspectionPack.class)) {
            assertTrue(research.contain(type), type.getName());
        }
    }

    @Test
    void writeArtifacts() throws IOException {
        Files.createDirectories(OUT);
        Files.writeString(OUT.resolve("audit-matrix.md"), auditMatrix());
        Files.writeString(OUT.resolve("claim-evidence-matrix.md"), claimEvidenceMatrix());
        Files.writeString(OUT.resolve("interventions.md"), interventionTable());
        writeWalkthrough();
    }

    // --------------------------------------------------------------- traceability

    private static AuditResult traceability(Contract contract) {
        List<String> findings = new ArrayList<>();
        Set<String> methods = Set.of(
                WaitingWorkByStepOracle.DEFINITION.name(),
                DispatchProfileOracle.DEFINITION.name(),
                EligibilityPoolOccupancyOracle.DEFINITION.name(),
                GameEvidence.OCCURRENCES.name(),
                GameEvidenceOracles.IDLENESS.name(),
                GameEvidenceOracles.COMPLETING_UNIT.name(),
                GameEvidenceOracles.COMPLETION_CHAIN.name());
        for (Rendered rendered : renderAll(contract)) {
            for (Statement s : rendered.statements()) {
                if (s.kind() == Kind.REFUSAL) {
                    continue;
                }
                if (s.support().isEmpty()) {
                    findings.add(rendered.id() + ": no cited evidence for '" + s.text() + "'");
                }
                boolean derived = Set.of(Kind.BOUNDARY_COUNT, Kind.EVENT_INTERVAL, Kind.AGGREGATE_MEASUREMENT,
                        Kind.NAMED_INTERPRETATION).contains(s.kind());
                if (derived && s.method().filter(methods::contains).isEmpty()) {
                    findings.add(rendered.id() + ": " + s.kind() + " without a named definition: '" + s.text() + "'");
                }
                for (Attempt attempt : rendered.attempts()) {
                    for (String label : s.support().observations()) {
                        if (!attempt.evidence().observations().containsKey(label)) {
                            findings.add(rendered.id() + ": cites missing observation " + label);
                        }
                    }
                }
                Set<Long> retained = rendered.attempts().getFirst().evidence().retainedEvents().stream()
                        .map(e -> e.sequence()).collect(Collectors.toSet());
                if (rendered.attempts().size() == 1 && !s.support().events().stream().allMatch(retained::contains)) {
                    findings.add(rendered.id() + ": cites an event that was not retained: '" + s.text() + "'");
                }
            }
        }
        return new AuditResult(contract.id(), "traceability", findings);
    }

    // --------------------------------------------------------------- reconstruction

    private static AuditResult reconstruction(Contract contract) {
        List<String> findings = new ArrayList<>();
        for (Attempt attempt : GameDiagnosticCorpus.auditAttempts()) {
            ExperimentFixture fixture = GameDiagnosticCorpus.fixture(attempt.label(), attempt.design(),
                    attempt.evidence().observation(ExperimentEvidence.CLOSING_LABEL).orders().getFirst().requestedQuantity(),
                    attempt.midRunLabel().map(l -> attempt.evidence().observation(l).metadata().currentTime().value()),
                    attempt.label().equals("G10"));
            Attempt replay = new Attempt(attempt.label(), attempt.design(), ExperimentRunner.run(fixture), attempt.midRunLabel());
            if (!contract.attempt(attempt).equals(contract.attempt(replay))) {
                findings.add(attempt.label() + ": a fresh run from the same explicit inputs renders different statements");
            }
        }
        // A late joiner holds a fresh observation but not the events before it.
        List<Statement> complete = contract.attempt(attempt("G2", GameDiagnosticCorpus.G2, 17));
        List<Statement> late = contract.attempt(GameDiagnosticCorpus.auditAttempts().stream()
                .filter(a -> a.label().equals("G10")).findFirst().orElseThrow());
        Map<String, Statement> completeBySubject = new LinkedHashMap<>();
        complete.forEach(s -> completeBySubject.put(s.subject() + "|" + s.question(), s));
        for (Statement s : late) {
            boolean currentState = s.support().events().isEmpty() && s.support().observations().contains(MID);
            Statement counterpart = completeBySubject.get(s.subject() + "|" + s.question());
            if (currentState && counterpart != null && !counterpart.text().equals(s.text())) {
                findings.add("G10: current-state statement differs from the complete-window run: '" + s.text() + "'");
            }
            if (!s.support().events().isEmpty() && s.kind() != Kind.REFUSAL) {
                findings.add("G10: event-based statement emitted without the events before the join: '" + s.text() + "'");
            }
        }
        return new AuditResult(contract.id(), "reconstruction", findings);
    }

    // --------------------------------------------------------------- counterexamples

    private static AuditResult counterexample(Contract contract) {
        List<String> findings = new ArrayList<>();
        // Multi-eligible waiting is never one machine's queue, and is never counted once per eligible machine.
        for (String id : List.of("G6", "G11a")) {
            Attempt attempt = audit(id);
            long waiting = waitingUnits(attempt);
            List<Statement> statements = contract.attempt(attempt);
            for (Statement s : statements) {
                if (s.question() == Question.Q1_WAITING && s.facet("step").isPresent()
                        && multiEligible(attempt, s.facet("step").get()) && s.facet("attributedResource").isPresent()) {
                    findings.add(id + ": shared waiting attributed to " + s.facet("attributedResource").get());
                }
            }
            long perResource = statements.stream()
                    .filter(s -> s.subject().startsWith("queue@" + MID))
                    .mapToLong(s -> Long.parseLong(s.facet("perResourceWaiting").orElse("0")))
                    .sum();
            long ownQueues = attempt.evidence().observation(MID).resources().stream().mapToLong(ResourceObservation::queueDepth).sum();
            if (perResource > waiting) {
                findings.add(id + ": per-machine queues total " + perResource + " for " + waiting + " waiting units (own queues "
                        + ownQueues + ")");
            }
        }
        // Completion-credited busyTicks is never shown as utilization.
        for (String id : List.of("G4", "G11b")) {
            for (Statement s : contract.attempt(audit(id))) {
                if (s.facet("usesBusyTicks").isPresent() || s.text().toLowerCase(Locale.ROOT).contains("utiliz")) {
                    findings.add(id + ": busyTicks read as utilization: '" + s.text() + "'");
                }
            }
        }
        // A single-run verdict must not name a step whose added capacity does not help (intervention truth).
        for (InterventionRow row : GameDiagnosticCorpus.interventions()) {
            for (Statement s : contract.attempt(attempt(row.id(), row.base()))) {
                if (s.kind() != Kind.NAMED_INTERPRETATION || s.question() != Question.Q2_LIMITING_STEP) {
                    continue;
                }
                List<String> named = List.of(s.facet("verdictStep").orElse("").split("[|+]"));
                if (!row.helpfulSteps().containsAll(named)) {
                    findings.add(row.id() + ": verdict " + named + " but single additions help only at " + row.helpfulSteps()
                            + ": '" + s.text() + "'");
                }
            }
        }
        // A resource-order change is never silently attributed to anything else.
        Pair reorder = pair("G9-reorder");
        for (Statement s : contract.compare(reorder.first(), reorder.second())) {
            if (s.kind() == Kind.COMPARISON && !s.facet("changes").orElse("").contains("ORDER")) {
                findings.add("G9: order change not reported: '" + s.text() + "'");
            }
        }
        return new AuditResult(contract.id(), "counterexample", findings);
    }

    // --------------------------------------------------------------- refusals

    private static AuditResult refusal(Contract contract) {
        List<String> findings = new ArrayList<>();
        for (Pair pair : GameDiagnosticCorpus.pairs()) {
            if (pair.question() != Question.Q6_CONFOUNDED_COMPARISON) {
                continue;
            }
            List<Statement> statements = contract.compare(pair.first(), pair.second());
            if (statements.stream().noneMatch(s -> s.kind() == Kind.REFUSAL && s.facet("refusalOf").filter("attribution"::equals).isPresent())) {
                findings.add(pair.id() + ": no explicit refusal of per-change attribution");
            }
            statements.stream().filter(s -> s.facet("attributedTo").isPresent())
                    .forEach(s -> findings.add(pair.id() + ": guessed attribution '" + s.text() + "'"));
        }
        for (String[] idle : List.of(new String[] {"G2", "Inspector 2"}, new String[] {"G8", "Assembler 2"})) {
            List<Statement> statements = contract.attempt(audit(idle[0]));
            boolean refused = statements.stream().anyMatch(s -> s.kind() == Kind.REFUSAL
                    && s.facet("refusalOf").filter("surplus"::equals).isPresent()
                    && s.facet("resource").filter(idle[1]::equals).isPresent());
            if (!refused) {
                findings.add(idle[0] + ": no explicit refusal of a surplus verdict for " + idle[1]);
            }
            statements.stream().filter(s -> s.facet("surplusVerdict").filter(idle[1]::equals).isPresent())
                    .forEach(s -> findings.add(idle[0] + ": guessed surplus '" + s.text() + "'"));
        }
        for (String id : List.of("G6", "G7")) {
            for (Statement s : contract.attempt(audit(id))) {
                if (s.question() == Question.Q2_LIMITING_STEP && s.kind() == Kind.NAMED_INTERPRETATION) {
                    findings.add(id + ": verdict where the evidence ties: '" + s.text() + "'");
                }
            }
        }
        List<Statement> late = contract.attempt(audit("G10"));
        if (late.stream().noneMatch(s -> s.facet("refusalOf").filter("interval-measurement"::equals).isPresent())) {
            findings.add("G10: no explicit 'measurement unavailable' for the incomplete window");
        }
        return new AuditResult(contract.id(), "refusal", findings);
    }

    // --------------------------------------------------------------- controlled mutation

    private static AuditResult controlledMutation(Contract contract) {
        List<String> findings = new ArrayList<>();
        Pattern effectWords = Pattern.compile("improv|worsen|due to|because");
        for (Pair pair : GameDiagnosticCorpus.pairs()) {
            if (pair.question() != Question.Q5_CONTROLLED_COMPARISON) {
                continue;
            }
            List<Statement> statements = contract.compare(pair.first(), pair.second());
            Statement comparison = statements.stream().filter(s -> s.kind() == Kind.COMPARISON).findFirst().orElse(null);
            if (comparison == null) {
                findings.add(pair.id() + ": no comparison statement");
                continue;
            }
            if (!comparison.facet("changeCount").equals(Optional.of("1"))) {
                findings.add(pair.id() + ": reports " + comparison.facet("changeCount").orElse("?") + " changes for one authored change");
            }
            if (!comparison.facet("completionDelta").equals(Optional.of(String.valueOf(pair.expectedDelta())))) {
                findings.add(pair.id() + ": outcome delta " + comparison.facet("completionDelta").orElse("?") + ", expected "
                        + pair.expectedDelta());
            }
            if (pair.expectedDelta() == 0) {
                statements.stream().filter(s -> effectWords.matcher(s.text().toLowerCase(Locale.ROOT)).find())
                        .forEach(s -> findings.add(pair.id() + ": effect claimed for a zero delta: '" + s.text() + "'"));
                // A single-run verdict may move when the evidence it rests on moves, even if completion does not
                // (relieving one of two co-binding steps leaves the other). The move must then be corroborated: a
                // step newly named on the variant must be one whose single addition to the variant helps.
                Optional<String> before = verdict(contract.attempt(pair.first()));
                Optional<String> after = verdict(contract.attempt(pair.second()));
                if (!before.equals(after) && after.isPresent()) {
                    long now = completion(pair.second());
                    for (String step : after.get().split("[|+]")) {
                        long relieved = completion(attempt(pair.id() + "+" + step,
                                GameDiagnosticCorpus.plusOne(pair.second().design(), step)));
                        if (relieved >= now) {
                            findings.add(pair.id() + ": verdict moved " + before + " -> " + after + " without an outcome"
                                    + " change, and adding " + step + " to the variant does not help (" + now + " -> "
                                    + relieved + ")");
                        }
                    }
                }
            } else if (outcomeText(contract.attempt(pair.first())).equals(outcomeText(contract.attempt(pair.second())))) {
                findings.add(pair.id() + ": outcome statement unchanged although completion changed");
            }
        }
        return new AuditResult(contract.id(), "controlled-mutation", findings);
    }

    private static Optional<String> verdict(List<Statement> statements) {
        return statements.stream()
                .filter(s -> s.question() == Question.Q2_LIMITING_STEP && s.kind() == Kind.NAMED_INTERPRETATION)
                .map(s -> s.facet("verdictStep").orElse(""))
                .findFirst();
    }

    private static String outcomeText(List<Statement> statements) {
        return statements.stream().filter(s -> s.subject().equals("outcome")).map(Statement::text).findFirst().orElse("");
    }

    // --------------------------------------------------------------- terminology

    /** Words whose ordinary meaning overstates what a non-refusal statement here can license. */
    static final Map<String, String> OVERSTATING = new LinkedHashMap<>();

    static {
        OVERSTATING.put("utiliz", "reads a completion-credited cumulative counter as a rate of use");
        OVERSTATING.put("%", "a percentage implies a normalized utilization basis");
        OVERSTATING.put("bottleneck", "implies the counterfactual that relieving it improves the outcome");
        OVERSTATING.put("constraint", "implies the counterfactual that relieving it improves the outcome");
        OVERSTATING.put("efficien", "implies a normative target the evidence does not define");
        OVERSTATING.put("blocked", "the non-spatial runtime has no blocking state");
        OVERSTATING.put("starv", "implies an upstream cause, and is false at quiescence");
        OVERSTATING.put("surplus", "a counterfactual one run cannot decide");
        OVERSTATING.put("because", "asserts a mechanism");
        OVERSTATING.put("due to", "asserts a mechanism");
        OVERSTATING.put("caused", "asserts a mechanism");
        OVERSTATING.put("variation", "a deterministic run has no run-to-run variation");
        OVERSTATING.put("queue at", "multi-eligible waiting is not one machine's queue");
    }

    private static AuditResult terminology(Contract contract) {
        List<String> findings = new ArrayList<>();
        for (Rendered rendered : renderAll(contract)) {
            for (Statement s : rendered.statements()) {
                if (s.kind() == Kind.REFUSAL) {
                    continue;
                }
                String text = s.text().toLowerCase(Locale.ROOT);
                OVERSTATING.forEach((word, why) -> {
                    if (text.contains(word)) {
                        findings.add(rendered.id() + ": '" + word + "' (" + why + "): '" + s.text() + "'");
                    }
                });
            }
        }
        return new AuditResult(contract.id(), "terminology", findings.stream().distinct().toList());
    }

    // =============================================================== artifacts

    private record Rendered(String id, List<Attempt> attempts, List<Statement> statements) {}

    private static List<Rendered> renderAll(Contract contract) {
        List<Rendered> rendered = new ArrayList<>();
        for (Attempt attempt : GameDiagnosticCorpus.auditAttempts()) {
            rendered.add(new Rendered(attempt.label(), List.of(attempt), contract.attempt(attempt)));
        }
        for (Pair pair : GameDiagnosticCorpus.pairs()) {
            rendered.add(new Rendered(pair.id(), List.of(pair.first(), pair.second()), contract.compare(pair.first(), pair.second())));
        }
        return rendered;
    }

    private static String auditMatrix() {
        StringBuilder out = new StringBuilder("# Audit matrix\n\n| Contract | ");
        out.append(String.join(" | ", AUDITS)).append(" |\n|---|").append("---|".repeat(AUDITS.size())).append('\n');
        results().forEach((contract, byAudit) -> {
            out.append("| ").append(contract).append(" | ");
            out.append(AUDITS.stream().map(a -> byAudit.get(a).passed() ? "PASS" : "FAIL (" + byAudit.get(a).findings().size() + ")")
                    .collect(Collectors.joining(" | ")));
            out.append(" |\n");
        });
        out.append("\n## Findings\n");
        results().forEach((contract, byAudit) -> byAudit.values().stream().filter(r -> !r.passed()).forEach(r -> {
            out.append("\n### ").append(contract).append(" -- ").append(r.audit()).append("\n\n");
            r.findings().forEach(f -> out.append("- ").append(f).append('\n'));
        }));
        return out.toString();
    }

    private static String claimEvidenceMatrix() {
        StringBuilder out = new StringBuilder("# Claim-to-evidence matrix\n");
        for (Contract contract : GameContracts.all()) {
            out.append("\n## ").append(contract.id()).append("\n\n").append(contract.summary()).append('\n');
            for (Rendered rendered : renderAll(contract)) {
                out.append("\n### ").append(rendered.id()).append("\n\n| Question | Kind | Method | Evidence | Statement |\n|---|---|---|---|---|\n");
                for (Statement s : rendered.statements()) {
                    out.append("| ").append(s.question()).append(" | ").append(s.kind()).append(" | ")
                            .append(s.method().orElse("")).append(" | ").append(s.support().summary().replace("|", "/"))
                            .append(" | ").append(s.text().replace("|", "/")).append(" |\n");
                }
            }
        }
        return out.toString();
    }

    private static String interventionTable() {
        StringBuilder out = new StringBuilder("# Intervention completions (authoritative run outputs)\n\n");
        out.append("| Base | Completion | + cutter | + assembler | + inspector | Source |\n|---|---:|---:|---:|---:|---|\n");
        for (InterventionRow row : GameDiagnosticCorpus.interventions()) {
            out.append("| ").append(row.id()).append(" | ").append(completion(attempt(row.id(), row.base()))).append(" | ")
                    .append(completion(attempt(row.id() + "+cut", GameDiagnosticCorpus.plusOne(row.base(), CUT)))).append(" | ")
                    .append(completion(attempt(row.id() + "+assemble", GameDiagnosticCorpus.plusOne(row.base(), ASSEMBLE)))).append(" | ")
                    .append(completion(attempt(row.id() + "+inspect", GameDiagnosticCorpus.plusOne(row.base(), INSPECT))))
                    .append(" | hand-derived, confirmed |\n");
        }
        LinearRoutingFamily g6 = GameDiagnosticCorpus.G6;
        out.append("| G6 | ").append(completion(attempt("G6", g6))).append(" | ")
                .append(completion(attempt("G6+cut", GameDiagnosticCorpus.plusOne(g6, CUT)))).append(" | ")
                .append(completion(attempt("G6+assemble", GameDiagnosticCorpus.plusOne(g6, ASSEMBLE)))).append(" | ")
                .append(completion(attempt("G6+inspect", GameDiagnosticCorpus.plusOne(g6, INSPECT))))
                .append(" | run-derived |\n");
        out.append("\n## G6 resource orders (run-derived)\n\n");
        Map<Long, List<String>> byCompletion = new java.util.TreeMap<>();
        for (List<String> order : permutations(g6.resources().stream().map(LinearRoutingFamily.Resource::name).toList())) {
            long tick = completion(attempt("G6-order", g6.withResourceOrder(order)));
            byCompletion.computeIfAbsent(tick, ignored -> new ArrayList<>()).add(String.join(", ", order));
        }
        byCompletion.forEach((tick, orders) -> out.append("- ").append(tick).append(": ").append(orders.size())
                .append(" orders, e.g. [").append(orders.getFirst()).append("]\n"));
        return out.toString();
    }

    private static List<List<String>> permutations(List<String> items) {
        if (items.isEmpty()) {
            return List.of(List.of());
        }
        List<List<String>> result = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            List<String> rest = new ArrayList<>(items);
            String head = rest.remove(i);
            for (List<String> tail : permutations(rest)) {
                List<String> permutation = new ArrayList<>();
                permutation.add(head);
                permutation.addAll(tail);
                result.add(permutation);
            }
        }
        return result;
    }

    /** The blinded owner packet: the minimal bundle's statements per shuffled item, and a separate key. */
    private static void writeWalkthrough() throws IOException {
        Contract minimal = new GameContracts.ClaimEvidenceBundle(false);
        Contract chained = new GameContracts.ClaimEvidenceBundle(true);
        List<String[]> items = new ArrayList<>();
        Map<String, String> truth = new LinkedHashMap<>();
        truth.put("G1", "Q2 truth: adding an inspector helps (67->56); a cutter or an assembler alone does not. Q4: the last unit"
                + " waited longest for CUT (33), the release-at-once backlog; CUT is not the limiting step.");
        truth.put("G2", "Q2 truth: adding an assembler helps (56->45). Q3: Inspector 2 is idle with no work at tick 17, yet removing"
                + " it costs 11 ticks; surplus is not decidable from the run.");
        truth.put("G4", "Q2 truth: adding a cutter helps (45->37). At tick 9 both twin slots are in use while completed-step credit"
                + " is 4 ticks; at completion the twin's credited ticks (48) exceed elapsed time (45).");
        truth.put("G5", "Q2 truth: adding an inspector helps (67->45); the CUT pool is the most occupied but adding a cutter does"
                + " nothing.");
        truth.put("G6", "Multi-eligible INSPECT waiting at tick 17 is not either eligible resource's queue. The completion chain"
                + " ties at tick 33; resource order alone moves completion between 46 and 47.");
        truth.put("G7", "Q2 truth: no single addition helps (68); only adding both an assembler and an inspector does (46).");
        truth.put("G8", "Q3: Assembler 2 is idle with no work at tick 11; removing it changes nothing (67): surplus for completion,"
                + " but not decidable from the run.");
        truth.put("G10", "Same run as G2 seen by a late joiner: current state is available, interval measurements are refused.");
        for (Attempt attempt : GameDiagnosticCorpus.auditAttempts()) {
            if (truth.containsKey(attempt.label())) {
                items.add(new String[] {attempt.label(), "attempt",
                        "Design: " + designLine(attempt) + "\n\n" + playerView(minimal.attempt(attempt)),
                        chainLine(chained.attempt(attempt)), truth.get(attempt.label())});
            }
        }
        Map<String, String> pairTruth = new LinkedHashMap<>();
        pairTruth.put("G1+inspect", "Controlled: one inspector added, 67->56.");
        pairTruth.put("G1+cut", "Controlled: one cutter added, no change.");
        pairTruth.put("G2+assemble", "Controlled: one assembler added, 56->45 (constraint migration from INSPECT to ASSEMBLE).");
        pairTruth.put("G3+cut", "Controlled: one cutter added, 45->37 (migration to CUT).");
        pairTruth.put("G8-remove-Assembler-2", "Controlled removal, no change: Assembler 2 was not needed to finish at 67.");
        pairTruth.put("G9-reorder", "Only the resource order changed, 11->9.");
        pairTruth.put("G1+assemble+inspect", "Confounded: singles give 0 (assembler) and -11 (inspector); together -22."
                + " Attribution is order-dependent.");
        pairTruth.put("G7+assemble+inspect", "Confounded interaction: each single change gives 0; together -22.");
        for (Pair pair : GameDiagnosticCorpus.pairs()) {
            if (pairTruth.containsKey(pair.id())) {
                items.add(new String[] {pair.id(), "comparison",
                        "First design: " + designLine(pair.first()) + "\n\nSecond design: " + designLine(pair.second())
                                + "\n\n" + playerView(minimal.compare(pair.first(), pair.second())),
                        "", pairTruth.get(pair.id())});
            }
        }
        Collections.shuffle(items, new Random(20261005L));
        StringBuilder packet = new StringBuilder("""
                # Blinded product-owner walkthrough packet

                Shuffled with a fixed seed; fixture identities, the oracle and the design notes are withheld. Each item is a
                completed attempt (one design run on its fixed workload) or a comparison of two attempts. Each item states the
                authored design; the bullets are exactly what the minimal claim-evidence bundle shows, in its order, with the
                named derivation behind any derived statement in brackets.

                Answer from the statements alone, before opening the key:

                - **Attempt items:** (1) what is waiting at the mid-run tick, and for which step? (2) which step limits this
                  design, if the statements let you say? (3) for any idle resource: is it starved, surplus, or can you not
                  tell? (4) what is the largest measured delay? Then read the optional completion-chain line and say whether it
                  clarifies, changes or misleads your answer to (2).
                - **Comparison items:** (5) what changed, and what changed in the outcome? (6) can you say which change
                  accounts for the difference?

                Record any wording that was ambiguous or that suggested more than it states.
                """);
        StringBuilder key = new StringBuilder("# Walkthrough key (do not read before answering)\n\n| Item | Fixture | Truth |\n|---|---|---|\n");
        for (int i = 0; i < items.size(); i++) {
            String[] item = items.get(i);
            String code = "W" + (i + 1);
            packet.append("\n## ").append(code).append(" (").append(item[1]).append(")\n\n").append(item[2]).append('\n');
            if (!item[3].isEmpty()) {
                packet.append("\nOptional completion-chain line: ").append(item[3]).append('\n');
            }
            key.append("| ").append(code).append(" | ").append(item[0]).append(" | ").append(item[4]).append(" |\n");
        }
        Files.writeString(OUT.resolve("walkthrough-packet.md"), packet.toString());
        Files.writeString(OUT.resolve("walkthrough-key.md"), key.toString());
    }

    private static String designLine(Attempt attempt) {
        LinearRoutingFamily design = attempt.design();
        String routing = design.steps().stream().map(step -> step.name() + " " + step.duration())
                .collect(Collectors.joining(" -> "));
        String resources = design.resources().stream()
                .map(r -> r.name() + " (" + GameDiagnostics.describe(r) + ")")
                .collect(Collectors.joining(", "));
        long quantity = attempt.evidence().observation(ExperimentEvidence.CLOSING_LABEL).orders().getFirst().requestedQuantity();
        return "routing " + routing + " ticks; one order of " + quantity + " units at tick 0; resources in order: " + resources + ".";
    }

    private static String playerView(List<Statement> statements) {
        return statements.stream()
                .map(s -> "- " + s.text() + s.method().filter(m -> s.kind() != Kind.REFUSAL).map(m -> " [" + m + "]").orElse(""))
                .collect(Collectors.joining("\n"));
    }

    private static String chainLine(List<Statement> statements) {
        return statements.stream()
                .filter(s -> s.question() == Question.Q2_LIMITING_STEP
                        && s.method().filter(GameEvidenceOracles.COMPLETION_CHAIN.name()::equals).isPresent())
                .map(Statement::text)
                .findFirst()
                .orElse("");
    }

    // =============================================================== helpers

    private static Attempt audit(String id) {
        return GameDiagnosticCorpus.auditAttempts().stream().filter(a -> a.label().equals(id)).findFirst().orElseThrow();
    }

    private static Pair pair(String id) {
        return GameDiagnosticCorpus.pairs().stream().filter(p -> p.id().equals(id)).findFirst().orElseThrow();
    }

    private static long completion(Attempt attempt) {
        return CompletionTickOracle.completionTick(attempt.evidence());
    }

    private static long waitingUnits(Attempt attempt) {
        return GameStatements.queued(attempt.evidence().observation(MID)).size();
    }

    private static boolean multiEligible(Attempt attempt, String step) {
        return attempt.design().eligibleResources(step).size() > 1;
    }

    private static ResourceObservation resource(RuntimeObservation observation, String name) {
        return observation.resources().stream().filter(r -> r.name().equals(name)).findFirst().orElseThrow();
    }

    private static void assertWaiting(Attempt attempt, Map<String, Long> expected, Set<String> shared) {
        OracleOutcome<List<WaitingAtStep>> outcome = new WaitingWorkByStepOracle(MID).evaluateOn(attempt.evidence());
        assertInstanceOf(OracleOutcome.Derived.class, outcome, attempt.label());
        List<WaitingAtStep> waiting = ((OracleOutcome.Derived<List<WaitingAtStep>>) outcome).value();
        Map<String, Long> actual = waiting.stream().collect(Collectors.toMap(WaitingAtStep::stepName, WaitingAtStep::waitingJobs));
        assertEquals(expected, actual, attempt.label());
        for (WaitingAtStep step : waiting) {
            assertEquals(shared.contains(step.stepName()), step.attribution() instanceof Attribution.SharedEligibleSet,
                    attempt.label() + " " + step.stepName());
        }
    }

    private static void assertIdleWithoutWork(Attempt attempt, String resource) {
        Statement idle = GameStatements.idleness(attempt, MID).stream()
                .filter(s -> s.facet("resource").filter(resource::equals).isPresent())
                .findFirst()
                .orElseThrow(() -> new AssertionError(resource + " is not idle in " + attempt.label()));
        assertEquals(Optional.of("true"), idle.facet("idleWithoutWork"), attempt.label() + " " + resource);
    }

    private static void assertCompletingUnit(Attempt attempt, long leadTime, List<Long> waits) {
        CompletingUnit unit = ((OracleOutcome.Derived<CompletingUnit>) new GameEvidenceOracles.CompletingUnitTimeline()
                .evaluateOn(attempt.evidence())).value();
        assertEquals(leadTime, unit.leadTime(), attempt.label());
        assertEquals(waits, unit.segments().stream().map(GameEvidenceOracles.UnitSegment::waitTicks).toList(), attempt.label());
    }

    private static void assertStepWaits(Attempt attempt, List<Long> totals) {
        DispatchProfileOracle.DispatchProfile profile = ((OracleOutcome.Derived<DispatchProfileOracle.DispatchProfile>)
                new DispatchProfileOracle(ExperimentEvidence.CLOSING_LABEL).evaluateOn(attempt.evidence())).value();
        assertEquals(totals, profile.waits().stream().map(DispatchProfileOracle.StepWait::totalWaitTicks).toList(), attempt.label());
    }

    private static CompletionChain chain(Attempt attempt) {
        return ((OracleOutcome.Derived<CompletionChain>) new GameEvidenceOracles.CompletionChainTrace()
                .evaluateOn(attempt.evidence())).value();
    }

    private static void assertPacing(Attempt attempt, Optional<String> step) {
        assertEquals(step, chain(attempt).uniquePacingStep().map(OperationStep::name), attempt.label());
    }
}
