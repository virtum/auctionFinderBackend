package com.filocha.handler;

import com.filocha.messaging.messages.finder.ItemFinderRequestMessage;
import com.filocha.messaging.messages.finder.ItemFinderResponseMessage;
import com.filocha.messaging.messages.subscriptions.SubscriptionsRequestModel;
import com.filocha.messaging.messages.subscriptions.SubscriptionsResponseModel;
import com.filocha.messaging.server.ServerBusImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class MessageHandler {

    @Value("${activeMqHost}")
    private String activeMqHost;

    @Autowired
    private SubscriptionHandler subscriptionHandler;

    @PostConstruct
    public void createHandlers() {
        ServerBusImpl serverBus = new ServerBusImpl();
        serverBus.setConsumerAndProducer(activeMqHost);

        serverBus.addHandler(message -> subscriptionHandler.handleSubscription(message),
                ItemFinderRequestMessage.class,
                ItemFinderResponseMessage.class);

        serverBus.addHandler(it -> {
            //List<SubscriberModel> subscriptions = repository.findAllUserSubscriptions(it.getEmail());
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
