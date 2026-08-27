package com.arcogine.challenge.admissibility;

import java.util.List;

/** An immutable game-owned snapshot of the equipment placed in one candidate draft. */
public record CandidateDraftSnapshot(List<PlacedEquipment> placedEquipment) {

    public CandidateDraftSnapshot {
        placedEquipment = placedEquipment == null ? List.of() : List.copyOf(placedEquipment);
    }
}