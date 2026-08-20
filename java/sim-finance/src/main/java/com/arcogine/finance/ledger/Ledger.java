package com.arcogine.finance.ledger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Finance-owned mutable state. Only mutates by appending an already-balanced {@link JournalEntry}
 * -- there is no other way to change a balance, so debits==credits holds for every entry ever
 * posted, not just checked once.
 */
public class Ledger implements LedgerView {

    private final List<JournalEntry> entries = new ArrayList<>();

    public void post(JournalEntry entry) {
        entries.add(entry);
    }

    @Override
    public List<JournalEntry> entries() {
        return List.copyOf(entries);
    }

    @Override
    public BigDecimal balance(Account account) {
        BigDecimal debits = sumFor(account, Side.DEBIT);
        BigDecimal credits = sumFor(account, Side.CREDIT);
        return account.normalSide() == Side.DEBIT ? debits.subtract(credits) : credits.subtract(debits);
    }

    private BigDecimal sumFor(Account account, Side side) {
        return entries.stream()
                .flatMap(e -> e.postings().stream())
                .filter(p -> p.account() == account && p.side() == side)
                .map(Posting::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
