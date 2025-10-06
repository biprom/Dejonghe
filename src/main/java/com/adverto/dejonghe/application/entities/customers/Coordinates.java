package com.adverto.dejonghe.application.entities.customers;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
@NoArgsConstructor
public class Coordinates {
    private Double longitude;
    private Double latitude;
}
