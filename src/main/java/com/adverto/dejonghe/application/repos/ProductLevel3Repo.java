package com.adverto.dejonghe.application.repos;

import com.adverto.dejonghe.application.entities.product.product.ProductLevel2;
import com.adverto.dejonghe.application.entities.product.product.ProductLevel3;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductLevel3Repo extends MongoRepository<ProductLevel3, String> {
    Long removeById(String id);
    @Query(value="{ 'productLevel2.name' : ?0 , 'productLevel2.productLevel1.name' : ?1}")
    List<ProductLevel3> findByPreviousLevelNames(String level2Name, String productLevel1Name);
}
