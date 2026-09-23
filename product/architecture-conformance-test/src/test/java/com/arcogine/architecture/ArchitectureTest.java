package com.arcogine.architecture;

import static com.tngtech.archunit.lang.conditions.ArchConditions.callMethod;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.arcogine.factory.jobs.Job;
import com.arcogine.factory.machines.Machine;
import com.arcogine.finance.ledger.JournalEntry;
import com.arcogine.finance.ledger.Ledger;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.SimTime;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces, as CI-checked rules, the module-boundary and capability guardrails documented in
 * CONTRIBUTING.md and docs/architecture/overview.md -- so a future change that reintroduces a
 * forbidden Finance-to-Factory dependency, code outside Finance posting to the ledger directly,
 * or code outside Factory driving a Job/Machine's lifecycle directly fails the build instead of
 * only failing review.
 *
 * <p>Deliberately a small, fixed rule set -- this is not a general architecture-policy framework,
 * just executable versions of specific invariants this codebase actually relies on. Scans only
 * main sources ({@link ImportOption.DoNotIncludeTests}) from this module's test classpath, which
 * is where every domain module is visible.
 *
 * <p>This class previously lived in interfaces/api's test classpath (the only module that, by
 * virtue of depending on every domain, could see all sides of these rules) alongside an
 * API-specific DTO-boundary rule. That rule proved a boundary for the HTTP adapter interfaces/api
 * provided; it was removed, not relocated, when that adapter was retired -- see
 * docs/architecture/overview.md for the durable "DTOs never re-enter domain decision paths"
 * principle it encoded.
 */
@AnalyzeClasses(packages = "com.arcogine", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule sim_finance_must_not_depend_on_sim_factory = noClasses()
            .that()
            .resideInAPackage("com.arcogine.finance..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("com.arcogine.factory..")
            .because("Finance interprets OrderCompleted events, not Factory's internal state -- it "
                    + "must never reach into Factory directly to infer what happened");

    @ArchTest
    static final ArchRule only_finance_may_post_to_the_ledger = noClasses()
            .that()
            .resideOutsideOfPackage("com.arcogine.finance..")
            .should(callMethod(Ledger.class, "post", JournalEntry.class))
            .because("Ledger.post must only be called from within sim-finance -- external callers "
                    + "get LedgerView (FinanceHandler.ledger()), which excludes it");

    @ArchTest
    static final ArchRule only_factory_may_drive_a_jobs_lifecycle = noClasses()
            .that()
            .resideOutsideOfPackage("com.arcogine.factory..")
            .should(callMethod(Job.class, "start", MachineId.class)
                    .or(callMethod(Job.class, "completeStep", SimTime.class)))
            .because("Job's production-lifecycle mutators must only be called from within "
                    + "sim-factory -- external callers get JobView (FactoryHandler.job(JobId)/"
                    + "jobsView()), which excludes them");

    @ArchTest
    static final ArchRule only_factory_may_mutate_machine_state = noClasses()
            .that()
            .resideOutsideOfPackage("com.arcogine.factory..")
            .should(callMethod(Machine.class, "startJob", JobId.class)
                    .or(callMethod(Machine.class, "completeJob", JobId.class))
                    .or(callMethod(Machine.class, "enqueueJob", JobId.class))
                    .or(callMethod(Machine.class, "dequeueJob"))
                    .or(callMethod(Machine.class, "setAvailability", boolean.class))
                    .or(callMethod(Machine.class, "setBusyTicks", long.class)))
            .because("Machine's mutators must only be called from within sim-factory -- external "
                    + "callers get MachineView (FactoryHandler.machinesView()), which excludes them");
}
