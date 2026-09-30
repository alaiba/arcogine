package com.arcogine.factory.research;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.process.OrderObservation;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.factory.process.RuntimeEventType;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.factory.research.ExperimentFixture.WindowIntent;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Research-local experiment for the non-spatial factory-design strategy-space investigation.
 *
 * <p>It executes the pass-1 protocol pre-registered in the research evidence workspace
 * ({@code workspace/research/investigations/factory-design-game-strategy-space-protocol.md}). It is
 * kept in that workspace and copied into this package only while it runs; it is not a maintained
 * corpus fixture, and nothing it derives is an Engine fact, a game-owned analytic or a public API.
 *
 * <p>Operational choices the protocol leaves to the harness, fixed here before any run:
 *
 * <ul>
 *   <li>cell-level constraint-capacity, irrelevant-capacity and migration verdicts use the cell's
 *       cheapest feasible frontier design (first by design key within its tie class) as the base;
 *       the starter design and every feasible frontier design of the reference cell are also reported;
 *   <li>migration means a step that was not in the base's active set becomes active after relieving
 *       the base's constraint, with completion strictly earlier than the base;
 *   <li>irrelevant capacity is only judged when the at-constraint addition is material;
 *   <li>probe designs above a catalogue quantity limit are still simulated (they probe the Engine,
 *       they are not candidates) and are flagged.
 * </ul>
 */
class StrategySpaceExperimentTest {

    static final ProductId PRODUCT = new ProductId(1);
    static final long OPERATION_ID = 1;
    static final double UNIT_PRICE = 10.0;
    static final long QUIESCENCE_DEADLINE = 10_000;
    static final int CUT = 0;
    static final int ASSEMBLE = 1;
    static final int INSPECT = 2;
    static final List<String> STEP_NAMES = List.of("CUT", "ASSEMBLE", "INSPECT");
    static final Path OUT = Path.of("build", "strategy-space");

    static final List<Profile> PROFILES = List.of(new Profile("P1", 2, 6, 3), new Profile("P2", 3, 4, 5));
    static final List<Long> QUANTITIES = List.of(6L, 12L, 24L);
    static final List<String> RULES = List.of("UNIFORM", "WORK");
    static final List<Integer> DELTAS = List.of(75, 90, 100);
    static final List<Integer> PHIS = List.of(100, 125, 150, 200);
    static final List<Integer> TARGET_FRACTIONS = List.of(20, 40, 60, 80);
    static final List<Integer> BUDGET_SLACKS = List.of(0, 10, 25, 50);

    static final String REFERENCE_PROFILE = "P1";
    static final long REFERENCE_QUANTITY = 12;
    static final CostParams REFERENCE_COST_F = new CostParams("WORK", 90, 125);
    static final CostParams REFERENCE_COST_D = new CostParams("WORK", 90, 0);
    static final int REFERENCE_F = 60;
    static final int REFERENCE_BETA = 25;

    static final Design STARTER = new Design(1, 1, 0, 1, 0);

    // ---------------------------------------------------------------- catalogue and designs

    enum Offer {
        CUTTER(1, 4, CUT),
        ASSEMBLER(1, 6, ASSEMBLE),
        TWIN_ASSEMBLER(2, 3, ASSEMBLE),
        INSPECTOR(1, 4, INSPECT),
        FLEX_CELL(1, 3, ASSEMBLE, INSPECT);

        final int concurrency;
        final int limit;
        final Set<Integer> steps;

        Offer(int concurrency, int limit, int... steps) {
            this.concurrency = concurrency;
            this.limit = limit;
            Set<Integer> served = new TreeSet<>();
            for (int step : steps) {
                served.add(step);
            }
            this.steps = Collections.unmodifiableSet(served);
        }

        boolean servesAny(Set<Integer> stepSet) {
            for (int step : steps) {
                if (stepSet.contains(step)) {
                    return true;
                }
            }
            return false;
        }
    }

    enum Family {
        D,
        F;

        boolean admits(Design design) {
            return this == F || design.flex() == 0;
        }
    }

    record Profile(String name, long cut, long assemble, long inspect) {
        long duration(int step) {
            return switch (step) {
                case CUT -> cut;
                case ASSEMBLE -> assemble;
                default -> inspect;
            };
        }
    }

    record Provision(int cut, int asmOnly, int inspOnly, int flex) {
        boolean atMost(Provision other) {
            return cut <= other.cut && asmOnly <= other.asmOnly && inspOnly <= other.inspOnly && flex <= other.flex;
        }

        @Override
        public String toString() {
            return "(" + cut + "," + asmOnly + "," + inspOnly + "," + flex + ")";
        }
    }

    record Design(int cutters, int assemblers, int twins, int inspectors, int flex) {
        int count(Offer offer) {
            return switch (offer) {
                case CUTTER -> cutters;
                case ASSEMBLER -> assemblers;
                case TWIN_ASSEMBLER -> twins;
                case INSPECTOR -> inspectors;
                case FLEX_CELL -> flex;
            };
        }

        Design plus(Offer offer, int units) {
            return new Design(
                    cutters + (offer == Offer.CUTTER ? units : 0),
                    assemblers + (offer == Offer.ASSEMBLER ? units : 0),
                    twins + (offer == Offer.TWIN_ASSEMBLER ? units : 0),
                    inspectors + (offer == Offer.INSPECTOR ? units : 0),
                    flex + (offer == Offer.FLEX_CELL ? units : 0));
        }

        boolean projectable() {
            return cutters >= 1 && assemblers + twins + flex >= 1 && inspectors + flex >= 1;
        }

        boolean withinLimits() {
            for (Offer offer : Offer.values()) {
                if (count(offer) > offer.limit) {
                    return false;
                }
            }
            return true;
        }

        int slotsServing(Set<Integer> stepSet) {
            int slots = 0;
            for (Offer offer : Offer.values()) {
                if (offer.servesAny(stepSet)) {
                    slots += count(offer) * offer.concurrency;
                }
            }
            return slots;
        }

        Provision provision() {
            return new Provision(cutters, assemblers + 2 * twins, inspectors, flex);
        }

        String key() {
            return "C" + cutters + "A" + assemblers + "T" + twins + "I" + inspectors + "F" + flex;
        }
    }

    static Offer dedicatedFor(int step) {
        return switch (step) {
            case CUT -> Offer.CUTTER;
            case ASSEMBLE -> Offer.ASSEMBLER;
            default -> Offer.INSPECTOR;
        };
    }

