package com.adverto.dejonghe.application.views.subViews.toolsSubView;

import com.adverto.dejonghe.application.customEvents.AddRemoveProductEvent;
import com.adverto.dejonghe.application.dbservices.ProductService;
import com.adverto.dejonghe.application.entities.enums.workorder.Tools;
import com.adverto.dejonghe.application.entities.enums.workorder.ToolsLabor;
import com.adverto.dejonghe.application.entities.product.product.Product;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
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
public class ToolsRegularIntenseView extends VerticalLayout {

    ProductService productService;
    ApplicationEventPublisher eventPublisher;

    H2 title;
    Tools selectedTool;
    List<Product> selectedProducts;
    RadioButtonGroup<String> radioGroup;

    @Autowired
    public void ToolsRegularIntenseView(ProductService productService,
                                        ApplicationEventPublisher eventPublisher) {
        this.productService = productService;
        this.eventPublisher = eventPublisher;

        title = new H2();
        radioGroup = new RadioButtonGroup<>();
        radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroup.setItems(ToolsLabor.REGULAR.getDiscription(),ToolsLabor.INTENSE.getDiscription());
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
            Product productToAdd = new Product();
            productToAdd.setAbbreviation(selectedTool.getAbbreviationIndustry());
            productToAdd.setSelectedAmount(1.0);
            productToAdd.setInternalName(selectedTool.getDiscription() +" " + radioGroup.getValue());
            selectedProducts.add(productToAdd);

            eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",null));
        });
    }

    public Tools getSelectedTool() {
        return selectedTool;
    }

    public void setSelectedTool(Tools selectedTool) {
        title.setText(selectedTool.getDiscription() + " toevoegen?");
        this.selectedTool = selectedTool;
    }

    public List<Product> getSelectedProducts() {
        return selectedProducts;
    }

    public void setSelectedProducts(List<Product> selectedProducts) {
        this.selectedProducts = selectedProducts;
    }
}
