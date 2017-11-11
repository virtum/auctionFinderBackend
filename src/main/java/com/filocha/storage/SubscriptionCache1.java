package com.filocha.storage;

import com.filocha.finder.AuctionFinder;
import com.filocha.finder.RequestModel;
import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import rx.subjects.PublishSubject;

import java.util.*;
import java.util.stream.Collectors;

public class SubscriptionCache1 {

    private List<SubscriberModel1> userAuctions;
    private PublishSubject<RequestModel> requests;
    private AuctionFinder auctionFinder;
    public PublishSubject<Model> subscriptions = PublishSubject.create();

    public SubscriptionCache1(List<SubscriberModel1> userAuctions, PublishSubject<RequestModel> requests, AuctionFinder auctionFinder) {
        this.userAuctions = userAuctions;
        this.requests = requests;
        this.auctionFinder = auctionFinder;

        initialize();
    }

    private void initialize() {
        subscriptions
                .subscribe(it -> {
                    if (it.isNewSubscription()) {
                        if (handleSubscription1(it)) {
                            onNextRequest(auctionFinder, it);
                        }
                    } else {
                        updateUrls(it);
                    }
                });
    }

    private void updateUrls(Model model) {
        SubscriberModel1 subscriber = findSubscriberByEmail(model.getEmail())
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
    private List<String> prepareNewAuctionsUrl(Set<String> currentUrls, List<String> urlsToUpdate) {
        return urlsToUpdate
                .stream()
                .filter(i -> !currentUrls.contains(i))
                .collect(Collectors.toList());
    }

    private Optional<SubscriberModel1> findSubscriberByEmail(final String email) {
        return userAuctions
                .stream()
                .filter(sub -> sub.getEmail().equals(email))
                .findFirst();
    }

    private Optional<AuctionModel> getAuction(final List<AuctionModel> auctions, String itemName) {
        return auctions
                .stream()
                .filter(auction -> auction.getItemName().equals(itemName))
                .findFirst();
    }

    private boolean handleSubscription1(Model model) {
        Optional<SubscriberModel1> subscriber = findSubscriberByEmail(model.getEmail());
        if (!subscriber.isPresent()) {
            addSubscription1(model.getEmail(), model.getItem());
            return true;
        }

        return updateUserSubscription1(subscriber.get(), model.getItem());
    }

    private void addSubscription1(String userEmail, String itemName) {
        AuctionModel auction = new AuctionModel();
        auction.setItemName(itemName);
        auction.setUrls(new HashSet<>());

        SubscriberModel1 subscriber = new SubscriberModel1();
        subscriber.setEmail(userEmail);
        subscriber.setAuctions(new ArrayList<>(Collections.singletonList(auction)));

        userAuctions.add(subscriber);
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

        userAuctions.set(userAuctions.indexOf(subscriber), updatedSubscriber);

        return true;
    }

    private void onNextRequest(AuctionFinder auctionFinder, Model model) {
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
