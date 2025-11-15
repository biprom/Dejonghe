package com.adverto.dejonghe.application.implementations;

import com.adverto.dejonghe.application.entities.product.product.Product;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class DataImplementation implements JRDataSource {

    DecimalFormat df = new DecimalFormat("0.00");
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
                if((products.get(lastFiledAdded).getShowDate()) && (products.get(lastFiledAdded).getDate() != null)){
                    return products.get(lastFiledAdded).getDate().format(dateTimeFormatter).toString();
                }
                else{
                    return null;
                }
            }
            catch (Exception e){
                return null;
            }

        } else if (jrField.getName().equals("Omschrijving")) {
            return products.get(lastFiledAdded).getInternalName();
        } else if (jrField.getName().equals("Aantal")) {
            try{
                String product =  products.get(lastFiledAdded).getSelectedAmount().toString().replace('.',',');
                if (product.endsWith(",0")) {
                    product = product.substring(0, product.length() - 2);
                }
                if(product.equals("0")){
                    return null;
                }
                return product;
            }
            catch (Exception e){
                return null;
            }
        } else if (jrField.getName().equals("Eenheidsprijs")) {
            try{
                if(products.get(lastFiledAdded).getSellPrice() != null){
                    if(products.get(lastFiledAdded).getSellPrice().equals(0.0)){
                        return null;
                    }
                    return String.format("%.2f", products.get(lastFiledAdded).getSellPrice() ) + "€";
                }
                else{
                    return "";
                }
            }
            catch (Exception e){
                return "";
            }
        } else if (jrField.getName().equals("Totaal")) {
            if(products.get(lastFiledAdded).getTotalPrice() != null){
                if(products.get(lastFiledAdded).getTotalPrice().equals(0.0)){
                    return null;
                }
                return products.get(lastFiledAdded).getTotalPrice();
            }
            return null;
        } else if (jrField.getName().equals("btwStatus")) {
            if(products.get(lastFiledAdded).getSelectedAmount() != null){
                if(!products.get(lastFiledAdded).getSelectedAmount().equals(0.0)){
                    return products.get(lastFiledAdded).getVat().getDiscription();
                }
                else{
                    return null;
                }
            }
            else {
                return null;
            }

        }
        return "";
    }
}
