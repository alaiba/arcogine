# Arcogine — Concepts

This page explains what Arcogine simulates and how to interpret what you see in the UI. Read this before diving into scenarios.

This document describes the **current factory-simulation experience** — one current mode of engaging with Arcogine, not the complete Arcogine product ontology. See the [Product Charter](charter.md) for the enduring product vision.

## The big picture

Arcogine models a simplified factory that makes products and sells them. Three systems interact in a feedback loop:

```text
     You set a price
           │
           ▼
    Demand responds        (lower price → more orders)
           │
           ▼
    Factory produces        (machines process jobs through routing steps)
           │
           ▼
    KPIs update             (throughput, lead time, backlog, revenue)
           │
           ▼
    You (or the agent)      (observe KPIs, adjust price or machines)
    make decisions
           │
           └───────────────► loop repeats
```

Your goal is to keep this loop healthy: enough demand to generate revenue, enough capacity to fulfill it, and short enough lead times that demand doesn't collapse.

## Core vocabulary

| Term | Meaning |
|------|---------|
| **Tick** | One unit of simulation time. Events happen at specific ticks. |
| **Event** | Something that happens: an order arrives, a machine starts work, a task finishes, the price changes. The simulation advances by processing events in time order. |
| **Scenario** | A TOML file that defines the factory setup: machines, products, routings, and economic parameters. Loading a scenario configures the entire simulation. |
| **Seed** | The random number seed in the scenario. Same seed = same results every time (deterministic simulation). |

## The factory

### Machines

Machines (also called equipment in ISA-95 terminology) are the physical resources that do work. Each machine can process one job at a time. You can toggle machines online/offline during a run.

A machine that is **offline** stops accepting new jobs. A machine can only be taken offline while it is **idle**: if it has active jobs, the request is rejected and the machine keeps running until its current work completes. Wait for the machine to become idle, then take it offline. Once offline, the machine can be brought back online at any time.

### Products and routings

A **product** (e.g., "Widget A") has a **routing** — an ordered list of processing steps. Each step requires a specific machine and takes a specific number of ticks.

Example: Widget A's routing might be Mill (5 ticks) → Lathe (3 ticks) → QC Station (2 ticks). A job for Widget A must visit all three machines in order.

### Orders and jobs

When an order arrives, Arcogine stores an immutable **order** containing the accepted product, quantity, creation time, and unit price. It then creates a mutable **job** that references that order and moves through the routing steps, waiting in machine queues when a machine is busy.

The current implementation remains one order → one job. This separation exists so accepted intent does not have to mutate as execution progresses; later engine-readiness work can allow one order to produce multiple work items/jobs without copying the commercial/order facts into each execution object.

Order quantity now consumes proportional production work: a job's routing repeats once per unit of quantity, so an order for 10 units keeps a machine occupied roughly ten times as long as an otherwise identical order for 1 unit. The job's step counter (`current_step` of `total_steps`) advances once per routing step *executed*, not once per unit -- for a multi-step route it advances once per step, so it reaches `total_steps` only after every step has run for every unit. Completed-unit progress can be derived from it (`current_step / steps per unit`), but the counter itself is an executed-step count, not a unit count. This is represented as one job with a larger step count -- not as ten separate jobs -- so a large-quantity order still only creates one order and one job.

The current lifecycle is:

1. **Order accepted** — the order event freezes the unit price and quantity in immutable order intent
2. **Job created** — one execution job references that order, with its routing sized to repeat once per unit of quantity
3. **In progress** — the job is processed on a machine or waits in a queue, one routing pass per unit
4. **Completed** — all routing steps for every unit finish; the referenced order's sales value (quantity x its locked-in price) is added to completed sales value exactly once

The existing API/UI job projection still shows product, quantity, and completed value on each job for compatibility. Those values are now read from the immutable referenced order rather than stored as mutable job-owned state.

## The economy

### Price and demand

The price you set is your **offer price** — the ask currently on the table for new customers, not an external market signal (Arcogine doesn't model the broader market, just the firm's own pricing decisions). The economy model connects it to order volume:

