import { appendFileSync, existsSync, readFileSync, readdirSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const repositoryRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..');
const productRoot = path.join(repositoryRoot, 'product');
const reportSuffix = path.join('build', 'reports', 'jacoco', 'test', 'jacocoTestReport.xml');
const reports = [];

function discoverModules(directory) {
  for (const entry of readdirSync(directory, { withFileTypes: true })) {
    if (!entry.isDirectory() || entry.name.startsWith('.') || entry.name === 'src' || entry.name === 'gradle') {
      continue;
    }
    if (entry.name === 'build') {
      const report = path.join(directory, reportSuffix);
      if (existsSync(report)) reports.push(report);
      continue;
    }
    discoverModules(path.join(directory, entry.name));
  }
}

function ratio(covered, missed) {
  if (covered + missed === 0) return 'n/a';
  return `${(100 * covered / (covered + missed)).toFixed(1)}%`;
}

function counters(report) {
  const xml = readFileSync(report, 'utf8');
  const summary = xml.slice(xml.lastIndexOf('</package>') + '</package>'.length);
  const values = new Map();
  for (const match of summary.matchAll(/<counter type="(LINE|BRANCH)" missed="(\d+)" covered="(\d+)"\s*\/>/g)) {
    values.set(match[1], { missed: Number(match[2]), covered: Number(match[3]) });
  }
  if (!values.has('LINE')) return null; // Test-only modules have no main-source denominator.
  if (!values.has('BRANCH')) {
    throw new Error(`Missing report-level LINE or BRANCH counter: ${report}`);
  }
  return values;
}

discoverModules(productRoot);
if (reports.length === 0) throw new Error('No Java JaCoCo reports found');
reports.sort();
const lines = [
  '### Combined Java coverage by module',
  '',
  '| Module | LINE | BRANCH |',
  '| --- | ---: | ---: |',
];
for (const report of reports) {
  const module = path.relative(productRoot, report).split(path.sep).at(-6);
  const counts = counters(report);
  if (counts === null) continue;
  const line = counts.get('LINE');
  const branch = counts.get('BRANCH');
  lines.push(`| ${module} | ${ratio(line.covered, line.missed)} | ${ratio(branch.covered, branch.missed)} |`);
}
lines.push('', 'LINE has a 90% per-module gate. BRANCH is a review signal, not a gate.', '');
const output = lines.join('\n');
process.stdout.write(`${output}\n`);
if (process.env.GITHUB_STEP_SUMMARY) appendFileSync(process.env.GITHUB_STEP_SUMMARY, `${output}\n`);
