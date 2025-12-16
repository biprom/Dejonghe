package com.adverto.dejonghe.application.views.articles;

import com.adverto.dejonghe.application.dbservices.*;
import com.adverto.dejonghe.application.entities.product.enums.E_Product_Level;
import com.adverto.dejonghe.application.entities.product.product.*;
import com.adverto.dejonghe.application.repos.ProductRepo;
import com.adverto.dejonghe.application.views.subViews.SetView;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
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
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.apache.poi.ss.util.CellReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY_INLINE;

@PageTitle("Artikelen")
@Route("")
@Menu(order = 0, icon = LineAwesomeIconUrl.COG_SOLID)
public class ImportArticleView extends Div implements BeforeEnterObserver {

    //@Value( "${linkSpreadsheetBulk}" )
    private FileSystemResource linkToBulkSpreadsheet = new FileSystemResource("/Users/bramvandenberghe/Desktop/dejonghe.xlsx");
    //private FileSystemResource linkToBulkSpreadsheet = new FileSystemResource("D:\\Algemeen\\Documentatie\\Dejonghe-techniek\\Database\\dejonghe.xlsx\\dejonghe.xlsx");

    Notification deleteCustomerNotification;

    private final ProductService productService;
    private ProductLevel1Service productLevel1Service;
    private ProductLevel2Service productLevel2Service;
    private ProductLevel3Service productLevel3Service;
    private ProductLevel4Service productLevel4Service;
    private ProductLevel5Service productLevel5Service;
    private ProductLevel6Service productLevel6Service;
    private ProductLevel7Service productLevel7Service;

    private ProductRepo productRepo;
    private SupplierService supplierService;

    private SetView setView;

    private List<ProductLevel1>level1List;
    private List<ProductLevel2>level2List;
    private List<ProductLevel3>level3List;
    private List<ProductLevel4>level4List;
    private List<ProductLevel5>level5List;
    private List<ProductLevel6>level6List;
    private List<ProductLevel7>level7List;

    private Dialog bulkDialog;
    private Dialog configureSetDialog;
    Span levelSpan;

    private final Grid<Product> grid = new Grid<>(Product.class, false);
    private List<Product>productForGrid;

    private TextField tfFilter;
    private TextField tfId;
    private TextField tfProductCode;
    private TextField tfPositionNumber;
    private TextField tfInternalName;
    private TextField tfComment;
    private TextField tfUnit;

    private TextField tfPurchasePrice;
    private TextField tfSellMargin;
    private TextField tfSellPrice;

    private ComboBox<ProductLevel1>cbProductLevel1;
    private ComboBox<ProductLevel2>cbProductLevel2;
    private ComboBox<ProductLevel3>cbProductLevel3;
    private ComboBox<ProductLevel4>cbProductLevel4;
    private ComboBox<ProductLevel5>cbProductLevel5;
    private ComboBox<ProductLevel6>cbProductLevel6;
    private ComboBox<ProductLevel7>cbProductLevel7;

    Notification changePurchasePriceNotification;

    private Button bAddProductLevel1;
    private Button bAddProductLevel2;
    private Button bAddProductLevel3;
    private Button bAddProductLevel4;
    private Button bAddProductLevel5;
    private Button bAddProductLevel6;
    private Button bAddProductLevel7;

    private final Button buttonNewProduct = new Button("Nieuw Artikel");
    private final Button buttonSave = new Button("Bewaar");
    private final Button addBulk = new Button("Voeg Bulk toe");
    private final Button buttonConfigureSet = new Button("Maak set aan");

    private List<String> importBulkLevelList = new ArrayList<>();

    private E_Product_Level selectedProductLevel;
    private Grid<ProductDiscriptionAndId>bulkGrid = new Grid<>(ProductDiscriptionAndId.class);

    private Grid<PurchasePrice>purchasePriceGrid = new Grid<>();

    DecimalFormat df = new DecimalFormat("#.##");

    private Spreadsheet spreadsheet;

    private Binder<Product> productBinder;

    private Product selectedProduct;

