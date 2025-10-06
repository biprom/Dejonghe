package com.adverto.dejonghe.application.entities.product.product;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
@NoArgsConstructor
public class Supplier {
    @Id
    private String id;
    private String name;
    private String street;
    private String zipCode;
    private String city;
    private String country;
    private String vatNumber;
    private String comment;
    private String alertMessage;
    private Boolean alert;
}
