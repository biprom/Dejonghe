package com.adverto.dejonghe.application.entities.product.product;

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
public class ProductLink {
    @Id
    private String id;
    private LocalDate date;
    private String link;
}
