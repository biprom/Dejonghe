package com.adverto.dejonghe.application.dbservices;

import com.adverto.dejonghe.application.entities.product.product.*;
import com.adverto.dejonghe.application.repos.ProductLevel4Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductLevel4Service {
    @Autowired
    ProductLevel4Repo productLevel4Repo;

    public Optional<List<ProductLevel4>> getAllProductLevel4() {
        List<ProductLevel4> level4List = productLevel4Repo.findAll();
        if (!level4List.isEmpty()) {
            Collections.sort(level4List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
            return Optional.of(level4List);
        }
        else{
            return Optional.empty();
        }
    }

    public void saveProductlevelItems(List<String>stringList, ProductLevel3 productLevel3){
        for(String string : stringList){
            ProductLevel4 productLevel4 = new ProductLevel4();
            productLevel4.setName(string);
            productLevel4.setProductLevel3(productLevel3);
            productLevel4.setTime(LocalDateTime.now());
            productLevel4Repo.save(productLevel4);
        }
    }

    public Optional<List<ProductDiscriptionAndId>> getProductLevel4NamesAndLevelAndId() {
        List<ProductLevel4> level4List = productLevel4Repo.findAll();
        if (!level4List.isEmpty()) {
            Collections.sort(level4List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
            return Optional.of(level4List.stream().map(x -> new ProductDiscriptionAndId(x.getId(),x.getName()+ " - "
                    + x.getProductLevel3().getName()+ " - "
                    + x.getProductLevel3().getProductLevel2().getName()+ " - "
                    + x.getProductLevel3().getProductLevel2().getProductLevel1().getName())).collect(Collectors.toList()));
        }
        else{
            return Optional.empty();
        }
    }

    public void removeById(String id) {
        if(id != null && !id.isEmpty()){
            productLevel4Repo.removeById(id);
        }
    }

    public Optional<List<ProductLevel4>> getProductLevel4ByPreviousLevelNames(ProductLevel3 productLevel3, ProductLevel2 productLevel2, ProductLevel1 productLevel1) {
        List<ProductLevel4> level4List = productLevel4Repo.findByPreviousLevelNames(productLevel3.getName(),productLevel2.getName(),productLevel1.getName());
        if (!level4List.isEmpty()) {
            Collections.sort(level4List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
            return Optional.of(level4List);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<ProductLevel4> getProductLevel4ByName(String name) {
        List<ProductLevel4> level4List = productLevel4Repo.findAll();
        if (!level4List.isEmpty()) {
            Collections.sort(level4List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
            return Optional.of(level4List.stream().filter(item -> item.getName().toLowerCase().matches(name.toLowerCase())).findFirst().orElse(null));
        }
        else{
            return Optional.empty();
        }
    }
}
