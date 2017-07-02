package com.filocha;


import com.filocha.email.EmailSender;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmailSenderTest {

    @Autowired
    private EmailSender emailSender;

    @Ignore
    @Test
    public void shouldSendEmail() {
        emailSender.sendEmail("pawel.filocha@gmail.com", "test");

    }
}
