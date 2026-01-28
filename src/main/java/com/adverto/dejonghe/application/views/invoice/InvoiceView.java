package com.adverto.dejonghe.application.views.invoice;

import com.adverto.dejonghe.application.customEvents.AddProductEventListener;
import com.adverto.dejonghe.application.dbservices.CustomerService;
import com.adverto.dejonghe.application.dbservices.InvoiceService;
import com.adverto.dejonghe.application.dbservices.ProductService;
import com.adverto.dejonghe.application.entities.customers.Address;
import com.adverto.dejonghe.application.entities.customers.Customer;
import com.adverto.dejonghe.application.entities.enums.employee.UserFunction;
import com.adverto.dejonghe.application.entities.invoice.Invoice;
import com.adverto.dejonghe.application.services.invoice.InvoiceServices;
import com.adverto.dejonghe.application.views.subViews.SearchCustomerSubView;
import com.adverto.dejonghe.application.views.subViews.SelectProductSubView;
import com.adverto.dejonghe.application.views.subViews.ShowImageSubVieuw;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.router.*;
import jakarta.annotation.PostConstruct;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@PageTitle("FacturatieView")
@Route("facturatieview")
@Menu(order = 0, icon = LineAwesomeIconUrl.EURO_SIGN_SOLID)
public class InvoiceView extends Div implements HasUrlParameter<String> {

    private final InvoiceService invoiceService;
    ProductService productService;
    SelectProductSubView selectProductSubView;
    SearchCustomerSubView searchCustomerSubView;
    CustomerService customerService;
    GridFsTemplate gridFsTemplate;
    ShowImageSubVieuw showImageSubView;
    InvoiceServices invoiceServices;

    SplitLayout mainSplitLayout;
    SplitLayout headerSplitLayout;
    FormLayout headerFormLayout;
    Dialog searchCustomerDialog;
    Dialog finishInvoiceDialog;

    Customer selectedCustomer;
    Address selectedAddress;

    Invoice selectedInvoice;

    ComboBox<Customer> customerComboBox = new ComboBox<>();
    Card customerCard;
    Span badge;
    TextField tfInvoiceNumber;
    DatePicker invoiceDatePicker;
    DatePicker expiryDatePicker;
    //ComboBox<InvoiceStatus> invoiceStatusComboBox;
    TextArea invoiceCommentTextArea;

    MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    Upload dropEnabledUpload = new Upload(buffer);
    Button showImageButton;
    Dialog imageDialog;

    Binder<Invoice>invoiceBinder;

    Button generateInvoiceButton = new Button("Maak factuur");
    Button showPDFButton = new Button("Bekijk PDF");

    String linkParameter;

    AddProductEventListener listener;

    Checkbox checkbToCheck;
    Checkbox checkbApproved;
    Checkbox checkbRejected;

    Optional<List<Customer>> allCustomers;

    public InvoiceView(ProductService productService,
                       SelectProductSubView selectProductSubView,
                       SearchCustomerSubView searchCustomerSubView,
                       CustomerService customerService,
                       InvoiceService invoiceService,
                       GridFsTemplate gridFsTemplate,
                       ShowImageSubVieuw showImageSubView,
                       InvoiceServices invoiceServices,
                       AddProductEventListener listener) {
        this.productService = productService;
        this.selectProductSubView = selectProductSubView;
        this.searchCustomerSubView = searchCustomerSubView;
        this.customerService = customerService;
        this.invoiceService = invoiceService;
        this.gridFsTemplate = gridFsTemplate;
        this.showImageSubView = showImageSubView;
        this.invoiceServices = invoiceServices;
        this.listener = listener;

        this.setSizeFull();
        setUpShowImageButton();
        setUpSearchCustomerDialog();
        setUpMainSplitLayout();
        setUpHeaderSplitLayout();
        mainSplitLayout.addToPrimary(selectProductSubView.getLayout());
        VerticalLayout vLayout = new VerticalLayout();
        vLayout.add(selectProductSubView.getFilter());
        vLayout.add(selectProductSubView.getSelectedProductGrid());
        vLayout.setSizeFull();
        setUpHeaderFormLayout();
        headerSplitLayout.addToPrimary(headerFormLayout);
        headerSplitLayout.addToSecondary(vLayout);
        mainSplitLayout.addToSecondary(headerSplitLayout);
        this.add(mainSplitLayout);
        setUpInvoiceBinder();
        setUpUpload();
        setUpImageDialog();
        setUpFinishWorkOrderDialog();
        setUpFinishButton();
        setUpToInvoiceButton();
    }

