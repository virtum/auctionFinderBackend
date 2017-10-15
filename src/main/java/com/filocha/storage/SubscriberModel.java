package com.filocha.storage;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Document(collection = "subscribers")
public class SubscriberModel {
    @Id
    private String id;
    private String email;
    private Map<String, List<String>> itemWithUrls;

}
