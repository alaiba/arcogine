package com.arcogine.economy.pricing;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.handler.EventHandler;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.types.SimError;
import java.util.ArrayList;
import java.util.List;

public class PricingState implements EventHandler {

    public record PricePoint(long tick, double price) {}

    private double currentPrice;
    private final List<PricePoint> priceHistory;

    public PricingState(double initialPrice) {
        this.currentPrice = initialPrice;
        this.priceHistory = new ArrayList<>();
        this.priceHistory.add(new PricePoint(0, initialPrice));
    }

    public void setPrice(double price, long tick) {
        this.currentPrice = price;
        this.priceHistory.add(new PricePoint(tick, price));
    }

    public double currentPrice() {
        return currentPrice;
    }

    public List<PricePoint> priceHistory() {
        return List.copyOf(priceHistory);
    }

    @Override
    public void handleEvent(Event event, Scheduler scheduler) throws SimError {
        switch (event.payload()) {
            case EventPayload.PriceChange pc -> setPrice(pc.newPrice(), event.time().ticks());
            default -> {}
        }
    }
}
