package com.adverto.dejonghe.application.views.workorder;

import com.adverto.dejonghe.application.customEvents.AddProductEventListener;
import com.adverto.dejonghe.application.dbservices.CustomerService;
import com.adverto.dejonghe.application.dbservices.EmployeeService;
import com.adverto.dejonghe.application.dbservices.ProductService;
import com.adverto.dejonghe.application.dbservices.WorkOrderService;
import com.adverto.dejonghe.application.entities.WorkOrder.BowlEntity;
import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrder;
import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrderHeader;
import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrderTime;
import com.adverto.dejonghe.application.entities.customers.Address;
import com.adverto.dejonghe.application.entities.employee.Employee;
import com.adverto.dejonghe.application.entities.enums.employee.UserFunction;
import com.adverto.dejonghe.application.entities.enums.fleet.Fleet;
import com.adverto.dejonghe.application.entities.enums.fleet.FleetWorkType;
import com.adverto.dejonghe.application.entities.enums.product.VAT;
import com.adverto.dejonghe.application.entities.enums.workorder.*;
import com.adverto.dejonghe.application.entities.product.product.Product;
import com.adverto.dejonghe.application.services.workorder.WorkOrderServices;
import com.adverto.dejonghe.application.views.subViews.CurrentWorkOrdersSubView;
import com.adverto.dejonghe.application.views.subViews.SelectProductSubView;
import com.adverto.dejonghe.application.views.subViews.ShowImageSubVieuw;
import com.adverto.dejonghe.application.views.subViews.toolsSubView.*;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ErrorLevel;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.router.*;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.PostConstruct;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
    WorkOrderServices workOrderServices;
    GridFsTemplate gridFsTemplate;
    CurrentWorkOrdersSubView currtentWorkOrdersSubVieuw;
    ShowImageSubVieuw showImageSubVieuw;
    AddProductEventListener addProductEventListener;
    ToolsFuelView toolsFuelView;
    ToolsOkSubView toolsOkSubView;
    ToolsPTAView  toolsPTAView;
    ToolsRegularIntenseFuelView toolsRegularIntenseFuelView;
    ToolsRegularIntenseView toolsRegularIntenseView;
    ToolsSpyLaneView toolsSpyLaneView;
    ToolsThicknessMeterView toolsThicknessMeterView;
    ToolsWorkhoursView toolsWorkhoursView;
    ToolsFixedPriceView toolsFixedPriceView;

    SplitLayout mainSplitLayout;
    SplitLayout headerSplitLayout;

    FormLayout formLayout;

    ComboBox<Address> addressComboBox = new ComboBox<>();
    DatePicker datePicker = new DatePicker();
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

    Grid<BowlEntity>bowlGrid;
    List<BowlEntity>selectedBowlEntities;
    BowlEntity selectedBowlEntity;

    ComboBox<Fleet> fleetComboBox = new ComboBox<>();
    TextField tfFleetHours = new TextField();
    TextField tfRoadTax = new TextField();
    TextField tfTunnelTax = new TextField();
    ComboBox<FleetWorkType> fleetWorkTypeComboBox = new ComboBox<>();

    MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    Upload dropEnabledUpload = new Upload(buffer);
    Button showImageButton;

    Button saveWorkOrderButton = new Button("Bewaar werkbon");
    Button finishButton = new Button("Stuur door facturatie");

    VerticalLayout vLayoutHeaderLevel2Tabs;
    VerticalLayout vLayoutHeaderLevel3Tabs;
    VerticalLayout vLayoutHeaderLevel4Tabs;
    VerticalLayout vLayoutHeaderLevel5Tabs;
    VerticalLayout vLayoutHeaderLevel6Tabs;
    VerticalLayout vLayoutHeaderLevel7Tabs;
    VerticalLayout vLayoutHeaderLevel8Tabs;

    VerticalLayout level5VerticalLayout;

    Integer selectedTeam = 1;

    Binder<WorkOrder>workOrderBinder;
    Binder<WorkOrderHeader>workOrderHeaderBinder;

    WorkOrder selectedWorkOrder;
    List<WorkOrder> selectedCoupledWorkOrders;
    Dialog finishWorkOrderDialog;
    Dialog searchCurrentWorkOrderDialog;
    Dialog removeWorkOrderHourDialog;
    Dialog removeBowlDialog;
    Dialog imageDialog;
    Dialog errorDialog;

    Tabs buddyTab;
    Button addTabButton;

    String linkParameter;

    Grid.Column<WorkOrderTime> upColumn;
    Grid.Column<WorkOrderTime> downColumn;
    Grid.Column<WorkOrderTime> overNightColumn;

    Dialog optionDialog;

    TimePicker timeUpPicker;
    TimePicker timeStartPicker;
    TimePicker timeStopPicker;
    TimePicker timeDownPicker;

    List<String>errorList = new ArrayList<>();
    VerticalLayout errorDialogVerticalLayout;
    VerticalLayout errorDialogErrorsVerticalLayout;

    Grid.Column<BowlEntity> chassisNrColumn;
    Grid.Column<BowlEntity> bowlNrEntityColumnIn;
    Grid.Column<BowlEntity> bowlInColumn;
    Grid.Column<BowlEntity> bowlNrEntityColumnUit;
    Grid.Column<BowlEntity> bowlUitColumn;
    Grid.Column<BowlEntity> draaiurenColumn;

    public WorkorderView(ProductService productService,
                         CustomerService customerService,
                         EmployeeService employeeService,
                         SelectProductSubView selectProductSubView,
                         WorkOrderService workOrderService,
                         WorkOrderServices workOrderServices,
                         GridFsTemplate gridFsTemplate,
                         CurrentWorkOrdersSubView currtentWorkOrdersSubVieuw,
                         ShowImageSubVieuw showImageSubVieuw,
                         AddProductEventListener listener,
                         ToolsFuelView toolsFuelView,
                         ToolsOkSubView toolsOkSubView,
                         ToolsPTAView toolsPTAView,
                         ToolsRegularIntenseFuelView toolsRegularIntenseFuelView,
                         ToolsRegularIntenseView toolsRegularIntenseView,
                         ToolsSpyLaneView toolsSpyLaneView,
                         ToolsThicknessMeterView toolsThicknessMeterView,
                         ToolsWorkhoursView toolsWorkhoursView,
                         ToolsFixedPriceView toolsFixedPriceView) {
        this.productService = productService;
        this.selectProductSubView = selectProductSubView;
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.workOrderService = workOrderService;
        this.workOrderServices = workOrderServices;
        this.gridFsTemplate = gridFsTemplate;
        this.currtentWorkOrdersSubVieuw = currtentWorkOrdersSubVieuw;
        this.showImageSubVieuw = showImageSubVieuw;
        this.addProductEventListener = listener;
        this.toolsFuelView = toolsFuelView;
        this.toolsOkSubView = toolsOkSubView;
        this.toolsRegularIntenseFuelView = toolsRegularIntenseFuelView;
        this.toolsRegularIntenseView = toolsRegularIntenseView;
        this.toolsSpyLaneView = toolsSpyLaneView;
        this.toolsThicknessMeterView = toolsThicknessMeterView;
        this.toolsWorkhoursView = toolsWorkhoursView;
        this.toolsPTAView = toolsPTAView;
        this.toolsFixedPriceView = toolsFixedPriceView;


        selectProductSubView.setUserFunction(UserFunction.TECHNICIAN);
        setUpImageDialog();
        setUpCurrentWorkOrderDialog();
        setUpFinishWorkOrderDialog();
        setUpRemoveWorkOrderHour();
        setUpRemoveBowlDialog();
        setUpFinishButton();
        setUpSaveButton();
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
        verticalLayout.setMargin(false);
        verticalLayout.setWidth("100%");
        verticalLayout.add(getMainButtons(),formLayout);
        headerSplitLayout.addToPrimary(verticalLayout);
        VerticalLayout vLayout = new VerticalLayout();
        vLayout.add(selectProductSubView.getFilter());
        vLayout.add(selectProductSubView.getSelectedProductGrid());
        HorizontalLayout hLayout = new HorizontalLayout();
        hLayout.setWidth("100%");
        hLayout.add(dropEnabledUpload,showImageButton);
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidth("100%");
        horizontalLayout.add(saveWorkOrderButton,finishButton);
        vLayout.add(hLayout,horizontalLayout);
        headerSplitLayout.addToSecondary(vLayout);
        mainSplitLayout.addToPrimary(selectProductSubView.getLayout());
        mainSplitLayout.addToSecondary(headerSplitLayout);
        this.setHeightFull();
        add(mainSplitLayout);
        setUpWorkOrderBinder();
        setUpWorkOrderHeaderBinder();
        setUpSpinnerGrid();
        setUpTimeGrid();
        setUpErrorDialog();
    }

    private void setUpErrorDialog() {
        errorDialog = new Dialog();
        errorDialog.setWidth("100%");
        errorDialog.setHeight("30%");
        errorDialog.setDraggable(true);
        createLayoutErrorDialog(errorDialog);
    }

    private void createLayoutErrorDialog(Dialog dialog) {
        errorDialogVerticalLayout = new VerticalLayout();
        errorDialogErrorsVerticalLayout = new VerticalLayout();
        errorDialogErrorsVerticalLayout.setWidth("100%");
        errorDialogErrorsVerticalLayout.add(new H2("Koekoe"));

        H2 headline = new H2("Controle Werkbon");
        headline.getStyle().set("margin", "var(--lumo-space-m) 0")
                .set("font-size", "1.5em").set("font-weight", "bold");
        errorDialogVerticalLayout.add(headline);


        errorDialogVerticalLayout.add(errorDialogErrorsVerticalLayout);

        Button closeButton = new Button("Close");
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClickListener(e -> dialog.close());
        errorDialogVerticalLayout.add(closeButton);

        errorDialogVerticalLayout.setPadding(false);
        errorDialogVerticalLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        errorDialogVerticalLayout.getStyle().set("width", "300px").set("max-width", "100%");
        errorDialogVerticalLayout.setAlignSelf(FlexComponent.Alignment.END, closeButton);

        dialog.add(errorDialogVerticalLayout);
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

    private void setUpSpinnerGrid() {
        bowlGrid = new Grid<>();
        bowlGrid.setAllRowsVisible(true);
        chassisNrColumn = bowlGrid.addComponentColumn(item -> {
            TextField tfChassisNumber = new TextField();
            tfChassisNumber.setWidth("100%");
            tfChassisNumber.setValue(item.getChassisNumber());
            tfChassisNumber.addValueChangeListener(event -> {
                try {
                    item.setChassisNumber(tfChassisNumber.getValue());
                    workOrderTimeGrid.getDataProvider().refreshAll();
                    selectedWorkOrder.getWorkOrderHeaderList().get(selectedTeam - 1).setBowlEntityList(selectedBowlEntities);
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
            return tfChassisNumber;
        }).setHeader("Chassis nr.");
        draaiurenColumn = bowlGrid.addComponentColumn(item -> {
            TextField tfHours = new TextField();
            tfHours.setWidth("100%");
            tfHours.setValue(item.getWorkhours().toString());
            tfHours.addValueChangeListener(event -> {
                try {
                    item.setWorkhours(Integer.valueOf(tfHours.getValue()));
                    workOrderTimeGrid.getDataProvider().refreshAll();
                    selectedWorkOrder.getWorkOrderHeaderList().get(selectedTeam - 1).setBowlEntityList(selectedBowlEntities);
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
            return tfHours;
        }).setHeader("Draaiuren");

        bowlUitColumn = bowlGrid.addComponentColumn(item -> {
            Checkbox checkbRemoved = new Checkbox(item.getBBowlRemoved());
            checkbRemoved.setValue(item.getBBowlRemoved());
            checkbRemoved.getStyle().set("transform", "scale(1.5)");
            checkbRemoved.addValueChangeListener(event -> {
                try {
                    item.setBBowlRemoved(checkbRemoved.getValue());
                    workOrderTimeGrid.getDataProvider().refreshAll();
                    selectedWorkOrder.getWorkOrderHeaderList().get(selectedTeam - 1).setBowlEntityList(selectedBowlEntities);
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
            return checkbRemoved;
        }).setFlexGrow(0).setAutoWidth(true);
        bowlNrEntityColumnUit = bowlGrid.addComponentColumn(item -> {
            TextField tfBowlNumberRemoved = new TextField();
            tfBowlNumberRemoved.setWidth("100%");
            tfBowlNumberRemoved.setValue(item.getBowlRemovedNumber());
            tfBowlNumberRemoved.addValueChangeListener(event -> {
                try {
                    item.setBowlRemovedNumber(tfBowlNumberRemoved.getValue());
                    workOrderTimeGrid.getDataProvider().refreshAll();
                    selectedWorkOrder.getWorkOrderHeaderList().get(selectedTeam - 1).setBowlEntityList(selectedBowlEntities);
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
            return tfBowlNumberRemoved;
        }).setHeader("Bowl nr. uit");

        bowlInColumn = bowlGrid.addComponentColumn(item -> {
            Checkbox checkbBowlReplaced = new Checkbox(item.getBBowlReplaced());
            checkbBowlReplaced.setValue(item.getBBowlReplaced());
            checkbBowlReplaced.getStyle().set("transform", "scale(1.5)");
            checkbBowlReplaced.addValueChangeListener(event -> {
                try {
                    item.setBBowlReplaced(checkbBowlReplaced.getValue());
                    workOrderTimeGrid.getDataProvider().refreshAll();
                    selectedWorkOrder.getWorkOrderHeaderList().get(selectedTeam - 1).setBowlEntityList(selectedBowlEntities);
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
            return checkbBowlReplaced;
        }).setFlexGrow(0).setAutoWidth(true);
        bowlNrEntityColumnIn = bowlGrid.addComponentColumn(item -> {
            TextField tfBowlNumberReplaced = new TextField();
            tfBowlNumberReplaced.setWidth("100%");
            tfBowlNumberReplaced.setValue(item.getBowlReplacedNumber());
            tfBowlNumberReplaced.addValueChangeListener(event -> {
                try {
                    item.setBowlReplacedNumber(tfBowlNumberReplaced.getValue());
                    workOrderTimeGrid.getDataProvider().refreshAll();
                    selectedWorkOrder.getWorkOrderHeaderList().get(selectedTeam - 1).setBowlEntityList(selectedBowlEntities);
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
            return tfBowlNumberReplaced;
        }).setHeader("Bowl nr. in");
        bowlGrid.addComponentColumn(item -> {
            Button plusButton = new Button(new Icon(VaadinIcon.PLUS));
            plusButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            plusButton.setAriaLabel("Voeg een regel toe");
            plusButton.addClickListener(event -> {
                BowlEntity bowlEntity = new BowlEntity();
                bowlEntity.setChassisNumber("");
                bowlEntity.setWorkhours(1);
                bowlEntity.setBBowlRemoved(false);
                bowlEntity.setBBowlReplaced(false);
                bowlEntity.setBowlRemovedNumber("");
                bowlEntity.setBowlReplacedNumber("");
                bowlEntity.setWorkDate(LocalDate.now());
                selectedBowlEntities.add(bowlEntity);
                bowlGrid.getDataProvider().refreshAll();
                Notification.show("Een regel is toegevoegd!");
            });
            return plusButton;
        }).setFlexGrow(0);

        bowlGrid.addComponentColumn(item -> {
            Button plusButton = new Button(new Icon(VaadinIcon.MINUS));
            plusButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            plusButton.setAriaLabel("Voeg een regel toe");
            plusButton.addClickListener(event -> {
                selectedBowlEntity = item;
                removeBowlDialog.open();
                Notification.show("Een regel is toegevoegd!");
            });
            return plusButton;
        }).setFlexGrow(0);
    }

    private void setUpTimeGrid() {
        workOrderTimeGrid = new Grid<>();
        //workOrderTimeGrid.setHeight("250px");
        workOrderTimeGrid.setAllRowsVisible(true);
        upColumn = workOrderTimeGrid.addComponentColumn(item -> {
            timeUpPicker = new TimePicker();
            timeUpPicker.setValue(item.getTimeUp());
            timeUpPicker.setLocale(Locale.FRENCH);
            timeUpPicker.setMin(LocalTime.of(5,0,0));
            timeUpPicker.setStep(Duration.ofMinutes(15));
            timeUpPicker.setWidth("100%");
            timeUpPicker.addValueChangeListener(event -> {
                try {
                    item.setTimeUp(timeUpPicker.getValue());
                    workOrderTimeGrid.getDataProvider().refreshAll();
                    selectedWorkOrder.getWorkOrderHeaderList().get(selectedTeam - 1).setWorkOrderTimeList(selectedWorkOrderTimes);
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
            timeStartPicker = new TimePicker();
            timeStartPicker.setValue(item.getTimeStart());
            timeStartPicker.setLocale(Locale.FRENCH);
            timeStartPicker.setMin(LocalTime.of(5,0,0));
            timeStartPicker.setStep(Duration.ofMinutes(15));
            timeStartPicker.setWidth("100%");
            timeStartPicker.addFocusListener(event -> {
                if(timeUpPicker.getValue() != null) {
                    timeStartPicker.setMin(timeUpPicker.getValue());
                }
                else{
                    timeStartPicker.setMin(LocalTime.of(5,0,0));
                }

            });
            timeStartPicker.addValueChangeListener(event -> {
                try {
                    item.setTimeStart(timeStartPicker.getValue());
                    workOrderTimeGrid.getDataProvider().refreshAll();
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
        }).setHeader("Start werk");

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
            timeStopPicker = new TimePicker();
            timeStopPicker.setValue(item.getTimeStop());
            timeStopPicker.setLocale(Locale.FRENCH);
            timeStopPicker.setStep(Duration.ofMinutes(15));
            timeStopPicker.setWidth("100%");
            timeStopPicker.addFocusListener(event -> {
                timeStopPicker.setMin(timeStartPicker.getValue());
            });
            timeStopPicker.addValueChangeListener(event -> {
                try {
                    item.setTimeStop(timeStopPicker.getValue());
                    //timeDownPicker.setMin(timeStopPicker.getValue());
                    workOrderTimeGrid.getDataProvider().refreshAll();
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
        }).setHeader("Stop werk");

        downColumn = workOrderTimeGrid.addComponentColumn(item -> {
            timeDownPicker = new TimePicker();
            timeDownPicker.setValue(item.getTimeDown());
            timeDownPicker.setLocale(Locale.FRENCH);
            timeDownPicker.setStep(Duration.ofMinutes(15));
            timeDownPicker.setWidth("100%");
            timeDownPicker.addFocusListener(event -> {
                timeDownPicker.setMin(timeStopPicker.getValue());
            });
            timeDownPicker.addValueChangeListener(event -> {
                try {
                    item.setTimeDown(timeDownPicker.getValue());
                    selectedWorkOrder.getWorkOrderHeaderList().get(selectedTeam-1).setWorkOrderTimeList(selectedWorkOrderTimes);
                    try{
                        long hours = ChronoUnit.HOURS.between(item.getTimeUp(), item.getTimeDown());
                        tfFleetHours.setPlaceholder(String.valueOf(hours));
                    }
                    catch (Exception e){
                    }
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

        overNightColumn = workOrderTimeGrid.addComponentColumn(item -> {
            Checkbox checkbox = new Checkbox();
            checkbox.getStyle().set("transform", "scale(1.5)");
            if(item.getBOvernight() != null){
                checkbox.setValue(item.getBOvernight());
            }
           else{
               checkbox.setValue(false);
            }
            checkbox.addValueChangeListener(event -> {
                try {
                    item.setBOvernight(checkbox.getValue());
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
            return checkbox;
        }).setHeader("Overn.").setFlexGrow(0);
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
        }).setFlexGrow(0);

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
        }).setFlexGrow(0);
    }

    private void setUpCurrentWorkOrderDialog() {
        searchCurrentWorkOrderDialog = new Dialog();
        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE));
        closeButton.addClickListener(event -> {
            searchCurrentWorkOrderDialog.close();
        });
        Button openCurrentWorkOrderButton = new Button("Selecteer lopende werkbon.",
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
                Tab parentTab = new Tab();
                WorkOrder partentWorkOrder = selectedCoupledWorkOrders.stream().filter(item -> item.getStarter()).findFirst().get();
                parentTab.setLabel(partentWorkOrder.getWorkDateTime().format(DateTimeFormatter.ofPattern("dd/MM")));
                parentTab.getElement().setProperty("workOrderId", partentWorkOrder.getId());
                buddyTab.add(parentTab);
                buddyTab.setSelectedTab(parentTab);
                setSelectedWorkOrder(partentWorkOrder);

                //Try to add ChildrenTab
                selectedCoupledWorkOrders.stream().filter(item -> item.getStarter() == false).forEach(item -> {
                    Tab childTab = new Tab();
                    childTab.setLabel(item.getWorkDateTime().format(DateTimeFormatter.ofPattern("dd/MM")));
                    childTab.getElement().setProperty("workOrderId", item.getId());
                    buddyTab.add(childTab);
                });

                searchCurrentWorkOrderDialog.close();
            }
            else{
                searchCurrentWorkOrderDialog.close();
            }
                });
        openCurrentWorkOrderButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        searchCurrentWorkOrderDialog.getHeader().add(openCurrentWorkOrderButton);
        searchCurrentWorkOrderDialog.getHeader().add(closeButton);
        searchCurrentWorkOrderDialog.setCloseOnEsc(true);
        searchCurrentWorkOrderDialog.setCloseOnOutsideClick(true);
        searchCurrentWorkOrderDialog.setWidth("100%");
        searchCurrentWorkOrderDialog.setHeight("100%");
        searchCurrentWorkOrderDialog.setHeaderTitle("Lopende werkbonnen");
        searchCurrentWorkOrderDialog.add(currtentWorkOrdersSubVieuw);
    }

    private void setUpRemoveWorkOrderHour() {
        removeWorkOrderHourDialog = new Dialog();
        removeWorkOrderHourDialog.setHeaderTitle("Ben je zeker dat je de geselecteerde tijd wil verwijderen?");

        VerticalLayout dialogLayout = createDialogLayoutRemoveHour();
        removeWorkOrderHourDialog.add(dialogLayout);

        Button saveButton = createRemoveTime(removeWorkOrderHourDialog);
        Button cancelButton = new Button("Niet Verwijderen", e -> removeWorkOrderHourDialog.close());
        removeWorkOrderHourDialog.getFooter().add(cancelButton);
        removeWorkOrderHourDialog.getFooter().add(saveButton);
    }

    private void setUpRemoveBowlDialog() {
        removeBowlDialog = new Dialog();
        removeBowlDialog.setHeaderTitle("Ben je zeker dat je de bowl wil verwijderen?");

        VerticalLayout dialogLayout = createDialogLayoutBowl();
        removeBowlDialog.add(dialogLayout);

        Button saveButton = createRemoveBowl(removeBowlDialog);
        Button cancelButton = new Button("Niet Verwijderen", e -> removeBowlDialog.close());
        removeBowlDialog.getFooter().add(cancelButton);
        removeBowlDialog.getFooter().add(saveButton);
    }

    private Button createRemoveBowl(Dialog removeWorkOrderHourDialog) {

        Button removeButton = new Button("Verwijderen");
        removeButton.addClickListener(click -> {
            selectedBowlEntities.remove(selectedBowlEntity);
            bowlGrid.getDataProvider().refreshAll();
            Notification.show("Bowl verwijderen!");
            removeBowlDialog.close();
        });
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        return removeButton;
    }

    private Button createRemoveTime(Dialog removeWorkOrderHourDialog) {

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

    private static VerticalLayout createDialogLayoutBowl() {

        Span span = new Span("Deze bowl wordt definitief verwijderd!");
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

        finishWorkOrderDialog.addOpenedChangeListener(listener -> {
            errorList = workOrderServices.checkWorkOrderBeforeSendToInvoice(selectedWorkOrder, errorList);
            if(errorList.size() > 0){
                errorDialogErrorsVerticalLayout.removeAll();
                for(String error : errorList){
                    Button button = new Button(error);
                    button.addThemeVariants(ButtonVariant.LUMO_ERROR);
                    button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                    errorDialogErrorsVerticalLayout.add(button);
                }
                finishWorkOrderDialog.close();
            }
            if (listener.isOpened()){
                if(errorList.isEmpty()) {
                    saveButton.setEnabled(true);
                }
                else{
                    saveButton.setEnabled(false);
                    errorDialog.setTop(headerSplitLayout.getSplitterPosition().toString()+"%");
                    errorDialog.setHeight("100%");
                    errorDialog.open();
                }
            }
        });
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
                workOrderBinder.writeBean(selectedWorkOrder);
                workOrderHeaderBinder.writeBean(selectedWorkOrder.getWorkOrderHeaderList().get(selectedTeam-1));
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
        finishButton.setWidth("50%");
        finishButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_SUCCESS);
        finishButton.addClickListener(e -> {
            finishWorkOrderDialog.open();
        });
    }

    private void setUpSaveButton() {
        saveWorkOrderButton.setWidth("50%");
        saveWorkOrderButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_WARNING);
        saveWorkOrderButton.addClickListener(e -> {
            try {
                saveSelectedWorkOrder();
            } catch (ValidationException f) {
                Notification.show("Kon de werkbon nog niet bewaren");
            }
        });
    }

    private void setUpShowImageButton() {
        showImageButton = new Button("Er zijn in deze werkbon geen foto's toegevoegd, gelieve altijd een foto te koppelen van de werken!");
        showImageButton.setWidth("50%");

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
        workOrderHeaderBinder.forField(typeComboBox)
                .asRequired("Gelieve een werktype te selecteren!")
                .bind(WorkOrderHeader::getWorkType, WorkOrderHeader::setWorkType);
        workOrderHeaderBinder.forField(taDiscription)
                .asRequired("Gelieve een omschrijving in te geven!")
                .bind(WorkOrderHeader::getDiscription, WorkOrderHeader::setDiscription);
        workOrderHeaderBinder.forField(fleetComboBox)
                .withValidator(value -> {
                    WorkLocation keuze = locationComboBox.getValue();
                    if(value != null){
                        return true;
                    }
                    if (WorkLocation.ON_THE_MOVE.equals(keuze)) {
                        return false;
                    }
                    return true;
                }, "Verplichte invulling nodig wegens keuze verplaatsing")
                .bind(WorkOrderHeader::getFleet, WorkOrderHeader::setFleet);
        workOrderHeaderBinder.forField(tfFleetHours)
                .withNullRepresentation("")
                .withConverter(new StringToDoubleConverter("Dit is geen decimaal getal!"))
                .withValidator(value -> {
                    Fleet fleet = fleetComboBox.getValue();
                    FleetWorkType fleetWorkType = fleetWorkTypeComboBox.getValue();
                    if((value != null) && (fleetWorkType != null)) {
                        return true;
                    }
                    if ((Fleet.TRUCK_CRANE.equals(fleet)) && ((fleetWorkType.equals(FleetWorkType.INTENS) || (fleetWorkType.equals(FleetWorkType.REGULAR)) ) )) {
                        return false;
                    }
                    return true;
                }, "Verplichte invulling nodig wegens keuze kraan")
                .bind(WorkOrderHeader::getFleetHours, WorkOrderHeader::setFleetHours);
        workOrderHeaderBinder.forField(tfRoadTax)
                .withNullRepresentation("")
                .withConverter(new StringToDoubleConverter("Dit is geen decimaal getal!"))
                .bind(WorkOrderHeader::getRoadTax, WorkOrderHeader::setRoadTax);
        workOrderHeaderBinder.forField(tfTunnelTax)
                .withNullRepresentation("")
                .withConverter(new StringToDoubleConverter("Dit is geen decimaal getal!"))
                .bind(WorkOrderHeader::getTunnelTax, WorkOrderHeader::setTunnelTax);
        workOrderHeaderBinder.forField(fleetWorkTypeComboBox)
                .withValidator(value -> {
                    Fleet fleet = fleetComboBox.getValue();
                    if(value != null){
                        return true;
                    }
                    if (Fleet.TRUCK_CRANE.equals(fleet)) {
                        return false;
                    }
                    return true;
                }, "Verplichte invulling nodig wegens keuze kraan")
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
        workOrderBinder.forField(locationComboBox)
                .asRequired("Gelieve een locatie in te geven!")
                .bind(WorkOrder::getWorkLocation, WorkOrder::setWorkLocation);
        workOrderBinder.forField(datePicker)
                .asRequired("Gelieve een datum te selecteren!")
                .bind(x -> x.getWorkDateTime().toLocalDate(), (x,y)-> x.setWorkDateTime(y.atStartOfDay()));
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
        //formLayout.setRowSpacing("1px");
        //formLayout.setColumnSpacing("1px");
        formLayout.add(getFirstStepHeader());
        return formLayout;
    }

    private ComboBox<WorkType> getTypeComboBox() {
        typeComboBox = new ComboBox<>();
        typeComboBox.setItems(WorkType.values());
        typeComboBox.setItemLabelGenerator(WorkType::getDiscription);
        typeComboBox.setPlaceholder("Type");
        typeComboBox.setVisible(false);
        typeComboBox.setWidthFull();
        typeComboBox.addValueChangeListener(event -> {
            if(event.getValue() == WorkType.CENTRIFUGE){
                if(locationComboBox.getValue() != null && locationComboBox.getValue().equals(WorkLocation.ON_THE_MOVE)){
                    level5VerticalLayout.remove(bowlGrid);
                    chassisNrColumn.setVisible(true);
                    bowlNrEntityColumnIn.setVisible(true);
                    bowlInColumn.setVisible(true);
                    bowlNrEntityColumnUit.setVisible(true);
                    bowlUitColumn.setVisible(true);
                    draaiurenColumn.setVisible(true);
                    level5VerticalLayout.addComponentAsFirst(bowlGrid);
                } else if (locationComboBox.getValue() != null && locationComboBox.getValue().equals(WorkLocation.WORKPLACE)) {
                    level5VerticalLayout.remove(bowlGrid);
                    chassisNrColumn.setVisible(false);
                    bowlNrEntityColumnIn.setVisible(false);
                    bowlInColumn.setVisible(false);
                    bowlNrEntityColumnUit.setVisible(true);
                    bowlUitColumn.setVisible(false);
                    draaiurenColumn.setVisible(false);
                    level5VerticalLayout.addComponentAsFirst(bowlGrid);
                }
                else{
                    level5VerticalLayout.remove(bowlGrid);
                }
            }
            else{
                level5VerticalLayout.remove(bowlGrid);
            }
        });
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
        vLayoutHeaderLevel7Tabs.setVisible(false);

        vLayoutHeaderLevel8Tabs = new VerticalLayout();
        vLayoutHeaderLevel8Tabs.setSizeFull();
        vLayoutHeaderLevel8Tabs.setVisible(false);

        locationComboBox = new ComboBox<>();
        locationComboBox.setItems(WorkLocation.values());
        locationComboBox.setItemLabelGenerator(WorkLocation::getDiscription);
        locationComboBox.setPlaceholder("Locatie");
        locationComboBox.setWidthFull();
        locationComboBox.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                typeComboBox.clear();
                typeComboBox.setVisible(true);
                if(event.getValue().compareTo(WorkLocation.ON_THE_MOVE) == 0){
                    downColumn.setVisible(true);
                    overNightColumn.setVisible(true);
                    upColumn.setVisible(true);
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
                    vLayoutHeaderLevel3Tabs.add(getLevel5());
                    vLayoutHeaderLevel4Tabs.add(getLevel3(WorkLocation.ON_THE_MOVE));
                    vLayoutHeaderLevel5Tabs.add(getLevel4(WorkLocation.ON_THE_MOVE));
                    vLayoutHeaderLevel6Tabs.add(getLevel6(WorkLocation.ON_THE_MOVE));
                    workOrderHeaderBinder.validate();
                    //vLayoutHeaderLevel7Tabs.add(dropEnabledUpload,showImageButton);
                    //vLayoutHeaderLevel8Tabs.add(finishButton);
                    
                }
                else{
                    downColumn.setVisible(false);
                    overNightColumn.setVisible(false);
                    upColumn.setVisible(false);
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
                    vLayoutHeaderLevel3Tabs.add(getLevel5());
                    vLayoutHeaderLevel2Tabs.add(getLevel2(WorkLocation.WORKPLACE));
                    vLayoutHeaderLevel4Tabs.add(getLevel3(WorkLocation.WORKPLACE));
                    vLayoutHeaderLevel6Tabs.add(getLevel6(WorkLocation.WORKPLACE));
                    //vLayoutHeaderLevel7Tabs.add(dropEnabledUpload);
                    //vLayoutHeaderLevel8Tabs.add(finishButton);
                }
            }
        });
        return locationComboBox;
    }

    private VerticalLayout getLevel6(WorkLocation workLocation) {
        VerticalLayout vLayout = new VerticalLayout();
        vLayout.setPadding(false);
        vLayout.setMargin(false);
        HorizontalLayout horizontalLayoutLevel6 = new HorizontalLayout();
        horizontalLayoutLevel6.setSizeFull();
        horizontalLayoutLevel6.setPadding(false);
        horizontalLayoutLevel6.setMargin(false);
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
            addAvatarToVirtualList(horizontalLayoutLevel6, Arrays.stream(Tools.values()).filter(filter -> filter.getWorkLocation().equals(WorkLocation.ON_THE_MOVE)).filter(filter -> filter.getGroup().equals(1)).sorted(Comparator.comparing(Tools::getPosition)).collect(Collectors.toList()));
            addAvatarToVirtualList(horizontalLayoutLevel6, Arrays.stream(Tools.values()).filter(filter -> filter.getWorkLocation().equals(WorkLocation.ON_THE_MOVE)).filter(filter -> filter.getGroup().equals(2)).sorted(Comparator.comparing(Tools::getPosition)).collect(Collectors.toList()));
            addAvatarToVirtualList(horizontalLayoutLevel6, Arrays.stream(Tools.values()).filter(filter -> filter.getWorkLocation().equals(WorkLocation.ON_THE_MOVE)).filter(filter -> filter.getGroup().equals(3)).sorted(Comparator.comparing(Tools::getPosition)).collect(Collectors.toList()));
            addAvatarToVirtualList(horizontalLayoutLevel6, Arrays.stream(Tools.values()).filter(filter -> filter.getWorkLocation().equals(WorkLocation.ON_THE_MOVE)).filter(filter -> filter.getGroup().equals(4)).sorted(Comparator.comparing(Tools::getPosition)).collect(Collectors.toList()));
            vLayout.add(hideButton,horizontalLayoutLevel6);
            return vLayout;
        }
        else{
            addAvatarToVirtualList(horizontalLayoutLevel6, Arrays.stream(Tools.values()).filter(filter -> filter.getWorkLocation().equals(WorkLocation.WORKPLACE)).filter(filter -> filter.getGroup().equals(1)).sorted(Comparator.comparing(Tools::getPosition)).collect(Collectors.toList()));
            addAvatarToVirtualList(horizontalLayoutLevel6, Arrays.stream(Tools.values()).filter(filter -> filter.getWorkLocation().equals(WorkLocation.WORKPLACE)).filter(filter -> filter.getGroup().equals(2)).sorted(Comparator.comparing(Tools::getPosition)).collect(Collectors.toList()));
            addAvatarToVirtualList(horizontalLayoutLevel6, Arrays.stream(Tools.values()).filter(filter -> filter.getWorkLocation().equals(WorkLocation.WORKPLACE)).filter(filter -> filter.getGroup().equals(3)).sorted(Comparator.comparing(Tools::getPosition)).collect(Collectors.toList()));

            vLayout.add(hideButton,horizontalLayoutLevel6);
            return vLayout;
        }

    }

    private void addAvatarToVirtualList(HorizontalLayout horizontalLayoutLevel6, List<Tools> toolsToAdd) {
        VirtualList<Tools> toolsVirtualList = new VirtualList<>();
        toolsVirtualList.setHeight("220px");
        visualizeSelectedOptions(toolsToAdd);
        toolsVirtualList.setItems(toolsToAdd);
        toolsVirtualList.setRenderer(toolCardRenderer);
        horizontalLayoutLevel6.add(toolsVirtualList);
    }

    private void visualizeSelectedOptions(List<Tools> toolsToAdd){
        for(Tools tool : toolsToAdd){
            Set<Product> collect = selectedWorkOrder.getProductList().stream().filter(item -> {
                if(item.getAbbreviation() != null){
                    return item.getAbbreviation()
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

    private VerticalLayout getLevel5() {
        level5VerticalLayout = new VerticalLayout();
        level5VerticalLayout.setWidth("100%");
        level5VerticalLayout.setPadding(false);
        taDiscription.setWidthFull();
        taDiscription.setHeight("100%");
        taDiscription.setPlaceholder("Gelieve een duidelijke omschrijving te geven betreffende de uitgevoerde werken");
        level5VerticalLayout.add(taDiscription);
        return level5VerticalLayout;
    }

    private HorizontalLayout getLevel4(WorkLocation workLocation) {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setPadding(false);
        horizontalLayout.setWidth("100%");
        if(workLocation == WorkLocation.ON_THE_MOVE){

            HorizontalLayout horizontalLayout2 = new HorizontalLayout();
            horizontalLayout2.setVisible(false);

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
            fleetComboBox.addValueChangeListener(item -> {
                if((item.getValue() != null) && (item.getValue().equals(Fleet.TRUCK_CRANE))){
                    horizontalLayout2.setVisible(true);
                }
                else{
                    horizontalLayout2.setVisible(false);
                }
            });

            tfRoadTax.setWidth("100%");
            tfRoadTax.setSuffixComponent(new Span("€"));
            tfRoadTax.setPlaceholder("Wegentaks");

            tfTunnelTax.setWidth("100%");
            tfTunnelTax.setSuffixComponent(new Span("€"));
            tfTunnelTax.setPlaceholder("Tunneltaks");

            horizontalLayout2.setWidth("100%");
            fleetWorkTypeComboBox.setItems(FleetWorkType.values());
            fleetWorkTypeComboBox.setItemLabelGenerator(FleetWorkType::getDiscription);
            fleetWorkTypeComboBox.setWidth("50%");
            fleetWorkTypeComboBox.setPlaceholder("Type werk kraan");
            fleetWorkTypeComboBox.addValueChangeListener(item -> {
                if((item.getValue() != null) && (item.getValue().equals(FleetWorkType.DELIVERY))){
                    tfFleetHours.setEnabled(false);
                }
                else{
                    tfFleetHours.setEnabled(true);
                }
            });

            tfFleetHours.setWidth("50%");
            tfFleetHours.setPlaceholder("Aantal uren kraan");
            horizontalLayout2.add(fleetWorkTypeComboBox, tfFleetHours);

            vLayout1.setPadding(false);
            vLayout2.setPadding(false);

            vLayout2.add(tfRoadTax,tfTunnelTax);
            vLayout1.add(fleetComboBox, horizontalLayout2);



            horizontalLayout.add(vLayout1, vLayout2);
        }
        return horizontalLayout;
    }

    private VerticalLayout getLevel3(WorkLocation workLocation) {
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setPadding(false);
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
        addItemsToBowlGrid(selectedWorkOrder.getWorkOrderHeaderList().get(0).getBowlEntityList());

        Icon homeIcon1 = VaadinIcon.WORKPLACE.create();
        homeIcon1.addClickListener(e -> {
            selectedTeam = 1;
            selectProductSubView.setSelectedTeam(selectedTeam-1);
            workOrderHeaderBinder.readBean(selectedWorkOrder.getWorkOrderHeaderList().get(0));
            addItemsToWorkTimeGrid(selectedWorkOrder.getWorkOrderHeaderList().get(0).getWorkOrderTimeList());
            addItemsToBowlGrid(selectedWorkOrder.getWorkOrderHeaderList().get(0).getBowlEntityList());
            horizontalLayout1.setClassName("selected");
            horizontalLayout2.setClassName("");
            horizontalLayout3.setClassName("");
            horizontalLayout4.setClassName("");
        });

        cbExtraEmployees1.setWidth("100%");
        Optional<List<Employee>> employees1 = employeeService.getAll();
        if(employees1.isPresent()){
            cbMasterEmployee1.setPlaceholder("Team 1");
            cbMasterEmployee1.setClearButtonVisible(true);
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
            addItemsToBowlGrid(selectedWorkOrder.getWorkOrderHeaderList().get(1).getBowlEntityList());
            selectedTeam = 2;
            selectProductSubView.setSelectedTeam(selectedTeam-1);
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
            cbMasterEmployee2.setClearButtonVisible(true);
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
            addItemsToBowlGrid(selectedWorkOrder.getWorkOrderHeaderList().get(2).getBowlEntityList());
            selectedTeam = 3;
            selectProductSubView.setSelectedTeam(selectedTeam-1);
            horizontalLayout1.setClassName("");
            horizontalLayout2.setClassName("");
            horizontalLayout3.setClassName("selected");
            horizontalLayout4.setClassName("");
        });

        cbExtraEmployees3.setWidth("100%");
        Optional<List<Employee>> employees3 = employeeService.getAll();
        if(employees3.isPresent()){
            cbMasterEmployee3.setPlaceholder("Team 3");
            cbMasterEmployee3.setClearButtonVisible(true);
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
            addItemsToBowlGrid(selectedWorkOrder.getWorkOrderHeaderList().get(3).getBowlEntityList());
            selectedTeam = 4;
            selectProductSubView.setSelectedTeam(selectedTeam-1);
            horizontalLayout1.setClassName("");
            horizontalLayout2.setClassName("");
            horizontalLayout3.setClassName("");
            horizontalLayout4.setClassName("selected");
        });

        cbExtraEmployees4.setWidth("100%");
        Optional<List<Employee>> employees4 = employeeService.getAll();
        if(employees4.isPresent()){
            cbMasterEmployee4.setPlaceholder("Team 4");
            cbMasterEmployee4.setClearButtonVisible(true);
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

    private void addItemsToBowlGrid(List<BowlEntity> bowlEntityList) {
        if((bowlEntityList != null) && (bowlEntityList.size() > 0)){
            selectedBowlEntities = bowlEntityList;
            bowlGrid.setItems(selectedBowlEntities);
        }
        else{
            List<BowlEntity> emptyList = new ArrayList<>();
            BowlEntity bowlEntity = new BowlEntity();
            bowlEntity.setChassisNumber("");
            bowlEntity.setWorkhours(1);
            bowlEntity.setBBowlRemoved(false);
            bowlEntity.setBBowlReplaced(false);
            bowlEntity.setBowlRemovedNumber("");
            bowlEntity.setBowlReplacedNumber("");
            bowlEntity.setWorkDate(LocalDate.now());
            emptyList.add(bowlEntity);
            selectedBowlEntities = emptyList;
            bowlGrid.setItems(selectedBowlEntities);
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
            addItemsToBowlGrid(selectedWorkOrder.getWorkOrderHeaderList().get(0).getBowlEntityList());
            selectedTeam = 1;
            selectProductSubView.setSelectedTeam(selectedTeam-1);
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
            addItemsToBowlGrid(selectedWorkOrder.getWorkOrderHeaderList().get(1).getBowlEntityList());
            selectedTeam = 2;
            selectProductSubView.setSelectedTeam(selectedTeam-1);
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
            addItemsToBowlGrid(selectedWorkOrder.getWorkOrderHeaderList().get(2).getBowlEntityList());
            selectedTeam = 3;
            selectProductSubView.setSelectedTeam(selectedTeam-1);
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
            addItemsToBowlGrid(selectedWorkOrder.getWorkOrderHeaderList().get(3).getBowlEntityList());
            selectedTeam = 4;
            selectProductSubView.setSelectedTeam(selectedTeam-1);
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
        verticalLayout.add(getDatePicker());
        verticalLayout.add(getLocationComboBox());
        verticalLayout.add(getTypeComboBox());
        return verticalLayout;
    }

    private HorizontalLayout getMainButtons() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidth("100%");
        horizontalLayout.setSpacing(true);

        Button addProductScaleButton = new Button("LINKS");
        Button seeWorkOrderScaleButton = new Button("RECHTS");

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

    private DatePicker getDatePicker() {
        datePicker.setLocale(Locale.FRENCH);
        datePicker.setValue(LocalDate.now());
        datePicker.setWidthFull();
        datePicker.addValueChangeListener(event -> {
            buddyTab.getSelectedTab().setLabel(event.getValue().format(DateTimeFormatter.ofPattern("dd/MM")));
        });
        return datePicker;
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
        addressComboBox.setItemLabelGenerator(address -> {
            if(address.getCustomerName() != null){
                return address.getCustomerName() + " / " + address.getAddressName();
            }
            else{
                return address.getAddressName();
            }
        });
        addressComboBox.setWidthFull();
        return addressComboBox;
    }

    private ComponentRenderer<Component, Tools> toolCardRenderer = new ComponentRenderer<>(
            tool -> {
                HorizontalLayout cardLayout = new HorizontalLayout();
                cardLayout.setMargin(false);

                Avatar avatar = new Avatar(tool.getDiscription());
                Button avatarButton = new Button(avatar);
                avatarButton.getStyle()
                        .set("background", "transparent")
                        .set("border", "none")
                        .set("color", "inherit") // optioneel: behoudt tekstkleur van parent
                        .set("box-shadow", "none"); // optioneel: verwijdert hover-shadow
                avatar.setHeight("32px");
                avatar.setWidth("32px");
                avatar.setAbbreviation(" ");
                if(tool.getbSelected()){
                    avatar.setColorIndex(5);
                }
                VerticalLayout infoLayout = new VerticalLayout();
                infoLayout.setSpacing(false);
                infoLayout.setPadding(false);
                infoLayout.getElement().appendChild(
                        ElementFactory.createStrong(tool.getDiscription()));

                optionDialog = new Dialog();
                optionDialog.setWidth("35%");
                optionDialog.setHeight("35%");

                avatarButton.addClickListener(e -> {
                    optionDialog.removeAll();
                    if((tool.equals(Tools.PERSONENKOOI))||
                    (tool.equals(Tools.LINTZAAGMACHINE))||
                    (tool.equals(Tools.SCHAARLIFT_JLG_KLEIN))||
                    (tool.equals(Tools.SCHAARLIFT_JLG_GROOT))){
                        toolsOkSubView.setSelectedTool(tool);
                        toolsOkSubView.setSelectedProducts(selectedWorkOrder.getProductList());
                        optionDialog.add(toolsOkSubView);
                    }
                    if((tool.equals(Tools.GRIJPBAK))||
                            (tool.equals(Tools.BREEKHAMER17TON))||
                            (tool.equals(Tools.BREEKHAMER60TON))){
                        toolsRegularIntenseView.setSelectedTool(tool);
                        toolsRegularIntenseView.setSelectedProducts(selectedWorkOrder.getProductList());
                        optionDialog.add(toolsRegularIntenseView);
                    }
                    if((tool.equals(Tools.RUPSSCHAARLIFT))||
                            (tool.equals(Tools.GRAAFKRAAN_1700))){
                        toolsRegularIntenseFuelView.setSelectedTool(tool);
                        toolsRegularIntenseFuelView.setSelectedProducts(selectedWorkOrder.getProductList());
                        optionDialog.add(toolsRegularIntenseFuelView);
                    }
                    if((tool.equals(Tools.SPINHOOGTEWERKER))||
                            (tool.equals(Tools.GRAAFKRAAN_6000))||
                            (tool.equals(Tools.GRAAFKRAAN14000))||
                            (tool.equals(Tools.STAMPER_TRILPLAAT_65))||
                            (tool.equals(Tools.STAMPER_TRILPLAAT_85))||
                            (tool.equals(Tools.STAMPER_TRILPLAAT_500))){
                        toolsFuelView.setSelectedTool(tool);
                        toolsFuelView.setSelectedProducts(selectedWorkOrder.getProductList());
                        optionDialog.add(toolsFuelView);
                    }
                    if((tool.equals(Tools.BROMMER))||
                            (tool.equals(Tools.BETONZAAGMACHINE))){
                        toolsThicknessMeterView.setSelectedTool(tool);
                        toolsThicknessMeterView.setSelectedProducts(selectedWorkOrder.getProductList());
                        optionDialog.add(toolsThicknessMeterView);
                    }
                    if(
                        (tool.equals(Tools.LASEREN))||
                        (tool.equals(Tools.PLOOIEN))||
                        (tool.equals(Tools.LASEREN_EN_PLOOIEN))||
                        (tool.equals(Tools.CNC_DRAAIEN_EN_FREZEN))||
                        (tool.equals(Tools.CNC_SNIJDEN))){
                        toolsWorkhoursView.setSelectedTool(tool);
                        toolsWorkhoursView.setSelectedProducts(selectedWorkOrder.getProductList());
                        optionDialog.add(toolsWorkhoursView);
                    }
                    if((tool.equals(Tools.BALANCEREN_KLEINE_ONDERDELEN)||
                            (tool.equals(Tools.GROTE_DRAAIBANK))||
                            (tool.equals(Tools.KLEINE_DRAAIBANK)))){
                        toolsFixedPriceView.setSelectedTool(tool);
                        toolsFixedPriceView.setSelectedProducts(selectedWorkOrder.getProductList());
                        optionDialog.add(toolsFixedPriceView);
                    }
                    if((tool.equals(Tools.OPLASSEN_PTA))){
                        toolsPTAView.setSelectedTool(tool);
                        toolsPTAView.setSelectedProducts(selectedWorkOrder.getProductList());
                        optionDialog.add(toolsPTAView);
                    }
                    if((tool.equals(Tools.SPIEBAAN_DUWEN))){
                        toolsSpyLaneView.setSelectedTool(tool);
                        toolsSpyLaneView.setSelectedProducts(selectedWorkOrder.getProductList());
                        optionDialog.add(toolsSpyLaneView);
                    }
                    optionDialog.open();
                });

                cardLayout.add(avatarButton, infoLayout);
                return cardLayout;
            });

    private void setSelectedWorkOrder(WorkOrder workOrder) {
        selectedWorkOrder = workOrder;
        workOrderBinder.readBean(selectedWorkOrder);
        workOrderHeaderBinder.readBean(selectedWorkOrder.getWorkOrderHeaderList().get(0));
        addItemsToWorkTimeGrid(selectedWorkOrder.getWorkOrderHeaderList().get(0).getWorkOrderTimeList());
        addItemsToBowlGrid(selectedWorkOrder.getWorkOrderHeaderList().get(0).getBowlEntityList());
        updateGetImageButton();
        selectProductSubView.setSelectedProductList(selectedWorkOrder.getProductList());
        selectProductSubView.setSelectedTeam(selectedTeam-1);
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
        workOrderBinder.validate();
        workOrderHeaderBinder.validate();
        addItemsToWorkTimeGrid(selectedWorkOrder.getWorkOrderHeaderList().get(0).getWorkOrderTimeList());
        addItemsToBowlGrid(selectedWorkOrder.getWorkOrderHeaderList().get(0).getBowlEntityList());
        selectProductSubView.setSelectedProductList(selectedWorkOrder.getProductList());
        addTabButton.setEnabled(false);
    }

    private void createNewLinkedWorkOrder(WorkOrder starterWorkOrder) throws ValidationException {
        WorkOrder newWorkOrder = new WorkOrder();
        newWorkOrder.setStarter(false);
        newWorkOrder.setWorkDateTime(LocalDateTime.now());
        newWorkOrder.setWorkOrderStatus(WorkOrderStatus.RUNNING);
        newWorkOrder.setWorkAddress(selectedWorkOrder.getWorkAddress());
        //newWorkOrder.setWorkLocation(selectedWorkOrder.getWorkLocation());
        //newWorkOrder.setWorkType(selectedWorkOrder.getWorkType());
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
        product1.setId("99999999");
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
        workOrderBinder.validate();
        workOrderHeaderBinder.validate();
        addItemsToWorkTimeGrid(selectedWorkOrder.getWorkOrderHeaderList().get(0).getWorkOrderTimeList());
        addItemsToBowlGrid(selectedWorkOrder.getWorkOrderHeaderList().get(0).getBowlEntityList());
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
                    selectProductSubView.setSelectedProductList(selectedWorkOrder.getProductList());
                    selectProductSubView.getSelectedProductGrid().getDataProvider().refreshAll();
                    if(event.getMessage().matches("Product verwijderd")){
                        Notification notification = Notification.show("Artikel is verwijderd.");
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                    else{
                        Notification notification = Notification.show("Artikel is toegevoegd.");
                        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    }

                }
                catch (ValidationException e) {
                    Notification notification = Notification.show("Gelieve eerst de verplichte velden in te vullen aub.");
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    selectProductSubView.getSelectedProductList().remove(selectProductSubView.getSelectedProductList().get(selectProductSubView.getSelectedProductList().size() - 1));
                    selectProductSubView.getSelectedProductGrid().getDataProvider().refreshAll();
                }
            });
        });
    }


    @Override
    public void setParameter(BeforeEvent beforeEvent,@OptionalParameter String s) {
        if(s != null){

            //Open WorkOrder by an other page and search WorkOrder by linkParameter.
            //So for every page with CurrentWorkOrderSubView in it that will open a clicked item in WorkOrderView
            linkParameter = s;
                List<WorkOrder> workOrderListByStarterId = workOrderService.getWorkOrderListByStarterId(s);
                if((workOrderListByStarterId !=  null) && (workOrderListByStarterId.size() > 0)){
                    selectedCoupledWorkOrders = workOrderListByStarterId;

                    buddyTab.removeAll();
                    //add Parent Tab
                    Tab parentTab = new Tab();
                    WorkOrder partentWorkOrder = selectedCoupledWorkOrders.stream().filter(item -> item.getStarter()).findFirst().get();
                    parentTab.setLabel(partentWorkOrder.getWorkDateTime().format(DateTimeFormatter.ofPattern("dd/MM")));
                    parentTab.getElement().setProperty("workOrderId", partentWorkOrder.getId());
                    buddyTab.add(parentTab);
                    setSelectedWorkOrder(partentWorkOrder);
                    buddyTab.setSelectedTab(parentTab);

                    //Try to add ChildrenTab
                    selectedCoupledWorkOrders.stream().filter(item -> item.getStarter() == false).forEach(item -> {
                        Tab childTab = new Tab();
                        childTab.setLabel(item.getWorkDateTime().format(DateTimeFormatter.ofPattern("dd/MM")));
                        childTab.getElement().setProperty("workOrderId", item.getId());
                        buddyTab.add(childTab);
                    });

                    buddyTab.addSelectedChangeListener(event -> {
                        if(event.isFromClient()){
                            Tab selectedTab = buddyTab.getSelectedTab();
                            setSelectedWorkOrder(workOrderService.getWorkOrderById(selectedTab.getElement().getProperty("workOrderId")).get());
                        }
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

