#!/usr/bin/env node
// Read the merged, reviewed retrospective report and publish its outcome.
import fs from 'node:fs';
import {fileURLToPath} from 'node:url';
import {gh,pages,publish} from './checkpoint-comment.mjs';
const STATE='.github/continuous-improvement/retrospective.json';
export function checkpointFromBody(body) {
  const found=[...(body??'').matchAll(/^Retrospective checkpoint: #([1-9][0-9]*)\s*$/gm)];
  if(found.length!==1)throw Error('Retrospective PR requires one checkpoint coordinate');
  return Number(found[0][1]);
}
export function summarize({report,state,sha,repo,pr}) {
  if(!/^[a-f0-9]{40}$/.test(sha))throw Error('Invalid merge SHA');
  const path=state.latestReport;
  if(!/^docs\/history\/continuous-improvement\/delivery-process-retrospective-\d{4}-\d\d-\d\d\.md$/.test(path))throw Error('Invalid Git report path');
  const scope=report.match(/^Scope:\s*#?(\d+)\s*->\s*#?(\d+)\s*$/m);
  if(!scope||Number(scope[2])!==state.baselinePr||Number(scope[1])>=Number(scope[2]))throw Error('Report scope does not match baseline');
  function section(key) {
    const start=report.indexOf('## '+key+'\n');
    if(start===-1)throw Error('Report missing '+key);
    const rest=report.slice(start+key.length+4), next=rest.search(/^## /m);
    const value=(next===-1?rest:rest.slice(0,next)).trim();
    if(!value)throw Error('Empty report section '+key);
    return value;
  }
  return {path,body:['## Delivery-process retrospective — complete','',`**Scope:** PR #${scope[1]} (exclusive) → PR #${scope[2]} (inclusive)`,`**Git report:** [${path}](https://github.com/${repo}/blob/${sha}/${path})`,`**Merged report PR:** #${pr}`,'','### Results',section('Result'),'','### Actions',section('Actions')].join('\n')};
}
export function complete({api=gh,repo,pr,sha,read=path=>fs.readFileSync(path,'utf8')}) {
  const info=api('GET',`repos/${repo}/pulls/${pr}`);
  if(!info.merged||info.base?.ref!=='main'||info.merge_commit_sha!==sha)throw Error('Not a merged PR at this exact main commit');
  const files=pages(api,`repos/${repo}/pulls/${pr}/files`).map(x=>x.filename);
  if(!files.includes(STATE))return {status:'not-retrospective'};
  if(!/Retrospective checkpoint/.test(info.body??''))return {status:'no-checkpoint'};
  const number=checkpointFromBody(info.body),state=JSON.parse(read(STATE));
  const summary=summarize({report:read(state.latestReport),state,sha,repo,pr});
  if(!files.includes(summary.path))throw Error('Report missing from merged PR diff');
  return publish({api,repo,number,practice:'retrospective',runId:`pr-${pr}-merged`,body:summary.body});
}
if(process.argv[1]===fileURLToPath(import.meta.url)) {
  try{
    const repo=process.env.GITHUB_REPOSITORY,pr=Number(process.env.RETROSPECTIVE_PR_NUMBER),sha=process.env.RETROSPECTIVE_MERGE_SHA;
    if(!repo||!Number.isSafeInteger(pr)||pr<1||!sha)throw Error('Missing retrospective workflow inputs');
    console.log(JSON.stringify(complete({repo,pr,sha})));
  }catch(error){console.error('Retrospective publication failed: '+error.message);process.exitCode=1;}
}
