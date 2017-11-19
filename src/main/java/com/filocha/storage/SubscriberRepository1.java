package com.filocha.storage;

import org.springframework.data.mongodb.core.MongoTemplate;
import rx.subjects.PublishSubject;

public class SubscriberRepository1 {

    // TODO make this closable
    public static PublishSubject<SubscriberModel> updateSubscriber(MongoTemplate mongoTemplate) {
        PublishSubject<SubscriberModel> subscriptions = PublishSubject.create();
        subscriptions
                .subscribe(sub -> mongoTemplate.save(sub));

        return subscriptions;
    }

}
