package com.filocha.finder;

import https.webapi_allegro_pl.service.ItemsListType;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuctionFinderImplTest {

    @Autowired
    private AuctionFinderImpl auctionFinder;

    @Ignore
    @Test
    public void shouldFindAnyAuction() throws ExecutionException, InterruptedException {
        CompletableFuture<List<ItemsListType>> auctions = auctionFinder.findAuctions("nokia");
        List<ItemsListType> result = auctions.get();

        assertThat(result.size(), greaterThan(0));
    }

    @Ignore
    @Test
    public void shouldSendMultipleAsyncRequest() throws ExecutionException, InterruptedException {
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < 150; i++) {
            futures.add(auctionFinder.findAuctions("nokia"));
        }

        for (Future<?> future : futures) {
            future.get();
        }
    }

    @Test
    public void should() {
        ConcurrentLinkedQueue subscriptions = new ConcurrentLinkedQueue();
        for (int i = 0; i < 1001; i++) {
            subscriptions.add(i);
        }

        boolean isEmpty = auctionFinder.sendPackages(subscriptions);

        assertThat(isEmpty, equalTo(true));
    }


}
