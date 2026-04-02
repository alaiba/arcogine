import { useCallback, useMemo, useState } from 'react';
import { SCENARIOS, type ScenarioKey } from '../../data/scenarios';
import { useSimulationStore } from '../../stores/simulation';

export function Toolbar() {
  const snapshot = useSimulationStore((s) => s.snapshot);
  const loading = useSimulationStore((s) => s.loading);
  const error = useSimulationStore((s) => s.error);
  const loadScenario = useSimulationStore((s) => s.loadScenario);
  const runSim = useSimulationStore((s) => s.runSim);
  const pauseSim = useSimulationStore((s) => s.pauseSim);
  const stepSim = useSimulationStore((s) => s.stepSim);
  const resetSim = useSimulationStore((s) => s.resetSim);
  const toggleAgent = useSimulationStore((s) => s.toggleAgent);

  const [scenarioKey, setScenarioKey] = useState<ScenarioKey>('basic');

  const scenarioLoaded = snapshot?.scenario_loaded ?? false;
  const completed = snapshot?.run_state === 'Completed';

  const controlsDisabled = !scenarioLoaded || loading;
  const runStepDisabled = controlsDisabled || completed;

  const loadSelected = useCallback(() => {
    void loadScenario(SCENARIOS[scenarioKey].toml);
  }, [loadScenario, scenarioKey]);

  const btn =
    'rounded-md border border-zinc-600 bg-zinc-800 px-3 py-1.5 text-sm font-medium text-zinc-100 transition hover:bg-zinc-700 disabled:cursor-not-allowed disabled:opacity-40';

  const scenarioOptions = useMemo(
    () =>
      (Object.keys(SCENARIOS) as ScenarioKey[]).map((key) => (
        <option key={key} value={key}>
          {SCENARIOS[key].label}
        </option>
      )),
    [],
  );

  return (
    <header className="flex flex-wrap items-center gap-3 border-b border-zinc-700 bg-zinc-900 px-4 py-2">
      <div className="flex items-center gap-2">
        <label htmlFor="scenario-select" className="text-xs font-medium uppercase tracking-wide text-zinc-400">
          Scenario
        </label>
        <select
          id="scenario-select"
          value={scenarioKey}
          onChange={(e) => setScenarioKey(e.target.value as ScenarioKey)}
          disabled={loading}
          className="rounded-md border border-zinc-600 bg-zinc-800 px-2 py-1.5 text-sm text-zinc-100 disabled:opacity-40"
        >
          {scenarioOptions}
        </select>
        <button type="button" className={btn} onClick={loadSelected} disabled={loading}>
          Load
        </button>
      </div>

      <div className="h-6 w-px bg-zinc-700" aria-hidden />

      <div className="flex flex-wrap items-center gap-2">
        <button type="button" className={btn} onClick={() => void runSim()} disabled={runStepDisabled}>
          Run
        </button>
        <button type="button" className={btn} onClick={() => void pauseSim()} disabled={controlsDisabled}>
          Pause
        </button>
        <button type="button" className={btn} onClick={() => void stepSim()} disabled={runStepDisabled}>
          Step
        </button>
        <button type="button" className={btn} onClick={() => void resetSim()} disabled={controlsDisabled}>
          Reset
        </button>
      </div>

      <div className="h-6 w-px bg-zinc-700" aria-hidden />

      <button
        type="button"
        className={`${btn} ${snapshot?.agent_enabled ? 'border-emerald-700 bg-emerald-900/40' : ''}`}
        onClick={() => void toggleAgent(!snapshot?.agent_enabled)}
        disabled={!snapshot || loading}
      >
        Agent {snapshot?.agent_enabled ? 'ON' : 'OFF'}
      </button>

      {loading && <span className="text-sm text-zinc-400">Loading…</span>}
      {error && <span className="text-sm text-red-400">{error}</span>}
    </header>
  );
}
