# ISA-95 Semantic Mapping

> **Status:** Maintained architectural reference  
> **Scope:** Mapping between Arcogine manufacturing-domain semantics and ISA-95 / IEC 62264 concepts  
> **Authority:** Describes current mappings, deliberate divergences, and design constraints; it does not establish ISA-95 conformance  
> **Related:** [Product Charter](../product/charter.md), [Architecture Overview](overview.md), [Standards Alignment](standards-alignment.md), [Factory-Design Game Consumer Initiative](../planning/factory-design-game-consumer.md)

## 1. Purpose

Arcogine uses ISA-95 as a semantic reference for manufacturing-domain modeling and enterprise/manufacturing integration. This document records:

- how current Arcogine concepts correspond to ISA-95 concepts;
- where the correspondence is exact, approximate, absent, or deliberately different;
- which domain distinctions future Arcogine changes should preserve;
- which ISA-95 concepts are useful now and which are premature;
- how a future interoperability adapter could map Arcogine data without forcing ISA-95 terminology into every internal type.

This document is intentionally broader than any one consumer initiative. The factory-design game is one use case that makes several model gaps actionable, but the mapping also informs future simulation, verification, digital-twin, scheduling, reporting, and industrial-integration work.

This document is **not**:

- an ISA-95 conformance statement;
- an implementation of the ISA-95 information models;
- an import/export or B2MML profile;
- a requirement to reproduce the complete ISA-95 object hierarchy;
- a roadmap;
- a mandate to rename clear Arcogine concepts solely to resemble a standard.

## 2. Reference boundary

ISA-95, also published internationally as IEC 62264, defines models, terminology, activities, object attributes, and information exchanges for integrating manufacturing operations with enterprise functions. Arcogine uses the series as a reference rather than claiming to implement it.

The parts most relevant to this mapping are:

| Part | Relevance to Arcogine |
|---|---|
| Part 1 — Models and Terminology | Manufacturing/control scope, hierarchy, functions, and shared terminology |
| Part 2 — Objects and Attributes for Enterprise-Control System Integration | Conceptual information exchanged between manufacturing and enterprise functions |
| Part 3 — Activity Models of Manufacturing Operations Management | Level-3 manufacturing-operations activities |
| Part 4 — Objects and Attributes for Manufacturing Operations Management Integration | MOM object models and attributes, including definition, schedule, execution, and performance concerns |
| Part 5 — Business-to-Manufacturing Transactions | Transactions and information exchanges between applications |

The latest ISA Part 1 publication is ANSI/ISA-95.00.01-2025. Other parts have independent publication dates. A newer Part 1 does not imply that Arcogine automatically conforms to the series or that every term below has a one-to-one mapping.

## 3. Alignment vocabulary

Every mapping or proposal in this document uses one of these dispositions:

| Disposition | Meaning |
|---|---|
| **Adopt** | Arcogine uses substantially the same concept and terminology |
| **Alias** | Arcogine preserves the semantic role but uses a clearer internal or consumer-facing term |
| **Diverge** | Arcogine intentionally models the concern differently; the difference must be explicit |
| **Extend** | Arcogine adds a concern that is outside, more specific than, or orthogonal to the ISA-95 mapping |
| **Defer** | The concept is relevant but no current use case justifies implementing it |
| **Refactor** | The current Arcogine model collapses distinctions that a concrete use case now requires separating |

These dispositions prevent two opposite mistakes:

1. renaming code without changing semantics and calling that standards alignment;
2. ignoring a useful standard distinction merely because Arcogine prefers simpler vocabulary.

## 4. Alignment dimensions and current position

Four different claims must remain distinct:

```text
Terminology alignment
    shared or explicitly mapped names

Semantic alignment
    corresponding domain meaning and invariants

Structural alignment
    equivalent separation of definitions, requests, schedules,
    execution state, and performance records

Interchange / conformance
    defined schemas, transactions, profiles, and validated compatibility
```

Arcogine's current position is:

| Dimension | Current assessment |
|---|---|
| Scenario vocabulary | Partial alignment: the schema uses `equipment`, `material`, `process_segment`, and `operations_definition` |
| Runtime terminology | Deliberately Arcogine-specific: `Machine`, `Job`, `Routing`, and `RoutingStep` are approachable aliases or partial analogues |
| Semantic alignment | Narrow production-execution concepts are mappable, but several mappings are approximate |
| Structural alignment | Limited: resource definitions, requests, execution, and performance are not consistently separated |
| Equipment hierarchy | Not implemented |
| Material/resource models | Minimal and specialized |
| ISA-95 transactions or interchange | Not implemented |
| Conformance claim | None |

