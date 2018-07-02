package com.filocha.handler;

import com.filocha.messaging.messages.subscriptions.Subscription;
import com.filocha.messaging.messages.subscriptions.SubscriptionsRequestModel;
import com.filocha.messaging.messages.subscriptions.SubscriptionsResponseModel;
import com.filocha.storage.RepositoryExtensions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    public SubscriptionsResponseModel getAllUserSubscriptions(SubscriptionsRequestModel request) {
        final List<Subscription> items = RepositoryExtensions
                .findSubscriber(mongoTemplate, request.getEmail())
                .map(sub -> sub
                        .getAuctions()
                        .stream()
                        .map(item -> Subscription
                                .builder()
                                .numberOfFoundItems(item.getUrls().size())
                                .itemName(item.getItemName())
                                .creationDate(item.getCreationDate())
                                .build())
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        return SubscriptionsResponseModel
                .builder()
                .userSubscriptions(items)
                .build();
    }

}
