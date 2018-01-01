package com.filocha.handler;

import com.filocha.messaging.messages.subscriptions.SubscriptionsRequestModel;
import com.filocha.messaging.messages.subscriptions.SubscriptionsResponseModel;
import com.filocha.storage.AuctionModel;
import com.filocha.storage.RepositoryExtensions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemsListHandler {

    @Autowired
    private MongoTemplate mongoTemplate;

    public SubscriptionsResponseModel handleMessage(SubscriptionsRequestModel message) {
        // TODO check if empty then stream
        List<String> auctions = RepositoryExtensions
                .findSubscriber(mongoTemplate, message.getEmail())
                .getAuctions()
                .stream()
                .map(AuctionModel::getItemName)
                .collect(Collectors.toList());

        return SubscriptionsResponseModel
                .builder()
                .userSubscriptions(auctions)
                .build();
    }

}
