package com.arcogine.factory.research;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.factory.process.RuntimeEventType;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

/** Independent research experiment. Uses only publication and supported runtime evidence. */
class StrategySpaceAdversarialTest {
    static final String TYPES = "CATIF";
    static final int[] LIMITS = {4, 6, 3, 4, 3};
    static final int[] PRICES = {150, 200, 360, 250, 313};
    static final long[] P2 = {3, 4, 5};
    static final ProductId PRODUCT = new ProductId(1);
    static final Path OUT = Path.of("build", "strategy-space-review");

    static FactoryModel model(String order, long[] durations, boolean reverseList) {
        List<ConfiguredResource> resources = new ArrayList<>();
        List<Set<MachineId>> eligible = List.of(new TreeSet<>(), new TreeSet<>(), new TreeSet<>());
        for (int i = 0; i < order.length(); i++) {
            char type = order.charAt(i);
            MachineId id = new MachineId(i + 1L);
            resources.add(new ConfiguredResource(id, type + "-" + i, type == 'T' ? 2 : 1, null, 0));
            if (type == 'C') {
                eligible.get(0).add(id);
            } else {
                if (type == 'A' || type == 'T' || type == 'F') {
                    eligible.get(1).add(id);
                }
                if (type == 'I' || type == 'F') {
                    eligible.get(2).add(id);
                }
            }
        }
        if (reverseList) {
            Collections.reverse(resources);
        }
        List<OperationStepDefinition> steps = new ArrayList<>();
        for (int s = 0; s < 3; s++) {
            steps.add(new OperationStepDefinition(s + 1, "step-" + s, eligible.get(s), durations[s]));
        }
        return new FactoryModel(resources, List.of(new OperationDefinition(1, "serial", steps)),
                List.of(new ProductDefinition(PRODUCT, "item", 1)));
    }

    static ExperimentEvidence execute(FactoryModel model, long quantity) {
        ExperimentEvidence evidence = ExperimentRunner.run(new ExperimentFixture("independent-order-review", model,
                List.of(ExperimentStep.submit(PRODUCT, quantity, 10.0), ExperimentStep.observe("submitted"),
                        ExperimentStep.advanceToQuiescence(10000), ExperimentStep.captureEvents("complete")),
                ExperimentFixture.WindowIntent.COMPLETE_RUN, List.of()));
        assertTrue(evidence.allCommandsAccepted());
        assertTrue(evidence.window().isComplete());
        assertEquals(1, evidence.commands().size());
        assertEquals(0, evidence.retainedEvents().stream()
                .filter(e -> e.eventType() == RuntimeEventType.MACHINE_AVAILABILITY_CHANGED).count());
        return evidence;
    }

    static long completion(ExperimentEvidence evidence) {
        var completions = evidence.retainedEvents().stream()
                .filter(e -> e.eventType() == RuntimeEventType.ORDER_COMPLETED).toList();
        assertEquals(1, completions.size());
        long tick = completions.getFirst().simulationTime().value();
        assertEquals(tick, evidence.observation(ExperimentEvidence.CLOSING_LABEL)
                .orders().getFirst().completedAt().value());
        return tick;
    }

    static String key(int[] counts) {
        StringBuilder key = new StringBuilder();
        for (int k = 0; k < 5; k++) {
            key.append(TYPES.charAt(k)).append(counts[k]);
        }
        return key.toString();
    }

    static void orders(int[] remaining, String prefix, Consumer<String> action) {
        boolean done = true;
        for (int t = 0; t < 5; t++) {
            if (remaining[t] > 0) {
                done = false;
                remaining[t]--;
                orders(remaining, prefix + TYPES.charAt(t), action);
                remaining[t]++;
            }
        }
        if (done) {
            action.accept(prefix);
        }
    }

    @Test
    void allAffordableIdentityOrdersAndControlledInterventions() throws Exception {
        Files.createDirectories(OUT);
        StringBuilder csv = new StringBuilder("design,cost,order,completion,flexAssembly,flexInspection\n");
        for (int c = 1; c <= LIMITS[0]; c++) {
            for (int a = 0; a <= LIMITS[1]; a++) {
                for (int t = 0; t <= LIMITS[2]; t++) {
                    for (int i = 0; i <= LIMITS[3]; i++) {
                        for (int f = 0; f <= LIMITS[4]; f++) {
                            int[] counts = {c, a, t, i, f};
                            int cost = 0;
                            for (int n = 0; n < 5; n++) {
                                cost += counts[n] * PRICES[n];
                            }
                            if (cost > 1062 || a + t + f == 0 || i + f == 0) {
                                continue;
                            }
                            final int credits = cost;
                            final String design = key(counts);
                            orders(counts.clone(), "", order -> {
                                FactoryModel m = model(order, P2, false);
                                ExperimentEvidence e = execute(m, 12);
                                long tick = completion(e);
                                long[] flexUses = new long[3];
                                for (var event : e.retainedEvents()) {
                                    if (event.payload() instanceof RuntimeEventPayload.JobDispatched d
                                            && order.charAt((int) d.machineId().value() - 1) == 'F') {
                                        flexUses[d.stepIndex()]++;
                                    }
                                }
                                if (design.equals("C1A1T0I1F1")) {
                                    assertEquals(e.withNormalizedRunIdentity(),
                                            execute(m, 12).withNormalizedRunIdentity());
                                    assertEquals(tick, completion(execute(model(order, P2, true), 12)));
                                }
                                csv.append(design).append(',').append(credits).append(',').append(order).append(',')
                                        .append(tick).append(',').append(flexUses[1]).append(',')
                                        .append(flexUses[2]).append('\n');
                            });
                        }
                    }
                }
            }
        }
        Files.writeString(OUT.resolve("identity-orders.csv"), csv.toString());
        StringBuilder probes = new StringBuilder("profile,quantity,base,addition,completion\n");
        for (String base : List.of("CAI", "CAII", "CAAII", "CTII", "CAIF", "CAAAAAAF")) {
            for (String addition : List.of("", "C", "CC", "A", "I", "F")) {
                probes.append("P2,12,").append(base).append(',').append(addition).append(',')
                        .append(completion(execute(model(base + addition, P2, false), 12))).append('\n');
            }
        }
        for (long quantity : List.of(1L, 6L, 24L)) {
            for (String base : List.of("CAI", "CAII", "CAIF", "CTII")) {
                probes.append("P2,").append(quantity).append(',').append(base).append(",,")
                        .append(completion(execute(model(base, P2, false), quantity))).append('\n');
            }
        }
        Files.writeString(OUT.resolve("interventions.csv"), probes.toString());
    }
}
