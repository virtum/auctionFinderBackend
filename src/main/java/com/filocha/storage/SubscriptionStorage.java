package com.filocha.storage;

import com.filocha.finder.AuctionFinder;
import com.filocha.finder.RequestModel;
import com.filocha.finder.ResponseModel;
import com.filocha.messaging.messages.finder.ItemFinderRequestMessage;
import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.subjects.PublishSubject;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SubscriptionStorage {

    @Autowired
    private AuctionFinder auctionFinder;

    public static Map<String, Map<String, List<String>>> userAuctions = new ConcurrentHashMap<>();

    public static List<SubscriberModel1> userAuctions1 = new ArrayList<>();
    public static PublishSubject<RequestModel> requests = PublishSubject.create();
    public static PublishSubject<ResponseModel> responses = PublishSubject.create();


    public static PublishSubject<ItemFinderRequestMessage> subscriptions = PublishSubject.create();

    @PostConstruct
    private void initialize() {
        // TODO add second map for user and his items with found urls or thinkg about one for both subscriptions and found
        // TODO second thought - make userSctions as Map<String, List<AuctionModel>> then change code for checking url duplicates
        // TODO from SubscriptionServiceImpl class
        subscriptions
                .subscribe(request -> {
                    String userEmail = request.getEmail();
                    String itemName = request.getItem();

                    if (handleSubscription(userEmail, itemName)) {
                        onNextRequest(userEmail, itemName);
                    }
                });
    }

    public static boolean containsEmail(final String email) {
        return userAuctions1
                .stream()
                .map(SubscriberModel1::getEmail)
                .filter(email::equals)
                .findFirst()
                .isPresent();
    }

    public static boolean containsItem(final String email, final String itemName) {
        return userAuctions1
                .stream()
                .filter(sub -> sub.getEmail().equals(email))
                .map(SubscriberModel1::getAuctions)
                .flatMap(Collection::stream)
                .map(AuctionModel::getItemName)
                .filter(itemName::equals)
                .findFirst()
                .isPresent();
    }

    private boolean handleSubscription(String userEmail, String itemName) {
        if (!userAuctions.containsKey(userEmail)) {
            addSubscription(userEmail, itemName);
            return true;
        }
        return updateUserSubscription(userEmail, itemName);
    }

    private void addSubscription(String userEmail, String itemName) {
        Map<String, List<String>> itemsWithUrls = new HashMap<>();
        itemsWithUrls.put(itemName, new ArrayList<>());

        userAuctions.put(userEmail, itemsWithUrls);
    }

    private boolean updateUserSubscription(String userEmail, String itemName) {
        if (userAuctions.get(userEmail).containsKey(itemName)) {
            return false;
        }
        userAuctions.get(userEmail).put(itemName, new ArrayList<>());

        return true;
    }

    private void onNextRequest(String userEmail, String item) {
        DoGetItemsListRequest request = auctionFinder.createRequest(item);

        RequestModel model = new RequestModel(request, userEmail, item);

        requests.onNext(model);
    }
}

class ItemsModel {
    private String itemName;
    private List<String> urls;
}
