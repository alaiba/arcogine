# Historical evidence

This area stores dated, non-normative evidence that is useful for understanding how Arcogine changed but does not define current repository behavior.

Current product, architecture, planning, research, and development rules live in their canonical maintained locations under `docs/`. Historical evidence must not be read as current authority merely because it remains useful for later comparison.

## Continuous improvement

Delivery-process retrospective records live under `continuous-improvement/`. The normative retrospective method lives in `docs/development/continuous-improvement.md`; the dated records preserve what was measured and concluded at one point in time.

## Decision rationale

Historical decision-rationale records live under `decisions/`, one dated file per retained decision named `YYYY-MM-DD-<semantic-slug>.md`; semantic filenames and the directory listing are the only index. The retention test and record shape live in [`docs/development/researching.md`](../development/researching.md#historical-decision-rationale). Each record preserves why a significant reconciled choice was made at one point in time — the serious alternatives, the decisive trade-offs, and the conditions that would justify revisiting it — and names the canonical documents it was reconciled into.

A record is not architecture and carries no status, approval, or numbering. It cannot introduce, extend, override, or repair a current requirement: whatever still governs Arcogine lives in the owning architecture, specification, or executable contract, which must remain understandable without reading any record. Editing a record changes no current meaning, and a record that differs from current architecture is history rather than drift. Most decisions have no record; its absence says nothing about a decision's importance.
