package com.adverto.dejonghe.application.entities.enums.employee;

public enum UserFunction {
    ADMIN ("Administrator"),
    TECHNICIAN   ("Technieker"),
    WAREHOUSEWORKER("WarehouseWorker"),;

    private final String discription;

    UserFunction(String discription) {
        this.discription = discription;
    }

    public String getDiscription() { return discription; }
}
