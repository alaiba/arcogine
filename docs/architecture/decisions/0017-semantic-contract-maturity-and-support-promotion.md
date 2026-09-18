# ADR-0017: Semantic-contract maturity and scoped support promotion

Status: Accepted
Date: 2026-09-18
Supersedes: ADR-0014

## Context

Arcogine names several semantic contracts whose exact meaning must stay attributable over time:
the `factory-model:v1` fingerprint policy ([ADR-0006](0006-durable-semantic-fingerprint-contract.md)),
the unreleased `factory-model:v2` design ([Factory Model v2 Canonicalization](../factory-model-v2.md)),
the `engine-semantics:v1` interpretation ([ADR-0015](0015-engine-semantics-identity-and-reproducibility.md),
[Engine Semantics v1](../engine-semantics-v1.md)), controlled revision history
([ADR-0008](0008-controlled-revision-identity-and-lineage.md)), the supported runtime contract
([ADR-0011](0011-runtime-observation-and-event-contract.md),
[ADR-0012](0012-external-interchange-and-serialization-boundaries.md)), Governance evidence
([ADR-0016](0016-governance-evidence-provenance.md)), and durable operational identity
([ADR-0013](0013-durable-operational-identity.md)).

Those decisions do not agree on **when** a named contract acquires a future support obligation or
**what** that obligation is. ADR-0006 freezes a fingerprint grammar once the implementation ships.
ADR-0014 promised permanent verifier/decoder retention for *every* released Factory policy and
automatic `factory-model:vN+1` progression whenever an authored fact cannot be represented. The V2
specification froze its grammar on the first fingerprint produced by a shipped implementation *or*
recorded against a controlled revision. ADR-0015 promises attribution plus a verifiable definition
but explicitly not permanent re-execution. ADR-0012 defers outward HTTP compatibility until the
domain contract stabilizes. ADR-0016 and ADR-0013 accept logical invariants while deferring their
representations. The repository therefore already distinguishes several kinds of promise, but the
trigger for each is inconsistent, "ships" is undefined for a repository with no releases, and a
newly named contract can become a permanent support commitment days after acceptance, before its
implementation and consumers have exercised it.

A bounded high-risk research question investigated this lifecycle, and an independent adversarial
review accepted its conclusion with qualifications. This ADR records the decision that survived and
every qualification the review made binding. The exact research evidence coordinates are recorded
in the delivery history of the change that accepted this ADR; the rules below are complete without
them.

Two facts constrain this decision and are recorded here so it stays intelligible:

