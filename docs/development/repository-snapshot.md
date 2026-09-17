# Repository snapshot

Arcogine's Repomix snapshot is the required repository-content corpus for formal Consistency reviews in ChatGPT. It trades repeated GitHub file/search calls for one locally searchable representation of exact current canonical `main`, allowing the reviewer to spend its work on deeper repository analysis.

From a clean checkout of current `main`, run:

```bash
./arcogine snapshot
```

The command writes `logs/arcogine-main-<short-sha>.xml`. Upload that file to the Arcogine ChatGPT project before running the formal Consistency review.

## Exact-current-main requirement

Generation is fail-closed. It requires:

- branch `main`;
- a clean working tree;
- `origin` pointing to `alaiba/arcogine` on GitHub;
- local `HEAD` exactly equal to `git ls-remote origin refs/heads/main`.

Being merely an ancestor of canonical `main` is not sufficient. If local `main` is behind, ahead/unpushed, or otherwise different, update it and retry. The direct remote lookup avoids accepting a stale locally cached `origin/main`.

The generated header records repository, branch, full commit SHA, UTC generation time, pinned Repomix version, and the authority boundary. A Consistency review independently resolves live GitHub `main` and requires exact equality with that recorded commit. A missing, malformed, or stale snapshot makes the review `INCOMPLETE`; the reviewer does not reconstruct repository content through GitHub as a fallback.

## Corpus shape

The snapshot is optimized for semantic review rather than minimum token count:

- XML output;
- full text content rather than code compression;
- source comments retained;
- directory structure and files retained;
- generic Repomix summary omitted;
- generated/dependency material excluded through the repository's `.gitignore`;
- Repomix default/dot-ignore layers disabled so exclusions remain repository-visible and auditable;
- explicit secret-like exclusions retained;
- Repomix security checking enabled.

The wrapper also prepends a `<tracked_files>` manifest generated from `git ls-files`. It enumerates every tracked path even when Repomix does not include a file's contents (for example, binary assets). This lets the Consistency reviewer reason about path existence, links, packaging, and repository shape without requiring binary contents in the prompt.

The snapshot intentionally does not include Git history/diffs. The reviewer gets recency from one live GitHub compare between the previous reviewed head and the snapshot/current-main head, then searches the current corpus without a historical scope boundary.

## Authority boundary

For the exact recorded commit, the snapshot is authoritative for repository content. It is not live authority for issues, pull requests, submitted reviews, unresolved review threads, CI/check status, mergeability, or other mutable GitHub state; those remain GitHub-connector concerns.

The snapshot is generated output under `logs/` and is not committed.
