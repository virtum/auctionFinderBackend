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
        List<SubscriberModel1> userAuctions = new ArrayList<>();
        PublishSubject<RequestModel> requests = PublishSubject.create();

        SubscriptionCache1 cache = new SubscriptionCache1(userAuctions, requests, new AuctionFinderImpl());

        String email = "user@email";

        cache.subscriptions.onNext(Model.createNewSubscription(email, "item1"));
        cache.subscriptions.onNext(Model.createNewSubscription(email, "item2"));

        assertEquals(1, userAuctions.size());

        SubscriberModel1 subscriber = userAuctions.get(0);
        assertEquals(email, subscriber.getEmail());
        assertEquals(2, subscriber.getAuctions().size());
    }

    @Test
    public void shouldUpdateUrls() {
        List<SubscriberModel1> userAuctions = new ArrayList<>();
        PublishSubject<RequestModel> requests = PublishSubject.create();

        SubscriptionCache1 cache = new SubscriptionCache1(userAuctions, requests, new AuctionFinderImpl());

        String email = "user@email";
        String item = "item";

        cache.subscriptions.onNext(Model.createNewSubscription(email, item));

        assertEquals(1, userAuctions.size());

        SubscriberModel1 subscriber = userAuctions.get(0);
        assertEquals(email, subscriber.getEmail());

        cache.subscriptions.onNext(Model.createModelForUpdate(email, item, new ArrayList<>(Collections.singletonList("url1"))));
        cache.subscriptions.onNext(Model.createModelForUpdate(email, item, new ArrayList<>(Collections.singletonList("url2"))));

        assertEquals(2, subscriber.getAuctions().get(0).getUrls().size());
    }


    @Test
    public void shouldNotEmitRequesOnDuplicatedeSubscription() {
        List<SubscriberModel1> userAuctions = new ArrayList<>();
        PublishSubject<RequestModel> requests = PublishSubject.create();

        SubscriptionCache1 cache = new SubscriptionCache1(userAuctions, requests, new AuctionFinderImpl());

        String email = "user@email";

        ReplaySubject<RequestModel> emitted = ReplaySubject.create();
        requests.subscribe(emitted);

        cache.subscriptions.onNext(Model.createNewSubscription(email, "item1"));
        cache.subscriptions.onNext(Model.createNewSubscription(email, "item1"));

        requests.onCompleted();

        Iterable items = emitted.toBlocking().toIterable();
        Assertions.assertThat(items).hasSize(1);
    }

}