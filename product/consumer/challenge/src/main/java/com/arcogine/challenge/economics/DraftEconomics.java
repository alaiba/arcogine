package com.arcogine.challenge.economics;

/**
 * An immutable, derived snapshot of draft construction economics for one exact challenge budget,
 * catalogue, and set of draft equipment occurrences.
 *
 * <p>This is not mutable ledger state and does not model transactions, a wallet, or purchase/sale
 * history -- {@link DraftEconomicsCalculator} derives it fresh from its inputs each time it is
 * called.
 *
 * @param startingBudgetCredits the challenge's starting budget this snapshot was derived from
 * @param committedConstructionCostCredits sum of purchase costs of the priced draft occurrences
 * @param remainingBudgetCredits {@code startingBudgetCredits - committedConstructionCostCredits}
 */
public record DraftEconomics(
        long startingBudgetCredits,
        long committedConstructionCostCredits,
        long remainingBudgetCredits) {}
