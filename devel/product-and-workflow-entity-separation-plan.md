# Separate Product Definition and Production Workflow Into First-Class Entities

> **Date:** 2026-04-14
> **Scope:** Introduce explicit Product, SalesOrder, and WorkOrder entities so that "what can be made" and "what is being made" are clearly separated in the domain model.
> **Primary sources:** `crates/sim-types/src/scenario.rs`, `crates/sim-factory/src/jobs.rs`, `crates/sim-factory/src/process.rs`, `crates/sim-factory/src/routing.rs`, `crates/sim-economy/src/demand.rs`, `crates/sim-api/src/state.rs`, `docs/standards-alignment.md`

---

## 1. Goal

- Establish **Product** as a first-class runtime entity with its own struct, name, and properties — not just a `ProductId` alias used as a routing lookup key.
- Introduce a **SalesOrder** entity that represents demand-side intent (customer order, quantity, due date) independently of production execution.
- Rename the current **Job** to **WorkOrder**, separating "what was ordered" from "what is being manufactured."
- Align the entity model with ISA-95 terminology already adopted in docs and scenario naming (`material`, `operations_definition`, `process_segment`), so the runtime code matches the documented standards alignment.

---

## 2. Non-Negotiable Constraints

1. **Determinism contract is preserved.** Same scenario + seed must produce identical results before and after the refactor. Verified by existing determinism tests (`crates/sim-core/tests/determinism.rs`).
2. **Scenario TOML schema evolution is explicit.** Add schema-aware loading with clear migration errors for unsupported versions; this plan only migrates the currently checked-in canonical examples (`examples/basic_scenario.toml`, `examples/overload_scenario.toml`, `examples/capacity_expansion_scenario.toml`). (`crates/sim-core/src/scenario.rs`, `crates/sim-types/src/scenario.rs`, `examples/basic_scenario.toml`).
3. **Event-driven architecture stays.** All state mutations flow through events and `EventHandler`. No direct state mutation from API or agent. (`docs/architecture.md`).
4. **Crate dependency DAG is preserved.** `sim-types` → `sim-core` → domain crates → `sim-api` → `sim-cli`. No circular dependencies. (`docs/architecture.md`).
5. **API contracts will be updated in a breaking manner.** REST + SSE payload shapes are expected to change, and implementation will target the new contract directly (`crates/sim-api/src/state.rs`, `crates/sim-api/src/routes.rs`).
6. **Headless runner must remain functional.** `sim-core::runner::run_scenario` is the primary execution path for testing and CLI. (`crates/sim-core/src/runner.rs`).

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

(Recommended) Introduce three new runtime entities (`Product`, `SalesOrder`, `WorkOrder`) in a phased refactor that preserves the existing event system and scenario format while intentionally changing public contracts where needed.

### ISA-95 Alignment Appendix

- `[[material]]` → runtime `Product` (`ProductId`, `name`) and routing linkage (`routing_id`) maintained in `ProductStore`.
- `[[operations_definition]]` and `[[process_segment]]` → runtime `Routing` and `RoutingStep` as execution sequence and machine/duration primitives.
- Demand-side lifecycle → `SalesOrder` (new runtime entity, emitted/handled as `OrderAccepted` + linked work execution).
- Execution-side lifecycle → `WorkOrder` (renamed from `Job`) with `work_order_id`, queueing, start/end events, and completion.
- Snapshot/API visibility is moved from flat `jobs: Vec<JobInfo>` to explicit `WorkOrderInfo` + `SalesOrderInfo` (+ optional `ProductInfo`) so monitoring reflects ISA-95 role separation.

Rationale:
- A `Product` runtime struct makes product names, properties, and future attributes (cost, category, BOM) available without re-parsing config.
- Splitting `SalesOrder` from `WorkOrder` enables modeling of order acceptance, due dates, backlog aging, and future multi-order-per-job batching — all without changing the event engine.
- The existing `Job` struct maps naturally to `WorkOrder`; the refactor is mostly renaming + extracting order-side fields.
- ISA-95 alignment is already documented and naming conventions are already in config; extending them to runtime closes the gap cheaply.
- A clear scenario schema version is introduced, canonical examples are migrated, and `SalesOrder` is derived automatically from `OrderCreation` events.

---

## 5. Phased Plan

### Phase 1. Introduce a runtime Product entity in sim-factory

Objective: Make product name and properties available at runtime, not just `ProductId`.

Planned work:

