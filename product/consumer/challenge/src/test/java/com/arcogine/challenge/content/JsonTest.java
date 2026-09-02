package com.arcogine.challenge.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JsonTest {

    @Test
    void parsesNestedObjectsArraysAndScalars() throws JsonSyntaxException {
        Object result = Json.parse(
                "{\"a\": 1, \"b\": [1, 2.5, \"x\", true, false, null], \"c\": {\"d\": \"e\"}}");

        assertTrue(result instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        assertEquals(1L, map.get("a"));
        assertEquals(List.of(1L, 2.5, "x", true, false), ((List<?>) map.get("b")).subList(0, 5));
        assertNull(((List<?>) map.get("b")).get(5));
        assertEquals("e", ((Map<?, ?>) map.get("c")).get("d"));
    }

    @Test
    void parsesEscapedStrings() throws JsonSyntaxException {
        Object result = Json.parse("\"a\\n\\t\\\"\\\\b\\u0041\"");
        assertEquals("a\n\t\"\\bA", result);
    }

    @Test
    void rejectsTrailingContent() {
        JsonSyntaxException e = assertThrows(JsonSyntaxException.class, () -> Json.parse("{} garbage"));
        assertTrue(e.getMessage().contains("trailing"));
    }

    @Test
    void rejectsUnterminatedObject() {
        assertThrows(JsonSyntaxException.class, () -> Json.parse("{\"a\": 1"));
    }

    @Test
    void rejectsInvalidLiteral() {
        assertThrows(JsonSyntaxException.class, () -> Json.parse("nul"));
    }

    @Test
    void rejectsNullSource() {
        assertThrows(JsonSyntaxException.class, () -> Json.parse(null));
    }

    @Test
    void parsesEmptyObjectAndArray() throws JsonSyntaxException {
        assertEquals(Map.of(), Json.parse("{}"));
        assertEquals(List.of(), Json.parse("[]"));
    }

    @Test
    void rejectsLeadingZeroInteger() {
        assertThrows(JsonSyntaxException.class, () -> Json.parse("01"));
        assertThrows(JsonSyntaxException.class, () -> Json.parse("-01"));
    }

    @Test
    void acceptsValidZeroLedNumericLiterals() throws JsonSyntaxException {
        assertEquals(0L, Json.parse("0"));
        assertEquals(0.5, Json.parse("0.5"));
        assertEquals(0L, Json.parse("-0"));
    }

    @Test
    void rejectsRawControlCharacterInString() {
        assertThrows(JsonSyntaxException.class, () -> Json.parse("\"a\nb\""));
        assertThrows(JsonSyntaxException.class, () -> Json.parse("\"a\tb\""));
    }

    @Test
    void acceptsEscapedControlCharactersInString() throws JsonSyntaxException {
        assertEquals("a\nb", Json.parse("\"a\\nb\""));
        assertEquals("a\tb", Json.parse("\"a\\tb\""));
    }

    @Test
    void rejectsANumberMissingADigitBeforeTheDecimalPoint() {
        assertThrows(JsonSyntaxException.class, () -> Json.parse(".5"));
        assertThrows(JsonSyntaxException.class, () -> Json.parse("-.5"));
    }

    @Test
    void rejectsANumberMissingADigitAfterTheDecimalPoint() {
        assertThrows(JsonSyntaxException.class, () -> Json.parse("1."));
        assertThrows(JsonSyntaxException.class, () -> Json.parse("-1."));
    }

    @Test
    void rejectsANumberMissingADigitInTheExponent() {
        assertThrows(JsonSyntaxException.class, () -> Json.parse("1e"));
        assertThrows(JsonSyntaxException.class, () -> Json.parse("1e+"));
    }

    @Test
    void acceptsWellFormedFractionalAndExponentialNumbers() throws JsonSyntaxException {
        assertEquals(1.5, Json.parse("1.5"));
        assertEquals(-1.5, Json.parse("-1.5"));
        assertEquals(1.0e10, Json.parse("1e10"));
        assertEquals(1.5e-3, Json.parse("1.5e-3"));
    }

    @Test
    void rejectsNonAsciiUnicodeDigits() {
        // Arabic-indic digit one (U+0661): Character.isDigit(...) accepts it, but JSON's number
        // grammar permits only ASCII 0-9.
        assertThrows(JsonSyntaxException.class, () -> Json.parse("١"));
        assertThrows(JsonSyntaxException.class, () -> Json.parse("٠١"));
        assertThrows(JsonSyntaxException.class, () -> Json.parse("1١"));
        assertThrows(JsonSyntaxException.class, () -> Json.parse("1.١"));
        assertThrows(JsonSyntaxException.class, () -> Json.parse("1e١"));
    }

    @Test
    void rejectsNonJsonWhitespaceBetweenTokens() {
        // Form feed (0x0C) is Java whitespace but not JSON insignificant whitespace.
        assertThrows(JsonSyntaxException.class, () -> Json.parse("{\"a\":\f1}"));
    }

    @Test
    void acceptsAllJsonInsignificantWhitespaceBetweenTokens() throws JsonSyntaxException {
        Object result = Json.parse("{ \t\r\n\"a\"\t\r\n:\t\r\n1\t\r\n}");
        assertEquals(Map.of("a", 1L), result);
    }
}
