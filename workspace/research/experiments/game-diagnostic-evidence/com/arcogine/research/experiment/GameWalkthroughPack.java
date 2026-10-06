package com.arcogine.research.experiment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.research.experiment.GameDiagnostics.Attempt;
import com.arcogine.research.experiment.GameDiagnostics.Contract;
import com.arcogine.research.experiment.GameDiagnostics.Kind;
import com.arcogine.research.experiment.GameDiagnostics.Statement;
import com.arcogine.research.experiment.LinearRoutingFamily.Resource;
import com.arcogine.research.experiment.LinearRoutingFamily.Step;
import com.arcogine.research.experiment.WaitingWorkByStepOracle.Attribution;
import com.arcogine.research.experiment.WaitingWorkByStepOracle.WaitingAtStep;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * The second owner walkthrough: fresh PRESS -> WELD -> PAINT scenarios rendered through the revised
 * plain bundle (C3c), every shown line and every try generated from real runs. The hand-derived values
 * asserted here are the pre-registered oracle (committed before this class existed); a disagreement
 * is investigated, never copied from the run. Writes {@code pack.json} (what the session shows) and
 * {@code key.json} (opened only at the reveal) under the module's build directory.
 */
class GameWalkthroughPack {

    private static final Path OUT = Path.of("build", "game-diagnostic-walkthrough");
    private static final String PRESS = "PRESS";
    private static final String WELD = "WELD";
    private static final String PAINT = "PAINT";
    private static final long UNITS = 8;
    private static final Contract PLAIN = new GamePlainContract.PlainBundle();

    // =============================================================== designs

    static LinearRoutingFamily line(long press, long weld, long paint, Resource... resources) {
        return new LinearRoutingFamily(List.of(new Step(PRESS, press), new Step(WELD, weld), new Step(PAINT, paint)),
                List.of(resources));
    }

    static Resource machine(String name, String step) {
        return Resource.of(name, 1, step);
    }

    static final LinearRoutingFamily TUTORIAL =
            line(1, 3, 1, machine("Press", PRESS), machine("Welder", WELD), machine("Painter", PAINT));
    static final LinearRoutingFamily A =
            line(4, 2, 5, machine("Press", PRESS), machine("Welder", WELD), machine("Painter", PAINT));
    static final LinearRoutingFamily D = line(1, 6, 2, machine("Press", PRESS), machine("Welder 1", WELD),
            machine("Welder 2", WELD), machine("Painter", PAINT));
    static final LinearRoutingFamily B = line(3, 1, 5, machine("Press", PRESS), machine("Welder", WELD),
            machine("Painter 1", PAINT), machine("Painter 2", PAINT));
    /**
     * Welder 2 is authored last so that removing it renumbers no other machine: with positional
     * identity, a removal before another machine would also change that machine's identity, which the
     * change set must (and does) report as a second change.
     */
    static final LinearRoutingFamily C = line(2, 3, 4, machine("Press", PRESS), machine("Welder 1", WELD),
            machine("Painter", PAINT), machine("Welder 2", WELD));
    static final LinearRoutingFamily E =
            line(2, 4, 4, machine("Press", PRESS), machine("Welder", WELD), machine("Painter", PAINT));
    static final LinearRoutingFamily F =
            line(3, 5, 2, machine("Press", PRESS), machine("Welder", WELD), machine("Painter", PAINT));
    static final LinearRoutingFamily K2_BASE =
            line(2, 3, 4, machine("Press", PRESS), machine("Welder", WELD), machine("Painter", PAINT));

    /** One more single-slot machine for {@code step}, appended, named with the next free number. */
    static LinearRoutingFamily plus(LinearRoutingFamily design, String step) {
        String base = switch (step) {
            case PRESS -> "Press";
            case WELD -> "Welder";
            case PAINT -> "Painter";
            default -> throw new IllegalArgumentException(step);
        };
        long existing = design.resources().stream()
                .filter(r -> r.name().equals(base) || r.name().startsWith(base + " "))
                .count();
        return GameDiagnosticCorpus.appended(design, machine(base + " " + (existing + 1), step));
    }

    static Attempt run(String id, LinearRoutingFamily design, long quantity, Optional<Long> snapshot, boolean lateJoin) {
        return GameDiagnosticCorpus.attempt("walkthrough-" + id, design, quantity, snapshot, lateJoin);
    }

