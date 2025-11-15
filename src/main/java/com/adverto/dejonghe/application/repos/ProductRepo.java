package com.adverto.dejonghe.application.repos;

import com.adverto.dejonghe.application.entities.product.product.Product;
import com.adverto.dejonghe.application.entities.product.product.ProductLevel7;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductRepo extends MongoRepository<Product, String> {
    List<Product> findByProductCodeContainsIgnoreCase(String productCode);
    List<Product> findByProductCodeEqualsIgnoreCase(String productCode);
    List<Product> findByInternalNameContainsIgnoreCase(String internalName);
    List<Product> findByCommentContainsIgnoreCase(String comment);
    List<Product> findByInternalNameIgnoreCaseContainingOrProductCodeIgnoreCaseContaining(String internalName, String comment);

    @Query("{$or: [ " +
            "{ 'internalName': { $regex: ?0, $options: 'i' } }, " +
            "{ 'productCode':   { $regex: ?1, $options: 'i' } }, " +
            "{ 'comment':       { $regex: ?2, $options: 'i' } } ] }")
    List<Product> searchByNormalizedText(String internalName, String code, String comment);

    List<Product> findByProductLevel7_nameIsAndProductLevel6_nameIsAndProductLevel5_nameIsAndProductLevel4_nameIsAndProductLevel3_nameIsAndProductLevel2_nameIsAndProductLevel1_nameIs(String productlevel7, String productlevel6, String productLevel5, String productLevel4, String productLevel3, String productLevel2, String productLevel1);
    List<Product> findByProductLevel6_nameIsAndProductLevel5_nameIsAndProductLevel4_nameIsAndProductLevel3_nameIsAndProductLevel2_nameIsAndProductLevel1_nameIs(String productlevel6, String productLevel5, String productLevel4, String productLevel3, String productLevel2, String productLevel1);
    List<Product> findByProductLevel5_nameIsAndProductLevel4_nameIsAndProductLevel3_nameIsAndProductLevel2_nameIsAndProductLevel1_nameIs(String productLevel5, String productLevel4, String productLevel3, String productLevel2, String productLevel1);
    List<Product> findByProductLevel4_nameIsAndProductLevel3_nameIsAndProductLevel2_nameIsAndProductLevel1_nameIs(String productLevel4, String productLevel3, String productLevel2, String productLevel1);
    List<Product> findByProductLevel3_nameIsAndProductLevel2_nameIsAndProductLevel1_nameIs(String productLevel3, String productLevel2, String productLevel1);
    List<Product> findByProductLevel2_nameIsAndProductLevel1_nameIs(String productLevel2, String productLevel1);
    List<Product> findByProductLevel1_nameIs(String productLevel1);
    List<Product> findBySet(Boolean set);
}