1. Add a `Product` struct to `crates/sim-factory/src/lib.rs` (new file `crates/sim-factory/src/product.rs`) with fields: `id: ProductId`, `name: String`, `routing_id: u64`. Expose via `pub mod product`.
2. Add a `ProductStore` (similar pattern to `MachineStore`, `RoutingStore`) that holds `Vec<Product>` and supports lookup by `ProductId`.
3. Move routing mapping ownership so `RoutingStore` stores routings only by routing ID; `ProductStore` owns `ProductId -> routing_id`.
4. Add `ProductStore` as a field on `FactoryHandler` and add a helper method `get_routing_for_product` that chains `ProductStore` + `RoutingStore` lookups.
5. Update `build_handler_from_config()` in `crates/sim-api/src/state.rs` to construct `ProductStore` from `MaterialConfig` entries and pass it to `FactoryHandler::new`.
6. Update `DemandModel` in `crates/sim-economy/src/demand.rs` to accept `Vec<ProductId>` (unchanged) or optionally reference product names for future per-product demand.
7. Add unit tests for `ProductStore` in the new `product.rs` module and `FactoryHandler::get_routing_for_product`.
8. Introduce a `schema_version` field in `SimulationParams` (`crates/sim-types/src/scenario.rs`), enforce supported versions in `crates/sim-core/src/scenario.rs`, and migrate `examples/*.toml` by adding `schema_version` to `[simulation]`.
9. Update `crates/sim-factory/src/process.rs` test helpers (`one_machine_one_product`, `two_step_handler`) and `crates/sim-factory/src/routing.rs` tests to match the new `ProductStore` ownership and removed routing API.
10. Keep `crates/sim-api/tests/scenario_baselines.rs` aligned with the production builder once `FactoryHandler::new` signature changes.

Files expected:
- `crates/sim-factory/src/product.rs` (new)
- `crates/sim-factory/src/lib.rs` (add `pub mod product`)
- `crates/sim-factory/src/process.rs` (constructor updates + helper tests)
- `crates/sim-factory/src/routing.rs` (remove `product_routing` ownership + tests)
- `crates/sim-api/src/state.rs` (build handler construction)
- `crates/sim-api/tests/scenario_baselines.rs` (reuse production builder)
- `crates/sim-types/src/scenario.rs` (add schema_version)
- `crates/sim-core/src/scenario.rs` (validate schema_version)
- `examples/basic_scenario.toml`
- `examples/overload_scenario.toml`
- `examples/capacity_expansion_scenario.toml`

Acceptance criteria:
- `Product` struct exists and is populated at runtime with name and routing_id.
- `FactoryHandler` can look up product name and routing by `ProductId` through one owned product mapping API.
- `RoutingStore` no longer has a `ProductId -> routing_id` map, preventing duplicate ownership of that mapping.
- All existing tests pass. Determinism tests pass.
- Scenario versioning is introduced and canonical example TOML files are migrated to include `schema_version`.
- No compilation warnings or dead code from removed `RoutingStore` methods (`add_product_routing`, `get_routing_for_product`).

---

### Phase 2. Introduce SalesOrder entity and split from Job

Objective: Separate "what was ordered" (demand-side) from "what is being produced" (execution-side).

Planned work:

1. Add a `SalesOrder` struct to `crates/sim-factory/src/order.rs` (new file) with fields: `id: OrderId`, `product_id: ProductId`, `quantity: u64`, `ordered_at: SimTime`, `price_at_order: f64`. Add `OrderId` to `crates/sim-types/src/lib.rs`.
2. Add `OrderStore` to manage sales orders (create, lookup, iterate).
3. Add `OrderStatus` enum: `Pending`, `Released`, `Fulfilled`, `Cancelled`.
4. Add `OrderAccepted` as an event path in `EventPayload`/`EventType` in `crates/sim-core/src/event.rs`, with `Event::new` mapping.
5. Modify `FactoryHandler::handle_order_creation` in `crates/sim-factory/src/process.rs` to first create a `SalesOrder`, schedule `OrderAccepted` on the `Scheduler` at current time as an explicit no-op passthrough event, then create a `WorkOrder` linked to it via `order_id: OrderId`.
6. Add `order_id: OrderId` field to `Job`/`WorkOrder` struct in `crates/sim-factory/src/jobs.rs`.
7. Add a configurable revenue mode in `EconomyConfig` with values `order_time` and `completion_time` (default `order_time`) and update completion-time calculation in `crates/sim-factory/src/process.rs`.
8. Add `OrderStore` to `FactoryHandler` in `crates/sim-factory/src/process.rs`.
9. Add `revenue_pricing: RevenuePricing` to `FactoryHandler` and pass it from `build_handler_from_config()` when constructing the factory.
10. Store completed `Job`/`WorkOrder` revenue on the execution entity so `SimSnapshot` can read it consistently.
11. Add a full-stack determinism integration test in `crates/sim-api/tests/determinism_full.rs` that runs the production builder path twice with the same seed.
12. Add unit tests for `SalesOrder` lifecycle, `OrderAccepted` emission/correlation, and the order-to-job linkage.

Files expected:
- `crates/sim-types/src/lib.rs` (add `OrderId`)
- `crates/sim-factory/src/order.rs` (new)
- `crates/sim-factory/src/lib.rs` (add `pub mod order`)
- `crates/sim-factory/src/jobs.rs` (add `order_id` field)
- `crates/sim-factory/src/process.rs` (handler wiring, `WorkOrder` revenue persistence)
- `crates/sim-api/src/state.rs` (builder wiring, snapshot read path)
- `crates/sim-api/tests/scenario_baselines.rs` (builder deduplication)
- `crates/sim-api/tests/determinism_full.rs` (new)

