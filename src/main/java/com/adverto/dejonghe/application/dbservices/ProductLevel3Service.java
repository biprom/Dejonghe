package com.adverto.dejonghe.application.dbservices;

import com.adverto.dejonghe.application.entities.product.product.ProductDiscriptionAndId;
import com.adverto.dejonghe.application.entities.product.product.ProductLevel1;
import com.adverto.dejonghe.application.entities.product.product.ProductLevel2;
import com.adverto.dejonghe.application.entities.product.product.ProductLevel3;
import com.adverto.dejonghe.application.repos.ProductLevel3Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ProductLevel3Service {
    @Autowired
    ProductLevel3Repo productLevel3Repo;

    public Optional<List<ProductLevel3>> getAllProductLevel3() {
        List<ProductLevel3> level3List = productLevel3Repo.findAll();
        if (!level3List.isEmpty()) {
            level3List.sort((o1, o2) ->
                    compareOnderdeel(o1.getName(), o2.getName())
            );
            return Optional.of(level3List);
        }
        else{
            return Optional.empty();
        }
    }

    public void saveProductlevelItems(List<String>stringList, ProductLevel2 productLevel2){
        for(String string : stringList){
            ProductLevel3 productLevel3 = new ProductLevel3();
            productLevel3.setName(string);
            productLevel3.setProductLevel2(productLevel2);
            productLevel3.setTime(LocalDateTime.now());
            productLevel3Repo.save(productLevel3);
        }
    }

    public Optional<List<ProductDiscriptionAndId>> getProductLevel3NamesAndLevelAndId() {
        List<ProductLevel3> level3List = productLevel3Repo.findAll();
        if (!level3List.isEmpty()) {
            level3List.sort((o1, o2) ->
                    compareOnderdeel(o1.getName(), o2.getName())
            );
            return Optional.of(level3List.stream().map(x -> new ProductDiscriptionAndId(x.getId(),x.getName() + " - "
                    + x.getProductLevel2().getName()+ " - "
                    + x.getProductLevel2().getProductLevel1().getName())).collect(Collectors.toList()));
        }
        else{
            return Optional.empty();
        }
    }

    public void removeById(String id) {
        if(id != null && !id.isEmpty()){
            productLevel3Repo.removeById(id);
        }
    }

    public Optional<List<ProductLevel3>> getProductLevel3sFromPreviousLevels(ProductLevel2 productLevel2, ProductLevel1 productLevel1) {
        List<ProductLevel3> level3List = productLevel3Repo.findByPreviousLevelNames(productLevel2.getName(),productLevel1.getName());
        if (!level3List.isEmpty()) {
            level3List.sort((o1, o2) ->
                    compareOnderdeel(o1.getName(), o2.getName())
            );
            return Optional.of(level3List);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<ProductLevel3> getProductLevel3ByName(String name) {
        List<ProductLevel3> level3List = productLevel3Repo.findAll();
        if (!level3List.isEmpty()) {
            level3List.sort((o1, o2) ->
                    compareOnderdeel(o1.getName(), o2.getName())
            );
            return Optional.of(level3List.stream().filter(item -> item.getName().toLowerCase().matches(name.toLowerCase())).findFirst().orElse(null));
        }
        else{
            return Optional.empty();
        }
    }

    private int compareOnderdeel(String s1, String s2) {
        if((s1 != null) && (s2 != null)){
            List<Object> parts1 = splitAlphaNumeric(s1);
            List<Object> parts2 = splitAlphaNumeric(s2);

            int len = Math.min(parts1.size(), parts2.size());

            for (int i = 0; i < len; i++) {
                Object p1 = parts1.get(i);
                Object p2 = parts2.get(i);

                int cmp;
                if (p1 instanceof String && p2 instanceof String) {
                    cmp = ((String) p1).compareToIgnoreCase((String) p2);
                } else if (p1 instanceof Number && p2 instanceof Number) {
                    cmp = Double.compare(((Number) p1).doubleValue(), ((Number) p2).doubleValue());
                } else {
                    // String vs Number → String komt altijd eerst
                    cmp = (p1 instanceof String) ? -1 : 1;
                }

                if (cmp != 0) return cmp;
            }

            // Als alles gelijk is, kortere string komt eerst
            return Integer.compare(parts1.size(), parts2.size());
        }
        return 9999;
    }

    private List<Object> splitAlphaNumeric(String input) {
        List<Object> parts = new ArrayList<>();

        Matcher matcher = Pattern.compile("(\\d+[\\.,]?\\d*|\\D+)").matcher(input);
        while (matcher.find()) {
            String part = matcher.group(1).trim();
            if (part.matches("\\d+[\\.,]?\\d*")) {
                part = part.replace(",", "."); // vervang komma door punt
                try {
                    parts.add(Double.parseDouble(part));
                } catch (NumberFormatException e) {
                    parts.add(part); // fallback: behandel als string
                }
            } else {
                parts.add(part);
            }
        }

        return parts;
    }
}
