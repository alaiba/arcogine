const fs=require('fs'),cp=require('child_process'),assert=require('assert/strict');
const git=(...args)=>cp.execFileSync('git',args,{encoding:'utf8',maxBuffer:16*1024*1024}).trim();
const baseline='cb1afadaf683f915a31b09df2fd732da10515df4';
const design='baf2e2a94750a350cc533663d52c4c147f41d9ba';
const producer='a0feeffcdec4a932e3ddbb62ce2551cc2540b1e4';
const crosswalk=JSON.parse(git('show',design+':workspace/assurance/core-reliability-phase-a-crosswalk.json'));
function tree(sha){return Object.fromEntries(git('ls-tree','-r',sha,'product').split('\n').filter(s=>s.endsWith('.java')).map(s=>{const [meta,path]=s.split('\t');return [path,meta.split(' ')[2]];}));}
const old=tree(crosswalk.assessedCommit),current=tree(baseline),prior=tree(producer);
assert.deepEqual(Object.fromEntries(crosswalk.files.map(f=>[f.path,f.blob])),old);
const addedMapping={
 'EligibleMachines.java':['F-PUBLISH','F-CODEC','F-ASSEMBLE','E-DISPATCH'],
 'MissingEligibleResourceOrderProbe.java':['F-PUBLISH'],
 'MissingEligibleResourceOrderTest.java':['F-PUBLISH'],
 'EligibleMachineOrderTest.java':['E-DISPATCH','F-ASSEMBLE'],
 'SequenceExhaustionTest.java':['E-SCHEDULE','E-ARITH'],
 'SharedBacklogAdmissionBenchmarkTest.java':['E-WORK'],
 'SharedBacklogEquivalenceTest.java':['E-WORK'],
 'TaskEndTimeOverflowTest.java':['T-TIME','E-ARITH'],
 'EvidenceUseRevisionBinding.java':['G-EVIDENCE','G-REVISION'],
 'EvidenceUseRevisionBindingTest.java':['G-EVIDENCE','G-REVISION'],
 'ExactOccupancyAreaTest.java':['R-DERIVE'],
 'ExperimentStepTest.java':['R-CAPTURE'],
 'SchedulerSequenceExhaustionTest.java':['E-SCHEDULE','E-ARITH']
};
const priorRows=Object.fromEntries(crosswalk.files.map(f=>[f.path,f]));
const files=Object.entries(current).map(([path,blob])=>{
 const contracts=priorRows[path]?.contracts||addedMapping[path.split('/').at(-1)]; assert.ok(contracts,path);
 return {path,blob,sourceSet:path.match(/\/src\/(main|test|jmh)\//)?.[1],contracts,mappingOrigin:priorRows[path]?'Phase A inherited; inspected changed scoped responsibilities':'revision responsibility mapping',phaseABlob:old[path]||null,change:!old[path]?'added':old[path]===blob?'unchanged':'changed',producerBlob:prior[path]||null};
});
const contracts=[...new Set(files.flatMap(f=>f.contracts))].sort();assert.equal(contracts.length,33);
const summary={phaseACount:Object.keys(old).length,currentCount:files.length,bySourceSet:{},byChange:{},contracts:contracts.length};
for(const f of files){summary.bySourceSet[f.sourceSet]=(summary.bySourceSet[f.sourceSet]||0)+1;summary.byChange[f.change]=(summary.byChange[f.change]||0)+1;}
fs.writeFileSync(__dirname+'/results/inventory.json',JSON.stringify({baseline,design,producer,summary,contracts,files,removed:Object.keys(old).filter(p=>!current[p]),driftSinceProducer:git('diff','--stat',producer,baseline),javaDriftSinceProducer:files.filter(f=>f.producerBlob!==f.blob)},null,2));
console.log(summary);
