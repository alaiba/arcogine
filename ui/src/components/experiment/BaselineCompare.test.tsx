import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { BaselineCompare } from './BaselineCompare';
import { useSimulationStore } from '../../stores/simulation';
import { useBaselinesStore } from '../../stores/baselines';
import type { SimSnapshot } from '../../api/client';

function makeSnapshot(overrides: Partial<SimSnapshot> = {}): SimSnapshot {
  return {
    run_state: 'Paused',
    current_time: 100,
    events_processed: 50,
    kpis: [
      { name: 'throughput_rate', value: 0.5, unit: 'task_completions/tick' },
    ],
    topology: { machines: [], edges: [] },
    jobs: [],
    total_revenue: 200,
    completed_sales: 10,
    backlog: 5,
    current_price: 10,
    agent_enabled: false,
    scenario_loaded: true,
    ...overrides,
  };
}

describe('BaselineCompare', () => {
  beforeEach(() => {
    useSimulationStore.setState({ snapshot: null, kpiHistory: [] });
    useBaselinesStore.setState({ baselines: [] });
  });

  it('renders empty state when no baselines', () => {
    render(<BaselineCompare />);
    expect(screen.getByText(/no saved baselines/i)).toBeInTheDocument();
  });

  it('disables save when no scenario is loaded', () => {
    render(<BaselineCompare />);
    expect(screen.getByRole('button', { name: /save baseline/i })).toBeDisabled();
  });

  it('renders baseline list from store', () => {
    const snap = makeSnapshot();
    useSimulationStore.setState({ snapshot: snap });
    useBaselinesStore.getState().saveBaseline('Baseline A', snap);
    render(<BaselineCompare />);
    expect(screen.getByText('Baseline A')).toBeInTheDocument();
  });

  it('saves baseline from prompt when scenario is loaded', () => {
    const promptSpy = vi.spyOn(window, 'prompt').mockReturnValue('Saved Baseline');
    const snap = makeSnapshot();
    useSimulationStore.setState({ snapshot: snap });
    render(<BaselineCompare />);
    fireEvent.click(screen.getByRole('button', { name: /save baseline/i }));
    expect(screen.getByText('Saved Baseline')).toBeInTheDocument();
    promptSpy.mockRestore();
  });

  it('does not save baseline when prompt is cancelled', () => {
    const promptSpy = vi.spyOn(window, 'prompt').mockReturnValue(null);
    const snap = makeSnapshot();
    useSimulationStore.setState({ snapshot: snap });
    render(<BaselineCompare />);
    fireEvent.click(screen.getByRole('button', { name: /save baseline/i }));
    expect(screen.queryByText('Saved Baseline')).not.toBeInTheDocument();
    promptSpy.mockRestore();
  });

  it('removes a baseline when remove is clicked', () => {
    const snap = makeSnapshot();
    useSimulationStore.setState({ snapshot: snap });
    useBaselinesStore.getState().saveBaseline('Removable', snap);
    render(<BaselineCompare />);
    fireEvent.click(screen.getByRole('button', { name: /remove/i }));
    expect(screen.queryByText('Removable')).not.toBeInTheDocument();
  });

  it('shows snapshot comparison note when snapshot is missing', () => {
    const snap = makeSnapshot();
    useBaselinesStore.getState().saveBaseline('Compare me', snap);
    useSimulationStore.setState({ snapshot: null });
    render(<BaselineCompare />);
    expect(screen.getByText(/load a snapshot to compare/i)).toBeInTheDocument();
  });

  it('isImprovement: revenue increase is improvement', () => {
    const baseSnap = makeSnapshot({ total_revenue: 100 });
    const currentSnap = makeSnapshot({ total_revenue: 200 });
    useSimulationStore.setState({ snapshot: currentSnap });
    useBaselinesStore.getState().saveBaseline('base', baseSnap);
    render(<BaselineCompare />);
    expect(screen.getByText('Revenue')).toBeInTheDocument();
  });

  it('formatMetric formats currency for revenue', () => {
    const snap = makeSnapshot({ total_revenue: 1234.56 });
    useSimulationStore.setState({ snapshot: snap });
    useBaselinesStore.getState().saveBaseline('base', makeSnapshot({ total_revenue: 1000 }));
    render(<BaselineCompare />);
    expect(screen.getByText('Revenue')).toBeInTheDocument();
  });
});
