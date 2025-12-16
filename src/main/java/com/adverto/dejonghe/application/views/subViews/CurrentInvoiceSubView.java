package com.adverto.dejonghe.application.views.subViews;

import com.adverto.dejonghe.application.customEvents.GetSelectedInvoiceEvent;
import com.adverto.dejonghe.application.dbservices.InvoiceService;
import com.adverto.dejonghe.application.entities.invoice.Invoice;
import com.adverto.dejonghe.application.services.invoice.InvoiceServices;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    Checkbox filterToCheck;
    Checkbox filterApproved;
    Checkbox filterRejected;
    TextField filterSubject;
    TextField filterName;
    TextField filterResponsible;

    Invoice selectedInvoice;
    Notification deleteInvoiceNotification;


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

        filteredInvoices = new ArrayList<>();

        filterToCheck = new Checkbox();
        filterApproved = new Checkbox();
        filterRejected = new Checkbox();

        filterSubject = new TextField();
        filterSubject.setPlaceholder("Commentaar");

        filterResponsible = new TextField();
        filterResponsible.setPlaceholder("Verantwoordelijk");

        filterName = new TextField();
        filterName.setPlaceholder("Werfadres,Stad,Straat");

        filterToCheck.addValueChangeListener(event -> {
            if(event.getValue() == true) {
                addItemsToPendingWorkOrderGridFromFilter(
                        proFormaInvoiceList.stream()
                                .filter(invoice -> invoice.getToCheck() != null)
                                .filter(invoice ->
                                        invoice.getToCheck().equals(filterToCheck.getValue()))
                                .collect(Collectors.toList()));
            }
            else{
                addItemsToPendingWorkOrderGridFromFilter(proFormaInvoiceList);
                proFormaInvoiceGrid.getDataProvider().refreshAll();
            }
        });

        filterApproved.addValueChangeListener(event -> {
            if(event.getValue() == true) {
                addItemsToPendingWorkOrderGridFromFilter(
                        proFormaInvoiceList.stream()
                                .filter(invoice -> invoice.getBApproved() != null)
                                .filter(invoice ->
                                        invoice.getBApproved().equals(filterApproved.getValue()))
                                .collect(Collectors.toList()));
            }
            else{
                addItemsToPendingWorkOrderGridFromFilter(proFormaInvoiceList);
                proFormaInvoiceGrid.getDataProvider().refreshAll();
            }
        });

        filterRejected.addValueChangeListener(event -> {
            if(event.getValue() == true) {
                addItemsToPendingWorkOrderGridFromFilter(
                        proFormaInvoiceList.stream()
                                .filter(invoice -> invoice.getBRejected() != null)
                                .filter(invoice ->
                                        invoice.getBRejected().equals(filterRejected.getValue()))
                                .collect(Collectors.toList()));
            }
            else{
                addItemsToPendingWorkOrderGridFromFilter(proFormaInvoiceList);
                proFormaInvoiceGrid.getDataProvider().refreshAll();
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
        Grid.Column<Invoice> columnInvoiceNumber = proFormaInvoiceGrid.addColumn(invoice -> invoice.getInvoiceNumber()).setHeader("Nummer").setAutoWidth(true);
        Grid.Column<Invoice> columnCustomer = proFormaInvoiceGrid.addColumn(invoice -> invoice.getWorkAddress().getAddressName()).setHeader("Klant").setAutoWidth(true);
        Grid.Column<Invoice> columnInvoiceDate = proFormaInvoiceGrid.addColumn(invoice -> invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).setHeader("Datum").setAutoWidth(true);
        Grid.Column<Invoice> columnToCheck = proFormaInvoiceGrid.addComponentColumn(item -> {
            Checkbox checkbox = new Checkbox();
            if (item.getToCheck() != null) {
                checkbox.setValue(item.getToCheck());
            } else {
                checkbox.setValue(false);
            }
            checkbox.setEnabled(false);
            return checkbox;
        }).setHeader("Te Controleren").setAutoWidth(true);
        Grid.Column<Invoice> columnApproved = proFormaInvoiceGrid.addComponentColumn(item -> {
            Checkbox checkbox = new Checkbox();
            if(item.getBApproved() != null){
                checkbox.setValue(item.getBApproved());
            }
            else{
                checkbox.setValue(false);
            }
            checkbox.setEnabled(false);
            return checkbox;
        }).setHeader("Goedgekeurd").setAutoWidth(true);
        Grid.Column<Invoice> columnRejected = proFormaInvoiceGrid.addComponentColumn(item -> {
            Checkbox checkbox = new Checkbox();
            if(item.getBRejected() != null){
                checkbox.setValue(item.getBRejected());
            }
            else{
                checkbox.setValue(false);
            }
            checkbox.setEnabled(false);
            return checkbox;
        }).setHeader("Afgekeurd").setAutoWidth(true);
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

        proFormaInvoiceGrid.addComponentColumn(item -> {
            if((item.getBApproved() != null) && (item.getBApproved() == true)){
                Span badge = new Span("Goedgekeurd");
                badge.getElement().getThemeList().add("badge success");
                return badge;
            }
            if((item.getBRejected() != null) && (item.getBRejected() == true)){
                Span badge = new Span("Afgekeurd");
                badge.getElement().getThemeList().add("badge error");
                return badge;
            }
            if((item.getToCheck() != null) && (item.getToCheck() == true)){
                Span badge = new Span("Te controleren");
                badge.getElement().getThemeList().add("badge warning");
                return badge;
            }
            else{
                Span badge = new Span("Te bekijken");
                badge.getElement().getThemeList().add("badge base");
                return badge;
            }

        });

        proFormaInvoiceGrid.addComponentColumn(item -> {
            Button closeButton = new Button(VaadinIcon.TRASH.create());
            closeButton.addThemeVariants(ButtonVariant.LUMO_WARNING);
            closeButton.setAriaLabel("Verwijder factuur");
            closeButton.addClickListener(event -> {
                selectedInvoice = item;
                deleteInvoiceNotification.open();
            });
            return closeButton;
        }).setHeader("Verwijder").setAutoWidth(true);

        proFormaInvoiceGrid.addItemClickListener(event -> {
            selectedInvoice = event.getItem();
            eventPublisher.publishEvent(new GetSelectedInvoiceEvent(this, selectedInvoice));
        });

        headerRow = proFormaInvoiceGrid.appendHeaderRow();
        headerRow.getCell(columnInvoiceNumber).setComponent(filterName);
        headerRow.getCell(columnToCheck).setComponent(filterToCheck);
        headerRow.getCell(columnApproved).setComponent(filterApproved);
        headerRow.getCell(columnRejected).setComponent(filterRejected);
        //headerRow.getCell(columnSubject).setComponent(filterSubject);

        return proFormaInvoiceGrid;
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


    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {

    }
}
