package com.filocha.storage;

import com.filocha.finder.AuctionFinder;
import com.filocha.finder.RequestModel;
import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;

import java.util.*;
import java.util.stream.Collectors;

public class SubscriptionCache {

    // TODO replace void closable
    public static void startCache(Observable<Model> subscriptions, List<SubscriberModel> userAuctions,
                                  Observer<RequestModel> requests, AuctionFinder auctionFinder, PublishSubject<SubscriberModel> repository) {
        subscriptions
                .subscribe(it -> {
                    if (it.isNewSubscription()) {
                        if (handleSubscription(repository, it, userAuctions)) {
                            onNextRequest(auctionFinder, it, requests);
                        }
                    } else {
                        updateUrls(repository, it, userAuctions);
                    }
                });
    }

    private static void updateUrls(PublishSubject<SubscriberModel> repository, Model model, List<SubscriberModel> userAuctions) {
        SubscriberModel subscriber = findSubscriberByEmail(model.getEmail(), userAuctions)
                .orElseThrow(() -> new NoSuchElementException("Email: " + model.getEmail() + " was not found"));

        AuctionModel auctionToUpdate = getAuction(subscriber.getAuctions(), model.getItem()).
                orElseThrow(() -> new NoSuchElementException("Auctions for item:" + model.getItem() + " was not found"));

        List<String> newUrls = prepareNewAuctionsUrl(auctionToUpdate.getUrls(), model.getUrls());
        if (!newUrls.isEmpty()) {
            auctionToUpdate.getUrls().addAll(newUrls);
            repository.onNext(subscriber);
            // onNext for email sender
        }
    }

    //TODO change method to void, and send new url list if urls > 0 in email
    private static List<String> prepareNewAuctionsUrl(Set<String> currentUrls, List<String> urlsToUpdate) {
        return urlsToUpdate
                .stream()
                .filter(i -> !currentUrls.contains(i))
                .collect(Collectors.toList());
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

    private static boolean handleSubscription(PublishSubject<SubscriberModel> repository, Model model, List<SubscriberModel> userAuctions) {
        Optional<SubscriberModel> subscriber = findSubscriberByEmail(model.getEmail(), userAuctions);
        if (!subscriber.isPresent()) {
            addSubscription(repository, model.getEmail(), model.getItem(), userAuctions);
            return true;
        }

        return updateUserSubscription(repository, subscriber.get(), model.getItem(), userAuctions);
    }

    private static void addSubscription(PublishSubject<SubscriberModel> repository, String userEmail, String itemName, List<SubscriberModel> userAuctions) {
        AuctionModel auction = new AuctionModel();
        auction.setItemName(itemName);
        auction.setUrls(new HashSet<>());

        SubscriberModel subscriber = new SubscriberModel();
        subscriber.setEmail(userEmail);
        subscriber.setAuctions(new ArrayList<>(Collections.singletonList(auction)));

        userAuctions.add(subscriber);

        repository.onNext(subscriber);
    }

    private static boolean updateUserSubscription(PublishSubject<SubscriberModel> repository, SubscriberModel subscriber, String itemName, List<SubscriberModel> userAuctions) {
        if (getAuction(subscriber.getAuctions(), itemName).isPresent()) {
            return false;
        }

        // TODO replace this code with references
        SubscriberModel updatedSubscriber = new SubscriberModel();
        BeanUtils.copyProperties(subscriber, updatedSubscriber);

        AuctionModel newAuction = new AuctionModel();
        newAuction.setItemName(itemName);
        newAuction.setUrls(new HashSet<>());

        updatedSubscriber.getAuctions().add(newAuction);
        userAuctions.set(userAuctions.indexOf(subscriber), updatedSubscriber);

        repository.onNext(updatedSubscriber);

        return true;
    }

    private static void onNextRequest(AuctionFinder auctionFinder, Model model, Observer<RequestModel> requests) {
        DoGetItemsListRequest request = auctionFinder.createRequest(model.getItem());

        RequestModel req = new RequestModel(request, model.getEmail(), model.getItem());

        requests.onNext(req);
    }
}

@Data
class Model {

    private String email;
    private String item;
    private List<String> urls;
    private boolean isNewSubscription;

    public static Model createNewSubscription(String email, String item) {
        Model subscription = new Model();
        subscription.setEmail(email);
        subscription.setItem(item);
        subscription.setUrls(new ArrayList<>());
        subscription.setNewSubscription(true);

        return subscription;
    }

    public static Model createModelForUpdate(String email, String item, List<String> urls) {
        Model toUpdate = new Model();
        toUpdate.setEmail(email);
        toUpdate.setItem(item);
        toUpdate.setUrls(urls);
        toUpdate.setNewSubscription(false);

        return toUpdate;
    }
}
