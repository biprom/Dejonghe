package com.adverto.dejonghe.application.entities.enums.product;

public enum VAT {
    EENENTWINTIG ("21%"),
    MEDECONTRACTANT   ("Medec");

    private final String discription;

    VAT(String discription) {
        this.discription = discription;
    }

    public String getDiscription() { return discription; }
}
