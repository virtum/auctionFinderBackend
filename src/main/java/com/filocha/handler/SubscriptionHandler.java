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

    /**
     * Creates new subscription for specific user and item.
     *
     * @param request incoming request message
     * @return information that new subscription was received
     */
    //TODO change method name
    ItemFinderResponseMessage handleMessage(ItemFinderRequestMessage request) {
        subscriptionService.createNewSubscription(request);

        return ItemFinderResponseMessage
                .builder()
                .response("Subscribed!")
                .build();
    }
}
