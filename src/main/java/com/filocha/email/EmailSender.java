package com.filocha.email;

import com.filocha.finder.ResponseModel;
import https.webapi_allegro_pl.service.ItemsListType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class EmailSender {

    @Value("${mailUsername}")
    private String email;

    @Value("${mailPassword}")
    private String password;

    @Autowired
    public JavaMailSender emailSender;

    public void sendEmail(String userEmail, ResponseModel response) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(userEmail);
            message.setSubject("test");
            message.setText(prepareTestMessage(response));
            emailSender.send(message);
        });
    }

    private String prepareTestMessage(ResponseModel response) {
        String auctions = "";
        try {
            for (ItemsListType auction : response.getResponse().get()) {
                auctions += "http://allegro.pl/i" + auction.getItemId() + ".html\n";
            }
        } catch (InterruptedException e) {
            // TODO add logger
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO add logger
            e.printStackTrace();
        }
        return auctions;
    }

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        mailSender.setUsername(email);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
}
