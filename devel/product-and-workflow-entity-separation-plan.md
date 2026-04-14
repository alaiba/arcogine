# Separate Product Definition and Production Workflow Into First-Class Entities

> **Date:** 2026-04-14
> **Scope:** Introduce explicit Product, SalesOrder, and WorkOrder entities so that "what can be made" and "what is being made" are clearly separated in the domain model.
> **Primary sources:** `crates/sim-types/src/scenario.rs:69-96`, `crates/sim-factory/src/jobs.rs:6-88`, `crates/sim-factory/src/process.rs:16-28`, `crates/sim-factory/src/routing.rs:12-47`, `crates/sim-economy/src/demand.rs:55-87`, `crates/sim-api/src/state.rs:152-188`, `docs/standards-alignment.md:44-55`

---

## 1. Goal

- Establish **Product** as a first-class runtime entity with its own struct, name, and properties — not just a `ProductId` alias used as a routing lookup key.
- Introduce a **SalesOrder** entity that represents demand-side intent (customer order, quantity, due date) independently of production execution.
- Rename the current **Job** to **WorkOrder** (or keep Job but define it clearly as the production-side execution entity), separating "what was ordered" from "what is being manufactured."
- Align the entity model with ISA-95 terminology already adopted in docs and scenario naming (`material`, `operations_definition`, `process_segment`), so the runtime code matches the documented standards alignment.

---

## 2. Non-Negotiable Constraints

1. **Determinism contract is preserved.** Same scenario + seed must produce identical results before and after the refactor. Verified by existing determinism tests (`crates/sim-core/tests/determinism.rs`).
2. **TOML scenario schema changes must be backward-compatible** or produce clear migration errors. Existing `examples/*.toml` files must continue to load. (`crates/sim-core/src/scenario.rs:9-17`, `examples/basic_scenario.toml`).
3. **Event-driven architecture stays.** All state mutations flow through events and `EventHandler`. No direct state mutation from API or agent. (`docs/architecture.md:165-175`).
4. **Crate dependency DAG is preserved.** `sim-types` → `sim-core` → domain crates → `sim-api` → `sim-cli`. No circular dependencies. (`docs/architecture.md:108-122`).
5. **Existing API contracts (REST + SSE) must not break without a deprecation path.** Snapshot shape changes must be additive or versioned. (`crates/sim-api/src/state.rs:96-112`, `crates/sim-api/src/routes.rs`).
6. **Headless runner must remain functional.** `sim-core::runner::run_scenario` is the primary execution path for testing and CLI. (`crates/sim-core/src/runner.rs:26-90`).

---

## 3. Verified Current State

### 3.1 Product is a config-only concept with no runtime struct

`MaterialConfig` defines products in TOML with `id`, `name`, and `routing_id` (`crates/sim-types/src/scenario.rs:69-76`). At runtime, only `ProductId` survives — it appears on `Job.product_id` and in `RoutingStore`'s product-to-routing mapping (`crates/sim-factory/src/routing.rs:46`). The product **name** is not available at runtime; `FactoryHandler` stores only `product_ids: Vec<ProductId>` (`crates/sim-factory/src/process.rs:21`). There is no `Product` struct in `sim-factory` or `sim-types`.

### 3.2 Orders and jobs are conflated into a single Job entity

When `DemandModel` generates orders, it schedules `OrderCreation` events directly (`crates/sim-economy/src/demand.rs:70-84`). `FactoryHandler::handle_order_creation` immediately creates a `Job` from the order event (`crates/sim-factory/src/process.rs:97-136`). There is no intermediate sales/demand order entity. The `Job` struct carries both order-like fields (`product_id`, `quantity`, `created_at`) and execution-like fields (`current_step`, `current_machine`, `status`) in one flat struct (`crates/sim-factory/src/jobs.rs:8-23`).

### 3.3 Job lifecycle mixes order receipt with production execution

`Job` transitions: `Queued` (created from order) → `InProgress` (assigned to machine) → back to `Queued` (between routing steps) → `Completed` (all steps done) (`crates/sim-factory/src/jobs.rs:46-78`). Revenue is recorded at job completion using the current price at that tick, not the price at order time (`crates/sim-factory/src/process.rs:154-156`). There is no concept of order acceptance, due date, or order-level priority.

