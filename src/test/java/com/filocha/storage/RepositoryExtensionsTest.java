package com.filocha.storage;

import com.filocha.MongoTestConfig;
import com.filocha.finder.AuctionFinderImpl;
import com.filocha.finder.RequestModel;
import io.reactivex.subjects.PublishSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@ContextConfiguration(classes = {MongoTestConfig.class})
@RunWith(SpringRunner.class)
@TestPropertySource(
        locations = "classpath:applicationTest.properties")
public class RepositoryExtensionsTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void shouldSaveNewSubscription() {
        final PublishSubject<Model> subscriptions = PublishSubject.create();
        final List<SubscriberModel> userAuctions = new ArrayList<>();
        final PublishSubject<RequestModel> requests = PublishSubject.create();
        final PublishSubject<SubscriberModel> repository = RepositoryExtensions.updateSubscriber(mongoTemplate);
        final PublishSubject<Model> emailSender = PublishSubject.create();

        SubscriptionCache.startCache(subscriptions, userAuctions, requests, new AuctionFinderImpl(), repository, emailSender);

        final String email = UUID.randomUUID().toString();
        final String item = UUID.randomUUID().toString();

        subscriptions.onNext(Model.createNewSubscription(email, item));

        final SubscriberModel subscriber = RepositoryExtensions.findSubscriber(mongoTemplate, email).get();

