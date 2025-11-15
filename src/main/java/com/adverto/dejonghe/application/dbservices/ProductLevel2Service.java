package com.adverto.dejonghe.application.dbservices;

import com.adverto.dejonghe.application.entities.product.product.ProductDiscriptionAndId;
import com.adverto.dejonghe.application.entities.product.product.ProductLevel1;
import com.adverto.dejonghe.application.entities.product.product.ProductLevel2;
import com.adverto.dejonghe.application.repos.ProductLevel2Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductLevel2Service {
    @Autowired
    ProductLevel2Repo productLevel2Repo;

    public Optional<List<ProductLevel2>> getProductLevel2sFromPreviousLevels(ProductLevel1 productLevel1) {
        List<ProductLevel2> level2List = productLevel2Repo.findByPreviousLevelNames(productLevel1.getName());
        if (!level2List.isEmpty()) {
            Collections.sort(level2List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
            return Optional.of(level2List);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<List<ProductLevel2>> getAllProductLevel2() {
        List<ProductLevel2> level2List = productLevel2Repo.findAll();
        Collections.sort(level2List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
        if (!level2List.isEmpty()) {
            return Optional.of(level2List);
        }
        else{
            return Optional.empty();
        }
    }

    public void saveProductlevelItems(List<String>stringList, ProductLevel1 productLevel1) {
        for(String string : stringList){
            ProductLevel2 productLevel2 = new ProductLevel2();
            productLevel2.setName(string);
            productLevel2.setProductLevel1(productLevel1);
            productLevel2.setTime(LocalDateTime.now());
            productLevel2Repo.save(productLevel2);
        }
    }

    public Optional<List<ProductDiscriptionAndId>> getProductLevel2NamesAndId() {
        List<ProductLevel2> level2List = productLevel2Repo.findAll();
        Collections.sort(level2List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
        if (!level2List.isEmpty()) {
            return Optional.of(level2List.stream().map(x -> new ProductDiscriptionAndId(x.getId(),x.getName()  + " - " + x.getProductLevel1().getName())).collect(Collectors.toList()));
        }
        else{
            return Optional.empty();
        }
    }

    public void removeById(String id) {
        if(id != null && !id.isEmpty()){
            productLevel2Repo.removeById(id);
        }
    }

    public Optional<ProductLevel2> getProductLevel2ByName(String name) {
        List<ProductLevel2> level2List = productLevel2Repo.findAll();
        if (!level2List.isEmpty()) {
            Collections.sort(level2List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
            return Optional.of(level2List.stream().filter(item -> item.getName().toLowerCase().matches(name.toLowerCase())).findFirst().orElse(null));
        }
        else{
            return Optional.empty();
        }
    }
}
