package com.adverto.dejonghe.application.entities.invoice;

import com.adverto.dejonghe.application.entities.WorkOrder.WorkOrder;
import com.adverto.dejonghe.application.entities.customers.Address;
import com.adverto.dejonghe.application.entities.customers.Customer;
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

    Boolean bFinalInvoice = Boolean.FALSE;
    Integer invoiceNumber;
    Integer finalInvoiceNumber;
    Customer customer;
    Address workAddress;
    LocalDate invoiceDate;
    LocalDate expiryDate;
    LocalDate paymentDate;
    String discription;
    Boolean toCheck = false;
    Boolean bApproved = false;
    Boolean bRejected = false;
    Boolean open = false;
    Boolean expired = false;
    Boolean paid = false;
    Boolean reminder1 = false;
    Boolean reminder2 = false;
    Boolean reminder3 = false;

    Set<WorkOrder> workOrderList;

    List<Product>productList;

    List<String>imageList;

}
