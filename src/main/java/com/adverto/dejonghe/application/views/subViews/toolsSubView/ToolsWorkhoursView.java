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
public class ToolsWorkhoursView extends VerticalLayout {

    ProductService productService;
    ApplicationEventPublisher eventPublisher;

    H3 title;
    Tools selectedTool;
    List<Product> selectedProducts;
    Integer amountWorkhours = 15;
    Integer selectedTeam;

    @Autowired
    public void ToolsWorkhoursView(ProductService productService,
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
        TextField tfWorkhours = new TextField();
        tfWorkhours.setSuffixComponent(new Span("Minuten"));
        Button minusButton = new Button(VaadinIcon.MINUS.create());
        minusButton.addClickListener(buttonClickEvent ->{
            amountWorkhours = amountWorkhours-15;
            tfWorkhours.setValue(String.valueOf(amountWorkhours));
                });
        Button plusButton = new Button(VaadinIcon.PLUS.create());
        plusButton.addClickListener(buttonClickEvent ->{
            amountWorkhours = amountWorkhours+15;
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
            Product productToAdd = productService.findByProductCodeContaining(selectedTool.getAbbreviationIndustry()).get().get(0);
            try{
                productToAdd.setSelectedAmount(Double.valueOf(amountWorkhours)/60.0);
            }
            catch (Exception ex){
                productToAdd.setSelectedAmount(Double.valueOf(0));
            }
            productToAdd.setTeamNumber(selectedTeam);
            productToAdd.setInternalName(selectedTool.getDiscription());
            selectedProducts.add(productToAdd);

            eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",null));

        });
    }

    public Tools getSelectedTool() {
        return selectedTool;
    }

    public void setSelectedToolTeam(Tools selectedTool, Integer seletedTeam) {
        title.setText(selectedTool.getDiscription() + " toevoegen?");
        this.selectedTool = selectedTool;
        this.selectedTeam = seletedTeam;
    }

    public List<Product> getSelectedProducts() {
        return selectedProducts;
    }

    public void setSelectedProducts(List<Product> selectedProducts) {
        this.selectedProducts = selectedProducts;
    }
}
