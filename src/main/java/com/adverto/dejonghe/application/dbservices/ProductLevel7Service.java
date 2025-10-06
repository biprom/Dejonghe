package com.adverto.dejonghe.application.dbservices;

import com.adverto.dejonghe.application.entities.product.product.*;
import com.adverto.dejonghe.application.repos.ProductLevel7Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductLevel7Service {
    @Autowired
    ProductLevel7Repo productLevel7Repo;

    public Optional<List<ProductLevel7>> getAllProductLevel7() {
        List<ProductLevel7> level7List = productLevel7Repo.findAll();
        if (!level7List.isEmpty()) {
            Collections.sort(level7List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
            return Optional.of(level7List);
        }
        else{
            return Optional.empty();
        }
    }

    public void saveProductlevelItems(List<String>stringList, ProductLevel6 productLevel6){
        for(String string : stringList){
            ProductLevel7 productLevel7 = new ProductLevel7();
            productLevel7.setName(string);
            productLevel7.setProductLevel6(productLevel6);
            productLevel7.setTime(LocalDateTime.now());
            productLevel7Repo.save(productLevel7);
        }
    }

    public Optional<List<ProductDiscriptionAndId>> getProductLevel7NamesAndLevelAndId() {
        List<ProductLevel7> level7List = productLevel7Repo.findAll();
        if (!level7List.isEmpty()) {
            Collections.sort(level7List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
            return Optional.of(level7List.stream().map(x -> new ProductDiscriptionAndId(x.getId(),x.getName()+ " - "
                    + x.getProductLevel6().getName()+ " - "
                    + x.getProductLevel6().getProductLevel5().getName()+ " - "
                    + x.getProductLevel6().getProductLevel5().getProductLevel4().getName()+ " - "
                    + x.getProductLevel6().getProductLevel5().getProductLevel4().getProductLevel3().getName()+ " - "
                    + x.getProductLevel6().getProductLevel5().getProductLevel4().getProductLevel3().getProductLevel2().getName()+ " - "
                    + x.getProductLevel6().getProductLevel5().getProductLevel4().getProductLevel3().getProductLevel2().getProductLevel1().getName())).collect(Collectors.toList()));
        }
        else{
            return Optional.empty();
        }
    }

    public void removeById(String id) {
        if(id != null && !id.isEmpty()){
            productLevel7Repo.removeById(id);
        }
    }

    public Optional<List<ProductLevel7>> getProductLevel7ByPreviousLevelNames(ProductLevel6 productLevel6,ProductLevel5 productLevel5,ProductLevel4 productLevel4,ProductLevel3 productLevel3,ProductLevel2 productLevel2,ProductLevel1 productLevel1) {
        List<ProductLevel7> level7List = productLevel7Repo.findByPreviousLevelNames(productLevel6.getName(),
                productLevel5.getName(),productLevel4.getName(),productLevel3.getName(),productLevel2.getName(),productLevel1.getName());
        if (!level7List.isEmpty()) {
            Collections.sort(level7List, (o1, o2) -> (o1.getName().compareTo(o2.getName())));
            return Optional.of(level7List);
        }
        else{
            return Optional.empty();
        }
    }
}
