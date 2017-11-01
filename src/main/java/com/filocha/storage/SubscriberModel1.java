package com.filocha.storage;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
public class SubscriberModel1 {

    @Id
    private String id;
    private String email;
    private List<AuctionModel> auctions;
}
