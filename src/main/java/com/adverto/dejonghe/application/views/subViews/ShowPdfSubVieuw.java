package com.adverto.dejonghe.application.views.subViews;

import com.adverto.dejonghe.application.dbservices.WorkOrderService;
import com.adverto.dejonghe.application.entities.enums.employee.UserFunction;
import com.adverto.dejonghe.application.entities.images.ImageEntity;
import com.adverto.dejonghe.application.services.ImageService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.StreamResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
public class ShowPdfSubVieuw extends VerticalLayout implements BeforeEnterObserver {

    WorkOrderService workOrderService;
    ImageService imageService;
    InputStream imageInputStream;

    List<String>pdfList;

    Span title = new Span("PDF's");
    UserFunction userFunction = UserFunction.ADMIN;

    @Autowired
    public ShowPdfSubVieuw(WorkOrderService workOrderService,
                           ImageService imageService) {
        this.workOrderService = workOrderService;
        this.imageService = imageService;
        this.removeAll();
    }

    private void setUpPdf() {
        this.removeAll();
        this.add(title);
        if(pdfList != null && pdfList.size() > 0) {
            for(String pdfId : pdfList) {
                try {
                    Optional<ImageEntity> optionalImageEntity = imageService.getImage(pdfId);
                    VerticalLayout layout = new VerticalLayout();
                    layout.setSizeFull();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        imageInputStream = optionalImageEntity.get().getInputStream();
                        baos.write(imageInputStream.readAllBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    StreamResource pdfResource = new StreamResource(optionalImageEntity.get().getImage(),
                            () -> {
                                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(baos.toByteArray());
                                try {
                                    baos.close();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                return byteArrayInputStream;
                            });

                    // Anchor dat naar de resource verwijst
                    String fileName = pdfResource.getName();
                    pdfResource.setContentType("application/pdf");
                    Anchor link = new Anchor(pdfResource, "Open PDF : " + fileName);
                    link.setTarget("_blank"); // <--- opent in nieuw tabblad
                    if(!(this.userFunction.equals(UserFunction.TECHNICIAN))){
                        Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
                        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
                        removeButton.addClickListener(e -> {
                            imageService.removeImage(pdfId);
                            pdfList.remove(pdfId);
                            setUpPdf();
                        });
                        HorizontalLayout horizontalLayout = new HorizontalLayout();
                        horizontalLayout.add(link,removeButton);
                        layout.add(horizontalLayout);
                    }
                    else{
                        layout.add(link);
                    }

                    this.add(layout);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void setSelectedWorkOrder(List<String> pdfList) {
        this.pdfList = pdfList;
        setUpPdf();
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
    }

    public void setUser(UserFunction userFunction) {
        this.userFunction = userFunction;
    }
}
