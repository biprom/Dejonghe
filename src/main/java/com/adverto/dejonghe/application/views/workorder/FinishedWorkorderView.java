package com.adverto.dejonghe.application.views.workorder;

import com.adverto.dejonghe.application.customEvents.AddProductEventListener;
import com.adverto.dejonghe.application.customEvents.GetSelectedWorkOrderEvent;
import com.adverto.dejonghe.application.customEvents.ReloadProductListEvent;
import com.adverto.dejonghe.application.dbservices.*;
import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrder;
import com.adverto.dejonghe.application.entities.enums.employee.UserFunction;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkOrderStatus;
import com.adverto.dejonghe.application.entities.invoice.Invoice;
import com.adverto.dejonghe.application.services.invoice.InvoiceServices;
import com.adverto.dejonghe.application.views.subViews.CurrentWorkOrdersSubView;
import com.adverto.dejonghe.application.views.subViews.SelectProductSubView;
import com.adverto.dejonghe.application.views.subViews.ShowImageSubVieuw;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.*;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import java.util.*;

@PageTitle("Afgewerkte Werkbonnen")
@Route("werkbonnenAfgewerkt")
@Menu(order = 0, icon = LineAwesomeIconUrl.WRENCH_SOLID)
@Component
@Scope("prototype")
public class FinishedWorkorderView extends Div implements BeforeEnterObserver {

    private final InvoiceServices invoiceServices;
    EmployeeService employeeService;
    CustomerService customerService;
    ProductService productService;
    SelectProductSubView selectProductSubView;
    WorkOrderService workOrderService;
    GridFsTemplate gridFsTemplate;
    CurrentWorkOrdersSubView currtentWorkOrdersSubVieuw;
    ShowImageSubVieuw showImageSubVieuw;
    AddProductEventListener listener;
    InvoiceServices createOngoingInvoiceService;
    InvoiceService invoiceService;

    Optional<List<WorkOrder>> allFinishedStarters;

    Button buttonSetWorkOrderOpen;
    Button makeProFormaPerDay;
    Button makeProFormaMerged;

    public FinishedWorkorderView(ProductService productService,
                                 CustomerService customerService,
                                 EmployeeService employeeService,
                                 SelectProductSubView selectProductSubView,
                                 WorkOrderService workOrderService,
                                 GridFsTemplate gridFsTemplate,
                                 CurrentWorkOrdersSubView currtentWorkOrdersSubVieuw,
                                 ShowImageSubVieuw showImageSubVieuw,
                                 AddProductEventListener listener,
                                 InvoiceServices createOngoingInvoiceService, InvoiceServices invoiceServices,
                                 InvoiceService invoiceService) {
        this.productService = productService;
        this.selectProductSubView = selectProductSubView;
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.workOrderService = workOrderService;
        this.gridFsTemplate = gridFsTemplate;
        this.currtentWorkOrdersSubVieuw = currtentWorkOrdersSubVieuw;
        this.showImageSubVieuw = showImageSubVieuw;
        this.listener = listener;
        this.createOngoingInvoiceService = createOngoingInvoiceService;
        this.invoiceServices = invoiceServices;
        this.invoiceService = invoiceService;
    }

    private HorizontalLayout getButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidth("100%");

        buttonSetWorkOrderOpen = new Button("Zet terug open");
        buttonSetWorkOrderOpen.setWidth("20%");

        makeProFormaPerDay = new Button("Maak proforma per dag");
        makeProFormaPerDay.setWidth("20%");

        makeProFormaMerged = new Button("Maak proforma samengevoegd");
        makeProFormaMerged.setWidth("20%");

        buttonLayout.add(getNavigateToSelectedWorkOrder(), buttonSetWorkOrderOpen, makeProFormaPerDay, makeProFormaMerged);

