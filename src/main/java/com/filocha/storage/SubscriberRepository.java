package com.filocha.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class SubscriberRepository {

    @Autowired
    private MongoOperations mongoOperations;

//    public void saveSubscription(String email, String item) {
//        SubscriberModel subscriber = new SubscriberModel();
//        subscriber.setEmail(email);
//        subscriber.setItemWithUrls(Collections.singletonMap(item, new ArrayList<>()));
//
//        mongoOperations.save(subscriber);
//    }
//
//    public void updateUserUrls(String userEmail, List<String> urls, String item) {
//        Query updateQuery = Query.query(Criteria.where("email").is(userEmail));
//        SubscriberModel subscriber = mongoOperations.findOne(updateQuery, SubscriberModel.class);
//
//        List<String> links = subscriber.getItemWithUrls().get(item);
//        links.addAll(urls);
//
//        Map<String, List<String>> updatedMap = subscriber.getItemWithUrls();
//        updatedMap.put(item, links);
//
//        subscriber.setItemWithUrls(updatedMap);
//        mongoOperations.save(subscriber);
//    }
//
//    public SubscriberModel findSubscriber(String email) {
//        Query query = new Query(Criteria.where("email").is(email));
//        return mongoOperations.findOne(query, SubscriberModel.class);
//    }
//
//    public List<SubscriberModel> findAllUserSubscriptions(String email) {
//        Query query = new Query(Criteria.where("email").is(email));
//        return mongoOperations.find(query, SubscriberModel.class);
//    }
//
//    public Map<String, Map<String, List<String>>> getUsersWithItems() {
//        return mongoOperations
//                .findAll(SubscriberModel.class)
//                .stream().collect(Collectors.toMap(SubscriberModel::getEmail, SubscriberModel::getItemWithUrls));
//    }

}
