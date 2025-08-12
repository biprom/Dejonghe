package com.adverto.dejonghe.application.entities.enums.fleet;

public enum FleetTruckCraneOptions {
    NO_OPTIONS("Geen opties"),
    PERSON_CAGE("Personenkooi"),
    GRAB_BUCKET("Grijpkooi"),
    PERSON_CAGE_GRAB_BUCKET("Personenkooi en Grijpbak");

    private final String options;

    FleetTruckCraneOptions(String options) {
        this.options = options;
    }

    public String getOptions() { return options; }
}
