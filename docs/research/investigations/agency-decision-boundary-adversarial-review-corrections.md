# Agency and Decision Boundary adversarial review — successor corrections

> **Artifact type:** Successor correction note to handed-off adversarial-review evidence
> **Corrected review coordinate:** `18018d3dbdee4fb9e5cb739585634563a5c3ec94` + `docs/research/agency-decision-boundary-adversarial-review.md`
> **Reviewed source report coordinate:** `bf744af171d5a42a3c578d33836e8e45bd33b0fe` + `docs/research/agency-decision-boundary-report.md`
> **Correction baseline (live `main`):** `cbfcd36fb8f72ac39cc2896de97cc0113a22e41b`
> **Workspace predecessor:** `794b65b70c1849e13eae592d0bce053006382050`
> **Authority:** Research evidence only. This note does not create or amend architecture, an ADR, planning, or production types.
> **Disposition impact:** The original `ACCEPT WITH QUALIFICATIONS` disposition remains unchanged.

This note does **not** replace, rewrite, or claim to be a new independent adversarial review. It preserves the exact handed-off review revision above and narrows claims whose wording exceeded current repository authority or evidence. Any later reconciliation must read this note together with the original review; where the two conflict on the points below, this successor correction controls.

## 1. `RevisionRecorder.source` / `subject` decomposition is not established

### Superseded wording

The original review states as repository fact that `RevisionRecorder.source` is a recording mechanism/channel while `RevisionRecorder.subject` is the person-like/actor-ish value, and later restates that classification as a headline audit result.

Current repository authority does not justify that exact semantic split.

ADR-0008 says the `recorder` identifies the human, service, agent, import process, or other source that caused Arcogine to record the revision, and that the minimum contract may represent that identity with a small `source` / `subject` value. The implementation Javadoc says only:

> "Identifies the source and subject that caused a revision to be recorded."

Call sites such as `RevisionRecorder("test", "operator")` illustrate usage; they do not establish a normative ontology in which `source` is always mechanism and `subject` is always actor/person.

### Corrected contract

> `RevisionRecorder` as a whole is persisted **recording provenance** identifying what caused Arcogine to record a controlled revision. Its internal `source` / `subject` decomposition is currently underspecified by durable authority.

Therefore:

- do not reinterpret `RevisionRecorder.source` as a canonical mechanism/channel field;
- do not reinterpret `RevisionRecorder.subject` as a canonical actor/principal field;
- do not mechanically migrate either slot into any future actor identity type;
- preserve the broader finding that existing attribution-shaped strings carry heterogeneous semantics and cannot be collapsed safely by field-name matching.

The review's conclusion that there is **no safe mechanical migration** from today's provenance strings into a shared actor abstraction still stands and is strengthened by this correction.

## 2. "Platform Agent is unrepresentable" is too strong

### Superseded wording

The original surviving invariant says cases 2, 5, and 17 make a platform `Agent` concept "unrepresentable."

Those proving cases falsify **Agent designs that collapse or hard-bind distinct roles** such as actor, controller/decision source, and subject. They do not prove that every conceivable generalized `Agent` abstraction is impossible. A generalized concept could compositionally preserve those roles rather than collapse them.

### Corrected contract

> **No platform-level `Agent` abstraction is currently justified.** Designs that collapse actor, controller/decision-source, and subject roles fail the proving cases, and no additional cross-case invariant has been found that warrants introducing a shared platform `Agent` concept now.

This is intentionally an evidence/necessity claim, not an impossibility theorem. Reconciliation should preserve the distinct-role invariants and the refusal to introduce a platform `Agent` under current evidence, while avoiding language that forecloses all future compositional designs.

## 3. Actor-identity lifecycle research must remain semantically independent of ADR-0013

### Superseded wording

The original review recommends that actor-identity lifecycle/equality/federation be answered with "one identity lifecycle/equality discipline shared with ADR-0013's unresolved durable operational-history identity."

That couples two different referents too strongly.

ADR-0013 explicitly distinguishes **actor identity** from the durable Operational continuation identity it is trying to define. Similar methodological questions — namespace, equality, rename, retirement, federation — do not establish that the answers should be shared.

### Corrected contract

> If actor identity becomes material enough to research, admit it as a **separate bounded research question** with its own referent, equality, lifecycle, namespace, and federation semantics. It may reuse the same research method and compare against ADR-0013 as a semantic neighbor, but it must not inherit Operational-continuation identity rules by default.

The relevant distinction is:

- actor identity answers **who/what is the attributable party**;
- Operational identity answers **which independently continuing accountable operational history this fact belongs to**.

They may correlate. They are not the same identity problem.

## 4. "No ADR is warranted now" must remain conditional, not permanent

### Superseded wording

The original review states categorically that "No ADR is warranted now" because the surviving content is largely refusal-to-build plus vocabulary discipline.

That is reasonable for the current evidence, but reconciliation must not turn it into a permanent architectural rule.

### Corrected contract

> **Under current evidence and current consumers, a new Agency-specific ADR is not justified.** Architecture prose or explicit no-action reconciliation is sufficient if it merely records the present distinctions, qualifications, and non-introduction decisions.
>
> If later reconciliation introduces hard-to-reverse persisted/public identity semantics, permanent prohibitions, cross-module equality rules, or another durable contract that constrains future implementations materially, the ADR threshold must be reconsidered under normal ADR policy.

In other words: "no ADR now" is a conclusion about the present decision surface, not an exemption from ADR discipline for future Agency-related semantics.

## 5. Shared-actor implementation trigger must be semantic, not numeric

### Superseded wording

The original review gives a concrete trigger for a shared actor type: "two consumers in different modules ... at least one committed implementation work."

That is a useful planning heuristic, not a durable semantic rule. The number two has no architectural significance by itself.

### Corrected contract

> Introduce a shared actor identity/value type only when **multiple concrete consumers demonstrate the same equality, namespace, lifecycle, and interoperability contract strongly enough that domain-local representations would duplicate one semantic invariant**.

A committed implementation consumer is strong evidence because it makes the requirement real, but no magic consumer count or module count should be encoded as an architectural threshold.

The current conclusion remains: today's evidence does not justify a shared actor value type.

## 6. Effect on reconciliation handoff

The Agency reconciliation should preserve these corrected conclusions:

- there is no platform-level `Agent` abstraction justified by current evidence;
- actor, controller/decision source, subject, operation, realization/transition, and observation provenance must remain semantically distinguishable where relevant;
- identity equality/lifecycle for any future actor identity is unresolved and must not be smuggled into a value type;
- existing provenance strings, including `RevisionRecorder.source` and `.subject`, must not be mechanically reinterpreted or migrated;
- actor-identity research, if admitted, is a separate question from Operational continuation identity;
- no Agency-specific ADR is needed **under current evidence** unless reconciliation itself creates an ADR-grade durable commitment;
- a future shared actor type should be justified by demonstrated common semantics across real consumers, not by a fixed numeric threshold.

Nothing in this correction changes the original review's `ACCEPT WITH QUALIFICATIONS` disposition, historical review baseline, or reviewed source-report coordinate.