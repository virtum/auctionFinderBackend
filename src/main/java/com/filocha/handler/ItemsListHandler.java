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

    public SubscriptionsResponseModel handleMessage(SubscriptionsRequestModel message) {
        final List<Subscription> items = RepositoryExtensions
                .findSubscriber(mongoTemplate, message.getEmail())
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
