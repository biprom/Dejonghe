package com.adverto.dejonghe.application.views.customers;

import com.adverto.dejonghe.application.Controllers.GoogleRestController;
import com.adverto.dejonghe.application.dbservices.CustomerService;
import com.adverto.dejonghe.application.entities.customers.*;
import com.adverto.dejonghe.application.repos.CustomerRepo;
import com.flowingcode.vaadin.addons.googlemaps.GoogleMap;
import com.flowingcode.vaadin.addons.googlemaps.LatLon;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.json.JSONException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY_INLINE;

@PageTitle("Klanten")
@Route("customers")
@Menu(order = 0, icon = LineAwesomeIconUrl.CLIPBOARD_SOLID)
public class CustomerView extends Div implements BeforeEnterObserver {

    CustomerService customerService;
    CustomerRepo customerRepo;
    GoogleRestController googleController;

    private final Grid<Customer> grid = new Grid<>(Customer.class, false);
    private Grid<Address> addressGrid = new Grid<>();
    private Grid<Contact> contactGrid = new Grid<>();

    private TextField tfFilter;
    private TextField tfId;
    private TextField tfName;
    private TextField tfVat;
    private TextField tfComment;
    private TextField tfAlertMesssage;
    private Checkbox checkbAlert;
    private Checkbox checkbAgro;
    private Checkbox checkbIndustry;
    private final Button bNewCustomer = new Button("Nieuwe klant");
    private final Button save = new Button("Bewaar");
    private final Button distanceButton = new Button("Haal afstand op");
    private final Button coordinatesButton = new Button("Haal Coordinaten op");
    GoogleMap googleMap;

    private Checkbox checkbInvoiceAddress;
    private TextField tfStreet;
    private TextField tfZip;
    private TextField tfCity;
    private TextField tfCountry;
    private TextField tfCoordinates;
    private TextField tfDistance;
    private TextArea taAddressComment;
    private TextField tfRoadTaxAtego;
    private TextField tfRoadTaxActros;
    private TextField tfRoadTaxArocs;
    private TextField tfAddressName;
    private TextField tfAddressInvoiceMail;
    private Binder<Customer> customerBinder;
    private Binder<Address>addressBinder;
    private List<Customer>customersForGrid;
    private Customer selectedCustomer;
    private List<Address> selectedAddressList;
    private List<Contact> selectedContactList;
    private Address selectedAddress;

    Notification deleteCustomerNotification;

    public CustomerView(CustomerService customerService,
                        CustomerRepo customerRepo,
                        GoogleRestController googleController) {

        this.customerService = customerService;
        this.customerRepo = customerRepo;
        this.googleController = googleController;

        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSplitterPosition(75);
        createReportError();
        setUpGoogleMap();
        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);
        add(splitLayout);
        setUpFilter();
        setUpGrid();
        addDataToGrid();
        setUpBinders();
        setUpAddressGrid();
        setUpContactGrid();

        bNewCustomer.addClickListener(e -> {
            Customer customer = new Customer();
            customer.setId(LocalDateTime.now().toString());
            customer.setName("");
            customer.setVatNumber("");
            customer.setComment("");
            customer.setAlertMessage("");
            customer.setAlert(false);
            List<Address>addressList = new ArrayList<>();
            Address address = new Address();
            addressList.add(address);
            selectedCustomer = customer;
            selectedAddress = address;
            customer.setAddresses(addressList);
            populateForm(customer);
            addressBinder.readBean(selectedAddress);
            //refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (selectedCustomer == null) {
                    selectedCustomer = new Customer();
                }
                addressBinder.writeBean(selectedAddress);
                customerBinder.writeBean(selectedCustomer);
                customerService.save(selectedCustomer);
                //grid.select(grid.getSelectedItems().stream().findFirst().get());
                //addDataToGrid();
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

        distanceButton.setIcon(new Icon(VaadinIcon.ROAD));
        distanceButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        distanceButton.addClickListener(e -> {
            if(!addressGrid.getSelectedItems().isEmpty()) {
                Address addressToLookUp = addressGrid.getSelectedItems().stream().findFirst().get();
                Optional<Double> optDistance = null;
                try {
                    optDistance = googleController.getOptDistanceforAdres(addressToLookUp);
                } catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }
                if(optDistance.isPresent()) {
                    addressToLookUp.setDistance(optDistance.get() *2);
                    customerBinder.readBean(selectedCustomer);
                    addressBinder.readBean(selectedAddress);
                    customerService.save(selectedCustomer);
                    //addressGrid.getDataProvider().refreshAll();
                }
                else{
                    Notification.show("De afstand kon niet worden gevonden");
                }
            }
            else{
                Notification.show("Gelieve eerst een adres aan te duiden");
            }
        });

