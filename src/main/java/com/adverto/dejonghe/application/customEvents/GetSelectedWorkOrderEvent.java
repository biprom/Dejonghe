package com.adverto.dejonghe.application.customEvents;

import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrder;
import org.springframework.context.ApplicationEvent;

public class GetSelectedWorkOrderEvent extends ApplicationEvent {
    private final WorkOrder selectedWorkOrder;

    public GetSelectedWorkOrderEvent(Object source, WorkOrder selectedWorkOrder) {
        super(source);
        this.selectedWorkOrder = selectedWorkOrder;
    }

    public WorkOrder getSelectedWorkOrder() {
        return selectedWorkOrder;
    }
}
