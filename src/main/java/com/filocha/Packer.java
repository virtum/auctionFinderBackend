package com.filocha;

import com.filocha.storage.SubscriptionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class Packer {

    @Autowired
    private SubscriptionServiceImpl subscriptionService;

    public ConcurrentLinkedQueue setSubscriptionsPackage() {
        ConcurrentLinkedQueue subscriptionsPackage = new ConcurrentLinkedQueue();

        subscriptionsPackage.addAll(subscriptionService.findAllSubscriptions());

        return subscriptionsPackage;
    }
}
