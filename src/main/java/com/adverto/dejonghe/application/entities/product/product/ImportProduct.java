package com.adverto.dejonghe.application.entities.product.product;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Document
@Getter
@Setter
@NoArgsConstructor
public class ImportProduct implements Serializable {
    @Id
    private String id;
    private String productCode;
    private String internalName;
    private String purchasePrice;
    private String sellPrice;
    private String sellMargin;
}
