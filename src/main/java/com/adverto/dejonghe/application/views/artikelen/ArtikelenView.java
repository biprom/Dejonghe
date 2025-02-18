package com.adverto.dejonghe.application.views.artikelen;

import com.adverto.dejonghe.application.dbservices.*;
import com.adverto.dejonghe.application.entities.product.enums.E_Product_Level;
import com.adverto.dejonghe.application.entities.product.enums.E_Product_PurchasingType;
import com.adverto.dejonghe.application.entities.product.product.*;
import com.adverto.dejonghe.application.repos.ProductRepo;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.apache.poi.ss.util.CellReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@PageTitle("Artikelen")
@Route("")
@Menu(order = 0, icon = LineAwesomeIconUrl.COG_SOLID)
public class ArtikelenView extends Div implements BeforeEnterObserver {

    //@Value( "${linkSpreadsheetBulk}" )
    private FileSystemResource linkToBulkSpreadsheet = new FileSystemResource("/Users/bramvandenberghe/Desktop/dejonghe.xlsx");

    private final ProductService productService;
    private ProductLevel1Service productLevel1Service;
    private ProductLevel2Service productLevel2Service;
    private ProductLevel3Service productLevel3Service;
    private ProductLevel4Service productLevel4Service;
    private ProductLevel5Service productLevel5Service;
    private ProductRepo productRepo;
    private SupplierService supplierService;

    private List<ProductLevel1>level1List;
    private List<ProductLevel2>level2List;
    private List<ProductLevel3>level3List;
    private List<ProductLevel4>level4List;
    private List<ProductLevel5>level5List;

    private Dialog bulkDialog;

    private final Grid<Product> grid = new Grid<>(Product.class, false);

    private TextField tfId;
    private TextField tfProductCode;
    private TextField tfFactoryName;
    private TextField tfInternalName;
    private TextField tfComment;

    private ComboBox<ProductLevel1>cbProductLevel1;
    private ComboBox<ProductLevel2>cbProductLevel2;
    private ComboBox<ProductLevel3>cbProductLevel3;
    private ComboBox<ProductLevel4>cbProductLevel4;
    private ComboBox<ProductLevel5>cbProductLevel5;
    private Checkbox checkbLinked;

    private Button bAddProductLevel1;
    private Button bAddProductLevel2;
    private Button bAddProductLevel3;
    private Button bAddProductLevel4;
    private Button bAddProductLevel5;

    private final Button cancel = new Button("Nieuw Artikel");
    private final Button save = new Button("Bewaar");
    private final Button addBulk = new Button("Voeg Bulk toe");

    private List<String> bulkStringList = new ArrayList<>();
    private E_Product_Level selectedProductLevel;
    private Grid<String>bulkGrid = new Grid<>(String.class);

    private Grid<PurchasePrice>purchasePriceGrid = new Grid<>();

    private Spreadsheet spreadsheet;

    private Binder<Product> binder;

    private Product selectedProduct;

    public ArtikelenView(ProductService productService,
                         ProductRepo productRepo,
                         ProductLevel1Service productLevel1Service,
                         ProductLevel2Service productLevel2Service,
                         ProductLevel3Service productLevel3Service,
                         ProductLevel4Service productLevel4Service,
                         ProductLevel5Service productLevel5Service,
                         SupplierService supplierService) {
        this.productService = productService;
        this.productRepo = productRepo;
        this.productLevel1Service = productLevel1Service;
        this.productLevel2Service = productLevel2Service;
        this.productLevel3Service = productLevel3Service;
        this.productLevel4Service = productLevel4Service;
        this.productLevel5Service = productLevel5Service;
        this.supplierService = supplierService;

        addClassNames("master-detail-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSplitterPosition(75);

        setUpSpreadSheet();
        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);
        add(splitLayout);
        setUpGrid();
        addDataToGrid();
        setUpBinder();
        setUpProductLevelComboBoxes();
        setUpBulkDialog();
        setUpPurchasegrid();


        cancel.addClickListener(e -> {
            Product product = new Product();
            product.setId(LocalDateTime.now().toString());
            product.setProductCode("");
            product.setFactoryName("");
            product.setInternalName("");
            product.setLinked(false);
            this.selectedProduct = product;
            populateForm(product);
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.selectedProduct == null) {
                    this.selectedProduct = new Product();
                }
                binder.writeBean(this.selectedProduct);
                productService.save(this.selectedProduct);
                addDataToGrid();
                //only clear the id tf -> for adding products faster (that are simular)
                tfId.setValue(LocalDateTime.now().toString());
                Notification.show("Data updated");
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });

