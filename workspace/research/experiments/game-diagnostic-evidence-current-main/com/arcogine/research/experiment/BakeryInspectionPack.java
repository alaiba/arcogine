package com.arcogine.research.experiment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.process.JobObservation;
import com.arcogine.factory.process.ResourceObservation;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.research.experiment.ExperimentFixture.WindowIntent;
import com.arcogine.research.experiment.GameDiagnostics.Attempt;
import com.arcogine.research.experiment.GameDiagnostics.Statement;
import com.arcogine.research.experiment.GameEvidence.Occurrence;
import com.arcogine.research.experiment.GameEvidence.Occurrences;
import com.arcogine.research.experiment.LinearRoutingFamily.Resource;
import com.arcogine.research.experiment.LinearRoutingFamily.Step;
import com.arcogine.types.JobId;
import com.arcogine.types.JobStatus;
import com.arcogine.types.MachineId;
import com.arcogine.types.MachineState;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * The starvation and utilization inspection prototype: four bakery scenarios (MIX -> BAKE -> PACK) of
 * increasing complexity, each run recorded tick by tick, and thirteen missions over them. Research
 * custody only.
 *
 * <p>Two layers are kept apart. The <em>machine slot state</em> (working or idle) is read directly from
 * the supported observation at each tick. The <em>flow characterization</em> of an idle slot (starved,
 * or no work left) is a research-local derivation over the whole observed system at that tick.
 * Utilization over a period is working slot-ticks over available slot-ticks. The hand-derived values
 * asserted here are the pre-registered missions oracle (committed before this class existed); a
 * disagreement is investigated, never copied from a run. Writes {@code inspection.json} (what the
 * viewer shows) and {@code missions-key.json} (opened one mission at a time, after the player answers)
 * under the module's build directory.
 */
class BakeryInspectionPack {

    private static final Path OUT = Path.of("build", "game-inspection");
    static final String MIX = "MIX";
    static final String BAKE = "BAKE";
    static final String PACK = "PACK";
    private static final long HORIZON = 45;

    static final ResearchDefinition SLOT_STATE = new ResearchDefinition(
            "machine-slot-state",
            "At one supported observation: a machine's slots in use are its active jobs (working); its other"
                    + " slots are idle. Read directly; nothing is derived.",
            Set.of(EvidenceInput.OBSERVATIONS));

    static final ResearchDefinition FLOW = new ResearchDefinition(
            "flow-characterization-of-idle-slots",
            "At one supported observation, for a machine with an idle slot: 'starved' when some unfinished unit"
                    + " still has a remaining routing step the published model makes this machine eligible for"
                    + " (a Queued unit's current step onward, an InProgress unit's later steps); otherwise 'no work"
                    + " left'. A Queued unit whose current step this machine serves while it has an idle slot is"
                    + " a contradiction under the current Engine and is rejected, not characterized.",
            Set.of(EvidenceInput.PUBLISHED_MODEL, EvidenceInput.OBSERVATIONS));

    static final ResearchDefinition UTILIZATION = new ResearchDefinition(
            "utilization-over-a-period",
            "For a machine and a period [a, b): the sum over ticks a..b-1 of its working slots at each tick's"
                    + " observation, divided by its slots times (b - a). Work in progress counts up to the period"
                    + " boundary; every machine is online throughout in these scenarios.",
            Set.of(EvidenceInput.OBSERVATIONS));

    // =============================================================== designs

    static LinearRoutingFamily bakery(long mix, long bake, long pack, Resource... resources) {
        return new LinearRoutingFamily(List.of(new Step(MIX, mix), new Step(BAKE, bake), new Step(PACK, pack)),
                List.of(resources));
    }

    static Resource machine(String name, int slots, String step) {
        return Resource.of(name, slots, step);
    }

    static final LinearRoutingFamily S1 = bakery(2, 4, 1,
            machine("Mixer", 1, MIX), machine("Oven", 1, BAKE), machine("Packer", 1, PACK));
    static final LinearRoutingFamily S2 = bakery(2, 4, 1,
            machine("Mixer", 1, MIX), machine("Oven", 2, BAKE), machine("Packer", 1, PACK));
    static final LinearRoutingFamily S3A = bakery(2, 3, 4,
            machine("Mixer", 1, MIX), machine("Oven", 1, BAKE), machine("Packer 1", 1, PACK), machine("Packer 2", 1, PACK));
    static final LinearRoutingFamily S3B = bakery(2, 3, 4,
            machine("Mixer", 1, MIX), machine("Oven 1", 1, BAKE), machine("Packer", 1, PACK), machine("Oven 2", 1, BAKE));
    static final LinearRoutingFamily S4 = bakery(2, 4, 4,
            machine("Mixer", 1, MIX), machine("Oven", 1, BAKE), machine("Packer", 1, PACK));

