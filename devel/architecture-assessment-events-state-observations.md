# Events–State–Observations Architecture Assessment

This document is the source-level companion to `docs/architecture.md`. It assesses the current Java implementation against the Events–State–Observations philosophy and the Commercial/Operational/Financial truth model, and lays out the remaining migration plan. It reflects the current state of the codebase, not the history of how it got here — see `git log` for that.

## Canonical invariant

```text
Events mutate State.
State produces Observations.
Observations inform Decisions.
Decisions produce Events.
```

See `docs/architecture.md`'s "Core Architecture Philosophy" section for the full narrative, and its "Commercial, Operational, and Financial Truth" section for the Finance domain model referenced throughout this document.

## Terminology (current, authoritative)

| Concept | Meaning | Owner | Mutability |
|---|---|---|---|
| `ObservedMarketPrice` | External/environmental market signal | Future environment/market domain — **not implemented** | — |
| `OfferPrice` | The firm's current asking price; input to the demand model | `PricingState` | Mutable — changes on `PriceChange` |
| `OrderPrice` | Price agreed when a specific order was created (`OfferPrice` at that instant, frozen) | Commercial fact, carried on `Job` | Immutable once the order exists |
| `OrderValue` | `quantity × OrderPrice` | Derived, on `Job.orderValue()` | Derived |
| `CompletedSalesValue` | Sum of `OrderValue` for completed orders — an operational/commercial KPI | `FactoryHandler` | Accumulates as orders complete |
| Ledger, Cash, Sales (financial balance) | Financial interpretation of operational facts | `FinanceHandler` / `Ledger` (sim-finance) | Mutates only by appending balanced `JournalEntry` records |
| Revenue (recognition policy, receivables, payables) | Accounting sophistication | Not modeled — explicit non-goal | — |

`OrderPrice`/`OrderValue` are worth a precision note: `Job` is where they're *carried* today, not what conceptually *owns* them. They are commercial/transaction facts — fixed at `OrderCreation`, consumed by Factory for production — not production state. `Job` holds them only because there is currently no separate commercial/order concept distinct from the production-lifecycle object. See `docs/architecture.md`'s "State" section for the full reasoning, including the (currently unwarranted) `Order`/`Job` split this would motivate if a second reason for it ever appears.

Three kinds of truth, related only by events:

```text
COMMERCIAL TRUTH                  OPERATIONAL TRUTH                 FINANCIAL TRUTH
"Order 42 was agreed              "Order 42 completed               "That completion caused
 at $12/unit for 10 units."    →   at t=500."                    →   these ledger postings."
```

## Current-state architecture matrix

