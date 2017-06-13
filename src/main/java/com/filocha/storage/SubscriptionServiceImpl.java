package com.filocha.storage;

import com.filocha.finder.AuctionFinder;
import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import https.webapi_allegro_pl.service.ItemsListType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

@Component
public class SubscriptionServiceImpl {

    @Autowired
    private MongoOperations mongoOperations;

    @Autowired
    private AuctionFinder auctionFinder;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    //TODO Replace with synchronous queue
    private ConcurrentLinkedQueue<DoGetItemsListRequest> requests = new ConcurrentLinkedQueue<>();
    private CopyOnWriteArrayList<CompletableFuture<List<ItemsListType>>> responses = new CopyOnWriteArrayList<>();

    public void fillQueueWithRequest(String item) {
        DoGetItemsListRequest request = auctionFinder.createRequest(item);
        requests.add(request);
    }

    public DoGetItemsListRequest getRequestFromQueue() {
        return requests.poll();
    }

    @PostConstruct
    public void sendRequets() {
        // TODO think how many threads do I need
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (requests.size() != 0) {
                    CompletableFuture<List<ItemsListType>> response = auctionFinder.findAuctions(getRequestFromQueue());
                    responses.add(response);
                }

            }
        });
    }

    @PostConstruct
    public void handleResponses() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            while (true) {
                for (CompletableFuture<List<ItemsListType>> response : responses) {
                    final CompletableFuture<List<ItemsListType>> responseFuture = within(response, Duration.ofSeconds(10));
                    responseFuture
                            .thenAccept(it -> {
                                // TODO after removing found item, add request once again to queue with found item to skip it in next request
                                System.out.println("val: " + it);
                            })
                            .exceptionally(throwable -> {
                                System.out.println("Timeout " + throwable);
                                // TODO after removing response from list, add same request once again
                                return null;
                            });
                    responses.remove(response);
                }
            }
        });
    }

    // TODO http://www.nurkiewicz.com/2014/12/asynchronous-timeouts-with.html - add documentation based on url data
    public static <T> CompletableFuture<T> within(CompletableFuture<T> future, Duration duration) {
        final CompletableFuture<T> timeout = failAfter(duration);
        return future.applyToEither(timeout, Function.identity());
    }

    public static <T> CompletableFuture<T> failAfter(Duration duration) {
        final CompletableFuture<T> promise = new CompletableFuture<>();
        scheduler.schedule(() -> {
            final TimeoutException ex = new TimeoutException("Timeout after " + duration);
            return promise.completeExceptionally(ex);
        }, duration.toMillis(), TimeUnit.MILLISECONDS);
        return promise;
    }

    public void saveSubscription(String email, String item) {
        SubscriberModel subscriber = new SubscriberModel(email, item);
        mongoOperations.save(subscriber);
    }

    public SubscriberModel findSubscriber(String email) {
        Query query = new Query(Criteria.where("email").is(email));
        return mongoOperations.findOne(query, SubscriberModel.class);
    }

    public List<SubscriberModel> findAllUserSubscriptions(String email) {
        Query query = new Query(Criteria.where("email").is(email));
        return mongoOperations.find(query, SubscriberModel.class);
    }

}

