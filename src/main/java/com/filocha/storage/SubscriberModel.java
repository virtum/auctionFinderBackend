package com.filocha.storage;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "subscribers")
public class SubscriberModel {
    @Id
    private String id;
    private String email;
    private String item;

    public SubscriberModel(String email, String item) {
        this.email = email;
        this.item = item;
    }

    public String getEmail() {
        return email;
    }

    public String getItem() {
        return item;
    }

}
