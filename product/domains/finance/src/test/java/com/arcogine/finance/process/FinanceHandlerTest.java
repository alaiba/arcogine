package com.arcogine.finance.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.finance.ledger.Account;
import com.arcogine.finance.ledger.JournalEntry;
import com.arcogine.finance.ledger.Posting;
import com.arcogine.finance.ledger.Side;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Finance is event-driven, not state-inspecting: it reacts only to OrderCompleted, the
 * operational fact Factory emits, and never reaches into Factory's state to infer transactions.
 */
class FinanceHandlerTest {

    @Test
    void noPostingExistsBeforeOrderCompleted() {
        FinanceHandler handler = new FinanceHandler();
        assertTrue(handler.ledger().entries().isEmpty());
    }

    @Test
    void exactlyOnePostingExistsAfterOrderCompleted() {
        FinanceHandler handler = new FinanceHandler();
        Scheduler sched = new Scheduler();

        Event orderCompleted = Event.of(
                SimTime.of(10), completion(1, 10, 12.0));
        handler.handleEvent(orderCompleted, sched);

        assertEquals(1, handler.ledger().entries().size());
    }

    @Test
    void immediateSettlementPostsCashDebitAndSalesCreditForOrderValue() {
        FinanceHandler handler = new FinanceHandler();
        Scheduler sched = new Scheduler();

        // quantity=10, unitPrice=12.0 -> orderValue=120.00
        Event orderCompleted = Event.of(
                SimTime.of(10), completion(1, 10, 12.0));
        handler.handleEvent(orderCompleted, sched);

        JournalEntry entry = handler.ledger().entries().get(0);
        assertEquals(2, entry.postings().size());

        Posting cashDebit = entry.postings().stream()
                .filter(p -> p.account() == Account.CASH)
                .findFirst()
                .orElseThrow();
        assertEquals(Side.DEBIT, cashDebit.side());
        assertEquals(0, cashDebit.amount().compareTo(new BigDecimal("120.00")));

        Posting salesCredit = entry.postings().stream()
                .filter(p -> p.account() == Account.SALES)
                .findFirst()
                .orElseThrow();
        assertEquals(Side.CREDIT, salesCredit.side());
        assertEquals(0, salesCredit.amount().compareTo(new BigDecimal("120.00")));

        assertEquals(0, handler.ledger().balance(Account.CASH).compareTo(new BigDecimal("120.00")));
        assertEquals(0, handler.ledger().balance(Account.SALES).compareTo(new BigDecimal("120.00")));
    }

    @Test
    void ignoresEventsOtherThanOrderCompleted() {
        FinanceHandler handler = new FinanceHandler();
        Scheduler sched = new Scheduler();

        handler.handleEvent(
                Event.of(SimTime.ZERO, new EventPayload.OrderCreation(new ProductId(1), 1, 5.0)), sched);
        handler.handleEvent(Event.of(
                SimTime.ZERO,
                new EventPayload.MachineAvailabilityChange(new MachineId(1), false)), sched);
        handler.handleEvent(Event.of(
                SimTime.ZERO,
                new EventPayload.TaskStart(new JobId(1), new MachineId(1), 0)), sched);

        assertTrue(handler.ledger().entries().isEmpty(), "only OrderCompleted should produce postings");
    }

    @Test
    void orderValueIsQuantizedToTheCanonicalCurrencyScaleAtTheBoundary() {
        FinanceHandler handler = new FinanceHandler();
        Scheduler sched = new Scheduler();

        // quantity=3, unitPrice=3.333 -> exact product 9.999, quantized (HALF_UP, scale 2) to 10.00.
        Event orderCompleted = Event.of(
                SimTime.of(1), completion(1, 3, 3.333));
        handler.handleEvent(orderCompleted, sched);

        assertEquals(0, handler.ledger().balance(Account.CASH).compareTo(new BigDecimal("10.00")));
    }

    @Test
    void eachCompletedOrderPostsItsOwnEntry() {
        FinanceHandler handler = new FinanceHandler();
        Scheduler sched = new Scheduler();

        handler.handleEvent(
                Event.of(SimTime.of(1), completion(1, 2, 10.0)),
                sched);
        handler.handleEvent(
                Event.of(SimTime.of(2), completion(2, 3, 5.0)),
                sched);

        assertEquals(2, handler.ledger().entries().size());
        assertEquals(0, handler.ledger().balance(Account.CASH).compareTo(new BigDecimal("35.00")));
    }

    @Test
    void deliveringTheSameOrderCompletedEventTwicePostsTwice() {
        // Documents the event-uniqueness assumption in FinanceHandler's class Javadoc:
        // FinanceHandler trusts each OrderCompleted it receives is a distinct completion and does
        // not de-duplicate, matching every other handler in the codebase. This is intentional,
        // not an oversight -- nothing today can actually redeliver an event (see the Javadoc for
        // why), so no guard exists. If that ever changes (e.g. a future event-replay feature),
        // this test is expected to be deliberately updated alongside adding a guard.
        FinanceHandler handler = new FinanceHandler();
        Scheduler sched = new Scheduler();
        Event orderCompleted = Event.of(
                SimTime.of(1), completion(1, 2, 10.0));

        handler.handleEvent(orderCompleted, sched);
        handler.handleEvent(orderCompleted, sched);

        assertEquals(2, handler.ledger().entries().size(), "no de-duplication: two deliveries, two postings");
        assertEquals(0, handler.ledger().balance(Account.CASH).compareTo(new BigDecimal("40.00")));
    }

    @Test
    void journalEntryIdentifiesTheCompletedOrderRatherThanItsCompletingChildJob() {
        FinanceHandler handler = new FinanceHandler();
        Scheduler sched = new Scheduler();

        handler.handleEvent(
                Event.of(SimTime.of(1), new EventPayload.OrderCompleted(
                        new OrderId(7), new JobId(21), new ProductId(1), 3, 10.0)),
                sched);

        assertEquals("Order 7 completed", handler.ledger().entries().getFirst().description());
    }

    /** An order-level completion whose completing child job identity differs from the order's. */
    private static EventPayload.OrderCompleted completion(long orderId, long quantity, double unitPrice) {
        return new EventPayload.OrderCompleted(
                new OrderId(orderId), new JobId(orderId * 10), new ProductId(1), quantity, unitPrice);
    }
}