    static Attempt run(String id, LinearRoutingFamily design, long snapshot) {
        return run(id, design, UNITS, Optional.of(snapshot), false);
    }

    static Attempt run(String id, LinearRoutingFamily design) {
        return run(id, design, UNITS, Optional.empty(), false);
    }

    // =============================================================== the session content

    record Option(String id, String text) {}

    record Ask(String id, String text, List<Option> options) {}

    record Try(String id, String label, Attempt after, long expected) {}

    private static final List<Option> YES_NO_RUN =
            List.of(new Option("1", "Yes"), new Option("2", "No"), new Option("3", "Can't tell from this run"));
    private static final List<Option> YES_NO_RUNS =
            List.of(new Option("1", "Yes"), new Option("2", "No"), new Option("3", "Can't tell from these runs"));
    private static final List<Option> WHICH_MACHINE = List.of(
            new Option("1", "Yes, a press"),
            new Option("2", "Yes, a welder"),
            new Option("3", "Yes, a painter"),
            new Option("4", "No single machine would"),
            new Option("5", "Can't tell from this run"));

    private static Ask whichMachine(String id) {
        return new Ask(id, "Would adding one machine make this order finish sooner?", WHICH_MACHINE);
    }

    private static Ask untried(String id) {
        return new Ask(id, "From the two runs you have now seen, would adding one of the machines you did NOT try make"
                + " the order finish sooner?", YES_NO_RUNS);
    }

    private static List<Try> addTries(String id, LinearRoutingFamily base, long press, long weld, long paint) {
        return List.of(
                new Try("press", "Add a press", run(id + "+press", plus(base, PRESS)), press),
                new Try("welder", "Add a welder", run(id + "+welder", plus(base, WELD)), weld),
                new Try("painter", "Add a painter", run(id + "+painter", plus(base, PAINT)), paint));
    }

    private static List<Try> withRemoval(String id, LinearRoutingFamily base, String removed, long removedCompletion,
            long press, long weld, long paint) {
        List<Try> tries = new ArrayList<>();
        tries.add(new Try("remove", "Remove " + removed,
                run(id + "-" + removed, GameDiagnosticCorpus.without(base, removed)), removedCompletion));
        tries.addAll(addTries(id, base, press, weld, paint));
        return tries;
    }

    // =============================================================== oracle assertions (pre-registered values)

    @Test
    void scenarioRunsReproduceThePreRegisteredOracle() {
        Attempt tutorial = run("T", TUTORIAL, 3, Optional.of(3L), false);
        assertEquals(11, completion(tutorial));
        assertEquals(11, completion(run("T+press", plus(TUTORIAL, PRESS), 3, Optional.empty(), false)));
        assertEquals(8, completion(run("T+welder", plus(TUTORIAL, WELD), 3, Optional.empty(), false)));
        assertEquals(11, completion(run("T+painter", plus(TUTORIAL, PAINT), 3, Optional.empty(), false)));
        assertWaiting(tutorial, Map.of(WELD, 2L), Set.of());
        assertSnapshotAt(tutorial, 3);

        Attempt a = run("A", A, 16);
        assertEquals(46, completion(a));
        assertSnapshotAt(a, 16);
        assertWaiting(a, Map.of(PRESS, 3L), Set.of());
        assertIdle(a, Set.of());
        assertLongestWait(a, PRESS, 28);
        assertTries(addTries("A", A, 46, 46, 39));

        Attempt d = run("D", D, 6);
        assertEquals(29, completion(d));
        assertSnapshotAt(d, 6);
        assertWaiting(d, Map.of(WELD, 4L, PRESS, 1L), Set.of(WELD));
        assertIdle(d, Set.of("Painter"));
        assertLongestWait(d, WELD, 12);
        assertTries(addTries("D", D, 29, 23, 28));

        Attempt b = run("B", B, 12);
        assertEquals(30, completion(b));
        assertSnapshotAt(b, 12);
        assertWaiting(b, Map.of(PRESS, 3L), Set.of());
        assertIdle(b, Set.of("Painter 2"));
        assertTries(withRemoval("B", B, "Painter 2", 44, 25, 30, 30));

        Attempt c = run("C", C, 7);
        assertEquals(37, completion(c));
        assertSnapshotAt(c, 7);
        assertWaiting(c, Map.of(PRESS, 4L, PAINT, 1L), Set.of());
        assertIdle(c, Set.of("Welder 2"));
        assertTries(withRemoval("C", C, "Welder 2", 37, 37, 37, 23));

        Attempt e = run("E", E);
        assertEquals(38, completion(e));
        assertTries(addTries("E", E, 38, 38, 38));
        assertEquals(24, completion(run("E+welder+painter", plus(plus(E, WELD), PAINT))));

        Attempt f = run("F", F, UNITS, Optional.of(12L), true);
        // A late joiner has no complete event history, so the event-based completion oracle refuses; the
        // finish time it is shown is the observation's own completion fact.
        assertEquals(45, GameStatements.completionTick(f));
        assertSnapshotAt(f, 12);
        assertWaiting(f, Map.of(PRESS, 3L, WELD, 2L), Set.of());
        assertIdle(f, Set.of("Painter"));

        assertEquals(23, completion(run("D+welder", plus(D, WELD))));
        assertEquals(37, completion(run("K2", K2_BASE)));
        assertEquals(30, completion(run("K2+press+painter", plus(plus(K2_BASE, PRESS), PAINT))));
        assertEquals(37, completion(run("K2+press", plus(K2_BASE, PRESS))));
        assertEquals(30, completion(run("K2+painter", plus(K2_BASE, PAINT))));
    }

