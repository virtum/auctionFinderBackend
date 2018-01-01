package com.filocha.storage;

import com.filocha.email.EmailSender;
import com.filocha.finder.AuctionFinder;
import com.filocha.finder.RequestModel;
import com.filocha.finder.ResponseModel;
import com.filocha.messaging.messages.finder.ItemFinderRequestMessage;
import com.filocha.throttle.ThrottleGuard;
import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import https.webapi_allegro_pl.service.ItemsListType;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import javax.annotation.PostConstruct;
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

        handleResponses();
        sendRequests();
        fillCacheWithDataFromDatabase();
    }

    public void createNewSubscription(ItemFinderRequestMessage request) {
        subscriptions.onNext(Model.createNewSubscription(request.getEmail(), request.getItem()));
    }

    private void fillCacheWithDataFromDatabase() {
        List<SubscriberModel> subscribers = RepositoryExtensions.getAllSubscribers(mongoTemplate);
        SubscriptionCache.startCache(subscriptions, subscribers, requests, auctionFinder, repository, emailSender);

        subscribers.forEach(subscriber -> subscriber
                .getAuctions()
                .forEach(auction -> {
                    DoGetItemsListRequest request = auctionFinder.createRequest(auction.getItemName());

                    RequestModel req = new RequestModel(request, subscriber.getEmail(), auction.getItemName());

                    requests.onNext(req);
                }));
    }

    private void sendRequests() {
        ThrottleGuard
                .throttle(requests, 1000, 100)
                .observeOn(Schedulers.io())
                .subscribe(request -> {
                    CompletableFuture<List<ItemsListType>> response = auctionFinder.findAuctions(request.getRequest());

                    ResponseModel responseModel = new ResponseModel(response, request, request.getItem());

                    responses.onNext(responseModel);
                });
    }

    private void handleResponses() {
        responses
                .observeOn(Schedulers.io())
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
}

