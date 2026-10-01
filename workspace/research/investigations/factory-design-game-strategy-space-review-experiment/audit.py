"""Independent CSV audit; never simulates Engine decisions or imports the author's classifier.

Run with Python 3 from anywhere. Original results are the sibling experiment/results;
new Java outputs belong in this directory's results/. Emits auditable JSON summaries.
"""
import csv
import itertools
import json
import math
from collections import defaultdict
from pathlib import Path

HERE = Path(__file__).resolve().parent
ORIGINAL = HERE.parent / "factory-design-game-strategy-space-experiment" / "results"
OUT = HERE / "results"


def read(path):
    with path.open(newline="", encoding="utf-8") as f:
        return list(csv.DictReader(f))


def frontier(entries):
    # Independent cost-per-completion reduction, then direct strict dominance.
    cost_at_time = {}
    for cost, tick, key in entries:
        cost_at_time[tick] = min(cost_at_time.get(tick, cost), cost)
    points = {(cost, tick) for tick, cost in cost_at_time.items()
              if not any(t <= tick and c <= cost and (t < tick or c < cost)
                         for t, c in cost_at_time.items())}
    return {(c, t, k) for c, t, k in entries if (c, t) in points}


def counts(row):
    return tuple(int(row[k]) for k in ("cutters", "assemblers", "twins", "inspectors", "flex"))


def price(row, prices):
    return sum(n * p for n, p in zip(counts(row), prices))


