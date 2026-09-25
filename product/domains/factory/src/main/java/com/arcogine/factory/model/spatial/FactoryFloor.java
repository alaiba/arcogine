package com.arcogine.factory.model.spatial;

/**
 * The authored plant-scope floor extent of a {@link SpatialRecord}, in integer cells.
 *
 * <p>This is an authored Factory fact under docs/architecture/factory-model.md -- it is not Engine
 * policy, runtime state, transfer state, or animation/presentation geometry. Range validity
 * ({@code width >= 1}, {@code height >= 1}) is deliberately not enforced by this constructor; see
 * {@link com.arcogine.factory.model.validation.FactoryModelValidator} for why cross-field/business
 * validation is centralized there rather than distributed across value-object constructors.
 */
public record FactoryFloor(long width, long height) {}
