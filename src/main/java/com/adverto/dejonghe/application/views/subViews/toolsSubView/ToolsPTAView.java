package com.adverto.dejonghe.application.views.subViews.toolsSubView;

import com.adverto.dejonghe.application.customEvents.AddRemoveProductEvent;
import com.adverto.dejonghe.application.dbservices.ProductService;
import com.adverto.dejonghe.application.entities.customers.Customer;
import com.adverto.dejonghe.application.entities.enums.workorder.Tools;
import com.adverto.dejonghe.application.entities.enums.workorder.ToolsPTAOptions;
import com.adverto.dejonghe.application.entities.product.product.Product;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
public class ToolsPTAView extends VerticalLayout {

    ProductService productService;
    ApplicationEventPublisher eventPublisher;

    H3 title;
    Tools selectedTool;
    List<Product> selectedProducts;
    Integer amountOfKg = 100;
    ComboBox<Product>cbPowderType;
    RadioButtonGroup<String> radioGroup;
    TextField tfAmount;
    Integer selectedTeam;
    Boolean bAgro;

    @Autowired
    public void ToolsPTAView(ProductService productService,
                             ApplicationEventPublisher eventPublisher) {
        this.productService = productService;
        this.eventPublisher = eventPublisher;

        title = new H3();
        radioGroup = new RadioButtonGroup<>();
        radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroup.setItems(ToolsPTAOptions.TROMMEL_SCHROEF.getDiscription()
                ,ToolsPTAOptions.KLEINE_ONDERDELEN_DECANTER.getDiscription()
                ,ToolsPTAOptions.ONDERDELEN_MIXER_POMP.getDiscription());
        radioGroup.addValueChangeListener(event -> {
            selectedTool.setAbbreviation(selectedTool.getAbbreviation().replace("std",""));
            selectedTool.setAbbreviation(selectedTool.getAbbreviation().replace("kod",""));
            selectedTool.setAbbreviation(selectedTool.getAbbreviation().replace("mp",""));
            if(event.getValue().equals(ToolsPTAOptions.TROMMEL_SCHROEF.getDiscription())){
                selectedTool.setAbbreviation(selectedTool.getAbbreviation()+"std");
            } else if (event.getValue().equals(ToolsPTAOptions.ONDERDELEN_MIXER_POMP.getDiscription())) {
                selectedTool.setAbbreviation(selectedTool.getAbbreviation()+"mp");
            } else{
                selectedTool.setAbbreviation(selectedTool.getAbbreviation()+"kod");
            }
        });
        Button okButton = new Button("Voeg toe");
        setUpOkButton(okButton);
        this.setAlignItems(Alignment.CENTER);
        this.add(title);
        this.add(radioGroup);
        this.add(getAmountOfKgComponent());
        this.add(okButton);
    }

    private HorizontalLayout getAmountOfKgComponent() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setSpacing(true);
        cbPowderType = new ComboBox<>();
        cbPowderType.setPlaceholder("Type poeder");
        Optional<List<Product>> optProductList = productService.getAllProductsByCategoryPTAPowder();
        if(optProductList.isPresent()) {
            cbPowderType.setItems(productService.getAllProductsByCategoryPTAPowder().get());
        }
        cbPowderType.setItemLabelGenerator(x -> x.getInternalName());

