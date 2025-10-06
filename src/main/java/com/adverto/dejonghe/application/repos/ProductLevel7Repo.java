package com.adverto.dejonghe.application.repos;

import com.adverto.dejonghe.application.entities.product.product.ProductLevel5;
import com.adverto.dejonghe.application.entities.product.product.ProductLevel7;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductLevel7Repo extends MongoRepository<ProductLevel7, String> {
    Long removeById(String id);
    @Query(value="{ 'productLevel6.name' : ?0 , 'productLevel6.productLevel5.name' : ?1 , 'productLevel6.productLevel5.productLevel4.name' : ?2 , 'productLevel6.productLevel5.productLevel4.productLevel3.name' : ?3 , 'productLevel6.productLevel5.productLevel4.productLevel3.productLevel2.name' : ?4 , 'productLevel6.productLevel5.productLevel4.productLevel3.productLevel2.productLevel1.name' : ?5 }")
    List<ProductLevel7> findByPreviousLevelNames(String productlevel6,String productLevel5,String productLevel4,String productLevel3,String productLevel2,String productLevel1);
}
