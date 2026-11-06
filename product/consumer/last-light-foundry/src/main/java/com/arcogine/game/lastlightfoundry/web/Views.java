package com.arcogine.game.lastlightfoundry.web;

import static com.arcogine.game.lastlightfoundry.json.Json.num;
import static com.arcogine.game.lastlightfoundry.json.Json.obj;

import com.arcogine.challenge.attempt.ChallengeAttempt;
import com.arcogine.challenge.comparison.AttemptComparisonResult;
import com.arcogine.challenge.evaluation.ChallengeEvaluationResult;
import com.arcogine.factory.process.AffectedEntityRef;
import com.arcogine.factory.process.JobObservation;
import com.arcogine.factory.process.OrderObservation;
import com.arcogine.factory.process.PendingWorkObservation;
import com.arcogine.factory.process.ResourceObservation;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.game.lastlightfoundry.content.Mission;
import com.arcogine.game.lastlightfoundry.draft.Installation;
import com.arcogine.game.lastlightfoundry.engine.DesignChecker;
import com.arcogine.game.lastlightfoundry.engine.RunDriver;
import com.arcogine.types.MachineId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Projects domain results into the outward JSON contract. Every Java {@code long} (ticks,
 * identities, quantities, sequences, credits, scores) is a decimal string so JavaScript never
 * rounds it; only presentation-only doubles (KPIs) are JSON numbers. Only supported observations
 * and supported runtime events are projected: internal scheduler events never appear.
 */
public final class Views {

    private Views() {}

    public static Map<String, Object> missionSummary(Mission mission) {
        return obj(
                "id", mission.id(),
                "title", mission.title(),
                "tagline", mission.tagline());
    }

    public static Map<String, Object> mission(Mission mission) {
        List<Object> equipment = new ArrayList<>();
        mission.catalogue().offers().forEach(offer -> {
            Mission.EquipmentTemplate template = mission.equipment().get(offer.itemId().value());
            equipment.add(obj(
                    "itemId", template.itemId(),
                    "name", template.name(),
                    "glyph", template.glyph(),
                    "concurrency", template.concurrency(),
                    "steps", template.steps(),
                    "costCredits", num(offer.purchaseCostCredits()),
                    "limit", offer.quantityLimit().isPresent() ? offer.quantityLimit().getAsInt() : null));
        });
        List<Object> steps = new ArrayList<>();
        mission.steps().forEach(step -> steps.add(obj("name", step.name(), "duration", num(step.duration()))));
        List<Object> starter = new ArrayList<>();
        mission.starterDraft().forEach(installation -> starter.add(installation(installation)));
        return obj(
                "id", mission.id(),
                "title", mission.title(),
                "tagline", mission.tagline(),
                "briefing", mission.briefing(),
                "hints", mission.hints(),
                "productName", mission.productName(),
                "quantity", num(mission.challenge().workload().requiredQuantity()),
                "budgetCredits", num(mission.challenge().startingBudget()),
                "deadlineTicks", num(mission.challenge().deadline()),
                "floor", obj("width", mission.challenge().floor().width(), "height", mission.challenge().floor().height()),
                "steps", steps,
                "equipment", equipment,
                "starterDraft", starter,
                "identity", obj(
                        "challenge", mission.challenge().identity().id() + "@" + mission.challenge().identity().version(),
                        "policy", mission.challenge().evaluationPolicy().id() + "@"
                                + mission.challenge().evaluationPolicy().version(),
                        "catalogue", mission.challenge().catalogueIdentity().id() + "@"
                                + mission.challenge().catalogueIdentity().version(),
                        "catalogueFingerprint", mission.challenge().catalogueSemanticFingerprint(),
                        "contentFingerprint", mission.contentFingerprint()));
    }

    public static Map<String, Object> installation(Installation installation) {
        return obj("number", installation.number(), "itemId", installation.itemId(),
                "x", installation.x(), "y", installation.y());
    }

    public static Map<String, Object> design(DesignChecker.DesignReport report) {
        Map<String, Object> economics;
        if (report.economics().isSuccess()) {
            economics = obj(
                    "ok", true,
                    "startingBudget", num(report.economics().economics().startingBudgetCredits()),
                    "committedCost", num(report.economics().economics().committedConstructionCostCredits()),
                    "remaining", num(report.economics().economics().remainingBudgetCredits()));
        } else {
            economics = obj(
                    "ok", false,
                    "code", report.economics().failure().code(),
                    "message", report.economics().failure().message());
        }
        List<Object> admissibilityIssues = new ArrayList<>();
        report.admissibility().issues().forEach(issue -> admissibilityIssues.add(
                obj("code", issue.code(), "path", issue.path(), "message", issue.message())));
        List<Object> publicationIssues = new ArrayList<>();
        report.publicationIssues().forEach(issue -> publicationIssues.add(
                obj("field", issue.field(), "message", issue.message())));
        return obj(
                "economics", economics,
                "admissibility", obj("admitted", report.admissibility().admitted(), "issues", admissibilityIssues),
                "publication", obj(
                        "valid", report.publishable(),
                        "issues", publicationIssues,
                        "fingerprint",
                        report.publishable() ? report.publishedVersion().fingerprint().toString() : null),
                "runnable", report.runnable());
    }