    record CostParams(String rule, int deltaPct, int phiPct) {
        long base(Profile profile, int step) {
            return rule.equals("UNIFORM") ? 100 : 50 * profile.duration(step);
        }

        long price(Offer offer, Profile profile) {
            return switch (offer) {
                case CUTTER -> base(profile, CUT);
                case ASSEMBLER -> base(profile, ASSEMBLE);
                case INSPECTOR -> base(profile, INSPECT);
                case TWIN_ASSEMBLER -> roundedPercent(deltaPct, 2 * base(profile, ASSEMBLE));
                case FLEX_CELL -> roundedPercent(phiPct, Math.max(base(profile, ASSEMBLE), base(profile, INSPECT)));
            };
        }

        long cost(Design design, Profile profile) {
            long total = 0;
            for (Offer offer : Offer.values()) {
                total += design.count(offer) * price(offer, profile);
            }
            return total;
        }

        String label() {
            return rule + "|d" + deltaPct + "|p" + phiPct;
        }
    }

    static long roundedPercent(int percent, long value) {
        return (percent * value + 50) / 100;
    }

    static List<Design> enumerate() {
        List<Design> designs = new ArrayList<>();
        for (int c = 1; c <= Offer.CUTTER.limit; c++) {
            for (int a = 0; a <= Offer.ASSEMBLER.limit; a++) {
                for (int t = 0; t <= Offer.TWIN_ASSEMBLER.limit; t++) {
                    for (int i = 0; i <= Offer.INSPECTOR.limit; i++) {
                        for (int f = 0; f <= Offer.FLEX_CELL.limit; f++) {
                            Design design = new Design(c, a, t, i, f);
                            if (design.projectable()) {
                                designs.add(design);
                            }
                        }
                    }
                }
            }
        }
        return designs;
    }

    // ---------------------------------------------------------------- projection and execution

    /** Projects a design onto the current Factory model; offers in table order unless reversed. */
    static FactoryModel project(Design design, Profile profile, boolean reversedOrder) {
        List<Offer> order = new ArrayList<>(List.of(Offer.values()));
        if (reversedOrder) {
            Collections.reverse(order);
        }
        List<ConfiguredResource> resources = new ArrayList<>();
        List<Set<MachineId>> eligible = new ArrayList<>();
        for (int s = 0; s < STEP_NAMES.size(); s++) {
            eligible.add(new TreeSet<>());
        }
        long next = 1;
        for (Offer offer : order) {
            for (int i = 1; i <= design.count(offer); i++) {
                MachineId id = new MachineId(next++);
                resources.add(new ConfiguredResource(id, offer.name() + " " + i, offer.concurrency, null, 0));
                for (int step : offer.steps) {
                    eligible.get(step).add(id);
                }
            }
        }
        List<OperationStepDefinition> steps = new ArrayList<>();
        for (int s = 0; s < STEP_NAMES.size(); s++) {
            steps.add(new OperationStepDefinition(s + 1, STEP_NAMES.get(s), eligible.get(s), profile.duration(s)));
        }
        return new FactoryModel(
                resources,
                List.of(new OperationDefinition(OPERATION_ID, "Widget routing", steps)),
                List.of(new ProductDefinition(PRODUCT, "Widget", OPERATION_ID)));
    }

    static ExperimentEvidence execute(FactoryModel model, long quantity, long midRunTick) {
        List<ExperimentStep> script = new ArrayList<>();
        script.add(ExperimentStep.submit(PRODUCT, quantity, UNIT_PRICE));
        script.add(ExperimentStep.observe("after-submission"));
        if (midRunTick > 0) {
            script.add(ExperimentStep.advanceUntil(midRunTick));
            script.add(ExperimentStep.observe("mid-run"));
            script.add(ExperimentStep.captureEvents("through-mid-run"));
        }
        script.add(ExperimentStep.advanceToQuiescence(QUIESCENCE_DEADLINE));
        script.add(ExperimentStep.captureEvents("through-completion"));
        return ExperimentRunner.run(
                new ExperimentFixture("strategy-space", model, script, WindowIntent.COMPLETE_RUN, List.of()));
    }

    /** Completion from the closing observation, cross-checked against the single ORDER_COMPLETED event. */
    static long completionTick(ExperimentEvidence evidence) {
        if (!evidence.allCommandsAccepted()) {
            throw new IllegalStateException("workload not accepted: " + evidence.commands());
        }
        RuntimeObservation closing = evidence.observation(ExperimentEvidence.CLOSING_LABEL);
        if (closing.orders().size() != 1) {
            throw new IllegalStateException("expected one order, got " + closing.orders());
        }
        OrderObservation order = closing.orders().getFirst();
        if (!order.complete() || order.completedAt() == null) {
            throw new IllegalStateException("order did not complete: " + order);
        }
        List<RuntimeEventEnvelope> completions = evidence.retainedEvents().stream()
                .filter(event -> event.eventType() == RuntimeEventType.ORDER_COMPLETED)
                .toList();
        if (completions.size() != 1
                || completions.getFirst().simulationTime().value() != order.completedAt().value()) {
            throw new IllegalStateException("ORDER_COMPLETED disagrees with the closing observation");
        }
        return order.completedAt().value();
    }

    // ---------------------------------------------------------------- research-local derivation

    record Pool(Set<Integer> steps, Set<MachineId> resources, long occupiedJobTicks, long capacityJobTicks) {
        String label() {
            return steps.stream().map(STEP_NAMES::get).collect(Collectors.joining("+"));
        }

        int compareRatio(Pool other) {
            return Long.compare(
                    Math.multiplyExact(occupiedJobTicks, other.capacityJobTicks),
                    Math.multiplyExact(other.occupiedJobTicks, capacityJobTicks));
        }

        String ratio() {
            return occupiedJobTicks + "/" + capacityJobTicks;
        }
    }

    /**
     * @param sharedResourceSteps for every resource eligible for more than one step, the steps it was
     *     actually dispatched for
     */
    record PoolReport(List<Pool> pools, List<Pool> active, Map<MachineId, Set<Integer>> sharedResourceSteps) {
        Set<Integer> activeSteps() {
            Set<Integer> steps = new TreeSet<>();
            active.forEach(pool -> steps.addAll(pool.steps()));
            return steps;
        }

        String activeLabel() {
            return active.stream().map(Pool::label).collect(Collectors.joining("|"));
        }

        boolean sharedResourceDualUse() {
            return sharedResourceSteps.values().stream().anyMatch(steps -> steps.size() >= 2);
        }

