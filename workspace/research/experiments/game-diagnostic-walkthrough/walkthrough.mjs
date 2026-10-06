#!/usr/bin/env node
// Second owner walkthrough for the factory-design game diagnostic-evidence investigation.
// Research custody only: a terminal presentation shell over a pre-generated scenario pack. It computes
// nothing about factories -- every line it shows was produced from real Engine runs by the research
// experiment (GameWalkthroughPack) -- and it reads the answer key only when the player asks for the
// reveal at the end.
//
// Usage (from the repository root):
//   node workspace/research/experiments/game-diagnostic-walkthrough/walkthrough.mjs
//   ... --restart   start over (the previous answers file is kept as a .bak)
//   ... --reveal    show the key next to a finished session's answers
//   ... --answers=<path>  use another answers file (default logs/game-walkthrough-2-answers.json)

import { createHash } from 'node:crypto';
import { existsSync, mkdirSync, readFileSync, renameSync, writeFileSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { createInterface } from 'node:readline';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..', '..', '..', '..');
const packDir = join(root, 'workspace', 'research', 'investigations', 'game-diagnostic-walkthrough-2');
const packPath = join(packDir, 'pack.json');
const keyPath = join(packDir, 'key.json');
const args = new Set(process.argv.slice(2));
const answersArg = [...args].find((a) => a.startsWith('--answers='));
const answersPath = answersArg
  ? resolve(answersArg.slice('--answers='.length))
  : join(root, 'logs', 'game-walkthrough-2-answers.json');

const WIDTH = 100;
const packText = readFileSync(packPath, 'utf8');
const pack = JSON.parse(packText);
const packSha = createHash('sha256').update(packText).digest('hex');
const rl = createInterface({ input: process.stdin, output: process.stdout });

class Quit extends Error {}

// Lines are queued as they arrive, so typed and piped input behave the same; end of input saves and quits.
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
  const line = pending.length > 0 ? pending.shift()
    : inputClosed ? null
      : await new Promise((resolveLine) => waiting.push(resolveLine));
  if (line === null) throw new Quit();
  return line;
}

// ------------------------------------------------------------------ answers file

function freshSession() {
  return { pack: pack.pack, packSha256: packSha, startedAt: new Date().toISOString(), answers: {}, tries: {},
    comments: {}, views: {}, completed: [] };
}

function loadSession() {
  if (args.has('--restart') && existsSync(answersPath)) {
    renameSync(answersPath, answersPath + '.' + Date.now() + '.bak');
  }
  if (!existsSync(answersPath)) {
    return freshSession();
  }
  const session = JSON.parse(readFileSync(answersPath, 'utf8'));
  if (session.packSha256 !== packSha) {
    console.log('The saved answers belong to a different scenario pack. Run again with --restart to start over.');
    process.exit(1);
  }
  return session;
}

function save(session) {
  mkdirSync(dirname(answersPath), { recursive: true });
  writeFileSync(answersPath, JSON.stringify(session, null, 2) + '\n');
}

// ------------------------------------------------------------------ output

function wrap(text, indent) {
  const words = text.split(' ');
  const lines = [];
  let line = '';
  for (const word of words) {
    if (line.length > 0 && indent.length + line.length + 1 + word.length > WIDTH) {
      lines.push(line);
      line = word;
    } else {
      line = line.length === 0 ? word : line + ' ' + word;
    }
  }
  lines.push(line);
  return lines.map((l, i) => (i === 0 ? indent : indent + '  ') + l).join('\n');
}

function say(text = '', indent = '') {
  console.log(text === '' ? '' : wrap(text, indent));
}

function rule(char = '-') {
  console.log(char.repeat(WIDTH));
}

const SECTIONS = [
  ['snapshot', 'Snapshot'],
  ['result', 'Result'],
  ['cannot-tell', 'What this run cannot tell you'],
];

