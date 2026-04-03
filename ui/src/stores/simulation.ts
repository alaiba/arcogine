import { create } from 'zustand';
import {
  getSnapshot,
  postAgent,
  postMachines,
  postPrice,
  postScenario,
  postSimPause,
  postSimReset,
  postSimRun,
  postSimStep,
} from '../api/client';
import type { KpiValue, SimEvent, SimSnapshot } from '../api/client';

export type {
  JobInfo,
  KpiValue,
  MachineInfo,
  SimEvent,
  SimSnapshot,
  TopologySnapshot,
} from '../api/client';
import { SseClient } from '../api/sse';

export type KpiHistoryPoint = {
  time: number;
  values: Record<string, number>;
};

export const MAX_KPI_HISTORY_POINTS = 500;

function kpiValuesRecord(kpis: KpiValue[]): Record<string, number> {
  const values: Record<string, number> = {};
  for (const k of kpis) {
    values[k.name] = k.value;
  }
  return values;
}

type SimulationState = {
  snapshot: SimSnapshot | null;
  events: SimEvent[];
  kpiHistory: KpiHistoryPoint[];
  connected: boolean;
  loading: boolean;
  error: string | null;
  sseClient: SseClient | null;
  loadScenario: (toml: string) => Promise<void>;
  runSim: () => Promise<void>;
  pauseSim: () => Promise<void>;
  stepSim: () => Promise<void>;
  resetSim: () => Promise<void>;
  changePrice: (price: number) => Promise<void>;
  changeMachine: (machine_id: number, online: boolean) => Promise<void>;
  toggleAgent: (enabled: boolean) => Promise<void>;
  fetchSnapshot: () => Promise<void>;
  connectSse: () => void;
  disconnectSse: () => void;
  clearError: () => void;
};

export const useSimulationStore = create<SimulationState>((set, get) => {
  const mergeSnapshot = (snapshot: SimSnapshot) =>
    set((s) => {
      const t = snapshot.current_time;
      const values = kpiValuesRecord(snapshot.kpis);
      const existing = s.kpiHistory;
      const lastIdx = existing.findIndex((p) => p.time === t);
      let next: KpiHistoryPoint[];
      if (lastIdx !== -1) {
        next = [...existing];
        next[lastIdx] = { time: t, values };
      } else {
        const insertAt = existing.findIndex((p) => p.time > t);
        if (insertAt === -1) {
          next = [...existing, { time: t, values }];
        } else {
          next = [...existing.slice(0, insertAt), { time: t, values }, ...existing.slice(insertAt)];
        }
      }
      if (next.length > MAX_KPI_HISTORY_POINTS) {
        next = next.slice(next.length - MAX_KPI_HISTORY_POINTS);
      }
      return { snapshot, kpiHistory: next };
    });

  const withLoading = async (fn: () => Promise<SimSnapshot>) => {
    set({ loading: true, error: null });
    try {
      const snapshot = await fn();
      mergeSnapshot(snapshot);
      set({ loading: false, error: null });
    } catch (e) {
      set({
        loading: false,
        error: e instanceof Error ? e.message : String(e),
      });
    }
  };

  return {
    snapshot: null,
    events: [],
    kpiHistory: [],
    connected: false,
    loading: false,
    error: null,
    sseClient: null,

    loadScenario: async (toml) => {
      set({ loading: true, error: null, kpiHistory: [], events: [] });
      try {
        await postScenario(toml);
        const snapshot = await getSnapshot();
        mergeSnapshot(snapshot);
        set({ loading: false, error: null });
      } catch (e) {
        set({
          loading: false,
          error: e instanceof Error ? e.message : String(e),
        });
      }
    },

    runSim: () => withLoading(postSimRun),
    pauseSim: () => withLoading(postSimPause),
    stepSim: () => withLoading(postSimStep),
    resetSim: async () => {
      set({ loading: true, error: null });
      try {
        const snapshot = await postSimReset();
        set({
          snapshot,
          kpiHistory: [],
          events: [],
          loading: false,
          error: null,
        });
      } catch (e) {
        set({
          loading: false,
          error: e instanceof Error ? e.message : String(e),
        });
      }
    },
    changePrice: (price) => withLoading(() => postPrice(price)),
    changeMachine: (machine_id, online) => withLoading(() => postMachines(machine_id, online)),
    toggleAgent: (enabled) => withLoading(() => postAgent(enabled)),

    fetchSnapshot: async () => {
      try {
        const snapshot = await getSnapshot();
        mergeSnapshot(snapshot);
        set({ error: null });
      } catch (e) {
        set({ error: e instanceof Error ? e.message : String(e) });
      }
    },

    connectSse: () => {
      get().disconnectSse();
      const client = new SseClient((event: SimEvent) => {
        set((s) => ({ events: [...s.events, event] }));
        void get().fetchSnapshot();
      });
      client.connect();
      set({ sseClient: client, connected: true });
    },

    disconnectSse: () => {
      const { sseClient } = get();
      sseClient?.disconnect();
      set({ sseClient: null, connected: false });
    },

    clearError: () => set({ error: null }),
  };
});
