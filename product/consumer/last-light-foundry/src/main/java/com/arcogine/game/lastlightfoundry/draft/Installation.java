package com.arcogine.game.lastlightfoundry.draft;

import com.arcogine.game.lastlightfoundry.json.Json;
import java.util.Map;

/**
 * One placed piece of equipment in a draft.
 *
 * <p>{@code number} is the explicit, stable installation identity: it alone derives the Factory
 * resource identity (and therefore the engine's ascending-identity tie-break). {@code x}/{@code y}
 * are cosmetic floor coordinates used for placement and collision rules only.
 */
public record Installation(int number, String itemId, int x, int y) {

    public static final int MAX_NUMBER = 9999;
    public static final int MAX_COORDINATE = 1000;

    /** Decodes one untrusted installation object, rejecting malformed or out-of-range members. */
    public static Installation fromJson(Object raw, String path) {
        Map<String, Object> object = Json.asObject(raw, path);
        int number = Json.integer(object, "number", path);
        if (number < 1 || number > MAX_NUMBER) {
            throw new Json.JsonException(path + ".number must be within [1, " + MAX_NUMBER + "]");
        }
        String itemId = Json.string(object, "itemId", path);
        if (itemId.isBlank() || itemId.length() > 100) {
            throw new Json.JsonException(path + ".itemId must be 1-100 characters");
        }
        int x = (int) Json.integral(object, "x", path, -MAX_COORDINATE, MAX_COORDINATE);
        int y = (int) Json.integral(object, "y", path, -MAX_COORDINATE, MAX_COORDINATE);
        return new Installation(number, itemId, x, y);
    }
}
