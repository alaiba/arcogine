package com.arcogine.challenge.admissibility;

import com.arcogine.challenge.ChallengeDefinition;
import com.arcogine.challenge.EquipmentCatalogueItemId;
import com.arcogine.challenge.catalogue.EquipmentCatalogue;
import com.arcogine.challenge.catalogue.EquipmentOffer;
import com.arcogine.challenge.economics.DraftEconomicsCalculator;
import com.arcogine.challenge.economics.DraftEconomicsFailure;
import com.arcogine.challenge.economics.DraftEconomicsResult;
import com.arcogine.challenge.economics.DraftEquipmentOccurrence;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Pure, deterministic game-rule admissibility for one exact challenge and draft snapshot. */
public final class CandidateAdmissibilityPolicy {

    private static final Comparator<PlacedEquipment> OCCURRENCE_ORDER = Comparator
            .comparing((PlacedEquipment placed) -> placed.occurrenceId().value())
            .thenComparing(placed -> placed.itemId().value())
            .thenComparingInt(placed -> placed.placement().x())
            .thenComparingInt(placed -> placed.placement().y());

    private CandidateAdmissibilityPolicy() {}

    public static CandidateAdmissibilityResult assess(
            ChallengeDefinition challenge,
            EquipmentCatalogue catalogue,
            CandidateDraftSnapshot snapshot) {
        if (challenge == null) {
            throw new NullPointerException("challenge");
        }
        if (catalogue == null) {
            throw new NullPointerException("catalogue");
        }
        if (snapshot == null) {
            throw new NullPointerException("snapshot");
        }

        List<PlacedEquipment> placed = snapshot.placedEquipment().stream()
                .sorted(OCCURRENCE_ORDER)
                .toList();
        List<CandidateAdmissibilityIssue> issues = new ArrayList<>();
        addIdentityIssues(placed, issues);
        if (!challenge.catalogueIdentity().equals(catalogue.identity())) {
            issues.add(issue("candidate.catalogue.identity-mismatch", "catalogue.identity",
                "catalogue identity does not match challenge identity: "
                    + catalogue.identity()));
        }
                if (challenge.catalogueSemanticFingerprint() != null
                    && !challenge.catalogueSemanticFingerprint().equals(catalogue.semanticFingerprint())) {
                    issues.add(issue("candidate.catalogue.semantic-fingerprint-mismatch",
                        "catalogue.semanticFingerprint",
                        "catalogue content does not match the challenge's versioned semantics"));
                }
        Set<EquipmentCatalogueItemId> available = new HashSet<>(challenge.availableEquipment());
        Map<EquipmentCatalogueItemId, Integer> quantities = new HashMap<>();

        for (PlacedEquipment equipment : placed) {
            String path = path(equipment);
            EquipmentOffer offer = catalogue.findByItemId(equipment.itemId()).orElse(null);
            if (offer == null) {
                issues.add(issue("candidate.occurrence.unknown-catalogue-item", path,
                        "no catalogue offer for item id: " + equipment.itemId().value()));
            } else {
                quantities.merge(equipment.itemId(), 1, Integer::sum);
            }
        }
        for (PlacedEquipment equipment : placed) {
            if (!available.contains(equipment.itemId())) {
                issues.add(issue("candidate.occurrence.unavailable-equipment", path(equipment),
                        "catalogue item is not available in this challenge: "
                                + equipment.itemId().value()));
            }
        }

        addQuantityIssues(quantities, catalogue, issues);
        DraftEconomicsResult economics = DraftEconomicsCalculator.calculate(challenge, catalogue,
                placed.stream().map(equipment -> new DraftEquipmentOccurrence(equipment.itemId())).toList());
        if (!economics.isSuccess()) {
            DraftEconomicsFailure failure = economics.failure();
            if (!failure.code().equals("draft.occurrence.unknown-catalogue-item")) {
                issues.add(issue("candidate.economics." + failure.code(), "draft.economics", failure.message()));
            }
        } else if (economics.economics().committedConstructionCostCredits() > challenge.startingBudget()) {
            issues.add(issue("candidate.budget.exceeded", "draft.economics.committedConstructionCostCredits",
                    "committed construction cost exceeds starting budget"));
        }

        addBoundsIssues(placed, challenge, issues);
        addOverlapIssues(placed, issues);
        return issues.isEmpty() ? CandidateAdmissibilityResult.success()
                : CandidateAdmissibilityResult.rejected(issues);
    }

    private static void addIdentityIssues(List<PlacedEquipment> placed,
            List<CandidateAdmissibilityIssue> issues) {
        Set<EquipmentOccurrenceId> seen = new HashSet<>();
        for (PlacedEquipment equipment : placed) {
            if (!seen.add(equipment.occurrenceId())) {
                issues.add(issue("candidate.occurrence.duplicate-identity", path(equipment),
                        "equipment occurrence id is duplicated: " + equipment.occurrenceId().value()));
            }
        }
    }

    private static void addQuantityIssues(Map<EquipmentCatalogueItemId, Integer> quantities,
            EquipmentCatalogue catalogue, List<CandidateAdmissibilityIssue> issues) {
        quantities.keySet().stream().sorted(Comparator.comparing(EquipmentCatalogueItemId::value))
                .forEach(itemId -> catalogue.findByItemId(itemId).ifPresent(offer -> {
                    if (offer.quantityLimit().isPresent()
                            && quantities.get(itemId) > offer.quantityLimit().getAsInt()) {
                        issues.add(issue("candidate.quantity-limit.exceeded", "item[" + itemId.value() + "]",
                                "quantity exceeds catalogue limit of " + offer.quantityLimit().getAsInt()));
                    }
                }));
    }

    private static void addBoundsIssues(List<PlacedEquipment> placed, ChallengeDefinition challenge,
            List<CandidateAdmissibilityIssue> issues) {
        for (PlacedEquipment equipment : placed) {
            GridPlacement placement = equipment.placement();
            if (placement.x() < 0 || placement.x() >= challenge.floor().width()
                    || placement.y() < 0 || placement.y() >= challenge.floor().height()) {
                issues.add(issue("candidate.placement.out-of-bounds", path(equipment),
                        "placement must satisfy 0 <= x < " + challenge.floor().width()
                                + " and 0 <= y < " + challenge.floor().height()));
            }
        }
    }

    private static void addOverlapIssues(List<PlacedEquipment> placed,
            List<CandidateAdmissibilityIssue> issues) {
        Set<GridPlacement> occupied = new HashSet<>();
        for (PlacedEquipment equipment : placed) {
            GridPlacement placement = equipment.placement();
            if (!occupied.add(placement)) {
                issues.add(issue("candidate.placement.overlap", path(equipment),
                        "placement cell is already occupied: (" + placement.x() + ", " + placement.y() + ")"));
            }
        }
    }

    private static String path(PlacedEquipment equipment) {
        return "placedEquipment[" + equipment.occurrenceId().value() + "]";
    }

    private static CandidateAdmissibilityIssue issue(String code, String path, String message) {
        return new CandidateAdmissibilityIssue(code, path, message);
    }
}