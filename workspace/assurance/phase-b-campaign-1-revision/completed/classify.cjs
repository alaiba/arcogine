// Deliberately conservative: execution failure is never automatically a semantic kill.
const fs=require('fs'), path=require('path'), assert=require('assert/strict');
const here=__dirname;
const ids=r=>(r.cases||[]).map(c=>c.class+'#'+c.name).sort();
const faults=r=>(r.cases||[]).flatMap(c=>[...(c.failure||[]),...(c.error||[])]);
function classify(base,run) {
 if(base.exitCode!==0 || !base.cases?.length || !base.suites?.length || base.suites.some(s=>+s.errors || +s.failures) || faults(base).length || base.cases.some(c=>c.skipped)) return 'INCONCLUSIVE: invalid baseline';
 if(run.timedOut || run.exitCode===null) return 'INCONCLUSIVE: timeout or missing exit';
 if(JSON.stringify(ids(base))!==JSON.stringify(ids(run))) return 'INCONCLUSIVE: discovery differs';
 if(run.cases.some(c=>c.skipped || c.error?.length) || run.suites.some(s=>+s.errors)) return 'INCONCLUSIVE: test error or skip';
 const failures=faults(run);
 if(run.exitCode===0 && !failures.length) return 'SURVIVED';
 if(run.exitCode===0 || !failures.length) return 'INCONCLUSIVE: inconsistent exit/results';
 if(!failures.every(f=>['org.opentest4j.AssertionFailedError','java.lang.AssertionError'].includes(f.type))) return 'INCONCLUSIVE: non-assertion failure requires triage';
 return 'ASSERTION-DETECTED: requires semantic triage';
}
const c={class:'Witness',name:'boundary',failure:[],error:[],skipped:false};
const b={exitCode:0,cases:[c],suites:[{errors:'0',failures:'0'}]};
const synthetic=[
 ['empty baseline',{exitCode:0,cases:[]},b,'INCONCLUSIVE: invalid baseline'],
 ['baseline suite error',{...b,suites:[{errors:'1',failures:'0'}]},b,'INCONCLUSIVE: invalid baseline'],
 ['baseline suite failure',{...b,suites:[{errors:'0',failures:'1'}]},b,'INCONCLUSIVE: invalid baseline'],
 ['missing test',b,{...b,cases:[]},'INCONCLUSIVE: discovery differs'],
 ['compile failure',b,{...b,exitCode:1,cases:[]},'INCONCLUSIVE: discovery differs'],
 ['discovery error',b,{...b,exitCode:1,cases:[{...c,error:[{type:'DiscoveryFailure'}]}]},'INCONCLUSIVE: test error or skip'],
 ['timeout',b,{...b,timedOut:true},'INCONCLUSIVE: timeout or missing exit'],
 ['skip',b,{...b,cases:[{...c,skipped:true}]},'INCONCLUSIVE: test error or skip'],
 ['nonassertion',b,{...b,exitCode:1,cases:[{...c,failure:[{type:'java.lang.IllegalStateException'}]}]},'INCONCLUSIVE: non-assertion failure requires triage'],
 ['nonzero no failure',b,{...b,exitCode:1},'INCONCLUSIVE: inconsistent exit/results'],
 ['pass',b,b,'SURVIVED'],
 ['assertion',b,{...b,exitCode:1,cases:[{...c,failure:[{type:'org.opentest4j.AssertionFailedError'}]}]},'ASSERTION-DETECTED: requires semantic triage']
].map(([name,base,run,expected])=>{const actual=classify(base,run);assert.equal(actual,expected,name);return {name,actual};});
fs.writeFileSync(path.join(here,'results/classifier-controls.json'),JSON.stringify(synthetic,null,2));
const base=JSON.parse(fs.readFileSync(path.join(here,'results/corpus/run.json'),'utf8'));
const ledger=fs.readdirSync(path.join(here,'results')).filter(n=>/^(T-|F-|G-|CH[123]-)/.test(n)).sort().map(name=>{
 const file=path.join(here,'results',name,'run.json');if(!fs.existsSync(file))return {name,outcome:'INCONCLUSIVE: unfinished'};
 const r=JSON.parse(fs.readFileSync(file,'utf8'));
 return {name,outcome:classify(base,r),exitCode:r.exitCode,tests:r.cases.length,failures:r.cases.filter(c=>c.failure.length||c.error.length).map(c=>({class:c.class,name:c.name,failure:c.failure,error:c.error}))};
});
fs.writeFileSync(path.join(here,'results/mutation-classification.json'),JSON.stringify(ledger,null,2));
console.log(JSON.stringify(ledger.reduce((out,r)=>(out[r.outcome]=(out[r.outcome]||0)+1,out),{})));
