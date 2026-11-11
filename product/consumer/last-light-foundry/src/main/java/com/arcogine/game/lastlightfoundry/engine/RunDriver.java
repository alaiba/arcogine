package com.arcogine.game.lastlightfoundry.engine;

import com.arcogine.challenge.attempt.ChallengeAttempt;
import com.arcogine.challenge.evaluation.AuthoritativeOutcomeFacts;
import com.arcogine.challenge.evaluation.ChallengeEvaluationInput;
import com.arcogine.challenge.evaluation.ChallengeEvaluationResult;
import com.arcogine.challenge.evaluation.EvaluationProvenance;
import com.arcogine.challenge.evaluation.ReferenceChallengeEvaluationPolicy;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.process.CommandResult;
import com.arcogine.factory.process.FactoryRuntime;
import com.arcogine.factory.process.OrderObservation;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.factory.process.RuntimeRunState;
import com.arcogine.game.lastlightfoundry.content.Mission;
import com.arcogine.game.lastlightfoundry.draft.Draft;
import com.arcogine.game.lastlightfoundry.draft.DraftProjector;
import com.arcogine.types.OrderId;
import com.arcogine.types.SimTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The serialized session driver for one run.
 *
 * <p>This is the only code that mutates a {@link FactoryRuntime} and the only caller of its
 * supported-event drain. Every public method is {@code synchronized}, so concurrent HTTP requests
 * are applied one at a time, and each command drains the runtime exactly once, appending to a
 * bounded retained log that readers page through by cursor. Simulation time moves only when the
 * engine processes an event; playback pacing in the browser decides how often commands arrive,
 * never what they mean.
 */
public final class RunDriver {

    /** How a run stands from the player's point of view. */
    public enum Status {
        /** Work is pending in the engine. */
        ACTIVE,
        /** The primary order completed (success or failure is the Challenge evaluation's call). */
        COMPLETED,
        /** The engine has no pending work but the primary order is incomplete. */
        BLOCKED,
        /** The workload submission was rejected without any mutation. */
        REJECTED,
        /** The engine faulted after a mutation. */
        FAULTED
    }

    /** The cursor asked for events older than the retained window. */
    public static final class GapException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final long oldestRetained;

        GapException(long oldestRetained) {
            super("events before sequence " + oldestRetained + " are no longer retained; resnapshot");
            this.oldestRetained = oldestRetained;
        }

