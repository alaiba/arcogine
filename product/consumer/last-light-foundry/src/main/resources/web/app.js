// Last Light Foundry browser client: input, view models and rendering only. Every number about
// cost, validity, time, completion and score is read from the Java server; nothing is computed
// here beyond layout and the advisory local cost estimate shown while editing.

import { api, ApiError } from './api.js';
import {
  createDraft, createEditor, estimateCost, move, occupant, place, redo, sell, toRequest, undo,
} from './draft.js';
import { acceptEvents, describeEvent, joinFromSnapshot } from './events.js';
import { CELL, glyphIcon, renderFloor } from './floor.js';

const $ = (id) => document.getElementById(id);

function h(tag, props = {}, ...children) {
  const node = document.createElement(tag);
  for (const [key, value] of Object.entries(props)) {
    if (value === null || value === undefined || value === false) continue;
    if (key === 'class') node.className = value;
    else if (key.startsWith('on')) node.addEventListener(key.slice(2), value);
    else node.setAttribute(key, value);
  }
  for (const child of children.flat()) {
    if (child !== null && child !== undefined) node.append(child);
  }
  return node;
}

function fill(node, ...children) {
  node.replaceChildren(...children.flat().filter((child) => child !== null && child !== undefined && child !== false));
}

const SPEEDS = {
  1: { events: 1, delay: 700 },
  2: { events: 1, delay: 250 },
  3: { events: 4, delay: 120 },
  4: { events: 60, delay: 20 },
};

const state = {
  missions: [],
  mission: null,
  drafts: new Map(),
  paletteItem: null,
  selected: null,
  cursor: { x: 0, y: 0 },
  mode: 'edit',
  design: null,
  run: null,
  join: null,
  eventLines: [],
  eventNote: '',
  hintsShown: 0,
  playing: false,
  busy: false,
};

// ---------- helpers ----------

function banner(kind, message) {
  const node = $('banner');
  node.hidden = !message;
  node.className = kind;
  node.textContent = message ?? '';
}

function editor() {
  return state.drafts.get(state.mission.id);
}

function setEditor(next) {
  state.drafts.set(state.mission.id, next);
}

function equipmentMap() {
  return new Map(state.mission.equipment.map((entry) => [entry.itemId, entry]));
}

function fmt(value) {
  return value === null || value === undefined ? '–' : String(value);
}

function reportError(error) {
  if (error instanceof ApiError) {
    if (error.kind === 'network') {
      state.playing = false;
      banner('error', 'Disconnected: the Last Light Foundry server cannot be reached. Nothing was changed. Restart it and press the button again.');
    } else {
      banner('error', `Refused (${error.code}): ${error.message}`);
    }
  } else {
    banner('error', `Unexpected client error: ${error.message}`);
  }
}

async function guarded(action) {
  if (state.busy) return;
  state.busy = true;
  try {
    await action();
  } catch (error) {
    reportError(error);
  } finally {
    state.busy = false;
    render();
  }
}

// ---------- mission loading ----------

async function loadMission(id) {
  state.playing = false;
  state.mission = await api.mission(id);
  if (!state.drafts.has(id)) {
    state.drafts.set(id, createEditor(createDraft(state.mission.starterDraft)));
  }
  state.paletteItem = null;
  state.selected = null;
  state.cursor = { x: 0, y: 0 };
  state.mode = 'edit';
  state.design = null;
  state.run = null;
  state.join = null;
  state.eventLines = [];
  state.eventNote = '';
  state.hintsShown = 0;
  banner('', null);
  render();
}

// ---------- editing ----------

function floorClick(event) {
  const machine = event.target.closest('.machine');
  if (machine) {
    state.selected = Number(machine.dataset.number);
    state.paletteItem = null;
    render();
    return;
  }
  const cell = event.target.closest('.cell');
  if (cell) activateCell(Number(cell.dataset.x), Number(cell.dataset.y));
}