- The repository owner has stated, as a maintained current constraint in the
  [Architecture Overview](../overview.md#current-implementation-constraints-mvp), that **this
  repository is the complete constituency of every Arcogine contract**: no external deployments,
  external consumers, externally retained revision stores or result histories, releases, packages,
  or images exist. Every consumer of a semantic identity is in-repository code, tests, or
  documentation, or does not yet exist. This decision relies on that statement; it must be updated
  before the first external artifact exists.
- The sibling question of how authored Factory semantics compose — whether spatial and other
  concerns are a linear whole-model `factory-model:vN` progression or independently applicable
  semantic components — is a separate research question. This ADR deliberately does not decide it.

ADR-0014 is superseded because two of its decisions are automatic future-support triggers that the
accepted rule replaces. Every other ADR-0014 decision remains current Factory architecture and is
restated below, with its original numbering, so that superseding ADR-0014 discards nothing that is
still in force.

## Decision

### 1. Durability is a scoped promise, and its dimensions stay distinct

A semantic contract's "durability" is never one undifferentiated guarantee. The following are
distinct obligations, each of which a promise may include or omit explicitly:

| Dimension | Question it answers |
|---|---|
| **historical content identity** | which authored content a `ModelFingerprint` or other content identity denotes, under which exact policy revision |
| **controlled occurrence identity** | which accepted historical occurrence a `ControlledRevisionId` or comparable identity denotes |
| **Engine interpretation identity** | which result-affecting interpretation an `EngineSemanticsVersion` denotes |
| **release** | that a distributable implementation with declared capabilities exists; build identity is diagnostic, never semantic identity |
| **retention** | for how long, and under whose custody, the definition, artifacts, fixtures, and records needed to explain a historical fact are kept |
| **decoding / resolution** | that a retained historical artifact can be decoded and verified against its exact definition revision |
| **execution support** | that an implementation executes a design or historical record under a given interpretation |
| **outward compatibility** | which external clients/artifacts interoperate across specified versions, with which migration and failure behavior |

Current normative authority — the one meaning implementations and consumers must agree on today —
does **not** automatically create any of these future obligations. Supersession changes what is
current; it never changes what an older artifact meant.

### 2. Shared minimum rules apply to every semantic contract, proving or promoted

1. **An exact referent is non-rebinding immediately.** A published artifact, an accepted
   occurrence, an attributed run, or an identified definition revision never silently acquires
   new content or meaning. This holds from creation/acceptance, including during proving.
2. **Every semantic contract has an exact definition revision**, and every semantic change during
   proving creates a new exact revision. An identifier, once bound to a meaning, is never reused
   for a changed meaning. An unqualified name, `latest`, a branch tip, or a build alone never
   identifies a contract revision.
3. **Introduction and promotion are distinguishable decisions.** They need not occur in different
   changes or on different dates: a contract whose evidence is complete at introduction may be
   introduced with its promotion record. What is required is that the record content (§6) exists;
   a separate ceremony is not.
4. **Release alone is not evidence of maturity.** A release may enact a promotion; it cannot
   substitute for its record.
5. **Existing commitments are never silently relabelled or rewritten.** A promoted promise is
   narrowed or withdrawn only through an explicit superseding decision that accounts for the actual
   constituency and retained artifacts (§9).
6. **A proving label never licenses reinterpretation.** A run keeps the interpretation it was
   established with; an artifact keeps the definition revision it was produced under.
7. **This decision changes no implementation semantics.** Adopting the lifecycle rule does not
   change any fingerprint byte, identity value, dispatch outcome, or persisted record.

### 3. Every contract is explicitly proving or promoted; silence is never durability

A semantic-contract revision is in exactly one of two states:

- **proving** — normative for current supported work, exact and non-rebinding, but carrying no
  future support promise beyond what its custody declaration (§4) and exit condition (§5) state;
- **promoted** — bound by a promotion record (§6) to explicit scoped promises and the evidence that
  justified them.

The introducing decision of every new semantic contract must state which it is. **The default for
silence is proving without promise:** a contract whose introducing decision neither declares a
scoped support promise with its evidence gate satisfied nor explicitly marks the contract as
proving is non-durable but non-rebinding. Silence never implies durability, and the absence of a
declaration is a defect in the introducing decision, not a promise.

A support obligation arises from exactly two mechanisms:

1. an explicit published promotion record; or
2. acceptance of an artifact or record into custody declared retained (§4).

Unobserved third-party reliance is an inventory risk to be recorded and addressed, not a mechanism
by which Arcogine acquires a support promise.

### 4. Custody is declared by the holding store or authority, and retained is the default

Whether a persisted artifact or record is **disposable** or **attributable and retained** is
determined by the **declared custody of the store or authority holding it**, not by whether some
acceptance operation was invoked:

1. An artifact is disposable only when it is held in custody explicitly declared **ephemeral**:
   test fixtures, temporary authorities, scratch stores, and comparable declarations.
2. Anything accepted into a store or authority without an ephemeral-custody declaration is
   **retained for the promised horizon** of the promise governing that store, and inherits the
   obligations that custody implies.
3. **A retained Factory or Governance authority may accept an artifact produced under a proving
   semantic contract only when the retained record preserves the exact definition revision the
   artifact was produced under together with the applicable custody declaration and support
   horizon.** An authority that cannot record both must reject such an artifact explicitly with an
   unsupported-policy or equivalent failure; it must never accept it and later resolve it under a
   different definition or fail silently.
4. Resolving a retained artifact produced under a proving revision is available only while that
   exact definition revision is retained; its custody declaration says for how long. A retained
   copy of an identifier or a digest without its definition revision is not a readability
   guarantee.

The Governance architecture owns the executable form of this rule for the controlled-revision
authority. The current authoritative store already refuses artifacts whose policy has no
registered verifier and registers only the promoted `factory-model:v1` policy, so no proving-policy
artifact can be accepted anywhere today; the recording obligation in item 3 becomes an executable
requirement of the first change that registers a proving-policy verifier.

### 5. Every proving revision declares an exit condition

A proving semantic-contract revision must declare either:

- a **custody horizon** — the bounded retention after which its artifacts and definition may be
  discarded; or
- a **concrete retirement or promotion trigger** — the observable event that ends the proving
  state by promotion, supersession, or explicit retirement.

A compatibility period, transition period, or proving state with no end condition is not a valid
proving state. Where an existing surface is discovered in that condition, the reconciliation that
discovers it assigns the smallest truthful exit condition rather than leaving it open.

The repository's live example is the legacy `FactoryModelVersion.contentHash()` provenance surface,
classified in §10.

### 6. Promotion is an explicit record with fixed content

A durable promotion is recorded in:

- an ADR; or
- for a semantic contract whose governing authority is a normative specification, a **dated
  promotion section in that specification**, subject to the same independent review an ADR
  receives.

Every promotion record names:

1. the **exact contract revision** being promoted;
2. the **scoped promises** being made, in the §1 dimensions;
3. the **supported scope** — which inputs, artifacts, consumers, and environments the promises
   cover;
4. the **evidence relied on**;
5. the **affected consumers and artifacts**, including retained histories;
6. the **evolution obligations** — how a successor is introduced and how the old identity is kept
   truthful;
7. the **retention and retirement obligations** — what is kept, for how long, and what retirement
   may remove.

Every semantic contract's owning durable document must carry one **compact support statement** so
a consumer does not reconstruct the promises from several historical ADRs. The statement
distinguishes at least: proving versus promoted; the exact definition revision or identity; the
historical decoding/resolution obligation; execution support, if any; outward compatibility, if
any; and the retention horizon or retirement rule where applicable. Where the owning authority is a
semantically immutable ADR, the support statement lives in the maintained current-state
architecture surface and this ADR carries the promotion record.

Promotion evidence is proportionate to the promise. Before a promise is recorded, the following
must be settled for the promised scope: the referent, equality, non-rebinding boundary, ordering
and normalization, ownership, and error behavior; for an executable or serialized compatibility
promise, an end-to-end producer-to-consumer path over the whole promised scope including rejected
inputs; at least one concrete use plus one independent challenge to the producer's assumptions (a
distinct consumer task, implementation, or independently derived oracle — a second copy of the same
happy-path test does not qualify); independently expected outcomes and negative cases, not only
round-trips through the same implementation; and a rehearsed evolution, coexistence-or-refusal,
migration classification, retirement, and missing-history case. A cross-language reproducibility
claim needs an independent implementation or an independently implemented conformance harness. A
purely logical invariant (for example "an accepted occurrence cannot be rebound") may be accepted
before code; that does not confer an implemented support claim. High-risk promises — identity,
persistence, history, compatibility — require an independent adversarial pass on the maturity
assessment before promotion, using the repository's existing research standard rather than a new
process tier. The assessment answers one bounded question: *for this exact revision and declared
promise, what evidence shows the boundaries survived implementation and consumer pressure, what
plausible remaining discovery would force a break, and why accept that risk now?*

