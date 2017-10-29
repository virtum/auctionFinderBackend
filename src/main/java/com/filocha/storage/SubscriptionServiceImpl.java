package com.filocha.storage;

import com.filocha.email.EmailSender;
import com.filocha.finder.AuctionFinder;
import com.filocha.finder.RequestModel;
import com.filocha.finder.ResponseModel;
import com.filocha.throttle.ThrottleGuard;
import https.webapi_allegro_pl.service.ItemsListType;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.schedulers.Schedulers;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SubscriptionServiceImpl {
    @Autowired
    private AuctionFinder auctionFinder;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private SubscriberRepository repository;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public Map<String, Map<String, List<String>>> userAuctions = new ConcurrentHashMap<>();

    @PostConstruct
    private void initialize() {
        // userAuctions = repository.getUsersWithItems();
        handleResponses();
        sendRequets();
    }

    private void sendRequets() {
        Observable<RequestModel> output = ThrottleGuard.throttle(SubscriptionStorage.requests, 1000, 100);
        output.observeOn(Schedulers.computation()).subscribe(request -> {
            CompletableFuture<List<ItemsListType>> response = auctionFinder.findAuctions(request.getRequest());

            ResponseModel responseModel = new ResponseModel(response, request, request.getItem());

            SubscriptionStorage.responses.onNext(responseModel);
        });
    }

    private void handleResponses() {
        SubscriptionStorage.responses.observeOn(Schedulers.computation()).subscribe(response -> {
            final CompletableFuture<List<ItemsListType>> responseFuture = within(response.getResponse(), Duration.ofSeconds(10));
            responseFuture
                    .thenAccept(it -> {
                        List<String> urls = handleUserAuctions(response);
                        if (urls.size() > 0) {
                            repository.updateUserUrls(response.getUserEmail(), urls, response.getItem());
                            emailSender.sendEmail(response.getUserEmail(), urls);
                        }
                        SubscriptionStorage.requests.onNext(response.getRequest());
                    })
                    .exceptionally(throwable -> {
                        SubscriptionStorage.requests.onNext(response.getRequest());
                        return null;
                    });
        });
    }

    public List<String> handleUserAuctions(ResponseModel response) {
        List<String> foundUrls = prepareAuctionsIdList(response);
        String userEmail = response.getUserEmail();

        if (!userAuctions.containsKey(userEmail)) {
            userAuctions.put(userEmail, Collections.singletonMap(response.getItem(), foundUrls));
            return foundUrls;
        }
        return updateUserUrls(userEmail, response.getItem(), foundUrls);
    }

    private List<String> updateUserUrls(String userEmail, String item, List<String> newUrls) {
        List<String> currentUrls = userAuctions.get(userEmail).get(item);

        List<String> urlToAdd = newUrls
                .stream()
                .filter(i -> !currentUrls.contains(i))
                .collect(Collectors.toList());

        currentUrls.addAll(urlToAdd);
        Map<String, List<String>> newMap = Collections.singletonMap(item, currentUrls);

        userAuctions.put(userEmail, newMap);

        return urlToAdd;
    }

    @SneakyThrows
    private List<String> prepareAuctionsIdList(ResponseModel response) {
        // get() method is allowed here because we already have completed completableFuture
        return response.getResponse().get()
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

