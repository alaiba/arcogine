const fs=require('fs'),path=require('path'),assert=require('assert/strict'),crypto=require('crypto');
const read=n=>JSON.parse(fs.readFileSync(path.join(__dirname,'results',n,'run.json'),'utf8'));
const clean=read('clean-retained'),base=read('corpus'),restored=read('restored');
assert.equal(clean.exitCode,0);assert.equal(clean.cases.length,305);
assert.equal(base.exitCode,0);assert.equal(base.cases.length,43);
assert.equal(restored.exitCode,0);assert.equal(restored.cases.length,43);
assert.deepEqual(base.sourceHashes,restored.sourceHashes);
assert.deepEqual(base.classfileHashes,restored.classfileHashes);
const main=h=>Object.fromEntries(Object.entries(h).filter(([p])=>p.includes('/classes/java/main/')));
assert.deepEqual(main(clean.classfileHashes),main(base.classfileHashes));
const definitions=fs.readFileSync(__dirname+'/mutation/mutants.txt','utf8').split(/^=== /m).slice(1).map(b=>({id:b.split(/\r?\n/)[0],file:b.match(/^file: (.+)$/m)[1].trim(),note:b.match(/^note: (.+)$/m)[1].trim()}));
const byId=Object.fromEntries(definitions.map(d=>[d.id,d]));
byId['CH1-MESSAGE']={file:'product/domains/factory/src/main/java/com/arcogine/factory/model/validation/FactoryModelValidator.java',note:'Non-resource Unicode diagnostic wording changes; full literal sequences must observe it.'};
byId['CH2-CATEGORY']={file:'product/storage/src/main/java/com/arcogine/storage/FileArcogineStorage.java',note:'Candidate mismatch is reported as STORAGE_INTEGRITY instead of FINGERPRINT_MISMATCH.'};
byId['CH3-UNREFERENCED']={file:'product/domains/factory/src/main/java/com/arcogine/factory/model/FactoryRuntimeAssembler.java',note:'Published operation with no product reference disappears; minimal routing 10 witness gets UnknownId.'};
const rows=JSON.parse(fs.readFileSync(__dirname+'/results/mutation-classification.json','utf8'));
assert.equal(rows.length,47);
const equivalent=new Set(['F-VAL-EMPTY-ELIGIBLE-NO-CONTINUE','F-ENC-NO-SORT','F-ENC-LONGBITS']);
const redundant=new Set(['F-DEC-NO-TRAILING-CHECK','F-DEC-NO-CONCURRENCY-RANGE','F-DEC-UTF8-REPLACE','F-DEC-ANY-MARKER']);
const diagnostic=new Set(['F-DEC-NEGATIVE-COUNT','CH1-MESSAGE']);
const exceptions={
 'CH3-UNREFERENCED':'Manual triage: both executed test bodies reach RoutingStore and throw UnknownId for an authored operation. Baseline and restoration resolve it; full discovery and single-source delta verified. This is the intended behavioral discrepancy, not test discovery failure.',
 'F-VAL-HIGH-AT-END':'Manual triage: validator rejects a lone trailing high surrogate via StringIndexOutOfBoundsException instead of structured validation; stack reaches rejectMalformedUnicode in the selected test body. Baseline and restoration pass.',
 'T-MACHINEID-REVERSED':'Manual triage: direct numeric ordering assertion expects -1 and observes 1. Additional decode exceptions are downstream effects, not the sole kill evidence.',
 'F-ENC-PREFIX':'Manual triage: independently literal golden-byte comparison fails at the definition prefix. Additional decoding exceptions are downstream effects.',
 'F-ENC-LITTLE-ENDIAN':'Manual triage: independently literal golden-byte comparison fails at integer byte order. Additional parsing/history exceptions are downstream effects.'
};
const triage=rows.map(row=>{
 const run=read(row.name);assert.equal(run.cases.length,43);assert.ok(byId[row.name]);
 const changed=Object.keys(base.sourceHashes).filter(p=>base.sourceHashes[p]!==run.sourceHashes[p]);
 assert.deepEqual(changed,[byId[row.name].file]);
 let outcome,rationale=byId[row.name].note;
 if(equivalent.has(row.name)){assert.equal(row.outcome,'SURVIVED');outcome='SURVIVED — equivalent control';}
 else if(redundant.has(row.name)){assert.equal(row.outcome,'SURVIVED');outcome='SURVIVED — redundant at required refusal boundary';}
 else if(row.name==='G-NO-EXTRACTOR-SUPPORT'){assert.equal(row.outcome,'SURVIVED');outcome='SURVIVED — uncovered guard';rationale+=' Current real comparator supports both artifacts; no unsupported-extractor case was exercised.';}
 else if(diagnostic.has(row.name)){assert.ok(row.failures.length);outcome='DETECTED — diagnostic observation only';rationale+=' Required semantic admission/refusal class is unchanged; do not count as a semantic violation.';}
 else if(row.name==='F-BINDING-DROPS-ELIGIBLE'){outcome='DETECTED — binding inclusion observation';rationale+=' Class-load/binding inclusion witness only; no claim of complete executed control flow.';}
 else {assert.ok(row.failures.length);assert.ok(row.outcome.startsWith('ASSERTION-DETECTED')||exceptions[row.name]);outcome='KILLED — bounded semantic control';}
 if(exceptions[row.name])rationale+=' '+exceptions[row.name];
 return {name:row.name,automatic:row.outcome,outcome,rationale,sourceDelta:changed,tests:run.cases.length,exitCode:run.exitCode,evidence:`results/${row.name}/run.json`,witnesses:row.failures.map(f=>f.class+'#'+f.name)};
});
const counts=triage.reduce((a,r)=>(a[r.outcome]=(a[r.outcome]||0)+1,a),{});
fs.writeFileSync(__dirname+'/results/triage.json',JSON.stringify({actor:'Continuing Codex evidence producer; not independent adjudication',counts,triage},null,2));
fs.writeFileSync(__dirname+'/mutation-triage.md','# Mutation triage\n\nProducer assessment only. Automatic classification is retained separately. Full failure payloads and discovery are linked per row. No timeout, compile or discovery failure occurred in these 47 runs. No unreachable mutant was inferred.\n\n| Control | Outcome | Rationale and evidence |\n|---|---|---|\n'+triage.map(r=>`| ${r.name} | ${r.outcome} | ${r.rationale.replaceAll('|','\\|')} [Observed tests/failures](${r.evidence}) |`).join('\n')+'\n');
const details={retainedTests:305,corpusTests:43,restoredTests:43,mutants:47,exactSingleSourceDeltaEveryMutant:true,restoredAllJavaSourcesIdentical:true,restoredAllCompiledClassesIdentical:true,retainedAndCorpusMainClassesIdentical:true,mainClassCount:Object.keys(main(base.classfileHashes)).length,counts};
fs.writeFileSync(__dirname+'/results/provenance-audit.json',JSON.stringify(details,null,2));
console.log(details);
