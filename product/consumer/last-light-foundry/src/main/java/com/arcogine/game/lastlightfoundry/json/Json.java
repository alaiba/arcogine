package com.arcogine.game.lastlightfoundry.json;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A small, strict JSON codec for the game's HTTP adapter and bundled content.
 *
 * <p>Parsing yields {@link Map} (insertion ordered), {@link List}, {@link String}, {@link
 * Boolean}, {@code null}, and {@link BigDecimal} for every number so no integer is silently
 * narrowed through {@code double}. Writing emits maps, lists, strings, booleans, {@code null},
 * {@link Integer} and finite {@link Double} values as JSON numbers; any {@link Long} must be
 * converted by the caller to a decimal string (see {@link #num(long)}), which keeps every Java
 * {@code long} lossless across JavaScript's 2^53 limit. Non-finite doubles are written as the
 * strings {@code "NaN"}, {@code "Infinity"} and {@code "-Infinity"}, never as invalid JSON.
 */
public final class Json {

    private static final int MAX_DEPTH = 32;

    private Json() {}

    /** A malformed or unsupported JSON document. */
    public static final class JsonException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public JsonException(String message) {
            super(message);
        }
    }

    /** Lossless wire form of a Java {@code long}: a decimal string. */
    public static String num(long value) {
        return Long.toString(value);
    }

    /** Parses one JSON document; trailing non-whitespace content is an error. */
    public static Object parse(String source) {
        if (source == null) {
            throw new JsonException("source must not be null");
        }
        Parser parser = new Parser(source);
        parser.skipWhitespace();
        Object value = parser.readValue(0);
        parser.skipWhitespace();
        if (!parser.atEnd()) {
            throw new JsonException("unexpected trailing content at offset " + parser.position);
        }
        return value;
    }

    /** Serialises a value tree to compact JSON. */
    public static String write(Object value) {
        StringBuilder out = new StringBuilder();
        write(value, out);
        return out.toString();
    }

    private static void write(Object value, StringBuilder out) {
        if (value == null) {
            out.append("null");
        } else if (value instanceof String text) {
            writeString(text, out);
        } else if (value instanceof Boolean flag) {
            out.append(flag.booleanValue());
        } else if (value instanceof Integer number) {
            out.append(number.intValue());
        } else if (value instanceof Double number) {
            if (number.isNaN()) {
                writeString("NaN", out);
            } else if (number.isInfinite()) {
                writeString(number > 0 ? "Infinity" : "-Infinity", out);
            } else {
                out.append(BigDecimal.valueOf(number).toPlainString());
            }
        } else if (value instanceof Long) {
            throw new IllegalArgumentException("long values must be sent as decimal strings (Json.num)");
        } else if (value instanceof Map<?, ?> map) {
            out.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                writeString(String.valueOf(entry.getKey()), out);
                out.append(':');
                write(entry.getValue(), out);
            }
            out.append('}');
        } else if (value instanceof Iterable<?> items) {
            out.append('[');
            boolean first = true;
            for (Object item : items) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                write(item, out);
            }
            out.append(']');
        } else {
            throw new IllegalArgumentException("unsupported JSON value type: " + value.getClass().getName());
        }
    }

    private static void writeString(String text, StringBuilder out) {
        out.append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
    }

    /** Convenience for building ordered objects: {@code obj("a", 1, "b", "x")}. */
    public static Map<String, Object> obj(Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("obj requires key/value pairs");
        }
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    private static final class Parser {
        private final String source;
        private int position;

        Parser(String source) {
            this.source = source;
        }

        boolean atEnd() {
            return position >= source.length();
        }

        void skipWhitespace() {
            while (!atEnd()) {
                char c = source.charAt(position);
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                    position++;
                } else {
                    break;
                }
            }
        }

        Object readValue(int depth) {
            if (depth > MAX_DEPTH) {
                throw new JsonException("document is nested too deeply");
            }
            if (atEnd()) {
                throw new JsonException("unexpected end of input");
            }
            char c = source.charAt(position);
            switch (c) {
                case '{':
                    return readObject(depth);
                case '[':
                    return readArray(depth);
                case '"':
                    return readString();
                case 't':
                    expect("true");
                    return Boolean.TRUE;
                case 'f':
                    expect("false");
                    return Boolean.FALSE;
                case 'n':
                    expect("null");
                    return null;
                default:
                    return readNumber();
            }
        }

        private void expect(String literal) {
            if (!source.startsWith(literal, position)) {
                throw new JsonException("unexpected token at offset " + position);
            }
            position += literal.length();
        }

        private Map<String, Object> readObject(int depth) {
            position++;
            Map<String, Object> map = new LinkedHashMap<>();
            skipWhitespace();
            if (peek() == '}') {
                position++;
                return map;
            }
            while (true) {
                skipWhitespace();
                if (peek() != '"') {
                    throw new JsonException("object key must be a string at offset " + position);
                }
                String key = readString();
                skipWhitespace();
                if (peek() != ':') {
                    throw new JsonException("expected ':' at offset " + position);
                }
                position++;
                skipWhitespace();
                if (map.containsKey(key)) {
                    throw new JsonException("duplicate object key: " + key);
                }
                map.put(key, readValue(depth + 1));
                skipWhitespace();
                char next = peek();
                position++;
                if (next == '}') {
                    return map;
                }
                if (next != ',') {
                    throw new JsonException("expected ',' or '}' at offset " + (position - 1));
                }
            }
        }

        private List<Object> readArray(int depth) {
            position++;
            List<Object> list = new ArrayList<>();
            skipWhitespace();
            if (peek() == ']') {
                position++;
                return list;
            }
            while (true) {
                skipWhitespace();
                list.add(readValue(depth + 1));
                skipWhitespace();
                char next = peek();
                position++;
                if (next == ']') {
                    return list;
                }
                if (next != ',') {
                    throw new JsonException("expected ',' or ']' at offset " + (position - 1));
                }
            }
        }

        private char peek() {
            if (atEnd()) {
                throw new JsonException("unexpected end of input");
            }
            return source.charAt(position);
        }

        private String readString() {
            position++;
            StringBuilder text = new StringBuilder();
            while (true) {
                if (atEnd()) {
                    throw new JsonException("unterminated string");
                }
                char c = source.charAt(position++);
                if (c == '"') {
                    return text.toString();
                }
                if (c < 0x20) {
                    throw new JsonException("control character in string at offset " + (position - 1));
                }
                if (c != '\\') {
                    text.append(c);
                    continue;
                }
                if (atEnd()) {
                    throw new JsonException("unterminated escape");
                }
                char escape = source.charAt(position++);
                switch (escape) {
                    case '"' -> text.append('"');
                    case '\\' -> text.append('\\');
                    case '/' -> text.append('/');
                    case 'b' -> text.append('\b');
                    case 'f' -> text.append('\f');
                    case 'n' -> text.append('\n');
                    case 'r' -> text.append('\r');
                    case 't' -> text.append('\t');
                    case 'u' -> {
                        if (position + 4 > source.length()) {
                            throw new JsonException("truncated unicode escape");
                        }
                        try {
                            text.append((char) Integer.parseInt(source.substring(position, position + 4), 16));
                        } catch (NumberFormatException e) {
                            throw new JsonException("invalid unicode escape at offset " + position);
                        }
                        position += 4;
                    }
                    default -> throw new JsonException("invalid escape at offset " + (position - 1));
                }
            }
        }

        private BigDecimal readNumber() {
            int start = position;
            while (!atEnd()) {
                char c = source.charAt(position);
                if ((c >= '0' && c <= '9') || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') {
                    position++;
                } else {
                    break;
                }
            }
            if (start == position) {
                throw new JsonException("unexpected character at offset " + start);
            }
            try {
                return new BigDecimal(source.substring(start, position));
            } catch (NumberFormatException e) {
                throw new JsonException("invalid number at offset " + start);
            }
        }
    }

    // ---- typed accessors used by request/content decoding ----

    /** Returns {@code value} as an object, or throws. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> asObject(Object value, String path) {
        if (!(value instanceof Map<?, ?>)) {
            throw new JsonException(path + " must be an object");
        }
        return (Map<String, Object>) value;
    }

    /** Returns {@code value} as an array, or throws. */
    @SuppressWarnings("unchecked")
    public static List<Object> asArray(Object value, String path) {
        if (!(value instanceof List<?>)) {
            throw new JsonException(path + " must be an array");
        }
        return (List<Object>) value;
    }

    /** Returns the required string member, or throws. */
    public static String string(Map<String, Object> object, String key, String path) {
        Object value = object.get(key);
        if (!(value instanceof String text)) {
            throw new JsonException(path + "." + key + " must be a string");
        }
        return text;
    }

    /** Returns the required integral member within the 32-bit range, or throws. */
    public static int integer(Map<String, Object> object, String key, String path) {
        return (int) integral(object, key, path, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /** Returns the required integral member within {@code [min, max]}, or throws. */
    public static long integral(Map<String, Object> object, String key, String path, long min, long max) {
        Object value = object.get(key);
        if (value instanceof String text) {
            // Large integers travel as decimal strings; accept that same form on input.
            try {
                value = new BigDecimal(text);
            } catch (NumberFormatException e) {
                throw new JsonException(path + "." + key + " must be an integer");
            }
        }
        if (!(value instanceof BigDecimal number)) {
            throw new JsonException(path + "." + key + " must be an integer");
        }
        try {
            long result = number.longValueExact();
            if (result < min || result > max) {
                throw new JsonException(path + "." + key + " must be within [" + min + ", " + max + "]");
            }
            return result;
        } catch (ArithmeticException e) {
            throw new JsonException(path + "." + key + " must be an integer within the 64-bit range");
        }
    }
}
