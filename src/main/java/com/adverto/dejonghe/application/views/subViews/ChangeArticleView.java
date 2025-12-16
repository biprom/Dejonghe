package com.adverto.dejonghe.application.views.subViews;

import com.adverto.dejonghe.application.dbservices.ProductService;
import com.adverto.dejonghe.application.entities.enums.employee.UserFunction;
import com.adverto.dejonghe.application.entities.product.product.Product;
import com.adverto.dejonghe.application.entities.product.product.ProductLink;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.binder.ValidationException;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Component
@Scope("prototype")
public class ChangeArticleView extends Div {

    VerticalLayout mainVerticalLayout;
    GridFsTemplate gridFsTemplate;
    ShowImageSubVieuw showImageSubVieuw;
    ShowPdfSubVieuw showPdfSubVieuw;
    ProductService productService;

    Dialog imageDialog;
    Dialog pdfDialog;

    MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    Upload dropEnabledUpload = new Upload(buffer);
    Button showImageButton;
    Button showPDFButton;

    Product selectedProduct;
    Grid<ProductLink>linkGrid = new Grid<>();

    Span titleSpan;

    public ChangeArticleView(GridFsTemplate gridFsTemplate,
                             ShowImageSubVieuw showImageSubVieuw,
                             ShowPdfSubVieuw showPdfSubVieuw,
                             ProductService productService) {
        this.gridFsTemplate = gridFsTemplate;
        this.showImageSubVieuw = showImageSubVieuw;
        this.showPdfSubVieuw = showPdfSubVieuw;
        this.productService = productService;
        setUpLinkGrid();
        setUpShowImageButton();
        setUpShowPDFButton();
        setUpImageDialog();
        setUpPdfDialog();
        setUpUpload();
        this.add(getMainVerticalLayout());
    }

    private void setUpLinkGrid() {
        linkGrid.addComponentColumn(productLink -> {
            TextField textField = new TextField();
            textField.setWidth("100%");
            if(productLink != null) {
                textField.setValue(productLink.getLink());
            }
            else{
                textField.setValue("");
            }
            textField.addValueChangeListener(event -> {
                productLink.setLink(textField.getValue());
                productService.save(selectedProduct);
            });
            return textField;
        }).setHeader("Link").setFlexGrow(10);

        linkGrid.addComponentColumn(productLink -> {
            Button linkButton = new Button(new Icon(VaadinIcon.LINK));
            linkButton.addClickListener(event -> {
                UI.getCurrent().getPage().open(productLink.getLink(), "_blank");
            });
            return linkButton;
        }).setHeader("Ga naar link").setFlexGrow(1).setFrozenToEnd(true);

        linkGrid.addComponentColumn(productLink -> {
            Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
            removeButton.addClickListener(event -> {
                selectedProduct.getLinkDocumentList().remove(productLink);
                linkGrid.getDataProvider().refreshAll();
                productService.save(selectedProduct);
            });
            return removeButton;
        }).setHeader("Verwijder").setFlexGrow(1).setFrozenToEnd(true);

        linkGrid.addComponentColumn(productLink -> {
            Button addButton = new Button(new Icon(VaadinIcon.PLUS));
            addButton.addClickListener(event -> {
                ProductLink newProductLink = new ProductLink();
                newProductLink.setDate(LocalDate.now());
                newProductLink.setLink("");
                selectedProduct.getLinkDocumentList().add(newProductLink);
                linkGrid.getDataProvider().refreshAll();
                productService.save(selectedProduct);
            });
            return addButton;
        }).setHeader("Voeg toe").setFlexGrow(1).setFrozenToEnd(true);
    }

