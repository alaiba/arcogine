package com.arcogine.types;

public record ProductId(long value) implements Comparable<ProductId> {
    @Override
    public int compareTo(ProductId other) {
        return Long.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return "Product(" + value + ")";
    }
}
