package com.arcogine.types;

import java.util.OptionalLong;

public sealed interface Quantity {
    record Units(long count) implements Quantity {}
    record Volume(double liters) implements Quantity {}

    static Quantity units(long n) {
        return new Units(n);
    }

    static Quantity volume(double liters) {
        return new Volume(liters);
    }

    default OptionalLong asUnits() {
        return switch (this) {
            case Units u -> OptionalLong.of(u.count());
            case Volume v -> OptionalLong.empty();
        };
    }

    static Quantity defaultQuantity() {
        return new Units(0);
    }
}
