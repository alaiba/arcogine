package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.types.EngineSemanticsVersion;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Acceptance evidence for the fixed Engine semantics identity of a runtime. */
class EngineSemanticsIdentityAcceptanceTest {

    private static FactoryModelVersion model() {
        return FactoryModelPublisher.publish(new FactoryModel(
                List.of(new ConfiguredResource(new MachineId(1), "Mill", 1, null, 0)),
                List.of(new OperationDefinition(
                        1,
                        "Widget Route",
                        List.of(new OperationStepDefinition(1, "Milling", Set.of(new MachineId(1)), 1)))),
                List.of(new ProductDefinition(new ProductId(1), "Widget", 1))));
    }

    @Test
    void freshRuntimesExposeOneStableSupportedSemanticsVersionIndependentOfRunIdentity() {
        FactoryModelVersion version = model();
        FactoryRuntime first = FactoryRuntime.forModel(version);
        FactoryRuntime second = FactoryRuntime.forModel(version);

        assertEquals(EngineSemanticsVersion.CURRENT, first.semanticsVersion());
        assertEquals("engine-semantics:v1", first.semanticsVersion().toString());
        assertEquals(first.semanticsVersion(), second.semanticsVersion());
        assertNotEquals(first.runId(), second.runId());
    }

    @Test
    void resetCreatesAFreshRunWithTheSameFixedSemanticsVersion() {
        FactoryRuntime original = FactoryRuntime.forModel(model());
        FactoryRuntime reset = original.reset();

        assertNotEquals(original.runId(), reset.runId());
        assertEquals(original.semanticsVersion(), reset.semanticsVersion());
        assertTrue(EngineSemanticsVersion.isSupported(reset.semanticsVersion()));
    }

    @Test
    void runtimeDoesNotExposeCallerSelectedOrMutableSemanticsVersion() {
        assertTrue(Arrays.stream(FactoryRuntime.class.getMethods())
                .filter(method -> method.getName().equals("forModel"))
                .noneMatch(method -> Arrays.asList(method.getParameterTypes()).contains(EngineSemanticsVersion.class)));
        assertTrue(Arrays.stream(FactoryRuntime.class.getMethods())
                .noneMatch(method -> method.getName().equals("setSemanticsVersion")));
    }
}