        String occupancy() {
            return pools.stream().map(pool -> pool.label() + "=" + pool.ratio()).collect(Collectors.joining(";"));
        }
    }

    /**
     * Pool occupancy over the complete run: pools are connected components of the step-resource
     * eligibility graph; a pool's occupied job-ticks sum JOB_DISPATCHED-to-JOB_STEP_COMPLETED
     * intervals of its steps; its capacity is the closing time multiplied by its resources' summed
     * concurrency. The active constraint is the pool (or equal pools) with the maximum ratio.
     */
    static final class StepPoolOccupancyOracle implements Oracle<PoolReport> {

        static final ResearchDefinition DEFINITION = new ResearchDefinition(
                "step-pool-occupancy-from-supported-events",
                "Partition the published model's routing steps into pools, the connected components of the"
                        + " step-resource eligibility graph. For the closing observation's time T and supported events"
                        + " 1..latestEventSequence, a pool's occupiedJobTicks is the sum over dispatches of its steps of"
                        + " JOB_STEP_COMPLETED time minus JOB_DISPATCHED time; its capacityJobTicks is T times the summed"
                        + " concurrency of the pool's resources. The active constraint is every pool with the maximum"
                        + " occupied/capacity ratio, compared exactly. Also report, for each resource eligible for more"
                        + " than one step, the steps it was dispatched for. Refuse when any event was not retained, when"
                        + " no time has elapsed, or when a completion has no matching dispatch.",
                Set.of(EvidenceInput.PUBLISHED_MODEL, EvidenceInput.OBSERVATIONS, EvidenceInput.SUPPORTED_EVENTS));

        private record StepKey(JobId jobId, int stepIndex) {}

        @Override
        public ResearchDefinition definition() {
            return DEFINITION;
        }

        @Override
        public OracleOutcome<PoolReport> evaluate(DeclaredEvidence evidence) {
            FactoryModel model = evidence.publishedModel();
            RuntimeObservation closing = evidence.observation(ExperimentEvidence.CLOSING_LABEL);
            long cursor = closing.metadata().latestEventSequence();
            long horizon = closing.metadata().currentTime().value();
            Optional<List<RuntimeEventEnvelope>> events = evidence.completeEvents(0, cursor);
            if (events.isEmpty()) {
                return evidence.underdetermined("supported events 1.." + cursor + " are not all retained");
            }
            if (horizon == 0) {
                return evidence.underdetermined("no supported time has elapsed");
            }
            List<OperationStepDefinition> steps = model.operations().getFirst().steps();
            int stepCount = steps.size();
            int[] parent = new int[stepCount];
            for (int i = 0; i < stepCount; i++) {
                parent[i] = i;
            }
            for (int i = 0; i < stepCount; i++) {
                for (int j = i + 1; j < stepCount; j++) {
                    Set<MachineId> shared = new HashSet<>(steps.get(i).eligibleResources());
                    shared.retainAll(steps.get(j).eligibleResources());
                    if (!shared.isEmpty()) {
                        parent[root(parent, j)] = root(parent, i);
                    }
                }
            }
            Map<MachineId, Integer> concurrency = new HashMap<>();
            model.resources().forEach(resource -> concurrency.put(resource.id(), resource.concurrency()));
            Map<MachineId, Integer> eligibleStepCount = new HashMap<>();
            for (OperationStepDefinition step : steps) {
                step.eligibleResources().forEach(id -> eligibleStepCount.merge(id, 1, Integer::sum));
            }

            Map<StepKey, Long> dispatchTime = new LinkedHashMap<>();
            Map<StepKey, Long> completionTime = new LinkedHashMap<>();
            Map<MachineId, Set<Integer>> sharedSteps = new TreeMap<>();
            for (RuntimeEventEnvelope event : events.get()) {
                switch (event.payload()) {
                    case RuntimeEventPayload.JobDispatched dispatched -> {
                        dispatchTime.put(
                                new StepKey(dispatched.jobId(), dispatched.stepIndex()), event.simulationTime().value());
                        if (eligibleStepCount.getOrDefault(dispatched.machineId(), 0) > 1) {
                            sharedSteps.computeIfAbsent(dispatched.machineId(), id -> new TreeSet<>())
                                    .add(dispatched.stepIndex());
                        }
                    }
                    case RuntimeEventPayload.JobStepCompleted completed -> completionTime.put(
                            new StepKey(completed.jobId(), completed.stepIndex()), event.simulationTime().value());
                    default -> { }
                }
            }
            for (StepKey completed : completionTime.keySet()) {
                if (!dispatchTime.containsKey(completed)) {
                    return evidence.underdetermined("completion without dispatch: " + completed);
                }
            }
            eligibleStepCount.forEach((id, count) -> {
                if (count > 1) {
                    sharedSteps.computeIfAbsent(id, key -> new TreeSet<>());
                }
            });
            long[] occupiedByStep = new long[stepCount];
            dispatchTime.forEach((key, start) ->
                    occupiedByStep[key.stepIndex()] += completionTime.getOrDefault(key, horizon) - start);

            Map<Integer, Set<Integer>> components = new TreeMap<>();
            for (int i = 0; i < stepCount; i++) {
                components.computeIfAbsent(root(parent, i), key -> new TreeSet<>()).add(i);
            }
            List<Pool> pools = new ArrayList<>();
            for (Set<Integer> component : components.values()) {
                Set<MachineId> resources = new TreeSet<>();
                long occupied = 0;
                for (int step : component) {
                    resources.addAll(steps.get(step).eligibleResources());
                    occupied += occupiedByStep[step];
                }
                long slots = 0;
                for (MachineId id : resources) {
                    slots += concurrency.get(id);
                }
                pools.add(new Pool(
                        Collections.unmodifiableSet(component),
                        Collections.unmodifiableSet(resources),
                        occupied,
                        Math.multiplyExact(horizon, slots)));
            }
            Pool best = pools.getFirst();
            for (Pool pool : pools) {
                if (pool.compareRatio(best) > 0) {
                    best = pool;
                }
            }
            Pool max = best;
            List<Pool> active = pools.stream().filter(pool -> pool.compareRatio(max) == 0).toList();
            Map<MachineId, Set<Integer>> shared = new TreeMap<>();
            sharedSteps.forEach((id, served) -> shared.put(id, Collections.unmodifiableSet(served)));
            return evidence.derived(new PoolReport(List.copyOf(pools), active, Collections.unmodifiableMap(shared)));
        }