| Area | Status | Evidence |
|---|---|---|
| `Event` / `EventPayload` / `EventType` (sim-core) | **Aligned** | All payloads are immutable records under a `sealed interface`; `Event.of()` derives `EventType` via an exhaustive switch. |
| `EventHandler` contract (sim-core) | **Aligned** | Single-method functional interface; every domain handler implements it uniformly, mutating only its own state. |
| `Scheduler` ordering (sim-core) | **Aligned** | Events are ordered by `(time, insertion sequence)`, so same-tick events dequeue deterministically (FIFO) rather than in `PriorityQueue`'s unspecified order. |
| `PricingState` (sim-economy) | **Aligned** | Sole owner of `OfferPrice` and its history; mutated only via `PriceChange`. |
| `DemandModel` (sim-economy) | **Aligned** | Reads `OfferPrice`/lead time on demand via `DoubleSupplier`s bound to `PricingState`/`FactoryHandler` at construction — no state of its own to keep in sync. |
| `FactoryHandler` / `Job` (sim-factory) | **Aligned** | Owns machines/jobs/queues/status (production state). Each `Job` also *carries* its own `OrderPrice`, captured immutably at `OrderCreation`, and derives `orderValue()` — a commercial fact riding along with the production object, not something Factory conceptually owns. `CompletedSalesValue`/`completedSales` are private with accessors. No reference to `PricingState` — the factory never needs `OfferPrice`. `EventPayload.OrderCompleted`'s identifier field is named `jobId` (typed `JobId`), not `orderId` — there is no separate `Order`/`OrderId` concept yet, so naming the field honestly (rather than implying a distinction that doesn't exist in code) keeps a future `Order` split an explicit migration if one is ever warranted, not a silent one. `FactoryHandler.machines`/`.jobs` fields are package-private (were `public`); external callers get read-only `MachineView`/`JobView` via `machinesView()`/`jobsView()`/`job(JobId)` — `Job.start`/`completeStep` and `Machine.startJob`/`completeJob`/`setAvailability`/etc. are excluded from those interfaces, so nothing outside `sim-factory` can bypass event-driven mutation, mirroring the `Ledger`/`LedgerView` fix. |
| `FinanceHandler` / `Ledger` (sim-finance) | **Aligned** | Reacts only to `OrderCompleted`; posts balanced `JournalEntry` records under an immediate-settlement policy; `Posting`/`JournalEntry` construction rejects non-positive amounts and unbalanced entries, so an invalid entry cannot enter state. Module depends only on `sim-types`/`sim-core` — structurally cannot reach into `FactoryHandler`. The `double` → `BigDecimal` conversion at this boundary uses an explicit, named, tested policy (`CurrencyPolicy`: scale 2, `HALF_UP`), not an ad hoc `setScale(...)` call — so two independent conversions of the same quantity can't silently round differently. `Ledger.post` isn't reachable from outside `FinanceHandler` — `FinanceHandler.ledger()` returns the read-only `LedgerView`, not the concrete `Ledger`. |
| Full-chain integration coverage (`OrderCreation` → `Factory` → `OrderCompleted` → `Finance` → `Ledger`) | **Aligned** | `OrderLifecycleIntegrationTest` drives a single order through the real, wired `IntegratedHandler` (not hand-built events in isolation) and asserts no posting exists before completion, exactly one after, and the posted amount matches; a second test injects two `OrderCompleted`s at the same tick and confirms each posts its own entry (exercising the `Scheduler` FIFO fix in combination with Finance); a third runs a full multi-order scenario and asserts the count of `OrderCompleted` events in the log equals both `FactoryHandler.completedSales()` and `Ledger.entries().size()` — no completion is silently un-posted or double-posted. |
| `IntegratedHandler` (sim-api) | **Mostly aligned** | Explicit, fixed dispatch order (Pricing → Demand → Factory → Finance → Agent); observation construction is extracted to `AgentObservationProjector` rather than inlined. Gap: the order is implicit in method-call sequence, not a declared, separately-tested contract. |
| `HandlerFactory` (sim-api) | **Aligned** | Pure wiring; no domain logic. |
| `AgentObservation` (sim-agents) | **Aligned structurally; naming gap** | Immutable, purpose-specific record; correctly limited to `OfferPrice`/backlog/lead-time/sales fields (never any order's `OrderPrice`). Gap: field names (`currentPrice`, `totalRevenue`) don't match the resolved `offerPrice`/`completedSalesValue` vocabulary. |
| `SalesAgent` (sim-agents) | **Aligned** | `decide()` is pure; on `AgentEvaluation` it schedules `PriceChange`/`AgentDecision` events rather than mutating state directly; `observation`/`interventions` are private with accessors. |
| `SimThread` / `SimCommand` (sim-api) | **Aligned** | Every command (`ChangePrice`, `ChangeMachine`, `ToggleAgent`) becomes a domain event dispatched through `IntegratedHandler.handleEvent`, appended to the log — no command bypasses the event system. |
| `SnapshotBuilder` (sim-api) | **Mostly aligned** | Legitimate, separate projection for the API/UI; `JobInfo.revenue` reads each job's own `orderValue()`, so it can't diverge from `FactoryHandler.completedSalesValue`. Does not expose Finance data (deliberate — no consumer needs it yet). Gap: no documented convention distinguishing domain-owned `XObservation` types from API DTOs for future domains to follow. |
| Module dependency structure | **Aligned; not enforced by CI** | `sim-agents` and `sim-finance` depend only on `sim-types`/`sim-core` — no compile-time path into `sim-factory`/`sim-economy` internals. Gap: this is true today by construction, but nothing fails the build if a future change violates it. |

## Remaining migration plan

Priorities: **P2** scalability preparation · **P3** future guardrail. (All P0 semantic-correctness and P1 structural items are resolved — see the matrix above.)

| Priority | Task | Why | Scope |
|---|---|---|---|
| P2 | Rename `AgentObservation.currentPrice` → `offerPrice`, `AgentObservation.totalRevenue` → `completedSalesValue`. | Cosmetic precision — the fields are already conceptually correct, just generically named. | `AgentObservation`, `SalesAgent`, `IntegratedHandler`'s observation construction, and referencing tests. |
| P2 | Define a narrow state-query interface pattern for `IntegratedHandler` (e.g. `PriceSource`, `LeadTimeSource`) so adding a domain doesn't mean adding new direct field access from the orchestrator. | Prepares for a third/fourth domain (inventory, procurement, workforce) without `IntegratedHandler` growing linearly with each new domain's internals. Prefer this over a generic event bus. | New narrow interfaces per domain; `IntegratedHandler` depends on interfaces, not concrete classes. |
| P2 | Document the "domain-owned observation type vs. API DTO" convention (each domain exposes its own `XObservation`, analogous to `AgentObservation`/`FinanceObservation`; `SnapshotBuilder`'s DTOs are a separate, API-facing concern) and require new domains to follow it. | Prevents each new domain from inventing its own ad hoc projection shape, and prevents `SnapshotBuilder` from becoming a de facto second source of truth. | Docs + a template/example; enforced via the `CONTRIBUTING.md` guardrails. |
| P2 | Add an ArchUnit-style package-dependency test asserting `sim-agents`/`sim-finance` never gain a compile-time dependency on `sim-factory`/`sim-economy` internals, and that no domain module calls a setter-shaped method on another domain's handler. | Turns the guardrails in `CONTRIBUTING.md` into something CI enforces, not just review discipline. | New Gradle test module/dependency (e.g. `com.tngtech.archunit`), one focused rule set. |
| P3 | Revisit the P2 state-query-interface pattern's adequacy now that Finance is a real second domain beyond Factory/Economy — validate the design against an actual case rather than a hypothetical one. | Keeps `IntegratedHandler` from becoming a god orchestrator as domains multiply. | Design review once the P2 interface pattern lands. |
| P3 | Consider whether `IntegratedHandler`'s dispatch order (Pricing → Demand → Factory → Finance → Agent) should become a declared, testable ordering contract (an ordered list + a test enumerating it) rather than implicit in method-call order. | "Event ordering becoming implicit... dependent on registration accident" is a named architectural-review trigger; today's order is simple enough to read at a glance, but that won't stay true indefinitely. | `IntegratedHandler` + one test. |

## Open question: commands vs. facts

`EventPayload` names are inconsistent about what they represent. `TaskEnd`, `OrderCompleted`, and `AgentEnabledChanged` are fact-shaped (past tense — something that happened). `PriceChange`, `OrderCreation`, and `MachineAvailabilityChange` are noun-phrases that read closer to requests — `SetOfferPrice`/`OfferPriceChanged`, `CreateOrder`/`OrderCreated` — even though they behave identically to the fact-shaped ones: every `EventPayload`, once scheduled, is applied unconditionally by its owning handler. There is no accept/reject step anywhere in the pipeline; where validation exists (e.g. `SalesAgent.decide()` clamping to `minPrice`/`maxPrice`), it happens before the event is emitted, inside the decision logic, not at application time.

This works today because there is exactly one trusted decision source per concern. It stops working cleanly once that assumption breaks — multiple or richer decision sources (e.g. a future `FinanceAgent` and `SalesAgent` both able to affect price; a UI command racing an agent decision; a domain rule that can legitimately reject a request rather than relying on the requester to have pre-validated it, like "can't take a machine offline mid-job"). At that point, collapsing "requested" and "happened" into one concept stops being safe, and the two-step shape below becomes worth the extra ceremony:

```text
COMMAND
CreateOrder(...)
        |
        v   domain validates/accepts/rejects
EVENT
OrderCreated(...)
```

This is not scheduled work — no rename, no new abstraction. It's recorded here because renaming `EventPayload`s to be consistently fact-shaped, on its own, would be misleading: it would look like this distinction exists without actually adding the accept/reject step that makes it meaningful. Revisit when a second untrusted or conflicting decision source is actually being added — that is the concrete trigger, not "agents get more sophisticated" in the abstract.

## Explicit non-goals

- **No generic event bus / pub-sub.** Explicit, deterministic, easy-to-read execution order is the point; a bus trades that for indirection.
- **No CQRS or event-sourcing framework.** `Event`/`EventLog`/`Scheduler` already give replayability where needed.
- **No command/event split (see "Open question: commands vs. facts" above) until there's a concrete second decision source that needs it.** Renaming `EventPayload`s to be fact-shaped without adding a real accept/reject step would be cosmetic, not architectural.
- **No global mutable `SimulationState` object.** That would recreate the duplicated-state problem this assessment resolved, at a larger scale.
- **No premature plugin framework** for future domains (inventory, procurement, workforce, maintenance). The P2 state-query-interface pattern should be validated against one real new domain before generalizing further.
- **No sophisticated accounting model.** A minimal double-entry ledger with an immediate-settlement policy is the intentional current architecture. Out of scope: GAAP/IFRS compliance, configurable revenue-recognition frameworks, `AccountsReceivable`/`AccountsPayable` unless an actual scenario needs them, payment terms, tax, depreciation, multi-currency, debt/equity financing, inventory accounting, budgeting, forecasting, fiscal periods/closing, a generalized accounting rules engine, or ERP abstractions.
- **No fixing the Rust-era documentation staleness** (`CONTRIBUTING.md`'s crate/Cargo references, `docs/standards-alignment.md`'s "Crate" column) as part of this line of work — real, but orthogonal. Worth its own follow-up.

## Target architecture

```text
             Events
                |
                v
        Domain-owned State
                |
                v
     Read-only Observations
                |
                v
     Agents / Policies / Experiments
                |
                v
        Decisions / Commands
                |
                v
              Events
```

- Each domain (`PricingState`, `FactoryHandler`, `FinanceHandler`, and any future `InventoryHandler`/`WorkforceHandler`) owns one slice of mutable state, mutated only inside its own `handleEvent`, with no cross-domain setters.
- Historical transaction facts travel *with the event that created them* — `OrderCreation` carries `unitPrice`, `OrderCompleted` carries the facts Finance needs — rather than being re-derived later from another domain's live mutable state.
- `IntegratedHandler` stays the explicit, deterministic composition root: its job is *sequencing* handler calls in a documented order, not *synchronizing* their internals or *assembling* observations inline.
- Agents and policies only ever see Observations, and only ever produce Events/Decisions — `SalesAgent.decide()` is the model to replicate. An agent can change `OfferPrice` for the future; it can never reach an existing order's `OrderPrice` or Finance's ledger.
- API/UI snapshots (`SnapshotBuilder`) remain a legitimate, separate projection for external consumers, documented as such, never a second authority for a number a domain observation also reports.
- Adding a new domain means: (1) a new `XHandler implements EventHandler` that owns its own state, (2) a new `XObservation` record for whatever needs to read it, (3) one line in `IntegratedHandler`'s explicit sequence — not new setters scattered across existing handlers.

This preserves deterministic behavior, explicit ordering, module boundaries, headless-first design, testability, and replayability, while keeping the couplings this assessment originally found — duplicated price state, hand-inlined observation construction, an event-bypassing command, an absent Finance boundary — closed.
