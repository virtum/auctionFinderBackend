package com.filocha.storage;

import com.filocha.MongoTestConfig;
import com.filocha.finder.AuctionFinderImpl;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;


@ContextConfiguration(classes = {MongoTestConfig.class})
@RunWith(SpringRunner.class)
@TestPropertySource(
        locations = "classpath:applicationTest.properties")
public class RepositoryExtensionsTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void shouldSaveNewSubscription() {
        // given
        final PublishSubject<Model> subscriptions = PublishSubject.create();
        final PublishSubject<SubscriberModel> repository = RepositoryExtensions.updateSubscriber(mongoTemplate);

        SubscriptionCache.startCache(subscriptions, PublishSubject.create(), new AuctionFinderImpl(),
                repository, PublishSubject.create());

        final String email = "email";
        final String item = "item";

        // when
        subscriptions.onNext(Model.createNewSubscription(email, item));

        // then: we have to wait for item to be stored in database
        final SubscriberModel subscriber = Observable
                .interval(100, TimeUnit.MILLISECONDS)
                .map(i -> RepositoryExtensions.findSubscriber(mongoTemplate, email))
                .filter(Optional::isPresent)
                .timeout(10, TimeUnit.SECONDS)
                .blockingFirst()
                .get();

        assertEquals(email, subscriber.getEmail());
        assertEquals(1, subscriber.getAuctions().size());
        assertEquals(item, subscriber.getAuctions().get(0).getItemName());
    }

    @Test
    public void shouldFindAllSubscribers() {
        // given
        final PublishSubject<Model> subscriptions = PublishSubject.create();
        final PublishSubject<SubscriberModel> repository = RepositoryExtensions.updateSubscriber(mongoTemplate);

        SubscriptionCache.startCache(subscriptions, PublishSubject.create(), new AuctionFinderImpl(),
                repository, PublishSubject.create());

        // when
        subscriptions.onNext(Model.createNewSubscription("email1", "item1"));
        subscriptions.onNext(Model.createNewSubscription("email2", "item2"));

        // then: we have to wait for items to be stored in database
        Observable
                .interval(100, TimeUnit.MILLISECONDS)
                .map(i -> RepositoryExtensions.getAllSubscribers(mongoTemplate))
                .filter(list -> list.size() == 2)
                .timeout(10, TimeUnit.SECONDS)
                .blockingFirst();
    }
}