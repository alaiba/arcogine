package com.arcogine.factory.routing;

import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import java.util.ArrayList;
import java.util.List;

public class RoutingStore {

    private final List<Routing> routings;
    private final List<ProductRouting> productRouting;

    public RoutingStore() {
        this.routings = new ArrayList<>();
        this.productRouting = new ArrayList<>();
    }

    public void addRouting(Routing routing) {
        routings.add(routing);
    }

    public void addProductRouting(ProductId productId, long routingId) {
        productRouting.add(new ProductRouting(productId, routingId));
    }

    public Routing getRoutingForProduct(ProductId productId) {
        long routingId = productRouting.stream()
                .filter(pr -> pr.productId().equals(productId))
                .map(ProductRouting::routingId)
                .findFirst()
                .orElseThrow(() -> new SimError.UnknownId("product routing", productId.value()));

        return routings.stream()
                .filter(r -> r.id() == routingId)
                .findFirst()
                .orElseThrow(() -> new SimError.UnknownId("routing", routingId));
    }

    public Routing getRouting(long id) {
        return routings.stream()
                .filter(r -> r.id() == id)
                .findFirst()
                .orElseThrow(() -> new SimError.UnknownId("routing", id));
    }

    private record ProductRouting(ProductId productId, long routingId) {}
}