summary = {"frontier_contexts_checked": 0, "cells_checked": 0}
for order, subdir in (("C", "pass-1"), ("R", "pass-2/order-R")):
    directory = ORIGINAL / subdir
    contexts = defaultdict(list)
    for row in read(directory / "frontiers.csv"):
        contexts[row["context"]].append(row)
    cell_map = defaultdict(list)
    for row in read(directory / "cells.csv"):
        cell_map[row["cell"].rsplit("|f", 1)[0]].append(row)
    for context, expected in contexts.items():
        profile, quantity, family, rule, delta, phi = context.split("|")
        rows = read(directory / f"designs-{profile}-{quantity}.csv")
        assert len(rows) == 2112 and len({r["design"] for r in rows}) == 2112
        if family == "D":
            rows = [r for r in rows if r["flex"] == "0"]
        base = [100] * 3 if rule == "UNIFORM" else (
            [100, 300, 150] if profile == "P1" else [150, 200, 250])
        prices = [base[0], base[1], (int(delta[1:]) * 2 * base[1] + 50) // 100,
                  base[2], (int(phi[1:]) * max(base[1:]) + 50) // 100]
        entries = [(price(r, prices), int(r["completion"]), r["design"]) for r in rows]
        actual = frontier(entries)
        exp = {(int(r["cost"]), int(r["completion"]), k)
               for r in expected for k in r["members"].split()}
        assert actual == exp, (order, context, actual ^ exp)
        summary["frontier_contexts_checked"] += 1
        floor = min(t for c, t, k in entries)
        starter = next(t for c, t, k in entries if k == "C1A1T0I1F0")
        for row in cell_map[context]:
            target = floor + int(row["f"]) * (starter - floor) // 100
            cheapest = min(c for c, t, k in entries if t <= target)
            budget = cheapest * (100 + int(row["beta"])) // 100
            feasible = {(c, t) for c, t, k in actual if c <= budget and t <= target}
            assert (target, budget, len(feasible)) == (
                int(row["target"]), int(row["budget"]), int(row["feasibleClasses"]))
            summary["cells_checked"] += 1

cells = read(ORIGINAL / "pass-2/pass2-cells.csv")
pairs = read(ORIGINAL / "pass-2/pass2-pairs.csv")
original_cells_c = {r["cell"]: r for r in read(ORIGINAL / "pass-1/cells.csv")}
original_cells_r = {r["cell"]: r for r in read(ORIGINAL / "pass-2/order-R/cells.csv")}
summary["cells_with_order_dependent_target_or_budget"] = sum(
    (r["target"], r["budget"]) != (original_cells_r[k]["target"], original_cells_r[k]["budget"])
    for k, r in original_cells_c.items())
summary["robust_material_cells_without_same_surviving_pair"] = sum(
    r["robustMaterial"] == "true" and r["survivingPairs"] == "0" for r in cells)
summary["original_counts"] = {
    family: {field: sum(r["family"] == family and r[field] == "true" for r in cells)
             for field in ("strongC", "materialC", "materialR", "robustMaterial", "positive2")}
    for family in ("D", "F")}
summary["uniform_pair_separation_sensitivity"] = {}
for percent in (2, 5, 10, 20):
    qualifying = defaultdict(set)
    for pair in pairs:
        # Uniform outcome-separation test, additional to the author's structural explanation.
        if int(pair["cheaperT"]) - int(pair["fasterT"]) >= math.ceil(percent * int(pair["cheaperT"]) / 100):
            qualifying[pair["cell"]].add(pair["order"])
    summary["uniform_pair_separation_sensitivity"][percent] = {
        family: sum(r["family"] == family and qualifying[r["cell"]] == {"C", "R"}
                    for r in cells) for family in ("D", "F")}

# Reprice fixed executions; hold the actual challenge B=1062 and target=57 fixed.
summary["fixed_reference_economics"] = []
for flex_price in (250, 280, 300, 313, 330, 350, 375, 400, 450, 500):
    for order, subdir in (("C", "pass-1"), ("R", "pass-2/order-R")):
        rows = read(ORIGINAL / subdir / "designs-P2-N12.csv")
        entries = [(price(r, [150, 200, 360, 250, flex_price]), int(r["completion"]), r["design"])
                   for r in rows]
        feasible = sorted((c, t, k) for c, t, k in frontier(entries) if c <= 1062 and t <= 57)
        summary["fixed_reference_economics"].append({"flex_price": flex_price, "order": order,
                                                    "feasible_frontier": feasible})

# Dedicated positive counterexample exists inside the original admitted window.
summary["dedicated_positive_examples"] = [r for r in cells if r["family"] == "D" and r["positive2"] == "true"][:3]

order_rows = read(OUT / "identity-orders.csv")
groups = defaultdict(list)
for row in order_rows:
    groups[row["design"]].append(row)
summary["identity_sweep"] = {"runs": len(order_rows), "designs": len(groups), "ranges": {}}
for key, rows in groups.items():
    counts_by_type = [rows[0]["order"].count(c) for c in "CATIF"]
    expected_permutations = math.factorial(sum(counts_by_type))
    for n in counts_by_type:
        expected_permutations //= math.factorial(n)
    assert len(rows) == expected_permutations == len({r["order"] for r in rows})
    times = [int(r["completion"]) for r in rows]
    summary["identity_sweep"]["ranges"][key] = [int(rows[0]["cost"]), min(times), max(times), len(rows)]
    if key == "C1A1T0I1F1":
        assert all(int(r["flexAssembly"]) > 0 and int(r["flexInspection"]) > 0 for r in rows)

ranges = summary["identity_sweep"]["ranges"]
summary["reference_two_order_envelope_violations"] = []
for key, rows in groups.items():
    times = {r["order"]: int(r["completion"]) for r in rows}
    canonical = "".join(c * rows[0]["order"].count(c) for c in "CATIF")
    reversed_order = canonical[::-1]
    lo, hi = sorted((times[canonical], times[reversed_order]))
    if min(times.values()) < lo or max(times.values()) > hi:
        summary["reference_two_order_envelope_violations"].append(key)
# A candidate is certainly nondominated under every allocation of orders if even its
# slowest outcome is not dominated by any other design's fastest possible outcome.
summary["universally_nondominated_reference_designs"] = []
for key in ("C1A1T0I2F0", "C1A1T0I1F1", "C1A0T1I2F0"):
    cost, lo, hi, permutations = ranges[key]
    dominators = [other for other, (c, t, _, _) in ranges.items()
                  if other != key and c <= cost and t <= hi and (c < cost or t < hi)]
    assert not dominators, (key, dominators)
    summary["universally_nondominated_reference_designs"].append(key)

summary["candidate_thresholds_all_orders"] = {
    pct: ranges["C1A1T0I2F0"][1] - ranges["C1A1T0I1F1"][2] >= math.ceil(pct * 56 / 100)
    for pct in (2, 5, 10, 15, 20)}
summary["interventions"] = read(OUT / "interventions.csv")

(OUT / "audit-summary.json").write_text(json.dumps(summary, indent=2) + "\n", encoding="utf-8")
print(json.dumps({k: v for k, v in summary.items() if k not in
                  ("fixed_reference_economics", "identity_sweep", "interventions", "dedicated_positive_examples")}, indent=2))
print("Identity sweep:", len(order_rows), "runs across", len(groups), "designs")
for key in ("C1A1T0I2F0", "C1A1T0I1F1", "C1A1T0I0F2", "C1A0T1I2F0"):
    print(key, ranges[key])