### 7. Pre-release correction of a normative-specification contract freezes by exercised section

A contract whose authority is a normative specification may be corrected while proving, subject to
§2. Once any attributed run, published artifact, or accepted record has **exercised** a rule or
section of that specification, that rule is frozen exactly as a released rule under the owning
identity rule (ADR-0006 for fingerprint grammar; ADR-0015 decision 12 for Engine semantics). Only
sections no attributed run, artifact, or record has exercised may be corrected, each correction is
recorded in the specification with its date and the rule changed, and the specification defines
what "released" means for that contract. After a section is exercised, a later semantic correction
requires a new semantics identity rather than reinterpretation.

[Engine Semantics v1](../engine-semantics-v1.md) records the release definition and current
section-level freeze status for `engine-semantics:v1`. ADR-0015's identity, non-rebinding,
attribution, fixture, and retirement decisions are unchanged by this rule.

### 8. Factory model policy rules carried forward from ADR-0014

ADR-0014's field membership, validation predicates, and compatibility rules remain current Factory
architecture. They are restated here with ADR-0014's numbering so that its supersession discards no
still-current constraint. Decisions 8 and 12 are replaced; every other decision is carried forward
unchanged in substance.

1. **`factory-model:v1` remains immutable permanently.** Existing fingerprints, canonical
   artifacts, controlled revisions, and historical resolution are never rewritten or rederived
   under a newer policy.