        private static int root(int[] parent, int node) {
            int current = node;
            while (parent[current] != current) {
                current = parent[current];
            }
            return current;
        }
    }

    static <T> T derived(OracleOutcome<T> outcome, String context) {
        return switch (outcome) {
            case OracleOutcome.Derived<T> value -> value.value();
            case OracleOutcome.Underdetermined<T> refused ->
                throw new IllegalStateException(context + ": derivation refused: " + refused.reasons());
        };
    }

    // ---------------------------------------------------------------- simulation cache

    record DesignResult(Design design, long completion, PoolReport pools) {}

    private final Map<String, DesignResult> cache = new HashMap<>();
    private final Set<String> replayed = new HashSet<>();
    private long runs;

    DesignResult simulate(Profile profile, long quantity, Design design, boolean reversed, boolean replay) {
        String key = profile.name() + "/N" + quantity + "/" + design.key() + (reversed ? "/reversed" : "");
        DesignResult cached = cache.get(key);
        if (cached != null && (!replay || replayed.contains(key))) {
            return cached;
        }
        FactoryModel model = project(design, profile, reversed);
        ExperimentEvidence evidence = execute(model, quantity, 0);
        runs++;
        if (replay) {
            ExperimentEvidence second = execute(model, quantity, 0);
            runs++;
            assertEquals(evidence.withNormalizedRunIdentity(), second.withNormalizedRunIdentity(), key + " replay");
            replayed.add(key);
        }
        long completion = completionTick(evidence);
        PoolReport report = derived(new StepPoolOccupancyOracle().evaluateOn(evidence), key);
        DesignResult result = new DesignResult(design, completion, report);
        if (cached != null) {
            assertEquals(cached, result, key + " cached result");
        }
        cache.put(key, result);
        return result;
    }

    // ---------------------------------------------------------------- frontier and cells

    record Entry(DesignResult result, long cost) {
        Design design() {
            return result.design();
        }
    }

    record TieClass(long cost, long completion, List<Entry> members) {}

    record Context(
            Profile profile,
            long quantity,
            Family family,
            CostParams cost,
            List<Entry> entries,
            List<TieClass> frontier,
            long starterCompletion,
            long floorCompletion,
            long floorCost) {

        String id() {
            return profile.name() + "|N" + quantity + "|" + family + "|" + cost.label();
        }

        long cheapestMeeting(long target) {
            for (TieClass tieClass : frontier) {
                if (tieClass.completion() <= target) {
                    return tieClass.cost();
                }
            }
            throw new IllegalStateException("no design meets " + target);
        }
    }

    static List<TieClass> frontier(List<Entry> entries) {
        List<Entry> sorted = new ArrayList<>(entries);
        sorted.sort(Comparator.comparingLong(Entry::cost)
                .thenComparingLong(entry -> entry.result().completion())
                .thenComparing(entry -> entry.design().key()));
        List<TieClass> out = new ArrayList<>();
        long best = Long.MAX_VALUE;
        int i = 0;
        while (i < sorted.size()) {
            long cost = sorted.get(i).cost();
            int j = i;
            while (j < sorted.size() && sorted.get(j).cost() == cost) {
                j++;
            }
            long min = sorted.get(i).result().completion();
            if (min < best) {
                List<Entry> members = sorted.subList(i, j).stream()
                        .filter(entry -> entry.result().completion() == min)
                        .toList();
                out.add(new TieClass(cost, min, members));
                best = min;
            }
            i = j;
        }
        return out;
    }

    record AwayProbe(int step, Offer offer, int units, long delta, boolean outsideLimit) {}

    record AtProbe(Offer offer, long delta, boolean outsideLimit) {}

    record Intervention(
            Design base,
            long baseCompletion,
            String activeLabel,
            List<AtProbe> atProbes,
            Offer bestAt,
            long bestDelta,
            boolean material,
            List<AwayProbe> away,
            boolean irrelevant,
            int migrationUnits,
            String migratedTo,
            String migrationTrace) {

        boolean migration() {
            return migrationUnits > 0;
        }
    }

    Intervention intervene(Context context, Design base, boolean replay) {
        Profile profile = context.profile();
        long quantity = context.quantity();
        DesignResult result = simulate(profile, quantity, base, false, replay);
        long baseCompletion = result.completion();
        Set<Integer> activeSteps = result.pools().activeSteps();
        Set<Offer> atOffers = new LinkedHashSet<>();
        for (Pool pool : result.pools().active()) {
            for (int step : pool.steps()) {
                atOffers.add(dedicatedFor(step));
            }
            if (pool.steps().size() > 1 && context.family() == Family.F) {
                atOffers.add(Offer.FLEX_CELL);
            }
        }
        List<Offer> orderedAt = new ArrayList<>(atOffers);
        orderedAt.sort(Comparator.naturalOrder());
        List<AtProbe> atProbes = new ArrayList<>();
        Offer bestAt = null;
        long bestDelta = Long.MIN_VALUE;
        for (Offer offer : orderedAt) {
            Design probe = base.plus(offer, 1);
            long delta = baseCompletion - simulate(profile, quantity, probe, false, replay).completion();
            atProbes.add(new AtProbe(offer, delta, !probe.withinLimits()));
            if (delta > bestDelta) {
                bestDelta = delta;
                bestAt = offer;
            }
        }
        long materialThreshold = Math.max(1, (5 * baseCompletion + 99) / 100);
        boolean material = bestDelta >= materialThreshold;

        List<AwayProbe> away = new ArrayList<>();
        long bestPrice = context.cost().price(bestAt, profile);
        for (int step = 0; step < STEP_NAMES.size(); step++) {
            if (activeSteps.contains(step)) {
                continue;
            }
            Offer offer = dedicatedFor(step);
            int matched = (int) Math.max(1, Math.round((double) bestPrice / context.cost().price(offer, profile)));
            Set<Integer> unitsSet = new TreeSet<>(List.of(1, matched));
            for (int units : unitsSet) {
                Design probe = base.plus(offer, units);
                long delta = baseCompletion - simulate(profile, quantity, probe, false, replay).completion();
                away.add(new AwayProbe(step, offer, units, delta, !probe.withinLimits()));
            }
        }
        long awayBestDelta = bestDelta;
        boolean irrelevant = material && away.stream().allMatch(probe -> 4 * probe.delta() <= awayBestDelta);

        int migrationUnits = 0;
        String migratedTo = "";
        StringBuilder trace = new StringBuilder(result.pools().activeLabel() + "@" + baseCompletion);
        for (int units = 1; units <= 4; units++) {
            DesignResult relieved = simulate(profile, quantity, base.plus(bestAt, units), false, replay);
            trace.append(" -> +").append(units).append(bestAt).append(":")
                    .append(relieved.pools().activeLabel()).append("@").append(relieved.completion());
            Set<Integer> newlyActive = new TreeSet<>(relieved.pools().activeSteps());
            newlyActive.removeAll(activeSteps);
            if (!newlyActive.isEmpty() && relieved.completion() < baseCompletion) {
                migrationUnits = units;
                migratedTo = relieved.pools().activeLabel();
                break;
            }
        }
        return new Intervention(
                base,
                baseCompletion,
                result.pools().activeLabel(),
                atProbes,
                bestAt,
                bestDelta,
                material,
                away,
                irrelevant,
                migrationUnits,
                migratedTo,
                trace.toString());
    }

