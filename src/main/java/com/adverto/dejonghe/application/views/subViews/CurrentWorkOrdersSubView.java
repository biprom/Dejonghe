package com.adverto.dejonghe.application.views.subViews;

import com.adverto.dejonghe.application.customEvents.GetSelectedWorkOrderEvent;
import com.adverto.dejonghe.application.customEvents.ReloadProductListEvent;
import com.adverto.dejonghe.application.dbservices.WorkOrderService;
import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrder;
import com.adverto.dejonghe.application.entities.enums.employee.UserFunction;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY_INLINE;

@Component
@Scope("prototype")
public class CurrentWorkOrdersSubView extends VerticalLayout implements BeforeEnterObserver {

    WorkOrderService workOrderService;
    ApplicationEventPublisher eventPublisher;

    TreeGrid<WorkOrder> pendingWorkOrdersGrid;
    List<WorkOrder>selectedWorkOrders;
    TreeData<WorkOrder> treeData;
    HeaderRow headerRow;

    TextField filterSubject;
    TextField filterName;
    TextField filterResponsible;

    WorkOrder selectedWorkOrder;
    List<WorkOrder>workOrderBundleList = new ArrayList<>();
    Notification deleteWorkorderNotification;
    Notification detachWorkorderNotification;

    UserFunction userFunction = UserFunction.TECHNICIAN;
    Grid.Column<WorkOrder> columnDetach;


    @Autowired
    public CurrentWorkOrdersSubView(WorkOrderService workOrderService,
                                    ApplicationEventPublisher eventPublisher) {
        this.workOrderService = workOrderService;
        this.eventPublisher = eventPublisher;
        setUpfilters();
        createReportDelete();
        createReportDetach();
        this.add(setUpGrid());

    }

    private void setUpfilters() {
        filterSubject = new TextField();
        filterSubject.setPlaceholder("Commentaar");

        filterResponsible = new TextField();
        filterResponsible.setPlaceholder("Verantwoordelijk");

        filterName = new TextField();
        filterName.setPlaceholder("Werfadres,Stad,Straat");

        filterSubject.addValueChangeListener(event -> {
            addItemsToPendingWorkOrderGridFromFilter(
                    selectedWorkOrders.stream()
                            .filter(workOrder ->
                                    workOrder.getWorkOrderHeaderList().stream()
                                            .anyMatch(header ->
                                                    header.getDiscription() != null &&
                                                            header.getDiscription().toLowerCase().contains(event.getValue().toLowerCase())
                                            )
                            )
                            .collect(Collectors.toList())
            );
            pendingWorkOrdersGrid.getDataProvider().refreshAll();
        });

        filterResponsible.addValueChangeListener(event -> {
            addItemsToPendingWorkOrderGridFromFilter(
                    selectedWorkOrders.stream()
                            .filter(workOrder -> getActiveMasterEmployees(workOrder).toLowerCase().contains(event.getValue().toLowerCase())).collect(Collectors.toList())
            );
            pendingWorkOrdersGrid.getDataProvider().refreshAll();
        });

        filterName.addValueChangeListener(event -> {
            if(event.getValue().length() > 0){
                List<WorkOrder> collect = selectedWorkOrders.stream().filter(filter -> (filter.getWorkAddress().getAddressName().toLowerCase().contains(event.getValue().toLowerCase())) ||
                        (filter.getWorkAddress().getCity().toLowerCase().contains(event.getValue().toLowerCase())) ||
                        (filter.getWorkAddress().getStreet().toLowerCase().contains(event.getValue().toLowerCase()))).collect(Collectors.toList());
                addItemsToPendingWorkOrderGridFromFilter(collect);
                pendingWorkOrdersGrid.getDataProvider().refreshAll();
            }
            else{
                addItemsToPendingWorkOrderGrid(selectedWorkOrders);
                pendingWorkOrdersGrid.getDataProvider().refreshAll();
            }

        });
    }

