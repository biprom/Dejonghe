package com.adverto.dejonghe.application.entities.enums.fleet;

public enum Fleet {
    VAN ("Bestelwagen"),
    VAN_TRAILER   ("Bestelwagen + aanhangwagen"),
    ATEGO ("Atego"),
    TRUCK_TRAILER("Truck + aanhangwagen"),
    TRUCK_LOWLOADER("Truck + dieplader"),
    TRUCK_CRANE("Kraan vrachtwagen");

    private final String discription;

    Fleet(String discription) {
        this.discription = discription;
    }

    public String getDiscription() { return discription; }
}
