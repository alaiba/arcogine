package com.arcogine.factory.model.v2;

/**
 * The authored plant-scope floor extent of a {@code factory-model:v2} design, in integer cells.
 *
 * <p>This is an authored Factory fact under ADR-0014 -- it is not Engine policy, runtime state,
 * transfer state, or animation/presentation geometry. Range validity ({@code width >= 1},
 * {@code height >= 1}) is deliberately not enforced by this constructor; see
 * {@link FactoryModelV2Validator} for why cross-field/business validation is centralized there
 * rather than distributed across value-object constructors.
 */
public record FactoryFloor(long width, long height) {}