        addBulk.addClickListener(e -> {
            bulkDialog.open();
            selectedProductLevel = E_Product_Level.PRODUCT;
        });
    }

    private void setUpPurchasegrid() {
        purchasePriceGrid.setWidth("100%");
        purchasePriceGrid.removeAllColumns();
        purchasePriceGrid.addComponentColumn(item -> {
            DatePicker datePicker = new DatePicker();
            if(item.getPurchaseDate() != null){
                datePicker.setValue(item.getPurchaseDate());
            }
            else{
                datePicker.setValue(LocalDate.now());
            }
            return datePicker;
        }).setHeader("Aankoopdatum");

        purchasePriceGrid.addComponentColumn(item -> {
            ComboBox<E_Product_PurchasingType> comboBox = new ComboBox<>();
            comboBox.setItems(E_Product_PurchasingType.values());
            if(item.getPurchasingType() != null){
                comboBox.setValue(item.getPurchasingType());
            }
            return comboBox;
        }).setHeader("Aankooptype");

        purchasePriceGrid.addComponentColumn(item -> {
            TextField textField = new TextField();
            if(item.getPrice() != null){
                textField.setValue(item.getPrice().toString());
            }
            else{
                textField.setValue("0.0");
            }
            return textField;
        }).setHeader("Aankoopprijs");

        purchasePriceGrid.addComponentColumn(item -> {
            ComboBox<Supplier> comboBox = new ComboBox<>();
            Optional<List<Supplier>> optSupplierList = supplierService.getAllProducts();
            if(!optSupplierList.isEmpty()){
                comboBox.setItems(optSupplierList.get());
            }
            if(item.getSupplier() != null){
                comboBox.setValue(item.getSupplier());
            }
            return comboBox;
        });
    }

    private void setUpSpreadSheet() {
        InputStream stream = null;
        try {
            stream = linkToBulkSpreadsheet.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            spreadsheet = new Spreadsheet(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        spreadsheet.setHeight("400px");
    }

    private void setUpBulkDialog() {
        bulkDialog = new Dialog();
        bulkDialog.addAttachListener(event -> {
            bulkStringList.clear();
            switch (selectedProductLevel){
                case PRODUCT:
                    bulkGrid.setVisible(false);
                    break;
                case PRODUCTLEVEL1:
                    if(!productLevel1Service.getProductLevel1Names().isEmpty()){
                        bulkGrid.setItems(productLevel1Service.getProductLevel1Names().get());
                        bulkGrid.setVisible(true);
                    }
                    else{
                        bulkGrid.setVisible(false);
                    }
                    break;
                case PRODUCTLEVEL2:
                    if(!productLevel2Service.getProductLevel2Names().isEmpty()){
                        bulkGrid.setItems(productLevel2Service.getProductLevel2Names().get());
                        bulkGrid.setVisible(true);
                    }
                    else{
                        bulkGrid.setVisible(false);
                    }
                    break;
                case PRODUCTLEVEL3:
                    if(!productLevel3Service.getProductLevel3Names().isEmpty()){
                        bulkGrid.setItems(productLevel3Service.getProductLevel3Names().get());
                        bulkGrid.setVisible(true);
                    }
                    else{
                        bulkGrid.setVisible(false);
                    }
                    break;
                case PRODUCTLEVEL4:
                    if(!productLevel4Service.getProductLevel4Names().isEmpty()){
                        bulkGrid.setItems(productLevel4Service.getProductLevel4Names().get());
                        bulkGrid.setVisible(true);
                    }
                    else{
                        bulkGrid.setVisible(false);
                    }
                    break;
                case PRODUCTLEVEL5:
                    if(!productLevel5Service.getProductLevel5Names().isEmpty()){
                        bulkGrid.setItems(productLevel5Service.getProductLevel5Names().get());
                        bulkGrid.setVisible(true);
                    }
                    else{
                        bulkGrid.setVisible(false);
                    }
                    break;
            }
        });
        bulkDialog.setHeaderTitle(
                String.format("Voeg Bulk toe"));
        bulkDialog.add("Ben je zeker dat je meerdere artikelen wilt kopieren vanuit Excel?");
        bulkDialog.add(spreadsheet);
        bulkDialog.add(getItemGrid());
        Button saveDialogButton = new Button("Bewaar", (e) -> {
            bulkStringList.clear();
            Set<CellReference> selectedCellReferences = spreadsheet.getSelectedCellReferences();
            selectedCellReferences.forEach(cellReference -> {
                bulkStringList.add(spreadsheet.getCell(cellReference).getStringCellValue());
            });
            switch (selectedProductLevel){
                case PRODUCT:
                    if(!bulkStringList.isEmpty()){
                        for(String item : bulkStringList){
                            Product newProduct = new Product();
                            newProduct.setId(LocalDateTime.now().toString());
                            newProduct.setInternalName(item);
                            newProduct.setLinked(false);
                            newProduct.setProductLevel1(cbProductLevel1.getValue());
                            newProduct.setProductLevel2(cbProductLevel2.getValue());
                            newProduct.setProductLevel3(cbProductLevel3.getValue());
                            newProduct.setProductLevel4(cbProductLevel4.getValue());
                            newProduct.setProductLevel5(cbProductLevel5.getValue());
                            productService.save(newProduct);
                        }
                        grid.setItems(q -> productRepo.findAll(VaadinSpringDataHelpers.toSpringPageRequest(q)).stream());
                    }
                    break;
                case PRODUCTLEVEL1:
                    if(bulkStringList.size() > 0){
                        productLevel1Service.saveProductlevelItems(bulkStringList);
                        cbProductLevel1.setItems(productLevel1Service.getAllProductLevel1().get());
                        cbProductLevel1.setItemLabelGenerator(x -> x.getName());
                    }
                    break;
                case PRODUCTLEVEL2:
                    if(bulkStringList.size() > 0){
                        productLevel2Service.saveProductlevelItems(bulkStringList);
                        cbProductLevel2.setItems(productLevel2Service.getAllProductLevel2().get());
                        cbProductLevel2.setItemLabelGenerator(x -> x.getName());
                    }
                    break;
                case PRODUCTLEVEL3:
                    if(bulkStringList.size() > 0){
                        productLevel3Service.saveProductlevelItems(bulkStringList);
                        cbProductLevel3.setItems(productLevel3Service.getAllProductLevel3().get());
                        cbProductLevel3.setItemLabelGenerator(x -> x.getName());
                    }
                    break;
                case PRODUCTLEVEL4:
                    if(bulkStringList.size() > 0){
                        productLevel4Service.saveProductlevelItems(bulkStringList);
                        cbProductLevel4.setItems(productLevel4Service.getAllProductLevel4().get());
                        cbProductLevel4.setItemLabelGenerator(x -> x.getName());
                    }
                    break;
                case PRODUCTLEVEL5:
                    if(bulkStringList.size() > 0){
                        productLevel5Service.saveProductlevelItems(bulkStringList);
                        cbProductLevel5.setItems(productLevel5Service.getAllProductLevel5().get());
                        cbProductLevel5.setItemLabelGenerator(x -> x.getName());
                    }
                    break;
            }
            bulkStringList.clear();
            bulkDialog.close();
        });
        saveDialogButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_SUCCESS);
        saveDialogButton.getStyle().set("margin-right", "auto");
        bulkDialog.getFooter().add(saveDialogButton);

        Button cancelDialogButton = new Button("Annuleer", (e) -> {
            bulkDialog.close();
        });
        cancelDialogButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        bulkDialog.getFooter().add(cancelDialogButton);
    }

    private Grid getItemGrid() {
        bulkGrid.removeAllColumns();
        bulkGrid.addColumn(item -> item).setHeader("Omschrijving");
        bulkGrid.addComponentColumn(item -> {
            Button removeButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
            removeButton.addThemeVariants(ButtonVariant.LUMO_WARNING);
            removeButton.addClickListener(event -> {
                switch (selectedProductLevel){
                    case PRODUCTLEVEL1:
                        productLevel1Service.removeByName(item);
                        try {
                            bulkGrid.setItems(productLevel1Service.getProductLevel1Names().get());
                        }
                        catch (Exception e) {
                            bulkGrid.setVisible(false);
                        }
                    case PRODUCTLEVEL2:
                        productLevel2Service.removeByName(item);
                        try {
                            bulkGrid.setItems(productLevel2Service.getProductLevel2Names().get());
                        }
                        catch (Exception e) {
                            bulkGrid.setVisible(false);
                        }
                    case PRODUCTLEVEL3:
                        productLevel3Service.removeByName(item);
                        try {
                            bulkGrid.setItems(productLevel3Service.getProductLevel3Names().get());
                        }
                        catch (Exception e) {
                            bulkGrid.setVisible(false);
                        }
                    case PRODUCTLEVEL4:
                        productLevel4Service.removeByName(item);
                        try {
                            bulkGrid.setItems(productLevel4Service.getProductLevel4Names().get());
                        }
                        catch (Exception e) {
                            bulkGrid.setVisible(false);
                        }
                    case PRODUCTLEVEL5:
                        productLevel5Service.removeByName(item);
                        try {
                            bulkGrid.setItems(productLevel5Service.getProductLevel5Names().get());
                        }
                        catch (Exception e) {
                            bulkGrid.setVisible(false);
                        }
                }

            });
            return removeButton;
        }).setHeader("Verwijder");
        return bulkGrid;
    }

    private void setUpProductLevelComboBoxes() {

        Optional<List<ProductLevel1>>optLevel1List = productLevel1Service.getAllProductLevel1();
        if (!optLevel1List.isEmpty()) {
            level1List = optLevel1List.get();
            cbProductLevel1.setItems(level1List);
            cbProductLevel1.setItemLabelGenerator(x -> x.getName());
        }
        else{
            level1List = new ArrayList<>();
        }

        Optional<List<ProductLevel2>>optLevel2List = productLevel2Service.getAllProductLevel2();
        if (!optLevel2List.isEmpty()) {
            level2List = optLevel2List.get();
            cbProductLevel2.setItems(level2List);
            cbProductLevel2.setItemLabelGenerator(x -> x.getName());
        }
        else{
            level2List = new ArrayList<>();
        }

        Optional<List<ProductLevel3>>optLevel3List = productLevel3Service.getAllProductLevel3();
        if (!optLevel3List.isEmpty()) {
            level3List = optLevel3List.get();
            cbProductLevel3.setItems(level3List);
            cbProductLevel3.setItemLabelGenerator(x -> x.getName());
        }
        else{
            level3List = new ArrayList<>();
        }

        Optional<List<ProductLevel4>>optLevel4List = productLevel4Service.getAllProductLevel4();
        if (!optLevel4List.isEmpty()) {
            level4List = optLevel4List.get();
            cbProductLevel4.setItems(level4List);
            cbProductLevel4.setItemLabelGenerator(x -> x.getName());
        }
        else{
            level4List = new ArrayList<>();
        }

        Optional<List<ProductLevel5>>optLevel5List = productLevel5Service.getAllProductLevel5();
        if (!optLevel5List.isEmpty()) {
            level5List = optLevel5List.get();
            cbProductLevel5.setItems(level5List);
            cbProductLevel5.setItemLabelGenerator(x -> x.getName());
        }
        else{
            level5List = new ArrayList<>();
        }

        if(!productLevel1Service.getAllProductLevel1().isEmpty()){
            cbProductLevel1.setItems(level1List);
        }

        if(!productLevel2Service.getAllProductLevel2().isEmpty()){
            cbProductLevel2.setItems(level2List);
        }

        if(!productLevel3Service.getAllProductLevel3().isEmpty()){
            cbProductLevel3.setItems(level3List);
        }

        if(!productLevel4Service.getAllProductLevel4().isEmpty()){
            cbProductLevel4.setItems(level4List);
        }

        if(!productLevel5Service.getAllProductLevel5().isEmpty()){
            cbProductLevel5.setItems(level5List);
        }

    }

    private void setUpBinder() {
        // Configure Form
        binder = new Binder<>(Product.class);

        binder.forField(tfId)
                .bind(x -> x.getId().toString(), (x,y)-> x.setId(y));
        binder.forField(tfProductCode)
                .bind(Product::getProductCode, Product::setProductCode);
        binder.forField(tfFactoryName)
                .bind(Product::getFactoryName, Product::setFactoryName);
        binder.forField(tfInternalName)
                .bind(Product::getInternalName, Product::setInternalName);
        binder.forField(tfComment)
                .bind(Product::getComment, Product::setComment);
        binder.forField(cbProductLevel1)
                        .bind(Product::getProductLevel1, Product::setProductLevel1);
        binder.forField(cbProductLevel2)
                .bind(Product::getProductLevel2, Product::setProductLevel2);
        binder.forField(cbProductLevel3)
                .bind(Product::getProductLevel3, Product::setProductLevel3);
        binder.forField(cbProductLevel4)
                .bind(Product::getProductLevel4, Product::setProductLevel4);
        binder.forField(cbProductLevel5)
                .bind(Product::getProductLevel5, Product::setProductLevel5);
        binder.forField(checkbLinked)
                .bind(Product::getLinked, Product::setLinked);
    }

    private void addDataToGrid() {
        Optional<List<Product>>optproducts = productService.getAllProducts();
        if (!optproducts.isEmpty()) {
            grid.setItems(q -> productRepo.findAll(VaadinSpringDataHelpers.toSpringPageRequest(q)).stream());
        }
        else{
            Notification.show("No products found");
            List<Product>products = new ArrayList<>();
            Product product = new Product();
            product.setId(LocalDateTime.now().toString());
            product.setProductCode("sampleCode");
            product.setFactoryName("fabrieksomschrijving");
            product.setInternalName("interne omschrijving");
            product.setLinked(false);
            products.add(product);
            productService.save(product);
            grid.setItems(products);
        }
    }

    private void setUpGrid() {
        // Configure Grid
        //grid.addColumn("id").setAutoWidth(true);
        grid.addColumn("productCode").setAutoWidth(true);
        grid.addColumn("factoryName").setAutoWidth(true);
        grid.addColumn("internalName").setAutoWidth(true);
        grid.addColumn("comment").setAutoWidth(true);
        grid.addColumn("linked").setAutoWidth(true);
        LitRenderer<Product> importantRenderer = LitRenderer.<Product>of(
                        "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", product -> product.getLinked() ? "check" : "minus").withProperty("color",
                        product -> product.getLinked()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(importantRenderer).setHeader("Gekoppeld").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                populateForm(event.getValue());
            }
             else {
                clearForm();
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        tfId = new TextField("Id");
        tfId.setEnabled(false);
        tfProductCode = new TextField("Artikelcode");
        tfFactoryName = new TextField("Omschrijving fabrikant");
        tfInternalName = new TextField("Interne omschrijving");
        tfComment = new TextField("Commentaar");
        cbProductLevel1 = new ComboBox("Artikelniveau1");
        cbProductLevel2 = new ComboBox("Artikelniveau2");
        cbProductLevel3 = new ComboBox("Artikelniveau3");
        cbProductLevel4 = new ComboBox("Artikelniveau4");
        cbProductLevel5 = new ComboBox("Artikelniveau5");
        checkbLinked = new Checkbox("Gekoppeld");
        formLayout.add(tfId,
                tfProductCode,
                tfFactoryName,
                tfInternalName,
                tfComment,
                setUpHorizontalLayoutFor(cbProductLevel1,bAddProductLevel1,E_Product_Level.PRODUCTLEVEL1),
                setUpHorizontalLayoutFor(cbProductLevel2,bAddProductLevel2,E_Product_Level.PRODUCTLEVEL2),
                        setUpHorizontalLayoutFor(cbProductLevel3,bAddProductLevel3,E_Product_Level.PRODUCTLEVEL3),
                                setUpHorizontalLayoutFor(cbProductLevel4,bAddProductLevel4,E_Product_Level.PRODUCTLEVEL4),
                                        setUpHorizontalLayoutFor(cbProductLevel5,bAddProductLevel5,E_Product_Level.PRODUCTLEVEL5),
                                                                        checkbLinked);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private HorizontalLayout setUpHorizontalLayoutFor(ComboBox comboBox, Button addButton, E_Product_Level productLevel) {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        horizontalLayout.setWidth("100%");
        comboBox.setWidth("100%");
        addButton = new Button(new Icon(VaadinIcon.PLUS));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_WARNING);
        addButton.addClickListener(event -> {
            switch(productLevel) {
                case E_Product_Level.PRODUCTLEVEL1:
                    Notification.show("Product level 1");
                    selectedProductLevel = E_Product_Level.PRODUCTLEVEL1;
                    break;
                case E_Product_Level.PRODUCTLEVEL2:
                    Notification.show("Product level 2");
                    selectedProductLevel = E_Product_Level.PRODUCTLEVEL2;
                    break;
                case E_Product_Level.PRODUCTLEVEL3:
                    Notification.show("Product level 3");
                    selectedProductLevel = E_Product_Level.PRODUCTLEVEL3;
                    break;
                case E_Product_Level.PRODUCTLEVEL4:
                    Notification.show("Product level 4");
                    selectedProductLevel = E_Product_Level.PRODUCTLEVEL4;
                    break;
                case E_Product_Level.PRODUCTLEVEL5:
                    Notification.show("Product level 5");
                    selectedProductLevel = E_Product_Level.PRODUCTLEVEL5;
                    break;
                default:
                    Notification.show("Geen geselecteerd niveau!");
            }
            bulkDialog.open();
        });
        horizontalLayout.add(comboBox,addButton);
        return horizontalLayout;
    }


    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBulk.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_WARNING);
        buttonLayout.add(save, cancel, addBulk);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.setHeight("100%");
        wrapper.add(grid);
        wrapper.add(purchasePriceGrid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Product value) {
        this.selectedProduct = value;
        binder.readBean(this.selectedProduct);

    }

}
