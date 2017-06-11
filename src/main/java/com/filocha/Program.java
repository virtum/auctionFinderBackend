package com.filocha;

import com.filocha.messaging.messages.finder.ItemFinderRequestMessage;
import com.filocha.messaging.messages.finder.ItemFinderResponseMessage;
import com.filocha.messaging.messages.subscriptions.SubscriptionsRequestModel;
import com.filocha.messaging.messages.subscriptions.SubscriptionsResponseModel;
import com.filocha.messaging.server.ServerBusImpl;
import com.filocha.storage.SubscriberModel;
import com.filocha.storage.SubscriptionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class Program {

    @Value("${activeMqHost}")
    private String activeMqHost;

    @Autowired
    private SubscriptionServiceImpl subscriptionService;

    @PostConstruct
    public void startServerBus() {
        ServerBusImpl serverBus = new ServerBusImpl();
        serverBus.setConsumerAndProducer(activeMqHost);
        serverBus.addHandler(it -> {
            // TODO move saving subscription to db to another place (use executor to handle with saving)
            //subscriptionService.saveSubscription(it.getEmail(), it.getItem());
            subscriptionService.fillQueueWithRequest(it.getItem());

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

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(Program.class, args);
    }

}
