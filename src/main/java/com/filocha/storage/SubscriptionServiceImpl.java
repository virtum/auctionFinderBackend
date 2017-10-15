package com.filocha.storage;

import com.filocha.email.EmailSender;
import com.filocha.finder.AuctionFinder;
import com.filocha.finder.RequestModel;
import com.filocha.finder.ResponseModel;
import com.filocha.throttle.ThrottleGuard;
import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import https.webapi_allegro_pl.service.ItemsListType;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public Map<String, Map<String, List<String>>> userAuctions = new ConcurrentHashMap<>();

    public void fillQueueWithRequest(String item, String userEmail) {
        DoGetItemsListRequest request = auctionFinder.createRequest(item);

        RequestModel model = new RequestModel(request, userEmail, item);

        requests.onNext(model);
    }


    @PostConstruct
    public void sendRequets() {
        Observable<RequestModel> output = ThrottleGuard.throttle(requests, 1000, 100);
        output.observeOn(Schedulers.computation()).subscribe(request -> {
            CompletableFuture<List<ItemsListType>> response = auctionFinder.findAuctions(request.getRequest());

            ResponseModel responseModel = new ResponseModel(response, request, request.getItem());

            responses.onNext(responseModel);
        });
    }

    @PostConstruct
    public void handleResponses() {
        responses.observeOn(Schedulers.computation()).subscribe(response -> {
            final CompletableFuture<List<ItemsListType>> responseFuture = within(response.getResponse(), Duration.ofSeconds(10));
            responseFuture
                    .thenAccept(it -> {
                        List<String> urls = handleUserAuctions(response);
                        if (urls.size() > 0) {
                            updateUserUrls(response.getUserEmail(), urls, response.getItem());
                            emailSender.sendEmail(response.getUserEmail(), urls);
                        }
                        requests.onNext(response.getRequest());
                    })
                    .exceptionally(throwable -> {
                        requests.onNext(response.getRequest());
                        return null;
                    });
        });
    }

    private List<String> handleUserAuctions(ResponseModel response) {
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
        List<Long> auctionsId = new ArrayList<>();

        //FIXME get? wtf?
        auctionsId.addAll(response.getResponse().get()
                .stream()
                .map(ItemsListType::getItemId)
                .collect(Collectors.toList()));

        return prepareTestMessage(auctionsId);
    }

    private List<String> prepareTestMessage(List<Long> urlsId) {
        return urlsId
                .stream()
                .map(url -> "http://allegro.pl/i" + url + ".html\n")
                .collect(Collectors.toList());
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
        SubscriberModel subscriber = new SubscriberModel();
        subscriber.setEmail(email);
        subscriber.setItemWithUrls(Collections.singletonMap(item, new ArrayList<>()));

        mongoOperations.save(subscriber);
    }

    private void updateUserUrls(String userEmail, List<String> urls, String item) {
        Query updateQuery = Query.query(Criteria.where("email").is(userEmail));
        SubscriberModel subscriber = mongoOperations.findOne(updateQuery, SubscriberModel.class);

        List<String> links = subscriber.getItemWithUrls().get(item);
        links.addAll(urls);

        Map<String, List<String>> updatedMap = subscriber.getItemWithUrls();
        updatedMap.put(item, links);

        subscriber.setItemWithUrls(updatedMap);
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

