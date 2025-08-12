package com.adverto.dejonghe.application.entities.enums.fleet;

public enum FleetWorkType {
    REGULAR ("Algemeen"),
    INTENS   ("Intensief"),
    DELIVERY   ("Forfait / Afleveren")
    ;

    private final String discription;

    FleetWorkType(String discription) {
        this.discription = discription;
    }

    public String getDiscription() { return discription; }
}
