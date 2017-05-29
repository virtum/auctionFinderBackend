package com.filocha.storage;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StorageTest {

    @Autowired
    private SubscriptionServiceImpl storageConfiguration;

    @Test
    public void shouldAddAndFindUser() {
        String email = "test@gmail.com";
        String item = "nokia";
        storageConfiguration.saveSubscription(email, item);

        SubscriberModel user = storageConfiguration.findSubscriber(email);

        assertThat(user.getEmail(), equalTo(email));
        assertThat(user.getItem(), equalTo(item));
    }

}
