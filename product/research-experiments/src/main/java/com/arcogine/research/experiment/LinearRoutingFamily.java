package com.arcogine.research.experiment;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A parameterized, non-spatial production family with one linear routing, authored from its
 * resources: an ordered list of routing steps, and an ordered list of resources that each have a
 * concurrency and the set of steps they may serve. A resource may serve several steps, which
 * current Factory semantics allow; its capacity is then shared between those steps.
 *
 * <p>The family projects onto the current Factory model exactly as authored: each step is one
 * routing step with one duration, and each resource is one {@link ConfiguredResource} eligible for
 * exactly the steps it lists. It invents nothing the model does not carry: there is no per-resource
 * speed, because duration belongs to the step, no spatial record, and {@code setupTime} and {@code
 * capacityLiters} keep values the current Engine does not read ({@code
 * docs/architecture/factory-model.md} section 2.1). Costs, budgets and any other game-side
 * parameters are not part of it.
 *
 * <p>Resource order is an explicit experiment input. Resources are numbered from 1 in list order and
 * steps likewise, so identities are stable for a given parameterization. Under the current Engine
 * interpretation, {@code MachineId} is the final resource-selection tie-break ({@code
 * docs/architecture/engine-semantics.md} section 2 rule 4): when selection among eligible resources
 * is otherwise tied, the resource with the lower identifier is chosen. Reordering resources can
 * therefore change which resource takes a unit of work and, when that choice matters later, the
 * run's outcome. Whether a particular design is sensitive to its resource order is a question an
 * experiment answers; this type records the order as an input and neither makes the tie-break a
 * policy nor implies that any design is order-sensitive. Because resource list order and identifiers
 * are canonical model content, a reordered family is also a different published model.
 *
 * @param steps the routing steps, in routing order; step identifiers are their 1-based positions
 * @param resources the resources, in authored order; machine identifiers are their 1-based
 *     positions
 */
public record LinearRoutingFamily(List<Step> steps, List<Resource> resources) {

    public static final ProductId PRODUCT = new ProductId(1);
    public static final long OPERATION_ID = 1;
    public static final String OPERATION_NAME = "Widget routing";
    public static final String PRODUCT_NAME = "Widget";
    public static final double UNIT_PRICE = 10.0;

    /** One routing step: its name and its duration in ticks. */
    public record Step(String name, long duration) {

        public Step {
            requireName(name, "step");
            if (duration < 1) {
                throw new IllegalArgumentException("step '" + name + "' needs a duration of at least one tick, got "
                        + duration);
            }
        }
    }

    /**
     * One configured resource: its name, its concurrency, and the names of the steps it may serve.
     */
    public record Resource(String name, int concurrency, Set<String> eligibleSteps) {

        public Resource {
            requireName(name, "resource");
            if (concurrency < 1) {
                throw new IllegalArgumentException("resource '" + name + "' needs a concurrency of at least 1, got "
                        + concurrency);
            }
            eligibleSteps = Set.copyOf(Objects.requireNonNull(eligibleSteps, "eligibleSteps"));
            if (eligibleSteps.isEmpty()) {
                throw new IllegalArgumentException("resource '" + name + "' must be eligible for at least one step");
            }
        }

        public static Resource of(String name, int concurrency, String... eligibleSteps) {
            return new Resource(name, concurrency, Set.of(eligibleSteps));
        }
    }

    public LinearRoutingFamily {
        steps = List.copyOf(Objects.requireNonNull(steps, "steps"));
        resources = List.copyOf(Objects.requireNonNull(resources, "resources"));
        if (steps.isEmpty() || resources.isEmpty()) {
            throw new IllegalArgumentException("a family needs at least one step and one resource");
        }
        Set<String> stepNames = new HashSet<>();
        for (Step step : steps) {
            if (!stepNames.add(step.name())) {
                throw new IllegalArgumentException("duplicate step name '" + step.name() + "'");
            }
        }
        Set<String> resourceNames = new HashSet<>();
        Set<String> servedSteps = new HashSet<>();
        for (Resource resource : resources) {
            if (!resourceNames.add(resource.name())) {
                throw new IllegalArgumentException("duplicate resource name '" + resource.name() + "'");
            }
            for (String step : resource.eligibleSteps()) {
                if (!stepNames.contains(step)) {
                    throw new IllegalArgumentException(
                            "resource '" + resource.name() + "' is eligible for unknown step '" + step + "'");
                }
            }
            servedSteps.addAll(resource.eligibleSteps());
        }
        for (Step step : steps) {
            if (!servedSteps.contains(step.name())) {
                throw new IllegalArgumentException("no resource is eligible for step '" + step.name() + "'");
            }
        }
    }

    /** The machine identifier of the named resource: its 1-based position in the resource order. */
    public MachineId resourceId(String resourceName) {
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).name().equals(resourceName)) {
                return new MachineId(i + 1L);
            }
        }
        throw new IllegalArgumentException("no resource named '" + resourceName + "'");
    }

    /** The step identifier of the named step: its 1-based position in the routing. */
    public long stepId(String stepName) {
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).name().equals(stepName)) {
                return i + 1L;
            }
        }
        throw new IllegalArgumentException("no step named '" + stepName + "'");
    }

    /** The identifiers of the resources eligible for the named step, in resource order. */
    public List<MachineId> eligibleResources(String stepName) {
        if (steps.stream().noneMatch(step -> step.name().equals(stepName))) {
            throw new IllegalArgumentException("no step named '" + stepName + "'");
        }
        List<MachineId> ids = new ArrayList<>();
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).eligibleSteps().contains(stepName)) {
                ids.add(new MachineId(i + 1L));
            }
        }
        return List.copyOf(ids);
    }

    /** The same design with its resources in {@code resourceNames} order, which must name each exactly once. */
    public LinearRoutingFamily withResourceOrder(List<String> resourceNames) {
        if (resourceNames.size() != resources.size() || Set.copyOf(resourceNames).size() != resources.size()) {
            throw new IllegalArgumentException("a resource order must name every resource exactly once: " + resourceNames);
        }
        List<Resource> reordered = new ArrayList<>();
        for (String name : resourceNames) {
            reordered.add(resources.get((int) resourceId(name).value() - 1));
        }
        return new LinearRoutingFamily(steps, reordered);
    }

    /** The same design with its resource order reversed. */
    public LinearRoutingFamily withReversedResourceOrder() {
        return new LinearRoutingFamily(steps, resources.reversed());
    }

    /** Builds the authored, spatial-record-free Factory model for this parameterization. */
    public FactoryModel model() {
        List<ConfiguredResource> configured = new ArrayList<>();
        for (int i = 0; i < resources.size(); i++) {
            Resource resource = resources.get(i);
            configured.add(new ConfiguredResource(new MachineId(i + 1L), resource.name(), resource.concurrency(), null, 0));
        }
        List<OperationStepDefinition> routing = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            routing.add(new OperationStepDefinition(
                    i + 1L, step.name(), new LinkedHashSet<>(eligibleResources(step.name())), step.duration()));
        }
        return new FactoryModel(
                configured,
                List.of(new OperationDefinition(OPERATION_ID, OPERATION_NAME, routing)),
                List.of(new ProductDefinition(PRODUCT, PRODUCT_NAME, OPERATION_ID)));
    }

    private static void requireName(String name, String kind) {
        if (Objects.requireNonNull(name, kind + " name").isBlank()) {
            throw new IllegalArgumentException(kind + " name must not be blank");
        }
    }
}
