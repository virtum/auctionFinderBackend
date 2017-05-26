package com.filocha;

import com.filocha.messaging.messages.finder.ItemFinderRequestMessage;
import com.filocha.messaging.messages.finder.ItemFinderResponseMessage;
import com.filocha.messaging.server.ServerBusImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class Program {

    @Value("${dockerPort}")
    private String dockerPort;

    @PostConstruct
    public void startServerBus() {
        ServerBusImpl serverBus = new ServerBusImpl();
        serverBus.setConsumerAndProducer(dockerPort);
        serverBus.addHandler(it -> {
            ItemFinderResponseMessage response = new ItemFinderResponseMessage();
            response.setResponse(it.getItemName() + " :work");
            System.out.println(it.getItemName());
            return response;
        }, ItemFinderRequestMessage.class, ItemFinderResponseMessage.class);
    }

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(Program.class, args);
    }

}
