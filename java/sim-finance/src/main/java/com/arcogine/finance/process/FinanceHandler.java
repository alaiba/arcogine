package com.arcogine.finance.process;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.handler.EventHandler;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.finance.ledger.Account;
import com.arcogine.finance.ledger.JournalEntry;
import com.arcogine.finance.ledger.Ledger;
import com.arcogine.finance.ledger.Posting;
import com.arcogine.finance.ledger.Side;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Finance's event-driven state owner. Reacts only to {@link EventPayload.OrderCompleted} -- the
 * operational fact Factory emits -- and interprets it financially; it never inspects Factory's
 * mutable state to infer what happened. Under the initial, deliberately simple immediate-
 * settlement policy, a completed order posts exactly DR Cash / CR Sales for its order value.
 */
public class FinanceHandler implements EventHandler {

    private final Ledger ledger = new Ledger();

    public Ledger ledger() {
        return ledger;
    }

    @Override
    public void handleEvent(Event event, Scheduler scheduler) throws SimError {
        switch (event.payload()) {
            case EventPayload.OrderCompleted oc -> postImmediateSettlement(oc, event.time());
            default -> {}
        }
    }

    private void postImmediateSettlement(EventPayload.OrderCompleted oc, SimTime time) {
        BigDecimal amount = orderValue(oc.quantity(), oc.unitPrice());
        ledger.post(new JournalEntry(
                time,
                "Order " + oc.orderId().value() + " completed",
                List.of(
                        new Posting(Account.CASH, Side.DEBIT, amount),
                        new Posting(Account.SALES, Side.CREDIT, amount))));
    }

    /**
     * Converts the event's double orderValue to a precise BigDecimal at this one boundary --
     * economic/commercial calculations upstream stay double; only the ledger, where the
     * debits==credits invariant is actually checked, needs exactness.
     */
    private static BigDecimal orderValue(long quantity, double unitPrice) {
        return BigDecimal.valueOf(unitPrice)
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
