package com.adverto.dejonghe.application.customEvents;

import com.adverto.dejonghe.application.entities.product.product.Product;
import org.springframework.context.ApplicationEvent;

public class AddRemoveProductEvent extends ApplicationEvent {
    private final String message;
    private final Product selectedProduct;

    public AddRemoveProductEvent(Object source, String message, Product selectedProduct) {
        super(source);
        this.message = message;
        this.selectedProduct = selectedProduct;
    }

    public String getMessage() {
        return message;
    }
    public Product getSelectedProduct() {return selectedProduct;}
}