    /** One more single-slot machine for {@code step}, appended, named with the next free number. */
    static LinearRoutingFamily plus(LinearRoutingFamily design, String step) {
        String base = switch (step) {
            case MIX -> "Mixer";
            case BAKE -> "Oven";
            case PACK -> "Packer";
            default -> throw new IllegalArgumentException(step);
        };
        long existing = design.resources().stream()
                .filter(r -> r.name().equals(base) || r.name().startsWith(base + " "))
                .count();
        return GameDiagnosticCorpus.appended(design, machine(base + " " + (existing + 1), 1, step));
    }

    // =============================================================== recorded runs

    /** One run recorded at every tick up to the horizon, with its complete supported event stream. */
    record Run(String id, String label, String role, Optional<String> parent, LinearRoutingFamily design, long quantity,
            ExperimentEvidence evidence) {

        long finish() {
            return evidence.observation(ExperimentEvidence.CLOSING_LABEL).orders().getFirst().completedAt().value();
        }

        RuntimeObservation at(long tick) {
            return evidence.observation("t" + Math.min(tick, HORIZON));
        }

        Attempt attempt() {
            return new Attempt(id, design, evidence, Optional.empty());
        }
    }

    private static final Map<String, Run> RUNS = new HashMap<>();

    static Run run(String id, String label, String role, Optional<String> parent, LinearRoutingFamily design,
            long quantity) {
        return RUNS.computeIfAbsent(id, ignored -> {
            List<ExperimentStep> script = new ArrayList<>();
            script.add(ExperimentStep.submit(LinearRoutingFamily.PRODUCT, quantity, LinearRoutingFamily.UNIT_PRICE));
            script.add(ExperimentStep.observe("t0"));
            for (long tick = 1; tick <= HORIZON; tick++) {
                script.add(ExperimentStep.advanceUntil(tick));
                script.add(ExperimentStep.observe("t" + tick));
            }
            script.add(ExperimentStep.advanceToQuiescence(1_000));
            script.add(ExperimentStep.captureEvents("whole-run"));
            ExperimentFixture fixture = new ExperimentFixture("bakery-" + id, design.model(), script,
                    WindowIntent.COMPLETE_RUN, List.of());
            return new Run(id, label, role, parent, design, quantity, ExperimentRunner.run(fixture));
        });
    }

    record Scenario(String id, String title, List<String> intro, List<Run> runs) {}

    static List<Scenario> scenarios() {
        Run s1 = run("S1", "First bake", "base", Optional.empty(), S1, 4);
        Run s2 = run("S2", "The big oven", "base", Optional.empty(), S2, 6);
        Run a = run("A", "Version A (second packer)", "version", Optional.empty(), S3A, 8);
        Run b = run("B", "Version B (second oven)", "version", Optional.empty(), S3B, 8);
        List<Run> s3 = List.of(a, b,
                run("A-packer2", "Version A without Packer 2", "variant", Optional.of("A"),
                        GameDiagnosticCorpus.without(S3A, "Packer 2"), 8),
                run("A+mixer", "Version A + Mixer 2", "variant", Optional.of("A"), plus(S3A, MIX), 8),
                run("A+oven", "Version A + Oven 2", "variant", Optional.of("A"), plus(S3A, BAKE), 8),
                run("A+packer", "Version A + Packer 3", "variant", Optional.of("A"), plus(S3A, PACK), 8),
                run("B-oven2", "Version B without Oven 2", "variant", Optional.of("B"),
                        GameDiagnosticCorpus.without(S3B, "Oven 2"), 8),
                run("B+mixer", "Version B + Mixer 2", "variant", Optional.of("B"), plus(S3B, MIX), 8),
                run("B+oven", "Version B + Oven 3", "variant", Optional.of("B"), plus(S3B, BAKE), 8),
                run("B+packer", "Version B + Packer 2", "variant", Optional.of("B"), plus(S3B, PACK), 8));
        Run s4 = run("S4", "Two busy machines", "base", Optional.empty(), S4, 8);
        List<Run> s4runs = List.of(s4,
                run("S4+mixer", "+ Mixer 2", "variant", Optional.of("S4"), plus(S4, MIX), 8),
                run("S4+oven", "+ Oven 2", "variant", Optional.of("S4"), plus(S4, BAKE), 8),
                run("S4+packer", "+ Packer 2", "variant", Optional.of("S4"), plus(S4, PACK), 8),
                run("S4+oven+packer", "+ Oven 2 and Packer 2", "variant", Optional.of("S4"), plus(plus(S4, BAKE), PACK), 8));
        return List.of(
                new Scenario("S1", "First bake", List.of(
                        "Your bakery makes loaves. Every loaf goes through three steps: MIX, then BAKE, then PACK.",
                        "Here there is one machine per step, and an order of 4 loaves arrives all at once at tick 0.",
                        "Nothing is shown until you ask. Start with 'design', then try 'at 3' or 'machine Oven'."), List.of(s1)),
                new Scenario("S2", "The big oven", List.of(
                        "Same recipe, but the Oven now has 2 slots: it can bake two loaves at the same time.",
                        "The order is 6 loaves this time."), List.of(s2)),
                new Scenario("S3", "Two upgrade options", List.of(
                        "A slower recipe (MIX 2, BAKE 3, PACK 4 ticks) and an order of 8 loaves.",
                        "There are two versions of the bakery: version A has a second packer, version B a second oven.",
                        "You can switch between them and to pre-computed variants of each: type 'variants'."), s3),
                new Scenario("S4", "Two busy machines", List.of(
                        "MIX 2, BAKE 4, PACK 4 ticks, one machine per step, and 8 loaves.",
                        "Pre-computed variants with extra machines are available: type 'variants'."), s4runs));
    }

