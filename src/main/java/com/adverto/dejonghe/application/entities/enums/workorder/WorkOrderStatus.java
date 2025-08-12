package com.adverto.dejonghe.application.entities.enums.workorder;

public enum WorkOrderStatus {
    RUNNING ("Lopend"),
    FINISHED   ("Afgewerkt");

    private final String discription;

    WorkOrderStatus(String discription) {
        this.discription = discription;
    }

    public String getDiscription() { return discription; }
}
