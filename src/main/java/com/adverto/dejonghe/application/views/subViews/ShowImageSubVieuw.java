package com.adverto.dejonghe.application.views.subViews;

import com.adverto.dejonghe.application.dbservices.WorkOrderService;
import com.adverto.dejonghe.application.entities.images.ImageEntity;
import com.adverto.dejonghe.application.services.ImageService;
import com.vaadin.flow.component.html.Image;
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

    List<String>imageList;

    @Autowired
    public ShowImageSubVieuw(WorkOrderService workOrderService,
                             ImageService imageService) {
        this.workOrderService = workOrderService;
        this.imageService = imageService;
        this.removeAll();
    }

    private void setUpImages() {
        imageEntityList.clear();
        this.removeAll();
        if(imageList != null && imageList.size() > 0) {
            for(String imageId : imageList) {
                try {
                    Optional<ImageEntity> optionalImageEntity = imageService.getImage(imageId);
                    HorizontalLayout layout = new HorizontalLayout();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        imageInputStream = optionalImageEntity.get().getInputStream();
                        Thumbnails.of( imageInputStream)
                                .size(900, 900)
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
                    TextArea textArea = new TextArea();
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
                    layout.add(foto, textArea);
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
}
