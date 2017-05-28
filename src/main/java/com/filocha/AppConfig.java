package com.filocha;

import com.mongodb.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import java.net.UnknownHostException;

@Configuration
public class AppConfig {
    @Bean
    public MongoDbFactory mongoDbFactory() throws UnknownHostException {
        //return new SimpleMongoDbFactory(new MongoClient("192.168.99.100", 27017), "auctionFinder");
        return new SimpleMongoDbFactory(new MongoClient("localhost", 27017), "auctionFinder");
    }

    @Bean
    public MongoOperations mongoOperations() throws UnknownHostException{
        return new MongoTemplate(mongoDbFactory());
    }
}
