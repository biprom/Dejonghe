package com.adverto.dejonghe.application.dbservices;

import com.adverto.dejonghe.application.entities.product.product.*;
import com.adverto.dejonghe.application.repos.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProductService {
    @Autowired
    ProductRepo productRepo;

    @Autowired
    MongoTemplate mongoTemplate;

    List<Product> elements = new ArrayList<>();

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
        return productRepo.findByProductCodeEqualsIgnoreCase("WU-CEN-AT").stream().findFirst();
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

    public Optional<List<Product>> findSetsWithThisElement(Product selectedProduct) {
        elements.clear();
        List<Product> setList = productRepo.findBySet(true);
        if((setList != null) && (setList.size() > 0)) {
            for(Product set : setList) {
                if((set.getSetList() != null) && (set.getSetList().size() > 0)){
                    List<Product> elementsWithSameCode = set.getSetList().stream().filter(item -> item.getProductCode().equalsIgnoreCase(selectedProduct.getProductCode())).collect(Collectors.toList());
                    if((elementsWithSameCode != null) && (elementsWithSameCode.size() > 0)) {
                        List<Product> returnValues = elementsWithSameCode.stream()
                                .filter(p -> matchesLevel(p, selectedProduct.getProductLevel1(),
                                        selectedProduct.getProductLevel2(),
                                        selectedProduct.getProductLevel3(),
                                        selectedProduct.getProductLevel4(),
                                        selectedProduct.getProductLevel5(),
                                        selectedProduct.getProductLevel6(),
                                        selectedProduct.getProductLevel7()
                                        ))
                                .toList();
                        if((returnValues != null) && (returnValues.size() > 0)) {
                            elements.add(set);
                        }
                    }
                }
            }
            return Optional.of(elements);
        }
        else return Optional.empty();
    }

    public boolean matchesLevel(Product product,
                                ProductLevel1 l1,
                                ProductLevel2 l2,
                                ProductLevel3 l3,
                                ProductLevel4 l4,
                                ProductLevel5 l5,
                                ProductLevel6 l6,
                                ProductLevel7 l7) {

        if (l1 != null) {
            if (product.getProductLevel1() == null ||
                    !product.getProductLevel1().getId().equals(l1.getId())) {
                return false;
            }
        }

        if (l2 != null) {
            if (product.getProductLevel2() == null ||
                    !product.getProductLevel2().getId().equals(l2.getId())) {
                return false;
            }
        }

        if (l3 != null) {
            if (product.getProductLevel3() == null ||
                    !product.getProductLevel3().getId().equals(l3.getId())) {
                return false;
            }
        }

        if (l4 != null) {
            if (product.getProductLevel4() == null ||
                    !product.getProductLevel4().getId().equals(l4.getId())) {
                return false;
            }
        }

        if (l5 != null) {
            if (product.getProductLevel5() == null ||
                    !product.getProductLevel5().getId().equals(l5.getId())) {
                return false;
            }
        }

        if (l6 != null) {
            if (product.getProductLevel6() == null ||
                    !product.getProductLevel6().getId().equals(l6.getId())) {
                return false;
            }
        }

        if (l7 != null) {
            if (product.getProductLevel7() == null ||
                    !product.getProductLevel7().getId().equals(l7.getId())) {
                return false;
            }
        }

        return true;
    }
}
