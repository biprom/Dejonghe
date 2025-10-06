package com.adverto.dejonghe.application.dbservices;

import com.adverto.dejonghe.application.entities.product.product.*;
import com.adverto.dejonghe.application.repos.ProductLevel6Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductLevel6Service {
    @Autowired
    ProductLevel6Repo productLevel6Repo;

    public Optional<List<ProductLevel6>> getAllProductLevel6() {
        List<ProductLevel6> level6List = productLevel6Repo.findAll();
        if (!level6List.isEmpty()) {
            Collections.sort(level6List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
            return Optional.of(level6List);
        }
        else{
            return Optional.empty();
        }
    }

    public void saveProductlevelItems(List<String>stringList, ProductLevel5 productLevel5){
        for(String string : stringList){
            ProductLevel6 productLevel6 = new ProductLevel6();
            productLevel6.setName(string);
            productLevel6.setProductLevel5(productLevel5);
            productLevel6.setTime(LocalDateTime.now());
            productLevel6Repo.save(productLevel6);
        }
    }

    public Optional<List<ProductDiscriptionAndId>> getProductLevel6NamesAndLevelAndId() {
        List<ProductLevel6> level6List = productLevel6Repo.findAll();
        if (!level6List.isEmpty()) {
            Collections.sort(level6List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
            return Optional.of(level6List.stream().map(x -> new ProductDiscriptionAndId(x.getId(),x.getName()+ " - "
                    + x.getProductLevel5().getName()+ " - "
                    + x.getProductLevel5().getProductLevel4().getName()+ " - "
                    + x.getProductLevel5().getProductLevel4().getProductLevel3().getName()+ " - "
                    + x.getProductLevel5().getProductLevel4().getProductLevel3().getProductLevel2().getName()+ " - "
                    + x.getProductLevel5().getProductLevel4().getProductLevel3().getProductLevel2().getProductLevel1().getName())).collect(Collectors.toList()));
        }
        else{
            return Optional.empty();
        }
    }

    public void removeById(String id) {
        if(id != null && !id.isEmpty()){
            productLevel6Repo.removeById(id);
        }
    }

    public Optional<List<ProductLevel6>> getProductLevel6ByPreviousLevelNames(ProductLevel5 productLevel5, ProductLevel4 productLevel4, ProductLevel3 productLevel3, ProductLevel2 productLevel2, ProductLevel1 productLevel1) {
        List<ProductLevel6> level6List = productLevel6Repo.findByPreviousLevelNames(productLevel5.getName(),productLevel4.getName(),productLevel3.getName(),productLevel2.getName(),productLevel1.getName());
        if (!level6List.isEmpty()) {
            Collections.sort(level6List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
            return Optional.of(level6List);
        }
        else{
            return Optional.empty();
        }
    }
}