function showFactory(scenario, position) {
  console.log();
  rule('=');
  say(`${scenario.title}   (${position} of ${pack.scenarios.length})`);
  rule();
  say('Design');
  for (const line of scenario.design) say(line, '  ');
  if (scenario.kind === 'comparison') {
    say('Second design');
    for (const line of scenario.secondDesign) say(line, '  ');
    say('Comparison');
    for (const s of scenario.statements) say('- ' + s.text, '  ');
  } else {
    for (const [section, heading] of SECTIONS) {
      const lines = scenario.statements.filter((s) => s.section === section);
      if (lines.length === 0) continue;
      say(heading);
      for (const s of lines) say('- ' + s.text, '  ');
    }
    if (scenario.statements.some((s) => s.section === 'more')) {
      say("(Type 'more' for each machine's work times.)", '  ');
    }
  }
  say("(At any prompt: '?' help, 'show' to see this again, 'evidence' for where each line comes from, 'q' to save and quit.)");
}

function showMore(scenario) {
  const lines = scenario.statements.filter((s) => s.section === 'more');
  say('Work times');
  if (lines.length === 0) say('- Not available for this item.', '  ');
  for (const s of lines) say('- ' + s.text, '  ');
}

function showEvidence(scenario) {
  say('Where each line comes from');
  for (const s of scenario.statements) {
    const how = s.kind === 'REFUSAL' ? 'states what the evidence does not show'
      : s.method ? `derived by "${s.method}"` : 'read directly';
    say(`- ${s.text}`, '  ');
    say(`${how}; ${s.evidence || 'no further evidence needed'} [${s.kind}]`, '      ');
  }
}

function showHelp() {
  say('Glossary');
  for (const g of pack.glossary) say('- ' + g, '  ');
  say("Commands: a number answers; 'show' repeats the factory; 'more' shows work times; 'evidence' shows sources;"
    + " 'q' saves and quits (run the same command later to continue).");
}

// ------------------------------------------------------------------ input

async function prompt(session, scenario, position, label, accept) {
  for (;;) {
    const raw = (await question(label)).trim();
    const lower = raw.toLowerCase();
    const views = (session.views[scenario.id] ??= {});
    if (lower === 'q') throw new Quit();
    if (lower === '?') { views.help = (views.help ?? 0) + 1; showHelp(); continue; }
    if (lower === 'show') { views.show = (views.show ?? 0) + 1; showFactory(scenario, position); continue; }
    if (lower === 'more') { views.more = (views.more ?? 0) + 1; showMore(scenario); continue; }
    if (lower === 'evidence') { views.evidence = (views.evidence ?? 0) + 1; showEvidence(scenario); continue; }
    const value = accept(raw);
    if (value !== undefined) return value;
  }
}

async function choose(session, scenario, position, question) {
  console.log();
  say(`${question.id}. ${question.text}`);
  for (const o of question.options) say(`${o.id}) ${o.text}`, '  ');
  return prompt(session, scenario, position, 'Your answer (number): ', (raw) => {
    const option = question.options.find((o) => o.id === raw);
    if (!option) say('Please type one of the numbers shown, or ? for help.');
    return option;
  });
}

async function ask(session, scenario, position, question) {
  if (session.answers[question.id]) return;
  const option = await choose(session, scenario, position, question);
  session.answers[question.id] = { option: option.id, text: option.text, at: new Date().toISOString() };
  save(session);
}

async function comment(session, scenario, position) {
  if (session.comments[scenario.id] !== undefined) return;
  console.log();
  const text = await prompt(session, scenario, position,
    'Anything unclear or misleading here? (type a comment, or press Enter to skip): ', (raw) => raw);
  session.comments[scenario.id] = text;
  save(session);
}

// ------------------------------------------------------------------ scenarios

async function tryOnce(session, scenario, position) {
  if (!scenario.tries || scenario.tries.length === 0) return;
  let chosen = scenario.tries.find((t) => t.id === session.tries[scenario.id]);
  if (!chosen) {
    console.log();
    say('Your try: change one thing and run the same order again. You get one try for this factory.');
    scenario.tries.forEach((t, i) => say(`${i + 1}) ${t.label}`, '  '));
    chosen = await prompt(session, scenario, position, 'Choose (number): ', (raw) => {
      const t = scenario.tries[Number(raw) - 1];
      if (!t) say('Please type one of the numbers shown.');
      return t;
    });
    session.tries[scenario.id] = chosen.id;
    save(session);
  }
  console.log();
  say(`Try: ${chosen.label}`);
  say(chosen.design[chosen.design.length - 1], '  ');
  for (const s of chosen.statements) say('- ' + s.text, '  ');
}

