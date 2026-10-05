package com.arcogine.research.experiment;

import com.arcogine.research.experiment.ExperimentFixture.WindowIntent;
import com.arcogine.research.experiment.GameDiagnostics.Attempt;
import com.arcogine.research.experiment.GameDiagnostics.Question;
import com.arcogine.research.experiment.LinearRoutingFamily.Resource;
import com.arcogine.research.experiment.ThreeStepRoutingFamily.Stage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The game diagnostic-evidence corpus: the strategy-space reference designs (G1-G8), the tracked
 * identity-order pair (G9), a late-joining evidence window (G10) and the tracked starter cases for
 * multi-eligible waiting and long concurrent steps (G11). Hand-derived ground truth lives in the
 * oracle note and in {@code GameDiagnosticEvidenceExperiment}, never in a run.
 */
final class GameDiagnosticCorpus {

    static final String CUT = ThreeStepRoutingFamily.CUT;
    static final String ASSEMBLE = ThreeStepRoutingFamily.ASSEMBLE;
    static final String INSPECT = ThreeStepRoutingFamily.INSPECT;
    static final String MID = "mid-run";
    static final long REFERENCE_QUANTITY = 12;
    private static final long DEADLINE = 1_000;

    /** G1: one resource per step. */
    static final LinearRoutingFamily G1 = CapacityCorpus.DEDICATED_LINE;
    /** G2: strategy-space S1, two dedicated inspectors. */
    static final LinearRoutingFamily G2 = CapacityCorpus.TWO_INSPECTORS;
    /** G3: G2 plus a second dedicated assembler. */
    static final LinearRoutingFamily G3 = appended(G2, Resource.of("Assembler 2", 1, ASSEMBLE));
    /** G4: strategy-space S3, one two-slot assembler. */
    static final LinearRoutingFamily G4 = new LinearRoutingFamily(G1.steps(), List.of(
            Resource.of("Cutter", 1, CUT),
            Resource.of("Twin Assembler", 2, ASSEMBLE),
            Resource.of("Inspector 1", 1, INSPECT),
            Resource.of("Inspector 2", 1, INSPECT)));
    /** G5: the accepted occupancy counterexample, the shared resource is the only inspector. */
    static final LinearRoutingFamily G5 = CapacityCorpus.SHARED_ONLY_INSPECTION;
    /** G6: strategy-space S2 in authored order. */
    static final LinearRoutingFamily G6 = CapacityCorpus.SHARED_ASSEMBLE_INSPECT;
    /** G7: ASSEMBLE and INSPECT co-binding, CUT 3, ASSEMBLE 5, INSPECT 5. */
    static final LinearRoutingFamily G7 =
            new ThreeStepRoutingFamily(Stage.of(3, 1), Stage.of(5, 1), Stage.of(5, 1)).asLinearFamily();
    /** G8: G1 plus a second dedicated assembler. */
    static final LinearRoutingFamily G8 = appended(G1, Resource.of("Assembler 2", 1, ASSEMBLE));
    /** G9: the tracked shared-resource pair, authored and reversed. */
    static final LinearRoutingFamily G9A = CapacityCorpus.SHARED_RESOURCE;
    static final LinearRoutingFamily G9B = CapacityCorpus.SHARED_RESOURCE.withReversedResourceOrder();

    private static final Map<String, Attempt> RUNS = new LinkedHashMap<>();

    private GameDiagnosticCorpus() {}

    static LinearRoutingFamily appended(LinearRoutingFamily family, Resource resource) {
        List<Resource> resources = new ArrayList<>(family.resources());
        resources.add(resource);
        return new LinearRoutingFamily(family.steps(), resources);
    }

    static LinearRoutingFamily without(LinearRoutingFamily family, String resourceName) {
        List<Resource> resources = new ArrayList<>(family.resources());
        resources.removeIf(resource -> resource.name().equals(resourceName));
        return new LinearRoutingFamily(family.steps(), resources);
    }

    /** {@code family} with one more single-slot resource dedicated to {@code step}, appended last, freshly named. */
    static LinearRoutingFamily plusOne(LinearRoutingFamily family, String step) {
        String base = switch (step) {
            case CUT -> "Cutter";
            case ASSEMBLE -> "Assembler";
            case INSPECT -> "Inspector";
            default -> throw new IllegalArgumentException(step);
        };
        int n = 2;
        while (hasName(family, base + " " + n)) {
            n++;
        }
        return appended(family, Resource.of(base + " " + n, 1, step));
    }

    private static boolean hasName(LinearRoutingFamily family, String name) {
        return family.resources().stream().anyMatch(resource -> resource.name().equals(name));
    }

    static ExperimentFixture fixture(String id, LinearRoutingFamily design, long quantity, Optional<Long> midTick, boolean lateJoin) {
        List<ExperimentStep> script = new ArrayList<>();
        script.add(ExperimentStep.submit(LinearRoutingFamily.PRODUCT, quantity, LinearRoutingFamily.UNIT_PRICE));
        if (midTick.isPresent()) {
            script.add(ExperimentStep.advanceUntil(midTick.get()));
            script.add(ExperimentStep.observe(MID));
            script.add(lateJoin ? ExperimentStep.discardEvents("joined-late") : ExperimentStep.captureEvents("through-mid-run"));
        }
        script.add(ExperimentStep.advanceToQuiescence(DEADLINE));
        script.add(ExperimentStep.captureEvents(lateJoin ? "after-joining" : "through-completion"));
        return new ExperimentFixture(id, design.model(), script,
                lateJoin ? WindowIntent.PARTIAL : WindowIntent.COMPLETE_RUN, List.of());
    }

