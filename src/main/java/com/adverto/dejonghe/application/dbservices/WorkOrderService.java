package com.adverto.dejonghe.application.dbservices;

import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrder;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkOrderStatus;
import com.adverto.dejonghe.application.repos.WorkOrderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WorkOrderService {
    @Autowired
    WorkOrderRepo workOrderRepo;

    List<WorkOrder>workOrderList = new ArrayList<>();

    public Optional<List<WorkOrder>> getAll() {
        List<WorkOrder> workOrders = workOrderRepo.findAll();
        if (!workOrders.isEmpty()) {
            return Optional.of(workOrders);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<List<WorkOrder>> getAllFinished() {
        List<WorkOrder> workOrders = workOrderRepo.findWorkOrderByWorkOrderStatus(WorkOrderStatus.FINISHED);
        if (!workOrders.isEmpty()) {
            return Optional.of(workOrders);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<List<WorkOrder>> getAllWorkOrdersByStatus(WorkOrderStatus status) {
        List<WorkOrder> workOrders = workOrderRepo.findWorkOrderByWorkOrderStatus(status);
        if (!workOrders.isEmpty()) {
            return Optional.of(workOrders);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<WorkOrder> getWorkOrderById(String id) {
        Optional<WorkOrder> optionalWorkOrder = Optional.of(workOrderRepo.findWorkOrderById(id));
        return optionalWorkOrder;
    }

    public Optional<WorkOrder> getStarterByLinkedId(String linkedId) {
        Optional<WorkOrder> optionalStarterWorkOrder = Optional.of(workOrderRepo.findWorkOrderByLinkedWorkOrdersContains(linkedId));
        return optionalStarterWorkOrder;
    }

    public Optional<List<WorkOrder>> getAllByStatusAndStarter(WorkOrderStatus status, Boolean starter){
        return Optional.of(workOrderRepo.findByWorkOrderStatusAndStarter(status,starter));
    }

    public void delete(WorkOrder workOrder) {
        workOrderRepo.delete(workOrder);
    }

    public String save(WorkOrder workOrder) {
        WorkOrder save = workOrderRepo.save(workOrder);
        return save.getId();
    }

    public List<WorkOrder> getCoupledWorkOrders(List<String> linkedWorkOrders) {
        workOrderList.clear();
        for (String linkedWorkOrder : linkedWorkOrders) {
            Optional<WorkOrder> optionalWorkOrder = Optional.of(workOrderRepo.findWorkOrderById(linkedWorkOrder));
            if (optionalWorkOrder.isPresent()) {
                workOrderList.add(optionalWorkOrder.get());
            }
        }
        return workOrderList;
    }

    public List<WorkOrder> getWorkOrderListByStarterId(String starterId) {
        workOrderList.clear();
        WorkOrder starter = workOrderRepo.findWorkOrderById(starterId);
        if (starter != null) {
            workOrderList.add(starter);
        }
        if((starter.getLinkedWorkOrders() != null) && (!starter.getLinkedWorkOrders().isEmpty())) {
            for(String id : starter.getLinkedWorkOrders()){
                Optional<WorkOrder> optionalWorkOrder = Optional.of(workOrderRepo.findWorkOrderById(id));
                if (optionalWorkOrder.isPresent()) {
                    workOrderList.add(optionalWorkOrder.get());
                }
            }
        }
        return workOrderList;
    }

    public Optional<WorkOrder> getWorkOrderByWorkDateTime(LocalDateTime workDateTime) {
        return Optional.of(workOrderRepo.findWorkOrderByWorkDateTime(workDateTime));
    }
}
