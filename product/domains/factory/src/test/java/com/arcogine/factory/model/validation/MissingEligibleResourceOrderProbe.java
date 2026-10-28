package com.arcogine.factory.model.validation;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.types.MachineId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Child-JVM entry point for {@link MissingEligibleResourceOrderTest}: validates the witness model
 * (one step eligible for {@code {1, 2, 3}}, no resources) built from every authored permutation
 * and prints one line of validator messages per permutation.
 */
final class MissingEligibleResourceOrderProbe {

    static final List<List<Long>> PERMUTATIONS = List.of(
            List.of(1L, 2L, 3L),
            List.of(1L, 3L, 2L),
            List.of(2L, 1L, 3L),
            List.of(2L, 3L, 1L),
            List.of(3L, 1L, 2L),
            List.of(3L, 2L, 1L));

    private MissingEligibleResourceOrderProbe() {}

    static FactoryModel witness(List<Long> authoredOrder) {
        LinkedHashSet<MachineId> eligible = new LinkedHashSet<>();
        authoredOrder.forEach(id -> eligible.add(new MachineId(id)));
        return new FactoryModel(
                List.of(),
                List.of(new OperationDefinition(
                        1, "Op", List.of(new OperationStepDefinition(1, "Step", eligible, 1)))),
                List.of());
    }

    static String messages(List<Long> authoredOrder) {
        return FactoryModelValidator.validate(witness(authoredOrder)).errors().stream()
                .map(ModelValidationError::message)
                .collect(Collectors.joining("|"));
    }

    public static void main(String[] args) {
        for (List<Long> permutation : PERMUTATIONS) {
            System.out.println(messages(permutation));
        }
    }
}