function activateCell(x, y) {
  state.cursor = { x, y };
  if (state.mode !== 'edit') {
    const here = occupant(editor().present, x, y);
    state.selected = here ? here.number : null;
    render();
    return;
  }
  const here = occupant(editor().present, x, y);
  if (here) {
    state.selected = here.number;
    state.paletteItem = null;
  } else if (state.paletteItem) {
    const result = place(editor(), state.mission.floor, state.paletteItem, x, y);
    if (result.error) banner('warn', result.error);
    else {
      banner('', null);
      setEditor(result.editor);
      state.selected = result.number;
      invalidateDesign();
    }
  } else if (state.selected !== null) {
    const result = move(editor(), state.mission.floor, state.selected, x, y);
    if (result.error) banner('warn', result.error);
    else {
      banner('', null);
      setEditor(result.editor);
      invalidateDesign();
    }
  }
  render();
}

function invalidateDesign() {
  state.design = null;
}

function floorKey(event) {
  const arrows = { ArrowLeft: [-1, 0], ArrowRight: [1, 0], ArrowUp: [0, -1], ArrowDown: [0, 1] };
  if (event.key in arrows) {
    event.preventDefault();
    const [dx, dy] = arrows[event.key];
    if (event.shiftKey && state.mode === 'edit' && state.selected !== null) {
      const item = editor().present.installations.find((candidate) => candidate.number === state.selected);
      const result = move(editor(), state.mission.floor, state.selected, item.x + dx, item.y + dy);
      if (result.error) banner('warn', result.error);
      else {
        setEditor(result.editor);
        state.cursor = { x: item.x + dx, y: item.y + dy };
        invalidateDesign();
      }
    } else {
      const { floor } = state.mission;
      state.cursor = {
        x: Math.min(floor.width - 1, Math.max(0, state.cursor.x + dx)),
        y: Math.min(floor.height - 1, Math.max(0, state.cursor.y + dy)),
      };
    }
    render();
  } else if (event.key === 'Enter') {
    event.preventDefault();
    activateCell(state.cursor.x, state.cursor.y);
  } else if ((event.key === 'Delete' || event.key === 'Backspace') && state.mode === 'edit') {
    sellSelected();
  }
}

function sellSelected() {
  if (state.selected === null) return;
  setEditor(sell(editor(), state.selected).editor);
  state.selected = null;
  invalidateDesign();
  render();
}

function onGlobalKey(event) {
  if (event.target.matches('input, select, textarea')) return;
  const mod = event.ctrlKey || event.metaKey;
  if (mod && event.key.toLowerCase() === 'z' && state.mode === 'edit') {
    event.preventDefault();
    setEditor(undo(editor()));
    invalidateDesign();
    render();
  } else if (mod && event.key.toLowerCase() === 'y' && state.mode === 'edit') {
    event.preventDefault();
    setEditor(redo(editor()));
    invalidateDesign();
    render();
  }
}

// ---------- server actions ----------

function checkDesign() {
  return guarded(async () => {
    state.design = await api.check(state.mission.id, toRequest(editor().present));
    banner('', null);
  });
}

function publishAndStart() {
  return guarded(async () => {
    try {
      const run = await api.start(state.mission.id, toRequest(editor().present));
      state.design = null;
      adoptRun(run);
      banner('ok', 'Design published. The engine now holds one real order; press Step or Play.');
      await syncEvents();
    } catch (error) {
      if (error instanceof ApiError && error.code === 'design.not-runnable') {
        state.design = error.body.design;
        banner('error', error.message);
      } else {
        throw error;
      }
    }
  });
}

function adoptRun(run) {
  state.run = run;
  state.mode = 'run';
  state.join = { runId: run.runId, fingerprint: run.modelFingerprint, cursor: 0n };
  state.eventLines = [];
  state.eventNote = '';
}

