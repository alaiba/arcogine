import { create } from 'zustand';
import type { SimSnapshot } from '../api/client';

export type BaselineEntry = {
  id: string;
  name: string;
  snapshot: SimSnapshot;
  savedAt: number;
};

const METRIC_KEYS = ['revenue', 'backlog', 'lead_time', 'throughput'] as const;
export type BaselineMetricKey = (typeof METRIC_KEYS)[number];

export type MetricDelta = {
  current: number;
  baseline: number;
  delta: number;
  pct: number;
};

function readMetric(snapshot: SimSnapshot, key: BaselineMetricKey): number {
  switch (key) {
    case 'revenue':
      return snapshot.total_revenue;
    case 'backlog':
      return snapshot.backlog;
    case 'lead_time':
      return snapshot.kpis.find((k) => k.name === 'lead_time')?.value ?? 0;
    case 'throughput':
      return snapshot.kpis.find((k) => k.name === 'throughput')?.value ?? 0;
    default: {
      const _exhaustive: never = key;
      return _exhaustive;
    }
  }
}

type BaselinesState = {
  baselines: BaselineEntry[];
  saveBaseline: (name: string, snapshot: SimSnapshot) => void;
  removeBaseline: (id: string) => void;
  clearBaselines: () => void;
  getDeltas: (
    current: SimSnapshot,
    baselineId: string,
  ) => Record<string, MetricDelta>;
};

export const useBaselinesStore = create<BaselinesState>((set, get) => ({
  baselines: [],

  saveBaseline: (name, snapshot) =>
    set((s) => ({
      baselines: [
        ...s.baselines,
        { id: crypto.randomUUID(), name, snapshot, savedAt: Date.now() },
      ].slice(-3),
    })),

  removeBaseline: (id) =>
    set((s) => ({
      baselines: s.baselines.filter((b) => b.id !== id),
    })),

  clearBaselines: () => set({ baselines: [] }),

  getDeltas: (current, baselineId) => {
    const baseline = get().baselines.find((b) => b.id === baselineId);
    if (!baseline) return {};
    const out: Record<string, MetricDelta> = {};
    for (const key of METRIC_KEYS) {
      const cur = readMetric(current, key);
      const base = readMetric(baseline.snapshot, key);
      const delta = cur - base;
      const pct = base !== 0 ? (delta / base) * 100 : 0;
      out[key] = { current: cur, baseline: base, delta, pct };
    }
    return out;
  },
}));