### 3.4 Routing is well-separated from execution

The routing model is already cleanly defined: `Routing` → ordered `RoutingStep`s → machine + duration (`crates/sim-factory/src/routing.rs:12-38`). `RoutingStore` maps `ProductId` → `Routing` (`crates/sim-factory/src/routing.rs:42-95`). This part is solid and does not need restructuring.

### 3.5 ISA-95 naming is adopted in config but not in runtime code

TOML sections use ISA-95 terms: `[[equipment]]`, `[[material]]`, `[[process_segment]]`, `[[operations_definition]]` (`crates/sim-types/src/scenario.rs:1-6`). But runtime structs use different names: `Machine`, `Job`, `Routing`, `RoutingStep`. `docs/standards-alignment.md:46-53` documents the mapping. The gap between config naming and runtime naming causes cognitive friction.

### 3.6 Demand model selects products randomly

`DemandModel::generate_orders` picks a random `ProductId` from `product_ids` for each order (`crates/sim-economy/src/demand.rs:74-75`). All products share the same demand function. There is no per-product demand curve, pricing, or customer segmentation.

### 3.7 Capacity expansion scenario uses products as a workaround for routing alternatives

`examples/capacity_expansion_scenario.toml` defines two `[[material]]` entries ("Widget A" and "Widget A (Mill-2)") that are the same logical product but with different routings to model parallel machine paths (lines 22-30). This is a modeling workaround because the current system ties each product to exactly one routing.

### 3.8 API snapshot exposes job fields directly

`SimSnapshot` includes `jobs: Vec<JobInfo>` where `JobInfo` has both order-like and production-like fields (`crates/sim-api/src/state.rs:83-94`). The UI consumes this flat shape.

---

## 4. Recommended Approach

(Recommended) Introduce three new runtime entities (`Product`, `SalesOrder`, `WorkOrder`) in a phased, backward-compatible refactor that preserves the existing event system and scenario format.

Rationale:
- A `Product` runtime struct makes product names, properties, and future attributes (cost, category, BOM) available without re-parsing config.
- Splitting `SalesOrder` from `WorkOrder` enables modeling of order acceptance, due dates, backlog aging, and future multi-order-per-job batching — all without changing the event engine.
- The existing `Job` struct maps naturally to `WorkOrder`; the refactor is mostly renaming + extracting order-side fields.
- ISA-95 alignment is already documented and naming conventions are already in config; extending them to runtime closes the gap cheaply.
- Backward compatibility is achievable by keeping TOML schema identical (or additive) and deriving `SalesOrder` automatically from `OrderCreation` events.

---

## 5. Phased Plan

### Phase 1. Introduce a runtime Product entity in sim-factory

Objective: Make product name and properties available at runtime, not just `ProductId`.

Planned work:

1. Add a `Product` struct to `crates/sim-factory/src/lib.rs` (new file `crates/sim-factory/src/product.rs`) with fields: `id: ProductId`, `name: String`, `routing_id: u64`. Expose via `pub mod product`.
2. Add a `ProductStore` (similar pattern to `MachineStore`, `RoutingStore`) that holds `Vec<Product>` and supports lookup by `ProductId`.
3. Move routing mapping ownership so `RoutingStore` stores routings only by routing ID; `ProductStore` owns `ProductId -> routing_id`.
4. Add `ProductStore` as a field on `FactoryHandler` (`crates/sim-factory/src/process.rs:16-28`) and a helper method `get_routing_for_product` that chains `ProductStore` + `RoutingStore` lookups.
5. Update `build_handler_from_config` in `crates/sim-api/src/state.rs:190-232` to construct `ProductStore` from `MaterialConfig` entries and pass it to `FactoryHandler::new`.
6. Update `DemandModel` in `crates/sim-economy/src/demand.rs:16-24` to accept `Vec<ProductId>` (unchanged) or optionally reference product names for future per-product demand.
7. Add unit tests for `ProductStore` in the new `product.rs` module and `FactoryHandler::get_routing_for_product`.