async function syncEvents() {
  const runId = state.run.runId;
  for (let page = 0; page < 20; page += 1) {
    let batch;
    try {
      batch = await api.events(runId, state.join.cursor.toString());
    } catch (error) {
      if (error instanceof ApiError && error.status === 410) {
        // Retained history was lost: resnapshot, and say that earlier events are unavailable.
        const fresh = await api.run(runId);
        state.run = fresh;
        state.join = joinFromSnapshot(fresh);
        state.eventNote = `Earlier events were not retained; showing events after #${fresh.latestEventSequence}.`;
        return;
      }
      throw error;
    }
    if (state.run.runId !== runId) return; // the run was replaced while the request was in flight
    const result = acceptEvents(state.join, batch.events);
    state.join = result.state;
    for (const event of result.accepted) {
      state.eventLines.unshift({ sequence: event.sequence, time: event.time, text: describeEvent(event), type: event.type });
    }
    state.eventLines.length = Math.min(state.eventLines.length, 300);
    if (result.gap || result.wrongModel) {
      const fresh = await api.run(runId);
      state.run = fresh;
      state.join = joinFromSnapshot(fresh);
      state.eventNote = 'A gap in the event stream was detected; resnapshotted from the server.';
      return;
    }
    if (!batch.more) return;
  }
}

function stepOnce() {
  return guarded(async () => {
    state.run = await api.step(state.run.runId, 1);
    await syncEvents();
  });
}

async function playLoop() {
  state.playing = true;
  render();
  const runId = state.run?.runId;
  while (state.playing && state.run && state.run.runId === runId && state.run.status === 'ACTIVE') {
    const speed = SPEEDS[$('speed').value];
    try {
      const advanced = await api.advance(runId, speed.events);
      // Retry or a contract switch may have replaced the current run while this request was in
      // flight: a response for a superseded run is discarded and never drives later commands.
      if (!state.run || state.run.runId !== runId) break;
      state.run = advanced;
      await syncEvents();
    } catch (error) {
      reportError(error);
      state.playing = false;
    }
    render();
    await new Promise((resolve) => setTimeout(resolve, speed.delay));
  }
  if (state.run?.runId === runId) state.playing = false;
  render();
}

function togglePlay() {
  if (state.playing) {
    state.playing = false;
    render();
  } else {
    playLoop();
  }
}

function retry() {
  state.playing = false;
  return guarded(async () => {
    adoptRun(await api.reset(state.run.runId));
    banner('ok', 'A fresh run was created from the same published design. The previous run is unchanged.');
    await syncEvents();
  });
}

function editDesign() {
  state.playing = false;
  state.mode = 'edit';
  banner('', null);
  render();
}

// ---------- rendering ----------

function render() {
  if (!state.mission) return;
  const m = state.mission;
  const run = state.run;
  $('tagline').textContent = m.tagline;
  renderTabs();
  renderStats();
  renderBriefing();
  renderPalette();
  renderFloorView();
  renderInspector();
  renderReports();
  renderResult();
  renderWork();
  renderEvents();
  const active = run && run.status === 'ACTIVE' && state.mode === 'run';
  $('undo').disabled = state.mode !== 'edit' || editor().past.length === 0;
  $('redo').disabled = state.mode !== 'edit' || editor().future.length === 0;
  $('check').disabled = state.mode !== 'edit' || state.busy;
  $('publish').disabled = state.mode !== 'edit' || state.busy;
  $('step').disabled = !active || state.playing || state.busy;
  $('play').disabled = !active || state.busy;
  $('play').textContent = state.playing ? 'Pause' : 'Play';
  $('reset').disabled = !run || state.busy;
  $('edit').disabled = state.mode !== 'run' || state.busy;
  const bar = run ? Math.min(100, (Number(run.time) / Number(run.deadline)) * 100) : 0;
  $('time-bar').style.width = `${bar}%`;
}

function renderTabs() {
  const tabs = $('mission-tabs');
  fill(tabs, ...state.missions.map((mission) => h('button', {
    type: 'button', 'data-testid': `tab-${mission.id}`,
    'aria-current': String(mission.id === state.mission.id),
    onclick: () => guarded(() => loadMission(mission.id)),
  }, mission.title)));
}

