import { useId, useMemo } from 'react';
import type { MachineInfo, RoutingEdge } from '../../api/client';
import { useSimulationStore } from '../../stores/simulation';

const NODE_W = 112;
const NODE_H = 52;
const GAP = 48;
const PAD = 24;

function stateClasses(state: MachineInfo['state']): string {
  switch (state) {
    case 'Idle':
      return 'fill-emerald-500/25 stroke-emerald-600 dark:stroke-emerald-400';
    case 'Busy':
      return 'fill-amber-500/25 stroke-amber-600 dark:stroke-amber-400';
    case 'Offline':
      return 'fill-red-500/25 stroke-red-600 dark:stroke-red-400';
    default:
      return 'fill-zinc-500/25 stroke-zinc-500';
  }
}

function layoutMachines(machines: MachineInfo[]) {
  const sorted = [...machines].sort((a, b) => a.id - b.id);
  const positions = new Map<number, { x: number; y: number }>();
  sorted.forEach((m, i) => {
    positions.set(m.id, { x: PAD + i * (NODE_W + GAP), y: PAD });
  });
  return { sorted, positions };
}

export function FactoryFlow() {
  const arrowId = useId().replace(/:/g, '');
  const topology = useSimulationStore((s) => s.snapshot?.topology);

  const { width, height, sorted, positions, edges } = useMemo(() => {
    const machines = topology?.machines ?? [];
    const rawEdges: RoutingEdge[] = topology?.edges ?? [];
    const { sorted: sm, positions: pos } = layoutMachines(machines);
    const w =
      sm.length === 0 ? 120 : PAD * 2 + sm.length * NODE_W + Math.max(0, sm.length - 1) * GAP;
    const h = PAD * 2 + NODE_H + 40;
    return { width: w, height: h, sorted: sm, positions: pos, edges: rawEdges };
  }, [topology]);

  if (!topology || sorted.length === 0) {
    return (
      <div className="rounded-xl border border-zinc-200 bg-zinc-50 p-6 text-center text-sm text-zinc-500 dark:border-zinc-700 dark:bg-zinc-900/50 dark:text-zinc-400">
        Load a scenario to view factory topology.
      </div>
    );
  }

  return (
    <div className="overflow-x-auto rounded-xl border border-zinc-200 bg-white p-3 dark:border-zinc-700 dark:bg-zinc-900/80">
      <svg
        width={width}
        height={height}
        viewBox={`0 0 ${width} ${height}`}
        className="mx-auto min-w-0 text-zinc-900 dark:text-zinc-100"
        role="img"
        aria-label="Factory routing graph"
      >
        <defs>
          <marker
            id={`factory-flow-arrow-${arrowId}`}
            markerWidth="8"
            markerHeight="8"
            refX="7"
            refY="4"
            orient="auto"
            markerUnits="strokeWidth"
          >
            <path d="M0,0 L8,4 L0,8 Z" className="fill-zinc-500 dark:fill-zinc-400" />
          </marker>
        </defs>
        {edges.map((e, i) => {
          const from = positions.get(e.from_machine_id);
          const to = positions.get(e.to_machine_id);
          if (!from || !to) return null;
          const x1 = from.x + NODE_W;
          const y1 = from.y + NODE_H / 2;
          const x2 = to.x;
          const y2 = to.y + NODE_H / 2;
          const mid = (x1 + x2) / 2;
          return (
            <path
              key={`${e.from_machine_id}-${e.to_machine_id}-${e.routing_name}-${i}`}
              d={`M ${x1} ${y1} C ${mid} ${y1}, ${mid} ${y2}, ${x2} ${y2}`}
              fill="none"
              stroke="currentColor"
              strokeWidth={1.5}
              className="text-zinc-400 dark:text-zinc-500"
              markerEnd={`url(#factory-flow-arrow-${arrowId})`}
            />
          );
        })}
        {sorted.map((m) => {
          const p = positions.get(m.id);
          if (!p) return null;
          return (
            <g key={m.id} transform={`translate(${p.x},${p.y})`}>
              <rect
                width={NODE_W}
                height={NODE_H}
                rx={8}
                strokeWidth={2}
                className={stateClasses(m.state)}
              />
              <text
                x={NODE_W / 2}
                y={20}
                textAnchor="middle"
                className="fill-zinc-900 text-[11px] font-semibold dark:fill-zinc-100"
              >
                {m.name.length > 14 ? `${m.name.slice(0, 12)}…` : m.name}
              </text>
              <text
                x={NODE_W / 2}
                y={38}
                textAnchor="middle"
                className="fill-zinc-600 text-[10px] dark:fill-zinc-400"
              >
                Q: {m.queue_depth}
              </text>
            </g>
          );
        })}
      </svg>
    </div>
  );
}
