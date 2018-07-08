package com.filocha.handler;

import com.filocha.MongoTestConfig;
import com.filocha.messaging.client.ClientBusImpl;
import com.filocha.messaging.messages.finder.ItemFinderRequestMessage;
import com.filocha.messaging.messages.finder.ItemFinderResponseMessage;
import com.filocha.messaging.messages.subscriptionDetails.SubscriptionDetailsRequestModel;
import com.filocha.messaging.messages.subscriptionDetails.SubscriptionDetailsResponseModel;
import com.filocha.messaging.messages.subscriptions.Subscription;
import com.filocha.messaging.messages.subscriptions.SubscriptionsRequestModel;
import com.filocha.messaging.messages.subscriptions.SubscriptionsResponseModel;
import com.filocha.storage.AuctionModel;
import com.filocha.storage.RepositoryExtensions;
import com.filocha.storage.SubscriberModel;
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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {JmsTestConfig.class, MongoTestConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:applicationTest.properties")
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

    @Test
    public void shouldGetSubscriptionDetails() throws ExecutionException, InterruptedException {
        // given
        final String email = UUID.randomUUID().toString();
        final String itemName = UUID.randomUUID().toString();
        final String dateOfFinding = DateTimeFormatter
                .ofPattern("dd/MM/yyyy - HH:mm")
                .format(ZonedDateTime.now());
        final String url = UUID.randomUUID().toString();
        final Set<String> urls = new HashSet<>(Collections.singletonList(url));

        mongoTemplate.save(SubscriberModel
                .builder()
                .email(email)
                .auctions(Arrays.asList(
                        AuctionModel
                                .builder()
                                .urls(urls)
                                .itemName(itemName)
                                .creationDate(dateOfFinding)
                                .build(),
                        AuctionModel
                                .builder()
                                .itemName(UUID.randomUUID().toString())
                                .build()))
                .build());

        final SubscriptionDetailsRequestModel request = SubscriptionDetailsRequestModel
                .builder()
                .email(email)
                .itemName(itemName)
                .build();

        // when
        final CompletableFuture<SubscriptionDetailsResponseModel> responseMessage = clientBus.sendRequest(request,
                SubscriptionDetailsRequestModel.class);

        // then
        final SubscriptionDetailsResponseModel result = responseMessage.get();

        assertEquals(itemName, result.getItemName());
        assertEquals(1, result.getUrls().size());
        assertEquals(url, result.getUrls().get(0));
    }
}