package com.adverto.dejonghe.application.entities.enums.workorder;

public enum Tools {
    PERSONENKOOI (1,1,  WorkLocation.ON_THE_MOVE,"Personenkooi",ToolsPopUpType.TOOLS_VIEW_OK, false, "IND_PK", "AGRO_PK"),
    GRIJPBAK (1,2,  WorkLocation.ON_THE_MOVE,"Grijpbak",ToolsPopUpType.TOOLS_VIEW_REGULAR_INTENSE,  false, "IND_GB","AGRO_GB" ),
    LINTZAAGMACHINE (1,3,  WorkLocation.ON_THE_MOVE,"Mobiele lintzaagmachine",ToolsPopUpType.TOOLS_VIEW_OK, false, "IND_LZM","AGRO_LZM" ),

    SCHAARLIFT_JLG_KLEIN (2,1,  WorkLocation.ON_THE_MOVE,"Schaarlift JLG klein",ToolsPopUpType.TOOLS_VIEW_OK, false, "IND_SJK","AGRO_SJK" ),
    SCHAARLIFT_JLG_GROOT   (2,2,  WorkLocation.ON_THE_MOVE,"Schaarlift JLG groot",ToolsPopUpType.TOOLS_VIEW_OK, false, "IND_SJG","AGRO_SJG"),
    RUPSSCHAARLIFT (2,3,  WorkLocation.ON_THE_MOVE,"Rupsschaarlift",ToolsPopUpType.TOOLS_VIEW_REGULAR_INTENSE_FUEL, false,"IND_RSL","AGRO_RSL"),
    SPINHOOGTEWERKER (2,4,  WorkLocation.ON_THE_MOVE,"Spinhoogtewerker",ToolsPopUpType.TOOLS_VIEW_FUEL,false,"IND_SHW","AGRO_SHW"),

    GRAAFKRAAN_1700(3,1,  WorkLocation.ON_THE_MOVE,"Graafkraan 1.7 ton",ToolsPopUpType.TOOLS_VIEW_REGULAR_INTENSE_FUEL,  false,"IND_GK17","AGRO_GK17"),
    GRAAFKRAAN_6000 (3,2,  WorkLocation.ON_THE_MOVE,"Graafkraan 6 ton",ToolsPopUpType.TOOLS_VIEW_FUEL,   false,"IND_GK60","AGRO_GK60"),
    GRAAFKRAAN14000 (3,3,  WorkLocation.ON_THE_MOVE,"Graafkraan 14 ton",ToolsPopUpType.TOOLS_VIEW_FUEL, false,"IND_GK14","AGRO_GK14"),
    BREEKHAMER17TON (3,4,  WorkLocation.ON_THE_MOVE,"Breekhamer 1.7 ton",ToolsPopUpType.TOOLS_VIEW_REGULAR_INTENSE, false,"IND_BH17","AGRO_BH17"),
    BREEKHAMER60TON (3,5,  WorkLocation.ON_THE_MOVE,"Breekhamer 6.0 ton",ToolsPopUpType.TOOLS_VIEW_REGULAR_INTENSE,  false,"IND_BH60","AGRO_BH60"),

    BROMMER (4,1,  WorkLocation.ON_THE_MOVE,"Brommer",ToolsPopUpType.TOOLS_VIEW_THICK_RUNNING_METER, false,"IND_BROM","AGRO_BROM"),
    BETONZAAGMACHINE (4,2,  WorkLocation.ON_THE_MOVE,"Betonzaagmachine",ToolsPopUpType.TOOLS_VIEW_THICK_RUNNING_METER,false,"IND_BZM","AGRO_BZM"),
    STAMPER_TRILPLAAT_65 (4,3,  WorkLocation.ON_THE_MOVE,"Stamper 65 kg",ToolsPopUpType.TOOLS_VIEW_FUEL,  false,"IND_ST65","AGRO_ST65"),
    STAMPER_TRILPLAAT_85 (4,4,  WorkLocation.ON_THE_MOVE,"Trilplaat 85 kg",ToolsPopUpType.TOOLS_VIEW_FUEL, false,"IND_ST85","AGRO_ST85"),
    STAMPER_TRILPLAAT_500 (4,5,  WorkLocation.ON_THE_MOVE,"Trilplaat 500 kg",ToolsPopUpType.TOOLS_VIEW_FUEL, false,"IND_ST500","AGRO_ST500"),


