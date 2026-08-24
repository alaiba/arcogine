package com.arcogine.finance.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.arcogine.types.SimTime;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class FinanceObservationTest {

    @Test
    void reflectsLedgerBalances() {
        Ledger ledger = new Ledger();
        ledger.post(new JournalEntry(
                SimTime.ZERO,
                "sale",
                List.of(
                        new Posting(Account.CASH, Side.DEBIT, new BigDecimal("42.00")),
                        new Posting(Account.SALES, Side.CREDIT, new BigDecimal("42.00")))));

        FinanceObservation observation = FinanceObservation.from(ledger);

        assertEquals(0, observation.cash().compareTo(new BigDecimal("42.00")));
        assertEquals(0, observation.sales().compareTo(new BigDecimal("42.00")));
    }
}
