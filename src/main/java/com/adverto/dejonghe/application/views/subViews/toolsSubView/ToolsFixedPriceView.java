package com.adverto.dejonghe.application.views.subViews.toolsSubView;

import com.adverto.dejonghe.application.customEvents.AddRemoveProductEvent;
import com.adverto.dejonghe.application.dbservices.ProductService;
import com.adverto.dejonghe.application.entities.enums.workorder.Tools;
import com.adverto.dejonghe.application.entities.product.product.Product;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.shared.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

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
            Product productToAdd = new Product();
            productToAdd.setAbbreviation(selectedTool.getAbbreviation());
            productToAdd.setSelectedAmount(Double.valueOf(amountWorkhours));
            productToAdd.setInternalName(selectedTool.getDiscription());
            selectedProducts.add(productToAdd);
            eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",null));
        });
    }

    public Tools getSelectedTool() {
        return selectedTool;
    }

    public void setSelectedTool(Tools selectedTool) {
        amountWorkhours = 1;
        tfWorkhours.setValue(String.valueOf(amountWorkhours));
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
