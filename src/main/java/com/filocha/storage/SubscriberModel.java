package com.filocha.storage;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "subscribers")
public class SubscriberModel {
    @Id
    private String id;
    private String email;
    private String item;
    private List<Long> urls;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public List<Long> getUrls() {
        return urls;
    }

    public void setUrls(List<Long> urls) {
        this.urls = urls;
    }
}
