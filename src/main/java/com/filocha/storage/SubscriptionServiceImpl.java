package com.filocha.storage;

import com.filocha.finder.AuctionFinder;
import com.filocha.finder.RequestModel;
import com.filocha.finder.ResponseModel;
import com.filocha.messaging.messages.finder.ItemFinderRequestMessage;
import com.filocha.throttle.ThrottleGuard;
import https.webapi_allegro_pl.service.ItemsListType;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SubscriptionServiceImpl {
    @Autowired
    private AuctionFinder auctionFinder;
    @Autowired
    private MongoTemplate mongoTemplate;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private PublishSubject<RequestModel> requests;
    private PublishSubject<Model> subscriptions;

    @PostConstruct
    private void initialize() {
        requests = PublishSubject.create();
        subscriptions = PublishSubject.create();

        SubscriptionCache.startCache(subscriptions, new ArrayList<>(), requests, auctionFinder, mongoTemplate);
        //Closable =  static

        handleResponses1();
        sendRequets1();
    }

    public void pushSubscription(ItemFinderRequestMessage request) {
        subscriptions.onNext(Model.createNewSubscription(request.getEmail(), request.getItem()));
    }

    private PublishSubject<ResponseModel> responses = PublishSubject.create();

    private void sendRequets1() {
        ThrottleGuard
                .throttle(requests, 1000, 100)
                .observeOn(Schedulers.io())
                .subscribe(request -> {
                    CompletableFuture<List<ItemsListType>> response = auctionFinder.findAuctions(request.getRequest());

                    ResponseModel responseModel = new ResponseModel(response, request, request.getItem());

                    responses.onNext(responseModel);
                });
    }

    private void handleResponses1() {
        responses
                .observeOn(Schedulers.io())
                .subscribe(response -> {
                    final CompletableFuture<List<ItemsListType>> responseFuture = within(response.getResponse(), Duration.ofSeconds(10));
                    responseFuture
                            .thenAccept(it -> {
                                List<String> urls = prepareAuctionsIdList(response);

                                subscriptions.onNext(Model.createModelForUpdate(response.getUserEmail(), response.getItem(), urls));
                                requests.onNext(response.getRequest());
                            })
                            .exceptionally(throwable -> {
                                requests.onNext(response.getRequest());
                                return null;
                            });
                });
    }

    @SneakyThrows
    private static List<String> prepareAuctionsIdList(ResponseModel responseWithUrls) {
        // get() method is allowed here because we already have completed completableFuture
        return responseWithUrls.getResponse().get()
                .stream()
                .map(ItemsListType::getItemId)
                .map(url -> "http://allegro.pl/i" + url + ".html\n")
                .collect(Collectors.toList());
    }

    // TODO http://www.nurkiewicz.com/2014/12/asynchronous-timeouts-with.html - add documentation based on url data
    public static <T> CompletableFuture<T> within(CompletableFuture<T> future, Duration duration) {
        final CompletableFuture<T> timeout = failAfter(duration);
        return future.applyToEither(timeout, Function.identity());
    }

    private static <T> CompletableFuture<T> failAfter(Duration duration) {
        final CompletableFuture<T> promise = new CompletableFuture<>();
        scheduler.schedule(() -> {
            final TimeoutException ex = new TimeoutException("Timeout after " + duration);
            return promise.completeExceptionally(ex);
        }, duration.toMillis(), TimeUnit.MILLISECONDS);
        return promise;
    }
}

