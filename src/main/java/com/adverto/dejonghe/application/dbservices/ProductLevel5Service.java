package com.adverto.dejonghe.application.dbservices;

import com.adverto.dejonghe.application.entities.product.product.*;
import com.adverto.dejonghe.application.repos.ProductLevel5Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductLevel5Service {
    @Autowired
    ProductLevel5Repo productLevel5Repo;

    public Optional<List<ProductLevel5>> getAllProductLevel5() {
        List<ProductLevel5> level5List = productLevel5Repo.findAll();
        if (!level5List.isEmpty()) {
            Collections.sort(level5List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
            return Optional.of(level5List);
        }
        else{
            return Optional.empty();
        }
    }

    public void saveProductlevelItems(List<String>stringList, ProductLevel4 productLevel4){
        for(String string : stringList){
            ProductLevel5 productLevel5 = new ProductLevel5();
            productLevel5.setName(string);
            productLevel5.setProductLevel4(productLevel4);
            productLevel5.setTime(LocalDateTime.now());
            productLevel5Repo.save(productLevel5);
        }
    }

    public Optional<List<ProductDiscriptionAndId>> getProductLevel5NamesAndLevelAndId() {
        List<ProductLevel5> level5List = productLevel5Repo.findAll();
        if (!level5List.isEmpty()) {
            Collections.sort(level5List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
            return Optional.of(level5List.stream().map(x -> new ProductDiscriptionAndId(x.getId(),x.getName()+ " - "
                    + x.getProductLevel4().getName()+ " - "
                    + x.getProductLevel4().getProductLevel3().getName()+ " - "
                    + x.getProductLevel4().getProductLevel3().getProductLevel2().getName()+ " - "
                    + x.getProductLevel4().getProductLevel3().getProductLevel2().getProductLevel1().getName())).collect(Collectors.toList()));
        }
        else{
            return Optional.empty();
        }
    }

    public void removeById(String id) {
        if(id != null && !id.isEmpty()){
            productLevel5Repo.removeById(id);
        }
    }

    public Optional<List<ProductLevel5>> getProductLevel5ByPreviousLevelNames(ProductLevel4 productLevel4,ProductLevel3 productLevel3,ProductLevel2 productLevel2,ProductLevel1 productLevel1) {
        List<ProductLevel5> level5List = productLevel5Repo.findByPreviousLevelNames(productLevel4.getName(),productLevel3.getName(),productLevel2.getName(),productLevel1.getName());
        if (!level5List.isEmpty()) {
            Collections.sort(level5List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
            return Optional.of(level5List);
        }
        else{
            return Optional.empty();
        }
    }
}
