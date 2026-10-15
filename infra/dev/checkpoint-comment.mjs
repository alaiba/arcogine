#!/usr/bin/env node
// Publish an informational practice result to an existing checkpoint.
import fs from 'node:fs';
import { execFileSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
export const CHECKPOINT_TITLE = 'Continuous improvement checkpoint';
const PRACTICES = new Set(['assessment','consistency','retrospective']);
export function marker(practice,runId) {
  if (!PRACTICES.has(practice) || !/^[A-Za-z0-9][A-Za-z0-9_.:-]{0,127}$/.test(runId ?? '')) throw Error('Invalid checkpoint result identity');
  return `<!-- arcogine-checkpoint-result:v1:${practice}:${runId} -->`;
}
export function gh(method,endpoint,body) {
  const args=['api','-X',method,endpoint,'-H','Accept: application/vnd.github+json'];
  if (body !== undefined) args.push('--input','-');
  return JSON.parse(execFileSync('gh',args,{encoding:'utf8',input:body === undefined ? undefined : JSON.stringify(body),maxBuffer:8*1024*1024}));
}
export function pages(api,endpoint) {
  const all=[];
  for(let page=1;page<=100;page++) {
    const result=api('GET',`${endpoint}${endpoint.includes('?')?'&':'?'}per_page=100&page=${page}`);
    if(!Array.isArray(result)||result.length>100)throw Error('Incomplete GitHub pagination: '+endpoint);
    all.push(...result);
    if(result.length<100)return all;
  }
  throw Error('GitHub pagination limit exceeded: '+endpoint);
}
export function checkpoint(api,repo,number) {
  if (!/^[\w.-]+\/[\w.-]+$/.test(repo))throw Error('Invalid repository');
  if(number!==undefined) {
    if(!Number.isSafeInteger(number)||number<1)throw Error('Invalid checkpoint number');
    const issue=api('GET',`repos/${repo}/issues/${number}`);
    if(issue?.title!==CHECKPOINT_TITLE||issue.pull_request)throw Error('Not a checkpoint issue #'+number);
    return number;
  }
  const matches=pages(api,`repos/${repo}/issues?state=open`).filter(i=>i.title===CHECKPOINT_TITLE&&!i.pull_request);
  if(matches.length!==1)throw Error('No unambiguous open checkpoint; specify --checkpoint');
  return matches[0].number;
}
export function publish({api=gh,repo,number,practice,runId,body}) {
  const id=checkpoint(api,repo,number), key=marker(practice,runId);
  if(typeof body!=='string'||!body.trim()||body.length>30000||body.includes('arcogine-checkpoint-result:v1:'))throw Error('Invalid result body');
  const text=`${key}\n${body.trim()}\n`, endpoint=`repos/${repo}/issues/${id}/comments`;
  const matching=pages(api,endpoint).filter(c=>typeof c.body==='string'&&c.body.startsWith(key+'\n'));
  // GraphQL viewer resolves both human credentials and GitHub Actions tokens.
  // A public marker identifies a run, never the authority to replace a comment.
  const viewer=api('POST','graphql',{query:'query { viewer { login databaseId } }'})?.data?.viewer;
  if(!viewer?.login||!Number.isSafeInteger(viewer.databaseId)||viewer.databaseId<1)throw Error('Cannot verify authenticated checkpoint publisher');
  if(matching.some(c=>c.user?.id!==viewer.databaseId||c.user?.login!==viewer.login))throw Error('Checkpoint practice/run marker ownership collision');
  if(matching.length>1)throw Error('Duplicate practice/run results on checkpoint');
  if(matching.length===1) {
    if(matching[0].body===text)return {status:'unchanged',id};
    api('PATCH',`repos/${repo}/issues/comments/${matching[0].id}`,{body:text});
    return {status:'updated',id};
  }
  api('POST',endpoint,{body:text});
  return {status:'created',id};
}
if(process.argv[1]===fileURLToPath(import.meta.url)) {
  try {
    const args=process.argv.slice(2), options={};
    for(let i=0;i<args.length;i+=2){if(!/^--(repo|checkpoint|practice|run-id|body-file)$/.test(args[i])||!args[i+1]||options[args[i]])throw Error('Invalid arguments');options[args[i]]=args[i+1];}
    if(!options['--practice']||!options['--run-id']||!options['--body-file'])throw Error('Required: --practice --run-id --body-file');
    const raw=options['--checkpoint'];
    if(raw&&!/^[1-9]\d*$/.test(raw))throw Error('Invalid checkpoint');
    const result=publish({repo:options['--repo']||process.env.GITHUB_REPOSITORY||'alaiba/arcogine',number:raw?Number(raw):undefined,practice:options['--practice'],runId:options['--run-id'],body:fs.readFileSync(options['--body-file'],'utf8')});
    console.log(`Checkpoint #${result.id}: ${result.status}`);
  }catch(error){console.error('Checkpoint publication failed: '+error.message);process.exitCode=1;}
}
