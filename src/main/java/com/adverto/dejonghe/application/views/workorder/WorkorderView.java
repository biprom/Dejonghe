package com.adverto.dejonghe.application.views.workorder;

import com.adverto.dejonghe.application.components.CustomTab;
import com.adverto.dejonghe.application.customEvents.AddProductEventListener;
import com.adverto.dejonghe.application.dbservices.CustomerService;
import com.adverto.dejonghe.application.dbservices.EmployeeService;
import com.adverto.dejonghe.application.dbservices.ProductService;
import com.adverto.dejonghe.application.dbservices.WorkOrderService;
import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrder;
import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrderHeader;
import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrderTime;
import com.adverto.dejonghe.application.entities.customers.Address;
import com.adverto.dejonghe.application.entities.employee.Employee;
import com.adverto.dejonghe.application.entities.enums.employee.UserFunction;
import com.adverto.dejonghe.application.entities.enums.fleet.Fleet;
import com.adverto.dejonghe.application.entities.enums.fleet.FleetTruckCraneOptions;
import com.adverto.dejonghe.application.entities.enums.fleet.FleetWorkType;
import com.adverto.dejonghe.application.entities.enums.product.VAT;
import com.adverto.dejonghe.application.entities.enums.workorder.Tools;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkLocation;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkOrderStatus;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkType;
import com.adverto.dejonghe.application.entities.product.product.Product;
import com.adverto.dejonghe.application.views.subViews.CurrentWorkOrdersSubView;
import com.adverto.dejonghe.application.views.subViews.SelectProductSubView;
import com.adverto.dejonghe.application.views.subViews.ShowImageSubVieuw;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.NumberField;
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
import com.vaadin.flow.router.Menu;
import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Werkbon")
@Route("werkbon")
@Menu(order = 0, icon = LineAwesomeIconUrl.WRENCH_SOLID)
public class WorkorderView extends Div implements HasUrlParameter<String> {

    EmployeeService employeeService;
    CustomerService customerService;
    ProductService productService;
    SelectProductSubView selectProductSubView;
    WorkOrderService workOrderService;
    GridFsTemplate gridFsTemplate;
    CurrentWorkOrdersSubView currtentWorkOrdersSubVieuw;
    ShowImageSubVieuw showImageSubVieuw;
    AddProductEventListener addProductEventListener;
    ApplicationEventPublisher eventPublisher;

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
    List<WorkOrderTime>selectedWorkOrderTimes;
    WorkOrderTime selectedWorkOrderTime;

    ComboBox<Fleet> fleetComboBox = new ComboBox<>();
    ComboBox<FleetTruckCraneOptions> fleetOptions = new ComboBox<>();
    TextField tfRoadTax = new TextField();
    TextField tfTunnelTax = new TextField();
    ComboBox<FleetWorkType> fleetWorkTypeComboBox = new ComboBox<>();

    MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    Upload dropEnabledUpload = new Upload(buffer);
    Button showImageButton;

    Button finishButton = new Button("Stuur door facturatie");

    VerticalLayout vLayoutHeaderLevel2Tabs;
    VerticalLayout vLayoutHeaderLevel3Tabs;
    VerticalLayout vLayoutHeaderLevel4Tabs;
    VerticalLayout vLayoutHeaderLevel5Tabs;
    VerticalLayout vLayoutHeaderLevel6Tabs;
    VerticalLayout vLayoutHeaderLevel7Tabs;
    VerticalLayout vLayoutHeaderLevel8Tabs;

    Integer selectedTeam = 1;

    Binder<WorkOrder>workOrderBinder;
    Binder<WorkOrderHeader>workOrderHeaderBinder;

    WorkOrder selectedWorkOrder;
    List<WorkOrder> selectedCoupledWorkOrders;
    Dialog finishWorkOrderDialog;
    Dialog searchCurrentWorkOrderDialog;
    Dialog removeWorkOrderHourDialog;
    Dialog imageDialog;

    Tabs buddyTab;
    Button addTabButton;

    String linkParameter;

