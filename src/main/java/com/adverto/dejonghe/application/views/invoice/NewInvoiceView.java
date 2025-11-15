package com.adverto.dejonghe.application.views.invoice;

import com.adverto.dejonghe.application.dbservices.CustomerService;
import com.adverto.dejonghe.application.dbservices.InvoiceService;
import com.adverto.dejonghe.application.dbservices.ProductService;
import com.adverto.dejonghe.application.entities.customers.Address;
import com.adverto.dejonghe.application.entities.customers.Customer;
import com.adverto.dejonghe.application.entities.enums.employee.UserFunction;
import com.adverto.dejonghe.application.entities.enums.invoice.InvoiceStatus;
import com.adverto.dejonghe.application.entities.invoice.Invoice;
import com.adverto.dejonghe.application.services.invoice.InvoiceServices;
import com.adverto.dejonghe.application.views.subViews.SearchCustomerSubView;
import com.adverto.dejonghe.application.views.subViews.SelectProductSubView;
import com.adverto.dejonghe.application.views.subViews.ShowImageSubVieuw;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@PageTitle("Facturatie")
@Route("facturatie")
@Menu(order = 0, icon = LineAwesomeIconUrl.EURO_SIGN_SOLID)
public class NewInvoiceView extends Div implements BeforeEnterObserver {

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
    Dialog finishWorkOrderDialog;

    Customer selectedCustomer;
    Address selectedAddress;

    Invoice selectedInvoice;

    ComboBox<Address> addressComboBox = new ComboBox<>();
    Card customerCard;
    Span badge;
    TextField tfInvoiceNumber;
    DatePicker invoiceDatePicker;
    ComboBox<InvoiceStatus> invoiceStatusComboBox;
    TextArea invoiceCommentTextArea;

    MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    Upload dropEnabledUpload = new Upload(buffer);
    Button showImageButton;
    Dialog imageDialog;

    Binder<Invoice>invoiceBinder;

    Button finishButton = new Button("Bewaar factuur");


    private FileSystemResource linkToBulkSpreadsheet = new FileSystemResource("/Users/bramvandenberghe/Desktop/facturatie.xlsx");

    public NewInvoiceView(ProductService productService,
                          SelectProductSubView selectProductSubView,
                          SearchCustomerSubView searchCustomerSubView,
                          CustomerService customerService,
                          InvoiceService invoiceService,
                          GridFsTemplate gridFsTemplate,
                          ShowImageSubVieuw showImageSubView,
                          InvoiceServices invoiceServices) {
        this.productService = productService;
        this.selectProductSubView = selectProductSubView;
        this.searchCustomerSubView = searchCustomerSubView;
        this.customerService = customerService;
        this.invoiceService = invoiceService;
        this.gridFsTemplate = gridFsTemplate;
        this.showImageSubView = showImageSubView;
        this.invoiceServices = invoiceServices;

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
    }

