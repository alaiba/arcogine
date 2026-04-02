import { useCallback } from 'react';
import { getExportEvents } from '../../api/client';
import { useSimulationStore } from '../../stores/simulation';

function triggerDownload(content: string, filename: string, mime: string) {
  const blob = new Blob([content], { type: mime });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.style.display = 'none';
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

function csvCell(value: string): string {
  if (/[",\n\r]/.test(value)) {
    return `"${value.replace(/"/g, '""')}"`;
  }
  return value;
}

function buildKpiCsv(kpis: { name: string; value: number; unit: string }[]): string {
  const lines = ['name,value,unit'];
  for (const k of kpis) {
    lines.push([csvCell(k.name), csvCell(String(k.value)), csvCell(k.unit)].join(','));
  }
  return lines.join('\n');
}

export function ExportMenu() {
  const snapshot = useSimulationStore((s) => s.snapshot);

  const exportCsv = useCallback(() => {
    if (!snapshot) return;
    const csv = buildKpiCsv(snapshot.kpis);
    triggerDownload(csv, `arcogine-kpis-${Date.now()}.csv`, 'text/csv;charset=utf-8');
  }, [snapshot]);

  const exportJson = useCallback(async () => {
    try {
      const { events } = await getExportEvents();
      const json = JSON.stringify({ events }, null, 2);
      triggerDownload(json, `arcogine-events-${Date.now()}.json`, 'application/json');
    } catch (e) {
      window.alert(e instanceof Error ? e.message : String(e));
    }
  }, []);

  const exportPng = useCallback(() => {
    window.alert('PNG export is not available yet.');
  }, []);

  const btn =
    'rounded-md border border-zinc-600 bg-zinc-800 px-3 py-1.5 text-sm font-medium text-zinc-100 transition hover:bg-zinc-700 disabled:cursor-not-allowed disabled:opacity-40';

  return (
    <div className="flex flex-wrap gap-2">
      <button type="button" className={btn} onClick={exportCsv} disabled={!snapshot}>
        Export CSV
      </button>
      <button type="button" className={btn} onClick={() => void exportJson()}>
        Export JSON
      </button>
      <button type="button" className={btn} onClick={exportPng}>
        Export PNG
      </button>
    </div>
  );
}
