package com.filocha.handler;

import com.filocha.messaging.messages.finder.ItemFinderRequestMessage;
import com.filocha.messaging.messages.finder.ItemFinderResponseMessage;
import com.filocha.messaging.messages.subscriptionDetails.SubscriptionDetailsRequestModel;
import com.filocha.messaging.messages.subscriptionDetails.SubscriptionDetailsResponseModel;
import com.filocha.messaging.messages.subscriptions.SubscriptionsRequestModel;
import com.filocha.messaging.messages.subscriptions.SubscriptionsResponseModel;
import com.filocha.messaging.server.ServerBusImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class MessageHandler {

    @Value("${activeMqHost}")
    private String activeMqHost;
    @Value("${requestQueue}")
    private String requestQueue;
    @Value("${responseQueue}")
    private String responseQueue;

    @Autowired
    private SubscriptionHandler subscriptionHandler;
    @Autowired
    private ItemsListHandler itemsListHandler;
    @Autowired
    private SubscriptionDetailsHandler subscriptionDetailsHandler;

    private ServerBusImpl serverBus;

    @PostConstruct
    public void createHandlers() {
        serverBus = new ServerBusImpl();
        serverBus.setConsumerAndProducer(activeMqHost, requestQueue, responseQueue);

        serverBus.addHandler(message -> subscriptionHandler.handleMessage(message),
                ItemFinderRequestMessage.class,
                ItemFinderResponseMessage.class);

        serverBus.addHandler(message -> itemsListHandler.handleMessage(message),
                SubscriptionsRequestModel.class,
                SubscriptionsResponseModel.class);

        serverBus.addHandler(message -> subscriptionDetailsHandler.handleMessage(message),
                SubscriptionDetailsRequestModel.class,
                SubscriptionDetailsResponseModel.class);
    }

    @PreDestroy
    public void close() {
        serverBus.close();
    }

}
