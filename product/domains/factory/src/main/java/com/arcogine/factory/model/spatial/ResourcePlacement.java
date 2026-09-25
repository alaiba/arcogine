package com.arcogine.factory.model.spatial;

/**
 * The authored minimum-coordinate reference-cell position of a resource's footprint in a
 * {@link SpatialRecord}.
 *
 * <p>The reference cell is the minimum-coordinate anchor of the footprint it is paired with in
 * {@link ResourceLayout} -- never a center point, arbitrary origin, sprite coordinate, or
 * connection point. Range validity ({@code x >= 0}, {@code y >= 0}) is deliberately not enforced
 * by this constructor; see {@link com.arcogine.factory.model.validation.FactoryModelValidator}.
 */
public record ResourcePlacement(long x, long y) {}
