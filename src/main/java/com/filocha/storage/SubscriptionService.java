package com.filocha.storage;

import com.filocha.email.EmailSender;
import com.filocha.finder.AuctionFinder;
import com.filocha.finder.RequestModel;
import com.filocha.finder.ResponseModel;
import com.filocha.messaging.messages.finder.ItemFinderRequestMessage;
import com.filocha.throttle.ThrottleGuard;
import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import https.webapi_allegro_pl.service.ItemsListType;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
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
public class SubscriptionService {
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

        startCache();
        startResponseHandler();
        startRequestHandler();
        fillCacheWithDataFromDatabase();
    }

    /**
     * Emits new subscription which will be created and saved in database in another reactive stream.
     *
     * @param message contains metadata necessary to create new subscription
     */
    public void createNewSubscription(final ItemFinderRequestMessage message) {
        subscriptions.onNext(Model.createNewSubscription(message.getEmail(), message.getItem()));
    }

    /**
     * Starts subscription cache.
     */
    private void startCache() {
        final Disposable subscription = SubscriptionCache
                .startCache(subscriptions, requests, auctionFinder, repository, emailSender);
        subscriptionsDisposer.add(subscription);
    }

    /**
     * Gets all subscriptions from database end emit each of them to another reactive stream.
     */
    private void fillCacheWithDataFromDatabase() {
        RepositoryExtensions
                .getAllSubscribers(mongoTemplate)
                .forEach(subscriber -> subscriber
                        .getAuctions()
                        .forEach(auction -> {
                            final DoGetItemsListRequest request = auctionFinder.createRequest(auction.getItemName());

                            requests.onNext(RequestModel
                                    .builder()
                                    .request(request)
                                    .userEmail(subscriber.getEmail())
                                    .item(auction.getItemName())
                                    .build());
                        }));
    }

    /**
     * Sends request to Allegro using throttling to prevent exceeding Allegro's requests limit. After sending the
     * request, new CompletableFuture is emitted to another reactive stream, when will be handled after receiving
     * response from Allegro.
     * <p>
     * It is guaranteed, that this method will be invoked only once by Spring during the initialization of application.
     */
    private void startRequestHandler() {
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

    /**
     * Gets incoming response from Allegro and emits it to another reactive stream to handle update, then takes request
     * and emits it to another reactive stream to send to Allegro.
     * <p>
     * It is guaranteed, that this method will be invoked only once by Spring during the initialization of application.
     */
    private void startResponseHandler() {
        final Disposable subscription = responses
                .observeOn(Schedulers.computation())
                .subscribe(response -> within(response.getResponse(), Duration.ofSeconds(10))
                        .thenAccept(auctions -> {
                            subscriptions.onNext(Model.createModelForUpdate(response.getUserEmail(), response.getItem(), prepareAuctionsUrls(auctions)));
                            requests.onNext(response.getRequest());
                        })
                        .exceptionally(throwable -> {
                            requests.onNext(response.getRequest());
                            return null;
                        }));

        subscriptionsDisposer.add(subscription);
    }

    /**
     * Converts auctions id to proper urls.
     *
     * @param auctions list of found auctions
     * @return list of auctions urls
     */
    private static List<String> prepareAuctionsUrls(final List<ItemsListType> auctions) {
        return auctions
                .stream()
                .map(ItemsListType::getItemId)
                .map(url -> "http://allegro.pl/i" + url + ".html\n")
                .collect(Collectors.toList());
    }

    /**
     * Closes all subscriptions.
     */
    @PreDestroy
    public void disposeSubscriptions() {
        subscriptionsDisposer.dispose();
    }
}

