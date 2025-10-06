package com.adverto.dejonghe.application.dbservices;

import com.adverto.dejonghe.application.entities.product.product.ProductDiscriptionAndId;
import com.adverto.dejonghe.application.entities.product.product.ProductLevel1;
import com.adverto.dejonghe.application.entities.product.product.ProductLevel2;
import com.adverto.dejonghe.application.entities.product.product.ProductLevel3;
import com.adverto.dejonghe.application.repos.ProductLevel3Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductLevel3Service {
    @Autowired
    ProductLevel3Repo productLevel3Repo;

    public Optional<List<ProductLevel3>> getAllProductLevel3() {
        List<ProductLevel3> level3List = productLevel3Repo.findAll();
        if (!level3List.isEmpty()) {
            Collections.sort(level3List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
            return Optional.of(level3List);
        }
        else{
            return Optional.empty();
        }
    }

    public void saveProductlevelItems(List<String>stringList, ProductLevel2 productLevel2){
        for(String string : stringList){
            ProductLevel3 productLevel3 = new ProductLevel3();
            productLevel3.setName(string);
            productLevel3.setProductLevel2(productLevel2);
            productLevel3.setTime(LocalDateTime.now());
            productLevel3Repo.save(productLevel3);
        }
    }

    public Optional<List<ProductDiscriptionAndId>> getProductLevel3NamesAndLevelAndId() {
        List<ProductLevel3> level3List = productLevel3Repo.findAll();
        if (!level3List.isEmpty()) {
            Collections.sort(level3List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
            return Optional.of(level3List.stream().map(x -> new ProductDiscriptionAndId(x.getId(),x.getName() + " - "
                    + x.getProductLevel2().getName()+ " - "
                    + x.getProductLevel2().getProductLevel1().getName())).collect(Collectors.toList()));
        }
        else{
            return Optional.empty();
        }
    }

    public void removeById(String id) {
        if(id != null && !id.isEmpty()){
            productLevel3Repo.removeById(id);
        }
    }

    public Optional<List<ProductLevel3>> getProductLevel3sFromPreviousLevels(ProductLevel2 productLevel2, ProductLevel1 productLevel1) {
        List<ProductLevel3> level3List = productLevel3Repo.findByPreviousLevelNames(productLevel2.getName(),productLevel1.getName());
        if (!level3List.isEmpty()) {
            Collections.sort(level3List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
            return Optional.of(level3List);
        }
        else{
            return Optional.empty();
        }
    }
}
