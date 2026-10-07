#!/usr/bin/env node
// Bakery inspection missions (starvation and utilization) for the factory-design game
// diagnostic-evidence investigation. Research custody only: a terminal viewer over a pre-generated
// data pack. Every fact it shows was recorded from real Engine runs by the research experiment
// (BakeryInspectionPack); the viewer only looks facts up, compresses unchanged ticks into ranges,
// and subtracts pre-computed running totals for a chosen period. It reads a mission's official answer
// only after the player has answered that mission.
//
// Usage (from anywhere):
//   node <repo>/workspace/research/experiments/game-diagnostic-inspection/inspect.mjs
//   ... --restart          start over (the previous answers file is kept as a .bak)
//   ... --answers=<path>   use another answers file (default <repo>/logs/game-inspection-answers.json)

import { createHash } from 'node:crypto';
import { existsSync, mkdirSync, readFileSync, renameSync, writeFileSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { createInterface } from 'node:readline';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..', '..', '..', '..');
const dataDir = join(root, 'workspace', 'research', 'investigations', 'game-inspection');
const packText = readFileSync(join(dataDir, 'inspection.json'), 'utf8');
const pack = JSON.parse(packText);
const packSha = createHash('sha256').update(packText).digest('hex');
const args = process.argv.slice(2);
const answersArg = args.find((a) => a.startsWith('--answers='));
const answersPath = answersArg ? resolve(answersArg.slice('--answers='.length)) : join(root, 'logs', 'game-inspection-answers.json');
const WIDTH = 100;

// ------------------------------------------------------------------ input (typed or piped alike)

class Quit extends Error {}
const rl = createInterface({ input: process.stdin, output: process.stdout });
const pending = [];
const waiting = [];
let inputClosed = false;
rl.on('line', (line) => (waiting.length > 0 ? waiting.shift()(line) : pending.push(line)));
rl.on('close', () => {
  inputClosed = true;
  while (waiting.length > 0) waiting.shift()(null);
});
async function question(label) {
  process.stdout.write(label);
  const line = pending.length > 0 ? pending.shift() : inputClosed ? null : await new Promise((r) => waiting.push(r));
  if (line === null) throw new Quit();
  return line.trim();
}

// ------------------------------------------------------------------ output

function wrap(text, indent = '') {
  const lines = [];
  let line = '';
  for (const word of text.split(' ')) {
    if (line.length > 0 && indent.length + line.length + 1 + word.length > WIDTH) {
      lines.push(line);
      line = word;
    } else {
      line = line.length === 0 ? word : `${line} ${word}`;
    }
  }
  lines.push(line);
  return lines.map((l, i) => (i === 0 ? indent : `${indent}  `) + l).join('\n');
}
const say = (text = '', indent = '') => console.log(text === '' ? '' : wrap(text, indent));
const rule = (c = '-') => console.log(c.repeat(WIDTH));
const pct = (num, den) => (den === 0 ? '-' : `${Math.round((100 * num) / den)}%`);
const list = (xs) => (xs.length <= 1 ? xs.join('') : `${xs.slice(0, -1).join(', ')} and ${xs[xs.length - 1]}`);
const loaves = (xs) => (xs.length === 1 ? `loaf ${xs[0]}` : `loaves ${list(xs.map(String))}`);

// ------------------------------------------------------------------ session

function freshSession() {
  return { pack: pack.pack, packSha256: packSha, startedAt: new Date().toISOString(), missions: {} };
}
function loadSession() {
  if (args.includes('--restart') && existsSync(answersPath)) renameSync(answersPath, `${answersPath}.${Date.now()}.bak`);
  if (!existsSync(answersPath)) return freshSession();
  const session = JSON.parse(readFileSync(answersPath, 'utf8'));
  if (session.packSha256 !== packSha) {
    console.log('The saved answers belong to a different data pack. Run again with --restart to start over.');
    process.exit(1);
  }
  return session;
}
function save(session) {
  mkdirSync(dirname(answersPath), { recursive: true });
  writeFileSync(answersPath, `${JSON.stringify(session, null, 2)}\n`);
}

// ------------------------------------------------------------------ views

const state = { scenario: null, design: null, mission: null };

function designsOf(scenario) {
  return scenario.designs;
}
function findDesign(id) {
  return designsOf(state.scenario).find((d) => d.id.toLowerCase() === id.toLowerCase());
}
function findMachine(design, name) {
  return design.machines.find((m) => m.name.toLowerCase() === name.toLowerCase());
}

function showDesign(d) {
  say(`${d.label}`);
  say(`Steps: ${d.steps.map((s) => `${s.name} ${s.ticks}`).join(' -> ')} ticks per loaf.`, '  ');
  say(`Machines: ${d.machines.map((m) => `${m.name} (${m.steps.join('+')}${m.slots > 1 ? `, ${m.slots} slots` : ''})`).join(', ')}.`, '  ');
  say(`Order: ${d.loaves} loaves, all released at tick 0.`, '  ');
}

function machineLine(m) {
  const idleSlots = m.slots - m.working;
  if (m.slots === 1) {
    return m.working === 1 ? `working on ${loaves(m.loaves)}` : `idle (${m.idle})`;
  }
  const parts = [`${m.working} of ${m.slots} slots working${m.working > 0 ? ` (${loaves(m.loaves)})` : ''}`];
  if (idleSlots > 0) parts.push(`${idleSlots} idle (${m.idle})`);
  return parts.join(', ');
}

function showAt(d, tick) {
  const t = Math.max(0, Math.min(tick, d.finish));
  const snap = d.snapshots[t];
  if (tick > d.finish) say(`The order finished at tick ${d.finish}; nothing changes after that. Showing tick ${d.finish}.`);
  say(`Tick ${t}: ${snap.finished} of ${d.loaves} loaves finished.`);
  for (const m of snap.machines) {
    const steps = findMachine(d, m.name).steps.join('+');
    say(`${m.name} (${steps}): ${machineLine(m)}`, '  ');
  }
  if (snap.waiting.length === 0) {
    say('No loaf is waiting to start a step.', '  ');
  }
  for (const w of snap.waiting) {
    const where = w.on ? `on ${w.on}` : `(not assigned yet; ${list(w.eligible)} may take them)`;
    say(`Waiting to start ${w.step} ${where}: ${loaves(w.loaves)}.`, '  ');
  }
}

function showMachine(d, name) {
  const machine = findMachine(d, name);
  if (!machine) {
    say(`No machine called '${name}' here. Machines: ${d.machines.map((m) => m.name).join(', ')}.`);
    return;
  }
  say(`${machine.name} (${machine.steps.join('+')}${machine.slots > 1 ? `, ${machine.slots} slots` : ''}), from tick 0 to ${d.finish}:`);
  let from = 0;
  let current = null;
  const flush = (to) => {
    if (current !== null) say(`from tick ${from} to ${to}: ${current}`, '  ');
  };
  for (let t = 0; t < d.finish; t++) {
    const m = d.snapshots[t].machines.find((x) => x.name === machine.name);
    const label = machineLine(m);
    if (label !== current) {
      flush(t);
      from = t;
      current = label;
    }
  }
  flush(d.finish);
  showBreakdown(d, machine, 0, d.finish);
}

function totals(d, machine, from, to) {
  const c = d.cumulative[machine.name];
  return {
    working: c.working[to] - c.working[from],
    starved: c.starved[to] - c.starved[from],
    noWorkLeft: c.noWorkLeft[to] - c.noWorkLeft[from],
    available: machine.slots * (to - from),
  };
}

function showBreakdown(d, machine, from, to) {
  const t = totals(d, machine, from, to);
  const unit = machine.slots > 1 ? 'slot-ticks' : 'ticks';
  say(`From tick ${from} to ${to}: working ${t.working}, idle-starved ${t.starved}, idle-no-work-left ${t.noWorkLeft}`
    + ` of ${t.available} ${unit}; utilization ${pct(t.working, t.available)}.`, '  ');
}

function showUtil(d, from, to) {
  say(`Utilization from tick ${from} to ${to} (${to - from} ticks):`);
  const rows = [['Machine', 'Slots', 'Working', 'Starved', 'No work left', 'Available', 'Utilization']];
  for (const machine of d.machines) {
    const t = totals(d, machine, from, to);
    rows.push([machine.name, String(machine.slots), String(t.working), String(t.starved), String(t.noWorkLeft),
      String(t.available), pct(t.working, t.available)]);
  }
  const widths = rows[0].map((_, i) => Math.max(...rows.map((r) => r[i].length)));
  for (const r of rows) console.log(`  ${r.map((cell, i) => cell.padEnd(widths[i])).join('  ')}`);
  say('(Working, starved, no work left and available are counted in slot-ticks: one slot for one tick.)', '  ');
}

function showUnit(d, n) {
  const unit = d.units.find((u) => u.loaf === n);
  if (!unit) {
    say(`Loaves are numbered 1 to ${d.loaves}.`);
    return;
  }
  say(`Loaf ${n}:`);
  for (const s of unit.steps) {
    say(`${s.step}: ready at tick ${s.ready}, waited ${s.start - s.ready}, worked from tick ${s.start} to ${s.end} on ${s.machine}.`, '  ');
  }
  const last = unit.steps[unit.steps.length - 1];
  say(`Finished at tick ${last.end}.`, '  ');
}

function showVariants() {
  say(`Designs in this scenario (current: ${state.design.id}):`);
  for (const d of designsOf(state.scenario)) {
    say(`${d.id.padEnd(16)} ${d.label}${d.parent ? ` (variant of ${d.parent})` : ''}`, '  ');
  }
  say("Type 'switch <id>' to inspect one, or 'compare <id>' to see how a variant's finish time differs from its parent.", '  ');
}

function showCompare(id) {
  const d = findDesign(id);
  if (!d || !d.comparison) {
    say("Type 'variants' to see which variants can be compared.");
    return;
  }
  say(`${d.label} compared with ${d.parent}:`);
  for (const line of d.comparison) say(`- ${line}`, '  ');
}

function showHelp() {
  say('Commands');
  for (const [cmd, what] of [
    ['mission', 'show the current mission again'],
    ['answer', 'answer the current mission'],
    ['design', 'the bakery shown now: steps, machines, order'],
    ['result', 'when the order finished'],
    ['at <tick>', 'what every machine is doing at that tick, and which loaves wait'],
    ['machine <name>', "one machine's timeline and how its time splits"],
    ['util [<from> <to>]', 'utilization of every machine over a period (default: the whole run)'],
    ['loaf <n>', "one loaf's journey through the steps"],
    ['variants', 'other designs in this scenario, if any'],
    ['switch <id>', 'inspect another design of this scenario'],
    ['compare <id>', 'how a variant changed the finish time'],
    ['explain <term>', `meaning of a term (${Object.keys(pack.glossary).join(', ')})`],
    ['sources', 'how the facts are derived'],
    ['q', 'save and quit (run again later to continue)'],
  ]) say(`${cmd.padEnd(20)} ${what}`, '  ');
}

function showExplain(term) {
  const entry = pack.glossary[term.toLowerCase()];
  if (!entry) {
    say(`Terms: ${Object.keys(pack.glossary).join(', ')}.`);
    return;
  }
  say(`${term.toLowerCase()}: ${entry}`);
}

function showSources() {
  say('Every fact comes from real recorded runs of the simulation, observed at every tick. Derived facts:');
  for (const d of pack.definitions) say(`- ${d.name}: ${d.text}`, '  ');
}

// ------------------------------------------------------------------ missions

function showMission(m) {
  console.log();
  rule();
  say(`Mission ${m.number} of ${pack.missions.length}  [${m.type}]   showing: ${state.design.label}`);
  rule();
  say(m.prompt);
  say('You will be asked:');
  for (const p of m.parts) {
    say(`- ${p.ask}`, '  ');
    say(`  Answer format: ${p.format.hint}${p.format.options ? ` Options: ${p.format.options.join('; ')}` : ''}`, '  ');
  }
  say("Explore with the commands ('help' lists them). When you know, type 'answer'.");
}

function normalize(text) {
  return text.toLowerCase().replace(/[^a-z0-9%/.\s]/g, ' ').replace(/\s+/g, ' ').trim();
}

function parsePercent(text) {
  const t = normalize(text);
  const ratio = t.match(/^(\d+(?:\.\d+)?)\s*(?:of|\/)\s*(\d+(?:\.\d+)?)$/);
  if (ratio) return (100 * Number(ratio[1])) / Number(ratio[2]);
  const p = t.match(/^(\d+(?:\.\d+)?)\s*%?$/);
  return p ? Number(p[1]) : null;
}

function check(key, given) {
  const g = normalize(given);
  switch (key.kind) {
    case 'machine':
      return g === normalize(key.value);
    case 'choice': {
      const value = normalize(key.value);
      return g === value || g === `${value})` || g.startsWith(`${value} `);
    }
    case 'integer':
      return /^\d+$/.test(g) && Number(g) === Number(key.value);
    case 'percent': {
      const p = parsePercent(given);
      return p !== null && Math.abs(p - (100 * key.working) / key.available) <= key.tolerancePoints;
    }
    case 'machines': {
      const names = new Set(g.split(/\s*(?:,|\band\b|&)\s*/).filter(Boolean));
      const expected = new Set(key.value.map(normalize));
      return names.size === expected.size && [...expected].every((n) => names.has(n));
    }
    default:
      return null;
  }
}

async function answerMission(session, m) {
  const record = (session.missions[m.number] ??= { parts: {}, commands: [] });
  for (const p of m.parts) {
    if (record.parts[p.id]) continue;
    let given = '';
    while (given === '') {
      say(`${p.ask}`);
      say(`Answer format: ${p.format.hint}${p.format.options ? ` Options: ${p.format.options.join('; ')}` : ''}`, '  ');
      given = await question('> ');
    }
    record.parts[p.id] = { given, at: new Date().toISOString() };
    save(session);
  }
  const key = JSON.parse(readFileSync(join(dataDir, 'missions-key.json'), 'utf8'))[String(m.number)];
  console.log();
  say(`Official answer: ${key.official}`);
  for (const p of m.parts) {
    const result = check(key.parts[p.id], record.parts[p.id].given);
    record.parts[p.id].match = result;
    const verdict = result === null ? 'recorded' : result ? 'matches' : 'differs';
    say(`${p.ask} -> you: "${record.parts[p.id].given}" (${verdict})`, '  ');
  }
  say(`Why: ${key.explanation}`, '  ');
  say(`How to find it: ${key.howToFind}`, '  ');
  say(`What this teaches: ${m.objective}`, '  ');
  record.answeredAt = new Date().toISOString();
  save(session);
  console.log();
  record.comment = await question('Any comment on this mission? (Enter to skip) ');
  record.done = true;
  save(session);
}

async function runMission(session, m) {
  const record = (session.missions[m.number] ??= { parts: {}, commands: [] });
  state.mission = m;
  showMission(m);
  for (;;) {
    const raw = await question(`[mission ${m.number}] > `);
    if (raw === '') continue;
    const [command, ...rest] = raw.split(/\s+/);
    const argText = rest.join(' ');
    const cmd = command.toLowerCase();
    record.commands.push(raw);
    save(session);
    const d = state.design;
    switch (cmd) {
      case 'q': case 'quit': throw new Quit();
      case 'help': case '?': showHelp(); break;
      case 'mission': showMission(m); break;
      case 'design': showDesign(d); break;
      case 'result': say(`${d.label}: the order of ${d.loaves} loaves finished at tick ${d.finish}.`); break;
      case 'at': {
        const t = Number(argText);
        if (argText === '' || !Number.isInteger(t) || t < 0) say("Type 'at' followed by a tick number, e.g. 'at 5'.");
        else showAt(d, t);
        break;
      }
      case 'machine': showMachine(d, argText); break;
      case 'util': {
        const nums = rest.map(Number);
        if (rest.length === 0) showUtil(d, 0, d.finish);
        else if (rest.length === 2 && nums.every(Number.isInteger) && nums[0] >= 0 && nums[0] < nums[1] && nums[1] <= d.finish) showUtil(d, nums[0], nums[1]);
        else say(`Type 'util' for the whole run, or 'util <from> <to>' with 0 <= from < to <= ${d.finish}.`);
        break;
      }
      case 'loaf': case 'unit': showUnit(d, Number(argText)); break;
      case 'variants': showVariants(); break;
      case 'switch': {
        const next = findDesign(argText);
        if (next) { state.design = next; say(`Now showing: ${next.label}.`); } else showVariants();
        break;
      }
      case 'compare': showCompare(argText); break;
      case 'explain': showExplain(argText); break;
      case 'glossary': showExplain(''); break;
      case 'sources': showSources(); break;
      case 'answer': await answerMission(session, m); return;
      default: say(`Unknown command '${command}'. Type 'help' for the list.`);
    }
    say(`(mission ${m.number}: 'answer' when ready, 'mission' to see it again)`);
  }
}

async function play(session) {
  say('Bakery inspection missions: starvation and utilization.');
  say(`Data pack ${pack.pack} (${packSha.slice(0, 12)}). Please do not open missions-key.json or the missions oracle note.`);
  say('Each mission states its goal and answer format. Explore with commands, then type "answer". You see the official'
    + ' answer, why, and how to find it, right after you answer. Progress is saved as you go; "q" quits.');
  let lastScenario = null;
  for (const m of pack.missions) {
    const scenario = pack.scenarios.find((s) => s.id === m.scenario);
    state.scenario = scenario;
    state.design = scenario.designs.find((d) => d.id === m.design);
    if (session.missions[m.number]?.done) {
      lastScenario = scenario.id;
      continue;
    }
    // A new scenario, or the first mission shown in this sitting (resuming), opens with its introduction.
    if (scenario.id !== lastScenario || !state.introShown) {
      state.introShown = true;
      console.log();
      rule('=');
      say(`Scenario ${scenario.id}: ${scenario.title}`);
      rule('=');
      for (const line of scenario.intro) say(line);
      lastScenario = scenario.id;
    }
    await runMission(session, m);
  }
  if (!session.finishedAt) {
    console.log();
    rule('=');
    const matched = Object.values(session.missions).flatMap((r) => Object.values(r.parts)).filter((p) => p.match === true).length;
    const checked = Object.values(session.missions).flatMap((r) => Object.values(r.parts)).filter((p) => p.match !== null && p.match !== undefined).length;
    say(`All ${pack.missions.length} missions done. ${matched} of ${checked} checked answers matched the official answer.`);
    session.overall = await question('Overall: what was clear, what was hard, what would you change? (Enter to skip) ');
    session.finishedAt = new Date().toISOString();
    save(session);
    say(`Saved in ${answersPath}.`);
  }
}

const session = loadSession();
try {
  await play(session);
} catch (error) {
  if (!(error instanceof Quit)) throw error;
  save(session);
  say(`Saved. Run the same command again to continue. (${answersPath})`);
} finally {
  rl.close();
}
