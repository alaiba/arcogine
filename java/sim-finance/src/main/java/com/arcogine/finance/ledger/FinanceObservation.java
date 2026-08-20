package com.arcogine.finance.ledger;

import java.math.BigDecimal;

/**
 * Read-only, purpose-specific projection of {@link Ledger} state for consumers that need to
 * observe financial position (e.g. a future FinanceAgent, or an API/UI display) without reaching
 * into the ledger's mutable posting history. Mirrors the role {@code AgentObservation} plays for
 * {@code SalesAgent} -- a separate observation type per domain, not a shared "WorldState".
 */
public record FinanceObservation(BigDecimal cash, BigDecimal sales) {

    public static FinanceObservation from(LedgerView ledger) {
        return new FinanceObservation(ledger.balance(Account.CASH), ledger.balance(Account.SALES));
    }
}
