package com.arcogine.game.lastlightfoundry.engine;

import com.arcogine.challenge.admissibility.CandidateAdmissibilityPolicy;
import com.arcogine.challenge.admissibility.CandidateAdmissibilityResult;
import com.arcogine.challenge.economics.DraftEconomicsCalculator;
import com.arcogine.challenge.economics.DraftEconomicsResult;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.validation.FactoryModelValidator;
import com.arcogine.factory.model.validation.ModelValidationError;
import com.arcogine.game.lastlightfoundry.content.Mission;
import com.arcogine.game.lastlightfoundry.draft.Draft;
import com.arcogine.game.lastlightfoundry.draft.DraftProjector;
import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates a draft against both independent authorities: Challenge economics and admissibility,
 * and Factory validation and publication. Neither verdict is derived from the other.
 */
public final class DesignChecker {

    private DesignChecker() {}

    /** One reason the draft cannot be published as a Factory model. */
    public record PublicationIssue(String field, String message) {}

    /**
     * The combined report.
     *
     * @param publishedVersion the immutable published Factory version, or {@code null} when the
     *     draft cannot be published
     */
    public record DesignReport(
            DraftEconomicsResult economics,
            CandidateAdmissibilityResult admissibility,
            List<PublicationIssue> publicationIssues,
            FactoryModelVersion publishedVersion) {

        public boolean publishable() {
            return publishedVersion != null;
        }

        public boolean runnable() {
            return publishable() && admissibility.admitted() && economics.isSuccess();
        }
    }

    /** Checks the draft. Never throws for player-authored content: problems are reported. */
    public static DesignReport check(Mission mission, Draft draft) {
        DraftEconomicsResult economics = DraftEconomicsCalculator.calculate(
                mission.challenge(), mission.catalogue(), DraftProjector.toEconomicsOccurrences(draft));
        CandidateAdmissibilityResult admissibility = CandidateAdmissibilityPolicy.assess(
                mission.challenge(), mission.catalogue(), DraftProjector.toCandidateSnapshot(draft));

        List<PublicationIssue> issues = new ArrayList<>();
        FactoryModelVersion published = null;
        DraftProjector.Projection projection = DraftProjector.toFactoryModel(mission, draft);
        projection.issues().forEach(message -> issues.add(new PublicationIssue("draft", message)));
        if (projection.derivable()) {
            List<ModelValidationError> errors = FactoryModelValidator.validate(projection.model()).errors();
            errors.forEach(error -> issues.add(new PublicationIssue(error.field(), error.message())));
            if (errors.isEmpty()) {
                published = FactoryModelPublisher.publish(projection.model());
            }
        }
        return new DesignReport(economics, admissibility, List.copyOf(issues), published);
    }
}
