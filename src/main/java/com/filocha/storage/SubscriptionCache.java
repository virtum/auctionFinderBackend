package com.filocha.storage;

import com.filocha.finder.AuctionFinder;
import com.filocha.finder.RequestModel;
import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;

import java.util.*;
import java.util.stream.Collectors;

public class SubscriptionCache {

    // TODO replace void closable
    public static void startCache(Observable<Model> subscriptions, List<SubscriberModel> userAuctions,
                                  Observer<RequestModel> requests, AuctionFinder auctionFinder,
                                  PublishSubject<SubscriberModel> repository, PublishSubject<Model> emailSender) {
        subscriptions
                .subscribe(it -> {
                    if (it.isNewSubscription()) {
                        if (handleSubscription(repository, it, userAuctions)) {
                            sendRequest(auctionFinder, it, requests);
                        }
                    } else {
                        updateUrls(repository, it, userAuctions, emailSender);
                    }
                });
    }

    private static boolean handleSubscription(PublishSubject<SubscriberModel> repository, Model model, List<SubscriberModel> userAuctions) {
        Optional<SubscriberModel> subscriber = findSubscriberByEmail(model.getEmail(), userAuctions);
        if (!subscriber.isPresent()) {
            addNewSubscription(repository, model.getEmail(), model.getItem(), userAuctions);
            return true;
        }

        return updateExistingSubscription(repository, subscriber.get(), model.getItem(), userAuctions);
    }

    private static void updateUrls(PublishSubject<SubscriberModel> repository, Model model, List<SubscriberModel> userAuctions,
                                   PublishSubject<Model> emailSender) {
        SubscriberModel subscriber = findSubscriberByEmail(model.getEmail(), userAuctions)
                .orElseThrow(() -> new NoSuchElementException("Email: " + model.getEmail() + " was not found"));

        AuctionModel auctionToUpdate = getAuction(subscriber.getAuctions(), model.getItem()).
                orElseThrow(() -> new NoSuchElementException("Auctions for item:" + model.getItem() + " was not found"));

        List<String> newUrls = model.getUrls()
                .stream()
                .filter(i -> !auctionToUpdate.getUrls().contains(i))
                .collect(Collectors.toList());

        if (!newUrls.isEmpty()) {
            auctionToUpdate.getUrls().addAll(newUrls);
            repository.onNext(subscriber);
            emailSender.onNext(Model.createModelForUpdate(model.getEmail(), model.getItem(), newUrls));
        }
    }

    private static void addNewSubscription(PublishSubject<SubscriberModel> repository, String userEmail, String itemName,
                                           List<SubscriberModel> userAuctions) {
        AuctionModel auction = new AuctionModel();
        auction.setItemName(itemName);
        auction.setUrls(new HashSet<>());

        SubscriberModel subscriber = new SubscriberModel();
        subscriber.setEmail(userEmail);
        subscriber.setAuctions(new ArrayList<>(Collections.singletonList(auction)));

        userAuctions.add(subscriber);

        repository.onNext(subscriber);
    }

    private static boolean updateExistingSubscription(PublishSubject<SubscriberModel> repository, SubscriberModel subscriber,
                                                      String itemName, List<SubscriberModel> userAuctions) {
        if (getAuction(subscriber.getAuctions(), itemName).isPresent()) {
            return false;
        }

        AuctionModel newAuction = new AuctionModel();
        newAuction.setItemName(itemName);
        newAuction.setUrls(new HashSet<>());

        subscriber.getAuctions().add(newAuction);
        userAuctions.set(userAuctions.indexOf(subscriber), subscriber);

        repository.onNext(subscriber);

        return true;
    }

    private static Optional<SubscriberModel> findSubscriberByEmail(final String email, List<SubscriberModel> userAuctions) {
        return userAuctions
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

    private static void sendRequest(AuctionFinder auctionFinder, Model model, Observer<RequestModel> requests) {
        DoGetItemsListRequest request = auctionFinder.createRequest(model.getItem());

        RequestModel req = new RequestModel(request, model.getEmail(), model.getItem());

        requests.onNext(req);
    }
}
