package com.filocha.storage;

import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Value
@Builder
// TODO rename this model
public class Model {

    private String email;
    private String item;
    private List<String> urls;
    private boolean isNewSubscription;

    public static Model createNewSubscription(final String email, final String item) {
        return Model
                .builder()
                .email(email)
                .item(item)
                .urls(new ArrayList<>())
                .isNewSubscription(true)
                .build();
    }

    public static Model createModelForUpdate(final String email, final String item, final List<String> urls) {
        return Model
                .builder()
                .email(email)
                .item(item)
                .urls(urls)
                .isNewSubscription(false)
                .build();
    }
}
