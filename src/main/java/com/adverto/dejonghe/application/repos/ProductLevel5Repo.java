package com.adverto.dejonghe.application.repos;

import com.adverto.dejonghe.application.entities.product.product.ProductLevel3;
import com.adverto.dejonghe.application.entities.product.product.ProductLevel5;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductLevel5Repo extends MongoRepository<ProductLevel5, String> {
    Long removeById(String id);
    @Query(value="{ 'productLevel4.name' : ?0 , 'productLevel4.productLevel3.name' : ?1 ,  'productLevel4.productLevel3.productLevel2.name' : ?2 , 'productLevel4.productLevel3.productLevel2.productLevel1.name' : ?3}")
    List<ProductLevel5> findByPreviousLevelNames(String productlevel4, String productLevel3, String productLevel2, String productLevel1);
}
