package com.adverto.dejonghe.application.entities.customers;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Getter
@Setter
@NoArgsConstructor
public class Address {
    private Boolean invoiceAddress;
    private String customerName;
    private String addressName;
    private String street;
    private String number;
    private String zip;
    private String city;
    private String country;
    private Coordinates coordinates;
    private Double distance;
    private String comment;
    private String invoiceMail;
    private List<Contact> contactList;
    private Double roadTaxAtego;
    private Double roadTaxActros;
    private Double roadTaxArocs;
}