    record Cell(
            Context context,
            int fraction,
            int slack,
            long target,
            long budget,
            List<TieClass> feasible,
            boolean weak,
            Optional<String> strongPair,
            int incomparableUnexplained,
            boolean monotone,
            int tieClasses,
            int provisionDistinctTies,
            boolean capitalBinding,
            Intervention intervention) {

        String id() {
            return context.id() + "|f" + fraction + "|b" + slack;
        }

        boolean strong() {
            return strongPair.isPresent();
        }

        boolean positive() {
            return intervention.material() && intervention.irrelevant() && intervention.migration() && weak && strong();
        }
    }

    Cell evaluate(Context context, int fraction, int slack, boolean replay) {
        long target = context.floorCompletion()
                + (fraction * (context.starterCompletion() - context.floorCompletion())) / 100;
        long cheapest = context.cheapestMeeting(target);
        long budget = cheapest * (100 + slack) / 100;
        List<TieClass> feasible = context.frontier().stream()
                .filter(tieClass -> tieClass.cost() <= budget && tieClass.completion() <= target)
                .toList();
        boolean weak = feasible.size() >= 2;

        Optional<String> strong = Optional.empty();
        int unexplained = 0;
        for (int i = 0; i < feasible.size(); i++) {
            for (int j = i + 1; j < feasible.size(); j++) {
                for (Entry cheaper : feasible.get(i).members()) {
                    for (Entry faster : feasible.get(j).members()) {
                        Provision a = cheaper.design().provision();
                        Provision b = faster.design().provision();
                        if (a.atMost(b) || b.atMost(a)) {
                            continue;
                        }
                        Set<Integer> constraint = cheaper.result().pools().activeSteps();
                        boolean relieves = faster.design().slotsServing(constraint)
                                > cheaper.design().slotsServing(constraint);
                        Entry moreFlex = cheaper.design().flex() >= faster.design().flex() ? cheaper : faster;
                        boolean flexExplained = cheaper.design().flex() == faster.design().flex()
                                || moreFlex.result().pools().sharedResourceDualUse();
                        if (relieves && flexExplained) {
                            if (strong.isEmpty()) {
                                strong = Optional.of(cheaper.design().key() + "[" + cheaper.cost() + ","
                                        + cheaper.result().completion() + "," + cheaper.result().pools().activeLabel()
                                        + "] vs " + faster.design().key() + "[" + faster.cost() + ","
                                        + faster.result().completion() + "," + faster.result().pools().activeLabel()
                                        + "]");
                            }
                        } else {
                            unexplained++;
                        }
                    }
                }
            }
        }

        boolean monotone = true;
        for (int i = 0; i + 1 < feasible.size(); i++) {
            for (Entry cheaper : feasible.get(i).members()) {
                for (Entry faster : feasible.get(i + 1).members()) {
                    Set<Integer> constraint = cheaper.result().pools().activeSteps();
                    boolean nested = cheaper.design().provision().atMost(faster.design().provision());
                    boolean relieves = faster.design().slotsServing(constraint)
                            > cheaper.design().slotsServing(constraint);
                    if (!nested || !relieves) {
                        monotone = false;
                    }
                }
            }
        }

        int ties = 0;
        int provisionTies = 0;
        for (TieClass tieClass : feasible) {
            if (tieClass.members().size() > 1) {
                ties++;
                long provisions = tieClass.members().stream().map(entry -> entry.design().provision()).distinct().count();
                if (provisions > 1) {
                    provisionTies++;
                }
            }
        }
        boolean binding = context.floorCost() > budget;
        Design base = feasible.getFirst().members().getFirst().design();
        Intervention intervention = intervene(context, base, replay);
        return new Cell(context, fraction, slack, target, budget, feasible, weak, strong, unexplained, monotone, ties,
                provisionTies, binding, intervention);
    }

    // ---------------------------------------------------------------- the experiment

