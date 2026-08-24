const BASE = '/api';

async function readErrorMessage(res: Response): Promise<string> {
  const fallback = res.statusText || `HTTP ${res.status}`;
  try {
    const body: unknown = await res.json();
    if (body && typeof body === 'object') {
      const o = body as Record<string, unknown>;
      if (typeof o.error === 'string') return o.error;
      if (typeof o.message === 'string') return o.message;
    }
  } catch {
    /* ignore */
  }
  return fallback;
}

async function jsonRequest<T>(path: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers);
  if (init?.body !== undefined && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  const res = await fetch(`${BASE}${path}`, { ...init, headers });
  if (!res.ok) {
    throw new Error(await readErrorMessage(res));
  }
  return res.json() as Promise<T>;
}

export interface KpiValue {
  name: string;
  value: number;
  unit: string;
}

export interface MachineInfo {
  id: number;
  name: string;
  state: 'Idle' | 'Busy' | 'Offline';
  queue_depth: number;
  active_jobs: number;
}

export interface RoutingEdge {
  from_machine_id: number;
  to_machine_id: number;
  routing_name: string;
}

export interface TopologySnapshot {
  machines: MachineInfo[];
  edges: RoutingEdge[];
}

export interface JobInfo {
  job_id: number;
  product_id: number;
  quantity: number;
  status: 'Queued' | 'InProgress' | 'Completed' | 'Cancelled';
  current_step: number;
  total_steps: number;
  created_at: number;
  completed_at: number | null;
  revenue: number | null;
}

export interface SimSnapshot {
  run_state: 'Idle' | 'Running' | 'Paused' | 'Completed';
  current_time: number;
  events_processed: number;
  kpis: KpiValue[];
  topology: TopologySnapshot;
  jobs: JobInfo[];
  total_revenue: number;
  completed_sales: number;
  backlog: number;
  current_price: number;
  agent_enabled: boolean;
  scenario_loaded: boolean;
}

export interface SimEvent {
  time: { '0': number };
  event_type: string;
  payload: Record<string, unknown>;
}

export interface EventLog {
  events: SimEvent[];
}

export function getHealth() {
  return jsonRequest<{ status: string }>('/health');
}

export function postScenario(toml: string) {
  return jsonRequest<{ success: boolean; message: string }>('/scenario', {
    method: 'POST',
    body: JSON.stringify({ toml }),
  });
}

export function postSimRun() {
  return jsonRequest<SimSnapshot>('/sim/run', { method: 'POST' });
}

export function postSimPause() {
  return jsonRequest<SimSnapshot>('/sim/pause', { method: 'POST' });
}

export function postSimStep() {
  return jsonRequest<SimSnapshot>('/sim/step', { method: 'POST' });
}

export function postSimReset() {
  return jsonRequest<SimSnapshot>('/sim/reset', { method: 'POST' });
}

export function postPrice(price: number) {
  return jsonRequest<SimSnapshot>('/price', {
    method: 'POST',
    body: JSON.stringify({ price }),
  });
}

export function postMachines(machine_id: number, online: boolean) {
  return jsonRequest<SimSnapshot>('/machines', {
    method: 'POST',
    body: JSON.stringify({ machine_id, online }),
  });
}

export function postAgent(enabled: boolean) {
  return jsonRequest<SimSnapshot>('/agent', {
    method: 'POST',
    body: JSON.stringify({ enabled }),
  });
}

export function getKpis() {
  return jsonRequest<KpiValue[]>('/kpis');
}

export function getSnapshot() {
  return jsonRequest<SimSnapshot>('/snapshot');
}

export function getTopology() {
  return jsonRequest<TopologySnapshot>('/factory/topology');
}

export function getJobs() {
  return jsonRequest<JobInfo[]>('/jobs');
}

export function getExportEvents() {
  return jsonRequest<EventLog>('/export/events');
}
