package com.adverto.dejonghe.application.views.Suppliers;

import com.adverto.dejonghe.application.dbservices.SupplierService;
import com.adverto.dejonghe.application.entities.customers.Customer;
import com.adverto.dejonghe.application.entities.product.product.Supplier;
import com.adverto.dejonghe.application.repos.SupplierRepo;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
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

import java.time.LocalDateTime;
import java.util.List;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY_INLINE;

@PageTitle("Leveranciers")
@Route("suppliers")
@Menu(order = 0, icon = LineAwesomeIconUrl.TRUCK_LOADING_SOLID)
public class SupplierView extends Div implements BeforeEnterObserver {

    SupplierRepo supplierRepo;
    SupplierService supplierService;

    private Binder<Supplier> supplierBinder;

    private final Grid<Supplier> grid = new Grid<>(Supplier.class, false);
    Button bNewSupplier = new Button("Nieuwe Leverancier");
    private final Button save = new Button("Bewaar");
    Notification deleteSupplierNotification;

    private List<Supplier>suppliersForGrid;
    private Supplier selectedSupplier;

    TextField tfFilter;
    TextField tfId;
    TextField tfName;
    TextField tfVat;
    TextField tfComment;
    TextField tfAlertMesssage;
    Checkbox checkbAlert;
    TextField tfStreet;
    TextField tfZip;
    TextField tfCity;
    TextField tfCountry;

    public SupplierView(SupplierService supplierService,
                        SupplierRepo supplierRepo) {

        this.supplierService = supplierService;
        this.supplierRepo = supplierRepo;

        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSplitterPosition(75);
        createReportError();
        setUpGrid();
        addDataToGrid();
        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);
        add(splitLayout);
        setUpBinders();

        bNewSupplier.addClickListener(e -> {
            Supplier supplier = new Supplier();
            supplier.setId(LocalDateTime.now().toString());
            supplier.setName("");
            supplier.setStreet("");
            supplier.setCity("");
            supplier.setCountry("");
            supplier.setVatNumber("");
            supplier.setComment("");
            supplier.setAlertMessage("");
            supplier.setAlert(false);
            populateForm(supplier);
            supplierBinder.readBean(supplier);
        });

        save.addClickListener(e -> {
            try {
                if (selectedSupplier == null) {
                    selectedSupplier = new Supplier();
                }
                supplierBinder.writeBean(selectedSupplier);
                supplierService.save(selectedSupplier);
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
        tfName = new TextField("Naam");
        tfVat = new TextField("BTW- nummer");
        tfComment = new TextField("Commentaar");
        tfAlertMesssage = new TextField("Alarmboodschap");
        checkbAlert = new Checkbox("Alarm");
        tfStreet = new TextField("Straat nr");
        tfZip = new TextField("Postcode");
        tfCity = new TextField("Stad");
        tfCountry = new TextField("Land");
        formLayout.add(
                tfFilter,
                tfId,
                tfName,
                tfVat,
                tfComment,
                tfAlertMesssage,
                checkbAlert,
                tfStreet,
                tfZip,
                tfCity,
                tfCountry);
        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout1 = new HorizontalLayout();
        buttonLayout1.setClassName("button-layout");
        bNewSupplier.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout1.add(save, bNewSupplier);
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
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addColumn("name").setAutoWidth(true);
        grid.addColumn("street").setAutoWidth(true);
        grid.addColumn("zipCode").setAutoWidth(true);
        grid.addColumn("city").setAutoWidth(true);
        grid.addColumn("country").setAutoWidth(true);
        grid.addColumn("vatNumber").setAutoWidth(true);
        grid.addColumn("comment").setAutoWidth(true);
        LitRenderer<Supplier> importantRenderer = LitRenderer.<Supplier>of(
                        "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", supplier -> supplier.getAlert() ? "check" : "minus").withProperty("color",
                        supplier -> supplier.getAlert()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");
        grid.addColumn(importantRenderer).setHeader("Alarm").setAutoWidth(true);
        grid.addColumn("alertMessage").setAutoWidth(true);

        grid.addComponentColumn(item -> {
            Button closeButton = new Button(new Icon(VaadinIcon.TRASH));
            closeButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            closeButton.addClickListener(event -> {
                Notification.show("Openen verificatie");
                selectedSupplier = item;
                deleteSupplierNotification.open();
            });
            return closeButton;
        });
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                selectedSupplier = event.getValue();
                populateForm(selectedSupplier);
            }
            else {
                // clearForm();
            }
        });
    }
    public Notification createReportError() {
        deleteSupplierNotification = new Notification();
        deleteSupplierNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);