    private Grid<WorkOrder> setUpGrid() {
        pendingWorkOrdersGrid = new TreeGrid<>();
        treeData = new TreeData<>();
        Grid.Column<WorkOrder> columnAddress = pendingWorkOrdersGrid.addHierarchyColumn(workOrder -> workOrder.getWorkAddress().getAddressName()).setHeader("Naam").setAutoWidth(true);
        pendingWorkOrdersGrid.addColumn(workorder -> workorder.getWorkDateTime().toLocalDate().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
                ))
                .setHeader("Datum").setAutoWidth(true);
        Grid.Column<WorkOrder> columnSubject = pendingWorkOrdersGrid.addComponentColumn(workOrder -> {
            TextArea textArea = new TextArea();
            textArea.setWidth("100%");
            textArea.setHeight("100%");
            textArea.setValue(getWorkOrderDiscriptions(workOrder));
            textArea.setReadOnly(true);
            return textArea;
        }).setHeader("Omschrijving").setAutoWidth(true);
        Grid.Column<WorkOrder> columnResponsible = pendingWorkOrdersGrid.addColumn(workOrder -> getActiveMasterEmployees(workOrder)).setHeader("Verantwoordelijke").setAutoWidth(true);
            columnDetach = pendingWorkOrdersGrid.addComponentColumn(item -> {
                if((!item.getStarter()) || (item.getLinkedWorkOrders() != null && item.getLinkedWorkOrders().size() > 0)){
                    Button closeButton = new Button(VaadinIcon.CONNECT.create());
                    closeButton.addThemeVariants(ButtonVariant.LUMO_WARNING);
                    closeButton.setAriaLabel("Ontkoppel werkbon");
                    closeButton.addClickListener(event -> {
                        selectedWorkOrder = item;
                        detachWorkorderNotification.open();
                    });
                    return closeButton;
                }
                else{
                    return new Span("");
                }
        }).setHeader("Ontkoppel").setAutoWidth(true);

        pendingWorkOrdersGrid.addComponentColumn(item -> {
            Button closeButton = new Button(VaadinIcon.TRASH.create());
            closeButton.addThemeVariants(ButtonVariant.LUMO_WARNING);
            closeButton.setAriaLabel("Verwijder werkbon");
            closeButton.addClickListener(event -> {
                selectedWorkOrder = item;
                deleteWorkorderNotification.open();
            });
            return closeButton;
        }).setHeader("Verwijder").setAutoWidth(true);

        pendingWorkOrdersGrid.addItemClickListener(event -> {
            selectedWorkOrder = treeData.getParent(event.getItem());
            if (selectedWorkOrder == null) {
                //if no parent then selected WorkOrder is the parent!
                selectedWorkOrder = event.getItem();
                //UI.getCurrent().getPage().executeJs("window.open($0, '_blank')", "/werkbon/"+event.getItem().getId());
            }
            eventPublisher.publishEvent(new GetSelectedWorkOrderEvent(this, selectedWorkOrder));
        });

        headerRow = pendingWorkOrdersGrid.appendHeaderRow();
        headerRow.getCell(columnAddress).setComponent(filterName);
        headerRow.getCell(columnSubject).setComponent(filterSubject);
        headerRow.getCell(columnResponsible).setComponent(filterResponsible);

