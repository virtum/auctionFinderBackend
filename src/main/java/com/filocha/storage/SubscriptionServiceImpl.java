package com.filocha.storage;

import com.filocha.finder.AuctionFinder;
import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import https.webapi_allegro_pl.service.ItemsListType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.*;

@Component
public class SubscriptionServiceImpl {

    @Autowired
    private MongoOperations mongoOperations;

    @Autowired
    private AuctionFinder auctionFinder;

    //TODO Replace with synchronous queue
    private ConcurrentLinkedQueue<DoGetItemsListRequest> requests = new ConcurrentLinkedQueue<>();
    private CopyOnWriteArrayList<CompletableFuture<List<ItemsListType>>> responses = new CopyOnWriteArrayList<>();

    public void fillQueueWithRequest(String item) {
        DoGetItemsListRequest request = auctionFinder.createRequest(item);
        requests.add(request);
    }

    public DoGetItemsListRequest getRequestFromQueue() {
        return requests.poll();
    }

    @PostConstruct
    public void sendRequets() {
        // TODO think how many threads do I need
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (requests.size() != 0) {
                    CompletableFuture<List<ItemsListType>> response = auctionFinder.findAuctions(getRequestFromQueue());
                    responses.add(response);
                }

            }
        });
    }

    @PostConstruct
    public void handleResponses() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            while (true) {
                for (CompletableFuture<List<ItemsListType>> response : responses) {
                    response.thenAcceptAsync(val -> {
                        System.out.println("Response val: " + val.get(0).getItemTitle());
                    });
                    responses.remove(response);
                }
            }
        });
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

