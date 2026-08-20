package com.arcogine.finance.ledger;

import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.math.BigDecimal;
import java.util.List;

/**
 * An immutable, balanced double-entry record: sum(debit postings) must equal sum(credit
 * postings), enforced here so that an unbalanced entry can never enter financial state at all --
 * there is no way to construct one.
 */
public record JournalEntry(SimTime time, String description, List<Posting> postings) {

    public JournalEntry {
        if (postings == null || postings.isEmpty()) {
            throw new SimError.OutOfRange("postings", "journal entry must have at least one posting");
        }
        postings = List.copyOf(postings);

        BigDecimal debits = sum(postings, Side.DEBIT);
        BigDecimal credits = sum(postings, Side.CREDIT);
        if (debits.compareTo(credits) != 0) {
            throw new SimError.UnbalancedJournalEntry(debits.toString(), credits.toString(), description);
        }
    }

    private static BigDecimal sum(List<Posting> postings, Side side) {
        return postings.stream()
                .filter(p -> p.side() == side)
                .map(Posting::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
