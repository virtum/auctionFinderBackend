package com.filocha.storage;

import com.filocha.email.EmailSender;
import com.filocha.finder.AuctionFinder;
import com.filocha.finder.RequestModel;
import com.filocha.finder.ResponseModel;
import com.filocha.messaging.messages.finder.ItemFinderRequestMessage;
import com.filocha.throttle.ThrottleGuard;
import https.webapi_allegro_pl.service.ItemsListType;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.filocha.storage.CompletableFutureExtension.within;

@Component
public class SubscriptionServiceImpl {
    @Autowired
    private AuctionFinder auctionFinder;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private EmailSender sender;

    private CompositeDisposable subscriptionsDisposer = new CompositeDisposable();
    private PublishSubject<RequestModel> requests;
    private PublishSubject<Model> subscriptions;
    private PublishSubject<SubscriberModel> repository;
    private PublishSubject<Model> emailSender;
    private PublishSubject<ResponseModel> responses = PublishSubject.create();

    @PostConstruct
    private void initialize() {
        requests = PublishSubject.create();
        subscriptions = PublishSubject.create();
        repository = RepositoryExtensions.updateSubscriber(mongoTemplate);
        emailSender = sender.createEmailSender();

        fillCacheWithDataFromDatabase();
        handleResponses();
        sendRequests();
    }

    public void createNewSubscription(final ItemFinderRequestMessage request) {
        subscriptions.onNext(Model.createNewSubscription(request.getEmail(), request.getItem()));
    }

    private void fillCacheWithDataFromDatabase() {
        //final List<SubscriberModel> subscribers = RepositoryExtensions.getAllSubscribers(mongoTemplate);

        final Disposable subscription = SubscriptionCache
                .startCache(subscriptions, requests, auctionFinder, repository, emailSender);
        subscriptionsDisposer.add(subscription);

//        subscribers.forEach(subscriber -> subscriber
//                .getAuctions()
//                .forEach(auction -> {
//                    final DoGetItemsListRequest request = auctionFinder.createRequest(auction.getItemName());
//
//                    requests.onNext(RequestModel
//                            .builder()
//                            .request(request)
//                            .userEmail(subscriber.getEmail())
//                            .item(auction.getItemName())
//                            .build());
//                }));
    }

    private void sendRequests() {
        final Disposable subscription = ThrottleGuard
                .throttle(requests, 1000, 100)
                .observeOn(Schedulers.computation())
                .subscribe(request -> {
                    final CompletableFuture<List<ItemsListType>> response = auctionFinder.findAuctions(request.getRequest());

                    responses.onNext(ResponseModel
                            .builder()
                            .response(response)
                            .request(request)
                            .userEmail(request.getUserEmail())
                            .item(request.getItem())
                            .build());
                });

        subscriptionsDisposer.add(subscription);
    }

    private void handleResponses() {
        final Disposable subscription = responses
                .observeOn(Schedulers.computation())
                .subscribe(response -> {
                    final CompletableFuture<List<ItemsListType>> responseFuture = within(response.getResponse(), Duration.ofSeconds(10));
                    responseFuture
                            .thenAccept(it -> {
                                subscriptions.onNext(Model.createModelForUpdate(response.getUserEmail(), response.getItem(), prepareAuctionsIdList(response)));
                                requests.onNext(response.getRequest());
                            })
                            .exceptionally(throwable -> {
                                requests.onNext(response.getRequest());
                                return null;
                            });
                });

        subscriptionsDisposer.add(subscription);
    }

    @SneakyThrows
    private static List<String> prepareAuctionsIdList(final ResponseModel responseWithUrls) {
        // get() method is allowed here because we already have completed completableFuture
        return responseWithUrls.getResponse().get()
                .stream()
                .map(ItemsListType::getItemId)
                .map(url -> "http://allegro.pl/i" + url + ".html\n")
                .collect(Collectors.toList());
    }

    @PreDestroy
    public void close() {
        subscriptionsDisposer.dispose();
    }
}