    public WorkorderView(ProductService productService,
                         CustomerService customerService,
                         EmployeeService employeeService,
                         SelectProductSubView selectProductSubView,
                         WorkOrderService workOrderService,
                         GridFsTemplate gridFsTemplate,
                         CurrentWorkOrdersSubView currtentWorkOrdersSubVieuw,
                         ShowImageSubVieuw showImageSubVieuw,
                         AddProductEventListener listener,
                         ApplicationEventPublisher eventPublisher) {
        this.productService = productService;
        this.selectProductSubView = selectProductSubView;
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.workOrderService = workOrderService;
        this.gridFsTemplate = gridFsTemplate;
        this.currtentWorkOrdersSubVieuw = currtentWorkOrdersSubVieuw;
        this.showImageSubVieuw = showImageSubVieuw;
        this.addProductEventListener = listener;
        this.eventPublisher = eventPublisher;

        selectProductSubView.setUserFunction(UserFunction.TECHNICIAN);
        setUpImageDialog();
        setUpCurrentWorkOrderDialog();
        setUpFinishWorkOrderDialog();
        setUpRemoveWorkOrderHour();
        setUpFinishButton();
        setUpUpload();
        setUpShowImageButton();
        setUpSplitLayouts();
        getWorkOrderHeader();
        formLayout.add(vLayoutHeaderLevel2Tabs);
        formLayout.add(vLayoutHeaderLevel3Tabs,2);
        formLayout.add(vLayoutHeaderLevel4Tabs,2);
        formLayout.add(vLayoutHeaderLevel5Tabs,2);
        formLayout.add(vLayoutHeaderLevel6Tabs,2);
        formLayout.add(vLayoutHeaderLevel7Tabs,2);
        formLayout.add(vLayoutHeaderLevel8Tabs,2);
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidth("100%");
        verticalLayout.add(getMainButtons(),formLayout);
        verticalLayout.addClassName("blueborder");
        headerSplitLayout.addToPrimary(verticalLayout);
        VerticalLayout vLayout = new VerticalLayout();
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


    private void setUpImageDialog() {
        imageDialog = new Dialog();
        imageDialog.setHeaderTitle("Toegevoegde foto's onder geselecteerde werkbon");
        imageDialog.add(showImageSubVieuw);
        Button cancelButton = new Button("Sluiten", e -> {
            try {
                workOrderHeaderBinder.writeBean(selectedWorkOrder.getWorkOrderHeaderList().get(selectedTeam-1));
            } catch (ValidationException ex) {
                throw new RuntimeException(ex);
            }
            try {
                workOrderBinder.writeBean(selectedWorkOrder);
            } catch (ValidationException ex) {
                throw new RuntimeException(ex);
            }
            workOrderService.save(selectedWorkOrder);
            imageDialog.close();
        });
        imageDialog.getFooter().add(cancelButton);
    }

    private void setUpTimeGrid() {
        workOrderTimeGrid = new Grid<>();
        workOrderTimeGrid.setHeight("250px");
        workOrderTimeGrid.setAllRowsVisible(true);
        workOrderTimeGrid.addComponentColumn(item -> {
            TimePicker timeUpPicker = new TimePicker();
            timeUpPicker.setValue(item.getTimeUp());
            timeUpPicker.setLocale(Locale.FRENCH);
            timeUpPicker.setStep(Duration.ofMinutes(15));
            timeUpPicker.setWidth("100%");
            timeUpPicker.addValueChangeListener(event -> {
                try {
                    item.setTimeUp(timeUpPicker.getValue());
                    selectedWorkOrder.getWorkOrderHeaderList().get(selectedTeam-1).setWorkOrderTimeList(selectedWorkOrderTimes);
                    saveSelectedWorkOrder();
                } catch (ValidationException e) {
                    throw new RuntimeException(e);
                }
                try {
                    workOrderBinder.writeBean(selectedWorkOrder);
                } catch (ValidationException e) {
                    throw new RuntimeException(e);
                }
            });
            return timeUpPicker;
        }).setHeader("Vertrek");

        workOrderTimeGrid.addComponentColumn(item -> {
            TimePicker timeStartPicker = new TimePicker();
            timeStartPicker.setValue(item.getTimeStart());
            timeStartPicker.setLocale(Locale.FRENCH);
            timeStartPicker.setStep(Duration.ofMinutes(15));
            timeStartPicker.setWidth("100%");
            timeStartPicker.addValueChangeListener(event -> {
                try {
                    item.setTimeStart(timeStartPicker.getValue());
                    selectedWorkOrder.getWorkOrderHeaderList().get(selectedTeam-1).setWorkOrderTimeList(selectedWorkOrderTimes);
                    saveSelectedWorkOrder();
                } catch (ValidationException e) {
                    throw new RuntimeException(e);
                }
                try {
                    workOrderBinder.writeBean(selectedWorkOrder);
                } catch (ValidationException e) {
                    throw new RuntimeException(e);
                }
            });
            return timeStartPicker;
        }).setHeader("Aankomst Klant");

        workOrderTimeGrid.addComponentColumn(item -> {
            ComboBox<Integer> cbPauze = new ComboBox();
            cbPauze.setItems(0,15,30,45,60);
            cbPauze.setWidth("100%");
            cbPauze.setValue(item.getPauze());
            cbPauze.addValueChangeListener(event -> {
                try {
                    item.setPauze(event.getValue());
                    selectedWorkOrder.getWorkOrderHeaderList().get(selectedTeam-1).setWorkOrderTimeList(selectedWorkOrderTimes);
                    saveSelectedWorkOrder();
                } catch (ValidationException e) {
                    throw new RuntimeException(e);
                }
                try {
                    workOrderBinder.writeBean(selectedWorkOrder);
                } catch (ValidationException e) {
                    throw new RuntimeException(e);
                }
            });
            return cbPauze;
        }).setHeader("Pauze");

        workOrderTimeGrid.addComponentColumn(item -> {
            TimePicker timeStopPicker = new TimePicker();
            timeStopPicker.setValue(item.getTimeStop());
            timeStopPicker.setLocale(Locale.FRENCH);
            timeStopPicker.setStep(Duration.ofMinutes(15));
            timeStopPicker.setWidth("100%");
            timeStopPicker.addValueChangeListener(event -> {
                try {
                    item.setTimeStop(timeStopPicker.getValue());
                    selectedWorkOrder.getWorkOrderHeaderList().get(selectedTeam-1).setWorkOrderTimeList(selectedWorkOrderTimes);
                    saveSelectedWorkOrder();
                } catch (ValidationException e) {
                    throw new RuntimeException(e);
                }
                try {
                    workOrderBinder.writeBean(selectedWorkOrder);
                } catch (ValidationException e) {
                    throw new RuntimeException(e);
                }
            });
            return timeStopPicker;
        }).setHeader("Vertrek Klant");

        workOrderTimeGrid.addComponentColumn(item -> {
            TimePicker timeDownPicker = new TimePicker();
            timeDownPicker.setValue(item.getTimeDown());
            timeDownPicker.setLocale(Locale.FRENCH);
            timeDownPicker.setStep(Duration.ofMinutes(15));
            timeDownPicker.setWidth("100%");
            timeDownPicker.addValueChangeListener(event -> {
                try {
                    item.setTimeDown(timeDownPicker.getValue());
                    selectedWorkOrder.getWorkOrderHeaderList().get(selectedTeam-1).setWorkOrderTimeList(selectedWorkOrderTimes);
                    saveSelectedWorkOrder();
                } catch (ValidationException e) {
                    throw new RuntimeException(e);
                }
                try {
                    workOrderBinder.writeBean(selectedWorkOrder);
                } catch (ValidationException e) {
                    throw new RuntimeException(e);
                }
            });
            return timeDownPicker;
        }).setHeader("Terug");

        workOrderTimeGrid.addComponentColumn(item -> {
            Button plusButton = new Button(new Icon(VaadinIcon.PLUS));
            plusButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            plusButton.setAriaLabel("Voeg een regel toe");
            plusButton.addClickListener(event -> {
                selectedWorkOrderTimes.add(new WorkOrderTime());
                workOrderTimeGrid.getDataProvider().refreshAll();
                Notification.show("Een regel is toegevoegd!");
            });
            return plusButton;
        }).setWidth("20px");

        workOrderTimeGrid.addComponentColumn(item -> {
            Button plusButton = new Button(new Icon(VaadinIcon.MINUS));
            plusButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            plusButton.setAriaLabel("Voeg een regel toe");
            plusButton.addClickListener(event -> {
                selectedWorkOrderTime = item;
                removeWorkOrderHourDialog.open();
                Notification.show("Een regel is toegevoegd!");
            });
            return plusButton;
        }).setWidth("20px");
    }

    private void setUpCurrentWorkOrderDialog() {
        searchCurrentWorkOrderDialog = new Dialog();
        Button closeButton = new Button("Selecteer lopende werkbon.",
                (e) -> {
                List<WorkOrder>selectedBundledWorkOrders;
            try{
                selectedBundledWorkOrders = currtentWorkOrdersSubVieuw.getSelectedBundeledWorkOrders();
            }
           catch (Exception ex){
               selectedBundledWorkOrders = null;
               searchCurrentWorkOrderDialog.close();
           }
            if((selectedBundledWorkOrders !=  null) && (selectedBundledWorkOrders.size() > 0)){
                selectedCoupledWorkOrders = selectedBundledWorkOrders;

                buddyTab.removeAll();
                //add Parent Tab
                CustomTab parentTab = new CustomTab();
                WorkOrder partentWorkOrder = selectedCoupledWorkOrders.stream().filter(item -> item.getStarter()).findFirst().get();
                parentTab.setLabel(partentWorkOrder.getWorkDateTime().format(DateTimeFormatter.ofPattern("dd/MM")));
                parentTab.setWorkOrderId(partentWorkOrder.getId());
                buddyTab.add(parentTab);
                buddyTab.setSelectedTab(parentTab);
                setSelectedWorkOrder(partentWorkOrder);

                //Try to add ChildrenTab
                selectedCoupledWorkOrders.stream().filter(item -> item.getStarter() == false).forEach(item -> {
                    CustomTab childTab = new CustomTab();
                    childTab.setLabel(item.getWorkDateTime().format(DateTimeFormatter.ofPattern("dd/MM")));
                    childTab.setWorkOrderId(item.getId());
                    buddyTab.add(childTab);
                });

                buddyTab.addSelectedChangeListener(event -> {
                    CustomTab selectedCustomTab = (CustomTab)buddyTab.getSelectedTab();
                    setSelectedWorkOrder(workOrderService.getWorkOrderById(selectedCustomTab.getWorkOrderId()).get());
                });

                searchCurrentWorkOrderDialog.close();
            }
            else{
                searchCurrentWorkOrderDialog.close();
            }
                });
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        searchCurrentWorkOrderDialog.getHeader().add(closeButton);
        searchCurrentWorkOrderDialog.setCloseOnEsc(true);
        searchCurrentWorkOrderDialog.setCloseOnOutsideClick(true);
        searchCurrentWorkOrderDialog.setWidth("50%");
        searchCurrentWorkOrderDialog.setHeight("50%");
        searchCurrentWorkOrderDialog.setHeaderTitle("Lopende werkbonnen");
        searchCurrentWorkOrderDialog.add(currtentWorkOrdersSubVieuw);
    }

    private void setUpRemoveWorkOrderHour() {
        removeWorkOrderHourDialog = new Dialog();
        removeWorkOrderHourDialog.setHeaderTitle("Ben je zeker dat je de geselecteerde tijd wil verwijderen?");

        VerticalLayout dialogLayout = createDialogLayoutRemoveHour();
        removeWorkOrderHourDialog.add(dialogLayout);

        Button saveButton = createRemoveButton(removeWorkOrderHourDialog);
        Button cancelButton = new Button("Niet Verwijderen", e -> removeWorkOrderHourDialog.close());
        removeWorkOrderHourDialog.getFooter().add(cancelButton);
        removeWorkOrderHourDialog.getFooter().add(saveButton);
    }

    private Button createRemoveButton(Dialog removeWorkOrderHourDialog) {

        Button removeButton = new Button("Verwijderen");
        removeButton.addClickListener(click -> {
            selectedWorkOrderTimes.remove(selectedWorkOrderTime);
            workOrderTimeGrid.getDataProvider().refreshAll();
            Notification.show("Een verwijderen!");
            removeWorkOrderHourDialog.close();
        });
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        return removeButton;

    }

    private static VerticalLayout createDialogLayoutRemoveHour() {

        Span span = new Span("Deze tijd wordt definitief verwijderd!");
        VerticalLayout dialogLayout = new VerticalLayout(span);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");

        return dialogLayout;
    }


    private void setUpFinishWorkOrderDialog() {
        finishWorkOrderDialog = new Dialog();
        finishWorkOrderDialog.setHeaderTitle("Ben je zeker dat je deze werkbon wil afsluiten?");

        VerticalLayout dialogLayout = createDialogLayout();
        finishWorkOrderDialog.add(dialogLayout);

        Button saveButton = createSaveButton(finishWorkOrderDialog);
        Button cancelButton = new Button("Niet Afwerken", e -> finishWorkOrderDialog.close());
        finishWorkOrderDialog.getFooter().add(cancelButton);
        finishWorkOrderDialog.getFooter().add(saveButton);
    }

    private static VerticalLayout createDialogLayout() {

        Span span = new Span("Door deze werkbon af te werken is het niet meer mogelijk wijzigingen door te voeren.");
        VerticalLayout dialogLayout = new VerticalLayout(span);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");

        return dialogLayout;
    }

    private Button createSaveButton(Dialog dialog) {
        Button saveButton = new Button("Afwerken");
        saveButton.addClickListener(click -> {
            dialog.close();
            try {
                workOrderHeaderBinder.writeBean(selectedWorkOrder.getWorkOrderHeaderList().get(selectedTeam-1));
                workOrderBinder.writeBean(selectedWorkOrder);
                selectedWorkOrder.setWorkOrderStatus(WorkOrderStatus.FINISHED);
                saveSelectedWorkOrder();
                Notification.show("Deze werkbon is afgewerkt");
            } catch (ValidationException e) {
                Notification.show("Deze werkbon kon niet worden bewaard.");
            }
            readNewWorkOrder();
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return saveButton;
    }

    private void setUpFinishButton() {
        finishButton.setWidth("100%");
        finishButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_WARNING);
        finishButton.addClickListener(e -> {
            finishWorkOrderDialog.open();
        });
    }

    private void setUpShowImageButton() {
        showImageButton = new Button("Er zijn in deze werkbon geen foto's toegevoegd, gelieve altijd een foto te koppelen van de werken!");
        showImageButton.setWidth("100%");

        showImageButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        showImageButton.addClickListener(e -> {
            if((selectedWorkOrder.getImageList() != null) && (selectedWorkOrder.getImageList().size() > 0)) {
                showImageSubVieuw.setSelectedWorkOrder(selectedWorkOrder.getImageList());
            }
            else{
                Notification notification = Notification.show("Deze werkbon bevat nog geen foto's!");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });
    }

    private void setUpUpload() {
        dropEnabledUpload.setWidthFull();
        dropEnabledUpload.setAcceptedFileTypes("image/tiff", ".jpeg");
        dropEnabledUpload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();

            Notification notification = Notification.show(errorMessage, 5000,
                    Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        dropEnabledUpload.addFailedListener(event -> {
            Notification.show("Deze foto kon niet worden verstruurd naar de server : " + event.getReason());
        });
        dropEnabledUpload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            InputStream inputStream = buffer.getInputStream(fileName);
            DBObject metaData = new BasicDBObject();
            metaData.put("timeOfUpload", LocalDateTime.now().toString());
            try {
                storeImageIdToThisWorkOrder(gridFsTemplate.store(inputStream, fileName, "image/png", metaData).toString());
                updateGetImageButton();
            } catch (ValidationException e) {
                Notification notification = Notification.show("De toegevoegde foto kon niet worden bewaard!");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                Notification.show("Inputstream van deze foto kon niet worden afgesloten!");
            }
        });
    }

    private void updateGetImageButton() {
        if(selectedWorkOrder.getImageList() != null) {
            showImageButton.setText("Deze werkbon bevat " + selectedWorkOrder.getImageList().size() + " foto(s), klik hier om ze te bekijken.");
        }
        else{
            showImageButton.setText("Deze werkbon bevat bevat geen foto's.");
        }
        showImageButton.removeThemeVariants(ButtonVariant.LUMO_ERROR);
        showImageButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        showImageButton.addClickListener(e -> {imageDialog.open();});
    }

    private void storeImageIdToThisWorkOrder(String idString) throws ValidationException {
        if(selectedWorkOrder.getImageList() != null) {
            selectedWorkOrder.getImageList().add(idString);
            saveSelectedWorkOrder();
        }
        else{
            List<String>imageIdList = new ArrayList<>();
            imageIdList.add(idString);
            selectedWorkOrder.setImageList(imageIdList);
            saveSelectedWorkOrder();
        }
    }

    private void setUpWorkOrderHeaderBinder() {
        workOrderHeaderBinder = new Binder<>(WorkOrderHeader.class);
        workOrderHeaderBinder.forField(taDiscription)
                .bind(WorkOrderHeader::getDiscription, WorkOrderHeader::setDiscription);
        workOrderHeaderBinder.forField(fleetComboBox)
                .bind(WorkOrderHeader::getFleet, WorkOrderHeader::setFleet);
        workOrderHeaderBinder.forField(fleetOptions)
                .bind(WorkOrderHeader::getFleetOptions, WorkOrderHeader::setFleetOptions);
        workOrderHeaderBinder.forField(tfRoadTax)
                .withNullRepresentation("")
                .withConverter(new StringToDoubleConverter("Dit is geen decimaal getal!"))
                .bind(WorkOrderHeader::getRoadTax, WorkOrderHeader::setRoadTax);
        workOrderHeaderBinder.forField(tfTunnelTax)
                .withNullRepresentation("")
                .withConverter(new StringToDoubleConverter("Dit is geen decimaal getal!"))
                .bind(WorkOrderHeader::getTunnelTax, WorkOrderHeader::setTunnelTax);
        workOrderHeaderBinder.forField(fleetWorkTypeComboBox)
                .bind(WorkOrderHeader::getFleetWorkType, WorkOrderHeader::setFleetWorkType);
        workOrderHeaderBinder.addValueChangeListener(workOrderHeader -> {
            try {
                saveSelectedWorkOrder();
            } catch (ValidationException e) {
                Notification.show("Kon de werkbon nog niet bewaren");
            }
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
        workOrderBinder.forField(typeComboBox)
                .asRequired("Gelieve een werktype te selecteren!")
                .bind(WorkOrder::getWorkType, WorkOrder::setWorkType);
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
                saveSelectedWorkOrder();
            } catch (ValidationException e) {
                Notification.show("Kon de werkbon nog niet bewaren");
            }
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
        vLayoutHeaderLevel6Tabs.setHeightFull();
        vLayoutHeaderLevel6Tabs.setVisible(true);

        vLayoutHeaderLevel7Tabs = new VerticalLayout();
        vLayoutHeaderLevel7Tabs.setSizeFull();
        vLayoutHeaderLevel7Tabs.setVisible(true);

        vLayoutHeaderLevel8Tabs = new VerticalLayout();
        vLayoutHeaderLevel8Tabs.setSizeFull();
        vLayoutHeaderLevel8Tabs.setVisible(true);

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
                    vLayoutHeaderLevel8Tabs.removeAll();
                    vLayoutHeaderLevel2Tabs.add(getLevel2(WorkLocation.ON_THE_MOVE));
                    vLayoutHeaderLevel3Tabs.add(getLevel5(WorkLocation.ON_THE_MOVE));
                    vLayoutHeaderLevel4Tabs.add(getLevel3(WorkLocation.ON_THE_MOVE));
                    vLayoutHeaderLevel5Tabs.add(getLevel4(WorkLocation.ON_THE_MOVE));
                    vLayoutHeaderLevel6Tabs.add(getLevel6(WorkLocation.ON_THE_MOVE));
                    vLayoutHeaderLevel7Tabs.add(dropEnabledUpload,showImageButton);
                    vLayoutHeaderLevel8Tabs.add(finishButton);
                    
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
                    vLayoutHeaderLevel7Tabs.removeAll();
                    vLayoutHeaderLevel8Tabs.removeAll();
                    vLayoutHeaderLevel2Tabs.add(getLevel2(WorkLocation.WORKPLACE));
                    vLayoutHeaderLevel3Tabs.add(getLevel5(WorkLocation.WORKPLACE));
                    vLayoutHeaderLevel4Tabs.add(getLevel3(WorkLocation.WORKPLACE));
                    vLayoutHeaderLevel6Tabs.add(getLevel6(WorkLocation.WORKPLACE));
                    vLayoutHeaderLevel7Tabs.add(dropEnabledUpload);
                    vLayoutHeaderLevel8Tabs.add(finishButton);
                }
            }
        });
        return locationComboBox;
    }

    private VerticalLayout getLevel6(WorkLocation workLocation) {
        VerticalLayout vLayout = new VerticalLayout();

        HorizontalLayout horizontalLayoutLevel6 = new HorizontalLayout();
        horizontalLayoutLevel6.setSizeFull();
        horizontalLayoutLevel6.setVisible(false);
        Button hideButton = new Button("Selectie van opties die op deze werkbon zijn gebruikt : klik om deze te zien, dubbelklik om te verbergen");
        hideButton.setWidth("100%");
        hideButton.addClickListener(event -> {
            horizontalLayoutLevel6.setVisible(true);
        });
        hideButton.addDoubleClickListener(event -> {
            horizontalLayoutLevel6.setVisible(false);
        });
        vLayout.add(hideButton,horizontalLayoutLevel6);

        if(workLocation == WorkLocation.ON_THE_MOVE){
            addAvatarToVirtualList(horizontalLayoutLevel6, Arrays.stream(Tools.values()).filter(filter -> filter.getWorkLocation().equals(WorkLocation.ON_THE_MOVE)).filter(filter -> filter.getGroup().equals(1)).collect(Collectors.toSet()));
            addAvatarToVirtualList(horizontalLayoutLevel6, Arrays.stream(Tools.values()).filter(filter -> filter.getWorkLocation().equals(WorkLocation.ON_THE_MOVE)).filter(filter -> filter.getGroup().equals(2)).collect(Collectors.toSet()));
            addAvatarToVirtualList(horizontalLayoutLevel6, Arrays.stream(Tools.values()).filter(filter -> filter.getWorkLocation().equals(WorkLocation.ON_THE_MOVE)).filter(filter -> filter.getGroup().equals(3)).collect(Collectors.toSet()));
            addAvatarToVirtualList(horizontalLayoutLevel6, Arrays.stream(Tools.values()).filter(filter -> filter.getWorkLocation().equals(WorkLocation.ON_THE_MOVE)).filter(filter -> filter.getGroup().equals(4)).collect(Collectors.toSet()));
            vLayout.add(hideButton,horizontalLayoutLevel6);
            return vLayout;
        }
        else{
            addAvatarToVirtualList(horizontalLayoutLevel6, Arrays.stream(Tools.values()).filter(filter -> filter.getWorkLocation().equals(WorkLocation.WORKPLACE)).filter(filter -> filter.getGroup().equals(1)).collect(Collectors.toSet()));
            addAvatarToVirtualList(horizontalLayoutLevel6, Arrays.stream(Tools.values()).filter(filter -> filter.getWorkLocation().equals(WorkLocation.WORKPLACE)).filter(filter -> filter.getGroup().equals(2)).collect(Collectors.toSet()));
            addAvatarToVirtualList(horizontalLayoutLevel6, Arrays.stream(Tools.values()).filter(filter -> filter.getWorkLocation().equals(WorkLocation.WORKPLACE)).filter(filter -> filter.getGroup().equals(3)).collect(Collectors.toSet()));

            vLayout.add(hideButton,horizontalLayoutLevel6);
            return vLayout;
        }

    }

    private void addAvatarToVirtualList(HorizontalLayout horizontalLayoutLevel6, Set<Tools> toolsToAdd) {
        VirtualList<Tools> toolsVirtualList = new VirtualList<>();
        visualizeSelectedOptions(toolsToAdd);
        toolsVirtualList.setItems(toolsToAdd);
        toolsVirtualList.setRenderer(toolCardRenderer);
        //visualizeSelectedOptions();
        horizontalLayoutLevel6.add(toolsVirtualList);
    }

    private void visualizeSelectedOptions(Set<Tools> toolsToAdd){
        for(Tools tool : toolsToAdd){
            Set<Product> collect = selectedWorkOrder.getProductList().stream().filter(item -> {
                if(item.getProductCode() != null){
                    return item.getProductCode()
                            .replace("1", "")
                            .replace("2", "")
                            .matches(tool.getAbbreviation());
                }
                else{
                    return false;
                }
            }).collect(Collectors.toSet());
            if(collect.size() > 0){
                tool.setbSelected(true);
            }
            else{
                tool.setbSelected(false);
            }
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

    private HorizontalLayout getLevel4(WorkLocation workLocation) {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidth("100%");
        if(workLocation == WorkLocation.ON_THE_MOVE){

            VerticalLayout vLayout1 = new VerticalLayout();
            vLayout1.setWidth("100%");
            vLayout1.setSpacing(true);

            VerticalLayout vLayout2 = new VerticalLayout();
            vLayout1.setWidth("100%");
            vLayout1.setSpacing(true);

            fleetComboBox.setItems(Fleet.values());
            fleetComboBox.setItemLabelGenerator(Fleet::getDiscription);
            fleetComboBox.setWidth("100%");
            fleetComboBox.setPlaceholder("Voertuig");
            fleetComboBox.addValueChangeListener(selectedValue -> {
                if(selectedValue.isFromClient()){
                    if (selectedValue.getValue().equals(Fleet.TRUCK_CRANE)) {
                        fleetOptions.setEnabled(true);
                        fleetOptions.setItems(FleetTruckCraneOptions.values());
                    }
                    else{
                        fleetOptions.clear();
                        fleetOptions.setEnabled(false);
                    }
                }
            });
            fleetOptions.setItemLabelGenerator(FleetTruckCraneOptions::getOptions);
            fleetOptions.setWidth("100%");
            fleetOptions.setPlaceholder("Voertuig opties");

            vLayout1.add(fleetComboBox, tfRoadTax);
            vLayout2.add(fleetOptions, tfTunnelTax,fleetWorkTypeComboBox);

            tfRoadTax.setWidth("100%");
            tfRoadTax.setPlaceholder("Wegentax");

            tfTunnelTax.setWidth("100%");
            tfTunnelTax.setPlaceholder("Tunneltax");

            fleetWorkTypeComboBox.setItems(FleetWorkType.values());
            fleetWorkTypeComboBox.setItemLabelGenerator(FleetWorkType::getDiscription);
            fleetWorkTypeComboBox.setWidth("100%");
            fleetWorkTypeComboBox.setPlaceholder("Type werk kraan");

            horizontalLayout.add(vLayout1, vLayout2);
        }
        return horizontalLayout;
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
        addItemsToWorkTimeGrid(selectedWorkOrder.getWorkOrderHeaderList().get(0).getWorkOrderTimeList());

        Icon homeIcon1 = VaadinIcon.WORKPLACE.create();
        homeIcon1.addClickListener(e -> {
            selectedTeam = 1;
            workOrderHeaderBinder.readBean(selectedWorkOrder.getWorkOrderHeaderList().get(0));
            addItemsToWorkTimeGrid(selectedWorkOrder.getWorkOrderHeaderList().get(0).getWorkOrderTimeList());
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
            addItemsToWorkTimeGrid(selectedWorkOrder.getWorkOrderHeaderList().get(1).getWorkOrderTimeList());
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
            addItemsToWorkTimeGrid(selectedWorkOrder.getWorkOrderHeaderList().get(2).getWorkOrderTimeList());
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
            addItemsToWorkTimeGrid(selectedWorkOrder.getWorkOrderHeaderList().get(3).getWorkOrderTimeList());
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

    private void addItemsToWorkTimeGrid(List<WorkOrderTime> workOrderTimeList) {
        if((workOrderTimeList != null) && (workOrderTimeList.size() > 0)){
            selectedWorkOrderTimes = workOrderTimeList;
            workOrderTimeGrid.setItems(selectedWorkOrderTimes);
        }
        else{
            List<WorkOrderTime> emptyList = new ArrayList<>();
            emptyList.add(new WorkOrderTime());
            selectedWorkOrderTimes = emptyList;
            workOrderTimeGrid.setItems(selectedWorkOrderTimes);
        }
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
            addItemsToWorkTimeGrid(selectedWorkOrder.getWorkOrderHeaderList().get(0).getWorkOrderTimeList());
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
            addItemsToWorkTimeGrid(selectedWorkOrder.getWorkOrderHeaderList().get(1).getWorkOrderTimeList());
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
            addItemsToWorkTimeGrid(selectedWorkOrder.getWorkOrderHeaderList().get(2).getWorkOrderTimeList());
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
            addItemsToWorkTimeGrid(selectedWorkOrder.getWorkOrderHeaderList().get(3).getWorkOrderTimeList());
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

        Button addProductScaleButton = new Button("Schaal 7/3");
        Button seeWorkOrderScaleButton = new Button("Schaal 3/7");

        addProductScaleButton.addClickListener(e -> {
            mainSplitLayout.setSplitterPosition(70);
        });
        seeWorkOrderScaleButton.addClickListener(e -> {
            mainSplitLayout.setSplitterPosition(30);
        });

        Button searchRunningWorkorders = new Button("Zoek Werkbon");
        searchRunningWorkorders.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchRunningWorkorders.addClickListener(e -> {
            Optional<List<WorkOrder>> allPendingStarters = workOrderService.getAllByStatusAndStarter(WorkOrderStatus.RUNNING, true);
            if(allPendingStarters.isPresent()){
                currtentWorkOrdersSubVieuw.addItemsToPendingWorkOrderGrid(allPendingStarters.get());
                currtentWorkOrdersSubVieuw.setAuthorisation(UserFunction.TECHNICIAN);
                searchCurrentWorkOrderDialog.open();
            }
            else{
                Notification notification = Notification.show("Geen lopenede werkbonnen gevonden");
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });

        buddyTab = new Tabs(new Tab(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM"))));

        addTabButton = new Button(VaadinIcon.PLUS.create());
        addTabButton.addClickListener(e -> {
            try {
                createNewLinkedWorkOrder(selectedWorkOrder);
            } catch (ValidationException ex) {
                throw new RuntimeException(ex);
            }
        });

        horizontalLayout.add(addTabButton);
        horizontalLayout.add(buddyTab);
        horizontalLayout.add(searchRunningWorkorders,addProductScaleButton,seeWorkOrderScaleButton);
        horizontalLayout.setAlignSelf(FlexComponent.Alignment.END, searchRunningWorkorders);

        return horizontalLayout;
    }

    private DateTimePicker getDateTimePicker() {
        dateTimePicker.setLocale(Locale.FRENCH);
        dateTimePicker.setValue(LocalDateTime.now());
        dateTimePicker.setWidthFull();
        dateTimePicker.addValueChangeListener(event -> {
            buddyTab.getSelectedTab().setLabel(event.getValue().format(DateTimeFormatter.ofPattern("dd/MM")));
        });
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
                avatar.setHeight("32px");
                avatar.setWidth("32px");
                avatar.setAbbreviation(" ");
                if(tool.getBSelected()){
                    avatar.setColorIndex(5);
                }
                VerticalLayout infoLayout = new VerticalLayout();
                infoLayout.setSpacing(false);
                infoLayout.setPadding(false);
                infoLayout.getElement().appendChild(
                        ElementFactory.createStrong(tool.getDiscription()));
                //infoLayout.add(new Div(new Text(tool.getComment())));

                VerticalLayout selectionLayout = new VerticalLayout();
                selectionLayout.setSpacing(false);
                selectionLayout.setPadding(false);
                selectionLayout.add(new Div(new Text(tool.getComment1())));
                NumberField nfComment1 = new NumberField();
                NumberField nfComment2 = new NumberField();
                selectionLayout.add(new Div(nfComment1));
                if(tool.getComment2() != null){
                    selectionLayout.add(new Div(new Text(tool.getComment2())));
                    selectionLayout.add(new Div(nfComment2));
                }
                Button saveButton = new Button("Bewaar");
                saveButton.addClickListener(e -> {
                    //publish event so the selectedProductSubView can add the product...
                    if(tool.getComment2() != null){
                        //Tool with two items
                        Product newProduct1 = new Product();
                        newProduct1.setOption(true);
                        newProduct1.setDate(LocalDate.now());
                        newProduct1.setProductCode(tool.getAbbreviation()+"1");
                        newProduct1.setId(tool.getAbbreviation()+"1");
                        newProduct1.setInternalName(tool.getDiscription() + " " + tool.getComment1().replace(':' ,' '));
                        newProduct1.setComment(tool.getDiscription() + " " + tool.getComment1().replace(':' ,' '));
                        newProduct1.setSelectedAmount(nfComment1.getValue());
                        selectProductSubView.addProductToSelectedProductsFromRemoteView(newProduct1);
                        //eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",newProduct1));

                        Product newProduct2 = new Product();
                        newProduct2.setOption(true);
                        newProduct2.setDate(LocalDate.now());
                        newProduct2.setProductCode(tool.getAbbreviation()+"2");
                        newProduct2.setId(tool.getAbbreviation()+"2");
                        newProduct2.setInternalName(tool.getDiscription() + " " + tool.getComment2().replace(':' ,' '));
                        newProduct2.setComment(tool.getDiscription() + " " + tool.getComment2().replace(':' ,' '));
                        newProduct2.setSelectedAmount(nfComment2.getValue());
                        selectProductSubView.addProductToSelectedProductsFromRemoteView(newProduct2);
                        avatar.setColorIndex(5);
                        try {
                            saveSelectedWorkOrder();
                        } catch (ValidationException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    else{
                        //Tools with One item
                        Product newProduct1 = new Product();
                        newProduct1.setOption(true);
                        newProduct1.setDate(LocalDate.now());
                        newProduct1.setProductCode(tool.getAbbreviation()+"1");
                        newProduct1.setId(tool.getAbbreviation()+"1");
                        newProduct1.setInternalName(tool.getDiscription() + " " + tool.getComment1().replace(':' ,' '));
                        newProduct1.setComment(tool.getDiscription() + " " + tool.getComment1().replace(':' ,' '));
                        newProduct1.setSelectedAmount(nfComment1.getValue());
                        selectProductSubView.addProductToSelectedProductsFromRemoteView(newProduct1);
                        avatar.setColorIndex(5);
                        try {
                            saveSelectedWorkOrder();
                        } catch (ValidationException ex) {
                            throw new RuntimeException(ex);
                        }
                    }

                });
                selectionLayout.add(new Div(saveButton));
                infoLayout
                        .add(new Details("Selectie", selectionLayout));

                cardLayout.add(avatar, infoLayout);
                return cardLayout;
            });

    private void setSelectedWorkOrder(WorkOrder workOrder) {
        selectedWorkOrder = workOrder;
        workOrderBinder.readBean(selectedWorkOrder);
        workOrderHeaderBinder.readBean(selectedWorkOrder.getWorkOrderHeaderList().get(0));
        addItemsToWorkTimeGrid(selectedWorkOrder.getWorkOrderHeaderList().get(0).getWorkOrderTimeList());
        updateGetImageButton();
        selectProductSubView.setSelectedProductList(selectedWorkOrder.getProductList());
        addTabButton.setEnabled(true);
    }

    private void readNewWorkOrder() {
        WorkOrder newWorkOrder = new WorkOrder();
        newWorkOrder.setWorkDateTime(LocalDateTime.now());
        newWorkOrder.setWorkOrderStatus(WorkOrderStatus.RUNNING);
        newWorkOrder.setStarter(true);
        newWorkOrder.setLinkedWorkOrders(new ArrayList<>());

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
        List<Product>products = new ArrayList<>();
        Product product1 = new Product();
        product1.setSelectedAmount(0.0);
        product1.setSellPrice(0.0);
        product1.setTotalPrice(0.0);
        product1.setVat(VAT.EENENTWINTIG);
        products.add(product1);
        newWorkOrder.setProductList(products);
        selectedWorkOrder = newWorkOrder;
        workOrderBinder.readBean(selectedWorkOrder);
        workOrderHeaderBinder.readBean(selectedWorkOrder.getWorkOrderHeaderList().get(0));
        addItemsToWorkTimeGrid(selectedWorkOrder.getWorkOrderHeaderList().get(0).getWorkOrderTimeList());
        selectProductSubView.setSelectedProductList(selectedWorkOrder.getProductList());
        addTabButton.setEnabled(false);
    }

    private void createNewLinkedWorkOrder(WorkOrder starterWorkOrder) throws ValidationException {
        WorkOrder newWorkOrder = new WorkOrder();
        newWorkOrder.setStarter(false);
        newWorkOrder.setWorkDateTime(LocalDateTime.now());
        newWorkOrder.setWorkOrderStatus(WorkOrderStatus.RUNNING);
        newWorkOrder.setWorkAddress(selectedWorkOrder.getWorkAddress());
        newWorkOrder.setWorkLocation(selectedWorkOrder.getWorkLocation());
        newWorkOrder.setWorkType(selectedWorkOrder.getWorkType());
        newWorkOrder.setWorkOrderStatus(selectedWorkOrder.getWorkOrderStatus());

        newWorkOrder.setLinkedWorkOrders(new ArrayList<>());

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
        List<Product>products = new ArrayList<>();
        Product product1 = new Product();
        product1.setSelectedAmount(0.0);
        product1.setSellPrice(0.0);
        product1.setTotalPrice(0.0);
        product1.setVat(VAT.EENENTWINTIG);
        products.add(product1);
        newWorkOrder.setProductList(products);
        String id = workOrderService.save(newWorkOrder);

        //add Id to Starter WorkOrder
        try{
            Optional<WorkOrder> optStarter = workOrderService.getStarterByLinkedId(selectedWorkOrder.getId());
            if(optStarter.isPresent()){
                optStarter.get().getLinkedWorkOrders().add(id);
                workOrderService.save(optStarter.get());
            }
        }
        catch (Exception e){
            selectedWorkOrder.getLinkedWorkOrders().add(id);
            workOrderService.save(selectedWorkOrder);
        }
        selectedWorkOrder = newWorkOrder;
        workOrderBinder.readBean(selectedWorkOrder);
        workOrderHeaderBinder.readBean(selectedWorkOrder.getWorkOrderHeaderList().get(0));
        addItemsToWorkTimeGrid(selectedWorkOrder.getWorkOrderHeaderList().get(0).getWorkOrderTimeList());
        selectProductSubView.setSelectedProductList(selectedWorkOrder.getProductList());
        Tab newTab = new Tab(selectedWorkOrder.getWorkDateTime().format(DateTimeFormatter.ofPattern("dd/MM")));
        buddyTab.add(newTab);
        buddyTab.setSelectedTab(newTab);
        addTabButton.setEnabled(false);
    }

    private String saveSelectedWorkOrder() throws ValidationException {
        workOrderHeaderBinder.writeBean(selectedWorkOrder.getWorkOrderHeaderList().get(selectedTeam-1));
        workOrderBinder.writeBean(selectedWorkOrder);
        selectedWorkOrder.setProductList(selectProductSubView.getSelectedProductList());
        String id = workOrderService.save(selectedWorkOrder);
        return id;
    }

    @PostConstruct
    private void init() {
        addProductEventListener.setEventConsumer(event -> {
            // UI-thread safe update
            UI.getCurrent().access(() -> {
                try {
                    saveSelectedWorkOrder();
                }
                catch (ValidationException e) {
                    Notification notification = Notification.show(event.getMessage());
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });
        });
    }

//    @Override
//    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
//        //Only read new WorkOrder to start if nog parameter in the link is set.
//        if(linkParameter == null){
//            readNewWorkOrder();
//        }
//    }

    @Override
    public void setParameter(BeforeEvent beforeEvent,@OptionalParameter String s) {
        //TODO open SelectedWorkOrder bij linkParameter
        if(s != null){

            //Open WorkOrder by an other page and search WorkOrder by linkParameter.
            //So for every page with CurrentWorkOrderSubView in it that will open a clicked item in WorkOrderView
            linkParameter = s;
                List<WorkOrder> workOrderListByStarterId = workOrderService.getWorkOrderListByStarterId(s);
                if((workOrderListByStarterId !=  null) && (workOrderListByStarterId.size() > 0)){
                    selectedCoupledWorkOrders = workOrderListByStarterId;

                    buddyTab.removeAll();
                    //add Parent Tab
                    CustomTab parentTab = new CustomTab();
                    WorkOrder partentWorkOrder = selectedCoupledWorkOrders.stream().filter(item -> item.getStarter()).findFirst().get();
                    parentTab.setLabel(partentWorkOrder.getWorkDateTime().format(DateTimeFormatter.ofPattern("dd/MM")));
                    parentTab.setWorkOrderId(partentWorkOrder.getId());
                    buddyTab.add(parentTab);
                    setSelectedWorkOrder(partentWorkOrder);
                    buddyTab.setSelectedTab(parentTab);

                    //Try to add ChildrenTab
                    selectedCoupledWorkOrders.stream().filter(item -> item.getStarter() == false).forEach(item -> {
                        CustomTab childTab = new CustomTab();
                        childTab.setLabel(item.getWorkDateTime().format(DateTimeFormatter.ofPattern("dd/MM")));
                        childTab.setWorkOrderId(item.getId());
                        buddyTab.add(childTab);
                    });

                    buddyTab.addSelectedChangeListener(event -> {
                        CustomTab selectedCustomTab = (CustomTab)buddyTab.getSelectedTab();
                        setSelectedWorkOrder(workOrderService.getWorkOrderById(selectedCustomTab.getWorkOrderId()).get());
                    });
                }
                else{
                    Notification show = Notification.show("Deze werkbon kon niet worden geopend!");
                    show.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
        }
        else{
            linkParameter = null;
            readNewWorkOrder();
        }
    }
}

