package com.adverto.dejonghe.application.entities.enums.workorder;

public enum WorkType {
    GENERAL ("Algemeen"),
    CENTRIFUGE   ("Centrifuge"),
    PROGRAMMATIC ("Programmatie"),
    PICKUP ("Afhaling");

    private final String discription;

    WorkType(String discription) {
        this.discription = discription;
    }

    public String getDiscription() { return discription; }
}