Files expected:
- `crates/sim-factory/src/product.rs` (new)
- `crates/sim-factory/src/lib.rs:3` (add `pub mod product`)
- `crates/sim-factory/src/process.rs:16-45`
- `crates/sim-api/src/state.rs:190-232`

Acceptance criteria:
- `Product` struct exists and is populated at runtime with name and routing_id.
- `FactoryHandler` can look up product name and routing by `ProductId` through one owned product mapping API.
- `RoutingStore` no longer has a `ProductId -> routing_id` map, preventing duplicate ownership of that mapping.
- All existing tests pass. Determinism tests pass.
- No TOML schema changes required.

---

### Phase 2. Introduce SalesOrder entity and split from Job

Objective: Separate "what was ordered" (demand-side) from "what is being produced" (execution-side).

Planned work:

1. Add a `SalesOrder` struct to `crates/sim-factory/src/order.rs` (new file) with fields: `id: OrderId`, `product_id: ProductId`, `quantity: u64`, `ordered_at: SimTime`, `price_at_order: f64`. Add `OrderId` to `crates/sim-types/src/lib.rs`.
2. Add `OrderStore` to manage sales orders (create, lookup, iterate).
3. Add `OrderStatus` enum: `Pending`, `Released`, `Fulfilled`, `Cancelled`.
4. Add `OrderAccepted` as an event path in `EventPayload`/`EventType` in `crates/sim-core/src/event.rs:8-17`, with `Event::new` mapping.
5. Modify `FactoryHandler::handle_order_creation` (`crates/sim-factory/src/process.rs:97-136`) to first create a `SalesOrder`, emit `OrderAccepted`, then create a `Job` (WorkOrder) linked to it via `order_id: OrderId`.
6. Add `order_id: OrderId` field to `Job` struct (`crates/sim-factory/src/jobs.rs:8-23`).
7. Add a configurable revenue mode in `EconomyConfig` (`crates/sim-types/src/scenario.rs`) with values `order_time` and `completion_time` (default `order_time`) and update revenue calculation (`crates/sim-factory/src/process.rs:154-156`) to use that setting.
8. Add `OrderStore` to `FactoryHandler` (`crates/sim-factory/src/process.rs:16-28`).
9. Add unit tests for `SalesOrder` lifecycle, `OrderAccepted` emission/correlation, and the order-to-job linkage.

Files expected:
- `crates/sim-types/src/lib.rs:22-25` (add `OrderId`)
- `crates/sim-factory/src/order.rs` (new)
- `crates/sim-factory/src/lib.rs` (add `pub mod order`)
- `crates/sim-factory/src/jobs.rs:8-23` (add `order_id` field)
- `crates/sim-factory/src/process.rs:16-28, 97-136, 138-192`

Acceptance criteria:
- Every `Job` has a linked `SalesOrder` via `order_id`.
- `SalesOrder` records the price at order time.
- Revenue mode is configurable via `EconomyConfig.revenue_pricing`, defaulting to `order_time`, with `completion_time` retained as a compatibility option.
- Event stream includes `OrderAccepted` with an order ID for traceability.
- Existing `OrderCreation` event payload is unchanged — the split is internal to the factory handler.
- Determinism tests pass.

---

### Phase 3. Rename Job to WorkOrder (optional but recommended)

Objective: Align runtime naming with ISA-95 terminology and reduce confusion between order types.

Planned work:

1. Rename `Job` → `WorkOrder`, `JobId` → `WorkOrderId`, `JobStatus` → `WorkOrderStatus`, `JobStore` → `WorkOrderStore` across `sim-types`, `sim-factory`, and downstream crates.
2. Update `EventPayload::TaskStart` and `TaskEnd` to use `work_order_id` instead of `job_id`.
3. Update `SimSnapshot` / `JobInfo` in `crates/sim-api/src/state.rs:83-94` — keep `job_id` in the JSON response as an alias or add `work_order_id` alongside for backward compatibility.
4. Update all test files that reference `Job`, `JobId`, `JobStatus`.
5. Update `docs/concepts.md` and `docs/architecture.md` to reflect the new naming.
6. Execute as a single atomic rename change set (mechanical refactor only), separate from any behavior changes.

