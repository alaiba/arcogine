package com.arcogine.architecture;

import static com.tngtech.archunit.core.domain.JavaCall.Predicates.target;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.name;
import static com.tngtech.archunit.lang.conditions.ArchConditions.callMethod;
import static com.tngtech.archunit.lang.conditions.ArchConditions.callMethodWhere;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.arcogine.factory.jobs.Job;
import com.arcogine.factory.machines.Machine;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.SimTime;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.StreamSupport;

/**
 * Enforces, as CI-checked rules, the module-boundary and capability guardrails documented in
 * CONTRIBUTING.md and docs/architecture/overview.md -- so a future change that reintroduces
 * code outside Factory driving a Job/Machine's lifecycle directly, production code depending on
 * the research experiment substrate, or simulation code reading the wall clock or running
 * concurrently fails the build instead of only failing review.
 *
 * <p>Deliberately a small, fixed rule set -- this is not a general architecture-policy framework,
 * just executable versions of specific invariants this codebase actually relies on. Scans only
 * main sources ({@link ImportOption.DoNotIncludeTests}) from this module's test classpath, which
 * is where every production and consumer module is visible.
 */
@AnalyzeClasses(packages = "com.arcogine", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    /** Packages whose code decides simulation outcomes: the scheduler core and the Factory domain. */
    private static final String[] SIMULATION_PACKAGES = {"com.arcogine.core..", "com.arcogine.factory.."};

    @ArchTest
    static final ArchRule governance_and_factory_do_not_depend_on_storage = noClasses()
            .that()
            .resideInAnyPackage("com.arcogine.governance..", "com.arcogine.factory..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("com.arcogine.storage..")
            .because("semantic owners consume the Governance port, not a storage mechanism");

    @ArchTest
    static final ArchRule storage_does_not_depend_on_factory = noClasses()
            .that()
            .resideInAPackage("com.arcogine.storage..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("com.arcogine.factory..")
            .because("Factory supplies artifacts through the domain-neutral verifier");

    @ArchTest
    static final ArchRule production_does_not_depend_on_research_infrastructure = noClasses()
            .that()
            .resideOutsideOfPackage("com.arcogine.research..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("com.arcogine.research..")
            .because("the research experiment substrate is non-shipped research infrastructure: "
                    + "production and consumer code never depends on it");

    @ArchTest
    static final ArchRule only_factory_may_drive_a_jobs_lifecycle = noClasses()
            .that()
            .resideOutsideOfPackage("com.arcogine.factory..")
            .should(callMethod(Job.class, "start", MachineId.class)
                    .or(callMethod(Job.class, "completeStep", SimTime.class)))
            .because("Job's production-lifecycle mutators must only be called from within "
                    + "Factory -- external callers get JobView (FactoryHandler.job(JobId)/"
                    + "jobsView()), which excludes them");

    @ArchTest
    static final ArchRule only_factory_may_mutate_machine_state = noClasses()
            .that()
            .resideOutsideOfPackage("com.arcogine.factory..")
            .should(callMethod(Machine.class, "startJob", JobId.class)
                    .or(callMethod(Machine.class, "completeJob", JobId.class))
                    .or(callMethod(Machine.class, "enqueueJob", JobId.class))
                    .or(callMethod(Machine.class, "dequeueJob"))
                    .or(callMethod(Machine.class, "setAvailability", boolean.class)))
            .because("Machine's mutators must only be called from within Factory -- external "
                    + "callers get MachineView (FactoryHandler.machinesView()), which excludes them");

    @ArchTest
    static final ArchRule simulation_never_reads_wall_clock_time = noClasses()
            .that()
            .resideInAnyPackage(SIMULATION_PACKAGES)
            .should()
            .dependOnClassesThat()
            .resideInAPackage("java.time..")
            .orShould().dependOnClassesThat().areAssignableTo(Date.class)
            .orShould().dependOnClassesThat().areAssignableTo(Calendar.class)
            .orShould(callMethod(System.class, "currentTimeMillis"))
            .orShould(callMethod(System.class, "nanoTime"))
            .because("simulated time is the only time a simulation outcome may depend on -- wall-clock "
                    + "or presentation pacing must never reach a decision (Determinism Contract); "
                    + "recording timestamps belong to Governance and Storage, outside these packages");

    @ArchTest
    // StreamSupport's factories accept a parallel flag. Exclude the entry point conservatively;
    // sequential simulation traversal uses collection.stream() instead.
    static final ArchRule simulation_never_runs_concurrently = noClasses()
            .that()
            .resideInAnyPackage(SIMULATION_PACKAGES)
            .should()
            .dependOnClassesThat()
            .resideInAPackage("java.util.concurrent..")
            .orShould()
            .dependOnClassesThat()
            .belongToAnyOf(Thread.class, ThreadGroup.class, Timer.class, TimerTask.class, StreamSupport.class)
            .orShould(callMethodWhere(target(name("parallel")).or(target(name("parallelStream")))))
            .because("a runtime is a single-owner session driven by one caller at a time; simulation "
                    + "code creates no threads, executors or parallel streams that could mutate its "
                    + "state concurrently (runtime contract)");
}
