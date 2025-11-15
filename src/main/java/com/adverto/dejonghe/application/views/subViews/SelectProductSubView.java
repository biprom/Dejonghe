package com.adverto.dejonghe.application.views.subViews;

import com.adverto.dejonghe.application.customEvents.AddProductEventListener;
import com.adverto.dejonghe.application.customEvents.AddRemoveProductEvent;
import com.adverto.dejonghe.application.customEvents.GetSelectedProductEvent;
import com.adverto.dejonghe.application.dbservices.*;
import com.adverto.dejonghe.application.entities.enums.employee.UserFunction;
import com.adverto.dejonghe.application.entities.enums.product.VAT;
import com.adverto.dejonghe.application.entities.product.product.*;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY_INLINE;

@org.springframework.stereotype.Component
@Scope("prototype")
public class SelectProductSubView extends VerticalLayout {
    ApplicationEventPublisher eventPublisher;
    ProductService productService;
    ProductLevel1Service productLevel1Service;
    ProductLevel2Service productLevel2Service;
    ProductLevel3Service productLevel3Service;
    ProductLevel4Service productLevel4Service;
    ProductLevel5Service productLevel5Service;
    ProductLevel6Service productLevel6Service;
    ProductLevel7Service productLevel7Service;
    AddProductEventListener listener;

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

    Notification checkDoubleProductInSelectedProductListNotification;
    Notification deleteProductNotification;

    UserFunction userFunction = UserFunction.ADMIN;
    Binder<Product> selectedProductBinder;

    //Grid.Column<Product> selectedProductDateColumn;
    Grid.Column<Product> selectedProductTotalPriceColumn;
    Grid.Column<Product> selectedProductUnitPriceColumn;
    Grid.Column<Product> selectedProductVATalPriceColumn;
    Grid.Column<Product> codeColumn;
    Grid.Column<Product> ehColumn;
    Grid.Column<Product> collectColumn;
    Grid.Column<Product> plusColumn;
    Grid.Column<Product> minusColumn;
    Grid.Column<Product> posColumn;


    Binder<Product> productBinder;

    Grid.Column<Product> dateColumn;
    Grid.Column<Product> productCodeColumn;
    Grid.Column<Product> productInternalNameColumn;
    Grid.Column<Product> productCommentColumn;
    Grid.Column<Product> productPositionColumn;
    Grid.Column<Product> productPurchageColumn;
    Grid.Column<Product> productMarginColumn;
    Grid.Column<Product> productSellColumn;
    Grid.Column<Product> productMinus1Column;
    Grid.Column<Product> productAmountColumn;
    Grid.Column<Product> productPlus1Column;
    Grid.Column<Product> productVColumn;
    Grid.Column<Product> productUnitColumn;

    DecimalFormat df = new DecimalFormat("0.00");

    Editor<Product> selectedProductEditor;
    Editor<Product> productEditor;

    Boolean canEditProduct = false;
    Integer selectedTeam = 0;
    LocalDate documentDate;

    Dialog attachementDialog;
    VerticalLayout attachementDialogLayout;


    public SelectProductSubView(ProductService productService,
                                ProductLevel1Service productLevel1Service,
                                ProductLevel2Service productLevel2Service,
                                ProductLevel3Service productLevel3Service,
                                ProductLevel4Service productLevel4Service,
                                ProductLevel5Service productLevel5Service,
                                ProductLevel6Service productLevel6Service,
                                ProductLevel7Service productLevel7Service,
                                ApplicationEventPublisher eventPublisher,
                                AddProductEventListener listener) {
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

        setUpSplitLayout();
        createReportError();
        setUpFilter();
        setUpFilterSelectedProduct();
        setUpProductGrid();
        setUpSelectedProductGrid();
        createReportErrorRemoveProduct();
        setUpAttachementDialog();
        splitLayout.addToPrimary(setUpGridLayoutButtons());
        splitLayout.addToSecondary(setUpHorizontalButtonSelectionAndGridbar());
        this.add(splitLayout);
        this.setMargin(false);
        this.setPadding(false);
        this.setSpacing(false);
        this.setHeightFull();
    }

