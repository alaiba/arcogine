package com.arcogine.challenge.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assertions.assertNull;

import com.arcogine.challenge.ChallengeDefinition;
import com.arcogine.challenge.EquipmentCatalogueItemId;
import com.arcogine.challenge.admissibility.CandidateAdmissibilityPolicy;
import com.arcogine.challenge.admissibility.CandidateAdmissibilityResult;
import com.arcogine.challenge.admissibility.CandidateDraftSnapshot;
import com.arcogine.challenge.validation.ChallengeDefinitionValidationResult;
import com.arcogine.challenge.validation.ChallengeDefinitionValidator;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChallengeContentLoaderTest {

    private static final String VALID_MINIMAL = """
            {
              "schemaVersion": "challenge-content:v1",
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
              "schemaVersion": "challenge-content:v1",
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
                  "schemaVersion": "challenge-content:v1",
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

    @Test
    void rejectsMissingSchemaVersion() {
        String source = """
                {
                  "identity": {"id": "x", "version": "1"},
                  "floor": {"width": 4, "height": 8},
                  "startingBudget": 100,
                  "workload": {"productReference": "widget", "requiredQuantity": 20},
                  "deadline": 400,
                  "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"}
                }
                """;

        ChallengeContentLoadResult result = ChallengeContentLoader.load(source);

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.code().equals("content.field.missing") && i.path().equals("schemaVersion")));
    }

    @Test
    void rejectsUnsupportedSchemaVersion() {
        String source = """
                {
                  "schemaVersion": "challenge-content:v99",
                  "identity": {"id": "x", "version": "1"},
                  "floor": {"width": 4, "height": 8},
                  "startingBudget": 100,
                  "workload": {"productReference": "widget", "requiredQuantity": 20},
                  "deadline": 400,
                  "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"}
                }
                """;

        ChallengeContentLoadResult result = ChallengeContentLoader.load(source);

        assertFalse(result.isSuccess());
        assertEquals("content.schemaVersion.unsupported", result.issues().get(0).code());
        assertEquals("schemaVersion", result.issues().get(0).path());
    }

    @Test
    void rejectsUnsupportedEvaluationPolicy() {
        String source = """
                {
                  "schemaVersion": "challenge-content:v1",
                  "identity": {"id": "x", "version": "1"},
                  "floor": {"width": 4, "height": 8},
                  "startingBudget": 100,
                  "workload": {"productReference": "widget", "requiredQuantity": 20},
                  "deadline": 400,
                  "evaluationPolicy": {"id": "policy.unknown", "version": "7"}
                }
                """;

        ChallengeContentLoadResult result = ChallengeContentLoader.load(source);

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.code().equals("evaluationPolicy.unsupported") && i.path().equals("evaluationPolicy")));
    }

    @Test
    void loadsAValidCatalogue() {
        String source = """
                {
                  "schemaVersion": "equipment-catalogue:v1",
                  "identity": {"id": "catalogue.core", "version": "1"},
                  "offers": [
                    {"itemId": "assembler.basic", "purchaseCostCredits": 500},
                    {"itemId": "assembler.fast", "purchaseCostCredits": 900, "quantityLimit": 2}
                  ]
                }
                """;

        EquipmentCatalogueContentLoadResult result = ChallengeContentLoader.loadCatalogue(source);

        assertTrue(result.isSuccess(), () -> "expected success, got issues: " + result.issues());
        assertEquals("catalogue.core", result.catalogue().identity().id());
        assertEquals(2, result.catalogue().offers().size());
    }

    @Test
    void rejectsCatalogueWithDuplicateItemIds() {
        String source = """
                {
                  "schemaVersion": "equipment-catalogue:v1",
                  "identity": {"id": "catalogue.core", "version": "1"},
                  "offers": [
                    {"itemId": "assembler.basic", "purchaseCostCredits": 500},
                    {"itemId": "assembler.basic", "purchaseCostCredits": 600}
                  ]
                }
                """;

        EquipmentCatalogueContentLoadResult result = ChallengeContentLoader.loadCatalogue(source);

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream().anyMatch(i -> i.code().equals("catalogue.offers.itemId.duplicate")));
    }

    @Test
    void rejectsCatalogueMissingSchemaVersion() {
        String source = """
                {
                  "identity": {"id": "catalogue.core", "version": "1"},
                  "offers": []
                }
                """;

        EquipmentCatalogueContentLoadResult result = ChallengeContentLoader.loadCatalogue(source);

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.code().equals("content.field.missing") && i.path().equals("schemaVersion")));
    }

    private static final String VALID_CAPACITY_COST_TRADEOFF = """
            {
              "schemaVersion": "challenge-content:v1",
              "identity": {"id": "capacity-cost-tradeoff", "version": "1"},
              "floor": {"width": 14, "height": 12},
              "startingBudget": 20000,
              "workload": {"productReference": "widget", "requiredQuantity": 500},
              "availableEquipment": ["assembler.basic", "assembler.fast", "assembler.premium"],
              "deadline": 1000,
              "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"}
            }
            """;

    private static final String VALID_TIGHT_DEADLINE = """
            {
              "schemaVersion": "challenge-content:v1",
              "identity": {"id": "tight-deadline", "version": "1"},
              "floor": {"width": 8, "height": 8},
              "startingBudget": 3000,
              "workload": {"productReference": "gizmo", "requiredQuantity": 12},
              "availableEquipment": ["assembler.basic"],
              "deadline": 60,
              "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"}
            }
            """;

    private static final String VALID_LARGE_FLOOR_BOTTLENECK = """
            {
              "schemaVersion": "challenge-content:v1",
              "identity": {"id": "bottleneck-relief", "version": "3"},
              "floor": {"width": 30, "height": 24},
              "startingBudget": 75000,
              "workload": {"productReference": "widget-pro", "requiredQuantity": 2000},
              "availableEquipment": ["assembler.basic", "assembler.fast", "packer.standard"],
              "deadline": 5000,
              "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"}
            }
            """;

    @Test
    void loadsMultipleDistinctChallengeDefinitionsThroughTheSameContentPath() {
        List<String> sources = List.of(
                VALID_MINIMAL,
                VALID_WITH_CATALOGUE_IDENTITY,
                VALID_CAPACITY_COST_TRADEOFF,
                VALID_TIGHT_DEADLINE,
                VALID_LARGE_FLOOR_BOTTLENECK);

        List<ChallengeDefinition> definitions = sources.stream()
                .map(ChallengeContentLoader::load)
                .map(result -> {
                    assertTrue(result.isSuccess(), () -> "expected success, got issues: " + result.issues());
                    return result.definition();
                })
                .toList();

        // Every fixture loads through the same ChallengeContentLoader.load(...) entry point yet
        // produces a distinct, content-valid ChallengeDefinition with meaningfully varied floor
        // size, budget, deadline, workload quantity, and equipment set.
        assertEquals(5, definitions.stream().map(ChallengeDefinition::identity).distinct().count());
        assertEquals(
                5,
                definitions.stream()
                        .map(d -> d.floor().width() + "x" + d.floor().height())
                        .distinct()
                        .count());
        assertEquals(5, definitions.stream().map(ChallengeDefinition::startingBudget).distinct().count());
        assertEquals(5, definitions.stream().map(ChallengeDefinition::deadline).distinct().count());
        assertEquals(
                5,
                definitions.stream()
                        .map(d -> d.workload().requiredQuantity())
                        .distinct()
                        .count());

        for (ChallengeDefinition definition : definitions) {
            ChallengeDefinitionValidationResult validation = ChallengeDefinitionValidator.validate(definition);
            assertTrue(validation.isValid(), () -> "expected content-valid definition, got: " + validation.issues());
        }
    }

    private static final String CATALOGUE_CORE = """
            {
              "schemaVersion": "equipment-catalogue:v1",
              "identity": {"id": "catalogue.core", "version": "1"},
              "offers": [
                {"itemId": "assembler.basic", "purchaseCostCredits": 500},
                {"itemId": "assembler.fast", "purchaseCostCredits": 900, "quantityLimit": 2}
              ]
            }
            """;

    @Test
    void loadsChallengeWithCatalogueWhenEveryReferenceResolves() {
        ChallengeWithCatalogueLoadResult result =
                ChallengeContentLoader.loadChallengeWithCatalogue(VALID_MINIMAL, CATALOGUE_CORE);

        assertTrue(result.isSuccess(), () -> "expected success, got issues: " + result.issues());
        assertEquals("bottleneck-101", result.definition().identity().id());
        assertEquals("catalogue.core", result.catalogue().identity().id());
    }

    @Test
    void rejectsChallengeWithCatalogueWhenAnAvailableEquipmentReferenceIsUnknown() {
        String challengeWithUnknownReference = """
                {
                  "schemaVersion": "challenge-content:v1",
                  "identity": {"id": "bottleneck-101", "version": "1"},
                  "floor": {"width": 10, "height": 8},
                  "startingBudget": 5000,
                  "workload": {"productReference": "widget", "requiredQuantity": 20},
                  "availableEquipment": ["assembler.basic", "assembler.unknown"],
                  "deadline": 400,
                  "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"}
                }
                """;

        ChallengeWithCatalogueLoadResult result = ChallengeContentLoader.loadChallengeWithCatalogue(
                challengeWithUnknownReference, CATALOGUE_CORE);

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.code().equals("catalogue.challenge.availableEquipment.unresolved")
                        && i.path().equals("availableEquipment[1]")));
    }

    @Test
    void rejectsChallengeWithCatalogueWhenCatalogueItselfHasDuplicateItemIds() {
        String catalogueWithDuplicates = """
                {
                  "schemaVersion": "equipment-catalogue:v1",
                  "identity": {"id": "catalogue.core", "version": "1"},
                  "offers": [
                    {"itemId": "assembler.basic", "purchaseCostCredits": 500},
                    {"itemId": "assembler.basic", "purchaseCostCredits": 600}
                  ]
                }
                """;

        ChallengeWithCatalogueLoadResult result =
                ChallengeContentLoader.loadChallengeWithCatalogue(VALID_MINIMAL, catalogueWithDuplicates);

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream().anyMatch(i -> i.code().equals("catalogue.offers.itemId.duplicate")));
    }

    @Test
    void rejectsChallengeWithCatalogueWhenTheDefinitionItselfIsContentInvalid() {
        String invalidChallenge = """
                {
                  "schemaVersion": "challenge-content:v1",
                  "identity": {"id": "x", "version": "1"},
                  "floor": {"width": 4, "height": 8},
                  "startingBudget": -5,
                  "workload": {"productReference": "widget", "requiredQuantity": 20},
                  "deadline": 400,
                  "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"}
                }
                """;

        ChallengeWithCatalogueLoadResult result =
                ChallengeContentLoader.loadChallengeWithCatalogue(invalidChallenge, CATALOGUE_CORE);

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream().anyMatch(i -> i.code().startsWith("definition.")));
    }

    // --- loadChallengeWithCatalogue must produce a definition that is genuinely
    // provenance-compatible with CandidateAdmissibilityPolicy's own catalogue-identity and
    // semantic-fingerprint checks, not just one that passes the loader's own narrower checks. ---

    @Test
    void loadChallengeWithCatalogueResultIsProvenanceCompatibleWithAdmissibility() {
        ChallengeWithCatalogueLoadResult result =
                ChallengeContentLoader.loadChallengeWithCatalogue(VALID_MINIMAL, CATALOGUE_CORE);

        assertTrue(result.isSuccess(), () -> "expected success, got issues: " + result.issues());

        CandidateDraftSnapshot emptyDraft = new CandidateDraftSnapshot(List.of());
        CandidateAdmissibilityResult admissibility =
                CandidateAdmissibilityPolicy.assess(result.definition(), result.catalogue(), emptyDraft);

        // An empty draft has no candidate-specific issues (no occurrences, no budget spend), so a
        // rejection here could only be a catalogue-provenance rejection -- exactly what the candidate-admissibility capability
        // independently checks and what this loader must have already ruled out.
        assertTrue(admissibility.admitted(),
                () -> "expected the loaded definition/catalogue pair to be provenance-compatible "
                        + "with CandidateAdmissibilityPolicy, got: " + admissibility.issues());
    }

    @Test
    void rejectsChallengeWithCatalogueWhenExplicitCatalogueIdentityMismatchesTheResolvedCatalogue() {
        ChallengeWithCatalogueLoadResult result =
                ChallengeContentLoader.loadChallengeWithCatalogue(VALID_WITH_CATALOGUE_IDENTITY, CATALOGUE_CORE);

        // VALID_WITH_CATALOGUE_IDENTITY declares catalogueIdentity {"id": "catalogue.core",
        // "version": "3"}; CATALOGUE_CORE's actual identity is {"catalogue.core", "1"} -- an
        // explicit, mismatched assertion that must be rejected rather than silently accepted.
        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.code().equals("content.catalogue.identity-mismatch")
                        && i.path().equals("catalogueIdentity")));
    }

    @Test
    void rejectsChallengeWithCatalogueWhenExplicitFingerprintMismatchesTheResolvedCatalogue() {
        String challengeWithMatchingIdentityButWrongFingerprint = """
                {
                  "schemaVersion": "challenge-content:v1",
                  "identity": {"id": "bottleneck-101", "version": "1"},
                  "floor": {"width": 10, "height": 8},
                  "startingBudget": 5000,
                  "workload": {"productReference": "widget", "requiredQuantity": 20},
                  "availableEquipment": [],
                  "deadline": 400,
                  "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"},
                  "catalogueIdentity": {"id": "catalogue.core", "version": "1"},
                  "catalogueSemanticFingerprint": "definitely-not-the-real-fingerprint"
                }
                """;

        ChallengeWithCatalogueLoadResult result = ChallengeContentLoader.loadChallengeWithCatalogue(
                challengeWithMatchingIdentityButWrongFingerprint, CATALOGUE_CORE);

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.code().equals("content.catalogue.semantic-fingerprint-mismatch")
                        && i.path().equals("catalogueSemanticFingerprint")));
    }

    // --- Integer-looking JSON numbers must decode exactly, not round-trip through
    // double (which silently loses precision above 2^53 and can saturate outside int/long). ---

    private static String minimalWithStartingBudget(String budgetLiteral) {
        return """
                {
                  "schemaVersion": "challenge-content:v1",
                  "identity": {"id": "x", "version": "1"},
                  "floor": {"width": 4, "height": 8},
                  "startingBudget": %s,
                  "workload": {"productReference": "widget", "requiredQuantity": 20},
                  "deadline": 400,
                  "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"}
                }
                """
                .formatted(budgetLiteral);
    }

    @Test
    void decodesIntegerLiteralsAboveDoublePrecisionExactly() {
        // 2^53 + 1 = 9007199254740993 cannot be represented exactly as a double.
        ChallengeContentLoadResult result =
                ChallengeContentLoader.load(minimalWithStartingBudget("9007199254740993"));

        assertTrue(result.isSuccess(), () -> "expected success, got issues: " + result.issues());
        assertEquals(9007199254740993L, result.definition().startingBudget());
    }

    @Test
    void decodesLongMaxValueExactlyAtTheBoundary() {
        ChallengeContentLoadResult result =
                ChallengeContentLoader.load(minimalWithStartingBudget(String.valueOf(Long.MAX_VALUE)));

        assertTrue(result.isSuccess(), () -> "expected success, got issues: " + result.issues());
        assertEquals(Long.MAX_VALUE, result.definition().startingBudget());
    }

    @Test
    void rejectsAnIntegerLiteralOnePastLongMaxValueAsOutOfRangeRatherThanSaturating() {
        java.math.BigInteger onePastLongMax =
                java.math.BigInteger.valueOf(Long.MAX_VALUE).add(java.math.BigInteger.ONE);
        ChallengeContentLoadResult result =
                ChallengeContentLoader.load(minimalWithStartingBudget(onePastLongMax.toString()));

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.code().equals("content.field.out-of-range") && i.path().equals("startingBudget")));
    }

    private static String minimalWithFloorWidth(String widthLiteral) {
        return """
                {
                  "schemaVersion": "challenge-content:v1",
                  "identity": {"id": "x", "version": "1"},
                  "floor": {"width": %s, "height": 8},
                  "startingBudget": 100,
                  "workload": {"productReference": "widget", "requiredQuantity": 20},
                  "deadline": 400,
                  "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"}
                }
                """
                .formatted(widthLiteral);
    }

    @Test
    void decodesIntegerMaxValueExactlyAtTheBoundary() {
        ChallengeContentLoadResult result =
                ChallengeContentLoader.load(minimalWithFloorWidth(String.valueOf(Integer.MAX_VALUE)));

        assertTrue(result.isSuccess(), () -> "expected success, got issues: " + result.issues());
        assertEquals(Integer.MAX_VALUE, result.definition().floor().width());
    }

    @Test
    void rejectsAnIntegerLiteralOnePastIntegerMaxValueAsOutOfRangeRatherThanSaturating() {
        long onePastIntMax = ((long) Integer.MAX_VALUE) + 1L;
        ChallengeContentLoadResult result =
                ChallengeContentLoader.load(minimalWithFloorWidth(String.valueOf(onePastIntMax)));

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.code().equals("content.field.out-of-range") && i.path().equals("floor.width")));
    }

    @Test
    void rejectsAnOfferQuantityLimitOnePastIntegerMaxValue() {
        long onePastIntMax = ((long) Integer.MAX_VALUE) + 1L;
        String source =
                """
                {
                  "schemaVersion": "equipment-catalogue:v1",
                  "identity": {"id": "catalogue.core", "version": "1"},
                  "offers": [
                    {"itemId": "assembler.basic", "purchaseCostCredits": 500, "quantityLimit": %s}
                  ]
                }
                """
                        .formatted(onePastIntMax);

        EquipmentCatalogueContentLoadResult result = ChallengeContentLoader.loadCatalogue(source);

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.code().equals("content.field.out-of-range")
                        && i.path().equals("offers[0].quantityLimit")));
    }

    // --- A blank (but present) catalogueSemanticFingerprint must fail as a structured
    // issue, not reach ChallengeDefinition's constructor and throw IllegalArgumentException. ---

    @Test
    void rejectsBlankCatalogueSemanticFingerprintAsAStructuredIssueNotAThrownException() {
        String source = """
                {
                  "schemaVersion": "challenge-content:v1",
                  "identity": {"id": "x", "version": "1"},
                  "floor": {"width": 4, "height": 8},
                  "startingBudget": 100,
                  "workload": {"productReference": "widget", "requiredQuantity": 20},
                  "deadline": 400,
                  "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"},
                  "catalogueIdentity": {"id": "catalogue.core", "version": "1"},
                  "catalogueSemanticFingerprint": " "
                }
                """;

        ChallengeContentLoadResult result = ChallengeContentLoader.load(source);

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.code().equals("content.field.blank")
                        && i.path().equals("catalogueSemanticFingerprint")));
    }

    // --- A catalogueSemanticFingerprint supplied without a catalogueIdentity is an
    // incomplete/invalid partial binding and must fail as a structured issue, not silently drop
    // the fingerprint or throw a NullPointerException from loadChallengeWithCatalogue. ---

    @Test
    void rejectsCatalogueSemanticFingerprintWithoutCatalogueIdentityAsAStructuredIssue() {
        String source = """
                {
                  "schemaVersion": "challenge-content:v1",
                  "identity": {"id": "x", "version": "1"},
                  "floor": {"width": 4, "height": 8},
                  "startingBudget": 100,
                  "workload": {"productReference": "widget", "requiredQuantity": 20},
                  "deadline": 400,
                  "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"},
                  "catalogueSemanticFingerprint": "fp-abc123"
                }
                """;

        ChallengeContentLoadResult result = ChallengeContentLoader.load(source);

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.code().equals("content.catalogueIdentity.missing-for-fingerprint")
                        && i.path().equals("catalogueIdentity")));
    }

    @Test
    void loadChallengeWithCatalogueRejectsFingerprintWithoutCatalogueIdentityInsteadOfThrowing() {
        String challengeSource = """
                {
                  "schemaVersion": "challenge-content:v1",
                  "identity": {"id": "x", "version": "1"},
                  "floor": {"width": 4, "height": 8},
                  "startingBudget": 100,
                  "workload": {"productReference": "widget", "requiredQuantity": 20},
                  "deadline": 400,
                  "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"},
                  "catalogueSemanticFingerprint": "fp-abc123"
                }
                """;
        String catalogueSource = """
                {
                  "schemaVersion": "equipment-catalogue:v1",
                  "identity": {"id": "catalogue.core", "version": "1"},
                  "offers": []
                }
                """;

        ChallengeWithCatalogueLoadResult result =
                ChallengeContentLoader.loadChallengeWithCatalogue(challengeSource, catalogueSource);

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.code().equals("content.catalogueIdentity.missing-for-fingerprint")
                        && i.path().equals("catalogueIdentity")));
    }

    // --- Decoder error-branch coverage: mistyped nested values and accessor-misuse edge cases. ---

    @Test
    void rejectsNonStringIdentityIdField() {
        String source = """
                {
                  "schemaVersion": "challenge-content:v1",
                  "identity": {"id": 5, "version": "1"},
                  "floor": {"width": 4, "height": 8},
                  "startingBudget": 100,
                  "workload": {"productReference": "widget", "requiredQuantity": 20},
                  "deadline": 400,
                  "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"}
                }
                """;

        ChallengeContentLoadResult result = ChallengeContentLoader.load(source);

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.code().equals("content.field.type") && i.path().equals("identity.id")));
    }

    @Test
    void rejectsNonObjectIdentityField() {
        String source = """
                {
                  "schemaVersion": "challenge-content:v1",
                  "identity": "not-an-object",
                  "floor": {"width": 4, "height": 8},
                  "startingBudget": 100,
                  "workload": {"productReference": "widget", "requiredQuantity": 20},
                  "deadline": 400,
                  "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"}
                }
                """;

        ChallengeContentLoadResult result = ChallengeContentLoader.load(source);

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.code().equals("content.field.type") && i.path().equals("identity")));
    }

    @Test
    void rejectsNonObjectCatalogueIdentityField() {
        String source = """
                {
                  "schemaVersion": "challenge-content:v1",
                  "identity": {"id": "x", "version": "1"},
                  "floor": {"width": 4, "height": 8},
                  "startingBudget": 100,
                  "workload": {"productReference": "widget", "requiredQuantity": 20},
                  "deadline": 400,
                  "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"},
                  "catalogueIdentity": "not-an-object"
                }
                """;

        ChallengeContentLoadResult result = ChallengeContentLoader.load(source);

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.code().equals("content.field.type") && i.path().equals("catalogueIdentity")));
    }

    @Test
    void rejectsNonStringCatalogueSemanticFingerprintField() {
        String source = """
                {
                  "schemaVersion": "challenge-content:v1",
                  "identity": {"id": "x", "version": "1"},
                  "floor": {"width": 4, "height": 8},
                  "startingBudget": 100,
                  "workload": {"productReference": "widget", "requiredQuantity": 20},
                  "deadline": 400,
                  "evaluationPolicy": {"id": "policy.contract-completion", "version": "1"},
                  "catalogueSemanticFingerprint": 12345
                }
                """;

        ChallengeContentLoadResult result = ChallengeContentLoader.load(source);

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.code().equals("content.field.type") && i.path().equals("catalogueSemanticFingerprint")));
    }

    @Test
    void rejectsNonArrayOffersField() {
        String source = """
                {
                  "schemaVersion": "equipment-catalogue:v1",
                  "identity": {"id": "catalogue.core", "version": "1"},
                  "offers": "not-an-array"
                }
                """;

        EquipmentCatalogueContentLoadResult result = ChallengeContentLoader.loadCatalogue(source);

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.code().equals("content.field.type") && i.path().equals("offers")));
    }

    @Test
    void rejectsNonObjectOfferElement() {
        String source = """
                {
                  "schemaVersion": "equipment-catalogue:v1",
                  "identity": {"id": "catalogue.core", "version": "1"},
                  "offers": ["not-an-object"]
                }
                """;

        EquipmentCatalogueContentLoadResult result = ChallengeContentLoader.loadCatalogue(source);

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.code().equals("content.field.type") && i.path().equals("offers[0]")));
    }

    @Test
    void rejectsMissingOffersField() {
        String source = """
                {
                  "schemaVersion": "equipment-catalogue:v1",
                  "identity": {"id": "catalogue.core", "version": "1"}
                }
                """;

        EquipmentCatalogueContentLoadResult result = ChallengeContentLoader.loadCatalogue(source);

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.code().equals("content.field.missing") && i.path().equals("offers")));
    }

    @Test
    void rejectsNonLongOfferPurchaseCostCredits() {
        String source = """
                {
                  "schemaVersion": "equipment-catalogue:v1",
                  "identity": {"id": "catalogue.core", "version": "1"},
                  "offers": [
                    {"itemId": "assembler.basic", "purchaseCostCredits": 4.5}
                  ]
                }
                """;

        EquipmentCatalogueContentLoadResult result = ChallengeContentLoader.loadCatalogue(source);

        assertFalse(result.isSuccess());
        assertTrue(result.issues().stream()
                .anyMatch(i -> i.code().equals("content.field.type")
                        && i.path().equals("offers[0].purchaseCostCredits")));
    }

    @Test
    void successfulLoadResultHasNoIssuesAndFailedLoadResultHasNoDefinition() {
        ChallengeContentLoadResult success = ChallengeContentLoader.load(VALID_MINIMAL);
        assertTrue(success.issues().isEmpty());
        assertTrue(success.isSuccess());

        ChallengeContentLoadResult failure = ChallengeContentLoader.load("{}");
        assertNull(failure.definition());
        assertFalse(failure.isSuccess());
        assertFalse(failure.issues().isEmpty());
    }

    @Test
    void successfulCatalogueLoadResultHasNoIssuesAndFailedResultHasNoCatalogue() {
        EquipmentCatalogueContentLoadResult success = ChallengeContentLoader.loadCatalogue(CATALOGUE_CORE);
        assertTrue(success.issues().isEmpty());
        assertTrue(success.isSuccess());

        EquipmentCatalogueContentLoadResult failure = ChallengeContentLoader.loadCatalogue("{}");
        assertNull(failure.catalogue());
        assertFalse(failure.isSuccess());
        assertFalse(failure.issues().isEmpty());
    }

    @Test
    void successfulAggregateLoadResultHasNoIssuesAndFailedResultHasNoDefinitionOrCatalogue() {
        ChallengeWithCatalogueLoadResult success =
                ChallengeContentLoader.loadChallengeWithCatalogue(VALID_MINIMAL, CATALOGUE_CORE);
        assertTrue(success.issues().isEmpty());
        assertTrue(success.isSuccess());

        ChallengeWithCatalogueLoadResult failure = ChallengeContentLoader.loadChallengeWithCatalogue("{}", "{}");
        assertNull(failure.definition());
        assertNull(failure.catalogue());
        assertFalse(failure.isSuccess());
        assertFalse(failure.issues().isEmpty());
    }
}