    public static Map<String, Object> run(
            RunDriver driver, List<ChallengeAttempt> attempts, AttemptComparisonResult comparison) {
        RunDriver.RunView view = driver.view();
        RuntimeObservation observation = view.observation();
        Mission mission = driver.mission();
        List<Object> resources = new ArrayList<>();
        for (ResourceObservation resource : observation.resources()) {
            List<Object> active = new ArrayList<>();
            resource.activeJobIds().forEach(job -> active.add(num(job.value())));
            resources.add(obj(
                    "id", num(resource.machineId().value()),
                    "name", resource.name(),
                    "state", resource.state().name(),
                    "concurrency", resource.concurrency(),
                    "activeJobs", active,
                    "queueDepth", resource.queueDepth()));
        }
        List<Object> orders = new ArrayList<>();
        for (OrderObservation order : observation.orders()) {
            orders.add(obj(
                    "id", num(order.orderId().value()),
                    "primary", order.orderId().equals(view.primaryOrder()),
                    "productId", num(order.productId().value()),
                    "requested", num(order.requestedQuantity()),
                    "released", num(order.releasedQuantity()),
                    "completed", num(order.completedQuantity()),
                    "createdAt", num(order.createdAt().value()),
                    "completedAt", order.completedAt() == null ? null : num(order.completedAt().value()),
                    "complete", order.complete()));
        }
        List<Object> jobs = new ArrayList<>();
        for (JobObservation job : observation.jobs()) {
            jobs.add(obj(
                    "id", num(job.jobId().value()),
                    "orderId", num(job.orderId().value()),
                    "ordinal", num(job.ordinalWithinOrder()),
                    "status", job.status().name(),
                    "step", job.currentStep(),
                    "totalSteps", job.totalSteps(),
                    "machineId", job.currentMachineId() == null ? null : num(job.currentMachineId().value()),
                    "completedAt", job.completedAt() == null ? null : num(job.completedAt().value())));
        }
        List<Object> pending = new ArrayList<>();
        for (PendingWorkObservation work : observation.pendingWork()) {
            List<Object> eligible = new ArrayList<>();
            work.eligibleMachineIds().forEach(id -> eligible.add(num(id.value())));
            pending.add(obj("jobId", num(work.jobId().value()), "eligible", eligible));
        }
        long deadline = mission.challenge().deadline();
        List<Object> attemptViews = new ArrayList<>();
        for (ChallengeAttempt attempt : attempts) {
            ChallengeEvaluationResult result = attempt.evaluationResult();
            attemptViews.add(obj(
                    "attemptId", attempt.id().toString(),
                    "runId", result.provenance().runReference(),
                    "successful", result.successful(),
                    "score", result.score().toString(),
                    "cost", num(attempt.economics().committedConstructionCostCredits()),
                    "deadlineMargin", result.deadlineMarginTicks() == null ? null : num(result.deadlineMarginTicks())));
        }
        return obj(
                "runId", observation.metadata().runId().value().toString(),
                "missionId", mission.id(),
                "modelFingerprint", observation.metadata().modelFingerprint().toString(),
                "contentFingerprint", mission.contentFingerprint(),
                "status", view.status().name(),
                "detail", view.detail(),
                "time", num(observation.metadata().currentTime().value()),
                "runState", observation.metadata().runState().name(),
                "latestEventSequence", num(observation.metadata().latestEventSequence()),
                "oldestRetainedSequence", num(view.oldestRetainedSequence()),
                "deadline", num(deadline),
                "deadlinePassed", observation.metadata().currentTime().value() > deadline,
                "processedEvents", num(view.processedEvents()),
                "cost", num(driver.report().economics().economics().committedConstructionCostCredits()),
                "resources", resources,
                "orders", orders,
                "jobs", jobs,
                "pending", pending,
                "performance", obj(
                        "backlog", num(observation.performance().backlog()),
                        "completedOrders", num(observation.performance().completedOrders()),
                        "completedSalesValue", observation.performance().completedSalesValue(),
                        "averageLeadTime", observation.performance().averageLeadTime(),
                        "throughputPerTick", observation.performance().throughputPerTick()),
                "evaluation", evaluation(view.evaluation()),
                "transcript", driver.transcript(),
                "attempts", attemptViews,
                "comparison", comparison(comparison));
    }

