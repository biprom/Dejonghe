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
public class ToolsThicknessMeterView extends VerticalLayout {

    ProductService productService;
    ApplicationEventPublisher eventPublisher;

    H3 title;
    Tools selectedTool;
    List<Product> selectedProducts;
    Integer depth = 1;
    Integer meters = 1;
    Integer selectedTeam;

    TextField tfThickness;
    TextField tfRunningMeter;

    @Autowired
    public void ToolsThicknessMeterView(ProductService productService,
                                        ApplicationEventPublisher eventPublisher) {
        this.productService = productService;
        this.eventPublisher = eventPublisher;

        title = new H3();

        Button okButton = new Button("Voeg toe");
        setUpOkButton(okButton);

        this.setAlignItems(Alignment.CENTER);
        this.add(title);
        this.add(getThicknessComponent());
        this.add(getRunningMetersComponent());
        this.add(okButton);
    }

    private HorizontalLayout getThicknessComponent() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setSpacing(true);
        tfThickness = new TextField();
        Button minusButton = new Button(VaadinIcon.MINUS.create());
        minusButton.addClickListener(buttonClickEvent ->{
            depth--;
            tfThickness.setValue(String.valueOf(depth));
                });
        Button plusButton = new Button(VaadinIcon.PLUS.create());
        plusButton.addClickListener(buttonClickEvent ->{
            depth++;
            tfThickness.setValue(String.valueOf(depth));
        });

        tfThickness.setSuffixComponent(new Span("Diepte [cm]"));
        tfThickness.setValue(depth.toString());
        horizontalLayout.add(minusButton, tfThickness,  plusButton);
        return horizontalLayout;
    }

    private HorizontalLayout getRunningMetersComponent() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setSpacing(true);
        tfRunningMeter = new TextField();
        Button minusButton = new Button(VaadinIcon.MINUS.create());
        minusButton.addClickListener(buttonClickEvent ->{
            meters--;
            tfRunningMeter.setValue(String.valueOf(meters));
                });
        Button plusButton = new Button(VaadinIcon.PLUS.create());
        plusButton.addClickListener(buttonClickEvent ->{
            meters++;
            tfRunningMeter.setValue(String.valueOf(meters));
        });
        tfRunningMeter.setSuffixComponent(new Span("Lopende meter [m]"));
        tfRunningMeter.setValue(meters.toString());
        horizontalLayout.add(minusButton, tfRunningMeter,  plusButton);
        return horizontalLayout;
    }

    private void setUpOkButton(Button okButton) {
        okButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        okButton.setWidth("100%");
        okButton.addClickListener(e -> {
            Product productToAdd = productService.findByProductCodeContaining(selectedTool.getAbbreviationIndustry()).get().get(0);
            productToAdd.setSelectedAmount(1.0);
            productToAdd.setTeamNumber(selectedTeam);
            selectedProducts.add(productToAdd);

            Product productToAdd2 = new Product();
            productToAdd2.setAbbreviation(selectedTool.getAbbreviationIndustry());
            productToAdd2.setProductLevel1(productToAdd.getProductLevel1());
            productToAdd2.setSelectedAmount(1.0);
            productToAdd2.setTeamNumber(selectedTeam);
            productToAdd2.setInternalName(selectedTool.getDiscription() + " diepte [cm] : " + depth + " aantal meter : " + meters );
            selectedProducts.add(productToAdd2);

            eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",null));
        });
    }

    public Tools getSelectedTool() {
        return selectedTool;
    }

    public void setSelectedToolTeam(Tools selectedTool, Integer selectedTeam) {
        depth = 1;
        tfThickness.setValue(depth.toString());
        meters = 1;
        tfRunningMeter.setValue(meters.toString());
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
}
