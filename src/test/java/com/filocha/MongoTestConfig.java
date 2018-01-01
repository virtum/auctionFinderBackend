package com.filocha;

import com.filocha.storage.SubscriberModel;
import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

@TestConfiguration
public class MongoTestConfig {

    @Value("${mongoDbHost}")
    private String mongoDbHost;
    @Value("${databaseName}")
    private String databaseName;

    @Bean
    public MongoTemplate mongoTemplate() {
        MongoTemplate template = new MongoTemplate(new MongoClient(mongoDbHost), databaseName);
        template.dropCollection(SubscriberModel.class);

        return template;
    }
}
