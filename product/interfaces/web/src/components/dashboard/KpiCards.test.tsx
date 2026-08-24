import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { KpiCards } from './KpiCards';
import { useSimulationStore } from '../../stores/simulation';
import type { SimSnapshot } from '../../api/client';

function makeSnapshot(overrides: Partial<SimSnapshot> = {}): SimSnapshot {
  return {
    run_state: 'Paused',
    current_time: 100,
    events_processed: 50,
    kpis: [
      { name: 'throughput_rate', value: 0.5, unit: 'task_completions/tick' },
      { name: 'event_count', value: 50, unit: 'events' },
    ],
    topology: { machines: [], edges: [] },
    jobs: [
      {
        job_id: 1,
        product_id: 1,
        quantity: 3,
        status: 'Completed',
        current_step: 1,
        total_steps: 1,
        created_at: 10,
        completed_at: 20,
        revenue: 30,
      },
    ],
    total_revenue: 150.5,
    completed_sales: 5,
    backlog: 3,
    current_price: 10,
    agent_enabled: false,
    scenario_loaded: true,
    ...overrides,
  };
}

describe('KpiCards', () => {
  beforeEach(() => {
    useSimulationStore.setState({ snapshot: null, kpiHistory: [] });
  });

  it('renders placeholder cards when snapshot is null', () => {
    render(<KpiCards />);
    const dashes = screen.getAllByText('—');
    expect(dashes.length).toBeGreaterThanOrEqual(4);
  });

  it('renders four KPI cards with correct labels', () => {
    useSimulationStore.setState({ snapshot: makeSnapshot() });
    render(<KpiCards />);
    expect(screen.getByText('Revenue')).toBeInTheDocument();
    expect(screen.getByText('Backlog')).toBeInTheDocument();
    expect(screen.getByText('Lead time')).toBeInTheDocument();
    expect(screen.getByText('Throughput')).toBeInTheDocument();
  });

  it('computes avgLeadTimeTicks from completed jobs', () => {
    useSimulationStore.setState({
      snapshot: makeSnapshot({
        kpis: [],
        jobs: [
          {
            job_id: 1,
            product_id: 1,
            quantity: 1,
            status: 'Completed',
            current_step: 1,
            total_steps: 1,
            created_at: 0,
            completed_at: 10,
            revenue: 10,
          },
          {
            job_id: 2,
            product_id: 1,
            quantity: 1,
            status: 'Completed',
            current_step: 1,
            total_steps: 1,
            created_at: 0,
            completed_at: 20,
            revenue: 10,
          },
        ],
      }),
    });
    render(<KpiCards />);
    expect(screen.getByText('15')).toBeInTheDocument();
  });

  it('renders fallback when lead-time and throughput KPIs are missing', () => {
    useSimulationStore.setState({
      snapshot: makeSnapshot({
        kpis: [],
        jobs: [],
      }),
    });
    render(<KpiCards />);
    const dashes = screen.getAllByText('—');
    expect(dashes.length).toBeGreaterThanOrEqual(2);
  });

  it('renders non-finite numbers as em dash', () => {
    useSimulationStore.setState({
      snapshot: makeSnapshot({ backlog: Infinity }),
    });
    render(<KpiCards />);
    const dashes = screen.getAllByText('—');
    expect(dashes.length).toBeGreaterThanOrEqual(1);
  });
});
