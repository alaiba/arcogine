package com.arcogine.challenge.content;

import com.arcogine.challenge.ChallengeDefinition;
import com.arcogine.challenge.ChallengeIdentity;
import com.arcogine.challenge.ChallengeWorkload;
import com.arcogine.challenge.EquipmentCatalogueItemId;
import com.arcogine.challenge.EvaluationPolicyIdentity;
import com.arcogine.challenge.FactoryFloorConstraint;
import com.arcogine.challenge.catalogue.EquipmentCatalogue;
import com.arcogine.challenge.catalogue.EquipmentCatalogueIdentity;
import com.arcogine.challenge.catalogue.EquipmentCatalogueIssue;
import com.arcogine.challenge.catalogue.EquipmentCatalogueValidator;
import com.arcogine.challenge.catalogue.EquipmentOffer;
import com.arcogine.challenge.validation.ChallengeDefinitionIssue;
import com.arcogine.challenge.validation.ChallengeDefinitionValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The C5 content-loading layer: converts an untrusted external JSON representation of a challenge
 * into a structurally complete {@link ChallengeDefinition}, or into deterministic, actionable
 * {@link ChallengeContentIssue} diagnostics explaining why it could not.
 *
 * <p>This is the layer C1 explicitly deferred: {@code ChallengeDefinition} and its nested value
 * records reject structurally absent fields via ordinary constructor {@code
 * NullPointerException}s, which is unsuitable for untrusted input. {@code ChallengeContentLoader}
 * never lets a malformed or incomplete external representation reach a {@code
 * ChallengeDefinition} constructor uncaught -- every missing, mistyped, or malformed field is
 * translated into a {@link ChallengeContentIssue} before construction is attempted.
 *
 * <p>This loader decodes content only. It does not decide whether an already-constructed
 * definition's scalar content is valid (that is {@code ChallengeDefinitionValidator}'s job) and it
 * does not resolve catalogue references against a real catalogue (that is {@code
 * EquipmentCatalogueValidator}'s job). Callers that need both structural decoding and content
 * validation should call {@link #load(String)} and then run the returned definition through {@code
 * ChallengeDefinitionValidator} themselves.
 *
 * <p>Loading is pure and deterministic: it performs no I/O of its own (callers supply the source
 * text), consults no wall-clock time or random state, and produces an equal result for equal
 * input every time.
 */
public final class ChallengeContentLoader {

    /**
     * The only content schema version this loader accepts for challenge definition documents.
     *
     * <p>This is a content-profile concept, distinct from {@link ChallengeIdentity#version()} (the
     * identity/version of a particular challenge's own content) and from {@link
     * EvaluationPolicyIdentity#version()} (the version of the evaluation policy a challenge
     * selects). Bumping it is a breaking change to the JSON document shape this loader understands,
     * not a change to any individual challenge or policy.
     */
    public static final String CHALLENGE_SCHEMA_VERSION = "challenge-content:v1";

    /** The only content schema version this loader accepts for equipment catalogue documents. */
    public static final String CATALOGUE_SCHEMA_VERSION = "equipment-catalogue:v1";

    private ChallengeContentLoader() {}

    /** Decodes {@code source} (a JSON document) into a {@link ChallengeContentLoadResult}. */
    public static ChallengeContentLoadResult load(String source) {
        ChallengeDecodeOutcome outcome = decodeChallenge(source);
        if (outcome.definition() == null) {
            return ChallengeContentLoadResult.failure(outcome.issues());
        }
        return ChallengeContentLoadResult.success(outcome.definition());
    }

    /**
     * Internal decode outcome carrying not just the decoded {@link ChallengeDefinition} (or
     * failure issues) but also whether the source document explicitly declared a
     * {@code catalogueIdentity}/{@code catalogueSemanticFingerprint} binding, as opposed to
     * {@code ChallengeDefinition} synthesizing its own default. {@link #load(String)} discards
     * this distinction; {@link #loadChallengeWithCatalogue} needs it to decide whether an
     * unspecified binding should be verified against the real catalogue or bound to it.
     */
    private record ChallengeDecodeOutcome(
            ChallengeDefinition definition,
            List<ChallengeContentIssue> issues,
            boolean explicitCatalogueIdentity,
            boolean explicitCatalogueFingerprint) {}

    private static ChallengeDecodeOutcome decodeChallenge(String source) {
        if (source == null) {
            return new ChallengeDecodeOutcome(
                    null,
                    List.of(new ChallengeContentIssue("content.source.null", "$", "source must not be null")),
                    false,
                    false);
        }

        Object parsed;
        try {
            parsed = Json.parse(source);
        } catch (JsonSyntaxException e) {
            return new ChallengeDecodeOutcome(
                    null,
                    List.of(new ChallengeContentIssue("content.malformed-json", "$", e.getMessage())),
                    false,
                    false);
        }

        if (!(parsed instanceof Map<?, ?> rawRoot)) {
            return new ChallengeDecodeOutcome(
                    null,
                    List.of(new ChallengeContentIssue(
                            "content.root.not-object", "$", "root value must be a JSON object")),
                    false,
                    false);
        }

        List<ChallengeContentIssue> issues = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = (Map<String, Object>) rawRoot;

        requireSchemaVersion(root, CHALLENGE_SCHEMA_VERSION, issues);

        Map<String, Object> identityNode = requireObject(root, "identity", issues);
        String identityId = identityNode == null ? null : requireString(identityNode, "identity.id", issues);
        String identityVersion =
                identityNode == null ? null : requireString(identityNode, "identity.version", issues);

        Map<String, Object> floorNode = requireObject(root, "floor", issues);
        Integer floorWidth = floorNode == null ? null : requireInt(floorNode, "floor.width", issues);
        Integer floorHeight = floorNode == null ? null : requireInt(floorNode, "floor.height", issues);

        Long startingBudget = requireLong(root, "startingBudget", issues);

        Map<String, Object> workloadNode = requireObject(root, "workload", issues);
        String productReference =
                workloadNode == null ? null : requireString(workloadNode, "workload.productReference", issues);
        Integer requiredQuantity =
                workloadNode == null ? null : requireInt(workloadNode, "workload.requiredQuantity", issues);

        List<EquipmentCatalogueItemId> availableEquipment =
                readAvailableEquipment(root, issues);

        Long deadline = requireLong(root, "deadline", issues);

        Map<String, Object> policyNode = requireObject(root, "evaluationPolicy", issues);
        String policyId = policyNode == null ? null : requireString(policyNode, "evaluationPolicy.id", issues);
        String policyVersion =
                policyNode == null ? null : requireString(policyNode, "evaluationPolicy.version", issues);

        Map<String, Object> catalogueIdentityNode = optionalObject(root, "catalogueIdentity", issues);
        String catalogueId = catalogueIdentityNode == null
                ? null
                : requireString(catalogueIdentityNode, "catalogueIdentity.id", issues);
        String catalogueVersion = catalogueIdentityNode == null
                ? null
                : requireString(catalogueIdentityNode, "catalogueIdentity.version", issues);

        String catalogueSemanticFingerprint =
                optionalNonBlankString(root, "catalogueSemanticFingerprint", issues);

        if (policyId != null && policyVersion != null) {
            EvaluationPolicyResolver.resolve(new EvaluationPolicyIdentity(policyId, policyVersion))
                    .ifPresent(issue -> issues.add(issue));
        }

        boolean explicitCatalogueIdentity = catalogueIdentityNode != null;
        boolean explicitCatalogueFingerprint = catalogueSemanticFingerprint != null;

        if (!issues.isEmpty()) {
            return new ChallengeDecodeOutcome(
                    null, issues, explicitCatalogueIdentity, explicitCatalogueFingerprint);
        }

        ChallengeIdentity identity = new ChallengeIdentity(identityId, identityVersion);
        FactoryFloorConstraint floor = new FactoryFloorConstraint(floorWidth, floorHeight);
        ChallengeWorkload workload = new ChallengeWorkload(productReference, requiredQuantity);
        EvaluationPolicyIdentity evaluationPolicy = new EvaluationPolicyIdentity(policyId, policyVersion);

        ChallengeDefinition definition;
        if (catalogueIdentityNode != null) {
            EquipmentCatalogueIdentity catalogueIdentity =
                    new EquipmentCatalogueIdentity(catalogueId, catalogueVersion);
            definition = new ChallengeDefinition(
                    identity,
                    floor,
                    startingBudget,
                    workload,
                    availableEquipment,
                    deadline,
                    evaluationPolicy,
                    catalogueIdentity,
                    catalogueSemanticFingerprint);
        } else {
            definition = new ChallengeDefinition(
                    identity, floor, startingBudget, workload, availableEquipment, deadline, evaluationPolicy);
        }
        return new ChallengeDecodeOutcome(
                definition, List.of(), explicitCatalogueIdentity, explicitCatalogueFingerprint);
    }

    /** Decodes {@code source} (a JSON document) into an {@link EquipmentCatalogueContentLoadResult}. */
    public static EquipmentCatalogueContentLoadResult loadCatalogue(String source) {
        if (source == null) {
            return EquipmentCatalogueContentLoadResult.failure(
                    List.of(new ChallengeContentIssue("content.source.null", "$", "source must not be null")));
        }

        Object parsed;
        try {
            parsed = Json.parse(source);
        } catch (JsonSyntaxException e) {
            return EquipmentCatalogueContentLoadResult.failure(
                    List.of(new ChallengeContentIssue("content.malformed-json", "$", e.getMessage())));
        }

        if (!(parsed instanceof Map<?, ?> rawRoot)) {
            return EquipmentCatalogueContentLoadResult.failure(List.of(
                    new ChallengeContentIssue("content.root.not-object", "$", "root value must be a JSON object")));
        }

        List<ChallengeContentIssue> issues = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = (Map<String, Object>) rawRoot;

        requireSchemaVersion(root, CATALOGUE_SCHEMA_VERSION, issues);

        Map<String, Object> identityNode = requireObject(root, "identity", issues);
        String identityId = identityNode == null ? null : requireString(identityNode, "identity.id", issues);
        String identityVersion =
                identityNode == null ? null : requireString(identityNode, "identity.version", issues);

        List<EquipmentOffer> offers = readOffers(root, issues);

        if (!issues.isEmpty()) {
            return EquipmentCatalogueContentLoadResult.failure(issues);
        }

        EquipmentCatalogue catalogue = new EquipmentCatalogue(
                new EquipmentCatalogueIdentity(identityId, identityVersion), offers);

        List<EquipmentCatalogueIssue> validationIssues = EquipmentCatalogueValidator.validate(catalogue).issues();
        if (!validationIssues.isEmpty()) {
            return EquipmentCatalogueContentLoadResult.failure(translateCatalogueIssues(validationIssues));
        }

        return EquipmentCatalogueContentLoadResult.success(catalogue);
    }

    /**
     * Decodes {@code challengeSource} and {@code catalogueSource} together and confirms that
     * every catalogue reference the challenge declares actually resolves against the decoded
     * catalogue.
     *
     * <p>{@link #load(String)} and {@link #loadCatalogue(String)} each decode and content-validate
     * one document in isolation; neither calls {@code
     * EquipmentCatalogueValidator#validateChallengeResolution}, since that check requires both
     * documents at once. This method runs the full pipeline -- decode both, content-validate both
     * ({@code ChallengeDefinitionValidator} and {@code EquipmentCatalogueValidator#validate}), then
     * cross-resolve -- and aggregates every issue from every step into one deterministic, ordered
     * result rather than stopping at the first failing step.
     */
    public static ChallengeWithCatalogueLoadResult loadChallengeWithCatalogue(
            String challengeSource, String catalogueSource) {
        ChallengeDecodeOutcome challengeOutcome = decodeChallenge(challengeSource);
        EquipmentCatalogueContentLoadResult catalogueResult = loadCatalogue(catalogueSource);

        List<ChallengeContentIssue> issues = new ArrayList<>();
        issues.addAll(challengeOutcome.issues());
        issues.addAll(catalogueResult.issues());
        if (!issues.isEmpty()) {
            return ChallengeWithCatalogueLoadResult.failure(issues);
        }

        ChallengeDefinition definition = challengeOutcome.definition();
        EquipmentCatalogue catalogue = catalogueResult.catalogue();

        List<ChallengeDefinitionIssue> definitionIssues =
                ChallengeDefinitionValidator.validate(definition).issues();
        for (ChallengeDefinitionIssue issue : definitionIssues) {
            issues.add(new ChallengeContentIssue("definition." + issue.code(), issue.path(), issue.message()));
        }

        // C2's CandidateAdmissibilityPolicy independently rejects a catalogue identity mismatch
        // or an unbound/mismatched semantic fingerprint. A definition/catalogue pair loaded here
        // as "success" must not be one C2 would immediately reject on those grounds. When the
        // source explicitly declared a catalogueIdentity/catalogueSemanticFingerprint, that is an
        // assertion this method verifies against the actually-resolved catalogue; when it did
        // not, ChallengeDefinition's own default synthesized a placeholder that this method binds
        // to the real, resolved catalogue instead of leaving unbound/mismatched.
        if (challengeOutcome.explicitCatalogueIdentity()
                && !definition.catalogueIdentity().equals(catalogue.identity())) {
            issues.add(new ChallengeContentIssue(
                    "content.catalogue.identity-mismatch",
                    "catalogueIdentity",
                    "challenge catalogueIdentity does not match the loaded catalogue's identity: "
                            + catalogue.identity()));
        }
        if (challengeOutcome.explicitCatalogueFingerprint()
                && !definition.catalogueSemanticFingerprint().equals(catalogue.semanticFingerprint())) {
            issues.add(new ChallengeContentIssue(
                    "content.catalogue.semantic-fingerprint-mismatch",
                    "catalogueSemanticFingerprint",
                    "catalogue content does not match the challenge's declared semantic fingerprint"));
        }

        if (!issues.isEmpty()) {
            return ChallengeWithCatalogueLoadResult.failure(issues);
        }

        if (!challengeOutcome.explicitCatalogueIdentity() || !challengeOutcome.explicitCatalogueFingerprint()) {
            EquipmentCatalogueIdentity boundIdentity =
                    challengeOutcome.explicitCatalogueIdentity() ? definition.catalogueIdentity() : catalogue.identity();
            String boundFingerprint = challengeOutcome.explicitCatalogueFingerprint()
                    ? definition.catalogueSemanticFingerprint()
                    : catalogue.semanticFingerprint();
            definition = new ChallengeDefinition(
                    definition.identity(),
                    definition.floor(),
                    definition.startingBudget(),
                    definition.workload(),
                    definition.availableEquipment(),
                    definition.deadline(),
                    definition.evaluationPolicy(),
                    boundIdentity,
                    boundFingerprint);
        }

        List<EquipmentCatalogueIssue> resolutionIssues =
                EquipmentCatalogueValidator.validateChallengeResolution(definition, catalogue).issues();
        issues.addAll(translateCatalogueIssues(resolutionIssues));

        if (!issues.isEmpty()) {
            return ChallengeWithCatalogueLoadResult.failure(issues);
        }
        return ChallengeWithCatalogueLoadResult.success(definition, catalogue);
    }

    private static List<ChallengeContentIssue> translateCatalogueIssues(List<EquipmentCatalogueIssue> issues) {
        List<ChallengeContentIssue> translated = new ArrayList<>();
        for (EquipmentCatalogueIssue issue : issues) {
            translated.add(new ChallengeContentIssue(
                    "catalogue." + issue.code(), issue.path(), issue.message()));
        }
        return translated;
    }

    private static List<EquipmentOffer> readOffers(Map<String, Object> root, List<ChallengeContentIssue> issues) {
        if (!root.containsKey("offers")) {
            issues.add(new ChallengeContentIssue("content.field.missing", "offers", "field is required"));
            return List.of();
        }
        Object raw = root.get("offers");
        if (!(raw instanceof List<?> rawList)) {
            issues.add(new ChallengeContentIssue("content.field.type", "offers", "must be an array"));
            return List.of();
        }
        List<EquipmentOffer> offers = new ArrayList<>();
        for (int i = 0; i < rawList.size(); i++) {
            Object element = rawList.get(i);
            String path = "offers[" + i + "]";
            Map<String, Object> offerNode = asObject(element, path, issues);
            if (offerNode == null) {
                continue;
            }
            String itemId = requireString(offerNode, path + ".itemId", issues);
            Long purchaseCostCredits = requireLong(offerNode, path + ".purchaseCostCredits", issues);
            Integer quantityLimit = optionalOfferQuantityLimit(offerNode, path, issues);
            if (itemId == null || purchaseCostCredits == null) {
                continue;
            }
            offers.add(quantityLimit == null
                    ? EquipmentOffer.of(new EquipmentCatalogueItemId(itemId), purchaseCostCredits)
                    : EquipmentOffer.of(new EquipmentCatalogueItemId(itemId), purchaseCostCredits, quantityLimit));
        }
        return offers;
    }

    private static Integer optionalOfferQuantityLimit(
            Map<String, Object> offerNode, String offerPath, List<ChallengeContentIssue> issues) {
        String path = offerPath + ".quantityLimit";
        if (!offerNode.containsKey("quantityLimit") || offerNode.get("quantityLimit") == null) {
            return null;
        }
        Object value = offerNode.get("quantityLimit");
        if (value instanceof Double) {
            issues.add(new ChallengeContentIssue("content.field.type", path, "must be an integer"));
            return null;
        }
        if (!(value instanceof Number n)) {
            issues.add(new ChallengeContentIssue("content.field.type", path, "must be an integer"));
            return null;
        }
        if (value instanceof java.math.BigInteger || n.longValue() < Integer.MIN_VALUE || n.longValue() > Integer.MAX_VALUE) {
            issues.add(new ChallengeContentIssue(
                    "content.field.out-of-range", path, "must be within the 32-bit integer range"));
            return null;
        }
        return n.intValue();
    }

    private static void requireSchemaVersion(
            Map<String, Object> root, String expected, List<ChallengeContentIssue> issues) {
        String schemaVersion = requireString(root, "schemaVersion", issues);
        if (schemaVersion != null && !expected.equals(schemaVersion)) {
            issues.add(new ChallengeContentIssue(
                    "content.schemaVersion.unsupported",
                    "schemaVersion",
                    "unsupported schema version: " + schemaVersion + " (expected " + expected + ")"));
        }
    }

    private static List<EquipmentCatalogueItemId> readAvailableEquipment(
            Map<String, Object> root, List<ChallengeContentIssue> issues) {
        Object raw = root.get("availableEquipment");
        if (raw == null) {
            return List.of();
        }
        if (!(raw instanceof List<?> rawList)) {
            issues.add(new ChallengeContentIssue(
                    "content.field.type", "availableEquipment", "must be an array of strings"));
            return List.of();
        }
        List<EquipmentCatalogueItemId> result = new ArrayList<>();
        for (int i = 0; i < rawList.size(); i++) {
            Object element = rawList.get(i);
            String path = "availableEquipment[" + i + "]";
            if (!(element instanceof String value)) {
                issues.add(new ChallengeContentIssue("content.field.type", path, "must be a string"));
                continue;
            }
            result.add(new EquipmentCatalogueItemId(value));
        }
        return result;
    }

    private static Map<String, Object> requireObject(
            Map<String, Object> parent, String path, List<ChallengeContentIssue> issues) {
        if (!parent.containsKey(path)) {
            issues.add(new ChallengeContentIssue("content.field.missing", path, "field is required"));
            return null;
        }
        return asObject(parent.get(path), path, issues);
    }

    private static Map<String, Object> optionalObject(
            Map<String, Object> parent, String path, List<ChallengeContentIssue> issues) {
        if (!parent.containsKey(path) || parent.get(path) == null) {
            return null;
        }
        return asObject(parent.get(path), path, issues);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asObject(
            Object value, String path, List<ChallengeContentIssue> issues) {
        if (!(value instanceof Map<?, ?> map)) {
            issues.add(new ChallengeContentIssue("content.field.type", path, "must be a JSON object"));
            return null;
        }
        return (Map<String, Object>) map;
    }

    private static String requireString(
            Map<String, Object> parent, String path, List<ChallengeContentIssue> issues) {
        String simpleKey = simpleKey(path);
        if (!parent.containsKey(simpleKey) || parent.get(simpleKey) == null) {
            issues.add(new ChallengeContentIssue("content.field.missing", path, "field is required"));
            return null;
        }
        Object value = parent.get(simpleKey);
        if (!(value instanceof String s)) {
            issues.add(new ChallengeContentIssue("content.field.type", path, "must be a string"));
            return null;
        }
        return s;
    }

    private static String optionalString(
            Map<String, Object> parent, String path, List<ChallengeContentIssue> issues) {
        if (!parent.containsKey(path) || parent.get(path) == null) {
            return null;
        }
        Object value = parent.get(path);
        if (!(value instanceof String s)) {
            issues.add(new ChallengeContentIssue("content.field.type", path, "must be a string"));
            return null;
        }
        return s;
    }

    /**
     * Like {@link #optionalString}, but also rejects a present-but-blank value with a structured
     * issue rather than letting it reach a downstream constructor that itself throws for a blank
     * value (see {@code ChallengeDefinition}'s constructor check on {@code
     * catalogueSemanticFingerprint}).
     */
    private static String optionalNonBlankString(
            Map<String, Object> parent, String path, List<ChallengeContentIssue> issues) {
        String value = optionalString(parent, path, issues);
        if (value != null && value.isBlank()) {
            issues.add(new ChallengeContentIssue("content.field.blank", path, "must not be blank when present"));
            return null;
        }
        return value;
    }

    private static Integer requireInt(
            Map<String, Object> parent, String path, List<ChallengeContentIssue> issues) {
        Number value = requireNumber(parent, path, issues);
        if (value == null) {
            return null;
        }
        if (value instanceof Double) {
            issues.add(new ChallengeContentIssue("content.field.type", path, "must be an integer"));
            return null;
        }
        if (value instanceof java.math.BigInteger
                || value.longValue() < Integer.MIN_VALUE
                || value.longValue() > Integer.MAX_VALUE) {
            issues.add(new ChallengeContentIssue(
                    "content.field.out-of-range", path, "must be within the 32-bit integer range"));
            return null;
        }
        return value.intValue();
    }

    private static Long requireLong(
            Map<String, Object> parent, String path, List<ChallengeContentIssue> issues) {
        Number value = requireNumber(parent, path, issues);
        if (value == null) {
            return null;
        }
        if (value instanceof Double) {
            issues.add(new ChallengeContentIssue("content.field.type", path, "must be an integer"));
            return null;
        }
        if (value instanceof java.math.BigInteger) {
            issues.add(new ChallengeContentIssue(
                    "content.field.out-of-range", path, "must be within the 64-bit integer range"));
            return null;
        }
        return value.longValue();
    }

    private static Number requireNumber(
            Map<String, Object> parent, String path, List<ChallengeContentIssue> issues) {
        String simpleKey = simpleKey(path);
        if (!parent.containsKey(simpleKey) || parent.get(simpleKey) == null) {
            issues.add(new ChallengeContentIssue("content.field.missing", path, "field is required"));
            return null;
        }
        Object value = parent.get(simpleKey);
        if (!(value instanceof Number n)) {
            issues.add(new ChallengeContentIssue("content.field.type", path, "must be a number"));
            return null;
        }
        return n;
    }

    private static String simpleKey(String path) {
        int dot = path.lastIndexOf('.');
        return dot < 0 ? path : path.substring(dot + 1);
    }
}
