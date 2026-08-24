import { useMemo } from 'react';
import type { JobInfo, KpiValue } from '../../api/client';
import { useSimulationStore } from '../../stores/simulation';

function findKpi(kpis: KpiValue[], name: string): KpiValue | undefined {
  return kpis.find((k) => k.name === name);
}

function formatNumber(value: number, maxFrac = 2): string {
  if (!Number.isFinite(value)) return '—';
  return new Intl.NumberFormat(undefined, {
    maximumFractionDigits: maxFrac,
    minimumFractionDigits: 0,
  }).format(value);
}

function avgLeadTimeTicks(jobs: JobInfo[]): number | null {
  const done = jobs.filter((j) => j.status === 'Completed' && j.completed_at != null);
  if (done.length === 0) return null;
  let sum = 0;
  for (const j of done) {
    sum += (j.completed_at as number) - j.created_at;
  }
  return sum / done.length;
}

type CardProps = {
  label: string;
  value: string;
  unit: string;
  className: string;
};

function Card({ label, value, unit, className }: CardProps) {
  return (
    <div
      className={`rounded-xl border border-white/10 p-4 shadow-sm backdrop-blur-sm ${className}`}
    >
      <p className="text-xs font-semibold uppercase tracking-wide text-zinc-600 dark:text-zinc-400">
        {label}
      </p>
      <p className="mt-2 text-2xl font-semibold tabular-nums text-zinc-900 dark:text-zinc-50">
        {value}
        {unit ? (
          <span className="ml-1.5 text-base font-normal text-zinc-600 dark:text-zinc-400">
            {unit}
          </span>
        ) : null}
      </p>
    </div>
  );
}

export function KpiCards() {
  const snapshot = useSimulationStore((s) => s.snapshot);

  const cards = useMemo(() => {
    if (!snapshot) {
      return {
        revenue: { value: '—', unit: '' },
        backlog: { value: '—', unit: '' },
        leadTime: { value: '—', unit: '' },
        throughput: { value: '—', unit: '' },
      };
    }

    const leadKpi = findKpi(snapshot.kpis, 'lead_time');
    const computedLead = avgLeadTimeTicks(snapshot.jobs);
    const throughputKpi =
      findKpi(snapshot.kpis, 'throughput_rate') ?? findKpi(snapshot.kpis, 'throughput');

    return {
      revenue: {
        value: new Intl.NumberFormat(undefined, {
          style: 'currency',
          currency: 'USD',
          maximumFractionDigits: 2,
        }).format(snapshot.total_revenue),
        unit: '',
      },
      backlog: {
        value: formatNumber(snapshot.backlog, 0),
        unit: 'orders',
      },
      leadTime: leadKpi
        ? { value: formatNumber(leadKpi.value, 2), unit: leadKpi.unit }
        : computedLead != null
          ? { value: formatNumber(computedLead, 1), unit: 'ticks' }
          : { value: '—', unit: '' },
      throughput: throughputKpi
        ? { value: formatNumber(throughputKpi.value, 4), unit: throughputKpi.unit }
        : { value: '—', unit: '' },
    };
  }, [snapshot]);

  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-4">
      <Card
        label="Revenue"
        value={cards.revenue.value}
        unit={cards.revenue.unit}
        className="bg-emerald-500/15 dark:bg-emerald-500/20"
      />
      <Card
        label="Backlog"
        value={cards.backlog.value}
        unit={cards.backlog.unit}
        className="bg-sky-500/15 dark:bg-sky-500/20"
      />
      <Card
        label="Lead time"
        value={cards.leadTime.value}
        unit={cards.leadTime.unit}
        className="bg-violet-500/15 dark:bg-violet-500/20"
      />
      <Card
        label="Throughput"
        value={cards.throughput.value}
        unit={cards.throughput.unit}
        className="bg-amber-500/15 dark:bg-amber-500/20"
      />
    </div>
  );
}
