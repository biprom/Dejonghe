package com.adverto.dejonghe.application.entities.customers;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Getter
@Setter
@NoArgsConstructor
public class CustomerImport {
    @Id
    private String id;
    private String name;
    private String vatNumber;
    private String comment;
    private Boolean invoiceAddress;
    private String street;
    private String number;
    private String zip;
    private String city;
    private String country;
}
