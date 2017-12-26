package com.filocha;

import com.filocha.messaging.client.ClientBusImpl;
import com.filocha.messaging.server.ServerBusImpl;
import org.apache.log4j.BasicConfigurator;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MessagingTests {

    @Test
    public void shouldSendAndReceiveMessageViaActiveMQ() throws ExecutionException, InterruptedException {
        BasicConfigurator.configure();

        ServerBusImpl serverBus = new ServerBusImpl();
        serverBus.setConsumerAndProducer("failover://tcp://192.168.99.100:61616");
        //serverBus.setConsumerAndProducer("failover://tcp://localhost:61616");
        serverBus.addHandler(it -> it + " world", String.class, String.class);

        ClientBusImpl clientBus = new ClientBusImpl();
        clientBus.setConsumerAndProducer("failover://tcp://192.168.99.100:61616");
        //clientBus.setConsumerAndProducer("failover://tcp://localhost:61616");
        CompletableFuture<String> future = clientBus.sendRequest("hello", String.class);

        String response = future.get();

        assertThat(response, equalTo("hello world"));
    }
}
