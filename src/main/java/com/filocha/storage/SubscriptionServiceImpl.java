package com.filocha.storage;

import com.filocha.email.EmailSender;
import com.filocha.finder.AuctionFinder;
import com.filocha.finder.RequestModel;
import com.filocha.finder.ResponseModel;
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

    @Autowired
    private EmailSender emailSender;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    //TODO Replace with synchronous queue
    private ConcurrentLinkedQueue<RequestModel> requests = new ConcurrentLinkedQueue<>();
    private CopyOnWriteArrayList<ResponseModel> responses = new CopyOnWriteArrayList<>();

    public void fillQueueWithRequest(String item, String userEmail) {
        DoGetItemsListRequest request = auctionFinder.createRequest(item);

        RequestModel model = new RequestModel();
        model.setRequest(request);
        model.setUserEmail(userEmail);

        requests.add(model);
    }

    public RequestModel getRequestFromQueue() {
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
                    RequestModel requestModel = getRequestFromQueue();
                    CompletableFuture<List<ItemsListType>> response = auctionFinder.findAuctions(requestModel.getRequest());

                    ResponseModel responseModel = new ResponseModel();
                    responseModel.setUserEmail(requestModel.getUserEmail());
                    responseModel.setResponse(response);

                    responses.add(responseModel);
                }

            }
        });
    }

    @PostConstruct
    public void handleResponses() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            while (true) {
                for (ResponseModel response : responses) {
                    final CompletableFuture<List<ItemsListType>> responseFuture = within(response.getResponse(), Duration.ofSeconds(10));
                    responseFuture
                            .thenAccept(it -> {
                                // TODO after removing found item, add request once again to queue with found item to skip it in next request
                                System.out.println("val: " + it);

                                // TODO send email using user email and url to auction
                                try {
                                    emailSender.sendEmail(response.getUserEmail(), createAuctionUrlFromAuctionId(response.getResponse().get().get(0).getItemId()));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }
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

    public String createAuctionUrlFromAuctionId(Long auctionId) {
        String url = "http://allegro.pl/i" + auctionId + ".html";
        return url;
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