Acceptance criteria:
- Every `Job` has a linked `SalesOrder` via `order_id`.
- `SalesOrder` records the price at order time.
- Revenue mode is configurable via `EconomyConfig.revenue_pricing`, defaulting to `order_time`, with `completion_time` retained as an explicit alternate mode.
- Event stream includes `OrderAccepted` with an order ID for traceability.
- Existing `OrderCreation` event payload is unchanged — the split is internal to the factory handler.
- Determinism tests pass.

---

### Phase 3. Rename Job to WorkOrder

Objective: Align runtime naming with ISA-95 terminology and reduce confusion between order types.

Planned work:

1. Rename `Job` → `WorkOrder`, `JobId` → `WorkOrderId`, `JobStatus` → `WorkOrderStatus`, `JobStore` → `WorkOrderStore` across `sim-types`, `sim-factory`, and downstream crates.
2. Update `EventPayload::TaskStart` and `TaskEnd` to use `work_order_id` instead of `job_id`.
3. Update `SimSnapshot` / `JobInfo` in `crates/sim-api/src/state.rs` to move from `job_id` to `work_order_id` and include order linkage fields.
4. Update all test files that reference `Job`, `JobId`, `JobStatus`.
5. Update `docs/concepts.md` and `docs/architecture.md` to reflect the new naming.
6. Execute as a single atomic rename change set (mechanical refactor only), separate from any behavior changes.

Files expected:
- `crates/sim-types/src/lib.rs`
- `crates/sim-factory/src/jobs.rs` (rename to `work_order.rs` or keep file name, rename types)
- `crates/sim-factory/src/process.rs` (all `job_id` references)
- `crates/sim-factory/src/machines.rs` (active_jobs, queue types)
- `crates/sim-core/src/event.rs`
- `crates/sim-api/src/state.rs`
- `crates/sim-api/src/routes.rs`
- All test files in `crates/sim-factory/`, `crates/sim-core/tests/`, `crates/sim-api/tests/`

Acceptance criteria:
- All production-execution entities are named `WorkOrder` in code.
- API JSON output uses the new names directly.
- `docs/` reflect the new naming.
- All tests pass.

---

### Phase 4. Expose new entities through the API and update UI

Objective: Make the separated entities visible and useful to the API consumer and the experiment console.

Planned work:

1. Add `ProductInfo` to `SimSnapshot` or a new `/api/products` endpoint in `crates/sim-api/src/routes.rs` returning product name, routing info.
2. Add `SalesOrderInfo` to `SimSnapshot` or a new `/api/orders` endpoint, returning order status, price at order, linked work order IDs.
3. Replace legacy JSON field names with explicit `ProductInfo`, `SalesOrderInfo`, and `work_order_id` in the exposed payloads.
4. Update `JobInfo` in snapshot to `WorkOrderInfo` and include `order_id` reference.
5. Update `ui/src/api/client.ts` types to reflect the new payload shape.
6. Update `ui/src/stores/simulation.ts` to handle new data.
7. Update `JobTracker` component (`ui/src/components/dashboard/JobTracker.tsx`) to show order linkage.

Files expected:
- `crates/sim-api/src/state.rs` (snapshot shape)
- `crates/sim-api/src/routes.rs`
- `ui/src/api/client.ts`
- `ui/src/stores/simulation.ts`
- `ui/src/components/dashboard/JobTracker.tsx`

Acceptance criteria:
- API consumers can distinguish sales orders from work orders.
- Product names are available via API without re-reading the scenario.
- UI shows order-to-work-order linkage.
- Existing SSE event stream is migrated to the new event payload field names.

---

### Phase 5. Enable per-product demand curves and order-level priority (future-facing)

Objective: Leverage the separated entities to support richer demand modeling.

Planned work:

1. Extend `EconomyConfig` and `MaterialConfig` in `crates/sim-types/src/scenario.rs` with optional per-product economy overrides (demand weight, price sensitivity).
2. Extend `DemandModel` (`crates/sim-economy/src/demand.rs`) to use per-product demand parameters when available.
3. Add optional `priority` and `due_date` fields to `SalesOrder`.
4. Add priority-aware queue dispatch in `FactoryHandler::try_dispatch_from_queue` (`crates/sim-factory/src/process.rs`).

Files expected:
- `crates/sim-types/src/scenario.rs`
- `crates/sim-economy/src/demand.rs`
- `crates/sim-factory/src/order.rs`
- `crates/sim-factory/src/process.rs`
- `examples/*.toml` (extended, not breaking)

Acceptance criteria:
- Scenarios with per-product demand overrides produce different order distributions than uniform demand.
- Priority-ordered dispatch is observable via backlog metrics.
- Scenarios without overrides behave identically to before.

