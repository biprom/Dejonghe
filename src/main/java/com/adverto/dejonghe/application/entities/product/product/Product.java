package com.adverto.dejonghe.application.entities.product.product;

import com.adverto.dejonghe.application.entities.enums.product.VAT;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Document
@Getter
@Setter
@NoArgsConstructor
public class Product implements Serializable {
    @Id
    private String id;
    private LocalDate date;
    private Boolean option;
    private Double selectedAmount;
    private String productCode;
    private String internalName;
    private String abbreviation;
    private Double purchasePrice;
    private Double sellPrice;
    private Double sellMargin;
    private Double totalPrice;
    private VAT vat;
    private String positionNumber;
    private String unit;
    private String comment;
    private List<PurchasePrice> purchasePriseList;
    private List<Long>buddyList;
    private Boolean linked;
    private ProductLevel1 productLevel1;
    private ProductLevel2 productLevel2;
    private ProductLevel3 productLevel3;
    private ProductLevel4 productLevel4;
    private ProductLevel5 productLevel5;
    private ProductLevel6 productLevel6;
    private ProductLevel7 productLevel7;
    private Integer teamNumber;
}
