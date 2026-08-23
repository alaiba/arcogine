import { describe, it, expect, beforeEach, vi } from 'vitest';
import { useSimulationStore, MAX_KPI_HISTORY_POINTS } from './simulation';
import type { SimSnapshot, SimEvent } from '../api/client';

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

const mockSseClients: Array<{
  connect: ReturnType<typeof vi.fn>;
  disconnect: ReturnType<typeof vi.fn>;
  emit: (event: SimEvent) => void;
}> = [];

vi.mock('../api/sse', () => {
  class MockSseClient {
    connect = vi.fn();
    disconnect = vi.fn();
    private onEvent: (event: SimEvent) => void;

    constructor(onEvent: (event: SimEvent) => void) {
      this.onEvent = onEvent;
      mockSseClients.push(this);
    }

    emit(event: SimEvent) {
      this.onEvent(event);
    }
  }
  return { SseClient: MockSseClient };
});

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
    mockSseClients.length = 0;
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
    it('replaces KPI history points for duplicate timestamps', async () => {
      useSimulationStore.setState({
        kpiHistory: [
          { time: 5, values: { event_count: 1 } },
          { time: 10, values: { event_count: 2 } },
        ],
      });

      const { postSimRun } = await import('../api/client');
      (postSimRun as ReturnType<typeof vi.fn>).mockResolvedValue(
        makeSnapshot(5, {
          kpis: [{ name: 'event_count', value: 99, unit: 'events' }],
        }),
      );

      await useSimulationStore.getState().runSim();
      const history = useSimulationStore.getState().kpiHistory;
      expect(history).toHaveLength(2);
      expect(history[0]).toEqual({ time: 5, values: { event_count: 99 } });
      expect(history[1]).toEqual({ time: 10, values: { event_count: 2 } });
    });

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

    it('inserts KPI history points to keep sorted order', async () => {
      useSimulationStore.setState({
        kpiHistory: [{ time: 10, values: { event_count: 10 } }],
      });

      const { postSimStep } = await import('../api/client');
      (postSimStep as ReturnType<typeof vi.fn>).mockResolvedValue(
        makeSnapshot(5, {
          kpis: [{ name: 'event_count', value: 5, unit: 'events' }],
        }),
      );

      await useSimulationStore.getState().stepSim();
      const history = useSimulationStore.getState().kpiHistory;
      expect(history).toHaveLength(2);
      expect(history).toEqual([
        { time: 5, values: { event_count: 5 } },
        { time: 10, values: { event_count: 10 } },
      ]);
    });

    it('updates snapshot when pausing simulation', async () => {
      const { postSimPause } = await import('../api/client');
      (postSimPause as ReturnType<typeof vi.fn>).mockResolvedValue(makeSnapshot(12));
      await useSimulationStore.getState().pauseSim();
      expect(useSimulationStore.getState().snapshot?.current_time).toBe(12);
      expect(useSimulationStore.getState().loading).toBe(false);
    });

    it('updates snapshot when changing price', async () => {
      const { postPrice } = await import('../api/client');
      (postPrice as ReturnType<typeof vi.fn>).mockResolvedValue(makeSnapshot(7));
      await useSimulationStore.getState().changePrice(15);
      expect(useSimulationStore.getState().snapshot?.current_time).toBe(7);
      expect(postPrice).toHaveBeenCalledWith(15);
    });

    it('updates snapshot when changing machine state', async () => {
      const { postMachines } = await import('../api/client');
      (postMachines as ReturnType<typeof vi.fn>).mockResolvedValue(makeSnapshot(7));
      await useSimulationStore.getState().changeMachine(11, false);
      expect(useSimulationStore.getState().snapshot?.current_time).toBe(7);
      expect(postMachines).toHaveBeenCalledWith(11, false);
    });

    it('updates snapshot when toggling agent', async () => {
      const { postAgent } = await import('../api/client');
      (postAgent as ReturnType<typeof vi.fn>).mockResolvedValue(makeSnapshot(7));
      await useSimulationStore.getState().toggleAgent(true);
      expect(useSimulationStore.getState().snapshot?.current_time).toBe(7);
      expect(postAgent).toHaveBeenCalledWith(true);
    });
  });

  describe('fetchSnapshot', () => {
    it('updates store state on success', async () => {
      const { getSnapshot } = await import('../api/client');
      (getSnapshot as ReturnType<typeof vi.fn>).mockResolvedValue(makeSnapshot(9));
      await useSimulationStore.getState().fetchSnapshot();
      expect(useSimulationStore.getState().snapshot?.current_time).toBe(9);
      expect(useSimulationStore.getState().error).toBeNull();
    });

    it('records errors from snapshot failures', async () => {
      const { getSnapshot } = await import('../api/client');
      (getSnapshot as ReturnType<typeof vi.fn>).mockRejectedValue(new Error('api down'));
      await useSimulationStore.getState().fetchSnapshot();
      expect(useSimulationStore.getState().error).toBe('api down');
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

    it('stores error when loadScenario fails', async () => {
      const { postScenario } = await import('../api/client');
      (postScenario as ReturnType<typeof vi.fn>).mockRejectedValue(new Error('bad scenario'));
      await useSimulationStore.getState().loadScenario('toml-content');
      expect(useSimulationStore.getState().loading).toBe(false);
      expect(useSimulationStore.getState().error).toBe('bad scenario');
      expect(useSimulationStore.getState().snapshot).toBeNull();
    });
  });

  describe('resetSim', () => {
    it('clears snapshot event history and loading state on success', async () => {
      useSimulationStore.setState({
        snapshot: makeSnapshot(1),
        events: [{ time: { '0': 1 }, event_type: 'TaskStart', payload: {} }],
        kpiHistory: [{ time: 1, values: { event_count: 1 } }],
      });

      const { postSimReset } = await import('../api/client');
      (postSimReset as ReturnType<typeof vi.fn>).mockResolvedValue(makeSnapshot(0));

      await useSimulationStore.getState().resetSim();
      const state = useSimulationStore.getState();
      expect(state.loading).toBe(false);
      expect(state.snapshot?.current_time).toBe(0);
      expect(state.events).toHaveLength(0);
      expect(state.kpiHistory).toHaveLength(0);
    });

    it('records errors from failed reset', async () => {
      const { postSimReset } = await import('../api/client');
      (postSimReset as ReturnType<typeof vi.fn>).mockRejectedValue(new Error('reset failed'));

      await useSimulationStore.getState().resetSim();
      expect(useSimulationStore.getState().loading).toBe(false);
      expect(useSimulationStore.getState().error).toBe('reset failed');
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

    it('adds events and refreshes snapshot on SSE payload', async () => {
      const { getSnapshot } = await import('../api/client');
      (getSnapshot as ReturnType<typeof vi.fn>).mockResolvedValue(makeSnapshot(42));
      useSimulationStore.getState().connectSse();
      const client = mockSseClients[mockSseClients.length - 1];
      client.emit({
        time: { '0': 5 },
        event_type: 'TaskEnd',
        payload: { machine_id: 1 },
      });
      expect(useSimulationStore.getState().events).toHaveLength(1);
      await vi.waitFor(() => {
        expect(useSimulationStore.getState().snapshot?.current_time).toBe(42);
      });
    });

    it('replaces SSE client when connecting twice', async () => {
      const { getSnapshot } = await import('../api/client');
      (getSnapshot as ReturnType<typeof vi.fn>).mockResolvedValue(makeSnapshot(3));
      useSimulationStore.getState().connectSse();
      const firstClient = mockSseClients[mockSseClients.length - 1];
      useSimulationStore.getState().connectSse();
      const secondClient = mockSseClients[mockSseClients.length - 1];
      expect(firstClient).not.toBe(secondClient);
      secondClient.emit({
        time: { '0': 9 },
        event_type: 'TaskEnd',
        payload: {},
      });
      expect(useSimulationStore.getState().events).toHaveLength(1);
    });
  });
});
