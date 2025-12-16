package com.adverto.dejonghe.application.views.subViews.toolsSubView;

import com.adverto.dejonghe.application.customEvents.AddRemoveProductEvent;
import com.adverto.dejonghe.application.dbservices.ProductService;
import com.adverto.dejonghe.application.entities.customers.Customer;
import com.adverto.dejonghe.application.entities.enums.workorder.Tools;
import com.adverto.dejonghe.application.entities.enums.workorder.ToolsLabor;
import com.adverto.dejonghe.application.entities.product.product.Product;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
public class ToolsRegularIntenseView extends VerticalLayout {

    ProductService productService;
    ApplicationEventPublisher eventPublisher;

    H2 title;
    Tools selectedTool;
    List<Product> selectedProducts;
    RadioButtonGroup<String> radioGroup;
    Integer selectedTeam;
    Boolean bAgro;

    @Autowired
    public void ToolsRegularIntenseView(ProductService productService,
                                        ApplicationEventPublisher eventPublisher) {
        this.productService = productService;
        this.eventPublisher = eventPublisher;

        title = new H2();
        radioGroup = new RadioButtonGroup<>();
        radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroup.setItems(ToolsLabor.REGULAR.getDiscription(),ToolsLabor.INTENSE.getDiscription());
        radioGroup.addValueChangeListener(event -> {
            selectedTool.setAbbreviation(selectedTool.getAbbreviation().replace("IN",""));
            selectedTool.setAbbreviation(selectedTool.getAbbreviation().replace("AL",""));
            if(event.getValue().equals(ToolsLabor.INTENSE.getDiscription())){
                selectedTool.setAbbreviation(selectedTool.getAbbreviation()+"IN");
            }
            else{
                selectedTool.setAbbreviation(selectedTool.getAbbreviation()+"AL");
            }
        });
        Button okButton = new Button("Voeg toe");
        setUpOkButton(okButton);

        this.setAlignItems(Alignment.CENTER);
        this.add(title);
        add(radioGroup);
        this.add(okButton);
    }

    private void setUpOkButton(Button okButton) {
        okButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        okButton.setWidth("100%");
        okButton.addClickListener(e -> {
            Product productToAdd = productService.findByProductCodeContaining(selectedTool.getAbbreviation()).get().get(0);
            productToAdd.setAbbreviation(selectedTool.getAbbreviation());
            productToAdd.setSelectedAmount(1.0);
            productToAdd.setTeamNumber(selectedTeam);
            productToAdd.setInternalName(selectedTool.getDiscription() +" " + radioGroup.getValue());
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
            productToAdd.setAbbreviation(selectedTool.getAbbreviation());
            selectedProducts.add(productToAdd);

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
