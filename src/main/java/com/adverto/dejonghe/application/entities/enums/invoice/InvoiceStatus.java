package com.adverto.dejonghe.application.entities.enums.invoice;

public enum InvoiceStatus {
    AANGEMAAKT("Aangemaakt"),
    TO_CHECK ("Te controleren"),
    SENT ("Verstuurd"),
    REMINDER1   ("Herinnering1"),
    REMINDER2   ("Herinnering2"),
    REMINDER3   ("Herinnering3"),
    ADVANCE("Voorschot"),
    PAYED("Betaald");

    private final String discription;

    InvoiceStatus(String discription) {
        this.discription = discription;
    }

    public String getDiscription() { return discription; }
}
