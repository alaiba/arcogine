import test from 'node:test';
import assert from 'node:assert/strict';
import {marker,publish,checkpoint,pages} from './checkpoint-comment.mjs';
const repo='alaiba/arcogine';
function mock(initial=[]) {
 const comments=[...initial], calls=[];
 const api=(method,url,body)=>{
  calls.push({method,url,body});
  if(method==='GET'&&url.endsWith('/issues/460'))return {title:'Continuous improvement checkpoint',state:'closed'};
  if(method==='GET'&&url.includes('issues?state=open'))return [{number:460,title:'Continuous improvement checkpoint'}];
  if(method==='GET'&&url.includes('/comments?'))return comments;
  if(method==='POST'){const entry={id:700+comments.length,body:body.body};comments.push(entry);return entry;}
  if(method==='PATCH'){comments.find(c=>String(c.id)===url.split('/').at(-1)).body=body.body;return {};}
  throw Error(method+' '+url);
 };
 return {api,comments,calls};
}
test('one outcome per practice/run; identical retry no-op, changed retry updates',()=>{
 const x=mock(),args={api:x.api,repo,number:460,practice:'assessment',runId:'head-1',body:'RUN Consistency'};
 assert.equal(publish(args).status,'created');
 assert.equal(publish(args).status,'unchanged');
 assert.equal(publish({...args,body:'RUN Retrospective'}).status,'updated');
 assert.equal(x.comments.length,1);
 assert.ok(x.comments[0].body.startsWith(marker('assessment','head-1')));
});
test('closed checkpoint is valid with exact issue number',()=>{
 assert.equal(publish({api:mock().api,repo,number:460,practice:'consistency',runId:'head-2',body:'CLEAN'}).status,'created');
});
test('ambiguous checkpoint and wrong identity fail without writes',()=>{
 const api=()=>[];
 assert.throws(()=>checkpoint(api,repo,undefined),/unambiguous/);
 assert.throws(()=>marker('kaizen','x'),/Invalid/);
 assert.throws(()=>marker('consistency','bad -->'),/Invalid/);
 const x=mock([{id:1,body:marker('assessment','same')+'\none\n'},{id:2,body:marker('assessment','same')+'\ntwo\n'}]);
 assert.throws(()=>publish({api:x.api,repo,number:460,practice:'assessment',runId:'same',body:'other'}),/Duplicate/);
 assert.equal(x.calls.filter(c=>c.method!=='GET').length,0);
});
test('pages must be complete and shaped correctly',()=>{
 let n=0;assert.equal(pages(()=>++n===1?Array(100).fill(0):[1],'test').length,101);
 assert.throws(()=>pages(()=>({}),'test'),/pagination/);
});