---

## 6. Validation Plan

1. Run `cargo test --workspace` after each phase to verify all existing tests pass.
2. Run `crates/sim-core/tests/determinism.rs` explicitly to verify determinism contract: same scenario + seed → identical `SimResult`.
3. Load each of the three example scenarios (`basic`, `overload`, `capacity_expansion`) via the API (`POST /api/scenario`) and verify successful load + run to completion.
4. After Phase 2, verify that `SalesOrder.price_at_order` matches the price at the tick when the order was generated, not the completion tick. Inspect via snapshot or add a targeted integration test.
5. After Phase 2, verify `revenue_pricing` behavior by testing both defaults and `completion_time` mode; document updated baselines where needed.
6. After Phase 3, verify the API JSON contract has migrated to the new field names and shapes.
7. After Phase 4, run the UI, load a scenario, and verify product names, order list, and work order linkage are visible.
8. After Phase 5, run a scenario with per-product demand overrides and verify differentiated demand distribution via KPI output.

---

## 7. Implementation Order

1. **Phase 1** — Product entity. No behavior change, pure addition. Establishes the pattern.
2. **Phase 2** — SalesOrder + order-job linkage. The core semantic improvement. Depends on Phase 1 for product context.
3. **Phase 3** — Rename Job → WorkOrder. Required to align runtime naming and complete the entity split.
4. **Phase 4** — API/UI exposure. Depends on Phases 1–3 for the entities to expose.
5. **Phase 5** — Per-product demand and priority. Depends on all prior phases for entity infrastructure.

---

## 8. Out of Scope

- **Bill of Materials (BOM) / material transformation** — planned for Phase 7 (`sim-material` crate) per architecture doc.
- **Multi-routing per product** — the capacity expansion workaround (duplicate materials) is acknowledged but not solved here. Addressing it requires routing selection logic that belongs in a separate plan.
- **Customer entity** — no customer/account model. Orders are anonymous demand.
- **Batch/process manufacturing** — Phase 7 scope, not touched here.
- **Full ISA-95 XML/B2MML serialization** — noted in `docs/standards-alignment.md` as post-MVP.
- **UI redesign** — Phase 4 updates existing components; no new pages or layout changes.
- **OpenAPI spec generation** — tracked separately.

---

## Findings

### F1: Revenue calculation change in Phase 2 alters simulation output
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** Phase 2 task 6 changes revenue from `current_price_at_completion * quantity` (`crates/sim-factory/src/process.rs`) to `price_at_order * quantity`. This changes numerical output for every scenario that has price changes during a run.

**Issue:** This intentionally changes simulation behavior, which means determinism tests comparing before/after will fail unless baselines are updated. The change is semantically correct (revenue should reflect the agreed price, not a future price), but it must be handled carefully.

**Recommendation:** Treat this as a deliberate behavior change. Update determinism test baselines after Phase 2. Document the change in a CHANGELOG or commit message. Keep `order_time` and `completion_time` as explicit options.

**Choices:**
- [x] Make revenue model configurable (`revenue_pricing: "order_time" | "completion_time"`, default `"order_time"`) and update baselines
- [ ] Hard-switch to order-time pricing and accept baseline breakage
- [ ] Defer revenue change to Phase 5

**[Applied]** Configurable `EconomyConfig::revenue_pricing` was added to Phase 2 tasks, and deterministic acceptance criteria now include both default behavior and `completion_time` mode.

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

**Context:** Phase 2 introduces `OrderId` and links it to `Job`, but the existing `EventPayload::OrderCreation` only carries `product_id` and `quantity` (`crates/sim-core/src/event.rs`). The `SalesOrder` is created inside `FactoryHandler`, so `order_id` is never visible in the event stream.

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

**Context:** Phase 1 introduces `ProductStore` holding product name and `routing_id`. `RoutingStore` already holds `product_routing: Vec<(ProductId, u64)>` (`crates/sim-factory/src/routing.rs`).

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

### F7: Phase 4 lacks an explicit breaking migration for API JSON
<!-- severity: major -->
<!-- dimension: gaps -->

**Context:** Phase 4 adds new fields to `SimSnapshot` and potentially new endpoints. Phase 3 renames `job_id` to `work_order_id`. The plan should make the breaking payload migration explicit for one clean-cut release.

**Issue:** Snapshot/API payloads are intentionally changing, so the migration and expected shape must be explicit to avoid implementation drift.

**Recommendation:** Phase 4 should specify: (a) rename payload fields in one atomic migration (no aliases), (b) define the new required JSON contract by endpoint, and (c) update UI and integration checks together.

**Choices:**
- [x] Execute a clean-cut payload migration with no aliases
- [ ] Keep old field names as aliases for one release

**[Applied]** `Phase 4` now uses direct renames with no alias fields and explicit contract updates.

**[Applied — Phase 3 and Phase 4 acceptance criteria now declare full payload migration.]**

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
| F7 | API JSON migration contract needs to be explicit | major | gaps | F2 |

