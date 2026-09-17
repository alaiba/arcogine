# Repository snapshot

Arcogine's Repomix snapshot is a whole-repository, point-in-time corpus for project-source retrieval. It can serve either as the exact repository-content view for its recorded commit or as a reusable baseline for a later descendant revision when live GitHub can establish the complete changed-path delta. This avoids discarding an otherwise-current repository corpus merely because a small number of paths changed after the snapshot was generated.

The snapshot is also the **required** repository-content corpus for formal Consistency reviews. That stricter workflow remains fail-closed unless the snapshot is exact current canonical `main`; see the exact-current-main rules below.

From a clean checkout of current `main`, run:

```bash
./arcogine snapshot
```

The command writes `logs/arcogine-main-<short-sha>.xml`. Upload that file to the Arcogine ChatGPT project before using it as project-source repository content or running a formal Consistency review.

## Project-source retrieval protocol

For ordinary retrieval, treat the snapshot commit as baseline `S` and identify the target repository ref from task context: normally `main`, or the relevant branch when the task concerns another branch.

1. Read the full commit SHA recorded in the snapshot header (`S`).
2. Use a single live GitHub compare from `S` to the target ref. Do not separately resolve the target SHA first unless that exact SHA is independently required. If a prior task-specific GitHub call already supplied the target SHA, reuse it.
3. When the compare shows no repository-content difference, use the snapshot directly for repository-content reads and searches. Do not redundantly fetch individual files through GitHub merely to re-establish identical content.
4. When `S` is an ancestor of the target and the compare provides a complete, usable changed-path delta, keep the snapshot as the primary corpus for unaffected paths. Use live target content only for affected paths. Treat additions, modifications, deletions, renames, and copies as affected; stale snapshot content for an affected path must not be treated as target-revision content.
5. For repository-wide or semantic searches under delta mode, search the snapshot as the baseline and reconcile the result with the affected-path set. Inspect live affected content where necessary so added or modified material is not missed and removed or replaced snapshot material cannot produce false conclusions.
6. When ancestry cannot be established, the compare is incomplete/too large/unavailable, or the delta cannot safely identify all affected paths, do not synthesize a target view from the snapshot. Use live repository evidence for that target or refresh the snapshot.
7. Use live GitHub separately when the task requires mutable state or history, including pull requests, reviews, unresolved threads, CI/checks, issues, mergeability, branch heads, commit/compare history, and repository writes. Do not make those calls merely to reconfirm repository content already established by the snapshot and delta.
8. For a long-running task whose conclusion materially depends on the latest repository state, repeat the `S`-to-target compare before finalizing and reconcile any additional delta.

The decision is:

```text
snapshot commit S
        |
one compare to target ref
        |
        +-- no content difference --------> snapshot is the target content corpus
        |
        +-- S is ancestor of target
        |      + complete delta ----------> snapshot for unaffected paths
        |      |                           live target content for affected paths
        |      |
        |      + unsafe/incomplete delta -> use live target evidence or refresh
        |
        +-- ancestry not established -----> use live target evidence or refresh

formal Consistency review: S must equal live main exactly, otherwise INCOMPLETE
```

This protocol is specifically about attached project-source retrieval; it does not change Arcogine's ordinary repository workflows.

## Reusable Project instruction block

The following block is the maintained copy-paste form of the retrieval strategy for a ChatGPT Project that has an Arcogine snapshot attached. Keep it aligned with the protocol above when that protocol changes.

