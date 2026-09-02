package com.arcogine.challenge.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.challenge.ChallengeDefinition;
import com.arcogine.challenge.EquipmentCatalogueItemId;
import com.arcogine.challenge.validation.ChallengeDefinitionValidationResult;
import com.arcogine.challenge.validation.ChallengeDefinitionValidator;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChallengeContentLoaderTest {

    private static final String VALID_MINIMAL = """
            {
              "identity": {"id": "bottleneck-101", "version": "1"},
              "floor": {"width": 10, "height": 8},
              "startingBudget": 5000,
              "workload": {"productReference": "widget", "requiredQuantity": 20},
              "availableEquipment": ["assembler.basic", "assembler.fast"],
              "deadline": 400,
              "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"}
            }
            """;

    private static final String VALID_WITH_CATALOGUE_IDENTITY = """
            {
              "identity": {"id": "budget-squeeze", "version": "2"},
              "floor": {"width": 6, "height": 6},
              "startingBudget": 1200,
              "workload": {"productReference": "gadget", "requiredQuantity": 5},
              "availableEquipment": [],
              "deadline": 200,
              "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"},
              "catalogueIdentity": {"id": "catalogue.core", "version": "3"},
              "catalogueSemanticFingerprint": "fp-abc123"
            }
            """;

    @Test
    void loadsAMinimalValidDefinition() {
        ChallengeContentLoadResult result = ChallengeContentLoader.load(VALID_MINIMAL);

        assertTrue(result.isSuccess(), () -> "expected success, got issues: " + result.issues());
        ChallengeDefinition definition = result.definition();
        assertEquals("bottleneck-101", definition.identity().id());
        assertEquals("1", definition.identity().version());
        assertEquals(10, definition.floor().width());
        assertEquals(8, definition.floor().height());
        assertEquals(5000L, definition.startingBudget());
        assertEquals("widget", definition.workload().productReference());
        assertEquals(20, definition.workload().requiredQuantity());
        assertEquals(
                List.of(new EquipmentCatalogueItemId("assembler.basic"), new EquipmentCatalogueItemId("assembler.fast")),
                definition.availableEquipment());
        assertEquals(400L, definition.deadline());
        assertEquals("policy.contract-completion", definition.evaluationPolicy().id());

        ChallengeDefinitionValidationResult validation = ChallengeDefinitionValidator.validate(definition);
        assertTrue(validation.isValid(), () -> "expected content-valid definition, got: " + validation.issues());
    }

    @Test
    void loadsADefinitionWithExplicitCatalogueIdentityAndFingerprint() {
        ChallengeContentLoadResult result = ChallengeContentLoader.load(VALID_WITH_CATALOGUE_IDENTITY);

        assertTrue(result.isSuccess(), () -> "expected success, got issues: " + result.issues());
        ChallengeDefinition definition = result.definition();
        assertEquals("catalogue.core", definition.catalogueIdentity().id());
        assertEquals("3", definition.catalogueIdentity().version());
        assertEquals("fp-abc123", definition.catalogueSemanticFingerprint());
    }

    @Test
    void isDeterministicAcrossRepeatedLoads() {
        ChallengeContentLoadResult first = ChallengeContentLoader.load(VALID_MINIMAL);
        ChallengeContentLoadResult second = ChallengeContentLoader.load(VALID_MINIMAL);
        assertEquals(first.definition(), second.definition());
    }

    @Test
    void rejectsMalformedJsonAsAContentIssueNotAnException() {
        ChallengeContentLoadResult result = ChallengeContentLoader.load("{ not json");

        assertFalse(result.isSuccess());
        assertEquals(1, result.issues().size());
        assertEquals("content.malformed-json", result.issues().get(0).code());
    }

    @Test
    void rejectsNonObjectRoot() {
        ChallengeContentLoadResult result = ChallengeContentLoader.load("[1, 2, 3]");

        assertFalse(result.isSuccess());
        assertEquals("content.root.not-object", result.issues().get(0).code());
    }

    @Test
    void rejectsNullSource() {
        ChallengeContentLoadResult result = ChallengeContentLoader.load(null);

        assertFalse(result.isSuccess());
        assertEquals("content.source.null", result.issues().get(0).code());
    }

    @Test
    void reportsEveryMissingRequiredFieldInOnePass() {
        ChallengeContentLoadResult result = ChallengeContentLoader.load("{}");

        assertFalse(result.isSuccess());
        List<String> codes = result.issues().stream().map(ChallengeContentIssue::code).toList();
        List<String> paths = result.issues().stream().map(ChallengeContentIssue::path).toList();
        assertTrue(codes.stream().allMatch("content.field.missing"::equals));
        assertTrue(paths.contains("identity"));
        assertTrue(paths.contains("floor"));
        assertTrue(paths.contains("startingBudget"));
        assertTrue(paths.contains("workload"));
        assertTrue(paths.contains("deadline"));
        assertTrue(paths.contains("evaluationPolicy"));
    }

    @Test
    void rejectsMistypedFields() {
        String source = """
                {
                  "identity": {"id": "x", "version": "1"},
                  "floor": {"width": "wide", "height": 8},
                  "startingBudget": "lots",
                  "workload": {"productReference": "widget", "requiredQuantity": 20},
                  "availableEquipment": "not-an-array",
                  "deadline": 400,
                  "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"}
                }
                """;

        ChallengeContentLoadResult result = ChallengeContentLoader.load(source);

        assertFalse(result.isSuccess());
        List<String> paths = result.issues().stream().map(ChallengeContentIssue::path).toList();
        assertTrue(paths.contains("floor.width"));
        assertTrue(paths.contains("startingBudget"));
        assertTrue(paths.contains("availableEquipment"));
    }

    @Test
    void rejectsNonIntegerNumericFields() {
        String source = """
                {
                  "identity": {"id": "x", "version": "1"},
                  "floor": {"width": 4.5, "height": 8},
                  "startingBudget": 100,
                  "workload": {"productReference": "widget", "requiredQuantity": 20},
                  "deadline": 400,
                  "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"}
                }
                """;

        ChallengeContentLoadResult result = ChallengeContentLoader.load(source);

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream().anyMatch(i -> i.path().equals("floor.width")));
    }

    @Test
    void rejectsMistypedAvailableEquipmentElements() {
        String source = """
                {
                  "identity": {"id": "x", "version": "1"},
                  "floor": {"width": 4, "height": 8},
                  "startingBudget": 100,
                  "workload": {"productReference": "widget", "requiredQuantity": 20},
                  "availableEquipment": ["ok", 5],
                  "deadline": 400,
                  "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"}
                }
                """;

        ChallengeContentLoadResult result = ChallengeContentLoader.load(source);

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream().anyMatch(i -> i.path().equals("availableEquipment[1]")));
    }

    @Test
    void loadingSucceedsButValidatorCatchesContentInvalidValues() {
        String source = """
                {
                  "identity": {"id": "", "version": "1"},
                  "floor": {"width": -1, "height": 8},
                  "startingBudget": -5,
                  "workload": {"productReference": "widget", "requiredQuantity": 0},
                  "deadline": 0,
                  "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"}
                }
                """;

        ChallengeContentLoadResult result = ChallengeContentLoader.load(source);

        assertTrue(result.isSuccess(), () -> "structural decoding should succeed: " + result.issues());
        ChallengeDefinitionValidationResult validation = ChallengeDefinitionValidator.validate(result.definition());
        assertFalse(validation.isValid());
        assertTrue(validation.issues().size() >= 5);
    }
}
