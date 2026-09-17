# Repository snapshot

Arcogine's Repomix snapshot is a whole-repository, point-in-time corpus for ChatGPT/project-source retrieval. It can serve either as the exact repository-content view for its recorded commit or as a reusable baseline for a later descendant revision when live GitHub can establish the complete changed-path delta. This avoids discarding an otherwise-current repository corpus merely because a small number of paths changed after the snapshot was generated.

The snapshot is also the **required** repository-content corpus for formal Consistency reviews in ChatGPT. That stricter workflow remains fail-closed unless the snapshot is exact current canonical `main`; see the exact-current-main rules below.

From a clean checkout of current `main`, run:

```bash
./arcogine snapshot
```

The command writes `logs/arcogine-main-<short-sha>.xml`. Upload that file to the Arcogine ChatGPT project before using it as project-source repository content or running a formal Consistency review.

## ChatGPT / project-source retrieval protocol

For ordinary ChatGPT/project-source retrieval, treat the snapshot commit as a baseline `S` and resolve the revision whose repository content the task actually needs as target `T`. For current-main grounding, `T` is live canonical `main`; for work on another branch, `T` is that branch's current head.

1. Read the full commit SHA recorded in the snapshot header (`S`).
2. Resolve the target revision SHA (`T`) through live GitHub.
3. When `S == T`, use the snapshot directly for repository-content reads and searches. Do not redundantly fetch individual files through GitHub merely to re-establish identical content.
4. When `S != T`, use one live repository compare to determine whether `S` is an ancestor of `T` and to obtain the complete `S..T` changed-path set.
5. When `S` is an ancestor of `T` and the comparison provides a complete, usable delta, keep the snapshot as the primary corpus for unchanged paths. Use live target content only for affected paths, including additions, modifications, deletions, and renames; stale snapshot content for an affected path must not be treated as target-revision content.
6. When ancestry cannot be established, the compare is incomplete/too large/unavailable, or the delta cannot safely identify all affected paths, do not synthesize a target view from the snapshot. Use live repository evidence for that target or refresh the snapshot.
7. Continue using live GitHub for mutable state such as pull requests, reviews, CI/checks, issues, mergeability, branch heads, repository writes, and history/compare evidence.
8. For a long-running task whose conclusion depends on current repository state, resolve `T` again before finalizing. If it moved, reconcile the additional delta rather than silently reporting against an older target.

The decision is:

```text
snapshot commit S
        |
target revision T
        |
        +-- S == T ----------------------> snapshot is the target content corpus
        |
        +-- S is ancestor of T
        |      + complete S..T delta ----> snapshot for unchanged paths
        |      |                           live target content for affected paths
        |      |
        |      + unsafe/incomplete delta -> use live target evidence or refresh
        |
        +-- S is not ancestor of T ------> use live target evidence or refresh

formal Consistency review: S must equal live main exactly, otherwise INCOMPLETE
```

This protocol is specifically about attached project-source retrieval; it does not change Arcogine's ordinary repository workflows.

## Exact-current-main requirement

Generation is fail-closed. It requires:

- branch `main`;
- a clean working tree;
- `origin` pointing to `alaiba/arcogine` on GitHub;
- local `HEAD` exactly equal to `git ls-remote origin refs/heads/main`.

Being merely an ancestor of canonical `main` is not sufficient for **generating** a new snapshot. If local `main` is behind, ahead/unpushed, or otherwise different, update it and retry. The direct remote lookup avoids captioning a stale locally cached state as a newly generated current-main corpus. Once generated and uploaded, however, the recorded commit may later remain useful as the baseline `S` under the ordinary retrieval protocol above.

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

The snapshot intentionally does not include Git history/diffs. Formal Consistency review gets recency from one live GitHub compare between the previous reviewed head and the snapshot/current-main head, then searches the current corpus without a historical scope boundary. Ordinary ChatGPT tasks use live GitHub compare/history when they need to reconcile a descendant target or inspect repository evolution.

## Authority boundary

For the exact recorded commit `S`, the snapshot is authoritative for repository content. When a target `T` is a verified descendant and live GitHub supplies a complete `S..T` changed-path delta, unchanged paths remain exactly represented by the snapshot while affected paths must be taken from the live target. The snapshot alone is not authority for `T`.

The snapshot is not live authority for issues, pull requests, submitted reviews, unresolved review threads, CI/check status, mergeability, branch heads, or other mutable GitHub state; those remain GitHub-connector concerns.

The snapshot is generated output under `logs/` and is not committed.
