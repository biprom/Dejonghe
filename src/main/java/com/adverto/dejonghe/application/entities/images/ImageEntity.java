package com.adverto.dejonghe.application.entities.images;

import lombok.*;
import org.springframework.core.io.Resource;
import org.springframework.data.annotation.Id;

import java.io.InputStream;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageEntity {

    @Id
    private String id;
    private String image;
    private InputStream inputStream;
    private Resource resource;
    private String comment;
    private boolean bVoorOpPdf;
}