2. **Authored spatial semantics use `factory-model:v2`.** V2 is a complete canonical Factory
   semantic artifact, not a spatial sidecar fingerprint. Its canonical byte grammar is defined
   normatively in [Factory Model v2 Canonicalization](../factory-model-v2.md); this ADR remains the
   authority for V2's semantic field membership, validation predicates, and compatibility rules.
   Whether V2's whole-model shape is the correct first successor to V1 is the open Factory
   composition question; nothing here promotes V2 (§10).
3. **V2 is exactly V1 semantics plus these five required authored additions:**

   | Addition | Meaning | Validation | Zero legal? | Fingerprinted? |
   |---|---|---|---|---|
   | floor width / height | plant extent in integer cells | each `>= 1`; the exact maximum-transfer predicate below must hold | no | yes |
   | resource position `x` / `y` | minimum-coordinate reference cell of the resource footprint | `x >= 0`, `y >= 0`; footprint occupies the exact cells defined below and must fit inside the floor | yes (`0,0` is valid) | yes |
   | footprint width / height | integer cells occupied by the resource from its reference cell | each `>= 1`; distinct resource footprints must not overlap | no | yes |
   | `ticksPerCell` | authored material-handling rate magnitude | integer `>= 0`; the exact maximum-transfer predicate below must hold | yes | yes |
   | `handlingTicks` | authored fixed overhead applied once per inter-resource transfer | integer `>= 0`; the exact maximum-transfer predicate below must hold | yes | yes |

   The position anchor is part of V2 model semantics: for a resource at `(x,y)` with footprint
   width `w` and height `h`, the footprint occupies exactly the integer cells `x..x+w-1` by
   `y..y+h-1`. A footprint fits inside a floor of width `W` and height `H` iff `x >= 0`,
   `y >= 0`, `w >= 1`, `h >= 1`, `x + w <= W`, and `y + h <= H`, evaluated with overflow-safe
   arithmetic. Two resources overlap iff those occupied-cell sets intersect.

   V2 publication must also prove the spatial transfer-duration magnitude is representable for
   the farthest possible pair of reference cells in the authored floor. With floor dimensions `W`
   and `H`:

   ```text
   maxManhattanDistance = (W - 1) + (H - 1)
   maxTransferDuration = handlingTicks + ticksPerCell * maxManhattanDistance
   ```

   Publication accepts the artifact only when every subtraction, addition, and multiplication in
   that predicate is representable in the runtime tick-duration type and `maxTransferDuration` is
   representable there. This bounds the derived transfer duration itself; it does not claim that
   adding an otherwise valid duration to an arbitrarily extreme current `SimTime` can never
   overflow, which remains the existing runtime time-addition validation's responsibility.

   All five additions are mandatory in a V2 artifact. Changing any of them changes the authored
   Factory design and therefore changes `ModelFingerprint`.
4. **The five additions are model facts, not Engine policy.** Distance metric, rounding,
   zero-distance behavior, destination binding, reservation, and transfer lifecycle do not belong
   in the model fingerprint.
