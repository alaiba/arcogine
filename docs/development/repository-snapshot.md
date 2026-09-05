# Repository snapshot

Arcogine provides one whole-repository snapshot for retrieval-oriented AI or project-source use. It gives an AI environment a searchable point-in-time cache of the repository without making that environment part of Arcogine's architecture.

From a clean checkout of `main`, run:

```bash
./arcogine snapshot
```

The command writes `logs/arcogine-main-<short-sha>.xml`. The file records the full represented commit SHA, a UTC generation timestamp, the pinned Repomix version, and the snapshot's authority boundary. It is generated output and is not committed.

The snapshot is useful for repository-content retrieval and claims about the recorded commit. It is not live authority for later changes to `main`, pull requests, reviews, CI/check status, mergeability, issues, or other mutable GitHub state. Inspect the live repository or GitHub whenever freshness or delivery state matters.

Generation refuses feature branches, detached HEADs, and dirty working trees so the artifact cannot silently be labeled as a clean `main` snapshot.
