package com.arcogine.game.lastlightfoundry.content;

import com.arcogine.challenge.ChallengeDefinition;
import com.arcogine.challenge.catalogue.EquipmentCatalogue;
import com.arcogine.challenge.content.ChallengeContentLoader;
import com.arcogine.challenge.content.ChallengeWithCatalogueLoadResult;
import com.arcogine.game.lastlightfoundry.draft.Installation;
import com.arcogine.game.lastlightfoundry.json.Json;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Loads the bundled authored missions through the real Challenge content loader. */
public final class MissionLibrary {

    private static final String SCHEMA = "last-light-foundry-mission:v1";
    private static final List<String> MISSION_IDS = List.of("wake-the-foundry", "the-relay");

    private final Map<String, Mission> missions;

    private MissionLibrary(Map<String, Mission> missions) {
        this.missions = missions;
    }

    /** Loads and validates every bundled mission; any content problem is fatal at start-up. */
    public static MissionLibrary loadBundled() {
        Map<String, Mission> loaded = new LinkedHashMap<>();
        for (String id : MISSION_IDS) {
            loaded.put(id, load(id));
        }
        return new MissionLibrary(loaded);
    }

    /** All missions in authored order. */
    public List<Mission> all() {
        return List.copyOf(missions.values());
    }

    /** The mission with this id, or {@code null}. */
    public Mission find(String id) {
        return missions.get(id);
    }

    /**
     * Loads one mission from classpath resources and validates the game wrapper against the
     * Challenge definition and catalogue.
     */
    public static Mission load(String id) {
        String base = "/missions/" + id + "/";
        String challengeSource = read(base + "challenge.json");
        String catalogueSource = read(base + "catalogue.json");
        String missionSource = read(base + "mission.json");

        ChallengeWithCatalogueLoadResult result =
                ChallengeContentLoader.loadChallengeWithCatalogue(challengeSource, catalogueSource);
        if (!result.isSuccess()) {
            throw new IllegalStateException("mission " + id + " challenge content is invalid: " + result.issues());
        }
        ChallengeDefinition challenge = result.definition();
        EquipmentCatalogue catalogue = result.catalogue();

        Map<String, Object> root = Json.asObject(Json.parse(missionSource), "mission");
        if (!SCHEMA.equals(Json.string(root, "schemaVersion", "mission"))) {
            throw new IllegalStateException("mission " + id + " has an unsupported schemaVersion");
        }
        if (!id.equals(Json.string(root, "id", "mission"))) {
            throw new IllegalStateException("mission " + id + " declares a different id");
        }

        List<Mission.StepSpec> steps = new ArrayList<>();
        for (Object raw : Json.asArray(root.get("steps"), "mission.steps")) {
            Map<String, Object> step = Json.asObject(raw, "mission.steps[]");
            steps.add(new Mission.StepSpec(
                    Json.string(step, "name", "step"), Json.integral(step, "duration", "step", 1, 1_000_000)));
        }
        if (steps.isEmpty()) {
            throw new IllegalStateException("mission " + id + " has no steps");
        }

        Map<String, Mission.EquipmentTemplate> equipment = new LinkedHashMap<>();
        for (Object raw : Json.asArray(root.get("equipment"), "mission.equipment")) {
            Map<String, Object> item = Json.asObject(raw, "mission.equipment[]");
            Mission.EquipmentTemplate template = new Mission.EquipmentTemplate(
                    Json.string(item, "itemId", "equipment"),
                    Json.string(item, "name", "equipment"),
                    Json.integer(item, "concurrency", "equipment"),
                    strings(item.get("steps"), "equipment.steps"),
                    Json.string(item, "glyph", "equipment"));
            if (equipment.put(template.itemId(), template) != null) {
                throw new IllegalStateException("mission " + id + " repeats equipment " + template.itemId());
            }
            for (String step : template.steps()) {
                if (steps.stream().noneMatch(spec -> spec.name().equals(step))) {
                    throw new IllegalStateException("mission " + id + ": unknown step " + step);
                }
            }
        }
        catalogue.offers().forEach(offer -> {
            if (!equipment.containsKey(offer.itemId().value())) {
                throw new IllegalStateException("mission " + id + ": no template for " + offer.itemId().value());
            }
        });
        if (equipment.size() != catalogue.offers().size()) {
            throw new IllegalStateException("mission " + id + ": templates and catalogue offers differ");
        }

        List<Installation> starter = new ArrayList<>();
        for (Object raw : Json.asArray(root.get("starterDraft"), "mission.starterDraft")) {
            starter.add(Installation.fromJson(raw, "starterDraft[]"));
        }

        return new Mission(
                id,
                Json.string(root, "title", "mission"),
                Json.string(root, "tagline", "mission"),
                strings(root.get("briefing"), "mission.briefing"),
                strings(root.get("hints"), "mission.hints"),
                challenge,
                catalogue,
                Json.string(root, "productName", "mission"),
                ((BigDecimal) root.get("unitPrice")).doubleValue(),
                List.copyOf(steps),
                Collections.unmodifiableMap(equipment),
                List.copyOf(starter),
                fingerprint(challengeSource, catalogueSource, missionSource));
    }

    private static List<String> strings(Object raw, String path) {
        List<String> out = new ArrayList<>();
        for (Object item : Json.asArray(raw, path)) {
            if (!(item instanceof String text)) {
                throw new IllegalStateException(path + " must contain strings");
            }
            out.add(text);
        }
        return List.copyOf(out);
    }

    private static String read(String resource) {
        try (InputStream in = MissionLibrary.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("missing bundled content: " + resource);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("unable to read " + resource, e);
        }
    }

    private static String fingerprint(String... sources) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String source : sources) {
                byte[] bytes = source.getBytes(StandardCharsets.UTF_8);
                digest.update(Integer.toString(bytes.length).getBytes(StandardCharsets.UTF_8));
                digest.update((byte) ':');
                digest.update(bytes);
            }
            return "sha256:" + HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