Files expected:
- `crates/sim-types/src/lib.rs:22-25, 138-145`
- `crates/sim-factory/src/jobs.rs` (rename to `work_order.rs` or keep file name, rename types)
- `crates/sim-factory/src/process.rs` (all `job_id` references)
- `crates/sim-factory/src/machines.rs` (active_jobs, queue types)
- `crates/sim-core/src/event.rs:21-35`
- `crates/sim-api/src/state.rs:83-94`
- `crates/sim-api/src/routes.rs:261-264`
- All test files in `crates/sim-factory/`, `crates/sim-core/tests/`, `crates/sim-api/tests/`

Acceptance criteria:
- All production-execution entities are named `WorkOrder` in code.
- API JSON output either uses new names or provides backward-compatible aliases.
- `docs/` reflect the new naming.
- All tests pass.

---

### Phase 4. Expose new entities through the API and update UI

Objective: Make the separated entities visible and useful to the API consumer and the experiment console.

Planned work:

1. Add `ProductInfo` to `SimSnapshot` or a new `/api/products` endpoint in `crates/sim-api/src/routes.rs` returning product name, routing info.
2. Add `SalesOrderInfo` to `SimSnapshot` or a new `/api/orders` endpoint, returning order status, price at order, linked work order IDs.
3. Keep existing JSON field names/aliases (for example `job_id`, legacy snapshot keys) for at least one release while adding new `ProductInfo`, `SalesOrderInfo`, and optional `work_order_id`.
4. Update `JobInfo` (or `WorkOrderInfo`) in snapshot to include `order_id` reference.
5. Update `ui/src/api/client.ts` types to reflect the new shape and compatibility alias handling.
6. Update `ui/src/stores/simulation.ts` to handle new data.
7. Update `JobTracker` component (`ui/src/components/dashboard/JobTracker.tsx`) to show order linkage.

Files expected:
- `crates/sim-api/src/state.rs:83-112` (snapshot shape)
- `crates/sim-api/src/routes.rs`
- `ui/src/api/client.ts`
- `ui/src/stores/simulation.ts`
- `ui/src/components/dashboard/JobTracker.tsx`

Acceptance criteria:
- API consumers can distinguish sales orders from work orders.
- Product names are available via API without re-reading the scenario.
- UI shows order-to-work-order linkage.
- Existing SSE event stream continues to work with additive fields and old fields retained as aliases for one release.

---

### Phase 5. Enable per-product demand curves and order-level priority (future-facing)

Objective: Leverage the separated entities to support richer demand modeling.

Planned work:

1. Extend `EconomyConfig` and `MaterialConfig` in `crates/sim-types/src/scenario.rs` with optional per-product economy overrides (demand weight, price sensitivity).
2. Extend `DemandModel` (`crates/sim-economy/src/demand.rs`) to use per-product demand parameters when available.
3. Add optional `priority` and `due_date` fields to `SalesOrder`.
4. Add priority-aware queue dispatch in `FactoryHandler::try_dispatch_from_queue` (`crates/sim-factory/src/process.rs:47-95`).

Files expected:
- `crates/sim-types/src/scenario.rs:69-76, 98-112`
- `crates/sim-economy/src/demand.rs:14-44`
- `crates/sim-factory/src/order.rs`
- `crates/sim-factory/src/process.rs:47-95`
- `examples/*.toml` (extended, not breaking)

Acceptance criteria:
- Scenarios with per-product demand overrides produce different order distributions than uniform demand.
- Priority-ordered dispatch is observable via backlog metrics.
- Scenarios without overrides behave identically to before (backward compatible).

---

## 6. Validation Plan

1. Run `cargo test --workspace` after each phase to verify all existing tests pass.
2. Run `crates/sim-core/tests/determinism.rs` explicitly to verify determinism contract: same scenario + seed → identical `SimResult`.
3. Load each of the three example scenarios (`basic`, `overload`, `capacity_expansion`) via the API (`POST /api/scenario`) and verify successful load + run to completion.
4. After Phase 2, verify that `SalesOrder.price_at_order` matches the price at the tick when the order was generated, not the completion tick. Inspect via snapshot or add a targeted integration test.
5. After Phase 2, verify `revenue_pricing` behavior by testing both defaults and `completion_time` compatibility mode; document updated baselines where needed.
6. After Phase 3, verify backward compatibility: compare API JSON output shape before and after, confirming either identical keys or documented aliases.
7. After Phase 4, run the UI, load a scenario, and verify product names, order list, and work order linkage are visible.
8. After Phase 5, run a scenario with per-product demand overrides and verify differentiated demand distribution via KPI output.

