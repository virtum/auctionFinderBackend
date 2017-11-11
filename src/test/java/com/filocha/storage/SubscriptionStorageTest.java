package com.filocha.storage;

import com.filocha.messaging.messages.finder.ItemFinderRequestMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SubscriptionStorageTest {

    @Autowired
    private SubscriptionStorage storage;

    @Test
    public void shouldAddTwoSubscriptionsForTheSameUser() {
        String userEmail = "user@email";

        ItemFinderRequestMessage firstSubscription = new ItemFinderRequestMessage();
        firstSubscription.setEmail(userEmail);
        firstSubscription.setItem("item1");

        ItemFinderRequestMessage secondSubscription = new ItemFinderRequestMessage();
        secondSubscription.setEmail(userEmail);
        secondSubscription.setItem("item2");

        SubscriptionStorage.subscriptions.onNext(firstSubscription);
        SubscriptionStorage.subscriptions.onNext(secondSubscription);

        assertEquals(1, SubscriptionStorage.userAuctions1.size());

        SubscriberModel1 subscriber = SubscriptionStorage.userAuctions1.get(0);
        assertEquals(userEmail, subscriber.getEmail());
        assertEquals(2, subscriber.getAuctions().size());
    }

}