The correct characterization is therefore:

> Arcogine is ISA-95-informed and semantically mappable across a narrow production-execution subset. Its scenario schema adopts selected ISA-95 terminology, but its runtime model does not currently implement the complete ISA-95 resource, hierarchy, schedule, performance, or exchange models.

## 5. Current concept mapping register

This table is the maintained working register. Update it when the domain model changes; do not preserve obsolete mappings merely for historical continuity.

| Arcogine concept | Current meaning | Closest ISA-95 semantic role | Mapping | Disposition | Current limitation or direction |
|---|---|---|---|---|---|
| Scenario `equipment` | Configured productive resource | Equipment | Strong vocabulary mapping | Adopt | The runtime object is still named `Machine` |
| `EquipmentConfig` | Name, concurrency, capacity, and setup parameters for one configured resource | Equipment information / resource properties | Partial | Alias | Definition and instance concerns are not separated |
| Runtime `Machine` | Operational resource instance with state, active jobs, queue, concurrency, capacity, setup time, and busy ticks | Equipment instance at approximately work-unit granularity | Approximate | Alias | No equipment class, capability, or hierarchy model |
| `MachineState` | `Idle`, `Busy`, or `Offline` | Equipment operational status / availability | Narrow | Alias | This is status, not equipment capability |
| Machine queue | Ordered work waiting for one machine | Execution scheduling / job-list state | Approximate | Alias | Must not be described as equipment capability |
| Machine concurrency | Number of simultaneously active jobs allowed | Resource capacity property | Partial | Alias | No generalized capability/capacity model |
| `capacityLiters` | Optional machine-specific volumetric property | Equipment property / capability parameter | Narrow | Alias | Specialized property, not a general resource requirement model |
| `setupTime` | Configured setup duration | Equipment or operation parameter | Partial | Alias | Current execution does not yet model full setup-state semantics |
| Scenario `material` / `MaterialConfig` | Named produced item linked to a routing | Material Definition analogue | Partial | Adopt at scenario boundary | Represents product-like output, not a generalized material model |
| Runtime `ProductId` | Identifier carried by jobs and routing lookup | Material Definition identifier | Weak | Refactor | No first-class runtime product/material definition behind the ID |
| Scenario `operations_definition` | Named ordered set of process segments | Operations Definition / Work Definition | Good vocabulary mapping | Adopt | Runtime equivalent is simplified `Routing` |
| Runtime `Routing` | Immutable ordered list of routing steps | Simplified Operations/Work Definition | Partial | Alias | Lacks explicit resource requirements, parameters, alternatives, and version semantics |
| Scenario `process_segment` | Named step, concrete equipment ID, and duration | Process Segment analogue | Partial vocabulary mapping | Adopt at scenario boundary | A true process segment is more abstract than one concrete machine assignment |
| Runtime `RoutingStep` | Step ID, name, concrete `MachineId`, and duration | Simplified work step / Process Segment analogue | Weak–partial | Refactor | Direct machine binding prevents capability pools and equivalent-resource dispatch |
| `Order` | Immutable request/accepted-order intent: product, quantity, agreed unit price, and creation time | Job Order / production request, approximating a production-order aggregate, extended with a commercial price | Partial | Alias | First-class and immutable, but still one-to-one with a `Job` and not a generalized production-order model |
| `Job` | Mutable execution/work-item analogue: status, current step, current machine, completion time, and a reference to its `Order` | Work item / job execution state | Partial | Alias | Request, execution, and result concerns are separated from `Order`, but `Job` still holds the whole `Order` rather than an ID-only reference |
| `JobStatus` | Queued, in progress, or completed | Work/job execution status | Good narrow mapping | Alias | Does not represent the complete requested/accepted/started/completed lifecycle |
| `OrderCreation` event | Acceptance/release fact that creates the `Order` and its associated `Job` | Job-order release / acceptance fact | Approximate | Alias | The event still creates exactly one order and one job together |
| `TaskStart` / `TaskEnd` | Actual operation execution facts | Work execution / performance facts | Good narrow mapping | Alias | No first-class operation-performance record exists beyond state and event history |
| `OrderCompleted` | Operational fact that one job completed its routing | Job response / production performance fact | Partial | Alias | Correlation is still by `jobId`; `Job` remains one-to-one with an `Order`, so this is only an approximate completion/performance fact |
| `Scheduler` | Deterministic ordering of simulation events | No direct ISA-95 object equivalent | Arcogine-specific | Extend | Simulation infrastructure, not an Operations Schedule by itself |
| `EventLog` | Append-only sequence of processed simulation facts | Performance/history source | Partial | Extend | Event history is not yet a structured ISA-95 performance model |
| `FactoryHandler` | Owner of machines, jobs, queues, routings, and production aggregates | Narrow production-execution function in a Level-3-like scope | Approximate | Alias | A class is not an ISA-95 level; Arcogine covers only a subset of MOM activities |
| Throughput, lead-time, backlog, utilization and related observations | Operational measures derived from simulation state and history | Operations Performance / manufacturing KPI information | Partial | Adopt or alias per KPI | Each KPI needs an explicit formula and semantic mapping before standards claims |
| Finance ledger and observations | Financial interpretation of completed operational work | Enterprise/business-side financial information | Adjacent, not one-to-one | Diverge | Deliberately separate from operational production truth |
| Economy/demand model | Offer price and demand-generation behavior | Business/planning input adjacent to Level 4 | Arcogine-specific | Extend | Not an ISA-95 enterprise-planning implementation |
| Proposed factory-floor position and footprint | Physical placement with transfer consequences | No one-to-one equipment-hierarchy mapping | Orthogonal | Extend | Must remain distinct from organizational/resource containment |
| Proposed resource pool / work center | Group of eligible resources and aggregate capacity | Work Center / resource scope | Potentially strong | Adopt or alias when implemented | Introduce only when it owns real dispatch, capacity, or reporting semantics |
| Enterprise / Site hierarchy | Organizational and physical scope above a factory | Enterprise / Site | Relevant but absent | Defer | No current multi-site or enterprise-scoped behavior |