    @Test
    void runPreRegisteredProtocol() throws IOException {
        Files.createDirectories(OUT);
        List<Design> designs = enumerate();
        StringBuilder summary = new StringBuilder("# Strategy-space experiment output (pass 1)\n\n");
        summary.append("Projectable designs per (profile, N): ").append(designs.size()).append("\n\n");

        Map<String, List<DesignResult>> results = new LinkedHashMap<>();
        for (Profile profile : PROFILES) {
            for (long quantity : QUANTITIES) {
                boolean replay = profile.name().equals(REFERENCE_PROFILE) && quantity == REFERENCE_QUANTITY;
                List<DesignResult> list = new ArrayList<>();
                StringBuilder csv = new StringBuilder(
                        "design,cutters,assemblers,twins,inspectors,flex,provision,completion,active,occupancy,sharedDualUse\n");
                for (Design design : designs) {
                    DesignResult result = simulate(profile, quantity, design, false, replay);
                    list.add(result);
                    csv.append(design.key()).append(',').append(design.cutters()).append(',')
                            .append(design.assemblers()).append(',').append(design.twins()).append(',')
                            .append(design.inspectors()).append(',').append(design.flex()).append(",\"")
                            .append(design.provision()).append("\",").append(result.completion()).append(',')
                            .append(result.pools().activeLabel()).append(',').append(result.pools().occupancy())
                            .append(',').append(result.pools().sharedResourceDualUse()).append('\n');
                }
                results.put(profile.name() + "/N" + quantity, list);
                Files.writeString(OUT.resolve("designs-" + profile.name() + "-N" + quantity + ".csv"), csv.toString());
            }
        }

        // Granularity control: one twin versus two single assemblers, all else equal.
        summary.append("## Granularity control (1 TWIN_ASSEMBLER vs 2 ASSEMBLER)\n\n");
        for (Map.Entry<String, List<DesignResult>> entry : results.entrySet()) {
            Map<String, DesignResult> byKey = new HashMap<>();
            entry.getValue().forEach(result -> byKey.put(result.design().key(), result));
            int pairs = 0;
            List<String> mismatches = new ArrayList<>();
            for (DesignResult result : entry.getValue()) {
                Design design = result.design();
                if (design.twins() >= 1 && design.assemblers() + 2 <= Offer.ASSEMBLER.limit) {
                    Design twin = design.plus(Offer.TWIN_ASSEMBLER, -1).plus(Offer.ASSEMBLER, 2);
                    DesignResult other = byKey.get(twin.key());
                    pairs++;
                    if (other.completion() != result.completion()) {
                        mismatches.add(design.key() + "=" + result.completion() + " vs " + twin.key() + "="
                                + other.completion());
                    }
                }
            }
            summary.append("- ").append(entry.getKey()).append(": ").append(pairs).append(" pairs, ")
                    .append(mismatches.size()).append(" completion mismatches")
                    .append(mismatches.isEmpty() ? "" : " e.g. " + mismatches.subList(0, Math.min(5, mismatches.size())))
                    .append('\n');
        }

        // Contexts and cells.
        List<Context> contexts = new ArrayList<>();
        for (Profile profile : PROFILES) {
            for (long quantity : QUANTITIES) {
                List<DesignResult> list = results.get(profile.name() + "/N" + quantity);
                long starter = list.stream().filter(r -> r.design().equals(STARTER)).findFirst().orElseThrow().completion();
                for (Family family : Family.values()) {
                    for (String rule : RULES) {
                        for (int delta : DELTAS) {
                            List<Integer> phis = family == Family.F ? PHIS : List.of(0);
                            for (int phi : phis) {
                                CostParams cost = new CostParams(rule, delta, phi);
                                List<Entry> entries = list.stream()
                                        .filter(r -> family.admits(r.design()))
                                        .map(r -> new Entry(r, cost.cost(r.design(), profile)))
                                        .toList();
                                List<TieClass> frontier = frontier(entries);
                                TieClass fastest = frontier.getLast();
                                contexts.add(new Context(profile, quantity, family, cost, entries, frontier, starter,
                                        fastest.completion(), fastest.cost()));
                            }
                        }
                    }
                }
            }
        }

        Map<String, Cell> cells = new LinkedHashMap<>();
        StringBuilder cellCsv = new StringBuilder("cell,profile,N,family,rule,delta,phi,f,beta,target,budget,starterT,floorT,"
                + "frontierClasses,feasibleClasses,weak,strong,strongPair,incomparableUnexplained,monotone,tieClasses,"
                + "provisionDistinctTies,capitalBinding,base,baseT,active,bestAt,bestDelta,material,irrelevant,"
                + "migrationUnits,migratedTo,positive\n");
        StringBuilder frontierCsv = new StringBuilder("context,class,cost,completion,members,provisions,active,occupancy\n");
        for (Context context : contexts) {
            for (int c = 0; c < context.frontier().size(); c++) {
                TieClass tieClass = context.frontier().get(c);
                frontierCsv.append(context.id()).append(',').append(c).append(',').append(tieClass.cost()).append(',')
                        .append(tieClass.completion()).append(",\"")
                        .append(tieClass.members().stream().map(e -> e.design().key()).collect(Collectors.joining(" ")))
                        .append("\",\"")
                        .append(tieClass.members().stream().map(e -> e.design().provision().toString())
                                .collect(Collectors.joining(" ")))
                        .append("\",\"")
                        .append(tieClass.members().stream().map(e -> e.result().pools().activeLabel())
                                .collect(Collectors.joining(" ")))
                        .append("\",\"")
                        .append(tieClass.members().stream().map(e -> e.result().pools().occupancy())
                                .collect(Collectors.joining(" ")))
                        .append("\"\n");
            }
            boolean replay = context.profile().name().equals(REFERENCE_PROFILE)
                    && context.quantity() == REFERENCE_QUANTITY;
            for (int fraction : TARGET_FRACTIONS) {
                for (int slack : BUDGET_SLACKS) {
                    Cell cell = evaluate(context, fraction, slack, replay);
                    cells.put(cell.id(), cell);
                    Intervention in = cell.intervention();
                    cellCsv.append(cell.id()).append(',').append(context.profile().name()).append(',')
                            .append(context.quantity()).append(',').append(context.family()).append(',')
                            .append(context.cost().rule()).append(',').append(context.cost().deltaPct()).append(',')
                            .append(context.cost().phiPct()).append(',').append(fraction).append(',').append(slack)
                            .append(',').append(cell.target()).append(',').append(cell.budget()).append(',')
                            .append(context.starterCompletion()).append(',').append(context.floorCompletion())
                            .append(',').append(context.frontier().size()).append(',').append(cell.feasible().size())
                            .append(',').append(cell.weak()).append(',').append(cell.strong()).append(",\"")
                            .append(cell.strongPair().orElse("")).append("\",").append(cell.incomparableUnexplained())
                            .append(',').append(cell.monotone()).append(',').append(cell.tieClasses()).append(',')
                            .append(cell.provisionDistinctTies()).append(',').append(cell.capitalBinding()).append(',')
                            .append(in.base().key()).append(',').append(in.baseCompletion()).append(',')
                            .append(in.activeLabel()).append(',').append(in.bestAt()).append(',')
                            .append(in.bestDelta()).append(',').append(in.material()).append(',')
                            .append(in.irrelevant()).append(',').append(in.migrationUnits()).append(',')
                            .append(in.migratedTo()).append(',').append(cell.positive()).append('\n');
                }
            }
        }
        Files.writeString(OUT.resolve("cells.csv"), cellCsv.toString());
        Files.writeString(OUT.resolve("frontiers.csv"), frontierCsv.toString());

        // Aggregate map by family.
        summary.append("\n## Cell map by family\n\n");
        summary.append("| Family | cells | CC material | IC holds | migration | MVS-W (CP) | CP binding | MVS-S | monotone |"
                + " incomparable-unexplained>0 | provision-distinct ties>0 | positive |\n");
        summary.append("|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|\n");
        for (Family family : Family.values()) {
            List<Cell> list = cells.values().stream().filter(c -> c.context().family() == family).toList();
            summary.append("| ").append(family).append(" | ").append(list.size())
                    .append(" | ").append(list.stream().filter(c -> c.intervention().material()).count())
                    .append(" | ").append(list.stream().filter(c -> c.intervention().irrelevant()).count())
                    .append(" | ").append(list.stream().filter(c -> c.intervention().migration()).count())
                    .append(" | ").append(list.stream().filter(Cell::weak).count())
                    .append(" | ").append(list.stream().filter(Cell::capitalBinding).count())
                    .append(" | ").append(list.stream().filter(Cell::strong).count())
                    .append(" | ").append(list.stream().filter(Cell::monotone).count())
                    .append(" | ").append(list.stream().filter(c -> c.incomparableUnexplained() > 0).count())
                    .append(" | ").append(list.stream().filter(c -> c.provisionDistinctTies() > 0).count())
                    .append(" | ").append(list.stream().filter(Cell::positive).count()).append(" |\n");
        }

        summary.append("\n## MVS-S and positive cells by (profile, N, family, rule)\n\n");
        summary.append("| profile | N | family | rule | cells | MVS-W | MVS-S | positive |\n|---|---:|---|---|---:|---:|---:|---:|\n");
        Map<String, List<Cell>> grouped = new TreeMap<>();
        for (Cell cell : cells.values()) {
            Context context = cell.context();
            grouped.computeIfAbsent(context.profile().name() + " | " + context.quantity() + " | " + context.family()
                    + " | " + context.cost().rule(), key -> new ArrayList<>()).add(cell);
        }
        grouped.forEach((key, list) -> summary.append("| ").append(key).append(" | ").append(list.size())
                .append(" | ").append(list.stream().filter(Cell::weak).count())
                .append(" | ").append(list.stream().filter(Cell::strong).count())
                .append(" | ").append(list.stream().filter(Cell::positive).count()).append(" |\n"));

        summary.append("\n## MVS-S cells by flex price (family F)\n\n| rule | phi | cells | MVS-S | positive |\n|---|---:|---:|---:|---:|\n");
        for (String rule : RULES) {
            for (int phi : PHIS) {
                List<Cell> list = cells.values().stream()
                        .filter(c -> c.context().family() == Family.F && c.context().cost().rule().equals(rule)
                                && c.context().cost().phiPct() == phi)
                        .toList();
                summary.append("| ").append(rule).append(" | ").append(phi).append(" | ").append(list.size())
                        .append(" | ").append(list.stream().filter(Cell::strong).count()).append(" | ")
                        .append(list.stream().filter(Cell::positive).count()).append(" |\n");
            }
        }

        // Reference case in full.
        for (Family family : Family.values()) {
            CostParams cost = family == Family.F ? REFERENCE_COST_F : REFERENCE_COST_D;
            String id = REFERENCE_PROFILE + "|N" + REFERENCE_QUANTITY + "|" + family + "|" + cost.label() + "|f"
                    + REFERENCE_F + "|b" + REFERENCE_BETA;
            Cell cell = cells.get(id);
            Context context = cell.context();
            summary.append("\n## Reference cell ").append(id).append("\n\n");
            summary.append("Prices: ");
            for (Offer offer : Offer.values()) {
                if (family.admits(new Design(1, 1, 0, 1, offer == Offer.FLEX_CELL ? 1 : 0))) {
                    summary.append(offer).append('=').append(cost.price(offer, context.profile())).append(' ');
                }
            }
            summary.append("\n\nstarter T=").append(context.starterCompletion()).append(", floor T=")
                    .append(context.floorCompletion()).append(" at cost ").append(context.floorCost())
                    .append(", target=").append(cell.target()).append(", budget=").append(cell.budget())
                    .append(", designs=").append(context.entries().size()).append("\n\n");
            summary.append("Complete global frontier (feasible rows marked):\n\n| # | cost | T | feasible | members |"
                    + " provision (cut,asm,insp,flex) | active | pool occupancy | shared dual use |\n"
                    + "|---:|---:|---:|---|---|---|---|---|---|\n");
            for (int c = 0; c < context.frontier().size(); c++) {
                TieClass tieClass = context.frontier().get(c);
                boolean feasible = cell.feasible().contains(tieClass);
                for (Entry member : tieClass.members()) {
                    summary.append("| ").append(c).append(" | ").append(tieClass.cost()).append(" | ")
                            .append(tieClass.completion()).append(" | ").append(feasible ? "yes" : "")
                            .append(" | ").append(member.design().key()).append(" | ")
                            .append(member.design().provision()).append(" | ")
                            .append(member.result().pools().activeLabel()).append(" | ")
                            .append(member.result().pools().occupancy()).append(" | ")
                            .append(member.result().pools().sharedResourceDualUse()).append(" |\n");
                }
            }
            summary.append("\nMVS-W=").append(cell.weak()).append(", MVS-S=").append(cell.strong())
                    .append(cell.strongPair().map(pair -> " (" + pair + ")").orElse(""))
                    .append(", incomparable-unexplained pairs=").append(cell.incomparableUnexplained())
                    .append(", monotone=").append(cell.monotone()).append(", tie classes=").append(cell.tieClasses())
                    .append(", capital binding=").append(cell.capitalBinding()).append(", positive=")
                    .append(cell.positive()).append("\n\n");

            summary.append("Interventions (starter design and every feasible frontier design):\n\n");
            List<Design> bases = new ArrayList<>();
            bases.add(STARTER);
            cell.feasible().forEach(tieClass -> tieClass.members().forEach(entry -> bases.add(entry.design())));
            for (Design base : bases) {
                Intervention in = intervene(context, base, true);
                summary.append("- base ").append(base.key()).append(" T=").append(in.baseCompletion())
                        .append(" active=").append(in.activeLabel()).append("; at-constraint ")
                        .append(in.atProbes()).append(" best=").append(in.bestAt()).append(" dT=")
                        .append(in.bestDelta()).append(" material=").append(in.material()).append("; away ")
                        .append(in.away()).append(" irrelevant=").append(in.irrelevant()).append("; migration ")
                        .append(in.migrationTrace()).append(" => units=").append(in.migrationUnits()).append(" to ")
                        .append(in.migratedTo()).append('\n');
            }

            // Projection-order control and waiting corroboration for load-bearing designs.
            summary.append("\nProjection-order control (reversed offer order):\n\n");
            for (TieClass tieClass : context.frontier()) {
                for (Entry entry : tieClass.members()) {
                    DesignResult reversed = simulate(context.profile(), context.quantity(), entry.design(), true, true);
                    if (reversed.completion() != entry.result().completion()) {
                        summary.append("- DIFFERS ").append(entry.design().key()).append(": ")
                                .append(entry.result().completion()).append(" vs reversed ")
                                .append(reversed.completion()).append('\n');
                    }
                }
            }
            summary.append("- checked all ").append(context.frontier().stream().mapToInt(t -> t.members().size()).sum())
                    .append(" frontier designs\n");

            summary.append("\nMid-run waiting corroboration (landed WaitingWorkByStepOracle at floor(T/2)):\n\n");
            Set<Design> loadBearing = new LinkedHashSet<>(bases);
            Intervention first = intervene(context, cell.intervention().base(), true);
            loadBearing.add(first.base().plus(first.bestAt(), 1));
            if (first.migration()) {
                loadBearing.add(first.base().plus(first.bestAt(), first.migrationUnits()));
            }
            for (Design design : loadBearing) {
                DesignResult result = simulate(context.profile(), context.quantity(), design, false, true);
                long mid = result.completion() / 2;
                ExperimentEvidence evidence = execute(project(design, context.profile(), false), context.quantity(), mid);
                runs++;
                assertEquals(result.completion(), completionTick(evidence));
                summary.append("- ").append(design.key()).append(" T=").append(result.completion()).append(" at ")
                        .append(mid).append(": ")
                        .append(describe(new WaitingWorkByStepOracle("mid-run").evaluateOn(evidence))).append('\n');
            }
        }

        // Candidate selection and robustness.
        summary.append("\n## Candidate selection and robustness\n\n");
        Cell reference = cells.get(REFERENCE_PROFILE + "|N" + REFERENCE_QUANTITY + "|F|" + REFERENCE_COST_F.label()
                + "|f" + REFERENCE_F + "|b" + REFERENCE_BETA);
        List<Cell> positives = cells.values().stream().filter(Cell::positive).toList();
        summary.append("Reference cell (family F) positive: ").append(reference.positive()).append("; positive cells in window: ")
                .append(positives.size()).append(" of ").append(cells.size()).append("\n\n");
        Cell candidate = reference.positive() ? reference : null;
        if (candidate == null) {
            int bestSupport = -1;
            for (Cell cell : positives) {
                int support = (int) neighbours(cell, cells).values().stream().flatMap(List::stream)
                        .filter(Cell::positive).count();
                if (support > bestSupport) {
                    bestSupport = support;
                    candidate = cell;
                }
            }
        }
        if (candidate != null) {
            summary.append("Candidate: ").append(candidate.id()).append(" pair ").append(candidate.strongPair().orElse(""))
                    .append("\n\n| axis | neighbours | MVS-S | positive | collapsed to one design |\n|---|---:|---:|---:|---:|\n");
            boolean robust = true;
            for (Map.Entry<String, List<Cell>> axis : neighbours(candidate, cells).entrySet()) {
                List<Cell> list = axis.getValue();
                long strong = list.stream().filter(Cell::strong).count();
                long collapsed = list.stream().filter(c -> c.feasible().size() <= 1).count();
                summary.append("| ").append(axis.getKey()).append(" | ").append(list.size()).append(" | ").append(strong)
                        .append(" | ").append(list.stream().filter(Cell::positive).count()).append(" | ")
                        .append(collapsed).append(" |\n");
                if (!list.isEmpty() && (2 * strong < list.size() || collapsed > 0)) {
                    robust = false;
                }
            }
            summary.append("\nRobust under the pre-registered rule: ").append(robust).append('\n');
        } else {
            summary.append("No positive cell exists in the declared window.\n");
        }

        summary.append("\n## Execution\n\nRuntime executions: ").append(runs).append("; replay-checked design keys: ")
                .append(replayed.size()).append('\n');
        Files.writeString(OUT.resolve("summary.md"), summary.toString());
        assertTrue(runs > 0);
    }

