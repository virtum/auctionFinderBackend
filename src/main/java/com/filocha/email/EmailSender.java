package com.filocha.email;

import com.filocha.storage.Model;
import io.reactivex.subjects.PublishSubject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;

@Component
public class EmailSender {

    @Value("${mailUsername}")
    private String email;

    @Value("${mailPassword}")
    private String password;

    @Autowired
    private JavaMailSender sender;

    // TODO make this closable
    public PublishSubject<Model> createEmailSender() {
        PublishSubject<Model> subscriptions = PublishSubject.create();
        subscriptions
                .subscribe(sub -> {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setTo(sub.getEmail());
                    message.setSubject("test");
                    message.setText(prepareTestMessage(sub.getUrls()));
                    sender.send(message);
                });

        return subscriptions;
    }

    private String prepareTestMessage(final List<String> urls) {
        final StringBuilder auctions = new StringBuilder("");

        urls.forEach(auctions::append);

        return auctions.toString();
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