## 6. Definition, request, execution, and performance

The most useful ISA-95 contribution to Arcogine is not vocabulary; it is the discipline of keeping different lifecycle concerns distinct.

### 6.1 Definition

Definition answers **what can be produced and how**:

```text
Product or Material Definition
Operations / Work Definition
Operation or Process Segment
Machine / Resource Definition
Resource capability and requirements
```

Arcogine currently represents part of this through `MaterialConfig`, `OperationsDefinitionConfig`, `Routing`, and `RoutingStep`. The main limitations are:

- no first-class runtime `Product` or generalized material definition;
- `RoutingStep` selects one concrete machine rather than expressing a resource requirement;
- no explicit definition version carried by in-flight work;
- machine definition and machine instance properties are combined.

### 6.2 Request and schedule

Request and schedule answer **what work is wanted, when, and under which constraints**:

```text
Production Order / Job Order
Product
Quantity
Release time
Due time
Priority
Scheduling scope
```

Arcogine currently receives work through `OrderCreation` events generated by the demand model. An immutable `Order` records the requested work (product, quantity, agreed price, creation time), and a `Job` is created alongside it to hold mutable execution state, referencing the `Order` it was created for.

For explicit production contracts and external consumers, Arcogine should further separate order intent from execution state along these lines:

```text
ProductionOrder
    external ID
    product definition/version
    requested quantity
    release time
    optional due time and priority
```

A due time may remain consumer-owned when it is only a game-scoring rule. It belongs in Arcogine when it affects scheduling, validation, or operational behavior.

### 6.3 Execution

Execution answers **what is happening now**:

```text
WorkItem
    production-order membership
    current operation
    assigned resource
    queue position
    status
    processing timing
    transfer state
```

This is the mutable shop-floor concern currently held mostly by `Job`, `Machine`, and machine queues.

### 6.4 Performance

Performance answers **what actually happened**:

```text
Actual start and completion times
Completed quantity
Operation history
Resource usage
Lead time
Throughput
Failures or rejections
Order completion outcome
```

Arcogine currently derives performance from jobs, machine state, aggregate counters, and the event log. This is sufficient for current simulation reports, but it is not yet a first-class performance model or ISA-95 interchange representation.

### 6.5 Current compression

The present model can be summarized as:

```text
Routing
    partial production definition

Order
    request facts
    + commercial facts

Job
    reference to its Order
    + mutable execution state
    + completion result

EventLog
    historical execution facts
```

Order intent is now separated from execution state as an immutable `Order`, but `Job` still holds the whole `Order` object (not an ID-only reference), and the relationship remains strictly one `Job` per `Order`. This becomes a further refactoring target when a concrete feature needs multiple work items per order, plan versions, or structured performance records that aggregate across jobs. The factory-design game's multi-job/quantity slice supplies that concrete trigger.

## 7. Resource definitions, instances, capabilities, and pools

