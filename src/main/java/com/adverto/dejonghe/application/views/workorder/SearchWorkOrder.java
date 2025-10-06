package com.adverto.dejonghe.application.views.workorder;

import com.adverto.dejonghe.application.dbservices.CustomerService;
import com.adverto.dejonghe.application.dbservices.EmployeeService;
import com.adverto.dejonghe.application.dbservices.ProductService;
import com.adverto.dejonghe.application.dbservices.WorkOrderService;
import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrder;
import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrderHeader;
import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrderTime;
import com.adverto.dejonghe.application.entities.customers.Address;
import com.adverto.dejonghe.application.entities.employee.Employee;
import com.adverto.dejonghe.application.entities.enums.fleet.Fleet;
import com.adverto.dejonghe.application.entities.enums.fleet.FleetWorkType;
import com.adverto.dejonghe.application.entities.enums.workorder.Tools;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkLocation;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkType;
import com.adverto.dejonghe.application.views.subViews.SelectProductSubView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.router.*;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@PageTitle("Zoek Werkbon")
@Route("zoekWerkbon")
@Menu(order = 0, icon = LineAwesomeIconUrl.WRENCH_SOLID)
public class SearchWorkOrder extends Div implements BeforeEnterObserver {

    EmployeeService employeeService;
    CustomerService customerService;
    ProductService productService;
    SelectProductSubView selectProductSubView;
    WorkOrderService workOrderService;

    SplitLayout mainSplitLayout;
    SplitLayout headerSplitLayout;

    FormLayout formLayout;

    ComboBox<Address> addressComboBox = new ComboBox<>();
    DateTimePicker dateTimePicker = new DateTimePicker();
    ComboBox<WorkLocation> locationComboBox = new ComboBox<>();
    ComboBox<WorkType> typeComboBox = new ComboBox<>();

    ComboBox<Employee>cbMasterEmployee1 = new ComboBox<>();
    ComboBox<Employee>cbMasterEmployee2 = new ComboBox<>();
    ComboBox<Employee>cbMasterEmployee3 = new ComboBox<>();
    ComboBox<Employee>cbMasterEmployee4 = new ComboBox<>();

    MultiSelectComboBox<Employee> cbExtraEmployees1 = new MultiSelectComboBox<>();
    MultiSelectComboBox<Employee> cbExtraEmployees2 = new MultiSelectComboBox<>();
    MultiSelectComboBox<Employee> cbExtraEmployees3 = new MultiSelectComboBox<>();
    MultiSelectComboBox<Employee> cbExtraEmployees4 = new MultiSelectComboBox<>();

    TextArea taDiscription = new TextArea();

    Grid<WorkOrderTime>workOrderTimeGrid;

//    TimePicker timePickerUpTeam1 = new TimePicker();
//    TimePicker timePickerDownTeam1 = new TimePicker();
//    TimePicker timePickerStartTeam1 = new TimePicker();
//    TimePicker timePickerStopTeam1 = new TimePicker();
//    ComboBox<Integer>cbPauze = new ComboBox<>();

    ComboBox<Fleet> fleetComboBox = new ComboBox<>();
    TextField tfRoadTax = new TextField();
    TextField tfTunnelTax = new TextField();
    ComboBox<FleetWorkType> fleetWorkTypeComboBox = new ComboBox<>();

    MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    Upload dropEnabledUpload = new Upload(buffer);

    VirtualList<Tools> toolsVirtualList;

    VerticalLayout vLayoutHeaderLevel2Tabs;
    VerticalLayout vLayoutHeaderLevel3Tabs;
    VerticalLayout vLayoutHeaderLevel4Tabs;
    VerticalLayout vLayoutHeaderLevel5Tabs;
    VerticalLayout vLayoutHeaderLevel6Tabs;
    VerticalLayout vLayoutHeaderLevel7Tabs;

    Integer selectedTeam = 1;

    Binder<WorkOrder>workOrderBinder;
    Binder<WorkOrderHeader>workOrderHeaderBinder;

    WorkOrder selectedWorkOrder;

    public SearchWorkOrder(ProductService productService,
                           CustomerService customerService,
                           EmployeeService employeeService,
                           SelectProductSubView selectProductSubView,
                           WorkOrderService workOrderService) {
        this.productService = productService;
        this.selectProductSubView = selectProductSubView;
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.workOrderService = workOrderService;

        setUpSplitLayouts();
        getWorkOrderHeader();
        VerticalLayout vLayout = new VerticalLayout();
        formLayout.add(vLayoutHeaderLevel2Tabs);
        formLayout.add(vLayoutHeaderLevel3Tabs,2);
        formLayout.add(vLayoutHeaderLevel4Tabs);
        formLayout.add(vLayoutHeaderLevel5Tabs);
        formLayout.add(vLayoutHeaderLevel6Tabs);
        formLayout.add(vLayoutHeaderLevel7Tabs,2);
        headerSplitLayout.addToPrimary(formLayout);
        vLayout.add(selectProductSubView.getFilter());
        vLayout.add(selectProductSubView.getSelectedProductGrid());
        headerSplitLayout.addToSecondary(vLayout);
        mainSplitLayout.addToPrimary(selectProductSubView.getLayout());
        mainSplitLayout.addToSecondary(headerSplitLayout);
        this.setHeightFull();
        add(mainSplitLayout);
        setUpWorkOrderBinder();
        setUpWorkOrderHeaderBinder();
        setUpTimeGrid();
    }