    GROTE_DRAAIBANK(1,1,WorkLocation.WORKPLACE,"Gebruik grote draaibank",ToolsPopUpType.TOOLS_VIEW_WORKHOURS,false,"IND_GD","AGRO_GD"),
    KLEINE_DRAAIBANK(1,2,WorkLocation.WORKPLACE,"Gebruik kleine draaibank",ToolsPopUpType.TOOLS_VIEW_WORKHOURS,false,"IND_KD","AGRO_KD"),
    OPLASSEN_PTA(1,3,WorkLocation.WORKPLACE,"Oplassen met PTA",ToolsPopUpType.TOOLS_VIEW_PTA,false,"IND_OPL_PTA","AGRO_OPL_PTA"),

    SPIEBAAN_DUWEN (2,1, WorkLocation.WORKPLACE,"Spiebaan duwen",ToolsPopUpType.TOOLS_VIEW_SPIELANE,  false,"IND_SD","AGRO_SD"),
    BALANCEREN_KLEINE_ONDERDELEN(2,2,  WorkLocation.WORKPLACE,"Balanceren kleine onderdelen",ToolsPopUpType.TOOLS_VIEW_WORKHOURS, false,"IND_BKO","AGRO_BKO"),

    LASEREN (3,1, WorkLocation.WORKPLACE,"Laseren",ToolsPopUpType.TOOLS_VIEW_WORKHOURS, false,"IND_LASER","AGRO_LASER"),
    PLOOIEN (3,2, WorkLocation.WORKPLACE,"Plooien",ToolsPopUpType.TOOLS_VIEW_WORKHOURS, false,"IND_PLOO","AGRO_PLOO"),
    LASEREN_EN_PLOOIEN (3,3, WorkLocation.WORKPLACE,"Laseren en plooien",ToolsPopUpType.TOOLS_VIEW_WORKHOURS, false,"IND_PLO&LASER","AGRO_PLO&LASER"),
    CNC_DRAAIEN_EN_FREZEN (3,4, WorkLocation.WORKPLACE,"CNC draaien en frezen",ToolsPopUpType.TOOLS_VIEW_WORKHOURS,  false,"IND_CNCDR&FR","AGRO_CNCDR&FR"),
    CNC_SNIJDEN (3,5,  WorkLocation.WORKPLACE,"CNC snijden",ToolsPopUpType.TOOLS_VIEW_WORKHOURS, false,"IND_CNCSN","AGRO_CNCSN"),
    ;

    private final Integer group;
    private ToolsPopUpType toolsPopUpType;
    private final Integer position;
    private final WorkLocation workLocation;
    private final String discription;
    private Boolean bSelected;
    private String abbreviationIndustry;
    private String abbreviationAgro;

    Tools(Integer group ,Integer position, WorkLocation workLocation, String discription, ToolsPopUpType toolsPopUpType, Boolean bSelected, String abbreviationIndustry, String abbreviationAgro ) {
        this.group = group;
        this.position = position;
        this.workLocation = workLocation;
        this.discription = discription;
        this.toolsPopUpType = toolsPopUpType;
        this.bSelected = bSelected;
        this.abbreviationIndustry = abbreviationIndustry;
        this.abbreviationAgro = abbreviationAgro;
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

    public String getAbbreviationIndustry() {
        return abbreviationIndustry;
    }

    public void setAbbreviationIndustry(String abbreviationIndustry) {
        this.abbreviationIndustry = abbreviationIndustry;
    }

    public String getAbbreviationAgro() {
        return abbreviationAgro;
    }

    public void setAbbreviationAgro(String abbreviationAgro) {
        this.abbreviationAgro = abbreviationAgro;
    }
}
