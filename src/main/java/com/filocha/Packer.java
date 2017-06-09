package com.filocha;

import com.filocha.storage.SubscriberModel;
import com.filocha.storage.SubscriptionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class Packer {

    @Autowired
    private SubscriptionServiceImpl subscriptionService;

    public ConcurrentLinkedQueue<SubscriberModel> setSubscriptionsPackage() {
        ConcurrentLinkedQueue<SubscriberModel> subscriptionsPackage = new ConcurrentLinkedQueue();

        subscriptionsPackage.addAll(subscriptionService.findAllSubscriptions());

        return subscriptionsPackage;
    }
}
