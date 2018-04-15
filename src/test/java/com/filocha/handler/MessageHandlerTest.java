package com.filocha.handler;

import com.filocha.MongoTestConfig;
import com.filocha.messaging.client.ClientBusImpl;
import com.filocha.messaging.messages.finder.ItemFinderRequestMessage;
import com.filocha.messaging.messages.finder.ItemFinderResponseMessage;
import com.filocha.messaging.messages.subscriptions.Subscription;
import com.filocha.messaging.messages.subscriptions.SubscriptionsRequestModel;
import com.filocha.messaging.messages.subscriptions.SubscriptionsResponseModel;
import com.filocha.storage.RepositoryExtensions;
import io.reactivex.Observable;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {JmsTestConfig.class, MongoTestConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        locations = "classpath:applicationTest.properties")
public class MessageHandlerTest {

    @Autowired
    private ClientBusImpl clientBus;
    @Autowired
    private MongoTemplate mongoTemplate;

    @SneakyThrows
    @Test
    public void shouldAddNewSubscription() {
        final ItemFinderRequestMessage requestMessage = ItemFinderRequestMessage
                .builder()
                .item(UUID.randomUUID().toString())
                .email(UUID.randomUUID().toString())
                .build();

        final CompletableFuture<ItemFinderResponseMessage> responseMessage = clientBus.sendRequest(requestMessage,
                ItemFinderRequestMessage.class);

        assertEquals("Subscribed!", responseMessage.get().getResponse());
    }

    @Test
    public void shouldGetUserSubscriptions() throws ExecutionException, InterruptedException {
        final String email = UUID.randomUUID().toString();
        final String item = UUID.randomUUID().toString();

        final ItemFinderRequestMessage newSubscriptionRequest = ItemFinderRequestMessage
                .builder()
                .item(item)
                .email(email)
                .build();

        clientBus.sendRequest(newSubscriptionRequest, ItemFinderRequestMessage.class);

        Observable
                .interval(100, TimeUnit.MILLISECONDS)
                .map(i -> RepositoryExtensions.findSubscriber(mongoTemplate, email))
                .filter(Optional::isPresent)
                .timeout(30, TimeUnit.SECONDS)
                .blockingFirst();

        final SubscriptionsRequestModel userItemsRequest = SubscriptionsRequestModel
                .builder()
                .email(email)
                .build();

        final CompletableFuture<SubscriptionsResponseModel> responseMessage = clientBus.sendRequest(userItemsRequest,
                SubscriptionsRequestModel.class);

        final List<Subscription> userSubscriptions = responseMessage.get().getUserSubscriptions();

        assertEquals(1, userSubscriptions.size());
        assertEquals(item, userSubscriptions.get(0).getItemName());
    }
}