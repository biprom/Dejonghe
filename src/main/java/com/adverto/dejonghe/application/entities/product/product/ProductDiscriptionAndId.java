package com.adverto.dejonghe.application.entities.product.product;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDiscriptionAndId {
    @Id
    private String id;
    private String discription;
}
