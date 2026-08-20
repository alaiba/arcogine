package com.arcogine.finance.ledger;

import com.arcogine.types.SimError;
import java.math.BigDecimal;
import java.util.Objects;

public record Posting(Account account, Side side, BigDecimal amount) {

    public Posting {
        Objects.requireNonNull(account, "account");
        Objects.requireNonNull(side, "side");
        Objects.requireNonNull(amount, "amount");
        if (amount.signum() <= 0) {
            throw new SimError.OutOfRange("amount", "posting amount must be positive, got " + amount);
        }
    }
}
