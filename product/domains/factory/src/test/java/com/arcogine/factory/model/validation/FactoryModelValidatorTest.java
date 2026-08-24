package com.arcogine.factory.model.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModel.OperationDefinition;
import com.arcogine.factory.model.FactoryModel.OperationStepDefinition;
import com.arcogine.factory.model.FactoryModel.ProductDefinition;
import com.arcogine.factory.model.FactoryModel.ResourceDefinition;
import com.arcogine.factory.model.FactoryModel.ResourceInstance;
import com.arcogine.factory.model.validation.FactoryModelValidator.Finding;
import com.arcogine.factory.model.validation.FactoryModelValidator.Result;
import com.arcogine.factory.model.validation.FactoryModelValidator.Severity;
import java.util.List;
import org.junit.jupiter.api.Test;

class FactoryModelValidatorTest {

    @Test
    void acceptsAValidCurrentSemanticsModel() {
        Result result = FactoryModelValidator.validate(validModel());

        assertTrue(result.isValid());
        assertTrue(result.findings().isEmpty());
    }

    @Test
    void reportsStructuredReferenceErrorsInDeterministicOrder() {
        FactoryModel invalid = new FactoryModel(
                FactoryModel.CURRENT_SCHEMA_VERSION,
                List.of(new ProductDefinition(20, "Widget", 999)),
                List.of(new OperationDefinition(30, "Widget routing", List.of(888L))),
                List.of(new OperationStepDefinition(40, "Milling", 5, List.of(777L))),
                List.of(new ResourceDefinition(10, "Mill", 1, null, 0)),
                List.of(new ResourceInstance(10, 666)));

        Result first = FactoryModelValidator.validate(invalid);
        Result second = FactoryModelValidator.validate(invalid);

        assertFalse(first.isValid());
        assertEquals(first, second);
        assertEquals(
                List.of(
                        "UNKNOWN_OPERATION",
                        "UNKNOWN_OPERATION_STEP",
                        "UNKNOWN_RESOURCE_DEFINITION",
                        "UNKNOWN_ELIGIBLE_RESOURCE"),
                first.findings().stream().map(Finding::code).toList());
        assertEquals(Severity.ERROR, first.findings().getFirst().severity());
        assertEquals("product", first.findings().getFirst().entityType());
        assertEquals(20L, first.findings().getFirst().entityId());
        assertEquals("operationDefinitionId", first.findings().getFirst().path());
        assertEquals(List.of(999L), first.findings().getFirst().relatedIds());
    }

    @Test
    void rejectsDuplicateIdentifiersAndInvalidExecutableValues() {
        FactoryModel invalid = new FactoryModel(
                FactoryModel.CURRENT_SCHEMA_VERSION,
                List.of(
                        new ProductDefinition(20, "Widget", 30),
                        new ProductDefinition(20, "Duplicate", 30)),
                List.of(new OperationDefinition(30, "Widget routing", List.of(40L))),
                List.of(new OperationStepDefinition(40, "Milling", 0, List.of())),
                List.of(new ResourceDefinition(10, "Mill", 0, 0.0, -1)),
                List.of(new ResourceInstance(10, 10)));

        Result result = FactoryModelValidator.validate(invalid);
        List<String> codes = result.findings().stream().map(Finding::code).toList();

        assertFalse(result.isValid());
        assertTrue(codes.contains("DUPLICATE_PRODUCT_ID"));
        assertTrue(codes.contains("INVALID_RESOURCE_CONCURRENCY"));
        assertTrue(codes.contains("INVALID_RESOURCE_CAPACITY"));
        assertTrue(codes.contains("INVALID_RESOURCE_SETUP_TIME"));
        assertTrue(codes.contains("INVALID_OPERATION_DURATION"));
        assertTrue(codes.contains("NO_ELIGIBLE_RESOURCE"));
    }

    @Test
    void resultAndFindingsDefensivelyCopyTheirLists() {
        Finding finding = new Finding(
                "TEST",
                Severity.WARNING,
                "warning",
                "resource",
                1,
                "field",
                List.of(2L));
        Result result = new Result(List.of(finding));

        assertEquals(List.of(2L), result.findings().getFirst().relatedIds());
        assertEquals(List.of(finding), result.findings());
    }

    private static FactoryModel validModel() {
        return new FactoryModel(
                FactoryModel.CURRENT_SCHEMA_VERSION,
                List.of(new ProductDefinition(20, "Widget", 30)),
                List.of(new OperationDefinition(30, "Widget routing", List.of(40L))),
                List.of(new OperationStepDefinition(40, "Milling", 5, List.of(10L))),
                List.of(new ResourceDefinition(10, "Mill", 1, null, 0)),
                List.of(new ResourceInstance(10, 10)));
    }
}
