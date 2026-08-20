# Separate Product Definition and Production Workflow Into First-Class Entities

> **Status:** Idea backlog. Not planned in detail — the codebase this was originally scoped against (Rust `crates/`) no longer exists after the Java rewrite. Confirmed 2026-08-20 that the underlying problem is still present in the current Java code.

## Problem

`Job` (`java/sim-factory/src/main/java/com/arcogine/factory/jobs/Job.java`) conflates two different concepts in one class:

- **Order-side fields**: `productId`, `quantity`, `unitPrice`, `createdAt` — what was ordered, at what price.
- **Execution-side fields**: `status`, `currentStep`, `currentMachine`, `completedAt` — what's happening on the shop floor.

There's no `SalesOrder` entity representing demand independently of production, and no `Product` entity beyond a bare `ProductId(long value)` — product name/properties aren't available at runtime.

## Idea

Split `Job` into a `SalesOrder` (demand-side intent) and a `WorkOrder` (production execution), and introduce a real `Product` entity. This also aligns runtime naming with the ISA-95 terms already used in scenario TOML (`material`, `process_segment`) per `docs/standards-alignment.md`.

Needs a fresh scoping pass against the current Java modules (`sim-types`, `sim-factory`, `sim-api`) before it's actionable — nothing below assumes any of the old Rust-era phasing still applies.
