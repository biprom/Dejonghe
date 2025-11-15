package com.adverto.dejonghe.application.views.articles;

import com.adverto.dejonghe.application.customEvents.AddProductEventListener;
import com.adverto.dejonghe.application.dbservices.*;
import com.adverto.dejonghe.application.entities.enums.employee.UserFunction;
import com.adverto.dejonghe.application.entities.product.product.Product;
import com.adverto.dejonghe.application.views.subViews.SelectProductSubView;
import com.adverto.dejonghe.application.views.subViews.SetView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.router.*;
import jakarta.annotation.PostConstruct;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@PageTitle("Producten")
@Route("product")
@Menu(order = 0, icon = LineAwesomeIconUrl.COG_SOLID)
public class ArticleView extends Div implements BeforeEnterObserver {
    SelectProductSubView selectProductSubView;
    ProductService productService;
    AddProductEventListener listener;

    SupplierService supplierService;

    SplitLayout mainSplitLayout;
    SplitLayout secondarySplitLayout;
    Grid<Product> setGrid = new Grid<>();
    HeaderRow headerRow;
    Editor<Product> editor = setGrid.getEditor();
    Binder<Product> binder;

    ConfirmDialog dialog;

    List<Product>setProducts;
    Product selectedSet;

    Grid.Column setNameColumn;
    Grid.Column setSellColumn;
    Grid.Column setCommentColumn;
    Grid.Column setPurchaseColumn;
    Grid.Column setMarginColumn;

    TextField tfEditName;
    TextField tfEditDescription;
    TextField tfEditPurchasePrice;
    TextField tfEditMargin;
    TextField tfEditPrice;

    TextField tfFiltername;
    TextField tfFilterDescription;

    Span groupSpan = new Span();

    public ArticleView(ProductService productService,
                       SelectProductSubView selectProductSubView,
                       AddProductEventListener listener,
                       SupplierService supplierService) {

        this.productService = productService;
        this.selectProductSubView = selectProductSubView;
        this.listener = listener;
        this.supplierService = supplierService;

        selectProductSubView.setUserFunctionAndDocumentDate(UserFunction.ADMIN, LocalDate.now());
        setUpConfirmDialog();
        setUpFilters();
        setUpSplitLayouts();
        groupSpan.getStyle()
                .set("color", "blue")
                .set("font-weight", "bold");
        mainSplitLayout.addToPrimary(selectProductSubView.getLayout());
        secondarySplitLayout.addToPrimary(new VerticalLayout(getProductDetail(),groupSpan));
        secondarySplitLayout.addToSecondary(selectProductSubView.getSelectedProductGrid());
        mainSplitLayout.addToSecondary(secondarySplitLayout);
        this.setHeightFull();
        add(mainSplitLayout);
        setUpBinder();
    }

    private void setUpConfirmDialog() {
        dialog = new ConfirmDialog();
        dialog.setHeader("Neem subfolders over?");
        dialog.setText(
                "Het geselecteerde set neemt de geselecteerde subfolders over van de linker kant. Is dit OK?");
        dialog.setCancelable(true);
        dialog.addCancelListener(event -> dialog.close());

        dialog.setConfirmText("Save");
        dialog.addConfirmListener(event -> {
            selectedSet.setProductLevel1(selectProductSubView.getSelectedProductLevel1());
            selectedSet.setProductLevel2(selectProductSubView.getSelectedProductLevel2());
            selectedSet.setProductLevel3(selectProductSubView.getSelectedProductLevel3());
            selectedSet.setProductLevel4(selectProductSubView.getSelectedProductLevel4());
            selectedSet.setProductLevel5(selectProductSubView.getSelectedProductLevel5());
            selectedSet.setProductLevel6(selectProductSubView.getSelectedProductLevel6());
            selectedSet.setProductLevel7(selectProductSubView.getSelectedProductLevel7());
            productService.save(selectedSet);
            groupSpan.setText(getGroupFromSelectedSet(selectedSet));
            Notification.show("Set is bewaard.");
        });
    }

    private void setUpFilters() {
        tfFilterDescription = new TextField();
        tfFilterDescription.setWidthFull();
        tfFiltername = new TextField();
        tfFiltername.setWidthFull();

        tfFiltername.addValueChangeListener(event -> {
            setGrid.setItems(setProducts.stream().filter(item -> item.getInternalName().toLowerCase().contains(tfFiltername.getValue().toLowerCase())).collect(Collectors.toSet()));
        });

        tfFilterDescription.addValueChangeListener(event -> {
            setGrid.setItems(setProducts.stream().filter(item -> item.getComment().toLowerCase().contains(tfFilterDescription.getValue().toLowerCase())).collect(Collectors.toSet()));
        });
    }