        Icon icon = VaadinIcon.WARNING.create();
        Button retryBtn = new Button("Annuleer",
                clickEvent -> deleteSupplierNotification.close());
        retryBtn.getStyle().setMargin("0 0 0 var(--lumo-space-l)");

        var layout = new HorizontalLayout(icon,
                new Text("Ben je zeker dat je deze klant wil wissen?"), retryBtn,
                createCloseBtn(deleteSupplierNotification));
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        deleteSupplierNotification.add(layout);

        return deleteSupplierNotification;
    }

    public Button createCloseBtn(Notification notification) {
        Button closeBtn = new Button(VaadinIcon.TRASH.create(),
                clickEvent -> {
                    if(suppliersForGrid != null){
                        //when filter is selected
                        suppliersForGrid.remove(selectedSupplier);
                        grid.getDataProvider().refreshAll();
                        supplierService.delete(selectedSupplier);
                        Notification.show("Klant is verwijderd");
                    }
                    else{
                        //when filter is not touched
                        supplierService.delete(selectedSupplier);
                        addDataToGrid();
                        Notification.show("Klant is verwijderd");
                    }
                    notification.close();
                });
        closeBtn.addThemeVariants(LUMO_TERTIARY_INLINE);

        return closeBtn;
    }

    private void addDataToGrid() {
        grid.setItems(q -> supplierRepo.findAll(VaadinSpringDataHelpers.toSpringPageRequest(q)).stream());
    }

    private void populateForm(Supplier value) {
        //selectedCustomer = value;
        supplierBinder.readBean(selectedSupplier);
    }

    private void setUpBinders() {
        // Configure Form
        supplierBinder = new Binder<>(Supplier.class);

        supplierBinder.forField(tfId)
                .withNullRepresentation("")
                .bind(x -> x.getId(), (x,y)-> x.setId(y));
        supplierBinder.forField(tfName)
                .withNullRepresentation("")
                .bind(Supplier::getName, Supplier::setName);
        supplierBinder.forField(tfStreet)
                .withNullRepresentation("")
                .bind(Supplier::getStreet, Supplier::setStreet);
        supplierBinder.forField(tfZip)
                .withNullRepresentation("")
                .bind(Supplier::getZipCode, Supplier::setZipCode);
        supplierBinder.forField(tfCity)
                .withNullRepresentation("")
                .bind(Supplier::getCity, Supplier::setCity);
        supplierBinder.forField(tfCountry)
                .withNullRepresentation("")
                .bind(Supplier::getCountry, Supplier::setCountry);
        supplierBinder.forField(tfVat)
                .withNullRepresentation("")
                .bind(Supplier::getVatNumber, Supplier::setVatNumber);
        supplierBinder.forField(tfComment)
                .withNullRepresentation("")
                .bind(Supplier::getComment, Supplier::setComment);
        supplierBinder.forField(checkbAlert)
                //.withNullRepresentation(false)
                .bind(Supplier::getAlert, Supplier::setAlert);
        supplierBinder.forField(tfAlertMesssage)
                .withNullRepresentation("")
                .bind(Supplier::getAlertMessage, Supplier::setAlertMessage);
        supplierBinder.addValueChangeListener(event -> {
            if (event.getValue() != null) {

                try {
                    supplierBinder.writeBean(selectedSupplier);
                } catch (ValidationException e) {
                    throw new RuntimeException(e);
                }
                grid.getDataProvider().refreshItem(selectedSupplier);
            }
        });
    }


    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {

    }
}
