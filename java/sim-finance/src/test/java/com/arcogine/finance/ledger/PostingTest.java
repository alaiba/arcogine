package com.arcogine.finance.ledger;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arcogine.types.SimError;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PostingTest {

    @Test
    void zeroAmountIsRejected() {
        assertThrows(
                SimError.OutOfRange.class,
                () -> new Posting(Account.CASH, Side.DEBIT, BigDecimal.ZERO));
    }

    @Test
    void negativeAmountIsRejected() {
        assertThrows(
                SimError.OutOfRange.class,
                () -> new Posting(Account.CASH, Side.DEBIT, new BigDecimal("-5.00")));
    }
}
