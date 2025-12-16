package com.adverto.dejonghe.application.entities.enums.workorder;

public enum ToolsPTAOptions {
    TROMMEL_SCHROEF ("trommel/schroef", "std"),
    KLEINE_ONDERDELEN_DECANTER   ("kleine onderdelen decanter", "kod"),
    ONDERDELEN_MIXER_POMP   ("onderdelen mixer/pomp", "mp");

    private final String discription;
    private final String additionToCode;

    ToolsPTAOptions(String discription, String additionToCode) {
        this.discription = discription;
        this.additionToCode = additionToCode;
    }

    public String getDiscription() { return discription; }

    public String getAdditionToCode() { return additionToCode; }
}
