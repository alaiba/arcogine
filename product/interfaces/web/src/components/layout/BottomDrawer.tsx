import { useMemo, useState } from 'react';
import type { SimEvent } from '../../stores/simulation';
import { useSimulationStore } from '../../stores/simulation';

const EVENT_FILTER_OPTIONS = [
  'All',
  'OrderCreation',
  'TaskEnd',
  'PriceChange',
  'AgentDecision',
] as const;

type EventFilter = (typeof EVENT_FILTER_OPTIONS)[number];

function getEventTime(ev: SimEvent): number {
  const t = ev.time as unknown;
  if (Array.isArray(t) && typeof t[0] === 'number') return t[0];
  if (t && typeof t === 'object' && t !== null && '0' in t) {
    const v = (t as Record<string, unknown>)['0'];
    if (typeof v === 'number') return v;
  }
  return 0;
}

function payloadSummary(payload: Record<string, unknown>): string {
  try {
    return JSON.stringify(payload).slice(0, 60);
  } catch {
    return '';
  }
}

export function BottomDrawer() {
  const events = useSimulationStore((s) => s.events);
  const [open, setOpen] = useState(false);
  const [filter, setFilter] = useState<EventFilter>('All');
  const [search, setSearch] = useState('');

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    return events.filter((ev) => {
      if (filter !== 'All' && ev.event_type !== filter) return false;
      if (!q) return true;
      const row = `${getEventTime(ev)} ${ev.event_type} ${payloadSummary(ev.payload)}`.toLowerCase();
      return row.includes(q);
    });
  }, [events, filter, search]);

  const rows = useMemo(
    () =>
      [...filtered].reverse().map((ev, i) => (
        <tr key={`${getEventTime(ev)}-${ev.event_type}-${i}`} className="border-b border-zinc-800 text-left text-xs">
          <td className="px-3 py-1.5 font-mono tabular-nums text-zinc-300">{getEventTime(ev)}</td>
          <td className="px-3 py-1.5 text-zinc-200">{ev.event_type}</td>
          <td className="max-w-[min(48rem,60vw)] truncate px-3 py-1.5 font-mono text-zinc-400" title={JSON.stringify(ev.payload)}>
            {payloadSummary(ev.payload)}
          </td>
        </tr>
      )),
    [filtered],
  );

  return (
    <div
      className={`flex shrink-0 flex-col border-t border-zinc-700 bg-zinc-900 transition-[height] ${
        open ? 'h-[min(40vh,22rem)]' : 'h-10'
      }`}
    >
      <div className="flex h-10 shrink-0 items-center justify-between gap-2 border-b border-zinc-800 px-3">
        <button
          type="button"
          onClick={() => setOpen((o) => !o)}
          className="flex items-center gap-2 text-sm font-medium text-zinc-200 hover:text-white"
          aria-expanded={open}
        >
          <span className="text-zinc-500" aria-hidden>
            {open ? '▼' : '▶'}
          </span>
          Event log
          <span className="rounded-full bg-zinc-800 px-2 py-0.5 text-[10px] font-normal text-zinc-400">
            {filtered.length}
          </span>
        </button>
        {open && (
          <div className="flex flex-wrap items-center gap-2">
            <select
              value={filter}
              onChange={(e) => setFilter(e.target.value as EventFilter)}
              className="rounded border border-zinc-600 bg-zinc-800 px-2 py-1 text-xs text-zinc-100"
            >
              {EVENT_FILTER_OPTIONS.map((opt) => (
                <option key={opt} value={opt}>
                  {opt}
                </option>
              ))}
            </select>
            <input
              type="search"
              placeholder="Search…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-40 rounded border border-zinc-600 bg-zinc-800 px-2 py-1 text-xs text-zinc-100 placeholder:text-zinc-500 sm:w-56"
            />
          </div>
        )}
      </div>
      {open && (
        <div className="min-h-0 flex-1 overflow-auto">
          <table className="w-full border-collapse">
            <thead className="sticky top-0 bg-zinc-900 text-[10px] font-semibold uppercase tracking-wide text-zinc-500">
              <tr>
                <th className="px-3 py-2 text-left">Time</th>
                <th className="px-3 py-2 text-left">Type</th>
                <th className="px-3 py-2 text-left">Payload</th>
              </tr>
            </thead>
            <tbody>{rows}</tbody>
          </table>
          {filtered.length === 0 && (
            <p className="px-3 py-6 text-center text-sm text-zinc-500">No events match.</p>
          )}
        </div>
      )}
    </div>
  );
}
