package com.adverto.dejonghe.application.repos;

import com.adverto.dejonghe.application.entities.product.product.ProductLevel3;
import com.adverto.dejonghe.application.entities.product.product.ProductLevel4;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductLevel4Repo extends MongoRepository<ProductLevel4, String> {
    Long removeById(String id);
    @Query(value="{ 'productLevel3.name' : ?0 , 'productLevel3.productLevel2.name' : ?1 ,  'productLevel3.productLevel2.productLevel1.name' : ?2}")
    List<ProductLevel4> findByPreviousLevelNames(String productlevel3, String productLevel2, String productLevel1);
}
