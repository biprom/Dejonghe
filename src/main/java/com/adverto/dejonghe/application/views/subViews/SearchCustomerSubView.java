package com.adverto.dejonghe.application.views.subViews;

import com.adverto.dejonghe.application.dbservices.CustomerService;
import com.adverto.dejonghe.application.entities.customers.Address;
import com.adverto.dejonghe.application.entities.customers.Customer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
public class SearchCustomerSubView extends VerticalLayout implements BeforeEnterObserver {
    CustomerService customerService;
    TextField tfCustomerFilter;
    ComboBox<Customer> cbCustomers;
    ComboBox<Address> cbWorkAddress;
    Dialog dialog;

    Customer selectedCustomer;
    Address selectedAddress;

    Button closeButton;

    public SearchCustomerSubView(CustomerService customerService) {
        this.customerService = customerService;

        setUpFilter();
        setUpCbCustomers();
        setUpCbWorkArdres();
        this.add(getTitleSpan());
        this.add(tfCustomerFilter);
        this.add(cbCustomers);
        this.add(cbWorkAddress);
    }

    private void setUpCbWorkArdres() {
        cbWorkAddress = new ComboBox<>();
        cbWorkAddress.setWidth("100%");
        cbWorkAddress.setPlaceholder("Gelieve een werfadres te selecteren");
        cbWorkAddress.setItemLabelGenerator(item -> item.getAddressName() + " " + item.getStreet() + " " + item.getZip() + " " + item.getCity());
        cbWorkAddress.addValueChangeListener(event -> {
            selectedAddress = cbWorkAddress.getValue();

        });
    }

    private Span getTitleSpan() {
        Span s = new Span("Gelieve een klant en werfadres te selecteren");
        s.getElement().getStyle().set("font-size", "40px");
        s.getElement().getStyle().set("color", "blue");
        return s;
    }

    private void setUpCbCustomers() {
        cbCustomers = new ComboBox<>();
        cbCustomers.setItems(customerService.getAllCustomers().get());
        cbCustomers.setWidth("100%");
        cbCustomers.setPlaceholder("Gelieve de klant te selecteren");
        cbCustomers.setItemLabelGenerator(Customer::getName);
        cbCustomers.addValueChangeListener(event -> {
            selectedCustomer = event.getValue();
            if(selectedCustomer.getAddresses() != null && !selectedCustomer.getAddresses().isEmpty()) {
                cbWorkAddress.setItems(selectedCustomer.getAddresses());
            }
           else{
                cbWorkAddress.clear();
            }
           selectedAddress = null;
        });
    }

    private void setUpFilter() {
        tfCustomerFilter = new TextField();
        tfCustomerFilter.setWidth("100%");
        tfCustomerFilter.setPlaceholder("Zoek Klant op Naam of BTW-nummer");
        tfCustomerFilter.addValueChangeListener(event -> {
            if(tfCustomerFilter.getValue() != "") {
                Optional<List<Customer>> customerByNameOrVat = customerService.getCustomerByNameOrVat(tfCustomerFilter.getValue());
                if (customerByNameOrVat.isPresent()) {
                    cbCustomers.setItems(customerByNameOrVat.get());
                }
                else{
                    cbCustomers.clear();
                    Notification.show("Geen klanten gevonden");
                }
            }
            else{
                cbCustomers.setItems(customerService.getAllCustomers().get());
            }
        });
    }

    public Customer getSelectedCustomer() {
        return selectedCustomer;
    }

    public Address getSelectedAddress() {
        return selectedAddress;
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {

    }
}
