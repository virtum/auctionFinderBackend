package com.filocha.handler;

import com.filocha.messaging.messages.subscriptionDetails.SubscriptionDetailsRequestModel;
import com.filocha.messaging.messages.subscriptionDetails.SubscriptionDetailsResponseModel;
import com.filocha.storage.AuctionModel;
import com.filocha.storage.RepositoryExtensions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SubscriptionDetailsHandler {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Retrieves details for specific item for specific user.
     *
     * @param request incoming request message
     * @return details for given item
     */
    public SubscriptionDetailsResponseModel getSubscriptionDetails(final SubscriptionDetailsRequestModel request) {
        final List<String> urls = RepositoryExtensions
                .findSubscriber(mongoTemplate, request.getEmail())
                .orElseThrow(NoSuchElementException::new)
                .getAuctions()
                .stream()
                .filter(auction -> auction.getItemName().equals(request.getItemName()))
                .map(AuctionModel::getUrls)
                .flatMap(Set::stream)
                .collect(Collectors.toList());

        return SubscriptionDetailsResponseModel
                .builder()
                .itemName(request.getItemName())
                .urls(urls)
                .build();
    }

}
