package com.arcogine.types;

public record MachineId(long value) implements Comparable<MachineId> {
    @Override
    public int compareTo(MachineId other) {
        return Long.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return "Machine(" + value + ")";
    }
}
