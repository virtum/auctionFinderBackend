package com.filocha;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MessagingTests {

    @Autowired
    JmsClient jsmClient;

    @Test
    public void shouldSendAndReceiveMessage() {
        String msg = "Hello world!";
        jsmClient.send(msg);
        String receivedMsg = jsmClient.receive();
        System.out.println(receivedMsg);

        assertThat(receivedMsg, equalTo(msg));
    }
}
