package com.arcogine.finance.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.types.SimTime;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class LedgerTest {

    @Test
    void balanceIsZeroWhenNoPostings() {
        Ledger ledger = new Ledger();
        assertEquals(0, ledger.balance(Account.CASH).compareTo(BigDecimal.ZERO));
        assertEquals(0, ledger.balance(Account.SALES).compareTo(BigDecimal.ZERO));
    }

    @Test
    void debitNormalAccountBalanceIsDebitsMinusCredits() {
        Ledger ledger = new Ledger();
        ledger.post(new JournalEntry(
                SimTime.ZERO,
                "sale",
                List.of(
                        new Posting(Account.CASH, Side.DEBIT, new BigDecimal("120.00")),
                        new Posting(Account.SALES, Side.CREDIT, new BigDecimal("120.00")))));

        assertEquals(0, ledger.balance(Account.CASH).compareTo(new BigDecimal("120.00")));
    }

    @Test
    void creditNormalAccountBalanceIsCreditsMinusDebits() {
        Ledger ledger = new Ledger();
        ledger.post(new JournalEntry(
                SimTime.ZERO,
                "sale",
                List.of(
                        new Posting(Account.CASH, Side.DEBIT, new BigDecimal("120.00")),
                        new Posting(Account.SALES, Side.CREDIT, new BigDecimal("120.00")))));

        assertEquals(0, ledger.balance(Account.SALES).compareTo(new BigDecimal("120.00")));
    }

    @Test
    void balancesAccumulateAcrossMultipleEntries() {
        Ledger ledger = new Ledger();
        for (String amount : List.of("120.00", "50.00", "30.00")) {
            ledger.post(new JournalEntry(
                    SimTime.ZERO,
                    "sale",
                    List.of(
                            new Posting(Account.CASH, Side.DEBIT, new BigDecimal(amount)),
                            new Posting(Account.SALES, Side.CREDIT, new BigDecimal(amount)))));
        }

        assertEquals(0, ledger.balance(Account.CASH).compareTo(new BigDecimal("200.00")));
        assertEquals(0, ledger.balance(Account.SALES).compareTo(new BigDecimal("200.00")));
    }

    @Test
    void entriesReturnsAnImmutableSnapshot() {
        Ledger ledger = new Ledger();
        assertTrue(ledger.entries().isEmpty());

        ledger.post(new JournalEntry(
                SimTime.ZERO,
                "sale",
                List.of(
                        new Posting(Account.CASH, Side.DEBIT, new BigDecimal("10.00")),
                        new Posting(Account.SALES, Side.CREDIT, new BigDecimal("10.00")))));

        assertEquals(1, ledger.entries().size());
    }
}