- **Base demand** — how many orders per evaluation period at the reference price
- **Price elasticity** — how strongly demand responds to price changes (higher elasticity = more sensitive)
- **Lead time sensitivity** — demand also drops when lead times grow (customers don't want to wait)

Lowering the offer price increases demand. But more orders means more factory load, which increases lead times, which suppresses demand. Finding the equilibrium is the challenge.

Changing the offer price only affects **future** orders — evaluated the next time demand is sampled. It never changes the terms of an order that already exists (see below).

### Completed sales value

Each order locks in its unit price **at the moment it's created**, using whatever the offer price was at that instant. That price stays on the immutable order for its entire lifecycle and never changes, even if the offer price moves while the associated job is still in production. An order created at $10 is still worth `quantity x $10` when it finishes, no matter what the offer price is by then.

The KPI dashboard's completed-sales figure (`completedSalesValue` in the API) is the sum of `quantity x unit price` for every order that has finished production — each using its own locked-in price, not whatever the offer price happens to be right now.

This is a deliberate product decision, not sophistication in accounting: "Completed sales value" is an operational number (how much value has this factory shipped), not a claim about recognized revenue. Arcogine has a small, separate Finance domain (a minimal double-entry ledger) that owns financial concepts like cash and a formally-recorded sales balance — under its current, deliberately simple immediate-settlement policy those numbers happen to match the operational figure above, but they answer a different question ("what has been financially recorded" vs. "what value has completed production") and aren't guaranteed to stay equal if Finance's policy evolves. See [`docs/architecture/overview.md`](../architecture/overview.md#pricing-orders-and-money-offerprice-vs-orderprice) for the full OfferPrice/OrderPrice model, the "Commercial, Operational, and Financial Truth" section for the Finance domain, and [`docs/planning/architecture-assessment-events-state-observations.md`](../planning/architecture-assessment-events-state-observations.md) for the current-state assessment and remaining migration plan.

## KPIs (Key Performance Indicators)

The dashboard shows four primary metrics, aligned with ISO 22400 definitions:

| KPI | What it measures | What to watch for |
|-----|-----------------|-------------------|
| **Throughput** | Jobs completed per unit time | Dropping throughput means a bottleneck or insufficient demand |
| **Lead time** | Average time from order creation to job completion | Rising lead time signals congestion or too much WIP |
| **Backlog** | Number of orders waiting or in progress | Growing backlog means demand outpaces capacity |
| **Utilization** | Fraction of time machines are actively working | Near 100% means machines are saturated; near 0% means idle capacity |

These four metrics are connected by Little's Law: Backlog ≈ Throughput x Lead Time. If you push throughput up without reducing lead time, backlog grows.

## The agent

The **Sales Agent** is an automated decision-maker that observes KPIs and adjusts the price. When enabled, it runs periodically (every `agent_eval_interval` ticks) and:

- Raises the price when backlog is high or lead times are growing
- Lowers the price when the factory has spare capacity

You can toggle the agent on and off at any time. This lets you compare manual control against the agent's strategy, or use the agent as a starting point and fine-tune from there.

## Baselines

You can **save a baseline** — a snapshot of the current KPI state — at any point during a run. Baselines let you:

- Record the state before making a change
- Compare two different strategies on the same scenario
- Track whether your interventions are actually improving things

The UI supports up to 3 saved baselines per session.

## The three built-in scenarios

### Basic — learn the controls

A balanced factory with moderate demand. Nothing is broken; use this to understand the UI, the simulation flow, and how the controls work.

**What to try:** Run the sim, watch KPIs stabilize, then try changing the price. Save a baseline, toggle the agent, and compare.

### Overload — manage a crisis

Low starting price drives high demand. The factory can't keep up, so backlog grows and lead times spike. Your job is to stabilize the system.

**What to try:** Raise the price to reduce demand pressure. Observe how backlog and lead time respond. Find the price that balances revenue against capacity.

### Capacity Expansion — structural versus tactical

Same demand pressure as Overload, but with additional machines. Compare whether adding capacity is more effective than tuning price.

**What to try:** Run both Overload and Capacity Expansion with the same strategy, then compare the baselines. Does more hardware beat better pricing?

## Simulation controls

| Control | What it does |
|---------|-------------|
| **Run** | Start the simulation. It advances continuously until paused, completed, or max ticks reached. |
| **Pause** | Stop advancing. The simulation state is preserved; you can inspect, adjust, and resume. |
| **Step** | Advance by exactly one event. Useful for understanding cause and effect. |
| **Reset** | Reload the current scenario from scratch. All state is cleared. |
| **Load scenario** | Parse and load a new scenario TOML. This also resets the simulation. |

## Headless mode

You can run scenarios without the UI:

```bash
java -jar dist/api/arcogine.jar run docs/examples/basic.toml
```

This executes the full simulation and prints a summary to stdout. Useful for batch comparisons or scripted experiments.

## What's next

- To set up the project, see the [Quick start](../../README.md#quick-start) in the root README.
- To understand the architecture, see [architecture/overview.md](../architecture/overview.md).
- To interact with the API directly, see [reference/api.md](../reference/api.md).
- To contribute, see [CONTRIBUTING.md](../../.github/CONTRIBUTING.md).
