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

    @PostConstruct
    public void createHandlers() {
        ServerBusImpl serverBus = new ServerBusImpl();
        serverBus.setConsumerAndProducer(activeMqHost, requestQueue, responseQueue);

        serverBus.addHandler(message -> subscriptionHandler.handleMessage(message),
                ItemFinderRequestMessage.class,
                ItemFinderResponseMessage.class);

        serverBus.addHandler(message -> itemsListHandler.handleMessage(message),
                SubscriptionsRequestModel.class,
                SubscriptionsResponseModel.class);
    }

}
