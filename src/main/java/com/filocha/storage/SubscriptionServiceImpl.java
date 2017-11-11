package com.filocha.storage;

import com.filocha.email.EmailSender;
import com.filocha.finder.AuctionFinder;
import com.filocha.finder.ResponseModel;
import com.filocha.throttle.ThrottleGuard;
import https.webapi_allegro_pl.service.ItemsListType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.schedulers.Schedulers;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

@Component
public class SubscriptionServiceImpl {
    @Autowired
    private AuctionFinder auctionFinder;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private SubscriberRepository repository;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    private void initialize() {
        // userAuctions = repository.getUsersWithItems();
        handleResponses();
        sendRequets();
    }

    private void sendRequets() {
        ThrottleGuard
                .throttle(SubscriptionCache.requests, 1000, 100)
                .observeOn(Schedulers.computation())
                .subscribe(request -> {
                    CompletableFuture<List<ItemsListType>> response = auctionFinder.findAuctions(request.getRequest());

                    ResponseModel responseModel = new ResponseModel(response, request, request.getItem());

                    SubscriptionCache.responses.onNext(responseModel);
                });
    }

    private void handleResponses() {
        SubscriptionCache
                .responses
                .observeOn(Schedulers.computation())
                .subscribe(response -> {
                    final CompletableFuture<List<ItemsListType>> responseFuture = within(response.getResponse(), Duration.ofSeconds(10));
                    responseFuture
                            .thenAccept(it -> {
                                SubscriptionCache.urls.onNext(response);
                                SubscriptionCache.requests.onNext(response.getRequest());
                            })
                            .exceptionally(throwable -> {
                                SubscriptionCache.requests.onNext(response.getRequest());
                                return null;
                            });
                });
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

