package com.arcogine.factory.model;

import com.arcogine.factory.model.validation.FactoryModelValidator;
import com.arcogine.types.MachineId;
import com.arcogine.types.ModelFingerprint;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

/**
 * An immutable, published identity of a {@link FactoryModel}.
 *
 * <p>{@link #contentHash()} is a deterministic digest of the model's semantic content, derived
 * entirely from {@code model} itself: two publications of an equal model always produce the same
 * hash, and there is no way to construct a version whose hash does not identify its model -- the
 * hash is not a constructor parameter a caller could forge or mismatch, it is computed here. This
 * is an internal, in-memory identity policy sufficient to let a runtime/result attribute itself to
 * the exact model it was instantiated from -- it is not a persisted, public, or cross-process
 * compatibility guarantee. A durable model repository/lineage/versioning scheme is out of scope
 * for this milestone; see ADR-0003.
 *
 * <p>{@link FactoryModelPublisher#publish(FactoryModel)} is the intended way to obtain an
 * instance, but the D2/D4 invariant that an invalid model can never be published or instantiated
 * is enforced here, in the canonical constructor itself, rather than relied upon merely by
 * convention: constructing a {@code FactoryModelVersion} directly from an invalid model throws
 * {@link com.arcogine.factory.model.validation.FactoryModelValidationException} exactly as
 * {@code publish} does, so there is no construction path -- public API misuse included -- that
 * produces a version wrapping an unvalidated model or a forged identity.
 */
public record FactoryModelVersion(FactoryModel model) {

    public FactoryModelVersion {
        if (model == null) {
            throw new NullPointerException("model");
        }
        FactoryModelValidator.requireValid(model);
    }

    public String contentHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonicalRepresentation(model).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public ModelFingerprint fingerprint() {
        return FactoryModelFingerprintV1.fingerprint(model);
    }

    /**
     * Builds an unambiguous canonical encoding of {@code model}'s semantic content.
     *
     * <p>Every variable-length field (names, id lists) is framed with an explicit
     * length-then-colon-then-content token (netstring-style), and every collection is
     * length-prefixed with its element count. This makes the encoding injective: unlike a naive
     * concatenation with fixed delimiters such as {@code ':'}/{@code ';'}, a value that happens to
     * contain a delimiter character cannot make two semantically different models collide onto the
     * same encoded string, because a decimal digit-only length prefix can never be confused with
     * delimiter-bearing content.
     */
    private static String canonicalRepresentation(FactoryModel model) {
        StringBuilder sb = new StringBuilder();

        List<ResourceDefinition> resources = model.resources();
        frame(sb, resources.size());
        for (ResourceDefinition r : resources) {
            frame(sb, r.id().value());
            frame(sb, r.name());
            frame(sb, r.concurrency());
            frame(sb, r.capacityLiters());
            frame(sb, r.setupTime());
        }

        List<OperationDefinition> operations = model.operations();
        frame(sb, operations.size());
        for (OperationDefinition op : operations) {
            frame(sb, op.id());
            frame(sb, op.name());
            List<OperationStepDefinition> steps = op.steps();
            frame(sb, steps.size());
            for (OperationStepDefinition step : steps) {
                frame(sb, step.stepId());
                frame(sb, step.name());
                frame(sb, step.duration());
                // eligibleResources is a Set: its iteration order isn't a defined canonical
                // ordering, so two semantically-equal steps could otherwise hash differently.
                // Sort by MachineId value explicitly rather than depending on Set/toString order.
                List<Long> sortedEligible =
                        step.eligibleResources().stream().map(MachineId::value).sorted().toList();
                frame(sb, sortedEligible.size());
                for (Long resourceId : sortedEligible) {
                    frame(sb, resourceId);
                }
            }
        }

        List<ProductDefinition> products = model.products();
        frame(sb, products.size());
        for (ProductDefinition p : products) {
            frame(sb, p.id().value());
            frame(sb, p.name());
            frame(sb, p.operationId());
        }

        return sb.toString();
    }

    private static void frame(StringBuilder sb, Object value) {
        String s = String.valueOf(value);
        sb.append(s.length()).append(':').append(s);
    }
}
