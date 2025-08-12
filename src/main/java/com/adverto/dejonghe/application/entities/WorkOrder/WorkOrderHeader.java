package com.adverto.dejonghe.application.entities.WorkOrder;

import com.adverto.dejonghe.application.entities.employee.Employee;
import com.adverto.dejonghe.application.entities.enums.fleet.Fleet;
import com.adverto.dejonghe.application.entities.enums.fleet.FleetTruckCraneOptions;
import com.adverto.dejonghe.application.entities.enums.fleet.FleetWorkType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalTime;
import java.util.List;

@Document
@Getter
@Setter
@NoArgsConstructor
public class WorkOrderHeader {

    String discription;

    List<WorkOrderTime>workOrderTimeList;
    Fleet fleet;
    FleetTruckCraneOptions fleetOptions;
    Double roadTax;
    Double tunnelTax;
    FleetWorkType fleetWorkType;

}