    private void setUpBinder() {

        binder = new Binder<>(Product.class);
        editor.setBinder(binder);
        editor.setBuffered(false);

        tfEditName = new TextField();
        tfEditName.setWidthFull();

        tfEditDescription = new TextField();
        tfEditDescription.setWidthFull();

        tfEditPurchasePrice = new TextField();
        tfEditPurchasePrice.setWidthFull();
        tfEditPurchasePrice.addValueChangeListener(event -> {
            try{
                selectedSet.setSellPrice(Double.parseDouble(tfEditPurchasePrice.getValue()) * (1 + (Double.parseDouble(tfEditMargin.getValue())/100)));
                setGrid.getDataProvider().refreshAll();
            }
            catch (NumberFormatException e){

            }
        });

        tfEditMargin = new TextField();
        tfEditMargin.setWidthFull();
        tfEditMargin.addValueChangeListener(event -> {
           try{
               selectedSet.setSellPrice(Double.parseDouble(tfEditPurchasePrice.getValue()) * (1 + (Double.parseDouble(tfEditMargin.getValue())/100)));
               setGrid.getDataProvider().refreshAll();
           }
           catch (NumberFormatException e){

           }
        });

        tfEditPrice = new TextField();
        tfEditPrice.setWidthFull();

        binder.forField(tfEditName)
                .asRequired("Mag niet leeg zijn")
                .withNullRepresentation("")
                .bind(Product::getInternalName, Product::setInternalName);
        setNameColumn.setEditorComponent(tfEditName);

        binder.forField(tfEditDescription)
                .asRequired("Mag niet leeg zijn")
                .withNullRepresentation("")
                .bind(Product::getComment, Product::setComment);
        setCommentColumn.setEditorComponent(tfEditDescription);

        binder.forField(tfEditPurchasePrice)
                .asRequired("Mag niet leeg zijn")
                .withConverter(new StringToDoubleConverter(String.valueOf(0.0)))
                .bind(Product::getPurchasePrice, Product::setPurchasePrice);
        setPurchaseColumn.setEditorComponent(tfEditPurchasePrice);

        binder.forField(tfEditMargin)
                .asRequired("Mag niet leeg zijn")
                .withConverter(new StringToDoubleConverter(String.valueOf(0.0)))
                .bind(Product::getSellMargin, Product::setSellMargin);
        setMarginColumn.setEditorComponent(tfEditMargin);

        binder.forField(tfEditPrice)
                .asRequired("Mag niet leeg zijn")
                .withConverter(new StringToDoubleConverter(String.valueOf(0.0)))
                .bind(Product::getSellPrice, Product::setSellPrice);
        setSellColumn.setEditorComponent(tfEditPrice);

        binder.addValueChangeListener(event -> {
            if(event.isFromClient()){
                productService.save(editor.getItem());
                Notification.show("Set is bewaard.");
            }
        });
    }


    private Component getProductDetail() {

        setGrid.setWidth("100%");
        setGrid.removeAllColumns();

        setNameColumn = setGrid.addColumn(item -> item.getInternalName()).setAutoWidth(true).setHeader("naam").setResizable(true);
        setCommentColumn = setGrid.addColumn(item -> item.getComment()).setAutoWidth(true).setHeader("commentaar").setResizable(true);
        setPurchaseColumn = setGrid.addColumn(item -> item.getPurchasePrice()).setAutoWidth(true).setHeader("Aankoop").setResizable(true);
        setMarginColumn = setGrid.addColumn(item -> item.getSellMargin()).setAutoWidth(true).setHeader("Marge").setResizable(true);
        setSellColumn = setGrid.addColumn(item -> item.getSellPrice()).setAutoWidth(true).setHeader("verkoopsprijs").setResizable(true).setAutoWidth(true);;

        setGrid.addComponentColumn(item -> {
            Button addButton = new Button(new Icon(VaadinIcon.CLUSTER));
            addButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            addButton.addClickListener(e -> {
                setGrid.select(item);
                selectItem(item);
                dialog.setText("Het geselecteerde set neemt de geselecteerde subfolders over van de linker kant : " +getGroupFromSelectedProduct() + " "  + ". Is dit OK?");
                dialog.open();
            });
            return addButton;
        }).setAutoWidth(true);;


        setGrid.addComponentColumn(item -> {
            Button addButton = new Button(new Icon(VaadinIcon.PLUS));
            addButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            addButton.addClickListener(e -> {
                Product newSetProduct = new Product();
                newSetProduct.setSet(true);
                newSetProduct.setPurchasePrice(0.0);
                newSetProduct.setSellMargin(0.0);
                newSetProduct.setSellPrice(0.0);
                newSetProduct.setInternalName("");
                newSetProduct.setComment("");
                setProducts.add(newSetProduct);
                productService.save(newSetProduct);
                setGrid.getDataProvider().refreshAll();
            });
            return addButton;
        }).setAutoWidth(true);;

        setGrid.addComponentColumn(item -> {
            Button addButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
            addButton.addThemeVariants(ButtonVariant.LUMO_WARNING);
            addButton.addClickListener(e -> {
                setProducts.remove(item);
                productService.delete(item);
                setGrid.getDataProvider().refreshAll();
            });
            return addButton;
        }).setAutoWidth(true);

        setGrid.addItemClickListener(event -> {
            selectItem(event.getItem());
        });

        setGrid.addItemDoubleClickListener(e -> {
            editor.editItem(e.getItem());
            Component editorComponent = e.getColumn().getEditorComponent();
            if (editorComponent instanceof Focusable) {
                ((Focusable) editorComponent).focus();
            }
        });

        setGrid.getHeaderRows().clear();
        headerRow = setGrid.appendHeaderRow();

        headerRow.getCell(setNameColumn).setComponent(
                tfFiltername);
        headerRow.getCell(setCommentColumn).setComponent(
                tfFilterDescription);

        return setGrid;
    }