    private static Map<String, Object> evaluation(ChallengeEvaluationResult result) {
        if (result == null) {
            return null;
        }
        List<Object> issues = new ArrayList<>();
        result.issues().forEach(issue -> issues.add(obj("code", issue.code(), "path", issue.path(), "message", issue.message())));
        return obj(
                "successful", result.successful(),
                "issues", issues,
                "deadlineMargin", result.deadlineMarginTicks() == null ? null : num(result.deadlineMarginTicks()),
                "unusedBudget", num(result.unusedBudgetCredits()),
                "score", result.score().toString(),
                "policy", result.evaluationPolicy().id() + "@" + result.evaluationPolicy().version());
    }

    private static Map<String, Object> comparison(AttemptComparisonResult result) {
        if (result == null) {
            return null;
        }
        if (!result.compatibility().comparable()) {
            List<Object> reasons = new ArrayList<>();
            result.compatibility().reasons().forEach(reason -> reasons.add(reason.code()));
            return obj("comparable", false, "reasons", reasons);
        }
        return obj(
                "comparable", true,
                "winner", result.comparison().winner().name(),
                "scoreDelta", result.comparison().scoreDelta().toString(),
                "deadlineMarginDelta",
                result.comparison().deadlineMarginDeltaTicks() == null
                        ? null
                        : result.comparison().deadlineMarginDeltaTicks().toString(),
                "unusedBudgetDelta", result.comparison().unusedBudgetDeltaCredits().toString(),
                "constructionCostDelta", result.comparison().constructionCostDeltaCredits().toString());
    }

    public static Map<String, Object> event(RuntimeEventEnvelope event) {
        List<Object> refs = new ArrayList<>();
        for (AffectedEntityRef ref : event.affectedEntityRefs()) {
            switch (ref) {
                case AffectedEntityRef.OrderRef order ->
                    refs.add(obj("kind", "order", "id", num(order.orderId().value())));
                case AffectedEntityRef.JobRef job -> refs.add(obj("kind", "job", "id", num(job.jobId().value())));
                case AffectedEntityRef.MachineRef machine ->
                    refs.add(obj("kind", "machine", "id", num(machine.machineId().value())));
            }
        }
        return obj(
                "runId", event.runId().value().toString(),
                "sequence", num(event.sequence()),
                "time", num(event.simulationTime().value()),
                "type", event.eventType().name(),
                "modelFingerprint", event.modelFingerprint().toString(),
                "refs", refs,
                "payload", payload(event.payload()));
    }

    private static Map<String, Object> payload(RuntimeEventPayload payload) {
        return switch (payload) {
            case RuntimeEventPayload.OrderAccepted accepted -> obj(
                    "orderId", num(accepted.orderId().value()),
                    "productId", num(accepted.productId().value()),
                    "quantity", num(accepted.quantity()),
                    "jobIds", accepted.jobIds().stream().map(job -> num(job.value())).toList());
            case RuntimeEventPayload.JobDispatched dispatched -> obj(
                    "jobId", num(dispatched.jobId().value()),
                    "orderId", num(dispatched.orderId().value()),
                    "machineId", num(dispatched.machineId().value()),
                    "stepIndex", dispatched.stepIndex());
            case RuntimeEventPayload.JobWaiting waiting -> obj(
                    "jobId", num(waiting.jobId().value()),
                    "orderId", num(waiting.orderId().value()),
                    "eligibleMachines",
                    waiting.eligibleMachines().stream().map(MachineId::value).map(value -> num(value)).toList());
            case RuntimeEventPayload.JobStepCompleted completed -> obj(
                    "jobId", num(completed.jobId().value()),
                    "orderId", num(completed.orderId().value()),
                    "machineId", num(completed.machineId().value()),
                    "stepIndex", completed.stepIndex(),
                    "jobComplete", completed.jobComplete());
            case RuntimeEventPayload.OrderCompleted completed -> obj(
                    "orderId", num(completed.orderId().value()),
                    "jobId", num(completed.jobId().value()),
                    "quantity", num(completed.quantity()));
            case RuntimeEventPayload.MachineAvailabilityChanged changed -> obj(
                    "machineId", num(changed.machineId().value()), "online", changed.online());
        };
    }
}