    private void setUpTimeGrid() {
        workOrderTimeGrid = new Grid<>();
        workOrderTimeGrid.setWidthFull();
        workOrderTimeGrid.addComponentColumn(component -> {
            TimePicker timeUpPicker = new TimePicker();
            timeUpPicker.setLocale(Locale.FRENCH);
            timeUpPicker.setStep(Duration.ofMinutes(15));
            timeUpPicker.setWidth("100%");
            timeUpPicker.addValueChangeListener(event -> {
                Notification.show("Time Up Picker Value: " + timeUpPicker.getValue().toString());
            });
            return timeUpPicker;
        }).setHeader("Tijd Vertrek");

        workOrderTimeGrid.addComponentColumn(component -> {
            TimePicker timeStartPicker = new TimePicker();
            timeStartPicker.setLocale(Locale.FRENCH);
            timeStartPicker.setStep(Duration.ofMinutes(15));
            timeStartPicker.setWidth("100%");
            timeStartPicker.addValueChangeListener(event -> {
                Notification.show("Time Up Picker Value: " + timeStartPicker.getValue().toString());
            });
            return timeStartPicker;
        }).setHeader("Tijd aankomst Klant");

        workOrderTimeGrid.addComponentColumn(component -> {
            TimePicker timeStopPicker = new TimePicker();
            timeStopPicker.setLocale(Locale.FRENCH);
            timeStopPicker.setStep(Duration.ofMinutes(15));
            timeStopPicker.setWidth("100%");
            timeStopPicker.addValueChangeListener(event -> {
                Notification.show("Time Up Picker Value: " + timeStopPicker.getValue().toString());
            });
            return timeStopPicker;
        }).setHeader("Tijd vertrek Klant");

        workOrderTimeGrid.addComponentColumn(component -> {
            TimePicker timeDownPicker = new TimePicker();
            timeDownPicker.setLocale(Locale.FRENCH);
            timeDownPicker.setStep(Duration.ofMinutes(15));
            timeDownPicker.setWidth("100%");
            timeDownPicker.addValueChangeListener(event -> {
                Notification.show("Time Up Picker Value: " + timeDownPicker.getValue().toString());
            });
            return timeDownPicker;
        }).setHeader("Tijd Terug");
    }

    private void setUpWorkOrderHeaderBinder() {
        workOrderHeaderBinder = new Binder<>(WorkOrderHeader.class);
        workOrderHeaderBinder.forField(taDiscription)
                .bind(WorkOrderHeader::getDiscription, WorkOrderHeader::setDiscription);
        workOrderHeaderBinder.forField(typeComboBox)
                .asRequired("Gelieve een werktype te selecteren!")
                .bind(WorkOrderHeader::getWorkType, WorkOrderHeader::setWorkType);
//        workOrderHeaderBinder.forField(timePickerUpTeam1)
//                .bind(WorkOrderHeader::getTimeUp, WorkOrderHeader::setTimeUp);
//        workOrderHeaderBinder.forField(timePickerStartTeam1)
//                .bind(WorkOrderHeader::getTimeStart, WorkOrderHeader::setTimeStart);
//        workOrderHeaderBinder.forField(timePickerStopTeam1)
//                .bind(WorkOrderHeader::getTimeStop, WorkOrderHeader::setTimeStop);
//        workOrderHeaderBinder.forField(timePickerDownTeam1)
//                .bind(WorkOrderHeader::getTimeDown, WorkOrderHeader::setTimeDown);
//        workOrderHeaderBinder.forField(cbPauze)
//                .bind(WorkOrderHeader::getPauze, WorkOrderHeader::setPauze);
        workOrderHeaderBinder.forField(fleetComboBox)
                .bind(WorkOrderHeader::getFleet, WorkOrderHeader::setFleet);
        workOrderHeaderBinder.forField(tfRoadTax)
                .withNullRepresentation("0.0")
                .withConverter(new StringToDoubleConverter("Dit is geen decimaal getal!"))
                .bind(WorkOrderHeader::getRoadTax, WorkOrderHeader::setRoadTax);
        workOrderHeaderBinder.forField(tfTunnelTax)
                .withNullRepresentation("0.0")
                .withConverter(new StringToDoubleConverter("Dit is geen decimaal getal!"))
                .bind(WorkOrderHeader::getTunnelTax, WorkOrderHeader::setTunnelTax);
        workOrderHeaderBinder.forField(fleetWorkTypeComboBox)
                .bind(WorkOrderHeader::getFleetWorkType, WorkOrderHeader::setFleetWorkType);
        workOrderHeaderBinder.addValueChangeListener(workOrderHeader -> {
            try {
                workOrderHeaderBinder.writeBean(selectedWorkOrder.getWorkOrderHeaderList().get(selectedTeam-1));
                workOrderBinder.writeBean(selectedWorkOrder);
            } catch (ValidationException e) {
                Notification.show("Kon de werkbon nog niet bewaren");
            }
            workOrderService.save(selectedWorkOrder);
        });
    }

