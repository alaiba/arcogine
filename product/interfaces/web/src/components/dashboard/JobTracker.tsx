import { useMemo, useState } from 'react';
import type { JobInfo } from '../../api/client';
import { useSimulationStore } from '../../stores/simulation';

type SortKey = 'job_id' | 'product_id' | 'status' | 'current_step' | 'created_at' | 'revenue';

function statusClass(status: JobInfo['status']): string {
  const base = 'rounded-md px-2 py-0.5 text-xs font-medium';
  switch (status) {
    case 'Queued':
      return `${base} bg-slate-500/20 text-slate-800 dark:text-slate-200`;
    case 'InProgress':
      return `${base} bg-sky-500/20 text-sky-900 dark:text-sky-200`;
    case 'Completed':
      return `${base} bg-emerald-500/20 text-emerald-900 dark:text-emerald-200`;
    case 'Cancelled':
      return `${base} bg-red-500/20 text-red-800 dark:text-red-200`;
    default:
      return `${base} bg-zinc-500/20`;
  }
}

function compareJobs(a: JobInfo, b: JobInfo, key: SortKey, dir: number): number {
  const va = a[key];
  const vb = b[key];
  if (va == null && vb == null) return 0;
  if (va == null) return 1;
  if (vb == null) return -1;
  if (typeof va === 'string' && typeof vb === 'string') {
    return va.localeCompare(vb) * dir;
  }
  if (typeof va === 'number' && typeof vb === 'number') {
    return (va - vb) * dir;
  }
  return 0;
}

const HEADER =
  'cursor-pointer select-none px-4 py-2 font-semibold text-zinc-700 hover:bg-zinc-100 dark:text-zinc-200 dark:hover:bg-zinc-800/80';

export function JobTracker() {
  const jobs = useSimulationStore((s) => s.snapshot?.jobs ?? []);
  const [sortKey, setSortKey] = useState<SortKey>('job_id');
  const [sortDir, setSortDir] = useState<1 | -1>(1);

  const sorted = useMemo(() => {
    const copy = [...jobs];
    copy.sort((a, b) => compareJobs(a, b, sortKey, sortDir));
    return copy;
  }, [jobs, sortKey, sortDir]);

  function toggle(key: SortKey) {
    if (sortKey === key) {
      setSortDir((d) => (d === 1 ? -1 : 1));
    } else {
      setSortKey(key);
      setSortDir(1);
    }
  }

  function headerLabel(key: SortKey, label: string) {
    const active = sortKey === key;
    return (
      <th className={HEADER} onClick={() => toggle(key)} scope="col">
        {label}
        {active ? (sortDir === 1 ? ' ↑' : ' ↓') : ''}
      </th>
    );
  }

  if (jobs.length === 0) {
    return (
      <div className="rounded-xl border border-zinc-200 bg-zinc-50 p-6 text-center text-sm text-zinc-500 dark:border-zinc-700 dark:bg-zinc-900/50 dark:text-zinc-400">
        No jobs recorded.
      </div>
    );
  }

  return (
    <div className="overflow-x-auto rounded-xl border border-zinc-200 bg-white dark:border-zinc-700 dark:bg-zinc-900/80">
      <table className="w-full min-w-[640px] border-collapse text-left text-sm">
        <thead>
          <tr className="border-b border-zinc-200 bg-zinc-50 dark:border-zinc-700 dark:bg-zinc-800/50">
            {headerLabel('job_id', 'Job ID')}
            {headerLabel('product_id', 'Product ID')}
            {headerLabel('status', 'Status')}
            {headerLabel('current_step', 'Current step')}
            {headerLabel('created_at', 'Created at')}
            {headerLabel('revenue', 'Revenue')}
          </tr>
        </thead>
        <tbody>
          {sorted.map((j) => (
            <tr
              key={j.job_id}
              className="border-b border-zinc-100 last:border-0 dark:border-zinc-800"
            >
              <td className="px-4 py-2 tabular-nums text-zinc-900 dark:text-zinc-100">
                {j.job_id}
              </td>
              <td className="px-4 py-2 tabular-nums text-zinc-700 dark:text-zinc-300">
                {j.product_id}
              </td>
              <td className="px-4 py-2">
                <span className={statusClass(j.status)}>{j.status}</span>
              </td>
              <td className="px-4 py-2 tabular-nums text-zinc-700 dark:text-zinc-300">
                {j.current_step} / {j.total_steps}
              </td>
              <td className="px-4 py-2 tabular-nums text-zinc-700 dark:text-zinc-300">
                {j.created_at}
              </td>
              <td className="px-4 py-2 tabular-nums text-zinc-700 dark:text-zinc-300">
                {j.revenue != null ? j.revenue.toFixed(2) : '—'}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
