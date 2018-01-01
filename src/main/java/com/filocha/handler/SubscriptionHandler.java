package com.filocha.handler;

import com.filocha.messaging.messages.finder.ItemFinderRequestMessage;
import com.filocha.messaging.messages.finder.ItemFinderResponseMessage;
import com.filocha.storage.SubscriptionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class SubscriptionHandler {

    @Autowired
    private SubscriptionServiceImpl subscriptionService;

    ItemFinderResponseMessage handleMessage(ItemFinderRequestMessage requestMessage) {
        subscriptionService.createNewSubscription(requestMessage);

        return ItemFinderResponseMessage
                .builder()
                .response("Subscribed!")
                .build();
    }
}
