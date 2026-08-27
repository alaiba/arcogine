package com.arcogine.challenge.catalogue;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Proves the {@code :challenge} module's no-factory-runtime dependency boundary at the classpath
 * level: {@code FactoryModelValidator} is not reachable from this test module, so {@link
 * EquipmentCatalogueValidator} cannot call, extend, or otherwise depend on it.
 */
class EquipmentCatalogueValidatorDependencyBoundaryTest {

    @Test
    void factoryModelValidatorIsNotOnTheClasspath() {
        assertThrows(
                ClassNotFoundException.class,
                () -> Class.forName("com.arcogine.factory.model.validation.FactoryModelValidator"));
    }
}
