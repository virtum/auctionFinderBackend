package com.filocha.handler;

import com.filocha.messaging.messages.finder.ItemFinderRequestMessage;
import com.filocha.messaging.messages.finder.ItemFinderResponseMessage;
import com.filocha.messaging.messages.subscriptions.SubscriptionsRequestModel;
import com.filocha.messaging.messages.subscriptions.SubscriptionsResponseModel;
import com.filocha.messaging.server.ServerBusImpl;
import com.filocha.storage.SubscriberModel;
import com.filocha.storage.SubscriptionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class EventHandler {

    @Value("${activeMqHost}")
    private String activeMqHost;

    @Autowired
    private SubscriptionServiceImpl subscriptionService;

    @PostConstruct
    public void createHandlers() {
        ServerBusImpl serverBus = new ServerBusImpl();
        serverBus.setConsumerAndProducer(activeMqHost);

        serverBus.addHandler(it -> {
            //ExecutorService executor = Executors.newSingleThreadExecutor();
            //executor.execute(() -> subscriptionService.saveSubscription(it.getEmail(), it.getItem()));

            subscriptionService.fillQueueWithRequest(it.getItem(), it.getEmail());

            ItemFinderResponseMessage response = new ItemFinderResponseMessage();
            response.setResponse("Subscribed!");
            System.out.println("User: " + it.getEmail() + " item: " + it.getItem());
            return response;
        }, ItemFinderRequestMessage.class, ItemFinderResponseMessage.class);

        serverBus.addHandler(it -> {
            List<SubscriberModel> subscriptions = subscriptionService.findAllUserSubscriptions(it.getEmail());
            List<String> auctions = new ArrayList<>();

            for (SubscriberModel subscription : subscriptions) {
                auctions.add(subscription.getItem());
            }

            SubscriptionsResponseModel response = new SubscriptionsResponseModel();
            response.setUserSubscriptions(auctions);

            return response;
        }, SubscriptionsRequestModel.class, SubscriptionsResponseModel.class);
    }

}
