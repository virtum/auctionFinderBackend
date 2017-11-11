package com.filocha.storage;

import com.filocha.finder.AuctionFinder;
import com.filocha.finder.RequestModel;
import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import rx.subjects.PublishSubject;

import java.util.*;
import java.util.stream.Collectors;

public class SubscriptionCache {

    public static void initialize(PublishSubject<Model> subscriptions, List<SubscriberModel1> userAuctions, PublishSubject<RequestModel> requests, AuctionFinder auctionFinder) {
        subscriptions
                .subscribe(it -> {
                    if (it.isNewSubscription()) {
                        if (handleSubscription1(it, userAuctions)) {
                            onNextRequest(auctionFinder, it, requests);
                        }
                    } else {
                        updateUrls(it, userAuctions);
                    }
                });
    }

    private static void updateUrls(Model model, List<SubscriberModel1> userAuctions) {
        SubscriberModel1 subscriber = findSubscriberByEmail(model.getEmail(), userAuctions)
                .orElseThrow(() -> new NoSuchElementException("Email: " + model.getEmail() + " was not found"));

        List<String> urlsToUpdate = model.getUrls();
        AuctionModel auctionToUpdate = getAuction(subscriber.getAuctions(), model.getItem()).
                orElseThrow(() -> new NoSuchElementException("Auctions for item:" + model.getItem() + " was not found"));

        //TODO send email with urls
        List<String> urlsToSend = prepareNewAuctionsUrl(auctionToUpdate.getUrls(), urlsToUpdate);

        // FIXME this update works through reference, how to do it better?
        // TODO should not be urlToSend instead?
        auctionToUpdate.getUrls().addAll(urlsToUpdate);
    }

    //TODO change method to void, and send new url list if urls > 0 in email
    private static List<String> prepareNewAuctionsUrl(Set<String> currentUrls, List<String> urlsToUpdate) {
        return urlsToUpdate
                .stream()
                .filter(i -> !currentUrls.contains(i))
                .collect(Collectors.toList());
    }

    private static Optional<SubscriberModel1> findSubscriberByEmail(final String email, List<SubscriberModel1> userAuctions) {
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

    private static boolean handleSubscription1(Model model, List<SubscriberModel1> userAuctions) {
        Optional<SubscriberModel1> subscriber = findSubscriberByEmail(model.getEmail(), userAuctions);
        if (!subscriber.isPresent()) {
            addSubscription1(model.getEmail(), model.getItem(), userAuctions);
            return true;
        }

        return updateUserSubscription1(subscriber.get(), model.getItem(), userAuctions);
    }

    private static void addSubscription1(String userEmail, String itemName, List<SubscriberModel1> userAuctions) {
        AuctionModel auction = new AuctionModel();
        auction.setItemName(itemName);
        auction.setUrls(new HashSet<>());

        SubscriberModel1 subscriber = new SubscriberModel1();
        subscriber.setEmail(userEmail);
        subscriber.setAuctions(new ArrayList<>(Collections.singletonList(auction)));

        userAuctions.add(subscriber);
    }

    private static boolean updateUserSubscription1(SubscriberModel1 subscriber, String itemName, List<SubscriberModel1> userAuctions) {
        if (getAuction(subscriber.getAuctions(), itemName).isPresent()) {
            return false;
        }

        SubscriberModel1 updatedSubscriber = new SubscriberModel1();
        BeanUtils.copyProperties(subscriber, updatedSubscriber);

        AuctionModel newAuction = new AuctionModel();
        newAuction.setItemName(itemName);
        newAuction.setUrls(new HashSet<>());

        updatedSubscriber.getAuctions().add(newAuction);

        userAuctions.set(userAuctions.indexOf(subscriber), updatedSubscriber);

        return true;
    }

    private static void onNextRequest(AuctionFinder auctionFinder, Model model, PublishSubject<RequestModel> requests) {
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
