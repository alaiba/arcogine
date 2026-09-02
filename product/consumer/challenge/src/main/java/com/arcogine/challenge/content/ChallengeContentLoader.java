package com.arcogine.challenge.content;

import com.arcogine.challenge.ChallengeDefinition;
import com.arcogine.challenge.ChallengeIdentity;
import com.arcogine.challenge.ChallengeWorkload;
import com.arcogine.challenge.EquipmentCatalogueItemId;
import com.arcogine.challenge.EvaluationPolicyIdentity;
import com.arcogine.challenge.FactoryFloorConstraint;
import com.arcogine.challenge.catalogue.EquipmentCatalogueIdentity;
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

    private ChallengeContentLoader() {}

    /** Decodes {@code source} (a JSON document) into a {@link ChallengeContentLoadResult}. */
    public static ChallengeContentLoadResult load(String source) {
        if (source == null) {
            return ChallengeContentLoadResult.failure(
                    List.of(new ChallengeContentIssue("content.source.null", "$", "source must not be null")));
        }

        Object parsed;
        try {
            parsed = Json.parse(source);
        } catch (JsonSyntaxException e) {
            return ChallengeContentLoadResult.failure(
                    List.of(new ChallengeContentIssue("content.malformed-json", "$", e.getMessage())));
        }

        if (!(parsed instanceof Map<?, ?> rawRoot)) {
            return ChallengeContentLoadResult.failure(List.of(
                    new ChallengeContentIssue("content.root.not-object", "$", "root value must be a JSON object")));
        }

        List<ChallengeContentIssue> issues = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = (Map<String, Object>) rawRoot;

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
                optionalString(root, "catalogueSemanticFingerprint", issues);

        if (!issues.isEmpty()) {
            return ChallengeContentLoadResult.failure(issues);
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
        return ChallengeContentLoadResult.success(definition);
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

    private static Integer requireInt(
            Map<String, Object> parent, String path, List<ChallengeContentIssue> issues) {
        Double value = requireNumber(parent, path, issues);
        if (value == null) {
            return null;
        }
        if (value != Math.floor(value) || value.isInfinite()) {
            issues.add(new ChallengeContentIssue("content.field.type", path, "must be an integer"));
            return null;
        }
        return value.intValue();
    }

    private static Long requireLong(
            Map<String, Object> parent, String path, List<ChallengeContentIssue> issues) {
        Double value = requireNumber(parent, path, issues);
        if (value == null) {
            return null;
        }
        if (value != Math.floor(value) || value.isInfinite()) {
            issues.add(new ChallengeContentIssue("content.field.type", path, "must be an integer"));
            return null;
        }
        return value.longValue();
    }

    private static Double requireNumber(
            Map<String, Object> parent, String path, List<ChallengeContentIssue> issues) {
        String simpleKey = simpleKey(path);
        if (!parent.containsKey(simpleKey) || parent.get(simpleKey) == null) {
            issues.add(new ChallengeContentIssue("content.field.missing", path, "field is required"));
            return null;
        }
        Object value = parent.get(simpleKey);
        if (!(value instanceof Double d)) {
            issues.add(new ChallengeContentIssue("content.field.type", path, "must be a number"));
            return null;
        }
        return d;
    }

    private static String simpleKey(String path) {
        int dot = path.lastIndexOf('.');
        return dot < 0 ? path : path.substring(dot + 1);
    }
}