    @Test
    void everyShownLineIsTraceableAndPlain() {
        List<String> findings = new ArrayList<>();
        for (List<Statement> shown : shownStatements()) {
            for (Statement s : shown) {
                if (s.kind() == Kind.REFUSAL) {
                    continue;
                }
                if (s.support().isEmpty()) {
                    findings.add("no cited evidence: " + s.text());
                }
                boolean derived = Set.of(Kind.BOUNDARY_COUNT, Kind.EVENT_INTERVAL, Kind.AGGREGATE_MEASUREMENT,
                        Kind.NAMED_INTERPRETATION).contains(s.kind());
                if (derived && s.method().isEmpty()) {
                    findings.add("derived without a named definition: " + s.text());
                }
                if (s.kind() == Kind.NAMED_INTERPRETATION || s.kind() == Kind.AGGREGATE_MEASUREMENT) {
                    findings.add("the plain bundle shows no verdict or aggregate: " + s.text());
                }
                String text = s.text().toLowerCase(Locale.ROOT);
                GameDiagnosticEvidenceExperiment.OVERSTATING.keySet().stream()
                        .filter(text::contains)
                        .forEach(word -> findings.add("'" + word + "': " + s.text()));
            }
        }
        assertTrue(findings.isEmpty(), String.join("\n", findings));
    }

    @Test
    void theLateJoinerIsShownRefusalsNotIntervals() {
        List<Statement> shown = PLAIN.attempt(run("F", F, UNITS, Optional.of(12L), true));
        assertTrue(shown.stream().noneMatch(s -> s.kind() == Kind.EVENT_INTERVAL));
        assertEquals(2, shown.stream().filter(s -> s.facet("refusalOf").filter("interval-measurement"::equals).isPresent())
                .count());
    }

