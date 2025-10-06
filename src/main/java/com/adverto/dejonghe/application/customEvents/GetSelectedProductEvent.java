package com.adverto.dejonghe.application.customEvents;

import com.adverto.dejonghe.application.entities.product.product.Product;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

public class GetSelectedProductEvent extends ApplicationEvent {
    private final Product selectedProduct;

    public GetSelectedProductEvent(Object source, Product selectedProduct) {
        super(source);
        this.selectedProduct = selectedProduct;
    }

    public Product getSelectedProduct() {
        return selectedProduct;
    }
}
