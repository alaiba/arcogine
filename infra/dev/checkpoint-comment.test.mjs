import test from 'node:test';
import assert from 'node:assert/strict';
import {marker,publish,checkpoint,pages} from './checkpoint-comment.mjs';
const repo='alaiba/arcogine';
function mock(initial=[],viewer={login:'alaiba',databaseId:17}) {
 const comments=[...initial], calls=[];
 const api=(method,url,body)=>{
  calls.push({method,url,body});
  if(method==='POST'&&url==='graphql')return {data:{viewer}};
  if(method==='GET'&&url.endsWith('/issues/460'))return {title:'Continuous improvement checkpoint',state:'closed'};
  if(method==='GET'&&url.includes('issues?state=open'))return [{number:460,title:'Continuous improvement checkpoint'}];
  if(method==='GET'&&url.includes('/comments?'))return comments;
  if(method==='POST'){const entry={id:700+comments.length,body:body.body,user:{id:viewer.databaseId,login:viewer.login}};comments.push(entry);return entry;}
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
 const x=mock([{id:1,user:{id:17,login:'alaiba'},body:marker('assessment','same')+'\none\n'},{id:2,user:{id:17,login:'alaiba'},body:marker('assessment','same')+'\ntwo\n'}]);
 assert.throws(()=>publish({api:x.api,repo,number:460,practice:'assessment',runId:'same',body:'other'}),/Duplicate/);
 assert.equal(x.calls.filter(c=>c.method!=='GET'&&c.url!=='graphql').length,0);
});
test('pages must be complete and shaped correctly',()=>{
 let n=0;assert.equal(pages(()=>++n===1?Array(100).fill(0):[1],'test').length,101);
 assert.throws(()=>pages(()=>({}),'test'),/pagination/);
});

test('foreign or missing authors cannot satisfy or replace a result for humans or Actions',()=>{
 for(const viewer of [{login:'alaiba',databaseId:17},{login:'github-actions[bot]',databaseId:41898282}]) {
  for(const user of [undefined,{login:'foreign',id:99},{login:viewer.login,id:99},{login:'foreign',id:viewer.databaseId}]) {
   for(const body of ['complete','different']) {
    const original={id:1,user,body:marker('retrospective','pr-501-merged')+'\ncomplete\n'};
    const x=mock([original],viewer);
    assert.throws(()=>publish({api:x.api,repo,number:460,practice:'retrospective',runId:'pr-501-merged',body}),/ownership collision/);
    assert.equal(original.body,marker('retrospective','pr-501-merged')+'\ncomplete\n');
    assert.equal(x.calls.filter(c=>c.method!=='GET'&&c.url!=='graphql').length,0);
   }
  }
 }
});

test('Actions retries only update their own result',()=>{
 const x=mock([],{login:'github-actions[bot]',databaseId:41898282});
 const args={api:x.api,repo,number:460,practice:'retrospective',runId:'pr-501-merged',body:'complete'};
 assert.equal(publish(args).status,'created');
 assert.equal(publish(args).status,'unchanged');
 assert.equal(publish({...args,body:'corrected'}).status,'updated');
 assert.equal(x.comments.length,1);
});

test('unverifiable publisher fails without issue-comment writes',()=>{
 for(const viewer of [null,{}, {login:'alaiba'}, {login:'alaiba',databaseId:0}]) {
  const x=mock([],viewer);
  assert.throws(()=>publish({api:x.api,repo,number:460,practice:'assessment',runId:'head',body:'complete'}),/verify authenticated/);
  assert.equal(x.calls.filter(c=>c.method!=='GET'&&c.url!=='graphql').length,0);
 }
});
