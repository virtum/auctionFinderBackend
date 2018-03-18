package com.filocha.storage;

import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Value
@Builder
public class AuctionModel {

    private String itemName;
    private Set<String> urls;
}
