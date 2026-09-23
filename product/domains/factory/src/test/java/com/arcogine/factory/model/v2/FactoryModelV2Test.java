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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FactoryModelV2Test {

    private static ConfiguredResource mill(long id) {
        return new ConfiguredResource(new MachineId(id), "Mill " + id, 1, null, 0);
    }

    private static ResourceLayout layout(long id, long x, long y) {
        return new ResourceLayout(new MachineId(id), new ResourcePlacement(x, y), new ResourceFootprint(1, 1));
    }

    private static OperationDefinition routing() {
        return new OperationDefinition(
                100,
                "Widget routing",
                List.of(new OperationStepDefinition(1, "Rough milling", Set.of(new MachineId(1)), 5)));
    }

    private static List<ProductDefinition> widget() {
        return List.of(new ProductDefinition(new ProductId(10), "Widget", 100));
    }

    @Test
    void spatialAbsenceMustBeExplicitRatherThanNull() {
        assertThrows(
                NullPointerException.class,
                () -> new FactoryModelV2(List.of(), List.of(), List.of(), null));
    }

    @Test
    void partialSpatialContentCannotBeConstructed() {
        // A present spatial record carries its floor and, per resource, both placement and
        // footprint; the magnitudes are primitives, so every construction supplies an explicit
        // value (zero is a legal explicit value, not an absent one).
        assertThrows(NullPointerException.class, () -> new SpatialRecord(null, 0, 0, List.of()));
        assertThrows(
                NullPointerException.class,
                () -> new ResourceLayout(null, new ResourcePlacement(0, 0), new ResourceFootprint(1, 1)));
        assertThrows(
                NullPointerException.class,
                () -> new ResourceLayout(new MachineId(1), null, new ResourceFootprint(1, 1)));
        assertThrows(
                NullPointerException.class,
                () -> new ResourceLayout(new MachineId(1), new ResourcePlacement(0, 0), null));
        assertThrows(
                NullPointerException.class,
                () -> new SpatialRecord(new FactoryFloor(1, 1), 0, 0, Arrays.asList(layout(1, 0, 0), null)));
    }

    @Test
    void defensivelyCopiesListsSoLaterMutationDoesNotLeak() {
        List<ConfiguredResource> resources = new ArrayList<>(List.of(mill(1)));
        List<ResourceLayout> layouts = new ArrayList<>(List.of(layout(1, 0, 0)));

        SpatialRecord spatial = new SpatialRecord(new FactoryFloor(10, 10), 1, 0, layouts);
        FactoryModelV2 model = new FactoryModelV2(resources, List.of(), List.of(), Optional.of(spatial));
        resources.clear();
        layouts.clear();

        assertEquals(1, model.resources().size());
        assertEquals(1, model.spatial().orElseThrow().resourceLayouts().size());
    }

    @Test
    void baseModelProjectsTheProductionRecordsWhetherSpatialIsPresentOrAbsent() {
        FactoryModelV2 absent = new FactoryModelV2(List.of(mill(1)), List.of(routing()), widget(), Optional.empty());
        FactoryModelV2 present = new FactoryModelV2(
                List.of(mill(1)),
                List.of(routing()),
                widget(),
                Optional.of(new SpatialRecord(new FactoryFloor(10, 10), 7, 11, List.of(layout(1, 3, 4)))));

        FactoryModel expected = new FactoryModel(List.of(mill(1)), List.of(routing()), widget());

        assertEquals(expected, absent.baseModel());
        assertEquals(expected, present.baseModel());
        assertTrue(FactoryModel.class.isInstance(present.baseModel()));
    }
}
