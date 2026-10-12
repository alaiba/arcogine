#!/usr/bin/env node
// Keep canonical role procedures provider-neutral and Copilot profiles adapter-only.
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

export const roles = [
  'consistency', 'continuous-improvement', 'dependency-maintainer',
  'pr-reviewer', 'researcher', 'work-planner',
];

export function checkRoleContracts(root) {
  const problems = [];
  const read = (p) => fs.readFileSync(path.join(root, p), 'utf8');
  const canonicalDir = 'docs/development/agent-roles';
  const adapterDir = '.github/agents';
  const agents = read('AGENTS.md');

  for (const role of roles) {
    const target = canonicalDir + '/' + role + '.md';
    if (!fs.existsSync(path.join(root, target))) {
      problems.push('missing canonical role: ' + target);
      continue;
    }
    const source = read(target);
    if (/^---\r?\n/.test(source) || /^(?:target|tools|disable-model-invocation|user-invocable):/m.test(source)) {
      problems.push('provider/runtime frontmatter in canonical role: ' + target);
    }
    if (!agents.includes(target)) problems.push('AGENTS.md does not route to ' + target);
  }

  if (fs.existsSync(path.join(root, adapterDir))) {
    for (const filename of fs.readdirSync(path.join(root, adapterDir))) {
      if (!filename.endsWith('.agent.md')) continue;
      const slug = filename.slice(0, -'.agent.md'.length);
      const file = adapterDir + '/' + filename;
      const source = read(file);
      const frontmatter = source.match(/^---\r?\n([\s\S]*?)\r?\n---\r?\n/);
      if (!roles.includes(slug)) problems.push('unowned role adapter: ' + file);
      if (!frontmatter || !/^target: github-copilot$/m.test(frontmatter[1])) {
        problems.push('invalid GitHub Copilot adapter frontmatter: ' + file);
      }
      const body = frontmatter ? source.slice(frontmatter[0].length) : source;
      if (!body.includes('../../' + canonicalDir + '/' + slug + '.md') || !body.includes('../../AGENTS.md')) {
        problems.push('adapter must load canonical role and AGENTS.md: ' + file);
      }
      if (body.length > 850 || /\n## /.test(body)) problems.push('adapter duplicates procedure text: ' + file);
    }
  }
  return problems;
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..', '..');
  const problems = checkRoleContracts(root);
  if (problems.length) {
    for (const problem of problems) console.error('FAIL: ' + problem);
    process.exitCode = 1;
  } else {
    console.log('Canonical role and runtime-adapter boundaries passed.');
  }
}
