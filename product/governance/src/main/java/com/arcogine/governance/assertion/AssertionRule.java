package com.arcogine.governance.assertion;

/**
 * How an {@link Assertion} determines whether authoritative state {@code T} satisfies it.
 *
 * <p>An {@code AssertionRule} is deliberately not the assertion's identity -- {@link
 * Assertion#equals(Object)} never considers it, only {@link AssertionId}/{@link
 * AssertionVersion}. This lets an assertion's implementation be replaced (rewritten, moved,
 * re-expressed) without silently becoming "a different assertion" to anything that only holds a
 * durable identity/version reference.
 *
 * <p>This narrow evaluation seam exists only to let G3 prove deterministic executable semantics
 * for structural assertions; it is not the G4 conformance-evaluation engine.
 */
@FunctionalInterface
public interface AssertionRule<T> {
    StructuralAssertionOutcome evaluate(T authoritativeState);
}
