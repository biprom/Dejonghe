package com.adverto.dejonghe.application.views.subViews.toolsSubView;

import com.adverto.dejonghe.application.customEvents.AddRemoveProductEvent;
import com.adverto.dejonghe.application.dbservices.ProductService;
import com.adverto.dejonghe.application.entities.customers.Customer;
import com.adverto.dejonghe.application.entities.enums.product.VAT;
import com.adverto.dejonghe.application.entities.enums.workorder.Tools;
import com.adverto.dejonghe.application.entities.product.product.Product;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
public class ToolsFixedPriceView extends VerticalLayout {

    ProductService productService;
    ApplicationEventPublisher eventPublisher;

    H3 title;
    Tools selectedTool;
    List<Product> selectedProducts;
    Integer amountWorkhours = 1;
    TextField tfWorkhours;
    Integer selectedTeam;
    Boolean bAgro = false;

    @Autowired
    public void ToolsFixedPriceView(ProductService productService,
                                    ApplicationEventPublisher eventPublisher) {
        this.productService = productService;
        this.eventPublisher = eventPublisher;

        title = new H3();

        Button okButton = new Button("Voeg toe");
        setUpOkButton(okButton);
        this.setAlignItems(Alignment.CENTER);
        this.add(title);
        this.add(getWorkhoursComponent());
        this.add(okButton);
    }

    private HorizontalLayout getWorkhoursComponent() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setSpacing(true);
        tfWorkhours = new TextField();
        tfWorkhours.setSuffixComponent(new Span());
        Button minusButton = new Button(VaadinIcon.MINUS.create());
        minusButton.addClickListener(buttonClickEvent ->{
            amountWorkhours--;
            tfWorkhours.setValue(String.valueOf(amountWorkhours));
                });
        Button plusButton = new Button(VaadinIcon.PLUS.create());
        plusButton.addClickListener(buttonClickEvent ->{
            amountWorkhours++;
            tfWorkhours.setValue(String.valueOf(amountWorkhours));
        });
        tfWorkhours.setValue(amountWorkhours.toString());
        horizontalLayout.add(minusButton, tfWorkhours,  plusButton);
        return horizontalLayout;
    }

    private void setUpOkButton(Button okButton) {
        okButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        okButton.setWidth("100%");
        okButton.addClickListener(e -> {
            Optional<List<Product>> productListToAdd = productService.findByProductCodeContaining(selectedTool.getAbbreviation());
            if(productListToAdd.isPresent()) {
                Product productToAdd = productListToAdd.get().getFirst();
                productToAdd.setVat(VAT.EENENTWINTIG);
                productToAdd.setTeamNumber(selectedTeam);
                productToAdd.setDate(LocalDate.now());
                productToAdd.setAbbreviation(selectedTool.getAbbreviation());
                productToAdd.setSelectedAmount(Double.valueOf(amountWorkhours));
                if(!bAgro) {
                    if(productToAdd.getSellPriceIndustry() == 0.0){
                        productToAdd.setTotalPrice(Double.valueOf(amountWorkhours) * productToAdd.getSellPrice());
                    }
                    else{
                        productToAdd.setTotalPrice(Double.valueOf(amountWorkhours) * productToAdd.getSellPriceIndustry());
                    }
                }
                else{
                    productToAdd.setTotalPrice(Double.valueOf(amountWorkhours) * productToAdd.getSellPrice());
                }
                selectedProducts.add(productToAdd);
                eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",null));
            }

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
        amountWorkhours = 1;
        tfWorkhours.setValue(String.valueOf(amountWorkhours));
        title.setText(selectedTool.getDiscription() + " toevoegen?");
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
