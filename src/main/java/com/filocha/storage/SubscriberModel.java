package com.filocha.storage;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Value
@Builder
@Document(collection = "subscribers")
public class SubscriberModel {

    @Id
    private String id;
    private String email;
    private List<AuctionModel> auctions;
}
