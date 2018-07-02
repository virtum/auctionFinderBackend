package com.filocha.finder;

import https.webapi_allegro_pl.service.DoGetItemsListRequest;
import https.webapi_allegro_pl.service.ItemsListType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AuctionFinder {

    /**
     * Creates request body by using Allegro API.
     *
     * @param itemToFind item to find in Allegro
     * @return request body, which will be used to find auctions in Allegro
     */
    DoGetItemsListRequest createRequest(String itemToFind);

    /**
     * Sends request in order to find auction in Allegro.
     *
     * @param request consists of all necessary metadata which will be send to Allegro
     * @return CompletableFuture of all auctions found in Allegro, for further processing in another thread
     */
    CompletableFuture<List<ItemsListType>> findAuctions(DoGetItemsListRequest request);
}
