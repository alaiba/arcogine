# Security ownership and assurance boundary

> **Status:** `ACTIVE` — decision-quality report written; durable consequences not yet reconciled
> **Research baseline:** live `main` `950a19742e811918a2312d8ef6fdf0ec129761d3`
> **Risk classification:** **High** (security, authority, trust boundaries, potential cross-domain ownership)
> **Authority:** Research evidence only. This document is not accepted architecture, product direction, security policy, or implementation commitment. Only a separate reconciliation change carries that authority.
> **Adversarial-review status:** `required, not yet performed`. The self-challenge in § Adversarial analysis is the author's own and must not be given the weight of an independent pass.

## Question

After accounting for Arcogine's existing current-security policy and controls, Operational/Digital-Twin security semantics, Governance responsibilities, domain ownership, and development/CI security controls, are there material security responsibilities that remain ownerless, ambiguously owned, or insufficiently represented in Arcogine's architecture/readiness model? If so, what is the smallest durable ownership structure that should carry them?

No brief for this question existed in [`docs/research/research-register.md`](../research-register.md) at the baseline, and none exists now. The question arrived already bounded by its handoff; it was executed as stated without sharpening. A register entry is *not* proposed for the question itself — see § Research-register implications for why.

## Decision at stake

Whether Arcogine should keep security distributed across existing owners, introduce a cross-cutting Security Assurance responsibility, establish a standalone Security architecture/delivery track, or adopt another structure. The answer changes whether a new owner, readiness model, planning track, or architecture surface is created — and, if not, what prevents cross-cutting security obligations from becoming ownerless.

## Scope and non-goals

In scope: ownership, boundaries, readiness, and evidence.

Explicitly **not** in scope, and not performed: implementing any control; creating a Security module, planning track, or delivery coordinate; editing Accepted ADRs; settling Proposed ADRs; prescribing an IdP, certificate technology, policy engine, secrets manager, scanner, or deployment platform; assuming hosted SaaS, multi-tenancy, or live industrial control exists today; rating Arcogine against a maturity checklist.

One incidental implementation defect surfaced while establishing the security surface. It is recorded in § Repository evidence because suppressing a real finding would be dishonest, not because this investigation became an audit.

## Executive conclusion

**Arcogine does not need a standalone Security track, and does not need a new security owner of any kind. Candidate C is rejected; Candidate A is very nearly sufficient and is the correct ownership model. What Arcogine is missing is not an owner but two artifacts: a maintained statement of what its security requirements and verification criteria actually are, and a working private vulnerability-report channel.**

Stated as the surviving distinction:

> A cross-cutting security **assurance policy surface** is justified. A cross-cutting security **owner, role, or track** is not.

Every *semantic* security concern the repository makes relevant already has a documented owner, and those owners are correct. Every *process* security concern — review, dependency remediation, scanning, control-plane protection — also already has an owner, and those owners demonstrably work. The failures found are failures of **recorded requirement and routed evidence**, not of assignment.

**Confidence: moderate-to-high on the negative conclusion** (no new owner/track is justified) — this is supported by convergent repository evidence and survives every proving case. **Confidence: moderate on the positive recommendations** (the specific artifacts), which are small, reversible, and individually evidenced, but have not been independently challenged.

This conclusion is **not yet decision-quality evidence for an ADR or comparable durable architecture**, because § 7/§ 9 of [`docs/development/researching.md`](../../development/researching.md) require a genuinely independent adversarial review for a high-risk question and none has occurred.

## Repository evidence

All claims below are `Repository fact` at baseline `950a1974` unless labeled otherwise.

### Semantic security ownership is already assigned, and the assignments are explicit

- [`docs/architecture/operational-execution-digital-twin.md`](../../architecture/operational-execution-digital-twin.md) (**Proposed** architectural reference) §5 owns claimed-versus-verified identity, trust roots and peer/source/target authenticity, credential/secret lifecycle, least privilege, revocation/expiry, physical safety enforcement, and fail-safe behavior. §6 owns external command lifecycle and the rule that an accepted command is not proof reality changed. §16 states ten safety/failure principles, including "Absence of authority is denial, not implicit permission" and "An unverifiable actor/source/target is not silently treated as trusted."
- Critically, §5 also **disclaims** ownership it is not entitled to: "The exact module ownership of reusable actor/capability semantics is still open and must not be forced into Operational merely because the first real-world consumer needs it." §17 explicitly defers authentication-provider and policy-framework selection.
- [ADR-0013](../../architecture/decisions/0013-durable-operational-identity.md) is **Accepted** and fixes accountable-operational-continuation identity, deliberately separate from actor identity.
- [ADR-0012](../../architecture/decisions/0012-external-interchange-and-serialization-boundaries.md) is **Accepted** and owns *semantic authority* over interchange formats. Inspection of all 15 decision sections confirms it does **not** address untrusted-input safety (parser hardening, resource exhaustion, deserialization). That is a real boundary of the ADR, not an oversight in it.
- [`docs/architecture/overview.md`](../../architecture/overview.md) records the cross-cutting attribution/decision boundaries and, at lines 22 and 31, points security-sensitive defaults and future real-execution security concerns at `.github/SECURITY.md`.
- The register already carries the open actor-identity question ("What durable actor identity, if any, must Arcogine share across concrete consumers…", `CANDIDATE`, Low) and the Operational boundaries question ("…actor/trust/authority, external operation realization…", `CANDIDATE`, High).

**Inference:** the semantic layer is not ownerless, not ambiguous, and not under-represented. It is deliberately and correctly deferred, with the deferral itself documented and with an explicit guard against over-assigning it to Operational.

### Process security ownership is also already assigned, and it demonstrably functions

