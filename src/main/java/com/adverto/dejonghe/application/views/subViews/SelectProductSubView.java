package com.adverto.dejonghe.application.views.subViews;

import com.adverto.dejonghe.application.customEvents.AddProductEventListener;
import com.adverto.dejonghe.application.customEvents.AddRemoveProductEvent;
import com.adverto.dejonghe.application.customEvents.GetSelectedProductEvent;
import com.adverto.dejonghe.application.dbservices.*;
import com.adverto.dejonghe.application.entities.customers.Customer;
import com.adverto.dejonghe.application.entities.enums.employee.UserFunction;
import com.adverto.dejonghe.application.entities.enums.product.VAT;
import com.adverto.dejonghe.application.entities.product.product.*;
import com.adverto.dejonghe.application.services.product.ProductServices;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.theme.lumo.LumoIcon;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY_INLINE;

@org.springframework.stereotype.Component
@Scope("prototype")
public class SelectProductSubView extends VerticalLayout {
    ApplicationEventPublisher eventPublisher;
    ProductServices productServices;
    ProductService productService;
    ProductLevel1Service productLevel1Service;
    ProductLevel2Service productLevel2Service;
    ProductLevel3Service productLevel3Service;
    ProductLevel4Service productLevel4Service;
    ProductLevel5Service productLevel5Service;
    ProductLevel6Service productLevel6Service;
    ProductLevel7Service productLevel7Service;
    AddProductEventListener listener;
    SetView setView;
    ShowImageSubVieuw imageView;
    ShowPdfSubVieuw pdfView;
    ShowLinkSubVieuw linkView;
    AddCoupledProductSubView addCoupledProductSubView;

    FormLayout formLayout;
    FormLayout formLayoutLastSelectedLevel;
    SplitLayout splitLayout;

    ProductLevel1 selectedProductLevel1;
    ProductLevel2 selectedProductLevel2;
    ProductLevel3 selectedProductLevel3;
    ProductLevel4 selectedProductLevel4;
    ProductLevel5 selectedProductLevel5;
    ProductLevel6 selectedProductLevel6;
    ProductLevel7 selectedProductLevel7;

    Grid<Product>productGrid;
    TextField tfFilter;
    List<Product>productList;

    TextField searchProduct;
    Grid<Product>selectedProductGrid;
    TextField tfFilterSelectedProduct;
    List<Product>selectedProductList;
    GridListDataView<Product> dataView;
    Product selectedProduct;
    Product doubleSelectedProduct;
    Product productToAdd;

    Product draggedItem;
    Product selectedSet;
    Customer selectedCustomer;

    Notification checkDoubleProductInSelectedProductListNotification;
    Notification deleteProductNotification;

    UserFunction userFunction = UserFunction.ADMIN;
    Binder<Product> selectedProductBinder;

    private Dialog configureSetDialog;
    private Dialog showImageDialog;
    private Dialog pdfDialog;
    private Dialog linkDialog;
    private Dialog addCoupledProductDialog;

    List<Product>selectedSetList = new ArrayList<>();
    boolean productSelectedFromCoupledProductPopUP = false;

    //Grid.Column<Product> selectedProductDateColumn;
    Grid.Column<Product> selectedProductTotalPriceColumn;
    Grid.Column<Product> selectedProductPurchasePriceColumn;
    Grid.Column<Product> selectedProductUnitPriceColumn;
    Grid.Column<Product> selectedProductVATalPriceColumn;
    Grid.Column<Product> selectedProductGridCodeColumn;
    Grid.Column<Product> selectedProductGridEHColumn;
    Grid.Column<Product> selectedProductGridRemarkColumn;
    Grid.Column<Product> selectedProductGridCollectColumn;
    Grid.Column<Product> selectedProductGridPlusColumn;
    Grid.Column<Product> selectedProductGridMinusColumn;
    Grid.Column<Product> selectedProductGridPosColumn;
    Grid.Column<Product> linkColumn;
    Grid.Column<Product> imageColumn;
    Grid.Column<Product> pdfColumn;
    Grid.Column<Product> soColumn;


    Binder<Product> productBinder;

    Grid.Column<Product> selectedProductGridDateColumn;
    Grid.Column<Product> selectedProductGridDateColumnToShowOnInvoice;
    Grid.Column<Product> productCodeColumn;
    Grid.Column<Product> productInternalNameColumn;
    Grid.Column<Product> productCommentColumn;
    Grid.Column<Product> productPositionColumn;
    Grid.Column<Product> productPurchageColumn;
    Grid.Column<Product> productMarginColumn;
    Grid.Column<Product> productMarginIndustryColumn;
    Grid.Column<Product> productSellColumn;
    Grid.Column<Product> productSellIndustryColumn;
    Grid.Column<Product> productMinus1Column;
    Grid.Column<Product> productAmountColumn;
    Grid.Column<Product> productPlus1Column;
    Grid.Column<Product> productVColumn;
    Grid.Column<Product> productUnitColumn;

    //DecimalFormat df = new DecimalFormat("0.00");
    NumberFormat df = NumberFormat.getNumberInstance(new Locale("nl", "BE"));

    Editor<Product> selectedProductEditor;
    Editor<Product> productEditor;

    Boolean canEditProduct = false;
    Integer selectedTeam = 0;
    LocalDate documentDate;

    Dialog attachementDialog;
    VerticalLayout attachementDialogLayout;

    Boolean setBoldMode = false;
    MenuBar actionBar;


    public SelectProductSubView(ProductService productService,
                                ProductLevel1Service productLevel1Service,
                                ProductLevel2Service productLevel2Service,
                                ProductLevel3Service productLevel3Service,
                                ProductLevel4Service productLevel4Service,
                                ProductLevel5Service productLevel5Service,
                                ProductLevel6Service productLevel6Service,
                                ProductLevel7Service productLevel7Service,
                                ApplicationEventPublisher eventPublisher,
                                AddProductEventListener listener,
                                ShowImageSubVieuw imageView,
                                ShowPdfSubVieuw pdfView,
                                ShowLinkSubVieuw linkView,
                                ProductServices productServices,
                                AddCoupledProductSubView addCoupledProductSubView) {
        this.productService = productService;
        this.productLevel1Service = productLevel1Service;
        this.productLevel2Service = productLevel2Service;
        this.productLevel3Service = productLevel3Service;
        this.productLevel4Service = productLevel4Service;
        this.productLevel5Service = productLevel5Service;
        this.productLevel6Service = productLevel6Service;
        this.productLevel7Service = productLevel7Service;
        this.eventPublisher = eventPublisher;
        this.listener = listener;
        this.imageView = imageView;
        this.pdfView = pdfView;
        this.linkView = linkView;
        this.productServices = productServices;
        this.addCoupledProductSubView = addCoupledProductSubView;
        setUpNumberFormat();
        setUpLinkDialog();
        setUpImageDialog();
        setUpPdfDialog();
        setUpSplitLayout();
        createReportError();
        setUpFilter();
        setUpFilterSelectedProduct();
        setUpProductGrid();
        setUpSelectedProductGrid();
        createReportErrorRemoveProduct();
        setUpAttachementDialog();
        setUpConfigureSetDialog();
        setUpSharedProductDialog();
        splitLayout.addToPrimary(setUpGridLayoutButtons());
        splitLayout.addToSecondary(setUpHorizontalButtonSelectionAndGridbar());
        this.add(splitLayout);
        this.setMargin(false);
        this.setPadding(false);
        this.setSpacing(false);
        this.setHeightFull();
    }

