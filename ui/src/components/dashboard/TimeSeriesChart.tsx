import { useMemo } from 'react';
import {
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { useSimulationStore } from '../../stores/simulation';

type Row = {
  time: number;
  event_count?: number;
  throughput_rate?: number;
  order_count?: number;
};

export function TimeSeriesChart() {
  const kpiHistory = useSimulationStore((s) => s.kpiHistory);

  const data = useMemo<Row[]>(
    () =>
      kpiHistory.map((p) => ({
        time: p.time,
        event_count: p.values.event_count,
        throughput_rate: p.values.throughput_rate,
        order_count: p.values.order_count,
      })),
    [kpiHistory],
  );

  if (data.length === 0) {
    return (
      <div className="flex h-[280px] items-center justify-center rounded-xl border border-zinc-200 bg-zinc-50 text-sm text-zinc-500 dark:border-zinc-700 dark:bg-zinc-900/50 dark:text-zinc-400">
        No simulation history yet. Run or step the simulation to collect KPI samples.
      </div>
    );
  }

  return (
    <div className="h-[280px] w-full min-h-0 min-w-0 rounded-xl border border-zinc-200 bg-white p-3 dark:border-zinc-700 dark:bg-zinc-900/80">
      <ResponsiveContainer width="100%" height="100%" minWidth={0} minHeight={0}>
        <LineChart data={data} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
          <XAxis
            dataKey="time"
            type="number"
            domain={['dataMin', 'dataMax']}
            tick={{ fontSize: 11 }}
            stroke="var(--color-zinc-500, #71717a)"
            label={{ value: 'Time (ticks)', position: 'insideBottom', offset: -4, fontSize: 11 }}
          />
          <YAxis tick={{ fontSize: 11 }} stroke="var(--color-zinc-500, #71717a)" width={44} />
          <Tooltip
            contentStyle={{
              borderRadius: '0.5rem',
              border: '1px solid rgb(63 63 70)',
              background: 'rgb(24 24 27)',
              color: 'rgb(244 244 245)',
            }}
            labelFormatter={(t) => `t = ${t}`}
          />
          <Legend wrapperStyle={{ fontSize: 12 }} />
          <Line
            type="monotone"
            dataKey="event_count"
            name="Event count"
            stroke="#3b82f6"
            strokeWidth={2}
            dot={false}
            connectNulls
          />
          <Line
            type="monotone"
            dataKey="throughput_rate"
            name="Throughput rate"
            stroke="#22c55e"
            strokeWidth={2}
            dot={false}
            connectNulls
          />
          <Line
            type="monotone"
            dataKey="order_count"
            name="Order count"
            stroke="#a855f7"
            strokeWidth={2}
            dot={false}
            connectNulls
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
