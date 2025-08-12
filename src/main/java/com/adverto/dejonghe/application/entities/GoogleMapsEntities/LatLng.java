package com.adverto.dejonghe.application.entities.GoogleMapsEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LatLng {

    private  Results[] results;
    private String status;

    public Results[] getResults() {
        return results;
    }

    public void setResults(Results[] results) {
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this. status = status;
    }
}
