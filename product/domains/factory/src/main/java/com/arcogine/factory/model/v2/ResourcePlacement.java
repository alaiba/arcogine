package com.arcogine.factory.model.v2;

/**
 * The authored minimum-coordinate reference-cell position of a resource's footprint in a
 * {@code factory-model:v2} design.
 *
 * <p>The reference cell is the minimum-coordinate anchor of the footprint it is paired with in
 * {@link SpatialConfiguredResource} -- never a center point, arbitrary origin, sprite coordinate,
 * or connection point. Range validity ({@code x >= 0}, {@code y >= 0}) is deliberately not
 * enforced by this constructor; see {@link FactoryModelV2Validator}.
 */
public record ResourcePlacement(long x, long y) {}