        return buttonLayout;
    }

    private void setUpOpenWorkOrder() {
        buttonSetWorkOrderOpen.addClickListener(event -> {
            List<WorkOrder> selectedBundeledWorkOrders = currtentWorkOrdersSubVieuw.getSelectedBundeledWorkOrders();
            if ((selectedBundeledWorkOrders != null) && (!selectedBundeledWorkOrders.isEmpty())) {
                selectedBundeledWorkOrders.forEach(workOrder -> {
                    if(workOrder != null){
                        workOrder.setWorkOrderStatus(WorkOrderStatus.RUNNING);
                        workOrderService.save(workOrder);
                    }
                });
                loadData();
            }
            else{
                Notification.show("Geen geselecteerde werkbonnen!");
            }
        });
    }


    @EventListener
    public void handleReloadEvent(ReloadProductListEvent event) {
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

    public void loadData() {
        allFinishedStarters = workOrderService.getAllByStatusAndStarter(WorkOrderStatus.FINISHED, true);
        if(allFinishedStarters.isPresent()){
            currtentWorkOrdersSubVieuw.addItemsToPendingWorkOrderGrid(allFinishedStarters.get());
            currtentWorkOrdersSubVieuw.setAuthorisation(UserFunction.ADMIN);
            currtentWorkOrdersSubVieuw.setSizeFull();
        }
        else{
            Notification notification = Notification.show("Geen lopenede werkbonnen gevonden");
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }

    private Button getNavigateToSelectedWorkOrder() {
        Button buttonOpenSelectedWorkOrder = new Button("Koppel geselecteerde werkbonnen");
        buttonOpenSelectedWorkOrder.setWidth("20%");
        buttonOpenSelectedWorkOrder.addClickListener(event -> {

            Optional<Set<WorkOrder>> selectedWorkOrders = currtentWorkOrdersSubVieuw.getSelectedWorkOrders();
            //All selected WorkOrders need to be Starters to couple!
            if (selectedWorkOrders.isPresent() && selectedWorkOrders.get().stream().allMatch(p -> p.getStarter() == true)) {
                //find oldest Workorder to set as starter, rest is non-starter
                Optional<WorkOrder> oldestWorkOrder = selectedWorkOrders.get().stream()
                        .min(Comparator.comparing(WorkOrder::getWorkDateTime));
                //all of the rest of the coupled Workorders need to ben of non-starter
                for(WorkOrder workOrder : selectedWorkOrders.get()){
                    if(!(workOrder.getId().equals(oldestWorkOrder.get().getId()))){
                        workOrder.setStarter(false);
                        workOrderService.save(workOrder);
                        //if oldest workorder does not contain a linkedWorkOrderList make one
                        if(oldestWorkOrder.get().getLinkedWorkOrders()==null){
                            oldestWorkOrder.get().setLinkedWorkOrders(new ArrayList<>());
                        }
                        //add id to oldest ticket linkedWorkOrderList
                        oldestWorkOrder.get().getLinkedWorkOrders().add(workOrder.getId());
                    }
                }
                workOrderService.save(oldestWorkOrder.get());
                loadData();
            }
            else{
                Notification notification = Notification.show("Werkbonnen die worden gekoppeld moeten van het type 'starter' zijn");
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });
        return buttonOpenSelectedWorkOrder;
    }

    private void setUpButtonMakeProformaInvoice() {
        makeProFormaMerged.addClickListener(event -> {
            Optional<Set<WorkOrder>> selectedWorkOrders = currtentWorkOrdersSubVieuw.getSelectedWorkOrders();
            if (selectedWorkOrders.isPresent()) {
                Invoice invoice = createOngoingInvoiceService.generateMergedInvoice(selectedWorkOrders.get());
                invoiceService.save(invoice);
            }
        });
    }

    private void setUpButtonMakeProformaPerDay(){
        makeProFormaPerDay.addClickListener(event -> {
            Optional<Set<WorkOrder>> selectedWorkOrders = currtentWorkOrdersSubVieuw.getSelectedWorkOrders();
            if (selectedWorkOrders.isPresent()) {
                Invoice invoice = createOngoingInvoiceService.getnerateInvoicePerDay(selectedWorkOrders.get());
                invoiceService.save(invoice);
            }
        });
    }


    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        this.removeAll();
        this.setSizeFull();
        this.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column");
        this.add(getButtonLayout());
        setUpOpenWorkOrder();
        setUpButtonMakeProformaInvoice();
        setUpButtonMakeProformaPerDay();
        currtentWorkOrdersSubVieuw.setWidthFull();
        currtentWorkOrdersSubVieuw.getStyle().set("flex-grow", "1");
        currtentWorkOrdersSubVieuw.getStyle().set("overflow", "auto");
        this.add(currtentWorkOrdersSubVieuw);
        loadData();
    }
}

