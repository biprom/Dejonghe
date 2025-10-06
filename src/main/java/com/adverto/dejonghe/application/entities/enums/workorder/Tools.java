package com.adverto.dejonghe.application.entities.enums.workorder;

public enum Tools {
    PERSONENKOOI (1,1,  WorkLocation.ON_THE_MOVE,"Personenkooi",ToolsPopUpType.TOOLS_VIEW_OK, false, "PK" ),
    GRIJPBAK (1,2,  WorkLocation.ON_THE_MOVE,"Grijpbak",ToolsPopUpType.TOOLS_VIEW_REGULAR_INTENSE,  false, "GB" ),
    LINTZAAGMACHINE (1,3,  WorkLocation.ON_THE_MOVE,"Mobiele lintzaagmachine",ToolsPopUpType.TOOLS_VIEW_OK, false, "LZM" ),

    SCHAARLIFT_JLG_KLEIN (2,1,  WorkLocation.ON_THE_MOVE,"Schaarlift JLG klein",ToolsPopUpType.TOOLS_VIEW_OK, false, "SJK" ),
    SCHAARLIFT_JLG_GROOT   (2,2,  WorkLocation.ON_THE_MOVE,"Schaarlift JLG groot",ToolsPopUpType.TOOLS_VIEW_OK, false, "SJG"),
    RUPSSCHAARLIFT (2,3,  WorkLocation.ON_THE_MOVE,"Rupsschaarlift",ToolsPopUpType.TOOLS_VIEW_REGULAR_INTENSE_FUEL, false,"RSL"),
    SPINHOOGTEWERKER (2,4,  WorkLocation.ON_THE_MOVE,"Spinhoogtewerker",ToolsPopUpType.TOOLS_VIEW_FUEL,false,"SHW"),

    GRAAFKRAAN_1700(3,1,  WorkLocation.ON_THE_MOVE,"Graafkraan 1.7 ton",ToolsPopUpType.TOOLS_VIEW_REGULAR_INTENSE_FUEL,  false,"GK17"),
    GRAAFKRAAN_6000 (3,2,  WorkLocation.ON_THE_MOVE,"Graafkraan 6 ton",ToolsPopUpType.TOOLS_VIEW_FUEL,   false,"GK60"),
    GRAAFKRAAN14000 (3,3,  WorkLocation.ON_THE_MOVE,"Graafkraan 14 ton",ToolsPopUpType.TOOLS_VIEW_FUEL, false,"GK14"),
    BREEKHAMER17TON (3,4,  WorkLocation.ON_THE_MOVE,"Breekhamer 1.7 ton",ToolsPopUpType.TOOLS_VIEW_REGULAR_INTENSE, false,"BH17"),
    BREEKHAMER60TON (3,5,  WorkLocation.ON_THE_MOVE,"Breekhamer 6.0 ton",ToolsPopUpType.TOOLS_VIEW_REGULAR_INTENSE,  false,"BH60"),

    BROMMER (4,1,  WorkLocation.ON_THE_MOVE,"Brommer",ToolsPopUpType.TOOLS_VIEW_THICK_RUNNING_METER, false,"BROM"),
    BETONZAAGMACHINE (4,2,  WorkLocation.ON_THE_MOVE,"Betonzaagmachine",ToolsPopUpType.TOOLS_VIEW_THICK_RUNNING_METER,false,"BZM"),
    STAMPER_TRILPLAAT_65 (4,3,  WorkLocation.ON_THE_MOVE,"Stamper 65 kg",ToolsPopUpType.TOOLS_VIEW_FUEL,  false,"ST65"),
    STAMPER_TRILPLAAT_85 (4,4,  WorkLocation.ON_THE_MOVE,"Trilplaat 85 kg",ToolsPopUpType.TOOLS_VIEW_FUEL, false,"ST85"),
    STAMPER_TRILPLAAT_500 (4,5,  WorkLocation.ON_THE_MOVE,"Trilplaat 500 kg",ToolsPopUpType.TOOLS_VIEW_FUEL, false,"ST500"),


    GROTE_DRAAIBANK(1,1,WorkLocation.WORKPLACE,"Gebruik grote draaibank",ToolsPopUpType.TOOLS_VIEW_WORKHOURS,false,"GD"),
    KLEINE_DRAAIBANK(1,2,WorkLocation.WORKPLACE,"Gebruik kleine draaibank",ToolsPopUpType.TOOLS_VIEW_WORKHOURS,false,"KD"),
    OPLASSEN_PTA(1,3,WorkLocation.WORKPLACE,"Oplassen met PTA",ToolsPopUpType.TOOLS_VIEW_PTA,false,"OPL_PTA"),

    SPIEBAAN_DUWEN (2,1, WorkLocation.WORKPLACE,"Spiebaan duwen",ToolsPopUpType.TOOLS_VIEW_SPIELANE,  false,"SD"),
    BALANCEREN_KLEINE_ONDERDELEN(2,2,  WorkLocation.WORKPLACE,"Balanceren kleine onderdelen",ToolsPopUpType.TOOLS_VIEW_WORKHOURS, false,"BKO"),

    LASEREN (3,1, WorkLocation.WORKPLACE,"Laseren",ToolsPopUpType.TOOLS_VIEW_WORKHOURS, false,"LASER"),
    PLOOIEN (3,2, WorkLocation.WORKPLACE,"Plooien",ToolsPopUpType.TOOLS_VIEW_WORKHOURS, false,"PLOO"),
    LASEREN_EN_PLOOIEN (3,3, WorkLocation.WORKPLACE,"Laseren en plooien",ToolsPopUpType.TOOLS_VIEW_WORKHOURS, false,"PLO&LASER"),
    CNC_DRAAIEN_EN_FREZEN (3,4, WorkLocation.WORKPLACE,"CNC draaien en frezen",ToolsPopUpType.TOOLS_VIEW_WORKHOURS,  false,"CNCDR&FR"),
    CNC_SNIJDEN (3,5,  WorkLocation.WORKPLACE,"CNC snijden",ToolsPopUpType.TOOLS_VIEW_WORKHOURS, false,"CNCSN"),
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
