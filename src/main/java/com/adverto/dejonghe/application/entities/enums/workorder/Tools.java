package com.adverto.dejonghe.application.entities.enums.workorder;

public enum Tools {
    PERSONENKOOI (1,1,  WorkLocation.ON_THE_MOVE,"Personenkooi",ToolsPopUpType.TOOLS_VIEW_OK, false, "OPVER-pk"),
    GRIJPBAK (1,2,  WorkLocation.ON_THE_MOVE,"Grijpbak",ToolsPopUpType.TOOLS_VIEW_REGULAR_INTENSE,  false, "OPVER-gb-"),
    MAX_TRAILER (1,3,  WorkLocation.ON_THE_MOVE,"MAX- trailer",ToolsPopUpType.TOOLS_VIEW_OK,  false, "OPVER-diepl"),
    LINTZAAGMACHINE (1,4,  WorkLocation.ON_THE_MOVE,"Mobiele lintzaagmachine",ToolsPopUpType.TOOLS_VIEW_OK, false, "OPVER-lzm"),

    SCHAARLIFT_JLG_KLEIN (2,1,  WorkLocation.ON_THE_MOVE,"Schaarlift JLG klein",ToolsPopUpType.TOOLS_VIEW_OK, false, "OPVER-sl-jlg-kl"),
    SCHAARLIFT_JLG_GROOT   (2,2,  WorkLocation.ON_THE_MOVE,"Schaarlift JLG groot",ToolsPopUpType.TOOLS_VIEW_OK, false, "OPVER-sl-jlg-gr"),
    RUPSSCHAARLIFT (2,3,  WorkLocation.ON_THE_MOVE,"Rupsschaarlift",ToolsPopUpType.TOOLS_VIEW_REGULAR_INTENSE_FUEL, false,"OPVER-rsl-"),
    SPINHOOGTEWERKER (2,4,  WorkLocation.ON_THE_MOVE,"Spinhoogtewerker",ToolsPopUpType.TOOLS_VIEW_FUEL,false,"OPVER-shw"),

    GRAAFKRAAN_1700(3,1,  WorkLocation.ON_THE_MOVE,"Graafkraan 1.7 ton",ToolsPopUpType.TOOLS_VIEW_REGULAR_INTENSE_FUEL,  false,"OPVER-gk17-"),
    GRAAFKRAAN_6000 (3,2,  WorkLocation.ON_THE_MOVE,"Graafkraan 6 ton",ToolsPopUpType.TOOLS_VIEW_FUEL,   false,"OPVER-gk60"),
    GRAAFKRAAN14000 (3,3,  WorkLocation.ON_THE_MOVE,"Graafkraan 14 ton",ToolsPopUpType.TOOLS_VIEW_FUEL, false,"OPVER-gk14"),
    BREEKHAMER17TON (3,4,  WorkLocation.ON_THE_MOVE,"Breekhamer 1.7 ton",ToolsPopUpType.TOOLS_VIEW_REGULAR_INTENSE, false,"OPVER-bh17-"),
    BREEKHAMER60TON (3,5,  WorkLocation.ON_THE_MOVE,"Breekhamer 6.0 ton",ToolsPopUpType.TOOLS_VIEW_REGULAR_INTENSE,  false,"OPVER-bh60-"),

    BROMMER (4,1,  WorkLocation.ON_THE_MOVE,"Brommer",ToolsPopUpType.TOOLS_VIEW_THICK_RUNNING_METER, false,"OPVER-brom"),
    BETONZAAGMACHINE (4,2,  WorkLocation.ON_THE_MOVE,"Betonzaagmachine",ToolsPopUpType.TOOLS_VIEW_THICK_RUNNING_METER,false,"OPVER-bzm600"),
    STAMPER_TRILPLAAT_65 (4,3,  WorkLocation.ON_THE_MOVE,"Stamper 65 kg",ToolsPopUpType.TOOLS_VIEW_FUEL,  false,"OPVER-st-tp65"),
    STAMPER_TRILPLAAT_85 (4,4,  WorkLocation.ON_THE_MOVE,"Trilplaat 85 kg",ToolsPopUpType.TOOLS_VIEW_FUEL, false,"OPVER-tr85"),
    STAMPER_TRILPLAAT_500 (4,5,  WorkLocation.ON_THE_MOVE,"Trilplaat 500 kg",ToolsPopUpType.TOOLS_VIEW_FUEL, false,"OPVER-tr500"),