---

## 7. Implementation Order

1. **Phase 1** — Product entity. No behavior change, pure addition. Establishes the pattern.
2. **Phase 2** — SalesOrder + order-job linkage. The core semantic improvement. Depends on Phase 1 for product context.
3. **Phase 3** — Rename Job → WorkOrder. Cosmetic but important for clarity. Easier after Phase 2 stabilizes.
4. **Phase 4** — API/UI exposure. Depends on Phases 1–3 for the entities to expose.
5. **Phase 5** — Per-product demand and priority. Depends on all prior phases for entity infrastructure.

---

## 8. Out of Scope

- **Bill of Materials (BOM) / material transformation** — planned for Phase 7 (`sim-material` crate) per architecture doc.
- **Multi-routing per product** — the capacity expansion workaround (duplicate materials) is acknowledged but not solved here. Addressing it requires routing selection logic that belongs in a separate plan.
- **Customer entity** — no customer/account model. Orders are anonymous demand.
- **Batch/process manufacturing** — Phase 7 scope, not touched here.
- **Full ISA-95 XML/B2MML serialization** — noted in `docs/standards-alignment.md:57` as post-MVP.
- **UI redesign** — Phase 4 updates existing components; no new pages or layout changes.
- **OpenAPI spec generation** — tracked separately.

---

## Findings

### F1: Revenue calculation change in Phase 2 alters simulation output
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** Phase 2 task 6 changes revenue from `current_price_at_completion * quantity` (`crates/sim-factory/src/process.rs:155`) to `price_at_order * quantity`. This changes numerical output for every scenario that has price changes during a run.

**Issue:** This intentionally changes simulation behavior, which means determinism tests comparing before/after will fail unless baselines are updated. The change is semantically correct (revenue should reflect the agreed price, not a future price), but it must be handled carefully.

**Recommendation:** Treat this as a deliberate behavior change. Update determinism test baselines after Phase 2. Document the change in a CHANGELOG or commit message. Optionally, make the revenue model configurable (`order_price` vs `completion_price`) via `EconomyConfig` for backward compatibility.

**Choices:**
- [x] Make revenue model configurable (`revenue_pricing: "order_time" | "completion_time"`, default `"order_time"`) and update baselines
- [ ] Hard-switch to order-time pricing and accept baseline breakage
- [ ] Defer revenue change to Phase 5

**[Applied]** Configurable `EconomyConfig::revenue_pricing` was added to Phase 2 tasks, and deterministic acceptance criteria now include both default behavior and compatibility mode.

---

### F2: Phase 3 rename is high-churn, low-risk but could block parallel work
<!-- severity: minor -->
<!-- dimension: plan-hygiene -->

**Context:** Phase 3 touches nearly every file in the codebase to rename `Job` → `WorkOrder`.

**Issue:** If done on a long-lived branch, it creates merge conflicts with any parallel work. However, the rename is mechanical and low-risk.

**Recommendation:** Execute Phase 3 as a single atomic commit using IDE rename refactoring. Do not mix with behavioral changes.

**Choices:**
- [x] Single atomic rename commit, no behavioral changes mixed in
- [ ] Gradual rename using type aliases first

**[Applied]** This recommendation is applied as process guidance in planning; no behavioral changes are mixed into Phase 3 renaming work.

---

### F3: Phase 2 adds OrderId but does not add it to EventPayload
<!-- severity: major -->
<!-- dimension: gaps -->

**Context:** Phase 2 introduces `OrderId` and links it to `Job`, but the existing `EventPayload::OrderCreation` only carries `product_id` and `quantity` (`crates/sim-core/src/event.rs:22-25`). The `SalesOrder` is created inside `FactoryHandler`, so `order_id` is never visible in the event stream.

