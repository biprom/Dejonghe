package com.adverto.dejonghe.application.repos;


import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrder;
import com.adverto.dejonghe.application.entities.employee.Employee;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkOrderStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkOrderRepo extends MongoRepository<WorkOrder, String> {
    List<WorkOrder>findWorkOrderByWorkOrderStatus(WorkOrderStatus status);
    WorkOrder findWorkOrderById(String workOrderId);
    WorkOrder findWorkOrderByLinkedWorkOrdersContains(String id);
    WorkOrder findWorkOrderByWorkDateTime(LocalDateTime workDateTime);
    List<WorkOrder>findByWorkOrderStatusAndStarter(WorkOrderStatus status, Boolean starter);
}
