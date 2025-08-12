package com.adverto.dejonghe.application.entities.GoogleMapsEntities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Distance {

    private  Integer value;
    private String text;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