    private void setUpWorkOrderBinder() {
        workOrderBinder = new Binder<>(WorkOrder.class);
        workOrderBinder.forField(addressComboBox)
                .asRequired("Elke werkbon met een werfadres hebben!")
                .bind(WorkOrder::getWorkAddress, WorkOrder::setWorkAddress);
        workOrderBinder.forField(addressComboBox)
                .asRequired("Gelieve een klant in te geven!")
                .bind(WorkOrder::getWorkAddress, WorkOrder::setWorkAddress);
        workOrderBinder.forField(dateTimePicker)
                .asRequired("Gelieve een datum te selecteren!")
                .bind(WorkOrder::getWorkDateTime, WorkOrder::setWorkDateTime);
        workOrderBinder.forField(locationComboBox)
                .asRequired("Gelieve een werkplaats te selecteren!")
                .bind(WorkOrder::getWorkLocation, WorkOrder::setWorkLocation);
        workOrderBinder.forField(cbMasterEmployee1)
                .withNullRepresentation(cbMasterEmployee1.getEmptyValue())
                .asRequired("Gelieve een verantwoordelijke te selecteren!")
                .bind(WorkOrder::getMasterEmployeeTeam1, WorkOrder::setMasterEmployeeTeam1);
        workOrderBinder.forField(cbMasterEmployee2)
                .bind(WorkOrder::getMasterEmployeeTeam2, WorkOrder::setMasterEmployeeTeam2);
        workOrderBinder.forField(cbMasterEmployee3)
                .bind(WorkOrder::getMasterEmployeeTeam3, WorkOrder::setMasterEmployeeTeam3);
        workOrderBinder.forField(cbMasterEmployee4)
                .bind(WorkOrder::getMasterEmployeeTeam4, WorkOrder::setMasterEmployeeTeam4);
        workOrderBinder.forField(cbExtraEmployees1)
                .bind(WorkOrder::getExtraEmployeesTeam1, WorkOrder::setExtraEmployeesTeam1);
        workOrderBinder.forField(cbExtraEmployees2)
                .bind(WorkOrder::getExtraEmployeesTeam2, WorkOrder::setExtraEmployeesTeam2);
        workOrderBinder.forField(cbExtraEmployees3)
                .bind(WorkOrder::getExtraEmployeesTeam3, WorkOrder::setExtraEmployeesTeam3);
        workOrderBinder.forField(cbExtraEmployees4)
                .bind(WorkOrder::getExtraEmployeesTeam4, WorkOrder::setExtraEmployeesTeam4);
        workOrderBinder.addValueChangeListener(workOrder -> {
            try {
                workOrderBinder.writeBean(selectedWorkOrder);
            } catch (ValidationException e) {
                Notification.show("Kon de werkbon nog niet bewaren");
            }
            workOrderService.save(selectedWorkOrder);
        });
    }

    private void setUpSplitLayouts() {
        mainSplitLayout = new SplitLayout();
        mainSplitLayout.setSizeFull();
        mainSplitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);