A durable resource model should distinguish:

```text
MachineDefinition
    type identity
    supported capabilities
    nominal capacity
    footprint or other static parameters
    processing/setup parameters

MachineInstance
    stable instance identity
    definition reference
    availability and operational state
    active work and queues
    spatial position, when relevant

ResourcePool / WorkCenter
    eligible resource instances
    aggregate capacity or reporting scope
    dispatch policy or scheduling boundary
```

### 7.1 Why the distinction matters

The current `RoutingStep -> MachineId` relationship means a routing selects one exact machine. This prevents a newly added equivalent machine from contributing capacity without changing the routing itself.

A capability-oriented operation should instead express something like:

```text
Operation requirement
    capability: CUT
    duration: 5 ticks
    eligible pool: cutting resources
```

The execution layer then selects an eligible instance using a documented deterministic policy.

This supports:

- equivalent machines;
- parallel capacity;
- resource substitution;
- bottleneck experimentation;
- scheduling and capacity aggregation;
- cleaner future mapping to equipment capabilities.

### 7.2 Determinism requirement

Resource selection must remain deterministic. Any ranking policy must define a final stable tie-breaker, such as machine instance ID. The ISA-95 mapping does not prescribe Arcogine's dispatch algorithm; Arcogine owns that simulation semantic.

## 8. Equipment hierarchy versus spatial layout

ISA-95-like resource scope and Arcogine spatial layout are related but independent dimensions.

### 8.1 Resource or hierarchy scope

```text
Enterprise
  Site
    Area
      Work Center
        Work Unit / Equipment
```

Such a hierarchy can provide:

- organizational and operational scope;
- containment and ownership;
- capacity aggregation;
- scheduling boundaries;
- reporting and authorization scope.

### 8.2 Spatial layout

```text
FactoryFloor
  Position
  Footprint
  Orientation
  Reachability
  Transfer distance
```

Spatial layout provides:

- physical placement;
- collision and boundary rules;
- movement or transfer consequences;
- visual and geometric relationships.

### 8.3 Arcogine policy

- Do not use hierarchy membership as a substitute for coordinates or distance.
- Do not introduce `Enterprise` and `Site` merely to resemble the standard.
- A factory-design vertical slice may implement floor geometry before a complete hierarchy.
- `WorkCenter` or `ResourcePool` is justified earlier than `Enterprise` when it directly owns dispatch, capacity, or reporting behavior.
- A future machine instance may reference both one hierarchy node and one spatial location.

## 9. Manufacturing-operations coverage

Arcogine currently models a narrow production-execution slice, not the complete Manufacturing Operations Management domain.

| Area | Current Arcogine coverage | Status |
|---|---|---|
| Production definition | Materials linked to routings and concrete-machine steps | Partial |
| Production requests | Demand-generated order-creation events | Minimal and implicit |
| Production scheduling | Deterministic event scheduler and per-machine FIFO queues | Simplified, simulation-specific |
| Production execution | Machines, jobs, task transitions, queues, completion | Core current coverage |
| Production performance | Events, completion aggregates, jobs, and selected KPIs/observations | Partial |
| Equipment management | Machine identity, state, concurrency, queue, capacity/setup properties | Partial |
| Equipment capability | Concrete machine binding only | Not first-class |
| Equipment hierarchy | None | Deferred |
| Material management | Product-like material configuration and `ProductId` | Minimal |
| Inventory operations | None | Deferred |
| Personnel operations | None | Deferred |
| Quality operations | Can be represented only as an ordinary routing step/machine | Not first-class |
| Maintenance operations | None | Deferred |
| Enterprise integration | Generic HTTP/SSE interfaces | No ISA-95 transaction/profile support |
| Spatial layout | Proposed by the factory-design initiative | Arcogine extension |

Use this matrix to prevent broad statements such as "the factory domain implements Level 3". It implements production-execution behavior within a Level-3-like scope; it does not cover the full set of MOM activities.

## 10. Naming and public-contract policy

ISA-95 terminology should improve precision, not reduce usability.

### 10.1 Use standard terms when

- the Arcogine concept has substantially the same semantics;
- the term appears at an industrial integration boundary;
- it clarifies a distinction that simpler vocabulary would otherwise collapse;
- a future adapter needs a stable mapping target.

### 10.2 Prefer an Arcogine alias when

- the standard term would be unnecessarily obscure for simulation or game clients;
- the Arcogine concept is intentionally narrower;
- the internal term is already clear and unambiguous.

For example:

```text
Machine
    ISA-95 mapping: equipment instance at work-unit-like granularity
```

