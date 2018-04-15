package com.filocha.storage;

import com.filocha.finder.AuctionFinder;
import com.filocha.finder.RequestModel;
import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public final class SubscriptionCache {

    public static Disposable startCache(final Observable<Model> subscriptions, final Observer<RequestModel> requests,
                                        final AuctionFinder auctionFinder, final Observer<SubscriberModel> repository,
                                        final Observer<Model> emailSender) {
        final List<SubscriberModel> userAuctions = new ArrayList<>();

        return subscriptions
                .observeOn(Schedulers.computation())
                .subscribe(it -> {
                    if (it.isNewSubscription()) {
                        if (handleSubscription(it, userAuctions, repository)) {
                            sendRequest(auctionFinder, it, requests);
                        }
                    } else {
                        updateUrls(it, userAuctions, repository, emailSender);
                    }
                });
    }

    private static boolean handleSubscription(final Model model, final List<SubscriberModel> userAuctions,
                                              final Observer<SubscriberModel> repository) {
        final Optional<SubscriberModel> subscriber = findSubscriberByEmail(model.getEmail(), userAuctions);
        if (!subscriber.isPresent()) {
            addNewSubscription(repository, model.getEmail(), model.getItem(), userAuctions);
            return true;
        }

        return updateExistingSubscription(repository, subscriber.get(), model.getItem(), userAuctions);
    }

    private static void updateUrls(final Model model, final List<SubscriberModel> userAuctions,
                                   final Observer<SubscriberModel> repository, final Observer<Model> emailSender) {
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
            //emailSender.onNext(Model.createModelForUpdate(model.getEmail(), model.getItem(), newUrls));
        }
    }

    private static void addNewSubscription(final Observer<SubscriberModel> repository, final String userEmail,
                                           final String itemName, final List<SubscriberModel> userAuctions) {
        final AuctionModel auction = AuctionModel
                .builder()
                .itemName(itemName)
                .creationDate(DateTimeFormatter
                        .ofPattern("dd/MM/yyyy - HH:mm")
                        .format(ZonedDateTime.now()))
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

    private static boolean updateExistingSubscription(final Observer<SubscriberModel> repository, final SubscriberModel subscriber,
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

    private static Optional<SubscriberModel> findSubscriberByEmail(final String email, final List<SubscriberModel> userAuctions) {
        return userAuctions
                .stream()
                .filter(sub -> sub.getEmail().equals(email))
                .findFirst();
    }

    private static Optional<AuctionModel> getAuction(final List<AuctionModel> auctions, final String itemName) {
        return auctions
                .stream()
                .filter(auction -> auction.getItemName().equals(itemName))
                .findFirst();
    }

    private static void sendRequest(final AuctionFinder auctionFinder, final Model model, final Observer<RequestModel> requests) {
        final DoGetItemsListRequest request = auctionFinder.createRequest(model.getItem());

        requests.onNext(RequestModel
                .builder()
                .request(request)
                .userEmail(model.getEmail())
                .item(model.getItem())
                .build());
    }
}
