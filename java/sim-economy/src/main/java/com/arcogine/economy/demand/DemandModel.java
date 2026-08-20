package com.arcogine.economy.demand;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.handler.EventHandler;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.List;
import java.util.Random;
import java.util.function.DoubleSupplier;

public class DemandModel implements EventHandler {

    private final double baseDemand;
    private final double priceElasticity;
    private final double leadTimeSensitivity;
    private final DoubleSupplier offerPrice;
    private final DoubleSupplier avgLeadTime;
    private final List<ProductId> productIds;
    private final Random rng;

    /**
     * offerPrice and avgLeadTime are read on demand from their owning subsystems
     * ({@code PricingState}/{@code FactoryHandler}) rather than pushed in as copies -- DemandModel
     * has no state of its own to keep in sync. A {@link DoubleSupplier} rather than a direct
     * dependency keeps sim-economy from having to depend on sim-factory just to read lead time.
     */
    public DemandModel(
            double baseDemand,
            double priceElasticity,
            double leadTimeSensitivity,
            DoubleSupplier offerPrice,
            DoubleSupplier avgLeadTime,
            List<ProductId> productIds,
            Random rng) {
        this.baseDemand = baseDemand;
        this.priceElasticity = priceElasticity;
        this.leadTimeSensitivity = leadTimeSensitivity;
        this.offerPrice = offerPrice;
        this.avgLeadTime = avgLeadTime;
        this.productIds = List.copyOf(productIds);
        this.rng = rng;
    }

    public double computeDemand() {
        double demand = baseDemand
                - priceElasticity * offerPrice.getAsDouble()
                - leadTimeSensitivity * avgLeadTime.getAsDouble();
        return Math.max(0.0, demand);
    }

    public long generateOrders(Scheduler scheduler) throws SimError {
        double expected = computeDemand();
        SimTime currentTime = scheduler.currentTime();

        long baseOrders = (long) Math.floor(expected);
        double fractional = expected - baseOrders;
        long extra = rng.nextDouble() < fractional ? 1 : 0;
        long orderCount = baseOrders + extra;

        for (long i = 0; i < orderCount; i++) {
            if (productIds.isEmpty()) {
                break;
            }
            ProductId productId = productIds.get(rng.nextInt(productIds.size()));
            long quantity = rng.nextInt(10) + 1L;
            scheduler.schedule(Event.of(
                    currentTime,
                    new EventPayload.OrderCreation(productId, quantity, offerPrice.getAsDouble())));
        }

        return orderCount;
    }

    @Override
    public void handleEvent(Event event, Scheduler scheduler) throws SimError {
        switch (event.payload()) {
            case EventPayload.DemandEvaluation ignored -> generateOrders(scheduler);
            default -> {}
        }
    }
}
