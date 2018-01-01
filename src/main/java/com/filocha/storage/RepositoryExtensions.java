package com.filocha.storage;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import rx.subjects.PublishSubject;

import java.util.List;

public class RepositoryExtensions {

    // TODO make this closable
    public static PublishSubject<SubscriberModel> updateSubscriber(MongoTemplate mongoTemplate) {
        PublishSubject<SubscriberModel> subscriptions = PublishSubject.create();
        subscriptions.subscribe(mongoTemplate::save);

        return subscriptions;
    }

    public static SubscriberModel findSubscriber(MongoTemplate mongoTemplate, String email) {
        return mongoTemplate.findOne(Query.query(Criteria.where("email").is(email)), SubscriberModel.class);
    }

    public static List<SubscriberModel> getAllSubscribers(MongoTemplate mongoTemplate) {
        return mongoTemplate.findAll(SubscriberModel.class);
    }

}
