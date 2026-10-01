package com.arcogine.research.experiment;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.type;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.FactoryRuntimeAssembler;
import com.arcogine.factory.process.CommandResult;
import com.arcogine.factory.process.FactoryHandler;
import com.arcogine.factory.process.FactoryRuntime;
import com.arcogine.factory.process.PendingWorkView;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Structural evidence that the research package reads only the supported runtime contract.
 *
 * <p>The module boundary proves part of this: the reusable substrate compiles against only
 * {@code :factory} and {@code :types}, so the scheduler and Governance are not visible to it at
 * all. These rules cover what the compiler cannot, because Factory's implementation types are
 * public inside {@code :factory}: nothing in the research package -- substrate, corpus or test --
 * uses the scheduler, the handler, the mutable machine/job/order/routing state the handler owns,
 * or the internal events a command result exposes; and the substrate depends on nothing but the
 * supported runtime contract and drives a runtime only through its supported session-control and
 * observation methods. That no production or consumer code depends on this package is enforced
 * where every such module is visible, by {@code architecture-conformance-test}.
 */
class ResearchPackageBoundaryTest {

    private static final String RESEARCH = "com.arcogine.research..";

    /** The reusable substrate: the module's main classes. */
    private static final JavaClasses SUBSTRATE = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.arcogine.research");

    /** Everything in the research package, including the corpus, test helpers and tests. */
    private static final JavaClasses RESEARCH_PACKAGE = new ClassFileImporter().importPackages("com.arcogine.research");

    /**
     * The supported session-control and observation surface of {@code FactoryRuntime}
     * ({@code docs/architecture/engine-semantics.md} section 1.2). Its read-only views of
     * implementation state are not part of it: supported evidence is the observation and the
     * supported-event stream.
     */
    private static final Set<String> SUPPORTED_RUNTIME_METHODS = Set.of(
            "forModel",
            "reset",
            "modelVersion",
            "runId",
            "submitWorkload",
            "setMachineAvailability",
            "advance",
            "advanceUntil",
            "observe",
            "drainSupportedEvents");

    /** The scheduler and its events, the handler, and the implementation state it owns. */
    private static final DescribedPredicate<JavaClass> RUNTIME_INTERNALS = resideInAnyPackage(
                    "com.arcogine.core..",
                    "com.arcogine.factory.machines..",
                    "com.arcogine.factory.jobs..",
                    "com.arcogine.factory.orders..",
                    "com.arcogine.factory.routing..")
            .or(type(FactoryHandler.class))
            .or(type(FactoryRuntimeAssembler.class))
            .or(type(PendingWorkView.class))
            .as("scheduler, handler or mutable Factory implementation state");

    /** Published-model and supported observation/event types, plus shared typed values. */
    private static final DescribedPredicate<JavaClass> SUPPORTED_CONTRACT = resideInAnyPackage(
                    "java..", RESEARCH, "com.arcogine.types..", "com.arcogine.factory.model..", "com.arcogine.factory.process..")
            .and(DescribedPredicate.not(RUNTIME_INTERNALS))
            .as("the supported runtime contract");

    private static final DescribedPredicate<JavaMethodCall> UNSUPPORTED_RUNTIME_CALL = DescribedPredicate.describe(
            "a FactoryRuntime method outside its supported session-control and observation surface",
            call -> call.getTargetOwner().isEquivalentTo(FactoryRuntime.class)
                    && !SUPPORTED_RUNTIME_METHODS.contains(call.getName()));

    private static final DescribedPredicate<JavaMethodCall> INTERNAL_COMMAND_EVENTS = DescribedPredicate.describe(
            "the internal events a command result lists",
            call -> call.getTargetOwner().isAssignableTo(CommandResult.class) && call.getName().equals("scheduledEvents"));

    @Test
    void theRulesApplyToTheScopesTheyName() {
        // Control: no rule below passes vacuously, and the substrate scope excludes the module's tests.
        assertTrue(SUBSTRATE.contain(ExperimentRunner.class));
        assertTrue(SUBSTRATE.contain(Oracle.class));
        assertFalse(SUBSTRATE.contain(StarterCorpus.class), "the corpus belongs to the module's tests");
        assertTrue(RESEARCH_PACKAGE.contain(StarterCorpus.class));
        assertTrue(RESEARCH_PACKAGE.contain(TamperedEvidence.class));
    }

    @Test
    void theSubstrateDependsOnlyOnTheSupportedRuntimeContract() {
        classes().that().resideInAPackage(RESEARCH)
                .should().onlyDependOnClassesThat(SUPPORTED_CONTRACT)
                .because("research evidence is the published model plus supported observations and supported events")
                .check(SUBSTRATE);
    }

    @Test
    void theSubstrateDrivesARuntimeOnlyThroughItsSupportedSurface() {
        noClasses().that().resideInAPackage(RESEARCH)
                .should().callMethodWhere(UNSUPPORTED_RUNTIME_CALL)
                .because("views of implementation state are not supported evidence")
                .check(SUBSTRATE);
    }

    @Test
    void nothingInTheResearchPackageReadsRuntimeInternals() {
        noClasses().that().resideInAPackage(RESEARCH)
                // This test names the internals it forbids, so it is the one class exempt from the rule.
                .and().doNotBelongToAnyOf(ResearchPackageBoundaryTest.class)
                .should().dependOnClassesThat(RUNTIME_INTERNALS)
                .orShould().callMethodWhere(INTERNAL_COMMAND_EVENTS)
                .because("nothing here may treat scheduler, handler or store internals as research truth")
                .check(RESEARCH_PACKAGE);
    }
}