    @Test
    void writePack() throws IOException {
        Files.createDirectories(OUT);
        Map<String, Object> pack = new LinkedHashMap<>();
        Map<String, Object> key = new LinkedHashMap<>();
        pack.put("pack", "game-diagnostic-walkthrough-2");
        pack.put("contract", PLAIN.id());
        pack.put("glossary", List.of(
                "Tick: one unit of simulated time.",
                "Waiting to start STEP: the unit is ready for that step, but no machine has started it.",
                "Idle: the machine has nothing in progress.",
                "Needed: the order would finish later without this machine.",
                "Try: change one thing, run the same order again, and compare finish times.",
                "Can't tell from this run: what is shown does not settle the question; a try might."));
        List<Object> scenarios = new ArrayList<>();
        scenarios.add(tutorialScenario());
        scenarios.add(attemptScenario("A", "Factory 1", run("A", A, 16),
                List.of(new Ask("A1", "At tick 16, which units are waiting, and for which step?", List.of(
                                new Option("1", "3 units waiting to start PRESS; nothing else is waiting"),
                                new Option("2", "3 units waiting to start PRESS and 1 waiting to start PAINT"),
                                new Option("3", "Nothing is waiting"),
                                new Option("4", "Can't tell from this run"))),
                        whichMachine("A2"),
                        new Ask("A3", "The last unit waited longest to start PRESS (28 ticks). Does that mean adding a"
                                + " press would make the order finish sooner?", YES_NO_RUN)),
                addTries("A", A, 46, 46, 39), untried("A4")));
        key.put("A1", answer("1", "Snapshot: 3 units (6-8) wait for PRESS; nothing waits for WELD or PAINT."));
        key.put("A2", answer("5", "One run does not tell. The runs show: a painter 46 -> 39; a press or a welder: no change."));
        key.put("A3", answer("3", "Can't tell from this run. In fact adding a press changes nothing"
                + " (46 -> 46): the longest wait is not where a machine helps here."));
        key.put("A4", answerByTry(Map.of("press", "3", "welder", "3", "painter", "3"),
                "Two runs show one change; they do not show what an untried machine would do."));

        scenarios.add(attemptScenario("D", "Factory 2", run("D", D, 6),
                List.of(new Ask("D1", "At tick 6, how many units are waiting to start WELD, and which welder will do"
                                + " them?", List.of(
                                new Option("1", "4 units, 2 assigned to each welder"),
                                new Option("2", "4 units on each welder (8 in total)"),
                                new Option("3", "4 units, not assigned to either welder yet"),
                                new Option("4", "Can't tell from this run"))),
                        new Ask("D2", "Could this order finish without the Painter?", YES_NO_RUN),
                        whichMachine("D3")),
                addTries("D", D, 29, 23, 28), untried("D4")));
        key.put("D1", answer("3", "4 units (3-6) wait for WELD in the shared backlog; neither welder's own queue holds them."));
        key.put("D2", answer("2", "The Painter is the only machine that can do PAINT."));
        key.put("D3", answer("5", "One run does not tell. The runs show: a welder 29 -> 23, a painter 29 -> 28, a press: no"
                + " change."));
        key.put("D4", answerByTry(Map.of("press", "3", "welder", "3", "painter", "3"),
                "Two runs show one change; they do not show what an untried machine would do."));

        scenarios.add(attemptScenario("B", "Factory 3", run("B", B, 12),
                List.of(new Ask("B1", "At tick 12, Painter 2 is idle. Is Painter 2 needed (would the order finish later"
                        + " without it)?", YES_NO_RUN)),
                withRemoval("B", B, "Painter 2", 44, 25, 30, 30),
                new Ask("B2", "From what you have now seen, is Painter 2 needed?", YES_NO_RUNS)));
        key.put("B1", answer("3", "One run does not tell. Removing Painter 2 makes the order finish at 44 instead of 30:"
                + " it is needed."));
        key.put("B2", answerByTry(Map.of("remove", "1", "press", "3", "welder", "3", "painter", "3"),
                "Only the removal run shows it: 30 -> 44, so Painter 2 is needed. Other tries do not tell."));

        scenarios.add(attemptScenario("C", "Factory 4", run("C", C, 7),
                List.of(new Ask("C1", "At tick 7, Welder 2 is idle. Is Welder 2 needed (would the order finish later"
                        + " without it)?", YES_NO_RUN)),
                withRemoval("C", C, "Welder 2", 37, 37, 37, 23),
                new Ask("C2", "From what you have now seen, is Welder 2 needed?", YES_NO_RUNS)));
        key.put("C1", answer("3", "One run does not tell. Removing Welder 2 leaves the finish at 37: it is not needed for"
                + " this order. Its snapshot looks just like Factory 3's Painter 2."));
        key.put("C2", answerByTry(Map.of("remove", "2", "press", "3", "welder", "3", "painter", "3"),
                "Only the removal run shows it: 37 -> 37, so Welder 2 is not needed. Other tries do not tell."));

        scenarios.add(attemptScenario("E", "Factory 5", run("E", E), List.of(whichMachine("E1")),
                addTries("E", E, 38, 38, 38), untried("E2")));
        key.put("E1", answer("5", "One run does not tell. In fact no single machine helps (all 38); only a welder and a"
                + " painter together do (38 -> 24)."));
        key.put("E2", answerByTry(Map.of("press", "3", "welder", "3", "painter", "3"),
                "One try that changed nothing does not show what the other machines would do."));

        scenarios.add(attemptScenario("F", "Factory 6", run("F", F, UNITS, Optional.of(12L), true),
                List.of(new Ask("F1", "At tick 12, which units are waiting, and for which step?", List.of(
                                new Option("1", "3 waiting to start PRESS and 2 waiting to start WELD"),
                                new Option("2", "3 waiting to start PRESS only"),
                                new Option("3", "2 waiting to start WELD only"),
                                new Option("4", "Can't tell from this run"))),
                        new Ask("F2", "How long did the last unit to finish wait, in total, before its steps started?",
                                List.of(new Option("1", "12 ticks"), new Option("2", "45 ticks"),
                                        new Option("3", "This view does not show it"), new Option("4", "0 ticks")))),
                List.of(), null));
        key.put("F1", answer("1", "Snapshot: 3 units (6-8) wait for PRESS; 2 units (3-4) wait for WELD."));
        key.put("F2", answer("3", "This view joined late and refuses waiting times; tick 12 is only the snapshot time."));

        scenarios.add(comparisonScenario("K1", "Comparison 1", run("D", D), run("D+welder", plus(D, WELD)),
                List.of(new Ask("K1", "What changed, and what happened?", List.of(
                        new Option("1", "Added Welder 3; the order finished 6 ticks sooner"),
                        new Option("2", "Added Welder 3; no change"),
                        new Option("3", "Added a press; the order finished 6 ticks sooner"),
                        new Option("4", "Can't tell"))))));
        key.put("K1", answer("1", "One change, 29 -> 23."));

        scenarios.add(comparisonScenario("K2", "Comparison 2", run("K2", K2_BASE),
                run("K2+press+painter", plus(plus(K2_BASE, PRESS), PAINT)),
                List.of(new Ask("K2a", "What changed?", List.of(
                                new Option("1", "Added Press 2 and Painter 2"),
                                new Option("2", "Added Painter 2 only"),
                                new Option("3", "Added Press 2 only"))),
                        new Ask("K2b", "The order finished 7 ticks sooner. How many of those 7 ticks came from adding"
                                + " Painter 2?", List.of(
                                new Option("1", "All 7"), new Option("2", "About half"), new Option("3", "None"),
                                new Option("4", "Can't tell from this pair"))))));
        key.put("K2a", answer("1", "Two changes at once."));
        key.put("K2b", answer("4", "The pair cannot apportion it. Separate runs happen to show the painter alone gives"
                + " all 7 (37 -> 30) and the press alone none; the pair does not license that."));

        pack.put("scenarios", scenarios);
        Files.writeString(OUT.resolve("pack.json"), Json.write(pack) + "\n");
        Files.writeString(OUT.resolve("key.json"), Json.write(key) + "\n");
    }

