package com.filocha.storage;

import com.filocha.finder.AuctionFinder;
import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class SubscriptionServiceImpl {

    @Autowired
    private MongoOperations mongoOperations;

    @Autowired
    private AuctionFinder auctionFinder;

    //TODO Replace with synchronous queue
    private ConcurrentLinkedQueue<DoGetItemsListRequest> requests = new ConcurrentLinkedQueue<>();

    public void fillQueueWithRequest(String item) {
        DoGetItemsListRequest request = auctionFinder.createRequest(item);
        requests.add(request);
    }

    public DoGetItemsListRequest getRequestFromQueue() {
        return requests.poll();
    }

    public void saveSubscription(String email, String item) {
        SubscriberModel subscriber = new SubscriberModel(email, item);
        mongoOperations.save(subscriber);
    }

    public SubscriberModel findSubscriber(String email) {
        Query query = new Query(Criteria.where("email").is(email));
        return mongoOperations.findOne(query, SubscriberModel.class);
    }

    public List<SubscriberModel> findAllUserSubscriptions(String email) {
        Query query = new Query(Criteria.where("email").is(email));
        return mongoOperations.find(query, SubscriberModel.class);
    }

}

