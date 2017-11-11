package com.filocha.handler;

import com.filocha.messaging.messages.finder.ItemFinderRequestMessage;
import com.filocha.messaging.messages.finder.ItemFinderResponseMessage;
import com.filocha.messaging.messages.subscriptions.SubscriptionsRequestModel;
import com.filocha.messaging.messages.subscriptions.SubscriptionsResponseModel;
import com.filocha.messaging.server.ServerBusImpl;
import com.filocha.storage.SubscriberModel;
import com.filocha.storage.SubscriberRepository;
import com.filocha.storage.SubscriptionCache;
import com.filocha.storage.SubscriptionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class EventHandler {

    @Value("${activeMqHost}")
    private String activeMqHost;

    @Autowired
    private SubscriptionServiceImpl subscriptionService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SubscriberRepository repository;

    @PostConstruct
    public void createHandlers() {
        ServerBusImpl serverBus = new ServerBusImpl();
        serverBus.setConsumerAndProducer(activeMqHost);

        serverBus.addHandler(message -> {
            //subscriptionCache.addSubscription(message.getEmail(), message.getItem());
            SubscriptionCache.subscriptions
                    .onNext(message);

            //subscriptionService.fillQueueWithRequest(it.getItem(), it.getEmail());

            ItemFinderResponseMessage response = new ItemFinderResponseMessage();
            response.setResponse("Subscribed!");
            System.out.println("User: " + message.getEmail() + " item: " + message.getItem());
            return response;
        }, ItemFinderRequestMessage.class, ItemFinderResponseMessage.class);

        serverBus.addHandler(it -> {
            List<SubscriberModel> subscriptions = repository.findAllUserSubscriptions(it.getEmail());
            List<String> auctions = new ArrayList<>();

            //FIXME change for due to new model
//
//            for (SubscriberModel subscription : subscriptions) {
//                auctions.add(subscription.getItem());
//            }

            SubscriptionsResponseModel response = new SubscriptionsResponseModel();
            response.setUserSubscriptions(auctions);

            return response;
        }, SubscriptionsRequestModel.class, SubscriptionsResponseModel.class);
    }

}
