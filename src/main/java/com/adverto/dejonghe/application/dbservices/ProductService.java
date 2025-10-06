package com.adverto.dejonghe.application.dbservices;

import com.adverto.dejonghe.application.entities.product.product.Product;
import com.adverto.dejonghe.application.repos.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class ProductService {
    @Autowired
    ProductRepo productRepo;

    public Optional<List<Product>>findByProductCodeContaining(String productCode) {
        Optional<List<Product>> optionalProducts = Optional.of(productRepo.findByProductCodeContainsIgnoreCase(productCode));
        return optionalProducts;
    }

    public Optional<List<Product>>findByProductCodeEqualCaseInsensitive(String productCode) {
        Optional<List<Product>> optionalProducts = Optional.of(productRepo.findByProductCodeEqualsIgnoreCase(productCode));
        return optionalProducts;
    }


    public void delete(Product product) {
        productRepo.delete(product);
    }


    public void save(Product newProduct) {
        if (newProduct.getInternalName() != null) {
            productRepo.save(newProduct);
        }
    }

    public Optional<Product> get(String id) {
        Optional<Product> optionalProduct = productRepo.findById(id);
        return optionalProduct;
    }

    public Optional<List<Product>> getAllProducts() {
        List<Product> products = productRepo.findAll();
        if (!products.isEmpty()) {
            return Optional.of(products);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<List<Product>> getProductByInternalNameOrCode(String internalName, String code) {
        return Optional.of(productRepo.findByInternalNameIgnoreCaseContainingOrProductCodeIgnoreCaseContaining(internalName,code));
    }

    public Optional<List<Product>> getProductByInternalNameOrCodeOrComment(String internalName, String code, String comment) {
        String nameRegex = prepareFlexibleRegex(internalName);
        String codeRegex = prepareFlexibleRegex(code);
        String commentRegex = prepareFlexibleRegex(comment);
        return Optional.of(productRepo.searchByNormalizedText(nameRegex,codeRegex,commentRegex));
    }

    private String prepareFlexibleRegex(String input) {
        if (input == null || input.isBlank()) {
            return ".*"; // matcht alles
        }

        // Escapen van regex-speciale karakters behalve , en .
        String escaped = Pattern.quote(input);

        // Vervang komma of punt in de input door regex die beide toestaat
        String flexible = escaped
                .replace(",", "[.,]")
                .replace("\\.", "[.,]");

        // Haal extra Pattern.quote escaping weg
        if (flexible.startsWith("\\Q") && flexible.endsWith("\\E")) {
            flexible = flexible.substring(2, flexible.length() - 2);
        }

        return ".*" + flexible + ".*";
    }

    public Optional<List<Product>> getAllProductsByCategory(String level1, String level2, String level3, String level4, String level5, String level6,String level7) {
        List<Product> products = productRepo.findByProductLevel7_nameIsAndProductLevel6_nameIsAndProductLevel5_nameIsAndProductLevel4_nameIsAndProductLevel3_nameIsAndProductLevel2_nameIsAndProductLevel1_nameIs(level1, level2, level3, level4, level5, level6, level7);
        if (!products.isEmpty()) {
            return Optional.of(products);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<List<Product>> getAllProductsByCategory(String level1, String level2, String level3, String level4, String level5, String level6) {
        List<Product> products = productRepo.findByProductLevel6_nameIsAndProductLevel5_nameIsAndProductLevel4_nameIsAndProductLevel3_nameIsAndProductLevel2_nameIsAndProductLevel1_nameIs(level1, level2, level3, level4, level5, level6);
        if (!products.isEmpty()) {
            return Optional.of(products);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<List<Product>> getAllProductsByCategory(String level1, String level2, String level3, String level4, String level5) {
        List<Product> products = productRepo.findByProductLevel5_nameIsAndProductLevel4_nameIsAndProductLevel3_nameIsAndProductLevel2_nameIsAndProductLevel1_nameIs(level1, level2, level3, level4, level5);
        if (!products.isEmpty()) {
            return Optional.of(products);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<List<Product>> getAllProductsByCategory(String level1, String level2, String level3, String level4) {
        List<Product> products = productRepo.findByProductLevel4_nameIsAndProductLevel3_nameIsAndProductLevel2_nameIsAndProductLevel1_nameIs(level1, level2, level3, level4);
        if (!products.isEmpty()) {
            return Optional.of(products);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<List<Product>> getAllProductsByCategory(String level1, String level2, String level3) {
        List<Product> products = productRepo.findByProductLevel3_nameIsAndProductLevel2_nameIsAndProductLevel1_nameIs(level1, level2, level3);
        if (!products.isEmpty()) {
            return Optional.of(products);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<List<Product>> getAllProductsByCategory(String level1, String level2) {
        List<Product> products = productRepo.findByProductLevel2_nameIsAndProductLevel1_nameIs(level1, level2);
        if (!products.isEmpty()) {
            return Optional.of(products);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<List<Product>> getAllProductsByCategory(String level1) {
        List<Product> products = productRepo.findByProductLevel1_nameIs(level1);
        if (!products.isEmpty()) {
            return Optional.of(products);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<List<Product>> getAllProductsByCategoryPTAPowder() {
        List<Product> products = productRepo.findByProductLevel3_nameIsAndProductLevel2_nameIsAndProductLevel1_nameIs("PTA Poeder","Oppervlakte behandeling","Grondstoffen");
        if (!products.isEmpty()) {
            return Optional.of(products);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<Product> getWorkhourForRegularLocalAgro(){
        return productRepo.findById("6878b9da8ebc04229d261455");
    }
    public Optional<Product> getWorkhourForRegularOnTheMoveAgro(){
        return productRepo.findById("6878b9da8ebc04229d261456");
    }
    public Optional<Product> getWorkhourForRegularLocalIndustrie(){
        return productRepo.findById("6878ba088ebc04229d261461");
    }
    public Optional<Product> getWorkhourForOnRegularTheMoveIndustrie(){
        return productRepo.findById("6878ba088ebc04229d261462");
    }


    public Optional<Product> getWorkhourForCentrifugeLocalAgro(){
        return productRepo.findById("6878b9da8ebc04229d261457");
    }
    public Optional<Product> getWorkhourForCentrifugeOnTheMoveAgro(){
        return productRepo.findById("6878b9da8ebc04229d261457");
    }
    public Optional<Product> getWorkhourForCentrifugeLocalIndustrie(){
        return productRepo.findById("6878ba088ebc04229d261463");
    }
    public Optional<Product> getWorkhourForOnCentrifugeTheMoveIndustrie(){
        return productRepo.findById("6878ba088ebc04229d261463");
    }


    public Optional<Product> getWorkhourForProgammationLocalAgro(){
        return productRepo.findById("6878b9da8ebc04229d261458");
    }
    public Optional<Product> getWorkhourForProgammationOnTheMoveAgro(){
        return productRepo.findById("6878b9da8ebc04229d261458");
    }
    public Optional<Product> getWorkhourForProgammationLocalIndustrie(){
        return productRepo.findById("6878ba088ebc04229d261464");
    }
    public Optional<Product> getWorkhourForOnProgammationTheMoveIndustrie(){
        return productRepo.findById("6878ba088ebc04229d261464");
    }

    public Optional<Product> getRegularKmAgro(){
        return productRepo.findById("6878b9da8ebc04229d26145e");
    }

    public Optional<Product> getRegularCraneAgro(){
        return productRepo.findById("6878b9da8ebc04229d26145f");
    }

    public Optional<Product> getRegularTrailerAgro(){
        return productRepo.findById("6878b9da8ebc04229d261460");
    }

    public Optional<Product> getRegularKmIndustry(){
        return productRepo.findById("6878ba088ebc04229d261469");
    }

    public Optional<Product> getRegularCraneIndustry(){
        return productRepo.findById("6878ba088ebc04229d26146a");
    }

    public Optional<Product> getRegularTrailerIndustry(){
        return productRepo.findById("6878ba088ebc04229d26146b");
    }

    public Optional<Product> getWorkHoursCraneRegularAgro(){
        return productRepo.findById("6878b9da8ebc04229d26145a");
    }

    public Optional<Product> getWorkHoursCraneIntenseAgro(){
        return productRepo.findById("6878b9da8ebc04229d26145b");
    }

    public Optional<Product> getWorkHoursCraneForfaitAgro(){
        return productRepo.findById("6878b9da8ebc04229d26145c");
    }

    public Optional<Product> getWorkHoursCraneRegularIndustry(){
        return productRepo.findById("6878ba088ebc04229d261465");
    }

    public Optional<Product> getWorkHoursCraneIntenseIndustry(){
        return productRepo.findById("6878ba088ebc04229d261466");
    }

    public Optional<Product> getWorkHoursCraneForfaitIndustry(){
        return productRepo.findById("6878ba088ebc04229d261467");
    }


}