function renderStats() {
  const m = state.mission;
  const run = state.run;
  const design = state.design;
  let budgetText;
  if (run && state.mode === 'run') {
    budgetText = `${BigInt(m.budgetCredits) - BigInt(run.cost)} / ${m.budgetCredits}`;
  } else if (design?.economics?.ok) {
    budgetText = `${design.economics.remaining} / ${m.budgetCredits}`;
  } else {
    budgetText = `${BigInt(m.budgetCredits) - estimateCost(editor().present, m.equipment)} / ${m.budgetCredits}`;
  }
  $('stat-budget').textContent = `${budgetText} cr`;
  $('stat-budget').classList.toggle('over', budgetText.startsWith('-'));
  $('stat-time').textContent = run ? `t = ${run.time}` : 't = –';
  $('stat-deadline').textContent = `t ≤ ${m.deadlineTicks}`;
  $('stat-deadline').classList.toggle('over', Boolean(run?.deadlinePassed));
  const primary = run?.orders.find((order) => order.primary);
  $('stat-order').textContent = primary ? `${primary.completed} / ${primary.requested}` : `0 / ${m.quantity}`;
}

function renderBriefing() {
  const m = state.mission;
  fill($('briefing'), ...m.briefing.map((line) => h('p', {}, line)));
  fill($('hints'), ...m.hints.slice(0, state.hintsShown).map((hint) => h('li', {}, hint)));
  $('hint-button').hidden = state.hintsShown >= m.hints.length;
}

function renderPalette() {
  const m = state.mission;
  const held = {};
  for (const item of editor().present.installations) held[item.itemId] = (held[item.itemId] ?? 0) + 1;
  fill($('palette'), ...m.equipment.map((entry) => {
    const count = held[entry.itemId] ?? 0;
    const full = entry.limit !== null && count >= entry.limit;
    return h('li', {}, h('button', {
      type: 'button', 'data-testid': `buy-${entry.itemId.replace('equipment.', '')}`,
      'aria-pressed': String(state.paletteItem === entry.itemId),
      disabled: state.mode !== 'edit' || full ? '' : null,
      title: `${entry.name}: concurrency ${entry.concurrency}, runs ${entry.steps.join(' + ')}`,
      onclick: () => {
        state.paletteItem = state.paletteItem === entry.itemId ? null : entry.itemId;
        state.selected = null;
        render();
      },
    }, glyphIcon(entry.glyph),
    h('span', {}, entry.name, h('span', { class: 'meta' },
      `${entry.steps.join(' + ')} · concurrency ${entry.concurrency} · ${count}${entry.limit !== null ? '/' + entry.limit : ''}`)),
    h('span', { class: 'cost' }, `${entry.costCredits} cr`)));
  }));
}

function renderFloorView() {
  const resources = state.run ? new Map(state.run.resources.map((resource) => [resource.id, resource])) : null;
  renderFloor($('floor'), {
    floor: state.mission.floor,
    installations: editor().present.installations,
    equipment: equipmentMap(),
    selected: state.selected,
    cursor: state.cursor,
    resources,
  });
}

function renderInspector() {
  const box = $('inspector');
  const item = editor().present.installations.find((candidate) => candidate.number === state.selected);
  if (!item) {
    fill(box, h('p', { class: 'note' }, state.paletteItem
      ? 'Click an empty floor cell to place the selected machine.'
      : 'Select a machine on the floor.'));
    return;
  }
  const template = equipmentMap().get(item.itemId);
  const live = state.run?.resources.find((resource) => resource.id === String(item.number));
  fill(box, 
    h('p', {}, h('b', {}, `${template.name} #${item.number}`)),
    h('p', { class: 'note' }, `Runs: ${template.steps.join(', ')} · concurrency ${template.concurrency} · ${template.costCredits} cr`),
    h('p', { class: 'note' }, `Installation number ${item.number} is this machine's identity in the engine. Cell (${item.x}, ${item.y}) is cosmetic.`),
    live ? h('p', {}, `Engine state: ${live.state}, ${live.activeJobs.length}/${live.concurrency} working, ${live.queueDepth} queued`) : null,
    state.mode === 'edit' ? h('button', { type: 'button', 'data-testid': 'sell', onclick: sellSelected }, 'Sell machine') : null);
}

