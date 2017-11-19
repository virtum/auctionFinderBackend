package com.filocha.storage;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import rx.subjects.PublishSubject;

import java.util.List;

public class RepositoryExtension {

    // TODO make this closable
    public static PublishSubject<SubscriberModel> updateSubscriber(MongoTemplate mongoTemplate) {
        PublishSubject<SubscriberModel> subscriptions = PublishSubject.create();
        subscriptions
                .subscribe(sub -> mongoTemplate.save(sub));

        return subscriptions;
    }

    public static SubscriberModel findSubscriber(MongoTemplate mongoTemplate, String email) {
        Query query = new Query(Criteria.where("email").is(email));
        return mongoTemplate.findOne(query, SubscriberModel.class);
    }

    public static List<SubscriberModel> findAllUserSubscriptions(MongoTemplate mongoTemplate, String email) {
        Query query = new Query(Criteria.where("email").is(email));
        return mongoTemplate.find(query, SubscriberModel.class);
    }

}
