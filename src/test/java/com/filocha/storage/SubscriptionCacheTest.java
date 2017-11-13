package com.filocha.storage;

import com.filocha.finder.AuctionFinderImpl;
import com.filocha.finder.RequestModel;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SubscriptionCacheTest {

    @Test
    public void shouldAddTwoSubscriptionsForTheSameUser() {
        PublishSubject<Model> subscriptions = PublishSubject.create();
        List<SubscriberModel1> userAuctions = new ArrayList<>();
        PublishSubject<RequestModel> requests = PublishSubject.create();

        //new SubscriptionCache(subscriptions, userAuctions, requests, new AuctionFinderImpl());
        SubscriptionCache.startCache(subscriptions, userAuctions, requests, new AuctionFinderImpl());

        String email = "user@email";

        subscriptions.onNext(Model.createNewSubscription(email, "item1"));
        subscriptions.onNext(Model.createNewSubscription(email, "item2"));

        assertEquals(1, userAuctions.size());

        SubscriberModel1 subscriber = userAuctions.get(0);
        assertEquals(email, subscriber.getEmail());
        assertEquals(2, subscriber.getAuctions().size());
    }

    @Test
    public void shouldUpdateUrls() {
        PublishSubject<Model> subscriptions = PublishSubject.create();
        List<SubscriberModel1> userAuctions = new ArrayList<>();
        PublishSubject<RequestModel> requests = PublishSubject.create();

        //new SubscriptionCache(subscriptions, userAuctions, requests, new AuctionFinderImpl());
        SubscriptionCache.startCache(subscriptions, userAuctions, requests, new AuctionFinderImpl());

        String email = "user@email";
        String item = "item";

        subscriptions.onNext(Model.createNewSubscription(email, item));

        assertEquals(1, userAuctions.size());

        SubscriberModel1 subscriber = userAuctions.get(0);
        assertEquals(email, subscriber.getEmail());

        subscriptions.onNext(Model.createModelForUpdate(email, item, new ArrayList<>(Collections.singletonList("url1"))));
        subscriptions.onNext(Model.createModelForUpdate(email, item, new ArrayList<>(Collections.singletonList("url2"))));

        assertEquals(2, subscriber.getAuctions().get(0).getUrls().size());
    }


    @Test
    public void shouldNotEmitRequestOnDuplicatedSubscription() {
        PublishSubject<Model> subscriptions = PublishSubject.create();
        List<SubscriberModel1> userAuctions = new ArrayList<>();
        PublishSubject<RequestModel> requests = PublishSubject.create();

        //new SubscriptionCache(subscriptions, userAuctions, requests, new AuctionFinderImpl());
        SubscriptionCache.startCache(subscriptions, userAuctions, requests, new AuctionFinderImpl());

        String email = "user@email";

        ReplaySubject<RequestModel> emitted = ReplaySubject.create();
        requests.subscribe(emitted);

        subscriptions.onNext(Model.createNewSubscription(email, "item1"));
        subscriptions.onNext(Model.createNewSubscription(email, "item1"));

        requests.onCompleted();

        Iterable items = emitted.toBlocking().toIterable();
        Assertions.assertThat(items).hasSize(1);
    }

}