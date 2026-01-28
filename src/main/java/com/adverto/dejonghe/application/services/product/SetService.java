package com.adverto.dejonghe.application.services.product;

import com.adverto.dejonghe.application.dbservices.ProductService;
import com.adverto.dejonghe.application.entities.product.product.Product;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SetService {


    private final ProductService productService;
    private boolean bRemoveItem = false;
    private Product productToRemove;

    public SetService(ProductService productService) {
        this.productService = productService;
    }

    public Double tryToCalculatePurchasePrice(Product set) {
        try{
            if((set.getSetList() != null) && (set.getSetList().size() > 0)){
                Double totalPurchasePrice = set.getSetList().stream().filter(product -> product.getPurchasePrice() != null).map(product -> product.getSelectedAmount() * product.getPurchasePrice()).reduce(0.0, Double::sum);
                return totalPurchasePrice;
            }
            else{
                return 0.0;
            }
        }
        catch (Exception e){
            return 0.0;
        }
    }

    public Double tryToCalculateSellAgroPrice(Product set) {
        try{
            if((set.getSetList() != null) && (set.getSetList().size() > 0)){
                Double totalSellPriceSetAgro = set.getSetList().stream().filter(product -> product.getSellPrice() != null).map(product -> product.getSelectedAmount() * product.getSellPrice()).reduce(0.0, Double::sum);
                return totalSellPriceSetAgro;
            }
            else{
                return 0.0;
            }
        }
        catch (Exception e){
            return 0.0;
        }
    }


    public Double tryToCalculateSellIndustryPrice(Product set) {
        try {
            if (set.getSetList() != null && !set.getSetList().isEmpty()) {

                return set.getSetList().stream()
                        .map(product -> {
                            Double industryPrice = product.getSellPriceIndustry();
                            Double agroPrice = product.getSellPrice();

                            Double priceToUse =
                                    (industryPrice != null && industryPrice != 0.0)
                                            ? industryPrice
                                            : agroPrice;

                            return product.getSelectedAmount() * (priceToUse != null ? priceToUse : 0.0);
                        })
                        .reduce(0.0, Double::sum);
            } else {
                return 0.0;
            }
        } catch (Exception e) {
            return 0.0;
        }
    }


    public Optional<List<Product>> removeItemFromSetsAndRecalculateSet(Product selectedProduct) {
        Optional<List<Product>> allSetsContainingselectedProduct = productService.getAllSetsContaining(selectedProduct);
        if(allSetsContainingselectedProduct.isPresent()){
            for(Product set : allSetsContainingselectedProduct.get()){
                if((set.getSetList() != null) && (set.getSetList().size() > 0)){
                    for(Product product : set.getSetList()){
                        productToRemove = product;
                        boolean matchesCode = selectedProduct.getProductCode().equals(product.getProductCode());
                        boolean matchesFolder = productService.matchesLevel(selectedProduct, product.getProductLevel1(),
                                product.getProductLevel2(),
                                product.getProductLevel3(),
                                product.getProductLevel4(),
                                product.getProductLevel5(),
                                product.getProductLevel6(),
                                product.getProductLevel7());
                        if(matchesCode && matchesFolder){
                            bRemoveItem = true;
                        }
                        else{
                            bRemoveItem = false;
                        }
                    }
                    if(bRemoveItem){
                        set.getSetList().remove(productToRemove);
                    }
                    else{

                    }
                }
                set.setPurchasePrice(tryToCalculatePurchasePrice(set));
                set.setSellPrice(tryToCalculateSellAgroPrice(set));
                set.setSellPriceIndustry(tryToCalculateSellIndustryPrice(set));
                productService.save(set);
            }
        }
        return allSetsContainingselectedProduct;
    }
}
