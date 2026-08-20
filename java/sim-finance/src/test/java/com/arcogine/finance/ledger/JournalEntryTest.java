package com.arcogine.finance.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/** The core financial invariant: sum(debits) == sum(credits), or the entry cannot be constructed. */
class JournalEntryTest {

    @Test
    void balancedEntryConstructsSuccessfully() {
        JournalEntry entry = new JournalEntry(
                SimTime.ZERO,
                "test",
                List.of(
                        new Posting(Account.CASH, Side.DEBIT, new BigDecimal("120.00")),
                        new Posting(Account.SALES, Side.CREDIT, new BigDecimal("120.00"))));

        assertEquals(2, entry.postings().size());
    }

    @Test
    void unbalancedEntryIsRejected() {
        SimError.UnbalancedJournalEntry error = assertThrows(
                SimError.UnbalancedJournalEntry.class,
                () -> new JournalEntry(
                        SimTime.ZERO,
                        "unbalanced",
                        List.of(
                                new Posting(Account.CASH, Side.DEBIT, new BigDecimal("120.00")),
                                new Posting(Account.SALES, Side.CREDIT, new BigDecimal("100.00")))));

        assertEquals("120.00", error.debits());
        assertEquals("100.00", error.credits());
    }

    @Test
    void entryWithNoPostingsIsRejected() {
        assertThrows(SimError.OutOfRange.class, () -> new JournalEntry(SimTime.ZERO, "empty", List.of()));
    }

    @Test
    void multiplePostingsOnEachSideCanBalance() {
        // Not exercised by the current immediate-settlement policy (always exactly one posting
        // per side), but the invariant is sum-based, not count-based -- confirm that holds.
        JournalEntry entry = new JournalEntry(
                SimTime.ZERO,
                "split",
                List.of(
                        new Posting(Account.CASH, Side.DEBIT, new BigDecimal("70.00")),
                        new Posting(Account.CASH, Side.DEBIT, new BigDecimal("50.00")),
                        new Posting(Account.SALES, Side.CREDIT, new BigDecimal("120.00"))));

        assertEquals(3, entry.postings().size());
    }
}
