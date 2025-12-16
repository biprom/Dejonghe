package com.adverto.dejonghe.application.dbservices;

import com.adverto.dejonghe.application.entities.product.product.*;
import com.adverto.dejonghe.application.repos.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ProductService {
    @Autowired
    ProductRepo productRepo;

    @Autowired
    MongoTemplate mongoTemplate;

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
            productRepo.save(newProduct);
    }

    public Optional<Product> get(String id) {
        Optional<Product> optionalProduct = productRepo.findById(id);
        return optionalProduct;
    }

    public Optional<List<Product>> getProductsById(List<String> idList) {
        Optional<List<Product>> optionalProduct = Optional.of(productRepo.findAllById(idList));
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

    public Optional<List<Product>> getAllSets() {
        List<Product> products = productRepo.findBySet(true);
        if (!products.isEmpty()) {
            return Optional.of(products);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<List<Product>> getAllSetsContaining(Product product) {
        Optional<List<Product>>allSets = getAllSets();
        if (!allSets.isEmpty()) {
            List<Product> collect = allSets.get().stream().filter(set -> set.getSetList().stream().filter(setItem -> setItem.getId().equals(product.getId())).findFirst().isPresent()).collect(Collectors.toList());
            return Optional.of(collect);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<Product> getWorkhourForRegularLocal(){
        return productRepo.findByProductCodeEqualsIgnoreCase("WU-AT").stream().findFirst();
    }
    public Optional<Product> getWorkhourForRegularOnTheMove(){
        return productRepo.findByProductCodeEqualsIgnoreCase("WU-VERP").stream().findFirst();
    }


    public Optional<Product> getWorkhourForCentrifugeLocal(){
        return productRepo.findByProductCodeEqualsIgnoreCase("WU-CEN-AT-").stream().findFirst();
    }
    public Optional<Product> getWorkhourForCentrifugeOnTheMove(){
        return productRepo.findByProductCodeEqualsIgnoreCase("WU-CEN-VERP").stream().findFirst();
    }


    public Optional<Product> getWorkhourForProgammationLocal(){
        return productRepo.findByProductCodeEqualsIgnoreCase("WU-PRO-AT").stream().findFirst();
    }
    public Optional<Product> getWorkhourForProgammationOnTheMove(){
        return productRepo.findByProductCodeEqualsIgnoreCase("WU-PRO-VERP").stream().findFirst();
    }

    public Optional<Product> getRegularKm(){
        return productRepo.findByProductCodeEqualsIgnoreCase("KMV").stream().findFirst();
    }

    public Optional<Product> getRegularCrane(){
        return productRepo.findByProductCodeEqualsIgnoreCase("KMV-kraan").stream().findFirst();
    }

    public Optional<Product> getRegularTrailer(){
        return productRepo.findByProductCodeEqualsIgnoreCase("KMV-oplegger").stream().findFirst();
    }

    public Optional<Product> getWorkHoursCraneRegular(){
        return productRepo.findByProductCodeEqualsIgnoreCase("WU-kraan-AL").stream().findFirst();
    }

    public Optional<Product> getWorkHoursCraneIntense(){
        return productRepo.findByProductCodeEqualsIgnoreCase("WU-kraan-IN").stream().findFirst();
    }

    public Optional<Product> getWorkHoursCraneForfait(){
        return productRepo.findByProductCodeEqualsIgnoreCase("TRANS-kraan").stream().findFirst();
    }

    public Optional<Product> getWorkHoursTrailerForfait(){
        return productRepo.findByProductCodeEqualsIgnoreCase("TRANS-oplegger").stream().findFirst();
    }

    public List<Product> findProductsByLevels(
            ProductLevel1 level1,
            ProductLevel2 level2,
            ProductLevel3 level3,
            ProductLevel4 level4,
            ProductLevel5 level5,
            ProductLevel6 level6,
            ProductLevel7 level7
    ) {
        Criteria criteria = new Criteria();

        // Dynamisch criteria toevoegen per level
        if (level1 != null) addLevelCriteria(criteria, "productLevel1", level1);
        if (level2 != null) addLevelCriteria(criteria, "productLevel2", level2);
        if (level3 != null) addLevelCriteria(criteria, "productLevel3", level3);
        if (level4 != null) addLevelCriteria(criteria, "productLevel4", level4);
        if (level5 != null) addLevelCriteria(criteria, "productLevel5", level5);
        if (level6 != null) addLevelCriteria(criteria, "productLevel6", level6);
        if (level7 != null) addLevelCriteria(criteria, "productLevel7", level7);

        Query query = new Query(criteria);
        return mongoTemplate.find(query, Product.class);
    }

    private void addLevelCriteria(Criteria baseCriteria, String fieldName, Object level) {
        try {
            var clazz = level.getClass();
            var id = (String) clazz.getMethod("getId").invoke(level);
            var name = (String) clazz.getMethod("getName").invoke(level);
            var time = (LocalDateTime) clazz.getMethod("getTime").invoke(level);

            if (id != null) baseCriteria.and(fieldName + ".id").is(id);
            if (name != null) baseCriteria.and(fieldName + ".name").is(name);
            if (time != null) baseCriteria.and(fieldName + ".time").is(time);

        } catch (Exception e) {
            throw new RuntimeException("Fout bij reflectie van " + fieldName, e);
        }
    }

}
