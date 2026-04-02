import { describe, it, expect, beforeEach } from 'vitest';
import { useBaselinesStore } from './baselines';
import type { SimSnapshot } from '../api/client';

function makeSnapshot(overrides: Partial<SimSnapshot> = {}): SimSnapshot {
  return {
    run_state: 'Idle',
    current_time: 0,
    events_processed: 0,
    kpis: [
      { name: 'lead_time', value: 5, unit: 'ticks' },
      { name: 'throughput', value: 10, unit: 'units/tick' },
    ],
    topology: { machines: [], edges: [] },
    jobs: [],
    total_revenue: 100,
    completed_sales: 5,
    backlog: 3,
    current_price: 10,
    agent_enabled: false,
    scenario_loaded: true,
    ...overrides,
  };
}

describe('useBaselinesStore', () => {
  beforeEach(() => {
    useBaselinesStore.setState({ baselines: [] });
  });

  it('starts with empty baselines', () => {
    expect(useBaselinesStore.getState().baselines).toHaveLength(0);
  });

  it('saves a baseline and assigns an id', () => {
    const snap = makeSnapshot();
    useBaselinesStore.getState().saveBaseline('test-baseline', snap);
    const baselines = useBaselinesStore.getState().baselines;
    expect(baselines).toHaveLength(1);
    expect(baselines[0].name).toBe('test-baseline');
    expect(baselines[0].id).toBeTruthy();
    expect(baselines[0].snapshot).toEqual(snap);
  });

  it('limits stored baselines to 3', () => {
    const { saveBaseline } = useBaselinesStore.getState();
    saveBaseline('b1', makeSnapshot());
    saveBaseline('b2', makeSnapshot());
    saveBaseline('b3', makeSnapshot());
    saveBaseline('b4', makeSnapshot());
    const baselines = useBaselinesStore.getState().baselines;
    expect(baselines).toHaveLength(3);
    expect(baselines[0].name).toBe('b2');
  });

  it('removes a baseline by id', () => {
    useBaselinesStore.getState().saveBaseline('keep', makeSnapshot());
    useBaselinesStore.getState().saveBaseline('remove', makeSnapshot());
    const toRemove = useBaselinesStore.getState().baselines[1];
    useBaselinesStore.getState().removeBaseline(toRemove.id);
    const baselines = useBaselinesStore.getState().baselines;
    expect(baselines).toHaveLength(1);
    expect(baselines[0].name).toBe('keep');
  });

  it('clears all baselines', () => {
    useBaselinesStore.getState().saveBaseline('b1', makeSnapshot());
    useBaselinesStore.getState().clearBaselines();
    expect(useBaselinesStore.getState().baselines).toHaveLength(0);
  });

  it('computes deltas between current and baseline snapshots', () => {
    const baseSnap = makeSnapshot({ total_revenue: 100, backlog: 10 });
    useBaselinesStore.getState().saveBaseline('base', baseSnap);
    const baselineId = useBaselinesStore.getState().baselines[0].id;

    const currentSnap = makeSnapshot({ total_revenue: 150, backlog: 5 });
    const deltas = useBaselinesStore.getState().getDeltas(currentSnap, baselineId);

    expect(deltas.revenue.current).toBe(150);
    expect(deltas.revenue.baseline).toBe(100);
    expect(deltas.revenue.delta).toBe(50);
    expect(deltas.revenue.pct).toBeCloseTo(50);

    expect(deltas.backlog.current).toBe(5);
    expect(deltas.backlog.baseline).toBe(10);
    expect(deltas.backlog.delta).toBe(-5);
    expect(deltas.backlog.pct).toBeCloseTo(-50);
  });

  it('returns empty deltas for unknown baseline id', () => {
    const deltas = useBaselinesStore
      .getState()
      .getDeltas(makeSnapshot(), 'nonexistent');
    expect(deltas).toEqual({});
  });

  it('handles zero baseline value in pct calculation', () => {
    const baseSnap = makeSnapshot({ total_revenue: 0 });
    useBaselinesStore.getState().saveBaseline('zero', baseSnap);
    const id = useBaselinesStore.getState().baselines[0].id;

    const currentSnap = makeSnapshot({ total_revenue: 100 });
    const deltas = useBaselinesStore.getState().getDeltas(currentSnap, id);
    expect(deltas.revenue.pct).toBe(0);
  });
});
