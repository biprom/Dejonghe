package com.adverto.dejonghe.application.dbservices;

import com.adverto.dejonghe.application.entities.product.product.ProductDiscriptionAndId;
import com.adverto.dejonghe.application.entities.product.product.ProductLevel1;
import com.adverto.dejonghe.application.repos.ProductLevel1Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductLevel1Service {
    @Autowired
    ProductLevel1Repo productLevel1Repo;

    public void saveProductlevelItems(List<String>stringList){
        for(String string : stringList){
            ProductLevel1 productLevel1 = new ProductLevel1();
            productLevel1.setName(string);
            productLevel1.setTime(LocalDateTime.now());
            productLevel1Repo.save(productLevel1);
        }
    }

    public Optional<List<ProductLevel1>> getAllProductLevel1() {
        List<ProductLevel1> level1List = productLevel1Repo.findAll();
        if (!level1List.isEmpty()) {
            Collections.sort(level1List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
            return Optional.of(level1List);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<ProductLevel1> getProductLevel1ByName(String name) {
        List<ProductLevel1> level1List = productLevel1Repo.findAll();
        if (!level1List.isEmpty()) {
            Collections.sort(level1List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
            return Optional.of(level1List.stream().filter(item -> item.getName().toLowerCase().matches(name.toLowerCase())).findFirst().orElse(null));
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<List<ProductDiscriptionAndId>> getProductDiscriptionAndId() {
        List<ProductLevel1> level1List = productLevel1Repo.findAll();
        if (!level1List.isEmpty()) {
            Collections.sort(level1List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
            return Optional.of(level1List.stream().map(x -> new ProductDiscriptionAndId(x.getId(),x.getName().toString() + " - Hoofdniveau")).collect(Collectors.toList()));
        }
        else{
            return Optional.empty();
        }
    }

    public void removeById(String id) {
        if(id != null && !id.isEmpty()){
            productLevel1Repo.removeById(id);
        }
    }
}
