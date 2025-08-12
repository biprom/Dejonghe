package com.adverto.dejonghe.application.repos;


import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrder;
import com.adverto.dejonghe.application.entities.enums.invoice.InvoiceStatus;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkOrderStatus;
import com.adverto.dejonghe.application.entities.invoice.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepo extends MongoRepository<Invoice, String> {
    List<Invoice>findInvoiceByInvoiceNumber(String invoiceNumber);
    List<Invoice>findInvoiceByInvoiceStatus(InvoiceStatus invoiceStatus);
    Optional<Invoice> findTopByOrderByInvoiceNumberDesc();
}
