package com.adverto.dejonghe.application.views.subViews.toolsSubView;

import com.adverto.dejonghe.application.customEvents.AddRemoveProductEvent;
import com.adverto.dejonghe.application.dbservices.ProductService;
import com.adverto.dejonghe.application.entities.enums.workorder.Tools;
import com.adverto.dejonghe.application.entities.enums.workorder.ToolsPTAOptions;
import com.adverto.dejonghe.application.entities.product.product.Product;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
    TextField tfFuel;

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

        tfFuel = new TextField();
        tfFuel.setSuffixComponent(new Span("Aantal gram"));
        Button minusButton = new Button(VaadinIcon.MINUS.create());
        minusButton.addClickListener(buttonClickEvent ->{
            amountOfKg = amountOfKg-100;
            tfFuel.setValue(amountOfKg.toString());
                });
        Button plusButton = new Button(VaadinIcon.PLUS.create());
        plusButton.addClickListener(buttonClickEvent ->{
            amountOfKg = amountOfKg+100;
            tfFuel.setValue(amountOfKg.toString());
        });
        tfFuel.setValue(amountOfKg.toString());
        horizontalLayout.add(cbPowderType,minusButton, tfFuel,  plusButton);
        return horizontalLayout;
    }

    private void setUpOkButton(Button okButton) {
        okButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        okButton.setWidth("100%");
        okButton.addClickListener(e -> {
            Product productToAdd = new Product();
            productToAdd.setAbbreviation(selectedTool.getAbbreviationIndustry());
            productToAdd.setSelectedAmount(1.0);
            productToAdd.setInternalName(selectedTool.getDiscription() + " " + radioGroup.getValue());

            Product productToAdd2 = new Product();
            productToAdd.setAbbreviation(selectedTool.getAbbreviationIndustry());
            productToAdd2.setSelectedAmount(Double.valueOf(amountOfKg));
            productToAdd2.setInternalName("Aantal gram : " + cbPowderType.getValue().getComment());
            selectedProducts.add(productToAdd);
            selectedProducts.add(productToAdd2);

            eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",null));

        });
    }

    public Tools getSelectedTool() {
        return selectedTool;
    }

    public void setSelectedTool(Tools selectedTool) {
        amountOfKg = 100;
        tfFuel.setValue(amountOfKg.toString());
        title.setText(selectedTool.getDiscription());
        this.selectedTool = selectedTool;
    }

    public List<Product> getSelectedProducts() {
        return selectedProducts;
    }

    public void setSelectedProducts(List<Product> selectedProducts) {
        this.selectedProducts = selectedProducts;
    }
}
