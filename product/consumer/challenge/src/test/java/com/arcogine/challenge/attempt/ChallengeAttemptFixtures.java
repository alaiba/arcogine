package com.arcogine.challenge.attempt;

import com.arcogine.challenge.ChallengeFixtures;
import com.arcogine.challenge.EquipmentCatalogueItemId;
import com.arcogine.challenge.admissibility.CandidateDraftSnapshot;
import com.arcogine.challenge.admissibility.EquipmentOccurrenceId;
import com.arcogine.challenge.admissibility.GridPlacement;
import com.arcogine.challenge.admissibility.PlacedEquipment;
import com.arcogine.challenge.economics.DraftEconomics;
import com.arcogine.challenge.evaluation.AuthoritativeOutcomeFacts;
import com.arcogine.challenge.evaluation.ChallengeEvaluationInput;
import com.arcogine.challenge.evaluation.ChallengeEvaluationResult;
import com.arcogine.challenge.evaluation.EvaluationProvenance;
import com.arcogine.challenge.evaluation.ReferenceChallengeEvaluationPolicy;
import java.util.List;

/** Shared fixtures for attempt/comparison tests. */
public final class ChallengeAttemptFixtures {

    private ChallengeAttemptFixtures() {}

    public static ChallengeEvaluationResult evaluate(boolean complete, Long completionTick, long cost) {
        ChallengeEvaluationInput input = new ChallengeEvaluationInput(
                ChallengeFixtures.referenceChallenge(), provenance(),
                new AuthoritativeOutcomeFacts(complete, completionTick), cost);
        return ReferenceChallengeEvaluationPolicy.evaluate(input);
    }

    public static EvaluationProvenance provenance() {
        return new EvaluationProvenance("model.factory-basics:v1", "run.factory-basics:historical-1");
    }

    public static DraftEconomics economics(long committedCost) {
        return new DraftEconomics(40_000L, committedCost, 40_000L - committedCost);
    }

    public static CandidateDraftSnapshot snapshot() {
        return new CandidateDraftSnapshot(List.of(placed("cutter-1")));
    }

    public static PlacedEquipment placed(String occurrenceId) {
        return new PlacedEquipment(new EquipmentOccurrenceId(occurrenceId),
                new EquipmentCatalogueItemId("equipment.cutter"), new GridPlacement(0, 0));
    }
}
