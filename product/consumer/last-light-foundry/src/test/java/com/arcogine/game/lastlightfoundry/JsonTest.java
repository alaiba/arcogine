package com.arcogine.game.lastlightfoundry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arcogine.game.lastlightfoundry.draft.Draft;
import com.arcogine.game.lastlightfoundry.json.Json;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JsonTest {

    @Test
    void roundTripsNestedStructuresPreservingKeyOrder() {
        String text = "{\"b\":[1,2,{\"c\":null}],\"a\":\"x\\ny\\u0041\",\"t\":true,\"f\":false}";
        Object parsed = Json.parse(" " + text + " ");
        assertEquals("{\"b\":[1,2,{\"c\":null}],\"a\":\"x\\nyA\",\"t\":true,\"f\":false}",
                Json.write(Json.obj("b", List.of(1, 2, Json.obj("c", null)), "a", "x\nyA", "t", true, "f", false)));
        assertEquals(List.of("b", "a", "t", "f"), List.copyOf(((Map<?, ?>) parsed).keySet()));
    }

    @Test
    void integersAboveTheJavaScriptSafeRangeSurviveTheWire() {
        String huge = "9007199254740993";
        Map<String, Object> parsed = Json.asObject(Json.parse("{\"a\":" + huge + ",\"b\":\"" + huge + "\"}"), "$");
        assertEquals(9007199254740993L, Json.integral(parsed, "a", "$", 0, Long.MAX_VALUE));
        assertEquals(9007199254740993L, Json.integral(parsed, "b", "$", 0, Long.MAX_VALUE));
        assertEquals("\"9223372036854775807\"", Json.write(Json.num(Long.MAX_VALUE)));
    }

    @Test
    void nonFiniteAndRawLongValuesCannotProduceInvalidOrLossyJson() {
        assertEquals("[\"NaN\",\"Infinity\",\"-Infinity\",1.5]",
                Json.write(List.of(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 1.5)));
        assertThrows(IllegalArgumentException.class, () -> Json.write(1L));
        assertThrows(IllegalArgumentException.class, () -> Json.write(new Object()));
        assertThrows(IllegalArgumentException.class, () -> Json.obj("odd"));
    }

    @Test
    void controlCharactersAreEscapedOnOutput() {
        assertEquals("\"\\u0001\\\"\\\\\\r\\t\"", Json.write("\u0001\"\\\r\t"));
    }

    @Test
    void malformedDocumentsAreRejected() {
        for (String bad : List.of("", "{", "[1,]", "{\"a\" 1}", "{\"a\":1,\"a\":2}", "tru", "\"abc", "\"\\q\"",
                "\"\\u12\"", "\"\\uZZZZ\"", "1 2", "-", "{1:2}", "[1 2]", "\"a\nb\"", "{\"a\":1 \"b\":2}",
                "\"\\", "[", "{\"a\":")) {
            assertThrows(Json.JsonException.class, () -> Json.parse(bad), bad);
        }
        assertThrows(Json.JsonException.class, () -> Json.parse(null));
        assertThrows(Json.JsonException.class, () -> Json.parse("[".repeat(100) + "]".repeat(100)));
    }

    @Test
    void escapesAndLiteralsParse() {
        assertEquals("a/b\b\f\n\r\t\"\\", Json.parse("\"a\\/b\\b\\f\\n\\r\\t\\\"\\\\\""));
        assertEquals(List.of(true, false), Json.parse("[true,false]"));
        assertNull(Json.parse("null"));
        assertEquals(Map.of(), Json.parse("{}"));
        assertEquals(List.of(), Json.parse("[]"));
    }

    @Test
    void typedAccessorsRejectWrongTypesAndRanges() {
        Map<String, Object> object = Json.asObject(Json.parse("{\"s\":\"x\",\"n\":3,\"f\":1.5,\"big\":1e30,\"o\":{}}"), "$");
        assertEquals("x", Json.string(object, "s", "$"));
        assertEquals(3, Json.integer(object, "n", "$"));
        assertThrows(Json.JsonException.class, () -> Json.string(object, "n", "$"));
        assertThrows(Json.JsonException.class, () -> Json.integer(object, "f", "$"));
        assertThrows(Json.JsonException.class, () -> Json.integer(object, "big", "$"));
        assertThrows(Json.JsonException.class, () -> Json.integer(object, "missing", "$"));
        assertThrows(Json.JsonException.class, () -> Json.integral(object, "n", "$", 5, 9));
        assertThrows(Json.JsonException.class, () -> Json.integral(Map.of("s", "abc"), "s", "$", 0, 9));
        assertThrows(Json.JsonException.class, () -> Json.asObject(List.of(), "$"));
        assertThrows(Json.JsonException.class, () -> Json.asArray(Map.of(), "$"));
    }

    @Test
    void draftDecodingValidatesEveryUntrustedMember() {
        Draft ok = Draft.fromJson(Json.parse("[{\"number\":1,\"itemId\":\"equipment.cutter\",\"x\":0,\"y\":2}]"));
        assertEquals(1, ok.installations().size());
        for (String bad : List.of(
                "[{\"number\":0,\"itemId\":\"a\",\"x\":0,\"y\":0}]",
                "[{\"number\":10000,\"itemId\":\"a\",\"x\":0,\"y\":0}]",
                "[{\"number\":1,\"itemId\":\"\",\"x\":0,\"y\":0}]",
                "[{\"number\":1,\"itemId\":\"a\",\"x\":5000,\"y\":0}]",
                "[{\"number\":1,\"itemId\":\"a\",\"x\":0}]",
                "[1]",
                "{}")) {
            assertThrows(Json.JsonException.class, () -> Draft.fromJson(Json.parse(bad)), bad);
        }
        String many = "[" + "{\"number\":1,\"itemId\":\"a\",\"x\":0,\"y\":0},".repeat(64)
                + "{\"number\":1,\"itemId\":\"a\",\"x\":0,\"y\":0}]";
        assertThrows(Json.JsonException.class, () -> Draft.fromJson(Json.parse(many)));
    }
}
