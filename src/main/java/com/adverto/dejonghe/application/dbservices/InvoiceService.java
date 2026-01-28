package com.adverto.dejonghe.application.dbservices;

import com.adverto.dejonghe.application.entities.invoice.Invoice;
import com.adverto.dejonghe.application.repos.InvoiceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InvoiceService {
    @Autowired
    InvoiceRepo invoiceRepo;

    public Optional<Invoice> getInvoiceById(String id) {
        Optional<Invoice> optionalInvoice = Optional.of(invoiceRepo.findInvoiceById(id));
        return optionalInvoice;
    }

    public Optional<List<Invoice>> getAll() {
        List<Invoice> invoices = invoiceRepo.findAll();
        if (!invoices.isEmpty()) {
            return Optional.of(invoices);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<Invoice> getLastProFormaInvoice() {
        return invoiceRepo.findTopBybFinalInvoiceFalseOrderByInvoiceNumberDesc();
    }

    public Optional<Invoice>  getLastFinalInvoice() {
        return invoiceRepo.findTopBybFinalInvoiceTrueOrderByInvoiceNumberDesc();
    }

    public Optional<List<Invoice>> getAllInvoicesByFinalInvoice(Boolean finalInvoice) {
        List<Invoice> invoices = invoiceRepo.findInvoiceBybFinalInvoice(finalInvoice);
        if (!invoices.isEmpty()) {
            return Optional.of(invoices);
        }
        else{
            return Optional.empty();
        }
    }

    public void delete(Invoice invoice) {
        invoiceRepo.delete(invoice);
    }

    public void save(Invoice invoice) {
        invoiceRepo.save(invoice);
        }


}
