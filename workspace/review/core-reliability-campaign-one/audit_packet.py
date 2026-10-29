"""Independent packet/inventory/mutation custody checks. Run from repository root."""
import hashlib
import json
import pathlib
import subprocess
import sys
import tempfile

ROOT = pathlib.Path(__file__).resolve().parents[3]
PACKET = ROOT / "logs/packet/workspace/assurance/phase-b-campaign-1"
BASE = "a0feeffcdec4a932e3ddbb62ce2551cc2540b1e4"
DESIGN = "baf2e2a94750a350cc533663d52c4c147f41d9ba"

def git(*args):
    return subprocess.check_output(["git", *args], cwd=ROOT)

def tree(ref):
    found = {}
    for line in git("ls-tree", "-r", ref, "product").decode().splitlines():
        meta, path = line.split("\t")
        if path.endswith(".java"):
            found[path] = meta.split()[2]
    return found

crosswalk = json.loads(git("show", DESIGN + ":workspace/assurance/core-reliability-phase-a-crosswalk.json"))
old = {row["path"]: row["blob"] for row in crosswalk["files"]}
assert old == tree(crosswalk["assessedCommit"])
new = tree(BASE)
expected = json.loads((PACKET / "results/inventory-reconciliation.json").read_text(encoding="utf-8"))
added = sorted(new.keys() - old.keys())
changed = sorted(p for p in old.keys() & new.keys() if old[p] != new[p])
assert added == [x["path"] for x in expected["added"]]
assert changed == [x["path"] for x in expected["changed"]]
assert all(x["blob"] == new[x["path"]] for x in expected["added"] + expected["changed"])
assert len(new) == 313 and len(old) == 300 and len(added) == 13 and len(changed) == 43
contracts = sorted(set(c for row in crosswalk["files"] for c in row["contracts"]))
assert len(contracts) == 33
print("INVENTORY: 300 -> 313; 257 unchanged, 43 changed, 13 added, 0 removed; 33 contracts")
print("LIVE DRIFT:", git("diff", "--stat", BASE, "origin/main").decode().strip())

def result(path):
    suites, status = {}, None
    for line in path.read_text(encoding="utf-8").splitlines():
        cols = line.split()
        if cols[:1] == ["STATUS"]:
            status = line
        elif len(cols) == 6:
            suites[cols[1]] = (cols[0], tuple(map(int, cols[2:])))
    return suites, status

baseline, status = result(PACKET / "mutation/run/BASELINE.result")
assert "ALL-PASS" in status and baseline
mutants = []
for block in (PACKET / "mutation/mutants.txt").read_text(encoding="utf-8").split("=== ")[1:]:
    lines = block.splitlines()
    ident = lines[0]
    selection = next(x.removeprefix("selection: ") for x in lines if x.startswith("selection: "))
    mutants.append((ident, selection))
counts = {"killed": 0, "survived": 0}
issues = []
for ident, selection in mutants:
    suites, status = result(PACKET / "mutation/run" / (ident + ".result"))
    def included(module, name):
        return ((selection in ("types", "types+factory") and module == "types")
                or (selection in ("factory", "types+factory") and module == "domains/factory")
                or (selection == "seam" and module in ("storage", "governance")))
    wanted = {name: facts for name, facts in baseline.items() if included(facts[0], name)}
    if suites.keys() != wanted.keys() or any(suites[n][1][0] != wanted[n][1][0] for n in suites.keys() & wanted.keys()):
        issues.append(ident + " discovered suite/test-count mismatch")
    assert "INCONCLUSIVE" not in status
    killed = any(values[1][2] + values[1][3] for values in suites.values())
    counts["killed" if killed else "survived"] += 1
print("MUTATION SUMMARIES:", len(mutants), "mutants", counts, "baseline suites", len(baseline))
print("DISCOVERY MISMATCHES:", issues)
assert not issues

print("LEDGER NEGATIVE CONTROL: empty passing baseline + discovery-error suite")
with tempfile.TemporaryDirectory(dir=ROOT / "logs") as temp:
    temp = pathlib.Path(temp)
    (temp / "BASELINE.result").write_text("STATUS BASELINE ALL-PASS selection=all exit=0 seconds=1\n")
    (temp / "T-CTOR-ADMITS-MINUS-ONE.result").write_text(
        "types com.example.DiscoveryFailure 1 0 0 1\nSTATUS T-CTOR-ADMITS-MINUS-ONE FAILING-TESTS selection=types exit=1 seconds=1\n")
    output = subprocess.check_output([sys.executable, "-I", str(PACKET / "tools/mutation_ledger.py"), str(temp), str(PACKET / "mutation/mutants.txt")], text=True)
    line = next(x for x in output.splitlines() if "`T-CTOR-ADMITS-MINUS-ONE`" in x)
    print(line)
    assert "KILLED" in line

vector_bytes = subprocess.check_output([sys.executable, "-I", "-X", "utf8", str(PACKET / "oracles/canonical_vectors.py")])
vectors = json.loads(vector_bytes)
assert vectors == json.loads((PACKET / "results/canonical-vectors.json").read_text(encoding="utf-8"))
for key, value in vectors.items():
    data = bytes.fromhex(value["hex"])
    assert len(data) == value["length"]
    assert hashlib.sha256(data).hexdigest() == value["fingerprint"].split(":")[-1]
    print("VECTOR", key, len(data), value["fingerprint"])
print("BYTE FRONTIER: representative offset 47, M->A (0x4d->0x41) is absent from six-value substitution alphabet")
assert 0x41 not in [0x4d ^ 1, 0x4d ^ 0x80, 0, 255, 0x4d + 1, 0x4d ^ 0x40]
print("PACKET AUDIT COMPLETE")
summary = {
    "inventory": {"historical": 300, "execution": 313, "unchanged": 257, "changed": 43, "added": 13, "removed": 0, "contracts": 33},
    "mutationSummaries": {"mutants": len(mutants), **counts, "baselineSuites": len(baseline), "discoveryMismatches": issues},
    "ledgerNegativeControl": {"input": "empty ALL-PASS baseline plus com.example.DiscoveryFailure errors=1", "observed": line},
    "vectors": vectors,
    "reproducedResultEquality": {file.name: file.read_bytes() == (PACKET / "results" / file.name).read_bytes()
        for file in (ROOT / "logs/reproduced-results").iterdir() if (PACKET / "results" / file.name).exists()},
    "liveMain": git("rev-parse", "origin/main").decode().strip(),
}
destination = pathlib.Path(__file__).parent / "results/packet-audit.json"
destination.write_text(json.dumps(summary, indent=2) + "\n", encoding="utf-8")
