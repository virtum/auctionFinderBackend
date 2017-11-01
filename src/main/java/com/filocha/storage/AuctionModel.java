package com.filocha.storage;

import lombok.Data;

import java.util.Set;

@Data
public class AuctionModel {

    private String itemName;
    private Set<String> urls;
}
