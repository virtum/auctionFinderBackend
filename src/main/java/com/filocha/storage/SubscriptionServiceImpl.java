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
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private Map<String, List<Long>> userAuctions = new ConcurrentHashMap<>();

    public void fillQueueWithRequest(String item, String userEmail) {
        DoGetItemsListRequest request = auctionFinder.createRequest(item);

        RequestModel model = new RequestModel(request, userEmail);

        requests.onNext(model);
    }


    @PostConstruct
    public void sendRequets() {
        Observable<RequestModel> output = ThrottleGuard.throttle(requests, 1000, 100);
        output.observeOn(Schedulers.computation()).subscribe(request -> {
            CompletableFuture<List<ItemsListType>> response = auctionFinder.findAuctions(request.getRequest());

            ResponseModel responseModel = new ResponseModel(response, request);

            responses.onNext(responseModel);
        });
    }

    @PostConstruct
    public void handleResponses() {
        responses.observeOn(Schedulers.computation()).subscribe(response -> {
            final CompletableFuture<List<ItemsListType>> responseFuture = within(response.getResponse(), Duration.ofSeconds(10));
            responseFuture
                    .thenAccept(it -> {
                        List<Long> urls = saveAuctionId(response);
                        if (urls.size() > 0) {
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

    private List<Long> saveAuctionId(ResponseModel response) {
        try {
            List<Long> auctionsId = prepareAuctionsIdList(response);
            String userEmail = response.getUserEmail();
            if (userAuctions.containsKey(userEmail)) {
                List<Long> userAuctionsId = userAuctions.get(userEmail);

                auctionsId.forEach(i -> {
                    if (auctionsId.contains(i)) {
                        auctionsId.remove(i);
                    }
                });
                userAuctionsId.addAll(auctionsId);
                return auctionsId;
            } else {
                userAuctions.put(response.getUserEmail(), auctionsId);
                return auctionsId;
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Long> prepareAuctionsIdList(ResponseModel response) throws ExecutionException, InterruptedException {
        List<Long> auctionsId = new ArrayList<>();

        for (ItemsListType auctions : response.getResponse().get()) {
            auctionsId.add(auctions.getItemId());
        }
        return auctionsId;
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