    // =============================================================== scenario builders

    private static Map<String, Object> tutorialScenario() {
        Attempt tutorial = run("T", TUTORIAL, 3, Optional.of(3L), false);
        Map<String, Object> scenario = attemptScenario("T", "Tutorial (not scored)", tutorial,
                List.of(new Ask("T1", "Practice: at tick 3, how many units are waiting to start WELD?", List.of(
                        new Option("1", "0"), new Option("2", "1"), new Option("3", "2"), new Option("4", "3")))),
                List.of(new Try("press", "Add a press", run("T+press", plus(TUTORIAL, PRESS), 3, Optional.empty(), false),
                        11)),
                null);
        scenario.put("kind", "tutorial");
        scenario.put("intro", List.of(
                "Each factory makes one order of identical units. Every unit goes through the steps in order.",
                "You see the design, a snapshot of one moment (sometimes), the result, and a list of what the run cannot"
                        + " tell you. Type 'more' for each machine's work times, 'evidence' for where each line comes"
                        + " from, '?' for the glossary.",
                "Then you answer a few questions by number, and you get one try: change one machine and run the same order"
                        + " again."));
        scenario.put("practiceAnswer", "3");
        scenario.put("practiceExplanation", "The snapshot line says 2 units (units 2-3) are waiting to start WELD on Welder.");
        scenario.put("tryExplanation", "A try changes one thing and runs the same order again. Here adding a press did not"
                + " change the finish time. In each factory you choose your own try.");
        return scenario;
    }

