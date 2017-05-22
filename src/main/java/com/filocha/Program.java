package com.filocha;

import com.filocha.messaging.messages.finder.ItemFinderRequestMessage;
import com.filocha.messaging.messages.finder.ItemFinderResponseMessage;
import com.filocha.messaging.server.ServerBusImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Program {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(Program.class, args);
        ServerBusImpl serverBus = new ServerBusImpl();
        //serverBus.setConsumerAndProducer("failover://tcp://192.168.99.100:61616");
        serverBus.setConsumerAndProducer("failover://tcp://localhost:61616");
        serverBus.addHandler(it -> {
            ItemFinderResponseMessage response = new ItemFinderResponseMessage();
            response.setResponse(it.getItemName() + " :work");
            System.out.println(it.getItemName());
            return response;
        }, ItemFinderRequestMessage.class, ItemFinderResponseMessage.class);


    }

}
