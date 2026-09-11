package com.arcogine.factory.model.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FactoryModelV2Test {

    private static ConfiguredResource mill(long id) {
        return new ConfiguredResource(new MachineId(id), "Mill " + id, 1, null, 0);
    }

    private static OperationDefinition routing() {
        return new OperationDefinition(
                100,
                "Widget routing",
                List.of(new OperationStepDefinition(1, "Rough milling", Set.of(new MachineId(1)), 5)));
    }

    @Test
    void requiresNonNullFloor() {
        assertThrows(
                NullPointerException.class,
                () -> new FactoryModelV2(null, 1, 1, List.of(), List.of(), List.of()));
    }

    @Test
    void defensivelyCopiesResourceListSoLaterMutationDoesNotLeak() {
        List<SpatialConfiguredResource> mutable = new ArrayList<>();
        mutable.add(new SpatialConfiguredResource(
                mill(1), new ResourcePlacement(0, 0), new ResourceFootprint(1, 1)));

        FactoryModelV2 model = new FactoryModelV2(
                new FactoryFloor(10, 10), 1, 0, mutable, List.of(), List.of());
        mutable.clear();

        assertEquals(1, model.resources().size());
    }

    @Test
    void baseModelProjectsOnlyV1ShapedContent() {
        SpatialConfiguredResource spatial = new SpatialConfiguredResource(
                mill(1), new ResourcePlacement(3, 4), new ResourceFootprint(2, 2));
        FactoryModelV2 model = new FactoryModelV2(
                new FactoryFloor(10, 10),
                7,
                11,
                List.of(spatial),
                List.of(routing()),
                List.of(new ProductDefinition(new ProductId(10), "Widget", 100)));

        FactoryModel base = model.baseModel();

        assertEquals(List.of(spatial.resource()), base.resources());
        assertEquals(List.of(routing()), base.operations());
        assertEquals(1, base.products().size());
        assertTrue(FactoryModel.class.isInstance(base));
    }
}
