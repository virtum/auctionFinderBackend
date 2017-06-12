package com.filocha;


import com.filocha.storage.SubscriptionServiceImpl;
import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SubscriptionServiceTest {

    @Autowired
    private SubscriptionServiceImpl service;

    @Test
    public void shouldCreateRequest() {
        service.fillQueueWithRequest("nokia");

        DoGetItemsListRequest request = service.getRequestFromQueue();

        assertThat(request.getFilterOptions().getItem().get(0).getFilterValueId().getItem().get(0), equalTo("nokia"));
    }

}