    private String getGroupFromSelectedProduct() {
        String groupString = "";
        if(selectProductSubView.getSelectedProductLevel1() != null){
            groupString = groupString + selectProductSubView.getSelectedProductLevel1().getName() + " ";
        }
        if(selectProductSubView.getSelectedProductLevel2() != null){
            groupString = groupString + selectProductSubView.getSelectedProductLevel2().getName() + " ";
        }
        if(selectProductSubView.getSelectedProductLevel3() != null){
            groupString = groupString + selectProductSubView.getSelectedProductLevel3().getName() + " ";
        }
        if(selectProductSubView.getSelectedProductLevel4() != null){
            groupString = groupString + selectProductSubView.getSelectedProductLevel4().getName() + " ";
        }
        if(selectProductSubView.getSelectedProductLevel5() != null){
            groupString = groupString + selectProductSubView.getSelectedProductLevel5().getName() + " ";
        }
        if(selectProductSubView.getSelectedProductLevel6() != null){
            groupString = groupString + selectProductSubView.getSelectedProductLevel6().getName() + " ";
        }
        if(selectProductSubView.getSelectedProductLevel7() != null){
            groupString = groupString + selectProductSubView.getSelectedProductLevel7().getName() + " ";
        }

        return groupString;
    }

    private String getGroupFromSelectedSet(Product set) {
        String groupString = "";
        if(set.getProductLevel1() != null){
            groupString = groupString + set.getProductLevel1().getName() + " ";
        }
        if(set.getProductLevel2() != null){
            groupString = groupString + set.getProductLevel2().getName() + " ";
        }
        if(set.getProductLevel3() != null){
            groupString = groupString + set.getProductLevel3().getName() + " ";
        }
        if(set.getProductLevel4() != null){
            groupString = groupString + set.getProductLevel4().getName() + " ";
        }
        if(set.getProductLevel5() != null){
            groupString = groupString + set.getProductLevel5().getName() + " ";
        }
        if(set.getProductLevel6() != null){
            groupString = groupString + set.getProductLevel6().getName() + " ";
        }
        if(set.getProductLevel7() != null){
            groupString = groupString + set.getProductLevel7().getName() + " ";
        }

        return groupString;
    }

    private void selectItem(Product item) {
        selectedSet = item;

        if((selectedSet.getSetList() != null) && (selectedSet.getSetList().size() > 0)){
            selectProductSubView.setSelectedProductList(selectedSet.getSetList());
            binder.readBean(selectedSet);
            groupSpan.setText(getGroupFromSelectedSet(item));
        }
        else{
            List<Product>productList = new ArrayList<>();
            selectedSet.setSetList(productList);
            selectProductSubView.setSelectedProductList(productList);
            binder.readBean(selectedSet);
            groupSpan.setText("Niet toegewezen aan een groep!");
        }
    }

    private void setUpSplitLayouts() {
        mainSplitLayout = new SplitLayout();
        mainSplitLayout.setSizeFull();
        mainSplitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);

        secondarySplitLayout = new SplitLayout();
        secondarySplitLayout.setSizeFull();
        secondarySplitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);

    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Optional<List<Product>> allSets = productService.getAllSets();
        if (allSets.isPresent()) {
            setProducts = allSets.get();
            setGrid.setItems(setProducts);
        }
        else{
            setProducts = new ArrayList<>();
            Product product = new Product();
            product.setSet(true);
            product.setComment("N/A");
            product.setPurchasePrice(0.0);
            product.setSellMargin(0.0);
            product.setSellPrice(0.0);
            product.setInternalName("N/A");
            setProducts.add(product);
            setGrid.setItems(setProducts);
            Notification.show("Geen sets gevonden.");
        }
    }

    @PostConstruct
    private void init() {
        listener.setEventConsumer(event -> {
            UI.getCurrent().access(() -> {
                try {
                    binder.writeBean(selectedSet);
                    productService.save(selectedSet);
                    Notification.show("Set is bewaard.");
                }
                catch (Exception e) {
                    Notification notification = Notification.show("Gelieve eerst een set aan te duiden.");
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });
        });
    }

}
