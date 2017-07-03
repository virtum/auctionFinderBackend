package com.filocha;


import com.filocha.finder.RequestModel;
import com.filocha.storage.SubscriptionServiceImpl;
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

//    @Test
//    public void shouldCreateRequest() {
//        service.fillQueueWithRequest("nokia", "test@mail.com");
//
//        RequestModel request = service.getRequestFromQueue();
//
//        assertThat(request.getRequest().getFilterOptions().getItem().get(0).getFilterValueId().getItem().get(0), equalTo("nokia"));
//    }

}
