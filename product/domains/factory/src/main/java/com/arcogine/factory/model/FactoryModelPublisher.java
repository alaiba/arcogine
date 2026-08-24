package com.arcogine.factory.model;

import com.arcogine.factory.model.validation.FactoryModelValidator;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Publishes a validated {@link FactoryModel} as an immutable {@link FactoryModelVersion}.
 *
 * <p>Publication validates the model first (see {@link FactoryModelValidator}) and never returns
 * a version for a structurally invalid model, so a runtime is never constructed from a partially
 * valid design.
 */
public final class FactoryModelPublisher {

    private FactoryModelPublisher() {}

    public static FactoryModelVersion publish(FactoryModel model) {
        FactoryModelValidator.requireValid(model);
        return new FactoryModelVersion(model, contentHash(model));
    }

    private static String contentHash(FactoryModel model) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonicalRepresentation(model).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String canonicalRepresentation(FactoryModel model) {
        StringBuilder sb = new StringBuilder();
        sb.append("resources=[");
        for (ResourceDefinition r : model.resources()) {
            sb.append(r.id().value())
                    .append(':')
                    .append(r.name())
                    .append(':')
                    .append(r.concurrency())
                    .append(':')
                    .append(r.capacityLiters())
                    .append(':')
                    .append(r.setupTime())
                    .append(';');
        }
        sb.append("]operations=[");
        for (OperationDefinition op : model.operations()) {
            sb.append(op.id()).append(':').append(op.name()).append(":[");
            for (OperationStepDefinition step : op.steps()) {
                sb.append(step.stepId())
                        .append(':')
                        .append(step.name())
                        .append(':')
                        .append(step.eligibleResources())
                        .append(':')
                        .append(step.duration())
                        .append(';');
            }
            sb.append("];");
        }
        sb.append("]products=[");
        for (ProductDefinition p : model.products()) {
            sb.append(p.id().value())
                    .append(':')
                    .append(p.name())
                    .append(':')
                    .append(p.operationId())
                    .append(';');
        }
        sb.append(']');
        return sb.toString();
    }
}
