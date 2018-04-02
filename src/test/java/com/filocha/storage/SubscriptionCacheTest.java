package com.filocha.storage;

import com.filocha.finder.AuctionFinderImpl;
import com.filocha.finder.RequestModel;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class SubscriptionCacheTest {

    @Test
    public void shouldAddTwoSubscriptionsForTheSameUser() {
        // given
        final ReplaySubject<Model> subscriptions = ReplaySubject.create();
        final ReplaySubject<SubscriberModel> repository = ReplaySubject.create();

        new SubscriptionCache().startCache(subscriptions, PublishSubject.create(), new AuctionFinderImpl(), repository,
                PublishSubject.create());

        final String email = "user@email";

        // when
        subscriptions.onNext(Model.createNewSubscription(email, "item1"));
        subscriptions.onNext(Model.createNewSubscription(email, "item2"));

        // then
        final SubscriberModel subscriber = repository
                .skip(1) // skip first emitted event
                .blockingFirst();

        assertEquals(email, subscriber.getEmail());
        assertEquals(2, subscriber.getAuctions().size());
    }

    @Test
    public void shouldUpdateUrls() {
        // given
        final ReplaySubject<Model> subscriptions = ReplaySubject.create();
        final ReplaySubject<SubscriberModel> repository = ReplaySubject.create();

        new SubscriptionCache().startCache(subscriptions, PublishSubject.create(), new AuctionFinderImpl(), repository,
                PublishSubject.create());

        final String email = "user@email";
        final String item = "item";

        // when
        // create new subscription
        subscriptions.onNext(Model.createNewSubscription(email, item));

        // emit events with found urls
        subscriptions.onNext(Model.createModelForUpdate(email, item, new ArrayList<>(Collections.singletonList("url1"))));
        subscriptions.onNext(Model.createModelForUpdate(email, item, new ArrayList<>(Collections.singletonList("url2"))));

        // then
        final SubscriberModel subscriber = repository
                .skip(2) // skip two emitted events
                .blockingFirst();

        assertEquals(2, subscriber.getAuctions().get(0).getUrls().size());
    }

    @Test
    public void shouldNotSaveSameUrlsTwice() {
        // given
        final ReplaySubject<Model> subscriptions = ReplaySubject.create();
        final ReplaySubject<SubscriberModel> repository = ReplaySubject.create();

        new SubscriptionCache().startCache(subscriptions, PublishSubject.create(), new AuctionFinderImpl(), repository,
                PublishSubject.create());

        final String email = "user@email";
        final String item = "item";

        // when
        // create new subscription
        subscriptions.onNext(Model.createNewSubscription(email, item));

        // emit events with found urls
        subscriptions.onNext(Model.createModelForUpdate(email, item, new ArrayList<>(Collections.singletonList("url1"))));
        subscriptions.onNext(Model.createModelForUpdate(email, item, new ArrayList<>(Collections.singletonList("url1"))));
        subscriptions.onNext(Model.createModelForUpdate(email, item, new ArrayList<>(Collections.singletonList("url2"))));

        // then
        final SubscriberModel subscriber = repository
                .skip(2) // skip two emitted events
                .blockingFirst();

        assertEquals(2, subscriber.getAuctions().get(0).getUrls().size());
    }

    @Test
    public void shouldNotEmitRequestOnDuplicatedSubscription() {
        // given
        final ReplaySubject<Model> subscriptions = ReplaySubject.create();
        final ReplaySubject<RequestModel> requests = ReplaySubject.create();

        new SubscriptionCache().startCache(subscriptions, requests, new AuctionFinderImpl(), PublishSubject.create(),
                PublishSubject.create());

        final String email = "user@email";

        // when
        subscriptions.onNext(Model.createNewSubscription(email, "item1"));
        subscriptions.onNext(Model.createNewSubscription(email, "item1"));
        subscriptions.onNext(Model.createNewSubscription(email, "item2"));

        // then
        final RequestModel request = requests
                .skip(1) // skip first emitted item
                .blockingFirst();

        assertEquals("item2", request.getItem());

    }
}