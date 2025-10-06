package com.adverto.dejonghe.application.entities.WorkOrder;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;

@Document
@Getter
@Setter
@NoArgsConstructor
public class BowlEntity {
    @Id
    private String id;
    String chassisNumber;
    LocalDate workDate;
    Integer workhours;
    Boolean bBowlRemoved;
    String bowlRemovedNumber;
    Boolean bBowlReplaced;
    String bowlReplacedNumber;
}
