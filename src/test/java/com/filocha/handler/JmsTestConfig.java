package com.filocha.handler;

import com.filocha.messaging.client.ClientBus;
import com.filocha.messaging.client.ClientBusImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class JmsTestConfig {

    @Value("${activeMqHost}")
    private String activeMqHost;
    @Value("${requestQueue}")
    private String requestQueue;
    @Value("${responseQueue}")
    private String responseQueue;

    @Autowired
    private MessageHandler messageHandler;

    @Bean
    public ClientBus clientBus() {
        messageHandler.createHandlers();

        ClientBusImpl clientBus = new ClientBusImpl();
        clientBus.setConsumerAndProducer(activeMqHost, responseQueue, requestQueue);

        return clientBus;
    }
}