- `SECURITY_AUTHORITY` is an established PR-review finding category ([`.github/agents/pr-reviewer.agent.md`](../../../.github/agents/pr-reviewer.agent.md) line 256), and security/authority is explicitly a **High** review-risk trigger (line 214). [`docs/development/reviewing.md`](../../development/reviewing.md) line 228 makes a security issue P0.
- [`.github/agents/dependency-maintainer.agent.md`](../../../.github/agents/dependency-maintainer.agent.md) owns security updates: §"Security update" requires advisory analysis and the smallest safe remediation, forbids weakening controls or allowlisting a vulnerability without evidence, and the sweep order prioritizes security updates first.
- CI (`.github/workflows/ci.yml`) runs Trivy SBOM scan, Trivy image scans, `npm audit`, and Gitleaks, with a **daily 05:00 UTC scheduled re-scan** explicitly so "findings can change without repo changes". Scanner binaries are SHA256-checksum-verified before install; all actions are SHA-pinned. The `gate` job fails closed on expectation drift.
- `.trivyignore` carries a real, reasoned exception policy: only non-shipped (test/build-scope) CVEs are suppressed, with the reason recorded per entry; "Shipped-runtime CVEs are deliberately NOT suppressed and will fail the gate."
- The repository-control-plane threat model is unusually explicit. `.github/CODEOWNERS` owns `/.github/` and `/.github/workflows/` and documents *why* it owns itself, citing GitHub's own guidance. `docs/development/reviewing.md` §"PR disposition merge gate" states the trust boundary, names two residual weaknesses (listener-coverage narrowing; required-check-name provenance), and gives four activation conditions.

### Live GitHub-side configuration (not encoded in git)

The `main` ruleset (`id 14822987`, "Protect main", `enforcement: active`, updated 2026-09-07) was read directly from the REST API:

| Documented activation condition | Live state | Verdict |
|---|---|---|
| 1. `disposition` required on `main` | `required_status_checks` includes `gate` and `disposition` | **Met** |
| 2. Agent identity cannot bypass / self-approve | `current_user_can_bypass: "never"` for this session's token | **Partly met** — see limitation |
| 3. Required checks enforce base freshness | `strict_required_status_checks_policy: true` | **Met** |
| 4. Ruleset requires Code Owner review | `require_code_owner_review: true` | **Present, effect unverified** |

Also present: `deletion` and `non_fast_forward` protection, `required_linear_history`, and `require_extra_approval_for_unattributed_changes: true`.

Two honest qualifications. First, condition 4 is configured alongside `required_approving_review_count: 0`; **this investigation did not establish whether GitHub enforces a Code Owner review when the required approving count is zero**, and that question is load-bearing for whether the documented mitigation for both named residual weaknesses is actually active. Second, condition 2 was verified only for the token this session holds, which is not the same as establishing it for every identity available to coding agents.

**Inference:** the four conditions are stated in `reviewing.md` as the definition of when the merge invariant is real, but **nothing in the repository verifies them**, and they describe state that lives outside git. `reviewing.md` says so itself: "Until all four activation conditions are true, the workflow existing and passing does not mean the merge invariant is actually enforced."

### Current product security surface

Arcogine ships more implemented security engineering than its security policy advertises:

- `product/interfaces/api/.../config/WebConfig.java` registers a `MaxBodySizeFilter` capping `/api/*` request bodies at 1 MiB, and drives CORS from `CORS_ALLOWED_ORIGIN` (permissive `*` when unset).
- `SseController` bounds concurrent event-stream connections with `new Semaphore(64)`, returning 503 beyond that.
- `SimController` bounds price input (`MAX_PRICE = 1_000_000.0`).
- `ScenarioLoader` performs referential-integrity and range validation over loaded TOML.
- `infra/docker/web.Dockerfile` sets a full security-header set including a restrictive `Content-Security-Policy`, and deliberately installs nginx from Alpine's repo so `apk upgrade` can patch it.
- `docs/development/testing.md` line 315 records these as "Security verification tests" living in `ApiSmokeTest`.

Three concrete weaknesses in that surface:

1. **The requirement set those tests trace to does not exist.** `ApiSmokeTest` labels its security cases `§3.1 Body-size limit`, `§3.2 Scenario load error propagation`, `§3.3 Handler error surfaces in snapshot`, `§3.9 SSE connection limit`, `§3.11 Economy/price input validation`. A repository-wide search finds **no document anywhere defining a §3.x numbering**. The tests are the only surviving trace of a requirement set nobody maintains, so no reader can tell whether §3.4–§3.8 and §3.10 were dropped deliberately, never existed, or are silently missing.
2. **`MaxBodySizeFilter` reads only `getContentLengthLong()`**, which returns `-1` for a chunked request with no `Content-Length`. Such a request bypasses the cap. `Repository fact` on the code path; **`Inference`** on exploitability, which was not executed against a running server.
3. **`SimThread` is a Spring `@Component`, i.e. a process-wide singleton.** There is exactly one simulation state, shared by every HTTP caller, with no session, tenant, or user concept anywhere in the API. `POST /api/scenario` accepts arbitrary TOML into it, and `ScenarioLoader` bounds values but not *size or cost* — no cap on equipment/material/segment counts, and `max_ticks` need only be `> 0`.

`.github/SECURITY.md`'s "Hardening for Network Deployment" list (bind address, CORS, TLS, dependency auditing, log verbosity) mentions none of the implemented controls above, and none of the three weaknesses. A reader following it would reasonably conclude those five steps make network exposure acceptable.

### Vulnerability reporting

`.github/SECURITY.md` §"Reporting a Vulnerability" instructs reporters to **open a public GitHub issue** labelled `security`, with "contact the maintainers directly" as an unaddressed fallback. The repository is **public** (`visibility: "public"`, confirmed via the REST API), and `has_issues: true`.