    // =============================================================== the two layers, per tick

    record SlotState(String machine, int slots, int working, List<Integer> units, String idleReason) {}

    record Waiting(String step, long count, List<Integer> units, String on, List<String> eligible) {}

    record Snapshot(long tick, List<SlotState> machines, List<Waiting> waiting, long finished, long total) {}

    static Snapshot snapshot(Run run, long tick) {
        RuntimeObservation observation = run.at(tick);
        FactoryModel model = run.evidence().publishedModel();
        List<OperationStepDefinition> routing = model.operations().getFirst().steps();
        Map<JobId, Integer> unitOf = new HashMap<>();
        observation.jobs().forEach(job -> unitOf.put(job.jobId(), (int) job.ordinalWithinOrder() + 1));
        List<SlotState> machines = new ArrayList<>();
        for (ResourceObservation resource : observation.resources()) {
            if (resource.state() == MachineState.Offline) {
                throw new AssertionError(run.id() + " @" + tick + ": " + resource.name() + " is offline; out of scope");
            }
            int working = resource.activeJobIds().size();
            List<Integer> units = resource.activeJobIds().stream().map(unitOf::get).sorted().toList();
            String reason = null;
            if (working < resource.concurrency()) {
                MachineId id = resource.machineId();
                for (JobObservation job : observation.jobs()) {
                    if (job.status() == JobStatus.Queued && routing.get(job.currentStep()).eligibleResources().contains(id)) {
                        throw new AssertionError(run.id() + " @" + tick + ": " + resource.name()
                                + " has an idle slot while unit " + unitOf.get(job.jobId()) + " waits for it");
                    }
                }
                boolean needed = observation.jobs().stream()
                        .filter(job -> job.status() != JobStatus.Completed)
                        .anyMatch(job -> {
                            int from = job.status() == JobStatus.Queued ? job.currentStep() : job.currentStep() + 1;
                            for (int s = from; s < routing.size(); s++) {
                                if (routing.get(s).eligibleResources().contains(id)) {
                                    return true;
                                }
                            }
                            return false;
                        });
                reason = needed ? "starved" : "no work left";
            }
            machines.add(new SlotState(resource.name(), resource.concurrency(), working, units, reason));
        }
        List<Waiting> waiting = new ArrayList<>();
        for (int s = 0; s < routing.size(); s++) {
            int stepIndex = s;
            List<Integer> units = observation.jobs().stream()
                    .filter(job -> job.status() == JobStatus.Queued && job.currentStep() == stepIndex)
                    .map(job -> unitOf.get(job.jobId()))
                    .sorted()
                    .toList();
            if (units.isEmpty()) {
                continue;
            }
            List<String> eligible = routing.get(s).eligibleResources().stream().sorted()
                    .map(id -> GameStatements.resourceName(model, id)).toList();
            waiting.add(new Waiting(routing.get(s).name(), units.size(), units,
                    eligible.size() == 1 ? eligible.getFirst() : null, eligible));
        }
        var order = observation.orders().getFirst();
        return new Snapshot(tick, machines, waiting, order.completedQuantity(), order.requestedQuantity());
    }

    /** Per machine, cumulative working, starved and no-work-left slot-ticks: index t covers ticks 0..t-1. */
    static Map<String, long[][]> cumulative(Run run) {
        long finish = run.finish();
        Map<String, long[][]> totals = new LinkedHashMap<>();
        for (Resource r : run.design().resources()) {
            totals.put(r.name(), new long[3][(int) finish + 1]);
        }
        for (long t = 0; t < finish; t++) {
            Snapshot snapshot = snapshot(run, t);
            for (SlotState m : snapshot.machines()) {
                long[][] c = totals.get(m.machine());
                int i = (int) t;
                int idle = m.slots() - m.working();
                c[0][i + 1] = c[0][i] + m.working();
                c[1][i + 1] = c[1][i] + ("starved".equals(m.idleReason()) ? idle : 0);
                c[2][i + 1] = c[2][i] + ("no work left".equals(m.idleReason()) ? idle : 0);
            }
        }
        return totals;
    }

    static long working(Run run, String machine, long from, long to) {
        long[][] c = cumulative(run).get(machine);
        return c[0][(int) to] - c[0][(int) from];
    }

    static long starved(Run run, String machine, long from, long to) {
        long[][] c = cumulative(run).get(machine);
        return c[1][(int) to] - c[1][(int) from];
    }

    static long noWorkLeft(Run run, String machine, long from, long to) {
        long[][] c = cumulative(run).get(machine);
        return c[2][(int) to] - c[2][(int) from];
    }

