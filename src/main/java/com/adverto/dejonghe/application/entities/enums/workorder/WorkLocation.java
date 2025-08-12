package com.adverto.dejonghe.application.entities.enums.workorder;

public enum WorkLocation {
    ON_THE_MOVE ("Op verplaatsing"),
    WORKPLACE   ("Atelier");

    private final String discription;

    WorkLocation(String discription) {
        this.discription = discription;
    }

    public String getDiscription() { return discription; }
}
