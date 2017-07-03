package com.filocha;


import com.filocha.finder.AuctionFinder;
import com.filocha.finder.RequestModel;
import com.filocha.storage.SubscriptionServiceImpl;
import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import rx.subjects.ReplaySubject;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SubscriptionServiceTest {

    @Autowired
    private SubscriptionServiceImpl service;

    @Autowired
    private AuctionFinder finder;

    //TODO is this test make sense? If so, I need to make subjects from SubscriptionServiceImpl public, which imo
    //is stupid
    @Ignore
    @Test
    public void shouldCreateRequest() {
        ReplaySubject<RequestModel> listener = ReplaySubject.create();
        //service.requests.subscribe(listener);

        DoGetItemsListRequest item = finder.createRequest("nokia");
        RequestModel model = new RequestModel(item, "test@mail.com");

        //service.requests.onNext(model);

        List<RequestModel> results = listener.buffer(100, TimeUnit.MILLISECONDS).toBlocking().first();

        Assertions.assertThat(results).containsExactly(model);

    }
}
