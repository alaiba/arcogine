# Security Ownership and Readiness Boundaries

> **Status:** ACTIVE
> **Scope:** Whether material security responsibilities remain ownerless, ambiguously owned, or insufficiently represented in Arcogine's architecture/readiness model, and what the smallest durable ownership structure carrying them would be
> **Research baseline:** live `main` `5d010e2aa79e36ca6a9793ebf305ba309e3da5a7` (resolved at start and re-verified unchanged at synthesis)
> **Authority:** Research only. This document is evidence, not accepted architecture, product direction, security policy, or implementation commitment. Nothing here becomes durable until a separate reconciliation change promotes it through its own independent review.
> **Risk classification:** **High** — security, authority, trust boundaries, and potential cross-domain ownership (`docs/development/researching.md` §7)
> **Adversarial-review status:** **Required, not yet performed.** The self-challenge below is the author's own and must not be read with the weight of an independent pass.

## Question

> After accounting for Arcogine's existing current-security policy and controls, Operational/Digital-Twin security semantics, Governance responsibilities, domain ownership, and development/CI security controls, are there material security responsibilities that remain ownerless, ambiguously owned, or insufficiently represented in Arcogine's architecture/readiness model? If so, what is the smallest durable ownership structure that should carry them?

## Decision at stake

Whether Arcogine should (A) keep security distributed across existing owners; (B) introduce a cross-cutting Security Assurance responsibility/readiness model; (C) establish a standalone Security architecture/delivery track; or (D) adopt some materially different structure.

The answer changes whether a new architectural owner, readiness track, planning surface, or research-register area is created — and, if not, what smaller corrections the evidence actually requires.

## Scope and non-goals

In scope: ownership, boundaries, readiness thresholds, and evidence. Explicitly **not** in scope: implementing any control; creating a Security module, track, or delivery coordinate; editing Accepted ADRs; settling Proposed ADRs; moving anything into `docs/planning/`; prescribing an IdP, certificate technology, policy engine, secrets manager, scanner, or deployment platform; assuming hosted SaaS, multi-tenancy, or live industrial control exists today; or rating Arcogine against a maturity checklist.

This investigation also does not re-open Operational trust/authority/command-safety semantics or Governance provenance/evidence semantics. It tests whether those boundaries hold; it does not redraw them.

---

## Executive conclusion

**Arcogine does not need a standalone Security architecture/delivery track. It also does not need a new cross-cutting Security Assurance *owner*. Candidate A — distributed ownership — survives, with four bounded corrections, none of which creates a new owner, module, track, or planning surface.**

Confidence:

- **Rejecting Candidate C (standalone Security track): high.** Every security responsibility Arcogine can actually reach today already has a named, contract-bearing owner, and the two proving cases that occurred live at this baseline were detected, remediated, and verified end-to-end by those owners. A Security track would duplicate Operational, Governance, interface, infrastructure, and repository-maintenance authority while owning nothing exclusively.
- **Rejecting Candidate B as a new owner: moderate-high.** One genuinely B-shaped obligation exists — *security readiness criteria by exposure/consequence class* — but it is a single missing paragraph inside an existing authority (`.github/SECURITY.md`), not a function that needs its own surface. Creating a cross-cutting assurance owner to carry one obligation is a larger abstraction than the evidence supports.
- **The surviving gaps themselves: high** for the two current, verifiable ones; **moderate** for the one triggered ownership ambiguity.

The most important negative finding is that **the widely assumed "Arcogine has no security owner" framing is false at this baseline.** Security responsibilities are distributed, but they are distributed to *named, contract-bearing roles with executable enforcement* — not left implicit.

The genuinely material findings are narrower than the question anticipated:

1. **A public repository's documented vulnerability-disclosure path is a public GitHub issue, and GitHub's private reporting channel is disabled.** Current, verifiable, one-setting remedy. *(Gap type: implementation gap in an owned responsibility.)*
2. **The one network-exposure control `.github/SECURITY.md` tells a deploying user to rely on — `CORS_ALLOWED_ORIGIN` — has no executable regression test, and `docs/development/testing.md` incorrectly claims it does.** Current, verifiable. *(Gap type: assurance gap plus a false evidence claim.)*
3. **`.github/SECURITY.md`'s "Hardening for Network Deployment" is a runbook with no threshold** — it says *how* to harden, never *at what point* a control stops being advisory and becomes required. *(Gap type: assurance gap — the single B-shaped obligation.)*
4. **Security of a hosted/multi-user Arcogine that performs no physical actuation has no current owner.** Operational's contract is scoped to *consequence*; a non-consequential hosted service falls outside it, and outside Governance, Factory, and Engine too. *(Gap type: ownership ambiguity, triggered — not current.)*

Finding 4 is the only genuine ownerless gap the coverage matrix produces, and it is correctly deferred: no hosted consumer exists. It warrants one bounded research-register entry, proposed below and **not** added by this document.

---

## Repository evidence

All statements in this section are `Repository fact` at baseline `5d010e2` unless marked otherwise.

### The existing security ownership map

| Responsibility | Owner (authority) | Status |
|---|---|---|
| Consequence-specific verified trust, external actor/peer/target authenticity, consequential authority, external command/deployment safety, observation authenticity/provenance, reconciliation, fail-safe/recovery | `docs/architecture/operational-execution-digital-twin.md` §5, §6, §16 | **Proposed** architecture; nothing implemented |
| Controlled semantic identity/history, change attribution, requirements/assertions, conformance/findings, evidence use, governed change, exceptions | `docs/architecture/governance-conformance.md` | Proposed architecture; identity/history, ChangeSet, requirements, and initial conformance slices implemented |
| Production semantics and deterministic simulation truth | `docs/architecture/factory-design.md`, Engine readiness, ADR-0005/0009/0010/0015 | Accepted and implemented |
| API attack surface: body-size limit, CORS configuration, SSE connection limit, bind address | `product/interfaces/api`, `product/interfaces/cli` | Implemented |
| Untrusted scenario/config parsing and validation | `product/simulation/.../ScenarioLoader.java`, `product/types/.../scenario/` | Implemented |
| Dependency vulnerability detection | `.github/workflows/ci.yml` (`java-audit`, `frontend`), daily `schedule` | Implemented and blocking |
| Dependency vulnerability remediation, triage, urgency, exception discipline | `.github/agents/dependency-maintainer.agent.md` | Implemented as standard work |
| Container/base-image vulnerability scanning | `ci.yml` `docker` job, Trivy image scan | Implemented and blocking |
| Secret scanning | `ci.yml` `security-secrets` (runs unconditionally, all triggers) | Implemented and blocking |
| CI/Actions authority and trust boundary | `.github/workflows/pr-disposition.yml`, `.github/CODEOWNERS`, `docs/development/reviewing.md` | Implemented, with a repository-owned activation checklist |
| Security-relevant review findings and severity calibration | `.github/agents/pr-reviewer.agent.md`, `docs/development/reviewing.md` (`P0` = catastrophic security) | Implemented |
| Control drift detection over CI/tooling and operational security/authority concerns | `.github/agents/consistency.agent.md` | Implemented as standard work |
| Cross-cutting security policy, posture, limitations, hardening guidance, mature-product principles, disclosure | `.github/SECURITY.md` | Implemented; carries five distinct authority roles (see below) |

**Critical status precision.** The Operational security semantics that carry the largest share of Arcogine's future security responsibility are **Proposed, not Accepted**. `docs/architecture/operational-execution-digital-twin.md` is a "Proposed architectural reference"; ADR-0013 (execution-context identity) is **Proposed** and the architecture explicitly states it "must not be accepted or implemented in its current kind-bound form"; ADR-0016 (governance evidence provenance) is **Proposed**. `docs/planning/operational-execution-digital-twin-readiness.md` is **NOT ADMITTED**.

This matters for the decision: any new security structure built now would rest substantially on unsettled architecture. That is an argument for *less* new structure, not more.

### Executable security controls that actually exist

`Repository fact`, verified by reading the files:

