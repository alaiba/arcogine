# Arcogine Storage

Status: Built-in product capability implemented for local, definition-bound controlled-revision history; stronger recovery and historical-definition support remain open
Owning architecture: [Architecture overview](overview.md)
Semantic port: [Controlled revisions](controlled-revisions.md)

## Responsibility and public boundary

Arcogine Storage stores authoritative information with guarantees owned by Arcogine. The initial
implementation realizes one bounded responsibility: Governance's `ControlledRevisionAuthority`.
Governance defines historical occurrence identity, lineage, acceptance and recording provenance;
Storage realizes persistent acceptance, coordination, integrity checks, and exact retrieval. Factory
currently provides canonical bytes, fingerprints, decoding, and a `SemanticArtifactVerifier`, and the
built-in provider persists those verified bytes. That is the current implementation boundary, not a
decision that fingerprint projection, a public reversible Factory artifact, and Storage representation
must permanently remain the same contract; the
[Factory canonical-artifact-boundary research](../research/investigations/factory-canonical-artifact-boundary.md)
owns that open question. Storage does not interpret Factory content or define an alternative revision identity.

Clients call `BuiltInStorage.open(Path, SemanticArtifactVerifier)`, receive an `ArcogineStorage`, and
obtain `ControlledRevisionAuthority` through `controlledRevisions()`. The filesystem implementation
is private. The root path is bootstrap configuration, not an exposed record layout. There is no
close-time commit: each successful `accept` commits its revision record during that call.

## Initial supported scope

The built-in provider uses an Arcogine-owned local filesystem location and one verifier definition
binding. It requires an absent root that it creates itself, or an existing root carrying its exact
private marker, lock metadata, and matching binding. A foreign or incomplete location is refused
without adoption, overwrite, repair, or deletion. Interrupted initialization may leave a refused
root; the implementation does not silently complete or erase it. A new root's record directories
are initialized under its lock. An existing root missing either directory is refused unchanged.

The representation retains the original private `proving-store` marker and `arcogine-proving-*`
record prefixes. This makes compatible existing roots readable without renaming or migrating them.
Those bytes describe legacy format, not the current product purpose or a stronger custody promise.
The verifier's opaque `definitionBinding()` is a conservative exact-build guard. A mismatched build
is refused before records are read, and the root remains untouched. Equal content fingerprints do
not prove equal definition meaning. A refusal preserves bytes but supplies neither a compatible
reader nor an exact-definition archive.

The accepted revision record is the authoritative commit boundary. Storage validates the candidate
and verifier, checks an already accepted parent under the current zero-or-one-parent policy, stores
the verified artifact, and installs the immutable revision record. Governance's `recordedAt` is
established by the authority at that boundary; the candidate timestamp is not trusted. The record,
artifact fingerprint and bytes, lineage, and recording provenance must agree on resolution. An ID
already present is rejected, even when the repeated candidate is equal; retry is not idempotent.
Accepted text is stored exactly or refused: valid Unicode, supplementary characters included,
resolves unchanged, while recorder text the private UTF-8 record format cannot represent (an
unpaired UTF-16 surrogate) is refused with `IllegalArgumentException` before anything is installed,
never substituted. A definition binding held to the same standard is refused before a root is
created or opened, so distinct bindings never share a stored marker.
Distinct accepted occurrences may share a fingerprint and physical artifact, including
`F1 -> F2 -> F1`, without sharing revision identity.

`findById` returns empty only for an absent ID. `resolve` reports a missing revision, missing
artifact, unsupported fingerprint, or integrity failure through `GovernanceHistoryException` as
appropriate; it never reconstructs an accepted basis from the current model or from a newly
supplied artifact. `revisions()` enumerates accepted records deterministically by ID. Corrupt or
mismatched records fail explicitly. Existing records and artifacts are never silently repaired.

## Concurrency, failure, and durability bounds

An in-process lock and a filesystem lock serialize normal operations on the owned root, including
conflicting acceptance. The initial evidence covers concurrent threads and reopening from a
separate JVM on the supported local filesystem. A second process observing an unfinished initial
creation can receive a refusal and retry after initialization. Independent-process contention,
network filesystems, and hostile concurrent modification are not established support claims.

The implementation forces a temporary file and requires atomic installation for records and
artifacts; unsupported atomic moves fail. Before the revision record is installed, there is no
accepted revision, even if an artifact was installed. A failure after installation but before the
caller receives confirmation can leave an accepted record: the caller must query by ID before
deciding how to proceed, and a second `accept` will still reject the duplicate. Cleanup must not
remove an artifact referenced by an installed record. Tests establish ordinary reopen and a
separate-JVM reopen. They do not establish survival of process kill during a write, OS crash, power
loss, disk loss, or arbitrary corruption; there is no repair, backup, authenticity, or unlimited
retention guarantee. [Storage failure-model research](../research/investigations/storage-failure-model.md)
owns the stronger unanswered question.

Storage's product ownership is separate from maturity of the stored semantic definition. Factory
and Engine definitions remain correctable development definitions. The current provider's
same-binding scope allows exact accepted Factory artifact recovery while that binding is available;
it does not offer cross-version compatibility, permanent interpretability, or released support.
Representation identity, content fingerprint, definition basis, and revision occurrence identity
remain distinct. [Historical storage support research](../research/investigations/storage-historical-support.md)
addresses stronger resolvability without presuming a migration mechanism. A use requiring retained
historical custody needs its own explicit support declaration and evidence under [semantic contract
support](../development/semantic-contract-support.md); the presence of physical storage is not that
declaration.