is preferable to renaming the class `WorkUnit` without adding actual hierarchy or work-unit semantics.

### 10.3 Do not use a standard term when

- Arcogine does not implement the distinction implied by that term;
- the name would create a false conformance impression;
- an Arcogine-specific extension is the real concern.

### 10.4 Public model metadata

A future versioned consumer or integration model may expose mapping metadata or documented aliases. It should not duplicate the same authoritative concept into separate "Arcogine" and "ISA-95" entity graphs.

## 11. Architecture and review checklist

For every new or materially changed manufacturing-domain concept, answer:

1. Is this a **definition**, **request**, **schedule**, **execution state**, or **performance result**?
2. Is it a resource **definition**, resource **instance**, hierarchy **scope**, or spatial **location**?
3. Who owns its mutable state and invariants?
4. Is its ISA-95 relationship exact, approximate, or absent?
5. Should Arcogine **adopt**, **alias**, **diverge**, **extend**, **defer**, or **refactor**?
6. Could a future adapter map it without guessing or losing essential meaning?
7. Are requested, accepted, started, completed, and reported states being collapsed?
8. Is the standard influencing real semantics, or only making names sound industrial?
9. Does the proposed abstraction have current behavior, or is it a standards-driven empty container?
10. Does it preserve Arcogine's authoritative state ownership, event causality, observations, and determinism contracts?

This checklist is a review aid, not a requirement to implement the ISA-95 ontology.

## 12. Design triggers and ADR boundaries

Revisit this document whenever a change introduces or materially alters:

- `Product`, `Material`, `ProductionOrder`, `WorkItem`, or structured performance records;
- machine/resource definitions and instances;
- resource capabilities, pools, or work centers;
- equipment hierarchy nodes;
- material, inventory, personnel, quality, or maintenance domains;
- schedule-versus-execution separation;
- ISA-95/B2MML import, export, transactions, or exchange profiles;
- public claims of ISA-95 compatibility or conformance.

This mapping document records current relationships and open design constraints. Create an ADR when a decision becomes accepted and hard to reverse, for example:

- aggregate boundaries between product, order, work item, and performance;
- capability-pool and deterministic dispatch semantics;
- hierarchy and spatial-model separation;
- the canonical public model contract;
- compatibility guarantees for an industrial interchange surface.

Do not create an ADR merely to state that `Machine` is an alias for an equipment instance.

## 13. Future interoperability path

A future adapter can translate explicitly between external ISA-95-oriented representations and Arcogine's canonical model:

```text
External ISA-95 / IEC 62264 representation
                    ↓
          Mapping and validation adapter
                    ↓
           Arcogine canonical model
                    ↓
        Simulation / verification / replay
                    ↓
       Performance and result projection
```

The adapter belongs at an interface boundary. The simulation core should not become a direct serialization of ISA-95 objects.

Any adapter or exchange profile must state:

- exact mappings;
- approximate mappings;
- ignored data;
- Arcogine extensions;
- lossy conversions;
- version and compatibility rules;
- which ISA-95 parts and profiles it actually supports.

Only such a defined and tested profile could justify an interoperability or conformance claim.

## 14. Explicit non-goals

This semantic mapping does not commit Arcogine to:

- implementing every ISA-95 object model;
- adding Enterprise, Site, Area, Work Center, and Work Unit classes immediately;
- replacing approachable runtime terms with standards terminology;
- B2MML serialization;
- ERP, MES, or MOM product scope;
- personnel, maintenance, quality, or inventory modules without concrete requirements;
- standards certification or conformance testing;
- treating spatial factory layout as equipment hierarchy.

## 15. Maintenance rule

Keep this document current rather than chronological:

- update the mapping register when the implementation changes;
- state implemented semantics as present fact;
- remove resolved gaps from current-gap descriptions;
- use Git history and ADRs to preserve decision chronology;
- track implementation work in issues or plans, not in this reference;
- re-check official standard publication metadata before making version-specific claims.

## 16. Official references

- [ISA-95 Series of Standards](https://www.isa.org/standards-and-publications/isa-standards/isa-95-standard)
- [ISA95 Standards Committee scope and purpose](https://www.isa.org/standards-and-publications/isa-standards/isa-standards-committees/isa95)
- [ISA announcement for ANSI/ISA-95.00.01-2025](https://www.isa.org/news-press-releases/2025/april/update-to-isa-95-standard-addresses-integration-of)

These links provide public summaries. The normative standards themselves remain the authoritative source for formal definitions and conformance requirements.
