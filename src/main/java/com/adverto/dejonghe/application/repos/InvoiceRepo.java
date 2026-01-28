package com.adverto.dejonghe.application.repos;


import com.adverto.dejonghe.application.entities.invoice.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepo extends MongoRepository<Invoice, String> {
    List<Invoice>findInvoiceByInvoiceNumber(String invoiceNumber);
    Invoice findInvoiceById(String workOrderId);
    List<Invoice>findInvoiceBybFinalInvoice(Boolean finalInvoice);
    Optional<Invoice> findTopBybFinalInvoiceFalseOrderByInvoiceNumberDesc();
    Optional<Invoice> findTopBybFinalInvoiceTrueOrderByInvoiceNumberDesc();
}
