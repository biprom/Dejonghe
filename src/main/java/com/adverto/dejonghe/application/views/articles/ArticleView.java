package com.adverto.dejonghe.application.views.articles;

import com.adverto.dejonghe.application.customEvents.GetSelectedProductEvent;
import com.adverto.dejonghe.application.customEvents.GetSelectedProductListener;
import com.adverto.dejonghe.application.dbservices.*;
import com.adverto.dejonghe.application.entities.enums.employee.UserFunction;
import com.adverto.dejonghe.application.entities.product.product.Product;
import com.adverto.dejonghe.application.entities.product.product.PurchasePrice;
import com.adverto.dejonghe.application.entities.product.product.Supplier;
import com.adverto.dejonghe.application.views.subViews.SelectProductSubView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import jakarta.annotation.PostConstruct;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@PageTitle("Producten")
@Route("product")
@Menu(order = 0, icon = LineAwesomeIconUrl.COG_SOLID)
public class ArticleView extends Div implements BeforeEnterObserver {
    SelectProductSubView selectProductSubView;
    ProductService productService;
    GetSelectedProductListener getSelectedProductListener;

    SupplierService supplierService;

    SplitLayout mainSplitLayout;
    private Grid<PurchasePrice> purchasePriceGrid = new Grid<>();

    Product selectedProduct;

    public ArticleView(ProductService productService,
                       SelectProductSubView selectProductSubView,
                       GetSelectedProductListener getSelectedProductListener,
                       SupplierService supplierService) {
        this.productService = productService;
        this.selectProductSubView = selectProductSubView;
        this.getSelectedProductListener = getSelectedProductListener;
        this.supplierService = supplierService;

        selectProductSubView.setUserFunction(UserFunction.WAREHOUSEWORKER);
        setUpSplitLayouts();
        mainSplitLayout.addToPrimary(selectProductSubView.getLayout());
        mainSplitLayout.addToSecondary(getProductDetail());
        this.setHeightFull();
        add(mainSplitLayout);
    }

