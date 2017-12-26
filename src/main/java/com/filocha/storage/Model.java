package com.filocha.storage;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Model {

    private String email;
    private String item;
    private List<String> urls;
    private boolean isNewSubscription;

    public static Model createNewSubscription(String email, String item) {
        Model subscription = new Model();
        subscription.setEmail(email);
        subscription.setItem(item);
        subscription.setUrls(new ArrayList<>());
        subscription.setNewSubscription(true);

        return subscription;
    }

    public static Model createModelForUpdate(String email, String item, List<String> urls) {
        Model toUpdate = new Model();
        toUpdate.setEmail(email);
        toUpdate.setItem(item);
        toUpdate.setUrls(urls);
        toUpdate.setNewSubscription(false);

        return toUpdate;
    }
}
