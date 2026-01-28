package com.adverto.dejonghe.application.views.subViews;

import com.adverto.dejonghe.application.customEvents.GetSelectedInvoiceEvent;
import com.adverto.dejonghe.application.dbservices.InvoiceService;
import com.adverto.dejonghe.application.entities.enums.invoice.FINAL_INVOICE_STATUS;
import com.adverto.dejonghe.application.entities.invoice.Invoice;
import com.adverto.dejonghe.application.entities.product.product.Product;
import com.adverto.dejonghe.application.services.invoice.InvoiceServices;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY_INLINE;

@Component
@Scope("prototype")
public class CurrentInvoiceSubView extends VerticalLayout implements BeforeEnterObserver {

    InvoiceService invoiceService;
    InvoiceServices invoiceServices;
    ApplicationEventPublisher eventPublisher;

    Grid<Invoice> proFormaInvoiceGrid;
    List<Invoice> proFormaInvoiceList;
    List<Invoice> filteredInvoices;
    HeaderRow headerRow;

    ComboBox<String> proformaStatusFilter;
    ComboBox<FINAL_INVOICE_STATUS> finalStatusFilter;
    TextField filterSubject;
    TextField filterName;
    TextField filterResponsible;

    Invoice selectedInvoice;
    Notification deleteInvoiceNotification;

    Grid.Column<Invoice> columnProformaStatus;
    Grid.Column<Invoice> columnFinalStatus;
    Grid.Column<Invoice> removeColumn;
    Grid.Column<Invoice> columnInvoicePaydate;
    Grid.Column<Invoice> columnInvoiceEndDate;
    Grid.Column<Invoice> totalPriceColumn;
    Grid.Column<Invoice> vatColumn;
    Grid.Column<Invoice> totalAndVatColumn;


    @Autowired
    public CurrentInvoiceSubView(InvoiceService invoiceService,
                                 InvoiceServices invoiceServices,
                                 ApplicationEventPublisher eventPublisher) {
        this.invoiceService = invoiceService;
        this.invoiceServices = invoiceServices;
        this.eventPublisher = eventPublisher;
        setUpfilters();
        createReportDelete();
        this.add(setUpGrid());

    }

