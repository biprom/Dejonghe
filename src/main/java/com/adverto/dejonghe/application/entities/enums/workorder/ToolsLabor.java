package com.adverto.dejonghe.application.entities.enums.workorder;

public enum ToolsLabor {
    REGULAR ("Algemeen"),
    INTENSE   ("Intensief");

    private final String discription;

    ToolsLabor(String discription) {
        this.discription = discription;
    }

    public String getDiscription() { return discription; }
}
