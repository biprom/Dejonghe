package com.adverto.dejonghe.application.views.subViews;

import com.adverto.dejonghe.application.dbservices.WorkOrderService;
import com.adverto.dejonghe.application.entities.enums.employee.UserFunction;
import com.adverto.dejonghe.application.entities.images.ImageEntity;
import com.adverto.dejonghe.application.entities.product.product.Product;
import com.adverto.dejonghe.application.services.ImageService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.StreamResource;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
public class ShowImageSubVieuw extends VerticalLayout implements BeforeEnterObserver {

    WorkOrderService workOrderService;
    ImageService imageService;
    List<ImageEntity>imageEntityList = new ArrayList<>();
    InputStream imageInputStream;

    UserFunction userFunction = UserFunction.ADMIN;
    List<String>imageList;

    ConfirmDialog confirmDialog;
    Span title = new Span("Foto's");

    @Autowired
    public ShowImageSubVieuw(WorkOrderService workOrderService,
                             ImageService imageService) {
        this.workOrderService = workOrderService;
        this.imageService = imageService;
        this.removeAll();
        setUpConfirmDialog();
    }

    private void setUpConfirmDialog() {
        confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Unsaved changes");
        confirmDialog.setText(
                "There are unsaved changes. Do you want to discard or save them?");

        confirmDialog.setRejectable(true);
        confirmDialog.setRejectText("Annuleer");
        confirmDialog.addRejectListener(event -> confirmDialog.close());

        confirmDialog.setConfirmText("Verwijder");
    }

    private void setUpImages() {
        imageEntityList.clear();
        this.removeAll();
        this.add(title);
        if(imageList != null && imageList.size() > 0) {
            for(String imageId : imageList) {
                try {
                    Optional<ImageEntity> optionalImageEntity = imageService.getImage(imageId);
                    HorizontalLayout layout = new HorizontalLayout();
                    layout.setSizeFull();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        imageInputStream = optionalImageEntity.get().getInputStream();
                        Thumbnails.of( imageInputStream)
                                .size(450, 450)
                                .toOutputStream(baos);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    StreamResource imageResource = new StreamResource(optionalImageEntity.get().getImage(),
                            () -> {
                                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(baos.toByteArray());
                                try {
                                    baos.close();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                return byteArrayInputStream;
                            });
                    Image foto = new Image(imageResource,optionalImageEntity.get().getImage());
                    if(!(this.userFunction.equals(UserFunction.TECHNICIAN))) {
                        VerticalLayout verticalLayout = new VerticalLayout();
                        verticalLayout.setWidth("100%");
                        TextArea textArea = new TextArea();
                        textArea.setWidth("100%");
                        textArea.setHeight("100%");
                        textArea.setPlaceholder("Gelieve altijd wat commentaar bij de foto's te plaatsen!");
                        if(optionalImageEntity.get().getComment() != null) {
                            textArea.setValue(optionalImageEntity.get().getComment());
                        }
                        textArea.addValueChangeListener(event -> {
                            try {
                                String newId = imageService.updateImageComment(imageId,textArea.getValue());
                                imageList.remove(imageId);
                                imageList.add(newId);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        Button removeButton = new Button("Verwijder foto");
                        removeButton.setWidth("100%");
                        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
                        removeButton.addClickListener(event -> {
                            confirmDialog.open();
                            confirmDialog.addConfirmListener(listener -> {
                                imageList.remove(imageId);
                                imageService.removeImage(imageId);
                                this.remove(layout);
                            });
                        });
                        verticalLayout.add(removeButton, textArea);
                        layout.add(foto, verticalLayout);
                    }
                    else{
                        layout.add(foto);
                    }

                    this.add(layout);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void setSelectedWorkOrder(List<String> imageList) {
        this.imageList = imageList;
        setUpImages();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        imageEntityList.clear();
    }

    public void setUser(UserFunction userFunction) {
        this.userFunction = userFunction;
    }

    public void setTitle(String selectedProduct) {
        this.title.setText(selectedProduct);
    }
}
