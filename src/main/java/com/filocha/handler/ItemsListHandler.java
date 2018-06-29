package com.filocha.handler;

import com.filocha.messaging.messages.subscriptions.Subscription;
import com.filocha.messaging.messages.subscriptions.SubscriptionsRequestModel;
import com.filocha.messaging.messages.subscriptions.SubscriptionsResponseModel;
import com.filocha.storage.RepositoryExtensions;
import com.filocha.storage.SubscriberModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ItemsListHandler {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Retrieves all user subscriptions with details.
     *
     * @param request incoming request message
     * @return all user subscriptions with details
     */
    //TODO channge method name
    public SubscriptionsResponseModel handleMessage(SubscriptionsRequestModel request) {
        final Optional<SubscriberModel> subscriber = RepositoryExtensions.findSubscriber(mongoTemplate, request.getEmail());

        final List<Subscription> items = new ArrayList<>();
        subscriber.ifPresent(sub -> sub
                .getAuctions()
                .forEach(item -> items.add(Subscription
                        .builder()
                        .numberOfFoundItems(item.getUrls().size())
                        .itemName(item.getItemName())
                        .creationDate(item.getCreationDate())
                        .build())));

        return SubscriptionsResponseModel
                .builder()
                .userSubscriptions(items)
                .build();
    }

}
