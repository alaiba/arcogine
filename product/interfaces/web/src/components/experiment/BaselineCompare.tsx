import { useCallback } from 'react';
import type { BaselineMetricKey } from '../../stores/baselines';
import { useBaselinesStore } from '../../stores/baselines';
import { useSimulationStore } from '../../stores/simulation';

const METRIC_ORDER: { key: BaselineMetricKey; label: string }[] = [
  { key: 'revenue', label: 'Revenue' },
  { key: 'backlog', label: 'Backlog' },
  { key: 'lead_time', label: 'Lead time' },
  { key: 'throughput', label: 'Throughput' },
];

function isImprovement(key: BaselineMetricKey, delta: number): boolean | null {
  if (delta === 0) return null;
  switch (key) {
    case 'revenue':
    case 'throughput':
      return delta > 0;
    case 'backlog':
    case 'lead_time':
      return delta < 0;
    default: {
      const _e: never = key;
      return _e;
    }
  }
}

function formatMetric(key: BaselineMetricKey, value: number): string {
  if (key === 'revenue') {
    return new Intl.NumberFormat(undefined, {
      style: 'currency',
      currency: 'USD',
      maximumFractionDigits: 2,
    }).format(value);
  }
  return new Intl.NumberFormat(undefined, { maximumFractionDigits: 4 }).format(value);
}

function ArrowUpGreen() {
  return (
    <svg className="h-4 w-4 shrink-0 text-emerald-500" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path
        fillRule="evenodd"
        d="M11.47 7.72a.75.75 0 011.06 0l7.5 7.5a.75.75 0 11-1.06 1.06L12 9.31l-6.97 6.97a.75.75 0 01-1.06-1.06l7.5-7.5z"
        clipRule="evenodd"
      />
    </svg>
  );
}

function ArrowDownRed() {
  return (
    <svg className="h-4 w-4 shrink-0 text-red-500" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path
        fillRule="evenodd"
        d="M12.53 16.28a.75.75 0 01-1.06 0l-7.5-7.5a.75.75 0 011.06-1.06L12 14.69l6.97-6.97a.75.75 0 011.06 1.06l-7.5 7.5z"
        clipRule="evenodd"
      />
    </svg>
  );
}

export function BaselineCompare() {
  const snapshot = useSimulationStore((s) => s.snapshot);
  const baselines = useBaselinesStore((s) => s.baselines);
  const saveBaseline = useBaselinesStore((s) => s.saveBaseline);
  const removeBaseline = useBaselinesStore((s) => s.removeBaseline);
  const getDeltas = useBaselinesStore((s) => s.getDeltas);

  const onSave = useCallback(() => {
    if (!snapshot?.scenario_loaded) return;
    const name = window.prompt('Baseline name');
    if (name == null) return;
    const trimmed = name.trim();
    if (!trimmed) return;
    saveBaseline(trimmed, snapshot);
  }, [saveBaseline, snapshot]);

  const btn =
    'rounded-md border border-zinc-600 bg-zinc-800 px-3 py-1.5 text-sm font-medium text-zinc-100 transition hover:bg-zinc-700 disabled:cursor-not-allowed disabled:opacity-40';

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h3 className="text-sm font-semibold text-zinc-100">Baselines</h3>
        <button type="button" className={btn} onClick={onSave} disabled={!snapshot?.scenario_loaded}>
          Save Baseline
        </button>
      </div>

      {baselines.length === 0 ? (
        <p className="text-sm text-zinc-500">No saved baselines. Run a simulation and save a snapshot.</p>
      ) : (
        <ul className="space-y-4">
          {baselines.map((b) => {
            const deltas = snapshot ? getDeltas(snapshot, b.id) : null;
            return (
              <li
                key={b.id}
                className="rounded-xl border border-zinc-700 bg-zinc-900/60 p-4 shadow-sm"
              >
                <div className="mb-3 flex flex-wrap items-start justify-between gap-2">
                  <div>
                    <p className="font-medium text-zinc-100">{b.name}</p>
                    <p className="text-xs text-zinc-500">
                      Saved {new Date(b.savedAt).toLocaleString()}
                    </p>
                  </div>
                  <button
                    type="button"
                    className={`${btn} border-red-900/50 text-red-200 hover:bg-red-950/40`}
                    onClick={() => removeBaseline(b.id)}
                  >
                    Remove
                  </button>
                </div>

                {!snapshot ? (
                  <p className="text-sm text-zinc-500">Load a snapshot to compare.</p>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full min-w-[280px] text-left text-sm">
                      <thead>
                        <tr className="border-b border-zinc-700 text-xs uppercase tracking-wide text-zinc-500">
                          <th className="pb-2 pr-3 font-medium">Metric</th>
                          <th className="pb-2 pr-3 font-medium">Current</th>
                          <th className="pb-2 pr-3 font-medium">Baseline</th>
                          <th className="pb-2 font-medium">Δ</th>
                        </tr>
                      </thead>
                      <tbody className="text-zinc-200">
                        {METRIC_ORDER.map(({ key, label }) => {
                          const d = deltas?.[key];
                          if (!d) return null;
                          const verdict = isImprovement(key, d.delta);
                          return (
                            <tr key={key} className="border-b border-zinc-800 last:border-0">
                              <td className="py-2 pr-3 text-zinc-300">{label}</td>
                              <td className="py-2 pr-3 tabular-nums">{formatMetric(key, d.current)}</td>
                              <td className="py-2 pr-3 tabular-nums">{formatMetric(key, d.baseline)}</td>
                              <td className="py-2">
                                <span className="inline-flex items-center gap-1.5 tabular-nums">
                                  {verdict === true ? <ArrowUpGreen /> : null}
                                  {verdict === false ? <ArrowDownRed /> : null}
                                  <span
                                    className={
                                      verdict === true
                                        ? 'text-emerald-400'
                                        : verdict === false
                                          ? 'text-red-400'
                                          : 'text-zinc-400'
                                    }
                                  >
                                    {d.delta > 0 ? '+' : ''}
                                    {formatMetric(key, d.delta)}
                                  </span>
                                  {d.baseline !== 0 ? (
                                    <span className="text-xs text-zinc-500">
                                      ({d.pct > 0 ? '+' : ''}
                                      {d.pct.toFixed(1)}%)
                                    </span>
                                  ) : null}
                                </span>
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                )}
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}
