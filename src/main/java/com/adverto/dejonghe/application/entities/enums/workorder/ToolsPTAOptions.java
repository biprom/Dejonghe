package com.adverto.dejonghe.application.entities.enums.workorder;

public enum ToolsPTAOptions {
    TROMMEL_SCHROEF ("trommel/schroef"),
    KLEINE_ONDERDELEN_DECANTER   ("kleine onderdelen decanter"),
    ONDERDELEN_MIXER_POMP   ("onderdelen mixer/pomp");

    private final String discription;

    ToolsPTAOptions(String discription) {
        this.discription = discription;
    }

    public String getDiscription() { return discription; }
}