    private void setUpfilters() {

        proformaStatusFilter = new ComboBox<>();
        finalStatusFilter = new ComboBox<>();

        filteredInvoices = new ArrayList<>();

        filterSubject = new TextField();
        filterSubject.setPlaceholder("Commentaar");

        filterResponsible = new TextField();
        filterResponsible.setPlaceholder("Verantwoordelijk");

        filterName = new TextField();
        filterName.setWidth("100%");
        filterName.setPlaceholder("Werfadres,Stad,Straat");

        proformaStatusFilter.setItems("","Geen Status","Te controleren","Goedgekeurd","Afgekeurd");
        finalStatusFilter.setItems(FINAL_INVOICE_STATUS.values());
        finalStatusFilter.setItemLabelGenerator(FINAL_INVOICE_STATUS::getDiscription);

        proformaStatusFilter.addValueChangeListener(event -> {
            if(event.getValue().matches("")) {
                addItemsToPendingWorkOrderGridFromFilter(proFormaInvoiceList);
                proFormaInvoiceGrid.getDataProvider().refreshAll();
            } else if (event.getValue().matches("Geen Status")) {
                addItemsToPendingWorkOrderGridFromFilter(
                        proFormaInvoiceList.stream()
                                .filter(invoice -> invoice.getToCheck() != null)
                                .filter(invoice ->
                                        invoice.getToCheck().equals(false)&&invoice.getBApproved().equals(false)&&invoice.getBRejected().equals(false))
                                .collect(Collectors.toList()));
            }
            else if (event.getValue().matches("Te controleren")) {
                addItemsToPendingWorkOrderGridFromFilter(
                        proFormaInvoiceList.stream()
                                .filter(invoice -> invoice.getToCheck() != null)
                                .filter(invoice ->
                                        invoice.getToCheck().equals(true))
                                .collect(Collectors.toList()));
            }
            else if (event.getValue().matches("Goedgekeurd")) {
                addItemsToPendingWorkOrderGridFromFilter(
                        proFormaInvoiceList.stream()
                                .filter(invoice -> invoice.getToCheck() != null)
                                .filter(invoice ->
                                        invoice.getBApproved().equals(true))
                                .collect(Collectors.toList()));
            }
            else if (event.getValue().matches("Afgekeurd")) {
                addItemsToPendingWorkOrderGridFromFilter(
                        proFormaInvoiceList.stream()
                                .filter(invoice -> invoice.getToCheck() != null)
                                .filter(invoice ->
                                        invoice.getBRejected().equals(true))
                                .collect(Collectors.toList()));
            }

            }
        );

        finalStatusFilter.addValueChangeListener(event -> {
            if(event.getValue().equals(FINAL_INVOICE_STATUS.OPEN)){
                addItemsToPendingWorkOrderGridFromFilter(
                        proFormaInvoiceList.stream()
                                .filter(invoice -> invoice.getOpen() != null)
                            .filter(invoice ->
                        invoice.getOpen().equals(true))
                        .collect(Collectors.toList()));
            }
            else if(event.getValue().equals(FINAL_INVOICE_STATUS.PAID)){
                addItemsToPendingWorkOrderGridFromFilter(
                        proFormaInvoiceList.stream()
                                .filter(invoice -> invoice.getPaid() != null)
                                .filter(invoice ->
                                        invoice.getPaid().equals(true))
                                .collect(Collectors.toList()));
            }
            else if(event.getValue().equals(FINAL_INVOICE_STATUS.EXPIRED)){
                addItemsToPendingWorkOrderGridFromFilter(
                        proFormaInvoiceList.stream()
                                .filter(invoice -> invoice.getExpired() != null)
                                .filter(invoice ->
                                        invoice.getExpired().equals(true))
                                .collect(Collectors.toList()));
            }
            else{
                addItemsToPendingWorkOrderGridFromFilter(
                        proFormaInvoiceList);
            }
        });

        filterSubject.addValueChangeListener(event -> {
            addItemsToPendingWorkOrderGridFromFilter(
                    proFormaInvoiceList.stream()
                            .filter(invoice ->
                                    invoice.getDiscription().toLowerCase().contains(event.getValue().toLowerCase()))
                            .collect(Collectors.toList()));
        });


        filterName.addValueChangeListener(event -> {
                List<Invoice> collect = proFormaInvoiceList.stream().filter(filter -> (filter.getWorkAddress().getStreet().toLowerCase().contains(event.getValue().toLowerCase())) ||
                        (filter.getWorkAddress().getCity().toLowerCase().contains(event.getValue().toLowerCase())) ||
                        (filter.getWorkAddress().getAddressName().toLowerCase().contains(event.getValue().toLowerCase()))).collect(Collectors.toList());
                addItemsToPendingWorkOrderGridFromFilter(collect);
        });
    }