function renderReports() {
  const box = $('reports');
  const design = state.design;
  if (!design) {
    fill(box, h('p', { class: 'note' }, 'Press Check design. Two independent authorities answer: the contract (budget, limits, floor) and the Factory model (can it be published?).'));
    return;
  }
  const economics = design.economics;
  const economicsLine = economics.ok
    ? `Cost ${economics.committedCost} of ${economics.startingBudget} cr · ${economics.remaining} left`
    : `Cost cannot be computed: ${economics.message}`;
  const admission = design.admissibility;
  const publication = design.publication;
  fill(box, 
    h('div', { class: `report ${admission.admitted && economics.ok ? 'ok' : 'bad'}`, 'data-testid': 'report-contract' },
      h('h3', {}, `Contract: ${admission.admitted ? 'admissible' : 'not admissible'}`),
      h('p', {}, economicsLine),
      admission.issues.length ? h('ul', {}, admission.issues.map((issue) => h('li', {}, issue.message))) : null),
    h('div', { class: `report ${publication.valid ? 'ok' : 'bad'}`, 'data-testid': 'report-factory' },
      h('h3', {}, `Factory model: ${publication.valid ? 'can be published' : 'cannot be published'}`),
      publication.valid ? h('p', { class: 'fp' }, publication.fingerprint) : null,
      publication.issues.length ? h('ul', {}, publication.issues.map((issue) => h('li', {}, `${issue.field}: ${issue.message}`))) : null),
    h('p', { class: 'note' }, design.runnable
      ? 'Both pass: you can publish and start.'
      : 'Both must pass before a run can start. They are independent checks.'));
}

function renderResult() {
  const card = $('result-card');
  const run = state.run;
  if (!run || state.mode !== 'run' || run.status === 'ACTIVE') {
    card.hidden = true;
    return;
  }
  card.hidden = false;
  const box = $('result');
  if (run.status === 'COMPLETED') {
    const evaluation = run.evaluation;
    const win = evaluation.successful;
    const rows = [
      ['Shipped at tick', run.orders.find((order) => order.primary).completedAt],
      ['Deadline margin (ticks)', evaluation.deadlineMargin],
      ['Unused budget (cr)', evaluation.unusedBudget],
      ['Reference score', evaluation.score],
    ];
    const comparison = run.comparison;
    fill(box, 
      h('p', { class: `verdict ${win ? 'win' : 'loss'}`, 'data-testid': 'verdict' },
        win ? 'Contract fulfilled' : 'Contract failed'),
      !win ? h('ul', {}, evaluation.issues.map((issue) => h('li', { 'data-testid': 'issue' }, `${issue.message} (${issue.code})`))) : null,
      h('table', {}, rows.map(([label, value]) => h('tr', {}, h('th', {}, label), h('td', { 'data-testid': `result-${label.split(' ')[0].toLowerCase()}` }, fmt(value))))),
      h('p', { class: 'medal-note' }, `Score is the official ${evaluation.policy} evaluation of the engine's result: 1 + deadline margin + unused budget, or 0 when the contract fails. Faster is not automatically a higher score.`),
      comparison ? h('p', { 'data-testid': 'comparison' },
        comparison.comparable
          ? `Compared with the previous attempt: ${comparison.winner === 'FIRST' ? 'the earlier attempt scored higher' : comparison.winner === 'SECOND' ? 'this attempt scored higher' : 'tie'} (score change ${comparison.scoreDelta}, cost change ${comparison.constructionCostDelta} cr).`
          : `Attempts are not comparable (${comparison.reasons.join(', ')}).`) : null,
      h('p', { class: 'note' }, `Attempts this session: ${run.attempts.length}. Run ${run.runId.slice(0, 8)} · model ${run.modelFingerprint.slice(-12)}`));
    if (state.announcedRun !== run.runId) {
      state.announcedRun = run.runId;
      banner(win ? 'ok' : 'warn', win ? 'The order shipped and the contract is fulfilled.' : 'The order shipped, but the contract failed. Edit the design and retry.');
    }
    return;
  }
  const messages = {
    BLOCKED: 'The engine has no pending work, but the order is not complete. This is not a success.',
    REJECTED: 'The engine rejected the workload; nothing changed.',
    FAULTED: 'The engine faulted after changing state. The run cannot continue.',
  };
  fill(box, 
    h('p', { class: 'verdict neutral', 'data-testid': 'verdict' }, run.status),
    h('p', {}, messages[run.status] ?? run.status),
    h('p', { class: 'note' }, run.detail));
}