### What merged as PR #294

PR [#294](https://github.com/alaiba/arcogine/pull/294) merged on 2026-09-09 as commit `56a8762`, from base `5d010e2a` — the SHA named at handoff. It is landed repository truth now, not open-PR evidence, and its two security threads resolved as follows:

- **Dependency CVE:** CI failed the Java dependency audit on `io.netty:netty-handler` CVE-2026-75595 (CRITICAL). The PR body records that this was "an unrelated, pre-existing finding on `main`'s own daily scheduled scan too, not caused by this PR's diff." It was remediated *inside that unrelated PR* by bumping a pinned override.
- **Workflow authority:** the `SECURITY_AUTHORITY` finding took **three review rounds**. A checkout pin, a `uses:` reference pinned to `@main`, and a job-level `environment:` gate were each proposed and each falsified by re-review. The accepted fix replaced `workflow_dispatch` with `repository_dispatch`, and the reasoning is now a permanent comment block in `.github/workflows/continuous-improvement.yml` headed `TRUST BOUNDARY (revised after two incorrect attempts...)`.

The accepted fix is **correct**, verified against primary documentation — see § External evidence.

## Candidate models / hypotheses

- **Candidate A — distributed ownership only.** Security is a cross-cutting quality attribute owned by existing domain, process, and infrastructure owners. `.github/SECURITY.md` plus normal engineering controls are the policy surface. No new structure.
- **Candidate B — cross-cutting Security Assurance responsibility.** Domain semantics stay put; a dedicated cross-cutting surface owns threat-model discipline, readiness criteria by exposure class, verification strategy, supply-chain/release assurance, vulnerability lifecycle, and recurring security-control evidence.
- **Candidate C — standalone Security architecture/delivery track.** Security gets its own architectural and delivery responsibility, possibly including shared identity/authn/authz/trust infrastructure and cross-domain security contracts.
- **Candidate D — A plus a bounded assurance *policy surface*, with no new owner.** Every ownership assignment under A is retained unchanged. What is added is *artifacts and routing*, not a role: a maintained security requirement/criteria set, an exposure-class readiness statement, and a working private disclosure path — each owned by an existing owner. This candidate was not in the handoff list; it emerged from the evidence and is materially distinct from B, because B creates a responsibility-holder and D creates only documents that existing holders maintain.

## External evidence

Only load-bearing material is recorded. Sources that would not have changed a candidate, proving case, or confidence were deliberately not gathered — see § Confidence and limitations.

### NIST SP 800-218, Secure Software Development Framework (SSDF) Version 1.1, February 2022

**Verification status: fetched and text-extracted during this investigation** from `https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-218.pdf` (36 pages); designation, version and date independently confirmed at `https://csrc.nist.gov/pubs/sp/800/218/final`.

Verbatim, load-bearing:

- **PO.2 — "Implement Roles and Responsibilities":** "Ensure that everyone inside and outside of the organization involved in the SDLC is prepared to perform their SDLC-related roles and responsibilities throughout the SDLC." **PO.2.1:** "Create new roles and alter responsibilities for existing roles *as needed* to encompass all parts of the SDLC." Its **Example 2** is: "Integrate the security roles into the software development team."
- **PO.1 — "Define Security Requirements for Software Development":** "Ensure that security requirements for software development are known at all times so that they can be taken into account throughout the SDLC…"
- **PO.4 — "Define and Use Criteria for Software Security Checks":** "…by defining and using criteria for checking the software's security during development." **PO.4.1:** "Define criteria for software security checks and track throughout the SDLC."
- **PW.1.1:** "Use forms of risk modeling – such as threat modeling, attack modeling, or attack surface mapping – to help assess the security risk for the software."
- **RV — "Respond to Vulnerabilities":** RV.1 "Identify and Confirm Vulnerabilities on an Ongoing Basis"; **RV.1.1** "Gather information from software acquirers, users, and public sources on potential vulnerabilities… and investigate all credible reports"; RV.2 "Assess, Prioritize, and Remediate Vulnerabilities"; RV.3 "Analyze Vulnerabilities to Identify Their Root Causes".

**What transfers:** SSDF's own structure separates *defining security requirements and check criteria* (PO.1, PO.4 — artifacts) from *assigning roles* (PO.2 — responsibility). Arcogine is strong on PO.2 and weak on PO.1/PO.4. That is precisely the A-versus-D distinction, arrived at independently from repository evidence and corroborated here.

**What does not transfer:** SSDF is written for organizations, and its practice set presumes a scale Arcogine does not have. It establishes *possibility and recognized practice*, never *necessity* for Arcogine. The document says so itself of its examples: "No examples or combination of examples are required, and the stated examples are not the only feasible options." PO.2.1's qualifier "as needed" and Example 2's "integrate the security roles into the software development team" are the opposite of a mandate to create a separate security function — which is why this source counts **against** Candidate C rather than for it.

### GitHub Actions event documentation

**Verification status: fetched during this investigation** (`docs.github.com`, "Events that trigger workflows", September 2026).

For `repository_dispatch`: "This event will only trigger a workflow run if the workflow file exists on the default branch," with `GITHUB_SHA` documented as "Last commit on default branch" and `GITHUB_REF` as "Default branch." Unlike `workflow_dispatch`, an alternate ref cannot be specified, so the default branch's copy of the workflow file is what runs. For `workflow_dispatch`, the file must exist on the default branch for the trigger to be available, but the run executes against the caller-selected `ref`.

**What transfers:** this confirms the platform invariant PR #294 relied on. The repository's claim in `continuous-improvement.yml` — that `repository_dispatch` is a platform-enforced default-branch guarantee whereas `workflow_dispatch` is not — is **accurate**. The remediation was technically correct, not merely plausible.

**What does not transfer:** nothing about whether the *process* that produced the fix was efficient. It took three falsified attempts.

### GitHub private vulnerability reporting

**Verification status: search-verified during this investigation**; the feature page is `https://docs.github.com/en/code-security/security-advisories/working-with-repository-security-advisories/configuring-private-vulnerability-reporting-for-a-repository`. Owners/administrators of **public** repositories can enable it; researchers then get a "Report a vulnerability" button on the repository's Advisories page, and administrators are notified. `alaiba/arcogine` is public, so this is available.

**What transfers:** a concrete, zero-cost, correctly-scoped alternative to the current public-issue instruction exists on the platform Arcogine already uses. **What does not transfer:** enabling it is a configuration change, not an architecture decision, and it does not by itself constitute a triage or remediation process.

### Deliberately not gathered

SLSA, OpenSSF, CISA secure-by-design, OWASP ASVS/API Security/SAMM, IEC 62443, and NIST SP 800-82 were **not** used as load-bearing evidence. Each would speak to a context Arcogine has not entered (no release channel exists at all; no actuation exists; no hosted service exists), so none could discriminate between the candidates *today*. Naming them as requirements would be exactly the compliance theater the brief forbids. They are recorded in § Follow-up triggers as lenses to consult when a concrete trigger fires — not as pending obligations.

## Security-context and trust-boundary map

| # | Context | Protected asset / consequence | Trust boundary | Threat source | Current owner | Current controls | Known limits | What changes when more consequential |
|---|---|---|---|---|---|---|---|---|
| 1 | Local single-user simulation | Local user's own files and CPU | None crossed — author, operator and subject are one person | Essentially none | Product/interface owners | Semantic validation; `127.0.0.1:3000` default bind | No authn/authz/encryption, deliberately | A second principal appears → context 2 or 4 |
| 2 | Simulation deliberately exposed to a network | Availability and integrity of the single shared sim state | HTTP boundary between operator and an untrusted client | Any network-reachable party | Product/interface owners; `.github/SECURITY.md` for guidance | 1 MiB body cap; SSE semaphore 64; CORS via env; price/economy bounds; CSP on web image | No authn; **one global `SimThread` singleton**; no cost bounds on scenarios; hardening list omits all of the above | Authn, abuse controls and per-principal isolation stop being optional |
| 3 | Repository / development / CI control plane | Integrity of `main`, of releases, and of the merge invariant | PR-author vs. trusted-`main` execution | A contributor, a compromised action, an agent with repo authority | PR Reviewer; dependency-maintainer; CODEOWNERS; `main` ruleset | SHA-pinned actions; checksum-verified scanners; `contents: read` default; `repository_dispatch` trust boundary; fail-closed `gate`; active ruleset | Four activation conditions unverified by anything; condition-4 effect at `count: 0` unestablished; scheduled-scan failures unrouted | More contributors/agents → self-approval and provenance risk grows |
| 4 | Hosted / multi-user, no actuation | Other users' data, sessions, and fair use | Tenant-to-tenant, and user-to-service | Any registered or anonymous user | **Open** — register's actor-identity question; explicitly *not* Operational | None — no user concept exists | Singleton state makes this structurally impossible today | Identity, authority, isolation and data protection become product architecture |
| 5 | Ingesting untrusted or independently authoritative external data | Correctness and authenticity of Arcogine's interpretation | Arcogine vs. an independently authoritative source | A malicious or faulty external source | Operational §8 (authenticity/provenance); ADR-0012 (semantic authority) | ADR-0012 keeps external formats as projections; ADR-0013 forbids inferring identity at ingestion | ADR-0012 covers semantics, **not** parser/resource safety | Ingestion hardening becomes a named requirement |
| 6 | Production-consequential Operational execution | Money, machines, orders — irreversible outside Arcogine | Arcogine vs. physical/financial reality | A compromised peer, target, credential or command path | Operational §5/§6/§16 | Architecture requirements only; nothing implemented | Explicitly not implemented, and explicitly stated as such | Everything in §16 becomes testable acceptance criteria |

The six contexts are deliberately not collapsed. Context 4 in particular is *not* Operational's, and Operational §5 says so.

## Coverage and gap matrix

Status key: **CN** covered now · **OFR** owned future responsibility (correctly deferred) · **AG** assurance gap · **IG** implementation gap · **OG** ownerless gap · **AMB** ownership ambiguity · **ND** intentional deferral / non-goal.

| Concern | Context | Current authority/owner | Current control/evidence | Status | Gap | Trigger | Recommended destination |
|---|---|---|---|---|---|---|---|
| Authentication / identity verification | 2,4,6 | Operational §5 (consequential); **open** (generic) | Architecture requirement only | OFR | none | Network exposure to untrusted principals; hosted use | Existing register question |
| Authorization / capability / least privilege | 2,4,6 | Operational §5; overview attribution boundaries | Requirement only | OFR | none | Same as above | Existing register question |
| Credential / key / trust lifecycle | 6 | Operational §5, §16.9 | Requirement only | OFR | none | First live adapter | ARCHITECTURE/ADR when adapter is admitted |
| Transport integrity / confidentiality | 2,6 | `.github/SECURITY.md` (guidance); Operational §5 | "Place it behind a reverse proxy" | CN | none | — | NO ACTION |
| API / browser attack surface | 2 | Product/interface owners | Body cap, SSE cap, CORS, CSP, input bounds; `ApiSmokeTest` | **AG + IG** | requirement set missing; chunked-body bypass | Already live | DEVELOPMENT/ASSURANCE POLICY |
| Shared-state isolation & abuse resistance | 2,4 | Product/interface owners | None — `SimThread` is a singleton | **AG** | hardening guidance omits it entirely | Already live whenever exposed | SECURITY POLICY |
| Untrusted scenario/model/config input | 2,5 | ADR-0012 (semantics); product/interface (safety) | Referential + range validation; no cost bounds | ND→AG | correct today; misdescribed for context 2 | Network exposure | SECURITY POLICY (state the limit) |
| Dependency vulnerability management | 3 | dependency-maintainer agent; CI | Trivy SBOM + image, `npm audit`, daily re-scan, reasoned `.trivyignore` | CN | — | — | NO ACTION |
| Unrouted scheduled-scan findings | 3 | **ambiguous** | Daily scan fails; no documented responder | **AMB** | detection automated, response not routed | Already live | DEVELOPMENT/ASSURANCE POLICY |
| SBOM / dependency provenance | 3,8 | CI (generates SBOM for scanning) | CycloneDX SBOM generated, not published | ND | none — no release channel exists | First distributed release | NO ACTION now |
| Container / base-image security | 3 | infra owners | `apk upgrade`; Trivy image scan; deliberate nginx sourcing | CN* | no `USER` directive (root); no misconfig scan | Third-party deployment | EXISTING PLANNING OWNER on trigger |
| Source / build / release provenance, signing | 8 | — | None | ND | none — nothing is released | First distributed release | NO ACTION now |
| CI / Actions authority, third-party action trust | 3 | PR Reviewer; CODEOWNERS | SHA-pinned actions; `contents: read`; `repository_dispatch` boundary; checksum-verified tools | CN | — | — | NO ACTION |
| Merge-invariant enforcement (GitHub-side) | 3 | `reviewing.md` activation checklist | Ruleset active; 4 conditions stated | **AG** | nothing verifies them; condition-4 effect unknown | Already live | DEVELOPMENT/ASSURANCE POLICY |
| Secret scanning | 3 | CI | Gitleaks, always runs, `fetch-depth: 0` | CN | — | — | NO ACTION |
| Dependency update automation | 3 | Dependabot config | Weekly gradle/npm/actions, grouped | CN | Dependabot **security** alerts not verifiable | — | see limitations |
| Threat modeling / abuse-case analysis | all | **none** | `threat` appears in exactly one file repo-wide | **AG** | no artifact; three falsified attempts in #294 | Already live | DEVELOPMENT/ASSURANCE POLICY |
| Security regression / negative testing | 2 | Product/interface owners | Real tests exist in `ApiSmokeTest` | CN* | traced to a non-existent spec | Already live | DEVELOPMENT/ASSURANCE POLICY |
| SAST / DAST / fuzzing | 2 | — | None | ND | none at current surface | Untrusted-input exposure | NO ACTION now |
| Audit / security-event evidence | 6 | Operational §16; Governance | Requirement only | OFR | none | First live adapter | ARCHITECTURE/ADR on trigger |
| Vulnerability reporting / disclosure | 3 | `.github/SECURITY.md` | **Public issue** instruction on a public repo | **IG** | no private channel | Already live | SECURITY POLICY |
| Vulnerability triage / remediation / verification | 3 | dependency-maintainer (deps only) | Advisory-analysis contract | AG | no path for a *reported* (non-dependency) vuln | Already live | SECURITY POLICY |
| Incident / recovery expectations | 6 | Operational §16 | Requirement only | OFR | none | First live adapter | NO ACTION now |
| External observation authenticity | 5,6 | Operational §8; ADR-0013 | Accepted identity/non-inference rules | OFR | none | First adapter | NO ACTION now |
| Consequential command safety | 6 | Operational §6, §16 | Requirement only | OFR | none | First adapter | NO ACTION now |
| Environment / deployment separation | 6 | Operational §16.10; Charter "reality is explicit" | Requirement only | OFR | none | First adapter | NO ACTION now |
| Agents/automation holding repository authority | 3 | AGENTS.md; CODEOWNERS; ruleset | `current_user_can_bypass: never`; commit-identity rule | CN* | activation condition 2 verified only for this token | More agent identities | DEVELOPMENT/ASSURANCE POLICY |

**No row is classified `OG` (ownerless).** That is the central negative result: across every material concern the repository makes relevant, there is no body of security work lacking a credible durable owner.

## Proving cases

| # | Case | A | B | C | D |
|---|---|:--:|:--:|:--:|:--:|
| 1 | Critical dependency CVE, no source change | partial | pass | pass (redundant) | **pass** |
| 2 | Workflow obtains write authority | partial | pass | pass (redundant) | **pass** |
| 3 | Simulation API deliberately network-exposed | **fail** | pass | pass (over-reaches) | **pass** |
| 4 | Hosted multi-user, no actuation | pass | pass | **fail** | **pass** |
| 5 | Untrusted scenario/config/model artifact | partial | pass | pass (over-reaches) | **pass** |
| 6 | First live industrial adapter | pass | partial | **fail** | **pass** |
| 7 | Credential / trust root compromised or expires | pass | pass | **fail** | **pass** |
| 8 | Release/container deployed by another organization | partial | pass | pass (premature) | **pass** |
| 9 | Vulnerability privately reported | **fail** | pass | pass (redundant) | **pass** |

**Case 1 — CVE with no source change.** Detection is genuinely solved: the daily scheduled re-scan exists precisely for this, and CVE-2026-75595 was in fact caught. Urgency and exception policy are solved by `.trivyignore`'s shipped-vs-not-shipped rule. What is *not* solved is routing: the #294 evidence shows the fix was carried by whichever unrelated PR happened to trip the gate. A's owners each did their job; nobody owned the finding itself. D fixes this with one sentence of routing policy, not a new role. C would create a security owner to receive an alert that a documented routing rule handles just as well.

**Case 2 — workflow write authority.** A detected it (`SECURITY_AUTHORITY` is already a category, security/authority is already High risk), assigned it, remediated it, and the fix is verifiably correct against GitHub's documentation. But it took three falsified attempts, each a plausible-looking mitigation. That is the signature of missing threat-model discipline (SSDF PW.1.1), not of missing ownership — a dedicated owner would have made the same three attempts without a discipline for reasoning about where workflow definitions are sourced from. D adds the discipline; C adds a person.

**Case 3 — deliberate network exposure. This is where A actually fails.** `.github/SECURITY.md` tells an operator to set bind, CORS, TLS, audit and log level. An operator who does all five still has: no authentication, one global simulation state shared across all callers, unbounded scenario cost, and a body cap bypassable without `Content-Length`. The policy does not say which requirements are hardening guidance and which are absent product architecture, so the honest answer to "at what point does authentication become required?" is currently unrecorded. This failure is an *artifact* failure — A's owner for this surface (product/interface) is correct and unambiguous.

**Case 4 — hosted multi-user, no actuation. This is where C fails hardest.** Identity, tenant authority, data protection and abuse controls here are not Operational's, and Operational §5 explicitly refuses them ("must not be forced into Operational merely because the first real-world consumer needs it"). A standalone Security track would be the obvious place to dump them — and would thereby take ownership of generic actor/capability semantics that the register has deliberately kept open pending a concrete consumer, inverting an ownership boundary the repository has already reasoned about carefully. A, B and D all correctly leave this with the open actor-identity question until a consumer exists.

**Case 5 — untrusted artifact.** ADR-0012 owns the semantics; parser/resource/path safety is unowned in the ADR but not unowned in the repository — it belongs to the interface that accepts the artifact. Today the local user authors their own scenarios, so no privilege boundary is crossed and inaction is correct. The gap is only that this correctness is contingent on context 1 and nothing records the contingency. Again: artifact, not owner.

**Case 6 — first live adapter.** A and D both keep verified trust, command safety, observation authenticity, reconciliation and fail-safe behavior with Operational, which is right. B is partial: a cross-cutting assurance *responsibility* would be tempted to co-own the adapter's security acceptance criteria, which §5/§16 already own. C fails outright by duplicating Operational. This case is the strongest argument for D over B — D adds documents, which cannot drift into co-ownership the way a responsibility-holder can.

**Case 7 — credential/trust compromise or expiry.** Operational §5 and §16.9 already own the semantics ("Credential or trust loss has explicit operational consequences"). The cross-cutting part — rotation mechanics, secret storage — is explicitly deferred as an implementation choice by §5 and §17. Nothing here is ownerless. C fails by re-owning it.

**Case 8 — third-party deployment.** No release channel exists; `dist/` is local and gitignored, no workflow publishes anything, nothing is signed, and the SBOM is generated only to be scanned. So provenance/SBOM/signing evidence is correctly absent, not missing. A is partial only because nothing records *what would become required* at that trigger. D records the trigger; C would build the machinery years early.

**Case 9 — privately reported vulnerability. A's clearest failure.** On a public repository, the documented reporting path is a public issue. A reporter who follows the policy discloses the vulnerability to the world. Neither triage nor verification is defined for anything that is not a dependency update. The fix is a policy edit plus a settings toggle — again an artifact, not an owner.

**Aggregate reading.** C fails cases 4, 6 and 7 by inverting ownership boundaries the repository has deliberately drawn, and adds nothing to 1, 2 and 9 that routing policy does not. A fails 3 and 9 and is only partial on 1, 5 and 8 — and *every one of those failures is an absent document, none is an absent owner*. B passes broadly but creates a responsibility-holder whose only durable output would be the documents D specifies, while carrying real risk of drifting into co-ownership (case 6). D passes every case at the smallest cost.

## Surviving invariants

The smallest rules that held across all nine cases:

1. **Security assurance may be cross-cutting even when security semantics remain owned by the domain that owns the consequential behavior.** The evidence supports this, so it is retained — but in its *artifact* form, not its *role* form. What is cross-cutting is the requirement set, the criteria, and the routing; the responsibility stays with the existing owner. The stronger reading — that a cross-cutting assurance *owner* is warranted — did not survive case 6.
2. **An absent control is a gap only when a trust boundary is actually crossed.** Contexts 1 and 6 both have almost no implemented controls, and only one of them is a problem.
3. **Consequential-execution security semantics belong to the owner of the consequence, never to a security surface.** Cases 6 and 7.
4. **Generic actor/authority semantics do not become Operational's merely because Operational needs them first** — and equally do not become a Security owner's. Case 4. This is already repository doctrine; the investigation confirmed rather than established it.
5. **A security control whose requirement is unrecorded decays silently.** `ApiSmokeTest`'s §3.x references are the repository's own worked example: the tests still pass, and the specification they implement has vanished.
6. **Detection without routed response is not coverage.** Case 1.

## Adversarial analysis (self-administered — not independent)

Attacks attempted on this report's own conclusion, per §9's failure-mode list:

- **Am I under-reading the gaps to protect a "no new track" conclusion?** The strongest counter-case is #294's three failed rounds plus five separate artifact gaps — a reader could fairly call that a pattern justifying a security owner. I do not think it survives, because each gap's *fix* is a document maintained by an existing owner, and no proving case produced work that an existing owner could not coherently accept. But this is the report's most contestable judgment and an independent reviewer should press it hardest.
- **Am I over-reading SSDF?** Possibly. PO.1/PO.4 map onto my recommendations almost too neatly. I have tried to control for this by deriving the finding from repository evidence first (the dangling §3.x references) and using SSDF only to corroborate, and by quoting SSDF's own disclaimer that its examples are not required. An independent reviewer should check whether I selected the practices that fit.
- **Is Candidate D a renamed Candidate B?** This is the sharpest structural objection. My defence is case 6: B creates a responsibility-holder that can drift into co-owning adapter security criteria; D creates documents that cannot. If a reviewer judges that distinction too fine to be operationally real, the conclusion should shift to B-minimal, and the recommendations would barely change — which is itself a reason to hold confidence at moderate rather than high.
- **Is the singleton/`SimThread` finding overstated?** It is `Repository fact` that the component is a singleton and that no user concept exists. The step from there to "any network-reachable client can disrupt another's session" is `Inference`, not executed. Labeled as such.
- **Stale baseline?** Re-checked at write time: live `main` is still `950a1974`. The handoff's stated baseline `5d010e2a` was already stale on arrival and its "open PR #294" premise was false — #294 merged from exactly that base. Both corrected here rather than inherited.
- **Possibility treated as necessity?** Deliberately guarded: SLSA, IEC 62443, OWASP and CISA were excluded precisely because invoking them would have manufactured necessity from possibility.
- **A future feature smuggled in as present necessity?** Checked each recommendation. The four "now" items are all provable against the *current* repository and the *current* public-repo, network-exposable surface. Nothing in the "now" list presumes hosting, actuation, or releases.

## What did not survive

- **Candidate C (standalone Security track)** — rejected. It fails cases 4, 6 and 7 by taking ownership the repository has deliberately placed elsewhere or deliberately left open, and it is redundant on the cases it passes. Useful negative knowledge: *the topic being security does not make the ownership question different from any other premature-abstraction question* — the same "no shared abstraction without a concrete consumer" discipline that produced ADR-0012 §15 and the Factory-resource "keep collapsed" conclusion applies unchanged.
- **Candidate B as stated (a cross-cutting assurance responsibility-holder)** — not rejected, but not selected. Its durable output is D's artifacts; its extra element is a role whose only proving-case effect was a *risk* (case 6 co-ownership drift).
- **The framing that PR #294 demonstrates an ownership failure** — falsified. #294 demonstrates the existing ownership model detecting a genuine security-authority defect, refusing two insufficient fixes, and landing a fix that is correct against primary platform documentation. It is evidence *for* distributed ownership. What it exposes is a missing *discipline* (threat modeling), which is why three plausible-but-wrong attempts were made.
- **The assumption that "no authentication" is Arcogine's main current security gap** — falsified. It is documented, deliberate, correctly owned, and appropriate for context 1. The real current-surface gaps are the undocumented shared-singleton exposure and the public vulnerability-reporting instruction.
- **The assumption that scanner/CI coverage was thin** — falsified by inspection. Checksum-verified scanner binaries, SHA-pinned actions, a fail-closed gate, a daily re-scan, and a reasoned CVE exception policy are stronger than the security policy that describes them.

## Confidence and limitations

**Surfaces that could not be inspected**, recorded rather than inferred:

- `GET /repos/…/vulnerability-alerts` and `/automated-security-fixes` — HTTP 403 from the agent proxy. **Whether Dependabot security alerts and automated security updates are enabled is unverified.** This matters for case 1 and should be checked by someone with direct repository access.
- `GET /repos/…/actions/permissions` — 403. Org/repo-level allowed-actions policy unverified.
- `GET /repos/…/environments` — 403. Environment protection rules unverified.
- `GET /repos/…/branches/main/protection` — 403 ("Resource not accessible by integration"); the ruleset API was readable and was used instead. Classic branch-protection settings, if any coexist, are unverified.
- `security_and_analysis` came back `null` on the repository object, which is not evidence that features are disabled.
- **Whether GitHub enforces `require_code_owner_review: true` when `required_approving_review_count: 0`** — not established. Load-bearing for activation condition 4.
- The chunked-body bypass was reasoned from source, **not executed** against a running server.
- No Java or frontend gate was run; this investigation made no code change, so none was required.

**What would change the conclusion:**

- Evidence that an existing owner has actually *refused* or *been unable to accept* a security responsibility → would move the answer toward B or C.
- A concrete admitted consumer for hosted multi-user operation → would promote case 4 from deferred to live and make the actor-identity question critical-path.
- Discovery that the four activation conditions are *not* in fact effective → would sharpen the control-plane item from assurance gap to implementation gap, without changing the ownership conclusion.
- An independent adversarial reviewer judging the B/D distinction operationally meaningless → would collapse D into B-minimal; recommendations would be substantially unchanged.

## Unresolved unknowns

1. Does `require_code_owner_review` bind when the required approving review count is zero?
2. Are Dependabot security alerts/updates enabled?
3. In a single-maintainer repository where agents commit as the human owner, can activation condition 2 ("the identity available to coding agents cannot… satisfy its own CODEOWNERS review requirement") be satisfied at all, or is it structurally unachievable until a second trusted identity exists? This is a genuine open question, not a defect claim — git authorship and GitHub review identity are distinct, and this investigation did not establish which identity agents can review under.
4. What became of the §3.x security-requirement numbering — dropped deliberately, or lost?

Items 1, 2 and 4 are verification tasks, not research questions. Item 3 may be a real bounded question — see below.

## Durable consequences

Recommendations, by trigger. **None of these is performed by this report**, and no implementation planning is admitted here.

### Now

1. **Replace the public-issue vulnerability instruction with GitHub private vulnerability reporting**, and state a minimal triage/remediation/verification expectation for a reported (non-dependency) vulnerability. → `SECURITY POLICY` (+ one repository setting).
2. **Record the security requirement/criteria set that `ApiSmokeTest`'s §3.x labels refer to**, or explicitly retire the numbering. Whichever is chosen, the executable checks and their stated requirements must point at each other. → `DEVELOPMENT/ASSURANCE POLICY`.
3. **Correct and complete `.github/SECURITY.md`'s network-exposure section** so it states the implemented controls (body cap, SSE cap, CORS, input bounds, web CSP) *and* the structural limits (single shared `SimThread`, no user concept, no scenario cost bounds), and separates hardening guidance from absent product architecture. → `SECURITY POLICY`.
4. **Route unattended security findings.** One sentence naming who responds to a failed daily scheduled scan and within what expectation, distinguishing it from a CVE that surfaces inside someone's PR. → `DEVELOPMENT/ASSURANCE POLICY`.
5. **Verify the four merge-gate activation conditions and record the result**, including the `count: 0` question. → `DEVELOPMENT/ASSURANCE POLICY`.
6. **Fix or document the `MaxBodySizeFilter` chunked-request path.** → `PRODUCT` (small implementation change, ordinary review).

### Before intentional network/public exposure

7. Decide and record, as readiness criteria rather than prose, at what exposure class authentication, per-principal isolation, scenario cost bounds and audit stop being optional. → `DEVELOPMENT/ASSURANCE POLICY`, informed by Operational §5 but **not** owned by Operational.

### Before hosted / multi-user operation

8. Treat the register's existing actor-identity question as triggered; it does not need a duplicate security question. → `RESEARCH` (existing entry).

### Before production-consequential Operational execution

9. No new action. Operational §5/§6/§16 already own this and already state it is unimplemented. Turning those principles into acceptance criteria is the readiness track's existing responsibility. → `NO ACTION` now; `EXISTING PLANNING OWNER` at the trigger.

### Triggered later by a concrete consumer

10. First distributed release → SBOM publication, artifact integrity, build provenance (consult SLSA/OpenSSF *then*). First third-party deployment → non-root containers, container misconfiguration scanning. Untrusted-input exposure → SAST/fuzzing consideration. → `NO ACTION` now.

**Reusable assets worth preserving** if this workspace is retired: the six-context trust-boundary map (it is what made "absent control ≠ gap" decidable); the §3.x dangling-reference example as a concrete instance of invariant 5; and the negative knowledge that #294 is evidence *for* distributed ownership rather than against it.

## Research-register implications

**No new research question is proposed for security ownership itself.** The question was answered; its consequences are policy and documentation edits, not further investigation. Creating a register entry would manufacture research where a reconciliation is what is needed.

Two candidates were considered and both rejected as duplicates: a security-identity question (duplicates the existing actor-identity `CANDIDATE`) and an operational-security question (duplicates the existing Operational boundaries `CANDIDATE`).

One item *may* qualify as genuinely independent, offered for an explicit decision rather than added:

- **Question:** In a single-maintainer repository where coding agents commit as the human owner, what independent-approval property can the merge gate actually guarantee, and what is the smallest arrangement that makes it true?
- **Area:** Repository control plane / delivery process
- **Priority:** Medium
- **State:** `CANDIDATE`, not `READY` — it is not yet established that this is a real limitation rather than a confusion between git authorship and GitHub review identity (unresolved unknown 3). Verification should precede admission.
- **Expected destination:** `docs/development/reviewing.md`, or no action if verification shows the property already holds.

**The register has not been modified by this run.**

## Implementation implication

**No implementation is admitted.** Recommendation 6 is a small ordinary product fix that needs no planning admission. Recommendations 1–5 and 7 are documentation/policy/configuration changes. Nothing here creates a Security module, planning track, or delivery coordinate.

## Follow-up triggers

Reopen or extend this question when: a concrete hosted/multi-user consumer is admitted; the first live operational adapter is proposed; Arcogine first publishes a release or container for external consumption; a second trusted maintainer identity exists; or a security incident occurs that an existing owner demonstrably could not receive. At the release and actuation triggers, the deliberately excluded lenses (SLSA/OpenSSF; IEC 62443, NIST SP 800-82) become worth verifying with precise designations.

## Sources

- NIST SP 800-218, *Secure Software Development Framework (SSDF) Version 1.1: Recommendations for Mitigating the Risk of Software Vulnerabilities*, February 2022. Practices PO.1, PO.2/PO.2.1, PO.4/PO.4.1, PW.1.1, RV/RV.1.1/RV.2/RV.3. Landing page `https://csrc.nist.gov/pubs/sp/800/218/final`; PDF `https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-218.pdf`. **Verified in session** (fetched; text extracted; quoted passages read directly).
- GitHub Docs, *Events that trigger workflows* — `repository_dispatch` and `workflow_dispatch` sections, September 2026. `https://docs.github.com/en/actions/reference/workflows-and-actions/events-that-trigger-workflows`. **Verified in session.**
- GitHub Docs, *Configuring private vulnerability reporting for a repository*, September 2026. `https://docs.github.com/en/code-security/security-advisories/working-with-repository-security-advisories/configuring-private-vulnerability-reporting-for-a-repository`. **Verified in session** (search-level verification of availability and enablement path; the configuration page itself was not individually fetched).
- GitHub REST API, `GET /repos/alaiba/arcogine/rulesets/14822987` and `GET /repos/alaiba/arcogine`, read at baseline. **Verified in session.**
- Deliberately excluded as non-load-bearing at the current boundary: SLSA, OpenSSF, CISA secure-by-design, OWASP ASVS / API Security / SAMM, IEC 62443, NIST SP 800-82. **Not verified, and not relied upon.**
