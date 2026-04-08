# Arcogine — Concepts

This page explains what Arcogine simulates and how to interpret what you see in the UI. Read this before diving into scenarios.

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

A machine that is **offline** stops accepting new jobs. Jobs already in progress on that machine will still complete, but no new work starts until you bring it back online.

### Products and routings

A **product** (e.g., "Widget A") has a **routing** — an ordered list of processing steps. Each step requires a specific machine and takes a specific number of ticks.

Example: Widget A's routing might be Mill (5 ticks) → Lathe (3 ticks) → QC Station (2 ticks). A job for Widget A must visit all three machines in order.

### Jobs

When an order arrives, it creates a **job**. The job moves through the routing steps, waiting in machine queues when the machine is busy. A job's lifecycle is:

1. **Created** — an order event generates it
2. **In progress** — being processed on a machine, or waiting in a queue
3. **Completed** — all routing steps finished; revenue is recorded

## The economy

### Price and demand

The economy model connects your pricing decisions to order volume:

- **Base demand** — how many orders per evaluation period at the reference price
- **Price elasticity** — how strongly demand responds to price changes (higher elasticity = more sensitive)
- **Lead time sensitivity** — demand also drops when lead times grow (customers don't want to wait)

Lowering the price increases demand. But more orders means more factory load, which increases lead times, which suppresses demand. Finding the equilibrium is the challenge.

### Revenue

Revenue accumulates as completed jobs are sold at the current price. Total revenue = completed sales x price at completion. The KPI dashboard tracks this in real time.

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
cargo run --bin arcogine -- run --headless --scenario examples/basic_scenario.toml
```

This executes the full simulation and prints a summary to stdout. Useful for batch comparisons or scripted experiments.

## What's next

- To set up the project, see the [Quick start](../README.md#quick-start) in the root README.
- To understand the architecture, see [architecture.md](architecture.md).
- To interact with the API directly, see [api.md](api.md).
- To contribute, see [CONTRIBUTING.md](../CONTRIBUTING.md).
