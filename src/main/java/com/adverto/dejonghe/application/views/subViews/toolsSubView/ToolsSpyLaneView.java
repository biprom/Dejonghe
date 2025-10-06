package com.adverto.dejonghe.application.views.subViews.toolsSubView;

import com.adverto.dejonghe.application.customEvents.AddRemoveProductEvent;
import com.adverto.dejonghe.application.dbservices.ProductService;
import com.adverto.dejonghe.application.entities.enums.workorder.SpyLaneMaterial;
import com.adverto.dejonghe.application.entities.enums.workorder.Tools;
import com.adverto.dejonghe.application.entities.product.product.Product;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class ToolsSpyLaneView extends VerticalLayout {

    ProductService productService;
    ApplicationEventPublisher eventPublisher;

    H3 title;
    Tools selectedTool;
    List<Product> selectedProducts;
    ComboBox<Integer>cbWidthSpyLane;
    RadioButtonGroup<String> radioGroup;

    @Autowired
    public void ToolsSpyLaneView(ProductService productService,
                                 ApplicationEventPublisher eventPublisher) {
        this.productService = productService;
        this.eventPublisher = eventPublisher;

        title = new H3();

        radioGroup = new RadioButtonGroup<>();
        radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroup.setItems(SpyLaneMaterial.IRON.getDiscription(),SpyLaneMaterial.RVS.getDiscription());

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
            Product productToAdd = new Product();
            productToAdd.setAbbreviation(selectedTool.getAbbreviationIndustry());
            productToAdd.setSelectedAmount(1.0);
            productToAdd.setInternalName(selectedTool.getDiscription()+ " materiaal : " + radioGroup.getValue() + " breedte : " + cbWidthSpyLane.getValue().toString());
            selectedProducts.add(productToAdd);


            eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",null));
        });
    }

    public Tools getSelectedTool() {
        return selectedTool;
    }

    public void setSelectedTool(Tools selectedTool) {
        title.setText(selectedTool.getDiscription() + " selecteren?");
        this.selectedTool = selectedTool;
    }

    public List<Product> getSelectedProducts() {
        return selectedProducts;
    }

    public void setSelectedProducts(List<Product> selectedProducts) {
        this.selectedProducts = selectedProducts;
    }

}