    private void setUpUpload() {
        dropEnabledUpload.setWidthFull();
        //dropEnabledUpload.setAcceptedFileTypes("image/tiff", ".jpeg");
        dropEnabledUpload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();

            Notification notification = Notification.show(errorMessage, 5000,
                    Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        dropEnabledUpload.addFailedListener(event -> {
            Notification.show("Deze foto kon niet worden verstruurd naar de server : " + event.getReason());
        });
        dropEnabledUpload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            String mimeType = event.getMIMEType();
            InputStream inputStream = buffer.getInputStream(fileName);
            DBObject metaData = new BasicDBObject();
            metaData.put("timeOfUpload", LocalDateTime.now().toString());


            if (mimeType.contains("image/")) {
                try {
                    storeImageIdToThisArticle(gridFsTemplate.store(inputStream, fileName, mimeType, metaData).toString());
                    updateGetImageButton();
                } catch (ValidationException e) {
                    Notification notification = Notification.show("De toegevoegde foto kon niet worden bewaard!");
                    notification.setPosition(Notification.Position.MIDDLE);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Notification.show("Inputstream van deze foto kon niet worden afgesloten!");
                }
            } else if (mimeType.startsWith("application/pdf")) {
                try {
                    storePdfIdToThisArticle(gridFsTemplate.store(inputStream, fileName, mimeType, metaData).toString());
                    updateGetPDFButton();
                } catch (ValidationException e) {
                    Notification notification = Notification.show("De toegevoegde foto kon niet worden bewaard!");
                    notification.setPosition(Notification.Position.MIDDLE);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Notification.show("Inputstream van deze foto kon niet worden afgesloten!");
                }
            } else {
                Notification.show("Onbekend bestand, geen pdf of geen foto");
            }
        });
    }

    private void updateGetImageButton() {
        if(selectedProduct.getImageList() != null) {
            showImageButton.setText("Dit artikel bevat " + selectedProduct.getImageList().size() + " foto(s), klik hier om ze te bekijken.");
        }
        else{
            showImageButton.setText("Dit artikel bevat bevat geen foto's.");
        }
        showImageButton.removeThemeVariants(ButtonVariant.LUMO_ERROR);
        showImageButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
    }

    private void updateGetPDFButton() {
        if(selectedProduct.getPdfList() != null) {
            showPDFButton.setText("Dit artikel bevat " + selectedProduct.getPdfList().size() + " pdf(s), klik hier om ze te bekijken.");
        }
        else{
            showPDFButton.setText("Dit artikel bevat bevat geen pdf's.");
        }
        showPDFButton.removeThemeVariants(ButtonVariant.LUMO_ERROR);
        showPDFButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
    }

    private void setUpImageDialog() {
        imageDialog = new Dialog();
        imageDialog.setCloseOnEsc(true);
        imageDialog.setWidth("50%");
        imageDialog.setHeight("50%");
        imageDialog.setHeaderTitle("Toegevoegde foto's onder geselecteerde artikel");
        imageDialog.add(showImageSubVieuw);
        Button cancelButton = new Button("Sluiten", e -> {
            productService.save(selectedProduct);
            updateGetImageButton();
            imageDialog.close();
        });
        imageDialog.getFooter().add(cancelButton);
        imageDialog.addDialogCloseActionListener(event -> {
            //save when deleted
            productService.save(selectedProduct);
        });
    }

    private void setUpPdfDialog() {
        pdfDialog = new Dialog();
        pdfDialog.setCloseOnEsc(true);
        pdfDialog.setWidth("50%");
        pdfDialog.setHeight("50%");
        pdfDialog.setHeaderTitle("Toegevoegde pdf's onder geselecteerde artikel");
        pdfDialog.add(showPdfSubVieuw);
        Button cancelButton = new Button("Sluiten", e -> {
            productService.save(selectedProduct);
            updateGetPDFButton();
            pdfDialog.close();
        });
        pdfDialog.getFooter().add(cancelButton);
        pdfDialog.addDialogCloseActionListener(event -> {
            //save when deleted
            productService.save(selectedProduct);
        });
    }

    private void storeImageIdToThisArticle(String idString) throws ValidationException {
        if(selectedProduct.getImageList() != null) {
            selectedProduct.getImageList().add(idString);
            productService.save(selectedProduct);
        }
        else{
            List<String>imageIdList = new ArrayList<>();
            imageIdList.add(idString);
            selectedProduct.setImageList(imageIdList);
            productService.save(selectedProduct);
        }
    }

    private void storePdfIdToThisArticle(String idString) throws ValidationException {
        if(selectedProduct.getPdfList() != null) {
            selectedProduct.getPdfList().add(idString);
            productService.save(selectedProduct);
        }
        else{
            List<String>pdfIdList = new ArrayList<>();
            pdfIdList.add(idString);
            selectedProduct.setPdfList(pdfIdList);
            productService.save(selectedProduct);
        }
    }



    private VerticalLayout getMainVerticalLayout() {
        mainVerticalLayout = new VerticalLayout();
        mainVerticalLayout.setWidth("100%");
        mainVerticalLayout.setHeight("100%");
        mainVerticalLayout.setSpacing(true);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidth("100%");
        mainVerticalLayout.add(getTitleSpan());
        horizontalLayout.add(showImageButton,showPDFButton);
        mainVerticalLayout.add(dropEnabledUpload);
        mainVerticalLayout.add(horizontalLayout);
        mainVerticalLayout.add(linkGrid);

        return mainVerticalLayout;
    }

    private Span getTitleSpan() {
        titleSpan = new Span("");
        return titleSpan;
    }


    private void setUpShowImageButton() {
        showImageButton = new Button("Er zijn in dit artikel geen foto's toegevoegd, gelieve altijd een foto te koppelen van de werken!");
        showImageButton.setWidth("50%");

        showImageButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        showImageButton.addClickListener(e -> {
            if((selectedProduct.getImageList() != null) && (selectedProduct.getImageList().size() > 0)) {
                showImageSubVieuw.setUser(UserFunction.ADMIN);
                showImageSubVieuw.setSelectedWorkOrder(selectedProduct.getImageList());
                imageDialog.open();
            }
            else{
                Notification notification = Notification.show("Dit artikel bevat nog geen foto's!");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });
    }

    private void setUpShowPDFButton() {
        showPDFButton = new Button("Er zijn in dit artikel geen foto's toegevoegd, gelieve altijd een foto te koppelen van de werken!");
        showPDFButton.setWidth("50%");

        showPDFButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        showPDFButton.addClickListener(e -> {
            if((selectedProduct.getPdfList() != null) && (selectedProduct.getPdfList().size() > 0)) {
                showPdfSubVieuw.setSelectedWorkOrder(selectedProduct.getPdfList());
                pdfDialog.open();
            }
            else{
                Notification notification = Notification.show("Dit artikel bevat nog geen pdf's!");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });
    }

    public void setSelectedProduct(Product product) {
        selectedProduct = product;
        titleSpan.setText(selectedProduct.getInternalName());
        updateGetImageButton();
        updateGetPDFButton();
        if((selectedProduct.getLinkDocumentList() != null) && (selectedProduct.getLinkDocumentList().size() > 0)) {
            linkGrid.setItems(selectedProduct.getLinkDocumentList());
        }
        else{
            selectedProduct.setLinkDocumentList(getEmptyLinkedDocumentList());
            linkGrid.setItems(selectedProduct.getLinkDocumentList());
        }
    }

    private List<ProductLink> getEmptyLinkedDocumentList() {
        List<ProductLink>linkedDocumentList = new ArrayList<>();
        ProductLink productLink = new ProductLink();
        productLink.setDate(LocalDate.now());
        productLink.setLink("");
        linkedDocumentList.add(productLink);
        return linkedDocumentList;
    }
}
