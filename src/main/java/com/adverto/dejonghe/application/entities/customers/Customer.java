package com.adverto.dejonghe.application.entities.customers;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Objects;

@Document
@Getter
@Setter
@NoArgsConstructor
public class Customer {
    @Id
    private String id;
    private String name;
    private String vatNumber;
    private String comment;
    private List<Address> addresses;
    private List<Long> buddyList;
    private Boolean alert = false;
    private String alertMessage;
    private Boolean bAgro = false;
    private Boolean bIndustry = false;
}
