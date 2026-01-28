package com.adverto.dejonghe.application.dbservices;

import com.adverto.dejonghe.application.entities.product.product.ProductDiscriptionAndId;
import com.adverto.dejonghe.application.entities.product.product.ProductLevel1;
import com.adverto.dejonghe.application.repos.ProductLevel1Repo;
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
            level1List.sort((o1, o2) ->
                    compareOnderdeel(o1.getName(), o2.getName())
            );
            return Optional.of(level1List);
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<ProductLevel1> getProductLevel1ByName(String name) {
        List<ProductLevel1> level1List = productLevel1Repo.findAll();
        if (!level1List.isEmpty()) {
            level1List.sort((o1, o2) ->
                    compareOnderdeel(o1.getName(), o2.getName())
            );
            return Optional.of(level1List.stream().filter(item -> item.getName().toLowerCase().matches(name.toLowerCase())).findFirst().orElse(null));
        }
        else{
            return Optional.empty();
        }
    }

    public Optional<List<ProductDiscriptionAndId>> getProductDiscriptionAndId() {
        List<ProductLevel1> level1List = productLevel1Repo.findAll();
        if (!level1List.isEmpty()) {
            level1List.sort((o1, o2) ->
                    compareOnderdeel(o1.getName(), o2.getName())
            );
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