async function tutorial(session, scenario, position) {
  console.log();
  rule('=');
  say('How this works');
  rule();
  for (const line of scenario.intro) say(line);
  showFactory(scenario, position);
  const practice = scenario.questions[0];
  await ask(session, scenario, position, practice);
  const given = session.answers[practice.id].option;
  say(given === scenario.practiceAnswer ? 'Right.' : 'Not quite.');
  say(scenario.practiceExplanation);
  const demo = scenario.tries[0];
  console.log();
  say(`Example try: ${demo.label}`);
  say(demo.design[demo.design.length - 1], '  ');
  for (const s of demo.statements) say('- ' + s.text, '  ');
  say(scenario.tryExplanation);
  await prompt(session, scenario, position, 'Press Enter to start the scored factories. ', () => true);
}

async function play(session) {
  say(`Scenario pack ${pack.pack} (${packSha.slice(0, 12)}), contract ${pack.contract}.`);
  say('Please do not open key.json or the walkthrough oracle note before you finish. Answers are saved as you go.');
  for (let i = 0; i < pack.scenarios.length; i++) {
    const scenario = pack.scenarios[i];
    if (session.completed.includes(scenario.id)) continue;
    const position = i + 1;
    if (scenario.kind === 'tutorial') {
      await tutorial(session, scenario, position);
    } else {
      showFactory(scenario, position);
      for (const question of scenario.questions) await ask(session, scenario, position, question);
      await tryOnce(session, scenario, position);
      if (scenario.followUp) await ask(session, scenario, position, scenario.followUp);
      await comment(session, scenario, position);
    }
    session.completed.push(scenario.id);
    save(session);
  }
  if (session.overall === undefined) {
    console.log();
    rule('=');
    const text = (await question('All done. Overall, was this clear? Anything to change? (Enter to skip): ')).trim();
    session.overall = text;
    session.finishedAt = new Date().toISOString();
    save(session);
  }
}

// ------------------------------------------------------------------ reveal

function optionText(questionId, optionId) {
  for (const s of pack.scenarios) {
    for (const q of [...(s.questions ?? []), ...(s.followUp ? [s.followUp] : [])]) {
      if (q.id === questionId) return q.options.find((o) => o.id === optionId)?.text ?? optionId;
    }
  }
  return optionId;
}

function reveal(session) {
  const key = JSON.parse(readFileSync(keyPath, 'utf8'));
  let matches = 0;
  let total = 0;
  console.log();
  rule('=');
  say('Key');
  rule();
  for (const [questionId, entry] of Object.entries(key)) {
    const given = session.answers[questionId];
    const scenarioId = questionId.replace(/[0-9a-z]+$/, '');
    const keyed = entry.answer ?? entry.byTry?.[session.tries[scenarioId]];
    const same = given && keyed === given.option;
    total += 1;
    if (same) matches += 1;
    say(`${questionId}: ${same ? 'matches' : 'differs'}`);
    say(`yours: ${given ? given.text : '(not answered)'}`, '  ');
    say(`key:   ${keyed ? optionText(questionId, keyed) : '(depends on the try)'}`, '  ');
    say(entry.explanation, '  ');
  }
  rule();
  say(`${matches} of ${total} answers match the key. Matching is mechanical; the walkthrough record assesses them.`);
  say(`Your answers are saved in ${answersPath}.`);
  session.revealedAt = session.revealedAt ?? new Date().toISOString();
  save(session);
}

// ------------------------------------------------------------------ main

const session = loadSession();
try {
  if (args.has('--reveal')) {
    if (!session.finishedAt) {
      say('Finish the session first (run without --reveal).');
    } else {
      reveal(session);
    }
  } else {
    await play(session);
    const answer = (await question('Show the key next to your answers now? (y/n): ')).trim().toLowerCase();
    if (answer === 'y' || answer === 'yes') {
      reveal(session);
    } else {
      say('Run again with --reveal when you want to see the key.');
    }
  }
} catch (error) {
  if (!(error instanceof Quit)) throw error;
  save(session);
  say(`Saved. Run the same command again to continue. (${answersPath})`);
} finally {
  rl.close();
}
