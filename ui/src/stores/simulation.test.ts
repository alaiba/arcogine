import { describe, it, expect, beforeEach, vi } from 'vitest';
import { useSimulationStore, MAX_KPI_HISTORY_POINTS } from './simulation';
import type { SimSnapshot } from '../api/client';

vi.mock('../api/client', () => ({
  postScenario: vi.fn(),
  getSnapshot: vi.fn(),
  postSimRun: vi.fn(),
  postSimPause: vi.fn(),
  postSimStep: vi.fn(),
  postSimReset: vi.fn(),
  postPrice: vi.fn(),
  postMachines: vi.fn(),
  postAgent: vi.fn(),
}));

vi.mock('../api/sse', () => ({
  SseClient: class MockSseClient {
    connect = vi.fn();
    disconnect = vi.fn();
    constructor(_onEvent: unknown) {}
  },
}));

function makeSnapshot(time: number, overrides: Partial<SimSnapshot> = {}): SimSnapshot {
  return {
    run_state: 'Idle',
    current_time: time,
    events_processed: 0,
    kpis: [{ name: 'event_count', value: time, unit: 'events' }],
    topology: { machines: [], edges: [] },
    jobs: [],
    total_revenue: 0,
    completed_sales: 0,
    backlog: 0,
    current_price: 10,
    agent_enabled: false,
    scenario_loaded: true,
    ...overrides,
  };
}

describe('useSimulationStore', () => {
  beforeEach(() => {
    useSimulationStore.setState({
      snapshot: null,
      events: [],
      kpiHistory: [],
      connected: false,
      loading: false,
      error: null,
      sseClient: null,
    });
  });

  describe('mergeSnapshot via loadScenario', () => {
    it('appends to kpiHistory', async () => {
      const { postScenario, getSnapshot } = await import('../api/client');
      (postScenario as ReturnType<typeof vi.fn>).mockResolvedValue({ success: true });
      (getSnapshot as ReturnType<typeof vi.fn>).mockResolvedValue(makeSnapshot(10));

      await useSimulationStore.getState().loadScenario('test');
      expect(useSimulationStore.getState().kpiHistory).toHaveLength(1);
      expect(useSimulationStore.getState().kpiHistory[0].time).toBe(10);
    });

    it('trims kpiHistory to MAX_KPI_HISTORY_POINTS', async () => {
      const existing = Array.from({ length: MAX_KPI_HISTORY_POINTS }, (_, i) => ({
        time: i,
        values: { event_count: i },
      }));
      useSimulationStore.setState({ kpiHistory: existing });

      const { postScenario, getSnapshot } = await import('../api/client');
      (postScenario as ReturnType<typeof vi.fn>).mockResolvedValue({ success: true });
      (getSnapshot as ReturnType<typeof vi.fn>).mockResolvedValue(makeSnapshot(999));

      await useSimulationStore.getState().loadScenario('test');
      expect(useSimulationStore.getState().kpiHistory.length).toBeLessThanOrEqual(
        MAX_KPI_HISTORY_POINTS,
      );
    });
  });

  describe('withLoading', () => {
    it('sets loading true then false on success', async () => {
      const { postSimRun } = await import('../api/client');
      (postSimRun as ReturnType<typeof vi.fn>).mockResolvedValue(makeSnapshot(1));

      const promise = useSimulationStore.getState().runSim();
      expect(useSimulationStore.getState().loading).toBe(true);
      await promise;
      expect(useSimulationStore.getState().loading).toBe(false);
    });

    it('sets error on rejection', async () => {
      const { postSimRun } = await import('../api/client');
      (postSimRun as ReturnType<typeof vi.fn>).mockRejectedValue(new Error('fail'));

      await useSimulationStore.getState().runSim();
      expect(useSimulationStore.getState().loading).toBe(false);
      expect(useSimulationStore.getState().error).toBe('fail');
    });
  });

  describe('clearError', () => {
    it('clears the error state', () => {
      useSimulationStore.setState({ error: 'some error' });
      useSimulationStore.getState().clearError();
      expect(useSimulationStore.getState().error).toBeNull();
    });
  });

  describe('loadScenario', () => {
    it('calls postScenario then fetchSnapshot', async () => {
      const { postScenario, getSnapshot } = await import('../api/client');
      (postScenario as ReturnType<typeof vi.fn>).mockResolvedValue({ success: true });
      (getSnapshot as ReturnType<typeof vi.fn>).mockResolvedValue(makeSnapshot(0));

      await useSimulationStore.getState().loadScenario('toml-content');
      expect(postScenario).toHaveBeenCalledWith('toml-content');
      expect(getSnapshot).toHaveBeenCalled();
      expect(useSimulationStore.getState().snapshot).not.toBeNull();
    });
  });

  describe('SSE lifecycle', () => {
    it('connectSse creates client and sets connected', () => {
      useSimulationStore.getState().connectSse();
      expect(useSimulationStore.getState().connected).toBe(true);
      expect(useSimulationStore.getState().sseClient).not.toBeNull();
    });

    it('disconnectSse clears client and sets disconnected', () => {
      useSimulationStore.getState().connectSse();
      useSimulationStore.getState().disconnectSse();
      expect(useSimulationStore.getState().connected).toBe(false);
      expect(useSimulationStore.getState().sseClient).toBeNull();
    });
  });
});
