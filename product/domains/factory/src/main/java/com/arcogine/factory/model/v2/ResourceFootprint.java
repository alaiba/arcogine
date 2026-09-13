package com.arcogine.factory.model.v2;

/**
 * The authored integer-cell extent a resource occupies from its {@link ResourcePlacement}
 * reference cell, in a {@code factory-model:v2} design.
 *
 * <p>A resource placed at reference cell {@code (x,y)} with footprint {@code (width,height)}
 * occupies exactly the integer cells {@code x..x+width-1} by {@code y..y+height-1}, per
 * ADR-0014. Range validity ({@code width >= 1}, {@code height >= 1}) is deliberately not
 * enforced by this constructor; see {@link FactoryModelV2Validator}.
 */
public record ResourceFootprint(long width, long height) {}
