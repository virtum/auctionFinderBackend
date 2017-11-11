package com.filocha.storage;

import com.filocha.finder.AuctionFinder;
import com.filocha.finder.RequestModel;
import com.filocha.finder.ResponseModel;
import com.filocha.messaging.messages.finder.ItemFinderRequestMessage;
import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import https.webapi_allegro_pl.service.ItemsListType;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.subjects.PublishSubject;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class SubscriptionStorage {

    @Autowired
    private AuctionFinder auctionFinder;

    public static Map<String, Map<String, List<String>>> userAuctions = new ConcurrentHashMap<>();

    public static List<SubscriberModel1> userAuctions1 = new ArrayList<>();
    public static PublishSubject<RequestModel> requests = PublishSubject.create();
    public static PublishSubject<ResponseModel> responses = PublishSubject.create();


    public static PublishSubject<ItemFinderRequestMessage> subscriptions = PublishSubject.create();
    public static PublishSubject<ResponseModel> urls = PublishSubject.create();

    @PostConstruct
    public void initialize() {
        subscriptions
                .subscribe(request -> {
                    String userEmail = request.getEmail();
                    String itemName = request.getItem();

                    if (handleSubscription1(userEmail, itemName)) {
                        onNextRequest(userEmail, itemName);
                    }
                });

        urls
                .subscribe(SubscriptionStorage::updateUrls);
    }

    private static void updateUrls(ResponseModel responseWithUrls) {
        SubscriberModel1 subscriber = findSubscriberByEmail(responseWithUrls.getUserEmail())
                .orElseThrow(() -> new NoSuchElementException("Email: " + responseWithUrls.getUserEmail() + " was not found"));

        List<String> urlsToUpdate = prepareAuctionsIdList(responseWithUrls);
        AuctionModel auctionToUpdate = getAuction(subscriber.getAuctions(), responseWithUrls.getItem()).
                orElseThrow(() -> new NoSuchElementException("Auctions for item:" + responseWithUrls.getItem() + " was not found"));

        //TODO send email with urls
        List<String> urlsToSend = prepareNewAuctionsUrl(auctionToUpdate.getUrls(), urlsToUpdate);

        // FIXME this update works through reference, how to do it better?
        auctionToUpdate.getUrls().addAll(urlsToUpdate);
    }

    //TODO change method to void, and send new url list if urls > 0 in email
    private static List<String> prepareNewAuctionsUrl(Set<String> currentUrls, List<String> urlsToUpdate) {
        return urlsToUpdate
                .stream()
                .filter(i -> !currentUrls.contains(i))
                .collect(Collectors.toList());
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

    private static Optional<SubscriberModel1> findSubscriberByEmail(final String email) {
        return userAuctions1
                .stream()
                .filter(sub -> sub.getEmail().equals(email))
                .findFirst();
    }

    private static Optional<AuctionModel> getAuction(final List<AuctionModel> auctions, String itemName) {
        return auctions
                .stream()
                .filter(auction -> auction.getItemName().equals(itemName))
                .findFirst();
    }

    private boolean handleSubscription1(String email, String itemName) {
        Optional<SubscriberModel1> subscriber = findSubscriberByEmail(email);
        if (!subscriber.isPresent()) {
            addSubscription1(email, itemName);
            return true;
        }

        return updateUserSubscription1(subscriber.get(), itemName);
    }

    private void addSubscription1(String userEmail, String itemName) {
        AuctionModel auction = new AuctionModel();
        auction.setItemName(itemName);
        auction.setUrls(new HashSet<>());

        SubscriberModel1 subscriber = new SubscriberModel1();
        subscriber.setEmail(userEmail);
        subscriber.setAuctions(new ArrayList<>(Collections.singletonList(auction)));

        userAuctions1.add(subscriber);
    }

    private boolean updateUserSubscription1(SubscriberModel1 subscriber, String itemName) {
        if (getAuction(subscriber.getAuctions(), itemName).isPresent()) {
            return false;
        }

        SubscriberModel1 updatedSubscriber = new SubscriberModel1();
        BeanUtils.copyProperties(subscriber, updatedSubscriber);

        AuctionModel newAuction = new AuctionModel();
        newAuction.setItemName(itemName);
        newAuction.setUrls(new HashSet<>());

        updatedSubscriber.getAuctions().add(newAuction);

        userAuctions1.set(userAuctions1.indexOf(subscriber), updatedSubscriber);

        return true;
    }

    private void onNextRequest(String userEmail, String item) {
        DoGetItemsListRequest request = auctionFinder.createRequest(item);

        RequestModel model = new RequestModel(request, userEmail, item);

        requests.onNext(model);
    }
}