```text
Prefer the project-attached `arcogine-main-<sha>.xml` snapshot as the baseline for repository-content retrieval.

At the first repository grounding of a task/session:

1. Read the snapshot's recorded full commit SHA as `S`.
2. Identify the target repository ref from task context: normally `main`, or the relevant branch when the task concerns another branch.
3. Use a single live GitHub compare from `S` to that target ref. Do not separately resolve the target SHA first unless the exact SHA is independently required. If a prior task-specific GitHub call already supplied the target SHA, reuse it.
4. If the compare shows no repository-content difference, use the snapshot as the repository-content source and search corpus. Do not redundantly fetch the same files through GitHub.
5. If `S` is an ancestor of the target and the compare provides a complete usable changed-path delta:
   - keep the snapshot as the primary corpus for unaffected paths;
   - use live target content only for affected paths;
   - treat additions, modifications, deletions, renames, and copies as affected;
   - never use snapshot content from an affected path as evidence about the target revision.
6. For repository-wide or semantic searches under delta mode, search the snapshot as the baseline and reconcile the result with the affected-path set. Inspect live affected content where necessary so added or modified material is not missed and removed or replaced snapshot material cannot produce false conclusions.
7. If ancestry or a complete usable delta cannot be established, use live repository evidence for the target or obtain a fresh snapshot.
8. Use live GitHub separately when the task requires mutable state or history, including pull requests, reviews, unresolved threads, CI/checks, issues, mergeability, branch heads, commit/compare history, and repository writes. Do not make those calls merely to reconfirm repository content already established by the snapshot and delta.
9. For a long-running task whose conclusion materially depends on the latest repository state, repeat the `S`-to-target compare before finalizing and reconcile any new delta.

Formal Consistency review is the exception: its snapshot commit must exactly equal current live `main`. A missing, malformed, or stale snapshot makes the review `INCOMPLETE`; do not reconstruct that review corpus through delta reconciliation.
```

## Exact-current-main requirement

Generation is fail-closed. It requires:

- branch `main`;
- a clean working tree;
- `origin` pointing to `alaiba/arcogine` on GitHub;
- local `HEAD` exactly equal to `git ls-remote origin refs/heads/main`.

Being merely an ancestor of canonical `main` is not sufficient for **generating** a new snapshot. If local `main` is behind, ahead/unpushed, or otherwise different, update it and retry. The direct remote lookup avoids captioning a stale locally cached state as a newly generated current-main corpus. Once generated and uploaded, however, the recorded commit may later remain useful as baseline `S` under the ordinary retrieval protocol above.

The generated header records repository, branch, full commit SHA, UTC generation time, pinned Repomix version, and the authority boundary. A formal Consistency review independently resolves live GitHub `main` and requires exact equality with that recorded commit. A missing, malformed, or stale snapshot makes the review `INCOMPLETE`; the reviewer does not reconstruct repository content through a baseline-plus-delta fallback.

## Corpus shape

The snapshot is optimized for semantic review and retrieval rather than minimum token count:

- XML output;
- full text content rather than code compression;
- source comments retained;
- directory structure and files retained;
- generic Repomix summary omitted;
- generated/dependency material excluded through the repository's `.gitignore`;
- Repomix default/dot-ignore layers disabled so exclusions remain repository-visible and auditable;
- explicit secret-like exclusions retained;
- Repomix security checking enabled.

The wrapper also prepends a `<tracked_files>` manifest generated from `git ls-files`. It enumerates every tracked path even when Repomix does not include a file's contents (for example, binary assets). This lets a retrieval-oriented agent reason about path existence, links, packaging, and repository shape without requiring binary contents in the prompt.

The snapshot intentionally does not include Git history/diffs. Formal Consistency review gets recency from one live GitHub compare between the previous reviewed head and the snapshot/current-main head, then searches the current corpus without a historical scope boundary. Other tasks use live GitHub compare/history when they need to reconcile a descendant target or inspect repository evolution.

## Authority boundary

For the exact recorded commit `S`, the snapshot is authoritative for repository content. When one live compare from `S` to the target ref establishes a complete descendant delta, unaffected paths remain exactly represented by the snapshot while affected paths must be taken from the live target. The snapshot alone is not authority for the later target revision.

The snapshot is not live authority for issues, pull requests, submitted reviews, unresolved review threads, CI/check status, mergeability, branch heads, or other mutable GitHub state; those remain GitHub-connector concerns.

The snapshot is generated output under `logs/` and is not committed.
