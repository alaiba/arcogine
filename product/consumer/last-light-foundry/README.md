# Last Light Foundry

A local, single-player factory-optimization game powered by the real Arcogine engine. The last orbital foundry must build a rescue beacon before the station goes dark: you buy equipment, place it, publish the design, run it, read what the engine did, and improve the design. The browser only draws what the Java server reports; every cost, validity verdict, tick, completion and score comes from the Challenge, Factory and runtime APIs (see the [integration design](../../../docs/architecture/last-light-foundry.md)).

This is the first slice of the game: two contracts, one browser UI, no accounts, no network services other than the local server, and no manual JSON editing.

## Launch

Prerequisites: JDK 21 or newer. The first run also downloads the pinned Gradle distribution; after that, play needs no network access.

```bash
./arcogine play
```

(Equivalent: `cd product && ./gradlew :last-light-foundry:run`.) Then open <http://127.0.0.1:8551/>. Press Ctrl+C in the terminal to stop.

On Windows without a Bash shell, run `cd product` then `gradlew.bat :last-light-foundry:run`.

Configuration is by environment variable:

| Variable | Default | Meaning |
|---|---|---|
| `LLF_PORT` | `8551` | Port to listen on. |
| `LLF_HOST` | `127.0.0.1` | Interface to bind. The server has no authentication; leave it on loopback. |

## How to play

1. **Pick a contract** from the tabs at the top. *Wake the Foundry* teaches the loop; *The Relay* is the calibrated capacity challenge.
2. **Build.** Select a machine in the palette, then click an empty cell on the floor. Click a machine to select it, then click an empty cell to move it. Sell it from the inspector or with Delete. Undo and redo with Ctrl+Z / Ctrl+Y. The budget and equipment limits are shown live.
3. **Check design.** Two independent authorities answer. The *contract* says whether the design fits the budget, the equipment limits and the floor. The *Factory model* says whether the design can be published (every routing step needs a machine that can run it). A design must pass both.
4. **Publish and start.** This publishes an immutable Factory model and starts a run with one real order of the contract's quantity. The engine decides how the order is split into jobs, who works on what, and when.
5. **Step or Play.** *Step* asks the engine to process exactly one event. *Play* keeps asking at the chosen speed; speed only changes how quickly the screen asks, never the result. Pause any time.
6. **Read the result.** The result panel shows when the order shipped, the deadline margin, the unused budget and the **reference score** from the Challenge evaluation policy: `1 + deadline margin + unused budget` when the contract is met, `0` otherwise. A faster design is not automatically a higher-scoring one.
7. **Retry or edit.** *Retry* starts a fresh run of the same published design (the previous run is untouched). *Edit design* returns to the floor so you can improve the design. After two finished attempts the result panel compares them with the Challenge comparator.

The floor is a schematic: where a machine sits never changes how it processes work. The number shown on each machine (`#1`, `#2`, ...) is its identity in the engine; it is assigned when you place the machine, never changes when you move it, and is never reused after you sell it.

### Contracts

| Contract | Goal | Notes |
|---|---|---|
| Wake the Foundry | Ship 3 lamps in 30 ticks with 400 credits | Guided: the starter draft is admissible but cannot be published until you place an inspector. |
| The Relay | Ship 12 relay beacons in 57 ticks with 1,062 credits | Cut 3, assemble 4, inspect 5 ticks. Machines: cutter 150, assembler 200, twin assembler 360 (works two at once), inspector 250, flex cell 313 (assembles or inspects). Several structurally different designs win; a cheap single line misses the deadline. |

### Keyboard

Focus the floor, then: arrow keys move the cursor, Enter places the selected machine or selects what is under the cursor, Shift+arrows move the selected machine, Delete sells it, Ctrl+Z / Ctrl+Y undo and redo. The animation of busy machines respects the reduced-motion setting of your system.

## Troubleshooting

| Symptom | Likely cause |
|---|---|
| Banner says the server cannot be reached | The Java process stopped. Restart `./arcogine play`; nothing was changed on the server by the failed request. |
| "Address already in use" at start | Another process holds the port. Set `LLF_PORT` to a free port. |
| A design is refused with "cannot be run" | Read the Design reports panel: either the contract (budget, limit, overlap, bounds) or the Factory model (a step with no eligible machine) rejected it. |
| Old draft vanished after a page reload | Drafts, runs and attempts live in memory only in this slice; see Limits. |

## Limits of this slice

- Drafts, run history and attempts are not saved: a page reload resets the editor, and a server restart ends all runs.
- No engine checkpoint, material transport, distance-dependent processing, machine failure/repair or new analytics are claimed; none of those exist in the engine.
- The Governance/Storage evidence workshop, reserve (availability) scenarios, the remaining contracts, audio and artwork polish are later work.
- The HTTP adapter has no authentication and no published OpenAPI description (see the integration design).

## Developing

Java tests run with the normal Java gate (`./arcogine check`, or `cd product && ./gradlew :last-light-foundry:test`). The browser client is plain ES modules under `src/main/resources/web` with no build step; its tests live in `web-tests/` and are described in the [testing guide](../../../docs/development/testing.md#12-game-browser-tests).

Mission content (Challenge definition, equipment catalogue and the game wrapper that maps equipment to Factory resources) is data under `src/main/resources/missions/<id>/` and is validated at server start by the real Challenge content loader.

All artwork is original vector drawing authored for this project (the glyph paths in `floor.js` and the layout in `styles.css`); there are no bundled third-party assets, fonts or runtime CDN references.
