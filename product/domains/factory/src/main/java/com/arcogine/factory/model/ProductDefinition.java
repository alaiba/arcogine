package com.arcogine.factory.model;

import com.arcogine.types.ProductId;

/** A product/material that is produced by routing it through one {@link OperationDefinition}. */
public record ProductDefinition(ProductId id, String name, long operationId) {

    public ProductDefinition {
        if (id == null) {
            throw new NullPointerException("id");
        }
        if (name == null) {
            throw new NullPointerException("name");
        }
    }
}
