package com.arcogine.finance.ledger;

import java.math.BigDecimal;
import java.util.List;

/**
 * Read-only view of a {@link Ledger} -- deliberately excludes {@code post(JournalEntry)}. Only
 * {@code FinanceHandler} (which holds the concrete {@link Ledger}) can post; everything else,
 * including anything obtaining a reference via {@code FinanceHandler.ledger()}, is structurally
 * limited to reading, not just conventionally expected to. This is what makes "Finance owns
 * ledger mutation" a compile-time property rather than a social one.
 */
public interface LedgerView {

    List<JournalEntry> entries();

    /** Balance in the account's own normal direction (positive = more of what that account tracks). */
    BigDecimal balance(Account account);
}
