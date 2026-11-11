# Repository identity and task shorthand

This repository is the canonical Arcogine repository:

`alaiba/arcogine`

Agents operating from this repository context must treat that identity as already known. For GitHub operations involving issues, pull requests, branches, commits, workflows, files, or repository state:

- default to `alaiba/arcogine` unless the user explicitly identifies another repository;
- use repository-scoped operations first;
- do not search for or rediscover the repository before acting;
- use global repository discovery only for explicitly cross-repository tasks or when the requested operation genuinely cannot be resolved from this repository.

Repository context is sufficient authority to perform read-only repository operations without asking the user to restate the repository name or URL.

Repository search results are discovery aids, not revision authority. Search indexes may lag a mutable target ref. When a search hit informs a current-state or target-revision claim, fetch the matched path at the exact target ref or commit before relying on its content. Do not treat a snippet from another indexed commit as evidence about the target revision.

Common shorthand should be interpreted in repository context:

- “read an issue” means select and read an applicable open issue in this repository;
- “review PR” means select and review an applicable pull request in this repository, following the dedicated PR Reviewer contract;
- “check repo state” means inspect the state of this repository;
- references such as “the issue”, “the PR”, “main”, or a bare issue/PR number refer to this repository unless context explicitly establishes otherwise.

Repository workflow shorthand must match the entire token:

- `.?` = perform the Session-close Kaizen review before ending or deleting the current session;
- `./` = review or re-review the current applicable pull request using the dedicated PR Reviewer contract;
- `..` = read the current implementation pull request's live GitHub state and perform the next implementation-owned transition, if one is available;

## Recommending next work

There is no separate planning role. Known work waits in GitHub issues and open questions in the
research register; the repository owner chooses what to start. A request to recommend next work is
answered from open issues, `READY` research questions, open pull requests and their live
review/CI state.