---

### Iteration 1 — Applying findings

**F1 applied:** Phase 2 task 6 now specifies a configurable revenue model. Acceptance criteria include the explicit default and alternate behavior mode.

**F3 applied:** Phase 2 task list extended with task to introduce `OrderAccepted` event payload.

**F4 applied:** Phase 1 task list updated — `product_routing` mapping moves from `RoutingStore` to `ProductStore`.

**F5 applied:** Phase 2 test files explicitly named.

**F7 applied:** Phase 3 and Phase 4 acceptance criteria specify direct payload migration with no aliases.

---

### Iteration 1 — Re-sweep

### F8: Moving product_routing out of RoutingStore changes RoutingStore's public API
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** F4 recommendation moves `product_routing` from `RoutingStore` into `ProductStore`. `RoutingStore::get_routing_for_product` (`crates/sim-factory/src/routing.rs`) is called in `FactoryHandler::handle_order_creation` (line 104) and `handle_task_end` (line 161).

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

**Context:** F3 introduces an `OrderAccepted` event payload. This also requires a new `EventType::OrderAccepted` variant in `crates/sim-core/src/event.rs` and corresponding dispatch in handlers.

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

---

## Findings — Implementation-Readiness Review

### F10: Stale line-number references throughout the plan
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** The plan cites specific line ranges for almost every source file. Many of these are stale or imprecise when verified against the current codebase:

- `crates/sim-types/src/scenario.rs` → `MaterialConfig` starts at line 69 (correct), but `OperationsDefinitionConfig` ends at line 96 (correct).
- `crates/sim-factory/src/jobs.rs` → `Job` struct is at lines 8-23, not 6; `JobStore` continues to line 166, not 88.
- `crates/sim-factory/src/process.rs` → `FactoryHandler` struct is at lines 16-28 (correct), but line refs to `handle_order_creation` say 97-136 — the actual function spans 97-136 (correct). Revenue calculation says line 154-156 — actual `total_revenue` addition is at line 155 (correct). However, `try_dispatch_from_queue` is cited as `47-95` — actual is 48-95.
- `crates/sim-factory/src/routing.rs` → `RoutingStep` starts at 14, `RoutingStore` starts at line 43, `product_routing` is at line 46 (correct).
- `crates/sim-economy/src/demand.rs` → `generate_orders` spans lines 56-88 (off by one). Lines `16-24` for `DemandModel` struct are actually 16-24 (correct). Lines `70-84` for order generation are actually 70-84 (correct). Lines `74-75` for random product selection are actually line 74 (correct).
- `crates/sim-api/src/state.rs` → `IntegratedHandler` struct is at 153-159; its `EventHandler` impl is 161-188 (correct). Line refs `96-112` for snapshot — `SimSnapshot` is at 98-112 (close). Line ref `83-94` for `JobInfo` — actual is 83-94 (correct). `build_handler_from_config` is cited as `190-232` — actual is 190-267 (much longer).
- `crates/sim-api/src/routes.rs` → `query_jobs` is at lines 261-264 (correct).
- `crates/sim-core/src/event.rs` for `EventType` — actual is 8-17 (correct). Lines `22-25` for `OrderCreation` payload are 22-25 (correct). Lines `21-35` for `EventPayload` — actual range is 21-48 (broader than cited).
- `crates/sim-core/src/runner.rs` → `run_scenario` is at lines 26-90 (correct).
- `crates/sim-core/src/scenario.rs` → `load_scenario` is at lines 9-17 (correct).
- `crates/sim-types/src/lib.rs` → `JobId` is at lines 23-25 (off by one). Lines `138-145` for `JobStatus` — actual is 138-145 (correct).
- `docs/standards-alignment.md` → ISA-95 mapping table is at lines 44-55 (correct). Line 57 for "Not in MVP" is at line 57 (correct).
- `docs/standards-alignment.md` cited in section 3.5 — actual ISA-95 table is at 46-53 (correct).

**Issue:** A coding agent executing tasks like "modify `crates/sim-api/src/state.rs`" will find that `build_handler_from_config` actually spans lines 190-267. This can cause the agent to miss the latter half of the function (product_ids construction, demand model setup, etc.) or apply edits to the wrong location.

**Recommendation:** Replace specific line references in **Planned work** and **Files expected** sections with symbol-level references (e.g., `build_handler_from_config()` instead of `190-232`). Keep line references only in the **Verified Current State** section (section 3) where they serve as audit snapshots. A coding agent should grep for symbols, not rely on line numbers.

**Choices:**
- [x] Replace line-number references in Phase task lists with symbol-level references; keep line refs only in section 3 as snapshot markers
- [ ] Update all line numbers to current values
- [ ] Leave as-is; line refs are approximate

**[Applied]** Phase task lists and `Files expected` entries now use symbol-level references for implementation targets; line references are retained only in section 3 audit snapshots.

---