    private void setUpToInvoiceButton() {
        showPDFButton.setWidth("50%");
        showPDFButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_WARNING);
        showPDFButton.addClickListener(e -> {
            invoiceServices.generateInvoicePDF(selectedInvoice);
        });
    }

    private void setUpFinishButton() {
        generateInvoiceButton.setWidth("50%");
        generateInvoiceButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_WARNING);
        generateInvoiceButton.addClickListener(e -> {
            finishInvoiceDialog.open();
        });
    }

    private void setUpFinishWorkOrderDialog() {
        finishInvoiceDialog = new Dialog();
        finishInvoiceDialog.setHeaderTitle("Ben je zeker dat je deze factuur wil afwerken?");

        VerticalLayout dialogLayout = createDialogLayout();
        finishInvoiceDialog.add(dialogLayout);

        Button saveButton = createSaveButton(finishInvoiceDialog);
        Button cancelButton = new Button("Niet Afwerken", e -> finishInvoiceDialog.close());
        finishInvoiceDialog.getFooter().add(cancelButton);
        finishInvoiceDialog.getFooter().add(saveButton);
    }

    private Button createSaveButton(Dialog dialog) {
        Button saveButton = new Button("Afwerken");
        saveButton.addClickListener(click -> {
            dialog.close();
            try {
                selectedInvoice.setCustomer(selectedCustomer);
                invoiceBinder.writeBean(selectedInvoice);
                selectedInvoice.setProductList(selectProductSubView.getSelectedProductList());
                selectedInvoice.setBFinalInvoice(true);
                selectedInvoice.setOpen(true);
                selectedInvoice.setFinalInvoiceNumber(invoiceServices.getNewFinalInvoiceNumber());
                invoiceService.save(selectedInvoice);
                UI.getCurrent().navigate(ProformaInvoiceView.class);
                Notification.show("Deze factuur is afgewerkt");
            } catch (ValidationException e) {
                Notification.show("Deze factuur kon niet worden afgewerkt.");
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return saveButton;
    }

    private static VerticalLayout createDialogLayout() {

        Span span = new Span("Door deze factuur af te bewaren wordt het factuurnummer toegewezen aan deze factuur.");
        VerticalLayout dialogLayout = new VerticalLayout(span);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");

        return dialogLayout;
    }

    private void setUpInvoiceBinder() {
        invoiceBinder = new Binder<>();
        invoiceBinder.forField(tfInvoiceNumber)
                .asRequired("Elke factuur moet een factuurnummer hebben!")
                .withConverter(
                        new StringToIntegerConverter("Must enter a number"))
                .bind(Invoice::getInvoiceNumber, Invoice::setInvoiceNumber);
        invoiceBinder.forField(invoiceDatePicker)
                .asRequired("Elke factuur moet een factuurdatum hebben!")
                .bind(Invoice::getInvoiceDate, Invoice::setInvoiceDate);
        invoiceBinder.forField(expiryDatePicker)
                .asRequired("Elke factuur moet een vevaldatum hebben!")
                .bind(Invoice::getExpiryDate, Invoice::setExpiryDate);
        invoiceBinder.forField(customerComboBox)
                .asRequired("Gelieve een klant te selecteren!")
                .bind(Invoice::getCustomer, Invoice::setCustomer);
//        invoiceBinder.forField(invoiceStatusComboBox)
//                .asRequired("Gelieve een status te selecteren!")
//                .bind(Invoice::getInvoiceStatus, Invoice::setInvoiceStatus);
        invoiceBinder.forField(invoiceCommentTextArea)
                .bind(Invoice::getDiscription, Invoice::setDiscription);
        invoiceBinder.forField(checkbToCheck)
                .withNullRepresentation(false)
                .bind(Invoice::getToCheck, Invoice::setToCheck);
        invoiceBinder.forField(checkbApproved)
                .withNullRepresentation(false)
                .bind(Invoice::getBApproved, Invoice::setBApproved);
        invoiceBinder.forField(checkbRejected)
                .withNullRepresentation(false)
                .bind(Invoice::getBRejected, Invoice::setBRejected);
        invoiceBinder.addValueChangeListener(workOrder -> {
            try {
                if(workOrder.isFromClient()){
                    selectedInvoice.setCustomer(selectedCustomer);
                    invoiceBinder.writeBean(selectedInvoice);
                    selectedInvoice.setProductList(selectProductSubView.getSelectedProductList());
                    invoiceService.save(selectedInvoice);
                }
            } catch (ValidationException e) {
                Notification.show("Kon de werkbon nog niet bewaren, gelieve alle velden in te vullen aub");
            }
        });
    }

    private void setUpHeaderFormLayout() {
        headerFormLayout = new FormLayout();
        headerFormLayout.setSizeFull();
        headerFormLayout.add(getFirstStepHeader());
        headerFormLayout.add(getSecondStepHeader());
        headerFormLayout.add(getThirdStepHeader(),2);
    }

    private void setUpSearchCustomerDialog() {
        searchCustomerDialog = new Dialog();
        searchCustomerDialog.add(searchCustomerSubView);
        //searchCustomerSubView.setDialog(searchCustomerDialog);
        searchCustomerDialog.setCloseOnEsc(true);
        searchCustomerDialog.setHeight("50%");
        searchCustomerDialog.setWidth("50%");
        searchCustomerDialog.addDialogCloseActionListener(event -> {
            selectedCustomer = searchCustomerSubView.getSelectedCustomer();
            selectedAddress = searchCustomerSubView.getSelectedAddress();
            searchCustomerDialog.close();
            try{
                Address invoiceAddress = selectedCustomer.getAddresses().stream().filter(filter -> filter.getInvoiceAddress()).findFirst().get();
            }
            catch (Exception e){
                Notification notification = Notification.show("Kon geen volledig facturatie- adres vinden : " + e.getMessage());
                notification.setDuration(10000);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }

        });
    }

    private VerticalLayout getFirstStepHeader() {
        VerticalLayout vLayout = new VerticalLayout();
        vLayout.setSizeFull();
        vLayout.add(getCustomerComboBox());
        vLayout.add(getCustomerCard());
        vLayout.add(getInvoiceNumber());
        vLayout.add(getInvoiceDatePicker());
        vLayout.add(getExpiryDatePicker());
        //vLayout.add(getInvoiceStatusPicker());
        return vLayout;
    }

    private VerticalLayout getSecondStepHeader() {
        VerticalLayout vLayout = new VerticalLayout();

        VerticalLayout checkVLayout = new VerticalLayout();
        VerticalLayout buttonVLayout = new VerticalLayout();

        HorizontalLayout hLayout = new HorizontalLayout();
        hLayout.setWidth("100%");

        checkVLayout.add(getToCheck());
        checkVLayout.add(getApproved());
        checkVLayout.add(getRejected());

        buttonVLayout.add(generateInvoiceButton);
        buttonVLayout.add(showPDFButton);


        hLayout.add(checkVLayout, buttonVLayout);

        vLayout.setSizeFull();
        vLayout.add(getInvoiceCommentTextArea(),hLayout);

        return vLayout;
    }

    private Checkbox getToCheck() {
        checkbToCheck = new Checkbox("TE CONTROLEREN");
        checkbToCheck.addValueChangeListener(event -> {
            checkbApproved.setValue(false);
            checkbRejected.setValue(false);
        });
        return checkbToCheck;
    }

    private Checkbox getApproved() {
        checkbApproved = new Checkbox("GOEDGEKEURD");
        checkbApproved.addValueChangeListener(value -> {
            if(value.getValue()){
                checkbToCheck.setValue(false);
                checkbRejected.setValue(false);
            }
        });
        return checkbApproved;
    }

    private Checkbox getRejected() {
        checkbRejected = new Checkbox("AFGEKEURD");
        checkbRejected.addValueChangeListener(value -> {
            if(value.getValue()){
                checkbToCheck.setValue(false);
                checkbApproved.setValue(false);
            }
        });
        return checkbRejected;
    }

    private VerticalLayout getThirdStepHeader() {
        VerticalLayout vLayout = new VerticalLayout();
        vLayout.setSizeFull();
        vLayout.add(dropEnabledUpload);
        vLayout.add(showImageButton);
        return vLayout;
    }

    private TextArea getInvoiceCommentTextArea() {
        invoiceCommentTextArea = new TextArea("Commentaar");
        invoiceCommentTextArea.setWidth("100%");
        invoiceCommentTextArea.setHeight("200px");
        return invoiceCommentTextArea;
    }

//    private ComboBox getInvoiceStatusPicker() {
//        invoiceStatusComboBox = new ComboBox<>();
//        invoiceStatusComboBox.setWidthFull();
//        invoiceStatusComboBox.setItems(InvoiceStatus.values());
//        invoiceStatusComboBox.setItemLabelGenerator(item -> item.getDiscription());
//        return invoiceStatusComboBox;
//    }

    private DatePicker getInvoiceDatePicker() {
        invoiceDatePicker = new DatePicker();
        invoiceDatePicker.setSizeFull();
        invoiceDatePicker.setLocale(Locale.FRENCH);
        invoiceDatePicker.setValue(LocalDate.now());
        invoiceDatePicker.addValueChangeListener(event -> {
            expiryDatePicker.setValue(event.getValue().plusDays(14));
        });
        return invoiceDatePicker;
    }

    private DatePicker getExpiryDatePicker() {
        expiryDatePicker = new DatePicker();
        expiryDatePicker.setSizeFull();
        expiryDatePicker.setLocale(Locale.FRENCH);
        expiryDatePicker.setValue(LocalDate.now().plusDays(14));
        return expiryDatePicker;
    }

    private Component getCustomerCard() {
        customerCard = new Card();
        badge = new Span("Geen status");
        badge.getElement().getThemeList().add("badge success");
        customerCard.addThemeVariants(CardVariant.LUMO_ELEVATED);
        customerCard.setTitle(new Div("Naam klant"));
        customerCard.setSubtitle(new Div("BTW- nummer klant"));
        customerCard.setHeaderSuffix(badge);
        customerCard.setWidthFull();
        customerCard.add("commentaar bij klant -> meestal gekoppeld aan Alert- status");
        return customerCard;
    }

    private TextField getInvoiceNumber() {
        tfInvoiceNumber = new TextField();
        tfInvoiceNumber.setWidthFull();
        tfInvoiceNumber.setValue(String.valueOf(invoiceServices.getNewProFormaInvoiceNumber()));
        return tfInvoiceNumber;
    }

    private ComboBox getCustomerComboBox() {
        allCustomers = customerService.getAllCustomers();
        if (allCustomers.isPresent()) {
            customerComboBox.setItems(allCustomers.get());
        }
        else{
            Notification.show("Geen Klanten in de database");
        }
        customerComboBox.setPlaceholder("Gelieve een klant te selecteren");
        customerComboBox.setItemLabelGenerator(address -> address.getName());
        customerComboBox.addValueChangeListener(event -> {
                customerCard.removeAll();
                customerCard.setTitle(new Div(event.getValue().getName()));
                customerCard.setSubtitle(new Div(event.getValue().getVatNumber()));
                customerCard.add(event.getValue().getComment());
                if(event.getValue().getAlert()){
                    badge.setText("Alarm");
                    badge.getElement().getThemeList().clear();
                    badge.getElement().getThemeList().add("badge error");
                }
                else{
                    badge.setText("Geen Alarm");
                    badge.getElement().getThemeList().clear();
                    badge.getElement().getThemeList().add("badge success");
                }
                customerCard.setHeaderSuffix(badge);
        });
        customerComboBox.setWidthFull();
        return customerComboBox;
    }

    private void setUpMainSplitLayout() {
        mainSplitLayout = new SplitLayout();
        mainSplitLayout.setSizeFull();
        mainSplitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);
        mainSplitLayout.setSplitterPosition(0);
    }

    private void setUpHeaderSplitLayout() {
        headerSplitLayout = new SplitLayout();
        headerSplitLayout.setSizeFull();
        headerSplitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
        headerSplitLayout.setSplitterPosition(35);
    }

    private void setUpUpload() {
        dropEnabledUpload.setVisible(false);
        dropEnabledUpload.setWidthFull();
        dropEnabledUpload.setAcceptedFileTypes("image/tiff", ".jpeg");
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
                storeImageIdToThisInvoice(gridFsTemplate.store(inputStream, fileName, "image/png", metaData).toString());
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
        showImageButton.setText("Deze factuur bevat " + selectedInvoice.getImageList().size() + " foto(s), klik hier om ze te bekijken.");
        showImageButton.removeThemeVariants(ButtonVariant.LUMO_ERROR);
        showImageButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        showImageButton.addClickListener(e -> {imageDialog.open();});
    }

    private void storeImageIdToThisInvoice(String idString) throws ValidationException {
        if(selectedInvoice.getImageList() != null) {
            selectedInvoice.getImageList().add(idString);
            selectedInvoice.setCustomer(selectedCustomer);
            invoiceBinder.writeBean(selectedInvoice);
            selectedInvoice.setProductList(selectProductSubView.getSelectedProductList());
            invoiceService.save(selectedInvoice);
        }
        else{
            List<String>imageIdList = new ArrayList<>();
            imageIdList.add(idString);
            selectedInvoice.setImageList(imageIdList);
            selectedInvoice.setCustomer(selectedCustomer);
            invoiceBinder.writeBean(selectedInvoice);
            selectedInvoice.setProductList(selectProductSubView.getSelectedProductList());
            invoiceService.save(selectedInvoice);
        }
    }

    private void setUpImageDialog() {
        imageDialog = new Dialog();
        imageDialog.setHeaderTitle("Toegevoegde foto's onder geselecteerde factuur");
        imageDialog.add(showImageSubView);
        Button cancelButton = new Button("Sluiten", e -> {
            try {
                selectedInvoice.setCustomer(selectedCustomer);
                invoiceBinder.writeBean(selectedInvoice);
                selectedInvoice.setProductList(selectProductSubView.getSelectedProductList());
                invoiceService.save(selectedInvoice);
            } catch (ValidationException ex) {
                throw new RuntimeException(ex);
            }
            invoiceService.save(selectedInvoice);
            imageDialog.close();
        });
        imageDialog.getFooter().add(cancelButton);
    }

    private void setUpShowImageButton() {
        showImageButton = new Button("Er zijn in deze werkbon geen foto's toegevoegd, gelieve altijd een foto te koppelen van de werken!");
        showImageButton.setWidth("100%");

        showImageButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        showImageButton.addClickListener(e -> {
            if((selectedInvoice.getImageList() != null) && (selectedInvoice.getImageList().size() > 0)) {
                showImageSubView.setUser(UserFunction.ADMIN);
                showImageSubView.setSelectedWorkOrder(selectedInvoice.getImageList());
            }
            else{
                Notification notification = Notification.show("Deze werkbon bevat nog geen foto's!");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });
        showImageButton.setVisible(false);
    }

    private void readNewInvoice() {
        Invoice newInvoice = new Invoice();
        newInvoice.setInvoiceDate(LocalDate.now());
        newInvoice.setBFinalInvoice(false);
        newInvoice.setInvoiceNumber(invoiceServices.getNewProFormaInvoiceNumber());
        selectedInvoice = newInvoice;
        invoiceBinder.readBean(selectedInvoice);
        customerCard.removeAll();
        customerCard.setTitle(new Div("Gelieve een klant te selecteren."));
        customerCard.setSubtitle(new Div("Om een nieuwe klant aan te maken"));
    }

    private void saveSelectedProforma() throws ValidationException {
        invoiceBinder.writeBean(selectedInvoice);
        selectedInvoice.setProductList(selectProductSubView.getSelectedProductList());
        invoiceService.save(selectedInvoice);
    }


    @PostConstruct
    private void init() {
        listener.setEventConsumer(event -> {
            // UI-thread safe update
            UI.getCurrent().access(() -> {
                try {
                    saveSelectedProforma();
                }
                catch (ValidationException e) {
                    Notification notification = Notification.show(event.getMessage());
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });
        });
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String s) {
        if(s != null){

            //Open WorkOrder by an other page and search WorkOrder by linkParameter.
            //So for every page with CurrentWorkOrderSubView in it that will open a clicked item in WorkOrderView
            linkParameter = s;
            Optional<Invoice> optionalInvoiceById = invoiceService.getInvoiceById(s);
            if(optionalInvoiceById.isPresent()) {
                selectedInvoice = optionalInvoiceById.get();
                selectProductSubView.setSelectedProductList(selectedInvoice.getProductList());
                selectProductSubView.setUserFunctionAndDocumentDate(UserFunction.ADMIN, selectedInvoice.getInvoiceDate());
                selectedCustomer = selectedInvoice.getCustomer();
                selectProductSubView.setSelectedCustmer(selectedCustomer);
                invoiceBinder.readBean(selectedInvoice);
                Customer customerToSelect = allCustomers.get().stream().filter(item -> item.getName().matches(selectedInvoice.getCustomer().getName())).findFirst().get();
                customerComboBox.setValue(customerToSelect);
            }
            else{
                Notification show = Notification.show("Deze werkbon kon niet worden geopend!");
                show.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
        else{
            linkParameter = null;
            readNewInvoice();
        }
    }
}