        public long oldestRetained() {
            return oldestRetained;
        }
    }

    /** The cursor is ahead of anything this run has emitted. */
    public static final class CursorException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        CursorException(String message) {
            super(message);
        }
    }

    /** An atomic, immutable view of the run. */
    public record RunView(
            RuntimeObservation observation,
            Status status,
            String detail,
            OrderId primaryOrder,
            ChallengeEvaluationResult evaluation,
            ChallengeAttempt attempt,
            long processedEvents,
            long oldestRetainedSequence) {}

    /** A page of retained events. */
    public record EventPage(List<RuntimeEventEnvelope> events, long latestSequence, boolean more) {}

    private final Mission mission;
    private final Draft draft;
    private final DesignChecker.DesignReport report;
    private final FactoryModelVersion version;
    private final long retention;
    private final List<RuntimeEventEnvelope> log = new ArrayList<>();
    private final List<String> transcript = new ArrayList<>();
    private final FactoryRuntime runtime;
    private long firstRetained = 1;
    private long processedEvents;
    private Status status = Status.ACTIVE;
    private String detail = "";
    private OrderId primaryOrder;
    private ChallengeEvaluationResult evaluation;
    private ChallengeAttempt attempt;

    private RunDriver(
            Mission mission, Draft draft, DesignChecker.DesignReport report, FactoryRuntime runtime, long retention) {
        this.mission = mission;
        this.draft = draft;
        this.report = report;
        this.version = report.publishedVersion();
        this.runtime = runtime;
        this.retention = retention;
    }

    /**
     * Starts a run: instantiates a fresh runtime from the published version and submits the
     * mission's single primary workload with its real quantity.
     */
    public static RunDriver start(Mission mission, Draft draft, DesignChecker.DesignReport report, long retention) {
        if (!report.runnable()) {
            throw new IllegalArgumentException("only an admitted, published design can be run");
        }
        RunDriver driver = new RunDriver(
                mission, draft, report, FactoryRuntime.forModel(report.publishedVersion()), retention);
        driver.submitPrimaryWorkload();
        return driver;
    }

    /** A fresh run over the same published version: new run identity, this run untouched. */
    public synchronized RunDriver reset() {
        RunDriver next = new RunDriver(mission, draft, report, runtime.reset(), retention);
        next.submitPrimaryWorkload();
        return next;
    }

    private synchronized void submitPrimaryWorkload() {
        long quantity = mission.challenge().workload().requiredQuantity();
        transcript.add("submit " + DraftProjector.productId() + " x" + quantity);
        CommandResult<OrderId> result =
                runtime.submitWorkload(DraftProjector.productId(), quantity, mission.unitPrice());
        switch (result) {
            case CommandResult.Accepted<OrderId> accepted -> primaryOrder = accepted.value();
            case CommandResult.Rejected<OrderId> rejected -> {
                status = Status.REJECTED;
                detail = rejected.code() + ": " + rejected.diagnostic();
            }
            case CommandResult.Faulted<OrderId> faulted -> {
                primaryOrder = faulted.value();
                status = Status.FAULTED;
                detail = faulted.code() + ": " + faulted.diagnostic();
            }
        }
        drain();
        settle();
    }

    /** Processes up to {@code count} scheduler events one at a time. */
    public synchronized RunView step(int count) {
        if (status == Status.ACTIVE) {
            transcript.add("step " + count);
            try {
                for (int i = 0; i < count && status == Status.ACTIVE; i++) {
                    if (runtime.advance().isEmpty()) {
                        break;
                    }
                    processedEvents++;
                    drain();
                    settle();
                }
            } catch (RuntimeException fault) {
                fail(fault);
            }
        }
        return view();
    }

    /**
     * Bounded advancement: processes events whose time does not exceed {@code untilTick}, at most
     * {@code maxEvents}. The tick is a bound, never a request to move the clock.
     */
    public synchronized RunView advance(long maxEvents, long untilTick) {
        if (status == Status.ACTIVE) {
            transcript.add("advance max=" + maxEvents + " until=" + untilTick);
            try {
                processedEvents += runtime.advanceUntil(SimTime.of(untilTick), maxEvents).size();
            } catch (RuntimeException fault) {
                fail(fault);
            }
            drain();
            settle();
        }
        return view();
    }

    /** The current atomic view. */
    public synchronized RunView view() {
        return new RunView(
                runtime.observe(), status, detail, primaryOrder, evaluation, attempt, processedEvents, firstRetained);
    }

    /** Retained events with sequence greater than {@code after}, at most {@code limit}. */
    public synchronized EventPage eventsAfter(long after, int limit) {
        long latest = runtime.observe().metadata().latestEventSequence();
        if (after < 0 || after > latest) {
            throw new CursorException("cursor " + after + " is outside 0.." + latest);
        }
        if (after + 1 < firstRetained) {
            throw new GapException(firstRetained);
        }
        List<RuntimeEventEnvelope> page = new ArrayList<>();
        for (RuntimeEventEnvelope event : log) {
            if (event.sequence() > after && page.size() < limit) {
                page.add(event);
            }
        }
        long last = page.isEmpty() ? after : page.get(page.size() - 1).sequence();
        return new EventPage(List.copyOf(page), latest, last < latest);
    }

    /** The ordered command transcript (what was asked of the engine, in order). */
    public synchronized List<String> transcript() {
        return List.copyOf(transcript);
    }

    public FactoryModelVersion version() {
        return version;
    }

    public Mission mission() {
        return mission;
    }

    public Draft draft() {
        return draft;
    }

    public DesignChecker.DesignReport report() {
        return report;
    }

    /** The single place the runtime's supported events are drained (the only capture owner). */
    private void drain() {
        log.addAll(runtime.drainSupportedEvents());
        while (log.size() > retention) {
            log.remove(0);
        }
        firstRetained = log.isEmpty() ? runtime.observe().metadata().latestEventSequence() + 1 : log.get(0).sequence();
    }

    private void fail(RuntimeException fault) {
        status = Status.FAULTED;
        detail = fault.getClass().getSimpleName() + ": " + fault.getMessage();
        drain();
    }

    /** Derives completion from the primary order's supported aggregate, never from quiescence. */
    private void settle() {
        if (status != Status.ACTIVE || primaryOrder == null) {
            return;
        }
        RuntimeObservation observation = runtime.observe();
        OrderObservation order = observation.orders().stream()
                .filter(candidate -> candidate.orderId().equals(primaryOrder))
                .findFirst()
                .orElseThrow();
        if (order.complete()) {
            status = Status.COMPLETED;
            long cost = report.economics().economics().committedConstructionCostCredits();
            evaluation = ReferenceChallengeEvaluationPolicy.evaluate(new ChallengeEvaluationInput(
                    mission.challenge(),
                    new EvaluationProvenance(
                            version.fingerprint().toString(),
                            observation.metadata().runId().value().toString()),
                    new AuthoritativeOutcomeFacts(true, order.completedAt().value()),
                    cost));
            attempt = ChallengeAttempt.record(
                    DraftProjector.toCandidateSnapshot(draft), report.economics().economics(), evaluation);
        } else if (observation.metadata().runState() == RuntimeRunState.QUIESCENT) {
            status = Status.BLOCKED;
            detail = "The engine has no pending work, but the primary order is not complete.";
        }
    }
}
