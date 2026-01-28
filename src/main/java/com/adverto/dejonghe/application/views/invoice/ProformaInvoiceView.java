package com.adverto.dejonghe.application.views.invoice;

import com.adverto.dejonghe.application.customEvents.GetSelectedInvoiceEvent;
import com.adverto.dejonghe.application.customEvents.ReloadProductListEvent;
import com.adverto.dejonghe.application.dbservices.InvoiceService;
import com.adverto.dejonghe.application.entities.invoice.Invoice;
import com.adverto.dejonghe.application.views.subViews.CurrentInvoiceSubView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.*;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.List;
import java.util.Optional;


@PageTitle("ProForma Facturatie")
@Route("proforma_facturatie")
@Menu(order = 0, icon = LineAwesomeIconUrl.EURO_SIGN_SOLID)
@Component
@Scope("prototype")
public class ProformaInvoiceView extends Div implements BeforeEnterObserver {

    CurrentInvoiceSubView currentInvoiceSubView;
    InvoiceService invoiceService;

    List<Invoice>allProformaInvoices;

    public ProformaInvoiceView(CurrentInvoiceSubView currentInvoiceSubView,
                               InvoiceService invoiceService) {
        this.currentInvoiceSubView = currentInvoiceSubView;
        this.invoiceService = invoiceService;
    }

    private void loadData(){
        Optional<List<Invoice>> allInvoicesByStatus = invoiceService.getAllInvoicesByFinalInvoice(false);
        if(allInvoicesByStatus.isPresent()){
            currentInvoiceSubView.addItemsToProformaGrid(allInvoicesByStatus.get());
            currentInvoiceSubView.viewAsProformaInvoices();
            currentInvoiceSubView.setSizeFull();
            this.setSizeFull();
            this.add(currentInvoiceSubView);
        }
        else{
            Notification notification = Notification.show("Geen proforma facturen gevonden");
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }

    @EventListener
    public void handleSelectedInvoiceEvent(GetSelectedInvoiceEvent event) {
        if (UI.getCurrent() != null && UI.getCurrent().equals(UI.getCurrent())) {
            Optional<Invoice> selectedInvoice = Optional.of(event.getSelectedInvoice());
            UI.getCurrent().navigate(InvoiceView.class, selectedInvoice.get().getId());
        }
    }


    @EventListener
    public void handleReloadEvent(ReloadProductListEvent event) {
        if (UI.getCurrent() != null && UI.getCurrent().equals(UI.getCurrent())) {
            loadData();
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        loadData();
    }
}