    static Map<String, List<Cell>> neighbours(Cell cell, Map<String, Cell> cells) {
        Context context = cell.context();
        Map<String, List<Cell>> axes = new LinkedHashMap<>();
        axes.put("target f", adjacent(TARGET_FRACTIONS, cell.fraction()).stream()
                .map(f -> cells.get(context.id() + "|f" + f + "|b" + cell.slack())).toList());
        axes.put("budget beta", adjacent(BUDGET_SLACKS, cell.slack()).stream()
                .map(b -> cells.get(context.id() + "|f" + cell.fraction() + "|b" + b)).toList());
        axes.put("quantity N", adjacent(QUANTITIES, context.quantity()).stream()
                .map(n -> cells.get(context.profile().name() + "|N" + n + "|" + context.family() + "|"
                        + context.cost().label() + "|f" + cell.fraction() + "|b" + cell.slack()))
                .toList());
        axes.put("twin delta", adjacent(DELTAS, context.cost().deltaPct()).stream()
                .map(d -> cells.get(withCost(cell, new CostParams(context.cost().rule(), d, context.cost().phiPct()))))
                .toList());
        if (context.family() == Family.F) {
            axes.put("flex phi", adjacent(PHIS, context.cost().phiPct()).stream()
                    .map(p -> cells.get(withCost(cell, new CostParams(context.cost().rule(), context.cost().deltaPct(), p))))
                    .toList());
        }
        String otherRule = context.cost().rule().equals("UNIFORM") ? "WORK" : "UNIFORM";
        axes.put("cost rule", List.of(cells.get(withCost(
                cell, new CostParams(otherRule, context.cost().deltaPct(), context.cost().phiPct())))));
        return axes;
    }

    static String withCost(Cell cell, CostParams cost) {
        Context context = cell.context();
        return context.profile().name() + "|N" + context.quantity() + "|" + context.family() + "|" + cost.label() + "|f"
                + cell.fraction() + "|b" + cell.slack();
    }

    static <T> List<T> adjacent(List<T> values, T value) {
        int index = values.indexOf(value);
        List<T> out = new ArrayList<>();
        if (index > 0) {
            out.add(values.get(index - 1));
        }
        if (index + 1 < values.size()) {
            out.add(values.get(index + 1));
        }
        return out;
    }

    static String describe(OracleOutcome<?> outcome) {
        return switch (outcome) {
            case OracleOutcome.Derived<?> value -> String.valueOf(value.value());
            case OracleOutcome.Underdetermined<?> refused -> "REFUSED " + refused.reasons();
        };
    }
}