        tfAmount = new TextField();
        tfAmount.setSuffixComponent(new Span("Aantal gram"));
        Button minusButton = new Button(VaadinIcon.MINUS.create());
        minusButton.addClickListener(buttonClickEvent ->{
            amountOfKg = amountOfKg-100;
            tfAmount.setValue(amountOfKg.toString());
            if(amountOfKg < 0){
                amountOfKg = 0;
                tfAmount.setValue("0");
            }
                });
        Button plusButton = new Button(VaadinIcon.PLUS.create());
        plusButton.addClickListener(buttonClickEvent ->{
            amountOfKg = amountOfKg+100;
            tfAmount.setValue(amountOfKg.toString());
        });
        tfAmount.setValue(amountOfKg.toString());
        horizontalLayout.add(cbPowderType,minusButton, tfAmount,  plusButton);
        return horizontalLayout;
    }

    private void setUpOkButton(Button okButton) {
        okButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        okButton.setWidth("100%");
        okButton.addClickListener(e -> {

            Product productToAdd = productService.findByProductCodeContaining(selectedTool.getAbbreviation()).get().get(0);
            productToAdd.setAbbreviation(selectedTool.getAbbreviation());
            productToAdd.setTeamNumber(selectedTeam);
            productToAdd.setSelectedAmount(1.0);
            if(!bAgro) {
                if(productToAdd.getSellPriceIndustry() == 0.0){
                    productToAdd.setTotalPrice(1.0 * productToAdd.getSellPrice());
                }
                else{
                    productToAdd.setTotalPrice(1.0 * productToAdd.getSellPriceIndustry());
                }
            }
            else{
                productToAdd.setTotalPrice(1.0 * productToAdd.getSellPrice());
            }
            productToAdd.setInternalName(selectedTool.getDiscription() + " " + radioGroup.getValue());

            Product productToAdd2 = productService.findByProductCodeContaining(cbPowderType.getValue().getProductCode()).get().get(0);
            productToAdd2.setAbbreviation(selectedTool.getAbbreviation());
            productToAdd2.setTeamNumber(selectedTeam);
            productToAdd2.setSelectedAmount(Double.valueOf(amountOfKg));
            if(!bAgro) {
                if(productToAdd.getSellPriceIndustry() == 0.0){
                    productToAdd2.setTotalPrice(Double.valueOf(amountOfKg) * productToAdd2.getSellPrice());
                }
                else{
                    productToAdd2.setTotalPrice(Double.valueOf(amountOfKg) * productToAdd2.getSellPriceIndustry());
                }
            }
            else{
                productToAdd2.setTotalPrice(Double.valueOf(amountOfKg) * productToAdd2.getSellPrice());
            }
            productToAdd2.setInternalName("Aantal gram : " + cbPowderType.getValue().getComment());

            Product totalProduct = new Product();
            totalProduct.setAbbreviation(selectedTool.getAbbreviation());
            totalProduct.setTeamNumber(selectedTeam);
            totalProduct.setInternalName("Gebruik PTA + oplaspoeder");
            totalProduct.setSelectedAmount(1.0);
            try{
                totalProduct.setTotalPrice(Double.valueOf(productToAdd.getSellPrice() + (productToAdd2.getSelectedAmount() * productToAdd2.getSellPrice())));
            }
            catch (Exception ex){
                totalProduct.setSellPrice(0.0);
            }

            selectedProducts.add(totalProduct);

            eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",null));

            getParent().ifPresent(parent -> {
                if (parent instanceof Dialog dialog) {
                    dialog.close();
                }
            });


        });
    }

    public Tools getSelectedTool() {
        return selectedTool;
    }

    public void setSelectedToolTeam(Tools selectedTool, Integer selectedTeam) {
        amountOfKg = 100;
        tfAmount.setValue(amountOfKg.toString());
        title.setText(selectedTool.getDiscription());
        this.selectedTool = selectedTool;
        this.selectedTeam = selectedTeam;
    }

    public List<Product> getSelectedProducts() {
        return selectedProducts;
    }

    public void setSelectedProducts(List<Product> selectedProducts) {
        this.selectedProducts = selectedProducts;
    }

    public void setCustomerByWorkAddress(Optional<List<Customer>> customerByWorkAddress) {
        try{
            if(customerByWorkAddress.get().getFirst().getBIndustry()){
                bAgro = !customerByWorkAddress.get().getFirst().getBIndustry();
            }
            else{
                bAgro = true;
            }
        }
        catch(Exception e){
            bAgro = true;
        }

    }
}
