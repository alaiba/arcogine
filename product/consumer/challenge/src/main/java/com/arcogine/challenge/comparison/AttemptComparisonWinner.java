package com.arcogine.challenge.comparison;

/**
 * Which of two compared attempts scored better under their shared evaluation policy.
 *
 * <p>This ordering only ever applies within one {@code AttemptComparison}, whose compatibility
 * check already guarantees both attempts share the same evaluation-policy identity/version. It is
 * derived solely from that policy's own score, never from a game-invented cross-policy ranking.
 */
public enum AttemptComparisonWinner {
    FIRST,
    SECOND,
    TIE
}
