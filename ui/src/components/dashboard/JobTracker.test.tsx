import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { JobTracker } from './JobTracker';
import { useSimulationStore } from '../../stores/simulation';
import type { SimSnapshot, JobInfo } from '../../api/client';

function makeSnapshot(jobs: JobInfo[]): SimSnapshot {
  return {
    run_state: 'Paused',
    current_time: 100,
    events_processed: 50,
    kpis: [],
    topology: { machines: [], edges: [] },
    jobs,
    total_revenue: 100,
    completed_sales: 2,
    backlog: 1,
    current_price: 10,
    agent_enabled: false,
    scenario_loaded: true,
  };
}

describe('JobTracker', () => {
  beforeEach(() => {
    useSimulationStore.setState({ snapshot: null, kpiHistory: [] });
  });

  it('renders empty state when no jobs', () => {
    useSimulationStore.setState({ snapshot: makeSnapshot([]) });
    render(<JobTracker />);
    expect(screen.getByText(/no jobs recorded/i)).toBeInTheDocument();
  });

  it('renders job rows with correct status badges', () => {
    useSimulationStore.setState({
      snapshot: makeSnapshot([
        {
          job_id: 1,
          product_id: 1,
          quantity: 2,
          status: 'Completed',
          current_step: 1,
          total_steps: 1,
          created_at: 5,
          completed_at: 15,
          revenue: 20,
        },
        {
          job_id: 2,
          product_id: 1,
          quantity: 1,
          status: 'Queued',
          current_step: 0,
          total_steps: 1,
          created_at: 10,
          completed_at: null,
          revenue: null,
        },
      ]),
    });
    render(<JobTracker />);
    expect(screen.getByText('Completed')).toBeInTheDocument();
    expect(screen.getByText('Queued')).toBeInTheDocument();
  });

  it('handles null revenue values', () => {
    useSimulationStore.setState({
      snapshot: makeSnapshot([
        {
          job_id: 1,
          product_id: 1,
          quantity: 1,
          status: 'InProgress',
          current_step: 0,
          total_steps: 1,
          created_at: 5,
          completed_at: null,
          revenue: null,
        },
      ]),
    });
    render(<JobTracker />);
    expect(screen.getByText('—')).toBeInTheDocument();
  });

  it('sort toggle changes direction', () => {
    useSimulationStore.setState({
      snapshot: makeSnapshot([
        {
          job_id: 1,
          product_id: 1,
          quantity: 1,
          status: 'Completed',
          current_step: 1,
          total_steps: 1,
          created_at: 5,
          completed_at: 15,
          revenue: 10,
        },
        {
          job_id: 2,
          product_id: 1,
          quantity: 1,
          status: 'Queued',
          current_step: 0,
          total_steps: 1,
          created_at: 10,
          completed_at: null,
          revenue: null,
        },
      ]),
    });
    render(<JobTracker />);
    const header = screen.getByText(/Job ID/);
    fireEvent.click(header);
    expect(header.textContent).toContain('↓');
  });
});
