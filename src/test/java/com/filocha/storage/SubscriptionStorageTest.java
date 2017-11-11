package com.filocha.storage;

import com.filocha.finder.AuctionFinderImpl;
import com.filocha.finder.RequestModel;
import com.filocha.finder.ResponseModel;
import com.filocha.messaging.messages.finder.ItemFinderRequestMessage;
import https.webapi_allegro_pl.service.ItemsListType;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;

public class SubscriptionStorageTest {

    @BeforeClass
    public static void initialize() {
        SubscriptionStorage.initialize(new AuctionFinderImpl());
    }

    @After
    public void clear() {
        SubscriptionStorage.userAuctions1.clear();
    }

    @Test
    public void shouldAddTwoSubscriptionsForTheSameUser() {
        String email = "user@email";

        // create subscriptions
        ItemFinderRequestMessage firstSubscription = prepareTestSubscription(email, "item1");
        ItemFinderRequestMessage secondSubscription = prepareTestSubscription(email, "item2");


        // push subscriptions
        SubscriptionStorage.subscriptions.onNext(firstSubscription);
        SubscriptionStorage.subscriptions.onNext(secondSubscription);

        assertEquals(1, SubscriptionStorage.userAuctions1.size());

        SubscriberModel1 subscriber = SubscriptionStorage.userAuctions1.get(0);
        assertEquals(email, subscriber.getEmail());
        assertEquals(2, subscriber.getAuctions().size());
    }

    @Test
    public void shouldUpdateUrls() {
        String email = "user@email";
        String item = "item";

        // create subscription
        ItemFinderRequestMessage subscription = prepareTestSubscription(email, item);

        // push subscription
        SubscriptionStorage.subscriptions.onNext(subscription);
        assertEquals(1, SubscriptionStorage.userAuctions1.size());

        SubscriberModel1 subscriber = SubscriptionStorage.userAuctions1.get(0);
        assertEquals(email, subscriber.getEmail());

        // prepare test responses
        ResponseModel response1 = preapreTestResponse(email, item);
        ResponseModel response2 = preapreTestResponse(email, item);

        // push responses
        SubscriptionStorage.urls.onNext(response1);
        SubscriptionStorage.urls.onNext(response2);

        assertEquals(2, subscriber.getAuctions().get(0).getUrls().size());
    }

    private ItemFinderRequestMessage prepareTestSubscription(String email, String item) {
        ItemFinderRequestMessage subscription = new ItemFinderRequestMessage();
        subscription.setEmail(email);
        subscription.setItem(item);

        return subscription;
    }

    private ResponseModel preapreTestResponse(String email, String item) {
        ItemsListType items = new ItemsListType();
        items.setItemId(new Random().nextLong());

        CompletableFuture<List<ItemsListType>> response = new CompletableFuture<>();
        response.complete(new ArrayList<>(Collections.singletonList(items)));

        RequestModel request = new RequestModel(null, email, item);

        return new ResponseModel(response, request, item);
    }
}