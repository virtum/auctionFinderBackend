package com.filocha;


import com.filocha.finder.AuctionFinder;
import com.filocha.finder.RequestModel;
import com.filocha.finder.ResponseModel;
import com.filocha.storage.SubscriptionServiceImpl;
import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import https.webapi_allegro_pl.service.ItemsListType;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import rx.subjects.ReplaySubject;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SubscriptionServiceTest {

    //    @Autowired
    private SubscriptionServiceImpl service = new SubscriptionServiceImpl();

    //    @Autowired
    private AuctionFinder finder;

    //TODO is this test make sense? If so, I need to make subjects from SubscriptionServiceImpl public, which imo
    //is stupid
    @Ignore
    @Test
    public void shouldCreateRequest() {
        ReplaySubject<RequestModel> listener = ReplaySubject.create();
        //service.requests.subscribe(listener);

        String item = "nokia";
        DoGetItemsListRequest request = finder.createRequest(item);
        RequestModel model = new RequestModel(request, "test@mail.com", item);

        //service.requests.onNext(model);

        List<RequestModel> results = listener.buffer(100, TimeUnit.MILLISECONDS).toBlocking().first();

        Assertions.assertThat(results).containsExactly(model);

    }

    @Test
    public void shouldContinueLookingForAnotherAuctions() {
        // adding new urlId
        service.handleUserAuctions(prepareTestData(1));
        assertEquals(1, service.userAuctions.get("test").get("item").size());

        // adding same urlId
        service.handleUserAuctions(prepareTestData(1));
        assertEquals(1, service.userAuctions.get("test").get("item").size());

        // adding new urlId
        service.handleUserAuctions(prepareTestData(2));
        assertEquals(2, service.userAuctions.get("test").get("item").size());
    }

    private ResponseModel prepareTestData(long itemId) {
        ItemsListType item1 = new ItemsListType();
        item1.setItemId(itemId);

        CompletableFuture future = new CompletableFuture();
        future.complete(Arrays.asList(item1));

        RequestModel request = new RequestModel(null, "test", "item");

        return new ResponseModel(future, request, "item");
    }
}
