package com.arcogine.challenge.content;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A minimal, dependency-free JSON parser used only to decode game-owned challenge content.
 *
 * <p>This is deliberately not a general-purpose JSON library: it supports exactly the value
 * shapes the challenge content format needs (objects, arrays, strings, numbers, booleans, and
 * null), reports malformed input as a {@link JsonSyntaxException} carrying a human-readable
 * position, and introduces no third-party dependency into the headless {@code :challenge} module.
 *
 * <p>Parsing is pure: it performs no I/O, holds no mutable static state, and always produces an
 * equal result tree for equal input.
 */
final class Json {

    private final String source;
    private int position;

    private Json(String source) {
        this.source = source;
    }

    /**
     * Parses {@code source} as a single JSON value and returns the decoded tree ({@link Map},
     * {@link List}, {@link String}, {@link Double}, {@link Boolean}, or {@code null}).
     */
    static Object parse(String source) throws JsonSyntaxException {
        if (source == null) {
            throw new JsonSyntaxException("source must not be null");
        }
        Json parser = new Json(source);
        parser.skipWhitespace();
        Object value = parser.parseValue();
        parser.skipWhitespace();
        if (parser.position != parser.source.length()) {
            throw parser.error("unexpected trailing content");
        }
        return value;
    }

    private Object parseValue() throws JsonSyntaxException {
        if (position >= source.length()) {
            throw error("unexpected end of input");
        }
        char c = source.charAt(position);
        return switch (c) {
            case '{' -> parseObject();
            case '[' -> parseArray();
            case '"' -> parseString();
            case 't', 'f' -> parseBoolean();
            case 'n' -> parseNull();
            default -> parseNumber();
        };
    }

    private Map<String, Object> parseObject() throws JsonSyntaxException {
        expect('{');
        Map<String, Object> result = new LinkedHashMap<>();
        skipWhitespace();
        if (peek() == '}') {
            position++;
            return result;
        }
        while (true) {
            skipWhitespace();
            if (peek() != '"') {
                throw error("expected string key");
            }
            String key = parseString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            Object value = parseValue();
            result.put(key, value);
            skipWhitespace();
            char next = peek();
            if (next == ',') {
                position++;
            } else if (next == '}') {
                position++;
                return result;
            } else {
                throw error("expected ',' or '}'");
            }
        }
    }

    private List<Object> parseArray() throws JsonSyntaxException {
        expect('[');
        List<Object> result = new ArrayList<>();
        skipWhitespace();
        if (peek() == ']') {
            position++;
            return result;
        }
        while (true) {
            skipWhitespace();
            result.add(parseValue());
            skipWhitespace();
            char next = peek();
            if (next == ',') {
                position++;
            } else if (next == ']') {
                position++;
                return result;
            } else {
                throw error("expected ',' or ']'");
            }
        }
    }

    private String parseString() throws JsonSyntaxException {
        expect('"');
        StringBuilder builder = new StringBuilder();
        while (true) {
            if (position >= source.length()) {
                throw error("unterminated string");
            }
            char c = source.charAt(position++);
            if (c == '"') {
                return builder.toString();
            }
            if (c == '\\') {
                if (position >= source.length()) {
                    throw error("unterminated escape");
                }
                char escape = source.charAt(position++);
                switch (escape) {
                    case '"' -> builder.append('"');
                    case '\\' -> builder.append('\\');
                    case '/' -> builder.append('/');
                    case 'b' -> builder.append('\b');
                    case 'f' -> builder.append('\f');
                    case 'n' -> builder.append('\n');
                    case 'r' -> builder.append('\r');
                    case 't' -> builder.append('\t');
                    case 'u' -> builder.append(parseUnicodeEscape());
                    default -> throw error("invalid escape '\\" + escape + "'");
                }
            } else {
                builder.append(c);
            }
        }
    }

    private char parseUnicodeEscape() throws JsonSyntaxException {
        if (position + 4 > source.length()) {
            throw error("truncated unicode escape");
        }
        String hex = source.substring(position, position + 4);
        position += 4;
        try {
            return (char) Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            throw error("invalid unicode escape '" + hex + "'");
        }
    }

    private Boolean parseBoolean() throws JsonSyntaxException {
        if (source.startsWith("true", position)) {
            position += 4;
            return Boolean.TRUE;
        }
        if (source.startsWith("false", position)) {
            position += 5;
            return Boolean.FALSE;
        }
        throw error("invalid literal");
    }

    private Object parseNull() throws JsonSyntaxException {
        if (source.startsWith("null", position)) {
            position += 4;
            return null;
        }
        throw error("invalid literal");
    }

    private Double parseNumber() throws JsonSyntaxException {
        int start = position;
        if (peek() == '-') {
            position++;
        }
        while (position < source.length() && Character.isDigit(source.charAt(position))) {
            position++;
        }
        if (position < source.length() && source.charAt(position) == '.') {
            position++;
            while (position < source.length() && Character.isDigit(source.charAt(position))) {
                position++;
            }
        }
        if (position < source.length() && (source.charAt(position) == 'e' || source.charAt(position) == 'E')) {
            position++;
            if (position < source.length()
                    && (source.charAt(position) == '+' || source.charAt(position) == '-')) {
                position++;
            }
            while (position < source.length() && Character.isDigit(source.charAt(position))) {
                position++;
            }
        }
        if (position == start) {
            throw error("invalid number");
        }
        try {
            return Double.parseDouble(source.substring(start, position));
        } catch (NumberFormatException e) {
            throw error("invalid number");
        }
    }

    private void expect(char expected) throws JsonSyntaxException {
        if (position >= source.length() || source.charAt(position) != expected) {
            throw error("expected '" + expected + "'");
        }
        position++;
    }

    private char peek() throws JsonSyntaxException {
        if (position >= source.length()) {
            throw error("unexpected end of input");
        }
        return source.charAt(position);
    }

    private void skipWhitespace() {
        while (position < source.length() && Character.isWhitespace(source.charAt(position))) {
            position++;
        }
    }

    private JsonSyntaxException error(String message) {
        return new JsonSyntaxException(message + " at position " + position);
    }
}