    private static Map<String, Object> attemptScenario(String id, String title, Attempt attempt, List<Ask> asks,
            List<Try> tries, Ask followUp) {
        Map<String, Object> scenario = new LinkedHashMap<>();
        scenario.put("id", id);
        scenario.put("kind", "attempt");
        scenario.put("title", title);
        scenario.put("design", designLines(attempt));
        scenario.put("statements", lines(PLAIN.attempt(attempt)));
        scenario.put("questions", asks.stream().map(GameWalkthroughPack::ask).toList());
        List<Object> tryList = new ArrayList<>();
        for (Try t : tries) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", t.id());
            entry.put("label", t.label());
            entry.put("design", designLines(t.after()));
            entry.put("statements", lines(PLAIN.compare(attempt, t.after())));
            tryList.add(entry);
        }
        scenario.put("tries", tryList);
        if (followUp != null) {
            scenario.put("followUp", ask(followUp));
        }
        return scenario;
    }

    private static Map<String, Object> comparisonScenario(String id, String title, Attempt first, Attempt second,
            List<Ask> asks) {
        Map<String, Object> scenario = new LinkedHashMap<>();
        scenario.put("id", id);
        scenario.put("kind", "comparison");
        scenario.put("title", title);
        scenario.put("design", designLines(first));
        scenario.put("secondDesign", designLines(second));
        scenario.put("statements", lines(PLAIN.compare(first, second)));
        scenario.put("questions", asks.stream().map(GameWalkthroughPack::ask).toList());
        return scenario;
    }

    private static Map<String, Object> ask(Ask ask) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", ask.id());
        out.put("text", ask.text());
        out.put("options", ask.options().stream().map(o -> {
            Map<String, Object> option = new LinkedHashMap<>();
            option.put("id", o.id());
            option.put("text", o.text());
            return option;
        }).toList());
        return out;
    }

    private static Map<String, Object> answer(String option, String explanation) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("answer", option);
        out.put("explanation", explanation);
        return out;
    }

    private static Map<String, Object> answerByTry(Map<String, String> byTry, String explanation) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("byTry", new java.util.TreeMap<>(byTry));
        out.put("explanation", explanation);
        return out;
    }

    private static List<Object> lines(List<Statement> statements) {
        List<Object> out = new ArrayList<>();
        for (Statement s : statements) {
            Map<String, Object> line = new LinkedHashMap<>();
            line.put("section", s.facet("section").orElse(GamePlainContract.RESULT));
            line.put("text", s.text());
            line.put("kind", s.kind().name());
            line.put("method", s.method().orElse(""));
            line.put("evidence", s.support().summary());
            out.add(line);
        }
        return out;
    }

    private static List<String> designLines(Attempt attempt) {
        LinearRoutingFamily design = attempt.design();
        String routing = design.steps().stream().map(step -> step.name() + " " + step.duration())
                .collect(Collectors.joining(" -> "));
        String machines = design.resources().stream()
                .map(r -> r.name() + " (" + GameDiagnostics.describe(r) + ")")
                .collect(Collectors.joining(", "));
        long quantity = attempt.evidence().observation(ExperimentEvidence.CLOSING_LABEL).orders().getFirst().requestedQuantity();
        return List.of(
                "Steps (ticks each): " + routing + ".",
                "Order: " + quantity + " units, all released at tick 0.",
                "Machines, in order: " + machines + ".");
    }

    private static List<List<Statement>> shownStatements() {
        List<List<Statement>> shown = new ArrayList<>();
        shown.add(PLAIN.attempt(run("T", TUTORIAL, 3, Optional.of(3L), false)));
        shown.add(PLAIN.attempt(run("A", A, 16)));
        shown.add(PLAIN.attempt(run("D", D, 6)));
        shown.add(PLAIN.attempt(run("B", B, 12)));
        shown.add(PLAIN.attempt(run("C", C, 7)));
        shown.add(PLAIN.attempt(run("E", E)));
        shown.add(PLAIN.attempt(run("F", F, UNITS, Optional.of(12L), true)));
        for (Try t : addTries("A", A, 46, 46, 39)) {
            shown.add(PLAIN.compare(run("A", A, 16), t.after()));
        }
        shown.add(PLAIN.compare(run("D", D), run("D+welder", plus(D, WELD))));
        shown.add(PLAIN.compare(run("K2", K2_BASE), run("K2+press+painter", plus(plus(K2_BASE, PRESS), PAINT))));
        return shown;
    }

    // =============================================================== helpers

    private static long completion(Attempt attempt) {
        return CompletionTickOracle.completionTick(attempt.evidence());
    }

    private static void assertTries(List<Try> tries) {
        for (Try t : tries) {
            assertEquals(t.expected(), completion(t.after()), t.after().label());
        }
    }

    private static void assertWaiting(Attempt attempt, Map<String, Long> expected, Set<String> shared) {
        OracleOutcome<List<WaitingAtStep>> outcome =
                new WaitingWorkByStepOracle(GameDiagnosticCorpus.MID).evaluateOn(attempt.evidence());
        List<WaitingAtStep> waiting = ((OracleOutcome.Derived<List<WaitingAtStep>>) outcome).value();
        assertEquals(expected, waiting.stream().collect(Collectors.toMap(WaitingAtStep::stepName, WaitingAtStep::waitingJobs)),
                attempt.label());
        for (WaitingAtStep step : waiting) {
            assertEquals(shared.contains(step.stepName()), step.attribution() instanceof Attribution.SharedEligibleSet,
                    attempt.label() + " " + step.stepName());
        }
    }

    /** Every snapshot tick was chosen to carry events, so the observation's own time equals the request. */
    private static void assertSnapshotAt(Attempt attempt, long tick) {
        assertEquals(tick, GameStatements.tickOf(attempt, GameDiagnosticCorpus.MID), attempt.label());
    }

    private static void assertIdle(Attempt attempt, Set<String> idleWithoutWork) {
        Set<String> actual = GamePlainContract.idle(attempt, GameDiagnosticCorpus.MID).stream()
                .filter(s -> s.facet("idleWithoutWork").filter("true"::equals).isPresent())
                .map(s -> s.facet("resource").orElseThrow())
                .collect(Collectors.toSet());
        assertEquals(idleWithoutWork, actual, attempt.label());
    }

    private static void assertLongestWait(Attempt attempt, String step, long ticks) {
        Statement last = GamePlainContract.lastUnit(attempt);
        assertEquals(Optional.of(step), last.facet("longestWaitStep"), attempt.label());
        assertEquals(Optional.of(String.valueOf(ticks)), last.facet("longestWaitTicks"), attempt.label());
    }

    /** A minimal deterministic JSON writer for maps (insertion order), lists, strings and numbers. */
    static final class Json {

        private Json() {}

        static String write(Object value) {
            StringBuilder out = new StringBuilder();
            write(value, out, 0);
            return out.toString();
        }

        private static void write(Object value, StringBuilder out, int indent) {
            if (value instanceof Map<?, ?> map) {
                if (map.isEmpty()) {
                    out.append("{}");
                    return;
                }
                out.append("{\n");
                int i = 0;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    out.append("  ".repeat(indent + 1));
                    string(String.valueOf(entry.getKey()), out);
                    out.append(": ");
                    write(entry.getValue(), out, indent + 1);
                    out.append(++i < map.size() ? ",\n" : "\n");
                }
                out.append("  ".repeat(indent)).append('}');
            } else if (value instanceof List<?> list) {
                if (list.isEmpty()) {
                    out.append("[]");
                    return;
                }
                out.append("[\n");
                for (int i = 0; i < list.size(); i++) {
                    out.append("  ".repeat(indent + 1));
                    write(list.get(i), out, indent + 1);
                    out.append(i + 1 < list.size() ? ",\n" : "\n");
                }
                out.append("  ".repeat(indent)).append(']');
            } else if (value instanceof Number || value instanceof Boolean) {
                out.append(value);
            } else {
                string(String.valueOf(value), out);
            }
        }

        private static void string(String s, StringBuilder out) {
            out.append('"');
            for (char c : s.toCharArray()) {
                switch (c) {
                    case '"' -> out.append("\\\"");
                    case '\\' -> out.append("\\\\");
                    case '\n' -> out.append("\\n");
                    case '\r' -> out.append("\\r");
                    case '\t' -> out.append("\\t");
                    default -> {
                        if (c < 0x20 || c > 0x7e) {
                            out.append(String.format("\\u%04x", (int) c));
                        } else {
                            out.append(c);
                        }
                    }
                }
            }
            out.append('"');
        }
    }
}
