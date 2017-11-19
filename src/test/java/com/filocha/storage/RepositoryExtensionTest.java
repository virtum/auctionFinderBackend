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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class RepositoryExtensionTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void shouldSaveNewSubscription() {
        PublishSubject<Model> subscriptions = PublishSubject.create();
        List<SubscriberModel> userAuctions = new ArrayList<>();
        PublishSubject<RequestModel> requests = PublishSubject.create();

        SubscriptionCache.startCache(subscriptions, userAuctions, requests, new AuctionFinderImpl(), mongoTemplate);

        String email = "user@email";
        String item = "item";
        subscriptions.onNext(Model.createNewSubscription(email, item));

        SubscriberModel subscriber = RepositoryExtension.findSubscriber(mongoTemplate, email);

        assertEquals(email, subscriber.getEmail());
        assertEquals(1, subscriber.getAuctions().size());
        assertEquals(item, subscriber.getAuctions().get(0).getItemName());
    }

}