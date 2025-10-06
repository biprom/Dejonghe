package com.adverto.dejonghe.application.entities.product.enums;

public enum E_Product_Level {
    PRODUCT("product"),
    PRODUCTLEVEL1("niveau 1"),
    PRODUCTLEVEL2("niveau 2"),
    PRODUCTLEVEL3("niveau 3"),
    PRODUCTLEVEL4("niveau 4"),
    PRODUCTLEVEL5("niveau 5"),
    PRODUCTLEVEL6("niveau 6"),
    PRODUCTLEVEL7("niveau 7"),;

    public final String label;

    private E_Product_Level(String label) {
        this.label = label;
    }
}
