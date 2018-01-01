package com.filocha.handler;

import com.filocha.MongoTestConfig;
import com.filocha.messaging.client.ClientBusImpl;
import com.filocha.messaging.messages.finder.ItemFinderRequestMessage;
import com.filocha.messaging.messages.finder.ItemFinderResponseMessage;
import com.filocha.messaging.messages.subscriptions.SubscriptionsRequestModel;
import com.filocha.messaging.messages.subscriptions.SubscriptionsResponseModel;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {JmsTestConfig.class, MongoTestConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        locations = "classpath:applicationTest.properties")
public class MessageHandlerTest {

    @Autowired
    private ClientBusImpl clientBus;

    @SneakyThrows
    @Test
    public void shouldAddNewSubscription() {
        ItemFinderRequestMessage requestMessage = ItemFinderRequestMessage
                .builder()
                .item(UUID.randomUUID().toString())
                .email(UUID.randomUUID().toString())
                .build();

        CompletableFuture<ItemFinderResponseMessage> responseMessage = clientBus.sendRequest(requestMessage,
                ItemFinderRequestMessage.class);

        assertEquals("Subscribed!", responseMessage.get().getResponse());
    }

    @SneakyThrows
    @Test
    public void shouldGetUserSubscriptions() {
        String email = UUID.randomUUID().toString();
        String item = UUID.randomUUID().toString();

        ItemFinderRequestMessage newSubscriptionRequest = ItemFinderRequestMessage
                .builder()
                .item(item)
                .email(email)
                .build();

        clientBus.sendRequest(newSubscriptionRequest, ItemFinderRequestMessage.class).get();

        SubscriptionsRequestModel userItemsRequest = SubscriptionsRequestModel
                .builder()
                .email(email)
                .build();

        CompletableFuture<SubscriptionsResponseModel> responseMessage = clientBus.sendRequest(userItemsRequest,
                SubscriptionsRequestModel.class);
        List<String> userSubscriptions = responseMessage.get().getUserSubscriptions();

        assertEquals(1, userSubscriptions.size());
        assertEquals(item, userSubscriptions.get(0));
    }
}