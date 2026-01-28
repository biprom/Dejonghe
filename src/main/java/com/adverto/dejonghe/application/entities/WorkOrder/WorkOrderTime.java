package com.adverto.dejonghe.application.entities.WorkOrder;

import com.adverto.dejonghe.application.entities.customers.Address;
import com.adverto.dejonghe.application.entities.employee.Employee;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkLocation;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkOrderStatus;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkType;
import com.adverto.dejonghe.application.entities.product.product.Product;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Document
@Getter
@Setter
@NoArgsConstructor
public class WorkOrderTime {
    @Id
    private String id;

    LocalTime timeUp;
    LocalTime timeDown;
    LocalTime timeStart;
    LocalTime timeStop;
    Integer pauze = 0;
    Boolean bOvernight;

}
