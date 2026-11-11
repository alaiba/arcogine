# Session-close Kaizen

When the user's entire message is `.?`, inspect the current session and live repository for anything
that should survive deletion of the conversation, following the classification and capture-preference
rules below.
Prefer improving an existing authoritative artifact over creating a new one, and never replace known
repository context with generic GitHub discovery.

Finish every review with an explicit deletion verdict: either the session is safe to delete because
nothing unique remains, or name exactly what still needs to be captured first. Default output reports
material findings/actions plus that verdict; do not narrate adjacent practices — a Consistency review
or a delivery-process retrospective — that the session gives no concrete reason to recommend.

## Classification and capture

- **Trigger:** `.?` at the close of a meaningful coding-agent session; see `AGENTS.md`.
- **Purpose:** preserve durable lessons as executable safeguards, standard work, or maintained knowledge.
- **Owner:** the coding agent.
- **Cadence:** event-driven only; no recurring due state.

At `.?`, inspect the current session and live repository for anything learned, decided, repeated, or
encountered that should survive deletion of the conversation. Classify each material candidate as one
of:

- **Already encoded** — the repository already captures the lesson or invariant adequately; make no
  duplicate change.
- **Bake in** — the lesson is durable and generally reusable; identify the narrowest authoritative
  repository surface that should encode it.
- **Follow-up** — the improvement is worthwhile but belongs in separate work rather than being
  smuggled into the current PR or slice.
- **Discard** — the observation is situational, transient, or otherwise not worth preserving.

Prefer the strongest durable capture that actually fits the lesson. Use an executable guard/test only
when the invariant is mechanically observable and the guard exercises behavior or repository state
rather than merely asserting that instruction prose still contains particular wording. Otherwise
prefer canonical helper/tooling, agent/contributor standard work, maintained documentation, then
canonical architecture or specification for genuinely architectural or hard-to-reverse constraints.
Generalize incidents into semantic rules rather than preserving session or PR coordinates as durable
concepts. Prefer improving an existing authoritative artifact over creating a new one.

Do not manufacture a lesson merely to produce an output. Default output reports material findings and
actions plus the deletion verdict; it does not narrate adjacent practices that the session gives no
concrete reason to invoke — for example "Consistency review not warranted" or "retrospective not
assessed" are not useful by default (see [Invocation boundaries](../continuous-improvement.md#invocation-boundaries)). Finish
every review with an explicit deletion verdict: either the session is safe to delete because nothing
unique remains, or name exactly what still needs to be captured first.