### F11: Determinism tests use NoopHandler — they do not validate factory/economy behavior
<!-- severity: major -->
<!-- dimension: testing -->

**Context:** The plan's non-negotiable constraint #1 says: "Verified by existing determinism tests (`crates/sim-core/tests/determinism.rs`)." Phase 2 acceptance criteria say: "Determinism tests pass."

**Issue:** The actual determinism tests in `crates/sim-core/tests/determinism.rs` use a `NoopHandler` that does nothing — it does not construct a `FactoryHandler`, `DemandModel`, or any domain handler. The tests only verify that the runner's event scheduling is deterministic (DemandEvaluation events fire at the right ticks). They do **not** verify that factory + economy behavior produces identical results across runs.

The `crates/sim-api/tests/scenario_baselines.rs` file has an `IntegratedHandler` that wires up factory + demand + pricing, and it runs the basic/overload scenarios — but it does not perform determinism checks (same seed → identical output across two runs).

This means Phase 2's revenue model change, order-to-job linkage, and `OrderAccepted` event emission have **no existing determinism test** that would catch non-deterministic behavior. The plan's reliance on "determinism tests pass" is weaker than it appears.

**Recommendation:** Phase 2 should add a new determinism integration test in `crates/sim-api/tests/` that runs a full `IntegratedHandler` twice with the same scenario+seed and asserts `event_log`, `final_time`, and `total_revenue` are identical. This closes the gap between the plan's determinism claim and actual test coverage.

**Choices:**
- [x] Add a full-stack determinism test in `crates/sim-api/tests/determinism_full.rs` as a Phase 2 task
- [ ] Extend existing `crates/sim-core/tests/determinism.rs` to use an integrated handler
- [ ] Accept current coverage as sufficient

**[Applied]** A full-stack integration determinism test is now planned as `crates/sim-api/tests/determinism_full.rs` in Phase 2.

---

### F12: `scenario_baselines.rs` duplicates `build_handler_from_config` — Phase 2 must update both
<!-- severity: major -->
<!-- dimension: gaps -->

**Context:** `crates/sim-api/tests/scenario_baselines.rs` contains a standalone `build_handlers_from_scenario()` function and a local `IntegratedHandler` struct that duplicate the logic in `crates/sim-api/src/state.rs`. The test version constructs `MachineStore`, `RoutingStore`, `FactoryHandler`, `DemandModel`, and `PricingState` independently.

**Issue:** Phase 1 changes `FactoryHandler::new` to accept a `ProductStore` instead of `product_ids`, and removes `product_routing` from `RoutingStore`. Phase 2 adds `OrderStore`, `OrderAccepted` events, and revenue mode configuration. **Both** the production `build_handler_from_config` and the test `build_handlers_from_scenario` must be updated in lockstep, or the integration tests will fail to compile.

The plan's **Files expected** sections for Phases 1 and 2 do not mention `crates/sim-api/tests/scenario_baselines.rs`.

**Recommendation:** Add `crates/sim-api/tests/scenario_baselines.rs` to the Files expected list for Phase 1 and Phase 2. Consider refactoring the test helper to reuse the production `build_handler_from_config` to eliminate the duplication.

**Choices:**
- [x] Add `crates/sim-api/tests/scenario_baselines.rs` to Phase 1 and Phase 2 file lists; refactor to reuse production builder
- [ ] Add to file lists but keep separate test builder
- [ ] Leave as-is; tests will break and be fixed reactively

**[Applied]** `crates/sim-api/tests/scenario_baselines.rs` is now listed in both phase file lists and is designated to use the shared production builder path instead of duplicated setup logic.

---

### F13: Phase 2 `OrderAccepted` event is emitted but no handler processes it
<!-- severity: minor -->
<!-- dimension: gaps -->

**Context:** Phase 2 task 5 says `FactoryHandler` emits `OrderAccepted` after creating a `SalesOrder`. Phase 2 also adds `OrderAccepted` to `EventType` and `EventPayload` (per F3/F9 applied).

**Issue:** The `OrderAccepted` event is scheduled onto the `Scheduler`, which means it will be dequeued and dispatched to the `IntegratedHandler` in the main loop. But no handler (`FactoryHandler`, `DemandModel`, `PricingState`, `SalesAgent`) has a match arm for it. The event will be silently ignored (the `_ => {}` catch-all in each handler), which is fine for now — but it means `OrderAccepted` events at the same time as other events could alter dispatch order due to the `BinaryHeap`'s tie-breaking behavior, potentially affecting determinism.

**Recommendation:** `OrderAccepted` should be scheduled onto the `Scheduler` at current time as a no-op passthrough event. This keeps observability via the normal event pipeline without adding handler complexity; only `_ => {}` passthrough behavior is required.

**Choices:**
- [ ] Emit `OrderAccepted` directly into the event log (not onto the scheduler) from within `FactoryHandler`; add it to SSE broadcast separately
- [x] Schedule it normally and add `_ => {}` documentation noting it's a no-op
- [ ] Defer to Phase 4

