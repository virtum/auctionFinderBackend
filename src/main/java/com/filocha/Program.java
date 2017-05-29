package com.filocha;

import com.filocha.messaging.messages.finder.ItemFinderRequestMessage;
import com.filocha.messaging.messages.finder.ItemFinderResponseMessage;
import com.filocha.messaging.server.ServerBusImpl;
import com.filocha.storage.SubscriptionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

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
            subscriptionService.saveSubscription(it.getEmail(), it.getItem());

            ItemFinderResponseMessage response = new ItemFinderResponseMessage();
            response.setResponse("Subscribed!");
            System.out.println("User: " + it.getEmail() + " item: " + it.getItem());
            return response;
        }, ItemFinderRequestMessage.class, ItemFinderResponseMessage.class);
    }

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(Program.class, args);
    }

}
