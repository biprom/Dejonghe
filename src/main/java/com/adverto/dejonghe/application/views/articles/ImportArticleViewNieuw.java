package com.adverto.dejonghe.application.views.articles;

import com.adverto.dejonghe.application.dbservices.*;
import com.adverto.dejonghe.application.entities.enums.employee.UserFunction;
import com.adverto.dejonghe.application.entities.product.enums.E_Product_Level;
import com.adverto.dejonghe.application.entities.product.product.*;
import com.adverto.dejonghe.application.repos.ProductRepo;
import com.adverto.dejonghe.application.services.product.ProductServices;
import com.adverto.dejonghe.application.services.product.SetService;
import com.adverto.dejonghe.application.views.subViews.*;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.*;
import org.apache.poi.ss.util.CellReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY_INLINE;

@PageTitle("ArtikelenNieuw")
@Route("")
@Menu(order = 0, icon = LineAwesomeIconUrl.COG_SOLID)
public class ImportArticleViewNieuw extends VerticalLayout implements BeforeEnterObserver {

    //@Value( "${linkSpreadsheetBulk}" )
    private FileSystemResource linkToBulkSpreadsheet = new FileSystemResource("/Users/bramvandenberghe/Desktop/dejonghe.xlsx");
    //private FileSystemResource linkToBulkSpreadsheet = new FileSystemResource("D:\\Algemeen\\Documentatie\\Dejonghe-techniek\\Database\\dejonghe.xlsx\\dejonghe.xlsx");

    Notification deleteProductNotification;

    private final ProductService productService;
    private ProductLevel1Service productLevel1Service;
    private ProductLevel2Service productLevel2Service;
    private ProductLevel3Service productLevel3Service;
    private ProductLevel4Service productLevel4Service;
    private ProductLevel5Service productLevel5Service;
    private ProductLevel6Service productLevel6Service;
    private ProductLevel7Service productLevel7Service;
    private SetService setService;

    private ProductRepo productRepo;
    private SupplierService supplierService;

    private SetView setView;
    private MoveView moveView;
    private CopyView copyView;
    private SetViewSimple setViewSimple;
    private NewArticleView newArticleView;
    private ChangeArticleView changeArticleView;
    private ShowImageSubVieuw showImageSubVieuw;
    private ShowPdfSubVieuw showPdfSubVieuw;
    private ShowLinkSubVieuw showLinkSubVieuw;

    private List<ProductLevel1>level1List;
    private List<ProductLevel2>level2List;
    private List<ProductLevel3>level3List;
    private List<ProductLevel4>level4List;
    private List<ProductLevel5>level5List;
    private List<ProductLevel6>level6List;
    private List<ProductLevel7>level7List;

    private Dialog bulkDialog;
    private Dialog configureSetDialog;
    private Dialog newArticleDialog;
    private Dialog changeArticleDialog;
    private Dialog showImageDialog;
    private Dialog pdfDialog;
    private Dialog linkDialog;
    private Dialog setViewSimpleDialog;
    private Dialog moveProductDialog;
    private Dialog copyProductDialog;

    private final Grid<Product> grid = new Grid<>(Product.class, false);
    private List<Product> productsForGrid;

    private TextField tfGeneralFilter;
    private TextField tfFolderFilter;
    private TextField tfProductCode;
    private TextField tfPositionNumber;
    private TextField tfInternalName;
    private TextField tfComment;
    private TextField tfMoQ;
    private TextField tfUnit;

    private TextField tfPurchasePrice;
    private TextField tfSellMargin;
    private TextField tfSellIndustryMargin;
    private TextField tfSellPrice;
    private TextField tfSellIndustryPrice;

    private ComboBox<ProductLevel1>cbProductLevel1;
    private ComboBox<ProductLevel2>cbProductLevel2;
    private ComboBox<ProductLevel3>cbProductLevel3;
    private ComboBox<ProductLevel4>cbProductLevel4;
    private ComboBox<ProductLevel5>cbProductLevel5;
    private ComboBox<ProductLevel6>cbProductLevel6;
    private ComboBox<ProductLevel7>cbProductLevel7;

    private Button bAddProductLevel1;
    private Button bAddProductLevel2;
    private Button bAddProductLevel3;
    private Button bAddProductLevel4;
    private Button bAddProductLevel5;
    private Button bAddProductLevel6;
    private Button bAddProductLevel7;

    Grid.Column codeColumn;
    Grid.Column posColumn;
    Grid.Column internalNameColumn;
    Grid.Column purchaceColumn;
    Grid.Column sellComumn;
    Grid.Column sellIndustryComumn;
    Grid.Column marginColumn;
    Grid.Column marginIndustryColumn;
    Grid.Column commentColumn;
    Grid.Column moqColumn;
    Grid.Column unitColumn;

    private final Button buttonNewProduct = new Button("Nieuw");
    private final Button buttonSave = new Button("Bewaar");

    private List<String> importBulkLevelList = new ArrayList<>();

    private E_Product_Level selectedProductLevel;
    private Grid<ProductDiscriptionAndId>bulkGrid = new Grid<>(ProductDiscriptionAndId.class);

    private Grid<PurchasePrice>purchasePriceGrid = new Grid<>();

    NumberFormat df = NumberFormat.getNumberInstance(new Locale("nl", "BE"));

    private Spreadsheet spreadsheet;

    private Binder<Product> productBinder;
    Editor<Product> editor;

    private Product selectedProduct;
    Button goToFolderButton;

    AtomicBoolean updating = new AtomicBoolean(false);
    List<Product>productListToShowInGrid = new ArrayList<>();
    List<Product>temporaryProductList = new ArrayList<>();
    Optional<List<Product>> byProductCodeContaining;
    List<Product>selectedSetList = new ArrayList<>();
    MenuBar actionBar;

