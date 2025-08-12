package com.adverto.dejonghe.application.entities.GoogleMapsEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Elements {

    private  Distance[] elements;

    public Distance[] getElements() {
        return elements;
    }

    public void setElements(Distance[] elements) {
        this.elements = elements;
    }
}
