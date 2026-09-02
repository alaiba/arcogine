package com.arcogine.api.architecture;

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
 * CONTRIBUTING.md and docs/architecture/overview.md -- so a future change that reintroduces one
 * of the couplings this codebase has spent effort removing (agents depending on Factory/Economy
 * internals, code outside Finance posting to the ledger directly, code outside Factory driving a
 * Job/Machine's lifecycle directly) fails the build instead of only failing review.
 *
 * <p>Deliberately a small, fixed rule set -- this is not a general architecture-policy framework,
 * just executable versions of specific invariants this codebase actually relies on. Scans only
 * main sources ({@link ImportOption.DoNotIncludeTests}) from sim-api's test classpath, which is
 * where every domain module is already visible.
 */
@AnalyzeClasses(packages = "com.arcogine", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule sim_agents_must_not_depend_on_sim_factory = noClasses()
            .that()
            .resideInAPackage("com.arcogine.agents..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("com.arcogine.factory..")
            .because("sim-agents must stay decoupled from Factory internals -- AgentObservation is "
                    + "the only surface it needs");

    @ArchTest
    static final ArchRule sim_agents_must_not_depend_on_sim_economy = noClasses()
            .that()
            .resideInAPackage("com.arcogine.agents..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("com.arcogine.economy..")
            .because("sim-agents must stay decoupled from Economy internals -- AgentObservation is "
                    + "the only surface it needs");

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
    static final ArchRule sim_finance_must_not_depend_on_sim_economy = noClasses()
            .that()
            .resideInAPackage("com.arcogine.finance..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("com.arcogine.economy..")
            .because("Finance has no reason to know OfferPrice or demand state -- only "
                    + "OrderCompleted, which already carries the facts it needs");

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

    /**
     * Gate 4 acceptance criterion 7 (docs/planning/factory-simulation-engine-readiness.md §8.4):
     * API/UI DTOs remain outward projections and are never reused as domain decision inputs. The
     * supported direction is {@code factory runtime semantics -> RuntimeObservation/RuntimeEvent ->
     * outward adapters/DTOs}; a DTO must never flow back into a {@code FactoryRuntime} decision
     * path. This is the structural half of the G4-C acceptance list
     * ({@code apiDtosDoNotReenterDomainDecisionPaths}); the behavioural half lives in
     * {@code Gate4CHeadlessClosureAcceptanceTest}. This module's test classpath is the only place
     * that can see both sides of the boundary, since the domain modules do not depend on
     * {@code interfaces/api} at all.
     */
    @ArchTest
    static final ArchRule api_dtos_must_not_reenter_domain_decision_paths = noClasses()
            .that()
            .resideInAPackage("com.arcogine.factory..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("com.arcogine.api..", "org.springframework..", "jakarta.servlet..")
            .because("Gate 4 runtime semantics must be decided from authoritative factory state "
                    + "alone -- RuntimeObservation/RuntimeEvent project outward to API DTOs, and "
                    + "those DTOs must never re-enter a FactoryRuntime decision path");

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
