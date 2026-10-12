import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { checkRoleContracts, roles } from './check-agent-role-contracts.mjs';

function fixture(run) {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'arcogine-roles-'));
  const make = (name, content) => {
    const file = path.join(root, name);
    fs.mkdirSync(path.dirname(file), { recursive: true });
    fs.writeFileSync(file, content);
  };
  try {
    make('AGENTS.md', roles.map(r => 'docs/development/agent-roles/' + r + '.md').join('\n'));
    for (const role of roles) make('docs/development/agent-roles/' + role + '.md', '# ' + role + '\n\nRole procedure.\n');
    run({ root, make });
  } finally {
    fs.rmSync(root, { recursive: true, force: true });
  }
}

const adapter = (role, body = 'Read [the procedure](../../docs/development/agent-roles/' + role + '.md) and [AGENTS.md](../../AGENTS.md).') =>
  '---\nname: ' + role + '\ntarget: github-copilot\n---\n\n' + body + '\n';

test('accepts all canonical provider-neutral roles without adapters', () => fixture(({ root }) => {
  assert.deepEqual(checkRoleContracts(root), []);
}));

test('accepts a thin explicit Copilot adapter', () => fixture(({ root, make }) => {
  make('.github/agents/researcher.agent.md', adapter('researcher'));
  assert.deepEqual(checkRoleContracts(root), []);
}));

test('rejects missing roles, provider metadata, and missing AGENTS routing', () => fixture(({ root, make }) => {
  fs.rmSync(path.join(root, 'docs/development/agent-roles/consistency.md'));
  make('docs/development/agent-roles/researcher.md', '---\ntarget: github-copilot\n---\n# Researcher\n');
  make('AGENTS.md', 'docs/development/agent-roles/researcher.md');
  const errors = checkRoleContracts(root).join('\n');
  assert.match(errors, /missing canonical role:.*consistency.md/);
  assert.match(errors, /provider\/runtime frontmatter.*researcher.md/);
  assert.match(errors, /does not route to.*work-planner.md/);
}));

test('rejects adapter that cannot load the canonical role or duplicates procedure', () => fixture(({ root, make }) => {
  make('.github/agents/researcher.agent.md', adapter('researcher', '## A competing role procedure\n\nUse separate rules.'));
  assert.match(checkRoleContracts(root).join('\n'), /adapter must load canonical role/);
  assert.match(checkRoleContracts(root).join('\n'), /adapter duplicates procedure text/);
}));