5. **Orientation is not part of V2**, nor are paths, graph edges, aisles, conveyors, transport
   resources, obstacles, congestion, floor identity, connection points, authoritative animation
   coordinates, or route topology.
6. **Footprint remains canonical even though Engine Semantics v1 does not use it in transfer
   distance.** A later Engine semantics version may interpret the same V2 footprint differently
   without requiring every V2 design to be re-fingerprinted.
7. **Fingerprint-policy version and controlled-revision identity remain distinct.** A
   `ControlledRevision` references exactly one `ModelFingerprint`; lineage may cross Factory
   model-policy versions without rewriting either artifact.
8. **Historical artifact resolution is policy-aware, and its permanence follows each policy's
   promise.** *(Replaces ADR-0014 decision 8.)* Factory retains the verifier/decoder necessary to
   resolve every **promoted** Factory model policy for the horizon its promotion record states;
   for `factory-model:v1` that horizon is permanent (§9). A **proving** policy's resolution
   obligation follows its custody declaration and exit condition (§4, §5), not an automatic
   permanent promise. Registering any additional policy must never make historical V1 revisions
   unreadable or unverifiable.
9. **A V1 model remains fully supported under its existing non-spatial semantics.** V1 has no
   spatial facts and therefore no spatial transfer behavior; this is the truthful execution of a
   design that never authored spatial semantics, not a degraded mode.
10. **There is no automatic V1-to-V2 lift.** Arcogine never synthesizes position, footprint,
    `ticksPerCell`, or `handlingTicks` for an existing V1 artifact.
11. **Cross-policy comparison is explicit.** A semantic `ChangeSet` must not silently span V1 and
    V2 by inventing layout facts. Before the first V1-to-V2 controlled transition, Arcogine must
    provide multi-policy artifact resolution with a registered V2 verifier/decoder and either an
    explicit model-policy migration classification or an explicitly chosen common semantic
    representation for any fine-grained comparison that claims equivalence; the narrowest
    mechanism the real transition requires, not a generic migration framework.
12. **Evolution invariant.** *(Replaces ADR-0014 decision 12.)* When a behaviorally relevant
    authored fact cannot be represented without changing the meaning of a promoted Factory model
    policy, Arcogine introduces a **new exact definition revision** for the changed semantics. That
    revision is proving until promoted under §6; the promoted policy it succeeds remains immutable
    and resolvable exactly as its promotion record promises; fingerprints are never rewritten;
    lineage may continue across policies; cross-policy comparison remains explicit. Whether the new
    revision is a linear whole-model `factory-model:vN+1`, a compositional component, or another
    structure is decided by the Factory composition result, not by this invariant. Nothing about
    introducing a new revision automatically extends permanent resolution, coexistence machinery,
    or execution support to it.

### 9. Current promoted contracts and their grandfathered promises

The following promises were made before this decision and are **retained**, each deliberately
re-examined against the complete in-repository constituency. Retention rather than narrowing is
chosen wherever narrowing would produce no concrete simplification, would require a migration or
refusal path with no beneficiary, or would remove a truthfulness rule. None of these promises may be
narrowed later except through an explicit superseding decision that inventories retained artifacts
and dependents.

**`factory-model:v1` — promoted.** Authority: ADR-0006 (grammar, vectors, immutability) and §8
above (resolution, execution support, no lift). In-repository dependents: `FactoryModelVersion`
publication and fingerprinting, `FactoryModelArtifactV1` decoding, the controlled-revision
authority's artifact verification and historical resolution, `FactoryModelSemanticComparator`,
`ConformanceEvaluator`/`ChangeSetFactory`, runtime observation/event provenance, and the pinned
golden vectors.