        headerSplitLayout = new SplitLayout();
        headerSplitLayout.setSplitterPosition(50);
        headerSplitLayout.setSizeFull();
        headerSplitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);

        headerSplitLayout.addSplitterDragendListener(event -> {
            selectProductSubView.setSplitPosition(headerSplitLayout.getSplitterPosition());
        });
    }

    private FormLayout getWorkOrderHeader() {
        formLayout = new FormLayout();
        formLayout.setSizeFull();
        formLayout.addClassName("blueborder");
        formLayout.add(getFirstStepHeader());
        return formLayout;
    }

    private ComboBox<WorkType> getTypeComboBox() {
        typeComboBox = new ComboBox<>();
        typeComboBox.setItems(WorkType.values());
        typeComboBox.setItemLabelGenerator(WorkType::getDiscription);
        typeComboBox.setPlaceholder("Type");
        typeComboBox.setWidthFull();
        return typeComboBox;
    }

    private ComboBox<WorkLocation> getLocationComboBox() {
        vLayoutHeaderLevel2Tabs = new VerticalLayout();
        vLayoutHeaderLevel2Tabs.setSizeFull();
        vLayoutHeaderLevel2Tabs.setVisible(false);

        vLayoutHeaderLevel3Tabs = new VerticalLayout();
        vLayoutHeaderLevel3Tabs.setSizeFull();
        vLayoutHeaderLevel3Tabs.setVisible(false);

        vLayoutHeaderLevel4Tabs = new VerticalLayout();
        vLayoutHeaderLevel4Tabs.setSizeFull();
        vLayoutHeaderLevel4Tabs.setVisible(false);

        vLayoutHeaderLevel5Tabs = new VerticalLayout();
        vLayoutHeaderLevel5Tabs.setSizeFull();
        vLayoutHeaderLevel5Tabs.setVisible(false);

        vLayoutHeaderLevel6Tabs = new VerticalLayout();
        vLayoutHeaderLevel6Tabs.setSizeFull();
        vLayoutHeaderLevel6Tabs.setVisible(true);

        vLayoutHeaderLevel7Tabs = new VerticalLayout();
        vLayoutHeaderLevel7Tabs.setSizeFull();
        vLayoutHeaderLevel7Tabs.setVisible(true);

        locationComboBox = new ComboBox<>();
        locationComboBox.setItems(WorkLocation.values());
        locationComboBox.setItemLabelGenerator(WorkLocation::getDiscription);
        locationComboBox.setPlaceholder("Locatie");
        locationComboBox.setWidthFull();
        locationComboBox.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                if(event.getValue().compareTo(WorkLocation.ON_THE_MOVE) == 0){
                    vLayoutHeaderLevel2Tabs.setVisible(true);
                    vLayoutHeaderLevel3Tabs.setVisible(true);
                    vLayoutHeaderLevel4Tabs.setVisible(true);
                    vLayoutHeaderLevel5Tabs.setVisible(true);
                    vLayoutHeaderLevel2Tabs.removeAll();
                    vLayoutHeaderLevel3Tabs.removeAll();
                    vLayoutHeaderLevel4Tabs.removeAll();
                    vLayoutHeaderLevel5Tabs.removeAll();
                    vLayoutHeaderLevel6Tabs.removeAll();
                    vLayoutHeaderLevel7Tabs.removeAll();
                    vLayoutHeaderLevel2Tabs.add(getLevel2(WorkLocation.ON_THE_MOVE));
                    vLayoutHeaderLevel3Tabs.add(getLevel5(WorkLocation.ON_THE_MOVE));
                    vLayoutHeaderLevel4Tabs.add(getLevel3(WorkLocation.ON_THE_MOVE));
                    vLayoutHeaderLevel5Tabs.add(getLevel4(WorkLocation.ON_THE_MOVE));
                    vLayoutHeaderLevel6Tabs.add(getLevel6(WorkLocation.ON_THE_MOVE));
                    vLayoutHeaderLevel7Tabs.add(dropEnabledUpload);
                    
                }
                else{
                    vLayoutHeaderLevel2Tabs.setVisible(true);
                    vLayoutHeaderLevel3Tabs.setVisible(true);
                    vLayoutHeaderLevel4Tabs.setVisible(true);
                    vLayoutHeaderLevel5Tabs.setVisible(true);
                    vLayoutHeaderLevel2Tabs.removeAll();
                    vLayoutHeaderLevel3Tabs.removeAll();
                    vLayoutHeaderLevel4Tabs.removeAll();
                    vLayoutHeaderLevel5Tabs.removeAll();
                    vLayoutHeaderLevel6Tabs.removeAll();
                    vLayoutHeaderLevel2Tabs.add(getLevel2(WorkLocation.WORKPLACE));
                    vLayoutHeaderLevel3Tabs.add(getLevel5(WorkLocation.WORKPLACE));
                    vLayoutHeaderLevel4Tabs.add(getLevel3(WorkLocation.WORKPLACE));
                    vLayoutHeaderLevel6Tabs.add(getLevel6(WorkLocation.WORKPLACE));
                    vLayoutHeaderLevel7Tabs.add(dropEnabledUpload);
                }
            }
        });
        return locationComboBox;
    }

    private VerticalLayout getLevel6(WorkLocation workLocation) {
        VerticalLayout verticalLayoutLevel6 = new VerticalLayout();
        verticalLayoutLevel6.setWidth("100%");
        if(workLocation == WorkLocation.ON_THE_MOVE){
            toolsVirtualList = new VirtualList<>();
            toolsVirtualList.setItems(Arrays.stream(Tools.values()).filter(item -> item.getWorkLocation().equals(WorkLocation.ON_THE_MOVE)).collect(Collectors.toSet()));
            toolsVirtualList.setRenderer(toolCardRenderer);
            verticalLayoutLevel6.add(toolsVirtualList);
            return verticalLayoutLevel6;
        }
        else{
            toolsVirtualList = new VirtualList<>();
            toolsVirtualList.setItems(Arrays.stream(Tools.values()).filter(item -> item.getWorkLocation().equals(WorkLocation.WORKPLACE)).collect(Collectors.toSet()));
            toolsVirtualList.setRenderer(toolCardRenderer);
            verticalLayoutLevel6.add(new Span("Gelieve de juiste opties te selecteren"));
            verticalLayoutLevel6.add(toolsVirtualList);
            return verticalLayoutLevel6;
        }

    }

    private VerticalLayout getLevel5(WorkLocation workLocation) {
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidth("100%");
        taDiscription.setWidthFull();
        taDiscription.setHeight("100%");
        taDiscription.setPlaceholder("Gelieve een duidelijke omschrijving te geven betreffende de uitgevoerde werken");
        verticalLayout.add(taDiscription);
        return verticalLayout;
    }

    private VerticalLayout getLevel4(WorkLocation workLocation) {
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidth("100%");
        if(workLocation == WorkLocation.ON_THE_MOVE){

            fleetComboBox.setItems(Fleet.values());
            fleetComboBox.setItemLabelGenerator(Fleet::getDiscription);
            fleetComboBox.setWidth("100%");
            fleetComboBox.setPlaceholder("Voertuig");

            tfRoadTax.setWidth("100%");
            tfRoadTax.setPlaceholder("Wegentax");

            tfTunnelTax.setWidth("100%");
            tfTunnelTax.setPlaceholder("Tunneltax");

            fleetWorkTypeComboBox.setItems(FleetWorkType.values());
            fleetWorkTypeComboBox.setItemLabelGenerator(FleetWorkType::getDiscription);
            fleetWorkTypeComboBox.setWidth("100%");
            fleetWorkTypeComboBox.setPlaceholder("Type werk kraan");

            verticalLayout.add(fleetComboBox, tfRoadTax, tfTunnelTax, fleetWorkTypeComboBox);
        }
        return verticalLayout;
    }

    private VerticalLayout getLevel3(WorkLocation workLocation) {
        VerticalLayout verticalLayout = new VerticalLayout();
        if(workLocation == WorkLocation.ON_THE_MOVE){

            verticalLayout.add(workOrderTimeGrid);
        }
        else{
            verticalLayout.add(workOrderTimeGrid);
        }
        return verticalLayout;
    }


    private VerticalLayout getLevel2(WorkLocation workLocation) {
        if(workLocation == WorkLocation.ON_THE_MOVE){
            return (getSecondStepOnTheMove());
        }
        else{
            return (getSecondStepHome());
        }
    }

    private VerticalLayout getSecondStepHome() {
        VerticalLayout secondStepHome = new VerticalLayout();
        secondStepHome.setWidth("100%");

        HorizontalLayout horizontalLayout1 = new HorizontalLayout();
        HorizontalLayout horizontalLayout2 = new HorizontalLayout();
        HorizontalLayout horizontalLayout3 = new HorizontalLayout();
        HorizontalLayout horizontalLayout4 = new HorizontalLayout();

        //setUpTeam1
        horizontalLayout1.setWidth("100%");
        horizontalLayout1.setSpacing(true);
        horizontalLayout1.setClassName("selected");
        workOrderHeaderBinder.readBean(selectedWorkOrder.getWorkOrderHeaderList().get(0));

        Icon homeIcon1 = VaadinIcon.WORKPLACE.create();
        homeIcon1.addClickListener(e -> {
            selectedTeam = 1;
            workOrderHeaderBinder.readBean(selectedWorkOrder.getWorkOrderHeaderList().get(0));
            horizontalLayout1.setClassName("selected");
            horizontalLayout2.setClassName("");
            horizontalLayout3.setClassName("");
            horizontalLayout4.setClassName("");
        });

        cbExtraEmployees1.setWidth("100%");
        Optional<List<Employee>> employees1 = employeeService.getAll();
        if(employees1.isPresent()){
            cbMasterEmployee1.setPlaceholder("Team 1");
            cbMasterEmployee1.setItems(employees1.get());
            cbMasterEmployee1.setItemLabelGenerator(item -> item.getFirstName()+ " " + item.getLastName());
            cbExtraEmployees1.setItems(employees1.get());
            cbExtraEmployees1.setItemLabelGenerator(item -> item.getAbbreviation());
        }
        else{
            Notification.show("Geen techniekers gevonden");
        }
        horizontalLayout1.add(homeIcon1);
        horizontalLayout1.add(cbMasterEmployee1);
        horizontalLayout1.add(cbExtraEmployees1);


        //setUpTeam2

        horizontalLayout2.setWidth("100%");
        horizontalLayout2.setSpacing(true);

        Icon homeIcon2 = VaadinIcon.WORKPLACE.create();
        homeIcon2.addClickListener(e -> {
            workOrderHeaderBinder.readBean(selectedWorkOrder.getWorkOrderHeaderList().get(1));
            selectedTeam = 2;
            horizontalLayout1.setClassName("");
            horizontalLayout2.setClassName("selected");
            horizontalLayout3.setClassName("");
            horizontalLayout4.setClassName("");
        });

        cbExtraEmployees2.setWidth("100%");
        Optional<List<Employee>> employees2 = employeeService.getAll();
        if(employees2.isPresent()){
            cbMasterEmployee2.setPlaceholder("Team 2");
            cbMasterEmployee2.setItems(employees2.get());
            cbMasterEmployee2.setItemLabelGenerator(item -> item.getFirstName()+ " " + item.getLastName());
            cbExtraEmployees2.setItems(employees2.get());
            cbExtraEmployees2.setItemLabelGenerator(item -> item.getAbbreviation());
        }
        else{
            Notification.show("Geen techniekers gevonden");
        }
        horizontalLayout2.add(homeIcon2);
        horizontalLayout2.add(cbMasterEmployee2);
        horizontalLayout2.add(cbExtraEmployees2);


        //setUpTeam3
        horizontalLayout3.setWidth("100%");
        horizontalLayout3.setSpacing(true);

        Icon homeIcon3 = VaadinIcon.WORKPLACE.create();
        homeIcon3.addClickListener(e -> {
            workOrderHeaderBinder.readBean(selectedWorkOrder.getWorkOrderHeaderList().get(2));
            selectedTeam = 3;
            horizontalLayout1.setClassName("");
            horizontalLayout2.setClassName("");
            horizontalLayout3.setClassName("selected");
            horizontalLayout4.setClassName("");
        });

        cbExtraEmployees3.setWidth("100%");
        Optional<List<Employee>> employees3 = employeeService.getAll();
        if(employees3.isPresent()){
            cbMasterEmployee3.setPlaceholder("Team 3");
            cbMasterEmployee3.setItems(employees3.get());
            cbMasterEmployee3.setItemLabelGenerator(item -> item.getFirstName()+ " " + item.getLastName());
            cbExtraEmployees3.setItems(employees3.get());
            cbExtraEmployees3.setItemLabelGenerator(item -> item.getAbbreviation());
        }
        else{
            Notification.show("Geen techniekers gevonden");
        }
        horizontalLayout3.add(homeIcon3);
        horizontalLayout3.add(cbMasterEmployee3);
        horizontalLayout3.add(cbExtraEmployees3);

        //setUpTeam4
        horizontalLayout4.setWidth("100%");

        Icon homeIcon4 = VaadinIcon.WORKPLACE.create();
        homeIcon4.addClickListener(e -> {
            workOrderHeaderBinder.readBean(selectedWorkOrder.getWorkOrderHeaderList().get(3));
            selectedTeam = 4;
            horizontalLayout1.setClassName("");
            horizontalLayout2.setClassName("");
            horizontalLayout3.setClassName("");
            horizontalLayout4.setClassName("selected");
        });

        cbExtraEmployees4.setWidth("100%");
        Optional<List<Employee>> employees4 = employeeService.getAll();
        if(employees4.isPresent()){
            cbMasterEmployee4.setPlaceholder("Team 4");
            cbMasterEmployee4.setItems(employees4.get());
            cbMasterEmployee4.setItemLabelGenerator(item -> item.getFirstName()+ " " + item.getLastName());
            cbExtraEmployees4.setItems(employees4.get());
            cbExtraEmployees4.setItemLabelGenerator(item -> item.getAbbreviation());
        }
        else{
            Notification.show("Geen techniekers gevonden");
        }
        horizontalLayout4.add(homeIcon4);
        horizontalLayout4.add(cbMasterEmployee4);
        horizontalLayout4.add(cbExtraEmployees4);


        secondStepHome.add(horizontalLayout1);
        secondStepHome.add(horizontalLayout2);
        secondStepHome.add(horizontalLayout3);
        secondStepHome.add(horizontalLayout4);

        return secondStepHome;
    }

    private VerticalLayout getSecondStepOnTheMove() {
        VerticalLayout secondStepHome = new VerticalLayout();
        secondStepHome.setWidth("100%");

        HorizontalLayout horizontalLayout1 = new HorizontalLayout();
        horizontalLayout1.setClassName("selected");
        HorizontalLayout horizontalLayout2 = new HorizontalLayout();
        HorizontalLayout horizontalLayout3 = new HorizontalLayout();
        HorizontalLayout horizontalLayout4 = new HorizontalLayout();

        //setUpTeam1
        horizontalLayout1.setWidth("100%");

        Icon moveIcon1 = VaadinIcon.CAR.create();
        moveIcon1.addClickListener(e -> {
            workOrderHeaderBinder.readBean(selectedWorkOrder.getWorkOrderHeaderList().get(0));
            selectedTeam = 1;
            horizontalLayout1.setClassName("selected");
            horizontalLayout2.setClassName("");
            horizontalLayout3.setClassName("");
            horizontalLayout4.setClassName("");
        });

        cbExtraEmployees1.setWidth("100%");
        Optional<List<Employee>> employees1 = employeeService.getAll();
        if(employees1.isPresent()){
            cbMasterEmployee1.setPlaceholder("Team 1");
            cbMasterEmployee1.setItems(employees1.get());
            cbMasterEmployee1.setItemLabelGenerator(item -> item.getFirstName() + " " + item.getLastName());
            cbExtraEmployees1.setItems(employees1.get());
            cbExtraEmployees1.setItemLabelGenerator(item -> item.getAbbreviation());
        }
        else{
            Notification.show("Geen techniekers gevonden");
        }
        horizontalLayout1.add(moveIcon1);
        horizontalLayout1.add(cbMasterEmployee1);
        horizontalLayout1.add(cbExtraEmployees1);


        //setUpTeam2

        horizontalLayout2.setWidth("100%");

        Icon moveIcon2 = VaadinIcon.CAR.create();
        moveIcon2.addClickListener(e -> {
            workOrderHeaderBinder.readBean(selectedWorkOrder.getWorkOrderHeaderList().get(1));
            selectedTeam = 2;
            horizontalLayout1.setClassName("");
            horizontalLayout2.setClassName("selected");
            horizontalLayout3.setClassName("");
            horizontalLayout4.setClassName("");
        });

        cbExtraEmployees2.setWidth("100%");
        Optional<List<Employee>> employees2 = employeeService.getAll();
        if(employees2.isPresent()){
            cbMasterEmployee2.setPlaceholder("Team 2");
            cbMasterEmployee2.setItems(employees2.get());
            cbMasterEmployee2.setItemLabelGenerator(item -> item.getFirstName()+ " " + item.getLastName());
            cbExtraEmployees2.setItems(employees2.get());
            cbExtraEmployees2.setItemLabelGenerator(item -> item.getAbbreviation());
        }
        else{
            Notification.show("Geen techniekers gevonden");
        }
        horizontalLayout2.add(moveIcon2);
        horizontalLayout2.add(cbMasterEmployee2);
        horizontalLayout2.add(cbExtraEmployees2);


        //setUpTeam3

        horizontalLayout3.setWidth("100%");

        Icon moveIcon3 = VaadinIcon.CAR.create();
        moveIcon3.addClickListener(e -> {
            workOrderHeaderBinder.readBean(selectedWorkOrder.getWorkOrderHeaderList().get(2));
            selectedTeam = 3;
            horizontalLayout1.setClassName("");
            horizontalLayout2.setClassName("");
            horizontalLayout3.setClassName("selected");
            horizontalLayout4.setClassName("");
        });

        cbExtraEmployees3.setWidth("100%");
        Optional<List<Employee>> employees3 = employeeService.getAll();
        if(employees3.isPresent()){
            cbMasterEmployee3.setPlaceholder("Team 3");
            cbMasterEmployee3.setItems(employees3.get());
            cbMasterEmployee3.setItemLabelGenerator(item -> item.getFirstName()+ " " + item.getLastName());
            cbExtraEmployees3.setItems(employees3.get());
            cbExtraEmployees3.setItemLabelGenerator(item -> item.getAbbreviation());
        }
        else{
            Notification.show("Geen techniekers gevonden");
        }
        horizontalLayout3.add(moveIcon3);
        horizontalLayout3.add(cbMasterEmployee3);
        horizontalLayout3.add(cbExtraEmployees3);

        //setUpTeam4

        horizontalLayout4.setWidth("100%");

        Icon moveIcon4 = VaadinIcon.CAR.create();
        moveIcon4.addClickListener(e -> {
            workOrderHeaderBinder.readBean(selectedWorkOrder.getWorkOrderHeaderList().get(3));
            selectedTeam = 4;
            horizontalLayout1.setClassName("");
            horizontalLayout2.setClassName("");
            horizontalLayout3.setClassName("");
            horizontalLayout4.setClassName("selected");
        });

        cbExtraEmployees4.setWidth("100%");
        Optional<List<Employee>> employees4 = employeeService.getAll();
        if(employees4.isPresent()){
            cbMasterEmployee4.setPlaceholder("Team 4");
            cbMasterEmployee4.setItems(employees4.get());
            cbMasterEmployee4.setItemLabelGenerator(item -> item.getFirstName()+ " " + item.getLastName());
            cbExtraEmployees4.setItems(employees4.get());
            cbExtraEmployees4.setItemLabelGenerator(item -> item.getAbbreviation());
        }
        else{
            Notification.show("Geen techniekers gevonden");
        }
        horizontalLayout4.add(moveIcon4);
        horizontalLayout4.add(cbMasterEmployee4);
        horizontalLayout4.add(cbExtraEmployees4);


        secondStepHome.add(horizontalLayout1);
        secondStepHome.add(horizontalLayout2);
        secondStepHome.add(horizontalLayout3);
        secondStepHome.add(horizontalLayout4);

        return secondStepHome;
    }


    private VerticalLayout getFirstStepHeader() {
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.add(getMainButtons());
        verticalLayout.add(getAddressComboBox());
        verticalLayout.add(getDateTimePicker());
        verticalLayout.add(getLocationComboBox());
        verticalLayout.add(getTypeComboBox());
        return verticalLayout;
    }

    private HorizontalLayout getMainButtons() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidth("100%");
        horizontalLayout.setSpacing(true);

        Button existingButton = new Button("Zoek Werkbon");
        existingButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        existingButton.setWidth("100%");
        existingButton.setHeight("100px");
        existingButton.addClickListener(e -> {
            Notification.show("Open werkbon");
        });

        horizontalLayout.add(existingButton);

        return horizontalLayout;
    }

    private DateTimePicker getDateTimePicker() {
        dateTimePicker.setLocale(Locale.FRENCH);
        dateTimePicker.setValue(LocalDateTime.now());
        dateTimePicker.setWidthFull();
        return dateTimePicker;
    }

    private ComboBox getAddressComboBox() {
        Optional<List<Address>> allCustomerAddresses = customerService.getAllCustomerAdresses();
        if (allCustomerAddresses.isPresent()) {
            addressComboBox.setItems(allCustomerAddresses.get());
        }
        else{
            Notification.show("Geen Klanten in de database");
        }
        addressComboBox.setPlaceholder("Gelieve een klant/adres te selecteren");
        addressComboBox.setItemLabelGenerator(address -> address.getAddressName());
        addressComboBox.setWidthFull();
        return addressComboBox;
    }

    private ComponentRenderer<Component, Tools> toolCardRenderer = new ComponentRenderer<>(
            tool -> {
                HorizontalLayout cardLayout = new HorizontalLayout();
                cardLayout.setMargin(true);

                Avatar avatar = new Avatar(tool.getDiscription());
                avatar.setHeight("64px");
                avatar.setWidth("64px");
                //avatar.setAbbreviation(tool.getDiscription());

                VerticalLayout infoLayout = new VerticalLayout();
                infoLayout.add(new Text("Extras"));
                infoLayout.setSpacing(false);
                infoLayout.setPadding(false);
                infoLayout.getElement().appendChild(
                        ElementFactory.createStrong(tool.getDiscription()));
                //infoLayout.add(new Div(new Text(tool.getComment())));

//                VerticalLayout selectionLayout = new VerticalLayout();
//                selectionLayout.setSpacing(false);
//                selectionLayout.setPadding(false);
//                selectionLayout.add(new Div(new Text(tool.getComment1())));
//                selectionLayout.add(new Div(new TextField()));
//                if(tool.getComment2() != null){
//                    selectionLayout.add(new Div(new Text(tool.getComment2())));
//                    selectionLayout.add(new Div(new TextField()));
//                }
//                selectionLayout.add(new Div(new Button("Bewaar")));
//                infoLayout
//                        .add(new Details("Selectie", selectionLayout));

                cardLayout.add(avatar, infoLayout);
                return cardLayout;
            });




    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        WorkOrder newWorkOrder = new WorkOrder();
        newWorkOrder.setWorkDateTime(LocalDateTime.now());

        WorkOrderHeader newWorkOrderHeader1 = new WorkOrderHeader();
        WorkOrderHeader newWorkOrderHeader2 = new WorkOrderHeader();
        WorkOrderHeader newWorkOrderHeader3 = new WorkOrderHeader();
        WorkOrderHeader newWorkOrderHeader4 = new WorkOrderHeader();

        List<WorkOrderHeader>workOrderHeaders = new ArrayList<>();
        workOrderHeaders.add(newWorkOrderHeader1);
        workOrderHeaders.add(newWorkOrderHeader2);
        workOrderHeaders.add(newWorkOrderHeader3);
        workOrderHeaders.add(newWorkOrderHeader4);
        newWorkOrder.setWorkOrderHeaderList(workOrderHeaders);
        selectedWorkOrder = newWorkOrder;
        workOrderBinder.readBean(selectedWorkOrder);
        workOrderHeaderBinder.readBean(selectedWorkOrder.getWorkOrderHeaderList().get(0));
    }
}
