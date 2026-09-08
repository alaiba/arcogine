# Implementation Planning

> **Status:** Maintained delivery-policy index  
> **Scope:** Work that is sufficiently decided and bounded to be implemented, validated, sequenced, or explicitly blocked on a concrete prerequisite  
> **Authority:** Planning only; current capability remains defined by landed code/tests and current-state documentation, while durable semantics remain owned by product, architecture, and accepted ADRs

## Admission rule

`docs/planning/` answers:

> **Given what is already known or decided, what work can an implementer execute, in what order, and against what acceptance evidence?**

A topic belongs here only when all of the following are true:

1. the semantic or product decision needed to define the work is sufficiently settled;
2. the owning track/module or integration boundary is known;
3. prerequisites and dependency direction are explicit;
4. the concrete implementation responsibility is bounded;
5. acceptance evidence can be stated before coding begins; and
6. non-goals are explicit enough to prevent adjacent speculative work from entering the slice.

A blocked plan may remain when the implementation contract itself is settled and the blocker is a concrete prerequisite. When research must still decide **what the implementation should mean**, the exploratory material belongs in [`../research/`](../research/README.md). A small implementation-admission guard may remain at an established planning path only to state that no work is admitted, name the research/ADR blockers, and define promotion criteria; it must contain no candidate semantics or speculative delivery sequence.

## What does not belong here

Move or keep the following under `docs/research/` until they reach decision quality:

- product hypotheses and playtest questions;
- unresolved architecture or ontology questions;
- technology-selection investigations;
- alternative designs awaiting evidence;
- "when justified", "after concrete need", or similar untriggered abstractions;
- open-decision tables whose evidence has not yet selected an implementation contract;
- speculative future tracks or slices whose ownership or equality/lifecycle rules are unresolved.

Non-goals and explicit future exclusions may remain in an implementation plan when they protect the admitted slice from scope expansion. A statement that something is **not part of this implementation** is different from carrying an unresolved design programme in the plan.

## Promotion flow

```text
Research question
      |
      v
Evidence + conclusion
      |
      v
Product / architecture / ADR decision where required
      |
      v
Concrete implementation responsibility + acceptance evidence
      |
      v
docs/planning/
      |
      v
implementation / PR / validation
```

Temporary `PLAN-<TRACK>-<LOCAL-ID>` coordinates are assigned only after work has crossed this admission boundary. Research documents do not receive delivery coordinates.

## Maintenance

When a plan is reviewed:

- remove or relocate exploratory sections instead of letting them accumulate beside executable work;
- keep implementation status synchronized with landed evidence;
- retire completed historical notes once their durable meaning exists in current architecture/reference/ADRs and they no longer protect an active downstream implementation;
- link to research for unresolved adjacent questions rather than describing those questions inline;
- do not reopen accepted architecture inside an implementation plan unless implementation evidence demonstrates a contradiction that must be escalated.