    public ImportArticleViewNieuw(ProductService productService,
                                  ProductRepo productRepo,
                                  ProductLevel1Service productLevel1Service,
                                  ProductLevel2Service productLevel2Service,
                                  ProductLevel3Service productLevel3Service,
                                  ProductLevel4Service productLevel4Service,
                                  ProductLevel5Service productLevel5Service,
                                  ProductLevel6Service productLevel6Service,
                                  ProductLevel7Service productLevel7Service,
                                  SupplierService supplierService,
                                  SetView setView,
                                  MoveView moveView,
                                  CopyView copyView,
                                  NewArticleView newArticleView,
                                  ChangeArticleView changeArticleView,
                                  ShowImageSubVieuw showImageSubVieuw,
                                  ShowPdfSubVieuw showPdfSubVieuw,
                                  ShowLinkSubVieuw showLinkSubVieuw,
                                  SetViewSimple setViewSimple,
                                  SetService setService) {
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
        this.moveView = moveView;
        this.copyView = copyView;
        this.newArticleView = newArticleView;
        this.changeArticleView = changeArticleView;
        this.showImageSubVieuw = showImageSubVieuw;
        this.showPdfSubVieuw = showPdfSubVieuw;
        this.showLinkSubVieuw = showLinkSubVieuw;
        this.setViewSimple = setViewSimple;
        this.setService = setService;

        addClassNames("master-detail-view");

        // Create UI
        setUpNumberFormat();
        setUpLinkDialog();
        setUpImageDialog();
        setUpPdfDialog();
        setUpSetSimpleDialog();
        createReportError();
        setUpSpreadSheet();
        this.setSizeFull();
        this.setWidth("100");
        add(createEditorLayout());
        add(createGridLayout());
        setUpGrid();
        addDataToGrid();
        setUpBinder();
        setUpProductLevelComboBoxes();
        setUpBulkDialog();
        setUpConfigureSetDialog();
        setUpCopyDialog();
        setUpMoveDialog();
        setUpNewArticleDialog();
        setUpChangeDialog();
        setUpPurchasePricegrid();
        setUpPriceValueChangeListeners();
        setUpFilter();
//        createReportChangePurchasePrice();

        buttonNewProduct.addClickListener(e -> {
            Product newProduct = new Product();
            newProduct.setId(LocalDateTime.now().toString());
            newProduct.setProductCode("");
            newProduct.setPurchasePrice(0.0);
            newProduct.setSellPrice(0.0);
            newProduct.setSellMargin(0.0);
            newProduct.setSellMarginIndustry(0.0);
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
            productsForGrid.add(newProduct);
            addItemsToGrid(productsForGrid);
            grid.scrollToItem(newProduct);
            grid.select(newProduct);
            editor.editItem(selectedProduct);
        });

        buttonSave.addClickListener(e -> {
            try {
                if (editor.getItem() != null) {
                    editor.save();
                    editor.closeEditor();

                    //add Purchase Items to this Product if they exist
                    if((selectedProduct.getPurchasePriseList() == null)||(selectedProduct.getPurchasePriseList().isEmpty())) {
                        byProductCodeContaining = productService.findByProductCodeContaining(tfProductCode.getValue());
                        if((!byProductCodeContaining.isEmpty()) && (byProductCodeContaining.get().size() > 0)) {
                            selectedProduct.setPurchasePriseList(byProductCodeContaining.get().getFirst().getPurchasePriseList());
                        }
                    }

                    //check if article is a part
                    //if it is nog part just save the article
                    //if it is in a part also edit the article in the set (code and folders have to be the same!!!)
                    if((selectedProduct.getSetElement() == null)||(selectedProduct.getSetElement() == false)){
                        productService.save(selectedProduct);
                    }
                    else{
                        Optional<List<Product>> setsWithThisElement = productService.findSetsWithThisElement(selectedProduct);
                        if(!setsWithThisElement.isEmpty()) {
                            setsWithThisElement.get().forEach(set -> {
                                //change margins and price
                                set.getSetList().stream().filter(item -> item.getProductCode().matches(selectedProduct.getProductCode())).forEach(item -> {
                                    item.setPurchasePrice(set.getPurchasePrice());
                                    item.setSellMargin(selectedProduct.getSellMargin());
                                    item.setSellPrice(selectedProduct.getSellPrice());
                                    item.setSellMarginIndustry(selectedProduct.getSellMarginIndustry());
                                    item.setSellPriceIndustry(selectedProduct.getSellPriceIndustry());
                                });
                                set.setSellPrice(setService.tryToCalculateSellAgroPrice(set));
                                set.setSellPriceIndustry(setService.tryToCalculateSellIndustryPrice(set));
                                productService.save(set);
                                productService.save(selectedProduct);
                            });
                        }
                        productService.save(selectedProduct);
                    }



                    //show pop up to check if there are multiple articles with the same code (not in sets!!!).
                    if((selectedProduct.getProductCode() != null) && (selectedProduct.getProductCode().length() > 0)) {
                        byProductCodeContaining = productService.findByProductCodeEqualCaseInsensitive(selectedProduct.getProductCode().toLowerCase());
                        if((byProductCodeContaining != null) && (!byProductCodeContaining.isEmpty())&& (byProductCodeContaining.get().size() > 1)) {
                            newArticleView.setSameProductList(byProductCodeContaining);
                            newArticleView.setCorrectedPrice(tfPurchasePrice.getValue());
                            newArticleDialog.open();
                        }
                    }
                }
                else{
                    Notification.show("Gelieve eerst een artikel aan te maken of te selecteren aub");
                }
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }

    private void setUpNumberFormat() {
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);
        df.setGroupingUsed(true);
    }

    private void setUpPdfDialog() {
        pdfDialog = new Dialog();
        pdfDialog.setCloseOnEsc(true);
        pdfDialog.setWidth("60%");
        pdfDialog.setHeight("60%");
        pdfDialog.add(showPdfSubVieuw);
        Button cancelButton = new Button("Sluiten", e -> {
            pdfDialog.close();
            refreshItemsInGrid();
        });
        pdfDialog.getFooter().add(cancelButton);
    }

    private void setUpSetSimpleDialog() {
        setViewSimpleDialog = new Dialog();
        setViewSimpleDialog.setCloseOnEsc(true);
        setViewSimpleDialog.setWidth("60%");
        setViewSimpleDialog.setHeight("60%");
        setViewSimpleDialog.setDraggable(true);
        setViewSimple.setParentDialog(setViewSimpleDialog);
        setViewSimple.setImportArticleViewNieuw(this);
        setViewSimpleDialog.add(setViewSimple);
        Button cancelButton = new Button("Sluiten", e -> {
            setViewSimpleDialog.close();
            refreshItemsInGrid();
        });
        setViewSimpleDialog.getFooter().add(cancelButton);
    }

    private void setUpLinkDialog() {
        linkDialog = new Dialog();
        linkDialog.setCloseOnEsc(true);
        linkDialog.setWidth("60%");
        linkDialog.setHeight("60%");
        linkDialog.add(showLinkSubVieuw);
        Button cancelButton = new Button("Sluiten", e -> {
            linkDialog.close();
            refreshItemsInGrid();
        });
        linkDialog.getFooter().add(cancelButton);
    }

    private void setUpImageDialog() {
        showImageDialog = new Dialog();
        showImageDialog.setCloseOnEsc(true);
        showImageDialog.setWidth("60%");
        showImageDialog.setHeight("60%");
        showImageDialog.add(showImageSubVieuw);
        Button cancelButton = new Button("Sluiten", e -> {
            showImageDialog.close();
            refreshItemsInGrid();
        });
        showImageDialog.getFooter().add(cancelButton);
    }

    private void setUpConfigureSetDialog() {
        configureSetDialog = new Dialog();
        configureSetDialog.setWidth("90%");
        configureSetDialog.setHeight("90%");
        configureSetDialog.add(setView);
        Button cancelButton = new Button("Sluiten", e -> {
            configureSetDialog.close();
            refreshItemsInGrid();
        });
        configureSetDialog.getFooter().add(cancelButton);
        configureSetDialog.addDialogCloseActionListener(event -> {
            configureSetDialog.close();
            refreshItemsInGrid();
        });
    }

    private void setUpCopyDialog() {
        copyProductDialog = new Dialog();
        copyProductDialog.setWidth("90%");
        copyProductDialog.setHeight("90%");
        copyProductDialog.add(copyView);
        Button cancelButton = new Button("Sluiten", e -> {
            copyProductDialog.close();
            refreshItemsInGrid();
        });
        copyProductDialog.getFooter().add(cancelButton);
        copyProductDialog.addDialogCloseActionListener(event -> {
            copyProductDialog.close();
            refreshItemsInGrid();
        });
    }

    private void setUpMoveDialog() {
        moveProductDialog = new Dialog();
        moveProductDialog.setWidth("90%");
        moveProductDialog.setHeight("90%");
        moveProductDialog.add(moveView);
        Button cancelButton = new Button("Sluiten", e -> {
            moveProductDialog.close();
            refreshItemsInGrid();
        });
        moveProductDialog.getFooter().add(cancelButton);
        moveProductDialog.addDialogCloseActionListener(event -> {
            moveProductDialog.close();
            refreshItemsInGrid();
        });
    }

    private void setUpChangeDialog(){
        changeArticleDialog = new Dialog();
        changeArticleDialog.setWidth("60%");
        changeArticleDialog.setHeight("60%");
        changeArticleDialog.setCloseOnEsc(true);
        changeArticleDialog.add(changeArticleView);
        Button cancelButton = new Button("Sluiten", e -> {
            changeArticleDialog.close();
            refreshItemsInGrid();
        });
        changeArticleDialog.getFooter().add(cancelButton);
    }

    private void setUpNewArticleDialog() {
        newArticleDialog = new Dialog();
        newArticleDialog.setWidth("90%");
        newArticleDialog.setHeight("50%");
        newArticleDialog.setCloseOnEsc(true);
        newArticleDialog.add(newArticleView);
        newArticleView.setConfirmDialog(newArticleDialog);
        newArticleDialog.addOpenedChangeListener(event -> {
            if(!event.isOpened()){
                Notification.show("PopUp is closed");
                if(!byProductCodeContaining.isEmpty()){
                    for(Product product : byProductCodeContaining.get() ){
                        Optional<Product> itemInGrid = productsForGrid.stream().filter(item -> item.getId().equals(product.getId())).findFirst();
                        if(itemInGrid.isPresent()) {
                            int id = productsForGrid.indexOf(itemInGrid.get());
                            productsForGrid.remove(itemInGrid.get());
                            Optional<Product> updatedProductFromDB = productService.get(product.getId());
                            if(updatedProductFromDB.isPresent()) {
                                productsForGrid.add(id,updatedProductFromDB.get());
                            }
                        }
                    }
                }
                grid.getDataProvider().refreshAll();
                newArticleDialog.close();
            }
        });
        Button cancelButton = new Button("Sluiten", e -> {
            newArticleDialog.close();
            refreshItemsInGrid();
        });
        newArticleDialog.getFooter().add(cancelButton);
    }


    private Notification createReportError() {
        deleteProductNotification = new Notification();
        deleteProductNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);

        Icon icon = VaadinIcon.WARNING.create();
        Button retryBtn = new Button("Annuleer",
                clickEvent -> deleteProductNotification.close());
        retryBtn.getStyle().setMargin("0 0 0 var(--lumo-space-l)");

        var layout = new HorizontalLayout(icon,
                new Text("Ben je zeker dat je dit artikel wil wissen?"), retryBtn,
                createCloseBtn(deleteProductNotification));
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        deleteProductNotification.add(layout);

        return deleteProductNotification;
    }

    public Button createCloseBtn(Notification notification) {
        Button closeBtn = new Button(VaadinIcon.TRASH.create(),
                clickEvent -> {
                    if(productsForGrid != null){
                        //when filter is selected
                        productsForGrid.remove(selectedProduct);
                        productService.delete(selectedProduct);
                        Notification.show("Artikel is verwijderd");
                    }
                    else{
                        //when filter is not touched
                        productsForGrid.remove(selectedProduct);
                        productService.delete(selectedProduct);
                        addDataToGrid();
                        Notification.show("Artikel is verwijderd");
                    }
                    //check if selectedProduct is in sets and remove product + recalc set
                    Optional<List<Product>>setsWhereProductIsIn = setService.removeItemFromSetsAndRecalculateSet(selectedProduct);
                    //TODO fucking refresh sellprices and purchaseprice when adding product

//                    //refresh set if it is in this folder
//                    if(setsWhereProductIsIn.isPresent()){
//                        if (setsWhereProductIsIn.get().size() >= 1) {
//                            for (Product set : setsWhereProductIsIn.get()) {
//                                for(Product product : productsForGrid){
//                                    System.out.println("product : " + product.getId() + " -> " + set.getId());
//                                    if(product.getId().equals(set.getId())){
//                                        product.setPurchasePrice(setService.tryToCalculatePurchasePrice(product));
//                                        product.setSellPrice(setService.tryToCalculateSellAgroPrice(product));
//                                        product.setSellPriceIndustry(setService.tryToCalculateSellIndustryPrice(product));
//                                    }
//                                }
//                            }
//                        }
//                    }
                    //addItemsToGrid(productsForGrid);
                    refreshItemsInGrid();
                    notification.close();

                });
        closeBtn.addThemeVariants(LUMO_TERTIARY_INLINE);

        return closeBtn;
    }

    private void setUpFilter() {
        tfGeneralFilter.setWidth("100%");
        tfGeneralFilter.addValueChangeListener(e -> {
            if (updating.get()) return;
            updating.set(true);
            cbProductLevel1.clear();
            cbProductLevel2.clear();
            cbProductLevel3.clear();
            cbProductLevel4.clear();
            cbProductLevel5.clear();
            cbProductLevel6.clear();
            cbProductLevel7.clear();
            cbProductLevel1.setPlaceholder("");
            cbProductLevel2.setPlaceholder("");
            cbProductLevel3.setPlaceholder("");
            cbProductLevel4.setPlaceholder("");
            cbProductLevel5.setPlaceholder("");
            cbProductLevel6.setPlaceholder("");
            cbProductLevel7.setPlaceholder("");
            tfFolderFilter.setValue("");
            Optional<List<Product>> optCustomer = productService.getProductByInternalNameOrCodeOrComment(tfGeneralFilter.getValue(), tfGeneralFilter.getValue(), tfGeneralFilter.getValue());
            if(optCustomer.isPresent()) {
                productsForGrid = optCustomer.get();
                addItemsToGrid(productsForGrid);
            }
            else{
                Notification.show("Geen klanten gevonden");
            }
            updating.set(false);
        });

        tfFolderFilter.setWidth("100%");
        tfFolderFilter.addValueChangeListener(e -> {
            if (updating.get()) return;
            updating.set(true);
            tfGeneralFilter.setValue("");
            addItemsToGrid(productsForGrid.stream().filter(item -> item.getInternalName().toLowerCase().contains(e.getValue().toLowerCase())).collect(Collectors.toList()));
            updating.set(false);
        });
    }

    private void setUpPriceValueChangeListeners() {
        tfPurchasePrice.addValueChangeListener(listener -> {
            if(listener.isFromClient()){
                //check if there are other selected products with this code and change them
                tryToCalculateSellPriceAgro(editor.getItem());
                tryToCalculateSellPriceIndustry(editor.getItem());
            }
        });
        tfSellMargin.addValueChangeListener(listener -> {
            if(listener.isFromClient()){
                tryToCalculateSellPriceAgro(editor.getItem());
            }
        });
        tfSellIndustryMargin.addValueChangeListener(listener -> {
            if(listener.isFromClient()){
                tryToCalculateSellPriceIndustry(editor.getItem());
            }
        });
    }

    private void setUpPurchasePricegrid() {
        purchasePriceGrid.setWidth("100%");
        purchasePriceGrid.removeAllColumns();
        purchasePriceGrid.addComponentColumn(item -> {
            DatePicker datePicker = new DatePicker();
            UI.getCurrent().getPage().executeJs(
                    "const dp = $0; dp.i18n = Object.assign(dp.i18n, {firstDayOfWeek: 1});",
                    datePicker.getElement()
            );
            datePicker.setLocale(new Locale("nl", "BE"));
            if(item.getPurchaseDate() != null){
                datePicker.setValue(item.getPurchaseDate());
            }
            else{
                datePicker.setValue(LocalDate.now());
            }
            datePicker.addValueChangeListener(listener -> {
                item.setPurchaseDate(datePicker.getValue());
                productService.save(selectedProduct);
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
        }).setHeader("Aankoopdatum").setResizable(true).setAutoWidth(true).setFlexGrow(1);


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
                    productService.save(selectedProduct);
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
        }).setHeader("Aantal").setResizable(true).setAutoWidth(true).setFlexGrow(1);

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
                    productService.save(selectedProduct);
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
        }).setHeader("Aankoopprijs").setResizable(true).setAutoWidth(true).setFlexGrow(1);