    private Component getProductDetail() {
        purchasePriceGrid.setWidth("100%");
        purchasePriceGrid.removeAllColumns();
        purchasePriceGrid.addComponentColumn(item -> {
            DatePicker datePicker = new DatePicker();
            datePicker.setLocale(Locale.FRENCH);
            if(item.getPurchaseDate() != null){
                datePicker.setValue(item.getPurchaseDate());
            }
            else{
                datePicker.setValue(LocalDate.now());
            }
            datePicker.addValueChangeListener(listener -> {
                item.setPurchaseDate(datePicker.getValue());
                //search every product with same productcode and save the Purchaselist to every item.
                if(selectedProduct.getProductCode() != null && selectedProduct.getProductCode().length() > 0){
                    Optional<List<Product>> byProductCodeEqualCaseInsensitive = productService.findByProductCodeEqualCaseInsensitive(selectedProduct.getProductCode());
                    if(byProductCodeEqualCaseInsensitive.isPresent()){
                        for(Product product : byProductCodeEqualCaseInsensitive.get()){
                            product.setPurchasePriseList(selectedProduct.getPurchasePriseList());
                            productService.save(product);
                        }
                        Notification.show("Er zijn " + byProductCodeEqualCaseInsensitive.get().size() + " artikels aangepast");
                    }
                }
            });
            return datePicker;
        }).setAutoWidth(true).setHeader("Aankoopdatum").setResizable(true);


        purchasePriceGrid.addComponentColumn(item -> {
            TextField textField = new TextField();
            if(item.getQuantity() != null){
                textField.setValue(item.getQuantity().toString());
            }
            else{
                textField.setValue("0.0");
            }
            textField.addValueChangeListener(listener -> {
                try{
                    item.setQuantity(Double.parseDouble(textField.getValue()));
                    //search every product with same productcode and save the Purchaselist to every item.
                    if(selectedProduct.getProductCode() != null && selectedProduct.getProductCode().length() > 0){
                        Optional<List<Product>> byProductCodeEqualCaseInsensitive = productService.findByProductCodeEqualCaseInsensitive(selectedProduct.getProductCode());
                        if(byProductCodeEqualCaseInsensitive.isPresent()){
                            for(Product product : byProductCodeEqualCaseInsensitive.get()){
                                product.setPurchasePriseList(selectedProduct.getPurchasePriseList());
                                productService.save(product);
                            }
                            Notification.show("Er zijn " + byProductCodeEqualCaseInsensitive.get().size() + " artikels aangepast");
                        }
                    }
                }
                catch (NumberFormatException exception){
                    Notification.show("Aantal kon niet worden bewaard");
                }
            });
            return textField;
        }).setAutoWidth(true).setHeader("Aantal").setResizable(true);

        purchasePriceGrid.addComponentColumn(item -> {
            TextField textField = new TextField();
            if(item.getPrice() != null){
                textField.setValue(item.getPrice().toString());
            }
            else{
                textField.setValue("0.0");
            }
            textField.addValueChangeListener(listener -> {
                try{
                    item.setPrice(Double.parseDouble(textField.getValue()));
                    //search every product with same productcode and save the Purchaselist to every item.
                    if(selectedProduct.getProductCode() != null && selectedProduct.getProductCode().length() > 0){
                        Optional<List<Product>> byProductCodeEqualCaseInsensitive = productService.findByProductCodeEqualCaseInsensitive(selectedProduct.getProductCode());
                        if(byProductCodeEqualCaseInsensitive.isPresent()){
                            for(Product product : byProductCodeEqualCaseInsensitive.get()){
                                product.setPurchasePriseList(selectedProduct.getPurchasePriseList());
                                productService.save(product);
                            }
                            Notification.show("Er zijn " + byProductCodeEqualCaseInsensitive.get().size() + " artikels aangepast");
                        }
                    }
                }
                catch (NumberFormatException exception){
                    Notification.show("De aankoopprijs kon niet worden bewaard");
                }
            });
            return textField;
        }).setAutoWidth(true).setHeader("Aankoopprijs").setResizable(true);

        purchasePriceGrid.addComponentColumn(item -> {
            ComboBox<Supplier> comboBox = new ComboBox<>();
            comboBox.setItemLabelGenerator(supplier -> supplier.getName());
            Optional<List<Supplier>> optSupplierList = supplierService.getAllSuppliers();
            if(!optSupplierList.isEmpty()){
                comboBox.setItems(optSupplierList.get());
            }
            if(item.getSupplier() != null){
                comboBox.setValue(item.getSupplier());
            }
            comboBox.addValueChangeListener(listener -> {
                item.setSupplier(comboBox.getValue());
                //search every product with same productcode and save the Purchaselist to every item.
                if(selectedProduct.getProductCode() != null && selectedProduct.getProductCode().length() > 0){
                    Optional<List<Product>> byProductCodeEqualCaseInsensitive = productService.findByProductCodeEqualCaseInsensitive(selectedProduct.getProductCode());
                    if(byProductCodeEqualCaseInsensitive.isPresent()){
                        for(Product product : byProductCodeEqualCaseInsensitive.get()){
                            product.setPurchasePriseList(selectedProduct.getPurchasePriseList());
                            productService.save(product);
                        }
                        Notification.show("Er zijn " + byProductCodeEqualCaseInsensitive.get().size() + " artikels aangepast");
                    }
                }
            });
            comboBox.addCustomValueSetListener(e -> {
                Supplier supplier = new Supplier();
                supplier.setName(e.getDetail());
                supplier.setAlert(false);
                supplierService.save(supplier);
                optSupplierList.get().add(supplier);
                comboBox.setItems(optSupplierList.get());
                comboBox.setValue(supplier);

                //search every product with same productcode and save the Purchaselist to every item.
                if(selectedProduct.getProductCode() != null && selectedProduct.getProductCode().length() > 0){
                    Optional<List<Product>> byProductCodeEqualCaseInsensitive = productService.findByProductCodeEqualCaseInsensitive(selectedProduct.getProductCode());
                    if(byProductCodeEqualCaseInsensitive.isPresent()){
                        for(Product product : byProductCodeEqualCaseInsensitive.get()){
                            product.setPurchasePriseList(selectedProduct.getPurchasePriseList());
                            productService.save(product);
                        }
                        Notification.show("Er zijn " + byProductCodeEqualCaseInsensitive.get().size() + " artikels aangepast");
                    }
                }
            });
            return comboBox;
        }).setAutoWidth(true).setHeader("Leverancier").setResizable(true);

        purchasePriceGrid.addComponentColumn(item -> {
            Button addButton = new Button(new Icon(VaadinIcon.PLUS));
            addButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            addButton.addClickListener(e -> {
                PurchasePrice purchasePrice = new PurchasePrice();
                purchasePrice.setPurchaseDate(LocalDate.now());
                selectedProduct.getPurchasePriseList().add(purchasePrice);
                purchasePriceGrid.getDataProvider().refreshAll();
            });
            return addButton;
        });

        purchasePriceGrid.addComponentColumn(item -> {
            Button addButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
            addButton.addThemeVariants(ButtonVariant.LUMO_WARNING);
            addButton.addClickListener(e -> {
                selectedProduct.getPurchasePriseList().remove(item);
                purchasePriceGrid.getDataProvider().refreshAll();
            });
            return addButton;
        });

        return purchasePriceGrid;
    }

    private void setUpSplitLayouts() {
        mainSplitLayout = new SplitLayout();
        mainSplitLayout.setSizeFull();
        mainSplitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);
    }

    @PostConstruct
    private void init() {
        getSelectedProductListener.setEventConsumer(event -> {
            // UI-thread safe update
            UI.getCurrent().access(() -> {
                Notification.show("Geslecteerd artikel kan worden gewijzigd!");
                selectedProduct = event.getSelectedProduct();
                if(selectedProduct.getPurchasePriseList() != null && selectedProduct.getPurchasePriseList().size() > 0){
                    purchasePriceGrid.setItems(selectedProduct.getPurchasePriseList());
                }
                else{
                    List<PurchasePrice>purchasePriceList = new ArrayList<>();
                    purchasePriceList.add(new PurchasePrice());
                    selectedProduct.setPurchasePriseList(purchasePriceList);
                    purchasePriceGrid.setItems(purchasePriceList);
                }
            });
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {

    }
}
