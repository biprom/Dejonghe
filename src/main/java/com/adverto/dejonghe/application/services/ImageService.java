package com.adverto.dejonghe.application.services;

import com.adverto.dejonghe.application.entities.images.ImageEntity;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ImageService {
    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFsOperations operations;

    public ImageEntity addImage(InputStream inputStream, String name, String comment) throws IOException {
        DBObject metaData = new BasicDBObject();
        metaData.put("type", "image");
        metaData.put("meta1", comment);
        ObjectId id = gridFsTemplate.store(
                inputStream, name, "image/jpg", metaData);
        ImageEntity newImageEntity = ImageEntity.builder()
                                    .inputStream(inputStream)
                                    .bVoorOpPdf(true)
                                    .comment(comment)
                                    .id(id.toString())
                                    .image(name)
                                    .build();
        return newImageEntity;
    }

    public String updateImageComment(String id, String comment) throws IOException {
        GridFSFile image = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(id)));
        try {
            image.getMetadata().remove("meta1");
        }
        catch (Exception e){
        }
        image.getMetadata().put("meta1",comment);
        InputStream inputStream = operations.getResource(image).getInputStream();
        String fileName = image.getFilename().split("\\.")[0];
        String newId = gridFsTemplate.store(inputStream , fileName+ThreadLocalRandom.current().nextInt(0, 9)+".jpg", "image/jpg", image.getMetadata()).toString();
        removeImage(id);
        return newId;
    }

    public Optional<ImageEntity> getImage(String id) throws IllegalStateException, IOException {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
        if(file != null){
            ImageEntity image = ImageEntity.builder().build();
            try{
                image.setComment(file.getMetadata().get("meta1").toString());
            }
            catch (Exception e){

            }
            image.setResource(operations.getResource(file));
            InputStream inputStream = operations.getResource(file).getInputStream();
            image.setInputStream(inputStream);
            image.setId(file.getId().toString());
            image.setImage(file.getFilename());
            image.setBVoorOpPdf(true);
            return Optional.of(image);
        }
        return Optional.empty();
    }

    public void removeImage(String id){
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(id)));
    }
}