    private Grid<Invoice> setUpGrid() {
        proFormaInvoiceGrid = new Grid<>();
        proFormaInvoiceList = new ArrayList<>();
        Grid.Column<Invoice> columnInvoiceNumber = proFormaInvoiceGrid.addColumn(invoice -> {
            if(invoice.getBFinalInvoice() == true){
                return invoice.getFinalInvoiceNumber();
            }
            else{
                return invoice.getInvoiceNumber();
            }
        }).setHeader("Nummer").setFlexGrow(1);
        Grid.Column<Invoice> columnInvoiceDate = proFormaInvoiceGrid.addColumn(invoice -> invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).setHeader("Datum").setFlexGrow(1);
        columnInvoicePaydate = proFormaInvoiceGrid.addColumn(invoice -> {
            if(invoice.getPaymentDate() != null){
                return invoice.getPaymentDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            }
            else{
                return "";
            }
        }).setHeader("Betaald op").setFlexGrow(1);

        columnInvoiceEndDate = proFormaInvoiceGrid.addColumn(invoice -> {
            if(invoice.getExpiryDate() != null){
                return invoice.getExpiryDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            }
            else{
                return "";
            }
        }).setHeader("Vervaldatum").setFlexGrow(1);

        Grid.Column<Invoice> columnCustomer = proFormaInvoiceGrid.addColumn(invoice -> invoice.getWorkAddress().getAddressName()).setHeader("Klant").setFlexGrow(1);
        totalPriceColumn = proFormaInvoiceGrid.addColumn(invoice -> {
            return invoice.getProductList().stream()
                    .filter(product -> product.getTotalPrice() != null)
                    .mapToDouble(Product::getTotalPrice)
                    .sum();
        }).setHeader("Excl").setFlexGrow(1);

        vatColumn = proFormaInvoiceGrid.addColumn(invoice -> {
            return Math.round(
                    invoice.getProductList().stream()
                            .filter(product -> product.getTotalPrice() != null && product.getVat() != null)
                            .mapToDouble(product ->
                                    (product.getVat().getValue() / 100.0) * product.getTotalPrice()
                            )
                            .sum() * 100
            ) / 100.0;
        }).setHeader("Btw").setFlexGrow(1);

        totalAndVatColumn = proFormaInvoiceGrid.addColumn(invoice -> {
                Double amount = invoice.getProductList().stream()
                .filter(product -> product.getTotalPrice() != null)
                .mapToDouble(Product::getTotalPrice)
                .sum();

                Double vat = Math.round(
                    invoice.getProductList().stream()
                            .filter(product -> product.getTotalPrice() != null && product.getVat() != null)
                            .mapToDouble(product ->
                                    (product.getVat().getValue() / 100.0) * product.getTotalPrice()
                            )
                            .sum() * 100
            ) / 100.0;
            return amount + vat;
        }).setHeader("Totaal").setFlexGrow(1);


//        Grid.Column<Invoice> columnSubject = proFormaInvoiceGrid.addComponentColumn(invoice -> {
//            TextArea textArea = new TextArea();
//            textArea.setWidth("100%");
//            textArea.setHeight("100%");
//            if(invoice.getDiscription() != null){
//                textArea.setValue(invoice.getDiscription());
//            }
//            else{
//                textArea.setValue("");
//            }
//            textArea.setReadOnly(true);
//            return textArea;
//        }).setHeader("Omschrijving").setAutoWidth(true);

        columnProformaStatus = proFormaInvoiceGrid.addComponentColumn(item -> {
            if((item.getBApproved() != null) && (item.getBApproved() == true)){
                Span badge = new Span("Goedgekeurd");
                badge.getElement().getThemeList().add("badge success");
                badge.getStyle().set("width", "200px");
                return badge;
            }
            if((item.getBRejected() != null) && (item.getBRejected() == true)){
                Span badge = new Span("Afgekeurd");
                badge.getElement().getThemeList().add("badge error");
                badge.getStyle().set("width", "200px");
                return badge;
            }
            if((item.getToCheck() != null) && (item.getToCheck() == true)){
                Span badge = new Span("Te controleren");
                badge.getElement().getThemeList().add("badge warning");
                badge.getStyle().set("width", "200px");
                return badge;
            }
            else{
                Span badge = new Span(" ");
                badge.getStyle().set("background-color", "transparent");
                badge.getStyle().set("width", "200px");
                return badge;
            }

        }).setHeader("Status").setFlexGrow(1);;

         columnFinalStatus = proFormaInvoiceGrid.addComponentColumn(item -> {
             //first alwasy check if invoice is expired
             if(item.getPaid() == false){
                 if(LocalDate.now().isAfter(item.getExpiryDate())){
                     item.setExpired(true);
                     item.setOpen(false);
                     invoiceService.save(item);
                 }
             }
             return getStatusBadgesforInvoice(item);
        }).setHeader("Status").setFlexGrow(1);

        removeColumn = proFormaInvoiceGrid.addComponentColumn(item -> {
            Button closeButton = new Button(VaadinIcon.TRASH.create());
            closeButton.addThemeVariants(ButtonVariant.LUMO_WARNING);
            closeButton.setAriaLabel("Verwijder factuur");
            closeButton.addClickListener(event -> {
                selectedInvoice = item;
                deleteInvoiceNotification.open();
            });
            return closeButton;
        }).setHeader("Verwijder").setFlexGrow(1);



        proFormaInvoiceGrid.addItemClickListener(event -> {
            //generate event so the invoice can be opened from motherView.
            selectedInvoice = event.getItem();
            eventPublisher.publishEvent(new GetSelectedInvoiceEvent(this, selectedInvoice));
        });

        headerRow = proFormaInvoiceGrid.appendHeaderRow();
        headerRow.getCell(columnCustomer).setComponent(filterName);
        headerRow.getCell(columnProformaStatus).setComponent(proformaStatusFilter);
        headerRow.getCell(columnFinalStatus).setComponent(finalStatusFilter);

        proFormaInvoiceGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        proFormaInvoiceGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);