| Promise | Current authority | Decision | Justification |
|---|---|---|---|
| grammar immutability; the `factory-model:v1` identifier is never reused | ADR-0006 | retain | a digest-bearing identity is truthful only if its grammar never changes; every in-repository consumer relies on it |
| golden vectors are part of the contract | ADR-0006 | retain | the vectors are the executable definition of the grammar; dropping them would weaken, not simplify |
| cross-language reproducibility | ADR-0006 | retain as a **promise not yet independently demonstrated** | the grammar is language-independent by construction; no independent implementation or another-language harness exists, so the support statement must say so rather than claim demonstrated interoperability |
| permanent verifier/decoder retention and historical resolution | §8 item 8 (formerly ADR-0014 decision 8) | retain | the decoder exists, is small, and has live dependents; narrowing would need a demonstrated reconstruction/refusal path with no beneficiary |
| full execution support under non-spatial semantics | §8 item 9 | retain | every runnable design in the repository is a V1 design |
| no automatic V1-to-V2 lift | §8 item 10 | retain | a truthfulness rule, not a cost |

**`engine-semantics:v1` — attribution and definition promoted; full release pending.** Authority:
ADR-0015 and [Engine Semantics v1](../engine-semantics-v1.md). In-repository dependents:
`FactoryRuntime`'s fixed `EngineSemanticsVersion`, the dispatch, derived-result, and identity
conformance suites, and the session/control acceptance suite.

| Promise | Current authority | Decision | Justification |
|---|---|---|---|
| exact attribution of every run to its semantics identity | ADR-0015 decisions 1, 7, 8, 10 | retain | reproducibility depends on it; already implemented at the runtime boundary |
| immutable specification for every rule an attributed run has exercised | ADR-0015 decision 12; §7 above | retain | rules exercised by attributed runs are frozen; unexercised sections follow the §7 correction rule recorded in the specification |
| fixture definitions survive retirement | ADR-0015 decisions 13, 14, 16 | retain | attribution plus a verifiable definition is the promised durability; permanent re-execution was never promised |
| retirement removes executability, not provenance | ADR-0015 decision 16 | retain | consistent with §1; no narrowing is possible without breaking historical attribution |

**Supported runtime contract and outward transport.** Current semantic authority under ADR-0011;
outward HTTP/OpenAPI compatibility is deliberately not promoted, per ADR-0012. Unchanged.

**Governance evidence and durable operational identity.** ADR-0016 and ADR-0013 are accepted
logical invariants whose representations remain deliberately deferred; neither is an implemented
format or storage support promise. Unchanged.

### 10. Current proving contracts and their exit conditions

**`factory-model:v2` — proving; no fingerprint policy released.** The model and validator exist;
no canonical bytes, fingerprint, verifier, or policy registration exist, and no V2 artifact can be
produced or accepted by any authority. Its grammar may be corrected by amending the specification
while proving. Exit condition: promotion by a dated promotion section in the V2 specification or an
ADR, after the Factory composition result is reconciled and the specification's golden vectors are
implemented; or retirement/supersession of the V2 design by that result. No V2 fingerprint may be
produced by a supported implementation, recorded against a controlled revision, or accepted into
retained custody before its promotion record or, for proving custody, its custody declaration
exists. Updating the V2 specification's lifecycle clause to this rule is not a release of a V2
fingerprint policy.

**Legacy `FactoryModelVersion.contentHash()` — proving in ephemeral custody, with a retirement
trigger.** ADR-0004 kept this Java-derived hash provisional and ADR-0006 retained it as legacy
provenance "while consumers are deliberately migrated", with no end condition. Inspection of every
in-repository use shows it flows only through in-process runtime/result provenance
(`IntegratedHandler` and `SimResult.modelContentHash`) and is asserted by tests; it is not exposed on
any outward API, CLI, or web surface, and no retained store holds a bare content hash. Its custody
is therefore ephemeral (in-process only), and its exit condition is **retirement when runtime and
result provenance migrate to `ModelFingerprint`** during the outward-consumer convergence work,
after which the legacy hash and its compatibility pin are removed. Until then its behavior stays
unchanged and ADR-0006's rule that bare content hashes are never reinterpreted as `factory-model:v1`
digests remains in force. This ADR admits the retirement as a planning dependency; it removes no
code.

### 11. Narrowing existing promises is permitted only through an explicit decision

