package com.arcogine.factory.model.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.ResourceDefinition;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FactoryModelValidatorTest {

    private static ResourceDefinition mill() {
        return new ResourceDefinition(new MachineId(1), "Mill", 1, null, 0);
    }

    private static OperationDefinition routing() {
        return new OperationDefinition(
                100,
                "Widget routing",
                List.of(new OperationStepDefinition(1, "Rough milling", Set.of(new MachineId(1)), 5)));
    }

    @Test
    void validModelHasNoErrors() {
        FactoryModel model = new FactoryModel(
                List.of(mill()), List.of(routing()), List.of(new ProductDefinition(new ProductId(10), "Widget", 100)));

        ModelValidationResult result = FactoryModelValidator.validate(model);

        assertTrue(result.isValid(), () -> result.errors().toString());
    }

    @Test
    void rejectsProductReferencingUnknownOperation() {
        FactoryModel model = new FactoryModel(
                List.of(mill()), List.of(routing()), List.of(new ProductDefinition(new ProductId(10), "Widget", 999)));

        ModelValidationResult result = FactoryModelValidator.validate(model);

        assertFalse(result.isValid());
    }

    @Test
    void rejectsStepReferencingUnknownResource() {
        OperationDefinition badRouting = new OperationDefinition(
                100,
                "Widget routing",
                List.of(new OperationStepDefinition(1, "Rough milling", Set.of(new MachineId(999)), 5)));
        FactoryModel model = new FactoryModel(
                List.of(mill()), List.of(badRouting), List.of(new ProductDefinition(new ProductId(10), "Widget", 100)));

        ModelValidationResult result = FactoryModelValidator.validate(model);

        assertFalse(result.isValid());
    }

    @Test
    void rejectsStepWithMoreThanOneEligibleResource() {
        ResourceDefinition otherMill = new ResourceDefinition(new MachineId(2), "Mill B", 1, null, 0);
        OperationDefinition multiEligible = new OperationDefinition(
                100,
                "Widget routing",
                List.of(new OperationStepDefinition(
                        1, "Rough milling", Set.of(new MachineId(1), new MachineId(2)), 5)));
        FactoryModel model = new FactoryModel(
                List.of(mill(), otherMill),
                List.of(multiEligible),
                List.of(new ProductDefinition(new ProductId(10), "Widget", 100)));

        ModelValidationResult result = FactoryModelValidator.validate(model);

        assertFalse(
                result.isValid(),
                "runtime cannot faithfully instantiate a step with more than one eligible resource "
                        + "in this milestone -- see FactoryRuntimeAssembler");
    }

    @Test
    void rejectsDuplicateResourceIds() {
        FactoryModel model = new FactoryModel(List.of(mill(), mill()), List.of(), List.of());

        ModelValidationResult result = FactoryModelValidator.validate(model);

        assertFalse(result.isValid());
    }

    @Test
    void rejectsResourceWithNonPositiveConcurrency() {
        ResourceDefinition idleMill = new ResourceDefinition(new MachineId(1), "Mill", 0, null, 0);
        FactoryModel model = new FactoryModel(List.of(idleMill), List.of(), List.of());

        ModelValidationResult result = FactoryModelValidator.validate(model);

        assertFalse(result.isValid());
    }

    @Test
    void rejectsDuplicateOperationIds() {
        FactoryModel model = new FactoryModel(List.of(mill()), List.of(routing(), routing()), List.of());

        ModelValidationResult result = FactoryModelValidator.validate(model);

        assertFalse(result.isValid());
    }

    @Test
    void rejectsStepWithNonPositiveDuration() {
        OperationDefinition zeroDuration = new OperationDefinition(
                100,
                "Widget routing",
                List.of(new OperationStepDefinition(1, "Rough milling", Set.of(new MachineId(1)), 0)));
        FactoryModel model = new FactoryModel(List.of(mill()), List.of(zeroDuration), List.of());

        ModelValidationResult result = FactoryModelValidator.validate(model);

        assertFalse(result.isValid());
    }

    @Test
    void rejectsStepWithNoEligibleResources() {
        OperationDefinition noEligible = new OperationDefinition(
                100, "Widget routing", List.of(new OperationStepDefinition(1, "Rough milling", Set.of(), 5)));
        FactoryModel model = new FactoryModel(List.of(mill()), List.of(noEligible), List.of());

        ModelValidationResult result = FactoryModelValidator.validate(model);

        assertFalse(result.isValid());
    }

    @Test
    void rejectsDuplicateProductIds() {
        ProductDefinition widget = new ProductDefinition(new ProductId(10), "Widget", 100);
        FactoryModel model = new FactoryModel(List.of(mill()), List.of(routing()), List.of(widget, widget));

        ModelValidationResult result = FactoryModelValidator.validate(model);

        assertFalse(result.isValid());
    }

    @Test
    void rejectsOperationWithNoSteps() {
        OperationDefinition empty = new OperationDefinition(100, "Empty", List.of());
        FactoryModel model = new FactoryModel(List.of(mill()), List.of(empty), List.of());

        ModelValidationResult result = FactoryModelValidator.validate(model);

        assertFalse(result.isValid());
    }

    @Test
    void requireValidThrowsOnInvalidModel() {
        FactoryModel model = new FactoryModel(List.of(mill(), mill()), List.of(), List.of());

        FactoryModelValidationException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        FactoryModelValidationException.class,
                        () -> FactoryModelValidator.requireValid(model));

        assertFalse(exception.result().isValid());
    }
}
