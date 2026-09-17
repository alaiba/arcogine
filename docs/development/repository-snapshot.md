# Repository snapshot

Arcogine's Repomix snapshot is a whole-repository, point-in-time corpus for ChatGPT/project-source use. In ChatGPT, it can replace repeated GitHub file/search calls when it represents the exact current canonical `main`, allowing repository-content grounding to come from one searchable artifact while live GitHub remains responsible for mutable delivery state.

The snapshot is also the **required** repository-content corpus for formal Consistency reviews in ChatGPT. That stricter workflow remains fail-closed when a current snapshot is unavailable; see the exact-current-main rules below.

From a clean checkout of current `main`, run:

```bash
./arcogine snapshot
```

The command writes `logs/arcogine-main-<short-sha>.xml`. Upload that file to the Arcogine ChatGPT project before using it as project-source repository content or running a formal Consistency review.

## ChatGPT / project-source retrieval protocol

For ChatGPT or another retrieval/project-source environment that does **not** have a current local Git checkout, use the snapshot as the primary source for `main` repository content only after proving that it is current:

1. read the full commit SHA recorded in the snapshot header (`S`);
2. resolve the current live `main` SHA from canonical `alaiba/arcogine` (`M`);
3. when `S == M`, use the snapshot for `main` repository-content reads and searches and do not redundantly fetch individual `main` files through GitHub merely to re-establish the same content;
4. continue using live GitHub for mutable state such as pull requests, reviews, CI/checks, issues, mergeability, branch heads, and repository writes, and for content on branches other than the represented `main`;
5. when `S != M`, treat the snapshot as stale and use live repository evidence instead, except that a formal Consistency review follows the fail-closed rule in the next section rather than reconstructing its corpus through GitHub; and
6. for a long-running task whose conclusion depends on current `main`, resolve live `main` again before finalizing. If it moved, inspect the material change and reconcile the conclusion rather than silently reporting against a stale baseline.

The decision is intentionally small:

```text
snapshot commit S
        |
live main commit M
        |
        +-- S == M --> snapshot is primary for main repository content
        |              live GitHub remains primary for mutable state,
        |              other branches, and writes
        |
        +-- S != M --> snapshot is stale
                       use live repository evidence
                       (formal Consistency review: INCOMPLETE instead)
```

Agents that already have a current local Git checkout should use the local repository tree normally. They do not need to route repository-content reads through Repomix merely because this ChatGPT/project-source optimization exists.

## Exact-current-main requirement

Generation is fail-closed. It requires:

- branch `main`;
- a clean working tree;
- `origin` pointing to `alaiba/arcogine` on GitHub;
- local `HEAD` exactly equal to `git ls-remote origin refs/heads/main`.

Being merely an ancestor of canonical `main` is not sufficient. If local `main` is behind, ahead/unpushed, or otherwise different, update it and retry. The direct remote lookup avoids accepting a stale locally cached `origin/main`.

The generated header records repository, branch, full commit SHA, UTC generation time, pinned Repomix version, and the authority boundary. A formal Consistency review independently resolves live GitHub `main` and requires exact equality with that recorded commit. A missing, malformed, or stale snapshot makes the review `INCOMPLETE`; the reviewer does not reconstruct repository content through GitHub as a fallback.

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

The snapshot intentionally does not include Git history/diffs. Formal Consistency review gets recency from one live GitHub compare between the previous reviewed head and the snapshot/current-main head, then searches the current corpus without a historical scope boundary. Other ChatGPT tasks should likewise use live GitHub whenever history, branch evolution, or other mutable state is material.

## Authority boundary

For the exact recorded commit, the snapshot is authoritative for repository content. When that commit is also the current live `main`, it is therefore sufficient as the primary content corpus for ChatGPT/project-source grounding of `main`.

It is not live authority for issues, pull requests, submitted reviews, unresolved review threads, CI/check status, mergeability, branch heads, or other mutable GitHub state; those remain GitHub-connector concerns. Nor does it represent feature/research branches created after the snapshot.

The snapshot is generated output under `logs/` and is not committed.