- **Least-privilege CI tokens.** `ci.yml` sets `permissions: contents: read` at workflow level, restates it per job, and gives the aggregate `gate` job `permissions: {}`.
- **Every third-party action pinned to a full 40-hex commit SHA** with a version comment (`actions/checkout@3d3c42e5...` etc.).
- **Scanner binaries pinned by version *and* SHA-256**, verified with `sha256sum -c` before install (`TRIVY_SHA256`, `GITLEAKS_SHA256`); `actionlint` likewise in `.github/scripts/check-actions-workflows.sh`, which deliberately refuses a `PATH`-provided binary.
- **Daily scheduled security re-scan** (`cron: '0 5 * * *'`) that ignores path classification, so newly published CVEs surface without a repository change.
- **Fail-closed CI aggregation.** The `gate` job fails if its `needs:` list and its `expected_to_run` map drift apart, and fails on any job that skipped despite its surface being selected.
- **A CVE exception ledger with justifications.** `.trivyignore` suppresses only non-shipped findings, each annotated with why it is not on the runtime classpath; `docs/development/testing.md` records that shipped-runtime CVEs are remediated by version override rather than suppressed.
- **SBOM generation** (CycloneDX, ~179 components) scanned by `trivy sbom` as a blocking gate.
- **Executable security regression tests** in the ordinary `interfaces/api` suite: `oversizedBodyReturnsPayloadTooLarge`, `bodyUnderLimitIsAccepted`, `sseConnectionLimitReturns503`, `invalidTomlContentReturnsBadRequest`, plus CLI bind-address defaults (`127.0.0.1:3000`) and explicit-override behavior in `ArcogineCommandTest`.
- **Web image security headers**, including a restrictive `Content-Security-Policy`, in `infra/docker/web.Dockerfile`.

### The repository control plane, verified against live GitHub configuration

`Repository fact`, read from the GitHub API at synthesis time (this is configuration **not** encoded in git):

The `Protect main` ruleset (id `14822987`) is `enforcement: active` on `~DEFAULT_BRANCH` with:

- `deletion`, `non_fast_forward`, and `required_linear_history` rules;
- `pull_request` with `require_code_owner_review: true`, `require_extra_approval_for_unattributed_changes: true`, `required_approving_review_count: 0`;
- `required_status_checks` with `strict_required_status_checks_policy: true` and required contexts `gate` and `disposition`;
- `current_user_can_bypass: "never"`.

Cross-referenced against `docs/development/reviewing.md`'s own four-item activation checklist: items **1** (`disposition` required), **3** (base freshness via strict policy), and **4** (Code Owner review required) are **confirmed active**. Item **2** — that the identity available to coding agents cannot bypass required checks and cannot satisfy its own `.github/CODEOWNERS` review requirement — **could not be confirmed**, and there is contrary indication: `.github/CODEOWNERS` names `@alaiba`, and every review on the open pull request examined below is authored by `alaiba` with `author_association: OWNER`. Whether the agent identity and the owner identity are genuinely separable is not determinable from the surfaces available here. `docs/development/reviewing.md` already names this exact dependency ("Both mitigations depend on the *reviewing* owner identity being genuinely independent of whoever could otherwise self-approve the change"), so this is a known, repository-acknowledged condition rather than a discovered gap.

Also `Repository fact` from the API:

- Repository is **public**, Apache-2.0, `allow_forking: true`, issues enabled.
- **Private vulnerability reporting: `{"enabled": false}`** (HTTP 200 — a feature-state response, not an authorization failure).
- **Dependabot alerts: disabled** (HTTP 403 with the documented feature-state message "Dependabot alerts are disabled for this repository", distinct from the "Resource not accessible by integration" scope errors returned for other endpoints). `.github/dependabot.yml` configures dependency *version* updates, which is a separate feature.
- **Zero releases and zero tags.**

Surfaces that **could not be inspected** and are recorded rather than inferred: Actions repository permission settings, environment protection rules, secret-scanning configuration, code-scanning configuration, and legacy branch-protection detail — all returned proxy or scope errors.

### Vocabulary sweep

A repository-wide search (`git grep -il`, all tracked files) over the security vocabulary the brief specified, plus semantic neighbors, returned:

| Term | Files | Term | Files |
|---|---:|---|---:|
| `provenance` | 90 | `threat` | **1** |
| `external` | 88 | `supply chain` | **0** |
| `authority` | 71 | `TLS` | **1** |
| `workflow` | 47 | `least privilege` | 3 |
| `trust` | 33 | `vulnerability` | 3 |
| `deployment` | 31 | `authentication` | 4 |
| `audit` | 29 | `revocation` | 4 |
| `security` / `authorization` | 28 | `CORS` | 5 |

The distribution is itself evidence. Arcogine's security-adjacent vocabulary is overwhelmingly *semantic* (provenance, authority, trust, external) and concentrated in Operational and Governance architecture. The *assurance* vocabulary (threat, supply chain, TLS, least privilege) is nearly absent, and where it appears it appears almost exclusively in `.github/SECURITY.md`.

Specifically: **`threat` appears in exactly one file** — `.github/SECURITY.md` — and only as the phrase "threat assumptions must be explicit and testable," a requirement placed on a *future* production-consequential path. **No threat-model artifact exists anywhere in the repository.** `supply chain` appears nowhere; artifact signing appears only as a future *Governance revision-record integrity* possibility in ADR-0008 and `governance-conformance.md`, never as release-artifact provenance.

`docs/architecture/standards-alignment.md` — the repository's authority on which external standard families Arcogine aligns to and at what level — covers ISA-95/IEC 62264, B2MML, AutomationML/IEC 62714, Asset Administration Shell, OPC UA/IEC 62541, and ISO 9001. It contains **no entry for any security or OT-security standard family**; security/control frameworks appear once, as a "potential Governance projection."

### The `.github/SECURITY.md` authority question

`Repository fact`: the single file carries five distinct authority roles simultaneously —

1. **current-state posture and known limitations** (a current-state claim);
2. **a deployment hardening runbook** (operator guidance for a user of the software);
3. **security scan/gate ownership** (development/assurance policy, partly duplicating `docs/development/testing.md`);
4. **mature-product security principles** (normative, Charter-derived);
5. **vulnerability reporting policy** (the actual security policy).

`Inference`: the Product Charter §10 defines exactly the categories that are being mixed here (Normative / Current state / Research / Planned / Proposed / Historical) and states "A reader should never be left guessing whether a statement is mature product ambition, current implementation, an open research question, admitted implementation work, an open proposal, or a historical artifact." `.github/SECURITY.md` **does** label its own split ("This document has two parts...") and repeatedly disclaims that planned principles are implemented. So the document is honest and self-aware; the finding is **mild** — role overloading, not misleading content. It does not on its own justify a new authority surface. What is genuinely missing from it is not separation but a *threshold* (see the gap matrix).

### Open pull request as proving-case evidence

`Repository fact`, re-resolved at synthesis (the handoff description of this pull request is **stale**):

