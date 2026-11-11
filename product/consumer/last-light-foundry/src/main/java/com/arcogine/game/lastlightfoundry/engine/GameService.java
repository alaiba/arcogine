package com.arcogine.game.lastlightfoundry.engine;

import com.arcogine.challenge.attempt.ChallengeAttempt;
import com.arcogine.challenge.comparison.AttemptComparisonResult;
import com.arcogine.challenge.comparison.ChallengeAttemptComparator;
import com.arcogine.game.lastlightfoundry.content.Mission;
import com.arcogine.game.lastlightfoundry.content.MissionLibrary;
import com.arcogine.game.lastlightfoundry.draft.Draft;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Process-wide game state: the mission library, the live runs, and this session's finished
 * attempts. Attempt history lives only for the lifetime of the server process.
 */
public final class GameService {

    private static final int MAX_LIVE_RUNS = 8;

    /** Events retained per run for cursor-based client joins. */
    public static final long EVENT_RETENTION = 5_000;

    private final MissionLibrary library;
    private final Map<String, RunDriver> runs = new LinkedHashMap<>();
    private final Map<String, List<ChallengeAttempt>> attempts = new LinkedHashMap<>();
    private final Map<String, String> attemptRuns = new LinkedHashMap<>();

    private final long retention;

    public GameService(MissionLibrary library) {
        this(library, EVENT_RETENTION);
    }

    /** Creates a service retaining at most {@code retention} events per run. */
    public GameService(MissionLibrary library, long retention) {
        this.library = library;
        this.retention = retention;
    }

    public MissionLibrary library() {
        return library;
    }

    /** Checks a draft without creating a run. */
    public DesignChecker.DesignReport check(Mission mission, Draft draft) {
        return DesignChecker.check(mission, draft);
    }

    /** Starts a run for an admitted, publishable design, or returns empty when it cannot run. */
    public synchronized Optional<RunDriver> start(Mission mission, Draft draft) {
        DesignChecker.DesignReport report = DesignChecker.check(mission, draft);
        if (!report.runnable()) {
            return Optional.empty();
        }
        return Optional.of(register(RunDriver.start(mission, draft, report, retention)));
    }

    /** Resets a run to a fresh run over the same published version. */
    public synchronized Optional<RunDriver> reset(String runId) {
        RunDriver existing = runs.get(runId);
        return existing == null ? Optional.empty() : Optional.of(register(existing.reset()));
    }

    public synchronized Optional<RunDriver> find(String runId) {
        return Optional.ofNullable(runs.get(runId));
    }

    /**
     * Records the run's finished attempt (once) and returns the ordered attempts for its mission.
     */
    public synchronized List<ChallengeAttempt> recordAttempt(String runId, RunDriver driver) {
        RunDriver.RunView view = driver.view();
        List<ChallengeAttempt> forMission =
                attempts.computeIfAbsent(driver.mission().id(), key -> new ArrayList<>());
        if (view.attempt() != null && attemptRuns.putIfAbsent(view.attempt().id().toString(), runId) == null) {
            forMission.add(view.attempt());
        }
        return List.copyOf(forMission);
    }

    /** Compares two attempts with the Challenge comparator; first is the earlier one. */
    public AttemptComparisonResult compare(ChallengeAttempt first, ChallengeAttempt second) {
        return ChallengeAttemptComparator.compare(first, second);
    }

    /** The run that produced an attempt, for display. */
    public synchronized String runOf(ChallengeAttempt attempt) {
        return attemptRuns.get(attempt.id().toString());
    }

    private RunDriver register(RunDriver driver) {
        String key = driver.view().observation().metadata().runId().value().toString();
        runs.put(key, driver);
        while (runs.size() > MAX_LIVE_RUNS) {
            String oldest = runs.keySet().iterator().next();
            runs.remove(oldest);
        }
        return driver;
    }
}
