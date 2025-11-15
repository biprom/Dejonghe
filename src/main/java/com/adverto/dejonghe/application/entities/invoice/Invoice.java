package com.adverto.dejonghe.application.entities.invoice;

import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrder;
import com.adverto.dejonghe.application.entities.customers.Address;
import com.adverto.dejonghe.application.entities.customers.Customer;
import com.adverto.dejonghe.application.entities.enums.invoice.InvoiceStatus;
import com.adverto.dejonghe.application.entities.product.product.Product;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Document
@Getter
@Setter
@NoArgsConstructor
public class Invoice {
    @Id
    private String id;

    Integer invoiceNumber;
    Customer customer;
    Address workAddress;
    LocalDate invoiceDate;
    LocalDate expiryDate;
    InvoiceStatus invoiceStatus;
    String discription;
    Boolean toCheck;
    Boolean bApproved;
    Boolean bRejected;

    Set<WorkOrder> workOrderList;

    List<Product>productList;

    List<String>imageList;

}
