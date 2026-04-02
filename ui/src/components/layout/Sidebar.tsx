import { useCallback, useEffect, useMemo, useState } from 'react';
import type { MetricDelta } from '../../stores/baselines';
import { useBaselinesStore } from '../../stores/baselines';
import { useSimulationStore } from '../../stores/simulation';

const METRIC_LABELS: Record<string, string> = {
  revenue: 'Revenue',
  backlog: 'Backlog',
  lead_time: 'Lead time',
  throughput: 'Throughput',
};

function deltaClass(metric: string, delta: number): string {
  if (delta === 0) return 'text-zinc-400';
  const higherIsBetter = metric === 'revenue' || metric === 'throughput';
  const good =
    higherIsBetter ? delta > 0 : delta < 0;
  return good ? 'text-emerald-500' : 'text-red-500';
}

function formatDelta(d: MetricDelta): string {
  const sign = d.delta >= 0 ? '+' : '';
  return `${sign}${d.delta.toFixed(2)} (${sign}${d.pct.toFixed(1)}%)`;
}

export function Sidebar() {
  const snapshot = useSimulationStore((s) => s.snapshot);
  const loading = useSimulationStore((s) => s.loading);
  const changePrice = useSimulationStore((s) => s.changePrice);
  const changeMachine = useSimulationStore((s) => s.changeMachine);

  const baselines = useBaselinesStore((s) => s.baselines);
  const saveBaseline = useBaselinesStore((s) => s.saveBaseline);
  const getDeltas = useBaselinesStore((s) => s.getDeltas);

  const [localPrice, setLocalPrice] = useState(10);

  useEffect(() => {
    if (snapshot) setLocalPrice(snapshot.current_price);
  }, [snapshot, snapshot?.current_price]);

  const machines = snapshot?.topology.machines ?? [];

  const baselinePanels = useMemo(() => {
    if (!snapshot) return [];
    return baselines.map((b) => ({
      baseline: b,
      deltas: getDeltas(snapshot, b.id),
    }));
  }, [baselines, getDeltas, snapshot]);

  const onPriceInput = useCallback(
    (v: number) => {
      setLocalPrice(v);
      if (snapshot) void changePrice(v);
    },
    [changePrice, snapshot],
  );

  const onSaveBaseline = useCallback(() => {
    if (!snapshot) return;
    const name =
      window.prompt('Baseline name', `Baseline ${new Date().toLocaleString()}`)?.trim() ||
      `Baseline ${new Date().toISOString()}`;
    saveBaseline(name, snapshot);
  }, [saveBaseline, snapshot]);

  return (
    <aside className="flex w-80 shrink-0 flex-col gap-5 overflow-y-auto border-l border-zinc-700 bg-zinc-900 p-4">
      <section>
        <h2 className="mb-2 text-xs font-semibold uppercase tracking-wide text-zinc-400">Price</h2>
        <div className="flex flex-col gap-2">
          <div className="flex items-baseline justify-between gap-2">
            <span className="text-2xl font-semibold tabular-nums text-zinc-100">
              {snapshot ? localPrice.toFixed(1) : '—'}
            </span>
            <span className="text-xs text-zinc-500">0.5 – 50</span>
          </div>
          <input
            type="range"
            min={0.5}
            max={50}
            step={0.5}
            value={snapshot ? localPrice : 10}
            onChange={(e) => onPriceInput(Number(e.target.value))}
            disabled={!snapshot || loading}
            className="w-full accent-emerald-500 disabled:opacity-40"
          />
        </div>
      </section>

      <section>
        <h2 className="mb-2 text-xs font-semibold uppercase tracking-wide text-zinc-400">Machines</h2>
        {!snapshot ? (
          <p className="text-sm text-zinc-500">Load a scenario to configure machines.</p>
        ) : (
          <ul className="flex flex-col gap-2">
            {machines.map((m) => {
              const online = m.state !== 'Offline';
              return (
                <li
                  key={m.id}
                  className="flex items-center justify-between gap-2 rounded-md border border-zinc-700 bg-zinc-800/50 px-3 py-2"
                >
                  <span className="truncate text-sm font-medium text-zinc-200">{m.name}</span>
                  <button
                    type="button"
                    role="switch"
                    aria-checked={online}
                    disabled={loading}
                    onClick={() => void changeMachine(m.id, !online)}
                    className={`inline-flex h-7 w-12 shrink-0 cursor-pointer items-center rounded-full border px-0.5 transition-colors disabled:cursor-not-allowed disabled:opacity-40 ${
                      online
                        ? 'justify-end border-emerald-600 bg-emerald-900/40'
                        : 'justify-start border-zinc-600 bg-zinc-800'
                    }`}
                  >
                    <span className="h-5 w-5 rounded-full bg-zinc-200 shadow" />
                  </button>
                </li>
              );
            })}
          </ul>
        )}
      </section>

      <section>
        <h2 className="mb-2 text-xs font-semibold uppercase tracking-wide text-zinc-400">Baselines</h2>
        <button
          type="button"
          onClick={onSaveBaseline}
          disabled={!snapshot || loading}
          className="mb-3 w-full rounded-md border border-zinc-600 bg-zinc-800 py-2 text-sm font-medium text-zinc-100 hover:bg-zinc-700 disabled:cursor-not-allowed disabled:opacity-40"
        >
          Save baseline
        </button>
        {baselinePanels.length === 0 ? (
          <p className="text-sm text-zinc-500">No saved baselines.</p>
        ) : (
          <ul className="flex flex-col gap-3">
            {baselinePanels.map(({ baseline, deltas }) => (
              <li key={baseline.id} className="rounded-md border border-zinc-700 bg-zinc-800/40 p-3">
                <div className="mb-2 flex items-start justify-between gap-2">
                  <span className="text-sm font-medium text-zinc-100">{baseline.name}</span>
                  <span className="text-[10px] text-zinc-500">
                    {new Date(baseline.savedAt).toLocaleDateString()}
                  </span>
                </div>
                <dl className="grid grid-cols-1 gap-1.5 text-xs">
                  {Object.entries(deltas).map(([key, d]) => (
                    <div key={key} className="flex justify-between gap-2">
                      <dt className="text-zinc-400">{METRIC_LABELS[key] ?? key}</dt>
                      <dd className={`tabular-nums ${deltaClass(key, d.delta)}`}>{formatDelta(d)}</dd>
                    </div>
                  ))}
                </dl>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section>
        <h2 className="mb-2 text-xs font-semibold uppercase tracking-wide text-zinc-400">Export</h2>
        <div className="flex flex-col gap-2">
          <button
            type="button"
            className="rounded-md border border-dashed border-zinc-600 py-2 text-sm text-zinc-400 hover:border-zinc-500 hover:text-zinc-300"
          >
            Export CSV
          </button>
          <button
            type="button"
            className="rounded-md border border-dashed border-zinc-600 py-2 text-sm text-zinc-400 hover:border-zinc-500 hover:text-zinc-300"
          >
            Export JSON
          </button>
          <button
            type="button"
            className="rounded-md border border-dashed border-zinc-600 py-2 text-sm text-zinc-400 hover:border-zinc-500 hover:text-zinc-300"
          >
            Export PNG
          </button>
        </div>
      </section>
    </aside>
  );
}