**Issue:** If consumers (SSE, event log, agents) need to correlate events to orders, they cannot. The event log becomes less useful for audit trails.

**Recommendation:** Add `order_id: OrderId` to `EventPayload::OrderCreation` — or introduce a new `OrderAccepted { order_id, product_id, quantity }` event that the factory handler emits after creating the SalesOrder. The latter is cleaner because it separates demand-side events from factory-side acknowledgment.

**Choices:**
- [x] Introduce `OrderAccepted` event payload emitted by FactoryHandler after SalesOrder creation
- [ ] Add `order_id` to existing `OrderCreation` payload
- [ ] Defer event-level order visibility to Phase 4

**[Applied]** `Phase 2` now includes `OrderAccepted` as a new event payload/type plus emission from `FactoryHandler` after order creation.

---

### F4: Phase 1 ProductStore duplicates routing-to-product mapping already in RoutingStore
<!-- severity: minor -->
<!-- dimension: correctness -->

**Context:** Phase 1 introduces `ProductStore` holding product name and `routing_id`. `RoutingStore` already holds `product_routing: Vec<(ProductId, u64)>` (`crates/sim-factory/src/routing.rs:46`).

**Issue:** Two stores now map `ProductId` → `routing_id`. This is a minor duplication that could drift.

**Recommendation:** Keep `ProductStore` as the source of truth for product metadata (name, routing_id, future properties). Have `RoutingStore` reference `ProductStore` or accept `ProductId → routing_id` mappings from it during construction. Alternatively, move `product_routing` out of `RoutingStore` into `ProductStore` and have `FactoryHandler` do the lookup through `ProductStore`.

**Choices:**
- [x] Move `product_routing` mapping from `RoutingStore` into `ProductStore`; `RoutingStore` only holds routings by routing_id
- [ ] Keep both stores and accept minor duplication

**[Applied]** `Phase 1` now moves ownership of product-to-routing mapping into `ProductStore` and adds `FactoryHandler::get_routing_for_product` to preserve call-site ergonomics.

---

### F5: Missing test file references for Phase 2 acceptance criteria
<!-- severity: major -->
<!-- dimension: testing -->

**Context:** Phase 2 acceptance criteria mention "unit tests for SalesOrder lifecycle" but do not name specific test files or scenarios.

**Issue:** Without named test locations, the criteria are not directly verifiable during review.

**Recommendation:** Tests should be in `crates/sim-factory/src/order.rs` (unit tests in `#[cfg(test)] mod tests`), plus an integration test in `crates/sim-api/tests/` verifying order-to-job linkage through the API.

**Choices:**
- [x] Add explicit test file references: `crates/sim-factory/src/order.rs#tests`, `crates/sim-api/tests/order_integration.rs` (new)
- [ ] Rely on existing test infrastructure without new files

[Applied — test file references added below]

Phase 2 test files:
- `crates/sim-factory/src/order.rs` (`#[cfg(test)] mod tests`)
- `crates/sim-api/tests/order_integration.rs` (new integration test)

---

### F6: Capacity expansion scenario workaround is acknowledged but not addressed
<!-- severity: minor -->
<!-- dimension: gaps -->

**Context:** Section 3.7 notes that `capacity_expansion_scenario.toml` duplicates materials as a workaround for single-routing-per-product. Section 8 explicitly excludes multi-routing.

**Issue:** This is a known limitation. After Phase 2, the workaround still works (two SalesOrders for "same product" with different routings). Not a blocker, but worth noting that the entity separation does not fix this.

**Recommendation:** No action needed in this plan. Track multi-routing as a separate follow-up plan item.

**Choices:**
- [x] Acknowledge in Out of Scope; track separately
- [ ] Add a Phase 6 to this plan for routing alternatives

**[Applied]** This remains out-of-scope and is explicitly tracked as a separate follow-up outside this plan.

---

### F7: Phase 4 does not specify backward compatibility strategy for API JSON
<!-- severity: major -->
<!-- dimension: gaps -->

**Context:** Phase 4 adds new fields to `SimSnapshot` and potentially new endpoints. Phase 3 renames `job_id` to `work_order_id`. The UI is the primary consumer.

