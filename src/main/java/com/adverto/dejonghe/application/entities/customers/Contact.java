package com.adverto.dejonghe.application.entities.customers;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
@NoArgsConstructor
public class Contact {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String cellphone;
    private String function;
    private Boolean active;
}
