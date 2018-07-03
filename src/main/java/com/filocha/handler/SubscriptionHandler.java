package com.filocha.handler;

import com.filocha.messaging.messages.finder.ItemFinderRequestMessage;
import com.filocha.messaging.messages.finder.ItemFinderResponseMessage;
import com.filocha.storage.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class SubscriptionHandler {

    @Autowired
    private SubscriptionService subscriptionService;

    /**
     * Creates new subscription for specific user and item.
     *
     * @param request incoming request message
     * @return information that new subscription was received
     */
    ItemFinderResponseMessage createNewSubscription(ItemFinderRequestMessage request) {
        subscriptionService.createNewSubscription(request);

        return ItemFinderResponseMessage
                .builder()
                .response("Subscribed!")
                .build();
    }
}
