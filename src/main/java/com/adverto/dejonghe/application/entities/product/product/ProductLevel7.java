package com.adverto.dejonghe.application.entities.product.product;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Getter
@Setter
@NoArgsConstructor
public class ProductLevel7 {
    @Id
    private String id;
    private String name;
    private ProductLevel6 productLevel6;
    private LocalDateTime time;
}