    static Attempt attempt(String id, LinearRoutingFamily design, long quantity, Optional<Long> midTick, boolean lateJoin) {
        String key = id + "|" + GameDiagnostics.order(design) + "|" + design.steps() + "|" + quantity + "|" + midTick + "|" + lateJoin;
        return RUNS.computeIfAbsent(key, ignored -> new Attempt(
                id,
                design,
                ExperimentRunner.runAndReplay(fixture(id, design, quantity, midTick, lateJoin)),
                midTick.map(tick -> MID)));
    }

    static Attempt attempt(String id, LinearRoutingFamily design) {
        return attempt(id, design, REFERENCE_QUANTITY, Optional.empty(), false);
    }

    static Attempt attempt(String id, LinearRoutingFamily design, long midTick) {
        return attempt(id, design, REFERENCE_QUANTITY, Optional.of(midTick), false);
    }

    /** The single attempts every audit renders. */
    static List<Attempt> auditAttempts() {
        return List.of(
                attempt("G1", G1, 33),
                attempt("G2", G2, 17),
                attempt("G3", G3),
                attempt("G4", G4, 9),
                attempt("G5", G5),
                attempt("G6", G6, 17),
                attempt("G7", G7),
                attempt("G8", G8, 11),
                attempt("G9a", G9A, 2, Optional.empty(), false),
                attempt("G9b", G9B, 2, Optional.empty(), false),
                attempt("G10", G2, REFERENCE_QUANTITY, Optional.of(17L), true),
                attempt("G11a", StarterCorpus.MULTI_ELIGIBLE_FAMILY.asLinearFamily(), StarterCorpus.MULTI_ELIGIBLE_QUANTITY,
                        Optional.of(5L), false),
                attempt("G11b", StarterCorpus.LONG_STEP_PARALLEL_FAMILY.asLinearFamily(),
                        StarterCorpus.LONG_STEP_PARALLEL_QUANTITY, Optional.of(10L), false));
    }

    /** A comparison with its hand-derived ground truth. */
    record Pair(String id, Question question, Attempt first, Attempt second, long expectedFirst, long expectedSecond,
            int expectedChanges) {

        long expectedDelta() {
            return expectedSecond - expectedFirst;
        }
    }

    /** Bases with hand-derived single-addition interventions: base id, design, completion, +cut, +assemble, +inspect. */
    record InterventionRow(String id, LinearRoutingFamily base, long completion, long plusCut, long plusAssemble, long plusInspect) {

        List<String> helpfulSteps() {
            List<String> steps = new ArrayList<>();
            if (plusCut < completion) {
                steps.add(CUT);
            }
            if (plusAssemble < completion) {
                steps.add(ASSEMBLE);
            }
            if (plusInspect < completion) {
                steps.add(INSPECT);
            }
            return steps;
        }
    }

    static List<InterventionRow> interventions() {
        return List.of(
                new InterventionRow("G1", G1, 67, 67, 67, 56),
                new InterventionRow("G2", G2, 56, 56, 45, 56),
                new InterventionRow("G3", G3, 45, 37, 45, 45),
                new InterventionRow("G4", G4, 45, 37, 45, 45),
                new InterventionRow("G5", G5, 67, 67, 67, 45),
                new InterventionRow("G7", G7, 68, 68, 68, 68));
    }

    static List<Pair> pairs() {
        List<Pair> pairs = new ArrayList<>();
        for (InterventionRow row : interventions()) {
            Attempt base = attempt(row.id(), row.base());
            pairs.add(new Pair(row.id() + "+cut", Question.Q5_CONTROLLED_COMPARISON, base,
                    attempt(row.id() + "+cut", plusOne(row.base(), CUT)), row.completion(), row.plusCut(), 1));
            pairs.add(new Pair(row.id() + "+assemble", Question.Q5_CONTROLLED_COMPARISON, base,
                    attempt(row.id() + "+assemble", plusOne(row.base(), ASSEMBLE)), row.completion(), row.plusAssemble(), 1));
            pairs.add(new Pair(row.id() + "+inspect", Question.Q5_CONTROLLED_COMPARISON, base,
                    attempt(row.id() + "+inspect", plusOne(row.base(), INSPECT)), row.completion(), row.plusInspect(), 1));
        }
        pairs.add(new Pair("G2-remove-Inspector-2", Question.Q5_CONTROLLED_COMPARISON, attempt("G2", G2),
                attempt("G2-Inspector 2", without(G2, "Inspector 2")), 56, 67, 1));
        pairs.add(new Pair("G8-remove-Assembler-2", Question.Q5_CONTROLLED_COMPARISON, attempt("G8", G8),
                attempt("G8-Assembler 2", without(G8, "Assembler 2")), 67, 67, 1));
        pairs.add(new Pair("G9-reorder", Question.Q5_CONTROLLED_COMPARISON,
                attempt("G9a", G9A, 2, Optional.empty(), false), attempt("G9b", G9B, 2, Optional.empty(), false), 11, 9, 1));
        pairs.add(new Pair("G1+assemble+inspect", Question.Q6_CONFOUNDED_COMPARISON, attempt("G1", G1),
                attempt("G1+assemble+inspect", plusOne(plusOne(G1, ASSEMBLE), INSPECT)), 67, 45, 2));
        pairs.add(new Pair("G1+cut+inspect", Question.Q6_CONFOUNDED_COMPARISON, attempt("G1", G1),
                attempt("G1+cut+inspect", plusOne(plusOne(G1, CUT), INSPECT)), 67, 56, 2));
        pairs.add(new Pair("G7+assemble+inspect", Question.Q6_CONFOUNDED_COMPARISON, attempt("G7", G7),
                attempt("G7+assemble+inspect", plusOne(plusOne(G7, ASSEMBLE), INSPECT)), 68, 46, 2));
        return pairs;
    }
}
