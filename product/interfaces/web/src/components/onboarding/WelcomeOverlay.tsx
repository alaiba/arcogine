import { useCallback, useState } from 'react';
import { SCENARIO_BASIC, SCENARIOS, type ScenarioKey } from '../../data/scenarios';
import { useSimulationStore } from '../../stores/simulation';

type WelcomeOverlayProps = {
  onDismiss: () => void;
};

const CARD_COPY: Record<
  ScenarioKey,
  { title: string; description: string }
> = {
  basic: {
    title: 'Basic',
    description:
      'Balanced demand and capacity with a three-stage routing. Ideal for learning controls and KPIs.',
  },
  overload: {
    title: 'Overload',
    description:
      'High demand and low starting price stress the line—watch backlog and lead time spike.',
  },
  capacity_expansion: {
    title: 'Capacity Expansion',
    description:
      'Extra milling capacity meets heavy demand. Compare throughput and revenue vs overload.',
  },
};

export function WelcomeOverlay({ onDismiss }: WelcomeOverlayProps) {
  const snapshot = useSimulationStore((s) => s.snapshot);
  const loadScenario = useSimulationStore((s) => s.loadScenario);
  const runSim = useSimulationStore((s) => s.runSim);
  const loading = useSimulationStore((s) => s.loading);
  const [dismissed, setDismissed] = useState(false);

  const needsScenario = snapshot == null || !snapshot.scenario_loaded;
  const visible = needsScenario && !dismissed;

  const dismiss = useCallback(() => {
    setDismissed(true);
    onDismiss();
  }, [onDismiss]);

  const quickStart = useCallback(() => {
    void (async () => {
      await loadScenario(SCENARIO_BASIC);
      await runSim();
    })();
  }, [loadScenario, runSim]);

  const loadKey = useCallback(
    (key: ScenarioKey) => {
      void loadScenario(SCENARIOS[key].toml);
    },
    [loadScenario],
  );

  const cardClass =
    'flex flex-col rounded-2xl border border-zinc-700/80 bg-zinc-900/90 p-6 shadow-xl backdrop-blur-md transition hover:border-zinc-500/80';

  if (!visible) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-zinc-950/80 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="welcome-title"
    >
      <div className="relative max-h-[min(90vh,900px)] w-full max-w-5xl overflow-y-auto rounded-3xl border border-zinc-700 bg-gradient-to-b from-zinc-900 to-zinc-950 p-8 shadow-2xl">
        <button
          type="button"
          onClick={dismiss}
          className="absolute right-4 top-4 rounded-lg border border-zinc-600 px-3 py-1.5 text-sm text-zinc-300 transition hover:bg-zinc-800"
        >
          Close
        </button>

        <div className="mb-8 pr-16 text-center sm:text-left">
          <h1 id="welcome-title" className="text-2xl font-bold tracking-tight text-zinc-50 sm:text-3xl">
            Welcome to Arcogine
          </h1>
          <p className="mt-2 max-w-2xl text-sm text-zinc-400 sm:text-base">
            Pick a factory scenario to explore the simulator, or jump in with a quick start run.
          </p>
          <div className="mt-6 flex flex-wrap justify-center gap-3 sm:justify-start">
            <button
              type="button"
              onClick={quickStart}
              disabled={loading}
              className="rounded-xl bg-emerald-600 px-5 py-2.5 text-sm font-semibold text-white shadow-lg shadow-emerald-900/30 transition hover:bg-emerald-500 disabled:cursor-not-allowed disabled:opacity-40"
            >
              Quick Start
            </button>
            <button
              type="button"
              onClick={dismiss}
              className="rounded-xl border border-zinc-600 px-5 py-2.5 text-sm font-medium text-zinc-200 transition hover:bg-zinc-800"
            >
              Dismiss
            </button>
          </div>
        </div>

        <div className="grid gap-4 sm:grid-cols-3">
          {(Object.keys(SCENARIOS) as ScenarioKey[]).map((key) => {
            const copy = CARD_COPY[key];
            return (
              <article key={key} className={cardClass}>
                <h2 className="text-lg font-semibold text-zinc-100">{copy.title}</h2>
                <p className="mt-2 flex-1 text-sm leading-relaxed text-zinc-400">{copy.description}</p>
                <button
                  type="button"
                  onClick={() => loadKey(key)}
                  disabled={loading}
                  className="mt-5 w-full rounded-lg border border-zinc-600 bg-zinc-800/80 py-2 text-sm font-medium text-zinc-100 transition hover:bg-zinc-700 disabled:cursor-not-allowed disabled:opacity-40"
                >
                  Load {SCENARIOS[key].label}
                </button>
              </article>
            );
          })}
        </div>
      </div>
    </div>
  );
}
