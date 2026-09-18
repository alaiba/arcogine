# Repository snapshot

Arcogine's Repomix snapshot is a whole-repository, point-in-time corpus for project-source retrieval. It can serve either as the exact repository-content view for its recorded commit or as a reusable baseline for a later descendant revision when live GitHub can establish the complete changed-path delta and the exact target commit. This avoids discarding an otherwise-current repository corpus merely because a small number of paths changed after the snapshot was generated.

The snapshot is also the **required baseline** for formal Consistency reviews. Consistency uses the same revision-bound retrieval protocol below: an older snapshot remains usable when one compare establishes an exact descendant target and a complete changed-path delta.

From a clean checkout of current `main`, run:

```bash
./arcogine snapshot
```

The command writes `logs/arcogine-main-<short-sha>.xml`. Upload that file to the Arcogine ChatGPT project before using it as project-source repository content or running a formal Consistency review.

## Project-source retrieval protocol

For ordinary retrieval, treat the snapshot commit as baseline `S` and identify the target repository ref from task context: normally `main`, or the relevant branch when the task concerns another branch.

1. Read the full commit SHA recorded in the snapshot header (`S`).
2. Use one live GitHub compare from `S` to the target ref, using a compare surface that also exposes the exact resolved target commit SHA (`T`). If a prior task-specific GitHub call already supplied the exact target SHA, reuse it as `T` and compare `S` directly to `T` instead of resolving the ref again.
3. If the chosen compare surface does not expose the exact resolved target SHA, do not use its changed-path set for delta-mode live reads. Prefer a compare surface that returns both `T` and the delta in one call; otherwise resolve `T` separately and repeat the compare as `S..T` before reading affected content.
4. When the compare shows no repository-content difference, use the snapshot directly for repository-content reads and searches. Do not redundantly fetch individual files through GitHub merely to re-establish identical content.
5. When `S` is an ancestor of `T`, the compare provides a complete usable changed-path delta, and exact `T` is known, keep the snapshot as the primary corpus for unaffected paths. Use live content only for affected paths and fetch it at immutable `ref=T`, never through the mutable branch ref used to initiate the compare. Treat additions, modifications, deletions, renames, and copies as affected; stale snapshot content for an affected path must not be treated as target-revision content.
6. For repository-wide or semantic searches under delta mode, search the snapshot as the baseline and reconcile the result with the affected-path set. Inspect affected content at `T` where necessary so added or modified material is not missed and removed or replaced snapshot material cannot produce false conclusions.
7. When ancestry cannot be established, the compare is incomplete/too large/unavailable, exact `T` cannot be established, or the delta cannot safely identify all affected paths, do not synthesize a target view from the snapshot. Use live repository evidence for that target or refresh the snapshot.
8. Use live GitHub separately when the task requires mutable state or history, including pull requests, reviews, unresolved threads, CI/checks, issues, mergeability, branch heads, commit/compare history, and repository writes. Do not make those calls merely to reconfirm repository content already established by the snapshot and revision-bound delta.
9. For a long-running task whose conclusion materially depends on the latest repository state, repeat the `S`-to-target compare before finalizing. If the target moved, establish the new exact `T` and reconcile the new delta before reporting a latest-state conclusion.

The decision is:

```text
snapshot commit S
        |
one compare to target ref
        |
        +-- no content difference --------> snapshot is the target content corpus
        |
        +-- descendant target, exact T known
        |      + complete delta ----------> snapshot for unaffected paths
        |      |                           affected paths read at ref=T
        |      |
        |      + unsafe/incomplete delta -> use live target evidence or refresh
        |
        +-- exact T / ancestry unavailable -> use live target evidence or refresh
```

This protocol is specifically about attached project-source retrieval; it does not change Arcogine's ordinary repository workflows.

## Reusable Project instruction block

The following block is the maintained copy-paste form of the retrieval strategy for a ChatGPT Project that has an Arcogine snapshot attached. Keep it aligned with the protocol above when that protocol changes.