        coordinatesButton.setIcon(new Icon(VaadinIcon.AIRPLANE));
        coordinatesButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        coordinatesButton.addClickListener(e -> {
            //TODO get coordinates
            if(!addressGrid.getSelectedItems().isEmpty()) {
                Address addressToChange = addressGrid.getSelectedItems().stream().findFirst().get();
                Optional<Coordinates> optCoordinates = googleController.getOptCoordinatesforAdres(addressToChange);
                if(optCoordinates.isPresent()) {
                    addressToChange.setCoordinates(optCoordinates.get());
                    customerBinder.readBean(selectedCustomer);
                    addressBinder.readBean(selectedAddress);
                    customerService.save(selectedCustomer);
                }
                else{
                    Notification.show("De coordinaten konden niet worden gevonden");
                }
            }
            else{
                Notification.show("Gelieve eerst een adres aan te duiden");
            }
        });
    }

    public Notification createReportError() {
        deleteCustomerNotification = new Notification();
        deleteCustomerNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);

        Icon icon = VaadinIcon.WARNING.create();
        Button retryBtn = new Button("Annuleer",
                clickEvent -> deleteCustomerNotification.close());
        retryBtn.getStyle().setMargin("0 0 0 var(--lumo-space-l)");

        var layout = new HorizontalLayout(icon,
                new Text("Ben je zeker dat je deze klant wil wissen?"), retryBtn,
                createCloseBtn(deleteCustomerNotification));
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        deleteCustomerNotification.add(layout);

        return deleteCustomerNotification;
    }

    public Button createCloseBtn(Notification notification) {
        Button closeBtn = new Button(VaadinIcon.TRASH.create(),
                clickEvent -> {
                    if(customersForGrid != null){
                        //when filter is selected
                        customersForGrid.remove(selectedCustomer);
                        grid.getDataProvider().refreshAll();
                        customerService.delete(selectedCustomer);
                        Notification.show("Klant is verwijderd");
                    }
                    else{
                        //when filter is not touched
                        customerService.delete(selectedCustomer);
                        addDataToGrid();
                        Notification.show("Klant is verwijderd");
                    }
            notification.close();
                });
        closeBtn.addThemeVariants(LUMO_TERTIARY_INLINE);

        return closeBtn;
    }

    private void setUpFilter() {
        tfFilter.addValueChangeListener(e -> {
            Optional<List<Customer>> optCustomer = customerService.getCustomerByNameOrVat(tfFilter.getValue());
            if(optCustomer.isPresent()) {
                customersForGrid = optCustomer.get();
                grid.setItems(customersForGrid);
            }
            else{
                Notification.show("Geen klanten gevonden");
            }
        });
    }

    private void setUpGoogleMap() {
        googleMap = new GoogleMap("AIzaSyDPXSH4nbG9IiEHq-KF1FJrLmnS2iCOAFA",null,null);
        googleMap.getElement().setAttribute("version", "3.8.1");
        googleMap.setWidth("30%");
        googleMap.setHeight("30%");
        googleMap.setMapType(GoogleMap.MapType.HYBRID);
        googleMap.setSizeFull();
        googleMap.setCenter(new LatLon(50.9747148, 3.0145726));
    }

    private void setUpContactGrid() {
        contactGrid.setWidth("100%");
        contactGrid.removeAllColumns();
        contactGrid.addComponentColumn( item -> {
            TextField tfFirstName = new TextField();
            tfFirstName.setWidth("100%");
            tfFirstName.setValue(item.getFirstName());
            tfFirstName.addValueChangeListener(e -> {
                item.setFirstName(tfFirstName.getValue());
                customerService.save(selectedCustomer);
            });
            return tfFirstName;
        }).setAutoWidth(true).setHeader("Voornaam");
        contactGrid.addComponentColumn( item -> {
            TextField tfLastName = new TextField();
            tfLastName.setWidth("100%");
            tfLastName.setValue(item.getLastName());
            tfLastName.addValueChangeListener(e -> {
                item.setLastName(tfLastName.getValue());
                customerService.save(selectedCustomer);
            });
            return tfLastName;
        }).setAutoWidth(true).setHeader("Familienaam");
        contactGrid.addComponentColumn( item -> {
            TextField tfFunction = new TextField();
            tfFunction.setWidth("100%");
            if(item.getFunction() != null){
                tfFunction.setValue(item.getFunction());
            }
            else{
                tfFunction.setValue("N/A");
            }
            tfFunction.addValueChangeListener(e -> {
                item.setFunction(tfFunction.getValue());
                customerService.save(selectedCustomer);
            });
            return tfFunction;
        }).setAutoWidth(true).setHeader("Functie");
        contactGrid.addComponentColumn( item -> {
            TextField tfEmail = new TextField();
            tfEmail.setWidth("100%");
            tfEmail.setValue(item.getEmail());
            tfEmail.addValueChangeListener(e -> {
                item.setEmail(tfEmail.getValue());
                customerService.save(selectedCustomer);
            });
            return tfEmail;
        }).setAutoWidth(true).setHeader("email");
        contactGrid.addComponentColumn( item -> {
            TextField tfPhone = new TextField();
            tfPhone.setWidth("100%");
            tfPhone.setValue(item.getPhone());
            tfPhone.addValueChangeListener(e -> {
                item.setPhone(tfPhone.getValue());
                customerService.save(selectedCustomer);
            });
            return tfPhone;
        }).setAutoWidth(true).setHeader("Telefoon");
        contactGrid.addComponentColumn( item -> {
            TextField tfCellphone = new TextField();
            tfCellphone.setWidth("100%");
            tfCellphone.setValue(item.getCellphone());
            tfCellphone.addValueChangeListener(e -> {
                item.setCellphone(tfCellphone.getValue());
                customerService.save(selectedCustomer);
            });
            return tfCellphone;
        }).setAutoWidth(true).setHeader("GSM");

        contactGrid.addComponentColumn( item -> {
            Checkbox checkbActive = new Checkbox();
            checkbActive.setWidth("100%");
            checkbActive.setValue(item.getActive());
            checkbActive.addValueChangeListener(e -> {
                item.setActive(checkbActive.getValue());
                customerService.save(selectedCustomer);
            });
            return checkbActive;
        }).setAutoWidth(true).setHeader("Actief");

        contactGrid.addComponentColumn( item -> {
            Button addButton = new Button(new Icon(VaadinIcon.PLUS));
            addButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            addButton.addClickListener(e -> {
                Contact contact = new Contact();
                contact.setActive(true);
                contact.setCellphone("N/A");
                contact.setPhone("N/A");
                contact.setEmail("N/A");
                contact.setFirstName("N/A");
                contact.setLastName("N/A");
                contact.setFunction("N/A");
                selectedContactList.add(contact);
                contactGrid.getDataProvider().refreshAll();
            });
            return addButton;
        }).setAutoWidth(true).setHeader("");

        contactGrid.addComponentColumn( item -> {
            Button addButton = new Button(new Icon(VaadinIcon.MINUS));
            addButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            addButton.addClickListener(e -> {
                selectedContactList.remove(item);
                contactGrid.getDataProvider().refreshAll();
            });
            return addButton;
        }).setAutoWidth(true).setHeader("");
    }

    private void setUpAddressGrid() {
        addressGrid.setWidth("100%");
        addressGrid.removeAllColumns();
        addressGrid.addComponentColumn(item -> {
            Checkbox checkbInvoiceAddress = new Checkbox();
            checkbInvoiceAddress.setEnabled(false);
            if(item.getInvoiceAddress() != null){
                checkbInvoiceAddress.setValue(item.getInvoiceAddress());
            }
            else{
                checkbInvoiceAddress.setValue(false);
            }
            return checkbInvoiceAddress;
        }).setAutoWidth(true).setHeader("FactAdr.");

        addressGrid.addComponentColumn(item -> {
            TextField tfName = new TextField();
            tfName.setEnabled(false);
            if(item.getAddressName() != null){
                tfName.setValue(item.getAddressName());
            }
            else{
                tfName.setValue("");
            }
            return tfName;
        }).setAutoWidth(true).setHeader("Naam");

        addressGrid.addComponentColumn(item -> {
            TextField tfStreet = new TextField();
            tfStreet.setEnabled(false);
            if(item.getStreet() != null){
                tfStreet.setValue(item.getStreet());
            }
            else{
                tfStreet.setValue("");
            }
            return tfStreet;
        }).setAutoWidth(true).setHeader("Straat nr");

        addressGrid.addComponentColumn(item -> {
            TextField tfPostcode = new TextField();
            tfPostcode.setEnabled(false);
            if(item.getZip() != null){
                tfPostcode.setValue(item.getZip());
            }
            else{
                tfPostcode.setValue("");
            }
            return tfPostcode;
        }).setAutoWidth(true).setHeader("Postcode");

        addressGrid.addComponentColumn(item -> {
            TextField tfCity = new TextField();
            tfCity.setEnabled(false);
            if(item.getCity() != null){
                tfCity.setValue(item.getCity());
            }
            else{
                tfCity.setValue("");
            }
            return tfCity;
        }).setAutoWidth(true).setHeader("Stad");

        addressGrid.addComponentColumn(item -> {
            TextField tfCountry = new TextField();
            tfCountry.setEnabled(false);
            if(item.getCountry() != null){
                tfCountry.setValue(item.getCountry());
            }
            else{
                tfCountry.setValue("");
            }
            return tfCountry;
        }).setAutoWidth(true).setHeader("Land");

        addressGrid.addComponentColumn(item -> {
            TextField tfInvoiceMail = new TextField();
            tfInvoiceMail.setEnabled(false);
            if(item.getInvoiceMail() != null){
                tfInvoiceMail.setValue(item.getInvoiceMail());
            }
            else{
                tfInvoiceMail.setValue("");
            }
            return tfInvoiceMail;
        }).setAutoWidth(true).setHeader("Facturatiemail");


//        addressGrid.addComponentColumn(item -> {
//            TextField tfComment = new TextField();
//            if(item.getComment() != null){
//                tfComment.setValue(item.getComment());
//            }
//            else{
//                tfComment.setValue("");
//            }
//            tfComment.addValueChangeListener(event -> {
//                item.setComment(event.getValue());
//                customerService.save(selectedCustomer);
//            });
//            return tfComment;
//        }).setAutoWidth(true).setHeader("Commentaar");
//
//        addressGrid.addComponentColumn(item -> {
//            TextField tfDistance = new TextField();
//            if(item.getDistance() != null){
//                tfDistance.setValue(item.getDistance().toString());
//            }
//            else{
//                tfDistance.setValue("0.0");
//            }
//            tfDistance.addValueChangeListener(event -> {
//                try{
//                    item.setDistance(Double.valueOf(event.getValue()));
//                }
//                catch (NumberFormatException exception) {
//                    Notification.show("Deze afstand kon niet worden bewaard");
//                    item.setDistance(Double.valueOf(event.getValue()));
//                }
//                customerService.save(selectedCustomer);
//            });
//            return tfDistance;
//        }).setAutoWidth(true).setHeader("Afstand");

        addressGrid.addComponentColumn(item -> {
            Button addButton = new Button(new Icon(VaadinIcon.PLUS));
            addButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            addButton.addClickListener(e -> {
                Address address = new Address();
                selectedCustomer.getAddresses().add(address);
                addressGrid.getDataProvider().refreshAll();
            });
            return addButton;
        });

        addressGrid.addComponentColumn(item -> {
            Button removeButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
            removeButton.addThemeVariants(ButtonVariant.LUMO_WARNING);
            removeButton.addClickListener(e -> {
                selectedCustomer.getAddresses().remove(item);
                addressGrid.getDataProvider().refreshAll();
            });
            return removeButton;
        });

//        addressGrid.addComponentColumn(item -> {
//            TextField tfRoadTaxAtego = new TextField();
//            if(item.getRoadTaxAtego() != null){
//                tfRoadTaxAtego.setValue(item.getRoadTaxAtego().toString());
//            }
//            else{
//                tfRoadTaxAtego.setValue("0.0");
//            }
//            tfRoadTaxAtego.addValueChangeListener(event -> {
//                try{
//                    item.setRoadTaxAtego(Double.valueOf(event.getValue()));
//                    customerService.save(selectedCustomer);
//                }
//                catch (NumberFormatException exception) {
//                    Notification.show("Deze wegentaks kon niet worden bewaard");
//                }
//            });
//            return tfRoadTaxAtego;
//        }).setAutoWidth(true).setHeader("Wegentaks Atego");
//
//        addressGrid.addComponentColumn(item -> {
//            TextField tfActros = new TextField();
//            if(item.getRoadTaxActros() != null){
//                tfActros.setValue(item.getRoadTaxActros().toString());
//            }
//            else{
//                tfActros.setValue("0.0");
//            }
//            tfActros.addValueChangeListener(event -> {
//                try{
//                    item.setRoadTaxActros(Double.valueOf(event.getValue()));
//                    customerService.save(selectedCustomer);
//                }
//                catch (NumberFormatException exception) {
//                    Notification.show("Deze wegentaks kon niet worden bewaard");
//                }
//            });
//            return tfActros;
//        }).setAutoWidth(true).setHeader("Wegentaks Actros");
//
//        addressGrid.addComponentColumn(item -> {
//            TextField tfArocs = new TextField();
//            if(item.getRoadTaxArocs() != null){
//                tfArocs.setValue(item.getRoadTaxArocs().toString());
//            }
//            else{
//                tfArocs.setValue("0.0");
//            }
//            tfArocs.addValueChangeListener(event -> {
//                try{
//                    item.setRoadTaxArocs(Double.valueOf(event.getValue()));
//                    customerService.save(selectedCustomer);
//                }
//                catch (NumberFormatException exception) {
//                    Notification.show("Deze wegentaks kon niet worden bewaard");
//                }
//            });
//            return tfArocs;
//        }).setAutoWidth(true).setHeader("Wegentaks Arocs");

        addressGrid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                selectedAddress = event.getValue();
                addressBinder.readBean(selectedAddress);
                if((selectedAddress.getContactList() != null) && (selectedAddress.getContactList().size() > 0)){
                    contactGrid.setItems(selectedAddress.getContactList());
                    selectedContactList = selectedAddress.getContactList();
                }
                else{
                    List<Contact> newContactList = getNewContactList();
                    contactGrid.setItems(newContactList);
                    selectedAddress.setContactList(newContactList);
                    selectedContactList = newContactList;
                }
            }
            else {
                //clearForm();
            }
        });
    }

    private void setUpBinders() {
        // Configure Form
        customerBinder = new Binder<>(Customer.class);

        customerBinder.forField(tfId)
                .withNullRepresentation("")
                .bind(x -> x.getId(), (x,y)-> x.setId(y));
        customerBinder.forField(tfName)
                .withNullRepresentation("")
                .bind(Customer::getName, Customer::setName);
        customerBinder.forField(tfVat)
                .withNullRepresentation("")
                .bind(Customer::getVatNumber, Customer::setVatNumber);
        customerBinder.forField(tfComment)
                .withNullRepresentation("")
                .bind(Customer::getComment, Customer::setComment);
        customerBinder.forField(checkbAlert)
                //.withNullRepresentation(false)
                .bind(Customer::getAlert, Customer::setAlert);
        customerBinder.forField(checkbAgro)
                //.withNullRepresentation(false)
                .bind(Customer::getBAgro, Customer::setBAgro);
        customerBinder.forField(checkbIndustry)
                //.withNullRepresentation(false)
                .bind(Customer::getBIndustry, Customer::setBIndustry);
        customerBinder.forField(tfAlertMesssage)
                .withNullRepresentation("")
                .bind(Customer::getAlertMessage, Customer::setAlertMessage);
        customerBinder.addValueChangeListener(event -> {
            if (event.getValue() != null) {

                try {
                    customerBinder.writeBean(selectedCustomer);
                } catch (ValidationException e) {
                    throw new RuntimeException(e);
                }
                grid.getDataProvider().refreshItem(selectedCustomer);
            }
        });

        addressBinder = new Binder<>(Address.class);

        addressBinder.forField(checkbInvoiceAddress)
                .withNullRepresentation(false)
                        .bind(x -> x.getInvoiceAddress(),(x,y)-> x.setInvoiceAddress(y));
        addressBinder.forField(tfStreet)
                .withNullRepresentation("")
                .bind(x -> x.getStreet(), (x,y)-> x.setStreet(y));
        addressBinder.forField(tfZip)
                .withNullRepresentation("")
                .bind(Address::getZip, Address::setZip);
        addressBinder.forField(tfCity)
                .withNullRepresentation("")
                .bind(Address::getCity, Address::setCity);
        addressBinder.forField(tfCountry)
                .withNullRepresentation("")
                .bind(Address::getCountry, Address::setCountry);
        addressBinder.forField(taAddressComment)
                .withNullRepresentation("")
                .bind(Address::getComment, Address::setComment);
        addressBinder.forField(tfDistance)
                .withConverter(
                        new StringToDoubleConverter("Gelieve een getal in te vullen aub")
                )
                .withNullRepresentation(0.0)
                .bind(Address::getDistance, Address::setDistance);
        addressBinder.forField(tfCoordinates)
                .bindReadOnly(item -> {
                    if(item.getCoordinates() != null){
                        return item.getCoordinates().getLatitude() + " " + item.getCoordinates().getLongitude();
                    }
                    else{
                        return "Geen Coordinaten";
                    }
                });
        addressBinder.forField(tfRoadTaxActros)
                .withConverter(
                        new StringToDoubleConverter("Gelieve een getal in te vullen aub")
                )
                .withNullRepresentation(0.0)
                .bind(Address::getRoadTaxActros, Address::setRoadTaxActros);
        addressBinder.forField(tfRoadTaxAtego)
                .withConverter(
                        new StringToDoubleConverter("Gelieve een getal in te vullen aub")
                )
                .withNullRepresentation(0.0)
                .bind(Address::getRoadTaxAtego, Address::setRoadTaxAtego);
        addressBinder.forField(tfRoadTaxArocs)
                .withConverter(
                        new StringToDoubleConverter("Gelieve een getal in te vullen aub")
                )
                .withNullRepresentation(0.0)
                .bind(Address::getRoadTaxArocs, Address::setRoadTaxArocs);

        addressBinder.forField(tfAddressName)
                .withNullRepresentation("")
                .bind(Address::getAddressName, Address::setAddressName);

        addressBinder.forField(tfAddressInvoiceMail)
                .withNullRepresentation("")
                .bind(Address::getInvoiceMail, Address::setInvoiceMail);

        addressBinder.addValueChangeListener(event -> {
            try {
                addressBinder.writeBean(selectedAddress);
            } catch (ValidationException e) {
                Notification.show("Het geselecteerd adres kan niet worden bewaard" + e.getMessage());
            }
        });
        addressBinder.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                try {
                    addressBinder.writeBean(selectedAddress);
                    addressGrid.getDataProvider().refreshAll();
                } catch (ValidationException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void addDataToGrid() {
        grid.setItems(q -> customerRepo.findAll(VaadinSpringDataHelpers.toSpringPageRequest(q)).stream());
    }

    private void setUpGrid() {
        // Configure Grid
        //grid.addColumn("id").setAutoWidth(true);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addColumn("name").setAutoWidth(true);
        grid.addColumn("vatNumber").setAutoWidth(true);
        grid.addColumn("comment").setAutoWidth(true);
        LitRenderer<Customer> importantRenderer = LitRenderer.<Customer>of(
                        "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", customer -> customer.getAlert() ? "check" : "minus").withProperty("color",
                        customer -> customer.getAlert()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(importantRenderer).setHeader("Alarm").setAutoWidth(true);
        grid.addColumn("alertMessage").setAutoWidth(true);
        LitRenderer<Customer> agroRenderer = LitRenderer.<Customer>of(
                        "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", customer -> customer.getBAgro() ? "check" : "minus").withProperty("color",
                        customer -> customer.getBAgro()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");
        grid.addColumn(agroRenderer).setHeader("Agro").setAutoWidth(true);
        LitRenderer<Customer> industryRenderer = LitRenderer.<Customer>of(
                        "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", customer -> customer.getBIndustry() ? "check" : "minus").withProperty("color",
                        customer -> customer.getBIndustry()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");
        grid.addColumn(industryRenderer).setHeader("Industrie").setAutoWidth(true);
        grid.addComponentColumn(item -> {
            Button closeButton = new Button(new Icon(VaadinIcon.TRASH));
            closeButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            closeButton.addClickListener(event -> {
                Notification.show("Openen verificatie");
                selectedCustomer = item;
                deleteCustomerNotification.open();
            });
            return closeButton;
        });
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                selectedCustomer = event.getValue();
                if((selectedCustomer.getAddresses() != null) && (selectedCustomer.getAddresses().size() > 0)){
                    addressGrid.setItems(selectedCustomer.getAddresses());
                    selectedAddressList = selectedCustomer.getAddresses();
                    addressGrid.select(selectedAddressList.get(0));
                }
                else{
                    List<Address> newAddresList = getNewAddressList();
                    addressGrid.setItems(newAddresList);
                    selectedCustomer.setAddresses(newAddresList);
                    selectedAddressList = selectedCustomer.getAddresses();
                    addressGrid.select(selectedAddressList.get(0));
                }
                populateForm(selectedCustomer);
            }
            else {
               // clearForm();
            }
        });
    }

    private List<Address> getNewAddressList() {
        List<Address> addressList = new ArrayList<>();
        Address address = new Address();
        address.setCity("");
        address.setCountry("");
        address.setStreet("");
        address.setZip("");
        address.setInvoiceAddress(false);
        address.setDistance(0.0);
        Coordinates coordinates = new Coordinates();
        coordinates.setLatitude(5.75);
        coordinates.setLongitude(2.2);
        address.setCoordinates(coordinates);
        addressList.add(address);
        return addressList;
    }

    private List<Contact> getNewContactList() {
        List<Contact> contactList = new ArrayList<>();
        Contact contact = new Contact();
        contact.setActive(true);
        contact.setCellphone("N/A");
        contact.setPhone("N/A");
        contact.setEmail("N/A");
        contact.setFirstName("N/A");
        contact.setLastName("N/A");
        contactList.add(contact);
        return contactList;
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout1 = new HorizontalLayout();
        HorizontalLayout buttonLayout2 = new HorizontalLayout();
        buttonLayout1.setClassName("button-layout");
        bNewCustomer.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout1.add(save, bNewCustomer);
        buttonLayout2.add(distanceButton, coordinatesButton);
        editorLayoutDiv.add(buttonLayout2);
        editorLayoutDiv.add(buttonLayout1);
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
        HorizontalLayout checkbLayout = new HorizontalLayout();
        checkbAgro = new Checkbox("Agro");
        checkbIndustry = new Checkbox("Industrie");
        checkbInvoiceAddress = new Checkbox("Facturatie- adres");
        tfStreet = new TextField("Straat nr");
        tfZip = new TextField("Postcode");
        tfCity = new TextField("Stad");
        tfCountry = new TextField("Land");
        tfCoordinates = new TextField("Coordinaten");
        tfCoordinates.setEnabled(false);
        tfDistance = new TextField("Afstand");
        taAddressComment = new TextArea("Commentaar");
        tfRoadTaxAtego = new TextField("Wegentaks Atego");
        tfRoadTaxActros = new TextField("Wegentaks Oplegger");
        tfRoadTaxArocs = new TextField("Wegentaks Kraan");
        tfAddressName = new TextField("Naam");
        tfAddressInvoiceMail = new TextField("Facturatie- mail");

        Span searchTitle = new Span("Zoek klant");
        searchTitle.getElement().getStyle().set("font-size", "22px");
        searchTitle.getElement().getStyle().set("background-color", "yellow");

        Span customerTitle = new Span("Klantgegevens");
        customerTitle.getElement().getStyle().set("font-size", "22px");
        customerTitle.getElement().getStyle().set("background-color", "yellow");

        Span adressTitle = new Span("Adresgegevens");
        adressTitle.getElement().getStyle().set("font-size", "22px");
        adressTitle.getElement().getStyle().set("background-color", "yellow");
        checkbLayout.add(checkbAgro,checkbIndustry);
        formLayout.add(
                searchTitle,
                tfFilter,
                customerTitle,
                tfId,
                tfName,
                tfVat,
                tfComment,
                checkbLayout,
                tfAlertMesssage,
                checkbAlert,
                adressTitle,
                checkbInvoiceAddress,
                tfStreet,
                tfZip,
                tfCity,
                tfCountry,
                tfCoordinates,
                tfDistance,
                tfAddressName,
                tfAddressInvoiceMail,
                taAddressComment,
                tfRoadTaxAtego,
                tfRoadTaxActros,
                tfRoadTaxArocs);
        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        VerticalLayout mapLayout = new VerticalLayout();
        mapLayout.setHeight("100%");
        mapLayout.add(googleMap);
        wrapper.add(grid);
        wrapper.add(addressGrid);
        wrapper.add(contactGrid);
        wrapper.add(mapLayout);
    }

    private void clearForm() {
        populateForm(null);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
        addressGrid.select(null);
        addressGrid.getDataProvider().refreshAll();
    }

    private void populateForm(Customer value) {
        //selectedCustomer = value;
        customerBinder.readBean(selectedCustomer);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {

    }
}