    private void setUpNumberFormat() {
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);
        df.setGroupingUsed(true);
    }

    private void setUpSharedProductDialog() {
        addCoupledProductDialog = new Dialog();
        addCoupledProductDialog.setCloseOnEsc(true);
        addCoupledProductDialog.setWidth("60%");
        addCoupledProductDialog.setHeight("60%");
        addCoupledProductDialog.add(addCoupledProductSubView);
        Button cancelButton = new Button("Annuleer", e -> {
            addCoupledProductDialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button saveButton = new Button("Voeg toe", e -> {
            List<Product> productsToAdd = addCoupledProductSubView.getSelectedProdcutList();
            if((productsToAdd != null) && (productsToAdd.size() > 0)) {
                productSelectedFromCoupledProductPopUP = true;
                productsToAdd.stream().filter(item -> item.getSelectedAmount() > 0).forEach(item -> {addProductToSelectedProductList(item);});
                productSelectedFromCoupledProductPopUP = false;
            }
            addCoupledProductDialog.close();
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        addCoupledProductDialog.getFooter().getElement().getStyle()
                .set("width", "100%")
                .set("display", "flex")
                .set("justify-content", "space-between");
        addCoupledProductDialog.getFooter().add(cancelButton);
        addCoupledProductDialog.getFooter().add(saveButton);
    }

    private void setUpConfigureSetDialog() {
        configureSetDialog = new Dialog();
        configureSetDialog.setWidth("60%");
        configureSetDialog.setHeight("60%");
        //configureSetDialog.add(setView);
        configureSetDialog.addDialogCloseActionListener(event -> {
            configureSetDialog.close();
            //if selected product is a Set, try to refresh setParts param isSetPart
            //refreshSelectedProductAndSelectedSetLogoButton();
        });
    }

    private void setUpPdfDialog() {
        pdfDialog = new Dialog();
        pdfDialog.setCloseOnEsc(true);
        pdfDialog.setWidth("60%");
        pdfDialog.setHeight("60%");
        pdfDialog.add(pdfView);
        Button cancelButton = new Button("Sluiten", e -> {
            pdfDialog.close();
        });
        pdfDialog.getFooter().add(cancelButton);
    }

    private void setUpImageDialog() {
        showImageDialog = new Dialog();
        showImageDialog.setCloseOnEsc(true);
        showImageDialog.setWidth("60%");
        showImageDialog.setHeight("60%");
        showImageDialog.add(imageView);
        Button cancelButton = new Button("Sluiten", e -> {
            showImageDialog.close();
        });
        showImageDialog.getFooter().add(cancelButton);
    }

    private void setUpLinkDialog() {
        linkDialog = new Dialog();
        linkDialog.setCloseOnEsc(true);
        linkDialog.setWidth("60%");
        linkDialog.setHeight("60%");
        linkDialog.add(linkView);
        Button cancelButton = new Button("Sluiten", e -> {
            linkDialog.close();
        });
        linkDialog.getFooter().add(cancelButton);
    }

    private void setUpAttachementDialog() {
        attachementDialog = new Dialog();
        attachementDialog.setHeaderTitle("Datum bijlage");

        VerticalLayout dialogLayout = createDialogLayout();
        attachementDialog.add(dialogLayout);

        //Button saveButton = createSaveButton(attachementDialog);
        Button cancelButton = new Button("Annuleer", e -> attachementDialog.close());
        attachementDialog.getFooter().add(cancelButton);
        //attachementDialog.getFooter().add(saveButton);
    }

    private VerticalLayout createDialogLayout() {
        attachementDialogLayout = new VerticalLayout();
        attachementDialogLayout.setPadding(false);
        attachementDialogLayout.setSpacing(false);
        attachementDialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        attachementDialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");
        return attachementDialogLayout;
    }

    private Button createSaveButton(Dialog dialog) {
        Button saveButton = new Button("Toevoegen");
        saveButton.addClickListener(click -> {
            dialog.close();
//            try {
//                selectedInvoice.setCustomer(selectedCustomer);
//                invoiceBinder.writeBean(selectedInvoice);
//                selectedInvoice.setProductList(selectProductSubView.getSelectedProductList());
//                invoiceService.save(selectedInvoice);
//                Notification.show("Deze factuur is afgewerkt");
//            } catch (ValidationException e) {
//                Notification.show("Deze factuur kon niet worden afgewerkt.");
//            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return saveButton;
    }

    private void setUpSplitLayout() {
        splitLayout = new SplitLayout();
        splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(50);
    }

    private void setUpFilter() {
        tfFilter = new TextField();
        tfFilter.setWidth("100%");
        tfFilter.setPlaceholder("Filter via code, naam, commentaar");
        tfFilter.addValueChangeListener(event -> {
            List<Product> filteredProductList = productList.stream().filter(item -> ((item.getProductCode() != null)&& (item.getProductCode().toLowerCase().contains(tfFilter.getValue().toLowerCase())))
                    || ((item.getInternalName() != null) && (item.getInternalName().toLowerCase().contains(tfFilter.getValue().toLowerCase())))
                    || ((item.getComment() != null) && (item.getComment().toLowerCase().contains(tfFilter.getValue().toLowerCase())))).collect(Collectors.toList());
            addItemsToProductGrid(filteredProductList);
        });
    }

    private void setUpFilterSelectedProduct() {
        tfFilterSelectedProduct = new TextField();
        tfFilterSelectedProduct.setWidth("100%");
        tfFilterSelectedProduct.setPlaceholder("Filter via artikelcode, naam");
        tfFilterSelectedProduct.addValueChangeListener(event -> {
            List<Product> filteredSelectedProductList = selectedProductList.stream().filter(item -> ((item.getProductCode() != null )&&(item.getProductCode().toLowerCase().contains(tfFilterSelectedProduct.getValue().toLowerCase())))
                    || ((item.getInternalName() != null) && (item.getInternalName().toLowerCase().contains(tfFilterSelectedProduct.getValue().toLowerCase())))
                    //|| ((item.getComment() != null) && (item.getComment().toLowerCase().contains(tfFilterSelectedProduct.getValue().toLowerCase())))
                    ).collect(Collectors.toList());
            selectedProductGrid.setItems(filteredSelectedProductList);
        });
    }

    private void setUpSelectedProductGrid() {
        selectedProductGrid = new Grid<>();
        HorizontalLayout attachementHlayout = new HorizontalLayout();
        Checkbox selectAll = new Checkbox();
        selectAll.addValueChangeListener(e -> {
            selectedProductList.stream().filter(item -> (item.getBComment() == false) && (item.getBWorkHour() == false) && (item.getBTravel() == false)).forEach(item -> item.setBSelectedForAttachement(e.getValue()));
            selectedProductGrid.setItems(selectedProductList);
            eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",null));
        });
        Button addToAttachementButton = new Button(new Icon(VaadinIcon.FILE_O));
        addToAttachementButton.addClickListener(e -> {
            List<LocalDate> uniqueDates = selectedProductList.stream()
                    .map(Product::getDate)
                    .distinct()
                    .collect(Collectors.toList());
            attachementDialogLayout.removeAll();
            for (LocalDate uniqueDate : uniqueDates) {
                Button button = new Button(uniqueDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                button.addClickListener(click -> {
                    selectedProductList.stream().filter(product -> product.getBSelectedForAttachement() != null).filter(product -> product.getBSelectedForAttachement() == true).collect(Collectors.toList()).forEach(product -> {
                        product.setBSelectedForAttachement(false);
                        product.setAttachementNumber(uniqueDate);
                        product.setBAttachement(true);});
                    eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",null));
                    selectedProductGrid.getDataProvider().refreshAll();
                    attachementDialog.close();
                });
                attachementDialogLayout.add(button);
            }
            attachementDialog.open();
        });
        attachementHlayout.setAlignItems(Alignment.CENTER);
        attachementHlayout.add(selectAll, addToAttachementButton);

        selectedProductGrid.setRowsDraggable(true);
        selectedProductGrid.setAllRowsVisible(true);
        selectedProductGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        selectedProductGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        selectedProductGrid.addComponentColumn(item -> {
        Button closeButton = new Button(new Icon(VaadinIcon.TRASH));
        closeButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        closeButton.addClickListener(event -> {
            selectedProduct = item;
            deleteProductNotification.open();
        });
        return closeButton;
    }).setFlexGrow(0).setFrozen(true);

        selectedProductGridDateColumn = selectedProductGrid.addComponentColumn(item -> {
            if(item.getDate() == null){
                item.setDate(LocalDate.now());
            }
            LocalDate current = item.getDate();

            // Vind de vorige item in de lijst
            int index = dataView.getItems().toList().indexOf(item);
            LocalDate prevDate = (index > 0) ? dataView.getItems().toList().get(index - 1).getDate() : null;

            if (prevDate != null && prevDate.equals(current)) {
                if((item.getDateToShowOnInvoice() == null) || (item.getDateToShowOnInvoice().length() == 0)){
                    item.setDateToShowOnInvoice("");
                }
                return new Span("");
            } else {
                Span label = new Span(current.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")).toString());
                if((item.getDateToShowOnInvoice() == null) || (item.getDateToShowOnInvoice().length() == 0)){
                    item.setDateToShowOnInvoice(current.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")).toString());
                }
                label.getStyle().set("font-weight", "bold");
                return label;
            }
            //return item.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }).setHeader("Datum");

        selectedProductGridDateColumnToShowOnInvoice = selectedProductGrid.addColumn(item -> {
           if((item.getDateToShowOnInvoice() != null) && (item.getDateToShowOnInvoice().length() > 0)){
               return item.getDateToShowOnInvoice();
           }
           else{
               return "";
           }
        }).setHeader("Datum N").setResizable(true);

        selectedProductGridCodeColumn = selectedProductGrid.addColumn(item -> {
            if(item.getProductCode() != null){
                return item.getProductCode();
            }
            else{
                return " ";
            }

        }).setHeader("Code").setWidth("10%").setResizable(true);

        selectedProductGridCollectColumn = selectedProductGrid.addComponentColumn(item -> {
            try{
                if((item.getBComment().equals(Boolean.TRUE)) || (item.getBWorkHour().equals(Boolean.TRUE)) || (item.getBTravel().equals(Boolean.TRUE))){
                    return new Span("");
                }
                else{
                    Checkbox checkbox = new Checkbox();

                    if((item.getRemark() != null ) && (item.getRemark() == true)){
                        checkbox.addClassName("my-checkbox");
                    } else if ((item.getBAttachement() != null) && (item.getBAttachement() == true)) {
                        checkbox.addClassName("attached");
                    } else{
                        checkbox.addClassName("my-checkbox");
                    }
                    if(item.getBAttachement() != null && item.getBSelectedForAttachement() != null){
                        checkbox.setValue(item.getBAttachement()|| item.getBSelectedForAttachement());
                    }
                    checkbox.addClickListener(event -> {
                        if (checkbox.getValue()) {
                            item.setBSelectedForAttachement(true);
                        } else {
                            item.setBAttachement(false);
                            item.setBSelectedForAttachement(false);
                            selectedProductGrid.getDataProvider().refreshAll();
                            eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product verwijderd",null));
                        }
                    });
                    return checkbox;
                }
            }
            catch (Exception e){
                Checkbox checkbox = new Checkbox();
                if(item.getBAttachement() != null && item.getBSelectedForAttachement() != null){
                    checkbox.setValue(item.getBAttachement()|| item.getBSelectedForAttachement());
                }
                checkbox.addClickListener(event -> {
                    if (checkbox.getValue()) {
                        item.setBSelectedForAttachement(true);
                    } else {
                        item.setBAttachement(false);
                        item.setBSelectedForAttachement(false);
                        selectedProductGrid.getDataProvider().refreshAll();
                        eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product verwijderd",null));
                    }
                });
                return checkbox;
            }
            }).setAutoWidth(true).setFlexGrow(0).setFrozen(true).setHeader(attachementHlayout);

        Grid.Column<Product> productNameColumn = selectedProductGrid.addColumn(item -> item.getInternalName()).setHeader("Naam").setResizable(true).setAutoWidth(true).setFlexGrow(10);
        selectedProductGridPosColumn = selectedProductGrid.addColumn(item -> item.getPositionNumber()).setHeader("Pos.").setResizable(true).setAutoWidth(true).setFlexGrow(0).setFrozenToEnd(true);
        selectedProductGridMinusColumn = selectedProductGrid.addComponentColumn(item -> {
            Button minusButton = new Button(" - ");
            minusButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            minusButton.addClickListener(event -> {
                item.setSelectedAmount(item.getSelectedAmount() - 1);
                if((item.getSellPrice() != null)){
                    item.setTotalPrice(getTotalProductPrice(item));
                }
                else{
                    item.setTotalPrice(0.0);
                }
                selectedProductGrid.getDataProvider().refreshAll();
                setTotalsInFooter();

                //publish event so the received View can store the selected Workorder/Invoice...
                eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",item));
            });
            return minusButton;
        }).setHeader(" - 1 ").setAutoWidth(true).setFlexGrow(0).setFrozenToEnd(true);
        Grid.Column<Product> productSelectedAmountColumn = selectedProductGrid.addColumn(item -> item.getSelectedAmount()).setHeader("Aantal").setAutoWidth(true).setFlexGrow(0).setFrozenToEnd(true);
        selectedProductGridPlusColumn = selectedProductGrid.addComponentColumn(item -> {
            Button plusButton = new Button(" + ");
            plusButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            plusButton.addClickListener(event -> {
                item.setSelectedAmount(item.getSelectedAmount() + 1);
                if((item.getSellPrice() != null)){
                    item.setTotalPrice(getTotalProductPrice(item));
                }
                else{
                    item.setTotalPrice(0.0);
                }
                selectedProductGrid.getDataProvider().refreshAll();
                setTotalsInFooter();

                //publish event so the received View can store the selected Workorder/Invoice...
                eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",item));
            });
            return plusButton;
        }).setHeader(" + 1 ").setAutoWidth(true).setFlexGrow(0).setFrozenToEnd(true);

        selectedProductPurchasePriceColumn = selectedProductGrid.addColumn(item -> {
            if(item.getPurchasePrice() != null){
                return df.format(item.getPurchasePrice());
            }
            else{
                return "";
            }
        }).setHeader("Aankoop").setAutoWidth(true).setFlexGrow(1).setFrozenToEnd(true).setTextAlign(ColumnTextAlign.END);

        selectedProductUnitPriceColumn = selectedProductGrid.addColumn(item -> {
            try {
                if(item.getBComment() == true){
                    return "";
                }
                //if customer is selected
                if (selectedCustomer.getBAgro()) {
                    //if customer is agro
                    return df.format(item.getSellPrice());
                } else {
                    //if customer is industry
                    //if there is a industry price get this one
                    if ((item.getSellPriceIndustry() != null) && (item.getSellPriceIndustry() > 0.0)) {
                        return df.format(item.getSellPriceIndustry());
                    } else {
                        //else get the agro one
                        return df.format(item.getSellPrice());
                    }
                }
            }
            catch (Exception e){
                return "N/A";
            }
        }).setHeader("E/P").setAutoWidth(true).setFlexGrow(1).setFrozenToEnd(true).setTextAlign(ColumnTextAlign.END);


        selectedProductTotalPriceColumn = selectedProductGrid.addColumn(item -> {
            if ((item.getBComment() == null)|| (!item.getBComment())) {
                if(item.getTotalPrice() != null){
                    return String.valueOf(df.format(item.getTotalPrice()));
                }
                else{
                    if((item.getSelectedAmount() != null) && (item.getSellPrice() != null)){
                        item.setTotalPrice(getTotalProductPrice(item));
                        return df.format(item.getTotalPrice());
                    }
                    else{
                        item.setTotalPrice(0.0);
                        return "0.0";
                    }
                }
            }
            else{
                return "";
            }

        }).setHeader("Tot").setAutoWidth(true).setFlexGrow(1).setFrozenToEnd(true).setTextAlign(ColumnTextAlign.END);

        selectedProductVATalPriceColumn = selectedProductGrid.addColumn(item -> {
            if ((item.getBComment() == null)|| (!item.getBComment())) {
                if(item.getVat() != null){
                    return item.getVat().getDiscription();
                }
                else{
                    item.setVat(VAT.EENENTWINTIG);
                    return VAT.EENENTWINTIG.getDiscription();
                }
            }
            else{
                return "";
            }
        }).setHeader("BTW.").setAutoWidth(true).setFlexGrow(0).setFrozenToEnd(true).setTextAlign(ColumnTextAlign.END);

        selectedProductGridEHColumn = selectedProductGrid.addColumn(Product::getUnit).setHeader("EH").setAutoWidth(true).setFlexGrow(0).setFrozenToEnd(true).setTextAlign(ColumnTextAlign.END);
        selectedProductGrid.addComponentColumn(item -> {
           return getActionMenu(item);
        }).setAutoWidth(true).setFlexGrow(2).setFrozen(true);

        selectedProductGridRemarkColumn = selectedProductGrid.addComponentColumn(item -> {
            Checkbox checkbox = new Checkbox();
            if(item.getRemark() != null){
                checkbox.setValue(item.getRemark());
            }
            else{
                checkbox.setValue(false);
            }

            checkbox.addClickListener(event -> {
                if(checkbox.getValue()){
                    item.setRemark(true);
                    eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",null));
                    selectedProductGrid.getDataProvider().refreshAll();
                    Notification.show("Er is een remark aan dit artikel gekoppeld");
                }
                else{
                    item.setRemark(false);
                    eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",null));
                    selectedProductGrid.getDataProvider().refreshAll();
                    Notification.show("Er is geen remark aan dit artikel gekoppeld");
                }
            });
            return checkbox;
        }).setAutoWidth(true).setFlexGrow(0).setFrozen(true).setHeader("opm.");


        selectedProductGrid.setWidth("100%");

        selectedProductList = new ArrayList<>();
        dataView = selectedProductGrid.setItems(selectedProductList);

        selectedProductGrid.addDragStartListener(e -> {
            draggedItem = e.getDraggedItems().get(0);
            selectedProductGrid.setDropMode(GridDropMode.BETWEEN);
        });

        selectedProductGrid.addDropListener(e -> {
            Product targetProduct = e.getDropTargetItem().orElse(null);
            GridDropLocation dropLocation = e.getDropLocation();

            boolean productWasDroppedOntoItself = draggedItem
                    .equals(targetProduct);

            if (targetProduct == null || productWasDroppedOntoItself)
                return;

            dataView.removeItem(draggedItem);

            if (dropLocation == GridDropLocation.BELOW) {
                dataView.addItemAfter(draggedItem, targetProduct);
            } else {
                dataView.addItemBefore(draggedItem, targetProduct);
            }
            eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",null));
        });

        selectedProductGrid.addDragEndListener(e -> {
            draggedItem = null;
            selectedProductGrid.setDropMode(null);
        });

        selectedProductGrid.setPartNameGenerator(product -> {
            if((product.getRemark() != null ) && (product.getRemark() == true)){
                return "remark";
            } else if ((product.getBAttachement() != null) && (product.getBAttachement() == true)) {
                return "attachement";
            } else{
                return "null";
            }
        });

        selectedProductBinder = new Binder<>(Product.class);
        selectedProductEditor = selectedProductGrid.getEditor();
        selectedProductEditor.setBinder(selectedProductBinder);

        TextField tfDateToShowOnInvoice = new TextField();
        tfDateToShowOnInvoice.setWidthFull();
        addCloseHandler(tfDateToShowOnInvoice, selectedProductEditor);
        selectedProductBinder.forField(tfDateToShowOnInvoice)
                //.asRequired("Gelieve een datum in te geven aub.")
                //.withStatusLabel(firstNameValidationMessage)
                .bind(Product::getDateToShowOnInvoice, Product::setDateToShowOnInvoice);
        selectedProductGridDateColumnToShowOnInvoice.setEditorComponent(tfDateToShowOnInvoice);

        TextField productNameField = new TextField();
        productNameField.setWidthFull();
        addCloseHandler(productNameField, selectedProductEditor);
        selectedProductBinder.forField(productNameField)
                .withNullRepresentation("")
                .asRequired("Gelieve een omschrijving in te geven aub.")
                //.withStatusLabel(firstNameValidationMessage)
                .bind(Product::getInternalName, Product::setInternalName);
        productNameColumn.setEditorComponent(productNameField);

        TextField tfAmount = new TextField();
        tfAmount.setWidthFull();
        addCloseHandler(tfAmount, selectedProductEditor);
        selectedProductBinder.forField(tfAmount)
                .withNullRepresentation("0.0")
                .asRequired("Gelieve een aantal in te geven aub.")
                .withConverter(
                        new StringToDoubleConverter("Dit is geen decimaal getal"))
                .bind(Product::getSelectedAmount, Product::setSelectedAmount);
        productSelectedAmountColumn.setEditorComponent(tfAmount);

        TextField tfUnitPrice = new TextField();
        tfUnitPrice.setWidthFull();
        addCloseHandler(tfUnitPrice, selectedProductEditor);
        selectedProductBinder.forField(tfUnitPrice)
                        .asRequired("Gelieve een aantal in te geven aub.")
                        .withNullRepresentation("0.0")
                        .withConverter(
                        new StringToDoubleConverter("Dit is geen decimaal getal"))
                .bind(product -> {
                    //if customer is selected
                    if (selectedCustomer.getBAgro()) {
                        //if customer is agro
                        return product.getSellPrice();
                    } else {
                        //if customer is industry
                        //if there is a industry price get this one
                        if ((product.getSellPriceIndustry() != null) && (product.getSellPriceIndustry() > 0.0)) {
                            return product.getSellPriceIndustry();
                        } else {
                            //else get the agro one
                            return product.getSellPrice();
                        }
                    }
                }, (product,sellPrice) -> {
                    if (selectedCustomer.getBAgro()) {
                        product.setSellPrice(sellPrice);
                    }
                    else{
                        product.setSellPriceIndustry(sellPrice);
                    }
                });
        selectedProductUnitPriceColumn.setEditorComponent(tfUnitPrice);

        Select<VAT> sVAT = new Select();
        sVAT.setItems(VAT.values());
        sVAT.setItemLabelGenerator(vat -> vat.getDiscription());
        addCloseHandler(sVAT, selectedProductEditor);
        selectedProductBinder.forField(sVAT)
                .asRequired("Gelieve een BTW- tarief in te geven aub.")
                .bind(Product::getVat, Product::setVat);
        selectedProductVATalPriceColumn.setEditorComponent(sVAT);

        selectedProductBinder.addValueChangeListener(event -> {
            Product productToChange = selectedProductEditor.getItem();
            productToChange.setTotalPrice(getTotalProductPrice(productToChange));
            selectedProductGrid.getDataProvider().refreshItem(productToChange);
            setTotalsInFooter();
            //publish event so the received View can store the selected Workorder/Invoice...
            eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",null));
        });

        selectedProductGrid.addItemClickListener(e -> {
            selectedProductEditor.editItem(e.getItem());
            Component editorComponent = e.getColumn().getEditorComponent();
            if (editorComponent instanceof Focusable) {
                ((Focusable) editorComponent).focus();
            }
        });
    }

    private Component getActionMenu(Product item) {
        actionBar = new MenuBar();
        MenuItem actie = createIconItem(actionBar, VaadinIcon.PLUS_CIRCLE, "Voeg toe");
        actie.getSubMenu().addItem("Artikel", e -> addProduct(item));
        actie.getSubMenu().addItem("Commentaar", e -> addComment(item));
        return actionBar;
    }

    private void addProduct(Product item) {
        Product newProduct = new Product();
        newProduct.setDate(item.getDate());
        newProduct.setTeamNumber(selectedTeam);
        newProduct.setSelectedAmount(null);
        //newProduct.setSellPrice(0.0);
        //newProduct.setTotalPrice(0.0);
        newProduct.setVat(VAT.EENENTWINTIG);
        newProduct.setBComment(false);
        dataView.addItemAfter(newProduct, item);
        eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",null));

    }

    private MenuItem createIconItem(MenuBar menu, VaadinIcon iconName,
                                    String ariaLabel) {
        Icon icon = new Icon(iconName);
        MenuItem item = menu.addItem(icon);
        item.setAriaLabel(ariaLabel);

        return item;
    }


    private void addComment(Product item) {
        Product newProduct = new Product();
        newProduct.setDate(item.getDate());
        newProduct.setTeamNumber(selectedTeam);
        newProduct.setSelectedAmount(null);
        //newProduct.setSellPrice(0.0);
        //newProduct.setTotalPrice(0.0);
        newProduct.setVat(VAT.EENENTWINTIG);
        newProduct.setBComment(true);
        dataView.addItemAfter(newProduct, item);
        eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",null));
    }

    private void setTotalsInFooter(){
        try{
            selectedProductTotalPriceColumn.setFooter(String.format("%s total ex BTW", df.format(selectedProductList.stream().mapToDouble(item -> item.getTotalPrice()).sum())));
        }
        catch(Exception e){
            selectedProductTotalPriceColumn.setFooter("N/A");
        }
    }

    private Double getTotalProductPrice(Product productToRecalc) {
        if((selectedCustomer != null) && (productToRecalc.getBComment() == false)){
            if(selectedCustomer.getBAgro()){
                return productToRecalc.getSelectedAmount() * productToRecalc.getSellPrice();
            }
            else{
                if((productToRecalc.getSellPriceIndustry() != null) && (productToRecalc.getSellPriceIndustry() > 0.0)){
                    return productToRecalc.getSelectedAmount() * productToRecalc.getSellPriceIndustry();
                }
                else{
                    return productToRecalc.getSelectedAmount() * productToRecalc.getSellPrice();
                }
            }
        }
        else{
            return 0.0;
        }

    }

    private static void addCloseHandler(Component textField,
                                        Editor<Product> editor) {
        textField.getElement().addEventListener("keydown", e -> editor.cancel())
                .setFilter("event.code === 'Escape'");
    }

    private void setUpProductGrid() {
        productGrid = new Grid<>();
        productGrid.setWidth("100%");
        productGrid.setAllRowsVisible(true);
        productGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
//        Grid.Column<Product> sortColumn = productGrid.addColumn(item -> {
//            if(!item.getPositionNumber().isEmpty()){
//                try{
//                    return Integer.valueOf(item.getPositionNumber().split("[^0-9]")[0]);
//                }
//                catch (Exception e){
//                    return 4999;
//                }
//            }
//            else{
//                return 5000;
//            }
//        });
//        sortColumn.setAutoWidth(true);
//        sortColumn.setVisible(false);
//        productGrid.sort(List.of(new GridSortOrder<>(sortColumn, SortDirection.ASCENDING)));

        productCodeColumn = productGrid.addComponentColumn(item -> {
            if(item.getProductCode() != null){
                if(item.isBoldMode() == false){
                    return new Span(item.getProductCode());
                }
                else{
                    Span span = new Span(item.getProductCode());
                    span.getStyle().set("font-weight", "bold");
                    span.addClassName("boxed-text");
                    return span;
                }
            }
            else{
                return new Span("");
            }
        }).setHeader("Code").setAutoWidth(true).setResizable(true);
        imageColumn = productGrid.addComponentColumn(product -> {
            if((product.getImageList() != null) && (!product.getImageList().isEmpty())){
                Button imageButton = new Button(new Icon(VaadinIcon.CAMERA));
                imageButton.addClickListener(event -> {
                    imageView.setUser(UserFunction.TECHNICIAN);
                    imageView.setSelectedWorkOrder(product.getImageList());
                    imageView.setTitle(product.getProductCode() + " " + product.getInternalName());
                    showImageDialog.open();
                });
                return imageButton;
            }
            else{
                return new Span("");
            }
        }).setHeader(new Icon(VaadinIcon.CAMERA)).setAutoWidth(true).setFlexGrow(0);

        soColumn = productGrid.addComponentColumn(product -> {
            if((product.getSetElement() != null) && (product.getSetElement() == true)){
                Button elementButton = new Button("O");
                elementButton.addThemeVariants(ButtonVariant.LUMO_ICON);
                elementButton.addClickListener(event -> {
                    selectedSetList.clear();
                    Optional<List<Product>> allSets = productService.getAllSetsContaining(product);
                    if(!allSets.isEmpty()){
                        selectedSetList.addAll(allSets.get());
                        setView.setSetList(selectedSetList);
                        configureSetDialog.open();
                    }
                });
                return elementButton;
            }
            if ((product.getSet() != null ) && (product.getSet() == true)) {
                Button setButton = new Button("S");
                setButton.addThemeVariants(ButtonVariant.LUMO_ICON);
                setButton.addClickListener(event -> {
                    selectedProduct = product;
                    setView.setSelectedProductForSet(selectedProduct);
                    configureSetDialog.open();
                });
                return setButton;
            }
            else{
                return new Span("");
            }
        }).setHeader(new Span("S/0")).setAutoWidth(true).setFlexGrow(0);

        pdfColumn = productGrid.addComponentColumn(product -> {
            if((product.getPdfList() != null) && (!product.getPdfList().isEmpty())){
                Button pdfButton = new Button(new Icon(VaadinIcon.FILE_FONT));
                pdfButton.addClickListener(event -> {
                    pdfView.setUser(UserFunction.TECHNICIAN);
                    pdfView.setSelectedWorkOrder(product.getPdfList());
                    pdfView.setTitle(product.getProductCode() + " " + product.getInternalName());
                    pdfDialog.open();
                });
                return pdfButton;
            }
            else return new Span("");
        }).setHeader(new Icon(VaadinIcon.FILE_FONT)).setAutoWidth(true).setFlexGrow(0);

        linkColumn = productGrid.addComponentColumn(product -> {
            if((product.getLinkDocumentList() != null) && (!product.getLinkDocumentList().isEmpty())&& (product.getLinkDocumentList().stream().anyMatch(item -> item.getLink().length() > 0))){
                Button linkButton = new Button(new Icon(VaadinIcon.LINK));
                linkButton.addClickListener(event -> {
                    linkView.setSelectedWorkOrder(product.getLinkDocumentList());
                    linkView.setTitle(product.getProductCode() + " " + product.getInternalName());
                    linkDialog.open();
                });
                return linkButton;
            }
            else return new Span("");
        }).setHeader(new Icon(VaadinIcon.LINK)).setAutoWidth(true).setFlexGrow(0);
        productPositionColumn = productGrid.addColumn(item -> item.getPositionNumber()).setComparator((o1, o2) -> {
            if((o1.getPositionNumber() != null) && (o2.getPositionNumber() != null)) {
                return compareOnderdeel(o1.getPositionNumber(), o2.getPositionNumber());
            }
            else{
                return -1;
            }

        }).setHeader("Pos.").setAutoWidth(true).setResizable(true);
        productInternalNameColumn = productGrid.addComponentColumn(item -> {
                    if(item.getInternalName() != null){
                        if(item.isBoldMode() == false){
                            return new Span(item.getInternalName());
                        }
                        else{
                            Span span = new Span(item.getInternalName());
                            span.getStyle().set("font-weight", "bold");
                            span.addClassName("boxed-text");
                            return span;
                        }
                    }
                    else{
                        return new Span("");
                    }
                }).setHeader("Naam").setSortable(true)
                .setComparator((p1, p2) -> {
                    // 1️⃣ Boolean eerst (true bovenaan)
                    int boolCompare = Boolean.compare(
                            p2.isBoldMode(), // true eerst → p2 vs p1
                            p1.isBoldMode()
                    );

                    if (boolCompare != 0) {
                        return boolCompare;
                    }

                    // 2️⃣ Daarna alfabetisch sorteren
                    return compareOnderdeel(p1.getInternalName(), p2.getInternalName());
                }).setAutoWidth(true).setResizable(true);
        productCommentColumn = productGrid.addColumn(item -> item.getComment()).setHeader("Commentaar").setAutoWidth(true).setResizable(true);
        productPurchageColumn = productGrid.addColumn(item -> df.format(item.getPurchasePrice())).setHeader("Aankoopprijs").setAutoWidth(true).setResizable(true);
        productMarginColumn = productGrid.addColumn(item -> {
            try{
                return df.format(item.getSellMargin());
            }
            catch (Exception e){
                return "N/A";
            }
        }).setHeader("Marge A").setAutoWidth(true).setResizable(true);
        productSellColumn = productGrid.addColumn(item -> df.format(item.getSellPrice())).setHeader("Verkoopsprijs A").setAutoWidth(true).setResizable(true);

        productMarginIndustryColumn = productGrid.addColumn(item -> {
            try{
                return df.format(item.getSellMarginIndustry());
            }
            catch (Exception e){
                return "N/A";
            }
        }).setHeader("Marge I").setAutoWidth(true).setResizable(true);

        productSellIndustryColumn = productGrid.addColumn(item -> {
            if(item.getSellPriceIndustry() != null){
                return df.format(item.getSellPriceIndustry());
            }
            else{
                return "0.0";
            }

        }).setHeader("Verkoopsprijs I").setAutoWidth(true).setResizable(true);

        productGrid.sort(Collections.singletonList(
                new GridSortOrder<>(productInternalNameColumn, SortDirection.ASCENDING)
        ));

        productMinus1Column = productGrid.addComponentColumn(item -> {
            Button minusButton = new Button("-");
            minusButton.addThemeVariants(LUMO_TERTIARY_INLINE);
            minusButton.addClickListener(event -> {
                item.setSelectedAmount(item.getSelectedAmount() - 1);
                productGrid.getDataProvider().refreshItem(item);
            });
            return minusButton;
        }).setHeader(" - 1 ").setAutoWidth(true).setFlexGrow(0).setFrozenToEnd(true);

        productAmountColumn = productGrid.addComponentColumn(item -> {
            TextField tfAmount = new TextField();
            tfAmount.addThemeVariants(TextFieldVariant.LUMO_SMALL);
            tfAmount.setMaxWidth(4, Unit.PICAS);
            if(item.getSelectedAmount() != null){
                tfAmount.setValue(item.getSelectedAmount().toString());
            }
            else{
                item.setSelectedAmount(0.0);
                tfAmount.setValue("0.0");
            }
            tfAmount.addValueChangeListener(value -> {
                try{
                    item.setSelectedAmount(Double.parseDouble(value.getValue().toString()));
                    //todo add evetListener
                    eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",item));
                }
                catch (NumberFormatException e){
                    Notification.show("Kon de waarde niet herkennen");
                }
            });
            return tfAmount;
        }).setHeader("Aantal").setWidth("100px").setAutoWidth(true).setFlexGrow(0).setFrozenToEnd(true);
         productPlus1Column = productGrid.addComponentColumn(item -> {
            Button plusButton = new Button("+");
            plusButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            plusButton.addClickListener(event -> {
                item.setSelectedAmount(item.getSelectedAmount() + 1);
                productGrid.getDataProvider().refreshItem(item);
            });
            return plusButton;
        }).setHeader(" + 1 ").setAutoWidth(true).setFlexGrow(0).setFrozenToEnd(true);
        productUnitColumn = productGrid.addColumn(Product::getUnit).setHeader("EH").setAutoWidth(true).setFlexGrow(0).setFrozenToEnd(true);

        productVColumn = productGrid.addComponentColumn(item -> {
            Button plusButton = new Button("V");
            plusButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            plusButton.addClickListener(event -> {
                productSelectedFromCoupledProductPopUP = false;
                addProductToSelectedProductList(item);
                productSelectedFromCoupledProductPopUP = false;
            });
            return plusButton;
        }).setHeader("V").setAutoWidth(true).setFlexGrow(0).setFrozenToEnd(true);

        productBinder = new Binder<>(Product.class);
        productEditor = productGrid.getEditor();
        productEditor.setBinder(productBinder);

        TextField tfProductCode = new TextField();
        tfProductCode.setWidthFull();
        productBinder.forField(tfProductCode)
                .asRequired("Gelieve een artikelCode in te geven aub.")
                .bind(Product::getProductCode, Product::setProductCode);
        productCodeColumn.setEditorComponent(tfProductCode);

        TextField tfInternalName = new TextField();
        tfInternalName.setWidthFull();
        productBinder.forField(tfInternalName)
                .asRequired("Gelieve een interne omschrijving in te geven aub.")
                .bind(Product::getInternalName, Product::setInternalName);
        productInternalNameColumn.setEditorComponent(tfInternalName);

        TextField tfPosition = new TextField();
        tfPosition.setWidthFull();
        productBinder.forField(tfPosition)
                .asRequired("Gelieve een positienummer in te geven aub.")
                .bind(Product::getPositionNumber, Product::setPositionNumber);
        productPositionColumn.setEditorComponent(tfPosition);

        TextField tfComment = new TextField();
        tfComment.setWidthFull();
        productBinder.forField(tfComment)
                .asRequired("Gelieve commentaar in te geven aub.")
                .bind(Product::getComment, Product::setComment);
        productCommentColumn.setEditorComponent(tfComment);

        TextField tfPurchasePrice = new TextField();
        tfPurchasePrice.setWidthFull();
        productBinder.forField(tfPurchasePrice)
                .asRequired("Gelieve een aantal in te geven aub.")
                .withConverter(
                        new StringToDoubleConverter("Dit is geen decimaal getal"))
                .bind(Product::getPurchasePrice, Product::setPurchasePrice);
        productPurchageColumn.setEditorComponent(tfPurchasePrice);

        TextField tfMargin = new TextField();
        tfMargin.setWidthFull();
        productBinder.forField(tfMargin)
                .asRequired("Gelieve een aantal in te geven aub.")
                .withConverter(
                        new StringToDoubleConverter("Dit is geen decimaal getal"))
                .bind(Product::getSellMargin, Product::setSellMargin);
        productMarginColumn.setEditorComponent(tfMargin);

        TextField tfUnit = new TextField();
        tfUnit.setWidthFull();
        productBinder.forField(tfUnit)
                .asRequired("Gelieve commentaar in te geven aub.")
                .bind(Product::getUnit, Product::setUnit);
        productUnitColumn.setEditorComponent(tfUnit);

        productBinder.addValueChangeListener(event -> {
            Product productToChange = productEditor.getItem();
            saveChangedProductIfAllParametersAreOK(productToChange);
            productGrid.getDataProvider().refreshItem(productToChange);
            eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",selectedProduct));

        });

        productGrid.addItemClickListener(e -> {
            if(canEditProduct == true){
                eventPublisher.publishEvent(new GetSelectedProductEvent(this, e.getItem()));
                productEditor.editItem(e.getItem());
                Component editorComponent = e.getColumn().getEditorComponent();
                if (editorComponent instanceof Focusable) {
                    ((Focusable) editorComponent).focus();
                }
            }
        });
    }

    private void addProductToSelectedProductList(Product item) {
        productToAdd = new Product();
        productToAdd.setTeamNumber(selectedTeam);
        productToAdd.setId(item.getId());
        productToAdd.setProductCode(item.getProductCode());
        productToAdd.setInternalName(item.getInternalName());
        productToAdd.setPositionNumber(item.getPositionNumber());
        productToAdd.setUnit(item.getUnit());
        productToAdd.setSelectedAmount(item.getSelectedAmount());
        productToAdd.setPurchasePrice(item.getPurchasePrice());
        productToAdd.setProductLevel1(item.getProductLevel1());
        productToAdd.setProductLevel2(item.getProductLevel2());
        productToAdd.setProductLevel3(item.getProductLevel3());
        productToAdd.setProductLevel4(item.getProductLevel4());
        productToAdd.setProductLevel5(item.getProductLevel5());
        productToAdd.setProductLevel6(item.getProductLevel6());
        productToAdd.setProductLevel7(item.getProductLevel7());
        productToAdd.setTeamNumber(selectedTeam);
        productToAdd.setDate(documentDate);
        productToAdd.setSet(item.getSet());
        productToAdd.setSetElement(item.getSetElement());
        productToAdd.setImageList(item.getImageList());
        productToAdd.setSetList(item.getSetList());
        productToAdd.setPdfList(item.getPdfList());
        productToAdd.setLinkDocumentList(item.getLinkDocumentList());
        productToAdd.setPurchasePrice(item.getPurchasePrice());
        productToAdd.setSellPrice(item.getSellPrice());
        productToAdd.setSellMargin(item.getSellMargin());
        productToAdd.setSellPriceIndustry(item.getSellPriceIndustry());
        productToAdd.setSellMarginIndustry(item.getSellMarginIndustry());
        if(item.getProductCode().startsWith("WU-")){
            productToAdd.setBWorkHour(true);
        }

        //now calc the total price in fuction of (agro/industry/set)
        //if product is not a set
        if((item.getSet() == null)  || (item.getSet() == false)){
            if(selectedCustomer != null){
                //if customer is selected
                productToAdd.setTotalPrice(getTotalProductPrice(item));
            }
            else{
                //if no customer is selected -> when we fill sets with products
                //nothing needs to be done because sellPrice and sellPriceIndustry is already filled in
                //we don't care about the getTotalPrice because total is always calculated under!
            }
        }
        else{
            //if product to add is a set
            try{
                //productToAdd.setSellPrice(item.getSetList().stream().map(product -> product.getSelectedAmount() * product.getPurchasePrice() * product.getSellMargin()).reduce(0.0, Double::sum));
                //productToAdd.setSellPriceIndustry(item.getSetList().stream().map(product -> product.getSelectedAmount() * product.getPurchasePrice() * product.getSellMarginIndustry()).reduce(0.0, Double::sum));
                if(selectedCustomer != null){
                    productToAdd.setTotalPrice(getTotalProductPrice(productToAdd));
                }
            }
            catch (Exception e){
                productToAdd.setSellPrice(0.0);
                productToAdd.setTotalPrice(getTotalProductPrice(item));
            }
        }


        if(item.getSelectedAmount() > 0.0){

            Optional<Product>optDoubleSelectedProduct = selectedProductList.stream().filter(product -> {
                if(product.getId() != null){
                    if(product.getId().matches(productToAdd.getId())){
                        return true;
                    }
                    else{
                        return false;
                    }
                }
                return false;
            }).findFirst();

            if(productSelectedFromCoupledProductPopUP == false) {
                //Check for coupledProducts (products that are always selected together)
                Optional<List<Product>> coupledProducts = productServices.getCoupledProducts(productToAdd);
                if (item.getProductLevel1().getName().matches("Montagemateriaal")) {
                    if ((!coupledProducts.isEmpty()) && (coupledProducts.isPresent()) && (coupledProducts.get().size() > 0)) {
                        //checkDoubleProductInSelectedProductListNotification.close();
                        addCoupledProductSubView.setCoupledProducts(coupledProducts.get());
                        addCoupledProductSubView.setSelectedCustomer(selectedCustomer);
                        addCoupledProductSubView.setSelectedTeam(selectedTeam);
                        addCoupledProductSubView.setSelectedDocumentDate(documentDate);
                        addCoupledProductSubView.setSelectedProdcutList(selectedProductList);
                        addCoupledProductSubView.addSelectedProduct(productToAdd);
                        addCoupledProductSubView.allreadyInSelectedList(optDoubleSelectedProduct.isPresent());
                        addCoupledProductDialog.open();
                    }
                } else {
                    //Notification.show("Gelieve een geldig positief nummer in te geven!");
                }

                //check if product allready is in selected List only if there are no coupled products
                if (!((!coupledProducts.isEmpty()) && (coupledProducts.isPresent()) && (coupledProducts.get().size() > 0))) {
                    if (optDoubleSelectedProduct.isPresent()) {
                        doubleSelectedProduct = optDoubleSelectedProduct.get();
                        checkDoubleProductInSelectedProductListNotification.open();
                    } else {
                        doubleSelectedProduct = null;
                        selectedProductList.add(productToAdd);
                        List<Product> filteredSelectedProductList = selectedProductList.stream().filter(product -> (product.getTeamNumber() == selectedTeam)
                        ).collect(Collectors.toList());
                        selectedProductGrid.setItems(filteredSelectedProductList);
                        setTotalsInFooter();
                        //publish event so the received View can store the selected Workorder/Invoice...
                        eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd", productToAdd));
                    }
                    item.setSelectedAmount(0.0);
                    productGrid.getDataProvider().refreshItem(item);


                }
            }
            else{
                //save products selected from coupled product popup
                //if selected products contains these -> add them and recalculate
                if (optDoubleSelectedProduct.isPresent()) {
                    doubleSelectedProduct = optDoubleSelectedProduct.get();
                    doubleSelectedProduct.setSelectedAmount(doubleSelectedProduct.getSelectedAmount()+productToAdd.getSelectedAmount());
                    doubleSelectedProduct.setTotalPrice(doubleSelectedProduct.getTotalPrice()+productToAdd.getTotalPrice());
                    selectedProductGrid.getDataProvider().refreshAll();
                }
                else{
                    doubleSelectedProduct = null;
                    selectedProductList.add(productToAdd);
                }

                List<Product> filteredSelectedProductList = selectedProductList.stream().filter(product -> (product.getTeamNumber() == selectedTeam)
                ).collect(Collectors.toList());
                selectedProductGrid.setItems(filteredSelectedProductList);

                eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd", productToAdd));
            }
            item.setSelectedAmount(0.0);
            productGrid.getDataProvider().refreshItem(item);
        }
        else{
                Notification.show("Gelieve een geldig hoeveelheid in te vullen aub!");
            }
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



    private void saveChangedProductIfAllParametersAreOK(Product productToChange) {
        try{
            if((productToChange.getPurchasePrice() != null) && (productToChange.getSellMargin() != null)){
                productToChange.setSellPrice(productToChange.getPurchasePrice() * productToChange.getSellMargin());
                productService.save(productToChange);
            }
            else{
                Notification show = Notification.show("Gelieve de aankoopwaarde en de marge in te geven aub.");
                show.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        }
        catch (Exception e){
            Notification show = Notification.show("De verkoopprijs kon niet worden berekend!");
            show.addThemeVariants(NotificationVariant.LUMO_WARNING);
        }

    }

    public void addProductToSelectedProductsFromRemoteView(Product productToAddFromRemoteView){
            productToAdd = new Product();
            productToAdd.setTeamNumber(selectedTeam);
            productToAdd.setId(productToAddFromRemoteView.getId());
            productToAdd.setProductCode(productToAddFromRemoteView.getProductCode());
            productToAdd.setDate(documentDate);
            productToAdd.setOption(productToAddFromRemoteView.getOption());
            productToAdd.setInternalName(productToAddFromRemoteView.getInternalName());
            productToAdd.setComment(productToAddFromRemoteView.getInternalName());
            productToAdd.setPositionNumber(productToAddFromRemoteView.getPositionNumber());
            productToAdd.setUnit(productToAddFromRemoteView.getUnit());
            productToAdd.setSelectedAmount(productToAddFromRemoteView.getSelectedAmount());
            if(productToAdd.getSellPrice() != null){
                productToAdd.setSellPrice(productToAddFromRemoteView.getSellPrice());
            }
            else{
                productToAdd.setSellPrice(0.0);
            }
            productToAdd.setTotalPrice(getTotalProductPrice(productToAdd));

            //check if product allready is in selected List
            if(productToAdd.getSelectedAmount() > 0.0){
                Optional<Product>optDoubleSelectedProduct;
                try{
                    optDoubleSelectedProduct = selectedProductList.stream().filter(product -> product.getId().matches(productToAddFromRemoteView.getId())).findFirst();
                }
                catch(Exception e){
                    optDoubleSelectedProduct = Optional.empty();
                }

                if(optDoubleSelectedProduct.isPresent()){
                    doubleSelectedProduct = optDoubleSelectedProduct.get();
                    checkDoubleProductInSelectedProductListNotification.open();
                }
                else{
                    doubleSelectedProduct = null;
                    selectedProductList.add(productToAdd);
                    selectedProductGrid.getDataProvider().refreshAll();
                    setTotalsInFooter();
                }
            }
            else{
                Notification.show("Gelieve een geldig positief nummer in te geven!");
            }

    }

    private FormLayout setUpHorizontalButtonSelectionAndGridbar() {
        formLayoutLastSelectedLevel = new FormLayout();
        return formLayoutLastSelectedLevel;
    }

    private VerticalLayout setUpGridLayoutButtons() {
        VerticalLayout verticalLayout = new VerticalLayout();
        formLayout = new FormLayout();
        formLayout.setSizeFull();
        formLayout.setWidth("100%");
        addButtonsToHorizontalButtonLayoutLevel1(productLevel1Service.getAllProductLevel1());
        verticalLayout.add(addSearchProductField(), formLayout);
        return verticalLayout;
    }

    private Component addSearchProductField() {
        searchProduct = new TextField();
        searchProduct.setWidth("100%");
        searchProduct.setPlaceholder("Zoek product op code of naam of commentaar");
        searchProduct.addValueChangeListener(value -> {
            if((value != null) && (value.getValue().length() > 0)) {
                Optional<List<Product>> productByInternalNameOrComment = productService.getProductByInternalNameOrCodeOrComment(value.getValue(), value.getValue(), value.getValue());
                if(productByInternalNameOrComment.isPresent()){
                    productList = productByInternalNameOrComment.get();
                    addItemsToProductGrid(productByInternalNameOrComment.get());
                    productGrid.setAllRowsVisible(true);
                    formLayoutLastSelectedLevel.removeAll();
                    VerticalLayout verticalLayout = new VerticalLayout();
                    verticalLayout.setSizeFull();
                    verticalLayout.setSpacing(true);
                    verticalLayout.add(tfFilter,productGrid);
                    formLayoutLastSelectedLevel.add(verticalLayout);
                    formLayoutLastSelectedLevel.setColspan(verticalLayout,2);
                }
                Notification.show("Producten aan het zoeken.");
            }
        });
        return searchProduct;
    }

    private void addButtonsToHorizontalButtonLayoutLevel1(Optional<List<ProductLevel1>> allProductLevel1) {
        formLayout.removeAll();
        if (allProductLevel1.isPresent()) {
            Button returnButton = new Button(LumoIcon.ANGLE_LEFT.create());
            returnButton.addClickListener(x -> {
                getBackToLastLevel();
            });
            formLayout.add(returnButton);
            for(ProductLevel1 productLevel1 : allProductLevel1.get()){
                Button productLevel1Button = new Button(productLevel1.getName());
                productLevel1Button.addClickListener(e -> {
                    formLayout.getChildren().forEach(child -> child.getElement().getThemeList().clear());
                    productLevel1Button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                    searchProduct.clear();
                    selectedProductLevel1 = productLevel1;
                    selectedProductLevel2 = null;
                    selectedProductLevel3 = null;
                    selectedProductLevel4 = null;
                    selectedProductLevel5 = null;
                    selectedProductLevel6 = null;
                    selectedProductLevel7 = null;
                    addButtonsToVerticalButtonLayoutLevel2(productLevel2Service.getProductLevel2sFromPreviousLevels(productLevel1));
                });
                formLayout.add(productLevel1Button);
            }
        }
    }

    private void addButtonsToHorizontalButtonLayoutLevel2(Optional<List<ProductLevel2>> allProductLevel2, ProductLevel2 productLevel2ToSelect) {
        formLayout.removeAll();
        if (allProductLevel2.isPresent()) {
            Button returnButton = new Button(LumoIcon.ANGLE_LEFT.create());
            returnButton.addClickListener(x -> {
                getBackToLastLevel();
            });
            formLayout.add(returnButton);
            for(ProductLevel2 productLevel2 : allProductLevel2.get()){
                Button productLevel2Button = new Button(productLevel2.getName());
                productLevel2Button.addClickListener(e -> {
                    formLayout.getChildren().forEach(child -> child.getElement().getThemeList().clear());
                    productLevel2Button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                    searchProduct.clear();
                    selectedProductLevel2 = productLevel2;
                    selectedProductLevel3 = null;
                    selectedProductLevel4 = null;
                    selectedProductLevel5 = null;
                    selectedProductLevel6 = null;
                    selectedProductLevel7 = null;
                    addButtonsToVerticalButtonLayoutLevel3(productLevel3Service.getProductLevel3sFromPreviousLevels(productLevel2,selectedProductLevel1));
                });
                formLayout.add(productLevel2Button);
                if(productLevel2.equals(productLevel2ToSelect)){
                    selectedProductLevel2 = productLevel2;
                    productLevel2Button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                    addButtonsToVerticalButtonLayoutLevel3(productLevel3Service.getProductLevel3sFromPreviousLevels(productLevel2,selectedProductLevel1));
                }
            }
        }
    }

    private void addButtonsToHorizontalButtonLayoutLevel3(Optional<List<ProductLevel3>> allProductLevel3, ProductLevel3 productLevel3ToSelect) {
        formLayout.removeAll();
        if (allProductLevel3.isPresent()) {
            Button returnButton = new Button(LumoIcon.ANGLE_LEFT.create());
            returnButton.addClickListener(x -> {
                getBackToLastLevel();
            });
            formLayout.add(returnButton);
            for(ProductLevel3 productLevel3 : allProductLevel3.get()){
                Button productLevel3Button = new Button(productLevel3.getName());
                productLevel3Button.addClickListener(e -> {
                    formLayout.getChildren().forEach(child -> child.getElement().getThemeList().clear());
                    productLevel3Button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                    searchProduct.clear();
                    selectedProductLevel3 = productLevel3;
                    selectedProductLevel4 = null;
                    selectedProductLevel5 = null;
                    selectedProductLevel6 = null;
                    selectedProductLevel7 = null;
                    addButtonsToVerticalButtonLayoutLevel4(productLevel4Service.getProductLevel4ByPreviousLevelNames(productLevel3,selectedProductLevel2,selectedProductLevel1));
                });
                formLayout.add(productLevel3Button);
                if(productLevel3.equals(productLevel3ToSelect)){
                    selectedProductLevel3 = productLevel3;
                    productLevel3Button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                    addButtonsToVerticalButtonLayoutLevel4(productLevel4Service.getProductLevel4ByPreviousLevelNames(productLevel3,selectedProductLevel2,selectedProductLevel1));
                }
            }
        }
    }

    private void addButtonsToHorizontalButtonLayoutLevel4(Optional<List<ProductLevel4>> allProductLevel4, ProductLevel4 productLevel4ToSelect) {
        formLayout.removeAll();
        if (allProductLevel4.isPresent()) {
            Button returnButton = new Button(LumoIcon.ANGLE_LEFT.create());
            returnButton.addClickListener(x -> {
                getBackToLastLevel();
            });
            formLayout.add(returnButton);
            for(ProductLevel4 productLevel4 : allProductLevel4.get()){
                Button productLevel4Button = new Button(productLevel4.getName());
                productLevel4Button.addClickListener(e -> {
                    formLayout.getChildren().forEach(child -> child.getElement().getThemeList().clear());
                    productLevel4Button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                    searchProduct.clear();
                    selectedProductLevel4 = productLevel4;
                    selectedProductLevel5 = null;
                    selectedProductLevel6 = null;
                    selectedProductLevel7 = null;
                    addButtonsToVerticalButtonLayoutLevel5(productLevel5Service.getProductLevel5ByPreviousLevelNames(productLevel4,selectedProductLevel3,selectedProductLevel2,selectedProductLevel1));
                });
                formLayout.add(productLevel4Button);
                if(productLevel4.equals(productLevel4ToSelect)){
                    selectedProductLevel4 = productLevel4;
                    productLevel4Button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                    addButtonsToVerticalButtonLayoutLevel5(productLevel5Service.getProductLevel5ByPreviousLevelNames(productLevel4,selectedProductLevel3,selectedProductLevel2,selectedProductLevel1));
                }
            }
        }
    }

    private void addButtonsToHorizontalButtonLayoutLevel5(Optional<List<ProductLevel5>> allProductLevel5, ProductLevel5 productLevel5ToSelect) {
        formLayout.removeAll();
        if (allProductLevel5.isPresent()) {
            Button returnButton = new Button(LumoIcon.ANGLE_LEFT.create());
            returnButton.addClickListener(x -> {
                getBackToLastLevel();
            });
            formLayout.add(returnButton);
            for(ProductLevel5 productLevel5 : allProductLevel5.get()){
                Button productLevel5Button = new Button(productLevel5.getName());
                productLevel5Button.addClickListener(e -> {
                    formLayout.getChildren().forEach(child -> child.getElement().getThemeList().clear());
                    productLevel5Button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                    searchProduct.clear();
                    selectedProductLevel5 = productLevel5;
                    selectedProductLevel6 = null;
                    selectedProductLevel7 = null;
                    addButtonsToVerticalButtonLayoutLevel6(productLevel6Service.getProductLevel6ByPreviousLevelNames(productLevel5,selectedProductLevel4,selectedProductLevel3,selectedProductLevel2,selectedProductLevel1));
                });
                formLayout.add(productLevel5Button);
                if(productLevel5.equals(productLevel5ToSelect)){
                    selectedProductLevel5 = productLevel5;
                    productLevel5Button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                    addButtonsToVerticalButtonLayoutLevel6(productLevel6Service.getProductLevel6ByPreviousLevelNames(productLevel5,selectedProductLevel4,selectedProductLevel3,selectedProductLevel2,selectedProductLevel1));
                }
            }
        }
    }

    private void addButtonsToHorizontalButtonLayoutLevel6(Optional<List<ProductLevel6>> allProductLevel6, ProductLevel6 productLevel6ToSelect) {
        formLayout.removeAll();
        if (allProductLevel6.isPresent()) {
            Button returnButton = new Button(LumoIcon.ANGLE_LEFT.create());
            returnButton.addClickListener(x -> {
                getBackToLastLevel();
            });
            formLayout.add(returnButton);
            for(ProductLevel6 productLevel6 : allProductLevel6.get()){
                Button productLevel6Button = new Button(productLevel6.getName());
                productLevel6Button.addClickListener(e -> {
                    formLayout.getChildren().forEach(child -> child.getElement().getThemeList().clear());
                    productLevel6Button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                    searchProduct.clear();
                    selectedProductLevel6 = productLevel6;
                    selectedProductLevel7 = null;
                    addButtonsToVerticalButtonLayoutLevel7(productLevel7Service.getProductLevel7ByPreviousLevelNames(productLevel6,selectedProductLevel5,selectedProductLevel4,selectedProductLevel3,selectedProductLevel2,selectedProductLevel1));
                });
                formLayout.add(productLevel6Button);
                if(productLevel6.equals(productLevel6ToSelect)){
                    selectedProductLevel6 = productLevel6;
                    productLevel6Button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                    addButtonsToVerticalButtonLayoutLevel7(productLevel7Service.getProductLevel7ByPreviousLevelNames(productLevel6,selectedProductLevel5,selectedProductLevel4,selectedProductLevel3,selectedProductLevel2,selectedProductLevel1));
                }
            }
        }
    }

    private void getBackToLastLevel() {
        if(selectedProductLevel7 != null){
            formLayoutLastSelectedLevel.removeAll();
            addButtonsToHorizontalButtonLayoutLevel6(productLevel6Service.getProductLevel6ByPreviousLevelNames(selectedProductLevel5,selectedProductLevel4,selectedProductLevel3,selectedProductLevel2,selectedProductLevel1),selectedProductLevel6);
            selectedProductLevel7 = null;
        }
        else if(selectedProductLevel6 != null){
            formLayoutLastSelectedLevel.removeAll();
            addButtonsToHorizontalButtonLayoutLevel5(productLevel5Service.getProductLevel5ByPreviousLevelNames(selectedProductLevel4,selectedProductLevel3,selectedProductLevel2,selectedProductLevel1),selectedProductLevel5);
            selectedProductLevel6 = null;
        }
        else if(selectedProductLevel5 != null){
            formLayoutLastSelectedLevel.removeAll();
            addButtonsToHorizontalButtonLayoutLevel4(productLevel4Service.getProductLevel4ByPreviousLevelNames(selectedProductLevel3,selectedProductLevel2,selectedProductLevel1),selectedProductLevel4);
            selectedProductLevel5 = null;
        }
        else if(selectedProductLevel4 != null){
            formLayoutLastSelectedLevel.removeAll();
            addButtonsToHorizontalButtonLayoutLevel3(productLevel3Service.getProductLevel3sFromPreviousLevels(selectedProductLevel2,selectedProductLevel1),selectedProductLevel3);
            selectedProductLevel4 = null;
        }
        else if(selectedProductLevel3 != null){
            formLayoutLastSelectedLevel.removeAll();
            addButtonsToHorizontalButtonLayoutLevel2(productLevel2Service.getProductLevel2sFromPreviousLevels(selectedProductLevel1),selectedProductLevel2);
            selectedProductLevel3 = null;
        }
        else if(selectedProductLevel2 != null){
            formLayoutLastSelectedLevel.removeAll();
            addButtonsToHorizontalButtonLayoutLevel1(productLevel1Service.getAllProductLevel1());
            selectedProductLevel2 = null;
        }
    }

    private void addButtonsToVerticalButtonLayoutLevel2(Optional<List<ProductLevel2>> productLevel2sFromPreviousLevels) {
        formLayoutLastSelectedLevel.removeAll();
        if (productLevel2sFromPreviousLevels.isPresent()) {
            for(ProductLevel2 productLevel2 : productLevel2sFromPreviousLevels.get()){
                Button productLevel2Button = new Button(productLevel2.getName());
                productLevel2Button.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                productLevel2Button.addClickListener(e -> {
                    searchProduct.clear();
                    formLayoutLastSelectedLevel.removeAll();
                    addButtonsToHorizontalButtonLayoutLevel2(productLevel2sFromPreviousLevels, productLevel2);
                });
                formLayoutLastSelectedLevel.add(productLevel2Button);
            }
        }
        //else add products to Grid
        else{
            Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(selectedProductLevel1.getName());
            if(allProductsByCategory.isPresent()){
                productList = allProductsByCategory.get();
                addItemsToProductGrid(allProductsByCategory.get());
                formLayoutLastSelectedLevel.removeAll();
                VerticalLayout verticalLayout = new VerticalLayout();
                verticalLayout.setSizeFull();
                verticalLayout.setSpacing(true);
                verticalLayout.add(tfFilter,productGrid);
                formLayoutLastSelectedLevel.add(verticalLayout);
                //formLayoutLastSelectedLevel.setColspan(verticalLayout,2);
            }
            else{
                Notification.show("Geen materialen gevonden in dit level");
            }

        }
    }

    private void addButtonsToVerticalButtonLayoutLevel3(Optional<List<ProductLevel3>> productLevel3sFromPreviousLevels) {
        formLayoutLastSelectedLevel.removeAll();
        if (productLevel3sFromPreviousLevels.isPresent()) {
            for(ProductLevel3 productLevel3 : productLevel3sFromPreviousLevels.get()){
                Button productLevel3Button = new Button(productLevel3.getName());
                productLevel3Button.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                productLevel3Button.addClickListener(e -> {
                    searchProduct.clear();
                    formLayoutLastSelectedLevel.removeAll();
                    addButtonsToHorizontalButtonLayoutLevel3(productLevel3sFromPreviousLevels,productLevel3);
                });
                formLayoutLastSelectedLevel.add(productLevel3Button);
            }
        }
        //else add products to Grid
        else{
            Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(selectedProductLevel2.getName(), selectedProductLevel1.getName());
            if(allProductsByCategory.isPresent()){
                productList = allProductsByCategory.get();
                addItemsToProductGrid(allProductsByCategory.get());
                formLayoutLastSelectedLevel.removeAll();
                VerticalLayout verticalLayout = new VerticalLayout();
                verticalLayout.setWidth("100%");
                verticalLayout.setHeightFull();
                verticalLayout.setSpacing(true);
                verticalLayout.add(tfFilter,productGrid);
                formLayoutLastSelectedLevel.add(verticalLayout);
                formLayoutLastSelectedLevel.setColspan(verticalLayout,2);
            }
            else{
                Notification.show("Geen materialen gevonden in dit level");
            }

        }
    }

    private void addButtonsToVerticalButtonLayoutLevel4(Optional<List<ProductLevel4>> productLevel4sFromPreviousLevels) {
        formLayoutLastSelectedLevel.removeAll();
        if (productLevel4sFromPreviousLevels.isPresent()) {
            for(ProductLevel4 productLevel4 : productLevel4sFromPreviousLevels.get()){
                Button productLevel4Button = new Button(productLevel4.getName());
                productLevel4Button.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                productLevel4Button.addClickListener(e -> {
                    searchProduct.clear();
                    formLayoutLastSelectedLevel.removeAll();
                    addButtonsToHorizontalButtonLayoutLevel4(productLevel4sFromPreviousLevels,productLevel4);
                });
                formLayoutLastSelectedLevel.add(productLevel4Button);
            }
        }
        //else add products to Grid
        else{
            Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(selectedProductLevel3.getName(), selectedProductLevel2.getName(), selectedProductLevel1.getName());
            if(allProductsByCategory.isPresent()){
                productList = allProductsByCategory.get();
                addItemsToProductGrid(allProductsByCategory.get());
                formLayoutLastSelectedLevel.removeAll();
                VerticalLayout verticalLayout = new VerticalLayout();
                verticalLayout.setWidth("100%");
                verticalLayout.setHeightFull();
                verticalLayout.setSpacing(true);
                verticalLayout.add(tfFilter,productGrid);
                formLayoutLastSelectedLevel.add(verticalLayout);
                formLayoutLastSelectedLevel.setColspan(verticalLayout,2);
            }
            else{
                Notification.show("Geen materialen gevonden in dit level");
            }
        }
    }

    private void addButtonsToVerticalButtonLayoutLevel5(Optional<List<ProductLevel5>> productLevel5sFromPreviousLevels) {
        formLayoutLastSelectedLevel.removeAll();
        if (productLevel5sFromPreviousLevels.isPresent()) {
            for(ProductLevel5 productLevel5 : productLevel5sFromPreviousLevels.get()){
                Button productLevel5Button = new Button(productLevel5.getName());
                productLevel5Button.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                productLevel5Button.addClickListener(e -> {
                    searchProduct.clear();
                    formLayoutLastSelectedLevel.removeAll();
                     addButtonsToHorizontalButtonLayoutLevel5(productLevel5sFromPreviousLevels,productLevel5);
                });
                formLayoutLastSelectedLevel.add(productLevel5Button);
            }
        }
        //else add products to Grid
        else{
            Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(selectedProductLevel4.getName(), selectedProductLevel3.getName(), selectedProductLevel2.getName(), selectedProductLevel1.getName());
            if(allProductsByCategory.isPresent()){
                productList = allProductsByCategory.get();
                addItemsToProductGrid(allProductsByCategory.get());
                formLayoutLastSelectedLevel.removeAll();
                VerticalLayout verticalLayout = new VerticalLayout();
                verticalLayout.setWidth("100%");
                verticalLayout.setSpacing(true);
                verticalLayout.add(tfFilter,productGrid);
                formLayoutLastSelectedLevel.add(verticalLayout);
                formLayoutLastSelectedLevel.setColspan(verticalLayout,2);
            }
            else{
                Notification.show("Geen materialen gevonden in dit level");
            }

        }
    }

    private void addButtonsToVerticalButtonLayoutLevel6(Optional<List<ProductLevel6>> productLevel6sFromPreviousLevels) {
        formLayoutLastSelectedLevel.removeAll();
        if (productLevel6sFromPreviousLevels.isPresent()) {
            for(ProductLevel6 productLevel6 : productLevel6sFromPreviousLevels.get()){
                Button productLevel6Button = new Button(productLevel6.getName());
                productLevel6Button.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                productLevel6Button.addClickListener(e -> {
                    searchProduct.clear();
                    formLayoutLastSelectedLevel.removeAll();
                    addButtonsToHorizontalButtonLayoutLevel6(productLevel6sFromPreviousLevels,productLevel6);
                });
                formLayoutLastSelectedLevel.add(productLevel6Button);
            }
        }
        //else add products to Grid
        else{
            Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(selectedProductLevel5.getName(), selectedProductLevel4.getName(), selectedProductLevel3.getName(), selectedProductLevel2.getName(), selectedProductLevel1.getName());
            if(allProductsByCategory.isPresent()){
                productList = allProductsByCategory.get();
                addItemsToProductGrid(allProductsByCategory.get());
                formLayoutLastSelectedLevel.removeAll();
                VerticalLayout verticalLayout = new VerticalLayout();
                verticalLayout.setHeightFull();
                verticalLayout.setSpacing(true);;
                verticalLayout.add(tfFilter,productGrid);
                formLayoutLastSelectedLevel.add(verticalLayout);
                formLayoutLastSelectedLevel.setColspan(verticalLayout,2);
            }
            else{
                Notification.show("Geen materialen gevonden in dit level");
            }

        }
    }

    private void addButtonsToVerticalButtonLayoutLevel7(Optional<List<ProductLevel7>> productLevel7sFromPreviousLevels) {
        formLayoutLastSelectedLevel.removeAll();
        if (productLevel7sFromPreviousLevels.isPresent()) {
            for(ProductLevel7 productLevel7 : productLevel7sFromPreviousLevels.get()){
                Button productLevel7Button = new Button(productLevel7.getName());
                productLevel7Button.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                productLevel7Button.addClickListener(e -> {
                    searchProduct.clear();
                    Notification.show("Je zit in de eindlevel!");

                });
                formLayoutLastSelectedLevel.add(productLevel7Button);
            }
        }
        //else add products to Grid
        else{
            Optional<List<Product>> allProductsByCategory = productService.getAllProductsByCategory(selectedProductLevel6.getName(), selectedProductLevel5.getName(), selectedProductLevel4.getName(), selectedProductLevel3.getName(), selectedProductLevel2.getName(), selectedProductLevel1.getName());
            if(allProductsByCategory.isPresent()){
                productList = allProductsByCategory.get();
                addItemsToProductGrid(allProductsByCategory.get());
                formLayoutLastSelectedLevel.removeAll();
                VerticalLayout verticalLayout = new VerticalLayout();
                verticalLayout.setWidth("100%");
                verticalLayout.setHeightFull();
                verticalLayout.setSpacing(true);
                verticalLayout.add(tfFilter,productGrid);
                formLayoutLastSelectedLevel.add(verticalLayout);
                formLayoutLastSelectedLevel.setColspan(verticalLayout,2);
            }
            else{
                Notification.show("Geen materialen gevonden in dit level");
            }
        }
    }

    public VerticalLayout getLayout() {
        return this;
    }

    private Button createCloseBtn(Notification notification) {
        Button closeBtn = new Button(VaadinIcon.TRASH.create(),
                clickEvent -> {
                    notification.close();
                });
        closeBtn.addThemeVariants(LUMO_TERTIARY_INLINE);

        return closeBtn;
    }

    private Notification createReportError() {
        checkDoubleProductInSelectedProductListNotification = new Notification();
        checkDoubleProductInSelectedProductListNotification.addThemeVariants(NotificationVariant.LUMO_WARNING);
        checkDoubleProductInSelectedProductListNotification.setPosition(Notification.Position.TOP_CENTER);
        Icon icon = VaadinIcon.WARNING.create();
        Button retryBtn = new Button("Voeg aantal bij bestaande geselecteerd artikel!",
                clickEvent -> {
                    doubleSelectedProduct.setSelectedAmount(doubleSelectedProduct.getSelectedAmount()+productToAdd.getSelectedAmount());
                    doubleSelectedProduct.setTotalPrice(doubleSelectedProduct.getTotalPrice()+productToAdd.getTotalPrice());
                    selectedProductGrid.getDataProvider().refreshAll();
                    checkDoubleProductInSelectedProductListNotification.close();
                    eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",doubleSelectedProduct));
                });
        retryBtn.getStyle().setMargin("0 0 0 var(--lumo-space-l)");

        var layout = new HorizontalLayout(icon,
                new Text("Dit artikel is al geselecteerd in de lijst"), retryBtn);
        layout.setWidth("100%");
        //layout.setAlignItems(FlexComponent.Alignment.CENTER);

        checkDoubleProductInSelectedProductListNotification.add(layout);


        return checkDoubleProductInSelectedProductListNotification;
    }

//    public Button createCloseBtn(Notification notification) {
//        Button closeBtn = new Button("Voeg dit artikel er nog eens in!",
//                clickEvent -> {
//                    doubleSelectedProduct = null;
//                    selectedProductList.add(productToAdd);
//                    selectedProductGrid.getDataProvider().refreshAll();
//                    setTotalsInFooter();
//                    notification.close();
//                });
//        closeBtn.addThemeVariants(LUMO_TERTIARY_INLINE);
//        return closeBtn;
//    }

    private Notification createReportErrorRemoveProduct() {
        deleteProductNotification = new Notification();
        deleteProductNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);

        Icon icon = VaadinIcon.WARNING.create();
        Button retryBtn = new Button("Annuleer",
                clickEvent -> deleteProductNotification.close());
        retryBtn.getStyle().setMargin("0 0 0 var(--lumo-space-l)");

        var layout = new HorizontalLayout(icon,
                new Text("Ben je zeker dat je dit artikel wil wissen?"), retryBtn,
                createRemoveProductBtn(deleteProductNotification));
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        deleteProductNotification.add(layout);

        return deleteProductNotification;
    }

    public Button createRemoveProductBtn(Notification notification) {
        Button closeBtn = new Button(VaadinIcon.TRASH.create(),
                clickEvent -> {
                    if(selectedProductList != null){
                        selectedProductList.remove(selectedProduct);
                        selectedProductGrid.getDataProvider().refreshAll();
                        eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product verwijderd",null));

                        //check if delete event came from SetSubView -> so we need to check if product is in other sets
                        //if so the product needs to stay a setElement
                        if(selectedSet != null){
                            Optional<List<Product>> allSetsContaining = productService.getAllSetsContaining(selectedProduct);
                            if(!allSetsContaining.isEmpty()){
                                if(allSetsContaining.get().stream().anyMatch(set -> !(set.getId().matches(selectedSet.getId())))){
                                    //The selected product is still in other other sets
                                    //so the selected product isSetElement may remain
                                    selectedProduct.setSetElement(true);
                                    productService.save(selectedProduct);
                                    eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product verwijderd",null));
                                }
                                else{
                                    //The selected product is not in any set
                                    //so the selected product isSetElement need to be removed
                                    selectedProduct.setSetElement(false);
                                    productService.save(selectedProduct);
                                    eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product verwijderd",null));
                                }
                            }
                            else{
                                //Also here the selected product is not in any set
                                selectedProduct.setSetElement(false);
                                productService.save(selectedProduct);
                                eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product verwijderd",null));
                            }
                        }
                    }
                    else{
                        //when filter is not touched
                        productService.delete(selectedProduct);
                        selectedProductGrid.getDataProvider().refreshAll();
                        eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product verwijderd",null));
                        //notification.close();
                    }
                    deleteProductNotification.close();
                });
        closeBtn.addThemeVariants(LUMO_TERTIARY_INLINE);

        return closeBtn;
    }

    public TextField getFilter() {
        return tfFilterSelectedProduct;
    }

    public Grid getSelectedProductGrid() {
        return selectedProductGrid;
    }

    public List<Product> getSelectedProductList() {
        if(selectedProductList != null){
            return selectedProductList;
        }
        else{
            return new ArrayList<>();
        }
    }

    public void setSelectedSet(Product set){
        this.selectedSet = set;
    }

    public void setSelectedProductList(List<Product> selectedProductListFromDocument) {
        if(selectedProductList != null){
            selectedProductList = selectedProductListFromDocument;
            selectedProductGrid.setItems(selectedProductList);
            selectedProductGrid.getDataProvider().refreshAll();
        }
        else{
            selectedProductList = new ArrayList<>();
        }
    }

    public void setSplitPosition(Double splitterPosition) {
        splitLayout.setSplitterPosition(splitterPosition);
    }

    public void setUserFunctionAndDocumentDate(UserFunction userFunction, LocalDate documentDate) {
        this.userFunction = userFunction;
        this.documentDate = documentDate;
        if(userFunction.equals(userFunction.TECHNICIAN)){

            //selectedProductDateColumn.setVisible(false);
            selectedProductGridDateColumn.setVisible(false);
            selectedProductGridDateColumnToShowOnInvoice.setVisible(false);
            selectedProductTotalPriceColumn.setVisible(false);
            selectedProductUnitPriceColumn.setVisible(false);
            selectedProductPurchasePriceColumn.setVisible(false);
            selectedProductVATalPriceColumn.setVisible(false);
            selectedProductGridCollectColumn.setVisible(false);

            productCodeColumn.setVisible(true);
            productCommentColumn.setVisible(true);
            productInternalNameColumn.setVisible(true);
            productPositionColumn.setVisible(true);
            productPurchageColumn.setVisible(false);
            productMarginColumn.setVisible(false);
            productMarginIndustryColumn.setVisible(false);
            productSellColumn.setVisible(false);
            productSellIndustryColumn.setVisible(false);
            selectedProductGridRemarkColumn.setVisible(false);
            linkColumn.setVisible(false);

            productMinus1Column.setVisible(true);
            productAmountColumn.setVisible(true);
            productPlus1Column.setVisible(true);
            productVColumn.setVisible(true);

            canEditProduct = false;

            selectedProductGridPlusColumn.setVisible(true);
            selectedProductGridMinusColumn.setVisible(true);
            selectedProductGridEHColumn.setVisible(true);
            selectedProductGridCodeColumn.setVisible(true);
            selectedProductGridPosColumn.setVisible(true);

        } else if (userFunction.equals(UserFunction.WAREHOUSEWORKER)) {

            //selectedProductDateColumn.setVisible(false);
            selectedProductGridDateColumn.setVisible(false);
            selectedProductGridDateColumnToShowOnInvoice.setVisible(false);
            selectedProductTotalPriceColumn.setVisible(false);
            selectedProductUnitPriceColumn.setVisible(false);
            selectedProductPurchasePriceColumn.setVisible(false);
            selectedProductVATalPriceColumn.setVisible(false);

            productCodeColumn.setVisible(true);
            productCommentColumn.setVisible(true);
            productInternalNameColumn.setVisible(true);
            productPositionColumn.setVisible(true);
            productPurchageColumn.setVisible(true);
            productMarginColumn.setVisible(true);
            productMarginIndustryColumn.setVisible(true);
            productSellColumn.setVisible(true);
            productSellIndustryColumn.setVisible(true);
            selectedProductGridCollectColumn.setVisible(false);
            selectedProductGridRemarkColumn.setVisible(false);

            productMinus1Column.setVisible(false);
            productAmountColumn.setVisible(false);
            productPlus1Column.setVisible(false);
            productVColumn.setVisible(false);

            canEditProduct = true;

            selectedProductGridPlusColumn.setVisible(true);
            selectedProductGridMinusColumn.setVisible(true);
            selectedProductGridEHColumn.setVisible(true);
            selectedProductGridCodeColumn.setVisible(true);
            selectedProductGridPosColumn.setVisible(true);
            linkColumn.setVisible(true);

        }
        else if (userFunction.equals(UserFunction.MAKE_SETS)) {

            //selectedProductDateColumn.setVisible(false);
            selectedProductGridDateColumn.setVisible(false);
            selectedProductGridDateColumnToShowOnInvoice.setVisible(false);
            selectedProductTotalPriceColumn.setVisible(true);
            selectedProductUnitPriceColumn.setVisible(false);
            selectedProductVATalPriceColumn.setVisible(false);

            selectedProductPurchasePriceColumn.setVisible(true);

            productCodeColumn.setVisible(true);
            productCommentColumn.setVisible(false);
            productInternalNameColumn.setVisible(false);
            productPositionColumn.setVisible(false);
            productPurchageColumn.setVisible(true);
            productMarginColumn.setVisible(true);
            productMarginIndustryColumn.setVisible(true);
            productSellColumn.setVisible(true);
            productSellIndustryColumn.setVisible(true);
            selectedProductGridCollectColumn.setVisible(false);
            selectedProductGridRemarkColumn.setVisible(false);

            productMinus1Column.setVisible(true);
            productAmountColumn.setVisible(true);
            productPlus1Column.setVisible(true);
            productVColumn.setVisible(true);

            canEditProduct = false;

            selectedProductGridPlusColumn.setVisible(true);
            selectedProductGridMinusColumn.setVisible(true);
            selectedProductGridEHColumn.setVisible(true);
            selectedProductGridCodeColumn.setVisible(true);
            selectedProductGridPosColumn.setVisible(false);
            linkColumn.setVisible(false);
        }

        else{

            //selectedProductDateColumn.setVisible(true);
            selectedProductGridDateColumn.setVisible(false);
            selectedProductGridDateColumnToShowOnInvoice.setVisible(true);
            selectedProductTotalPriceColumn.setVisible(true);
            selectedProductUnitPriceColumn.setVisible(true);
            selectedProductPurchasePriceColumn.setVisible(false);
            selectedProductVATalPriceColumn.setVisible(true);
            selectedProductGridCollectColumn.setVisible(true);

            productCodeColumn.setVisible(true);
            productCommentColumn.setVisible(true);
            productInternalNameColumn.setVisible(true);
            productPositionColumn.setVisible(true);
            productPurchageColumn.setVisible(false);
            productMarginColumn.setVisible(false);
            productMarginIndustryColumn.setVisible(false);
            productSellColumn.setVisible(false);
            productSellIndustryColumn.setVisible(false);

            selectedProductGridRemarkColumn.setVisible(true);
            productMinus1Column.setVisible(true);
            productAmountColumn.setVisible(true);
            productPlus1Column.setVisible(true);
            productVColumn.setVisible(true);

            canEditProduct = false;

            selectedProductGridPlusColumn.setVisible(false);
            selectedProductGridMinusColumn.setVisible(false);
            selectedProductGridEHColumn.setVisible(false);
            selectedProductGridCodeColumn.setVisible(false);
            selectedProductGridPosColumn.setVisible(false);
            linkColumn.setVisible(true);
        }
    }

    public Integer getSelectedTeam() {
        return selectedTeam;
    }

    public void setSelectedTeam(Integer selectedTeam) {
        this.selectedTeam = selectedTeam;
        List<Product> filteredSelectedProductList = selectedProductList.stream().filter(item -> (item.getTeamNumber() == selectedTeam)
        ).collect(Collectors.toList());
        selectedProductGrid.setItems(filteredSelectedProductList);
    }

    public ProductLevel1 getSelectedProductLevel1() {
        return selectedProductLevel1;
    }

    public ProductLevel2 getSelectedProductLevel2() {
        return selectedProductLevel2;
    }

    public ProductLevel3 getSelectedProductLevel3() {
        return selectedProductLevel3;
    }

    public ProductLevel4 getSelectedProductLevel4() {
        return selectedProductLevel4;
    }

    public ProductLevel5 getSelectedProductLevel5() {
        return selectedProductLevel5;
    }

    public ProductLevel6 getSelectedProductLevel6() {
        return selectedProductLevel6;
    }

    public ProductLevel7 getSelectedProductLevel7() {
        return selectedProductLevel7;
    }

    public void setItemsToProductGridToEditSet(Product selectedSet) {

        productList = productService.findProductsByLevels(
                selectedSet.getProductLevel1(),
                selectedSet.getProductLevel2(),
                selectedSet.getProductLevel3(),
                selectedSet.getProductLevel4(),
                selectedSet.getProductLevel5(),
                selectedSet.getProductLevel6(),
                selectedSet.getProductLevel7());
        if(productList != null){
            addItemsToProductGrid(productList);
            productGrid.setAllRowsVisible(true);
            formLayoutLastSelectedLevel.removeAll();
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSizeFull();
            verticalLayout.setSpacing(true);
            verticalLayout.add(tfFilter,productGrid);
            formLayoutLastSelectedLevel.add(verticalLayout);
            formLayoutLastSelectedLevel.setColspan(verticalLayout,2);
        }

        //set grid full height
        setSplitPosition(0.0);
    }

    public void addItemsToProductGrid(List<Product> productList){
        //if multiple products are from folder 'montagemateriaal' + something else -> set those of 'montagemateriaal' bold
        List<Product> montagemateriaal = productList.stream().filter(item -> item.getProductLevel1().getName().matches("Montagemateriaal")).collect(Collectors.toList());
        if(montagemateriaal.size() < productList.size()){
            montagemateriaal.forEach(item -> item.setBoldMode(true));
        }

        productGrid.setItems(productList);
        if(userFunction.equals(userFunction.TECHNICIAN)){
            Boolean showImageColumn = productList.stream().anyMatch(product -> product.getImageList()!= null && !product.getImageList().isEmpty());
            imageColumn.setVisible(showImageColumn);
            Boolean showPdfColumn = productList.stream().anyMatch(product -> product.getPdfList()!= null && !product.getImageList().isEmpty());
            pdfColumn.setVisible(showPdfColumn);
            Boolean showSOColumn = productList.stream().anyMatch(product -> ((product.getSet() != null) && (product.getSet())) || ((product.getSetElement() != null) && (product.getSetElement())));
            soColumn.setVisible(showSOColumn);
        }
        else{
            imageColumn.setVisible(true);
            pdfColumn.setVisible(true);
            soColumn.setVisible(true);
        }
    }

    public void setSelectedCustmer(Customer custmer){
        this.selectedCustomer = custmer;
    }
}