Because the constituency statement in the Architecture Overview now records a complete inventory,
narrowing a grandfathered promise is *possible*. It is not done merely because it is possible. Any
future narrowing must inventory every retained history, export, dependent, and explicit promise;
classify each as retained relied-on history, migrated with attribution, invalid for new use, or
disposable; demonstrate reconstruction and refusal behavior before removing a reader; and be
recorded as a superseding decision. A changed interpretation creates a new definition and normally
a new artifact; an original occurrence's recorded basis remains original.

### 12. What this decision does not decide

- Factory model semantic composition: aggregate-versus-concern structure, component identities, or
  a replacement Factory schema shape. The Factory composition research owns that question, and §8
  item 12 is written so either outcome fits.
- Storage, codec, retention-horizon, or custody-declaration *implementations*, including how a
  retained authority records a custody declaration.
- Any support decision for a future external consumer, deployment, release, or package; the
  Architecture Overview constituency statement must change first.
- A generic versioning framework, semantic-versioning syntax, release calendar, plugin model,
  central maturity registry, or universal maturity enum. Two states and one record shape are the
  whole mechanism.
- Any Engine dispatch, transfer, scheduling, or analytics semantics; any Factory field, equality,
  or canonicalization change; any identifier rename or migration.

## Alternatives considered

### Eager durability at introduction, acceptance, or first artifact

The current repository rule in its strongest form: scope the initial contract narrowly, preserve
exact historical meaning, and mint a new identity when meaning changes. Retained as a valid *local*
choice and as `factory-model:v1`'s existing obligation, which this decision grandfathers. Rejected
as the repository-wide automatic rule: "ships" was undefined for a repository with no releases, the
proving window between acceptance and freezing has been days, and the unreleased V2 estate would
have acquired permanent multi-policy resolution and coexistence machinery before any consumer
existed. Note that everything provenance actually needs from this model survives in §2: every
exact revision is immutable and identifiers are never reused. What is no longer automatic is
"named ⇒ supported forever".

### Mandatory separate promotion for every contract

Normative-but-provisional first, then a later, always-separate promotion ceremony. Rejected in its
universal form: non-rebinding of accepted history cannot wait for promotion, and a narrow contract
whose evidence is complete at introduction gains nothing from a second date. Its forcing-function
value is kept by the record content in §6 and by independent review for high-risk promises.

### Release-bound durability

Free evolution until a named supported release. Rejected as the trigger: controlled history and
retained artifacts can be relied on before any release, and a release date supplies no codec,
interoperability, or migration evidence. A release remains a possible coordination point that may
enact a promotion under §6.

### Automatic identity-now, support-later; constituency-bound durability; support attached to release channels; durable core plus provisional extensions

Each was tested. The first is equivalent to §2 plus §6. The second collapses into §4's custody
rule. The third collapses into release-bound durability or into per-consumer agreements that have no
consumers. The fourth is the Factory composition question and is deliberately left to it.

### Narrowing `factory-model:v1` now that the constituency is complete

Considered per promise in §9. Rejected: no narrowing produces a concrete simplification, and each
would require a reconstruction/refusal path with no beneficiary. Retention of the small existing
decoder is cheaper than the decision to remove it.

### Editorial amendment of ADR-0014 instead of supersession

Rejected. Replacing decisions 8 and 12 changes accepted semantics; the ADR process requires a
superseding Accepted ADR, and this one restates every unaffected ADR-0014 decision so the historical
record can be marked Superseded without loss.

## Consequences

- New semantic contracts state proving or promoted explicitly; a silent contract is proving
  without promise. Reviewers can reject an introducing decision that says neither.
- A promotion is recognizable by its record content, not by a status word, a version spelling, or
  a release. The record location is fixed (§6), so a reviewer knows where to look.
- ADR-0014 decisions 8 and 12 no longer create automatic permanent resolution or automatic
  `vN+1` progression for future policies. `factory-model:v1`'s permanent resolution is unchanged.
- The V2 specification's lifecycle clause changes from an automatic freeze trigger to the §10 exit
  condition; V2 remains proving and unreleased.
