// Direct encoding of factory-model.md section 4; no product classes or Java oracle helpers.
const fs=require('fs'),crypto=require('crypto'),assert=require('assert/strict');
const min=-(1n<<63n),max=(1n<<63n)-1n;
const r=(id,name,concurrency,capacity,setup)=>({id,name,concurrency,capacity,setup});
const s=(id,name,duration,eligible)=>({id,name,duration,eligible});
const fixtures={
 empty:{resources:[],operations:[],products:[]},
 representative:{resources:[r(1n,'Mixer',2n,1500.5,30n),r(-7n,'Prüf;stand|A',1n,null,0n)],operations:[{id:10n,name:'Blend',steps:[s(1n,'Load',5n,[1n,-7n]),s(2n,'Mix 🌀',9n,[1n])]}],products:[{id:100n,name:'Paint',operation:10n}]},
 'signed-extremes':{resources:[r(max,'max',2147483647n,-0,min),r(min,'min',1n,0,max),r(0n,'nan',1n,NaN,-1n),r(-1n,'inf',1n,Infinity,0n),r(2n,'neg-inf',1n,-Infinity,0n)],operations:[{id:min,name:'',steps:[s(max,'\0:|;\n',max,[max,min,0n,-1n,2n]),s(max,'dup-step-id',1n,[min])]}],products:[{id:min,name:'日本',operation:min},{id:max,name:'\u{10ffff}',operation:min}]},
 'literal-challenger':{resources:[r(min,'é🌀',2147483647n,-0,min)],operations:[{id:-1n,name:'\0e\u0301',steps:[s(max,'\u{10ffff}',max,[min])]}],products:[{id:0n,name:'',operation:-1n}]}
};
function encode(model){
 const chunks=[],fields=[];let offset=0;
 function emit(name,b){chunks.push(b);fields.push({offset,name,hex:b.toString('hex')});offset+=b.length;}
 function u(name,v){const b=Buffer.alloc(8);b.writeBigUInt64BE(BigInt(v));emit(name,b);}
 function i(name,v){const b=Buffer.alloc(8);b.writeBigInt64BE(BigInt(v));emit(name,b);}
 function text(name,v){if(!v.isWellFormed())throw new Error('non-scalar text');const b=Buffer.from(v,'utf8');u(name+'.bytes',b.length);emit(name,b);}
 emit('prefix',Buffer.from('arcogine.factory-model\0','ascii'));
 u('resources.count',model.resources.length);
 model.resources.forEach((r,k)=>{const p=`resources[${k}]`;i(p+'.id',r.id);text(p+'.name',r.name);i(p+'.concurrency',r.concurrency);emit(p+'.presence',Buffer.from([r.capacity===null?0:1]));if(r.capacity!==null){const b=Buffer.alloc(8);if(Number.isNaN(r.capacity))b.writeBigUInt64BE(0x7ff8000000000000n);else b.writeDoubleBE(r.capacity);emit(p+'.capacity',b);}i(p+'.setup',r.setup);});
 u('operations.count',model.operations.length);
 model.operations.forEach((o,k)=>{const p=`operations[${k}]`;i(p+'.id',o.id);text(p+'.name',o.name);u(p+'.steps.count',o.steps.length);o.steps.forEach((s,j)=>{const q=p+`.steps[${j}]`;i(q+'.id',s.id);text(q+'.name',s.name);i(q+'.duration',s.duration);const sorted=[...new Set(s.eligible)].sort((a,b)=>a<b?-1:a>b?1:0);u(q+'.eligible.count',sorted.length);sorted.forEach((id,z)=>i(q+`.eligible[${z}]`,id));});});
 u('products.count',model.products.length);
 model.products.forEach((p,k)=>{i(`products[${k}].id`,p.id);text(`products[${k}].name`,p.name);i(`products[${k}].operation`,p.operation);});
 const bytes=Buffer.concat(chunks);return {length:bytes.length,hex:bytes.toString('hex'),fingerprint:'factory-model:sha256:'+crypto.createHash('sha256').update(bytes).digest('hex'),fields};
}
const result=Object.fromEntries(Object.entries(fixtures).map(([k,m])=>[k,encode(m)]));
assert.throws(()=>encode({resources:[r(1n,'\ud800',1n,null,0n)],operations:[],products:[]}));
const java=fs.readFileSync(__dirname+'/../experiment-sources/factory/src/test/java/com/arcogine/factory/model/Campaign1CodecExperimentTest.java','utf8');
for(const [key,prefix] of [['empty','EMPTY'],['representative','REPRESENTATIVE'],['signed-extremes','EXTREMES']]) {
 const hex=java.match(new RegExp('String '+prefix+'_HEX =\\s*"([a-f0-9]+)";'))?.[1];
 assert.ok(hex,'literal '+prefix);assert.equal(result[key].hex,hex,key);
}
fs.writeFileSync(__dirname+'/../results/rederived-vectors.json',JSON.stringify(result,null,2));
console.log(Object.fromEntries(Object.entries(result).map(([k,v])=>[k,{length:v.length,fingerprint:v.fingerprint}])));