        purchasePriceGrid.addComponentColumn(item -> {
            TextField textField = new TextField();
            textField.setWidth("100%");
            if(item.getPrice() != null){
                if(item.getComment() != null){
                    textField.setValue(item.getComment().toString());
                }
                else{
                    textField.setValue("");
                }
            }
            textField.addValueChangeListener(listener -> {
                item.setComment(listener.getValue());
                productService.save(selectedProduct);
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
            return textField;
            }).setFlexGrow(4).setHeader("Commentaar").setResizable(true);

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
                productService.save(selectedProduct);
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
        }).setHeader("Leverancier").setAutoWidth(true).setFlexGrow(1).setResizable(true);

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
        }).setAutoWidth(true).setFlexGrow(1);

        purchasePriceGrid.addComponentColumn(item -> {
            Button addButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
            addButton.addThemeVariants(ButtonVariant.LUMO_WARNING);
            addButton.addClickListener(e -> {
                selectedProduct.getPurchasePriseList().remove(item);
                purchasePriceGrid.getDataProvider().refreshAll();
            });
            return addButton;
        }).setAutoWidth(true).setFlexGrow(1).setFrozenToEnd(true);

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
                addItemsToGrid(productRepo.findAll());
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


        cbProductLevel1.addClassName("highlight-border");
        cbProductLevel1.addValueChangeListener(event -> {

            if (updating.get()) return;
            updating.set(true);
            try{
                tfGeneralFilter.setValue("");
                tfFolderFilter.setValue("");
                Optional<List<ProductLevel2>>productLevel2List = productLevel2Service.getProductLevel2sFromPreviousLevels(event.getValue());
                if (!productLevel2List.isEmpty()) {
                    cbProductLevel2.setEnabled(true);
                    level2List = productLevel2List.get();
                    cbProductLevel2.setPlaceholder("");
                    cbProductLevel2.setItems(level2List);
                    cbProductLevel2.setItemLabelGenerator(x -> x.getName());
                    cbProductLevel3.setPlaceholder("");
                    cbProductLevel3.clear();
                    cbProductLevel3.setEnabled(false);
                    cbProductLevel4.setPlaceholder("");
                    cbProductLevel4.clear();
                    cbProductLevel4.setEnabled(false);
                    cbProductLevel5.setPlaceholder("");
                    cbProductLevel5.clear();
                    cbProductLevel5.setEnabled(false);
                    cbProductLevel6.setPlaceholder("");
                    cbProductLevel6.clear();
                    cbProductLevel6.setEnabled(false);
                    cbProductLevel7.setPlaceholder("");
                    cbProductLevel7.clear();
                    cbProductLevel7.setEnabled(false);


                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName());
                    if(allProductsByCategory.isPresent()){
                        productsForGrid = allProductsByCategory.get();
                        addItemsToGrid(productsForGrid);
                    }
                    else{
                        List<Product>products = new ArrayList<>();
                        productsForGrid = products;
                        addItemsToGrid(productsForGrid);
                    }


                }
                else{
                    cbProductLevel2.setPlaceholder("");
                    cbProductLevel2.clear();
                    cbProductLevel2.setEnabled(false);
                    cbProductLevel3.setPlaceholder("");
                    cbProductLevel3.clear();
                    cbProductLevel3.setEnabled(false);
                    cbProductLevel4.setPlaceholder("");
                    cbProductLevel4.clear();
                    cbProductLevel4.setEnabled(false);
                    cbProductLevel5.setPlaceholder("");
                    cbProductLevel5.clear();
                    cbProductLevel5.setEnabled(false);
                    cbProductLevel6.setPlaceholder("");
                    cbProductLevel6.clear();
                    cbProductLevel6.setEnabled(false);
                    cbProductLevel7.setPlaceholder("");
                    cbProductLevel7.clear();
                    cbProductLevel7.setEnabled(false);

                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName());
                    if(allProductsByCategory.isPresent()){
                        productsForGrid = allProductsByCategory.get();
                        addItemsToGrid(productsForGrid);
                    }
                    else{
                        List<Product>products = new ArrayList<>();
                        productsForGrid = products;
                        addItemsToGrid(productsForGrid);
                    }

                }
            }
             finally {
                updating.set(false);
            }

        });

        cbProductLevel2.addClassName("highlight-border");
        cbProductLevel2.addValueChangeListener(event -> {

            if (updating.get()) return;
            updating.set(true);
            try {
                tfGeneralFilter.setValue("");
                tfFolderFilter.setValue("");
                Optional<List<ProductLevel3>> productLevel3List = productLevel3Service.getProductLevel3sFromPreviousLevels(cbProductLevel2.getValue(), cbProductLevel1.getValue());
                if (!productLevel3List.isEmpty()) {
                    cbProductLevel3.setEnabled(true);
                    level3List = productLevel3List.get();
                    cbProductLevel3.setItems(level3List);
                    cbProductLevel3.setItemLabelGenerator(x -> x.getName());
                    cbProductLevel4.setPlaceholder("");
                    cbProductLevel4.clear();
                    cbProductLevel4.setEnabled(false);
                    cbProductLevel5.setPlaceholder("");
                    cbProductLevel5.clear();
                    cbProductLevel5.setEnabled(false);
                    cbProductLevel6.setPlaceholder("");
                    cbProductLevel6.clear();
                    cbProductLevel6.setEnabled(false);
                    cbProductLevel7.setPlaceholder("");
                    cbProductLevel7.clear();
                    cbProductLevel7.setEnabled(false);


                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName(), cbProductLevel1.getValue().getName());
                    if (allProductsByCategory.isPresent()) {
                        productsForGrid = allProductsByCategory.get();
                        addItemsToGrid(productsForGrid);
                    } else {
                        List<Product> products = new ArrayList<>();
                        productsForGrid = products;
                        addItemsToGrid(productsForGrid);
                    }

                } else {
                    cbProductLevel3.setPlaceholder("");
                    cbProductLevel3.clear();
                    cbProductLevel3.setEnabled(false);
                    cbProductLevel4.setPlaceholder("");
                    cbProductLevel4.clear();
                    cbProductLevel4.setEnabled(false);
                    cbProductLevel5.setPlaceholder("");
                    cbProductLevel5.clear();
                    cbProductLevel5.setEnabled(false);
                    cbProductLevel6.setPlaceholder("");
                    cbProductLevel6.clear();
                    cbProductLevel6.setEnabled(false);
                    cbProductLevel7.setPlaceholder("");
                    cbProductLevel7.clear();
                    cbProductLevel7.setEnabled(false);

                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName(), cbProductLevel1.getValue().getName());
                    if (allProductsByCategory.isPresent()) {
                        productsForGrid = allProductsByCategory.get();
                        addItemsToGrid(productsForGrid);
                    } else {
                        List<Product> products = new ArrayList<>();
                        productsForGrid = products;
                        addItemsToGrid(productsForGrid);
                    }

                }
            }
            finally {
                updating.set(false);
            }
        });

        cbProductLevel3.addClassName("highlight-border");
        cbProductLevel3.addValueChangeListener(event -> {

            if (updating.get()) return;
            updating.set(true);
            try {
                tfGeneralFilter.setValue("");
                tfFolderFilter.setValue("");
                Optional<List<ProductLevel4>> productLevel4List = productLevel4Service.getProductLevel4ByPreviousLevelNames(cbProductLevel3.getValue(), cbProductLevel2.getValue(), cbProductLevel1.getValue());
                if (!productLevel4List.isEmpty()) {
                    cbProductLevel4.setEnabled(true);
                    level4List = productLevel4List.get();
                    cbProductLevel4.setItems(level4List);
                    cbProductLevel4.setItemLabelGenerator(x -> x.getName());
                    cbProductLevel5.setPlaceholder("");
                    cbProductLevel5.clear();
                    cbProductLevel5.setEnabled(false);
                    cbProductLevel6.setPlaceholder("");
                    cbProductLevel6.clear();
                    cbProductLevel6.setEnabled(false);
                    cbProductLevel7.setPlaceholder("");
                    cbProductLevel7.clear();
                    cbProductLevel7.setEnabled(false);


                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName(), cbProductLevel2.getValue().getName(), cbProductLevel1.getValue().getName());
                    if (allProductsByCategory.isPresent()) {
                        productsForGrid = allProductsByCategory.get();
                        addItemsToGrid(productsForGrid);
                    } else {
                        List<Product> products = new ArrayList<>();
                        productsForGrid = products;
                        addItemsToGrid(productsForGrid);
                    }


                } else {
                    cbProductLevel4.setPlaceholder("");
                    cbProductLevel4.clear();
                    cbProductLevel4.setEnabled(false);
                    cbProductLevel5.setPlaceholder("");
                    cbProductLevel5.clear();
                    cbProductLevel5.setEnabled(false);
                    cbProductLevel6.setPlaceholder("");
                    cbProductLevel6.clear();
                    cbProductLevel6.setEnabled(false);
                    cbProductLevel7.setPlaceholder("");
                    cbProductLevel7.clear();
                    cbProductLevel7.setEnabled(false);

                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName(), cbProductLevel2.getValue().getName(), cbProductLevel1.getValue().getName());
                    if (allProductsByCategory.isPresent()) {
                        productsForGrid = allProductsByCategory.get();
                        addItemsToGrid(productsForGrid);
                    } else {
                        List<Product> products = new ArrayList<>();
                        productsForGrid = products;
                        addItemsToGrid(productsForGrid);
                    }

                }
            }
            finally {
                updating.set(false);
            }
        });

        cbProductLevel4.addClassName("highlight-border");
        cbProductLevel4.addValueChangeListener(event -> {

            if (updating.get()) return;
            updating.set(true);
            try {
                tfGeneralFilter.setValue("");
                tfFolderFilter.setValue("");
                Optional<List<ProductLevel5>> productLevel5List = productLevel5Service.getProductLevel5ByPreviousLevelNames(cbProductLevel4.getValue(), cbProductLevel3.getValue(), cbProductLevel2.getValue(), cbProductLevel1.getValue());
                if (!productLevel5List.isEmpty()) {
                    cbProductLevel5.setEnabled(true);
                    level5List = productLevel5List.get();
                    cbProductLevel5.setItems(level5List);
                    cbProductLevel5.setItemLabelGenerator(x -> x.getName());
                    cbProductLevel6.setPlaceholder("");
                    cbProductLevel6.clear();
                    cbProductLevel6.setEnabled(false);
                    cbProductLevel7.setPlaceholder("");
                    cbProductLevel7.clear();
                    cbProductLevel7.setEnabled(false);


                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName(), cbProductLevel3.getValue().getName(), cbProductLevel2.getValue().getName(), cbProductLevel1.getValue().getName());
                    if (allProductsByCategory.isPresent()) {
                        productsForGrid = allProductsByCategory.get();
                        addItemsToGrid(productsForGrid);
                    } else {
                        List<Product> products = new ArrayList<>();
                        productsForGrid = products;
                        addItemsToGrid(productsForGrid);
                    }

                } else {
                    cbProductLevel5.setPlaceholder("");
                    cbProductLevel5.clear();
                    cbProductLevel5.setEnabled(false);
                    cbProductLevel6.setPlaceholder("");
                    cbProductLevel6.clear();
                    cbProductLevel6.setEnabled(false);
                    cbProductLevel7.setPlaceholder("");
                    cbProductLevel7.clear();
                    cbProductLevel7.setEnabled(false);
                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName(), cbProductLevel3.getValue().getName(), cbProductLevel2.getValue().getName(), cbProductLevel1.getValue().getName());
                    if (allProductsByCategory.isPresent()) {
                        productsForGrid = allProductsByCategory.get();
                        addItemsToGrid(productsForGrid);
                    } else {
                        List<Product> products = new ArrayList<>();
                        productsForGrid = products;
                        addItemsToGrid(productsForGrid);
                    }
                }
            }
            finally {
                updating.set(false);
            }
        });

        cbProductLevel5.addClassName("highlight-border");
        cbProductLevel5.addValueChangeListener(event -> {

            if (updating.get()) return;
            updating.set(true);
            try {
                tfGeneralFilter.setValue("");
                tfFolderFilter.setValue("");
                Optional<List<ProductLevel6>> productLevel6List = productLevel6Service.getProductLevel6ByPreviousLevelNames(cbProductLevel5.getValue(), cbProductLevel4.getValue(), cbProductLevel3.getValue(), cbProductLevel2.getValue(), cbProductLevel1.getValue());
                if (!productLevel6List.isEmpty()) {
                    cbProductLevel6.setEnabled(true);
                    level6List = productLevel6List.get();
                    cbProductLevel6.setItems(level6List);
                    cbProductLevel6.setItemLabelGenerator(x -> x.getName());
                    cbProductLevel7.setPlaceholder("");
                    cbProductLevel7.clear();
                    cbProductLevel7.setEnabled(false);


                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName(), cbProductLevel4.getValue().getName(), cbProductLevel3.getValue().getName(), cbProductLevel2.getValue().getName(), cbProductLevel1.getValue().getName());
                    if (allProductsByCategory.isPresent()) {
                        productsForGrid = allProductsByCategory.get();
                        addItemsToGrid(productsForGrid);
                    } else {
                        List<Product> products = new ArrayList<>();
                        productsForGrid = products;
                        addItemsToGrid(productsForGrid);
                    }


                } else {
                    cbProductLevel6.setPlaceholder("");
                    cbProductLevel6.clear();
                    cbProductLevel6.setEnabled(false);
                    cbProductLevel7.setPlaceholder("");
                    cbProductLevel7.clear();
                    cbProductLevel7.setEnabled(false);
                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName(), cbProductLevel4.getValue().getName(), cbProductLevel3.getValue().getName(), cbProductLevel2.getValue().getName(), cbProductLevel1.getValue().getName());
                    if (allProductsByCategory.isPresent()) {
                        productsForGrid = allProductsByCategory.get();
                        addItemsToGrid(productsForGrid);
                    } else {
                        List<Product> products = new ArrayList<>();
                        productsForGrid = products;
                        addItemsToGrid(productsForGrid);
                    }

                }
            }
            finally {
                updating.set(false);
            }
        });
        cbProductLevel6.addClassName("highlight-border");
        cbProductLevel6.addValueChangeListener(event -> {

            if (updating.get()) return;
            updating.set(true);
            try {
                tfGeneralFilter.setValue("");
                tfFolderFilter.setValue("");
                Optional<List<ProductLevel7>> productLevel7List = productLevel7Service.getProductLevel7ByPreviousLevelNames(cbProductLevel6.getValue(), cbProductLevel5.getValue(), cbProductLevel4.getValue(), cbProductLevel3.getValue(), cbProductLevel2.getValue(), cbProductLevel1.getValue());
                if (!productLevel7List.isEmpty()) {
                    cbProductLevel7.setEnabled(true);
                    level7List = productLevel7List.get();
                    cbProductLevel7.setItems(level7List);
                    cbProductLevel7.setItemLabelGenerator(x -> x.getName());


                    Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName(), cbProductLevel5.getValue().getName(), cbProductLevel4.getValue().getName(), cbProductLevel3.getValue().getName(), cbProductLevel2.getValue().getName(), cbProductLevel1.getValue().getName());
                    if (allProductsByCategory.isPresent()) {
                        productsForGrid = allProductsByCategory.get();
                        addItemsToGrid(productsForGrid);
                    } else {
                        List<Product> products = new ArrayList<>();
                        productsForGrid = products;
                        addItemsToGrid(productsForGrid);
                    }


                } else {
                    cbProductLevel7.setPlaceholder("");
                    cbProductLevel7.clear();
                    cbProductLevel7.setEnabled(false);
                    if (selectedProduct == null) {
                        //filter Products
                        Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName(), cbProductLevel5.getValue().getName(), cbProductLevel4.getValue().getName(), cbProductLevel3.getValue().getName(), cbProductLevel2.getValue().getName(), cbProductLevel1.getValue().getName());
                        if (allProductsByCategory.isPresent()) {
                            productsForGrid = allProductsByCategory.get();
                            addItemsToGrid(allProductsByCategory.get());
                        } else {
                            List<Product> products = new ArrayList<>();
                            productsForGrid = products;
                            addItemsToGrid(productsForGrid);
                        }
                    }
                }

            }
            finally {
                updating.set(false);
            }
        });

        cbProductLevel7.addClassName("highlight-border");
        cbProductLevel7.addValueChangeListener(event -> {
            if (updating.get()) return;
            updating.set(true);
            try {
                tfGeneralFilter.setValue("");
                tfFolderFilter.setValue("");
                Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(event.getValue().getName(), cbProductLevel6.getValue().getName(), cbProductLevel5.getValue().getName(), cbProductLevel4.getValue().getName(), cbProductLevel3.getValue().getName(), cbProductLevel2.getValue().getName(), cbProductLevel1.getValue().getName());
                if (allProductsByCategory.isPresent()) {
                    productsForGrid = allProductsByCategory.get();
                    addItemsToGrid(productsForGrid);
                } else {
                    List<Product> products = new ArrayList<>();
                    productsForGrid = products;
                    addItemsToGrid(productsForGrid);
                }
            }
            finally {
                updating.set(false);
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
        editor = grid.getEditor();
        editor.setBuffered(true);
        editor.setBinder(productBinder);
        productBinder.forField(tfProductCode)
                .withNullRepresentation("")
                .bind(Product::getProductCode, Product::setProductCode);
        codeColumn.setEditorComponent(tfProductCode);
        productBinder.forField(tfPositionNumber)
                .withNullRepresentation("")
                    .bind(Product::getPositionNumber, Product::setPositionNumber);
        posColumn.setEditorComponent(tfPositionNumber);
        tfInternalName.setWidth("100%");
        productBinder.forField(tfInternalName)
                .withNullRepresentation("")
                .bind(Product::getInternalName, Product::setInternalName);
        internalNameColumn.setEditorComponent(tfInternalName);
        productBinder.forField(tfPurchasePrice)
                .withNullRepresentation("0.00")
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
                .bind(Product::getPurchasePrice, Product::setPurchasePrice);
        purchaceColumn.setEditorComponent(tfPurchasePrice);
        productBinder.forField(tfSellMargin)
                .withNullRepresentation("0.00")
                .withConverter(
                        new StringToDoubleConverter("De ingave moet een decimaal nummer zijn (getal met een punt als komma)"))
                .bind(Product::getSellMargin, Product::setSellMargin);
        marginColumn.setEditorComponent(tfSellMargin);
        productBinder.forField(tfSellIndustryMargin)
                .withNullRepresentation("0.00")
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
                .bind(Product::getSellMarginIndustry, Product::setSellMarginIndustry);
        marginIndustryColumn.setEditorComponent(tfSellIndustryMargin);

        productBinder.forField(tfSellPrice)
                .withNullRepresentation("0.0")
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
                .bind(Product::getSellPrice, Product::setSellPrice);
        sellComumn.setEditorComponent(tfSellPrice);
        productBinder.forField(tfSellIndustryPrice)
                .withNullRepresentation("0.0")
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
                .bind(Product::getSellPriceIndustry, Product::setSellPriceIndustry);
        sellIndustryComumn.setEditorComponent(tfSellIndustryPrice);
        tfComment.setWidth("100%");
        productBinder.forField(tfComment)
                .withNullRepresentation("")
                .bind(Product::getComment, Product::setComment);
        commentColumn.setEditorComponent(tfComment);
        tfMoQ.setWidth("100%");
        productBinder.forField(tfMoQ)
                .withNullRepresentation("")
                .bind(Product::getMoq, Product::setMoq);
        moqColumn.setEditorComponent(tfMoQ);
        productBinder.forField(tfUnit)
                .withNullRepresentation("")
                .bind(Product::getUnit, Product::setUnit);
        unitColumn.setEditorComponent(tfUnit);
//        productBinder.forField(cbProductLevel1)
//                        .bind(Product::getProductLevel1, Product::setProductLevel1);
//        productBinder.forField(cbProductLevel2)
//                .bind(Product::getProductLevel2, Product::setProductLevel2);
//        productBinder.forField(cbProductLevel3)
//                .bind(Product::getProductLevel3, Product::setProductLevel3);
//        productBinder.forField(cbProductLevel4)
//                .bind(Product::getProductLevel4, Product::setProductLevel4);
//        productBinder.forField(cbProductLevel5)
//                .bind(Product::getProductLevel5, Product::setProductLevel5);
//        productBinder.forField(cbProductLevel6)
//                .bind(Product::getProductLevel6, Product::setProductLevel6);
//        productBinder.forField(cbProductLevel7)
//                .bind(Product::getProductLevel7, Product::setProductLevel7);
//        productBinder.addValueChangeListener(event -> {
//            if ((selectedProduct != null) && (event.getValue() != null)) {
//                try {
//                    productBinder.writeBean(selectedProduct);
//                } catch (ValidationException e) {
//                    throw new RuntimeException(e);
//                }
//                grid.getDataProvider().refreshItem(selectedProduct);
//            }
//            else{
//                if(!event.isFromClient()){
//                    clearForm();
//                    cbProductLevel1.setValue(cbProductLevel1.getEmptyValue());
//                    cbProductLevel2.setValue(cbProductLevel2.getEmptyValue());
//                    cbProductLevel3.setValue(cbProductLevel3.getEmptyValue());
//                    cbProductLevel4.setValue(cbProductLevel4.getEmptyValue());
//                    cbProductLevel5.setValue(cbProductLevel5.getEmptyValue());
//                    cbProductLevel6.setValue(cbProductLevel6.getEmptyValue());
//                    cbProductLevel7.setValue(cbProductLevel7.getEmptyValue());
//                }
//            }
//        });
    }

    private void addDataToGrid() {
        Optional<List<Product>>optproducts = productService.getAllProducts();
        if (!optproducts.isEmpty()) {
            addItemsToGrid(optproducts.get());
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
            productsForGrid = products;
            addItemsToGrid(productsForGrid);
        }
    }

    private void setUpGrid() {
        // Configure Grid
        //grid.addColumn("id").setAutoWidth(true);
        Grid.Column<Product> trashColumn = grid.addComponentColumn(item -> {
            Button closeButton = new Button(new Icon(VaadinIcon.TRASH));
            closeButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            closeButton.addClickListener(event -> {
                Notification.show("Openen verificatie");
                selectedProduct = item;
                deleteProductNotification.open();
            });
            return closeButton;
        }).setFlexGrow(0).setHeader(new Icon(VaadinIcon.TRASH)).setTextAlign(ColumnTextAlign.CENTER);
        trashColumn.addClassName("center-header");
        trashColumn.getElement().getThemeList().add("no-min-width");
        trashColumn.setWidth("70px");

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
        Grid.Column<Product> camColumn = grid.addComponentColumn(product -> {
            if ((product.getImageList() != null) && (!product.getImageList().isEmpty())) {
                Button imageButton = new Button(new Icon(VaadinIcon.CAMERA));
                imageButton.addClickListener(event -> {
                    showImageSubVieuw.setUser(UserFunction.TECHNICIAN);
                    showImageSubVieuw.setSelectedWorkOrder(product.getImageList());
                    showImageSubVieuw.setTitle(product.getProductCode() + " " + product.getInternalName());
                    showImageDialog.open();
                });
                return imageButton;
            } else {
                return new Span("");
            }
        }).setFlexGrow(0).setHeader(new Icon(VaadinIcon.CAMERA)).setTextAlign(ColumnTextAlign.CENTER);
        camColumn.addClassName("center-header");
        camColumn.getElement().getThemeList().add("no-min-width");
        camColumn.setWidth("70px");

        Grid.Column<Product> OSGrid = grid.addComponentColumn(product -> {
            if ((product.getSetElement() != null) && (product.getSetElement() == true)) {
                Button elementButton = new Button("O");
                elementButton.addThemeVariants(ButtonVariant.LUMO_ICON);
                elementButton.addClickListener(event -> {
                    selectedSetList.clear();
                    Optional<List<Product>> allSets = productService.getAllSetsContaining(product);
                    if (!allSets.isEmpty()) {
                        selectedSetList.addAll(allSets.get());
                        setViewSimple.setSelectedProduct(product);
                        setViewSimpleDialog.open();
                    }
                });
                return elementButton;
            }
            if ((product.getSet() != null) && (product.getSet() == true)) {
                Button setButton = new Button("S");
                setButton.addThemeVariants(ButtonVariant.LUMO_ICON);
                setButton.addClickListener(event -> {
                    selectedProduct = product;
                    openEditSet();
                });
                return setButton;
            } else {
                //Check if there are Copies
                if((product.getProductCode() != null) && (!product.getProductCode().isEmpty())){
                    Optional<List<Product>> byProductCodeEqualCaseInsensitive = productService.findByProductCodeEqualCaseInsensitive(product.getProductCode());
                    if (byProductCodeEqualCaseInsensitive.isPresent()) {
                        if(byProductCodeEqualCaseInsensitive.get() != null){
                            long amountArticlesInSet = byProductCodeEqualCaseInsensitive.get().stream().filter(item -> (item.getSetElement() != null) &&(item.getSetElement() == true)).count();
                            if(amountArticlesInSet > 0){
                                Button setButton = new Button("C"+amountArticlesInSet);
                                setButton.addThemeVariants(ButtonVariant.LUMO_ICON);
                                setButton.addClickListener(event -> {
                                    selectedProduct = product;
                                    setViewSimple.setSelectedProduct(product);
                                    setViewSimpleDialog.open();
                                });
                                return setButton;
                            }
                            else{
                                return new Span("");
                            }
                        }
                    }
                }
                return new Span("");
            }
        }).setFlexGrow(1).setHeader(new Span("S/O/C")).setTextAlign(ColumnTextAlign.CENTER);
        OSGrid.addClassName("center-header");
        OSGrid.getElement().getThemeList().add("no-min-width");
        OSGrid.setWidth("70px");

        Grid.Column<Product> pdfColumn = grid.addComponentColumn(product -> {
            if ((product.getPdfList() != null) && (!product.getPdfList().isEmpty())) {
                Button pdfButton = new Button(new Icon(VaadinIcon.FILE_FONT));
                pdfButton.addClickListener(event -> {
                    //TODO show pdf Dialog
                    showPdfSubVieuw.setUser(UserFunction.TECHNICIAN);
                    showPdfSubVieuw.setSelectedWorkOrder(product.getPdfList());
                    showPdfSubVieuw.setTitle(product.getProductCode() + " " + product.getInternalName());
                    pdfDialog.open();
                });
                return pdfButton;
            } else return new Span("");
        }).setFlexGrow(0).setHeader(new Icon(VaadinIcon.FILE_FONT)).setTextAlign(ColumnTextAlign.CENTER);
        pdfColumn.addClassName("center-header");
        pdfColumn.getElement().getThemeList().add("no-min-width");
        pdfColumn.setWidth("70px");

        Grid.Column<Product> linkColumn = grid.addComponentColumn(product -> {
            if ((product.getLinkDocumentList() != null) && (!product.getLinkDocumentList().isEmpty()) && (product.getLinkDocumentList().stream().anyMatch(item -> item.getLink().length() > 0))) {
                Button linkButton = new Button(new Icon(VaadinIcon.LINK));
                linkButton.addClickListener(event -> {
                    showLinkSubVieuw.setSelectedWorkOrder(product.getLinkDocumentList());
                    showLinkSubVieuw.setTitle(product.getProductCode() + " " + product.getInternalName());
                    linkDialog.open();
                });
                return linkButton;
            } else return new Span("");
        }).setFlexGrow(0).setHeader(new Icon(VaadinIcon.LINK)).setTextAlign(ColumnTextAlign.CENTER);
        linkColumn.addClassName("center-header");
        linkColumn.getElement().getThemeList().add("no-min-width");
        linkColumn.setWidth("70px");

        codeColumn = grid.addColumn("productCode").setHeader("Code").setResizable(true).setWidth("190px").setFlexGrow(0);
        posColumn = grid.addColumn(o -> o.getPositionNumber())
                .setComparator((o1, o2) -> {
                    return compareOnderdeel(o1.getPositionNumber(), o2.getPositionNumber()); // ascending of descending handled by Vaadin
                })
                .setHeader("Pos")
                .setResizable(true)
                .setWidth("75px")
                .setFlexGrow(0);
        internalNameColumn = grid.addColumn("internalName").setSortable(true)
                .setComparator((o1, o2) -> compareOnderdeel(o1.getInternalName(), o2.getInternalName()))
                .setHeader("Naam").setResizable(true).setFlexGrow(2);
        grid.sort(List.of(new GridSortOrder<>(internalNameColumn, SortDirection.ASCENDING)));
        purchaceColumn = grid.addColumn(item -> {
            if(item.getPurchasePrice() != null){
                return "€ " + df.format(item.getPurchasePrice());
            }
            else{
                return "-";
            }
        }).setHeader("Aankoop").setResizable(true).setWidth("120px").setFlexGrow(0).setTextAlign(ColumnTextAlign.END);

        marginColumn = grid.addColumn(item -> {
            if((item.getSellMargin() != null) && (!item.getSellMargin().isNaN())){
                return df.format(item.getSellMargin());
            }
            else{
                return "-";
            }
        }).setHeader("Marge A").setResizable(true).setWidth("120px").setFlexGrow(0).setTextAlign(ColumnTextAlign.END);

        sellComumn = grid.addColumn(item -> {
            if((item.getSellPrice() != null) && (!item.getSellPrice().isNaN())){
                return "€ " + df.format(item.getSellPrice());
            }
            else{
                return "-";
            }
        }).setHeader("Verkoop A").setResizable(true).setWidth("120px").setFlexGrow(0).setTextAlign(ColumnTextAlign.END);

        marginIndustryColumn = grid.addColumn(item -> {
            if(item.getSellMarginIndustry() != null){
                return df.format(item.getSellMarginIndustry());
            }
            else{
                return "-";
            }
        }).setHeader("Marge I").setResizable(true).setWidth("120px").setFlexGrow(0).setTextAlign(ColumnTextAlign.END);
        marginIndustryColumn.setClassNameGenerator(item -> "industry-column");

        sellIndustryComumn = grid.addColumn(item -> {
            if(item.getSellPriceIndustry() != null){
                return "€ " + df.format(item.getSellPriceIndustry());
            }
            else{
                return "-";
            }
        }).setHeader("Verkoop I").setResizable(true).setWidth("120px").setFlexGrow(0).setTextAlign(ColumnTextAlign.END);

        commentColumn = grid.addColumn("comment").setHeader("Commentaar").setWidth("330px").setFlexGrow(1).setResizable(true);
        moqColumn = grid.addColumn(item -> {
            if((item.getMoq() != null)){
                return item.getMoq();
            }
            else{
                return "";
            }
        }).setHeader("B/P.").setResizable(true).setWidth("100px").setFlexGrow(0).setFrozenToEnd(true);
        unitColumn = grid.addColumn(item -> {
            if(item.getUnit() != null){
                return item.getUnit();
            }
            else{
                return "";
            }
        }).setHeader("E/H.").setResizable(true).setWidth("80px").setFlexGrow(0).setFrozenToEnd(true);

        grid.setPartNameGenerator(product -> {
            return "industry";
        });

        //when a row is selected or deselected, populate form
        grid.addItemClickListener(event -> {
           editor.cancel();
           editor.closeEditor();

           selectedProduct = event.getItem();
           var value = event.getItem();

            if (value != null) {
                updating.set(true);
                if (value.getProductLevel1() != null){
                    if((cbProductLevel1.getValue() == null)){
                        cbProductLevel1.clear();
                        cbProductLevel1.setEnabled(true);
                        cbProductLevel1.setPlaceholder(value.getProductLevel1().getName());
                    }
                    else{
                        cbProductLevel1.setPlaceholder(value.getProductLevel1().getName());
                    }
                }
                else{
                    cbProductLevel1.clear();
                    cbProductLevel1.setEnabled(false);
                    cbProductLevel1.setPlaceholder("");
                }

                if (value.getProductLevel2() != null){
                    if(cbProductLevel2.getValue() == null){
                        cbProductLevel2.clear();
                        cbProductLevel2.setEnabled(true);
                        cbProductLevel2.setPlaceholder(value.getProductLevel2().getName());
                    }
                    else{
                        cbProductLevel2.setPlaceholder(value.getProductLevel2().getName());
                    }
                }
                else{
                    cbProductLevel2.clear();
                    cbProductLevel2.setEnabled(false);
                    cbProductLevel2.setPlaceholder("");
                }

                if (value.getProductLevel3() != null){
                    if(cbProductLevel3.getValue() == null){
                        cbProductLevel3.clear();
                        cbProductLevel3.setEnabled(true);
                        cbProductLevel3.setPlaceholder(value.getProductLevel3().getName());
                    }
                    else{
                        cbProductLevel3.setPlaceholder(value.getProductLevel3().getName());
                    }
                }
                else{
                    cbProductLevel3.clear();
                    cbProductLevel3.setEnabled(false);
                    cbProductLevel3.setPlaceholder("");
                }

                if (value.getProductLevel4() != null){
                    if((cbProductLevel4.getValue() == null)){
                        cbProductLevel4.clear();
                        cbProductLevel4.setEnabled(true);
                        cbProductLevel4.setPlaceholder(value.getProductLevel4().getName());
                    }
                    else{
                        cbProductLevel4.setPlaceholder(value.getProductLevel4().getName());
                    }
                }
                else{
                    cbProductLevel4.clear();
                    cbProductLevel4.setEnabled(false);
                    cbProductLevel4.setPlaceholder("");
                }

                if (value.getProductLevel5() != null){
                    if((cbProductLevel5.getValue() == null)){
                        cbProductLevel5.clear();
                        cbProductLevel5.setEnabled(true);
                        cbProductLevel5.setPlaceholder(value.getProductLevel5().getName());
                    }
                    else{
                        cbProductLevel5.setPlaceholder(value.getProductLevel5().getName());
                    }
                }
                else{
                    cbProductLevel5.clear();
                    cbProductLevel5.setEnabled(false);
                    cbProductLevel5.setPlaceholder("");
                }
                updating.set(false);
            }

            if(grid.getSelectedItems().size() > 0){
                if((event.getItem() != null) && (event.getItem().getPurchasePriseList() != null) && (event.getItem().getPurchasePriseList().size() > 0)){
                    purchasePriceGrid.setItems(event.getItem().getPurchasePriseList());
                }
                else{
                    List<PurchasePrice> newPurchasePriceList = getNewPurchasePriceList();
                    purchasePriceGrid.setItems(newPurchasePriceList);
                    event.getItem().setPurchasePriseList(newPurchasePriceList);
                }
            }
            else{
                List<PurchasePrice> newPurchasePriceList = getNewPurchasePriceList();
                purchasePriceGrid.setItems(newPurchasePriceList);
                purchasePriceGrid.getDataProvider().refreshAll();
            }
        });

        grid.addItemDoubleClickListener(event -> {
            selectedProduct = event.getItem();
            editor.cancel();
            editor.editItem(event.getItem());
            if((event.getItem().getSet() != null) && (event.getItem().getSet())){
                tfPurchasePrice.setEnabled(false);
                tfSellMargin.setEnabled(false);
                tfSellPrice.setEnabled(false);
                tfSellIndustryMargin.setEnabled(false);
                tfSellIndustryPrice.setEnabled(false);
            }
            else{
                tfPurchasePrice.setEnabled(true);
                tfSellMargin.setEnabled(true);
                tfSellPrice.setEnabled(true);
                tfSellIndustryMargin.setEnabled(true);
                tfSellIndustryPrice.setEnabled(true);
            }
            Component editorComponent = event.getColumn().getEditorComponent();
            if (editorComponent instanceof Focusable) {
                ((Focusable) editorComponent).focus();
            }
        });

        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }


    private int compareOnderdeel(String s1, String s2) {
        if((s1 != null) && (s2 != null)){
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
        return 9999;
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

    private Div createEditorLayout() {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setWidth("100%");
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setWidth("100%");
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        HorizontalLayout hLayout1 = new HorizontalLayout();
        hLayout1.setWidth("100%");
        HorizontalLayout hLayout2 = new HorizontalLayout();
        hLayout2.setWidth("100%");
        tfGeneralFilter = new TextField("");
        tfGeneralFilter.setPlaceholder("General filter");
        tfFolderFilter = new TextField("");
        tfFolderFilter.setPlaceholder("Folder filter");
        tfProductCode = new TextField("Artikelcode");
        tfPositionNumber = new TextField("Positienummer");
        tfInternalName = new TextField("Interne omschrijving");
        tfPurchasePrice = new TextField("Aankoopprijs");
        tfSellMargin = new TextField("Verkoopmarge Agro");
        tfSellPrice = new TextField("Verkoopprijs Agro");
        tfSellIndustryMargin = new TextField("Verkoopmarge Industrie");
        tfSellIndustryPrice = new TextField("Verkoopprijs Industrie");
        tfComment = new TextField("Commentaar");
        tfMoQ = new TextField("MoQ");
        tfUnit = new TextField("Eenheid");
        cbProductLevel1 = new ComboBox("");
        cbProductLevel1.setWidth("100%");
        cbProductLevel2 = new ComboBox("");
        cbProductLevel2.setWidth("100%");
        cbProductLevel3 = new ComboBox("");
        cbProductLevel3.setWidth("100%");
        cbProductLevel4 = new ComboBox("");
        cbProductLevel4.setWidth("100%");
        cbProductLevel5 = new ComboBox("");
        cbProductLevel5.setWidth("100%");
        cbProductLevel6 = new ComboBox("");
        cbProductLevel6.setWidth("100%");
        cbProductLevel7 = new ComboBox("");
        cbProductLevel7.setWidth("100%");
        hLayout1.add(getJumpToFolderIcon(),
                setUpHorizontalLayoutFor(cbProductLevel1,bAddProductLevel1,E_Product_Level.PRODUCTLEVEL1),
                setUpHorizontalLayoutFor(cbProductLevel2,bAddProductLevel2,E_Product_Level.PRODUCTLEVEL2),
                        setUpHorizontalLayoutFor(cbProductLevel3,bAddProductLevel3,E_Product_Level.PRODUCTLEVEL3),
                                setUpHorizontalLayoutFor(cbProductLevel4,bAddProductLevel4,E_Product_Level.PRODUCTLEVEL4),
                                        setUpHorizontalLayoutFor(cbProductLevel5,bAddProductLevel5,E_Product_Level.PRODUCTLEVEL5)
                //setUpHorizontalLayoutFor(cbProductLevel6,bAddProductLevel6,E_Product_Level.PRODUCTLEVEL6),
                    //setUpHorizontalLayoutFor(cbProductLevel7,bAddProductLevel7,E_Product_Level.PRODUCTLEVEL7),
                );
        hLayout2.add(tfGeneralFilter,tfFolderFilter, createButtonLayout());

        editorDiv.add(hLayout1);
        editorDiv.add(hLayout2);

        return editorLayoutDiv;
    }

    private Component getJumpToFolderIcon() {
        goToFolderButton = new Button(VaadinIcon.ARROW_CIRCLE_UP.create());
        goToFolderButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        goToFolderButton.addClickListener(event -> {

            String placeholder1 = cbProductLevel1.getPlaceholder();
            String placeholder2 = cbProductLevel2.getPlaceholder();
            String placeholder3 = cbProductLevel3.getPlaceholder();
            String placeholder4 = cbProductLevel4.getPlaceholder();
            String placeholder5 = cbProductLevel5.getPlaceholder();

            updating.set(true);
            cbProductLevel1.setItems(productLevel1Service.getAllProductLevel1().get());
            updating.set(false);
            if(placeholder1.length() > 0)
                cbProductLevel1.setValue(productLevel1Service.getProductLevel1ByName(placeholder1).get());
            if(placeholder2.length() > 0)
                cbProductLevel2.setValue(productLevel2Service.getProductLevel2ByName(placeholder2).get());
            if(placeholder3.length() > 0)
                cbProductLevel3.setValue(productLevel3Service.getProductLevel3ByName(placeholder3).get());
            if(placeholder4.length() > 0)
                cbProductLevel4.setValue(productLevel4Service.getProductLevel4ByName(placeholder4).get());
            if(placeholder5.length() > 0)
                cbProductLevel5.setValue(productLevel5Service.getProductLevel5ByName(placeholder5).get());
            Notification.show("Ga naar map");
        });
        return goToFolderButton;
    }

    private HorizontalLayout setUpHorizontalLayoutFor(ComboBox comboBox, Button addButton, E_Product_Level productLevel) {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidth("100%");
        horizontalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        horizontalLayout.setAlignItems(Alignment.BASELINE);
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


    private HorizontalLayout createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setAlignItems(Alignment.BASELINE);
        buttonLayout.setAlignItems(Alignment.END);
        buttonLayout.setClassName("button-layout");
        buttonNewProduct.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(buttonSave, buttonNewProduct, getActionMenu());
        return buttonLayout;
    }

    private MenuBar getActionMenu() {
        actionBar = new MenuBar();
        MenuItem actie = actionBar.addItem("Actie");
        actie.getSubMenu().addItem("Bewerk set", e -> openEditSet());
        actie.getSubMenu().addItem("Voeg toe", e -> changeProduct());
        actie.getSubMenu().addItem("Verplaats artikel", e -> moveProduct());
        actie.getSubMenu().addItem("Kopiëer artikel", e -> copyProduct());
        return actionBar;
    }

    private void moveProduct() {
        moveProductDialog.open();
        String result1 = cbProductLevel1.getValue() == null ? "" : cbProductLevel1.getValue().getName();
        String result2 = cbProductLevel2.getValue() == null ? "" : cbProductLevel2.getValue().getName();
        String result3 = cbProductLevel3.getValue() == null ? "" : cbProductLevel3.getValue().getName();
        String result4 = cbProductLevel4.getValue() == null ? "" : cbProductLevel4.getValue().getName();
        String result5 = cbProductLevel5.getValue() == null ? "" : cbProductLevel5.getValue().getName();
        moveView.goToSelectedFolder(result1, result2, result3, result4, result5);
    }

    private void copyProduct() {
        copyProductDialog.open();
        String result1 = cbProductLevel1.getValue() == null ? "" : cbProductLevel1.getValue().getName();
        String result2 = cbProductLevel2.getValue() == null ? "" : cbProductLevel2.getValue().getName();
        String result3 = cbProductLevel3.getValue() == null ? "" : cbProductLevel3.getValue().getName();
        String result4 = cbProductLevel4.getValue() == null ? "" : cbProductLevel4.getValue().getName();
        String result5 = cbProductLevel5.getValue() == null ? "" : cbProductLevel5.getValue().getName();
        copyView.goToSelectedFolder(result1, result2, result3, result4, result5);
    }

    private void openEditSet(){
        configureSetDialog.open();
        setView.setSelectedProductForSet(selectedProduct);
    }

    private void changeProduct(){
        changeArticleView.setSelectedProduct(selectedProduct);
        changeArticleDialog.open();
    }

    private Div createGridLayout() {
        Div wrapper = new Div();
        wrapper.setSizeFull();
        wrapper.setClassName("grid-wrapper");
        SplitLayout gridSplitLayoout = new SplitLayout();
        gridSplitLayoout.setHeight("100%");
        gridSplitLayoout.setOrientation(SplitLayout.Orientation.VERTICAL);
        gridSplitLayoout.setSplitterPosition(80);
        gridSplitLayoout.addToPrimary(grid);
        gridSplitLayoout.addToSecondary(purchasePriceGrid);
        wrapper.add(gridSplitLayoout);
        return wrapper;
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
            Notification.show(e.getMessage());
        }
    }

//    private void refreshSelectedProductAndSelectedSetLogoButton(){
//        if((selectedProduct != null) && (selectedProduct.getSetList() != null) && (selectedProduct.getSetList().size() > 0)){
//
//            for(Product setElement : selectedProduct.getSetList()){
//                productService.get(setElement.getId()).ifPresent(product -> {
//                    productForGrid.stream().filter(item -> item.getId().matches(product.getId())).findFirst().ifPresent(item -> {
//                        item.setSet(product.getSet());
//                        item.setSetElement(product.getSetElement());
//                        grid.getDataProvider().refreshItem(item);
//                    });
//                });
//            }
//        }
//        if((selectedSetList != null) && (selectedSetList.size() > 0)){
//            for (Product set : selectedSetList) {
//                for (Product setElement : set.getSetList()) {
//                    productService.get(setElement.getId()).ifPresent(product -> {
//                        productForGrid.stream().filter(item -> item.getId().matches(product.getId())).findFirst().ifPresent(item -> {
//                            item.setSet(product.getSet());
//                            item.setSetElement(product.getSetElement());
//                            grid.getDataProvider().refreshItem(item);
//                        });
//                    });
//                }
//            }
//        }
//    }

    private void tryToCalculateSellPriceAgro(Product product) {
        try{
            Optional<Double>optDoublePurchasePrice = Optional.of(Double.valueOf(tfPurchasePrice.getValue()));
            Optional<Double>optDoubleSellMargin = Optional.of(Double.valueOf(tfSellMargin.getValue()));

            if(optDoublePurchasePrice.isPresent()) {
                if(optDoubleSellMargin.isPresent()) {
                    product.setSellPrice(optDoublePurchasePrice.get()  *(optDoubleSellMargin.get()));
                    tfSellPrice.setValue(product.getSellPrice().toString());
                }
            }
            else{
                Notification.show("Gelieve te starten met de aankoopprijs en marge");
            }
        }
        catch (Exception e){
            Notification notification = new Notification("Gelieve decimale getallen in te vullen aub!");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void tryToCalculateSellPriceIndustry(Product product) {
        try{
            Optional<Double>optDoublePurchasePrice = Optional.of(Double.valueOf(tfPurchasePrice.getValue()));
            Optional<Double>optDoubleSellMargin = Optional.of(Double.valueOf(tfSellIndustryMargin.getValue()));

            if(optDoublePurchasePrice.isPresent()) {
                if(optDoubleSellMargin.isPresent()) {
                    product.setSellPriceIndustry(optDoublePurchasePrice.get()  *(optDoubleSellMargin.get()));
                    tfSellIndustryPrice.setValue(product.getSellPriceIndustry().toString());
                }
            }
            else{
                Notification.show("Gelieve te starten met de aankoopprijs en marge");
            }
        }
        catch (Exception e){
            Notification notification = new Notification("Gelieve decimale getallen in te vullen aub!");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void addItemsToGrid(List<Product> productList) {
        productListToShowInGrid.clear();
        productListToShowInGrid.addAll(productList);
        grid.setItems(productListToShowInGrid);
    }

    private void refreshItemsInGrid(){
        temporaryProductList.clear();
        temporaryProductList.addAll(productListToShowInGrid);

        productListToShowInGrid.clear();
        temporaryProductList.stream().forEach(item -> productService.get(item.getId()).ifPresent(product -> {productListToShowInGrid.add(product);}));
        grid.setItems(productListToShowInGrid);
    }

    public void goToRightFolderAndSelectProduct(Product product) {
        updating.set(true);
        cbProductLevel1.setItems(productLevel1Service.getAllProductLevel1().get());
        updating.set(false);
        if((product.getProductLevel1() != null)&&(product.getProductLevel1().getName().length() > 0))
            cbProductLevel1.setValue(productLevel1Service.getProductLevel1ByName(product.getProductLevel1().getName()).get());
        if((product.getProductLevel2() != null)&&(product.getProductLevel2().getName().length() > 0))
            cbProductLevel2.setValue(productLevel2Service.getProductLevel2ByName(product.getProductLevel2().getName()).get());
        if((product.getProductLevel3() != null)&&(product.getProductLevel3().getName().length() > 0))
            cbProductLevel3.setValue(productLevel3Service.getProductLevel3ByName(product.getProductLevel3().getName()).get());
        if((product.getProductLevel4() != null)&&(product.getProductLevel4().getName().length() > 0))
            cbProductLevel4.setValue(productLevel4Service.getProductLevel4ByName(product.getProductLevel4().getName()).get());
        if((product.getProductLevel5() != null)&&(product.getProductLevel5().getName().length() > 0))
            cbProductLevel5.setValue(productLevel5Service.getProductLevel5ByName(product.getProductLevel5().getName()).get());
        //select product
        Optional<Product> itemToSelect = productsForGrid.stream().filter(item -> item.getProductCode().matches(product.getProductCode())).findFirst();
        if(itemToSelect.isPresent()) {
            grid.select(itemToSelect.get());
        }
        else{
            Notification.show("Geen match gevonden");
        }
    }

//    private Notification createReportChangePurchasePrice() {
//        changePurchasePriceNotification = new Notification();
//        changePurchasePriceNotification.addThemeVariants(NotificationVariant.LUMO_WARNING);
//
//        Icon icon = VaadinIcon.WARNING.create();
//        Button retryBtn = new Button("Annuleer",
//                clickEvent -> changePurchasePriceNotification.close());
//        retryBtn.getStyle().setMargin("0 0 0 var(--lumo-space-l)");
//
//        var layout = new HorizontalLayout(icon,
//                new Text("Ben je zeker dat je de aankoopprijs van alle artikelen met deze code wilt wijzigen?"), retryBtn,
//                createRemoveProductBtn(changePurchasePriceNotification));
//        layout.setAlignItems(FlexComponent.Alignment.CENTER);
//
//        changePurchasePriceNotification.add(layout);
//
//        return changePurchasePriceNotification;
//    }
//
//    public Button createRemoveProductBtn(Notification notification) {
//        Button closeBtn = new Button(VaadinIcon.PENCIL.create(),
//                clickEvent -> {
//                    if((selectedProduct.getProductCode() != null) && (selectedProduct.getProductCode().length() > 1)){
//                        Optional<List<Product>> byProductCodeEqualCaseInsensitive = productService.findByProductCodeEqualCaseInsensitive(selectedProduct.getProductCode());
//                        if((byProductCodeEqualCaseInsensitive.isPresent() && (byProductCodeEqualCaseInsensitive.get().size() > 0))) {
//                            for(Product product : byProductCodeEqualCaseInsensitive.get()) {
//                                product.setPurchasePrice(selectedProduct.getPurchasePrice());
//                                tryToCalculateSellPrice(product);
//                                productService.save(product);
//                                try {
//                                    productBinder.writeBean(selectedProduct);
//                                } catch (ValidationException e) {
//                                    throw new RuntimeException(e);
//                                }
//                                productService.save(selectedProduct);
//                            }
//                        }
//                        Notification.show("Artikel : " + byProductCodeEqualCaseInsensitive.get().size() + " dezelfde artikelen gevonden en aangepast in de Database!");
//                    }
//                });
//        closeBtn.addThemeVariants(LUMO_TERTIARY_INLINE);
//
//        return closeBtn;
//    }
}
