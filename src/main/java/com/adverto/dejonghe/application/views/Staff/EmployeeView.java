package com.adverto.dejonghe.application.views.Staff;

import com.adverto.dejonghe.application.dbservices.EmployeeService;
import com.adverto.dejonghe.application.entities.employee.Employee;
import com.adverto.dejonghe.application.repos.EmployeeRepo;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY_INLINE;

@PageTitle("Techniekers")
@Route("employee")
@Menu(order = 0, icon = LineAwesomeIconUrl.WRENCH_SOLID)
public class EmployeeView extends Div implements BeforeEnterObserver {

    EmployeeRepo employeeRepo;
    EmployeeService employeeService;

    private Binder<Employee> employeeBinder;

    private final Grid<Employee> grid = new Grid<>(Employee.class, false);
    Button bNewEmployee = new Button("Nieuwe Technieker");
    private final Button save = new Button("Bewaar");
    Notification deleteEmployeeNotification;

    private List<Employee>employeesForGrid;
    private Employee selectedEmployee;

    TextField tfFilter;
    TextField tfId;
    TextField tfFirstName;
    TextField tfLastName;
    TextField tfAbbriviation;
    TextField tfPhone;
    TextField tfAlertMesssage;
    DatePicker dtDateOfBirth;
    DatePicker dtDateOfService;
    Checkbox checkbAlert;

    public EmployeeView(EmployeeService employeeService,
                        EmployeeRepo employeeRepo) {

        this.employeeService = employeeService;
        this.employeeRepo = employeeRepo;

        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSplitterPosition(75);
        splitLayout.setHeight("100%");
        createReportError();
        setUpGrid();
        addDataToGrid();
        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);
        this.setHeight("100%");
        add(splitLayout);
        setUpBinders();

        bNewEmployee.addClickListener(e -> {
            Employee employee = new Employee();
            employee.setId(LocalDateTime.now().toString());
            employee.setFirstName("");
            employee.setLastName("");
            employee.setAbbreviation("");
            employee.setPhoneNumber("");
            employee.setAlertMessage("");
            employee.setAlert(false);
            populateForm(employee);
            employeeBinder.readBean(employee);
        });