function renderWork() {
  const box = $('work');
  const run = state.run;
  if (!run) {
    fill(box, h('p', { class: 'note' }, 'Publish a design to start a run.'));
    return;
  }
  const primary = run.orders.find((order) => order.primary);
  const percent = Number(primary.completed) / Number(primary.requested) * 100;
  const byStatus = { Queued: 0, InProgress: 0, Completed: 0 };
  for (const job of run.jobs) byStatus[job.status] += 1;
  const stepNames = state.mission.steps.map((step) => step.name);
  const perStep = stepNames.map((_, index) => run.jobs.filter((job) => job.status !== 'Completed' && job.step === index).length);
  fill(box, 
    h('p', {}, `Order ${primary.id}: ${primary.completed} of ${primary.requested} units complete`),
    h('div', { class: 'progress', role: 'progressbar', 'aria-valuenow': String(Math.round(percent)), 'aria-valuemin': '0', 'aria-valuemax': '100' }, h('div', { style: `width:${percent}%` })),
    h('table', {},
      h('tr', {}, h('th', {}, 'Jobs'), h('td', {}, `${byStatus.InProgress} working · ${byStatus.Queued} waiting · ${byStatus.Completed} done`)),
      h('tr', {}, h('th', {}, 'By step'), h('td', {}, stepNames.map((name, index) => `${name} ${perStep[index]}`).join(' · '))),
      h('tr', {}, h('th', {}, 'Shared backlog'), h('td', { 'data-testid': 'pending' }, `${run.pending.length} waiting for any of several machines`)),
      h('tr', {}, h('th', {}, 'Machine queues'), h('td', {}, run.resources.map((resource) => `#${resource.id}: ${resource.queueDepth}`).join(' · '))),
      h('tr', {}, h('th', {}, 'Status'), h('td', { 'data-testid': 'status' }, run.status)),
      h('tr', {}, h('th', {}, 'Engine KPIs'), h('td', {}, `backlog ${run.performance.backlog} · orders done ${run.performance.completedOrders} · avg lead ${typeof run.performance.averageLeadTime === 'number' ? run.performance.averageLeadTime.toFixed(1) : run.performance.averageLeadTime} ticks`))));
}

function renderEvents() {
  $('event-status').textContent = state.eventNote || (state.run ? `Supported engine events for run ${state.run.runId.slice(0, 8)}, newest first.` : '');
  fill($('events'), ...state.eventLines.slice(0, 120).map((line) =>
    h('li', { class: line.type === 'ORDER_COMPLETED' ? 'done' : '' }, h('b', {}, `t=${line.time} #${line.sequence}`), ' ', line.text)));
}

// ---------- start-up ----------

async function init() {
  $('floor').addEventListener('click', floorClick);
  $('floor').addEventListener('keydown', floorKey);
  document.addEventListener('keydown', onGlobalKey);
  $('undo').addEventListener('click', () => { setEditor(undo(editor())); invalidateDesign(); render(); });
  $('redo').addEventListener('click', () => { setEditor(redo(editor())); invalidateDesign(); render(); });
  $('check').addEventListener('click', checkDesign);
  $('publish').addEventListener('click', publishAndStart);
  $('step').addEventListener('click', stepOnce);
  $('play').addEventListener('click', togglePlay);
  $('reset').addEventListener('click', retry);
  $('edit').addEventListener('click', editDesign);
  $('hint-button').addEventListener('click', () => { state.hintsShown += 1; render(); });
  try {
    state.missions = (await api.missions()).missions;
    await loadMission(state.missions[0].id);
  } catch (error) {
    reportError(error);
  }
}

init();