        return proFormaInvoiceGrid;
    }

    private com.vaadin.flow.component.Component getStatusBadgesforInvoice(Invoice item) {

        HorizontalLayout horizontalLayout = new HorizontalLayout();

        //add Status to HorizontalLayout
        if((item.getOpen() != null) && (item.getOpen() == true)){
            Span badge = new Span("Openstaand");
            badge.getElement().getThemeList().add("badge success");
            badge.getStyle().set("width", "100px");
            horizontalLayout.add(badge);
        }
        else if((item.getPaid() != null) && (item.getPaid() == true)){
            Span badge = new Span("Betaald");
            badge.getElement().getThemeList().add("badge success");
            badge.getStyle().set("width", "100px");
            horizontalLayout.add(badge);
        }
        else if((item.getExpired() != null) && (item.getExpired() == true)){
            Span badge = new Span("Vervallen");
            badge.getElement().getThemeList().add("badge error");
            badge.getStyle().set("width", "100px");
            horizontalLayout.add(badge);
        }
        else if((item.getReminder1() != null) && (item.getReminder1() == true)){
            Span badge = new Span("Herinnering1");
            badge.getElement().getThemeList().add("badge error");
            badge.getStyle().set("width", "100px");
            horizontalLayout.add(badge);
        }
        else if((item.getReminder2() != null) && (item.getReminder2() == true)){
            Span badge = new Span("Herinnering2");
            badge.getElement().getThemeList().add("badge error");
            badge.getStyle().set("width", "100px");
            horizontalLayout.add(badge);
        }
        else if((item.getReminder3() != null) && (item.getReminder3() == true)){
            Span badge = new Span("Herinnering3");
            badge.getElement().getThemeList().add("badge error");
            badge.getStyle().set("width", "100px");
            horizontalLayout.add(badge);
        }
        else{
            Span badge = new Span("Geen status");
            badge.getStyle().set("background-color", "transparent");
            badge.getStyle().set("width", "100px");
            horizontalLayout.add(badge);
        }
        //add amount of days next to it
        Period period;
        if(item.getPaid()){
            if((item.getExpiryDate() != null) && (item.getPaymentDate() != null)){
                period = Period.between(item.getPaymentDate(), item.getExpiryDate());
                if(period.getDays() < 0 ){
                    Span badge = new Span(""+ period.plusDays(2).getDays());
                    badge.getElement().getThemeList().add("badge error");
                    badge.getStyle().set("width", "100px");
                    horizontalLayout.add(badge);
                }
                else{
                    Span badge = new Span(""+ period.plusDays(2).getDays());
                    badge.getElement().getThemeList().add("badge success");
                    badge.getStyle().set("width", "100px");
                    horizontalLayout.add(badge);
                }
            }
        }
        else{
            period = Period.between(LocalDate.now(), item.getExpiryDate());
            if(period.getDays() < 0 ){
                Span badge = new Span(""+ period.plusDays(2).getDays());
                badge.getElement().getThemeList().add("badge error");
                badge.getStyle().set("width", "100px");
                horizontalLayout.add(badge);
            }
            else{
                Span badge = new Span(""+ period.plusDays(2).getDays());
                badge.getElement().getThemeList().add("badge success");
                badge.getStyle().set("width", "100px");
                horizontalLayout.add(badge);
            }
        }
        return horizontalLayout;
    }

    public void addItemsToProformaGrid(List<Invoice>invoiceList){
        if((invoiceList != null) && (invoiceList.size() > 0)){
            proFormaInvoiceGrid.setVisible(true);
            proFormaInvoiceList.clear();
            for(Invoice invoice : invoiceList){
                proFormaInvoiceList.add(invoice);
            }
            proFormaInvoiceGrid.setItems(proFormaInvoiceList);

        }
        else{
            proFormaInvoiceGrid.setVisible(false);
            Notification notification = Notification.show("Geen Proforma Facturen");
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }

    public void addItemsToPendingWorkOrderGridFromFilter(List<Invoice>filterList){
        if((filterList != null) && (filterList.size() > 0)){
            proFormaInvoiceGrid.setVisible(true);
            filteredInvoices.clear();
            for(Invoice invoice : filterList){
                filteredInvoices.add(invoice);
            }
            proFormaInvoiceGrid.setItems(filteredInvoices);
            proFormaInvoiceGrid.getDataProvider().refreshAll();
        }
        else{
            Notification notification = Notification.show("Geen gefilterde facturen");
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }


    public Notification createReportDelete() {
        deleteInvoiceNotification = new Notification();
        deleteInvoiceNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);

        Icon icon = VaadinIcon.WARNING.create();
        Button retryBtn = new Button("Annuleer",
                clickEvent -> deleteInvoiceNotification.close());
        retryBtn.getStyle().setMargin("0 0 0 var(--lumo-space-l)");

        var layout = new HorizontalLayout(icon,
                new Text("Ben je zeker dat je deze werkbon wil wissen?"), retryBtn,
                createCloseBtn(deleteInvoiceNotification));
        layout.setAlignItems(Alignment.CENTER);

        deleteInvoiceNotification.add(layout);

        return deleteInvoiceNotification;
    }



    public Button createCloseBtn(Notification notification) {
        Button removeBtn = new Button(VaadinIcon.TRASH.create(),
                clickEvent -> {
                    if(selectedInvoice != null){
                        //when filter is selected
                        try{
                            proFormaInvoiceList.remove(selectedInvoice);
                            filteredInvoices.remove(selectedInvoice);
                        }
                        catch(Exception e){

                        }
                        proFormaInvoiceGrid.getDataProvider().refreshAll();
                        invoiceService.delete(selectedInvoice);
                        Notification.show("Proforma is verwijderd");
                    }
                    else{
                        Notification.show("Geen Proforma te verwijderen");
                    }
                    notification.close();
                });
        removeBtn.addThemeVariants(LUMO_TERTIARY_INLINE);
        return removeBtn;
    }

    public Optional<Set<Invoice>> getSelectedWorkOrders(){
        Set<Invoice> selectedItems = proFormaInvoiceGrid.getSelectedItems();
        return Optional.of(selectedItems);
    }

    public void viewAsProformaInvoices(){
        columnProformaStatus.setVisible(true);
        removeColumn.setVisible(true);
        columnFinalStatus.setVisible(false);
        columnInvoicePaydate.setVisible(false);
        columnInvoiceEndDate.setVisible(false);
        totalPriceColumn.setVisible(false);
        vatColumn.setVisible(false);
        totalAndVatColumn.setVisible(false);
    }

    public void viewAsFinalWorkOrders(){
        columnProformaStatus.setVisible(false);
        removeColumn.setVisible(false);
        columnFinalStatus.setVisible(true);
        columnInvoicePaydate.setVisible(true);
        columnInvoiceEndDate.setVisible(true);
        totalPriceColumn.setVisible(true);
        vatColumn.setVisible(true);
        totalAndVatColumn.setVisible(true);
    }


    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {

    }
}