    static SlotState machineAt(Run run, String machine, long tick) {
        return snapshot(run, tick).machines().stream().filter(m -> m.machine().equals(machine)).findFirst().orElseThrow();
    }

    static Run find(String id) {
        return scenarios().stream().flatMap(s -> s.runs().stream()).filter(r -> r.id().equals(id)).findFirst().orElseThrow();
    }

    // =============================================================== oracle assertions (pre-registered values)

    @Test
    void runsReproduceThePreRegisteredMissionsOracle() {
        Run s1 = find("S1");
        assertEquals(19, s1.finish());
        assertEquals(Set.of("Packer"), snapshot(s1, 5).machines().stream()
                .filter(m -> m.working() < m.slots()).map(SlotState::machine).collect(Collectors.toSet()));
        assertEquals("starved", machineAt(s1, "Packer", 8).idleReason());
        assertEquals("no work left", machineAt(s1, "Mixer", 12).idleReason());
        assertEquals(8, working(s1, "Mixer", 0, 19));
        assertEquals(16, working(s1, "Oven", 0, 19));
        assertEquals(4, working(s1, "Packer", 0, 19));
        assertEquals(15, starved(s1, "Packer", 0, 19));
        assertEquals(0, noWorkLeft(s1, "Packer", 0, 19));

        Run s2 = find("S2");
        assertEquals(17, s2.finish());
        assertEquals(2, machineAt(s2, "Oven", 9).working());
        assertEquals(24, working(s2, "Oven", 0, 17));
        assertEquals(6, starved(s2, "Oven", 0, 17));
        assertEquals(4, noWorkLeft(s2, "Oven", 0, 17));
        assertEquals(20, working(s2, "Oven", 4, 14));

        assertEquals(30, find("A").finish());
        assertEquals("starved", machineAt(find("A"), "Packer 2", 13).idleReason());
        assertEquals(37, find("A-packer2").finish());
        assertEquals(37, find("B").finish());
        assertEquals("starved", machineAt(find("B"), "Oven 2", 7).idleReason());
        assertEquals(37, find("B-oven2").finish());
        assertEquals(30, find("A+mixer").finish());
        assertEquals(23, find("A+oven").finish());
        assertEquals(30, find("A+packer").finish());
        assertEquals(37, find("B+mixer").finish());
        assertEquals(37, find("B+oven").finish());
        assertEquals(23, find("B+packer").finish());

        Run s4 = find("S4");
        assertEquals(38, s4.finish());
        assertEquals(32, working(s4, "Oven", 0, 38));
        assertEquals(32, working(s4, "Packer", 0, 38));
        assertEquals(16, working(s4, "Mixer", 0, 38));
        assertEquals(38, find("S4+mixer").finish());
        assertEquals(38, find("S4+oven").finish());
        assertEquals(38, find("S4+packer").finish());
        assertEquals(24, find("S4+oven+packer").finish());
    }

    @Test
    void everySlotTickIsInExactlyOneStateAndTheBreakdownSumsToThePeriod() {
        for (Scenario scenario : scenarios()) {
            for (Run run : scenario.runs()) {
                long finish = run.finish();
                for (Resource r : run.design().resources()) {
                    long total = working(run, r.name(), 0, finish) + starved(run, r.name(), 0, finish)
                            + noWorkLeft(run, r.name(), 0, finish);
                    assertEquals(r.concurrency() * finish, total, run.id() + " " + r.name());
                }
                Snapshot end = snapshot(run, finish);
                assertEquals(end.total(), end.finished(), run.id() + " finished at its finish tick");
                assertTrue(end.machines().stream().allMatch(m -> m.working() == 0 && "no work left".equals(m.idleReason())),
                        run.id() + ": after the finish every machine has no work left, never starved");
            }
        }
    }

    // =============================================================== missions

    record Part(String id, String ask, Map<String, Object> format, Map<String, Object> key) {}

    record Mission(int number, String scenario, String design, String type, String prompt, String objective,
            List<Part> parts, String official, String explanation, String howToFind) {}

