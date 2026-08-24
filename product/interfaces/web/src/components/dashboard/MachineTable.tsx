import type { MachineInfo } from '../../api/client';
import { useSimulationStore } from '../../stores/simulation';

function stateCellClass(state: MachineInfo['state']): string {
  const base = 'rounded-md px-2 py-0.5 text-xs font-medium';
  switch (state) {
    case 'Idle':
      return `${base} bg-emerald-500/20 text-emerald-800 dark:text-emerald-200`;
    case 'Busy':
      return `${base} bg-amber-500/20 text-amber-900 dark:text-amber-200`;
    case 'Offline':
      return `${base} bg-red-500/20 text-red-800 dark:text-red-200`;
    default:
      return `${base} bg-zinc-500/20 text-zinc-700`;
  }
}

export function MachineTable() {
  const machines = useSimulationStore((s) => s.snapshot?.topology.machines ?? []);

  if (machines.length === 0) {
    return (
      <div className="rounded-xl border border-zinc-200 bg-zinc-50 p-6 text-center text-sm text-zinc-500 dark:border-zinc-700 dark:bg-zinc-900/50 dark:text-zinc-400">
        No machines in snapshot.
      </div>
    );
  }

  const rows = [...machines].sort((a, b) => a.id - b.id);

  return (
    <div className="overflow-x-auto rounded-xl border border-zinc-200 bg-white dark:border-zinc-700 dark:bg-zinc-900/80">
      <table className="w-full min-w-[420px] border-collapse text-left text-sm">
        <thead>
          <tr className="border-b border-zinc-200 bg-zinc-50 dark:border-zinc-700 dark:bg-zinc-800/50">
            <th className="px-4 py-2 font-semibold text-zinc-700 dark:text-zinc-200">Name</th>
            <th className="px-4 py-2 font-semibold text-zinc-700 dark:text-zinc-200">State</th>
            <th className="px-4 py-2 font-semibold text-zinc-700 dark:text-zinc-200">Active jobs</th>
            <th className="px-4 py-2 font-semibold text-zinc-700 dark:text-zinc-200">Queue depth</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((m) => (
            <tr
              key={m.id}
              className="border-b border-zinc-100 last:border-0 dark:border-zinc-800"
            >
              <td className="px-4 py-2 font-medium text-zinc-900 dark:text-zinc-100">{m.name}</td>
              <td className="px-4 py-2">
                <span className={stateCellClass(m.state)}>{m.state}</span>
              </td>
              <td className="px-4 py-2 tabular-nums text-zinc-700 dark:text-zinc-300">
                {m.active_jobs}
              </td>
              <td className="px-4 py-2 tabular-nums text-zinc-700 dark:text-zinc-300">
                {m.queue_depth}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
