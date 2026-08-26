package com.arcogine.types;

public record OrderId(long value) implements Comparable<OrderId> {
    @Override
    public int compareTo(OrderId other) {
        return Long.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return "Order(" + value + ")";
    }
}
