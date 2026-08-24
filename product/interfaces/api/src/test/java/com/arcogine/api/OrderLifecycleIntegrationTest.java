package com.arcogine.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.api.state.HandlerFactory;
import com.arcogine.api.state.IntegratedHandler;
import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.event.EventType;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.core.runner.SimResult;
import com.arcogine.core.runner.SimRunner;
import com.arcogine.core.scenario.ScenarioLoader;
import com.arcogine.finance.ledger.Account;
import com.arcogine.finance.ledger.JournalEntry;
import com.arcogine.finance.ledger.Posting;
import com.arcogine.finance.ledger.Side;
import com.arcogine.types.JobId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import com.arcogine.types.scenario.ScenarioConfig;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Exercises the full causal chain end to end -- OrderCreation -> Factory -> OrderCompleted ->
 * Finance -> Ledger -- through the real, wired IntegratedHandler, rather than testing each
 * handler in isolation with hand-constructed events (as FactoryHandlerTest/FinanceHandlerTest do)
 * or only checking aggregate outcomes after a full scenario run (as ScenarioBaselinesTest does).
 * This is what actually proves the chain is wired correctly end to end.
 */
class OrderLifecycleIntegrationTest {

    private static final String SINGLE_MACHINE_SCENARIO =
            """
            [simulation]
            rng_seed = 1
            max_ticks = 500
            demand_eval_interval = 1000
            agent_eval_interval = 1000

            [[equipment]]
            id = 1
            name = "Mill"

            [[material]]
            id = 1
            name = "Widget"
            routing_id = 1

            [[process_segment]]
            id = 1
            name = "Milling"
            equipment_id = 1
            duration = 5

            [[operations_definition]]
            id = 1
            name = "Widget routing"
            steps = [1]

            [economy]
            initial_price = 12.0
            base_demand = 0.0
            price_elasticity = 0.0
            lead_time_sensitivity = 0.0
            """;

    @Test
    void orderCreationFlowsThroughFactoryToExactlyOneFinancePosting() {
        ScenarioConfig config = ScenarioLoader.loadScenario(SINGLE_MACHINE_SCENARIO);
        IntegratedHandler handler = HandlerFactory.buildFromConfig(config);
        Scheduler scheduler = new Scheduler();

        assertTrue(handler.finance().ledger().entries().isEmpty(), "no posting before any order exists");

        Event order = Event.of(SimTime.ZERO, new EventPayload.OrderCreation(new ProductId(1), 10, 12.0));
        scheduler.schedule(order);
        scheduler.nextEvent();
        handler.handleEvent(order, scheduler);

        assertTrue(handler.finance().ledger().entries().isEmpty(), "no posting until the order completes");

        int completedCount = 0;
        Event next;
        while ((next = scheduler.nextEvent().orElse(null)) != null) {
            handler.handleEvent(next, scheduler);
            if (next.payload() instanceof EventPayload.OrderCompleted) {
                completedCount++;
            }
        }

        assertEquals(1, completedCount, "exactly one OrderCompleted for the one order created");
        assertEquals(1, handler.factory().completedSales());
        assertEquals(1, handler.finance().ledger().entries().size(), "exactly one journal entry posted");

        JournalEntry entry = handler.finance().ledger().entries().get(0);
        Posting cashDebit = entry.postings().stream()
                .filter(p -> p.account() == Account.CASH)
                .findFirst()
                .orElseThrow();
        assertEquals(Side.DEBIT, cashDebit.side());
        assertEquals(0, cashDebit.amount().compareTo(new BigDecimal("120.00")), "10 units at $12");

        assertEquals(
                0,
                handler.finance().ledger().balance(Account.CASH).compareTo(new BigDecimal("120.00")));
        assertEquals(
                0,
                handler.finance().ledger().balance(Account.SALES).compareTo(new BigDecimal("120.00")));
    }

    @Test
    void multipleOrdersCompletingAtTheSameTickEachPostTheirOwnEntry() {
        ScenarioConfig config = ScenarioLoader.loadScenario(SINGLE_MACHINE_SCENARIO);
        IntegratedHandler handler = HandlerFactory.buildFromConfig(config);
        Scheduler scheduler = new Scheduler();

        // Two single-step orders on independent machines would complete at the same tick; this
        // scenario has one machine, so drive two TaskEnd-equivalent completions manually at the
        // same SimTime by completing two already-in-progress jobs' final steps together instead --
        // simplest reliable way to get two OrderCompleted events at one tick without a second
        // machine in the scenario config.
        Event orderA = Event.of(SimTime.ZERO, new EventPayload.OrderCreation(new ProductId(1), 2, 10.0));
        scheduler.schedule(orderA);
        scheduler.nextEvent();
        handler.handleEvent(orderA, scheduler);

        // Order A's TaskEnd is scheduled for t=5. Manually enqueue a second, independent
        // OrderCompleted for a different job at that same tick to exercise the same-tick case
        // deterministically (job id 2 need not have gone through the full factory pipeline for
        // this -- FinanceHandler only reacts to the event itself).
        Event syntheticSecondCompletion = Event.of(
                SimTime.of(5), new EventPayload.OrderCompleted(new JobId(2), new ProductId(1), 3, 5.0));
        scheduler.schedule(syntheticSecondCompletion);

        int completedCount = 0;
        Event next;
        while ((next = scheduler.nextEvent().orElse(null)) != null) {
            handler.handleEvent(next, scheduler);
            if (next.payload() instanceof EventPayload.OrderCompleted) {
                completedCount++;
            }
        }

        assertEquals(2, completedCount);
        assertEquals(2, handler.finance().ledger().entries().size(), "each completion posts its own entry");
        assertEquals(
                0,
                handler.finance().ledger().balance(Account.CASH).compareTo(new BigDecimal("35.00")),
                "20.00 (order A) + 15.00 (synthetic order) = 35.00");
    }

    @Test
    void everyOrderCompletedEventCorrespondsToExactlyOneJournalEntryAcrossAFullScenario() throws SimError {
        ScenarioConfig config = ScenarioLoader.loadScenario(
                """
                [simulation]
                rng_seed = 42
                max_ticks = 500
                demand_eval_interval = 10
                agent_eval_interval = 50

                [[equipment]]
                id = 1
                name = "Mill"
                [[equipment]]
                id = 2
                name = "Lathe"

                [[material]]
                id = 1
                name = "Widget"
                routing_id = 1

                [[process_segment]]
                id = 1
                name = "Milling"
                equipment_id = 1
                duration = 5
                [[process_segment]]
                id = 2
                name = "Turning"
                equipment_id = 2
                duration = 3

                [[operations_definition]]
                id = 1
                name = "Widget routing"
                steps = [1, 2]

                [economy]
                initial_price = 5.0
                base_demand = 3.0
                price_elasticity = 0.3
                lead_time_sensitivity = 0.05
                """);
        IntegratedHandler handler = HandlerFactory.buildFromConfig(config);
        SimResult result = SimRunner.runScenario(config, handler);

        long orderCompletedEvents = result.eventLog().filterByType(EventType.OrderCompleted).count();

        assertTrue(orderCompletedEvents > 0, "scenario should complete at least one order");
        assertEquals(orderCompletedEvents, handler.factory().completedSales());
        assertEquals(
                orderCompletedEvents,
                handler.finance().ledger().entries().size(),
                "every OrderCompleted event corresponds to exactly one journal entry, no more, no fewer");

        for (JournalEntry entry : handler.finance().ledger().entries()) {
            List<Posting> postings = entry.postings();
            assertEquals(2, postings.size(), "each entry is exactly DR Cash / CR Sales, one posting each");
        }
    }
}