    GROTE_DRAAIBANK(1,1,WorkLocation.WORKPLACE,"Gebruik grote draaibank",ToolsPopUpType.TOOLS_VIEW_WORKHOURS,false,"OPAT-gr-draai"),
    KLEINE_DRAAIBANK(1,2,WorkLocation.WORKPLACE,"Gebruik kleine draaibank",ToolsPopUpType.TOOLS_VIEW_WORKHOURS,false,"OPAT-kl-draai"),
    OPLASSEN_PTA(1,3,WorkLocation.WORKPLACE,"Oplassen met PTA",ToolsPopUpType.TOOLS_VIEW_PTA,false,"OPAT-opl-pta-"),

    SPIEBAAN_DUWEN (2,1, WorkLocation.WORKPLACE,"Spiebaan duwen",ToolsPopUpType.TOOLS_VIEW_SPIELANE,  false,"OPAT-sbd"),
    BALANCEREN_KLEINE_ONDERDELEN(2,2,  WorkLocation.WORKPLACE,"Balanceren kleine onderdelen",ToolsPopUpType.TOOLS_VIEW_WORKHOURS, false,"OPAT-bko"),

    LASEREN (3,1, WorkLocation.WORKPLACE,"Laseren",ToolsPopUpType.TOOLS_VIEW_WORKHOURS, false,"OPAT-laser"),
    PLOOIEN (3,2, WorkLocation.WORKPLACE,"Plooien",ToolsPopUpType.TOOLS_VIEW_WORKHOURS, false,"OPAT-plooi"),
    LASEREN_EN_PLOOIEN (3,3, WorkLocation.WORKPLACE,"Laseren en plooien",ToolsPopUpType.TOOLS_VIEW_WORKHOURS, false,"OPAT-laser-plooi"),
    CNC_DRAAIEN_EN_FREZEN (3,4, WorkLocation.WORKPLACE,"CNC draaien en frezen",ToolsPopUpType.TOOLS_VIEW_WORKHOURS,  false,"OPAT-dr-fr"),
    CNC_SNIJDEN (3,5,  WorkLocation.WORKPLACE,"CNC snijden",ToolsPopUpType.TOOLS_VIEW_WORKHOURS, false,"OPAT-cncsn"),
    ;

    private final Integer group;
    private ToolsPopUpType toolsPopUpType;
    private final Integer position;
    private final WorkLocation workLocation;
    private final String discription;
    private Boolean bSelected;
    private String abbreviation;

    Tools(Integer group ,Integer position, WorkLocation workLocation, String discription, ToolsPopUpType toolsPopUpType, Boolean bSelected, String abbreviation ) {
        this.group = group;
        this.position = position;
        this.workLocation = workLocation;
        this.discription = discription;
        this.toolsPopUpType = toolsPopUpType;
        this.bSelected = bSelected;
        this.abbreviation = abbreviation;
    }

    public Integer getPosition() {
        return position;
    }

    public Integer getGroup() {return group;}

    public WorkLocation getWorkLocation() {return workLocation;}

    public String getDiscription() { return discription; }

    public ToolsPopUpType getToolsPopUpType() {
        return toolsPopUpType;
    }

    public Boolean getbSelected() {
        return bSelected;
    }

    public void setbSelected(Boolean bSelected) {
        this.bSelected = bSelected;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

}
