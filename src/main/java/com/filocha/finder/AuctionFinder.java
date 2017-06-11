package com.filocha.finder;

import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import https.webapi_allegro_pl.service.ItemsListType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AuctionFinder {

    DoGetItemsListRequest createRequest(String keyword);

    CompletableFuture<List<ItemsListType>> findAuctions(DoGetItemsListRequest request);
}
