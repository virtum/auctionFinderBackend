package com.filocha.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionServiceImpl {

    @Autowired
    private MongoOperations mongoOperations;

    // Temporary method for further refactor
    public void saveUser(String email, String item) {
        SubscriberModel subscriber = new SubscriberModel(email, item);
        mongoOperations.save(subscriber);
    }

    public SubscriberModel findUser(String email) {
        Query query = new Query(Criteria.where("email").is(email));
        return mongoOperations.findOne(query, SubscriberModel.class);
    }

}

