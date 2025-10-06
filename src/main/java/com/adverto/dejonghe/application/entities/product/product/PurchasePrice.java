package com.adverto.dejonghe.application.entities.product.product;

import com.adverto.dejonghe.application.entities.product.product.Supplier;
import com.adverto.dejonghe.application.entities.product.enums.E_Product_PurchasingType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document
@Getter
@Setter
@NoArgsConstructor
public class PurchasePrice {
    @Id
    private Long id;
    private LocalDate purchaseDate;
    private E_Product_PurchasingType purchasingType;
    private Double quantity;
    private Double price;
    private Supplier supplier;
}
