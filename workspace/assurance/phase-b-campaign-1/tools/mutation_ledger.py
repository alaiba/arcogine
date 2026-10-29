"""Summarize a Campaign 1 mutation run directory into a ledger.

Usage: python -I mutation_ledger.py RUN_DIR MUTANTS_TXT > mutation-ledger.md

A mutant is KILLED only when its selection compiled and ran, the clean BASELINE run of the same
classes passed, and at least one suite reports a failure or error. Compile/apply/build failures
are INCONCLUSIVE, never kills. A survivor is SURVIVED until triaged in the report.
"""

import os
import re
import sys


def parse_mutants(path):
    mutants, current = [], None
    for line in open(path, encoding="utf-8"):
        if line.startswith("=== "):
            current = {"id": line[4:].strip()}
            mutants.append(current)
        elif current is not None and line.startswith(("note: ", "selection: ")) and "old" not in current:
            key, value = line.split(": ", 1)
            current[key] = value.strip()
        elif line.strip() == "--- old" and current is not None:
            current["old"] = True
    return mutants


def parse_result(path):
    suites, status = {}, None
    for line in open(path, encoding="utf-8"):
        parts = line.split()
        if parts and parts[0] == "STATUS":
            status = line.strip()
        elif len(parts) == 6:
            suites[parts[1]] = (int(parts[2]), int(parts[4]) + int(parts[5]))
    return suites, status


def main():
    run_dir, mutants_path = sys.argv[1], sys.argv[2]
    baseline, baseline_status = parse_result(os.path.join(run_dir, "BASELINE.result"))
    assert "ALL-PASS" in baseline_status, baseline_status
    assert all(failed == 0 for _, failed in baseline.values())
    print("| Mutant | Selection | Outcome | Killing retained classes | Killing experiment classes | Seconds |")
    print("|---|---|---|---|---|---|")
    totals = {}
    for m in parse_mutants(mutants_path):
        path = os.path.join(run_dir, m["id"] + ".result")
        if not os.path.exists(path):
            outcome, retained, experiment, seconds = "NOT-RUN", [], [], ""
        else:
            suites, status = parse_result(path)
            seconds = (re.search(r"seconds=(\d+)", status or "") or [None, ""])[1]
            failing = sorted(c for c, (_, failed) in suites.items() if failed)
            retained = [c.rsplit(".", 1)[1] for c in failing if ".Campaign1" not in c]
            experiment = [c.rsplit(".", 1)[1] for c in failing if ".Campaign1" in c]
            missing = sorted(set(baseline) - set(suites))
            if status is None or "INCONCLUSIVE" in status:
                outcome = "INCONCLUSIVE (" + (status or "no status").split()[3] + ")"
            elif failing:
                outcome = "KILLED"
            elif "ALL-PASS" in status:
                outcome = "SURVIVED"
            else:
                outcome = "INCONCLUSIVE (" + status + ")"
            if outcome == "KILLED" and not retained:
                outcome = "KILLED (experiment only)"
            del missing
        totals[outcome] = totals.get(outcome, 0) + 1
        print("| `%s` | %s | %s | %s | %s | %s |" % (m["id"], m.get("selection", ""), outcome,
              ", ".join(retained) or "-", ", ".join(experiment) or "-", seconds))
    print()
    print("Totals: " + ", ".join("%s %d" % kv for kv in sorted(totals.items())))


if __name__ == "__main__":
    main()