**[Applied]** `OrderAccepted` will be scheduled on the `Scheduler` at current time and treated as a documented no-op passthrough event.

Depends on: F3 choice (introduce `OrderAccepted` event)

---

### F14: Phase 1 plan does not account for `RoutingStore::add_product_routing` call sites
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** Phase 1 (applied F4) moves `product_routing` from `RoutingStore` into `ProductStore`. This removes `RoutingStore::add_product_routing()` and `RoutingStore::get_routing_for_product()`.

**Issue:** `add_product_routing()` is called in three places:
1. `crates/sim-api/src/state.rs` — `routings.add_product_routing(ProductId(mat.id), mat.routing_id)`
2. `crates/sim-api/tests/scenario_baselines.rs` — same call
3. `crates/sim-factory/src/process.rs` tests (lines 296, 325) — `routings.add_product_routing(ProductId(1), 1)`

`get_routing_for_product()` is called in:
1. `crates/sim-factory/src/process.rs` — `self.routings.get_routing_for_product(product_id)`
2. `crates/sim-factory/src/process.rs` — same
3. `crates/sim-factory/src/process.rs` — same

The plan's Phase 1 **Files expected** lists `process.rs` and `sim-api/src/state.rs`, but does not mention the test helpers in `process.rs` (lines 281-328) or `scenario_baselines.rs`. Every test that constructs a `FactoryHandler` with a `RoutingStore` + `add_product_routing` must be updated.

**Recommendation:** Phase 1 Files expected should explicitly list all call sites that break: `crates/sim-factory/src/process.rs` (tests module), `crates/sim-factory/src/routing.rs` (remove methods + tests), `crates/sim-api/tests/scenario_baselines.rs`. The Phase 1 acceptance criteria should add: "No compilation warnings or dead code from removed `RoutingStore` methods."

**Choices:**
- [x] Expand Phase 1 Files expected to include all call sites: `routing.rs` (method removal + test updates), `process.rs` tests, `scenario_baselines.rs`
- [ ] Leave Files expected as-is and rely on compiler errors

**[Applied]** Phase 1 now includes explicit `routing.rs` migration, `process.rs` test helper updates, and `scenario_baselines.rs` alignment with the production handler construction.

---

### F15: Phase 2 `FactoryHandler` needs access to current price but currently receives it via `set_current_price`
<!-- severity: major -->
<!-- dimension: correctness -->

**Context:** Phase 2 task 7 adds a configurable revenue model. When `revenue_pricing = "order_time"`, revenue uses `SalesOrder.price_at_order`. When `revenue_pricing = "completion_time"`, revenue uses the current price at job completion, which is the existing behavior.

**Issue:** Currently, the current price flows into `FactoryHandler` via `IntegratedHandler.handle_event()` calling `self.factory.set_current_price(self.pricing.current_price)` before dispatching to the factory handler (`crates/sim-api/src/state.rs`). Then `handle_task_end` receives `self.current_price` as a parameter (`process.rs`). For `order_time` pricing, `FactoryHandler` needs access to the `SalesOrder`'s `price_at_order`, which it can get from `OrderStore`. But the plan does not specify how `FactoryHandler` knows which revenue mode to use — `EconomyConfig` is a scenario-level config and `FactoryHandler` currently has no reference to it.

**Recommendation:** `FactoryHandler::new()` should accept a `revenue_pricing` mode parameter (e.g., an enum). This should be passed down from `build_handler_from_config` which already reads `EconomyConfig`. The plan should specify this wiring explicitly.

**Choices:**
- [x] Add `revenue_pricing: RevenuePricing` enum field to `FactoryHandler`; pass from `build_handler_from_config`
- [ ] Store revenue mode on `OrderStore` and let it handle calculation
- [ ] Read from a global config reference

**[Applied]** `FactoryHandler` now carries `revenue_pricing: RevenuePricing` and receives it from `build_handler_from_config()`.

---

### F16: `SimSnapshot.JobInfo.revenue` computed differently than `FactoryHandler.total_revenue`
<!-- severity: minor -->
<!-- dimension: correctness -->

**Context:** In `build_snapshot()` (`crates/sim-api/src/state.rs`), per-job revenue is calculated as `handler.pricing.current_price * j.quantity as f64` for completed jobs. But `FactoryHandler.total_revenue` is accumulated as `current_price * quantity` at the **moment of completion** (`process.rs`). These use different prices — the snapshot uses the price *at snapshot time*, while the accumulator uses the price *at completion time*.

**Issue:** After Phase 2 introduces `price_at_order` on `SalesOrder`, there will be **three** different revenue calculations:
1. `FactoryHandler.total_revenue` — accumulated at completion time using whatever the revenue mode dictates.
2. `SimSnapshot.JobInfo.revenue` — computed at snapshot time using current price (wrong even today).
3. The new `SalesOrder.price_at_order` — the correct order-time price.

