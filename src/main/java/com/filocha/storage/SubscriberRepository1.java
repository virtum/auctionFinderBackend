package com.filocha.storage;

import org.springframework.data.mongodb.core.MongoTemplate;
import rx.subjects.PublishSubject;

public class SubscriberRepository1 {

    // TODO make this closable
    public static PublishSubject<Model> updateSubscriber(MongoTemplate mongoTemplate) {
        PublishSubject<Model> subscriptions = PublishSubject.create();
        subscriptions
                .subscribe(sub -> update(mongoTemplate, sub));

        return subscriptions;
    }

    private static void update(MongoTemplate mongoTemplate, Model model) {
        System.out.println();
    }
}