        save.addClickListener(e -> {
            try {
                if (selectedEmployee == null) {
                    selectedEmployee = new Employee();
                }
                employeeBinder.writeBean(selectedEmployee);
                employeeService.save(selectedEmployee);
                //grid.select(grid.getSelectedItems().stream().findFirst().get());
                addDataToGrid();
                //only clear the id tf -> for adding products faster (that are simelar)
                //tfId.setValue(LocalDateTime.now().toString());
                Notification.show("Data updated");
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });

    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        tfFilter = new TextField("Filter");
        tfId = new TextField("Id");
        tfId.setEnabled(false);
        tfFirstName = new TextField("Voornaam");
        tfLastName = new TextField("Familienaam");
        tfAbbriviation = new TextField("Afkorting");
        tfPhone = new TextField("Telefoonnummer");
        tfAlertMesssage = new TextField("Alarmboodschap");
        checkbAlert = new Checkbox("Alarm");
        dtDateOfBirth = new DatePicker("Geboortedatum");
        dtDateOfService = new DatePicker("Gestart op");
        formLayout.add(
                tfFilter,
                tfId,
                tfFirstName,
                tfLastName,
                tfAbbriviation,
                tfPhone,
                dtDateOfBirth,
                dtDateOfService,
                tfAlertMesssage,
                checkbAlert);
        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout1 = new HorizontalLayout();
        buttonLayout1.setClassName("button-layout");
        bNewEmployee.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout1.add(save, bNewEmployee);
        editorLayoutDiv.add(buttonLayout1);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void setUpGrid() {
        // Configure Grid
        //grid.addColumn("id").setAutoWidth(true);
        grid.setHeightFull();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addColumn("firstName").setAutoWidth(true);
        grid.addColumn("lastName").setAutoWidth(true);
        grid.addColumn("abbreviation").setAutoWidth(true);
        LitRenderer<Employee> importantRenderer = LitRenderer.<Employee>of(
                        "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", employee -> employee.getAlert() ? "check" : "minus").withProperty("color",
                        employee -> employee.getAlert()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");
        grid.addColumn(importantRenderer).setHeader("Alarm").setAutoWidth(true);
        grid.addColumn("alertMessage").setAutoWidth(true);

        grid.addComponentColumn(item -> {
            Button closeButton = new Button(new Icon(VaadinIcon.TRASH));
            closeButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            closeButton.addClickListener(event -> {
                Notification.show("Openen verificatie");
                selectedEmployee = item;
                deleteEmployeeNotification.open();
            });
            return closeButton;
        });
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                selectedEmployee = event.getValue();
                populateForm(selectedEmployee);
            }
            else {
                // clearForm();
            }
        });
    }
    public Notification createReportError() {
        deleteEmployeeNotification = new Notification();
        deleteEmployeeNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);

        Icon icon = VaadinIcon.WARNING.create();
        Button retryBtn = new Button("Annuleer",
                clickEvent -> deleteEmployeeNotification.close());
        retryBtn.getStyle().setMargin("0 0 0 var(--lumo-space-l)");

        var layout = new HorizontalLayout(icon,
                new Text("Ben je zeker dat je deze technieker wil wissen?"), retryBtn,
                createCloseBtn(deleteEmployeeNotification));
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        deleteEmployeeNotification.add(layout);

        return deleteEmployeeNotification;
    }

    public Button createCloseBtn(Notification notification) {
        Button closeBtn = new Button(VaadinIcon.TRASH.create(),
                clickEvent -> {
                    if(employeesForGrid != null){
                        //when filter is selected
                        employeesForGrid.remove(selectedEmployee);
                        grid.getDataProvider().refreshAll();
                        employeeService.delete(selectedEmployee);
                        Notification.show("Klant is verwijderd");
                    }
                    else{
                        //when filter is not touched
                        employeeService.delete(selectedEmployee);
                        addDataToGrid();
                        Notification.show("Klant is verwijderd");
                    }
                    notification.close();
                });
        closeBtn.addThemeVariants(LUMO_TERTIARY_INLINE);

        return closeBtn;
    }

    private void addDataToGrid() {
        grid.setItems(q -> employeeRepo.findAll(VaadinSpringDataHelpers.toSpringPageRequest(q)).stream());
    }

    private void populateForm(Employee value) {
        //selectedCustomer = value;
        employeeBinder.readBean(selectedEmployee);
    }

    private void setUpBinders() {
        // Configure Form
        employeeBinder = new Binder<>(Employee.class);

        employeeBinder.forField(tfId)
                .withNullRepresentation("")
                .bind(x -> x.getId(), (x,y)-> x.setId(y));
        employeeBinder.forField(tfFirstName)
                .withNullRepresentation("")
                .bind(Employee::getFirstName, Employee::setFirstName);
        employeeBinder.forField(tfLastName)
                .withNullRepresentation("")
                .bind(Employee::getLastName, Employee::setLastName);
        employeeBinder.forField(tfAbbriviation)
                .withNullRepresentation("")
                .bind(Employee::getAbbreviation, Employee::setAbbreviation);
        employeeBinder.forField(tfPhone)
                .withNullRepresentation("")
                .bind(Employee::getPhoneNumber, Employee::setPhoneNumber);
        employeeBinder.forField(checkbAlert)
                .bind(Employee::getAlert, Employee::setAlert);
        employeeBinder.forField(tfAlertMesssage)
                .withNullRepresentation("")
                .bind(Employee::getAlertMessage, Employee::setAlertMessage);
        employeeBinder.forField(dtDateOfBirth)
                .withNullRepresentation(LocalDate.now())
                .bind(Employee::getBirthDate, Employee::setBirthDate);
        employeeBinder.forField(dtDateOfService)
                .withNullRepresentation(LocalDate.now())
                .bind(Employee::getDateOfService, Employee::setDateOfService);

        employeeBinder.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                try {
                    employeeBinder.writeBean(selectedEmployee);
                } catch (ValidationException e) {
                    throw new RuntimeException(e);
                }
                grid.getDataProvider().refreshItem(selectedEmployee);
            }
        });
    }


    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {

    }
}