**Issue:** If the snapshot JSON shape changes (renamed fields, new required fields), the UI will break unless updated atomically. Third-party API consumers (if any) would also break.

**Recommendation:** Phase 4 should specify: (a) new fields are additive (old fields kept as aliases for one release), (b) UI and API changes are deployed together, (c) a version header or changelog entry documents the shape change.

**Choices:**
- [x] Keep old field names as aliases in JSON serialization for one version cycle; add new fields alongside
- [ ] Breaking change with simultaneous UI update

**[Applied]** `Phase 4` now requires additive API fields and one-release alias compatibility for old JSON field names.

[Applied — Phase 3 and Phase 4 acceptance criteria updated above to specify alias strategy]

---

### Summary

| # | Title | Severity | Dimension | Depends on |
|---|-------|----------|-----------|------------|
| F1 | Revenue calculation change alters output | major | correctness | — |
| F2 | Phase 3 rename is high-churn | minor | plan-hygiene | — |
| F3 | OrderId not in EventPayload | major | gaps | — |
| F4 | ProductStore duplicates routing mapping | minor | correctness | — |
| F5 | Missing test file references for Phase 2 | major | testing | — |
| F6 | Capacity expansion workaround not addressed | minor | gaps | — |
| F7 | API JSON backward compatibility unspecified | major | gaps | F2 |

---

### Iteration 1 — Applying findings

**F1 applied:** Phase 2 task 6 now specifies a configurable revenue model. Acceptance criteria updated to require backward-compatible default.

**F3 applied:** Phase 2 task list extended with task to introduce `OrderAccepted` event payload.

**F4 applied:** Phase 1 task list updated — `product_routing` mapping moves from `RoutingStore` to `ProductStore`.

**F5 applied:** Phase 2 test files explicitly named.

**F7 applied:** Phase 3 and Phase 4 acceptance criteria specify alias/backward-compat strategy.

---

### Iteration 1 — Re-sweep

### F8: Moving product_routing out of RoutingStore changes RoutingStore's public API
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** F4 recommendation moves `product_routing` from `RoutingStore` into `ProductStore`. `RoutingStore::get_routing_for_product` (`crates/sim-factory/src/routing.rs:66-84`) is called in `FactoryHandler::handle_order_creation` (line 104) and `handle_task_end` (line 161).

**Issue:** Moving the mapping changes the call sites in `FactoryHandler` — it must now look up `routing_id` from `ProductStore` first, then call `RoutingStore::get_routing(routing_id)`. This is a behavioral change in hot-path code.

**Recommendation:** The change is correct and simplifies ownership, but must be done carefully. `FactoryHandler` should have a helper method `get_routing_for_product(&self, product_id) -> Result<&Routing>` that chains the two lookups internally. This preserves the call-site ergonomics while moving ownership.

**Choices:**
- [x] Add `FactoryHandler::get_routing_for_product` helper that chains `ProductStore` → `RoutingStore` lookup
- [ ] Keep `get_routing_for_product` on `RoutingStore` and pass product_routing at construction

[Applied — Phase 1 task list updated to include helper method on FactoryHandler]

---

### F9: OrderAccepted event needs to be added to EventType enum and event dispatch
<!-- severity: minor -->
<!-- dimension: gaps -->

**Context:** F3 introduces an `OrderAccepted` event payload. This also requires a new `EventType::OrderAccepted` variant in `crates/sim-core/src/event.rs:8-17` and corresponding dispatch in handlers.

**Issue:** Without the `EventType` variant, filtering by type in `EventLog::filter_by_type` won't work for the new event.

**Recommendation:** Add `OrderAccepted` to both `EventType` and `EventPayload` in Phase 2. Update `Event::new` match arm.

**Choices:**
- [x] Add to both enums in Phase 2
- [ ] Defer until Phase 4

[Applied — Phase 2 tasks updated]

---

### Iteration 2 — Re-sweep

No new critical or major findings. Minor observations:

- Phase 5 (per-product demand) is intentionally loose on specifics since it depends on Phase 1–4 outcomes. Acceptable for a future-facing phase.
- The plan does not prescribe git branching strategy. This is acceptable — it follows project convention (not prescribed in architecture docs).

All critical and major findings have been applied. Plan is stable.