    private void setUpFinishButton() {
        finishButton.setWidth("100%");
        finishButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_WARNING);
        finishButton.addClickListener(e -> {
            finishWorkOrderDialog.open();
        });
    }

    private void setUpFinishWorkOrderDialog() {
        finishWorkOrderDialog = new Dialog();
        finishWorkOrderDialog.setHeaderTitle("Ben je zeker dat je deze factuur wil bewaren?");

        VerticalLayout dialogLayout = createDialogLayout();
        finishWorkOrderDialog.add(dialogLayout);

        Button saveButton = createSaveButton(finishWorkOrderDialog);
        Button cancelButton = new Button("Niet Bewaren", e -> finishWorkOrderDialog.close());
        finishWorkOrderDialog.getFooter().add(cancelButton);
        finishWorkOrderDialog.getFooter().add(saveButton);
    }

    private Button createSaveButton(Dialog dialog) {
        Button saveButton = new Button("Bewaren");
        saveButton.addClickListener(click -> {
            dialog.close();
            try {
                selectedInvoice.setCustomer(selectedCustomer);
                invoiceBinder.writeBean(selectedInvoice);
                selectedInvoice.setProductList(selectProductSubView.getSelectedProductList());
                invoiceService.save(selectedInvoice);
                Notification.show("Deze factuur is bewaard");
            } catch (ValidationException e) {
                Notification.show("Deze factuur kon niet worden bewaard.");
            }
            readNewInvoice();
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
        invoiceBinder.forField(addressComboBox)
                .asRequired("Gelieve een adres te selecteren!")
                .bind(Invoice::getWorkAddress, Invoice::setWorkAddress);
        invoiceBinder.forField(invoiceStatusComboBox)
                .asRequired("Gelieve een status te selecteren!")
                .bind(Invoice::getInvoiceStatus, Invoice::setInvoiceStatus);
        invoiceBinder.forField(invoiceCommentTextArea)
                .asRequired("Gelieve een omschrijving te geven!")
                .bind(Invoice::getDiscription, Invoice::setDiscription);
        invoiceBinder.addValueChangeListener(workOrder -> {
            try {
                selectedInvoice.setCustomer(selectedCustomer);
                invoiceBinder.writeBean(selectedInvoice);
                selectedInvoice.setProductList(selectProductSubView.getSelectedProductList());
                invoiceService.save(selectedInvoice);
            } catch (ValidationException e) {
                Notification.show("Kon de werkbon nog niet bewaren");
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
        searchCustomerSubView.setDialog(searchCustomerDialog);
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
        vLayout.add(getAddressComboBox());
        vLayout.add(getCustomerCard());
        vLayout.add(getInvoiceNumber());
        vLayout.add(getInvoiceDatePicker());
        vLayout.add(getInvoiceStatusPicker());
        return vLayout;
    }

    private VerticalLayout getSecondStepHeader() {
        VerticalLayout vLayout = new VerticalLayout();
        vLayout.setSizeFull();
        vLayout.add(getInvoiceCommentTextArea());
        return vLayout;
    }

    private VerticalLayout getThirdStepHeader() {
        VerticalLayout vLayout = new VerticalLayout();
        vLayout.setSizeFull();
        vLayout.add(dropEnabledUpload);
        vLayout.add(showImageButton);
        vLayout.add(finishButton);
        return vLayout;
    }

    private TextArea getInvoiceCommentTextArea() {
        invoiceCommentTextArea = new TextArea("Commentaar");
        invoiceCommentTextArea.setWidth("100%");
        invoiceCommentTextArea.setHeight("200px");
        return invoiceCommentTextArea;
    }

    private ComboBox getInvoiceStatusPicker() {
        invoiceStatusComboBox = new ComboBox<>();
        invoiceStatusComboBox.setWidthFull();
        invoiceStatusComboBox.setItems(InvoiceStatus.values());
        invoiceStatusComboBox.setItemLabelGenerator(item -> item.getDiscription());
        return invoiceStatusComboBox;
    }

    private DatePicker getInvoiceDatePicker() {
        invoiceDatePicker = new DatePicker();
        invoiceDatePicker.setSizeFull();
        invoiceDatePicker.setValue(LocalDate.now());
        return invoiceDatePicker;
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
        tfInvoiceNumber.setValue(String.valueOf(invoiceServices.getNewInvoiceNumber()));
        return tfInvoiceNumber;
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
        addressComboBox.addValueChangeListener(event -> {
            Optional<List<Customer>> optCustomersByStreet = customerService.getCustomersByStreet(event.getValue().getStreet());
            if(optCustomersByStreet.isPresent()){
                customerCard.removeAll();
                customerCard.setTitle(new Div(optCustomersByStreet.get().getFirst().getName()));
                customerCard.setSubtitle(new Div(optCustomersByStreet.get().getFirst().getVatNumber()));
                customerCard.add(optCustomersByStreet.get().getFirst().getComment());
                if(optCustomersByStreet.get().getFirst().getAlert()){
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
            }
            else{
                customerCard.removeAll();
                customerCard.setTitle(new Div("N/A"));
                customerCard.setSubtitle(new Div("N/A"));
                customerCard.add("N/A");
                badge.setText("N/A");
                badge.getElement().getThemeList().clear();
                badge.getElement().getThemeList().add("badge success");
                customerCard.setHeaderSuffix(badge);
            }
        });
        addressComboBox.setWidthFull();
        return addressComboBox;
    }

    private void setUpMainSplitLayout() {
        mainSplitLayout = new SplitLayout();
        mainSplitLayout.setSizeFull();
        mainSplitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);
    }

    private void setUpHeaderSplitLayout() {
        headerSplitLayout = new SplitLayout();
        headerSplitLayout.setSizeFull();
        headerSplitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
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
                showImageSubView.setSelectedWorkOrder(selectedInvoice.getImageList());
            }
            else{
                Notification notification = Notification.show("Deze werkbon bevat nog geen foto's!");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });
    }

    private void readNewInvoice() {
        Invoice newInvoice = new Invoice();
        newInvoice.setInvoiceDate(LocalDate.now());
        newInvoice.setInvoiceStatus(InvoiceStatus.AANGEMAAKT);
        newInvoice.setInvoiceNumber(invoiceServices.getNewInvoiceNumber());
        selectedInvoice = newInvoice;
        invoiceBinder.readBean(selectedInvoice);
        selectProductSubView.setUserFunctionAndDocumentDate(UserFunction.ADMIN, selectedInvoice.getInvoiceDate());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        readNewInvoice();
    }


}
