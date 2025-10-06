package com.adverto.dejonghe.application.views.workorder;
import com.adverto.dejonghe.application.customEvents.GetSelectedWorkOrderEvent;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import com.adverto.dejonghe.application.customEvents.AddProductEventListener;
import com.adverto.dejonghe.application.customEvents.ReloadProductListEvent;
import com.adverto.dejonghe.application.dbservices.CustomerService;
import com.adverto.dejonghe.application.dbservices.EmployeeService;
import com.adverto.dejonghe.application.dbservices.ProductService;
import com.adverto.dejonghe.application.dbservices.WorkOrderService;
import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrder;
import com.adverto.dejonghe.application.entities.enums.employee.UserFunction;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkOrderStatus;
import com.adverto.dejonghe.application.views.subViews.CurrentWorkOrdersSubView;
import com.adverto.dejonghe.application.views.subViews.SelectProductSubView;
import com.adverto.dejonghe.application.views.subViews.ShowImageSubVieuw;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.*;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.*;

@PageTitle("Lopende Werkbonnen")
@Route("werkbonnenLopende")
@Menu(order = 0, icon = LineAwesomeIconUrl.WRENCH_SOLID)
@Component
@Scope("prototype")
public class PendingWorkorderView extends Div implements BeforeEnterObserver {

    EmployeeService employeeService;
    CustomerService customerService;
    ProductService productService;
    SelectProductSubView selectProductSubView;
    WorkOrderService workOrderService;
    GridFsTemplate gridFsTemplate;
    CurrentWorkOrdersSubView currtentWorkOrdersSubVieuw;
    ShowImageSubVieuw showImageSubVieuw;
    AddProductEventListener listener;
    Optional<List<WorkOrder>> allPendingStarters;

    public PendingWorkorderView(ProductService productService,
                                CustomerService customerService,
                                EmployeeService employeeService,
                                SelectProductSubView selectProductSubView,
                                WorkOrderService workOrderService,
                                GridFsTemplate gridFsTemplate,
                                CurrentWorkOrdersSubView currtentWorkOrdersSubVieuw,
                                ShowImageSubVieuw showImageSubVieuw,
                                AddProductEventListener listener) {
        this.productService = productService;
        this.selectProductSubView = selectProductSubView;
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.workOrderService = workOrderService;
        this.gridFsTemplate = gridFsTemplate;
        this.currtentWorkOrdersSubVieuw = currtentWorkOrdersSubVieuw;
        this.showImageSubVieuw = showImageSubVieuw;
        this.listener = listener;
    }


    private void loadData(){
        allPendingStarters = workOrderService.getAllByStatusAndStarter(WorkOrderStatus.RUNNING, true);
        if(allPendingStarters.isPresent()){
            currtentWorkOrdersSubVieuw.addItemsToPendingWorkOrderGrid(allPendingStarters.get());
            currtentWorkOrdersSubVieuw.setAuthorisation(UserFunction.ADMIN);
            currtentWorkOrdersSubVieuw.setSizeFull();
            this.setSizeFull();
            this.add(getNavigateToSelectedWorkOrder());
            this.add(currtentWorkOrdersSubVieuw);
        }
        else{
            Notification notification = Notification.show("Geen lopenede werkbonnen gevonden");
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }

    private Button getNavigateToSelectedWorkOrder() {
        Button buttonOpenSelectedWorkOrder = new Button("Geselecteerde werkbon Openen");
        buttonOpenSelectedWorkOrder.setWidth("100%");
        buttonOpenSelectedWorkOrder.addClickListener(event -> {

            Optional<Set<WorkOrder>> selectedWorkOrders = currtentWorkOrdersSubVieuw.getSelectedWorkOrders();
            if (selectedWorkOrders.isPresent() && selectedWorkOrders.get().stream().anyMatch(p -> p.getStarter() == true)) {
                UI.getCurrent().navigate(WorkorderView.class, selectedWorkOrders.get().stream().findFirst().get().getId());
            }
            else{
                //get Starter of that WorkOrder
                String starterId = workOrderService.getStarterByLinkedId(selectedWorkOrders.get().stream().findFirst().get().getId()).get().getId();
                UI.getCurrent().navigate(WorkorderView.class, starterId);
            }
        });
        return buttonOpenSelectedWorkOrder;
    }

    @EventListener
    public void handleReloadEvent(ReloadProductListEvent event) {
        System.out.println("GetSelectedWorkOrderEventTest : " + event.getMessage());
        if (UI.getCurrent() != null && UI.getCurrent().equals(UI.getCurrent())) {
            loadData();
        }
    }

    @EventListener
    public void handleSelectedWorkOrderEvent(GetSelectedWorkOrderEvent event) {
        if (UI.getCurrent() != null && UI.getCurrent().equals(UI.getCurrent())) {
            //load WorkOrder what is double clicked!
            Optional<WorkOrder> selectedWorkOrder = Optional.of(event.getSelectedWorkOrder());
            if (selectedWorkOrder.isPresent() && selectedWorkOrder.get().getStarter() == true) {
                UI.getCurrent().navigate(WorkorderView.class, event.getSelectedWorkOrder().getId());
            }
            else{
                //get Starter of that WorkOrder
                String starterId = workOrderService.getStarterByLinkedId(event.getSelectedWorkOrder().getId()).get().getId();
                UI.getCurrent().navigate(WorkorderView.class, starterId);
            }
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        loadData();
    }

}

