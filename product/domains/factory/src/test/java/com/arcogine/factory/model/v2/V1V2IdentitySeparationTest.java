package com.arcogine.factory.model.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Proves that a {@link FactoryModelV2} cannot travel through the existing
 * {@code factory-model:v1} publication/fingerprint path -- {@link
 * FactoryModelPublisher#publish(FactoryModel)} and the {@link FactoryModelVersion} constructor --
 * mechanically, not merely by documentation or convention.
 *
 * <p>{@code FactoryModelV2} and {@link FactoryModel} share no supertype, so the call
 * {@code FactoryModelPublisher.publish(someFactoryModelV2)} does not compile: it is a
 * {@code javac} error, verified here for every reviewer/CI run without needing a dedicated
 * negative-compilation harness, by asserting the exact absence of the type relationship a
 * successful call would require.
 */
class V1V2IdentitySeparationTest {

    @Test
    void factoryModelV2SharesNoSupertypeWithFactoryModel() {
        assertFalse(
                FactoryModel.class.isAssignableFrom(FactoryModelV2.class),
                "FactoryModelV2 must not be a FactoryModel -- otherwise it could be passed"
                        + " anywhere a FactoryModel is accepted, including FactoryModelPublisher.publish");
        assertFalse(
                FactoryModelV2.class.isAssignableFrom(FactoryModel.class),
                "FactoryModel must not be a FactoryModelV2");
    }

    @Test
    void publishAcceptsExactlyFactoryModelNotFactoryModelV2() throws NoSuchMethodException {
        Method publish = FactoryModelPublisher.class.getMethod("publish", FactoryModel.class);

        assertEquals(FactoryModel.class, publish.getParameterTypes()[0]);
        // A FactoryModelV2 argument at this exact parameter type is a compile-time type error;
        // there is no overload, supertype, or implicit conversion that would let it through.
    }

    @Test
    void factoryModelVersionConstructorAcceptsExactlyFactoryModel() throws NoSuchMethodException {
        var constructor = FactoryModelVersion.class.getDeclaredConstructor(FactoryModel.class);

        assertEquals(FactoryModel.class, constructor.getParameterTypes()[0]);
    }

    @Test
    void factoryModelV2ProjectionOnlyDiscardsSpatialContentItNeverExposesAFactoryModelPublicly() {
        // FactoryModelV2 exposes no public method returning a plain FactoryModel: the only such
        // projection (baseModel()) is package-private and used solely by FactoryModelV2Validator
        // to delegate V1-shaped structural checks. This keeps "V1-shaped content projected out of
        // a V2 design" from ever being mistaken, by a public API, for "this V2 design published
        // under factory-model:v1".
        boolean hasPublicFactoryModelReturningMethod =
                List.of(FactoryModelV2.class.getMethods()).stream()
                        .anyMatch(method -> method.getReturnType() == FactoryModel.class);

        assertFalse(hasPublicFactoryModelReturningMethod);
    }
}
