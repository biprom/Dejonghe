package com.adverto.dejonghe.application.dbservices;

import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrder;
import com.adverto.dejonghe.application.entities.enums.invoice.InvoiceStatus;
import com.adverto.dejonghe.application.entities.enums.workorder.WorkOrderStatus;
import com.adverto.dejonghe.application.entities.invoice.Invoice;
import com.adverto.dejonghe.application.repos.InvoiceRepo;
import com.adverto.dejonghe.application.repos.WorkOrderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InvoiceService {
    @Autowired
    InvoiceRepo invoiceRepo;

    public Optional<List<Invoice>> getAll() {
        List<Invoice> invoices = invoiceRepo.findAll();
        if (!invoices.isEmpty()) {
            return Optional.of(invoices);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<Invoice> getLastInvoice() {
        return invoiceRepo.findTopByOrderByInvoiceNumberDesc();
    }

    public Optional<List<Invoice>> getAllInvoicesByStatus(InvoiceStatus status) {
        List<Invoice> invoices = invoiceRepo.findInvoiceByInvoiceStatus(status);
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
