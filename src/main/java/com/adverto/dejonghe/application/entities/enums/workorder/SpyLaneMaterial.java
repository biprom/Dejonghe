package com.adverto.dejonghe.application.entities.enums.workorder;

public enum SpyLaneMaterial {
    IRON ("ijzer", "FE"),
    RVS   ("RVS", "RVS");

    private final String discription;
    private final String element;

    SpyLaneMaterial(String discription, String element) {

        this.discription = discription;
        this.element = element;
    }

    public String getDiscription() { return discription; }
    public String getElement() { return element; }
}
