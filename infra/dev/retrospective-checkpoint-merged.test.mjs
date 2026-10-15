import test from 'node:test';
import assert from 'node:assert/strict';
import {checkpointFromBody,summarize,complete} from './retrospective-checkpoint-merged.mjs';
const repo='alaiba/arcogine',sha='a'.repeat(40),path='docs/history/continuous-improvement/delivery-process-retrospective-2026-10-14.md';
const state={baselinePr:475,latestReport:path},report='# Report\n\nScope: 368 -> 475\n\n## Result\n- Improved\n\n## Actions\n- #483\n';
test('strict retrospective scope, checkpoint and report identity',()=>{
 assert.equal(checkpointFromBody('Retrospective checkpoint: #460\n'),460);
 assert.throws(()=>checkpointFromBody('none'),/one checkpoint/);
 assert.throws(()=>checkpointFromBody('Retrospective checkpoint: #460\nRetrospective checkpoint: #460\n'),/one checkpoint/);
 const summary=summarize({report,state,sha,repo,pr:501});
 assert.match(summary.body,/Improved/);
 assert.match(summary.body,/blob\/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/);
 assert.throws(()=>summarize({report,state:{...state,baselinePr:476},sha,repo,pr:501}),/scope/);
});
function fake({body='Retrospective checkpoint: #460',files=['.github/continuous-improvement/retrospective.json',path],merged=true}={}) {
 const calls=[];
 const api=(method,url,data)=>{
  calls.push({method,url,data});
  if(method==='POST'&&url==='graphql')return {data:{viewer:{login:'github-actions[bot]',databaseId:41898282}}};
  if(method==='GET'&&url.endsWith('/pulls/501'))return {merged,base:{ref:'main'},merge_commit_sha:sha,body};
  if(method==='GET'&&url.includes('/pulls/501/files?'))return files.map(filename=>({filename}));
  if(method==='GET'&&url.endsWith('/issues/460'))return {title:'Continuous improvement checkpoint',state:'closed'};
  if(method==='GET'&&url.includes('/comments?'))return [];
  if(method==='POST')return {id:1};
  throw Error(method+' '+url);
 };
 return {api,calls};
}
test('merged report publishes distinct final outcome',()=>{
 const x=fake(),read=p=>p===path?report:JSON.stringify(state);
 assert.equal(complete({api:x.api,repo,pr:501,sha,read}).status,'created');
 assert.equal(x.calls.filter(c=>c.method==='POST'&&c.url!=='graphql').length,1);
 assert.match(x.calls.find(c=>c.method==='POST'&&c.url!=='graphql').data.body,/pr-501-merged/);
});
test('unmerged, unrelated, or unassociated PR cannot publish',()=>{
 const read=p=>p===path?report:JSON.stringify(state);
 const u=fake({merged:false});assert.throws(()=>complete({api:u.api,repo,pr:501,sha,read}),/Not a merged/);
 const n=fake({files:['README.md']});assert.equal(complete({api:n.api,repo,pr:501,sha,read}).status,'not-retrospective');
 const c=fake({body:'No checkpoint'});assert.equal(complete({api:c.api,repo,pr:501,sha,read}).status,'no-checkpoint');
 for(const x of [u,n,c])assert.equal(x.calls.filter(t=>t.method==='POST').length,0);
});
