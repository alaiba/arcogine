"""Reconcile the Phase A crosswalk's Java inventory with the Campaign 1 execution baseline.

Usage (repository root):
  python -I reconcile_inventory.py CROSSWALK_JSON EXECUTION_SHA > inventory-reconciliation.json

The crosswalk is read from its immutable Phase A commit; the current inventory from
`git ls-tree -r EXECUTION_SHA product` filtered to .java. Blob equality is the change authority.
"""

import json
import subprocess
import sys

CAMPAIGN_1 = {"T-VALUE", "T-TIME", "F-PUBLISH", "F-CODEC", "F-ASSEMBLE", "F-CHANGE", "G-CHANGE"}


def tree(sha):
    out = subprocess.run(["git", "ls-tree", "-r", sha, "product"], check=True, capture_output=True, text=True).stdout
    files = {}
    for line in out.splitlines():
        meta, path = line.split("\t", 1)
        if path.endswith(".java"):
            files[path] = meta.split()[2]
    return files


def source_set(path):
    for name in ("/src/main/", "/src/test/", "/src/jmh/"):
        if name in path:
            return name.strip("/").split("/")[1]
    return "other"


def main():
    crosswalk = json.load(open(sys.argv[1], encoding="utf-8"))
    sha = sys.argv[2]
    old = {f["path"]: f for f in crosswalk["files"]}
    current = tree(sha)
    old_tree = tree(crosswalk["assessedCommit"])
    assert set(old_tree) == set(old), "crosswalk path set no longer matches its assessed commit"
    mismatched = sorted(p for p in old if old[p]["blob"] != old_tree[p])
    assert not mismatched, mismatched
    added = sorted(set(current) - set(old))
    removed = sorted(set(old) - set(current))
    changed = sorted(p for p in set(current) & set(old) if current[p] != old[p]["blob"])
    counts = {}
    for p in current:
        counts[source_set(p)] = counts.get(source_set(p), 0) + 1
    campaign_changed = [p for p in changed if CAMPAIGN_1 & set(old[p]["contracts"])]
    result = {
        "phaseACommit": crosswalk["assessedCommit"],
        "executionCommit": sha,
        "phaseACount": len(old),
        "currentCount": len(current),
        "currentBySourceSet": dict(sorted(counts.items())),
        "added": [{"path": p, "blob": current[p], "sourceSet": source_set(p)} for p in added],
        "removed": removed,
        "changed": [{"path": p, "phaseABlob": old[p]["blob"], "blob": current[p],
                     "phaseAContracts": old[p]["contracts"]} for p in changed],
        "unchangedCount": len(set(current) & set(old)) - len(changed),
        "campaign1ChangedPaths": campaign_changed,
        "campaign1MappedPaths": sorted(p for p in old if CAMPAIGN_1 & set(old[p]["contracts"]) and p in current),
    }
    json.dump(result, sys.stdout, indent=2)
    sys.stdout.write("\n")


if __name__ == "__main__":
    main()