    private static Map<String, Object> format(String kind, String hint, List<String> options) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("kind", kind);
        f.put("hint", hint);
        if (!options.isEmpty()) {
            f.put("options", options);
        }
        return f;
    }

    private static Map<String, Object> accept(String kind, Object value) {
        Map<String, Object> k = new LinkedHashMap<>();
        k.put("kind", kind);
        k.put("value", value);
        return k;
    }

    private static Map<String, Object> ratio(long working, long available) {
        Map<String, Object> k = new LinkedHashMap<>();
        k.put("kind", "percent");
        k.put("working", working);
        k.put("available", available);
        k.put("tolerancePoints", 1.5);
        return k;
    }

    private static String pct(long working, long available, String unit) {
        return Math.round(100.0 * working / available) + "% (" + working + " of " + available + " " + unit + ")";
    }

    static List<Mission> missions() {
        Run s1 = find("S1");
        Run s2 = find("S2");
        Run a = find("A");
        Run s4 = find("S4");
        long f1 = s1.finish();
        long f2 = s2.finish();
        long f4 = s4.finish();
        String machineHint = "Type a machine name.";
        String reasonHint = "Type: starved  or  no work left";
        String pctHint = "Type a percentage (e.g. 50%) or working ticks of available ticks (e.g. 8 of 16).";
        String yesNoHint = "Type: yes  or  no";
        List<Mission> missions = new ArrayList<>();
        missions.add(new Mission(1, "S1", "S1", "Inspect", "Which machine is idle at tick 5?",
                "Read a machine's state at a moment: working or idle.",
                List.of(new Part("a", "Which machine is idle at tick 5?", format("machine", machineHint, List.of()),
                        accept("machine", "Packer"))),
                "The Packer.",
                "At tick 5 the Mixer is working on loaf 3 and the Oven on loaf 1; the Packer holds no loaf, so it is idle.",
                "Type 'at 5'."));
        missions.add(new Mission(2, "S1", "S1", "Inspect", "At tick 8 the Packer is idle. Why?",
                "'Starved': idle while work it will need is still upstream.",
                List.of(new Part("a", "Is the Packer starved, or does it have no work left?",
                        format("choice", reasonHint, List.of()), accept("choice", "starved"))),
                "Starved.",
                "Loaf 1 is already packed, but loaves 2-4 still need packing: loaf 2 is in the Oven and loaves 3-4 are waiting"
                        + " for it. Work the Packer will need exists upstream but has not reached it yet.",
                "Type 'at 8' (the Packer's line says why it is idle); 'explain starved' for the definition."));
        missions.add(new Mission(3, "S1", "S1", "Inspect", "At tick 12 the Mixer is idle. Why?",
                "Tell starvation apart from simply being finished.",
                List.of(new Part("a", "Is the Mixer starved, or does it have no work left?",
                        format("choice", reasonHint, List.of()), accept("choice", "no work left"))),
                "No work left.",
                "All 4 loaves were mixed by tick 8, so no loaf will ever need the Mixer again. That is not starvation.",
                "Type 'at 12' or 'machine Mixer'."));
        missions.add(new Mission(4, "S1", "S1", "Analyze",
                "Which machine has the highest utilization over the whole run, and how much is it?",
                "Utilization = time working / time available, over a stated period.",
                List.of(new Part("a", "Which machine has the highest utilization over the whole run?",
                                format("machine", machineHint, List.of()), accept("machine", "Oven")),
                        new Part("b", "What is its utilization over the whole run?", format("percent", pctHint, List.of()),
                                ratio(working(s1, "Oven", 0, f1), f1))),
                "The Oven, " + pct(working(s1, "Oven", 0, f1), f1, "ticks") + ".",
                "Over ticks 0-" + f1 + " the Oven worked " + working(s1, "Oven", 0, f1) + " of " + f1 + " ticks, the Mixer "
                        + working(s1, "Mixer", 0, f1) + " and the Packer " + working(s1, "Packer", 0, f1) + ".",
                "Type 'util' (the whole run is the default period); 'explain utilization'."));
        missions.add(new Mission(5, "S1", "S1", "Analyze", "Look at the Packer's idle time over the whole run.",
                "An idle-time breakdown: a machine can be idle mostly because it is starved.",
                List.of(new Part("a", "How many ticks was the Packer idle over the whole run?",
                                format("integer", "Type a number of ticks.", List.of()),
                                accept("integer", starved(s1, "Packer", 0, f1) + noWorkLeft(s1, "Packer", 0, f1))),
                        new Part("b", "Of those, how many ticks was it starved?",
                                format("integer", "Type a number of ticks.", List.of()),
                                accept("integer", starved(s1, "Packer", 0, f1)))),
                "Idle " + (starved(s1, "Packer", 0, f1) + noWorkLeft(s1, "Packer", 0, f1)) + " ticks, starved for all "
                        + starved(s1, "Packer", 0, f1) + " of them.",
                "The Packer needs 1 tick per loaf, but loaves only leave the Oven every 4 ticks. Between loaves it is"
                        + " starved: the Oven sets its pace. Its low utilization says it waits for upstream work, not that it"
                        + " is unnecessary.",
                "Type 'machine Packer' (timeline and breakdown) or 'util'."));
        missions.add(new Mission(6, "S2", "S2", "Inspect", "At tick 9, how many of the Oven's 2 slots are working?",
                "A machine can have several slots; state is per slot.",
                List.of(new Part("a", "How many of the Oven's slots are working at tick 9?",
                        format("integer", "Type a number: 0, 1 or 2.", List.of()),
                        accept("integer", (long) machineAt(s2, "Oven", 9).working()))),
                "2: loaves 3 and 4.",
                "The Oven bakes loaf 3 (ticks 6-10) and loaf 4 (ticks 8-12) at the same time.",
                "Type 'at 9' or 'machine Oven'."));
        missions.add(new Mission(7, "S2", "S2", "Analyze", "Look at the Oven's utilization over the whole run.",
                "Per-slot utilization; idle slots at the start and the end lower it.",
                List.of(new Part("a", "What is the Oven's utilization over the whole run?",
                                format("percent", pctHint + " Count slot-ticks: 2 slots for 1 tick = 2.", List.of()),
                                ratio(working(s2, "Oven", 0, f2), 2 * f2)),
                        new Part("b", "It is busy most of the time, so why is it not 100%?",
                                format("text", "Type a short reason (one line).", List.of()), accept("text", ""))),
                pct(working(s2, "Oven", 0, f2), 2 * f2, "slot-ticks") + ".",
                "Its slots are starved at the start (" + starved(s2, "Oven", 0, f2) + " slot-ticks while the first loaves are"
                        + " mixed) and have no work left at the end (" + noWorkLeft(s2, "Oven", 0, f2)
                        + " slot-ticks after the last loaf went in).",
                "Type 'util', then 'machine Oven' for where the idle slot-ticks are."));
        missions.add(new Mission(8, "S2", "S2", "Analyze", "What is the Oven's utilization from tick 4 to tick 14?",
                "The period changes the answer: always state the period.",
                List.of(new Part("a", "What is the Oven's utilization from tick 4 to tick 14?",
                        format("percent", pctHint, List.of()), ratio(working(s2, "Oven", 4, 14), 2 * 10))),
                pct(working(s2, "Oven", 4, 14), 20, "slot-ticks") + ".",
                "In the middle of the run both slots are always baking. The whole-run figure is lower only because of the"
                        + " start and the end.",
                "Type 'util 4 14'; 'explain period'."));
        missions.add(new Mission(9, "S3", "A", "Inspect", "In version A, Packer 2 is idle at tick 13. Why?",
                "Apply starvation in a design with two machines for one step.",
                List.of(new Part("a", "Is Packer 2 starved, or does it have no work left?",
                        format("choice", reasonHint, List.of()), accept("choice", "starved"))),
                "Starved.",
                "Packer 2 finished loaf 2 at tick 12; its next loaf (4) is still in the Oven until tick 14.",
                "Type 'at 13' (version A is shown); 'machine Packer 2'."));
        missions.add(new Mission(10, "S3", "A", "Experiment",
                "Is Packer 2 needed, meaning the order would finish later without it?",
                "'Needed' is answered by an experiment (remove it and compare), not by looking at one run.",
                List.of(new Part("a", "Is Packer 2 needed?", format("choice", yesNoHint, List.of()), accept("choice", "yes"))),
                "Yes.",
                "Without Packer 2 the order finishes at tick " + find("A-packer2").finish() + " instead of " + a.finish() + ".",
                "Type 'variants', then 'compare A-packer2'."));
        missions.add(new Mission(11, "S3", "B", "Experiment",
                "In version B, Oven 2 is also idle and starved at times, just like Packer 2 in version A. Is Oven 2 needed?",
                "Idle and starved look the same for a needed and an unneeded machine; only the experiment tells them apart.",
                List.of(new Part("a", "Is Oven 2 needed?", format("choice", yesNoHint, List.of()), accept("choice", "no"))),
                "No.",
                "Without Oven 2 the order still finishes at tick " + find("B-oven2").finish() + ". Packing sets the pace in"
                        + " version B, so the second oven only shortens the wait in front of the Packer.",
                "Type 'variants', then 'compare B-oven2'."));
        missions.add(new Mission(12, "S4", "S4", "Analyze",
                "Which machine or machines have the highest utilization over the whole run?",
                "Read utilization across machines; ties happen.",
                List.of(new Part("a", "Which machine or machines have the highest utilization?",
                        format("machines", "Type one name, or several names separated by 'and'.", List.of()),
                        accept("machines", List.of("Oven", "Packer")))),
                "The Oven and the Packer, tied at " + pct(working(s4, "Oven", 0, f4), f4, "ticks") + " each.",
                "The Mixer worked " + working(s4, "Mixer", 0, f4) + " of " + f4 + " ticks.",
                "Type 'util'."));
        missions.add(new Mission(13, "S4", "S4", "Experiment",
                "Use the variants: where does added capacity make the order finish sooner?",
                "High utilization does not tell you where added capacity helps; two steps can limit the order together.",
                List.of(new Part("a", "Would adding a second oven make the order finish sooner?",
                                format("choice", yesNoHint, List.of()), accept("choice", "no")),
                        new Part("b", "Would adding a second packer make the order finish sooner?",
                                format("choice", yesNoHint, List.of()), accept("choice", "no")),
                        new Part("c", "Which change does make it finish sooner?",
                                format("choice", "Type the letter.", List.of("a) add a mixer", "b) add an oven", "c) add a packer",
                                        "d) add an oven and a packer", "e) none of these")),
                                accept("choice", "d"))),
                "No; no; only adding an oven and a packer together (" + f4 + " -> " + find("S4+oven+packer").finish() + ").",
                "The Oven and the Packer limit the order together: with only one of them doubled, the other still sets the"
                        + " same pace. This is where the bottleneck concept begins.",
                "Type 'variants', then 'compare S4+oven', 'compare S4+packer', 'compare S4+oven+packer'."));
        return missions;
    }

    @Test
    void missionAnswersAgreeWithThePreRegisteredOracle() {
        List<Mission> missions = missions();
        assertEquals(13, missions.size());
        assertEquals("16 of 19", missions.get(3).parts().get(1).key().get("working") + " of "
                + missions.get(3).parts().get(1).key().get("available"));
        assertEquals(15L, missions.get(4).parts().get(0).key().get("value"));
        assertEquals(15L, missions.get(4).parts().get(1).key().get("value"));
        assertEquals(2L, missions.get(5).parts().get(0).key().get("value"));
        assertEquals("24 of 34", missions.get(6).parts().get(0).key().get("working") + " of "
                + missions.get(6).parts().get(0).key().get("available"));
        assertEquals("20 of 20", missions.get(7).parts().get(0).key().get("working") + " of "
                + missions.get(7).parts().get(0).key().get("available"));
    }

    // =============================================================== writing the pack

    @Test
    void writePack() throws IOException {
        Files.createDirectories(OUT);
        Map<String, Object> pack = new LinkedHashMap<>();
        pack.put("pack", "game-inspection-starvation-utilization");
        pack.put("definitions", List.of(definition(SLOT_STATE), definition(FLOW), definition(UTILIZATION),
                definition(GameEvidence.OCCURRENCES)));
        pack.put("glossary", glossary());
        List<Object> scenarioList = new ArrayList<>();
        for (Scenario scenario : scenarios()) {
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("id", scenario.id());
            s.put("title", scenario.title());
            s.put("intro", scenario.intro());
            List<Object> designs = new ArrayList<>();
            for (Run run : scenario.runs()) {
                designs.add(design(run));
            }
            s.put("designs", designs);
            scenarioList.add(s);
        }
        pack.put("scenarios", scenarioList);
        List<Object> missionList = new ArrayList<>();
        Map<String, Object> key = new LinkedHashMap<>();
        for (Mission m : missions()) {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("number", m.number());
            out.put("scenario", m.scenario());
            out.put("design", m.design());
            out.put("type", m.type());
            out.put("prompt", m.prompt());
            out.put("objective", m.objective());
            out.put("parts", m.parts().stream().map(p -> {
                Map<String, Object> part = new LinkedHashMap<>();
                part.put("id", p.id());
                part.put("ask", p.ask());
                part.put("format", p.format());
                return part;
            }).toList());
            missionList.add(out);
            Map<String, Object> k = new LinkedHashMap<>();
            Map<String, Object> parts = new LinkedHashMap<>();
            m.parts().forEach(p -> parts.put(p.id(), p.key()));
            k.put("parts", parts);
            k.put("official", m.official());
            k.put("explanation", m.explanation());
            k.put("howToFind", m.howToFind());
            key.put(String.valueOf(m.number()), k);
        }
        pack.put("missions", missionList);
        Files.writeString(OUT.resolve("inspection.json"), GameWalkthroughPack.Json.write(pack) + "\n");
        Files.writeString(OUT.resolve("missions-key.json"), GameWalkthroughPack.Json.write(key) + "\n");
    }

    private static Map<String, Object> definition(ResearchDefinition d) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("name", d.name());
        out.put("text", d.statement());
        return out;
    }

    private static Map<String, Object> design(Run run) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("id", run.id());
        d.put("label", run.label());
        d.put("role", run.role());
        run.parent().ifPresent(p -> d.put("parent", p));
        d.put("steps", run.design().steps().stream().map(s -> {
            Map<String, Object> step = new LinkedHashMap<>();
            step.put("name", s.name());
            step.put("ticks", s.duration());
            return step;
        }).toList());
        d.put("machines", run.design().resources().stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", r.name());
            m.put("steps", new TreeSet<>(r.eligibleSteps()).stream().toList());
            m.put("slots", r.concurrency());
            return m;
        }).toList());
        d.put("loaves", run.quantity());
        long finish = run.finish();
        d.put("finish", finish);
        List<Object> snapshots = new ArrayList<>();
        for (long t = 0; t <= finish; t++) {
            Snapshot s = snapshot(run, t);
            Map<String, Object> snap = new LinkedHashMap<>();
            snap.put("tick", t);
            snap.put("machines", s.machines().stream().map(m -> {
                Map<String, Object> out = new LinkedHashMap<>();
                out.put("name", m.machine());
                out.put("slots", m.slots());
                out.put("working", m.working());
                out.put("loaves", m.units());
                if (m.idleReason() != null) {
                    out.put("idle", m.idleReason());
                }
                return out;
            }).toList());
            snap.put("waiting", s.waiting().stream().map(w -> {
                Map<String, Object> out = new LinkedHashMap<>();
                out.put("step", w.step());
                out.put("loaves", w.units());
                if (w.on() != null) {
                    out.put("on", w.on());
                } else {
                    out.put("eligible", w.eligible());
                }
                return out;
            }).toList());
            snap.put("finished", s.finished());
            snapshots.add(snap);
        }
        d.put("snapshots", snapshots);
        Map<String, Object> totals = new LinkedHashMap<>();
        cumulative(run).forEach((name, c) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("working", toList(c[0]));
            m.put("starved", toList(c[1]));
            m.put("noWorkLeft", toList(c[2]));
            totals.put(name, m);
        });
        d.put("cumulative", totals);
        d.put("units", journeys(run));
        run.parent().ifPresent(parentId -> d.put("comparison",
                new GamePlainContract.PlainBundle().compare(find(parentId).attempt(), run.attempt()).stream()
                        .map(Statement::text).toList()));
        return d;
    }

    private static List<Long> toList(long[] values) {
        List<Long> out = new ArrayList<>();
        for (long v : values) {
            out.add(v);
        }
        return out;
    }

    private static List<Object> journeys(Run run) {
        OracleOutcome<Occurrences> outcome =
                new GameEvidenceOracles.StepOccurrences(ExperimentEvidence.CLOSING_LABEL).evaluateOn(run.evidence());
        Occurrences occurrences = ((OracleOutcome.Derived<Occurrences>) outcome).value();
        FactoryModel model = run.evidence().publishedModel();
        Map<Integer, List<Occurrence>> byUnit = new java.util.TreeMap<>();
        occurrences.occurrences().forEach(o -> byUnit.computeIfAbsent(o.unit(), u -> new ArrayList<>()).add(o));
        List<Object> out = new ArrayList<>();
        byUnit.forEach((unit, steps) -> {
            Map<String, Object> u = new LinkedHashMap<>();
            u.put("loaf", unit);
            u.put("steps", steps.stream().sorted(java.util.Comparator.comparingInt(Occurrence::stepIndex)).map(o -> {
                Map<String, Object> s = new LinkedHashMap<>();
                s.put("step", o.step().name());
                s.put("machine", GameStatements.resourceName(model, o.machineId()));
                s.put("ready", o.readyTick());
                s.put("start", o.dispatchTick());
                s.put("end", o.completionTick().orElseThrow());
                return s;
            }).toList());
            out.add(u);
        });
        return out;
    }

    private static Map<String, Object> glossary() {
        Map<String, Object> g = new LinkedHashMap<>();
        g.put("tick", "One unit of simulated time. 'At tick 5' means the state right after everything that happens at"
                + " tick 5.");
        g.put("step", "One stage every loaf goes through: MIX, then BAKE, then PACK. Each step takes a fixed number of"
                + " ticks.");
        g.put("machine", "Something that does a step. A loaf is worked on by one machine at a time.");
        g.put("slot", "Room on a machine for one loaf. Most machines have 1 slot; a 2-slot oven bakes two loaves at"
                + " once.");
        g.put("loaf", "One unit of the order. Loaves are numbered in the order they were released.");
        g.put("working", "A slot is working while it holds a loaf: from the tick the loaf starts that step until the"
                + " tick it finishes it.");
        g.put("idle", "A slot is idle when it holds no loaf. Idle says nothing about why: see 'starved' and 'no work"
                + " left'.");
        g.put("waiting", "A loaf is waiting when it is ready for its next step but no machine has started it. Here a"
                + " loaf never waits while a machine that can do its step is idle: work starts as soon as a slot is"
                + " free.");
        g.put("starved", "An idle machine is starved when work it will need is still upstream: some unfinished loaf"
                + " will need this machine's step later, but none has reached it yet. It is decided from the whole"
                + " bakery at that tick, not from the machine alone. Starved does NOT mean the machine is unnecessary,"
                + " or that it is the problem: often a slower step before it sets the pace.");
        g.put("no work left", "An idle machine has no work left when no unfinished loaf will ever need it again, or"
                + " the order is done. Being idle at the end is not starvation.");
        g.put("blocked", "In real factories a machine is blocked when it has finished an item but cannot pass it on,"
                + " because the space after it is full. This bakery has unlimited space between steps, so nothing is"
                + " ever blocked here. (A future concept.)");
        g.put("utilization", "The share of a machine's available time spent working, over a stated period: working"
                + " slot-ticks / (slots x ticks in the period). A 2-slot oven with one loaf for one tick is 50% for that"
                + " tick. Always state the period: the same machine can be 70% over the whole run and 100% in the"
                + " middle. The most utilized machine is NOT automatically the one to upgrade.");
        g.put("period", "A stretch of ticks. 'From tick 4 to tick 14' covers ticks 4 up to 13: ten ticks. The whole run"
                + " is from tick 0 to the tick the order finished.");
        g.put("variant", "A pre-computed copy of a design with one change (or, where stated, two). Switch to it to"
                + " inspect it; compare it to see how the finish time changed.");
        g.put("needed", "A machine is needed if the order would finish later without it. One run cannot show that;"
                + " removing the machine in a variant and comparing finish times can.");
        g.put("finish", "The tick at which the last loaf of the order is packed.");
        return g;
    }
}
