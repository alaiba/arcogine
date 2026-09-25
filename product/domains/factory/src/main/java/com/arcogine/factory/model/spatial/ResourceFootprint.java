package com.arcogine.factory.model.spatial;

/**
 * The authored integer-cell extent a resource occupies from its {@link ResourcePlacement}
 * reference cell, in a {@link SpatialRecord}.
 *
 * <p>A resource placed at reference cell {@code (x,y)} with footprint {@code (width,height)}
 * occupies exactly the integer cells {@code x..x+width-1} by {@code y..y+height-1}, per
 * docs/architecture/factory-model.md. Range validity ({@code width >= 1}, {@code height >= 1}) is
 * deliberately not enforced by this constructor; see
 * {@link com.arcogine.factory.model.validation.FactoryModelValidator}.
 */
public record ResourceFootprint(long width, long height) {}
