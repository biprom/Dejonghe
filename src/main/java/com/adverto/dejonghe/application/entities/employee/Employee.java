package com.adverto.dejonghe.application.entities.employee;

import com.adverto.dejonghe.application.entities.customers.Address;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document
@Getter
@Setter
@NoArgsConstructor
public class Employee {
    @Id
    private String id;
    private String firstName;
    private String lastName;
    private String abbreviation;
    private String phoneNumber;
    private String comment;
    private Boolean technician = false;
    private Boolean alert = false;
    private String alertMessage;
    private LocalDate birthDate;
    private LocalDate dateOfService;
}
