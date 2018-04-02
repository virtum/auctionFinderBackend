package com.filocha.storage;

import com.filocha.finder.AuctionFinderImpl;
import com.filocha.finder.RequestModel;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SubscriptionCacheTest {

    @Test
    public void shouldAddTwoSubscriptionsForTheSameUser() {
        // given
        final PublishSubject<Model> subscriptions = PublishSubject.create();
        final PublishSubject<SubscriberModel> repository = PublishSubject.create();

        final ReplaySubject<SubscriberModel> emitted = ReplaySubject.create();
        repository.subscribe(emitted);

        SubscriptionCache.startCache(subscriptions, PublishSubject.create(), new AuctionFinderImpl(), repository,
                PublishSubject.create());

        final String email = "user@email";

        // when
        subscriptions.onNext(Model.createNewSubscription(email, "item1"));
        subscriptions.onNext(Model.createNewSubscription(email, "item2"));

        // then
        final SubscriberModel subscriber = emitted
                .skip(1) // skip first emitted event
                .blockingFirst();

        assertEquals(email, subscriber.getEmail());
        assertEquals(2, subscriber.getAuctions().size());
        // TODO add assert to check items in list
    }

    @Test
    public void shouldUpdateUrls() {
        // given
        final PublishSubject<Model> subscriptions = PublishSubject.create();
        final PublishSubject<SubscriberModel> repository = PublishSubject.create();

        final ReplaySubject<SubscriberModel> emitted = ReplaySubject.create();
        repository.subscribe(emitted);

        SubscriptionCache.startCache(subscriptions, PublishSubject.create(), new AuctionFinderImpl(), repository,
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
        final SubscriberModel subscriber = emitted
                .skip(2) // skip two emitted events
                .blockingFirst();

        assertEquals(2, subscriber.getAuctions().get(0).getUrls().size());
    }


    @Test
    public void shouldNotEmitRequestOnDuplicatedSubscription() {
        // given
        final PublishSubject<Model> subscriptions = PublishSubject.create();
        final PublishSubject<RequestModel> requests = PublishSubject.create();

        final ReplaySubject<RequestModel> emitted = ReplaySubject.create();
        requests.subscribe(emitted);

        SubscriptionCache.startCache(subscriptions, requests, new AuctionFinderImpl(), PublishSubject.create(),
                PublishSubject.create());

        final String email = "user@email";

        // when
        subscriptions.onNext(Model.createNewSubscription(email, "item1"));
        subscriptions.onNext(Model.createNewSubscription(email, "item1"));

        // then
        RequestModel requestModel1 = requests
                .skip(1)
                .take(3, TimeUnit.SECONDS)
                .blockingFirst(null);

        requests.onComplete();

        assertNull(requestModel1);
    }

    @Test
    public void shouldNotEmitRequestOnDuplicatedSubscription1() {
        // given
        final PublishSubject<Model> subscriptions = PublishSubject.create();
        final PublishSubject<RequestModel> requests = PublishSubject.create();

        final ReplaySubject<RequestModel> emitted = ReplaySubject.create();
        requests.subscribe(emitted);

        SubscriptionCache.startCache(subscriptions, requests, new AuctionFinderImpl(), PublishSubject.create(),
                PublishSubject.create());

        final String email = "user@email";

        requests.onComplete();

        // when
        subscriptions.onNext(Model.createNewSubscription(email, "item1"));
        subscriptions.onNext(Model.createNewSubscription(email, "item1"));

        // then
        final Iterable items = emitted.blockingIterable();
        Assertions.assertThat(items).hasSize(1);
    }
}