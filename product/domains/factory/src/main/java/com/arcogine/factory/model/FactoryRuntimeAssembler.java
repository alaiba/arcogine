package com.arcogine.factory.model;

import com.arcogine.factory.machines.Machine;
import com.arcogine.factory.machines.MachineStore;
import com.arcogine.factory.process.FactoryHandler;
import com.arcogine.factory.routing.Routing;
import com.arcogine.factory.routing.RoutingStep;
import com.arcogine.factory.routing.RoutingStore;
import com.arcogine.types.ProductId;
import java.util.ArrayList;
import java.util.List;

/**
 * Constructs factory runtime state ({@link MachineStore}, {@link RoutingStore}, {@link
 * FactoryHandler}) from one published {@link FactoryModelVersion}.
 *
 * <p>This is the runtime-construction half of the model boundary: runtime state is instantiated
 * from the canonical model, never assembled directly from scenario/consumer input. The model
 * itself is never mutated by this process. No re-validation happens here: {@link
 * FactoryModelVersion}'s own canonical constructor already guarantees {@code version.model()} is
 * valid, since there is no way to construct a {@code FactoryModelVersion} wrapping an invalid
 * model in the first place.
 */
public final class FactoryRuntimeAssembler {

    private FactoryRuntimeAssembler() {}

    public static Assembled assemble(FactoryModelVersion version) {
        FactoryModel model = version.model();

        MachineStore machines = new MachineStore();
        for (ResourceDefinition resource : model.resources()) {
            machines.add(new Machine(
                    resource.id(),
                    resource.name(),
                    resource.concurrency(),
                    resource.capacityLiters(),
                    resource.setupTime()));
        }

        RoutingStore routings = new RoutingStore();
        for (OperationDefinition operation : model.operations()) {
            List<RoutingStep> steps = new ArrayList<>();
            for (OperationStepDefinition step : operation.steps()) {
                steps.add(new RoutingStep(
                        step.stepId(), step.name(), step.eligibleResources(), step.duration()));
            }
            routings.addRouting(new Routing(operation.id(), operation.name(), steps));
        }

        List<ProductId> productIds = new ArrayList<>();
        for (ProductDefinition product : model.products()) {
            productIds.add(product.id());
            routings.addProductRouting(product.id(), product.operationId());
        }

        FactoryHandler factory = new FactoryHandler(machines, routings, productIds);
        return new Assembled(factory, List.copyOf(productIds));
    }

    /** The runtime factory handler and the product ids it was assembled for. */
    public record Assembled(FactoryHandler factory, List<ProductId> productIds) {}
}
