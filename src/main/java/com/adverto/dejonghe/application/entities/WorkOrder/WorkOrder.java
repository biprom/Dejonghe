package com.adverto.dejonghe.application.entities.WorkOrder;

import com.adverto.dejonghe.application.entities.customers.Address;
import com.adverto.dejonghe.application.entities.employee.Employee;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkLocation;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkOrderStatus;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkType;
import com.adverto.dejonghe.application.entities.product.product.Product;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
@Document
@Getter
@Setter
@NoArgsConstructor
public class WorkOrder {
    @Id
    private String id;

    String workOrderNumber;
    Address workAddress;
    LocalDateTime workDateTime;
    WorkLocation workLocation;
    WorkType workType;
    WorkOrderStatus workOrderStatus;

    Employee masterEmployeeTeam1;
    Set<Employee> extraEmployeesTeam1;

    Employee masterEmployeeTeam2;
    Set<Employee> extraEmployeesTeam2;

    Employee masterEmployeeTeam3;
    Set<Employee> extraEmployeesTeam3;

    Employee masterEmployeeTeam4;
    Set<Employee> extraEmployeesTeam4;

    List<WorkOrderHeader> workOrderHeaderList;

    List<Product>productList;

    List<String>imageList;

    Boolean starter;
    List<String>linkedWorkOrders;

}
