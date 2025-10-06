package com.adverto.dejonghe.application.entities.enums.workorder;

public enum SpyLaneMaterial {
    IRON ("ijzer"),
    RVS   ("RVS");

    private final String discription;

    SpyLaneMaterial(String discription) {
        this.discription = discription;
    }

    public String getDiscription() { return discription; }
}
