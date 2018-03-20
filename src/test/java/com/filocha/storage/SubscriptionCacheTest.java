package com.filocha.storage;

import com.filocha.finder.AuctionFinderImpl;
import com.filocha.finder.RequestModel;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SubscriptionCacheTest {

    @Test
    public void shouldAddTwoSubscriptionsForTheSameUser() {
        final PublishSubject<Model> subscriptions = PublishSubject.create();
        final List<SubscriberModel> userAuctions = new ArrayList<>();
        final PublishSubject<RequestModel> requests = PublishSubject.create();
        final PublishSubject<SubscriberModel> repository = PublishSubject.create();
        final PublishSubject<Model> emailSender = PublishSubject.create();

        SubscriptionCache.startCache(subscriptions, userAuctions, requests, new AuctionFinderImpl(), repository, emailSender);

        final String email = "user@email";

        subscriptions.onNext(Model.createNewSubscription(email, "item1"));
        subscriptions.onNext(Model.createNewSubscription(email, "item2"));

        assertEquals(1, userAuctions.size());

        final SubscriberModel subscriber = userAuctions.get(0);
        assertEquals(email, subscriber.getEmail());
        assertEquals(2, subscriber.getAuctions().size());
    }

    @Test
    public void shouldUpdateUrls() {
        final PublishSubject<Model> subscriptions = PublishSubject.create();
        final List<SubscriberModel> userAuctions = new ArrayList<>();
        final PublishSubject<RequestModel> requests = PublishSubject.create();
        final PublishSubject<SubscriberModel> repository = PublishSubject.create();
        final PublishSubject<Model> emailSender = PublishSubject.create();

        SubscriptionCache.startCache(subscriptions, userAuctions, requests, new AuctionFinderImpl(), repository, emailSender);

        final String email = "user@email";
        final String item = "item";

        subscriptions.onNext(Model.createNewSubscription(email, item));

        assertEquals(1, userAuctions.size());

        final SubscriberModel subscriber = userAuctions.get(0);
        assertEquals(email, subscriber.getEmail());

        subscriptions.onNext(Model.createModelForUpdate(email, item, new ArrayList<>(Collections.singletonList("url1"))));
        subscriptions.onNext(Model.createModelForUpdate(email, item, new ArrayList<>(Collections.singletonList("url2"))));

        assertEquals(2, subscriber.getAuctions().get(0).getUrls().size());
    }


    @Test
    public void shouldNotEmitRequestOnDuplicatedSubscription() {
        final PublishSubject<Model> subscriptions = PublishSubject.create();
        final List<SubscriberModel> userAuctions = new ArrayList<>();
        final PublishSubject<RequestModel> requests = PublishSubject.create();
        final PublishSubject<SubscriberModel> repository = PublishSubject.create();
        final PublishSubject<Model> emailSender = PublishSubject.create();

        SubscriptionCache.startCache(subscriptions, userAuctions, requests, new AuctionFinderImpl(), repository, emailSender);

        final String email = "user@email";

        final ReplaySubject<RequestModel> emitted = ReplaySubject.create();
        requests.subscribe(emitted);

        subscriptions.onNext(Model.createNewSubscription(email, "item1"));
        subscriptions.onNext(Model.createNewSubscription(email, "item1"));

        requests.onComplete();

        final Iterable items = emitted.blockingIterable();
        Assertions.assertThat(items).hasSize(1);
    }

}