package com.adverto.dejonghe.application.entities.enums.product;

public enum VAT {
    EENENTWINTIG ("21%", 21.0),
    VERLEGD   ("0%", 0.0);

    private final String discription;
    private final Double value;

    VAT(String discription, Double value) {
        this.discription = discription;
        this.value = value;
    }

    public String getDiscription() { return discription; }
    public Double getValue() { return value; }
}
