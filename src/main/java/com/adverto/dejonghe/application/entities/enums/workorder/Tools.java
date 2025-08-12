package com.adverto.dejonghe.application.entities.enums.workorder;

public enum Tools {
    SCHAARLIFT_JLG_KLEIN (1, WorkLocation.ON_THE_MOVE,"Schaarlift JLG Klein", "Schaarlift JLG Klein - Uren gebruik : ",null , "SLkl", false ),
    SCHAARLIFT_JLG_GROOT   (1, WorkLocation.ON_THE_MOVE,"Schaarlift JLG Groot", "Schaarlift JLG Groot - Uren gebruik : ",null, "SLgr", false),
    RUPSSCHAARLIFT (1, WorkLocation.ON_THE_MOVE,"Rupsschaarlift", "Rupsschaarlift - brandstof : ",null, "RSL", false),
    SPINHOOGTEWERKER (1, WorkLocation.ON_THE_MOVE,"Spinhoogtewerker", "Spinhoogtewerker - brandstof : ",null, "SHW", false),
    GRAAFKRAAN_1700(2, WorkLocation.ON_THE_MOVE,"Graafkraan 1.7 ton", "Graafkraan 1.7 ton - brandstof : ",null, "GK1.7T", false),
    GRAAFKRAAN_6000 (2, WorkLocation.ON_THE_MOVE,"Graafkraan 6 ton", "Graafkraan 6 ton - brandstof : ",null, "GK6T", false),
    GRAAFKRAAN14000 (2, WorkLocation.ON_THE_MOVE,"Graafkraan 14 ton", "Graafkraan 14 ton - brandstof : ",null, "GK14T", false),
    STAMPER_TRILPLAAT_65 (2, WorkLocation.ON_THE_MOVE,"Trilplaat 65 kg", "Trilplaat 65 kg - brandstof : ",null, "ST65", false),
    STAMPER_TRILPLAAT_85 (3, WorkLocation.ON_THE_MOVE,"Trilplaat 85 kg", "Trilplaat 85 kg - brandstof : ",null, "ST85", false),
    STAMPER_TRILPLAAT_500 (3, WorkLocation.ON_THE_MOVE,"Trilplaat 500 kg", "Trilplaat 500 kg - brandstof : ",null, "ST500", false),
    BROMMER (3, WorkLocation.ON_THE_MOVE,"Brommer", "Brommer - brandstof : ","Slijtage blad in mm : ", "BROM", false),
    BETONZAAGMACHINE (3, WorkLocation.ON_THE_MOVE,"Betonzaagmachine","Betonzaagmachine - brandstof : ","Slijtage blad in mm : ", "BZ", false),
    LINTZAAGMACHINE (4, WorkLocation.ON_THE_MOVE,"Lintzaagmachine", "Lintzaagmachine - brandstof : ",null , "LZ", false),
    BREEKHAMER17TON (4, WorkLocation.ON_THE_MOVE,"Breekhamer 1.7 Ton", "Breekhamer 1.7 Ton - brandstof : ",null , "BH1.7", false),
    BREEKHAMER60TON (4, WorkLocation.ON_THE_MOVE,"Breekhamer 6.0 Ton", "Breekhamer 6.0 Ton - brandstof : ",null , "HB6.0", false),
    OVERNACHTING (4, WorkLocation.ON_THE_MOVE,"Overnachting", "",null , "ON", false),

    SPIEBAAN_DUWEN_IJZER (2, WorkLocation.WORKPLACE,"Spiebaan Duwen Ijzer", "Spiebaan Duwen ijzer werkuren","Spiebaan Duwen ijzer ditkte" , "SDFE", false),
    SPIEBAAN_DUWEN_RVS (2, WorkLocation.WORKPLACE,"Spiebaan Duwen RVS", "Spiebaan Duwen RVS werkuren","Spiebaan Duwen RVS ditkte" , "SDRVS", false),
    LASEREN (1, WorkLocation.WORKPLACE,"Laseren", "Tijd",null , "L", false),
    PLOOIEN (1, WorkLocation.WORKPLACE,"Plooien", "Tijd",null , "P", false),
    CNC_DRAAIEN_EN_FREZEN (2, WorkLocation.WORKPLACE,"CNC draaien en Frezen", "CNC draaien en Frezen werkuren",null , "D&F", false),
    CNC_SNIJDEN_TEKENEN (1, WorkLocation.WORKPLACE,"CNC Snijden (Incl tekening)", "CNC Snijden (Incl tekening) werkuren",null , "S&T", false),
    ;

    private final Integer group;
    private final WorkLocation workLocation;
    private final String discription;
    private final String comment1;
    private final String comment2;
    private final String abbreviation;
    private Boolean bSelected;

    Tools(Integer group, WorkLocation workLocation, String discription, String comment1, String comment2, String abbreviation, Boolean bSelected ) {
        this.group = group;
        this.workLocation = workLocation;
        this.discription = discription;
        this.comment1 = comment1;
        this.comment2 = comment2;
        this.abbreviation = abbreviation;
        this.bSelected = bSelected;
    }

    public Integer getGroup() {return group;}

    public WorkLocation getWorkLocation() {return workLocation;}

    public String getDiscription() { return discription; }

    public String getComment1() { return comment1; }

    public String getComment2() { return comment2; }

    public String getAbbreviation() { return abbreviation; }

    public Boolean getBSelected() { return bSelected; }

    public void setbSelected(Boolean bSelected) {
        this.bSelected = bSelected;
    }
}
