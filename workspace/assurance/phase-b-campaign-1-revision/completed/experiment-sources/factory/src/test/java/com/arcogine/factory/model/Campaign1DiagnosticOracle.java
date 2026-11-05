package com.arcogine.factory.model;

import java.util.List;

/** Literal observed vocabulary applied to independently derived predicate violations.
 * Ordering outside signed eligible IDs is a baseline observation, not a new contract.
 */
final class Campaign1DiagnosticOracle {
    private Campaign1DiagnosticOracle() {}

    static List<String> expected(FactoryModel model) {
        return Campaign1Oracle.violations(Campaign1Oracle.neutral(model)).stream()
                .map(Campaign1DiagnosticOracle::render).toList();
    }

    static String render(String violation) {
        String[] parts = violation.split(" ");
        String kind = parts[0];
        String entity = parts[1];
        String id = parts[2];
        String field = switch (entity) {
            case "resource" -> "resources[Machine(" + id + ")]";
            case "operation" -> "operations[" + id + "]";
            case "product" -> "products[Product(" + id + ")]";
            case "step" -> "operations[" + id.split("/")[0] + "].steps[" + id.split("/")[1] + "]";
            default -> throw new AssertionError(violation);
        };
        return switch (kind) {
            case "NAME" -> field + ".name: must contain only valid Unicode scalar values";
            case "CONCURRENCY" -> field + ".concurrency: must be > 0";
            case "DURATION" -> field + ".duration: must be > 0";
            case "EMPTY_STEPS" -> field + ".steps: must not be empty";
            case "EMPTY_ELIGIBLE" -> field + ".eligibleResources: must reference at least one resource";
            case "DANGLING_RESOURCE" -> field + ".eligibleResources: references nonexistent resource id: Machine(" + parts[4] + ")";
            case "DANGLING_OPERATION" -> field + ".operationId: references nonexistent operation id: " + parts[4];
            case "DUPLICATE" -> entity + "s: duplicate " + entity + " id: "
                    + (entity.equals("resource") ? "Machine(" + id + ")"
                    : entity.equals("product") ? "Product(" + id + ")" : id);
            default -> throw new AssertionError(violation);
        };
    }
}
