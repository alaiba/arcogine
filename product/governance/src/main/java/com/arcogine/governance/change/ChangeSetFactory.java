package com.arcogine.governance.change;

import com.arcogine.governance.ControlledRevisionAuthority;
import com.arcogine.governance.HistoricalRevision;
import com.arcogine.governance.SemanticArtifact;
import com.arcogine.governance.SemanticArtifactVerifier;
import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.ModelFingerprint;
import java.util.List;
import java.util.Objects;

/**
 * Builds {@link ChangeSet}s from Governance's own authoritative historical-resolution boundary
 * ({@link ControlledRevisionAuthority#resolve(ControlledRevisionId)}), so revision-to-revision
 * comparisons always go through exact historical semantic-state resolution rather than
 * ad hoc/test-only state.
 */
public final class ChangeSetFactory {

    private ChangeSetFactory() {}

    /**
     * Compares two already-accepted, authoritative controlled revisions.
     *
     * <p>Both revisions are resolved through {@code authority.resolve(...)}, so this always
     * exercises the persistence-acceptance boundary's exact historical semantic-state resolution path. If the two revisions
     * happen to share a fingerprint (e.g. a rollback), the extractor naturally yields zero
     * semantic changes while {@code baseRevisionId} and {@code resultingRevisionId} remain
     * distinct revision identities.
     */
    public static ChangeSet fromAuthoritativeRevisions(
            ControlledRevisionAuthority authority,
            ControlledRevisionId baseRevisionId,
            ControlledRevisionId candidateRevisionId,
            SemanticChangeExtractor extractor,
            ChangeProvenance provenance) {
        Objects.requireNonNull(authority, "authority");
        Objects.requireNonNull(baseRevisionId, "baseRevisionId");
        Objects.requireNonNull(candidateRevisionId, "candidateRevisionId");
        Objects.requireNonNull(extractor, "extractor");
        Objects.requireNonNull(provenance, "provenance");

        HistoricalRevision base = authority.resolve(baseRevisionId);
        HistoricalRevision candidate = authority.resolve(candidateRevisionId);
        List<SemanticChange> changes = compare(extractor, base.artifact(), candidate.artifact());

        return new ChangeSet(
                baseRevisionId,
                base.artifact().fingerprint(),
                candidate.artifact().fingerprint(),
                candidateRevisionId,
                changes,
                null,
                provenance);
    }

    /**
     * Compares an authoritative base revision against a candidate semantic snapshot that is not
     * (yet) an accepted controlled revision.
     *
     * <p>The candidate participates purely through its {@link SemanticArtifact}: it is never
     * assigned a synthetic {@link ControlledRevisionId}, and {@link ChangeSet#resultingRevisionId()}
     * is absent. The candidate becomes historically authoritative only if and when it is later
     * accepted through {@link ControlledRevisionAuthority#accept}, at which point {@link
     * #fromAuthoritativeRevisions} is the applicable path.
     *
     * <p>Unlike an authoritative revision -- whose fingerprint-to-bytes binding is verified by
     * the persistence-acceptance/resolution boundary ({@code FileControlledRevisionAuthority}) -- a candidate
     * snapshot is caller-supplied and never passes through that boundary. Before its declared
     * fingerprint is recorded as {@link ChangeSet#candidateFingerprint()}, {@code verifier} is used
     * to recompute the fingerprint from {@code candidateArtifact}'s own canonical bytes and confirm
     * it matches the declared one, mirroring {@code FileControlledRevisionAuthority}'s verification
     * precedent so a caller cannot claim an untrue candidate identity.
     */
    public static ChangeSet fromCandidateSnapshot(
            ControlledRevisionAuthority authority,
            ControlledRevisionId baseRevisionId,
            SemanticArtifact candidateArtifact,
            SemanticArtifactVerifier verifier,
            SemanticChangeExtractor extractor,
            ChangeProvenance provenance) {
        Objects.requireNonNull(authority, "authority");
        Objects.requireNonNull(baseRevisionId, "baseRevisionId");
        Objects.requireNonNull(candidateArtifact, "candidateArtifact");
        Objects.requireNonNull(verifier, "verifier");
        Objects.requireNonNull(extractor, "extractor");
        Objects.requireNonNull(provenance, "provenance");

        verifyCandidateFingerprintBinding(candidateArtifact, verifier);

        HistoricalRevision base = authority.resolve(baseRevisionId);
        List<SemanticChange> changes = compare(extractor, base.artifact(), candidateArtifact);

        return new ChangeSet(
                baseRevisionId,
                base.artifact().fingerprint(),
                candidateArtifact.fingerprint(),
                null,
                changes,
                null,
                provenance);
    }

    private static void verifyCandidateFingerprintBinding(
            SemanticArtifact candidateArtifact, SemanticArtifactVerifier verifier) {
        if (!verifier.supports(candidateArtifact.fingerprint())) {
            throw new IllegalArgumentException(
                    "verifier does not support the candidate artifact's declared fingerprint policy: "
                            + candidateArtifact.fingerprint());
        }
        ModelFingerprint computed;
        try {
            computed = verifier.fingerprint(candidateArtifact.canonicalBytes());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(
                    "candidate artifact fingerprint cannot be verified against its canonical bytes", e);
        }
        if (!candidateArtifact.fingerprint().equals(computed)) {
            throw new IllegalArgumentException(
                    "candidate artifact fingerprint does not match its canonical bytes: declared "
                            + candidateArtifact.fingerprint()
                            + ", computed "
                            + computed);
        }
    }

    private static List<SemanticChange> compare(
            SemanticChangeExtractor extractor, SemanticArtifact base, SemanticArtifact candidate) {
        if (!extractor.supports(base.fingerprint()) || !extractor.supports(candidate.fingerprint())) {
            throw new IllegalArgumentException(
                    "extractor does not support the fingerprint policy of the supplied artifacts");
        }
        return extractor.compare(base, candidate);
    }
}
