package com.filocha.storage;

import com.filocha.finder.AuctionFinderImpl;
import com.filocha.finder.RequestModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rx.subjects.PublishSubject;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class RepositoryExtensionTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    // TODO change database for test

    @Test
    public void shouldSaveNewSubscription() {
        PublishSubject<Model> subscriptions = PublishSubject.create();
        List<SubscriberModel> userAuctions = new ArrayList<>();
        PublishSubject<RequestModel> requests = PublishSubject.create();

        SubscriptionCache.startCache(subscriptions, userAuctions, requests, new AuctionFinderImpl(), mongoTemplate);

        String email = UUID.randomUUID().toString();
        String item = UUID.randomUUID().toString();

        subscriptions.onNext(Model.createNewSubscription(email, item));

        SubscriberModel subscriber = RepositoryExtension.findSubscriber(mongoTemplate, email);

        assertEquals(email, subscriber.getEmail());
        assertEquals(1, subscriber.getAuctions().size());
        assertEquals(item, subscriber.getAuctions().get(0).getItemName());
    }

    @Test
    public void shouldSaveTwoDifferentSubscriptionsForSameUser() {
        PublishSubject<Model> subscriptions = PublishSubject.create();
        List<SubscriberModel> userAuctions = new ArrayList<>();
        PublishSubject<RequestModel> requests = PublishSubject.create();

        SubscriptionCache.startCache(subscriptions, userAuctions, requests, new AuctionFinderImpl(), mongoTemplate);

        String email = UUID.randomUUID().toString();
        String item1 = UUID.randomUUID().toString();
        String item2 = UUID.randomUUID().toString();

        subscriptions.onNext(Model.createNewSubscription(email, item1));
        subscriptions.onNext(Model.createNewSubscription(email, item2));

        SubscriberModel subscriber = RepositoryExtension.findSubscriber(mongoTemplate, email);

        assertEquals(email, subscriber.getEmail());
        assertEquals(2, subscriber.getAuctions().size());
        assertEquals(item1, subscriber.getAuctions().get(0).getItemName());
        assertEquals(item2, subscriber.getAuctions().get(1).getItemName());
    }

    @Test
    public void shouldSaveNewSubscriptionForDifferentUsers() {
        PublishSubject<Model> subscriptions = PublishSubject.create();
        List<SubscriberModel> userAuctions = new ArrayList<>();
        PublishSubject<RequestModel> requests = PublishSubject.create();

        SubscriptionCache.startCache(subscriptions, userAuctions, requests, new AuctionFinderImpl(), mongoTemplate);

        String email1 = UUID.randomUUID().toString();
        String item1 = UUID.randomUUID().toString();

        String email2 = UUID.randomUUID().toString();
        String item2 = UUID.randomUUID().toString();

        subscriptions.onNext(Model.createNewSubscription(email1, item1));
        subscriptions.onNext(Model.createNewSubscription(email2, item2));

        SubscriberModel subscriber1 = RepositoryExtension.findSubscriber(mongoTemplate, email1);
        assertEquals(email1, subscriber1.getEmail());
        assertEquals(1, subscriber1.getAuctions().size());
        assertEquals(item1, subscriber1.getAuctions().get(0).getItemName());

        SubscriberModel subscriber2 = RepositoryExtension.findSubscriber(mongoTemplate, email2);
        assertEquals(email2, subscriber2.getEmail());
        assertEquals(1, subscriber2.getAuctions().size());
        assertEquals(item2, subscriber2.getAuctions().get(0).getItemName());
    }

    @Test
    public void shouldNotSaveSameSubscriptionForSameUser() {
        PublishSubject<Model> subscriptions = PublishSubject.create();
        List<SubscriberModel> userAuctions = new ArrayList<>();
        PublishSubject<RequestModel> requests = PublishSubject.create();

        SubscriptionCache.startCache(subscriptions, userAuctions, requests, new AuctionFinderImpl(), mongoTemplate);

        String email = UUID.randomUUID().toString();
        String item = UUID.randomUUID().toString();

        subscriptions.onNext(Model.createNewSubscription(email, item));
        subscriptions.onNext(Model.createNewSubscription(email, item));

        SubscriberModel subscriber = RepositoryExtension.findSubscriber(mongoTemplate, email);

        assertEquals(email, subscriber.getEmail());
        assertEquals(1, subscriber.getAuctions().size());
        assertEquals(item, subscriber.getAuctions().get(0).getItemName());
    }

    @Test
    public void shouldSaveUrlsForGivenItem() {
        PublishSubject<Model> subscriptions = PublishSubject.create();
        List<SubscriberModel> userAuctions = new ArrayList<>();
        PublishSubject<RequestModel> requests = PublishSubject.create();

        SubscriptionCache.startCache(subscriptions, userAuctions, requests, new AuctionFinderImpl(), mongoTemplate);

        String email = UUID.randomUUID().toString();
        String item = UUID.randomUUID().toString();
        String url1 = UUID.randomUUID().toString();
        String url2 = UUID.randomUUID().toString();
        List<String> urls = new ArrayList<>(Arrays.asList(url1, url2));

        subscriptions.onNext(Model.createNewSubscription(email, item));
        subscriptions.onNext(Model.createModelForUpdate(email, item, urls));

        SubscriberModel subscriber = RepositoryExtension.findSubscriber(mongoTemplate, email);

        Set<String> savedUrls = subscriber.getAuctions().get(0).getUrls();
        assertEquals(2, savedUrls.size());
        assertTrue(savedUrls.contains(url1));
        assertTrue(savedUrls.contains(url2));
    }

    @Test
    public void shouldUpdateUrlsForGivenItem() {
        PublishSubject<Model> subscriptions = PublishSubject.create();
        List<SubscriberModel> userAuctions = new ArrayList<>();
        PublishSubject<RequestModel> requests = PublishSubject.create();

        SubscriptionCache.startCache(subscriptions, userAuctions, requests, new AuctionFinderImpl(), mongoTemplate);

        String email = UUID.randomUUID().toString();
        String item = UUID.randomUUID().toString();
        String url1 = UUID.randomUUID().toString();
        String url2 = UUID.randomUUID().toString();
        List<String> urls = new ArrayList<>(Arrays.asList(url1, url2));

        subscriptions.onNext(Model.createNewSubscription(email, item));
        subscriptions.onNext(Model.createModelForUpdate(email, item, urls));

        String url3 = UUID.randomUUID().toString();
        List<String> newUrls = new ArrayList<>(Arrays.asList(url3));

        subscriptions.onNext(Model.createModelForUpdate(email, item, newUrls));

        SubscriberModel subscriber = RepositoryExtension.findSubscriber(mongoTemplate, email);

        Set<String> savedUrls = subscriber.getAuctions().get(0).getUrls();
        assertEquals(3, savedUrls.size());
        assertTrue(savedUrls.contains(url1));
        assertTrue(savedUrls.contains(url2));
        assertTrue(savedUrls.contains(url3));
    }

    @Test
    public void shouldNotSaveSameUrlsTwice() {
        PublishSubject<Model> subscriptions = PublishSubject.create();
        List<SubscriberModel> userAuctions = new ArrayList<>();
        PublishSubject<RequestModel> requests = PublishSubject.create();

        SubscriptionCache.startCache(subscriptions, userAuctions, requests, new AuctionFinderImpl(), mongoTemplate);

        String email = UUID.randomUUID().toString();
        String item = UUID.randomUUID().toString();
        String url1 = UUID.randomUUID().toString();
        String url2 = UUID.randomUUID().toString();
        List<String> urls = new ArrayList<>(Arrays.asList(url1, url2));

        subscriptions.onNext(Model.createNewSubscription(email, item));
        subscriptions.onNext(Model.createModelForUpdate(email, item, urls));

        List<String> newUrls = new ArrayList<>(Arrays.asList(url1));

        subscriptions.onNext(Model.createModelForUpdate(email, item, newUrls));

        SubscriberModel subscriber = RepositoryExtension.findSubscriber(mongoTemplate, email);

        Set<String> savedUrls = subscriber.getAuctions().get(0).getUrls();
        assertEquals(2, savedUrls.size());
        assertTrue(savedUrls.contains(url1));
        assertTrue(savedUrls.contains(url2));
    }

}