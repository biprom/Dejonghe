package com.adverto.dejonghe.application.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

@Configuration
public class GridFsConfiguration extends AbstractMongoClientConfiguration {

    @Autowired
    private MappingMongoConverter mongoConverter;


    @Override
    protected String getDatabaseName() {
        return "dejonghe";
    }

    @Bean
    public GridFsTemplate gridFsTemplate() {
        return new GridFsTemplate(mongoDbFactory(),mongoConverter);
    }

}
