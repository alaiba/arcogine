# 9. What "adversarial" means

Part of the [research operating model](../researching.md); it is mandatory whenever the entry point routes your task here.


Do not use "adversarial" as a synonym for "be critical" or "think of some objections." Arcogine defines it operationally:

> An **adversarial review** is an independent attempt to falsify the report's load-bearing conclusions, performed without responsibility for defending the original recommendation.

An adversarial reviewer specifically attempts to discover, as applicable:

- an omitted viable candidate;
- a proving case the proposed semantics cannot survive;
- a hidden implementation/infrastructure assumption;
- an unstated equality, identity, continuity, lifecycle, ordering, or authority assumption;
- an ownership inversion;
- conflict with repository authority;
- stale baseline evidence;
- a source that does not actually support the claim attributed to it;
- a misleading analogy;
- evidence of *possibility* being treated as evidence of *necessity*;
- a discretionary cost, or an equality requirement no consumer actually has, presented as a semantic falsification;
- an abstraction generalized beyond its actual consumers;
- an unresolved question silently declared settled;
- a conclusion stronger than its evidence.

When a report proposes a potentially transferable result, challenge that scope explicitly as part of the same adversarial pass: seek Arcogine-specific causes, contrary contexts, design choices presented as necessities, and narrower claims that survive where the broader formulation does not.

A clean adversarial review — one that finds nothing that survives scrutiny — is a valid and useful result. Do not optimize for producing findings, and do not establish a minimum finding count. An adversarial reviewer who manufactures a finding to justify the pass has failed the same way a PR reviewer who "optimizes for finding something wrong" has failed (`docs/development/reviewing.md`).

## Report input integrity

An adversarial review is a review of a specific report revision, not of a conversational summary of that report. Before substantive report-specific work, the reviewer must verify that the complete original report is available and record its evidence coordinate: temporary research-evidence workspace branch, exact report commit SHA, report path, and the report's stated research-baseline SHA. This check may confirm file identity and completeness without deeply consuming the recommendation, preserving the anchoring-control sequence below.

If the complete report cannot be resolved, stop the report-specific review and return `INPUT BLOCKED — ORIGINAL REPORT NOT AVAILABLE`, naming the missing coordinate or artifact. This is not one of the four adversarial-review dispositions because the report was not actually reviewed. Do not infer the report from the review prompt, reconstruct its claims from a summary, or issue `ACCEPT`, `ACCEPT WITH QUALIFICATIONS`, `MORE EVIDENCE REQUIRED`, or `REOPEN` against an unavailable report. Independent reconstruction performed in that situation may be useful new research evidence, but it must be labeled as such rather than presented as a disposition on the missing report.

## Independence and anchoring control

For a **high-risk** adversarial review (§7), the reviewer must not simply be the same research run continuing to defend its own report.

Prefer, in order:

1. a different researcher, person, or model family, where practical;
2. otherwise, at minimum, a fresh isolated session/run with no responsibility for preserving the original conclusion.

Before deeply consuming the report's recommendation, an independent adversarial reviewer should, in order:

1. read the research question/brief;
2. re-ground in current repository authorities (per §2);
3. independently reconstruct the major constraints;
4. identify plausible candidate answers;
5. identify likely failure/adversarial cases;

and only then evaluate the report's own reasoning and recommendation. This sequencing reduces anchoring on the author's conceptual framing — a reviewer who reads the recommendation first tends to evaluate it on its own terms rather than discovering what an independent pass would have found.

Do not claim "independent adversarial review" if the independence condition was not actually achieved. If a report's self-challenge is the only adversarial pass performed — because independent review was unavailable, deferred, or rate-limited — say so plainly rather than presenting it with the weight of an independent review. A prior Arcogine investigation encountered exactly this limitation and disclosed it directly rather than overstating the pass; that disclosure is the expected standard of honesty, not an exceptional one.

## Adversarial-review outcomes

Use this compact disposition model:

- **ACCEPT** — the load-bearing conclusion survives the adversarial pass.
- **ACCEPT WITH QUALIFICATIONS** — the conclusion survives, but the identified qualifications must be part of any durable reconciliation.
- **MORE EVIDENCE REQUIRED** — no falsification was established, but the evidence is insufficient for decision-quality promotion.
- **REOPEN** — a load-bearing conclusion was falsified, or a viable omitted model materially changes the question.

Individual challenged claims may be tracked more granularly when useful, but do not build a heavyweight issue/finding ledger for research unless later evidence demonstrates an actual need for one — the Consistency agent's GitHub-Issues ledger exists because recurring drift detection needed cross-session continuity at scale; a single adversarial review does not automatically need the same machinery.

The review itself should state: what was challenged; what evidence was considered; the result; the effect on the report's conclusion; and any qualifications that must survive reconciliation.
