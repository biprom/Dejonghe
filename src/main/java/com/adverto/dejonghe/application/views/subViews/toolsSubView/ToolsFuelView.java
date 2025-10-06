package com.adverto.dejonghe.application.views.subViews.toolsSubView;

import com.adverto.dejonghe.application.customEvents.AddRemoveProductEvent;
import com.adverto.dejonghe.application.dbservices.ProductService;
import com.adverto.dejonghe.application.entities.enums.workorder.Tools;
import com.adverto.dejonghe.application.entities.product.product.Product;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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

import java.util.List;

@Component
@Scope("prototype")
public class ToolsFuelView extends VerticalLayout {

    ProductService productService;
    ApplicationEventPublisher eventPublisher;

    H3 title;
    Tools selectedTool;
    List<Product> selectedProducts;
    Integer amountFuel = 1;
    TextField tfFuel;

    @Autowired
    public void ToolsFuelView(ProductService productService,
                              ApplicationEventPublisher eventPublisher) {
        this.productService = productService;
        this.eventPublisher = eventPublisher;

        title = new H3();
        this.setAlignItems(Alignment.CENTER);

        Button okButton = new Button("Voeg toe");
        setUpOkButton(okButton);
        this.add(title);
        add(getFuelComponent());
        this.add(okButton);
    }

    private HorizontalLayout getFuelComponent() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setSpacing(true);
        tfFuel = new TextField();
        Button minusButton = new Button(VaadinIcon.MINUS.create());
        minusButton.addClickListener(buttonClickEvent ->{
            amountFuel--;
            tfFuel.setValue(amountFuel.toString());
                });
        Button plusButton = new Button(VaadinIcon.PLUS.create());
        plusButton.addClickListener(buttonClickEvent ->{
            amountFuel++;
            tfFuel.setValue(amountFuel.toString());
        });

        tfFuel.setSuffixComponent(new Span("liter brandstof"));
        tfFuel.setValue(amountFuel.toString());

        horizontalLayout.add(minusButton, tfFuel,  plusButton);
        return horizontalLayout;
    }

    private void setUpOkButton(Button okButton) {
        okButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
        okButton.addClickListener(e -> {
            Product productToAdd = new Product();
            productToAdd.setAbbreviation(selectedTool.getAbbreviationIndustry());
            productToAdd.setSelectedAmount(Double.valueOf(amountFuel));
            productToAdd.setInternalName("Brandstof : " + selectedTool.getDiscription());
            selectedProducts.add(productToAdd);
            Product productToAdd2 = new Product();
            productToAdd2.setAbbreviation(selectedTool.getAbbreviationIndustry());
            productToAdd2.setSelectedAmount(Double.valueOf(amountFuel));
            productToAdd2.setInternalName("Forfait : " + selectedTool.getDiscription());
            selectedProducts.add(productToAdd2);
            eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",null));
        });
    }

    public Tools getSelectedTool() {
        return selectedTool;
    }

    public void setSelectedTool(Tools selectedTool) {
        amountFuel = 1;
        tfFuel.setValue(amountFuel.toString());
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
