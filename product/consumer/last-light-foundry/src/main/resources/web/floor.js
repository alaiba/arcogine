// SVG rendering of the schematic factory floor and equipment glyphs. Rendering only: it receives
// view models (the draft, the server's resource observations) and never derives simulation facts.

const NS = 'http://www.w3.org/2000/svg';
export const CELL = 64;

/** Original vector glyphs, drawn in a 40x40 box centred on the cell. Shapes, not just colour. */
export const GLYPHS = {
  cutter: 'M8 32 L20 6 L32 32 Z M20 14 L20 26',
  assembler: 'M8 8 H32 V32 H8 Z M8 20 H32 M20 8 V32',
  twin: 'M5 10 H18 V30 H5 Z M22 10 H35 V30 H22 Z M11 20 H12 M28 20 H29',
  inspector: 'M20 7 A13 13 0 1 0 20.1 7 Z M20 14 A6 6 0 1 0 20.1 14 Z',
  flex: 'M20 5 L33 12 V28 L20 35 L7 28 V12 Z M13 20 H27 M20 13 V27',
};

function el(name, attrs = {}, text) {
  const node = document.createElementNS(NS, name);
  for (const [key, value] of Object.entries(attrs)) node.setAttribute(key, String(value));
  if (text !== undefined) node.textContent = text;
  return node;
}

/** Draws a standalone glyph icon (used by the palette). */
export function glyphIcon(glyph, size = 30) {
  const svg = el('svg', { viewBox: '0 0 40 40', width: size, height: size, 'aria-hidden': 'true' });
  svg.appendChild(el('path', {
    d: GLYPHS[glyph] ?? GLYPHS.assembler, fill: 'none', stroke: 'currentColor',
    'stroke-width': 2.4, 'stroke-linejoin': 'round', 'stroke-linecap': 'round',
  }));
  return svg;
}

/**
 * @param {SVGSVGElement} svg
 * @param {{floor:{width:number,height:number}, installations:object[], equipment:Map<string,object>,
 *          selected:number|null, cursor:{x:number,y:number}, resources:Map<string,object>|null}} model
 */
export function renderFloor(svg, model) {
  const { floor, installations, equipment, selected, cursor, resources } = model;
  svg.setAttribute('viewBox', `0 0 ${floor.width * CELL} ${floor.height * CELL}`);
  svg.replaceChildren();
  for (let y = 0; y < floor.height; y += 1) {
    for (let x = 0; x < floor.width; x += 1) {
      const cell = el('rect', {
        class: `cell target${cursor.x === x && cursor.y === y ? ' cursor' : ''}`,
        x: x * CELL + 1, y: y * CELL + 1, width: CELL - 2, height: CELL - 2, rx: 6,
        'data-x': x, 'data-y': y, 'data-testid': `cell-${x}-${y}`,
      });
      svg.appendChild(cell);
    }
  }
  for (const item of installations) {
    const template = equipment.get(item.itemId);
    const resource = resources?.get(String(item.number)) ?? null;
    const state = resource?.state ?? 'Idle';
    const group = el('g', {
      class: `machine${selected === item.number ? ' selected' : ''}`,
      'data-number': item.number, 'data-state': state, 'data-testid': `machine-${item.number}`,
      transform: `translate(${item.x * CELL}, ${item.y * CELL})`,
    });
    group.appendChild(el('title', {}, `${template?.name ?? item.itemId} #${item.number}`));
    group.appendChild(el('rect', { class: 'halo', x: -2, y: -2, width: CELL + 4, height: CELL + 4, rx: 12 }));
    group.appendChild(el('rect', { class: 'body', x: 5, y: 5, width: CELL - 10, height: CELL - 10, rx: 8 }));
    group.appendChild(el('path', {
      class: 'glyph', d: GLYPHS[template?.glyph] ?? GLYPHS.assembler,
      transform: `translate(${(CELL - 40) / 2}, ${(CELL - 40) / 2 - 4})`,
    }));
    group.appendChild(el('text', { x: 9, y: CELL - 9 }, `#${item.number}`));
    if (resource) {
      const working = resource.activeJobs.length;
      const queued = resource.queueDepth;
      group.appendChild(el('text', { class: 'badge', x: CELL - 9, y: 18, 'text-anchor': 'end' },
        `${working}/${resource.concurrency}`));
      if (queued > 0) {
        group.appendChild(el('text', { x: CELL - 9, y: CELL - 9, 'text-anchor': 'end' }, `+${queued}q`));
      }
    }
    svg.appendChild(group);
  }
}
