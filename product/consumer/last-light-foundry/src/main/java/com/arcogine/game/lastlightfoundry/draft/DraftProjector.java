package com.arcogine.game.lastlightfoundry.draft;

import com.arcogine.challenge.EquipmentCatalogueItemId;
import com.arcogine.challenge.admissibility.CandidateDraftSnapshot;
import com.arcogine.challenge.admissibility.EquipmentOccurrenceId;
import com.arcogine.challenge.admissibility.GridPlacement;
import com.arcogine.challenge.admissibility.PlacedEquipment;
import com.arcogine.challenge.economics.DraftEquipmentOccurrence;
import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.game.lastlightfoundry.content.Mission;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The deterministic draft-to-Factory projection.
 *
 * <p>Every result-affecting rule is explicit here: a resource's identity is its installation
 * number; resources and each step's eligible set are ordered by ascending installation number;
 * operation, step and product identities are fixed by the mission. Floor coordinates, the order
 * the client listed installations in, and any collection traversal order never reach the Factory
 * model. Moving an installation changes nothing.
 */
public final class DraftProjector {

    /** Fixed identity of the mission's single operation. */
    static final long OPERATION_ID = 100;

    /** Fixed identity of the mission's single product. */
    static final long PRODUCT_ID = 1;

    private DraftProjector() {}

    /** The Challenge-side view of the draft: occurrences keyed by zero-padded installation number. */
    public static CandidateDraftSnapshot toCandidateSnapshot(Draft draft) {
        return new CandidateDraftSnapshot(ordered(draft).stream()
                .map(installation -> new PlacedEquipment(
                        occurrenceId(installation.number()),
                        new EquipmentCatalogueItemId(installation.itemId()),
                        new GridPlacement(installation.x(), installation.y())))
                .toList());
    }

    /** The economics-side view of the draft. */
    public static List<DraftEquipmentOccurrence> toEconomicsOccurrences(Draft draft) {
        return ordered(draft).stream()
                .map(installation -> new DraftEquipmentOccurrence(new EquipmentCatalogueItemId(installation.itemId())))
                .toList();
    }

    /** Stable Challenge occurrence identity for an installation number. */
    public static EquipmentOccurrenceId occurrenceId(int number) {
        return new EquipmentOccurrenceId(String.format("installation-%04d", number));
    }

    /** The result of projecting a draft: a Factory model, or the reasons it could not be derived. */
    public record Projection(FactoryModel model, List<String> issues) {
        public boolean derivable() {
            return issues.isEmpty();
        }
    }

    /** Projects the draft to canonical Factory facts using the mission's equipment templates. */
    public static Projection toFactoryModel(Mission mission, Draft draft) {
        List<String> issues = new ArrayList<>();
        List<Installation> installations = ordered(draft);
        List<ConfiguredResource> resources = new ArrayList<>();
        for (Installation installation : installations) {
            Mission.EquipmentTemplate template = mission.equipment().get(installation.itemId());
            if (template == null) {
                issues.add("installation " + installation.number() + " uses unknown equipment "
                        + installation.itemId());
                continue;
            }
            resources.add(new ConfiguredResource(
                    new MachineId(installation.number()),
                    template.name() + " #" + installation.number(),
                    template.concurrency(),
                    null,
                    0));
        }
        List<OperationStepDefinition> steps = new ArrayList<>();
        for (int index = 0; index < mission.steps().size(); index++) {
            Mission.StepSpec spec = mission.steps().get(index);
            Set<MachineId> eligible = new LinkedHashSet<>();
            for (Installation installation : installations) {
                Mission.EquipmentTemplate template = mission.equipment().get(installation.itemId());
                if (template != null && template.steps().contains(spec.name())) {
                    eligible.add(new MachineId(installation.number()));
                }
            }
            steps.add(new OperationStepDefinition(index + 1L, spec.name(), eligible, spec.duration()));
        }
        FactoryModel model = new FactoryModel(
                resources,
                List.of(new OperationDefinition(OPERATION_ID, mission.productName() + " routing", steps)),
                List.of(new ProductDefinition(new ProductId(PRODUCT_ID), mission.productName(), OPERATION_ID)));
        return new Projection(model, List.copyOf(issues));
    }

    /** The product the mission's primary workload is submitted for. */
    public static ProductId productId() {
        return new ProductId(PRODUCT_ID);
    }

    private static List<Installation> ordered(Draft draft) {
        return draft.installations().stream()
                .sorted(Comparator.comparingInt(Installation::number)
                        .thenComparing(Installation::itemId)
                        .thenComparingInt(Installation::x)
                        .thenComparingInt(Installation::y))
                .toList();
    }
}
