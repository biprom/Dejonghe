package com.adverto.dejonghe.application.views.subViews.toolsSubView;

import com.adverto.dejonghe.application.customEvents.AddRemoveProductEvent;
import com.adverto.dejonghe.application.dbservices.ProductService;
import com.adverto.dejonghe.application.entities.customers.Customer;
import com.adverto.dejonghe.application.entities.enums.workorder.SpyLaneMaterial;
import com.adverto.dejonghe.application.entities.enums.workorder.Tools;
import com.adverto.dejonghe.application.entities.product.product.Product;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
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
public class ToolsSpyLaneView extends VerticalLayout {

    ProductService productService;
    ApplicationEventPublisher eventPublisher;

    H3 title;
    Tools selectedTool;
    List<Product> selectedProducts;
    ComboBox<Integer>cbWidthSpyLane;
    RadioButtonGroup<SpyLaneMaterial> radioGroup;
    Integer selectedTeam;
    Boolean bAgro;

    @Autowired
    public void ToolsSpyLaneView(ProductService productService,
                                 ApplicationEventPublisher eventPublisher) {
        this.productService = productService;
        this.eventPublisher = eventPublisher;

        title = new H3();

        radioGroup = new RadioButtonGroup<>();
        radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroup.setItemLabelGenerator(item -> item.getDiscription());
        radioGroup.setItems(SpyLaneMaterial.values());

        this.setAlignItems(Alignment.CENTER);
        this.add(title);
        this.add(radioGroup);
        this.add(setUpCbWidthSpyLane());
        Button okButton = new Button("Voeg toe");
        setUpOkButton(okButton);
        this.add(okButton);
    }

    private ComboBox<Integer> setUpCbWidthSpyLane() {
        cbWidthSpyLane = new ComboBox<>();
        cbWidthSpyLane.setPlaceholder("breedte");
        cbWidthSpyLane.setItems(6,8,10,12,14,16,18,20,22,25);
        return cbWidthSpyLane;
    }

    private void setUpOkButton(Button okButton) {
        okButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        okButton.setWidth("100%");
        okButton.addClickListener(e -> {
            Product productToAdd = productService.findByProductCodeContaining(selectedTool.getAbbreviation() + "-" + cbWidthSpyLane.getValue() + "-" + radioGroup.getValue().getElement()).get().get(0);
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
        title.setText(selectedTool.getDiscription() + " selecteren?");
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
