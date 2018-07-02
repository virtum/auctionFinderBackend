package com.filocha.storage;

import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class RepositoryExtensions {

    /**
     * Returns observable stream which will be used to handle every subscriber update and stores it in database.
     *
     * @param mongoTemplate instance of mongo
     * @return observable stream to handle subscriber update
     */
    public static PublishSubject<SubscriberModel> updateSubscriber(final MongoTemplate mongoTemplate) {
        final PublishSubject<SubscriberModel> subject = PublishSubject.create();

        subject
                .observeOn(Schedulers.io())
                .subscribe(mongoTemplate::save);

        return subject;
    }

    /**
     * Finds specific subscriber with subscriptions in database by given email.
     *
     * @param mongoTemplate instance of mongo
     * @param email         subscriber email
     * @return if non-null, Optional with SubscriberModel, otherwise returns an empty Optional
     */
    public static Optional<SubscriberModel> findSubscriber(final MongoTemplate mongoTemplate, final String email) {
        return Optional.ofNullable(mongoTemplate.findOne(Query.query(Criteria.where("email").is(email)), SubscriberModel.class));
    }

    /**
     * Returns list of all subscribers with subscriptions in database.
     *
     * @param mongoTemplate instance of mongo
     * @return list of all subscribers with subscriptions
     */
    public static List<SubscriberModel> getAllSubscribers(final MongoTemplate mongoTemplate) {
        return new CopyOnWriteArrayList<>(mongoTemplate.findAll(SubscriberModel.class));
    }

}
