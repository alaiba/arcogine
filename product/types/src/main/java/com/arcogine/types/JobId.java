package com.arcogine.types;

public record JobId(long value) implements Comparable<JobId> {
    @Override
    public int compareTo(JobId other) {
        return Long.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return "Job(" + value + ")";
    }
}