```text
Prefer the project-attached `arcogine-main-<sha>.xml` snapshot as the baseline for repository-content retrieval.

At the first repository grounding of a task/session:

1. Read the snapshot's recorded full commit SHA as `S`.
2. Identify the target repository ref from task context: normally `main`, or the relevant branch when the task concerns another branch.
3. Use one live GitHub compare from `S` to that target ref, using a compare surface that also exposes the exact resolved target commit SHA as `T`. If a prior task-specific GitHub call already supplied the exact target SHA, reuse it as `T` and compare `S` directly to `T`.
4. If the chosen compare surface does not expose exact `T`, do not use its changed-path set for delta-mode live reads. Prefer a compare surface that returns `T` and the delta together; otherwise resolve `T` separately and repeat the compare as `S..T`.
5. If the compare shows no repository-content difference, use the snapshot as the repository-content source and search corpus. Do not redundantly fetch the same files through GitHub.
6. If `S` is an ancestor of `T`, exact `T` is known, and the compare provides a complete usable changed-path delta:
   - keep the snapshot as the primary corpus for unaffected paths;
   - use live target content only for affected paths, fetched at immutable `ref=T` rather than the mutable branch ref;
   - treat additions, modifications, deletions, renames, and copies as affected;
   - never use snapshot content from an affected path as evidence about `T`.
7. For repository-wide or semantic searches under delta mode, search the snapshot as the baseline and reconcile the result with the affected-path set. Inspect affected content at `T` where necessary so added or modified material is not missed and removed or replaced snapshot material cannot produce false conclusions.
8. If ancestry, exact `T`, or a complete usable delta cannot be established, use live repository evidence for the target or obtain a fresh snapshot.
9. Use live GitHub separately when the task requires mutable state or history, including pull requests, reviews, unresolved threads, CI/checks, issues, mergeability, branch heads, commit/compare history, and repository writes. Do not make those calls merely to reconfirm repository content already established by the snapshot and revision-bound delta.
10. For a long-running task whose conclusion materially depends on the latest repository state, repeat the `S`-to-target compare before finalizing. If the target moved, establish the new exact `T` and reconcile the new delta.

Formal Consistency review follows this same protocol. Because its conclusion is repository-wide, it must establish one exact target `T` and a complete target corpus; if provenance is missing/malformed or ancestry/exact `T`/a complete usable delta cannot be established, refresh the snapshot rather than recording an incomplete review.
```

## Snapshot generation requirement

Generation is fail-closed. It requires:

- branch `main`;
- a clean working tree;
- `origin` pointing to `alaiba/arcogine` on GitHub;
- local `HEAD` exactly equal to `git ls-remote origin refs/heads/main`.

Being merely an ancestor of canonical `main` is not sufficient for **generating** a new snapshot. If local `main` is behind, ahead/unpushed, or otherwise different, update it and retry. The direct remote lookup avoids captioning a stale locally cached state as a newly generated current-main corpus. Once generated and uploaded, however, the recorded commit may later remain useful as baseline `S` under the ordinary retrieval protocol above.

The generated header records repository, branch, full commit SHA, UTC generation time, pinned Repomix version, and the authority boundary. Consumers independently resolve their exact target revision and apply the retrieval protocol above. For a formal Consistency review, a behind snapshot is valid only when it can be reconciled to exact current `main` through a complete safe delta; otherwise the review is `INCOMPLETE` and the snapshot must be refreshed.

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

The snapshot intentionally does not include Git history/diffs. A Consistency review may use two compares for different purposes: `S..T` establishes the exact target corpus when the snapshot baseline is behind, while the previous reviewed head to `T` supplies recency bias. It then searches the complete target corpus without a historical scope boundary. Other tasks use live GitHub compare/history when they need to reconcile a descendant target or inspect repository evolution.

## Authority boundary

For the exact recorded commit `S`, the snapshot is authoritative for repository content. When one live compare from `S` to the target ref establishes a complete descendant delta and exact target commit `T`, unaffected paths remain exactly represented by the snapshot while affected paths must be read from immutable `T`. The snapshot alone is not authority for the later target revision, and affected-path reads through a mutable branch ref must not be mixed with a delta derived from an earlier target revision.

The snapshot is not live authority for issues, pull requests, submitted reviews, unresolved review threads, CI/check status, mergeability, branch heads, or other mutable GitHub state; those remain GitHub-connector concerns.

The snapshot is generated output under `logs/` and is not committed.