        return pendingWorkOrdersGrid;
    }

    private String getWorkOrderDiscriptions(WorkOrder workOrder) {
        String discription = "";

        if(workOrder.getWorkOrderHeaderList().get(0).getDiscription() != null){
            discription = discription + workOrder.getWorkOrderHeaderList().get(0).getDiscription() + "\n";
        }
        if(workOrder.getWorkOrderHeaderList().get(1).getDiscription() != null){
            discription = discription + workOrder.getWorkOrderHeaderList().get(1).getDiscription() + "\n";
        }
        if(workOrder.getWorkOrderHeaderList().get(2).getDiscription() != null){
            discription = discription + workOrder.getWorkOrderHeaderList().get(2).getDiscription() + "\n";
        }
        if(workOrder.getWorkOrderHeaderList().get(3).getDiscription() != null){
            discription = discription + workOrder.getWorkOrderHeaderList().get(3).getDiscription() + "\n";
        }
        return discription;
    }

    private String getActiveMasterEmployees(WorkOrder workOrder) {
        String masterEmployees = "";

        if(workOrder.getMasterEmployeeTeam1() != null){
            masterEmployees = masterEmployees + workOrder.getMasterEmployeeTeam1().getFirstName() + " " + workOrder.getMasterEmployeeTeam1().getLastName().substring(0,1) + ", ";
        }
        if(workOrder.getMasterEmployeeTeam2() != null){
            masterEmployees = masterEmployees + workOrder.getMasterEmployeeTeam2().getFirstName() + " " + workOrder.getMasterEmployeeTeam1().getLastName().substring(0,1) + ", ";
        }
        if(workOrder.getMasterEmployeeTeam3() != null){
            masterEmployees = masterEmployees + workOrder.getMasterEmployeeTeam3().getFirstName() + " " + workOrder.getMasterEmployeeTeam1().getLastName().substring(0,1) + ", ";
        }
        if(workOrder.getMasterEmployeeTeam4() != null){
            masterEmployees = masterEmployees + workOrder.getMasterEmployeeTeam4().getFirstName() + " " + workOrder.getMasterEmployeeTeam1().getLastName().substring(0,1) + ", ";
        }
        return masterEmployees;
    }

    public void addItemsToPendingWorkOrderGrid(List<WorkOrder>workOrderList){
        if((workOrderList != null) && (workOrderList.size() > 0)){
            pendingWorkOrdersGrid.setVisible(true);
            selectedWorkOrders = workOrderList;
            treeData.clear();
            for(WorkOrder parent : workOrderList){
                treeData.addItem(null, parent);
                List<WorkOrder> children = getCoupledWorkOrders(parent);
                for (WorkOrder child : children) {
                    treeData.addItem(parent, child);
                }
            }
            TreeDataProvider<WorkOrder> dataProvider = new TreeDataProvider<>(treeData);
            pendingWorkOrdersGrid.setDataProvider(dataProvider);

        }
        else{
            pendingWorkOrdersGrid.setVisible(false);
            Notification notification = Notification.show("Geen lopende Werkbonnen");
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }

    public void addItemsToPendingWorkOrderGridFromFilter(List<WorkOrder>filteredWorkOrderList){
        if((filteredWorkOrderList != null) && (filteredWorkOrderList.size() > 0)){
            pendingWorkOrdersGrid.setVisible(true);
            treeData.clear();
            for(WorkOrder parent : filteredWorkOrderList){
                treeData.addItem(null, parent);
                List<WorkOrder> children = getCoupledWorkOrders(parent);
                for (WorkOrder child : children) {
                    treeData.addItem(parent, child);
                }
            }
            TreeDataProvider<WorkOrder> dataProvider = new TreeDataProvider<>(treeData);
            pendingWorkOrdersGrid.setDataProvider(dataProvider);

        }
        else{
            treeData.clear();
            TreeDataProvider<WorkOrder> dataProvider = new TreeDataProvider<>(treeData);
            pendingWorkOrdersGrid.setDataProvider(dataProvider);
            Notification notification = Notification.show("Geen lopende Werkbonnen");
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }

    public List<WorkOrder> getCoupledWorkOrders(WorkOrder starterWorkOrder) {
        return workOrderService.getCoupledWorkOrders(starterWorkOrder.getLinkedWorkOrders());
    }

    public Notification createReportDelete() {
        deleteWorkorderNotification = new Notification();
        deleteWorkorderNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);

        Icon icon = VaadinIcon.WARNING.create();
        Button retryBtn = new Button("Annuleer",
                clickEvent -> deleteWorkorderNotification.close());
        retryBtn.getStyle().setMargin("0 0 0 var(--lumo-space-l)");

        var layout = new HorizontalLayout(icon,
                new Text("Ben je zeker dat je deze werkbon wil wissen?"), retryBtn,
                createCloseBtn(deleteWorkorderNotification));
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        deleteWorkorderNotification.add(layout);

        return deleteWorkorderNotification;
    }

    public Notification createReportDetach() {
        detachWorkorderNotification = new Notification();
        detachWorkorderNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);

        Icon icon = VaadinIcon.WARNING.create();
        Button retryBtn = new Button("Annuleer",
                clickEvent -> detachWorkorderNotification.close());
        retryBtn.getStyle().setMargin("0 0 0 var(--lumo-space-l)");

        var layout = new HorizontalLayout(icon,
                new Text("Ben je zeker dat je deze werkbon wil ontkoppelen?"), retryBtn,
                createDetachBtn(detachWorkorderNotification));
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        detachWorkorderNotification.add(layout);

        return detachWorkorderNotification;
    }

    public Button createDetachBtn(Notification notification) {
        Button connectBtn = new Button(VaadinIcon.CONNECT.create(),
                clickEvent -> {
                    if((selectedWorkOrder.getStarter() != null) && (selectedWorkOrder.getStarter() == true)){
                        //TODO find coupled workorders and set next workorder as starer + copy  coupled to the next workorder
                        List<WorkOrder> workOrderListByStarterId = workOrderService.getWorkOrderListByStarterId(selectedWorkOrder.getId());
                        WorkOrder starter = workOrderListByStarterId.stream().filter(workOrder -> workOrder.getStarter() == true).findFirst().get();
                        List<String> linkedWorkOrders = starter.getLinkedWorkOrders();
                        if(linkedWorkOrders != null && !linkedWorkOrders.isEmpty()){
                            //this is a coupled starter
                            Optional<WorkOrder> optFirstCoupledWorkOrder = workOrderService.getWorkOrderById(starter.getLinkedWorkOrders().get(0));
                            if(optFirstCoupledWorkOrder.isPresent()){
                                starter.getLinkedWorkOrders().remove(0);
                                optFirstCoupledWorkOrder.get().setLinkedWorkOrders(starter.getLinkedWorkOrders());
                                optFirstCoupledWorkOrder.get().setStarter(true);
                                workOrderService.save(starter);
                                workOrderService.save(optFirstCoupledWorkOrder.get());
                                eventPublisher.publishEvent(new ReloadProductListEvent(this,"message"));
                                addItemsToPendingWorkOrderGrid(selectedWorkOrders);
                                pendingWorkOrdersGrid.getDataProvider().refreshAll();

                            }
                            else{
                                Notification show = Notification.show("Geen gekoppelde werkbon gevonden!");
                                show.addThemeVariants(NotificationVariant.LUMO_ERROR);
                            }
                        }
                        else{
                            //this is a standalone starter so it can not be detached!
                            Notification show = Notification.show("Deze werkbon is niet gekoppeld en kan daardoor niet ontkoppeld worden!");
                            show.addThemeVariants(NotificationVariant.LUMO_ERROR);

                        }
                    }
                    else{
                        //TODO detach workorder from the starter by removing the id and set workorder as starter.
                        Optional<WorkOrder> starterByLinkedId = workOrderService.getStarterByLinkedId(selectedWorkOrder.getId());
                        if(starterByLinkedId.isPresent()){
                            starterByLinkedId.get().getLinkedWorkOrders().remove(selectedWorkOrder.getId());
                            workOrderService.save(starterByLinkedId.get());
                            selectedWorkOrder.setStarter(true);
                            workOrderService.save(selectedWorkOrder);
                            eventPublisher.publishEvent(new ReloadProductListEvent(this,"message"));
                            addItemsToPendingWorkOrderGrid(selectedWorkOrders);
                            pendingWorkOrdersGrid.getDataProvider().refreshAll();
                        }
                        else{
                            Notification show = Notification.show("Geen starter gevonden voor deze werkbon!");
                            show.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        }

                    }
                    notification.close();
                });
        connectBtn.addThemeVariants(LUMO_TERTIARY_INLINE);
        return connectBtn;
    }

    public Button createCloseBtn(Notification notification) {
        Button removeBtn = new Button(VaadinIcon.TRASH.create(),
                clickEvent -> {
                    if(selectedWorkOrders != null){
                        //when filter is selected
                        selectedWorkOrders.remove(selectedWorkOrder);
                        pendingWorkOrdersGrid.getDataProvider().refreshAll();
                        workOrderService.delete(selectedWorkOrder);
                        Notification.show("Werkbon is verwijderd");
                    }
                    else{
                        Notification.show("Geen werkbonnen te verwijderen");
                    }
                    notification.close();
                });
        removeBtn.addThemeVariants(LUMO_TERTIARY_INLINE);
        return removeBtn;
    }


    public List<WorkOrder> getSelectedBundeledWorkOrders() {
        workOrderBundleList.clear();
        List<WorkOrder>selectedChildren = treeData.getChildren(selectedWorkOrder);
        if(selectedChildren != null){
            workOrderBundleList.add(selectedWorkOrder);
            workOrderBundleList.addAll(selectedChildren);
            return workOrderBundleList;
        }
        else{
            workOrderBundleList.add(selectedWorkOrder);
            return (workOrderBundleList);
        }
    }

    public Optional<Set<WorkOrder>> getSelectedWorkOrders(){
        Set<WorkOrder> selectedItems = pendingWorkOrdersGrid.getSelectedItems();
        return Optional.of(selectedItems);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {

    }

    public void setAuthorisation(UserFunction userFunction) {
        this.userFunction = userFunction;
        if(this.userFunction.compareTo(UserFunction.ADMIN)==0 ){
            pendingWorkOrdersGrid.setSelectionMode(Grid.SelectionMode.MULTI);
            columnDetach.setVisible(true);
        }
        else{
            pendingWorkOrdersGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
            columnDetach.setVisible(false);
        }
    }

    public void removeSelectedWorkOrdersFromGrid() {
        for(WorkOrder workOrder : getSelectedWorkOrders().get()){
            treeData.removeItem(workOrder);
        }
        pendingWorkOrdersGrid.getDataProvider().refreshAll();
    }
}