Pull request [#294](https://github.com/alaiba/arcogine/pull/294) is open, non-draft, `mergeable_state: clean`, base level with live `main`, head `45800079cff8ac8cd6345304168000c05daa32c6`. It contains two independent security threads:

- **A dependency CVE thread.** CI's Java dependency audit failed on `io.netty:netty-handler` CVE-2026-75595 (CRITICAL) — a pre-existing finding that also failed `main`'s own daily scheduled scan, not caused by the pull request's diff. It was remediated in-pull-request by bumping the already-existing pinned Netty override `4.2.16.Final` → `4.2.17.Final`, the same mechanism used for a prior Netty batch. Netty is test-scope only there (`spring-boot-starter-webflux` for `WebTestClient`), never shipped. Current-head CI is green.
- **A GitHub Actions write-authority thread.** Independent review raised a `SECURITY_AUTHORITY` finding that the new write-capable workflow's `workflow_dispatch` trigger let a manual run select an arbitrary branch, whose *own copy of the workflow definition* would then execute with `issues: write`. **The handoff described this as still blocking; it is not.** Across five review rounds the implementation attempted a checkout pin, a `@main`-pinned reusable-workflow split, and a GitHub Environment gate — each of which review correctly rejected, because in every case the branch-controlled caller could simply decline to use the protection. It was finally closed by replacing `workflow_dispatch` with `repository_dispatch`, which moves the boundary to the platform's own event model. The latest review disposition on the current head is **READY TO MERGE**.

`Inference`: this is the single most decision-relevant piece of internal evidence in the investigation, and it points **against** new structure. A genuine, subtle, platform-level trust-boundary defect in write-capable repository automation was detected, resisted three plausible-but-insufficient fixes, and was closed at the correct boundary — entirely inside the existing PR Reviewer contract, with no security specialist, no security track, and no security-specific review procedure. It also demonstrates the existing model's *failure* mode is the right one: review refused to accept a fix that documented the exposure accurately but did not close it.

---

## Security-context / trust-boundary map

Deliberately not collapsed into one "production security" model.

### 1. Current local-first single-user simulation

- **Protected assets:** the developer's own machine and local scenario files. Nothing multi-party.
- **Trust boundary:** process boundary only. The API binds `127.0.0.1:3000` by default.
- **Threat sources:** essentially only a malicious local scenario file or a hostile dependency.
- **Owner:** interface modules (`interfaces/api`, `interfaces/cli`) plus Engine for scenario validation.
- **Controls:** loopback default bind (tested), 1 MiB request-body cap (tested), 64-connection SSE cap (tested), structural scenario validation with economy value bounds (tested).
- **Known limitations:** documented honestly in `.github/SECURITY.md` — no authentication, permissive CORS, unencrypted state.
- **What changes when more consequential:** essentially everything; this context's controls are calibrated for a single trusted user and say so.

### 2. Current simulation deliberately exposed over a network

- **Protected assets:** simulation state and control (anyone reaching the port can load scenarios, mutate price, toggle agents, and read everything); the host's CPU/memory.
- **Trust boundary:** becomes the network. There is **no authentication** — reachability is authorization.
- **Threat sources:** any network-reachable party.
- **Owner:** `.github/SECURITY.md` for guidance; `interfaces/api` for mechanism. **No owner for the threshold question.**
- **Controls:** the hardening runbook (bind address, `CORS_ALLOWED_ORIGIN`, reverse-proxy TLS, dependency audit, log verbosity); body/SSE caps.
- **Known limitations:** stated. But `.github/SECURITY.md` never says *at what point* authentication becomes required rather than optional — and the CORS control it recommends has no regression test.
- **What changes when more consequential:** this is the first context where a control's absence has a third-party victim.

### 3. Repository / development / CI / dependency / release control plane

- **Protected assets:** `main`'s integrity, the merge gate's integrity, `GITHUB_TOKEN` authority, the dependency graph, and (prospectively) any published artifact.
- **Trust boundary:** between *reviewed default-branch content* and *branch-controlled content*, which on GitHub is event-dependent, not intuitive.
- **Threat sources:** a malicious or mistaken pull request (repository is public and forkable); a compromised third-party action; a compromised upstream dependency; an agent operating with repository authority.
- **Owner:** `docs/development/reviewing.md` + PR Reviewer + `.github/CODEOWNERS` + Dependency Maintainer + Consistency, with the `Protect main` ruleset as enforcement.
- **Controls:** the full list under "Executable security controls" and "The repository control plane" above.
- **Known limitations:** explicitly enumerated in-repository — the listener-coverage narrowing gap and the check-name-provenance gap are both *documented in the workflow file and in `reviewing.md`*, with CODEOWNERS named as the mitigation and the scheduled sweep as defense in depth. Activation item 2 (reviewer-identity independence) is unverified. Dependabot alerts are off. Private vulnerability reporting is off.
- **What changes when more consequential:** publishing a release artifact adds provenance/SBOM obligations that do not exist today.

This is by a wide margin Arcogine's **most mature** security context — which is the correct prioritization for a project whose product surface is a local simulator but whose repository is public and agent-operated.

### 4. Future hosted / multi-user Arcogine with no physical actuation

- **Protected assets:** other users' models, scenarios, and results; service availability; tenant separation.
- **Trust boundary:** between tenants, and between authenticated users and the service.
- **Threat sources:** other tenants, unauthenticated internet, credential theft.
- **Owner:** **none.** Operational's contract is explicitly scoped to *external consequence*; a hosted simulator has none. Governance owns governance of the *modeled business*, not of Arcogine-as-a-service. Factory/Engine own production semantics. Interface modules own an API surface, not an identity model.
- **Controls:** none, correctly — nothing hosted exists.
- **What changes:** this is the one context where the *ownership question itself* is open rather than the implementation.

### 5. Future external integrations ingesting untrusted or independently authoritative data

- **Protected assets:** the integrity of Arcogine's reconciled interpretation of reality.
- **Trust boundary:** between an external source's claim and Arcogine's acceptance of it as evidence.
- **Owner:** **Operational**, unambiguously. `operational-execution-digital-twin.md` §8 and §16 already require independent observation provenance, explicit authoritative subject correspondence ("names, endpoints, connector configuration, namespace placement, or coincident identifiers must never establish this correspondence implicitly"), and that "an unverifiable actor/source/target is not silently treated as trusted."
- **Status:** correctly deferred; the semantics are Proposed and the research is open.

### 6. Future production-consequential Operational execution

- **Protected assets:** physical equipment, people, money, and orders — consequences that cannot be undone by resetting simulation state.
- **Owner:** **Operational**, unambiguously, per §5, §6, and the ten safety principles in §16.
- **Status:** correctly deferred and explicitly **NOT ADMITTED** for implementation.

---

## Candidate models

### Candidate A — distributed ownership only

Security remains a cross-cutting quality attribute. Existing domain owners own security-relevant semantics; `.github/SECURITY.md`, development standards, CI, dependency maintenance, and review/test infrastructure provide assurance. No standalone Security architecture or readiness track exists.

### Candidate B — cross-cutting Security Assurance responsibility

Domain semantics stay put. A dedicated cross-cutting assurance surface owns threat-model discipline, security readiness criteria by exposure/consequence class, AppSec/verification strategy, supply-chain and release assurance, vulnerability lifecycle, and recurring security-control evidence review — while owning no actor/authority/trust/domain semantics.

### Candidate C — standalone Security architecture/delivery track

Security receives its own architectural and delivery responsibility, potentially including shared identity/authentication/authorization/trust infrastructure and cross-domain security contracts.

### Candidate D — "A-plus": distributed ownership with named corrections inside existing authorities

Identical to A in structure, but explicitly records that three narrow obligations currently fall between owners and assigns each to an **existing** authority rather than a new one: the disclosure path and the exposure-class readiness threshold to `.github/SECURITY.md`; the security-control evidence claim to `docs/development/testing.md`; and the non-consequential multi-user ownership question to the research register.

D is materially distinct from A because A as literally stated ("`.github/SECURITY.md` and normal engineering controls provide the cross-cutting policy surface") asserts current sufficiency, and the evidence shows three specific respects in which that is not true. D is materially distinct from B because it creates **no new owner** — it repairs existing ones.

---

## External evidence

Only load-bearing material is listed. Sources deliberately **not** pursued because they could not discriminate between the candidates: OWASP ASVS and SAMM (they specify verification requirements and maturity streams, not ownership allocation — Arcogine's open question is who owns, not which requirements), and NIST CSF (organization-level risk governance, far above this question's altitude).

### GitHub Actions trust semantics — *verified in session*

**Source:** GitHub Docs, "Secure use reference" (`docs.github.com/en/actions/reference/security/secure-use`), fetched during this investigation.

Establishes three things directly:

- `pull_request_target` and `workflow_run` are **privileged** triggers that "may have repository write access and access to referenced secrets," and "Workflows that use these triggers **must not explicitly check out untrusted code**."
- Least privilege: "It's good security practice to set the default permission for the `GITHUB_TOKEN` to read access only for repository contents. The permissions can then be increased, as required, for individual jobs."
- "Pinning an action to a full-length commit SHA is currently the only way to use an action as an immutable release."

**What transfers:** Arcogine's CI already implements all three of GitHub's own headline hardening controls — `pr-disposition.yml` never checks out pull-request code (it checks out `ref: main` and re-derives everything via the API), `ci.yml` defaults to `contents: read` and escalates per job, and every action is SHA-pinned. This is direct evidence that the *existing* owners are producing correct outcomes on the highest-risk surface.

**What does not transfer:** GitHub's guidance addresses workflow authoring; it says nothing about whether a project needs a security owner, and cannot be read as establishing one.

**Verification note.** A separate fetch of GitHub's "Events that trigger workflows" reference returned a summary that stated the workflow *definition* is sourced from the default branch for `pull_request` and `pull_request_review` as well. That is **inconsistent** with the secure-use page and with the entire reason `pull_request_target` exists, and it is not treated as evidence here. The parts corroborated independently — that `repository_dispatch`, `schedule`, `workflow_run`, and `pull_request_target` bind `GITHUB_REF` to the default/base branch, while `workflow_dispatch` binds it to the *dispatched* branch or tag — are consistent with the secure-use page and with the pull-request review analysis cited above, and are used only at that strength. This discrepancy is itself a small illustration of why `docs/development/researching.md` §5 requires precise provenance rather than a recalled summary.

### GitHub private vulnerability reporting — *verified in session*

**Source:** GitHub Docs, "Privately reporting a security vulnerability," fetched during this investigation.

Establishes: private vulnerability reporting is available for public repositories, is **not enabled by default**, requires explicit maintainer configuration, and "keeps vulnerability details confidential during the disclosure process," aligning with coordinated disclosure — as opposed to a public issue, which discloses on submission.

**What transfers:** directly and completely. Arcogine's repository is public, the feature is off, and `.github/SECURITY.md` directs reporters to open a public issue. The gap and its remedy are both concrete.

**What does not transfer:** nothing material; this is a platform-feature fact, not an analogy.

### NIST SP 800-218 (SSDF) — *partially verified in session*

**Provenance:** NIST Special Publication 800-218, *Secure Software Development Framework (SSDF) Version 1.1: Recommendations for Mitigating the Risk of Software Vulnerabilities*, February 2022 — designation, title, version, and date **verified** against the official NIST CSRC publication page.

**What it establishes (verified):** the SSDF is "a core set of high-level secure software development practices that can be **integrated into each SDLC implementation**" — outcome-oriented practices folded into an existing development lifecycle, not a prescribed organizational structure, team, or architectural domain.

**What transfers:** this is the strongest external discriminator between Candidates B and C. The most widely adopted secure-development framework locates secure-development responsibility in *practices integrated into existing lifecycle roles*, not in a security-owned architectural domain. That supports keeping security semantics with their domains, and supports assurance being a *property of how existing owners work* rather than a new owner.

**What does not transfer:** the SSDF establishes *possibility and structure*, not Arcogine necessity. It is written for organizations producing software for external consumers; Arcogine currently publishes no release. It also says nothing about how a single-maintainer public repository should allocate ownership.

**Unverified background — do not treat as load-bearing.** The four SSDF practice-group abbreviations and names are known from background knowledge, but an attempt to verify them against the official PDF during this investigation produced a corrupted extraction (duplicate abbreviations, evidently invented expansions). They are therefore **not cited** and no argument here depends on them. Anyone extending this analysis must verify them from the source before use.

### SLSA — *verified in session*

**Provenance:** SLSA specification, Build levels page for **v1.1** (`slsa.dev/spec/v1.1/levels`), fetched during this investigation; the page itself indicates **v1.2 is the currently active version**, so re-check against v1.2 before any load-bearing use.

**What it establishes:** SLSA Build L1 requires a *package* to have provenance describing how it was built; L2 adds signed provenance from a hosted build platform; L3 adds a hardened, isolated platform. Provenance attaches to **published/distributed artifacts**, not to source code generally.

**What transfers:** it settles a *trigger*, and settles it negatively for now. Arcogine has **zero releases and zero tags**. There is no published artifact for provenance to attach to, so SLSA imposes no present obligation. It also tells us precisely what the trigger is: the first published release or distributed container image.

**What does not transfer:** SLSA levels are a supply-chain maturity ladder for artifact consumers. Nothing in it implies a project without consumers should pre-build the ladder, and treating an L-number as a goal in itself would be exactly the compliance theater the brief excludes.

### IEC 62443-3-2 — *provenance verified; content from secondary official sources*

**Provenance:** IEC 62443-3-2:2020, *Security for industrial automation and control systems — Part 3-2: Security risk assessment for system design* (also published as ANSI/ISA-62443-3-2-2020). Designation, part, edition year, and title verified via the IEC webstore listing and ISA's product listing.

**What it establishes:** the standard requires partitioning an industrial automation and control system (the "system under consideration") into **zones and conduits** based on risk, then performing detailed risk assessment per zone/conduit and assigning **Security Level targets (SL-T)** on the basis of threat and risk.

**What transfers:** the structural principle that operational-technology security assurance is **consequence-scoped and partition-scoped** — security requirements are derived per zone from that zone's risk, not from a single global security authority owning all trust semantics. That corroborates Arcogine's existing boundary: consequence-specific trust semantics belong with the owner of the consequential behavior (Operational), while the *method* (risk assessment discipline) is a cross-cutting practice. It also corroborates `operational-execution-digital-twin.md` §2's refusal of a global execution-kind taxonomy: 62443 likewise partitions by risk rather than labeling a whole system.

**What does not transfer — and this matters.** Arcogine has no industrial automation and control system, no zones, no conduits, no system under consideration, and no deployed asset. 62443 establishes *how the problem is structured* when it exists; it establishes **no Arcogine requirement today**, and citing it as one would be exactly the "framework analogy treated as an Arcogine requirement" failure mode this investigation is required to avoid. Its content beyond the title/scope was read from official secondary listings rather than the paywalled normative text; any future normative dependency must cite the exact clause from the standard itself, per `docs/architecture/standards-alignment.md`.

---

## Proving cases

Derived from the candidate models. "Survives" means the candidate places the case coherently without inventing an owner or duplicating one.

### 1. A new critical dependency CVE appears with no source-code change

**What actually happens** (`Repository fact`, observed live at this baseline): the daily 05:00 UTC scheduled CI run ignores path classification and re-runs `java-audit`, `frontend` npm audit, Trivy image scans, and the secret scan. Netty CVE-2026-75595 (CRITICAL) surfaced exactly this way and failed the gate on `main` itself. Dependency Maintainer owns remediation and is explicitly forbidden from allowlisting without evidence-backed reason. Remediation went in as a version override; `.trivyignore` records only non-shipped exceptions with inline justification; PR Reviewer verified closure against green current-head CI.

**A survives** — completely, with demonstrated evidence. **B survives** but adds nothing: every step already has an owner. **C fails** — it would take remediation ownership away from the maintainer who understands the dependency graph.

**Residual:** Dependabot alerts are disabled, so GitHub-native advisory alerting does not exist; detection depends entirely on Arcogine's own scanners. Given the daily sweep across SBOM, npm, and images, this is defensible and arguably deliberate — but it is a single point of detection failure and is not recorded anywhere as a decision.

### 2. A repository workflow obtains write authority

**What actually happens** (`Repository fact`): analyzed in detail above. Detected by PR Reviewer as a `SECURITY_AUTHORITY` finding, survived three insufficient remediations, closed by moving to `repository_dispatch`. The pre-existing `pr-disposition.yml` demonstrates the same discipline independently: it documents its trust boundary in the workflow file itself, uses `pull_request_target`/`workflow_run`/`schedule` precisely because they are base-sourced, never checks out pull-request code, and *documents its own residual gaps* (listener-coverage narrowing, check-name provenance) with CODEOWNERS as the named mitigation.

**A survives, strongly.** The distinction between reviewed code and branch-controlled execution is owned by `docs/development/reviewing.md` and enforced by an active ruleset. **B survives** but is redundant here. **C fails** — a Security track would have to own the pull-request review contract itself, which is already a mature, contract-bearing role.

`Inference`: this case is the strongest single argument against new structure, precisely because it is the case a new Security owner would most plausibly claim.

### 3. The existing simulation API is deliberately exposed to a network

**What actually happens:** `.github/SECURITY.md` provides five hardening steps. `interfaces/api` provides body-size and SSE caps; `interfaces/cli` provides the loopback default. All are tested — **except CORS**, which has no test anywhere in `product/`, contrary to `docs/development/testing.md`'s explicit claim.

**Nothing answers "at what point does authentication become required?"** The hardening list is presented as advisory throughout ("If you expose the current simulation service beyond localhost, apply at least..."), and the mature-product principles section is explicitly scoped to production-consequential execution — leaving deliberate network exposure of a non-consequential simulator in a gap between the two.

**A partially fails.** Guidance exists and mechanism exists, but the *threshold* has no owner and one recommended control has no evidence. **B survives** — "security readiness criteria by exposure class" is exactly B's remit. **C survives** but is disproportionate. **D survives** — the threshold is one paragraph in an authority that already exists.

`Inference`: this is the only proving case where B does real work that A does not. It is also the case that shows B's work is *small*.

### 4. Arcogine becomes a hosted multi-user service, still with no physical actuation

**What actually happens:** nothing places this. Operational's own architecture says its domain is where "independently existing systems, external observations, trusted correspondences, command/result facts, deployment application, and modeled-versus-observed reconciliation enter the picture" — a hosted simulator has none of those. Its §5 requirements (verified identity, trust roots, credential lifecycle, physical safety, fail-safe) are all framed as what "real external consequence adds." Governance owns governance *of the modeled business*, explicitly disclaiming that Arcogine implements SOC 2 or ISO 27001 itself. Factory/Engine own production semantics. Interfaces own an API surface.

So: multi-tenant identity, per-user authority over models and scenarios, tenant data isolation, and abuse/rate control have **no owner**.

**A fails.** **B partially survives** — B owns readiness criteria and verification, but B is explicitly forbidden from owning actor/authority/trust semantics, so B cannot own tenant authority either; it would identify the gap without being able to fill it. **C survives** — this is C's strongest case. **D survives** by naming it as an open research question rather than pre-assigning it.

`Inference`: this is the single case that could justify new structure, and it is **entirely hypothetical at this baseline** — no hosted consumer exists, and the Charter explicitly lists "its current single-user, local-first deployment model" as an implementation choice rather than product identity, without committing to hosting. Creating an owner now for a consumer that does not exist would be premature abstraction of exactly the kind `operational-execution-digital-twin.md` §17 warns against. Note also that the generic half of this question — actor, capability, delegation, authority — is *already* open research in the Agency and Decision Boundary investigation, which explicitly refuses to force actor/capability semantics into Operational merely because Operational has the first concrete consumer. What that research does **not** cover is the deployment-security half: tenant isolation, session/credential handling, and abuse resistance for a hosted service.

### 5. Arcogine accepts an untrusted scenario/config/model artifact

**What actually happens:** `ScenarioLoader.loadScenario` parses TOML via Jackson, then runs structural validation — non-zero `max_ticks`, at least one equipment and one material entry, duplicate-ID rejection, non-zero concurrency, reference integrity, and a `MAX_ECON_VALUE` bound of 1,000,000. The HTTP path (`POST /api/scenario`) accepts TOML in a JSON body and is capped at 1 MiB. `invalidTomlContentReturnsBadRequest` and the economy-bound `OutOfRange` cases are tested.

Resource exhaustion is only partly bounded: `max_ticks` is validated as `> 0` with no upper bound, and equipment/material/operation counts are unbounded within the 1 MiB body cap. `loadScenarioFile(String path)` reads an arbitrary filesystem path, but is reachable only from the CLI — the HTTP surface takes content, never a path, so there is no path-traversal exposure through the API.

**A survives.** Ownership is unambiguous (Engine for validation semantics, interfaces for transport limits) and the existing quality model — Checkstyle, coverage verification, deterministic tests — carries it. **B survives** but adds only a testing preference. **C fails** — parser robustness is not a domain.

`Inference`: the residual resource-exhaustion exposure is an ordinary implementation gap in an owned area, materially bounded today because the surface is loopback-only. It becomes material in context 2, not context 1.

### 6. The first live industrial adapter is introduced

**What actually happens:** `operational-execution-digital-twin.md` §14 already scopes this precisely — adapters sit *behind* Arcogine semantic contracts, carry separate subject-correspondence / observation-mapping / operation-realization responsibilities, and must not collapse into a `direction = observe | actuate` flag. §16's ten safety principles govern the consequential path. Implementation is **NOT ADMITTED** and gated behind six explicit promotion criteria.

**A survives.** **B survives** and does one useful thing A does not: nothing today defines *what security evidence* must exist before a live adapter is accepted — though `docs/planning/operational-execution-digital-twin-readiness.md` promotion criterion 5 ("failure/safety semantics are explicit for consequential behavior") and criterion 6 ("executable acceptance evidence is defined") already reserve that slot for the Operational readiness owner. **C fails** — it would duplicate Operational wholesale.

### 7. A credential or trust root is compromised or expires

**Product side:** `.github/SECURITY.md`'s mature-product principles already name credential/trust lifecycle, revocation, and loss-of-trust behavior; `operational-execution-digital-twin.md` §5 and §16.9 make them Operational requirements. Owned, deferred, correct.

**Repository side:** a compromised maintainer token or Actions secret has no documented response. `incident` as a security term appears nowhere in the repository.

**A partially fails** on the repository side; **B survives**; **C survives** but is disproportionate; **D survives**.

`Inference`: this is a genuine absence, but classifying it as a *gap* would be exactly the "absence of a mature control equals defect" error the brief excludes. Arcogine is a single-maintainer public repository with no production deployment, no customer data, and no published artifact. Incident-response procedure is correctly deferred; the trigger is hosted operation or a published release, not repository maturity.

### 8. Arcogine produces a release/container that another organization deploys

**What actually happens:** nothing — **zero releases, zero tags**. Container images are built and scanned in CI (`arcogine-api:ci`, `arcogine-ui:ci`) but never published. An SBOM is generated for scanning and never distributed.

**All candidates survive trivially**, because the case does not exist. The useful output is the *trigger*: at the first published artifact, SLSA-style build provenance, a distributed SBOM, artifact integrity/signing, and a supported-versions policy all become live simultaneously — and would land on infrastructure/CI ownership, not on a product domain.

`Inference`: the currently correct answer is **no action**. Pre-building release provenance for a project with no consumers is the clearest available example of what this investigation must not recommend.

**Residual observation:** neither Dockerfile sets a `USER` directive, so both runtime images run as root. Owner: infrastructure. This is a real hardening gap, low-consequence while images are never published or network-exposed, and it becomes material at either trigger.

### 9. A security vulnerability is privately reported

**What actually happens:** `.github/SECURITY.md` says "please report it by opening a GitHub issue with the label `security`. For sensitive issues, contact the maintainers directly." The repository is **public**, so the primary documented channel discloses the vulnerability publicly at the moment of reporting. The "contact the maintainers directly" fallback names no address, no key, and no response expectation. GitHub's private vulnerability reporting — designed for exactly this, available on public repositories — is **disabled**.

**A fails**, plainly and currently. **B survives** — vulnerability lifecycle is B's remit. **C survives** but is disproportionate to a one-setting fix. **D survives** — the owner (`.github/SECURITY.md`) exists and is simply wrong.

`Inference`: this is the clearest genuine current defect the investigation found, and notably it is a *policy* defect, not an architecture defect. It also demonstrates the failure mode of Candidate A as literally stated: `.github/SECURITY.md` is nominated as the cross-cutting policy surface, but nothing owns *reviewing whether it is still correct*.

---

## Coverage and gap matrix

Status vocabulary: **covered now** / **owned future** (deferred correctly, owner clear) / **ownerless gap** / **ownership ambiguity** / **assurance gap** / **implementation gap** / **research gap** / **intentional deferral**.

| Concern | Context | Current owner | Current control/evidence | Status | Gap | Trigger | Recommended destination |
|---|---|---|---|---|---|---|---|
| Vulnerability reporting / coordinated disclosure | 3 | `.github/SECURITY.md` | Public-issue instruction; private reporting **disabled** | **implementation gap** | Public repo's documented channel discloses on submission | **Now** (already public) | `SECURITY POLICY` |
| CORS restriction evidence | 2 | `interfaces/api` + `testing.md` | Implemented in `WebConfig`; **no test**; `testing.md` claims otherwise | **assurance gap** + false evidence claim | Recommended control unverified; doc incorrect | **Now** | `DEVELOPMENT/ASSURANCE POLICY` |
| Security readiness threshold by exposure class | 2, 4 | **none** | Hardening runbook without a threshold | **assurance gap** | No answer to "when does a control become required?" | Before intentional network/public exposure | `SECURITY POLICY` |
| Hosted multi-user identity, tenant authority, data isolation, abuse control | 4 | **none** | none | **ownerless gap** / ownership ambiguity | Falls outside Operational (no consequence), Governance, Factory/Engine | Concrete hosted/multi-user consumer | `RESEARCH` |
| Dependency vulnerability detection | 3 | CI (`java-audit`, `frontend`, daily schedule) | SBOM + Trivy + npm audit, blocking, daily | **covered now** | Dependabot alerts disabled — single detection path, unrecorded | Reconsider if scanner coverage narrows | `NO ACTION` (optionally record the decision) |
| Dependency remediation / triage / exceptions | 3 | Dependency Maintainer | Contract + `.trivyignore` justified ledger | **covered now** | — | — | `NO ACTION` |
| CI / Actions authority, third-party action trust | 3 | `reviewing.md`, PR Reviewer, CODEOWNERS, ruleset | SHA-pinned actions, least-privilege tokens, base-sourced triggers, active ruleset, actionlint in required CI | **covered now** | Activation item 2 (reviewer-identity independence) unverified and repository-acknowledged | Change in maintainer/agent identity model | `NO ACTION` (limitation recorded) |
| Secret scanning | 3 | CI `security-secrets` | Gitleaks, unconditional on every trigger, SHA-pinned | **covered now** | — | — | `NO ACTION` |
| Container/base-image vulnerabilities | 3 | CI `docker` job | Trivy CRITICAL/HIGH blocking; `apk upgrade` in both images | **covered now** | — | — | `NO ACTION` |
| Container runtime hardening (non-root) | 3, 2 | infrastructure (`infra/docker/`) | Security headers + CSP on web image; **no `USER` directive** | **implementation gap** | Images run as root | Network exposure or first published image | `DEVELOPMENT/ASSURANCE POLICY` |
| API attack surface (body size, SSE limits, bind address) | 1, 2 | `interfaces/api`, `interfaces/cli` | Implemented and tested | **covered now** | — | — | `NO ACTION` |
| Untrusted scenario/config parsing | 1, 2 | Engine + interfaces | Structural validation, economy bounds, 1 MiB cap; tested | **covered now** | `max_ticks` and collection sizes unbounded upward | Network exposure | `NO ACTION` now; revisit at exposure |
| Authentication / identity for the current API | 2 | — (documented non-goal) | Documented limitation | **intentional deferral** | — | Network exposure or hosted use | `SECURITY POLICY` (threshold, above) |
| Consequential authority, verified peer/source/target trust, fail-safe | 6 | Operational (**Proposed**) | Architecture §5, §6, §16; **NOT ADMITTED** | **owned future** | — | First live adapter | `NO ACTION` (already owned) |
| External observation authenticity/provenance | 5 | Operational (**Proposed**) | Architecture §8, §8.1 | **owned future** | — | First external data source | `NO ACTION` |
| Credential/trust lifecycle, revocation (product) | 6 | Operational + `SECURITY.md` principles | Named as requirements | **owned future** | — | First live adapter | `NO ACTION` |
| Incident/recovery expectations (repository) | 3 | **none** | none | **intentional deferral** | No documented response to token/secret compromise | Hosted operation or published release | `NO ACTION` now |
| Release provenance, SBOM distribution, artifact signing | 3 | infrastructure/CI (prospective) | SBOM generated for scanning only; **zero releases** | **intentional deferral** | — | **First published release or image** | `NO ACTION` now |
| Audit / security-event evidence (product) | 6 | Governance + Operational | Charter causality/provenance; ADR-0011 event contract; Governance evidence **Proposed** | **owned future** | — | Consequential execution | `NO ACTION` |
| Compliance framework projection (SOC 2 / ISO 27001 of the *modeled business*) | — | Governance | Explicit non-claim in §14 | **covered now** | Not Arcogine's own compliance — do not conflate | — | `NO ACTION` |
| Threat modeling / abuse-case discipline | 2, 4, 6 | **none** | `threat` appears in exactly one file, as a future requirement | **assurance gap** | No threat-model artifact exists | Network exposure (folds into the readiness threshold) | `SECURITY POLICY` (same paragraph) |
| SAST / DAST / fuzzing | 1–3 | — | Checkstyle, ESLint, typecheck, coverage verification | **intentional deferral** | No evidence of need at the current surface | Network exposure with untrusted input at scale | `NO ACTION` |
| Security-standard alignment (OT / software security frameworks) | 5, 6 | `standards-alignment.md` | ISA-95/62264, B2MML, AutomationML, AAS, OPC UA, ISO 9001 — **no security family** | **owned future** | No entry for IEC 62443 or equivalent | First live adapter / conformance claim | `ARCHITECTURE/ADR` when triggered |
| Control-drift detection over security controls | 3 | Consistency agent | Remit covers CI/tooling and operational security/authority | **covered now** | — | — | `NO ACTION` |

---

## Candidate-model comparison

`S` = survives coherently. `P` = partially survives. `F` = fails (invents an owner, duplicates one, or cannot place the case).

| Proving case | A (distributed) | B (assurance owner) | C (Security track) | D (A + named corrections) |
|---|:--:|:--:|:--:|:--:|
| 1 — dependency CVE, no code change | **S** (demonstrated live) | S (redundant) | **F** (takes remediation from the maintainer) | **S** |
| 2 — workflow obtains write authority | **S** (demonstrated live) | S (redundant) | **F** (would own the review contract) | **S** |
| 3 — deliberate network exposure | **P** (no threshold; CORS untested) | **S** | S (disproportionate) | **S** |
| 4 — hosted multi-user, no actuation | **F** | **P** (identifies, cannot own authority semantics) | **S** | **S** (names it as research) |
| 5 — untrusted scenario artifact | **S** | S (adds a testing preference) | **F** (parsing is not a domain) | **S** |
| 6 — first live industrial adapter | **S** | S (adds pre-acceptance evidence) | **F** (duplicates Operational) | **S** |
| 7 — credential/trust compromise | **P** (repository side unowned) | S | S (disproportionate) | **S** (deferred with a trigger) |
| 8 — external org deploys a release | S (vacuous — no releases) | S (vacuous) | S (vacuous) | **S** (records the trigger) |
| 9 — private vulnerability report | **F** | **S** | S (disproportionate) | **S** |

**Reading the matrix.** C survives only case 4 in a way no other candidate does, and fails four cases outright by duplicating owners who already discharge them well. B survives everything but owns nothing exclusively except case 3's threshold and case 9's lifecycle — two obligations that fit inside one existing document. A fails cases 4 and 9 and is partial on 3 and 7. D survives every case without creating an owner.

**C is rejected with high confidence.** It is the only candidate that *loses* capability relative to the status quo: on cases 1, 2, 5, and 6 it would relocate responsibility away from owners who demonstrably handle it.

**B is rejected as a new owner with moderate-high confidence.** Its distinctive content reduces to two obligations, both of which have a natural home in `.github/SECURITY.md`. Creating a surface to hold them would also risk the failure mode the brief warned about — a cross-cutting assurance function drifting into second ownership of domain semantics — precisely because case 4 tempts it to own tenant authority, which it is defined not to own.

**A is rejected as literally stated**, because it asserts that `.github/SECURITY.md` plus normal engineering controls are currently sufficient, and cases 3, 4, 7, and 9 show three specific respects in which they are not.

**D survives.**

---

## Adversarial analysis

**This is the author's own self-challenge, not an independent pass.** Per `docs/development/researching.md` §7 and §9, this question is high-risk and its conclusion is **not yet decision-quality evidence** for an ADR or comparable durable architecture until a genuinely independent adversarial review is performed. That review has not occurred.

Challenges attempted against this report's own conclusion:

**"You concluded 'no new structure' on a question framed as a security investigation — is that motivated reasoning toward the least-work answer?"**
Partially defensible as a concern. The mitigation applied was to look hardest at the cases where new structure would be most justified (4 and 9) and to report both as genuine failures of Candidate A rather than explaining them away. Case 9 is reported as a current, concrete defect. Case 4 is reported as a genuine ownerless gap. The conclusion is not "nothing is wrong"; it is "the smallest correct response is not a new owner."

**"Case 4 is a real ownerless gap. Doesn't one ownerless gap justify one owner?"**
This is the strongest surviving objection. The counter-argument is that the gap has **no consumer** — nothing hosted exists, the Charter does not commit to hosting, and `operational-execution-digital-twin.md` §17 explicitly warns against selecting authentication providers and generic policy frameworks before a concrete requirement exists. Creating an owner for a hypothetical consumer inverts the repository's own stated discipline. But this argument would fail immediately if a hosted consumer became concrete, which is why it is recorded as a reopening trigger rather than a settled non-issue.

**"You leaned on pull request #294 as evidence that the existing model works. Is that over-generalizing from one incident?"**
The brief explicitly warned against this, and the risk is real. Three mitigations: the same discipline is independently visible in `pr-disposition.yml`, which predates that pull request and documents its own residual gaps unprompted; the dependency-CVE thread is a *second*, mechanically different case in the same pull request; and the conclusion does not rest on the pull request at all — it rests on the coverage matrix. The pull request corroborates; it does not carry.

**"Is the boundary between 'semantic security' and 'security assurance' real, or is it a convenient abstraction?"**
It is doing real work in this analysis — it is what lets case 6 stay with Operational while case 3's threshold does not. It is corroborated by IEC 62443's zone-scoped structure and by SSDF's integrate-into-existing-SDLC framing. But it is also the kind of clean-sounding distinction that can be over-applied, and it should be a primary falsification target for independent review. In particular, case 4 is the case where the boundary is *least* clear: tenant authority is arguably both.

**"Is `.github/SECURITY.md`'s role overloading actually a problem, or manufactured to have something to say?"**
Honestly assessed: it is mild. The document labels its own split and repeatedly disclaims that planned principles are implemented. This report therefore does **not** recommend splitting it, and reports the finding as "carries five roles" rather than "is misleading." The actionable part is the missing threshold, not the structure.

**"You claim CORS is untested. Could the test exist somewhere the search missed?"**
The search was `git grep -in "cors"` across all of `product/`, returning exactly three hits, all in `WebConfig.java` (an import, the method, and the environment-variable read). Search scope stated so the absence claim is falsifiable, per `docs/development/researching.md` §5.

**"Dependabot alerts being disabled — are you sure that is a feature state, not a token-scope error?"**
The response was HTTP 403 with the message "Dependabot alerts are disabled for this repository", which differs from the "Resource not accessible by integration" scope errors returned by other endpoints in the same authenticated sweep, and from the proxy-policy errors returned by others. That is good but not conclusive evidence, and it is reported as a residual observation rather than as a gap requiring action, precisely because Arcogine's own scanners provide the detection path.

**"Have you smuggled a future feature in as a present necessity anywhere?"**
Checked deliberately against each "now" recommendation. The disclosure-path fix is required by the repository being public *today*. The CORS evidence fix is required by `docs/development/testing.md` making a false claim *today*. The readiness threshold is explicitly **not** a "now" item — it is scoped to before intentional exposure. Release provenance, SAST/DAST, incident response, and standards alignment are all placed as triggered or no-action rather than as gaps.

---

## Surviving invariants

The smallest rules that held across every proving case:

1. **Security semantics belong to the owner of the behavior whose consequence they protect.** Consequential trust belongs to Operational because Operational owns consequence; scenario validation belongs to Engine because Engine owns scenario meaning; workflow trust belongs to the review/CI authority because it owns what may execute with repository authority.

2. **Security assurance may be cross-cutting even when security semantics remain owned by the domain that owns the consequential behavior — but a cross-cutting assurance obligation does not by itself justify a cross-cutting owner.** The evidence supports the first clause (cases 3, 6, 9 all have assurance content separable from domain semantics). It does **not** support promoting that separability into a new surface when the obligations fit inside existing authorities. This is a narrower invariant than the one the brief anticipated, and the narrowing is the finding.

3. **An absent control is a gap only when something that exists today is exposed by its absence.** Disclosure-path and CORS-evidence are gaps because the repository is public and the doc claims coverage now. Release provenance and incident response are not gaps because nothing is released and nothing is deployed.

4. **Documented evidence claims are security controls.** `docs/development/testing.md`'s incorrect CORS claim is materially a security defect, because a reader deciding whether to expose the service relies on it. A false assurance claim is worse than an acknowledged absence.

5. **Platform trust boundaries must be enforced at the platform, not described in the artifact they protect.** Three successive remediations in the observed pull request failed because they placed the control inside an artifact the attacker also controlled. This generalizes beyond GitHub Actions.

6. **Generic actor/capability/authority semantics are not automatically Operational, and are not automatically security-owned either.** Case 4 shows a security-relevant authority question that belongs to neither, and whose ownership is genuinely open.

---

## What did not survive

- **Candidate C (standalone Security track)** — rejected. Fails four proving cases by duplicating owners that demonstrably discharge them; survives only the one hypothetical case; would rest on architecture that is still Proposed.
- **Candidate B as a new owner** — rejected. Its distinctive content reduces to two obligations that fit inside `.github/SECURITY.md`. Retained *as a description of an obligation*, discarded *as a surface*.
- **Candidate A as literally stated** — rejected. Its claim of current sufficiency is falsified by cases 3, 4, 7, and 9.
- **The framing that Arcogine "has no security owner."** Falsified by the ownership map: security responsibilities are distributed to named, contract-bearing roles with executable enforcement and an active branch ruleset.
- **The assumption that pull request #294 contains a still-blocking security finding.** Falsified — resolved at head `4580007`, current disposition READY TO MERGE.
- **The hypothesis that Governance covers Arcogine's own security assurance.** Falsified by `governance-conformance.md` §14, which explicitly disclaims that Arcogine implements SOC 2 / ISO 27001 for itself; Governance governs the *modeled business*.
- **The hypothesis that Operational research already covers non-consequential multi-user security.** Falsified — Operational's contract is consequence-scoped throughout, and the Agency research covers the generic actor/capability half but not tenant isolation, session/credential handling, or abuse resistance.
- **Treating IEC 62443 or SSDF as establishing an Arcogine requirement.** Both establish structure and possibility; neither establishes necessity for a project with no industrial control system and no published artifact.

---

## Confidence and limitations

**Confidence:** high on rejecting Candidate C; moderate-high on rejecting Candidate B as an owner; high on the two current gaps; moderate on the triggered ownership ambiguity in case 4.

**What would change the conclusion:**

- A concrete hosted or multi-user Arcogine consumer becoming real — this would elevate case 4 from hypothetical to live and could justify a durable owner.
- A first published release or distributed image — this would activate provenance/SBOM/signing obligations simultaneously and could justify a supply-chain assurance responsibility.
- Evidence that the reviewer/owner identity is not separable from the implementing agent identity — this would materially weaken the control-plane assurance on which the "covered now" verdicts for cases 1 and 2 partly rest.
- Repeated security findings escaping the existing review/CI model — one handled case is corroboration, not proof of a durable capability.
- ADR-0013 being accepted in a form that changes what Operational owns.

**Surfaces that could not be inspected**, recorded rather than inferred: GitHub Actions repository permission settings; environment protection rules; secret-scanning and code-scanning configuration; legacy branch-protection detail (superseded by the inspected ruleset but not independently confirmed); whether the coding-agent identity is distinct from the CODEOWNERS identity; and the normative text of IEC 62443-3-2:2020 (paywalled — title/scope verified from official listings only).

**Evidence quality caveats:** the SSDF practice-group names could not be verified and are deliberately not used. One GitHub documentation fetch returned an internally inconsistent summary and was down-weighted to only its corroborated parts. The SLSA page fetched was v1.1 while v1.2 is current.

---

## Unresolved unknowns

1. **Who owns security for a hosted, non-consequential Arcogine?** The only genuine ownerless gap. Distinct from Operational (no consequence) and from the Agency investigation (which owns generic actor/capability semantics, not deployment security).
2. **What evidence should be required before deliberate network exposure is acceptable?** Identified as an obligation and assigned a destination, but its content is not settled by this investigation.
3. **Is the reviewer-identity independence condition in `docs/development/reviewing.md` actually satisfiable** in a single-maintainer, agent-operated repository? This is a repository-process question, not a research question, and is flagged rather than answered.
4. **Whether disabling Dependabot alerts is a deliberate decision** or an unexamined default. Not recorded anywhere.

---

## Durable consequences

Recommendations only. This document performs no promotion; each item requires its own independently reviewed change.

### Now

| Recommendation | Destination |
|---|---|
| Enable GitHub private vulnerability reporting, and rewrite `.github/SECURITY.md`'s "Reporting a Vulnerability" section so a public repository does not direct reporters to a public issue as the primary channel. Include a response expectation. | `SECURITY POLICY` |
| Resolve the CORS evidence discrepancy: add an executable regression test for `CORS_ALLOWED_ORIGIN` set/unset behavior, or correct `docs/development/testing.md`'s "Security verification tests" claim. The test is preferable — it is the one control `.github/SECURITY.md` tells a deploying user to rely on. | `DEVELOPMENT/ASSURANCE POLICY` |

### Before intentional network / public exposure

| Recommendation | Destination |
|---|---|
| Add an exposure-class → required-evidence statement to `.github/SECURITY.md`, so "when does authentication (or abuse control, or audit) stop being advisory?" has an answer. This is the one genuinely cross-cutting assurance obligation the evidence supports, and it belongs inside the existing authority. Threat-model discipline folds into this rather than becoming a separate artifact. | `SECURITY POLICY` |
| Revisit scenario resource bounds (`max_ticks` upper bound, collection-size caps) and container non-root execution. Both are ordinary implementation gaps in owned areas, immaterial while loopback-only. | `DEVELOPMENT/ASSURANCE POLICY` |

### Before hosted / multi-user operation

| Recommendation | Destination |
|---|---|
| Resolve who owns identity, tenant authority, data isolation, and abuse resistance for a non-consequential hosted Arcogine, before any hosted slice is admitted. | `RESEARCH` → then `ARCHITECTURE/ADR` |

### Before production-consequential Operational execution

| Recommendation | Destination |
|---|---|
| No new consequence. This is already owned by Operational architecture and gated by the readiness plan's six promotion criteria, which already reserve slots for explicit failure/safety semantics and executable acceptance evidence. | `NO ACTION` (already owned) |
| When a live adapter or a conformance claim becomes concrete, add a security-standard family entry to `docs/architecture/standards-alignment.md` at the same precision the document already requires of ISA-95/IEC 62264. | `ARCHITECTURE/ADR` when triggered |

### Triggered later by a concrete consumer

| Trigger | Recommendation | Destination |
|---|---|---|
| First published release or distributed image | Build provenance, distributed SBOM, artifact integrity, supported-versions policy — all simultaneously, all on infrastructure/CI ownership | `NO ACTION` now |
| Hosted operation or published release | Incident/recovery expectations for the repository and service | `NO ACTION` now |
| Network exposure with untrusted input at scale | Reconsider SAST/DAST/fuzzing | `NO ACTION` now |

### Explicit no-action

Creating a Security module, a Security architecture track, a Security planning surface, or a Security delivery coordinate. Moving any Operational or Governance security semantics to a new owner. Selecting an IdP, certificate scheme, policy engine, secrets manager, or SIEM.

---

## Implementation implication

**No implementation.** Two of the four recommendations are a repository setting plus a documentation correction; one is a small test; one is a research question. None requires a product-code change, a new module, an ADR, or a planning admission.

The only item that touches `product/` is the optional CORS regression test, which belongs in the existing `interfaces/api` suite alongside the body-size and SSE-limit tests, per `docs/development/testing.md`'s existing statement that hardening checks live in the regular suite rather than a separate pipeline.

---

## Research-register implications

`docs/research/README.md` was re-checked at this baseline: it contains **no** security architecture/readiness question, and the two nearest entries — the Operational actor/trust/authority question and the Agency and Decision Boundary question — are both scoped to *semantic* actor/capability/consequence boundaries, not to Arcogine's own deployment security or software assurance.

**One new bounded entry is proposed. It has not been added — the register is not mutated by this document.**

- **Question:** For a hosted or multi-user Arcogine that performs no external actuation, which security responsibilities — identity, per-user/tenant authority over models and scenarios, data isolation, session/credential handling, and abuse resistance — require a durable owner, and does any of them belong to an existing owner rather than a new one?
- **Area:** Cross-cutting / Interfaces
- **Recommended priority:** Medium
- **Lifecycle:** **`CANDIDATE`**, not `READY` — no hosted or multi-user consumer exists, so the question cannot yet be bounded by a real requirement, and `docs/research/README.md` reserves `READY` for questions an independent researcher could execute from stated evidence and exit criteria. Marking it `READY` would manufacture urgency the evidence does not support.
- **Expected destination:** architecture/ADR ownership decision if a durable owner is required; otherwise an explicit recorded no-action result. Implementation planning only after a concrete hosted consumer exists.
- **Explicit non-duplication:** must not restate the Operational actor/trust/authority question (consequence-scoped) or the Agency and Decision Boundary question (generic actor/capability semantics). Its distinctive content is deployment security for a non-consequential service.

No other new register entry is warranted. The other three findings have existing owners and do not require research to resolve.

---

## Follow-up triggers

Reopen or extend this investigation when any of the following occurs:

- a concrete hosted or multi-user Arcogine consumer appears;
- the first release or container image is published;
- ADR-0013 is accepted in a form that changes what Operational owns;
- a security finding escapes the existing review/CI model, or a second finding of the same class recurs;
- the maintainer/agent identity model changes such that the reviewing-owner independence condition is affected;
- Arcogine's scanner coverage narrows, making the disabled Dependabot alerts a live single point of detection failure.

Track follow-up through `docs/research/README.md`, not through a parallel ledger.

---

## Adversarial-review requirement

This question is **high risk** under `docs/development/researching.md` §7. The self-challenge above is the author's own and is explicitly **not** an independent pass.

Before any recommendation here is promoted into an ADR or comparable durable architecture, a **genuinely independent adversarial research review** is required under §9. The reviewer should be asked in particular to attempt to falsify:

- the claim that no new Security owner is needed;
- the claim that existing distributed ownership is sufficient for cases 1, 2, 5, and 6;
- the boundary drawn between semantic security and security assurance — especially where it is weakest, in the hosted multi-user case;
- the assumption that Operational and Agency research do not already cover the hosted multi-user gap;
- the treatment of IEC 62443 and SSDF as structure-establishing rather than requirement-establishing;
- whether any "now" recommendation is a future feature smuggled in as a present necessity;
- whether the deferrals (release provenance, incident response, SAST/DAST) are correctly deferred or convenient omissions.

This research is **not `CONCLUDED`**. Under `docs/research/README.md`'s lifecycle, `CONCLUDED` requires the durable consequence to be reconciled into its authoritative surface, or an explicit recorded no-action result — neither of which this document performs.

---

## Sources

**Repository (baseline `5d010e2aa79e36ca6a9793ebf305ba309e3da5a7`):** `AGENTS.md`; `.github/agents/researcher.agent.md`; `docs/development/researching.md`; `docs/research/README.md`; `docs/research/report-template.md`; `docs/product/charter.md`; `docs/architecture/overview.md`; `.github/SECURITY.md`; `docs/architecture/operational-execution-digital-twin.md`; `docs/research/operational-execution-digital-twin-boundaries.md`; `docs/planning/operational-execution-digital-twin-readiness.md`; `docs/research/agency-decision-boundary.md`; `docs/architecture/decisions/` (index plus per-ADR status sweep — ADR-0013 and ADR-0016 **Proposed**, ADR-0005 **Superseded**, remainder **Accepted**); `docs/architecture/governance-conformance.md`; `docs/architecture/standards-alignment.md`; `docs/development/testing.md`; `docs/development/reviewing.md`; `.github/agents/dependency-maintainer.agent.md`; `.github/agents/consistency.agent.md`; `.github/agents/work-planner.agent.md`; `.github/workflows/ci.yml`; `.github/workflows/pr-disposition.yml`; `.github/workflows/pr-disposition-review-trigger.yml`; `.github/CODEOWNERS`; `.github/CONTRIBUTING.md`; `.github/dependabot.yml`; `.github/security/gitleaks.toml`; `.github/scripts/check-actions-workflows.sh`; `.github/scripts/check-delivery-labels.py`; `.trivyignore`; `arcogine`; `infra/docker/api.Dockerfile`; `infra/docker/web.Dockerfile`; `product/interfaces/api/src/main/java/com/arcogine/api/config/WebConfig.java`; `product/interfaces/api/src/main/java/com/arcogine/api/controller/SseController.java`; `product/interfaces/api/src/main/java/com/arcogine/api/controller/SimController.java`; `product/interfaces/api/src/test/java/com/arcogine/api/ApiSmokeTest.java`; `product/interfaces/cli/src/main/java/com/arcogine/cli/ArcogineCommand.java`; `product/interfaces/cli/src/test/java/com/arcogine/cli/ArcogineCommandTest.java`; `product/simulation/src/main/java/com/arcogine/core/scenario/ScenarioLoader.java`.

**Live GitHub configuration (read via API at synthesis, not encoded in git):** repository metadata (`public`, Apache-2.0, `allow_forking: true`, zero releases, zero tags); ruleset `14822987` "Protect main" (`enforcement: active`); private-vulnerability-reporting state (`enabled: false`); Dependabot alerts feature state (disabled); pull request [#294](https://github.com/alaiba/arcogine/pull/294) metadata and its five review rounds.

**External:**

- GitHub Docs, "Secure use reference," `https://docs.github.com/en/actions/reference/security/secure-use` — **verified in session**. Establishes privileged-trigger risk, the untrusted-checkout prohibition, `GITHUB_TOKEN` least privilege, and full-SHA action pinning.
- GitHub Docs, "Privately reporting a security vulnerability," `https://docs.github.com/en/code-security/security-advisories/guidance-on-reporting-and-writing-information-about-vulnerabilities/privately-reporting-a-security-vulnerability` — **verified in session**. Establishes availability on public repositories, off-by-default, and the confidentiality difference versus a public issue.
- GitHub Docs, "Events that trigger workflows," `https://docs.github.com/en/actions/reference/workflows-and-actions/events-that-trigger-workflows` — **partially verified; internally inconsistent summary returned**. Used only for the parts corroborated by the secure-use page: `repository_dispatch`/`schedule`/`workflow_run`/`pull_request_target` bind `GITHUB_REF` to the default/base branch; `workflow_dispatch` binds it to the dispatched ref.
- NIST Special Publication 800-218, *Secure Software Development Framework (SSDF) Version 1.1: Recommendations for Mitigating the Risk of Software Vulnerabilities*, February 2022, `https://csrc.nist.gov/pubs/sp/800/218/final` — **designation, title, version, date and the "integrated into each SDLC implementation" framing verified in session**. Practice-group abbreviations/names **not verified** (corrupted PDF extraction) and deliberately not cited.
- SLSA specification v1.1, Build levels, `https://slsa.dev/spec/v1.1/levels` — **verified in session**, with the page indicating **v1.2 is currently active**; re-verify against v1.2 before load-bearing use. Establishes that provenance attaches to published/distributed artifacts.
- IEC 62443-3-2:2020, *Security for industrial automation and control systems — Part 3-2: Security risk assessment for system design* (also ANSI/ISA-62443-3-2-2020), `https://webstore.iec.ch/en/publication/30727` — **designation, part, edition year and scope verified via official IEC/ISA listings; normative text not accessed (paywalled)**. Establishes zone/conduit partitioning and per-zone Security Level targets. Any future normative dependency must cite the exact clause from the standard itself.
