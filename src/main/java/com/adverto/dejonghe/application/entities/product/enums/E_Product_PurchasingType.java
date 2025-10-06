package com.adverto.dejonghe.application.entities.product.enums;

public enum E_Product_PurchasingType {
    ONCE("Enkelvoudig"),
    BULK("Bulk");
    public final String label;

    private E_Product_PurchasingType(String label) {
        this.label = label;
    }
}
