package com.filocha.handler;

import com.filocha.messaging.client.ClientBusImpl;
import com.filocha.messaging.messages.finder.ItemFinderRequestMessage;
import com.filocha.messaging.messages.finder.ItemFinderResponseMessage;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MessageHandlerTest {

    @Value("${activeMqHost}")
    private String activeMqHost;
    @Value("${requestQueue}")
    private String requestQueue;
    @Value("${responseQueue}")
    private String responseQueue;

    @Autowired
    private MessageHandler messageHandler;

    @SneakyThrows
    @Test
    public void shouldAddNewSubscription() {
        messageHandler.createHandlers();

        ClientBusImpl clientBus = new ClientBusImpl();
        clientBus.setConsumerAndProducer(activeMqHost, responseQueue, requestQueue);

        ItemFinderRequestMessage requestMessage = ItemFinderRequestMessage
                .builder()
                .item("testItem")
                .email("testEmail")
                .build();

        CompletableFuture<ItemFinderResponseMessage> responseMessage = clientBus.sendRequest(requestMessage, ItemFinderRequestMessage.class);

        assertEquals("Subscribed!", responseMessage.get().getResponse());
    }
}