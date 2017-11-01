package com.filocha.storage;

import lombok.Data;

import java.util.List;

@Data
public class AuctionModel {

    private String itemName;
    private List<String> urls;
}