    public ImportArticleView(ProductService productService,
                             ProductRepo productRepo,
                             ProductLevel1Service productLevel1Service,
                             ProductLevel2Service productLevel2Service,
                             ProductLevel3Service productLevel3Service,
                             ProductLevel4Service productLevel4Service,
                             ProductLevel5Service productLevel5Service,
                             ProductLevel6Service productLevel6Service,
                             ProductLevel7Service productLevel7Service,
                             SupplierService supplierService,
                             SetView setView) {
        this.productService = productService;
        this.productRepo = productRepo;
        this.productLevel1Service = productLevel1Service;
        this.productLevel2Service = productLevel2Service;
        this.productLevel3Service = productLevel3Service;
        this.productLevel4Service = productLevel4Service;
        this.productLevel5Service = productLevel5Service;
        this.productLevel6Service = productLevel6Service;
        this.productLevel7Service = productLevel7Service;
        this.supplierService = supplierService;
        this.setView = setView;

        addClassNames("master-detail-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSplitterPosition(75);
        createReportError();
        setUpSpreadSheet();
        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);
        add(splitLayout);
        setUpGrid();
        addDataToGrid();
        setUpBinder();
        setUpProductLevelComboBoxes();
        setUpBulkDialog();
        setUpConfigureSetDialog();
        setUpPurchasePricegrid();
        setUpPriceValueChangeListeners();
        setUpFilter();
        createReportChangePurchasePrice();
        setUpLevelSpan();

        buttonNewProduct.addClickListener(e -> {
            Product newProduct = new Product();
            newProduct.setId(LocalDateTime.now().toString());
            newProduct.setProductCode("");
            newProduct.setPurchasePrice(0.0);
            newProduct.setSellPrice(0.0);
            newProduct.setSellMargin(0.0);
            newProduct.setInternalName("");
            newProduct.setLinked(false);
            if(cbProductLevel1.getValue() != null) {
                newProduct.setProductLevel1(cbProductLevel1.getValue());
            }
            if(cbProductLevel2.getValue() != null) {
                newProduct.setProductLevel2(cbProductLevel2.getValue());
            }
            if(cbProductLevel3.getValue() != null) {
                newProduct.setProductLevel3(cbProductLevel3.getValue());
            }
            if(cbProductLevel4.getValue() != null) {
                newProduct.setProductLevel4(cbProductLevel4.getValue());
            }
            if(cbProductLevel5.getValue() != null) {
                newProduct.setProductLevel5(cbProductLevel5.getValue());
            }
            if(cbProductLevel6.getValue() != null) {
                newProduct.setProductLevel6(cbProductLevel6.getValue());
            }
            if(cbProductLevel7.getValue() != null) {
                newProduct.setProductLevel7(cbProductLevel7.getValue());
            }
            selectedProduct = newProduct;
            populateForm(newProduct);
            //refreshGrid();
        });

        buttonSave.addClickListener(e -> {
            try {
                if (selectedProduct != null) {
                    productBinder.writeBean(selectedProduct);
                    productService.save(selectedProduct);
                    if(grid.getSelectedItems() == null) {
                        productForGrid.add(selectedProduct);
                    }
                    grid.setItems(productForGrid);

                    tfId.setValue(LocalDateTime.now().toString());
                    Notification.show("Data updated");
                }
                else{
                    Notification.show("Gelieve eerst een artikel aan te maken of te selecteren aub");
                }
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

        buttonConfigureSet.addClickListener(e -> {
            configureSetDialog.open();
            //TODO fix everything!!!
            setView.setSelectedProductForSet(selectedProduct);
        });
    }

    private void setUpConfigureSetDialog() {
        configureSetDialog = new Dialog();
        configureSetDialog.setWidth("90%");
        configureSetDialog.setHeight("90%");
        configureSetDialog.add(setView);
        configureSetDialog.addDialogCloseActionListener(event -> {
            grid.getDataProvider().refreshAll();
            configureSetDialog.close();
        });
    }

    private void setUpLevelSpan() {
        levelSpan.addClickListener(event -> {

                ProductLevel1 productLevel1 = cbProductLevel1.getValue();
                ProductLevel2 productLevel2 = cbProductLevel2.getValue();
                ProductLevel3 productLevel3 = cbProductLevel3.getValue();
                ProductLevel4 productLevel4 = cbProductLevel4.getValue();
                ProductLevel5 productLevel5 = cbProductLevel5.getValue();
                ProductLevel6 productLevel6 = cbProductLevel6.getValue();
                ProductLevel7 productLevel7 = cbProductLevel7.getValue();

                clearForm();

                //TODO filter by Comboboxes

                if(productLevel1 != null){
                    cbProductLevel1.setValue(productLevel1);
                }
                if(productLevel2 != null){
                    cbProductLevel2.setValue(productLevel2);
                }
                if(productLevel3 != null){
                    cbProductLevel3.setValue(productLevel3);
                }
                if(productLevel4 != null){
                    cbProductLevel4.setValue(productLevel4);
                }
                if(productLevel5 != null){
                    cbProductLevel5.setValue(productLevel5);
                }
                if(productLevel6 != null){
                    cbProductLevel6.setValue(productLevel6);
                }
                if(productLevel7 != null){
                    cbProductLevel7.setValue(productLevel7);
                }
        });
    }

    private Notification createReportError() {
        deleteCustomerNotification = new Notification();
        deleteCustomerNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);

        Icon icon = VaadinIcon.WARNING.create();
        Button retryBtn = new Button("Annuleer",
                clickEvent -> deleteCustomerNotification.close());
        retryBtn.getStyle().setMargin("0 0 0 var(--lumo-space-l)");

        var layout = new HorizontalLayout(icon,
                new Text("Ben je zeker dat je dit artikel wil wissen?"), retryBtn,
                createCloseBtn(deleteCustomerNotification));
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        deleteCustomerNotification.add(layout);

        return deleteCustomerNotification;
    }

    public Button createCloseBtn(Notification notification) {
        Button closeBtn = new Button(VaadinIcon.TRASH.create(),
                clickEvent -> {
                    if(productForGrid != null){
                        //when filter is selected
                        productForGrid.remove(selectedProduct);
                        grid.getDataProvider().refreshAll();
                        productService.delete(selectedProduct);
                        Notification.show("Artikel is verwijderd");
                    }
                    else{
                        //when filter is not touched
                        productService.delete(selectedProduct);
                        addDataToGrid();
                        Notification.show("Artikel is verwijderd");
                    }
                    notification.close();
                });
        closeBtn.addThemeVariants(LUMO_TERTIARY_INLINE);

        return closeBtn;
    }

    private void setUpFilter() {
        tfFilter.addValueChangeListener(e -> {
            Optional<List<Product>> optCustomer = productService.getProductByInternalNameOrCodeOrComment(tfFilter.getValue(), tfFilter.getValue(), tfFilter.getValue());
            if(optCustomer.isPresent()) {
                productForGrid = optCustomer.get();
                grid.setItems(productForGrid);
            }
            else{
                Notification.show("Geen klanten gevonden");
            }
        });
    }

    private void setUpPriceValueChangeListeners() {
        tfPurchasePrice.addValueChangeListener(listener -> {
            if(listener.isFromClient()){
                //check if there are other selected products with this code and change them
                tryToCalculateSellPrice(selectedProduct);
                productBinder.readBean(selectedProduct);
                changePurchasePriceNotification.open();
            }

        });
        tfSellMargin.addValueChangeListener(listener -> {
            if(listener.isFromClient()){
                tryToCalculateSellPrice(selectedProduct);
                productBinder.readBean(selectedProduct);
            }
        });
        tfSellPrice.addValueChangeListener(listener -> {
            if(listener.isFromClient()){
                productBinder.readBean(selectedProduct);
            }
        });
    }

    private void setUpPurchasePricegrid() {
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
        bulkDialog.setWidth("50%");
        bulkDialog.addAttachListener(event -> {
            importBulkLevelList.clear();
            switch (selectedProductLevel){
                case PRODUCT:
                    bulkGrid.setVisible(false);
                    break;
                case PRODUCTLEVEL1:
                    if(!productLevel1Service.getProductDiscriptionAndId().isEmpty()){
                        bulkGrid.setItems(productLevel1Service.getProductDiscriptionAndId().get());
                        bulkGrid.setVisible(true);
                    }
                    else{
                        bulkGrid.setVisible(false);
                    }
                    break;
                case PRODUCTLEVEL2:
                    if(!productLevel2Service.getProductLevel2NamesAndId().isEmpty()){
                        bulkGrid.setItems(productLevel2Service.getProductLevel2NamesAndId().get());
                        bulkGrid.setVisible(true);
                    }
                    else{
                        bulkGrid.setVisible(false);
                    }
                    break;
                case PRODUCTLEVEL3:
                    if(!productLevel3Service.getProductLevel3NamesAndLevelAndId().isEmpty()){
                        bulkGrid.setItems(productLevel3Service.getProductLevel3NamesAndLevelAndId().get());
                        bulkGrid.setVisible(true);
                    }
                    else{
                        bulkGrid.setVisible(false);
                    }
                    break;
                case PRODUCTLEVEL4:
                    if(!productLevel4Service.getProductLevel4NamesAndLevelAndId().isEmpty()){
                        bulkGrid.setItems(productLevel4Service.getProductLevel4NamesAndLevelAndId().get());
                        bulkGrid.setVisible(true);
                    }
                    else{
                        bulkGrid.setVisible(false);
                    }
                    break;
                case PRODUCTLEVEL5:
                    if(!productLevel5Service.getProductLevel5NamesAndLevelAndId().isEmpty()){
                        bulkGrid.setItems(productLevel5Service.getProductLevel5NamesAndLevelAndId().get());
                        bulkGrid.setVisible(true);
                    }
                    else{
                        bulkGrid.setVisible(false);
                    }
                    break;
                case PRODUCTLEVEL6:
                    if(!productLevel6Service.getProductLevel6NamesAndLevelAndId().isEmpty()){
                        bulkGrid.setItems(productLevel6Service.getProductLevel6NamesAndLevelAndId().get());
                        bulkGrid.setVisible(true);
                    }
                    else{
                        bulkGrid.setVisible(false);
                    }
                    break;
                case PRODUCTLEVEL7:
                    if(!productLevel7Service.getProductLevel7NamesAndLevelAndId().isEmpty()){
                        bulkGrid.setItems(productLevel7Service.getProductLevel7NamesAndLevelAndId().get());
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
            importBulkLevelList.clear();
            Set<CellReference> selectedCellReferences = spreadsheet.getSelectedCellReferences();

            //fill importBulkLevelList with selected Items from excel to save in levels
            if(!(selectedProductLevel == E_Product_Level.PRODUCT)){
                selectedCellReferences.forEach(cellReference -> {
                    importBulkLevelList.add(spreadsheet.getCell(cellReference).getStringCellValue());
                });
            }
            //else add Products from excel- selection
            else{
                CellReference maxCelReference = selectedCellReferences.stream().min(Comparator.comparing(CellReference::getRow)).get();
                int minRow = maxCelReference.getRow();
                CellReference minCellReference = selectedCellReferences.stream().max(Comparator.comparing(CellReference::getRow)).get();
                int maxRow = minCellReference.getRow();
                int i = minRow;
                while (i <= maxRow) {
                    Product newProduct = new Product();
                    newProduct.setLinked(false);
                    if(cbProductLevel1.getValue() != null){
                        newProduct.setProductLevel1(cbProductLevel1.getValue());
                    }
                    if(cbProductLevel2.getValue() != null){
                        newProduct.setProductLevel2(cbProductLevel2.getValue());
                    }
                    if(cbProductLevel3.getValue() != null){
                        newProduct.setProductLevel3(cbProductLevel3.getValue());
                    }
                    if(cbProductLevel4.getValue() != null){
                        newProduct.setProductLevel4(cbProductLevel4.getValue());
                    }
                    if(cbProductLevel5.getValue() != null){
                        newProduct.setProductLevel5(cbProductLevel5.getValue());
                    }
                    if(cbProductLevel6.getValue() != null){
                        newProduct.setProductLevel6(cbProductLevel6.getValue());
                    }
                    if(cbProductLevel7.getValue() != null){
                        newProduct.setProductLevel7(cbProductLevel7.getValue());
                    }
                    int finalI = i;
                    selectedCellReferences.stream().filter(item -> (item.getRow() == finalI)&&(item.getCol() == 0)).findFirst().ifPresent(cellReference -> {
                        newProduct.setProductCode(spreadsheet.getCell(cellReference).getStringCellValue());
                    });
                    selectedCellReferences.stream().filter(item -> (item.getRow() == finalI)&&(item.getCol() == 1)).findFirst().ifPresent(cellReference -> {
                        newProduct.setPositionNumber(spreadsheet.getCell(cellReference).getStringCellValue());
                    });
                    selectedCellReferences.stream().filter(item -> (item.getRow() == finalI)&&(item.getCol() == 2)).findFirst().ifPresent(cellReference -> {
                        newProduct.setInternalName(spreadsheet.getCell(cellReference).getStringCellValue());
                    });
                    selectedCellReferences.stream().filter(item -> (item.getRow() == finalI)&&(item.getCol() == 3)).findFirst().ifPresent(cellReference -> {
                        newProduct.setUnit(spreadsheet.getCell(cellReference).getStringCellValue());
                    });
                    selectedCellReferences.stream().filter(item -> (item.getRow() == finalI)&&(item.getCol() == 4)).findFirst().ifPresent(cellReference -> {
                        newProduct.setSellPrice(spreadsheet.getCell(cellReference).getNumericCellValue());
                    });
                    selectedCellReferences.stream().filter(item -> (item.getRow() == finalI)&&(item.getCol() == 5)).findFirst().ifPresent(cellReference -> {
                        newProduct.setPurchasePrice(spreadsheet.getCell(cellReference).getNumericCellValue());
                    });
                    if((newProduct.getSellPrice() != null) && (newProduct.getPurchasePrice() != null)){
                        newProduct.setSellMargin((newProduct.getSellPrice()/newProduct.getPurchasePrice()));
                    }
                    else{
                        newProduct.setSellMargin(0.0);
                        Notification.show("Kon geen marge berekenen op rij : " + finalI,5, Notification.Position.MIDDLE);
                    }
                    selectedCellReferences.stream().filter(item -> (item.getRow() == finalI)&&(item.getCol() == 8)).findFirst().ifPresent(cellReference -> {
                        newProduct.setComment(spreadsheet.getCell(cellReference).getStringCellValue());
                    });

                    productService.save(newProduct);
                    i++;
                }
                grid.setItems(q -> productRepo.findAll(VaadinSpringDataHelpers.toSpringPageRequest(q)).stream());
            }


            switch (selectedProductLevel){
                case PRODUCTLEVEL1:
                    if(importBulkLevelList.size() > 0){
                        productLevel1Service.saveProductlevelItems(importBulkLevelList);
                        cbProductLevel1.setItems(productLevel1Service.getAllProductLevel1().get());
                        cbProductLevel1.setItemLabelGenerator(x -> x.getName());
                        cbProductLevel1.setEnabled(true);
                    }
                    break;
                case PRODUCTLEVEL2:
                    if(importBulkLevelList.size() > 0){
                        if(cbProductLevel1.getValue() != null){
                            productLevel2Service.saveProductlevelItems(importBulkLevelList, cbProductLevel1.getValue());
                            cbProductLevel2.setItems(productLevel2Service.getProductLevel2sFromPreviousLevels(cbProductLevel1.getValue()).get());
                            cbProductLevel2.setItemLabelGenerator(x -> x.getName());
                            cbProductLevel2.setEnabled(true);
                        }
                        else{
                            Notification.show("Gelieve eerst een een productlevel 1 te selecteren aub");
                        }
                    }
                    break;
                case PRODUCTLEVEL3:
                    if(importBulkLevelList.size() > 0){
                        if(cbProductLevel2.getValue() != null){
                            productLevel3Service.saveProductlevelItems(importBulkLevelList, cbProductLevel2.getValue());
                            cbProductLevel3.setItems(productLevel3Service.getProductLevel3sFromPreviousLevels(cbProductLevel2.getValue(),cbProductLevel1.getValue()).get());
                            cbProductLevel3.setItemLabelGenerator(x -> x.getName());
                            cbProductLevel3.setEnabled(true);
                        }
                        else{
                            Notification.show("Gelieve eerst een een productlevel 1 te selecteren aub");
                        }
                    }
                    break;
                case PRODUCTLEVEL4:
                    if(importBulkLevelList.size() > 0){
                        if(cbProductLevel3.getValue() != null){
                            productLevel4Service.saveProductlevelItems(importBulkLevelList, cbProductLevel3.getValue());
                            cbProductLevel4.setItems(productLevel4Service.getProductLevel4ByPreviousLevelNames(cbProductLevel3.getValue(),cbProductLevel2.getValue(),cbProductLevel1.getValue()).get());
                            cbProductLevel4.setItemLabelGenerator(x -> x.getName());
                            cbProductLevel4.setEnabled(true);
                        }
                        else{
                            Notification.show("Gelieve eerst een een productlevel 1 te selecteren aub");
                        }
                    }
                    break;
                case PRODUCTLEVEL5:
                    if(importBulkLevelList.size() > 0){
                        if(cbProductLevel4.getValue() != null){
                            productLevel5Service.saveProductlevelItems(importBulkLevelList, cbProductLevel4.getValue());
                            cbProductLevel5.setItems(productLevel5Service.getProductLevel5ByPreviousLevelNames(cbProductLevel4.getValue(),cbProductLevel3.getValue(),cbProductLevel2.getValue(),cbProductLevel1.getValue()).get());
                            cbProductLevel5.setItemLabelGenerator(x -> x.getName());
                            cbProductLevel5.setEnabled(true);
                        }
                        else{
                            Notification.show("Gelieve eerst een een productlevel 1 te selecteren aub");
                        }
                    }
                    break;
                case PRODUCTLEVEL6:
                    if(importBulkLevelList.size() > 0){
                        if(cbProductLevel5.getValue() != null){
                            productLevel6Service.saveProductlevelItems(importBulkLevelList, cbProductLevel5.getValue());
                            cbProductLevel6.setItems(productLevel6Service.getProductLevel6ByPreviousLevelNames(cbProductLevel5.getValue(),cbProductLevel4.getValue(),cbProductLevel3.getValue(),cbProductLevel2.getValue(),cbProductLevel1.getValue()).get());
                            cbProductLevel6.setItemLabelGenerator(x -> x.getName());
                            cbProductLevel6.setEnabled(true);
                        }
                        else{
                            Notification.show("Gelieve eerst een een productlevel 1 te selecteren aub");
                        }
                    }
                    break;
                case PRODUCTLEVEL7:
                    if(importBulkLevelList.size() > 0){
                        if(cbProductLevel6.getValue() != null){
                            productLevel7Service.saveProductlevelItems(importBulkLevelList, cbProductLevel6.getValue());
                            cbProductLevel7.setItems(productLevel7Service.getProductLevel7ByPreviousLevelNames(cbProductLevel6.getValue(),cbProductLevel5.getValue(),cbProductLevel4.getValue(),cbProductLevel3.getValue(),cbProductLevel2.getValue(),cbProductLevel1.getValue()).get());
                            cbProductLevel7.setItemLabelGenerator(x -> x.getName());
                            cbProductLevel7.setEnabled(true);
                        }
                        else{
                            Notification.show("Gelieve eerst een een productlevel 1 te selecteren aub");
                        }
                    }
                    break;
            }
            importBulkLevelList.clear();
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
        bulkGrid.addColumn(item -> item.getDiscription()).setHeader("Omschrijving").setAutoWidth(true);
        bulkGrid.addComponentColumn(item -> {
            Button removeButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
            removeButton.addThemeVariants(ButtonVariant.LUMO_WARNING);
            removeButton.addClickListener(event -> {
                switch (selectedProductLevel){
                    case PRODUCTLEVEL1:
                        productLevel1Service.removeById(item.getId());
                        try {
                            bulkGrid.setItems(productLevel1Service.getProductDiscriptionAndId().get());
                        }
                        catch (Exception e) {
                            bulkGrid.setVisible(false);
                        }
                        break;
                    case PRODUCTLEVEL2:
                        productLevel2Service.removeById(item.getId());
                        try {
                            bulkGrid.setItems(productLevel2Service.getProductLevel2NamesAndId().get());
                        }
                        catch (Exception e) {
                            bulkGrid.setVisible(false);
                        }
                        break;
                    case PRODUCTLEVEL3:
                        productLevel3Service.removeById(item.getId());
                        try {
                            bulkGrid.setItems(productLevel3Service.getProductLevel3NamesAndLevelAndId().get());
                        }
                        catch (Exception e) {
                            bulkGrid.setVisible(false);
                        }
                        break;
                    case PRODUCTLEVEL4:
                        productLevel4Service.removeById(item.getId());
                        try {
                            bulkGrid.setItems(productLevel4Service.getProductLevel4NamesAndLevelAndId().get());
                        }
                        catch (Exception e) {
                            bulkGrid.setVisible(false);
                        }
                        break;
                    case PRODUCTLEVEL5:
                        productLevel5Service.removeById(item.getId());
                        try {
                            bulkGrid.setItems(productLevel5Service.getProductLevel5NamesAndLevelAndId().get());
                        }
                        catch (Exception e) {
                            bulkGrid.setVisible(false);
                        }
                        break;
                    case PRODUCTLEVEL6:
                        productLevel6Service.removeById(item.getId());
                        try {
                            bulkGrid.setItems(productLevel6Service.getProductLevel6NamesAndLevelAndId().get());
                        }
                        catch (Exception e) {
                            bulkGrid.setVisible(false);
                        }
                        break;
                    case PRODUCTLEVEL7:
                        productLevel7Service.removeById(item.getId());
                        try {
                            bulkGrid.setItems(productLevel7Service.getProductLevel7NamesAndLevelAndId().get());
                        }
                        catch (Exception e) {
                            bulkGrid.setVisible(false);
                        }
                        break;
                }
            });
            return removeButton;
        }).setHeader("Verwijder");
        return bulkGrid;
    }

    private void setUpProductLevelComboBoxes() {

        cbProductLevel2.setEnabled(false);
        cbProductLevel3.setEnabled(false);
        cbProductLevel4.setEnabled(false);
        cbProductLevel5.setEnabled(false);
        cbProductLevel6.setEnabled(false);
        cbProductLevel7.setEnabled(false);
        fillComboBoxLevel1WithItemsToStart();


        cbProductLevel1.addValueChangeListener(event -> {
            Optional<List<ProductLevel2>>productLevel2List = productLevel2Service.getProductLevel2sFromPreviousLevels(event.getValue());
            if (!productLevel2List.isEmpty()) {
                cbProductLevel2.setEnabled(true);
                level2List = productLevel2List.get();
                cbProductLevel2.setItems(level2List);
                cbProductLevel2.setItemLabelGenerator(x -> x.getName());
                cbProductLevel3.clear();
                cbProductLevel3.setEnabled(false);
                cbProductLevel4.clear();
                cbProductLevel4.setEnabled(false);
                cbProductLevel5.clear();
                cbProductLevel5.setEnabled(false);
                cbProductLevel6.clear();
                cbProductLevel6.setEnabled(false);
                cbProductLevel7.clear();
                cbProductLevel7.setEnabled(false);

                //filter Products
                if(selectedProduct == null){
                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName());
                    if(allProductsByCategory.isPresent()){
                        productForGrid = allProductsByCategory.get();
                        grid.setItems(productForGrid);
                    }
                    else{
                        List<Product>products = new ArrayList<>();
                        productForGrid = products;
                        grid.setItems(productForGrid);
                    }
                }

            }
            else{
                cbProductLevel2.clear();
                cbProductLevel2.setEnabled(false);
                cbProductLevel3.clear();
                cbProductLevel3.setEnabled(false);
                cbProductLevel4.clear();
                cbProductLevel4.setEnabled(false);
                cbProductLevel5.clear();
                cbProductLevel5.setEnabled(false);
                cbProductLevel6.clear();
                cbProductLevel6.setEnabled(false);
                cbProductLevel7.clear();
                cbProductLevel7.setEnabled(false);
                if(selectedProduct == null){
                    //filter Products
                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName());
                    if(allProductsByCategory.isPresent()){
                        productForGrid = allProductsByCategory.get();
                        grid.setItems(productForGrid);
                    }
                    else{
                        List<Product>products = new ArrayList<>();
                        productForGrid = products;
                        grid.setItems(productForGrid);
                    }
                }
            }

        });

        cbProductLevel2.addValueChangeListener(event -> {
            Optional<List<ProductLevel3>>productLevel3List = productLevel3Service.getProductLevel3sFromPreviousLevels(cbProductLevel2.getValue(),cbProductLevel1.getValue());
            if (!productLevel3List.isEmpty()) {
                cbProductLevel3.setEnabled(true);
                level3List = productLevel3List.get();
                cbProductLevel3.setItems(level3List);
                cbProductLevel3.setItemLabelGenerator(x -> x.getName());
                cbProductLevel4.clear();
                cbProductLevel4.setEnabled(false);
                cbProductLevel5.clear();
                cbProductLevel5.setEnabled(false);
                cbProductLevel6.clear();
                cbProductLevel6.setEnabled(false);
                cbProductLevel7.clear();
                cbProductLevel7.setEnabled(false);

                //filter Products
                if(selectedProduct == null){
                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName(),cbProductLevel1.getValue().getName());
                    if(allProductsByCategory.isPresent()){
                        productForGrid = allProductsByCategory.get();
                        grid.setItems(productForGrid);
                    }
                    else{
                        List<Product>products = new ArrayList<>();
                        productForGrid = products;
                        grid.setItems(productForGrid);
                    }
                }
            }
            else{
                cbProductLevel3.clear();
                cbProductLevel3.setEnabled(false);
                cbProductLevel4.clear();
                cbProductLevel4.setEnabled(false);
                cbProductLevel5.clear();
                cbProductLevel5.setEnabled(false);
                cbProductLevel6.clear();
                cbProductLevel6.setEnabled(false);
                cbProductLevel7.clear();
                cbProductLevel7.setEnabled(false);
                if(selectedProduct == null){
                    //filter Products
                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName(),cbProductLevel1.getValue().getName());
                    if(allProductsByCategory.isPresent()){
                        productForGrid = allProductsByCategory.get();
                        grid.setItems(productForGrid);
                    }
                    else{
                        List<Product>products = new ArrayList<>();
                        productForGrid = products;
                        grid.setItems(productForGrid);
                    }
                }
            }
        });

        cbProductLevel3.addValueChangeListener(event -> {
            Optional<List<ProductLevel4>>productLevel4List = productLevel4Service.getProductLevel4ByPreviousLevelNames(cbProductLevel3.getValue(),cbProductLevel2.getValue(),cbProductLevel1.getValue());
            if (!productLevel4List.isEmpty()) {
                cbProductLevel4.setEnabled(true);
                level4List = productLevel4List.get();
                cbProductLevel4.setItems(level4List);
                cbProductLevel4.setItemLabelGenerator(x -> x.getName());
                cbProductLevel5.clear();
                cbProductLevel5.setEnabled(false);
                cbProductLevel6.clear();
                cbProductLevel6.setEnabled(false);
                cbProductLevel7.clear();
                cbProductLevel7.setEnabled(false);

                if(selectedProduct == null){
                    //filter Products
                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName(),cbProductLevel2.getValue().getName(),cbProductLevel1.getValue().getName());
                    if(allProductsByCategory.isPresent()){
                        productForGrid = allProductsByCategory.get();
                        grid.setItems(productForGrid);
                    }
                    else{
                        List<Product>products = new ArrayList<>();
                        productForGrid = products;
                        grid.setItems(productForGrid);
                    }
                }

            }
            else{
                cbProductLevel4.clear();
                cbProductLevel4.setEnabled(false);
                cbProductLevel5.clear();
                cbProductLevel5.setEnabled(false);
                cbProductLevel6.clear();
                cbProductLevel6.setEnabled(false);
                cbProductLevel7.clear();
                cbProductLevel7.setEnabled(false);
                if(selectedProduct == null){
                    //filter Products
                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName(),cbProductLevel2.getValue().getName(),cbProductLevel1.getValue().getName());
                    if(allProductsByCategory.isPresent()){
                        productForGrid = allProductsByCategory.get();
                        grid.setItems(productForGrid);
                    }
                    else{
                        List<Product>products = new ArrayList<>();
                        productForGrid = products;
                        grid.setItems(productForGrid);
                    }
                }
            }
        });

        cbProductLevel4.addValueChangeListener(event -> {
            Optional<List<ProductLevel5>>productLevel5List = productLevel5Service.getProductLevel5ByPreviousLevelNames(cbProductLevel4.getValue(),cbProductLevel3.getValue(),cbProductLevel2.getValue(),cbProductLevel1.getValue());
            if (!productLevel5List.isEmpty()) {
                cbProductLevel5.setEnabled(true);
                level5List = productLevel5List.get();
                cbProductLevel5.setItems(level5List);
                cbProductLevel5.setItemLabelGenerator(x -> x.getName());
                cbProductLevel6.clear();
                cbProductLevel6.setEnabled(false);
                cbProductLevel7.clear();
                cbProductLevel7.setEnabled(false);

                if(selectedProduct == null){
                    //filter Products
                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName(),cbProductLevel3.getValue().getName(),cbProductLevel2.getValue().getName(),cbProductLevel1.getValue().getName());
                    if(allProductsByCategory.isPresent()){
                        productForGrid = allProductsByCategory.get();
                        grid.setItems(productForGrid);
                    }
                    else{
                        List<Product>products = new ArrayList<>();
                        productForGrid = products;
                        grid.setItems(productForGrid);
                    }
                }
            }
            else{
                cbProductLevel5.clear();
                cbProductLevel5.setEnabled(false);
                cbProductLevel6.clear();
                cbProductLevel6.setEnabled(false);
                cbProductLevel7.clear();
                cbProductLevel7.setEnabled(false);
                if(selectedProduct == null){
                    //filter Products
                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName(),cbProductLevel3.getValue().getName(),cbProductLevel2.getValue().getName(),cbProductLevel1.getValue().getName());
                    if(allProductsByCategory.isPresent()){
                        productForGrid = allProductsByCategory.get();
                        grid.setItems(productForGrid);
                    }
                    else{
                        List<Product>products = new ArrayList<>();
                        productForGrid = products;
                        grid.setItems(productForGrid);
                    }
                }
            }
        });

        cbProductLevel5.addValueChangeListener(event -> {
            Optional<List<ProductLevel6>>productLevel6List = productLevel6Service.getProductLevel6ByPreviousLevelNames(cbProductLevel5.getValue(),cbProductLevel4.getValue(),cbProductLevel3.getValue(),cbProductLevel2.getValue(),cbProductLevel1.getValue());
            if (!productLevel6List.isEmpty()) {
                cbProductLevel6.setEnabled(true);
                level6List = productLevel6List.get();
                cbProductLevel6.setItems(level6List);
                cbProductLevel6.setItemLabelGenerator(x -> x.getName());
                cbProductLevel7.clear();
                cbProductLevel7.setEnabled(false);

                if(selectedProduct == null){
                    //filter Products
                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName(),cbProductLevel4.getValue().getName(),cbProductLevel3.getValue().getName(),cbProductLevel2.getValue().getName(),cbProductLevel1.getValue().getName());
                    if(allProductsByCategory.isPresent()){
                        productForGrid = allProductsByCategory.get();
                        grid.setItems(productForGrid);
                    }
                    else{
                        List<Product>products = new ArrayList<>();
                        productForGrid = products;
                        grid.setItems(productForGrid);
                    }
                }

            }
            else{
                cbProductLevel6.clear();
                cbProductLevel6.setEnabled(false);
                cbProductLevel7.clear();
                cbProductLevel7.setEnabled(false);
                if(selectedProduct == null){
                    //filter Products
                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName(),cbProductLevel4.getValue().getName(),cbProductLevel3.getValue().getName(),cbProductLevel2.getValue().getName(),cbProductLevel1.getValue().getName());
                    if(allProductsByCategory.isPresent()){
                        productForGrid = allProductsByCategory.get();
                        grid.setItems(productForGrid);
                    }
                    else{
                        List<Product>products = new ArrayList<>();
                        productForGrid = products;
                        grid.setItems(productForGrid);
                    }
                }
            }
        });

        cbProductLevel6.addValueChangeListener(event -> {
            Optional<List<ProductLevel7>>productLevel7List = productLevel7Service.getProductLevel7ByPreviousLevelNames(cbProductLevel6.getValue(),cbProductLevel5.getValue(),cbProductLevel4.getValue(),cbProductLevel3.getValue(),cbProductLevel2.getValue(),cbProductLevel1.getValue());
            if (!productLevel7List.isEmpty()) {
                cbProductLevel7.setEnabled(true);
                level7List = productLevel7List.get();
                cbProductLevel7.setItems(level7List);
                cbProductLevel7.setItemLabelGenerator(x -> x.getName());

                if(selectedProduct == null){
                    //filter Products
                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName(),cbProductLevel5.getValue().getName(),cbProductLevel4.getValue().getName(),cbProductLevel3.getValue().getName(),cbProductLevel2.getValue().getName(),cbProductLevel1.getValue().getName());
                    if(allProductsByCategory.isPresent()){
                        productForGrid = allProductsByCategory.get();
                        grid.setItems(productForGrid);
                    }
                    else{
                        List<Product>products = new ArrayList<>();
                        productForGrid = products;
                        grid.setItems(productForGrid);
                    }
                }

            }
            else{
                cbProductLevel7.clear();
                cbProductLevel7.setEnabled(false);
                if(selectedProduct == null){
                    //filter Products
                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName(),cbProductLevel5.getValue().getName(),cbProductLevel4.getValue().getName(),cbProductLevel3.getValue().getName(),cbProductLevel2.getValue().getName(),cbProductLevel1.getValue().getName());
                    if(allProductsByCategory.isPresent()){
                        productForGrid = allProductsByCategory.get();
                        grid.setItems(allProductsByCategory.get());
                    }
                    else{
                        List<Product>products = new ArrayList<>();
                        productForGrid = products;
                        grid.setItems(productForGrid);
                    }
                }
            }
        });

        cbProductLevel7.addValueChangeListener(event -> {
            if(selectedProduct == null){
                //filter Products
                Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName(),cbProductLevel6.getValue().getName(),cbProductLevel5.getValue().getName(),cbProductLevel4.getValue().getName(),cbProductLevel3.getValue().getName(),cbProductLevel2.getValue().getName(),cbProductLevel1.getValue().getName());
                if(allProductsByCategory.isPresent()){
                    productForGrid = allProductsByCategory.get();
                    grid.setItems(productForGrid);
                }
                else{
                    List<Product>products = new ArrayList<>();
                    productForGrid = products;
                    grid.setItems(productForGrid);
                }
            }
        });
    }

    private void fillComboBoxLevel1WithItemsToStart() {
        Optional<List<ProductLevel1>>optLevel1List = productLevel1Service.getAllProductLevel1();
        if (!optLevel1List.isEmpty()) {
            cbProductLevel1.setEnabled(true);
            level1List = optLevel1List.get();
            cbProductLevel1.setItems(level1List);
            cbProductLevel1.setItemLabelGenerator(x -> x.getName());
        }
        else{
            cbProductLevel1.setEnabled(false);
            level1List = new ArrayList<>();
        }
    }

    private void setUpBinder() {
        // Configure Form
        productBinder = new Binder<>(Product.class);
        productBinder.forField(tfId)
                .bind(x -> x.getId().toString(), (x,y)-> x.setId(y));
        productBinder.forField(tfProductCode)
                .bind(Product::getProductCode, Product::setProductCode);
        productBinder.forField(tfPositionNumber)
                        .bind(Product::getPositionNumber, Product::setPositionNumber);
        productBinder.forField(tfInternalName)
                .bind(Product::getInternalName, Product::setInternalName);
        productBinder.forField(tfPurchasePrice)
//                .withNullRepresentation("0.0")
//                        .withValidator(
//                                value -> {
//                                    try {
//                                        double d = Double.parseDouble(value);
//                                        return true;
//                                    } catch (NumberFormatException nfe) {
//                                        return false;
//                                    }
//                                }
//                        ,"De ingave moet een decimaal nummer zijn (getal met een punt als komma)")
                .withConverter(
                        new StringToDoubleConverter("De ingave moet een decimaal nummer zijn (getal met een punt als komma)"))
                .bind(Product::getPurchasePrice, Product::setPurchasePrice);;
        productBinder.forField(tfSellMargin)
//                .withNullRepresentation("0.0")
//                .withValidator(
//                        value -> {
//                            try {
//                                double d = Double.parseDouble(value);
//                                return true;
//                            } catch (NumberFormatException nfe) {
//                                return false;
//                            }
//                        }
//                        ,"De ingave moet een decimaal nummer zijn (getal met een punt als komma)")
                .withConverter(
                        new StringToDoubleConverter("De ingave moet een decimaal nummer zijn (getal met een punt als komma)"))
                .bind(Product::getSellMargin, Product::setSellMargin);;

        productBinder.forField(tfSellPrice)
//                .withNullRepresentation("0.0")
//                .withValidator(
//                        value -> {
//                            try {
//                                double d = Double.parseDouble(value);
//                                return true;
//                            } catch (NumberFormatException nfe) {
//                                return false;
//                            }
//                        }
//                        ,"De ingave moet een decimaal nummer zijn (getal met een punt als komma)")
                .withConverter(
                        new StringToDoubleConverter("De ingave moet een decimaal nummer zijn (getal met een punt als komma)"))
                .bind(Product::getSellPrice, Product::setSellPrice);;

        productBinder.forField(tfComment)
                .bind(Product::getComment, Product::setComment);
        productBinder.forField(tfUnit)
                .bind(Product::getUnit, Product::setUnit);
        productBinder.forField(cbProductLevel1)
                        .bind(Product::getProductLevel1, Product::setProductLevel1);
        productBinder.forField(cbProductLevel2)
                .bind(Product::getProductLevel2, Product::setProductLevel2);
        productBinder.forField(cbProductLevel3)
                .bind(Product::getProductLevel3, Product::setProductLevel3);
        productBinder.forField(cbProductLevel4)
                .bind(Product::getProductLevel4, Product::setProductLevel4);
        productBinder.forField(cbProductLevel5)
                .bind(Product::getProductLevel5, Product::setProductLevel5);
        productBinder.forField(cbProductLevel6)
                .bind(Product::getProductLevel6, Product::setProductLevel6);
        productBinder.forField(cbProductLevel7)
                .bind(Product::getProductLevel7, Product::setProductLevel7);
        productBinder.addValueChangeListener(event -> {
            if ((selectedProduct != null) && (event.getValue() != null)) {
                try {
                    productBinder.writeBean(selectedProduct);
                } catch (ValidationException e) {
                    throw new RuntimeException(e);
                }
                grid.getDataProvider().refreshItem(selectedProduct);
            }
            else{
                if(!event.isFromClient()){
                    clearForm();
                    cbProductLevel1.setValue(cbProductLevel1.getEmptyValue());
                    cbProductLevel2.setValue(cbProductLevel2.getEmptyValue());
                    cbProductLevel3.setValue(cbProductLevel3.getEmptyValue());
                    cbProductLevel4.setValue(cbProductLevel4.getEmptyValue());
                    cbProductLevel5.setValue(cbProductLevel5.getEmptyValue());
                    cbProductLevel6.setValue(cbProductLevel6.getEmptyValue());
                    cbProductLevel7.setValue(cbProductLevel7.getEmptyValue());
                }
            }
        });
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
            product.setInternalName("interne omschrijving");
            product.setLinked(false);
            ProductLevel1 productLevel1 = new ProductLevel1();
            productLevel1.setName("N/A");
            product.setProductLevel1(productLevel1);
            products.add(product);
            productService.save(product);
            productForGrid = products;
            grid.setItems(productForGrid);
        }
    }

    private void setUpGrid() {
        // Configure Grid
        //grid.addColumn("id").setAutoWidth(true);
        grid.addComponentColumn(item -> {
            Button closeButton = new Button(new Icon(VaadinIcon.TRASH));
            closeButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            closeButton.addClickListener(event -> {
                Notification.show("Openen verificatie");
                selectedProduct = item;
                deleteCustomerNotification.open();
            });
            return closeButton;
        });

        grid.addComponentColumn(item -> {
            if((item.getSet() != null) && (item.getSet())){
                Span newSpan = new Span("SET");
                newSpan.getStyle().set("color", "red");
                newSpan.getStyle().set("font-weight", "bold");
                return newSpan;
            }
            return new Span("");

        });

        Grid.Column<Product> sortColumnPosNr = grid.addColumn(item -> {
            if((item.getPositionNumber() != null) && (!item.getPositionNumber().isEmpty())){
                try{
                    return Integer.valueOf(item.getPositionNumber().split("[^0-9]")[0]);
                }
                catch (Exception e){
                    return 4999;
                }
            }
            else{
                return 5000;
            }
        });
        sortColumnPosNr.setVisible(false);
        grid.addColumn("productCode").setHeader("Code").setResizable(true).setWidth("20%");
        grid.addColumn("positionNumber").setComparator((o1, o2) -> {
            if((o1.getPositionNumber() != null) && (o2.getPositionNumber() != null)) {
                return compareOnderdeel(o1.getPositionNumber(), o2.getPositionNumber());
            }
            else{
                return -1;
            }

        }).setHeader("Pos").setResizable(true).setFlexGrow(1);
        Grid.Column<Product> productColumn = grid.addColumn("internalName").setSortable(true) .setComparator((o1, o2) -> compareOnderdeel(o1.getInternalName(), o2.getInternalName())).setWidth("25%").setHeader("Naam").setResizable(true);
        grid.sort(List.of(new GridSortOrder<>(productColumn, SortDirection.ASCENDING)));
        grid.addColumn(item -> {
            if(item.getPurchasePrice() != null){
                return df.format(item.getPurchasePrice());
            }
            else{
                return "N/A";
            }

        }).setHeader("Aankoop").setTextAlign(ColumnTextAlign.END).setResizable(true).setFlexGrow(1);
        grid.addColumn(item -> {
            if(item.getSellPrice() != null){
                return df.format(item.getSellPrice());
            }
            else{
                return "N/A";
            }
        }).setHeader("Verkoop").setTextAlign(ColumnTextAlign.END).setResizable(true).setFlexGrow(1);;
        grid.addColumn(item -> {
            if(item.getSellMargin() != null){
                return df.format(item.getSellMargin());
            }
            else{
                return "N/A";
            }
        }).setHeader("Marge").setTextAlign(ColumnTextAlign.END).setResizable(true).setFlexGrow(1);
        grid.addColumn("comment").setHeader("Commentaar").setResizable(true).setFlexGrow(2);
        LitRenderer<Product> importantRenderer = LitRenderer.<Product>of(
                        "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", product -> product.getLinked() ? "check" : "minus").withProperty("color",
                        product -> product.getLinked()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");
        grid.addColumn(item -> {
            if(item.getUnit() != null){
                return item.getUnit();
            }
            else{
                return "";
            }
        }).setHeader("Eenh.").setResizable(true).setFlexGrow(1);

        grid.setPartNameGenerator(product -> {
            if((product.getSet() != null ) && (product.getSet() == true)){
                return "remark";
            } else if ((product.getSetElement() != null) && (product.getSetElement() == true)) {
                return "attachement";
            } else{
                return "null";
            }
        });

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                //first populate null to clear Level filters then select item
                populateForm(null);

                grid.select(event.getValue());
                selectedProduct = event.getValue();
                levelSpan.setText(getStringLevel(selectedProduct));
                populateForm(selectedProduct);
                if((event.getValue().getPurchasePriseList() != null) && (event.getValue().getPurchasePriseList().size() > 0)){
                    purchasePriceGrid.setItems(event.getValue().getPurchasePriseList());
                }
                else{
                    List<PurchasePrice> newPurchasePriceList = getNewPurchasePriceList();
                    purchasePriceGrid.setItems(newPurchasePriceList);
                    event.getValue().setPurchasePriseList(newPurchasePriceList);
                }
            }
             else {
                levelSpan.setText("Geen artikel geslecteerd");
                clearForm();
            }
        });
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    }

    private int compareOnderdeel(String s1, String s2) {
        List<Object> parts1 = splitAlphaNumeric(s1);
        List<Object> parts2 = splitAlphaNumeric(s2);

        int len = Math.min(parts1.size(), parts2.size());

        for (int i = 0; i < len; i++) {
            Object p1 = parts1.get(i);
            Object p2 = parts2.get(i);

            int cmp;
            if (p1 instanceof String && p2 instanceof String) {
                cmp = ((String) p1).compareToIgnoreCase((String) p2);
            } else if (p1 instanceof Number && p2 instanceof Number) {
                cmp = Double.compare(((Number) p1).doubleValue(), ((Number) p2).doubleValue());
            } else {
                // String vs Number → String komt altijd eerst
                cmp = (p1 instanceof String) ? -1 : 1;
            }

            if (cmp != 0) return cmp;
        }

        // Als alles gelijk is, kortere string komt eerst
        return Integer.compare(parts1.size(), parts2.size());
    }

    private List<Object> splitAlphaNumeric(String input) {
        List<Object> parts = new ArrayList<>();

        Matcher matcher = Pattern.compile("(\\d+[\\.,]?\\d*|\\D+)").matcher(input);
        while (matcher.find()) {
            String part = matcher.group(1).trim();
            if (part.matches("\\d+[\\.,]?\\d*")) {
                part = part.replace(",", "."); // vervang komma door punt
                try {
                    parts.add(Double.parseDouble(part));
                } catch (NumberFormatException e) {
                    parts.add(part); // fallback: behandel als string
                }
            } else {
                parts.add(part);
            }
        }

        return parts;
    }



    private String getStringLevel(Product selectedProduct) {
        String levelString = selectedProduct.getProductLevel1().getName();
        if(selectedProduct.getProductLevel2() != null){
            levelString = levelString + " " + selectedProduct.getProductLevel2().getName();
        }
        if(selectedProduct.getProductLevel3() != null){
            levelString = levelString + " " + selectedProduct.getProductLevel3().getName();
        }
        if(selectedProduct.getProductLevel4() != null){
            levelString = levelString + " " + selectedProduct.getProductLevel4().getName();
        }
        if(selectedProduct.getProductLevel5() != null){
            levelString = levelString + " " + selectedProduct.getProductLevel5().getName();
        }
        if(selectedProduct.getProductLevel6() != null){
            levelString = levelString + " " + selectedProduct.getProductLevel6().getName();
        }
        if(selectedProduct.getProductLevel7() != null){
            levelString = levelString + " " + selectedProduct.getProductLevel7().getName();
        }
        return levelString;
    }

    private List<PurchasePrice> getNewPurchasePriceList() {
        List<PurchasePrice> purchasePriceList = new ArrayList<>();
        PurchasePrice purchasePrice = new PurchasePrice();
        purchasePrice.setPurchaseDate(LocalDate.now());
        purchasePriceList.add(purchasePrice);
        return purchasePriceList;
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
        tfFilter = new TextField("Filter");
        tfId = new TextField("Id");
        tfId.setEnabled(false);
        tfId.setVisible(true);
        tfProductCode = new TextField("Artikelcode");
        tfPositionNumber = new TextField("Positienummer");
        tfInternalName = new TextField("Interne omschrijving");
        tfPurchasePrice = new TextField("Aankoopprijs");
        tfSellMargin = new TextField("Verkoopmarge");
        tfSellPrice = new TextField("Verkoopprijs");
        tfComment = new TextField("Commentaar");
        tfUnit = new TextField("Eenheid");
        levelSpan = new Span("Productniveau");
        levelSpan.getElement().getStyle().set("font-size", "18px");
        levelSpan.getElement().getStyle().set("background-color", "yellow");
        cbProductLevel1 = new ComboBox("Artikelniveau1");
        cbProductLevel2 = new ComboBox("Artikelniveau2");
        cbProductLevel3 = new ComboBox("Artikelniveau3");
        cbProductLevel4 = new ComboBox("Artikelniveau4");
        cbProductLevel5 = new ComboBox("Artikelniveau5");
        cbProductLevel6 = new ComboBox("Artikelniveau6");
        cbProductLevel7 = new ComboBox("Artikelniveau7");
        formLayout.add(
                tfFilter,
                tfId,
                levelSpan,
                setUpHorizontalLayoutFor(cbProductLevel1,bAddProductLevel1,E_Product_Level.PRODUCTLEVEL1),
                setUpHorizontalLayoutFor(cbProductLevel2,bAddProductLevel2,E_Product_Level.PRODUCTLEVEL2),
                        setUpHorizontalLayoutFor(cbProductLevel3,bAddProductLevel3,E_Product_Level.PRODUCTLEVEL3),
                                setUpHorizontalLayoutFor(cbProductLevel4,bAddProductLevel4,E_Product_Level.PRODUCTLEVEL4),
                                        setUpHorizontalLayoutFor(cbProductLevel5,bAddProductLevel5,E_Product_Level.PRODUCTLEVEL5),
                //setUpHorizontalLayoutFor(cbProductLevel6,bAddProductLevel6,E_Product_Level.PRODUCTLEVEL6),
                    //setUpHorizontalLayoutFor(cbProductLevel7,bAddProductLevel7,E_Product_Level.PRODUCTLEVEL7),
                tfProductCode,
                tfInternalName,
                tfPurchasePrice,
                tfSellMargin,
                tfSellPrice,
                tfPositionNumber,
                tfComment,
                tfUnit);

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
                case E_Product_Level.PRODUCTLEVEL6:
                    Notification.show("Product level 6");
                    selectedProductLevel = E_Product_Level.PRODUCTLEVEL6;
                    break;
                case E_Product_Level.PRODUCTLEVEL7:
                    Notification.show("Product level 7");
                    selectedProductLevel = E_Product_Level.PRODUCTLEVEL7;
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
        buttonNewProduct.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        buttonSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBulk.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_WARNING);
        buttonConfigureSet.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(buttonSave, buttonNewProduct, addBulk, buttonConfigureSet);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        SplitLayout gridSplitLayoout = new SplitLayout();
        gridSplitLayoout.setHeight("100%");
        gridSplitLayoout.setOrientation(SplitLayout.Orientation.VERTICAL);
        gridSplitLayoout.setSplitterPosition(80);
        gridSplitLayoout.addToPrimary(grid);
        gridSplitLayoout.addToSecondary(purchasePriceGrid);
        wrapper.add(gridSplitLayoout);
        splitLayout.addToPrimary(wrapper);
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Product value) {
        selectedProduct = value;
        try{
            productBinder.readBean(selectedProduct);
        }
        catch (Exception e){
            //Notification.show(e.getMessage());
        }
    }

    private void tryToCalculateSellPrice(Product product) {
        Optional<Double>optDoublePurchasePrice = Optional.of(product.getPurchasePrice());
        Optional<Double>optDoubleSellMargin = Optional.of(product.getSellMargin());

        if(optDoublePurchasePrice.isPresent()) {
            if(optDoubleSellMargin.isPresent()) {
                //calc sellPrice
                product.setSellPrice(optDoublePurchasePrice.get()*optDoubleSellMargin.get());
            }
        }
        else{
            Notification.show("Gelieve te starten met de aankoopprijs en marge");
        }
    }

    private Notification createReportChangePurchasePrice() {
        changePurchasePriceNotification = new Notification();
        changePurchasePriceNotification.addThemeVariants(NotificationVariant.LUMO_WARNING);

        Icon icon = VaadinIcon.WARNING.create();
        Button retryBtn = new Button("Annuleer",
                clickEvent -> changePurchasePriceNotification.close());
        retryBtn.getStyle().setMargin("0 0 0 var(--lumo-space-l)");

        var layout = new HorizontalLayout(icon,
                new Text("Ben je zeker dat je de aankoopprijs van alle artikelen met deze code wilt wijzigen?"), retryBtn,
                createRemoveProductBtn(changePurchasePriceNotification));
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        changePurchasePriceNotification.add(layout);

        return changePurchasePriceNotification;
    }

    public Button createRemoveProductBtn(Notification notification) {
        Button closeBtn = new Button(VaadinIcon.PENCIL.create(),
                clickEvent -> {
                    if((selectedProduct.getProductCode() != null) && (selectedProduct.getProductCode().length() > 1)){
                        Optional<List<Product>> byProductCodeEqualCaseInsensitive = productService.findByProductCodeEqualCaseInsensitive(selectedProduct.getProductCode());
                        if((byProductCodeEqualCaseInsensitive.isPresent() && (byProductCodeEqualCaseInsensitive.get().size() > 0))) {
                            for(Product product : byProductCodeEqualCaseInsensitive.get()) {
                                product.setPurchasePrice(selectedProduct.getPurchasePrice());
                                tryToCalculateSellPrice(product);
                                productService.save(product);
                                try {
                                    productBinder.writeBean(selectedProduct);
                                } catch (ValidationException e) {
                                    throw new RuntimeException(e);
                                }
                                productService.save(selectedProduct);
                            }
                        }
                        Notification.show("Artikel : " + byProductCodeEqualCaseInsensitive.get().size() + " dezelfde artikelen gevonden en aangepast in de Database!");
                    }
                });
        closeBtn.addThemeVariants(LUMO_TERTIARY_INLINE);

        return closeBtn;
    }
}