Phase 2 must also fix `build_snapshot`'s per-job revenue to be consistent with the configured revenue mode. The plan does not mention `build_snapshot` at all.

**Recommendation:** Phase 2 should update `build_snapshot()` to compute per-job revenue from `SalesOrder.price_at_order` (when `order_time` mode) or from a stored completion-time price. This requires either storing the revenue on the `Job`/`WorkOrder` directly, or looking up the linked `SalesOrder` in `build_snapshot`. Add `crates/sim-api/src/state.rs` `build_snapshot()` to Phase 2 files.

**Choices:**
- [x] Store computed revenue on `Job` at completion time (single source of truth); `build_snapshot` reads it
- [ ] Look up `SalesOrder` in `build_snapshot` for each completed job
- [ ] Defer snapshot revenue fix to Phase 4

**[Applied]** Work orders now persist computed revenue at completion, and `build_snapshot()` is planned to consume that stored value for consistency.

---

### F17: Phase 1 missing test for `FactoryHandler::new` signature change
<!-- severity: minor -->
<!-- dimension: testing -->

**Context:** Phase 1 changes `FactoryHandler::new` to accept `ProductStore` instead of `Vec<ProductId>`. Every existing test in `crates/sim-factory/src/process.rs` (tests module, lines 281-566) constructs `FactoryHandler` via helpers `one_machine_one_product()` and `two_step_handler()` that call `FactoryHandler::new(machines, routings, vec![ProductId(1)])`.

**Issue:** The plan's Phase 1 acceptance criteria say "All existing tests pass" but do not call out that the existing test helpers must be rewritten to construct and pass a `ProductStore`. The test helpers also call `routings.add_product_routing()` which Phase 1 removes from `RoutingStore`. This is a significant refactor of the test scaffolding — not just the production code.

**Recommendation:** Phase 1 task list should include a dedicated task: "Update `process.rs` test helpers (`one_machine_one_product`, `two_step_handler`) to construct `ProductStore` and pass it to `FactoryHandler::new`." This makes the scope explicit for a coding agent.

**Choices:**
- [x] Add explicit task to Phase 1: update `process.rs` test helpers and `routing.rs` test for new store API
- [ ] Rely on "all tests pass" as implicit coverage

**[Applied]** Phase 1 explicitly captures both `process.rs` helper migrations and `routing.rs` test updates for the new store API.

---

### F18: `FactoryHandler` cannot emit events to the event log directly — architectural constraint
<!-- severity: major -->
<!-- dimension: gaps -->

**Context:** `FactoryHandler` receives `&mut self` and `&mut Scheduler` in `handle_event` but has no direct access to `EventLog` or the broadcast channel. This makes scheduler-based emission the pragmatic route.

**Issue:** The event log is maintained by the runner loop (`crates/sim-core/src/runner.rs`) and the simulation thread (`crates/sim-api/src/state.rs`). `EventHandler::handle_event` cannot append to the log — it can only schedule new events on the `Scheduler`. This means `OrderAccepted` **must** go through the scheduler to appear in the event log and SSE stream, unless the `EventHandler` trait or the integrated handler is modified.

**Recommendation:** Two viable approaches:
(a) Schedule `OrderAccepted` at the current time — it will be dispatched, logged, and broadcast like any other event. The determinism concern from F13 is minimal because `BinaryHeap` ordering for same-time events is already deterministic (stable insertion order via the heap).
(b) Extend `EventHandler::handle_event` return type to include emitted events (e.g., `Vec<Event>`), and have the runner/integrated handler log them. This is a larger architectural change.

Option (a) is simpler and consistent with how `SalesAgent` already emits `PriceChange` and `AgentDecision` events at the current time.

**Choices:**
- [x] Schedule `OrderAccepted` on the `Scheduler` at current time (same pattern as `SalesAgent` events); document as no-op passthrough
- [ ] Extend `EventHandler` trait to return emitted events
- [ ] Defer `OrderAccepted` to Phase 4

**[Applied]** `OrderAccepted` is confirmed to use scheduler-based no-op passthrough emission (matching the choice at line 677) for visibility in event log and SSE.

Depends on: F13

---

### Summary

| # | Title | Severity | Dimension | Depends on |
|---|-------|----------|-----------|------------|
| F10 | Stale line-number references in plan | major | correctness | — |
| F11 | Determinism tests use NoopHandler | major | testing | — |
| F12 | `scenario_baselines.rs` duplicates builder | major | gaps | — |
| F13 | `OrderAccepted` event emitted but not handled | minor | gaps | F3 |
| F14 | Phase 1 missing `add_product_routing` call sites | major | correctness | F4 |
| F15 | Revenue mode not wired to `FactoryHandler` | major | correctness | F1 |
| F16 | Snapshot per-job revenue inconsistent with total_revenue | minor | correctness | F1, F15 |
| F17 | Phase 1 test helpers need explicit rewrite task | minor | testing | F14 |
| F18 | `FactoryHandler` cannot emit to event log directly | major | gaps | F13 |
