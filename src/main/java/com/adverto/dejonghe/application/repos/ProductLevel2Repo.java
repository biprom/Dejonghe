package com.adverto.dejonghe.application.repos;

import com.adverto.dejonghe.application.entities.product.product.ProductLevel2;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductLevel2Repo extends MongoRepository<ProductLevel2, String> {
    Long removeById(String id);
    @Query(value="{ 'productLevel1.name' : ?0 }")
    List<ProductLevel2> findByPreviousLevelNames(String level1);
}
