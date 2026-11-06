package com.arcogine.game.lastlightfoundry.draft;

import com.arcogine.game.lastlightfoundry.json.Json;
import java.util.ArrayList;
import java.util.List;

/** An untrusted, editable candidate design: the equipment installations the player placed. */
public record Draft(List<Installation> installations) {

    public static final int MAX_INSTALLATIONS = 64;

    public Draft {
        installations = List.copyOf(installations);
    }

    /** Decodes the {@code installations} array of a request body. */
    public static Draft fromJson(Object raw) {
        List<Object> items = Json.asArray(raw, "draft.installations");
        if (items.size() > MAX_INSTALLATIONS) {
            throw new Json.JsonException("draft.installations may hold at most " + MAX_INSTALLATIONS + " items");
        }
        List<Installation> installations = new ArrayList<>();
        for (Object item : items) {
            installations.add(Installation.fromJson(item, "draft.installations[]"));
        }
        return new Draft(installations);
    }
}