    private void setUpAttachementDialog() {
        attachementDialog = new Dialog();
        attachementDialog.setHeaderTitle("Datum bijlage");

        VerticalLayout dialogLayout = createDialogLayout();
        attachementDialog.add(dialogLayout);

        Button saveButton = createSaveButton(attachementDialog);
        Button cancelButton = new Button("Niet toevoegen", e -> attachementDialog.close());
        attachementDialog.getFooter().add(cancelButton);
        attachementDialog.getFooter().add(saveButton);
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
            productGrid.setItems(filteredProductList);
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
            boolean checked = e.getValue();
            selectedProductList.forEach(item -> item.setBAttachement(checked));
            selectedProductGrid.getDataProvider().refreshAll();
            eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",null));
        });
        Button addToAttachementButton = new Button("Voeg toe");
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
                //deleteProductNotification.open();
                if(selectedProductList != null){
                    //when filter is selected
                    selectedProductList.remove(selectedProduct);
                    selectedProductGrid.getDataProvider().refreshAll();
                    eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product verwijderd",null));
                    //notification.close();
                }
                else{
                    //when filter is not touched
                    productService.delete(selectedProduct);
                    selectedProductGrid.getDataProvider().refreshAll();
                    eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product verwijderd",null));
                    //notification.close();
                }
            });
            return closeButton;
        }).setFlexGrow(0).setFrozen(true);
        dateColumn = selectedProductGrid.addComponentColumn(item -> {
            if(item.getDate() == null){
                item.setDate(LocalDate.now());
            }
            LocalDate current = item.getDate();

            // Vind de vorige item in de lijst
            int index = dataView.getItems().toList().indexOf(item);
            LocalDate prevDate = (index > 0) ? dataView.getItems().toList().get(index - 1).getDate() : null;

            if (prevDate != null && prevDate.equals(current)) {
                return new Span("");
            } else {
                Span label = new Span(current.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")).toString());
                label.getStyle().set("font-weight", "bold");
                return label;
            }
            //return item.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }).setHeader("Datum").setResizable(true);
        codeColumn = selectedProductGrid.addColumn(item -> {
            if(item.getProductCode() != null){
                return item.getProductCode();
            }
            else{
                return " ";
            }

        }).setHeader("Code").setWidth("10%").setResizable(true);

        collectColumn = selectedProductGrid.addComponentColumn(item -> {
            try{
                if((item.getBComment().equals(Boolean.TRUE)) || (item.getBWorkHour().equals(Boolean.TRUE)) || (item.getBTravel().equals(Boolean.TRUE))){
                    return new Span("");
                }
                else{
                    Checkbox checkbox = new Checkbox();
                    checkbox.addClickListener(event -> {
                        if (checkbox.getValue()) {
                            item.setBSelectedForAttachement(true);
                        } else {
                            item.setBSelectedForAttachement(false);
                        }
                    });
                    return checkbox;
                }
            }
            catch (Exception e){
                Checkbox checkbox = new Checkbox();
                checkbox.addClickListener(event -> {
                    if (checkbox.getValue()) {
                        item.setBSelectedForAttachement(true);
                    } else {
                        item.setBSelectedForAttachement(false);
                    }
                });
                return checkbox;
            }
            }).setAutoWidth(true).setFlexGrow(0).setFrozen(true).setHeader(attachementHlayout);

        Grid.Column<Product> productNameColumn = selectedProductGrid.addColumn(item -> item.getInternalName()).setHeader("Naam").setResizable(true).setAutoWidth(true).setFlexGrow(10);
        posColumn = selectedProductGrid.addColumn(item -> item.getPositionNumber()).setHeader("Pos.").setResizable(true).setAutoWidth(true).setFlexGrow(0).setFrozenToEnd(true);
        minusColumn = selectedProductGrid.addComponentColumn(item -> {
            Button minusButton = new Button(" - ");
            minusButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            minusButton.addClickListener(event -> {
                item.setSelectedAmount(item.getSelectedAmount() - 1);
                if((item.getSellPrice() != null)){
                    item.setTotalPrice(getTotalProductPrice(item.getSelectedAmount(), item.getSellPrice()));
                }
                else{
                    item.setTotalPrice(0.0);
                }
                selectedProductGrid.getDataProvider().refreshAll();
                setTotalsInFooter();

                //publish event so the received View can store the selected Workorder/Invoice...
                eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",null));

            });
            return minusButton;
        }).setHeader(" - 1 ").setAutoWidth(true).setFlexGrow(0).setFrozenToEnd(true);
        Grid.Column<Product> productSelectedAmountColumn = selectedProductGrid.addColumn(item -> item.getSelectedAmount()).setHeader("Aantal").setAutoWidth(true).setFlexGrow(0).setFrozenToEnd(true);
        plusColumn = selectedProductGrid.addComponentColumn(item -> {
            Button plusButton = new Button(" + ");
            plusButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            plusButton.addClickListener(event -> {
                item.setSelectedAmount(item.getSelectedAmount() + 1);
                if((item.getSellPrice() != null)){
                    item.setTotalPrice(getTotalProductPrice(item.getSelectedAmount(), item.getSellPrice()));
                }
                else{
                    item.setTotalPrice(0.0);
                }
                selectedProductGrid.getDataProvider().refreshAll();
                setTotalsInFooter();

                //publish event so the received View can store the selected Workorder/Invoice...
                eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",null));
            });
            return plusButton;
        }).setHeader(" + 1 ").setAutoWidth(true).setFlexGrow(0).setFrozenToEnd(true);
        selectedProductUnitPriceColumn = selectedProductGrid.addColumn(item -> {
            if(item.getSellPrice() != null){
                return df.format(item.getSellPrice());
            }
            else{
                return "";
            }
        }).setHeader("E/P").setAutoWidth(true).setFlexGrow(1).setFrozenToEnd(true).setTextAlign(ColumnTextAlign.END);

        selectedProductTotalPriceColumn = selectedProductGrid.addColumn(item -> {
            if ((item.getBComment() == null)|| (!item.getBComment())) {
                if(item.getTotalPrice() != null){
                    return String.valueOf(df.format(item.getTotalPrice()));
                }
                else{
                    if((item.getSelectedAmount() != null) && (item.getSellPrice() != null)){
                        item.setTotalPrice(getTotalProductPrice(item.getSelectedAmount(), item.getSellPrice()));
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

        ehColumn = selectedProductGrid.addColumn(Product::getUnit).setHeader(" EH ").setAutoWidth(true).setFlexGrow(0).setFrozenToEnd(true).setTextAlign(ColumnTextAlign.END);
        selectedProductGrid.addComponentColumn(item -> {
            Button newProductButton = new Button(new Icon(VaadinIcon.PLUS_CIRCLE_O));
            newProductButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            newProductButton.addClickListener(event -> {
                Product newProduct = new Product();
                newProduct.setDate(item.getDate());
                newProduct.setTeamNumber(selectedTeam);
                //newProduct.setSelectedAmount(0.0);
                //newProduct.setSellPrice(0.0);
                //newProduct.setTotalPrice(0.0);
                newProduct.setVat(VAT.EENENTWINTIG);
                newProduct.setBComment(item.getBComment());
                dataView.addItemAfter(newProduct, item);
                eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",null));
            });
            return newProductButton;
        }).setAutoWidth(true).setFlexGrow(0).setFrozen(true);

        collectColumn = selectedProductGrid.addComponentColumn(item -> {
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

        DatePicker productDatePicker = new DatePicker();
        productDatePicker.setWidthFull();
        addCloseHandler(productDatePicker, selectedProductEditor);
        selectedProductBinder.forField(productDatePicker)
                .asRequired("Gelieve een datum in te geven aub.")
                //.withStatusLabel(firstNameValidationMessage)
                .bind(Product::getDate, Product::setDate);
        dateColumn.setEditorComponent(productDatePicker);

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
                .bind(Product::getSellPrice, Product::setSellPrice);
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
            productToChange.setTotalPrice(getTotalProductPrice(productToChange.getSelectedAmount(),productToChange.getSellPrice()));
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

    private void setTotalsInFooter(){
        try{
            selectedProductTotalPriceColumn.setFooter(String.format("%s total ex BTW", df.format(selectedProductList.stream().mapToDouble(item -> item.getTotalPrice()).sum())));
        }
        catch(Exception e){
            selectedProductTotalPriceColumn.setFooter("N/A");
        }
    }

    private Double getTotalProductPrice(Double selectedAmount, Double sellPrice) {
        if((selectedAmount != null) && (sellPrice != null)) {
            return selectedAmount * sellPrice;
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

        productCodeColumn = productGrid.addColumn(item -> item.getProductCode()).setHeader("Code").setAutoWidth(true).setResizable(true);
        productPositionColumn = productGrid.addColumn(item -> item.getPositionNumber()).setComparator((o1, o2) -> {
            if((o1.getPositionNumber() != null) && (o2.getPositionNumber() != null)) {
                return compareOnderdeel(o1.getPositionNumber(), o2.getPositionNumber());
            }
            else{
                return -1;
            }

        }).setHeader("Pos.").setAutoWidth(true).setResizable(true);
        productInternalNameColumn = productGrid.addColumn(item -> item.getInternalName()).setHeader("Naam").setSortable(true)
                .setComparator((o1, o2) -> compareOnderdeel(o1.getInternalName(), o2.getInternalName())).setAutoWidth(true).setResizable(true);
        productCommentColumn = productGrid.addColumn(item -> item.getComment()).setHeader("Commentaar").setAutoWidth(true).setResizable(true);
        productPurchageColumn = productGrid.addColumn(item -> df.format(item.getPurchasePrice())).setHeader("Aankoopprijs").setAutoWidth(true).setResizable(true);
        productMarginColumn = productGrid.addColumn(item -> {
            try{
                return df.format(item.getSellMargin());
            }
            catch (Exception e){
                return "N/A";
            }
        }).setHeader("Marge").setAutoWidth(true).setResizable(true);
        productSellColumn = productGrid.addColumn(item -> df.format(item.getSellPrice())).setHeader("Verkoopsprijs").setAutoWidth(true).setResizable(true);

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
        productUnitColumn = productGrid.addColumn(Product::getUnit).setHeader("EH.").setAutoWidth(true).setFlexGrow(0).setFrozenToEnd(true);

        productVColumn = productGrid.addComponentColumn(item -> {
            Button plusButton = new Button("V");
            plusButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            plusButton.addClickListener(event -> {
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

                if((item.getSet() == null)  || (item.getSet() == false)){
                    //if product is not a set
                    if(item.getSellPrice() != null){
                        productToAdd.setSellPrice(item.getSellPrice());
                    }
                    else{
                        productToAdd.setSellPrice(0.0);
                    }
                    productToAdd.setTotalPrice(getTotalProductPrice(item.getSelectedAmount(), item.getSellPrice()));
                }
                else{
                    try{
                        productToAdd.setSellPrice(item.getSetList().stream().map(product -> product.getSelectedAmount() * product.getPurchasePrice()).reduce(0.0, Double::sum));
                        productToAdd.setTotalPrice(productToAdd.getSellPrice() * (1 + (productToAdd.getSellMargin()/100)));
                    }
                    catch (Exception e){
                        productToAdd.setSellPrice(0.0);
                        productToAdd.setTotalPrice(getTotalProductPrice(item.getSelectedAmount(), item.getSellPrice()));
                    }
                }


                //check if product allready is in selected List
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
                    if(optDoubleSelectedProduct.isPresent()){
                        doubleSelectedProduct = optDoubleSelectedProduct.get();
                        checkDoubleProductInSelectedProductListNotification.open();
                    }
                    else{
                        doubleSelectedProduct = null;
                        selectedProductList.add(productToAdd);
                        List<Product> filteredSelectedProductList = selectedProductList.stream().filter(product -> (product.getTeamNumber() == selectedTeam)
                        ).collect(Collectors.toList());
                        selectedProductGrid.setItems(filteredSelectedProductList);
                        setTotalsInFooter();
                    }
                    item.setSelectedAmount(0.0);
                    productGrid.getDataProvider().refreshItem(item);

                    //publish event so the received View can store the selected Workorder/Invoice...
                    eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product toegevoegd",item));

                }
                else{
                    Notification.show("Gelieve een geldig positief nummer in te geven!");
                }
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
            productToAdd.setTotalPrice(getTotalProductPrice(productToAdd.getSelectedAmount(), productToAdd.getSellPrice()));

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
                    productGrid.setItems(productByInternalNameOrComment.get());
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
                productGrid.setItems(allProductsByCategory.get());
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
                productGrid.setItems(allProductsByCategory.get());
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
                productGrid.setItems(allProductsByCategory.get());
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
                productGrid.setItems(allProductsByCategory.get());
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
                productGrid.setItems(allProductsByCategory.get());
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
                productGrid.setItems(allProductsByCategory.get());
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

    private Notification createReportError() {
        checkDoubleProductInSelectedProductListNotification = new Notification();
        checkDoubleProductInSelectedProductListNotification.addThemeVariants(NotificationVariant.LUMO_WARNING);

        Icon icon = VaadinIcon.WARNING.create();
        Button retryBtn = new Button("Voeg aantal bij bestaande geselecteerd artikel!",
                clickEvent -> {
                    doubleSelectedProduct.setSelectedAmount(doubleSelectedProduct.getSelectedAmount()+productToAdd.getSelectedAmount());
                    selectedProductGrid.getDataProvider().refreshAll();
                    checkDoubleProductInSelectedProductListNotification.close();
                });
        retryBtn.getStyle().setMargin("0 0 0 var(--lumo-space-l)");

        var layout = new HorizontalLayout(icon,
                new Text("Dit artikel is al geselecteerd in de lijst"), retryBtn);
        layout.setWidth("100%");
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

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
                        //when filter is selected
                        selectedProductList.remove(selectedProduct);
                        selectedProductGrid.getDataProvider().refreshAll();
                        eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product verwijderd",null));
                        notification.close();
                    }
                    else{
                        //when filter is not touched
                        productService.delete(selectedProduct);
                        selectedProductGrid.getDataProvider().refreshAll();
                        eventPublisher.publishEvent(new AddRemoveProductEvent(this, "Product verwijderd",null));
                        notification.close();
                    }
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
            dateColumn.setVisible(false);
            selectedProductTotalPriceColumn.setVisible(false);
            selectedProductUnitPriceColumn.setVisible(false);
            selectedProductVATalPriceColumn.setVisible(false);
            collectColumn.setVisible(false);

            productCodeColumn.setVisible(true);
            productCommentColumn.setVisible(true);
            productInternalNameColumn.setVisible(true);
            productPositionColumn.setVisible(true);
            productPurchageColumn.setVisible(false);
            productMarginColumn.setVisible(false);
            productSellColumn.setVisible(false);

            productMinus1Column.setVisible(true);
            productAmountColumn.setVisible(true);
            productPlus1Column.setVisible(true);
            productVColumn.setVisible(true);

            canEditProduct = false;

            plusColumn.setVisible(true);
            minusColumn.setVisible(true);
            ehColumn.setVisible(true);
            codeColumn.setVisible(true);
            posColumn.setVisible(true);

        } else if (userFunction.equals(UserFunction.WAREHOUSEWORKER)) {

            //selectedProductDateColumn.setVisible(false);
            dateColumn.setVisible(false);
            selectedProductTotalPriceColumn.setVisible(false);
            selectedProductUnitPriceColumn.setVisible(false);
            selectedProductVATalPriceColumn.setVisible(false);

            productCodeColumn.setVisible(true);
            productCommentColumn.setVisible(true);
            productInternalNameColumn.setVisible(true);
            productPositionColumn.setVisible(true);
            productPurchageColumn.setVisible(true);
            productMarginColumn.setVisible(true);
            productSellColumn.setVisible(true);
            collectColumn.setVisible(false);

            productMinus1Column.setVisible(false);
            productAmountColumn.setVisible(false);
            productPlus1Column.setVisible(false);
            productVColumn.setVisible(false);

            canEditProduct = true;

            plusColumn.setVisible(true);
            minusColumn.setVisible(true);
            ehColumn.setVisible(true);
            codeColumn.setVisible(true);
            posColumn.setVisible(true);

        } else{

            //selectedProductDateColumn.setVisible(true);
            dateColumn.setVisible(true);
            selectedProductTotalPriceColumn.setVisible(true);
            selectedProductUnitPriceColumn.setVisible(true);
            selectedProductVATalPriceColumn.setVisible(true);
            collectColumn.setVisible(true);

            productCodeColumn.setVisible(true);
            productCommentColumn.setVisible(true);
            productInternalNameColumn.setVisible(true);
            productPositionColumn.setVisible(true);
            productPurchageColumn.setVisible(false);
            productMarginColumn.setVisible(false);
            productSellColumn.setVisible(false);

            productMinus1Column.setVisible(true);
            productAmountColumn.setVisible(true);
            productPlus1Column.setVisible(true);
            productVColumn.setVisible(true);

            canEditProduct = false;

            plusColumn.setVisible(false);
            minusColumn.setVisible(false);
            ehColumn.setVisible(false);
            codeColumn.setVisible(false);
            posColumn.setVisible(false);
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
            productGrid.setItems(productList);
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
}
