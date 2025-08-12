package com.adverto.dejonghe.application.entities.product.product;

import org.springframework.context.ApplicationEvent;

public class ProductAddedEvent extends ApplicationEvent {
    private final Product product;

    public ProductAddedEvent(Object source, Product product) {
        super(source);
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }
}