- `engine-semantics:v1` gains a definition of "released" and a section-level freeze rule in its
  specification; no Engine behavior changes.
- The controlled-revision authority gains a semantic custody rule; the first proving-policy
  verifier registration must implement the custody-declaration recording in §4 item 3.
- The legacy content hash gains an exit condition owned by outward-consumer convergence planning.
- Planning that held V2 canonical identity, V1/V2 coexistence, and spatial activation on *this*
  question is unblocked on this question only; the Factory composition prerequisite remains.
- No implementation, migration, identifier rename, or behavior change is required or authorized by
  this decision.

### Reusable proving cases

Future promotion assessments and reviews should reuse these discriminating cases rather than
re-derive them: a design recorded, changed, and restored to equal content in a third controlled
revision must keep three occurrences and old identifiers resolving the original facts, with no
spatial facts defaulted into history; a runtime that reports one semantics identity while a
transfer path is unimplemented must remain attributable without claiming the whole specification
mature; a headless consumer that works while legacy transport exports internal events must not be
counted as a stable outward contract; a correction after a completed evaluation is a new
attributable record, never a rebinding; consequential reliance on a pre-release record requires the
accepted command, authority, result, and applied configuration to be frozen and reconstructable
before the act, and prototype status is no escape; a proving-stage equality correction (for example
splitting one `name` into an operational label and a display label) creates a new exact revision
with regenerated declared-disposable fixtures or migrated identified data, and never mutates the
promoted `factory-model:v1` meaning. The universal claims that failed and should not be reargued
without new evidence: first publication proves maturity; a formal release proves maturity;
provisional permits same-identity reinterpretation; every Accepted ADR freezes a concrete API;
every named version requires perpetual execution; absence of external release records proves
absence of users; and a mandatory later ceremony for an already-proven narrow invariant.

### Reopening triggers

- The constituency statement changes: an external deployment, consumer, retained store, release,
  package, or image comes into existence. Every affected promise must be re-examined before that
  artifact exists.
- A provisional artifact must justify an accepted consequential act but its definition or basis
  cannot be retained or reconstructed under its custody declaration.
- Exact revision pinning plus unsupported-version rejection prove insufficient to make a proving
  contract safe for an in-repository consumer without immediate permanent support.
- Applying §6 adds work without changing any support, retention, or design decision.
- The Factory composition result, spatial implementation, a second material Engine consumer task,
  or analytics ownership changes the frozen scope of a promoted contract; perform a
  boundary-specific maturity assessment while preserving existing historical meaning.

## Charter alignment

The decision keeps history truthful and attributable — every exact referent is non-rebinding from
creation — while stopping Arcogine from converting each development build, proving grammar, or
unreleased design into a permanent product commitment. It supports lifecycle continuity and
consequence-proportionate controls: obligations follow declared custody and explicit promises, and
consequential reliance is an admission constraint rather than a trigger that manufactures maturity.

## Related decisions

- [ADR-0004](0004-model-identity-revision-lineage-and-external-change-control.md) — the
  provisional-versus-durable identity distinction this decision generalizes.
- [ADR-0006](0006-durable-semantic-fingerprint-contract.md) — the `factory-model:v1` grammar and
  immutability rule, retained unchanged and grandfathered here.
- [ADR-0008](0008-controlled-revision-identity-and-lineage.md) — controlled occurrence identity,
  unchanged.
- [ADR-0011](0011-runtime-observation-and-event-contract.md) and
  [ADR-0012](0012-external-interchange-and-serialization-boundaries.md) — the supported runtime
  contract and its deferred outward compatibility, unchanged.
- [ADR-0013](0013-durable-operational-identity.md) and
  [ADR-0016](0016-governance-evidence-provenance.md) — accepted logical invariants with deferred
  representations, unchanged.
- [ADR-0014](0014-factory-model-semantic-policy-evolution.md) — superseded; its unaffected
  decisions are restated in §8.
- [ADR-0015](0015-engine-semantics-identity-and-reproducibility.md) — Engine identity and
  reproducibility, unchanged; §7 records how its specification is corrected before release.
