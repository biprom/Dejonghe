package com.adverto.dejonghe.application.repos;

import com.adverto.dejonghe.application.entities.product.product.ProductLevel1;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductLevel1Repo extends MongoRepository<ProductLevel1, String> {
    Long removeById(String id);
}
