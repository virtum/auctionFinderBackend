package com.filocha.storage;

import com.filocha.email.EmailSender;
import com.filocha.finder.AuctionFinder;
import com.filocha.finder.RequestModel;
import com.filocha.finder.ResponseModel;
import com.filocha.throttle.ThrottleGuard;
import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import https.webapi_allegro_pl.service.ItemsListType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.subjects.PublishSubject;

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

    private PublishSubject<RequestModel> requests = PublishSubject.create();
    private PublishSubject<ResponseModel> responses = PublishSubject.create();

    public void fillQueueWithRequest(String item, String userEmail) {
        DoGetItemsListRequest request = auctionFinder.createRequest(item);

        RequestModel model = new RequestModel(request, userEmail);

        requests.onNext(model);
    }


    @PostConstruct
    public void sendRequets() {
        Observable<RequestModel> output = ThrottleGuard.throttle(requests, 1000, 100);

        //TODO subscribeOn or observeOn? Remind
        output.subscribe(item -> {
            RequestModel requestModel = item;
            CompletableFuture<List<ItemsListType>> response = auctionFinder.findAuctions(requestModel.getRequest());

            ResponseModel responseModel = new ResponseModel();
            responseModel.setUserEmail(requestModel.getUserEmail());
            responseModel.setResponse(response);

            responses.onNext(responseModel);
        });
    }

    @PostConstruct
    public void handleResponses() {
        responses.subscribe(response -> {
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
            //responses.remove(response);
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

