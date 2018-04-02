package com.filocha.storage;

import com.filocha.finder.AuctionFinder;
import com.filocha.finder.RequestModel;
import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.util.*;
import java.util.stream.Collectors;

public final class SubscriptionCache {

    private final List<SubscriberModel> userAuctions = new ArrayList<>();

    public Disposable startCache(final Observable<Model> subscriptions, final Observer<RequestModel> requests,
                                 final AuctionFinder auctionFinder, final Observer<SubscriberModel> repository,
                                 final Observer<Model> emailSender) {
        return subscriptions
                .observeOn(Schedulers.computation())
                .subscribe(it -> {
                    if (it.isNewSubscription()) {
                        if (handleSubscription(repository, it)) {
                            sendRequest(auctionFinder, it, requests);
                        }
                    } else {
                        updateUrls(repository, it, emailSender);
                    }
                });
    }

    private boolean handleSubscription(final Observer<SubscriberModel> repository, final Model model) {
        final Optional<SubscriberModel> subscriber = findSubscriberByEmail(model.getEmail(), userAuctions);
        if (!subscriber.isPresent()) {
            addNewSubscription(repository, model.getEmail(), model.getItem(), userAuctions);
            return true;
        }

        return updateExistingSubscription(repository, subscriber.get(), model.getItem(), userAuctions);
    }

    private void updateUrls(final Observer<SubscriberModel> repository, final Model model, final Observer<Model> emailSender) {
        final SubscriberModel subscriber = findSubscriberByEmail(model.getEmail(), userAuctions)
                .orElseThrow(() -> new NoSuchElementException("Email: " + model.getEmail() + " was not found"));

        final AuctionModel auctionToUpdate = getAuction(subscriber.getAuctions(), model.getItem()).
                orElseThrow(() -> new NoSuchElementException("Auctions for item:" + model.getItem() + " was not found"));

        final List<String> newUrls = model.getUrls()
                .stream()
                .filter(i -> !auctionToUpdate.getUrls().contains(i))
                .collect(Collectors.toList());

        if (!newUrls.isEmpty()) {
            auctionToUpdate.getUrls().addAll(newUrls);
            repository.onNext(subscriber);
            emailSender.onNext(Model.createModelForUpdate(model.getEmail(), model.getItem(), newUrls));
        }
    }

    private void addNewSubscription(final Observer<SubscriberModel> repository, final String userEmail,
                                    final String itemName, final List<SubscriberModel> userAuctions) {
        final AuctionModel auction = AuctionModel
                .builder()
                .itemName(itemName)
                .urls(new HashSet<>())
                .build();

        final SubscriberModel subscriber = SubscriberModel
                .builder()
                .email(userEmail)
                .auctions(new ArrayList<>(Collections.singleton(auction)))
                .build();

        userAuctions.add(subscriber);

        repository.onNext(subscriber);
    }

    private boolean updateExistingSubscription(final Observer<SubscriberModel> repository, final SubscriberModel subscriber,
                                               final String itemName, final List<SubscriberModel> userAuctions) {
        if (getAuction(subscriber.getAuctions(), itemName).isPresent()) {
            return false;
        }

        final AuctionModel newAuction = AuctionModel
                .builder()
                .itemName(itemName)
                .urls(new HashSet<>())
                .build();

        subscriber.getAuctions().add(newAuction);
        userAuctions.set(userAuctions.indexOf(subscriber), subscriber);

        repository.onNext(subscriber);

        return true;
    }

    private Optional<SubscriberModel> findSubscriberByEmail(final String email, final List<SubscriberModel> userAuctions) {
        return userAuctions
                .stream()
                .filter(sub -> sub.getEmail().equals(email))
                .findFirst();
    }

    private Optional<AuctionModel> getAuction(final List<AuctionModel> auctions, final String itemName) {
        return auctions
                .stream()
                .filter(auction -> auction.getItemName().equals(itemName))
                .findFirst();
    }

    private void sendRequest(final AuctionFinder auctionFinder, final Model model, final Observer<RequestModel> requests) {
        final DoGetItemsListRequest request = auctionFinder.createRequest(model.getItem());

        requests.onNext(RequestModel
                .builder()
                .request(request)
                .userEmail(model.getEmail())
                .item(model.getItem())
                .build());
    }
}
