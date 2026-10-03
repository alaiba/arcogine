package com.arcogine.research.experiment;

/**
 * An inclusive range of supported-event sequence numbers. Sequences start at {@code 1}; the
 * no-event initial observation reports cursor {@code 0}, which is a position, not a sequence.
 */
public record SequenceRange(long from, long through) {

    public SequenceRange {
        if (from < 1 || through < from) {
            throw new IllegalArgumentException("invalid supported-event sequence range " + from + ".." + through);
        }
    }

    public long size() {
        return through - from + 1;
    }
}
