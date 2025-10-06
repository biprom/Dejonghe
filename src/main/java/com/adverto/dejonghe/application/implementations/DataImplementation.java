package com.adverto.dejonghe.application.implementations;

import com.adverto.dejonghe.application.entities.product.product.Product;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class DataImplementation implements JRDataSource {

    DecimalFormat df = new DecimalFormat("#.00");
    NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.FRANCE);
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private int lastFiledAdded;
    private HashMap<String, Integer>fieldsNumber = new HashMap<>(  );
    List<Product>products;

    public DataImplementation(List<Product>products) {

        this.products = products;
        lastFiledAdded = products.size() ;

    }

    @Override
    public boolean next() throws JRException {
        if(lastFiledAdded > 0 ){
            lastFiledAdded --;
            return true;
        }
        return false;
    }

    @Override
    public Object getFieldValue(JRField jrField) throws JRException {
        if (jrField.getName().equals("Datum")) {
            try{
                return products.get(lastFiledAdded).getDate().format(dateTimeFormatter).toString();
            }
            catch (Exception e){
                return LocalDateTime.now().format(dateTimeFormatter).toString();
            }

        } else if (jrField.getName().equals("Omschrijving")) {
            return products.get(lastFiledAdded).getInternalName();
        } else if (jrField.getName().equals("Aantal")) {
            return products.get(lastFiledAdded).getSelectedAmount().toString();
        } else if (jrField.getName().equals("Eenheidsprijs")) {
            try{
                return df.format(products.get(lastFiledAdded).getSellPrice());
            }
            catch (Exception e){
                return null;
            }
        } else if (jrField.getName().equals("Totaal")) {
            return df.format(products.get(lastFiledAdded).getTotalPrice());
        } else if (jrField.getName().equals("btwStatus")) {
            return products.get(lastFiledAdded).getVat().getDiscription();
        }
        return "";
    }
}
