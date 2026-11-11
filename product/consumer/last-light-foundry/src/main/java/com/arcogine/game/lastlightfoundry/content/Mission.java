package com.arcogine.game.lastlightfoundry.content;

import com.arcogine.challenge.ChallengeDefinition;
import com.arcogine.challenge.catalogue.EquipmentCatalogue;
import com.arcogine.game.lastlightfoundry.draft.Installation;
import java.util.List;
import java.util.Map;

/**
 * One authored contract: the Challenge definition and catalogue (authoritative economics and
 * scoring inputs) plus the game-owned wrapper that derives Factory content from equipment.
 *
 * @param contentFingerprint SHA-256 over the exact authored content files, identifying this
 *     scenario for reproducibility records
 */
public record Mission(
        String id,
        String title,
        String tagline,
        List<String> briefing,
        List<String> hints,
        ChallengeDefinition challenge,
        EquipmentCatalogue catalogue,
        String productName,
        double unitPrice,
        List<StepSpec> steps,
        Map<String, EquipmentTemplate> equipment,
        List<Installation> starterDraft,
        String contentFingerprint) {

    /** One routing step of the mission's single product, in order. */
    public record StepSpec(String name, long duration) {}

    /** What a catalogue item becomes in the Factory model: a resource that can run named steps. */
    public record EquipmentTemplate(String itemId, String name, int concurrency, List<String> steps, String glyph) {}
}