        assertEquals(email, subscriber.getEmail());
        assertEquals(1, subscriber.getAuctions().size());
        assertEquals(item, subscriber.getAuctions().get(0).getItemName());
    }

    @Test
    public void shouldSaveTwoDifferentSubscriptionsForSameUser() {
        final PublishSubject<Model> subscriptions = PublishSubject.create();
        final List<SubscriberModel> userAuctions = new ArrayList<>();
        final PublishSubject<RequestModel> requests = PublishSubject.create();
        final PublishSubject<SubscriberModel> repository = RepositoryExtensions.updateSubscriber(mongoTemplate);
        final PublishSubject<Model> emailSender = PublishSubject.create();

        SubscriptionCache.startCache(subscriptions, userAuctions, requests, new AuctionFinderImpl(), repository, emailSender);

        final String email = UUID.randomUUID().toString();
        final String item1 = UUID.randomUUID().toString();
        final String item2 = UUID.randomUUID().toString();

        subscriptions.onNext(Model.createNewSubscription(email, item1));
        subscriptions.onNext(Model.createNewSubscription(email, item2));

        final SubscriberModel subscriber = RepositoryExtensions.findSubscriber(mongoTemplate, email).get();

        assertEquals(email, subscriber.getEmail());
        assertEquals(2, subscriber.getAuctions().size());
        assertEquals(item1, subscriber.getAuctions().get(0).getItemName());
        assertEquals(item2, subscriber.getAuctions().get(1).getItemName());
    }

    @Test
    public void shouldSaveNewSubscriptionForDifferentUsers() {
        final PublishSubject<Model> subscriptions = PublishSubject.create();
        final List<SubscriberModel> userAuctions = new ArrayList<>();
        final PublishSubject<RequestModel> requests = PublishSubject.create();
        final PublishSubject<SubscriberModel> repository = RepositoryExtensions.updateSubscriber(mongoTemplate);
        final PublishSubject<Model> emailSender = PublishSubject.create();

        SubscriptionCache.startCache(subscriptions, userAuctions, requests, new AuctionFinderImpl(), repository, emailSender);

        final String email1 = UUID.randomUUID().toString();
        final String item1 = UUID.randomUUID().toString();

        final String email2 = UUID.randomUUID().toString();
        final String item2 = UUID.randomUUID().toString();

        subscriptions.onNext(Model.createNewSubscription(email1, item1));
        subscriptions.onNext(Model.createNewSubscription(email2, item2));

        final SubscriberModel subscriber1 = RepositoryExtensions.findSubscriber(mongoTemplate, email1).get();
        assertEquals(email1, subscriber1.getEmail());
        assertEquals(1, subscriber1.getAuctions().size());
        assertEquals(item1, subscriber1.getAuctions().get(0).getItemName());

        final SubscriberModel subscriber2 = RepositoryExtensions.findSubscriber(mongoTemplate, email2).get();
        assertEquals(email2, subscriber2.getEmail());
        assertEquals(1, subscriber2.getAuctions().size());
        assertEquals(item2, subscriber2.getAuctions().get(0).getItemName());
    }

    @Test
    public void shouldNotSaveSameSubscriptionForSameUser() {
        final PublishSubject<Model> subscriptions = PublishSubject.create();
        final List<SubscriberModel> userAuctions = new ArrayList<>();
        final PublishSubject<RequestModel> requests = PublishSubject.create();
        final PublishSubject<SubscriberModel> repository = RepositoryExtensions.updateSubscriber(mongoTemplate);
        final PublishSubject<Model> emailSender = PublishSubject.create();

        SubscriptionCache.startCache(subscriptions, userAuctions, requests, new AuctionFinderImpl(), repository, emailSender);

        final String email = UUID.randomUUID().toString();
        final String item = UUID.randomUUID().toString();

        subscriptions.onNext(Model.createNewSubscription(email, item));
        subscriptions.onNext(Model.createNewSubscription(email, item));

        final SubscriberModel subscriber = RepositoryExtensions.findSubscriber(mongoTemplate, email).get();

        assertEquals(email, subscriber.getEmail());
        assertEquals(1, subscriber.getAuctions().size());
        assertEquals(item, subscriber.getAuctions().get(0).getItemName());
    }

    @Test
    public void shouldSaveUrlsForGivenItem() {
        final PublishSubject<Model> subscriptions = PublishSubject.create();
        final List<SubscriberModel> userAuctions = new ArrayList<>();
        final PublishSubject<RequestModel> requests = PublishSubject.create();
        final PublishSubject<SubscriberModel> repository = RepositoryExtensions.updateSubscriber(mongoTemplate);
        final PublishSubject<Model> emailSender = PublishSubject.create();

        SubscriptionCache.startCache(subscriptions, userAuctions, requests, new AuctionFinderImpl(), repository, emailSender);

        final String email = UUID.randomUUID().toString();
        final String item = UUID.randomUUID().toString();
        final String url1 = UUID.randomUUID().toString();
        final String url2 = UUID.randomUUID().toString();
        final List<String> urls = new ArrayList<>(Arrays.asList(url1, url2));

        subscriptions.onNext(Model.createNewSubscription(email, item));
        subscriptions.onNext(Model.createModelForUpdate(email, item, urls));

        final SubscriberModel subscriber = RepositoryExtensions.findSubscriber(mongoTemplate, email).get();

        final Set<String> savedUrls = subscriber.getAuctions().get(0).getUrls();
        assertEquals(2, savedUrls.size());
        assertTrue(savedUrls.contains(url1));
        assertTrue(savedUrls.contains(url2));
    }

    @Test
    public void shouldUpdateUrlsForGivenItem() {
        final PublishSubject<Model> subscriptions = PublishSubject.create();
        final List<SubscriberModel> userAuctions = new ArrayList<>();
        final PublishSubject<RequestModel> requests = PublishSubject.create();
        final PublishSubject<SubscriberModel> repository = RepositoryExtensions.updateSubscriber(mongoTemplate);
        final PublishSubject<Model> emailSender = PublishSubject.create();

        SubscriptionCache.startCache(subscriptions, userAuctions, requests, new AuctionFinderImpl(), repository, emailSender);

        final String email = UUID.randomUUID().toString();
        final String item = UUID.randomUUID().toString();
        final String url1 = UUID.randomUUID().toString();
        final String url2 = UUID.randomUUID().toString();
        final List<String> urls = new ArrayList<>(Arrays.asList(url1, url2));

        subscriptions.onNext(Model.createNewSubscription(email, item));
        subscriptions.onNext(Model.createModelForUpdate(email, item, urls));

        final String url3 = UUID.randomUUID().toString();
        final List<String> newUrls = new ArrayList<>(Arrays.asList(url3));

        subscriptions.onNext(Model.createModelForUpdate(email, item, newUrls));

        final SubscriberModel subscriber = RepositoryExtensions.findSubscriber(mongoTemplate, email).get();

        final Set<String> savedUrls = subscriber.getAuctions().get(0).getUrls();
        assertEquals(3, savedUrls.size());
        assertTrue(savedUrls.contains(url1));
        assertTrue(savedUrls.contains(url2));
        assertTrue(savedUrls.contains(url3));
    }

    @Test
    public void shouldNotSaveSameUrlsTwice() {
        final PublishSubject<Model> subscriptions = PublishSubject.create();
        final List<SubscriberModel> userAuctions = new ArrayList<>();
        final PublishSubject<RequestModel> requests = PublishSubject.create();
        final PublishSubject<SubscriberModel> repository = RepositoryExtensions.updateSubscriber(mongoTemplate);
        final PublishSubject<Model> emailSender = PublishSubject.create();

        SubscriptionCache.startCache(subscriptions, userAuctions, requests, new AuctionFinderImpl(), repository, emailSender);

        final String email = UUID.randomUUID().toString();
        final String item = UUID.randomUUID().toString();
        final String url1 = UUID.randomUUID().toString();
        final String url2 = UUID.randomUUID().toString();
        final List<String> urls = new ArrayList<>(Arrays.asList(url1, url2));

        subscriptions.onNext(Model.createNewSubscription(email, item));
        subscriptions.onNext(Model.createModelForUpdate(email, item, urls));

        final List<String> newUrls = new ArrayList<>(Arrays.asList(url1));

        subscriptions.onNext(Model.createModelForUpdate(email, item, newUrls));

        final SubscriberModel subscriber = RepositoryExtensions.findSubscriber(mongoTemplate, email).get();

        final Set<String> savedUrls = subscriber.getAuctions().get(0).getUrls();
        assertEquals(2, savedUrls.size());
        assertTrue(savedUrls.contains(url1));
        assertTrue(savedUrls.contains(url2));
    }

}