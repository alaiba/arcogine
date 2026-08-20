package com.arcogine.finance.ledger;

/**
 * The chart of accounts is deliberately tiny -- just enough for the initial immediate-settlement
 * policy (an order completing posts DR Cash / CR Sales). Each account has a "normal side": the
 * side that increases its balance, matching standard double-entry convention (Cash is an asset,
 * debit-normal; Sales is revenue, credit-normal).
 */
public enum Account {
    CASH(Side.DEBIT),
    SALES(Side.CREDIT);

    private final Side normalSide;

    Account(Side normalSide) {
        this.normalSide = normalSide;
    }

    public Side normalSide() {
        return normalSide;
    }
}
