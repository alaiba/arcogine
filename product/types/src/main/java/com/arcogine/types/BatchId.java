package com.arcogine.types;

public record BatchId(long value) implements Comparable<BatchId> {
    @Override
    public int compareTo(BatchId other) {
        return Long.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return "Batch(" + value + ")";
    }
}
