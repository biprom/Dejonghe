package com.adverto.dejonghe.application.services.product;

import com.adverto.dejonghe.application.dbservices.ProductService;
import com.adverto.dejonghe.application.entities.product.product.Product;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ProductServices {

    @Autowired
    ProductService productService;

    List<Product>coupledProductList = new ArrayList<>();

    public ProductServices(ProductService productService) {
        this.productService = productService;
    }

    public Optional<List<Product>> getCoupledProducts(Product product){

        coupledProductList.clear();

        if((product.getProductCode() != null) && (product.getProductCode().startsWith("RVS-B-"))){
            String size = String.valueOf(extractNumberBeforeX(product.getProductCode()));

            //find nut for this bolt
            Optional<List<Product>> optNuts = productService.findByProductCodeEqualCaseInsensitive("RVS-M-M" + size);
            if((!optNuts.isEmpty()) && (optNuts.isPresent())){
                //optNuts.get().stream().forEach(item -> item.setSelectedAmount(product.getSelectedAmount()));
                coupledProductList.addAll(optNuts.get().stream().filter(item -> item.getProductLevel1().getName().contains("Montagemateriaal")).collect(Collectors.toList()));
            }

            //find nut for this bolt
            Optional<List<Product>> optGuarantNuts = productService.findByProductCodeEqualCaseInsensitive("RVS-BM-M" + size);
            if((!optGuarantNuts.isEmpty()) && (optGuarantNuts.isPresent())){
                //optGuarantNuts.get().stream().forEach(item -> item.setSelectedAmount(product.getSelectedAmount()));
                coupledProductList.addAll(optGuarantNuts.get().stream().filter(item -> item.getProductLevel1().getName().contains("Montagemateriaal")).collect(Collectors.toList()));
            }

            //find small round for this bolt
            Optional<List<Product>> optSmallRound = productService.findByProductCodeEqualCaseInsensitive("RVS-VK-M" +size);
            if((!optSmallRound.isEmpty()) && (optSmallRound.isPresent())){
                //optSmallRound.get().stream().forEach(item -> item.setSelectedAmount(product.getSelectedAmount() * 2));
                coupledProductList.addAll(optSmallRound.get().stream().filter(item -> item.getProductLevel1().getName().contains("Montagemateriaal")).collect(Collectors.toList()));
            }

            //find large round for this bolt
            Optional<List<Product>> optLargeRound = productService.findByProductCodeEqualCaseInsensitive("RVS-VG-M" +size);
            if((!optLargeRound.isEmpty()) && (optLargeRound.isPresent())){
                //optLargeRound.get().stream().forEach(item -> item.setSelectedAmount(product.getSelectedAmount() * 2));
                coupledProductList.addAll(optLargeRound.get().stream().filter(item -> item.getProductLevel1().getName().contains("Montagemateriaal")).collect(Collectors.toList()));
            }

            //find spring round for this bolt
            Optional<List<Product>> optSpringRound = productService.findByProductCodeEqualCaseInsensitive("RVS-SV-M" +size);
            if((!optSpringRound.isEmpty()) && (optSpringRound.isPresent())){
                //optSpringRound.get().stream().forEach(item -> item.setSelectedAmount(product.getSelectedAmount() * 2));
                coupledProductList.addAll(optSpringRound.get().stream().filter(item -> item.getProductLevel1().getName().contains("Montagemateriaal")).collect(Collectors.toList()));
            }
        }

        if((product.getProductCode() != null) && (product.getProductCode().startsWith("RVS-IB-"))){
            String size = String.valueOf(extractNumberBeforeX(product.getProductCode()));

            //find nut for this bolt
            Optional<List<Product>> optNuts = productService.findByProductCodeEqualCaseInsensitive("RVS-M-M" + size);
            if((!optNuts.isEmpty()) && (optNuts.isPresent())){
                //optNuts.get().stream().forEach(item -> item.setSelectedAmount(product.getSelectedAmount()));
                coupledProductList.addAll(optNuts.get().stream().filter(item -> item.getProductLevel1().getName().contains("Montagemateriaal")).collect(Collectors.toList()));
            }

            //find nut for this bolt
            Optional<List<Product>> optGuarantNuts = productService.findByProductCodeEqualCaseInsensitive("RVS-BM-M" + size);
            if((!optGuarantNuts.isEmpty()) && (optGuarantNuts.isPresent())){
                //optGuarantNuts.get().stream().forEach(item -> item.setSelectedAmount(product.getSelectedAmount()));
                coupledProductList.addAll(optGuarantNuts.get().stream().filter(item -> item.getProductLevel1().getName().contains("Montagemateriaal")).collect(Collectors.toList()));
            }

            //find small round for this bolt
            Optional<List<Product>> optSmallRound = productService.findByProductCodeEqualCaseInsensitive("RVS-VK-M" +size);
            if((!optSmallRound.isEmpty()) && (optSmallRound.isPresent())){
                //optSmallRound.get().stream().forEach(item -> item.setSelectedAmount(product.getSelectedAmount() * 2));
                coupledProductList.addAll(optSmallRound.get().stream().filter(item -> item.getProductLevel1().getName().contains("Montagemateriaal")).collect(Collectors.toList()));
            }

            //find large round for this bolt
            Optional<List<Product>> optLargeRound = productService.findByProductCodeEqualCaseInsensitive("RVS-VG-M" +size);
            if((!optLargeRound.isEmpty()) && (optLargeRound.isPresent())){
                //optLargeRound.get().stream().forEach(item -> item.setSelectedAmount(product.getSelectedAmount() * 2));
                coupledProductList.addAll(optLargeRound.get().stream().filter(item -> item.getProductLevel1().getName().contains("Montagemateriaal")).collect(Collectors.toList()));
            }

            //find spring round for this bolt
            Optional<List<Product>> optSpringRound = productService.findByProductCodeEqualCaseInsensitive("RVS-SV-M" +size);
            if((!optSpringRound.isEmpty()) && (optSpringRound.isPresent())){
                //optSpringRound.get().stream().forEach(item -> item.setSelectedAmount(product.getSelectedAmount() * 2));
                coupledProductList.addAll(optSpringRound.get().stream().filter(item -> item.getProductLevel1().getName().contains("Montagemateriaal")).collect(Collectors.toList()));
            }
        }

        return Optional.of(coupledProductList);
    }

    public static int extractNumberBeforeX(String s) {
        // Regex zoekt naar 1 of 2 cijfers voor een 'x'
        Pattern pattern = Pattern.compile("(\\d{1,2})(?=x)");
        Matcher matcher = pattern.matcher(s);

        if (matcher.find()) {
            // Teruggeven als integer
            return Integer.parseInt(matcher.group(1));
        }

        // Geen match gevonden
        throw new IllegalArgumentException("No valid number found before 'x'");
    }

    public void calcSellPriceAgroFromPurchasePriceAndMargin(Product product) {
        try{
            product.setSellPrice(product.getPurchasePrice() *(product.getSellMargin()));
            }
        catch (Exception e){

        }
    }

    public void calcSellPriceIndustryFromPurchasePriceAndMarginIndustry(Product product) {
        try{
            product.setSellPriceIndustry(product.getPurchasePrice() *(product.getSellMarginIndustry()));
        }
        catch (Exception e){

        }
    }
}
