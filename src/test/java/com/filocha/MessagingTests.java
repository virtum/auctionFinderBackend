package com.filocha;

import com.filocha.messaging.client.ClientBusImpl;
import com.filocha.messaging.server.ServerBusImpl;
import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MessagingTests {

    @Value("${dockerPort}")
    private String dockerPort;

    @Test
    public void shouldSendAndReceiveMessageViaActiveMQ() throws ExecutionException, InterruptedException {
        BasicConfigurator.configure();

        ServerBusImpl serverBus = new ServerBusImpl();
        serverBus.setConsumerAndProducer(dockerPort);
        serverBus.addHandler(it -> it + " world", String.class, String.class);

        ClientBusImpl clientBus = new ClientBusImpl();
        clientBus.setConsumerAndProducer(dockerPort);
        CompletableFuture<String> future = clientBus.sendRequest("hello", String.class);

        String response = future.get();

        assertThat(response, equalTo("hello world"));
    }
}
