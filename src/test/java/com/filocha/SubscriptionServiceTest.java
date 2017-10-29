package com.filocha;


import com.filocha.finder.RequestModel;
import com.filocha.finder.ResponseModel;
import com.filocha.storage.SubscriptionServiceImpl;
import com.filocha.storage.SubscriptionStorage;
import https.webapi_allegro_pl.service.ItemsListType;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;

public class SubscriptionServiceTest {

    private SubscriptionServiceImpl service = new SubscriptionServiceImpl();

    @Test
    public void shouldContinueLookingForAnotherAuctions() {
        // adding new urlId
        service.handleUserAuctions(prepareTestData(1));
        assertEquals(1, SubscriptionStorage.userAuctions.get("test").get("item").size());

        // adding same urlId
        service.handleUserAuctions(prepareTestData(1));
        assertEquals(1, SubscriptionStorage.userAuctions.get("test").get("item").size());

        // adding new urlId
        service.handleUserAuctions(prepareTestData(2));
        assertEquals(2, SubscriptionStorage.userAuctions.get("test").get("item").size());
    }

    private ResponseModel prepareTestData(long itemId) {
        ItemsListType item1 = new ItemsListType();
        item1.setItemId(itemId);

        CompletableFuture future = new CompletableFuture();
        future.complete(Arrays.asList(item1));

        RequestModel request = new RequestModel(null, "test", "item");

        return new ResponseModel(future, request, "item");
    }
}
