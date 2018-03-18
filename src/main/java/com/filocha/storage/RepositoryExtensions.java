package com.filocha.storage;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import rx.subjects.PublishSubject;

import java.util.List;
import java.util.Optional;

public class RepositoryExtensions {

    // TODO make this closable
    public static PublishSubject<SubscriberModel> updateSubscriber(final MongoTemplate mongoTemplate) {
        final PublishSubject<SubscriberModel> subject = PublishSubject.create();
        subject.subscribe(mongoTemplate::save);

        return subject;
    }

    public static Optional<SubscriberModel> findSubscriber(final MongoTemplate mongoTemplate, final String email) {
        return Optional.ofNullable(mongoTemplate.findOne(Query.query(Criteria.where("email").is(email)), SubscriberModel.class));
    }

    public static List<SubscriberModel> getAllSubscribers(final MongoTemplate mongoTemplate) {
        return mongoTemplate.findAll(SubscriberModel.class);
    }

}
